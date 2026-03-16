package com.studyhelper.repository;

import com.studyhelper.entity.StudyRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface StudyRecordRepository extends JpaRepository<StudyRecord, Long> {

    List<StudyRecord> findByUserId(Long userId);

    List<StudyRecord> findByUserIdAndStudyDateBetween(Long userId, LocalDate startDate, LocalDate endDate);

    List<StudyRecord> findByUserIdAndCourseId(Long userId, Long courseId);

    @Query("SELECT sr FROM StudyRecord sr WHERE sr.user.id = :userId AND sr.course.id = :courseId " +
           "AND sr.studyDate BETWEEN :startDate AND :endDate")
    List<StudyRecord> findByUserIdAndCourseIdAndDateRange(
            @Param("userId") Long userId,
            @Param("courseId") Long courseId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    // 统计某段时间内的总学习时长
    @Query("SELECT COALESCE(SUM(sr.durationMinutes), 0) FROM StudyRecord sr " +
           "WHERE sr.user.id = :userId AND sr.studyDate BETWEEN :startDate AND :endDate")
    Integer getTotalDurationByUserIdAndDateRange(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    // 按课程分组统计学习时长
    @Query("SELECT sr.course.id as courseId, sr.course.name as courseName, " +
           "COALESCE(SUM(sr.durationMinutes), 0) as totalDuration " +
           "FROM StudyRecord sr " +
           "WHERE sr.user.id = :userId AND sr.studyDate BETWEEN :startDate AND :endDate " +
           "AND sr.course IS NOT NULL " +
           "GROUP BY sr.course.id, sr.course.name " +
           "ORDER BY totalDuration DESC")
    List<Object[]> getDurationByCourse(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    // 按日期统计学习时长趋势
    @Query("SELECT sr.studyDate as studyDate, COALESCE(SUM(sr.durationMinutes), 0) as dailyDuration " +
           "FROM StudyRecord sr " +
           "WHERE sr.user.id = :userId AND sr.studyDate BETWEEN :startDate AND :endDate " +
           "GROUP BY sr.studyDate " +
           "ORDER BY sr.studyDate")
    List<Object[]> getDailyDuration(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT sr.studyDate as studyDate, COALESCE(SUM(sr.durationMinutes), 0) as dailyDuration " +
            "FROM StudyRecord sr " +
            "WHERE sr.course.id = :courseId AND sr.user.id IN :userIds AND sr.studyDate BETWEEN :startDate AND :endDate " +
            "GROUP BY sr.studyDate " +
            "ORDER BY sr.studyDate")
    List<Object[]> getDailyDurationByCourseAndUsers(
            @Param("courseId") Long courseId,
            @Param("userIds") List<Long> userIds,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}
