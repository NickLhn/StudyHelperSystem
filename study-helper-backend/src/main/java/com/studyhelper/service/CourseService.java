package com.studyhelper.service;

import com.studyhelper.dto.CourseDTO;
import com.studyhelper.dto.CourseRequest;
import com.studyhelper.entity.Course;
import com.studyhelper.entity.Quiz;
import com.studyhelper.entity.QuizRecord;
import com.studyhelper.entity.StudentCourse;
import com.studyhelper.entity.User;
import com.studyhelper.repository.CourseRepository;
import com.studyhelper.repository.MaterialRepository;
import com.studyhelper.repository.QuizRecordRepository;
import com.studyhelper.repository.QuizRepository;
import com.studyhelper.repository.StudentCourseRepository;
import com.studyhelper.repository.StudyRecordRepository;
import com.studyhelper.repository.TaskRepository;
import com.studyhelper.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.IntStream;
import java.util.stream.Collectors;
import java.util.HashMap;

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
        course.setTeacher(request.getTeacher());
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

    public List<CourseDTO> getUserCourses(Long userId) {
        List<Course> courses = courseRepository.findByUserId(userId);
        return courses.stream()
                .map(this::toCourseDTOWithCounts)
                .collect(Collectors.toList());
    }

    public List<CourseDTO> getUserCoursesByCategory(Long userId, Course.Category category) {
        List<Course> courses = courseRepository.findByUserIdAndCategory(userId, category);
        return courses.stream()
                .map(this::toCourseDTOWithCounts)
                .collect(Collectors.toList());
    }

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

        if (studentCourseRepository.existsByStudentIdAndCourseId(userId, course.getId())) {
            throw new RuntimeException("您已经加入该课程");
        }

        StudentCourse sc = new StudentCourse();
        sc.setStudent(student);
        sc.setCourse(course);
        studentCourseRepository.save(sc);

        return toCourseDTOWithCounts(course);
    }

    public List<CourseDTO> getStudentCourses(Long userId) {
        List<StudentCourse> studentCourses = studentCourseRepository.findByStudentId(userId);
        return studentCourses.stream()
                .map(StudentCourse::getCourse)
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

        long studentCount = studentCourseRepository.countByCourseId(courseId);
        long materialCount = materialRepository.countByCourseId(courseId);
        long taskCount = taskRepository.countByCourseId(courseId);
        long quizCount = quizRepository.countByCourseId(courseId);

        LocalDate endDate = LocalDate.now();
        LocalDate startDate = "month".equalsIgnoreCase(period) ? endDate.minusDays(29) : endDate.minusDays(6);

        List<Long> studentIds = studentCourseRepository.findByCourseId(courseId).stream()
                .map(sc -> sc.getStudent().getId())
                .collect(Collectors.toList());

        List<String> labels = IntStream.rangeClosed(0, (int) (endDate.toEpochDay() - startDate.toEpochDay()))
                .mapToObj(i -> startDate.plusDays(i).toString())
                .collect(Collectors.toList());
        Map<String, Integer> dailyMap = new HashMap<>();
        labels.forEach(d -> dailyMap.put(d, 0));

        if (!studentIds.isEmpty()) {
            List<Object[]> daily = studyRecordRepository.getDailyDurationByCourseAndUsers(courseId, studentIds, startDate, endDate);
            for (Object[] row : daily) {
                String day = row[0].toString();
                Integer minutes = ((Number) row[1]).intValue();
                dailyMap.put(day, minutes);
            }
        }

        List<Integer> minutes = labels.stream().map(dailyMap::get).collect(Collectors.toList());

        List<Map<String, Object>> quizSummaries = quizRepository.findByCourseId(courseId).stream().map(q -> {
            List<QuizRecord> records = quizRecordRepository.findByQuizIdOrderByCreatedAtDesc(q.getId());
            int attempts = records.size();
            double avgScore = attempts == 0 ? 0 : records.stream().mapToInt(r -> r.getScore() == null ? 0 : r.getScore()).average().orElse(0);
            double avgAccuracy = attempts == 0 ? 0 : records.stream().mapToDouble(r -> r.getAccuracy() == null ? 0 : r.getAccuracy()).average().orElse(0);
            Map<String, Object> m = new HashMap<>();
            m.put("quizId", q.getId());
            m.put("title", q.getTitle());
            m.put("attempts", attempts);
            m.put("avgScore", Math.round(avgScore * 100.0) / 100.0);
            m.put("avgAccuracy", Math.round(avgAccuracy * 10000.0) / 100.0);
            return m;
        }).collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("studentCount", studentCount);
        result.put("materialCount", materialCount);
        result.put("taskCount", taskCount);
        result.put("quizCount", quizCount);
        result.put("studyTrend", Map.of("labels", labels, "minutes", minutes));
        result.put("quizSummaries", quizSummaries);
        return result;
    }

    public Map<String, Object> getCourseStats(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("课程不存在"));

        long studentCount = studentCourseRepository.countByCourseId(courseId);
        long materialCount = materialRepository.countByCourseId(courseId);
        long taskCount = taskRepository.countByCourseId(courseId);
        long quizCount = quizRepository.countByCourseId(courseId);

        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(6);

        List<Long> studentIds = studentCourseRepository.findByCourseId(courseId).stream()
                .map(sc -> sc.getStudent().getId())
                .collect(Collectors.toList());

        List<String> labels = IntStream.rangeClosed(0, (int) (endDate.toEpochDay() - startDate.toEpochDay()))
                .mapToObj(i -> startDate.plusDays(i).toString())
                .collect(Collectors.toList());
        Map<String, Integer> dailyMap = new HashMap<>();
        labels.forEach(d -> dailyMap.put(d, 0));

        if (!studentIds.isEmpty()) {
            List<Object[]> daily = studyRecordRepository.getDailyDurationByCourseAndUsers(courseId, studentIds, startDate, endDate);
            for (Object[] row : daily) {
                String day = row[0].toString();
                Integer minutes = ((Number) row[1]).intValue();
                dailyMap.put(day, minutes);
            }
        }

        List<Integer> minutes = labels.stream().map(dailyMap::get).collect(Collectors.toList());

        List<Map<String, Object>> quizSummaries = quizRepository.findByCourseId(courseId).stream().map(q -> {
            List<QuizRecord> records = quizRecordRepository.findByQuizIdOrderByCreatedAtDesc(q.getId());
            int attempts = records.size();
            double avgScore = attempts == 0 ? 0 : records.stream().mapToInt(r -> r.getScore() == null ? 0 : r.getScore()).average().orElse(0);
            double avgAccuracy = attempts == 0 ? 0 : records.stream().mapToDouble(r -> r.getAccuracy() == null ? 0 : r.getAccuracy()).average().orElse(0);
            Map<String, Object> m = new HashMap<>();
            m.put("quizId", q.getId());
            m.put("title", q.getTitle());
            m.put("attempts", attempts);
            m.put("avgScore", Math.round(avgScore * 100.0) / 100.0);
            m.put("avgAccuracy", Math.round(avgAccuracy * 10000.0) / 100.0);
            return m;
        }).collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("studentCount", studentCount);
        result.put("materialCount", materialCount);
        result.put("taskCount", taskCount);
        result.put("quizCount", quizCount);
        result.put("studyTrend", Map.of("labels", labels, "minutes", minutes));
        result.put("quizSummaries", quizSummaries);
        return result;
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
