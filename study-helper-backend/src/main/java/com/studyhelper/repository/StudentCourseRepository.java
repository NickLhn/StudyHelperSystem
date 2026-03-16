package com.studyhelper.repository;

import com.studyhelper.entity.StudentCourse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudentCourseRepository extends JpaRepository<StudentCourse, Long> {
    boolean existsByStudentIdAndCourseId(Long studentId, Long courseId);
    long countByCourseId(Long courseId);
    List<StudentCourse> findByCourseId(Long courseId);
    List<StudentCourse> findByStudentId(Long studentId);
    void deleteByStudentIdAndCourseId(Long studentId, Long courseId);
}

