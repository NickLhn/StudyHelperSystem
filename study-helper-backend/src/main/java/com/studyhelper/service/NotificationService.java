package com.studyhelper.service;

import com.studyhelper.dto.NotificationDTO;
import com.studyhelper.entity.*;
import com.studyhelper.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private StudentCourseRepository studentCourseRepository;

    @Autowired
    private HomeworkRepository homeworkRepository;

    @Autowired
    private HomeworkSubmissionRepository homeworkSubmissionRepository;

    @Autowired
    private MaterialRepository materialRepository;

    @Autowired
    private QuizRepository quizRepository;

    @Transactional(readOnly = true)
    public Map<String, Object> getNotifications(Long userId, Integer limit) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        List<NotificationDTO> notifications = switch (user.getRole()) {
            case TEACHER -> buildTeacherNotifications(userId);
            case STUDENT -> buildStudentNotifications(userId);
            case ADMIN -> buildAdminNotifications();
        };

        List<NotificationDTO> sorted = notifications.stream()
                .sorted(Comparator
                        .comparingInt((NotificationDTO item) -> levelPriority(item.getLevel()))
                        .thenComparing(NotificationDTO::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());

        int safeLimit = limit == null || limit < 1 ? 20 : Math.min(limit, 50);
        List<NotificationDTO> limitedItems = sorted.stream().limit(safeLimit).collect(Collectors.toList());

        Map<String, Long> categoryCounts = sorted.stream()
                .collect(Collectors.groupingBy(NotificationDTO::getCategory, Collectors.counting()));

        Map<String, Object> summary = new HashMap<>();
        summary.put("totalCount", sorted.size());
        summary.put("urgentCount", sorted.stream().filter(item -> "urgent".equals(item.getLevel())).count());
        summary.put("warningCount", sorted.stream().filter(item -> "warning".equals(item.getLevel())).count());
        summary.put("reviewCount", categoryCounts.getOrDefault("REVIEW", 0L));
        summary.put("homeworkCount", categoryCounts.getOrDefault("HOMEWORK", 0L));
        summary.put("quizCount", categoryCounts.getOrDefault("QUIZ", 0L));
        summary.put("materialCount", categoryCounts.getOrDefault("MATERIAL", 0L));

        Map<String, Object> result = new HashMap<>();
        result.put("items", limitedItems);
        result.put("summary", summary);
        return result;
    }

    private List<NotificationDTO> buildTeacherNotifications(Long userId) {
        LocalDateTime now = LocalDateTime.now();
        List<Homework> homeworks = homeworkRepository.findByTeacherIdOrderByCreatedAtDesc(userId);
        List<NotificationDTO> items = new ArrayList<>();

        for (Homework homework : homeworks) {
            List<HomeworkSubmission> submissions = homeworkSubmissionRepository.findByHomeworkIdOrderByCreatedAtDesc(homework.getId());
            long pendingReviewCount = submissions.stream()
                    .filter(submission -> submission.getStatus() == HomeworkSubmission.Status.REVIEW_PENDING)
                    .count();

            if (pendingReviewCount > 0) {
                LocalDateTime latestPendingAt = submissions.stream()
                        .filter(submission -> submission.getStatus() == HomeworkSubmission.Status.REVIEW_PENDING)
                        .map(HomeworkSubmission::getSubmittedAt)
                        .filter(Objects::nonNull)
                        .max(LocalDateTime::compareTo)
                        .orElse(homework.getUpdatedAt());

                items.add(createNotification(
                        "teacher-review-" + homework.getId(),
                        "REVIEW",
                        "待复核",
                        pendingReviewCount >= 3 ? "urgent" : "warning",
                        homework.getTitle() + " 有待复核提交",
                        homework.getCourse().getName() + " 当前有 " + pendingReviewCount + " 份提交等待教师复核。",
                        homework.getCourse().getName(),
                        "/teacher/homework/" + homework.getId(),
                        "立即复核",
                        latestPendingAt
                ));
            }

            long recentSubmissionCount = submissions.stream()
                    .filter(submission -> submission.getSubmittedAt() != null && submission.getSubmittedAt().isAfter(now.minusHours(36)))
                    .count();
            if (recentSubmissionCount > 0) {
                items.add(createNotification(
                        "teacher-submission-" + homework.getId(),
                        "HOMEWORK",
                        "作业提交",
                        "info",
                        homework.getTitle() + " 收到新的学生提交",
                        homework.getCourse().getName() + " 在最近 36 小时内新增 " + recentSubmissionCount + " 份作业提交。",
                        homework.getCourse().getName(),
                        "/teacher/homework/" + homework.getId(),
                        "查看提交",
                        submissions.get(0).getSubmittedAt()
                ));
            }

            if (homework.getStatus() == Homework.Status.PUBLISHED && homework.getDeadlineAt() != null && homework.getDeadlineAt().isAfter(now)) {
                long assignedCount = studentCourseRepository.countByCourseId(homework.getCourse().getId());
                long submissionCount = submissions.size();
                double submissionRate = assignedCount == 0 ? 0D : (double) submissionCount / assignedCount * 100;
                long hoursLeft = Math.max(0, Duration.between(now, homework.getDeadlineAt()).toHours());

                if (hoursLeft <= 72 && assignedCount > 0 && submissionCount < assignedCount) {
                    items.add(createNotification(
                            "teacher-deadline-" + homework.getId(),
                            "HOMEWORK",
                            "截止提醒",
                            hoursLeft <= 24 ? "urgent" : "warning",
                            homework.getTitle() + " 即将截止",
                            "距离截止还有 " + formatRemainingHours(hoursLeft) + "，当前提交率 " + roundPercent(submissionRate) + "%（" + submissionCount + "/" + assignedCount + "）。",
                            homework.getCourse().getName(),
                            "/teacher/homework/" + homework.getId(),
                            "查看作业",
                            homework.getDeadlineAt()
                    ));
                }
            }
        }

        return items;
    }

    private List<NotificationDTO> buildStudentNotifications(Long userId) {
        LocalDateTime now = LocalDateTime.now();
        List<StudentCourse> enrollments = studentCourseRepository.findByStudentId(userId);
        List<Long> courseIds = enrollments.stream()
                .map(studentCourse -> studentCourse.getCourse().getId())
                .collect(Collectors.toList());
        Map<Long, String> courseNameMap = enrollments.stream()
                .collect(Collectors.toMap(studentCourse -> studentCourse.getCourse().getId(), studentCourse -> studentCourse.getCourse().getName()));

        if (courseIds.isEmpty()) {
            return List.of();
        }

        List<NotificationDTO> items = new ArrayList<>();

        List<Homework> homeworks = homeworkRepository.findByCourseIdInAndStatusOrderByDeadlineAtAscCreatedAtDesc(courseIds, Homework.Status.PUBLISHED);
        for (Homework homework : homeworks) {
            Optional<HomeworkSubmission> submissionOpt = homeworkSubmissionRepository.findTopByHomeworkIdAndStudentIdOrderByCreatedAtDesc(homework.getId(), userId);

            if (submissionOpt.isEmpty()) {
                if (homework.getDeadlineAt() != null) {
                    if (homework.getDeadlineAt().isBefore(now)) {
                        if (Boolean.TRUE.equals(homework.getAllowLateSubmit())) {
                            items.add(createNotification(
                                    "student-overdue-" + homework.getId(),
                                    "HOMEWORK",
                                    "补交提醒",
                                    "urgent",
                                    homework.getTitle() + " 已过截止时间",
                                    "这份作业已经截止，但当前课程仍允许补交，请尽快处理。",
                                    courseNameMap.getOrDefault(homework.getCourse().getId(), homework.getCourse().getName()),
                                    "/student/homework/" + homework.getId(),
                                    "去完成",
                                    homework.getDeadlineAt()
                            ));
                        }
                    } else {
                        long hoursLeft = Math.max(0, Duration.between(now, homework.getDeadlineAt()).toHours());
                        if (hoursLeft <= 72) {
                            items.add(createNotification(
                                    "student-deadline-" + homework.getId(),
                                    "HOMEWORK",
                                    "截止提醒",
                                    hoursLeft <= 24 ? "urgent" : "warning",
                                    homework.getTitle() + " 即将截止",
                                    "距离截止还有 " + formatRemainingHours(hoursLeft) + "，建议优先完成这份作业。",
                                    courseNameMap.getOrDefault(homework.getCourse().getId(), homework.getCourse().getName()),
                                    "/student/homework/" + homework.getId(),
                                    "去完成",
                                    homework.getDeadlineAt()
                            ));
                        }
                    }
                }

                boolean hasNearDeadline = homework.getDeadlineAt() != null && !homework.getDeadlineAt().isBefore(now)
                        && Duration.between(now, homework.getDeadlineAt()).toHours() <= 72;
                if (!hasNearDeadline && homework.getCreatedAt() != null && homework.getCreatedAt().isAfter(now.minusDays(5))) {
                    items.add(createNotification(
                            "student-new-homework-" + homework.getId(),
                            "HOMEWORK",
                            "新作业",
                            "info",
                            homework.getTitle() + " 已发布",
                            "老师刚刚为 " + courseNameMap.getOrDefault(homework.getCourse().getId(), homework.getCourse().getName()) + " 发布了新作业。",
                            courseNameMap.getOrDefault(homework.getCourse().getId(), homework.getCourse().getName()),
                            "/student/homework/" + homework.getId(),
                            "查看作业",
                            homework.getCreatedAt()
                    ));
                }
            }

            submissionOpt.ifPresent(submission -> {
                LocalDateTime resultTime = submission.getReviewedAt() != null ? submission.getReviewedAt() : submission.getAutoGradedAt();
                if (resultTime != null
                        && resultTime.isAfter(now.minusDays(7))
                        && (submission.getStatus() == HomeworkSubmission.Status.REVIEWED
                        || submission.getStatus() == HomeworkSubmission.Status.AUTO_GRADED)) {
                    items.add(createNotification(
                            "student-result-" + submission.getId(),
                            "REVIEW",
                            "批改完成",
                            "success",
                            homework.getTitle() + " 已出结果",
                            "你的作业已经完成批改，可以查看得分与题目反馈。",
                            courseNameMap.getOrDefault(homework.getCourse().getId(), homework.getCourse().getName()),
                            "/student/homework/result/" + submission.getId(),
                            "查看结果",
                            resultTime
                    ));
                }
            });
        }

        List<Quiz> quizzes = quizRepository.findAvailableQuizzes(userId);
        quizzes.stream()
                .filter(quiz -> quiz.getCourse() != null && courseIds.contains(quiz.getCourse().getId()))
                .filter(quiz -> quiz.getStatus() == Quiz.Status.PUBLISHED)
                .filter(quiz -> quiz.getCreatedAt() != null && quiz.getCreatedAt().isAfter(now.minusDays(7)))
                .forEach(quiz -> items.add(createNotification(
                        "student-quiz-" + quiz.getId(),
                        "QUIZ",
                        "新测验",
                        "info",
                        quiz.getTitle() + " 已发布",
                        "老师刚刚发布了新测验，建议尽快进入查看并安排完成时间。",
                        courseNameMap.getOrDefault(quiz.getCourse().getId(), quiz.getCourse().getName()),
                        "/quiz/" + quiz.getId(),
                        "查看测验",
                        quiz.getCreatedAt()
                )));

        materialRepository.findAccessibleMaterials(userId).stream()
                .filter(material -> material.getCourse() != null && courseIds.contains(material.getCourse().getId()))
                .filter(material -> material.getCreatedAt() != null && material.getCreatedAt().isAfter(now.minusDays(7)))
                .forEach(material -> items.add(createNotification(
                        "student-material-" + material.getId(),
                        "MATERIAL",
                        "资料更新",
                        "info",
                        material.getName() + " 已更新到资料中心",
                        "课程资料中心新增了可下载资源，建议及时查看并同步学习进度。",
                        courseNameMap.getOrDefault(material.getCourse().getId(), material.getCourse().getName()),
                        "/material/" + material.getId(),
                        "查看资料",
                        material.getCreatedAt()
                )));

        return items;
    }

    private List<NotificationDTO> buildAdminNotifications() {
        return List.of(
                createNotification(
                        "admin-placeholder",
                        "MATERIAL",
                        "系统提醒",
                        "info",
                        "管理员消息中心已预留",
                        "当前阶段的动态通知主要覆盖教师和学生场景，管理员提醒可以在下一轮继续扩展。",
                        "平台治理",
                        "/admin/dashboard",
                        "返回后台",
                        LocalDateTime.now()
                )
        );
    }

    private NotificationDTO createNotification(String id,
                                               String category,
                                               String categoryLabel,
                                               String level,
                                               String title,
                                               String message,
                                               String courseName,
                                               String actionPath,
                                               String actionLabel,
                                               LocalDateTime createdAt) {
        NotificationDTO dto = new NotificationDTO();
        dto.setId(id);
        dto.setCategory(category);
        dto.setCategoryLabel(categoryLabel);
        dto.setLevel(level);
        dto.setTitle(title);
        dto.setMessage(message);
        dto.setCourseName(courseName);
        dto.setActionPath(actionPath);
        dto.setActionLabel(actionLabel);
        dto.setCreatedAt(createdAt);
        return dto;
    }

    private int levelPriority(String level) {
        return switch (level) {
            case "urgent" -> 0;
            case "warning" -> 1;
            case "success" -> 2;
            default -> 3;
        };
    }

    private String formatRemainingHours(long hoursLeft) {
        if (hoursLeft <= 24) {
            return hoursLeft + " 小时";
        }
        long days = Math.max(1, hoursLeft / 24);
        return days + " 天";
    }

    private double roundPercent(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
