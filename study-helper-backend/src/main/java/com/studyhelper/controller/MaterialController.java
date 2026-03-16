package com.studyhelper.controller;

import com.studyhelper.dto.ApiResponse;
import com.studyhelper.dto.CommentDTO;
import com.studyhelper.dto.MaterialDTO;
import com.studyhelper.service.MaterialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URLEncoder;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/api/material")
public class MaterialController {

    @Autowired
    private MaterialService materialService;

    @Value("${file.upload.path}")
    private String uploadPath;

    @PostMapping("/upload")
    public ApiResponse<MaterialDTO> uploadMaterial(
            @RequestParam Long userId,
            @RequestParam(required = false) Long courseId,
            @RequestParam String name,
            @RequestParam(required = false) String description,
            @RequestParam("file") MultipartFile file) {
        try {
            MaterialDTO material = materialService.uploadMaterial(userId, courseId, name, description, file);
            return ApiResponse.success("资料上传成功", material);
        } catch (RuntimeException e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }

    @GetMapping("/list")
    public ApiResponse<List<MaterialDTO>> getAllMaterials(@RequestParam Long userId) {
        try {
            List<MaterialDTO> materials = materialService.getAllMaterials(userId);
            return ApiResponse.success(materials);
        } catch (RuntimeException e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }

    @GetMapping("/list-by-course")
    public ApiResponse<List<MaterialDTO>> getMaterialsByCourse(
            @RequestParam Long courseId,
            @RequestParam Long userId) {
        try {
            List<MaterialDTO> materials = materialService.getMaterialsByCourse(courseId, userId);
            return ApiResponse.success(materials);
        } catch (RuntimeException e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }

    @GetMapping("/{materialId}")
    public ApiResponse<MaterialDTO> getMaterialById(
            @PathVariable Long materialId,
            @RequestParam Long userId) {
        try {
            MaterialDTO material = materialService.getMaterialById(materialId, userId);
            return ApiResponse.success(material);
        } catch (RuntimeException e) {
            return ApiResponse.error(404, e.getMessage());
        }
    }

    @GetMapping("/{materialId}/download")
    public ResponseEntity<Resource> downloadMaterial(
            @PathVariable Long materialId,
            @RequestParam Long userId) {
        try {
            MaterialDTO material = materialService.getMaterialById(materialId, userId);
            
            // 增加下载次数
            materialService.incrementDownloadCount(materialId);

            String url = materialService.generateDownloadUrl(materialId, userId);
            if (url != null && !url.isBlank()) {
                return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(url)).build();
            }

            // 构造文件路径
            Path filePath = Paths.get(uploadPath).resolve(material.getFilePath());
            File file = filePath.toFile();

            if (!file.exists()) {
                return ResponseEntity.notFound().build();
            }

            InputStreamResource resource = new InputStreamResource(new FileInputStream(file));

            String encodedFileName = URLEncoder.encode(material.getFileName(), StandardCharsets.UTF_8);
            
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                            "attachment; filename*=UTF-8''" + encodedFileName)
                    .body(resource);

        } catch (FileNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/{materialId}/like")
    public ApiResponse<MaterialDTO> toggleLike(
            @PathVariable Long materialId,
            @RequestParam Long userId) {
        try {
            MaterialDTO material = materialService.toggleLike(materialId, userId);
            String message = material.isLikedByCurrentUser() ? "点赞成功" : "取消点赞";
            return ApiResponse.success(message, material);
        } catch (RuntimeException e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }

    @PostMapping("/{materialId}/favorite")
    public ApiResponse<MaterialDTO> toggleFavorite(
            @PathVariable Long materialId,
            @RequestParam Long userId) {
        try {
            MaterialDTO material = materialService.toggleFavorite(materialId, userId);
            String message = material.isFavoritedByCurrentUser() ? "收藏成功" : "取消收藏";
            return ApiResponse.success(message, material);
        } catch (RuntimeException e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }

    @DeleteMapping("/{materialId}")
    public ApiResponse<String> deleteMaterial(
            @PathVariable Long materialId,
            @RequestParam Long userId) {
        try {
            materialService.deleteMaterial(materialId, userId);
            return ApiResponse.success("资料删除成功", null);
        } catch (RuntimeException e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }

    // 评论相关接口
    @GetMapping("/{materialId}/comments")
    public ApiResponse<List<CommentDTO>> getComments(@PathVariable Long materialId) {
        try {
            List<CommentDTO> comments = materialService.getCommentsByMaterial(materialId);
            return ApiResponse.success(comments);
        } catch (RuntimeException e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }

    @PostMapping("/{materialId}/comment")
    public ApiResponse<CommentDTO> addComment(
            @PathVariable Long materialId,
            @RequestParam Long userId,
            @RequestParam String content) {
        try {
            CommentDTO comment = materialService.addComment(materialId, userId, content);
            return ApiResponse.success("评论发表成功", comment);
        } catch (RuntimeException e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }
}
