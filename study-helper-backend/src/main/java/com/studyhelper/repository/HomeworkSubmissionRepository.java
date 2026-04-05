package com.studyhelper.repository;

import com.studyhelper.entity.HomeworkSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HomeworkSubmissionRepository extends JpaRepository<HomeworkSubmission, Long> {

    List<HomeworkSubmission> findByHomeworkIdOrderByCreatedAtDesc(Long homeworkId);

    List<HomeworkSubmission> findByStudentIdOrderByCreatedAtDesc(Long studentId);

    List<HomeworkSubmission> findByHomeworkIdInOrderByCreatedAtDesc(List<Long> homeworkIds);

    Optional<HomeworkSubmission> findTopByHomeworkIdAndStudentIdOrderByCreatedAtDesc(Long homeworkId, Long studentId);
}
