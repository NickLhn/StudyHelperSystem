package com.studyhelper.repository;

import com.studyhelper.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    Optional<User> findByStudentId(String studentId);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByStudentId(String studentId);

    Optional<User> findByEmployeeId(String employeeId);

    boolean existsByEmployeeId(String employeeId);

    // 按角色查询用户
    @Query("SELECT u FROM User u WHERE u.role = :role")
    org.springframework.data.domain.Page<User> findByRole(@Param("role") User.Role role, Pageable pageable);

    // 搜索用户
    @Query("SELECT u FROM User u WHERE (u.username LIKE %:keyword% OR u.email LIKE %:keyword% OR u.nickname LIKE %:keyword%) AND (:role IS NULL OR u.role = :role)")
    org.springframework.data.domain.Page<User> searchUsers(@Param("keyword") String keyword, @Param("role") User.Role role, Pageable pageable);

    // 统计指定时间段内的用户数量
    @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt BETWEEN :startDate AND :endDate")
    long countByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    // 按角色统计用户数量
    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role")
    long countByRole(@Param("role") User.Role role);
}
