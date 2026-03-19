package com.studyhelper.controller;

import com.studyhelper.config.AccessGuard;
import com.studyhelper.dto.ApiResponse;
import com.studyhelper.dto.CourseDTO;
import com.studyhelper.dto.CourseRequest;
import com.studyhelper.dto.UserDTO;
import com.studyhelper.entity.Course;
import com.studyhelper.entity.User;
import com.studyhelper.service.CourseService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/course")
public class CourseController {

    @Autowired
    private CourseService courseService;

    @Autowired
    private AccessGuard accessGuard;

    @PostMapping("/create")
    public ApiResponse<CourseDTO> createCourse(@RequestParam Long userId, @Valid @RequestBody CourseRequest request) {
        accessGuard.requireSelfOrAdmin(userId);
        try {
            CourseDTO course = courseService.createCourse(userId, request);
            return ApiResponse.success("课程创建成功", course);
        } catch (RuntimeException e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }

    @GetMapping("/list")
    public ApiResponse<List<CourseDTO>> getUserCourses(@RequestParam Long userId) {
        accessGuard.requireSelfOrAdmin(userId);
        try {
            List<CourseDTO> courses = courseService.getUserCourses(userId);
            return ApiResponse.success(courses);
        } catch (RuntimeException e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }

    @GetMapping("/student/list")
    public ApiResponse<List<CourseDTO>> getStudentCourses(@RequestParam Long userId) {
        accessGuard.requireSelfOrAdmin(userId);
        try {
            List<CourseDTO> courses = courseService.getStudentCourses(userId);
            return ApiResponse.success(courses);
        } catch (RuntimeException e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }

    @PostMapping("/join")
    public ApiResponse<CourseDTO> joinCourse(@RequestParam Long userId, @RequestParam String invitationCode) {
        accessGuard.requireSelfOrAdmin(userId);
        try {
            CourseDTO course = courseService.joinCourse(userId, invitationCode);
            return ApiResponse.success("加入课程成功", course);
        } catch (RuntimeException e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }

    @GetMapping("/list-by-category")
    public ApiResponse<List<CourseDTO>> getUserCoursesByCategory(@RequestParam Long userId, @RequestParam Course.Category category) {
        accessGuard.requireSelfOrAdmin(userId);
        try {
            List<CourseDTO> courses = courseService.getUserCoursesByCategory(userId, category);
            return ApiResponse.success(courses);
        } catch (RuntimeException e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }

    @GetMapping("/{courseId}")
    public ApiResponse<CourseDTO> getCourseById(@PathVariable Long courseId, @RequestParam Long userId) {
        accessGuard.requireSelfOrAdmin(userId);
        try {
            CourseDTO course = courseService.getCourseById(courseId, userId);
            return ApiResponse.success(course);
        } catch (RuntimeException e) {
            return ApiResponse.error(404, e.getMessage());
        }
    }

    @GetMapping("/{courseId}/students")
    public ApiResponse<List<UserDTO>> getCourseStudents(@PathVariable Long courseId, @RequestParam Long userId) {
        accessGuard.requireSelfOrAdmin(userId);
        try {
            List<User> students = courseService.getCourseStudents(courseId, userId);
            List<UserDTO> dtos = students.stream().map(UserDTO::fromUser).collect(Collectors.toList());
            return ApiResponse.success(dtos);
        } catch (RuntimeException e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }

    @PostMapping("/{courseId}/invitation-code/refresh")
    public ApiResponse<Map<String, Object>> refreshInvitationCode(@PathVariable Long courseId, @RequestParam Long userId) {
        accessGuard.requireSelfOrAdmin(userId);
        try {
            String code = courseService.refreshInvitationCode(courseId, userId);
            return ApiResponse.success("邀请码刷新成功", Map.of("invitationCode", code));
        } catch (RuntimeException e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }

    @DeleteMapping("/{courseId}/students/{studentId}")
    public ApiResponse<Void> removeStudent(@PathVariable Long courseId, @PathVariable Long studentId, @RequestParam Long userId) {
        accessGuard.requireSelfOrAdmin(userId);
        try {
            courseService.removeStudent(courseId, userId, studentId);
            return ApiResponse.success("移除学生成功", null);
        } catch (RuntimeException e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }

    @GetMapping("/{courseId}/stats")
    public ApiResponse<Map<String, Object>> getCourseStats(
            @PathVariable Long courseId,
            @RequestParam Long userId,
            @RequestParam(defaultValue = "week") String period) {
        accessGuard.requireSelfOrAdmin(userId);
        try {
            Map<String, Object> stats = courseService.getCourseStats(courseId, userId, period);
            return ApiResponse.success(stats);
        } catch (RuntimeException e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }

    @PutMapping("/{courseId}")
    public ApiResponse<CourseDTO> updateCourse(@PathVariable Long courseId, @RequestParam Long userId, @Valid @RequestBody CourseRequest request) {
        accessGuard.requireSelfOrAdmin(userId);
        try {
            CourseDTO course = courseService.updateCourse(courseId, userId, request);
            return ApiResponse.success("课程更新成功", course);
        } catch (RuntimeException e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }

    @DeleteMapping("/{courseId}")
    public ApiResponse<String> deleteCourse(@PathVariable Long courseId, @RequestParam Long userId) {
        accessGuard.requireSelfOrAdmin(userId);
        try {
            courseService.deleteCourse(courseId, userId);
            return ApiResponse.success("课程删除成功", null);
        } catch (RuntimeException e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }


    // 旧版无 userId 的接口已移除，统一使用带 userId 的新接口以进行权限校验
}
