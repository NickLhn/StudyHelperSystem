package com.studyhelper.repository;

import com.studyhelper.entity.HomeworkQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HomeworkQuestionRepository extends JpaRepository<HomeworkQuestion, Long> {

    List<HomeworkQuestion> findByHomeworkIdOrderBySortOrderAsc(Long homeworkId);
}
