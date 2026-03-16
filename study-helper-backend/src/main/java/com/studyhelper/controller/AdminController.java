package com.studyhelper.controller;

import com.studyhelper.dto.AdminStatsDTO;
import com.studyhelper.dto.ApiResponse;
import com.studyhelper.dto.UserDTO;
import com.studyhelper.entity.User;
import com.studyhelper.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    /**
     * 获取管理员仪表盘统计数据
     */
    @GetMapping("/stats")
    public ApiResponse<AdminStatsDTO> getAdminStats() {
        try {
            AdminStatsDTO stats = adminService.getAdminStats();
            return ApiResponse.success("获取统计数据成功", stats);
        } catch (Exception e) {
            return ApiResponse.error(500, "获取统计数据失败: " + e.getMessage());
        }
    }

    /**
     * 获取所有用户列表
     */
    @GetMapping("/users")
    public ApiResponse<Map<String, Object>> getAllUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            Map<String, Object> result = adminService.getAllUsers(keyword, role, page, size);
            return ApiResponse.success("获取用户列表成功", result);
        } catch (Exception e) {
            return ApiResponse.error(500, "获取用户列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取所有课程列表
     */
    @GetMapping("/courses")
    public ApiResponse<Map<String, Object>> getAllCourses(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            Map<String, Object> result = adminService.getAllCourses(page, size);
            return ApiResponse.success("获取课程列表成功", result);
        } catch (Exception e) {
            return ApiResponse.error(500, "获取课程列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取所有资料列表
     */
    @GetMapping("/materials")
    public ApiResponse<Map<String, Object>> getAllMaterials(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            Map<String, Object> result = adminService.getAllMaterials(page, size);
            return ApiResponse.success("获取资料列表成功", result);
        } catch (Exception e) {
            return ApiResponse.error(500, "获取资料列表失败: " + e.getMessage());
        }
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/users/{userId}")
    public ApiResponse<Void> deleteUser(@PathVariable Long userId) {
        try {
            adminService.deleteUser(userId);
            return ApiResponse.success("用户删除成功", null);
        } catch (Exception e) {
            return ApiResponse.error(500, "删除用户失败: " + e.getMessage());
        }
    }

    /**
     * 更新用户角色
     */
    @PutMapping("/users/{userId}/role")
    public ApiResponse<UserDTO> updateUserRole(@PathVariable Long userId, @RequestBody Map<String, String> request) {
        try {
            String newRole = request.get("role");
            UserDTO updatedUser = adminService.updateUserRole(userId, newRole);
            return ApiResponse.success("用户角色更新成功", updatedUser);
        } catch (Exception e) {
            return ApiResponse.error(500, "更新用户角色失败: " + e.getMessage());
        }
    }

    /**
     * 生成教师邀请码
     */
    @PostMapping("/invitation-codes")
    public ApiResponse<Map<String, Object>> generateInvitationCode(@RequestBody Map<String, Object> request) {
        try {
            String code = (String) request.get("code");
            String description = (String) request.get("description");
            Map<String, Object> invitationCode = adminService.generateInvitationCode(code, description);
            return ApiResponse.success("邀请码生成成功", invitationCode);
        } catch (Exception e) {
            return ApiResponse.error(500, "生成邀请码失败: " + e.getMessage());
        }
    }

    /**
     * 获取所有邀请码
     */
    @GetMapping("/invitation-codes")
    public ApiResponse<List<Map<String, Object>>> getAllInvitationCodes() {
        try {
            List<Map<String, Object>> codes = adminService.getAllInvitationCodes();
            return ApiResponse.success("获取邀请码列表成功", codes);
        } catch (Exception e) {
            return ApiResponse.error(500, "获取邀请码列表失败: " + e.getMessage());
        }
    }

    /**
     * 删除邀请码
     */
    @DeleteMapping("/invitation-codes/{id}")
    public ApiResponse<Void> deleteInvitationCode(@PathVariable Long id) {
        try {
            adminService.deleteInvitationCode(id);
            return ApiResponse.success("邀请码删除成功", null);
        } catch (Exception e) {
            return ApiResponse.error(500, "删除邀请码失败: " + e.getMessage());
        }
    }
}
