package com.studyhelper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.studyhelper.entity.Course;
import com.studyhelper.entity.StudentCourse;
import com.studyhelper.entity.User;
import com.studyhelper.repository.CourseRepository;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CourseArchiveIntegrationTest {

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

    @BeforeEach
    void setUp() {
        studentCourseRepository.deleteAll();
        courseRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void teacherAndStudentShouldSeeArchivedCourseInStatusLists() throws Exception {
        User teacher = createUser("archive_teacher", User.Role.TEACHER, "TeacherPass123!");
        teacher.setIsVerified(true);
        teacher.setEmployeeId("T" + UUID.randomUUID().toString().substring(0, 6));
        teacher = userRepository.save(teacher);

        User student = createUser("archive_student", User.Role.STUDENT, "StudentPass123!");
        student.setStudentId("SID_" + UUID.randomUUID().toString().substring(0, 8));
        student = userRepository.save(student);

        User outsider = createUser("archive_outsider", User.Role.STUDENT, "StudentPass123!");
        outsider.setStudentId("SID_" + UUID.randomUUID().toString().substring(0, 8));
        outsider = userRepository.save(outsider);

        Course course = new Course();
        course.setName("数据库系统");
        course.setCategory(Course.Category.REQUIRED);
        course.setTeacher(teacher.getUsername());
        course.setSemesterLabel("2026 春季学期");
        course.setInvitationCode("ARC" + UUID.randomUUID().toString().substring(0, 6));
        course.setUser(teacher);
        course = courseRepository.save(course);

        StudentCourse enrollment = new StudentCourse();
        enrollment.setCourse(course);
        enrollment.setStudent(student);
        studentCourseRepository.save(enrollment);

        String teacherToken = login("archive_teacher", "TeacherPass123!");
        String studentToken = login("archive_student", "StudentPass123!");
        String outsiderToken = login("archive_outsider", "StudentPass123!");

        mockMvc.perform(patch("/api/course/{courseId}/status", course.getId())
                        .param("userId", teacher.getId().toString())
                        .param("status", "ARCHIVED")
                        .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.status").value("ARCHIVED"))
                .andExpect(jsonPath("$.data.statusLabel").value("已归档"))
                .andExpect(jsonPath("$.data.semesterLabel").value("2026 春季学期"));

        mockMvc.perform(get("/api/course/list-by-status")
                        .param("userId", teacher.getId().toString())
                        .param("status", "ARCHIVED")
                        .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].status").value("ARCHIVED"))
                .andExpect(jsonPath("$.data[0].semesterLabel").value("2026 春季学期"));

        mockMvc.perform(get("/api/course/student/list-by-status")
                        .param("userId", student.getId().toString())
                        .param("status", "ARCHIVED")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].id").value(course.getId()))
                .andExpect(jsonPath("$.data[0].statusLabel").value("已归档"));

        mockMvc.perform(post("/api/course/join")
                        .param("userId", outsider.getId().toString())
                        .param("invitationCode", course.getInvitationCode())
                        .header("Authorization", "Bearer " + outsiderToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("该课程已归档，暂不支持新学生加入"));
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
