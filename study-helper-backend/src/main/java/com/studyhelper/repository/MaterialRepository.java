package com.studyhelper.repository;

import com.studyhelper.entity.Material;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MaterialRepository extends JpaRepository<Material, Long> {

    List<Material> findByUserId(Long userId);

    List<Material> findByCourseId(Long courseId);

    long countByCourseId(Long courseId);

    @Query("SELECT m FROM Material m WHERE m.user.id = :userId OR m.course.id IN " +
           "(SELECT c.id FROM Course c WHERE c.user.id = :userId) OR m.course.id IN " +
           "(SELECT sc.course.id FROM StudentCourse sc WHERE sc.student.id = :userId)")
    List<Material> findAccessibleMaterials(@Param("userId") Long userId);

    @Query("SELECT m FROM Material m ORDER BY m.downloadCount DESC")
    List<Material> findPopularMaterials();

    List<Material> findByNameContainingIgnoreCase(String keyword);

    // 统计指定时间段内的资料数量
    @Query("SELECT COUNT(m) FROM Material m WHERE m.createdAt BETWEEN :startDate AND :endDate")
    long countByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}
