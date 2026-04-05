package com.studyhelper.repository;

import com.studyhelper.entity.QuizRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuizRecordRepository extends JpaRepository<QuizRecord, Long> {

    List<QuizRecord> findByUserId(Long userId);

    List<QuizRecord> findByUserIdAndQuizId(Long userId, Long quizId);

    List<QuizRecord> findByQuizIdOrderByCreatedAtDesc(Long quizId);

    long countByUserIdAndQuizId(Long userId, Long quizId);

    Optional<QuizRecord> findByIdAndUserId(Long id, Long userId);

    @Query("SELECT qr FROM QuizRecord qr WHERE qr.user.id = :userId ORDER BY qr.createdAt DESC")
    List<QuizRecord> findRecentRecords(@Param("userId") Long userId);

    @Query("SELECT qr FROM QuizRecord qr WHERE qr.user.id = :userId AND qr.wrongQuestions IS NOT NULL AND qr.wrongQuestions != '[]'")
    List<QuizRecord> findRecordsWithWrongQuestions(@Param("userId") Long userId);
}
