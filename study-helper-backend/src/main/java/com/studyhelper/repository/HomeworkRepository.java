package com.studyhelper.repository;

import com.studyhelper.entity.Homework;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HomeworkRepository extends JpaRepository<Homework, Long> {

    List<Homework> findByTeacherIdOrderByCreatedAtDesc(Long teacherId);

    List<Homework> findByTeacherIdAndCourseIdOrderByCreatedAtDesc(Long teacherId, Long courseId);

    List<Homework> findByCourseIdOrderByCreatedAtDesc(Long courseId);

    List<Homework> findByCourseIdInAndStatusOrderByDeadlineAtAscCreatedAtDesc(List<Long> courseIds, Homework.Status status);

    long countByCourseId(Long courseId);

    long countByTeacherId(Long teacherId);
}
