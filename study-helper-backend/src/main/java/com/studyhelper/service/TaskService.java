package com.studyhelper.service;

import com.studyhelper.dto.TaskDTO;
import com.studyhelper.dto.TaskRequest;
import com.studyhelper.entity.Course;
import com.studyhelper.entity.Task;
import com.studyhelper.entity.User;
import com.studyhelper.repository.CourseRepository;
import com.studyhelper.repository.StudentCourseRepository;
import com.studyhelper.repository.TaskRepository;
import com.studyhelper.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private StudentCourseRepository studentCourseRepository;

    public TaskDTO createTask(Long userId, TaskRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        Task task = new Task();
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setStatus(request.getStatus());
        task.setPriority(request.getPriority());
        task.setPlanDate(request.getPlanDate());
        task.setReminderTime(request.getReminderTime());
        task.setReminderEnabled(request.isReminderEnabled());
        task.setUser(user);

        if (request.getCourseId() != null) {
            Course course = courseRepository.findById(request.getCourseId())
                    .orElseThrow(() -> new RuntimeException("课程不存在"));
            if (!course.getUser().getId().equals(userId)) {
                throw new RuntimeException("无权关联该课程");
            }
            task.setCourse(course);
        }

        Task savedTask = taskRepository.save(task);
        return TaskDTO.fromTask(savedTask);
    }

    public List<TaskDTO> getUserTasks(Long userId) {
        List<Task> tasks = taskRepository.findByUserId(userId);
        return tasks.stream()
                .map(TaskDTO::fromTask)
                .collect(Collectors.toList());
    }

    public List<TaskDTO> getCourseTasks(Long userId, Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("课程不存在"));

        boolean hasAccess = course.getUser().getId().equals(userId) ||
                studentCourseRepository.existsByStudentIdAndCourseId(userId, courseId);

        if (!hasAccess) {
            throw new RuntimeException("无权访问该课程任务");
        }

        List<Task> tasks = taskRepository.findByCourseId(courseId);
        return tasks.stream()
                .map(TaskDTO::fromTask)
                .collect(Collectors.toList());
    }

    public List<TaskDTO> getUserTasksByStatus(Long userId, Task.Status status) {
        List<Task> tasks = taskRepository.findByUserIdAndStatus(userId, status);
        return tasks.stream()
                .map(TaskDTO::fromTask)
                .collect(Collectors.toList());
    }

    public List<TaskDTO> getUserTasksByDate(Long userId, LocalDate date) {
        List<Task> tasks = taskRepository.findByUserIdAndPlanDate(userId, date);
        return tasks.stream()
                .map(TaskDTO::fromTask)
                .collect(Collectors.toList());
    }

    public List<TaskDTO> getUserTasksByDateRange(Long userId, LocalDate startDate, LocalDate endDate) {
        List<Task> tasks = taskRepository.findByUserIdAndPlanDateBetween(userId, startDate, endDate);
        return tasks.stream()
                .map(TaskDTO::fromTask)
                .collect(Collectors.toList());
    }

    public List<TaskDTO> getTodayTasksWithReminder(Long userId) {
        List<Task> tasks = taskRepository.findByUserIdAndReminderEnabledAndPlanDate(
                userId, true, LocalDate.now());
        return tasks.stream()
                .map(TaskDTO::fromTask)
                .collect(Collectors.toList());
    }

    public TaskDTO getTaskById(Long taskId, Long userId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("任务不存在"));

        if (!task.getUser().getId().equals(userId)) {
            throw new RuntimeException("无权访问该任务");
        }

        return TaskDTO.fromTask(task);
    }

    public TaskDTO updateTask(Long taskId, Long userId, TaskRequest request) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("任务不存在"));

        if (!task.getUser().getId().equals(userId)) {
            throw new RuntimeException("无权修改该任务");
        }

        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setStatus(request.getStatus());
        task.setPriority(request.getPriority());
        task.setPlanDate(request.getPlanDate());
        task.setReminderTime(request.getReminderTime());
        task.setReminderEnabled(request.isReminderEnabled());

        if (request.getCourseId() != null) {
            Course course = courseRepository.findById(request.getCourseId())
                    .orElseThrow(() -> new RuntimeException("课程不存在"));
            if (!course.getUser().getId().equals(userId)) {
                throw new RuntimeException("无权关联该课程");
            }
            task.setCourse(course);
        } else {
            task.setCourse(null);
        }

        Task updatedTask = taskRepository.save(task);
        return TaskDTO.fromTask(updatedTask);
    }

    public TaskDTO updateTaskStatus(Long taskId, Long userId, Task.Status status) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("任务不存在"));

        if (!task.getUser().getId().equals(userId)) {
            throw new RuntimeException("无权修改该任务");
        }

        task.setStatus(status);
        Task updatedTask = taskRepository.save(task);
        return TaskDTO.fromTask(updatedTask);
    }

    public void deleteTask(Long taskId, Long userId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("任务不存在"));

        if (!task.getUser().getId().equals(userId)) {
            throw new RuntimeException("无权删除该任务");
        }

        taskRepository.delete(task);
    }

    public long getTaskCountByStatus(Long userId, Task.Status status) {
        return taskRepository.countByUserIdAndStatus(userId, status);
    }
}
