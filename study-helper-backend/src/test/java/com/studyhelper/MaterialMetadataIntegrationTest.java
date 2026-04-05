package com.studyhelper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.studyhelper.entity.Course;
import com.studyhelper.entity.User;
import com.studyhelper.repository.CommentRepository;
import com.studyhelper.repository.CourseRepository;
import com.studyhelper.repository.MaterialRepository;
import com.studyhelper.repository.StudentCourseRepository;
import com.studyhelper.repository.UserMaterialActionRepository;
import com.studyhelper.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class MaterialMetadataIntegrationTest {

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
    private MaterialRepository materialRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private UserMaterialActionRepository userMaterialActionRepository;

    @Autowired
    private StudentCourseRepository studentCourseRepository;

    @BeforeEach
    void setUp() {
        commentRepository.deleteAll();
        userMaterialActionRepository.deleteAll();
        materialRepository.deleteAll();
        studentCourseRepository.deleteAll();
        courseRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void uploadAndFetchMaterialShouldExposeCategoryTagsAndVersionMetadata() throws Exception {
        User teacher = createTeacher("material_teacher", "TeacherPass123!");
        Course course = createCourse(teacher, "软件测试课程");
        String token = login("material_teacher", "TeacherPass123!");

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "unit-test-guide.pdf",
                "application/pdf",
                "pdf-content".getBytes()
        );

        String uploadResponse = mockMvc.perform(multipart("/api/material/upload")
                        .file(file)
                        .param("userId", teacher.getId().toString())
                        .param("courseId", course.getId().toString())
                        .param("name", "单元测试讲义")
                        .param("description", "帮助学生理解断言与Mock")
                        .param("category", "讲义")
                        .param("tags", "测试,JUnit,MockMvc")
                        .param("versionLabel", "v1.2")
                        .param("versionNote", "增加接口测试示例")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.category").value("讲义"))
                .andExpect(jsonPath("$.data.tags[0]").value("测试"))
                .andExpect(jsonPath("$.data.tags[1]").value("JUnit"))
                .andExpect(jsonPath("$.data.tags[2]").value("MockMvc"))
                .andExpect(jsonPath("$.data.versionLabel").value("v1.2"))
                .andExpect(jsonPath("$.data.versionNote").value("增加接口测试示例"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode uploadJson = objectMapper.readTree(uploadResponse);
        long materialId = uploadJson.path("data").path("id").asLong();

        mockMvc.perform(get("/api/material/{materialId}", materialId)
                        .param("userId", teacher.getId().toString())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.category").value("讲义"))
                .andExpect(jsonPath("$.data.tags[0]").value("测试"))
                .andExpect(jsonPath("$.data.tags[1]").value("JUnit"))
                .andExpect(jsonPath("$.data.tags[2]").value("MockMvc"))
                .andExpect(jsonPath("$.data.versionLabel").value("v1.2"))
                .andExpect(jsonPath("$.data.versionNote").value("增加接口测试示例"))
                .andExpect(jsonPath("$.data.courseName").value("软件测试课程"));

        mockMvc.perform(get("/api/material/list")
                        .param("userId", teacher.getId().toString())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].category").value("讲义"))
                .andExpect(jsonPath("$.data[0].tags[0]").value("测试"))
                .andExpect(jsonPath("$.data[0].versionLabel").value("v1.2"));
    }

    private User createTeacher(String username, String rawPassword) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(username + "@example.com");
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setRole(User.Role.TEACHER);
        user.setIsVerified(true);
        user.setEmployeeId("T" + UUID.randomUUID().toString().substring(0, 6));
        return userRepository.save(user);
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
