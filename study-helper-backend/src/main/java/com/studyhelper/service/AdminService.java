package com.studyhelper.service;

import com.studyhelper.dto.AdminStatsDTO;
import com.studyhelper.dto.UserDTO;
import com.studyhelper.entity.Course;
import com.studyhelper.entity.InvitationCode;
import com.studyhelper.entity.Material;
import com.studyhelper.entity.User;
import com.studyhelper.repository.InvitationCodeRepository;
import com.studyhelper.repository.CourseRepository;
import com.studyhelper.repository.MaterialRepository;
import com.studyhelper.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AdminService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private MaterialRepository materialRepository;

    @Autowired
    private InvitationCodeRepository invitationCodeRepository;

    /**
     * 获取管理员仪表盘统计数据
     */
    public AdminStatsDTO getAdminStats() {
        AdminStatsDTO stats = new AdminStatsDTO();
        
        // 总统计
        stats.setTotalUsers(userRepository.count());
        stats.setTotalCourses(courseRepository.count());
        stats.setTotalMaterials(materialRepository.count());
        
        // 今日新增统计
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = LocalDate.now().atTime(23, 59, 59);
        
        stats.setTodayNewUsers(userRepository.countByCreatedAtBetween(todayStart, todayEnd));
        stats.setTodayNewCourses(courseRepository.countByCreatedAtBetween(todayStart, todayEnd));
        stats.setTodayNewMaterials(materialRepository.countByCreatedAtBetween(todayStart, todayEnd));
        
        // 角色统计
        stats.setStudentCount(userRepository.countByRole(User.Role.STUDENT));
        stats.setTeacherCount(userRepository.countByRole(User.Role.TEACHER));
        stats.setAdminCount(userRepository.countByRole(User.Role.ADMIN));
        
        return stats;
    }

    /**
     * 获取所有用户列表
     */
    public Map<String, Object> getAllUsers(String keyword, String role, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<User> userPage;
        
        User.Role userRole = null;
        if (role != null && !role.isEmpty()) {
            try {
                userRole = User.Role.valueOf(role.toUpperCase());
            } catch (IllegalArgumentException e) {
                // 忽略无效的角色参数
            }
        }
        
        if (keyword != null && !keyword.isEmpty()) {
            userPage = userRepository.searchUsers(keyword, userRole, pageable);
        } else if (userRole != null) {
            userPage = userRepository.findByRole(userRole, pageable);
        } else {
            userPage = userRepository.findAll(pageable);
        }
        
        List<UserDTO> userDTOs = userPage.getContent().stream()
                .map(UserDTO::fromUser)
                .collect(Collectors.toList());
                
        Map<String, Object> result = new HashMap<>();
        result.put("content", userDTOs);
        result.put("totalPages", userPage.getTotalPages());
        result.put("totalElements", userPage.getTotalElements());
        
        return result;
    }

    /**
     * 获取所有课程列表
     */
    public Map<String, Object> getAllCourses(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Course> coursePage = courseRepository.findAll(pageable);
        
        List<Map<String, Object>> courseList = coursePage.getContent().stream().map(course -> {
            Map<String, Object> courseMap = new HashMap<>();
            courseMap.put("id", course.getId());
            courseMap.put("name", course.getName());
            courseMap.put("category", course.getCategory());
            courseMap.put("teacher", course.getTeacher());
            courseMap.put("userId", course.getUser().getId());
            courseMap.put("username", course.getUser().getUsername());
            courseMap.put("createdAt", course.getCreatedAt());
            return courseMap;
        }).collect(Collectors.toList());
        
        Map<String, Object> result = new HashMap<>();
        result.put("content", courseList);
        result.put("totalPages", coursePage.getTotalPages());
        result.put("totalElements", coursePage.getTotalElements());
        
        return result;
    }

    /**
     * 获取所有资料列表
     */
    public Map<String, Object> getAllMaterials(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Material> materialPage = materialRepository.findAll(pageable);
        
        List<Map<String, Object>> materialList = materialPage.getContent().stream().map(material -> {
            Map<String, Object> materialMap = new HashMap<>();
            materialMap.put("id", material.getId());
            materialMap.put("name", material.getName());
            materialMap.put("fileName", material.getFileName());
            materialMap.put("fileType", material.getFileType());
            materialMap.put("fileSize", material.getFileSize());
            materialMap.put("userId", material.getUser().getId());
            materialMap.put("username", material.getUser().getUsername());
            materialMap.put("courseId", material.getCourse() != null ? material.getCourse().getId() : null);
            materialMap.put("courseName", material.getCourse() != null ? material.getCourse().getName() : null);
            materialMap.put("downloadCount", material.getDownloadCount());
            materialMap.put("createdAt", material.getCreatedAt());
            return materialMap;
        }).collect(Collectors.toList());
        
        Map<String, Object> result = new HashMap<>();
        result.put("content", materialList);
        result.put("totalPages", materialPage.getTotalPages());
        result.put("totalElements", materialPage.getTotalElements());
        
        return result;
    }

    /**
     * 删除用户
     */
    public void deleteUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("用户不存在");
        }
        userRepository.deleteById(userId);
    }

    /**
     * 更新用户角色
     */
    public UserDTO updateUserRole(Long userId, String newRole) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        try {
            User.Role role = User.Role.valueOf(newRole.toUpperCase());
            user.setRole(role);
            User updatedUser = userRepository.save(user);
            return UserDTO.fromUser(updatedUser);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("无效的角色: " + newRole);
        }
    }

    /**
     * 生成教师邀请码
     */
    public Map<String, Object> generateInvitationCode(String code, String description) {
        if (code == null || code.trim().isEmpty()) {
            code = "TEACHER_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        }
        
        if (invitationCodeRepository.existsByCode(code)) {
            throw new RuntimeException("邀请码已存在");
        }
        
        InvitationCode invitationCode = new InvitationCode();
        invitationCode.setCode(code);
        invitationCode.setDescription(description != null ? description : "教师邀请码");
        
        InvitationCode savedCode = invitationCodeRepository.save(invitationCode);
        
        Map<String, Object> result = new HashMap<>();
        result.put("id", savedCode.getId());
        result.put("code", savedCode.getCode());
        result.put("description", savedCode.getDescription());
        result.put("createdAt", savedCode.getCreatedAt());
        result.put("expiresAt", savedCode.getExpiresAt());
        result.put("used", savedCode.getIsUsed());
        
        return result;
    }

    /**
     * 获取所有邀请码
     */
    public List<Map<String, Object>> getAllInvitationCodes() {
        List<InvitationCode> codes = invitationCodeRepository.findAll(Sort.by("createdAt").descending());
        
        return codes.stream().map(code -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", code.getId());
            map.put("code", code.getCode());
            map.put("description", code.getDescription());
            map.put("createdAt", code.getCreatedAt());
            map.put("expiresAt", code.getExpiresAt());
            map.put("used", code.getIsUsed());
            map.put("usedBy", code.getUsedByUserId());
            map.put("usedAt", code.getUsedAt());
            return map;
        }).collect(Collectors.toList());
    }

    /**
     * 删除邀请码
     */
    public void deleteInvitationCode(Long id) {
        if (!invitationCodeRepository.existsById(id)) {
            throw new RuntimeException("邀请码不存在");
        }
        invitationCodeRepository.deleteById(id);
    }
}