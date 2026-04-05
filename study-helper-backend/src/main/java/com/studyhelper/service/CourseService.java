package com.studyhelper.service;

import com.studyhelper.dto.CourseDTO;
import com.studyhelper.dto.CourseRequest;
import com.studyhelper.entity.Course;
import com.studyhelper.entity.Homework;
import com.studyhelper.entity.HomeworkAnswer;
import com.studyhelper.entity.HomeworkQuestion;
import com.studyhelper.entity.HomeworkSubmission;
import com.studyhelper.entity.Quiz;
import com.studyhelper.entity.QuizRecord;
import com.studyhelper.entity.StudentCourse;
import com.studyhelper.entity.User;
import com.studyhelper.repository.CourseRepository;
import com.studyhelper.repository.HomeworkAnswerRepository;
import com.studyhelper.repository.HomeworkQuestionRepository;
import com.studyhelper.repository.HomeworkRepository;
import com.studyhelper.repository.HomeworkSubmissionRepository;
import com.studyhelper.repository.MaterialRepository;
import com.studyhelper.repository.QuizRecordRepository;
import com.studyhelper.repository.QuizRepository;
import com.studyhelper.repository.StudentCourseRepository;
import com.studyhelper.repository.StudyRecordRepository;
import com.studyhelper.repository.TaskRepository;
import com.studyhelper.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class CourseService {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StudentCourseRepository studentCourseRepository;

    @Autowired
    private MaterialRepository materialRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private QuizRecordRepository quizRecordRepository;

    @Autowired
    private HomeworkRepository homeworkRepository;

    @Autowired
    private HomeworkQuestionRepository homeworkQuestionRepository;

    @Autowired
    private HomeworkSubmissionRepository homeworkSubmissionRepository;

    @Autowired
    private HomeworkAnswerRepository homeworkAnswerRepository;

    @Autowired
    private StudyRecordRepository studyRecordRepository;

    private String generateInvitationCode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random = new Random();
        String code;
        do {
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < 6; i++) {
                builder.append(chars.charAt(random.nextInt(chars.length())));
            }
            code = builder.toString();
        } while (courseRepository.existsByInvitationCode(code));
        return code;
    }

    public CourseDTO createCourse(Long userId, CourseRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        Course course = new Course();
        course.setName(request.getName());
        course.setCategory(request.getCategory());
        course.setStatus(Course.Status.ACTIVE);
        course.setTeacher(request.getTeacher());
        course.setSemesterLabel(normalizeNullable(request.getSemesterLabel()));
        course.setSchedule(request.getSchedule());
        course.setLocation(request.getLocation());
        course.setRemark(request.getRemark());
        course.setUser(user);
        course.setInvitationCode(generateInvitationCode());

        Course savedCourse = courseRepository.save(course);
        CourseDTO dto = CourseDTO.fromCourse(savedCourse);
        dto.setStudentCount(0L);
        dto.setMaterialCount(0L);
        dto.setTaskCount(0L);
        dto.setQuizCount(0L);
        return dto;
    }

    @Transactional(readOnly = true)
    public List<CourseDTO> getUserCourses(Long userId) {
        List<Course> courses = courseRepository.findByUserId(userId);
        return courses.stream()
                .map(this::toCourseDTOWithCounts)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CourseDTO> getUserCoursesByStatus(Long userId, Course.Status status) {
        List<Course> courses = courseRepository.findByUserIdAndStatus(userId, status);
        return courses.stream()
                .map(this::toCourseDTOWithCounts)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CourseDTO> getUserCoursesByCategory(Long userId, Course.Category category) {
        List<Course> courses = courseRepository.findByUserIdAndCategory(userId, category);
        return courses.stream()
                .map(this::toCourseDTOWithCounts)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CourseDTO> getUserCoursesByCategoryAndStatus(Long userId, Course.Category category, Course.Status status) {
        List<Course> courses = courseRepository.findByUserIdAndCategoryAndStatus(userId, category, status);
        return courses.stream()
                .map(this::toCourseDTOWithCounts)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CourseDTO getCourseById(Long courseId, Long userId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("课程不存在"));

        boolean hasAccess = course.getUser().getId().equals(userId) ||
                studentCourseRepository.existsByStudentIdAndCourseId(userId, courseId);
        if (!hasAccess) {
            throw new RuntimeException("无权访问该课程");
        }

        return toCourseDTOWithCounts(course);
    }

    public CourseDTO updateCourse(Long courseId, Long userId, CourseRequest request) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("课程不存在"));

        if (!course.getUser().getId().equals(userId)) {
            throw new RuntimeException("无权修改该课程");
        }

        course.setName(request.getName());
        course.setCategory(request.getCategory());
        course.setTeacher(request.getTeacher());
        course.setSemesterLabel(normalizeNullable(request.getSemesterLabel()));
        course.setSchedule(request.getSchedule());
        course.setLocation(request.getLocation());
        course.setRemark(request.getRemark());

        Course updatedCourse = courseRepository.save(course);
        return toCourseDTOWithCounts(updatedCourse);
    }

    public void deleteCourse(Long courseId, Long userId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("课程不存在"));

        if (!course.getUser().getId().equals(userId)) {
            throw new RuntimeException("无权删除该课程");
        }

        courseRepository.delete(course);
    }

    @Transactional(readOnly = true)
    public CourseDTO getCourseDetail(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("课程不存在"));
        return toCourseDTOWithCounts(course);
    }

    public CourseDTO joinCourse(Long userId, String invitationCode) {
        User student = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        Course course = courseRepository.findByInvitationCode(invitationCode)
                .orElseThrow(() -> new RuntimeException("邀请码无效"));

        if (course.getStatus() == Course.Status.ARCHIVED) {
            throw new RuntimeException("该课程已归档，暂不支持新学生加入");
        }

        if (studentCourseRepository.existsByStudentIdAndCourseId(userId, course.getId())) {
            throw new RuntimeException("您已经加入该课程");
        }

        StudentCourse sc = new StudentCourse();
        sc.setStudent(student);
        sc.setCourse(course);
        studentCourseRepository.save(sc);

        return toCourseDTOWithCounts(course);
    }

    @Transactional(readOnly = true)
    public List<CourseDTO> getStudentCourses(Long userId) {
        List<StudentCourse> studentCourses = studentCourseRepository.findByStudentId(userId);
        return studentCourses.stream()
                .map(StudentCourse::getCourse)
                .map(this::toCourseDTOWithCounts)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CourseDTO> getStudentCoursesByStatus(Long userId, Course.Status status) {
        List<StudentCourse> studentCourses = studentCourseRepository.findByStudentId(userId);
        return studentCourses.stream()
                .map(StudentCourse::getCourse)
                .filter(course -> course.getStatus() == status)
                .map(this::toCourseDTOWithCounts)
                .collect(Collectors.toList());
    }

    public List<User> getCourseStudents(Long courseId, Long userId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("课程不存在"));

        if (!course.getUser().getId().equals(userId)) {
            throw new RuntimeException("无权访问该课程");
        }

        List<StudentCourse> studentCourses = studentCourseRepository.findByCourseId(courseId);
        return studentCourses.stream()
                .map(StudentCourse::getStudent)
                .collect(Collectors.toList());
    }

    public List<User> getCourseStudents(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("课程不存在"));

        List<StudentCourse> studentCourses = studentCourseRepository.findByCourseId(courseId);
        return studentCourses.stream()
                .map(StudentCourse::getStudent)
                .collect(Collectors.toList());
    }

    public String refreshInvitationCode(Long courseId, Long userId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("课程不存在"));

        if (!course.getUser().getId().equals(userId)) {
            throw new RuntimeException("无权修改该课程");
        }

        String newCode = generateInvitationCode();
        course.setInvitationCode(newCode);
        courseRepository.save(course);
        return newCode;
    }

    public String refreshInvitationCode(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("课程不存在"));

        String newCode = generateInvitationCode();
        course.setInvitationCode(newCode);
        courseRepository.save(course);
        return newCode;
    }

    public void removeStudent(Long courseId, Long userId, Long studentId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("课程不存在"));

        if (!course.getUser().getId().equals(userId)) {
            throw new RuntimeException("无权修改该课程");
        }

        studentCourseRepository.deleteByStudentIdAndCourseId(studentId, courseId);
    }

    public void removeStudent(Long courseId, Long studentId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("课程不存在"));

        studentCourseRepository.deleteByStudentIdAndCourseId(studentId, courseId);
    }

    public Map<String, Object> getCourseStats(Long courseId, Long userId, String period) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("课程不存在"));

        if (!course.getUser().getId().equals(userId)) {
            throw new RuntimeException("无权访问该课程");
        }
        return buildCourseStats(courseId, period);
    }

    public Map<String, Object> getCourseStats(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("课程不存在"));
        return buildCourseStats(course.getId(), "week");
    }

    @Transactional
    public CourseDTO updateCourseStatus(Long courseId, Long userId, Course.Status status) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("课程不存在"));

        if (!course.getUser().getId().equals(userId)) {
            throw new RuntimeException("无权修改该课程");
        }

        course.setStatus(status);
        course.setArchivedAt(status == Course.Status.ARCHIVED ? LocalDateTime.now() : null);
        Course updatedCourse = courseRepository.save(course);
        return toCourseDTOWithCounts(updatedCourse);
    }

    private Map<String, Object> buildCourseStats(Long courseId, String period) {
        long studentCount = studentCourseRepository.countByCourseId(courseId);
        long materialCount = materialRepository.countByCourseId(courseId);
        long taskCount = taskRepository.countByCourseId(courseId);
        long quizCount = quizRepository.countByCourseId(courseId);
        long homeworkCount = homeworkRepository.countByCourseId(courseId);

        Map<String, Object> studyTrend = buildStudyTrend(courseId, period);
        List<Map<String, Object>> quizSummaries = buildQuizSummaries(courseId);
        List<Map<String, Object>> homeworkSummaries = buildHomeworkSummaries(courseId, studentCount);

        int quizAttempts = quizSummaries.stream()
                .mapToInt(item -> ((Number) item.get("attempts")).intValue())
                .sum();
        int homeworkSubmissions = homeworkSummaries.stream()
                .mapToInt(item -> ((Number) item.get("submissionCount")).intValue())
                .sum();
        int pendingHomeworkReviews = homeworkSummaries.stream()
                .mapToInt(item -> ((Number) item.get("pendingReviewCount")).intValue())
                .sum();

        double homeworkAverageScore = homeworkSummaries.stream()
                .filter(item -> ((Number) item.get("submissionCount")).intValue() > 0)
                .mapToDouble(item -> ((Number) item.get("averageScore")).doubleValue())
                .average()
                .orElse(0D);
        double homeworkAverageAccuracy = homeworkSummaries.stream()
                .filter(item -> ((Number) item.get("submissionCount")).intValue() > 0)
                .mapToDouble(item -> ((Number) item.get("averageAccuracy")).doubleValue())
                .average()
                .orElse(0D);

        Map<String, Object> result = new HashMap<>();
        result.put("studentCount", studentCount);
        result.put("materialCount", materialCount);
        result.put("taskCount", taskCount);
        result.put("quizCount", quizCount);
        result.put("homeworkCount", homeworkCount);
        result.put("quizAttempts", quizAttempts);
        result.put("homeworkSubmissions", homeworkSubmissions);
        result.put("pendingHomeworkReviews", pendingHomeworkReviews);
        result.put("homeworkAverageScore", roundTwo(homeworkAverageScore));
        result.put("homeworkAverageAccuracy", roundTwo(homeworkAverageAccuracy));
        result.put("studyTrend", studyTrend);
        result.put("quizSummaries", quizSummaries);
        result.put("homeworkSummaries", homeworkSummaries);
        result.put("homeworkQuestionInsights", buildHomeworkQuestionInsights(courseId));
        return result;
    }

    private Map<String, Object> buildStudyTrend(Long courseId, String period) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = "month".equalsIgnoreCase(period) ? endDate.minusDays(29) : endDate.minusDays(6);

        List<Long> studentIds = studentCourseRepository.findByCourseId(courseId).stream()
                .map(sc -> sc.getStudent().getId())
                .collect(Collectors.toList());

        List<String> labels = IntStream.rangeClosed(0, (int) (endDate.toEpochDay() - startDate.toEpochDay()))
                .mapToObj(i -> startDate.plusDays(i).toString())
                .collect(Collectors.toList());
        Map<String, Integer> dailyMap = new HashMap<>();
        labels.forEach(day -> dailyMap.put(day, 0));

        if (!studentIds.isEmpty()) {
            List<Object[]> daily = studyRecordRepository.getDailyDurationByCourseAndUsers(courseId, studentIds, startDate, endDate);
            for (Object[] row : daily) {
                String day = row[0].toString();
                Integer minutes = ((Number) row[1]).intValue();
                dailyMap.put(day, minutes);
            }
        }

        List<Integer> minutes = labels.stream().map(dailyMap::get).collect(Collectors.toList());
        return Map.of("labels", labels, "minutes", minutes);
    }

    private String normalizeNullable(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private List<Map<String, Object>> buildQuizSummaries(Long courseId) {
        return quizRepository.findByCourseId(courseId).stream()
                .map(quiz -> {
                    List<QuizRecord> records = quizRecordRepository.findByQuizIdOrderByCreatedAtDesc(quiz.getId());
                    int attempts = records.size();
                    double avgScore = attempts == 0
                            ? 0D
                            : records.stream().mapToInt(record -> record.getScore() == null ? 0 : record.getScore()).average().orElse(0D);
                    double avgAccuracy = attempts == 0
                            ? 0D
                            : records.stream().mapToDouble(record -> record.getAccuracy() == null ? 0D : record.getAccuracy()).average().orElse(0D);

                    Map<String, Object> item = new HashMap<>();
                    item.put("quizId", quiz.getId());
                    item.put("title", quiz.getTitle());
                    item.put("attempts", attempts);
                    item.put("avgScore", roundTwo(avgScore));
                    item.put("avgAccuracy", roundTwo(avgAccuracy * 100));
                    return item;
                })
                .collect(Collectors.toList());
    }

    private List<Map<String, Object>> buildHomeworkSummaries(Long courseId, long studentCount) {
        return homeworkRepository.findByCourseIdOrderByCreatedAtDesc(courseId).stream()
                .map(homework -> {
                    List<HomeworkSubmission> submissions = homeworkSubmissionRepository.findByHomeworkIdOrderByCreatedAtDesc(homework.getId());
                    int submissionCount = submissions.size();
                    long reviewedCount = submissions.stream()
                            .filter(submission -> submission.getStatus() == HomeworkSubmission.Status.REVIEWED
                                    || submission.getStatus() == HomeworkSubmission.Status.AUTO_GRADED)
                            .count();
                    long pendingReviewCount = submissions.stream()
                            .filter(submission -> submission.getStatus() == HomeworkSubmission.Status.REVIEW_PENDING)
                            .count();
                    double averageScore = submissions.stream()
                            .map(HomeworkSubmission::getFinalScore)
                            .filter(Objects::nonNull)
                            .mapToInt(Integer::intValue)
                            .average()
                            .orElse(0D);
                    double averageAccuracy = submissions.stream()
                            .map(HomeworkSubmission::getAccuracy)
                            .filter(Objects::nonNull)
                            .mapToDouble(Double::doubleValue)
                            .average()
                            .orElse(0D);

                    Map<String, Object> item = new HashMap<>();
                    item.put("homeworkId", homework.getId());
                    item.put("title", homework.getTitle());
                    item.put("status", homework.getStatus().name());
                    item.put("statusLabel", homework.getStatus().getLabel());
                    item.put("deadlineAt", homework.getDeadlineAt());
                    item.put("totalScore", Optional.ofNullable(homework.getTotalScore()).orElse(0));
                    item.put("submissionCount", submissionCount);
                    item.put("assignedCount", studentCount);
                    item.put("submissionRate", studentCount == 0 ? 0D : roundTwo((double) submissionCount / studentCount * 100));
                    item.put("reviewedCount", reviewedCount);
                    item.put("pendingReviewCount", pendingReviewCount);
                    item.put("averageScore", roundTwo(averageScore));
                    item.put("averageAccuracy", roundTwo(averageAccuracy * 100));
                    return item;
                })
                .collect(Collectors.toList());
    }

    private List<Map<String, Object>> buildHomeworkQuestionInsights(Long courseId) {
        List<Map<String, Object>> insights = homeworkRepository.findByCourseIdOrderByCreatedAtDesc(courseId).stream()
                .flatMap(homework -> homeworkQuestionRepository.findByHomeworkIdOrderBySortOrderAsc(homework.getId()).stream()
                        .filter(question -> question.getType() != HomeworkQuestion.Type.SHORT_ANSWER)
                        .map(question -> buildHomeworkQuestionInsight(homework, question))
                        .filter(item -> ((Number) item.get("attempts")).intValue() > 0))
                .sorted((left, right) -> Double.compare(
                        ((Number) left.get("accuracy")).doubleValue(),
                        ((Number) right.get("accuracy")).doubleValue()))
                .limit(6)
                .collect(Collectors.toList());

        return insights;
    }

    private Map<String, Object> buildHomeworkQuestionInsight(Homework homework, HomeworkQuestion question) {
        List<HomeworkAnswer> answers = homeworkAnswerRepository.findByQuestionId(question.getId());
        double accuracy = answers.isEmpty()
                ? 0D
                : answers.stream()
                .mapToDouble(answer -> {
                    int maxScore = Optional.ofNullable(question.getScore()).orElse(0);
                    if (maxScore == 0) {
                        return 0D;
                    }
                    return (double) Optional.ofNullable(answer.getScoreAwarded()).orElse(0) / maxScore;
                })
                .average()
                .orElse(0D);

        Map<String, Object> item = new HashMap<>();
        item.put("homeworkId", homework.getId());
        item.put("homeworkTitle", homework.getTitle());
        item.put("questionId", question.getId());
        item.put("content", question.getContent());
        item.put("type", question.getType().name());
        item.put("attempts", answers.size());
        item.put("accuracy", roundTwo(accuracy * 100));
        return item;
    }

    private double roundTwo(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private CourseDTO toCourseDTOWithCounts(Course course) {
        CourseDTO dto = CourseDTO.fromCourse(course);
        Long courseId = course.getId();
        dto.setStudentCount(studentCourseRepository.countByCourseId(courseId));
        dto.setMaterialCount(materialRepository.countByCourseId(courseId));
        dto.setTaskCount(taskRepository.countByCourseId(courseId));
        dto.setQuizCount(quizRepository.countByCourseId(courseId));
        return dto;
    }
}
