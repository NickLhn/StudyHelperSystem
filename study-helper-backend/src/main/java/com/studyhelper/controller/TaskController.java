package com.studyhelper.controller;

import com.studyhelper.config.AccessGuard;
import com.studyhelper.dto.ApiResponse;
import com.studyhelper.dto.TaskDTO;
import com.studyhelper.dto.TaskRequest;
import com.studyhelper.entity.Task;
import com.studyhelper.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/task")
public class TaskController {

    @Autowired
    private TaskService taskService;

    @Autowired
    private AccessGuard accessGuard;

    @PostMapping("/create")
    public ApiResponse<TaskDTO> createTask(@RequestParam Long userId, @Valid @RequestBody TaskRequest request) {
        accessGuard.requireSelfOrAdmin(userId);
        try {
            TaskDTO task = taskService.createTask(userId, request);
            return ApiResponse.success("任务创建成功", task);
        } catch (RuntimeException e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }

    @GetMapping("/list")
    public ApiResponse<List<TaskDTO>> getUserTasks(@RequestParam Long userId) {
        accessGuard.requireSelfOrAdmin(userId);
        try {
            List<TaskDTO> tasks = taskService.getUserTasks(userId);
            return ApiResponse.success(tasks);
        } catch (RuntimeException e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }

    @GetMapping("/list-by-course")
    public ApiResponse<List<TaskDTO>> getCourseTasks(@RequestParam Long userId, @RequestParam Long courseId) {
        accessGuard.requireSelfOrAdmin(userId);
        try {
            List<TaskDTO> tasks = taskService.getCourseTasks(userId, courseId);
            return ApiResponse.success(tasks);
        } catch (RuntimeException e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }

    @GetMapping("/list-by-status")
    public ApiResponse<List<TaskDTO>> getUserTasksByStatus(@RequestParam Long userId, @RequestParam Task.Status status) {
        accessGuard.requireSelfOrAdmin(userId);
        try {
            List<TaskDTO> tasks = taskService.getUserTasksByStatus(userId, status);
            return ApiResponse.success(tasks);
        } catch (RuntimeException e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }

    @GetMapping("/list-by-date")
    public ApiResponse<List<TaskDTO>> getUserTasksByDate(
            @RequestParam Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        accessGuard.requireSelfOrAdmin(userId);
        try {
            List<TaskDTO> tasks = taskService.getUserTasksByDate(userId, date);
            return ApiResponse.success(tasks);
        } catch (RuntimeException e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }

    @GetMapping("/list-by-date-range")
    public ApiResponse<List<TaskDTO>> getUserTasksByDateRange(
            @RequestParam Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        accessGuard.requireSelfOrAdmin(userId);
        try {
            List<TaskDTO> tasks = taskService.getUserTasksByDateRange(userId, startDate, endDate);
            return ApiResponse.success(tasks);
        } catch (RuntimeException e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }

    @GetMapping("/today-reminders")
    public ApiResponse<List<TaskDTO>> getTodayReminders(@RequestParam Long userId) {
        accessGuard.requireSelfOrAdmin(userId);
        try {
            List<TaskDTO> tasks = taskService.getTodayTasksWithReminder(userId);
            return ApiResponse.success(tasks);
        } catch (RuntimeException e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }

    @GetMapping("/statistics")
    public ApiResponse<Map<String, Long>> getTaskStatistics(@RequestParam Long userId) {
        accessGuard.requireSelfOrAdmin(userId);
        try {
            Map<String, Long> stats = new HashMap<>();
            stats.put("todo", taskService.getTaskCountByStatus(userId, Task.Status.TODO));
            stats.put("inProgress", taskService.getTaskCountByStatus(userId, Task.Status.IN_PROGRESS));
            stats.put("completed", taskService.getTaskCountByStatus(userId, Task.Status.COMPLETED));
            return ApiResponse.success(stats);
        } catch (RuntimeException e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }

    @GetMapping("/{taskId}")
    public ApiResponse<TaskDTO> getTaskById(@PathVariable Long taskId, @RequestParam Long userId) {
        accessGuard.requireSelfOrAdmin(userId);
        try {
            TaskDTO task = taskService.getTaskById(taskId, userId);
            return ApiResponse.success(task);
        } catch (RuntimeException e) {
            return ApiResponse.error(404, e.getMessage());
        }
    }

    @PutMapping("/{taskId}")
    public ApiResponse<TaskDTO> updateTask(@PathVariable Long taskId, @RequestParam Long userId, @Valid @RequestBody TaskRequest request) {
        accessGuard.requireSelfOrAdmin(userId);
        try {
            TaskDTO task = taskService.updateTask(taskId, userId, request);
            return ApiResponse.success("任务更新成功", task);
        } catch (RuntimeException e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }

    @PatchMapping("/{taskId}/status")
    public ApiResponse<TaskDTO> updateTaskStatus(
            @PathVariable Long taskId,
            @RequestParam Long userId,
            @RequestParam Task.Status status) {
        accessGuard.requireSelfOrAdmin(userId);
        try {
            TaskDTO task = taskService.updateTaskStatus(taskId, userId, status);
            return ApiResponse.success("状态更新成功", task);
        } catch (RuntimeException e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }

    @DeleteMapping("/{taskId}")
    public ApiResponse<String> deleteTask(@PathVariable Long taskId, @RequestParam Long userId) {
        accessGuard.requireSelfOrAdmin(userId);
        try {
            taskService.deleteTask(taskId, userId);
            return ApiResponse.success("任务删除成功", null);
        } catch (RuntimeException e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }
}
