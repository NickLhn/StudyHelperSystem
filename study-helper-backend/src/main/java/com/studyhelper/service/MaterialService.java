package com.studyhelper.service;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.GeneratePresignedUrlRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.studyhelper.config.OssClientProvider;
import com.studyhelper.dto.CommentDTO;
import com.studyhelper.dto.MaterialDTO;
import com.studyhelper.entity.*;
import com.studyhelper.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class MaterialService {

    @Autowired
    private MaterialRepository materialRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private StudentCourseRepository studentCourseRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private UserMaterialActionRepository userMaterialActionRepository;

    @Autowired
    private OssClientProvider ossClientProvider;

    @Value("${file.upload.path}")
    private String uploadPath;

    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(
        ".pdf", ".docx", ".pptx", ".jpg", ".jpeg", ".png"
    );
    private final ObjectMapper objectMapper = new ObjectMapper();

    public MaterialDTO uploadMaterial(Long userId,
                                      Long courseId,
                                      String name,
                                      String description,
                                      String category,
                                      String tags,
                                      String versionLabel,
                                      String versionNote,
                                      MultipartFile file) {
        // 验证文件
        validateFile(file);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        String filePath = saveFile(file, userId, courseId);

        Material material = new Material();
        material.setName(name);
        material.setFileName(file.getOriginalFilename());
        material.setFileType(getFileExtension(file.getOriginalFilename()));
        material.setFileSize(file.getSize());
        material.setFilePath(filePath);
        material.setDescription(description);
        material.setCategory(normalizeNullable(category));
        material.setTags(writeTags(tags));
        material.setVersionLabel(normalizeNullable(versionLabel));
        material.setVersionNote(normalizeNullable(versionNote));
        material.setUser(user);

        if (courseId != null) {
            Course course = courseRepository.findById(courseId)
                    .orElseThrow(() -> new RuntimeException("课程不存在"));
            if (!course.getUser().getId().equals(userId)) {
                throw new RuntimeException("无权关联该课程");
            }
            material.setCourse(course);
        }

        Material savedMaterial = materialRepository.save(material);
        return MaterialDTO.fromMaterial(savedMaterial);
    }

    private String writeTags(String rawTags) {
        List<String> parsedTags = parseTags(rawTags);
        try {
            return parsedTags.isEmpty() ? "[]" : objectMapper.writeValueAsString(parsedTags);
        } catch (Exception e) {
            return "[]";
        }
    }

    private List<String> parseTags(String rawTags) {
        if (rawTags == null || rawTags.isBlank()) {
            return List.of();
        }

        return Arrays.stream(rawTags.split("[,，]"))
                .map(String::trim)
                .filter(tag -> !tag.isBlank())
                .distinct()
                .limit(8)
                .collect(Collectors.toList());
    }

    private String normalizeNullable(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new RuntimeException("文件不能为空");
        }

        String extension = getFileExtension(file.getOriginalFilename()).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new RuntimeException("不支持的文件类型，仅支持: " + String.join(", ", ALLOWED_EXTENSIONS));
        }

        if (file.getSize() > 30 * 1024 * 1024) {
            throw new RuntimeException("文件大小不能超过30MB");
        }
    }

    private String saveFile(MultipartFile file, Long userId, Long courseId) {
        if (ossClientProvider.isAvailable()) {
            return saveToOss(file, userId, courseId);
        }
        try {
            String extension = getFileExtension(file.getOriginalFilename());
            String newFileName = UUID.randomUUID().toString() + extension;
            
            Path uploadDir = Paths.get(uploadPath);
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            Path filePath = uploadDir.resolve(newFileName);
            Files.write(filePath, file.getBytes());
            
            return newFileName;
        } catch (IOException e) {
            throw new RuntimeException("文件保存失败: " + e.getMessage());
        }
    }

    private String saveToOss(MultipartFile file, Long userId, Long courseId) {
        try {
            OSS oss = ossClientProvider.getOss();
            String bucket = ossClientProvider.getProperties().getBucket();
            String prefix = ossClientProvider.getProperties().getPrefix();
            if (prefix == null) prefix = "";
            if (!prefix.isEmpty() && !prefix.endsWith("/")) prefix = prefix + "/";

            String extension = getFileExtension(file.getOriginalFilename());
            String newFileName = UUID.randomUUID().toString() + extension;
            String scope = courseId == null ? "common" : ("course-" + courseId);
            String objectKey = prefix + "materials/" + scope + "/u-" + userId + "/" + newFileName;

            oss.putObject(bucket, objectKey, file.getInputStream());
            return objectKey;
        } catch (Exception e) {
            throw new RuntimeException("OSS上传失败: " + e.getMessage());
        }
    }

    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.lastIndexOf('.') == -1) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf('.')).toLowerCase();
    }

    @Transactional(readOnly = true)
    public List<MaterialDTO> getAllMaterials(Long userId) {
        List<Material> materials = materialRepository.findAccessibleMaterials(userId);
        return materials.stream()
                .map(this::enrichMaterialWithCounts)
                .map(MaterialDTO::fromMaterial)
                .peek(dto -> enrichUserActions(dto, userId))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MaterialDTO> getMaterialsByCourse(Long courseId, Long userId) {
        List<Material> materials = materialRepository.findByCourseId(courseId);
        return materials.stream()
                .filter(m -> m.getUser().getId().equals(userId) || 
                           (m.getCourse() != null && m.getCourse().getUser().getId().equals(userId)) ||
                           (m.getCourse() != null && studentCourseRepository.existsByStudentIdAndCourseId(userId, m.getCourse().getId())))
                .map(this::enrichMaterialWithCounts)
                .map(MaterialDTO::fromMaterial)
                .peek(dto -> enrichUserActions(dto, userId))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public MaterialDTO getMaterialById(Long materialId, Long userId) {
        Material material = getAccessibleMaterial(materialId, userId);
        Material enriched = enrichMaterialWithCounts(material);
        MaterialDTO dto = MaterialDTO.fromMaterial(enriched);
        enrichUserActions(dto, userId);
        return dto;
    }

    public void incrementDownloadCount(Long materialId) {
        Material material = materialRepository.findById(materialId)
                .orElseThrow(() -> new RuntimeException("资料不存在"));
        material.setDownloadCount(material.getDownloadCount() + 1);
        materialRepository.save(material);
    }

    @Transactional
    public MaterialDTO toggleLike(Long materialId, Long userId) {
        Material material = getAccessibleMaterial(materialId, userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        UserMaterialAction.ActionType actionType = UserMaterialAction.ActionType.LIKE;
        boolean exists = userMaterialActionRepository.existsByUserIdAndMaterialIdAndActionType(userId, materialId, actionType);

        if (exists) {
            // 取消点赞
            userMaterialActionRepository.findByUserIdAndMaterialIdAndActionType(userId, materialId, actionType)
                    .ifPresent(userMaterialActionRepository::delete);
            material.setLikeCount(Math.max(0, material.getLikeCount() - 1));
        } else {
            // 点赞
            UserMaterialAction action = new UserMaterialAction();
            action.setUser(user);
            action.setMaterial(material);
            action.setActionType(actionType);
            userMaterialActionRepository.save(action);
            material.setLikeCount(material.getLikeCount() + 1);
        }

        Material updated = materialRepository.save(material);
        MaterialDTO dto = MaterialDTO.fromMaterial(enrichMaterialWithCounts(updated));
        enrichUserActions(dto, userId);
        return dto;
    }

    @Transactional
    public MaterialDTO toggleFavorite(Long materialId, Long userId) {
        Material material = getAccessibleMaterial(materialId, userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        UserMaterialAction.ActionType actionType = UserMaterialAction.ActionType.FAVORITE;
        boolean exists = userMaterialActionRepository.existsByUserIdAndMaterialIdAndActionType(userId, materialId, actionType);

        if (exists) {
            // 取消收藏
            userMaterialActionRepository.findByUserIdAndMaterialIdAndActionType(userId, materialId, actionType)
                    .ifPresent(userMaterialActionRepository::delete);
            material.setFavoriteCount(Math.max(0, material.getFavoriteCount() - 1));
        } else {
            // 收藏
            UserMaterialAction action = new UserMaterialAction();
            action.setUser(user);
            action.setMaterial(material);
            action.setActionType(actionType);
            userMaterialActionRepository.save(action);
            material.setFavoriteCount(material.getFavoriteCount() + 1);
        }

        Material updated = materialRepository.save(material);
        MaterialDTO dto = MaterialDTO.fromMaterial(enrichMaterialWithCounts(updated));
        enrichUserActions(dto, userId);
        return dto;
    }

    public void deleteMaterial(Long materialId, Long userId) {
        Material material = materialRepository.findById(materialId)
                .orElseThrow(() -> new RuntimeException("资料不存在"));

        if (!material.getUser().getId().equals(userId)) {
            throw new RuntimeException("无权删除该资料");
        }

        // 删除物理文件
        deletePhysicalFile(material);

        materialRepository.delete(material);
    }


    public String generateDownloadUrl(Long materialId, Long userId) {
        if (!ossClientProvider.isAvailable()) return null;

        Material material = materialRepository.findById(materialId)
                .orElseThrow(() -> new RuntimeException("资料不存在"));
        getMaterialById(materialId, userId);

        String bucket = ossClientProvider.getProperties().getBucket();
        GeneratePresignedUrlRequest req = new GeneratePresignedUrlRequest(bucket, material.getFilePath());
        req.setExpiration(new Date(System.currentTimeMillis() + 10 * 60 * 1000));
        req.getResponseHeaders().setContentDisposition("attachment; filename=\"" + material.getFileName() + "\"");
        return ossClientProvider.getOss().generatePresignedUrl(req).toString();
    }

    private void deletePhysicalFile(Material material) {
        if (ossClientProvider.isAvailable()) {
            try {
                ossClientProvider.getOss().deleteObject(ossClientProvider.getProperties().getBucket(), material.getFilePath());
            } catch (Exception e) {
            }
            return;
        }
        try {
            Path filePath = Paths.get(uploadPath).resolve(material.getFilePath());
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
        }
    }

    // 评论相关方法
    @Transactional(readOnly = true)
    public List<CommentDTO> getCommentsByMaterial(Long materialId, Long userId) {
        getAccessibleMaterial(materialId, userId);
        List<Comment> comments = commentRepository.findByMaterialIdOrderByCreatedAtDesc(materialId);
        return comments.stream()
                .map(CommentDTO::fromComment)
                .collect(Collectors.toList());
    }

    public CommentDTO addComment(Long materialId, Long userId, String content) {
        Material material = getAccessibleMaterial(materialId, userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        if (content == null || content.trim().isEmpty()) {
            throw new RuntimeException("评论内容不能为空");
        }

        Comment comment = new Comment();
        comment.setContent(content.trim());
        comment.setUser(user);
        comment.setMaterial(material);

        Comment saved = commentRepository.save(comment);
        return CommentDTO.fromComment(saved);
    }

    private Material enrichMaterialWithCounts(Material material) {
        Long materialId = material.getId();
        material.setLikeCount((int) userMaterialActionRepository.countByMaterialIdAndActionType(
                materialId, UserMaterialAction.ActionType.LIKE));
        material.setFavoriteCount((int) userMaterialActionRepository.countByMaterialIdAndActionType(
                materialId, UserMaterialAction.ActionType.FAVORITE));
        material.setCommentCount((int) commentRepository.countByMaterialId(materialId));
        return material;
    }

    private void enrichUserActions(MaterialDTO dto, Long userId) {
        dto.setLikedByCurrentUser(userMaterialActionRepository.existsByUserIdAndMaterialIdAndActionType(
                userId, dto.getId(), UserMaterialAction.ActionType.LIKE));
        dto.setFavoritedByCurrentUser(userMaterialActionRepository.existsByUserIdAndMaterialIdAndActionType(
                userId, dto.getId(), UserMaterialAction.ActionType.FAVORITE));
    }

    private Material getAccessibleMaterial(Long materialId, Long userId) {
        Material material = materialRepository.findById(materialId)
                .orElseThrow(() -> new RuntimeException("资料不存在"));

        boolean hasAccess = material.getUser().getId().equals(userId) ||
                (material.getCourse() != null && material.getCourse().getUser().getId().equals(userId)) ||
                (material.getCourse() != null && studentCourseRepository.existsByStudentIdAndCourseId(userId, material.getCourse().getId()));

        if (!hasAccess) {
            throw new RuntimeException("无权访问该资料");
        }
        return material;
    }
}
