package com.studyhelper.controller;

import com.studyhelper.config.AccessGuard;
import com.studyhelper.dto.ApiResponse;
import com.studyhelper.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/notification")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private AccessGuard accessGuard;

    @GetMapping("/list")
    public ApiResponse<Map<String, Object>> getNotifications(@RequestParam Long userId,
                                                             @RequestParam(defaultValue = "20") Integer limit) {
        accessGuard.requireSelfOrAdmin(userId);
        try {
            return ApiResponse.success(notificationService.getNotifications(userId, limit));
        } catch (RuntimeException e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }
}
