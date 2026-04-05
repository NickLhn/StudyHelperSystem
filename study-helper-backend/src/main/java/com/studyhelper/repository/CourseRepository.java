package com.studyhelper.repository;

import com.studyhelper.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

    List<Course> findByUserId(Long userId);

    List<Course> findByUserIdAndStatus(Long userId, Course.Status status);

    List<Course> findByUserIdAndCategory(Long userId, Course.Category category);

    List<Course> findByUserIdAndCategoryAndStatus(Long userId, Course.Category category, Course.Status status);

    boolean existsByIdAndUserId(Long id, Long userId);

    // 统计指定时间段内的课程数量
    @Query("SELECT COUNT(c) FROM Course c WHERE c.createdAt BETWEEN :startDate AND :endDate")
    long countByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    Optional<Course> findByInvitationCode(String invitationCode);

    boolean existsByInvitationCode(String invitationCode);
}
