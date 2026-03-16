package com.studyhelper.controller;

import com.studyhelper.dto.*;
import com.studyhelper.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ApiResponse<UserDTO> register(@Valid @RequestBody RegisterRequest request) {
        try {
            UserDTO user = userService.register(request);
            return ApiResponse.success("注册成功", user);
        } catch (RuntimeException e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }

    @PostMapping("/login")
    public ApiResponse<Map<String, Object>> login(@Valid @RequestBody LoginRequest request) {
        try {
            UserDTO user = userService.login(request);
            Map<String, Object> data = new HashMap<>();
            data.put("user", user);
            data.put("token", "mock-token-" + user.getId());
            // 添加角色信息用于前端路由判断
            data.put("role", user.getRole());
            return ApiResponse.success("登录成功", data);
        } catch (RuntimeException e) {
            return ApiResponse.error(401, e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ApiResponse<UserDTO> getUserById(@PathVariable Long id) {
        try {
            UserDTO user = userService.getUserById(id);
            return ApiResponse.success(user);
        } catch (RuntimeException e) {
            return ApiResponse.error(404, e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ApiResponse<UserDTO> updateUser(@PathVariable Long id, @RequestBody RegisterRequest request) {
        try {
            UserDTO user = userService.updateUser(id, request);
            return ApiResponse.success("更新成功", user);
        } catch (RuntimeException e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }

    @PostMapping("/{id}/avatar")
    public ApiResponse<String> updateAvatar(@PathVariable Long id, @RequestParam String avatarUrl) {
        try {
            userService.updateAvatar(id, avatarUrl);
            return ApiResponse.success("头像更新成功", avatarUrl);
        } catch (RuntimeException e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }
}
