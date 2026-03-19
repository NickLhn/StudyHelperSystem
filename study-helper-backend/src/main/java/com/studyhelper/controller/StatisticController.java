package com.studyhelper.controller;

import com.studyhelper.config.AccessGuard;
import com.studyhelper.dto.ApiResponse;
import com.studyhelper.service.StatisticService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/statistics")
public class StatisticController {

    @Autowired
    private StatisticService statisticService;

    @Autowired
    private AccessGuard accessGuard;

    @GetMapping("/user")
    public ApiResponse<Map<String, Object>> getUserStatistics(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "week") String period) {
        accessGuard.requireSelfOrAdmin(userId);
        try {
            Map<String, Object> stats = statisticService.getUserStatistics(userId, period);
            return ApiResponse.success(stats);
        } catch (RuntimeException e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }

    @GetMapping("/comparison")
    public ApiResponse<Map<String, Object>> getComparisonStatistics(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "week") String period) {
        accessGuard.requireSelfOrAdmin(userId);
        try {
            Map<String, Object> comparison = statisticService.getComparisonStatistics(userId, period);
            return ApiResponse.success(comparison);
        } catch (RuntimeException e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }

    @PostMapping("/sync-from-tasks")
    public ApiResponse<String> syncFromTasks(@RequestParam Long userId) {
        accessGuard.requireSelfOrAdmin(userId);
        try {
            statisticService.createStudyRecordFromTasks(userId);
            return ApiResponse.success("同步成功");
        } catch (RuntimeException e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }
}
