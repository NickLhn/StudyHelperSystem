package com.studyhelper.controller;

import com.studyhelper.config.AccessGuard;
import com.studyhelper.dto.*;
import com.studyhelper.entity.Homework;
import com.studyhelper.service.HomeworkService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/homework")
public class HomeworkController {

    @Autowired
    private HomeworkService homeworkService;

    @Autowired
    private AccessGuard accessGuard;

    @PostMapping("/create")
    public ApiResponse<HomeworkDTO> createHomework(@RequestParam Long userId, @Valid @RequestBody HomeworkRequest request) {
        accessGuard.requireSelfOrAdmin(userId);
        try {
            return ApiResponse.success("作业创建成功", homeworkService.createHomework(userId, request));
        } catch (RuntimeException e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }

    @GetMapping("/teacher")
    public ApiResponse<List<HomeworkDTO>> getTeacherHomeworks(@RequestParam Long userId,
                                                              @RequestParam(required = false) Long courseId) {
        accessGuard.requireSelfOrAdmin(userId);
        try {
            return ApiResponse.success(homeworkService.getTeacherHomeworks(userId, courseId));
        } catch (RuntimeException e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }

    @GetMapping("/student")
    public ApiResponse<List<HomeworkDTO>> getStudentHomeworks(@RequestParam Long userId) {
        accessGuard.requireSelfOrAdmin(userId);
        try {
            return ApiResponse.success(homeworkService.getStudentHomeworks(userId));
        } catch (RuntimeException e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }

    @GetMapping("/{homeworkId}")
    public ApiResponse<Map<String, Object>> getHomeworkDetail(@PathVariable Long homeworkId, @RequestParam Long userId) {
        accessGuard.requireSelfOrAdmin(userId);
        try {
            return ApiResponse.success(homeworkService.getHomeworkDetail(homeworkId, userId));
        } catch (RuntimeException e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }

    @PatchMapping("/{homeworkId}/status")
    public ApiResponse<HomeworkDTO> updateHomeworkStatus(@PathVariable Long homeworkId,
                                                         @RequestParam Long userId,
                                                         @RequestParam Homework.Status status) {
        accessGuard.requireSelfOrAdmin(userId);
        try {
            return ApiResponse.success("状态更新成功", homeworkService.updateHomeworkStatus(userId, homeworkId, status));
        } catch (RuntimeException e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }

    @DeleteMapping("/{homeworkId}")
    public ApiResponse<Void> deleteHomework(@PathVariable Long homeworkId, @RequestParam Long userId) {
        accessGuard.requireSelfOrAdmin(userId);
        try {
            homeworkService.deleteHomework(userId, homeworkId);
            return ApiResponse.success("删除成功", null);
        } catch (RuntimeException e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }

    @PostMapping("/{homeworkId}/submit")
    public ApiResponse<Map<String, Object>> submitHomework(@PathVariable Long homeworkId,
                                                           @RequestParam Long userId,
                                                           @Valid @RequestBody HomeworkSubmitRequest request) {
        accessGuard.requireSelfOrAdmin(userId);
        try {
            return ApiResponse.success("提交成功", homeworkService.submitHomework(userId, homeworkId, request));
        } catch (RuntimeException e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }

    @GetMapping("/{homeworkId}/submissions")
    public ApiResponse<List<HomeworkSubmissionDTO>> getHomeworkSubmissions(@PathVariable Long homeworkId,
                                                                           @RequestParam Long userId) {
        accessGuard.requireSelfOrAdmin(userId);
        try {
            return ApiResponse.success(homeworkService.getHomeworkSubmissions(userId, homeworkId));
        } catch (RuntimeException e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }

    @GetMapping("/submissions/{submissionId}")
    public ApiResponse<Map<String, Object>> getSubmissionDetail(@PathVariable Long submissionId,
                                                                @RequestParam Long userId) {
        accessGuard.requireSelfOrAdmin(userId);
        try {
            return ApiResponse.success(homeworkService.getSubmissionDetail(userId, submissionId));
        } catch (RuntimeException e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }

    @PatchMapping("/submissions/{submissionId}/review")
    public ApiResponse<Map<String, Object>> reviewSubmission(@PathVariable Long submissionId,
                                                             @RequestParam Long userId,
                                                             @RequestBody HomeworkReviewRequest request) {
        accessGuard.requireSelfOrAdmin(userId);
        try {
            return ApiResponse.success("复核成功", homeworkService.reviewSubmission(userId, submissionId, request));
        } catch (RuntimeException e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }

    @GetMapping("/{homeworkId}/summary")
    public ApiResponse<Map<String, Object>> getHomeworkSummary(@PathVariable Long homeworkId,
                                                               @RequestParam Long userId) {
        accessGuard.requireSelfOrAdmin(userId);
        try {
            return ApiResponse.success(homeworkService.getHomeworkSummary(userId, homeworkId));
        } catch (RuntimeException e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }
}
