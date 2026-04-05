package com.studyhelper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.studyhelper.entity.Course;
import com.studyhelper.entity.Homework;
import com.studyhelper.entity.HomeworkSubmission;
import com.studyhelper.entity.Material;
import com.studyhelper.entity.Quiz;
import com.studyhelper.entity.StudentCourse;
import com.studyhelper.entity.User;
import com.studyhelper.repository.CourseRepository;
import com.studyhelper.repository.HomeworkRepository;
import com.studyhelper.repository.HomeworkSubmissionRepository;
import com.studyhelper.repository.MaterialRepository;
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

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class NotificationIntegrationTest {

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
    private StudentCourseRepository studentCourseRepository;

    @Autowired
    private HomeworkSubmissionRepository homeworkSubmissionRepository;

    @Autowired
    private HomeworkRepository homeworkRepository;

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private MaterialRepository materialRepository;

    @BeforeEach
    void setUp() {
        homeworkSubmissionRepository.deleteAll();
        homeworkRepository.deleteAll();
        quizRepository.deleteAll();
        materialRepository.deleteAll();
        studentCourseRepository.deleteAll();
        courseRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void teacherNotificationEndpointShouldReturnReviewAndDeadlineItems() throws Exception {
        User teacher = createUser("teacher_notify", User.Role.TEACHER, "TeacherPass123!");
        teacher.setIsVerified(true);
        teacher.setEmployeeId("T" + UUID.randomUUID().toString().substring(0, 6));
        teacher = userRepository.save(teacher);

        User student = createUser("student_notify", User.Role.STUDENT, "StudentPass123!");
        student.setStudentId("SID_" + UUID.randomUUID().toString().substring(0, 8));
        student = userRepository.save(student);

        Course course = createCourse(teacher, "软件工程通知课");
        enrollStudent(course, student);

        Homework homework = createHomework(course, teacher, "第 1 次作业", LocalDateTime.now().plusHours(18));
        HomeworkSubmission submission = createSubmission(homework, student, HomeworkSubmission.Status.REVIEW_PENDING, 18, 0.6);

        String token = login("teacher_notify", "TeacherPass123!");

        mockMvc.perform(get("/api/notification/list")
                        .param("userId", teacher.getId().toString())
                        .param("limit", "10")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.summary.totalCount").isNumber())
                .andExpect(jsonPath("$.data.summary.reviewCount").value(1))
                .andExpect(jsonPath("$.data.items[0].title").isNotEmpty())
                .andExpect(jsonPath("$.data.items[?(@.category=='REVIEW')]").isNotEmpty())
                .andExpect(jsonPath("$.data.items[?(@.actionPath=='/teacher/homework/" + homework.getId() + "')]").isNotEmpty());
    }

    @Test
    void studentNotificationEndpointShouldReturnHomeworkQuizAndMaterialItems() throws Exception {
        User teacher = createUser("teacher_feed", User.Role.TEACHER, "TeacherPass123!");
        teacher.setIsVerified(true);
        teacher.setEmployeeId("T" + UUID.randomUUID().toString().substring(0, 6));
        teacher = userRepository.save(teacher);

        User student = createUser("student_feed", User.Role.STUDENT, "StudentPass123!");
        student.setStudentId("SID_" + UUID.randomUUID().toString().substring(0, 8));
        student = userRepository.save(student);

        Course course = createCourse(teacher, "数据结构");
        enrollStudent(course, student);

        Homework dueHomework = createHomework(course, teacher, "链表作业", LocalDateTime.now().plusHours(12));
        Homework reviewedHomework = createHomework(course, teacher, "树结构作业", LocalDateTime.now().plusDays(5));
        createSubmission(reviewedHomework, student, HomeworkSubmission.Status.REVIEWED, 24, 0.8);

        Quiz quiz = new Quiz();
        quiz.setTitle("栈与队列测验");
        quiz.setDescription("新测验");
        quiz.setCourse(course);
        quiz.setUser(teacher);
        quiz.setQuestionCount(0);
        quiz.setTotalTime(20);
        quiz.setStatus(Quiz.Status.PUBLISHED);
        quiz.setCreatedAt(LocalDateTime.now().minusHours(2));
        quizRepository.save(quiz);

        Material material = new Material();
        material.setName("第 3 周讲义");
        material.setFileName("week3.pdf");
        material.setFileType("application/pdf");
        material.setFileSize(1024L);
        material.setFilePath("/files/week3.pdf");
        material.setDescription("课程讲义");
        material.setCourse(course);
        material.setUser(teacher);
        material.setCreatedAt(LocalDateTime.now().minusHours(3));
        materialRepository.save(material);

        String token = login("student_feed", "StudentPass123!");

        mockMvc.perform(get("/api/notification/list")
                        .param("userId", student.getId().toString())
                        .param("limit", "20")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.summary.homeworkCount").isNumber())
                .andExpect(jsonPath("$.data.summary.quizCount").value(1))
                .andExpect(jsonPath("$.data.summary.materialCount").value(1))
                .andExpect(jsonPath("$.data.items[?(@.category=='HOMEWORK')]").isNotEmpty())
                .andExpect(jsonPath("$.data.items[?(@.category=='QUIZ')]").isNotEmpty())
                .andExpect(jsonPath("$.data.items[?(@.category=='MATERIAL')]").isNotEmpty())
                .andExpect(jsonPath("$.data.items[?(@.actionPath=='/student/homework/" + dueHomework.getId() + "')]").isNotEmpty())
                .andExpect(jsonPath("$.data.items[?(@.actionPath=='/student/homework/result')]").doesNotExist());
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

    private Course createCourse(User teacher, String courseName) {
        Course course = new Course();
        course.setName(courseName);
        course.setCategory(Course.Category.REQUIRED);
        course.setTeacher(teacher.getUsername());
        course.setInvitationCode("IC" + UUID.randomUUID().toString().substring(0, 6));
        course.setUser(teacher);
        return courseRepository.save(course);
    }

    private void enrollStudent(Course course, User student) {
        StudentCourse studentCourse = new StudentCourse();
        studentCourse.setCourse(course);
        studentCourse.setStudent(student);
        studentCourseRepository.save(studentCourse);
    }

    private Homework createHomework(Course course, User teacher, String title, LocalDateTime deadlineAt) {
        Homework homework = new Homework();
        homework.setTitle(title);
        homework.setCourse(course);
        homework.setTeacher(teacher);
        homework.setStatus(Homework.Status.PUBLISHED);
        homework.setQuestionCount(1);
        homework.setTotalScore(30);
        homework.setAutoGradeEnabled(true);
        homework.setAllowLateSubmit(true);
        homework.setDeadlineAt(deadlineAt);
        homework.setCreatedAt(LocalDateTime.now().minusHours(4));
        return homeworkRepository.save(homework);
    }

    private HomeworkSubmission createSubmission(Homework homework, User student, HomeworkSubmission.Status status, int finalScore, double accuracy) {
        HomeworkSubmission submission = new HomeworkSubmission();
        submission.setHomework(homework);
        submission.setStudent(student);
        submission.setStatus(status);
        submission.setObjectiveScore(finalScore);
        submission.setSubjectiveScore(0);
        submission.setFinalScore(finalScore);
        submission.setAccuracy(accuracy);
        submission.setSubmittedAt(LocalDateTime.now().minusHours(2));
        submission.setAutoGradedAt(LocalDateTime.now().minusHours(2));
        if (status == HomeworkSubmission.Status.REVIEWED) {
            submission.setReviewedAt(LocalDateTime.now().minusHours(1));
        }
        return homeworkSubmissionRepository.save(submission);
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
