package com.studyhelper.repository;

import com.studyhelper.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByUserId(Long userId);

    List<Task> findByUserIdAndStatus(Long userId, Task.Status status);

    List<Task> findByUserIdAndPlanDate(Long userId, LocalDate planDate);

    List<Task> findByUserIdAndPlanDateAndStatus(Long userId, LocalDate planDate, Task.Status status);

    List<Task> findByUserIdAndPlanDateBetween(Long userId, LocalDate startDate, LocalDate endDate);

    List<Task> findByUserIdAndReminderEnabledAndPlanDate(Long userId, boolean reminderEnabled, LocalDate planDate);

    List<Task> findByCourseId(Long courseId);

    long countByCourseId(Long courseId);

    boolean existsByIdAndUserId(Long id, Long userId);

    long countByUserIdAndStatus(Long userId, Task.Status status);
}
