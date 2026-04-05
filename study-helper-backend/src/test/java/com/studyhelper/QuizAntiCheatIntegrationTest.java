package com.studyhelper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.studyhelper.entity.Course;
import com.studyhelper.entity.Question;
import com.studyhelper.entity.Quiz;
import com.studyhelper.entity.User;
import com.studyhelper.repository.CourseRepository;
import com.studyhelper.repository.QuestionRepository;
import com.studyhelper.repository.QuizRecordRepository;
import com.studyhelper.repository.QuizRepository;
import com.studyhelper.repository.StudentCourseRepository;
import com.studyhelper.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class QuizAntiCheatIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private QuizRecordRepository quizRecordRepository;

    @Autowired
    private StudentCourseRepository studentCourseRepository;

    @BeforeEach
    void setUp() {
        quizRecordRepository.deleteAll();
        questionRepository.deleteAll();
        quizRepository.deleteAll();
        studentCourseRepository.deleteAll();
        courseRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void quizShouldExposeAntiCheatSettingsAndBlockSecondAttempt() throws Exception {
        User teacher = createUser("quiz_teacher", User.Role.TEACHER, "TeacherPass123!");
        teacher.setIsVerified(true);
        teacher.setEmployeeId("T" + UUID.randomUUID().toString().substring(0, 6));
        teacher = userRepository.save(teacher);

        User student = createUser("quiz_student", User.Role.STUDENT, "StudentPass123!");
        student.setStudentId("SID_" + UUID.randomUUID().toString().substring(0, 8));
        student = userRepository.save(student);

        Course course = new Course();
        course.setName("算法设计");
        course.setCategory(Course.Category.REQUIRED);
        course.setTeacher(teacher.getUsername());
        course.setInvitationCode("IC" + UUID.randomUUID().toString().substring(0, 6));
        course.setUser(teacher);
        course = courseRepository.save(course);

        com.studyhelper.entity.StudentCourse enrollment = new com.studyhelper.entity.StudentCourse();
        enrollment.setCourse(course);
        enrollment.setStudent(student);
        studentCourseRepository.save(enrollment);

        Quiz quiz = new Quiz();
        quiz.setTitle("期中练习");
        quiz.setDescription("防作弊测试");
        quiz.setTotalTime(20);
        quiz.setQuestionCount(3);
        quiz.setMaxAttempts(1);
        quiz.setShuffleQuestions(true);
        quiz.setAutoSaveEnabled(true);
        quiz.setType(Quiz.Type.EXAM);
        quiz.setStatus(Quiz.Status.PUBLISHED);
        quiz.setUser(teacher);
        quiz.setCourse(course);
        quiz = quizRepository.save(quiz);

        Question q1 = createQuestion(quiz, "第一题", "A");
        Question q2 = createQuestion(quiz, "第二题", "B");
        Question q3 = createQuestion(quiz, "第三题", "C");

        String token = login("quiz_student", "StudentPass123!");

        mockMvc.perform(get("/api/quiz/{quizId}", quiz.getId())
                        .param("userId", student.getId().toString())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.quiz.maxAttempts").value(1))
                .andExpect(jsonPath("$.data.quiz.shuffleQuestions").value(true))
                .andExpect(jsonPath("$.data.quiz.autoSaveEnabled").value(true))
                .andExpect(jsonPath("$.data.quiz.remainingAttempts").value(1))
                .andExpect(jsonPath("$.data.questions[0].id").value(not(q1.getId().intValue())))
                .andExpect(jsonPath("$.data.questions[0].answer").isEmpty());

        String submitResponse = mockMvc.perform(post("/api/quiz/{quizId}/submit", quiz.getId())
                        .param("userId", student.getId().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "answers", Map.of(
                                        q1.getId().toString(), "A",
                                        q2.getId().toString(), "B",
                                        q3.getId().toString(), "C"
                                ),
                                "timeUsed", 180,
                                "autoSubmitted", false
                        )))
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.attemptNumber").value(1))
                .andExpect(jsonPath("$.data.timeUsed").value(180))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode submitJson = objectMapper.readTree(submitResponse);
        long recordId = submitJson.path("data").path("recordId").asLong();

        mockMvc.perform(get("/api/quiz/records/{recordId}", recordId)
                        .param("userId", student.getId().toString())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.score").value(3))
                .andExpect(jsonPath("$.data.timeUsed").value(180));

        mockMvc.perform(post("/api/quiz/{quizId}/submit", quiz.getId())
                        .param("userId", student.getId().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "answers", Map.of(q1.getId().toString(), "A"),
                                "timeUsed", 30,
                                "autoSubmitted", false
                        )))
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("已达到该测验的作答次数上限"));
    }

    private Question createQuestion(Quiz quiz, String content, String answer) {
        Question question = new Question();
        question.setQuiz(quiz);
        question.setContent(content);
        question.setType(Question.Type.SINGLE_CHOICE);
        question.setOptions("[\"选项A\",\"选项B\",\"选项C\",\"选项D\"]");
        question.setAnswer(answer);
        question.setScore(1);
        return questionRepository.save(question);
    }

    private User createUser(String username, User.Role role, String rawPassword) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(username + "@example.com");
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setRole(role);
        user.setIsVerified(role != User.Role.TEACHER);
        return user;
    }

    private String login(String account, String password) throws Exception {
        String response = mockMvc.perform(post("/api/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "account", account,
                                "password", password
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode jsonNode = objectMapper.readTree(response);
        return jsonNode.path("data").path("token").asText();
    }
}
