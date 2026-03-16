package com.studyhelper.repository;

import com.studyhelper.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {

    List<Question> findByQuizId(Long quizId);

    List<Question> findByQuizIdOrderByCreatedAtAsc(Long quizId);
}
