package com.studyhelper.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.studyhelper.dto.*;
import com.studyhelper.entity.*;
import com.studyhelper.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class HomeworkService {

    @Autowired
    private HomeworkRepository homeworkRepository;

    @Autowired
    private HomeworkQuestionRepository homeworkQuestionRepository;

    @Autowired
    private HomeworkSubmissionRepository homeworkSubmissionRepository;

    @Autowired
    private HomeworkAnswerRepository homeworkAnswerRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private StudentCourseRepository studentCourseRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional
    public HomeworkDTO createHomework(Long userId, HomeworkRequest request) {
        User teacher = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        if (teacher.getRole() != User.Role.TEACHER && teacher.getRole() != User.Role.ADMIN) {
            throw new RuntimeException("只有教师可以创建作业");
        }

        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new RuntimeException("课程不存在"));
        if (!canManageCourse(course, userId)) {
            throw new RuntimeException("无权在该课程下创建作业");
        }

        Homework homework = new Homework();
        homework.setTitle(request.getTitle());
        homework.setDescription(request.getDescription());
        homework.setDeadlineAt(request.getDeadlineAt());
        homework.setAutoGradeEnabled(Boolean.TRUE.equals(request.getAutoGradeEnabled()));
        homework.setAllowLateSubmit(Boolean.TRUE.equals(request.getAllowLateSubmit()));
        homework.setTeacher(teacher);
        homework.setCourse(course);

        int totalScore = 0;
        List<HomeworkQuestion> questions = new ArrayList<>();
        for (int i = 0; i < request.getQuestions().size(); i += 1) {
            HomeworkRequest.QuestionItem item = request.getQuestions().get(i);
            validateQuestion(item, i + 1);

            HomeworkQuestion question = new HomeworkQuestion();
            question.setSortOrder(i + 1);
            question.setContent(item.getContent().trim());
            question.setType(item.getType());
            question.setScore(item.getScore());
            question.setAnalysis(item.getAnalysis());
            question.setManualReviewRequired(Boolean.TRUE.equals(item.getManualReviewRequired()) || item.getType() == HomeworkQuestion.Type.SHORT_ANSWER);
            question.setOptions(writeValueAsString(item.getOptions() == null ? List.of() : item.getOptions()));
            question.setAnswer(item.getAnswer());
            question.setHomework(homework);
            questions.add(question);
            totalScore += item.getScore();
        }

        homework.setQuestionCount(questions.size());
        homework.setTotalScore(totalScore);

        Homework savedHomework = homeworkRepository.save(homework);
        homeworkQuestionRepository.saveAll(questions);
        return HomeworkDTO.fromHomework(savedHomework);
    }

    @Transactional(readOnly = true)
    public List<HomeworkDTO> getTeacherHomeworks(Long userId, Long courseId) {
        User user = getRequiredUser(userId);
        if (user.getRole() != User.Role.TEACHER && user.getRole() != User.Role.ADMIN) {
            throw new RuntimeException("只有教师可以查看作业列表");
        }

        List<Homework> homeworks = courseId == null
                ? homeworkRepository.findByTeacherIdOrderByCreatedAtDesc(userId)
                : homeworkRepository.findByTeacherIdAndCourseIdOrderByCreatedAtDesc(userId, courseId);

        return homeworks.stream()
                .map(HomeworkDTO::fromHomework)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<HomeworkDTO> getStudentHomeworks(Long userId) {
        getRequiredUser(userId);
        List<Long> courseIds = studentCourseRepository.findByStudentId(userId).stream()
                .map(studentCourse -> studentCourse.getCourse().getId())
                .collect(Collectors.toList());

        if (courseIds.isEmpty()) {
            return List.of();
        }

        return homeworkRepository.findByCourseIdInAndStatusOrderByDeadlineAtAscCreatedAtDesc(courseIds, Homework.Status.PUBLISHED).stream()
                .map(homework -> {
                    HomeworkDTO dto = HomeworkDTO.fromHomework(homework);
                    homeworkSubmissionRepository.findTopByHomeworkIdAndStudentIdOrderByCreatedAtDesc(homework.getId(), userId)
                            .ifPresent(dto::applySubmission);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getHomeworkDetail(Long homeworkId, Long userId) {
        Homework homework = getRequiredHomework(homeworkId);
        boolean manager = canManageHomework(homework, userId);
        boolean studentAccess = canAccessHomeworkAsStudent(homework, userId);

        if (!manager && !studentAccess) {
            throw new RuntimeException("无权查看该作业");
        }
        if (!manager && homework.getStatus() != Homework.Status.PUBLISHED) {
            throw new RuntimeException("该作业尚未发布");
        }

        List<HomeworkQuestionDTO> questions = homeworkQuestionRepository.findByHomeworkIdOrderBySortOrderAsc(homeworkId).stream()
                .map(question -> HomeworkQuestionDTO.fromQuestion(question, manager))
                .collect(Collectors.toList());

        HomeworkDTO homeworkDTO = HomeworkDTO.fromHomework(homework);
        Map<String, Object> result = new HashMap<>();
        result.put("homework", homeworkDTO);
        result.put("questions", questions);

        if (manager) {
            result.put("summary", buildSummary(homework));
        } else {
            homeworkSubmissionRepository.findTopByHomeworkIdAndStudentIdOrderByCreatedAtDesc(homeworkId, userId)
                    .ifPresent(submission -> {
                        homeworkDTO.applySubmission(submission);
                        result.put("latestSubmission", HomeworkSubmissionDTO.fromSubmission(submission));
                    });
            result.put("canSubmit", homework.getStatus() == Homework.Status.PUBLISHED &&
                    homeworkSubmissionRepository.findTopByHomeworkIdAndStudentIdOrderByCreatedAtDesc(homeworkId, userId).isEmpty());
        }

        return result;
    }

    @Transactional
    public HomeworkDTO updateHomeworkStatus(Long userId, Long homeworkId, Homework.Status status) {
        Homework homework = getRequiredHomework(homeworkId);
        if (!canManageHomework(homework, userId)) {
            throw new RuntimeException("无权修改该作业状态");
        }

        homework.setStatus(status);
        Homework saved = homeworkRepository.save(homework);
        return HomeworkDTO.fromHomework(saved);
    }

    @Transactional
    public void deleteHomework(Long userId, Long homeworkId) {
        Homework homework = getRequiredHomework(homeworkId);
        if (!canManageHomework(homework, userId)) {
            throw new RuntimeException("无权删除该作业");
        }
        homeworkRepository.delete(homework);
    }

    @Transactional
    public Map<String, Object> submitHomework(Long userId, Long homeworkId, HomeworkSubmitRequest request) {
        Homework homework = getRequiredHomework(homeworkId);
        if (!canAccessHomeworkAsStudent(homework, userId)) {
            throw new RuntimeException("无权提交该作业");
        }
        if (homework.getStatus() != Homework.Status.PUBLISHED) {
            throw new RuntimeException("该作业当前不可提交");
        }
        if (homeworkSubmissionRepository.findTopByHomeworkIdAndStudentIdOrderByCreatedAtDesc(homeworkId, userId).isPresent()) {
            throw new RuntimeException("你已经提交过该作业");
        }

        boolean isLate = homework.getDeadlineAt() != null && LocalDateTime.now().isAfter(homework.getDeadlineAt());
        if (isLate && !Boolean.TRUE.equals(homework.getAllowLateSubmit())) {
            throw new RuntimeException("已超过截止时间，当前不允许补交");
        }

        User student = getRequiredUser(userId);
        Map<Long, HomeworkSubmitRequest.AnswerItem> submittedAnswerMap = request.getAnswers().stream()
                .collect(Collectors.toMap(HomeworkSubmitRequest.AnswerItem::getQuestionId, Function.identity(), (left, right) -> right));

        List<HomeworkQuestion> questions = homeworkQuestionRepository.findByHomeworkIdOrderBySortOrderAsc(homeworkId);
        HomeworkSubmission submission = new HomeworkSubmission();
        submission.setHomework(homework);
        submission.setStudent(student);
        submission.setIsLate(isLate);
        submission.setSubmittedAt(LocalDateTime.now());

        int objectiveScore = 0;
        int pendingReviewCount = 0;
        List<HomeworkAnswer> answerEntities = new ArrayList<>();

        for (HomeworkQuestion question : questions) {
            HomeworkSubmitRequest.AnswerItem answerItem = submittedAnswerMap.get(question.getId());
            HomeworkAnswer answer = new HomeworkAnswer();
            answer.setSubmission(submission);
            answer.setQuestion(question);
            answer.setStudentAnswer(answerItem != null ? answerItem.getAnswer() : null);
            gradeAnswer(homework, question, answer);

            objectiveScore += answer.getAutoScore();
            if (Boolean.TRUE.equals(answer.getNeedsReview())) {
                pendingReviewCount += 1;
            }
            answerEntities.add(answer);
        }

        submission.setObjectiveScore(objectiveScore);
        submission.setSubjectiveScore(0);
        submission.setFinalScore(objectiveScore);
        submission.setAccuracy(homework.getTotalScore() == 0 ? 0D : roundTwo((double) objectiveScore / homework.getTotalScore()));
        submission.setAutoGradedAt(LocalDateTime.now());
        submission.setStatus(pendingReviewCount > 0 ? HomeworkSubmission.Status.REVIEW_PENDING : HomeworkSubmission.Status.AUTO_GRADED);

        HomeworkSubmission savedSubmission = homeworkSubmissionRepository.save(submission);
        homeworkAnswerRepository.saveAll(answerEntities);

        Map<String, Object> result = new HashMap<>();
        result.put("submission", HomeworkSubmissionDTO.fromSubmission(savedSubmission));
        result.put("pendingReviewCount", pendingReviewCount);
        result.put("objectiveScore", objectiveScore);
        result.put("finalScore", savedSubmission.getFinalScore());
        return result;
    }

    @Transactional(readOnly = true)
    public List<HomeworkSubmissionDTO> getHomeworkSubmissions(Long userId, Long homeworkId) {
        Homework homework = getRequiredHomework(homeworkId);
        if (!canManageHomework(homework, userId)) {
            throw new RuntimeException("无权查看提交记录");
        }

        return homeworkSubmissionRepository.findByHomeworkIdOrderByCreatedAtDesc(homeworkId).stream()
                .map(HomeworkSubmissionDTO::fromSubmission)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getSubmissionDetail(Long userId, Long submissionId) {
        HomeworkSubmission submission = homeworkSubmissionRepository.findById(submissionId)
                .orElseThrow(() -> new RuntimeException("提交记录不存在"));

        boolean manager = canManageHomework(submission.getHomework(), userId);
        boolean owner = Objects.equals(submission.getStudent().getId(), userId);
        if (!manager && !owner) {
            throw new RuntimeException("无权查看该提交详情");
        }

        List<HomeworkAnswerDTO> answers = homeworkAnswerRepository.findBySubmissionIdOrderByIdAsc(submissionId).stream()
                .map(answer -> HomeworkAnswerDTO.fromAnswer(answer, true))
                .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("homework", HomeworkDTO.fromHomework(submission.getHomework()));
        result.put("submission", HomeworkSubmissionDTO.fromSubmission(submission));
        result.put("answers", answers);
        result.put("canReview", manager);
        return result;
    }

    @Transactional
    public Map<String, Object> reviewSubmission(Long userId, Long submissionId, HomeworkReviewRequest request) {
        HomeworkSubmission submission = homeworkSubmissionRepository.findById(submissionId)
                .orElseThrow(() -> new RuntimeException("提交记录不存在"));

        if (!canManageHomework(submission.getHomework(), userId)) {
            throw new RuntimeException("无权复核该提交");
        }

        Map<Long, HomeworkReviewRequest.ReviewedAnswerItem> reviewedMap = Optional.ofNullable(request.getAnswers())
                .orElseGet(List::of)
                .stream()
                .collect(Collectors.toMap(HomeworkReviewRequest.ReviewedAnswerItem::getAnswerId, Function.identity(), (left, right) -> right));

        List<HomeworkAnswer> answers = homeworkAnswerRepository.findBySubmissionIdOrderByIdAsc(submissionId);
        boolean hasPending = false;

        for (HomeworkAnswer answer : answers) {
            if (!Boolean.TRUE.equals(answer.getNeedsReview())) {
                continue;
            }

            HomeworkReviewRequest.ReviewedAnswerItem reviewed = reviewedMap.get(answer.getId());
            if (reviewed == null) {
                hasPending = true;
                continue;
            }

            int maxScore = Optional.ofNullable(answer.getQuestion().getScore()).orElse(0);
            int teacherScore = Math.max(0, Math.min(reviewed.getTeacherScore(), maxScore));
            answer.setTeacherScore(teacherScore);
            answer.setTeacherComment(reviewed.getTeacherComment());
            answer.setScoreAwarded(teacherScore);
            answer.setReviewStatus(HomeworkAnswer.ReviewStatus.REVIEWED);
            answer.setIsCorrect(teacherScore >= maxScore);
        }

        int objectiveScore = 0;
        int subjectiveScore = 0;
        for (HomeworkAnswer answer : answers) {
            if (answer.getQuestion().getType() == HomeworkQuestion.Type.SHORT_ANSWER) {
                subjectiveScore += Optional.ofNullable(answer.getScoreAwarded()).orElse(0);
            } else {
                objectiveScore += Optional.ofNullable(answer.getScoreAwarded()).orElse(0);
            }

            if (Boolean.TRUE.equals(answer.getNeedsReview()) && answer.getReviewStatus() != HomeworkAnswer.ReviewStatus.REVIEWED) {
                hasPending = true;
            }
        }

        submission.setObjectiveScore(objectiveScore);
        submission.setSubjectiveScore(subjectiveScore);
        submission.setFinalScore(objectiveScore + subjectiveScore);
        submission.setAccuracy(submission.getHomework().getTotalScore() == 0
                ? 0D
                : roundTwo((double) submission.getFinalScore() / submission.getHomework().getTotalScore()));
        submission.setTeacherComment(request.getTeacherComment());
        submission.setReviewedAt(LocalDateTime.now());
        submission.setStatus(hasPending ? HomeworkSubmission.Status.REVIEW_PENDING : HomeworkSubmission.Status.REVIEWED);

        homeworkAnswerRepository.saveAll(answers);
        HomeworkSubmission saved = homeworkSubmissionRepository.save(submission);
        return getSubmissionDetail(userId, saved.getId());
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getHomeworkSummary(Long userId, Long homeworkId) {
        Homework homework = getRequiredHomework(homeworkId);
        if (!canManageHomework(homework, userId)) {
            throw new RuntimeException("无权查看该作业统计");
        }
        return buildSummary(homework);
    }

    private Map<String, Object> buildSummary(Homework homework) {
        List<HomeworkQuestion> questions = homeworkQuestionRepository.findByHomeworkIdOrderBySortOrderAsc(homework.getId());
        List<HomeworkSubmission> submissions = homeworkSubmissionRepository.findByHomeworkIdOrderByCreatedAtDesc(homework.getId());

        long assignedCount = studentCourseRepository.countByCourseId(homework.getCourse().getId());
        long submittedCount = submissions.size();
        long reviewedCount = submissions.stream()
                .filter(submission -> submission.getStatus() == HomeworkSubmission.Status.REVIEWED || submission.getStatus() == HomeworkSubmission.Status.AUTO_GRADED)
                .count();

        double avgScore = submissions.stream()
                .map(HomeworkSubmission::getFinalScore)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0D);

        List<Map<String, Object>> questionAccuracy = questions.stream()
                .map(question -> {
                    List<HomeworkAnswer> answers = homeworkAnswerRepository.findByQuestionId(question.getId());
                    double avgRate = answers.isEmpty() ? 0D : answers.stream()
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
                    item.put("questionId", question.getId());
                    item.put("content", question.getContent());
                    item.put("type", question.getType().name());
                    item.put("attempts", answers.size());
                    item.put("accuracy", roundTwo(avgRate * 100));
                    return item;
                })
                .collect(Collectors.toList());

        Map<String, Object> summary = new HashMap<>();
        summary.put("assignedCount", assignedCount);
        summary.put("submittedCount", submittedCount);
        summary.put("reviewedCount", reviewedCount);
        summary.put("submissionRate", assignedCount == 0 ? 0D : roundTwo((double) submittedCount / assignedCount * 100));
        summary.put("averageScore", roundTwo(avgScore));
        summary.put("questionAccuracy", questionAccuracy);
        return summary;
    }

    private void gradeAnswer(Homework homework, HomeworkQuestion question, HomeworkAnswer answer) {
        if (!Boolean.TRUE.equals(homework.getAutoGradeEnabled())) {
            answer.setNeedsReview(true);
            answer.setReviewStatus(HomeworkAnswer.ReviewStatus.PENDING);
            answer.setIsCorrect(null);
            answer.setAutoScore(0);
            answer.setScoreAwarded(0);
            answer.setMatchDetail("已关闭自动批改，等待教师复核");
            return;
        }

        switch (question.getType()) {
            case SINGLE_CHOICE, TRUE_FALSE -> {
                boolean correct = normalizeChoice(answer.getStudentAnswer()).equals(normalizeChoice(question.getAnswer()));
                answer.setIsCorrect(correct);
                answer.setAutoScore(correct ? question.getScore() : 0);
                answer.setScoreAwarded(answer.getAutoScore());
                answer.setNeedsReview(false);
                answer.setReviewStatus(HomeworkAnswer.ReviewStatus.NOT_REQUIRED);
                answer.setMatchDetail(correct ? "答案匹配" : "答案不匹配");
            }
            case FILL_BLANK -> {
                List<String> acceptedAnswers = parseAcceptedAnswers(question.getAnswer());
                String normalizedStudentAnswer = normalizeText(answer.getStudentAnswer());
                boolean correct = acceptedAnswers.stream().map(this::normalizeText).anyMatch(normalizedStudentAnswer::equals);
                answer.setIsCorrect(correct);
                answer.setAutoScore(correct ? question.getScore() : 0);
                answer.setScoreAwarded(answer.getAutoScore());
                answer.setNeedsReview(false);
                answer.setReviewStatus(HomeworkAnswer.ReviewStatus.NOT_REQUIRED);
                answer.setMatchDetail(correct ? "匹配预设答案" : "未命中预设答案");
            }
            case SHORT_ANSWER -> {
                answer.setNeedsReview(true);
                answer.setReviewStatus(HomeworkAnswer.ReviewStatus.PENDING);
                answer.setIsCorrect(null);
                answer.setAutoScore(0);
                answer.setScoreAwarded(0);
                answer.setMatchDetail("主观题已提交，等待教师复核");
            }
        }
    }

    private List<String> parseAcceptedAnswers(String answer) {
        if (answer == null || answer.isBlank()) {
            return List.of();
        }

        try {
            if (answer.trim().startsWith("[")) {
                return objectMapper.readValue(answer, new TypeReference<List<String>>() {
                });
            }
        } catch (Exception ignored) {
        }
        return List.of(answer);
    }

    private String normalizeChoice(String value) {
        return value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizeText(String value) {
        return value == null ? "" : value.trim().replaceAll("\\s+", "").toLowerCase(Locale.ROOT);
    }

    private void validateQuestion(HomeworkRequest.QuestionItem item, int order) {
        if (item.getType() == null) {
            throw new RuntimeException("第" + order + "题的类型不能为空");
        }
        if (item.getScore() == null || item.getScore() < 1) {
            throw new RuntimeException("第" + order + "题的分值必须大于 0");
        }
        switch (item.getType()) {
            case SINGLE_CHOICE -> {
                if (item.getOptions() == null || item.getOptions().size() < 2 || item.getOptions().stream().anyMatch(option -> option == null || option.isBlank())) {
                    throw new RuntimeException("第" + order + "题的选项不完整");
                }
                if (item.getAnswer() == null || item.getAnswer().isBlank()) {
                    throw new RuntimeException("第" + order + "题未设置正确答案");
                }
            }
            case TRUE_FALSE, FILL_BLANK -> {
                if (item.getAnswer() == null || item.getAnswer().isBlank()) {
                    throw new RuntimeException("第" + order + "题未设置参考答案");
                }
            }
            case SHORT_ANSWER -> {
                if (item.getAnswer() == null) {
                    item.setAnswer("");
                }
            }
        }
    }

    private String writeValueAsString(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            return "[]";
        }
    }

    private User getRequiredUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
    }

    private Homework getRequiredHomework(Long homeworkId) {
        return homeworkRepository.findById(homeworkId)
                .orElseThrow(() -> new RuntimeException("作业不存在"));
    }

    private boolean canManageHomework(Homework homework, Long userId) {
        User currentUser = getRequiredUser(userId);
        return Objects.equals(homework.getTeacher().getId(), userId) || currentUser.getRole() == User.Role.ADMIN;
    }

    private boolean canManageCourse(Course course, Long userId) {
        User currentUser = getRequiredUser(userId);
        return Objects.equals(course.getUser().getId(), userId) || currentUser.getRole() == User.Role.ADMIN;
    }

    private boolean canAccessHomeworkAsStudent(Homework homework, Long userId) {
        return studentCourseRepository.existsByStudentIdAndCourseId(userId, homework.getCourse().getId());
    }

    private double roundTwo(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
