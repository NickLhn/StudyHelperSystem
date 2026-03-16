package com.studyhelper.repository;

import com.studyhelper.entity.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuizRepository extends JpaRepository<Quiz, Long> {

    List<Quiz> findByUserId(Long userId);

    List<Quiz> findByCourseId(Long courseId);

    long countByCourseId(Long courseId);

    List<Quiz> findByStatus(Quiz.Status status);

    @Query("SELECT q FROM Quiz q WHERE q.user.id = :userId OR q.course.id IN " +
           "(SELECT c.id FROM Course c WHERE c.user.id = :userId) OR q.course.id IN " +
           "(SELECT sc.course.id FROM StudentCourse sc WHERE sc.student.id = :userId)")
    List<Quiz> findAccessibleQuizzes(@Param("userId") Long userId);

    @Query("SELECT q FROM Quiz q WHERE q.status = 'PUBLISHED' AND (" +
           "q.user.id = :userId OR q.course.id IN " +
           "(SELECT c.id FROM Course c WHERE c.user.id = :userId) OR q.course.id IN " +
           "(SELECT sc.course.id FROM StudentCourse sc WHERE sc.student.id = :userId))")
    List<Quiz> findAvailableQuizzes(@Param("userId") Long userId);
}
