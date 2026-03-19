package com.studyhelper.controller;

import com.studyhelper.config.AccessGuard;
import com.studyhelper.dto.ApiResponse;
import com.studyhelper.dto.QuizDTO;
import com.studyhelper.service.QuizService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/quiz")
public class QuizController {

    @Autowired
    private QuizService quizService;

    @Autowired
    private AccessGuard accessGuard;

    @PostMapping("/create")
    public ApiResponse<QuizDTO> createQuiz(
            @RequestParam Long userId,
            @RequestParam String title,
            @RequestParam(required = false) String description,
            @RequestParam(required = false, defaultValue = "0") Integer totalTime,
            @RequestParam(required = false) Long courseId,
            @RequestBody List<Map<String, Object>> questions) {
        accessGuard.requireSelfOrAdmin(userId);
        try {
            QuizDTO quiz = quizService.createQuiz(userId, title, description, totalTime, courseId, questions);
            return ApiResponse.success("测验创建成功", quiz);
        } catch (RuntimeException e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }

    @GetMapping("/my")
    public ApiResponse<List<QuizDTO>> getUserQuizzes(@RequestParam Long userId) {
        accessGuard.requireSelfOrAdmin(userId);
        try {
            List<QuizDTO> quizzes = quizService.getUserQuizzes(userId);
            return ApiResponse.success(quizzes);
        } catch (RuntimeException e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }

    @GetMapping("/available")
    public ApiResponse<List<QuizDTO>> getAvailableQuizzes(@RequestParam Long userId) {
        accessGuard.requireSelfOrAdmin(userId);
        try {
            List<QuizDTO> quizzes = quizService.getAvailableQuizzes(userId);
            return ApiResponse.success(quizzes);
        } catch (RuntimeException e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }

    @GetMapping("/list-by-course")
    public ApiResponse<List<QuizDTO>> getCourseQuizzes(@RequestParam Long userId, @RequestParam Long courseId) {
        accessGuard.requireSelfOrAdmin(userId);
        try {
            List<QuizDTO> quizzes = quizService.getCourseQuizzes(userId, courseId);
            return ApiResponse.success(quizzes);
        } catch (RuntimeException e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }

    @PatchMapping("/{quizId}/status")
    public ApiResponse<QuizDTO> updateQuizStatus(
            @PathVariable Long quizId,
            @RequestParam Long userId,
            @RequestParam com.studyhelper.entity.Quiz.Status status) {
        accessGuard.requireSelfOrAdmin(userId);
        try {
            QuizDTO quiz = quizService.updateQuizStatus(userId, quizId, status);
            return ApiResponse.success("状态更新成功", quiz);
        } catch (RuntimeException e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }

    @DeleteMapping("/{quizId}")
    public ApiResponse<Void> deleteQuiz(@PathVariable Long quizId, @RequestParam Long userId) {
        accessGuard.requireSelfOrAdmin(userId);
        try {
            quizService.deleteQuiz(userId, quizId);
            return ApiResponse.success("删除成功", null);
        } catch (RuntimeException e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }

    @GetMapping("/{quizId}")
    public ApiResponse<Map<String, Object>> getQuizDetail(
            @PathVariable Long quizId,
            @RequestParam Long userId) {
        accessGuard.requireSelfOrAdmin(userId);
        try {
            Map<String, Object> result = quizService.getQuizDetail(quizId, userId);
            return ApiResponse.success(result);
        } catch (RuntimeException e) {
            return ApiResponse.error(404, e.getMessage());
        }
    }

    @PostMapping("/{quizId}/submit")
    public ApiResponse<Map<String, Object>> submitQuiz(
            @PathVariable Long quizId,
            @RequestParam Long userId,
            @RequestBody Map<String, String> answers) {
        accessGuard.requireSelfOrAdmin(userId);
        try {
            Map<String, Object> result = quizService.submitQuiz(userId, quizId, answers);
            return ApiResponse.success("测验提交成功", result);
        } catch (RuntimeException e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }

    @GetMapping("/records")
    public ApiResponse<List<Map<String, Object>>> getUserRecords(@RequestParam Long userId) {
        accessGuard.requireSelfOrAdmin(userId);
        try {
            List<Map<String, Object>> records = quizService.getUserRecords(userId);
            return ApiResponse.success(records);
        } catch (RuntimeException e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }

    @GetMapping("/wrong-questions")
    public ApiResponse<List<Map<String, Object>>> getWrongQuestions(@RequestParam Long userId) {
        accessGuard.requireSelfOrAdmin(userId);
        try {
            List<Map<String, Object>> wrongQuestions = quizService.getWrongQuestions(userId);
            return ApiResponse.success(wrongQuestions);
        } catch (RuntimeException e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }
}
