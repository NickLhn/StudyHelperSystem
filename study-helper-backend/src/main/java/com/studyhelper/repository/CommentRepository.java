package com.studyhelper.repository;

import com.studyhelper.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findByMaterialIdOrderByCreatedAtDesc(Long materialId);

    long countByMaterialId(Long materialId);
}
