package com.studyhelper.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.studyhelper.dto.QuestionDTO;
import com.studyhelper.dto.QuizDTO;
import com.studyhelper.dto.QuizSubmitRequest;
import com.studyhelper.entity.*;
import com.studyhelper.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class QuizService {

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private QuizRecordRepository quizRecordRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private StudentCourseRepository studentCourseRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // 创建测验
    @Transactional
    public QuizDTO createQuiz(Long userId, String title, String description, 
                             Integer totalTime, Long courseId, Integer maxAttempts,
                             Boolean shuffleQuestions, Boolean autoSaveEnabled,
                             List<Map<String, Object>> questions) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        Quiz quiz = new Quiz();
        quiz.setTitle(title);
        quiz.setDescription(description);
        quiz.setTotalTime(totalTime);
        quiz.setQuestionCount(questions.size());
        quiz.setMaxAttempts(maxAttempts == null || maxAttempts < 1 ? 1 : maxAttempts);
        quiz.setShuffleQuestions(Boolean.TRUE.equals(shuffleQuestions));
        quiz.setAutoSaveEnabled(!Boolean.FALSE.equals(autoSaveEnabled));
        quiz.setType(Quiz.Type.PRACTICE);
        quiz.setStatus(Quiz.Status.DRAFT);
        quiz.setUser(user);

        if (courseId != null) {
            Course course = courseRepository.findById(courseId)
                    .orElseThrow(() -> new RuntimeException("课程不存在"));
            if (!course.getUser().getId().equals(userId)) {
                throw new RuntimeException("无权关联该课程");
            }
            quiz.setCourse(course);
        }

        Quiz savedQuiz = quizRepository.save(quiz);

        // 保存题目
        List<Question> questionList = new ArrayList<>();
        for (Map<String, Object> q : questions) {
            Question question = new Question();
            question.setContent((String) q.get("content"));
            question.setType(Question.Type.valueOf((String) q.get("type")));
            try {
                Object options = q.get("options");
                if (options instanceof String optionsJson) {
                    question.setOptions(optionsJson);
                } else {
                    question.setOptions(objectMapper.writeValueAsString(options));
                }
            } catch (Exception e) {
                throw new RuntimeException("题目选项序列化失败", e);
            }
            question.setAnswer((String) q.get("answer"));
            question.setScore(((Number) q.get("score")).intValue());
            question.setAnalysis((String) q.get("analysis"));
            question.setQuiz(savedQuiz);
            questionList.add(question);
        }
        questionRepository.saveAll(questionList);

        return QuizDTO.fromQuiz(savedQuiz);
    }

    // 获取用户可访问的测验
    @Transactional(readOnly = true)
    public List<QuizDTO> getUserQuizzes(Long userId) {
        List<Quiz> quizzes = quizRepository.findAccessibleQuizzes(userId);
        return quizzes.stream()
                .map(quiz -> toQuizDTOForUser(quiz, userId))
                .collect(Collectors.toList());
    }

    // 获取可参加的测验
    @Transactional(readOnly = true)
    public List<QuizDTO> getAvailableQuizzes(Long userId) {
        List<Quiz> quizzes = quizRepository.findAvailableQuizzes(userId);
        return quizzes.stream()
                .map(quiz -> toQuizDTOForUser(quiz, userId))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<QuizDTO> getCourseQuizzes(Long userId, Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("课程不存在"));

        boolean hasAccess = course.getUser().getId().equals(userId) ||
                studentCourseRepository.existsByStudentIdAndCourseId(userId, courseId);

        if (!hasAccess) {
            throw new RuntimeException("无权访问该课程测验");
        }

        List<Quiz> quizzes = quizRepository.findByCourseId(courseId);
        return quizzes.stream()
                .map(quiz -> toQuizDTOForUser(quiz, userId))
                .collect(Collectors.toList());
    }

    @Transactional
    public QuizDTO updateQuizStatus(Long userId, Long quizId, Quiz.Status status) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("测验不存在"));

        boolean canEdit = quiz.getUser().getId().equals(userId) ||
                (quiz.getCourse() != null && quiz.getCourse().getUser().getId().equals(userId));

        if (!canEdit) {
            throw new RuntimeException("无权修改该测验");
        }

        quiz.setStatus(status);
        Quiz saved = quizRepository.save(quiz);
        return QuizDTO.fromQuiz(saved);
    }

    @Transactional
    public void deleteQuiz(Long userId, Long quizId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("测验不存在"));

        boolean canDelete = quiz.getUser().getId().equals(userId) ||
                (quiz.getCourse() != null && quiz.getCourse().getUser().getId().equals(userId));

        if (!canDelete) {
            throw new RuntimeException("无权删除该测验");
        }

        quizRepository.delete(quiz);
    }

    // 获取测验详情（包括题目）
    @Transactional(readOnly = true)
    public Map<String, Object> getQuizDetail(Long quizId, Long userId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("测验不存在"));

        if (!canAccessQuiz(quiz, userId)) {
            throw new RuntimeException("无权访问该测验");
        }

        QuizDTO quizDTO = toQuizDTOForUser(quiz, userId);
        if (!isTeacherOrOwner(quiz, userId) && quizDTO.isAttemptLimitReached()) {
            throw new RuntimeException("已达到该测验的作答次数上限");
        }

        List<Question> questions = questionRepository.findByQuizIdOrderByCreatedAtAsc(quizId);
        List<QuestionDTO> questionDTOs = buildQuestionDTOs(quiz, userId, questions);

        Map<String, Object> result = Map.of(
            "quiz", quizDTO,
            "questions", questionDTOs
        );
        return result;
    }

    // 提交测验答案并自动批改
    @Transactional
    public Map<String, Object> submitQuiz(Long userId, Long quizId, QuizSubmitRequest request) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("测验不存在"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        if (!canAccessQuiz(quiz, userId)) {
            throw new RuntimeException("无权提交该测验");
        }

        long attemptsUsed = quizRecordRepository.countByUserIdAndQuizId(userId, quizId);
        int maxAttempts = quiz.getMaxAttempts() == null || quiz.getMaxAttempts() < 1 ? 1 : quiz.getMaxAttempts();
        if (!isTeacherOrOwner(quiz, userId) && attemptsUsed >= maxAttempts) {
            throw new RuntimeException("已达到该测验的作答次数上限");
        }

        Map<String, String> userAnswers = request == null || request.getAnswers() == null
                ? Map.of()
                : request.getAnswers();

        List<Question> questions = questionRepository.findByQuizIdOrderByCreatedAtAsc(quizId);
        int totalScore = questions.stream().mapToInt(Question::getScore).sum();
        int userScore = 0;
        List<Long> wrongQuestionIds = new ArrayList<>();

        // 批改
        for (Question question : questions) {
            String userAnswer = userAnswers.get(question.getId().toString());
            if (userAnswer != null && userAnswer.equals(question.getAnswer())) {
                userScore += question.getScore();
            } else {
                wrongQuestionIds.add(question.getId());
            }
        }

        double accuracy = totalScore == 0 ? 0D : (double) userScore / totalScore;

        // 保存记录
        QuizRecord record = new QuizRecord();
        record.setUser(user);
        record.setQuiz(quiz);
        record.setScore(userScore);
        record.setTotalScore(totalScore);
        record.setAccuracy(accuracy);
        record.setAnswers(writeValueAsString(userAnswers));
        record.setWrongQuestions(writeValueAsString(wrongQuestionIds));
        record.setPassed(accuracy >= 0.6); // 60%及格
        int timeUsed = normalizeTimeUsed(request == null ? null : request.getTimeUsed(), quiz.getTotalTime());
        LocalDateTime finishedAt = LocalDateTime.now();
        record.setTimeUsed(timeUsed);
        record.setStartedAt(finishedAt.minusSeconds(timeUsed));
        record.setFinishedAt(finishedAt);

        QuizRecord savedRecord = quizRecordRepository.save(record);

        return Map.of(
            "recordId", savedRecord.getId(),
            "attemptNumber", attemptsUsed + 1,
            "score", userScore,
            "totalScore", totalScore,
            "accuracy", accuracy,
            "isPassed", record.getPassed(),
            "wrongQuestions", wrongQuestionIds,
            "timeUsed", timeUsed
        );
    }

    // 获取用户的测验记录
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getUserRecords(Long userId) {
        return quizRecordRepository.findByUserId(userId).stream()
                .map(record -> {
                    Map<String, Object> recordMap = new HashMap<>();
                    recordMap.put("id", record.getId());
                    recordMap.put("quizId", record.getQuiz().getId());
                    recordMap.put("quizTitle", record.getQuiz().getTitle());
                    recordMap.put("score", record.getScore());
                    recordMap.put("totalScore", record.getTotalScore());
                    recordMap.put("accuracy", record.getAccuracy());
                    recordMap.put("isPassed", record.getPassed());
                    recordMap.put("timeUsed", record.getTimeUsed());
                    recordMap.put("createdAt", record.getCreatedAt());
                    return recordMap;
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getRecordDetail(Long userId, Long recordId) {
        QuizRecord record = quizRecordRepository.findByIdAndUserId(recordId, userId)
                .orElseThrow(() -> new RuntimeException("测验记录不存在"));

        List<Long> wrongQuestionIds = parseWrongQuestionIds(record.getWrongQuestions());
        QuizDTO quizDTO = toQuizDTOForUser(record.getQuiz(), userId);

        return Map.of(
                "id", record.getId(),
                "quiz", quizDTO,
                "score", record.getScore(),
                "totalScore", record.getTotalScore(),
                "accuracy", record.getAccuracy(),
                "isPassed", record.getPassed(),
                "timeUsed", record.getTimeUsed() == null ? 0 : record.getTimeUsed(),
                "answers", parseAnswerMap(record.getAnswers()),
                "wrongQuestions", wrongQuestionIds,
                "createdAt", record.getCreatedAt()
        );
    }

    // 获取错题记录
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getWrongQuestions(Long userId) {
        List<QuizRecord> records = quizRecordRepository.findRecordsWithWrongQuestions(userId);
        List<Map<String, Object>> wrongQuestions = new ArrayList<>();

        for (QuizRecord record : records) {
            try {
                List<Long> questionIds = objectMapper.readValue(
                    record.getWrongQuestions(), new TypeReference<List<Long>>() {});
                
                for (Long questionId : questionIds) {
                    Question question = questionRepository.findById(questionId).orElse(null);
                    if (question != null) {
                        wrongQuestions.add(Map.of(
                            "question", QuestionDTO.fromQuestion(question),
                            "quizTitle", question.getQuiz().getTitle(),
                            "recordedAt", record.getCreatedAt()
                        ));
                    }
                }
            } catch (Exception e) {
                // ignore
            }
        }

        return wrongQuestions;
    }

    private String writeValueAsString(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return "{}";
        }
    }

    private QuizDTO toQuizDTOForUser(Quiz quiz, Long userId) {
        QuizDTO dto = QuizDTO.fromQuiz(quiz);
        int attemptsUsed = (int) quizRecordRepository.countByUserIdAndQuizId(userId, quiz.getId());
        int maxAttempts = quiz.getMaxAttempts() == null || quiz.getMaxAttempts() < 1 ? 1 : quiz.getMaxAttempts();
        dto.setAttemptsUsed(attemptsUsed);
        dto.setRemainingAttempts(Math.max(0, maxAttempts - attemptsUsed));
        dto.setAttemptLimitReached(attemptsUsed >= maxAttempts);
        return dto;
    }

    private List<QuestionDTO> buildQuestionDTOs(Quiz quiz, Long userId, List<Question> sourceQuestions) {
        boolean includeAnswer = isTeacherOrOwner(quiz, userId);
        List<Question> questions = new ArrayList<>(sourceQuestions);
        if (Boolean.TRUE.equals(quiz.getShuffleQuestions()) && questions.size() > 1 && !isTeacherOrOwner(quiz, userId)) {
            int seed = Math.floorMod(Objects.hash(quiz.getId(), userId), questions.size() - 1) + 1;
            Collections.rotate(questions, seed);
        }
        return questions.stream()
                .map(question -> QuestionDTO.fromQuestion(question, includeAnswer))
                .collect(Collectors.toList());
    }

    private int normalizeTimeUsed(Integer providedTimeUsed, Integer totalTimeMinutes) {
        int fallback = 0;
        if (providedTimeUsed == null) {
            return fallback;
        }
        int normalized = Math.max(0, providedTimeUsed);
        if (totalTimeMinutes != null && totalTimeMinutes > 0) {
            return Math.min(normalized, totalTimeMinutes * 60);
        }
        return normalized;
    }

    private List<Long> parseWrongQuestionIds(String rawWrongQuestions) {
        try {
            if (rawWrongQuestions == null || rawWrongQuestions.isBlank()) {
                return List.of();
            }
            return objectMapper.readValue(rawWrongQuestions, new TypeReference<List<Long>>() {});
        } catch (Exception e) {
            return List.of();
        }
    }

    private Map<String, String> parseAnswerMap(String rawAnswers) {
        try {
            if (rawAnswers == null || rawAnswers.isBlank()) {
                return Map.of();
            }
            return objectMapper.readValue(rawAnswers, new TypeReference<Map<String, String>>() {});
        } catch (Exception e) {
            return Map.of();
        }
    }

    private boolean canAccessQuiz(Quiz quiz, Long userId) {
        return quiz.getUser().getId().equals(userId) ||
                (quiz.getCourse() != null && quiz.getCourse().getUser().getId().equals(userId)) ||
                (quiz.getCourse() != null && studentCourseRepository.existsByStudentIdAndCourseId(userId, quiz.getCourse().getId()));
    }

    private boolean isTeacherOrOwner(Quiz quiz, Long userId) {
        return quiz.getUser().getId().equals(userId) ||
                (quiz.getCourse() != null && quiz.getCourse().getUser().getId().equals(userId));
    }
}
