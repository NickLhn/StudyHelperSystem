package com.studyhelper.repository;

import com.studyhelper.entity.HomeworkAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HomeworkAnswerRepository extends JpaRepository<HomeworkAnswer, Long> {

    List<HomeworkAnswer> findBySubmissionIdOrderByIdAsc(Long submissionId);

    List<HomeworkAnswer> findByQuestionId(Long questionId);
}
