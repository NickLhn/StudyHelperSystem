package com.studyhelper.dto;

import com.studyhelper.entity.Quiz;

import java.time.LocalDateTime;
import java.util.List;

public class QuizDTO {

    private Long id;
    private String title;
    private String description;
    private Integer totalTime;
    private Integer questionCount;
    private Quiz.Type type;
    private String typeLabel;
    private Quiz.Status status;
    private String statusLabel;
    private Long userId;
    private String username;
    private Long courseId;
    private String courseName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static QuizDTO fromQuiz(Quiz quiz) {
        QuizDTO dto = new QuizDTO();
        dto.setId(quiz.getId());
        dto.setTitle(quiz.getTitle());
        dto.setDescription(quiz.getDescription());
        dto.setTotalTime(quiz.getTotalTime());
        dto.setQuestionCount(quiz.getQuestionCount());
        dto.setType(quiz.getType());
        dto.setTypeLabel(quiz.getType().getLabel());
        dto.setStatus(quiz.getStatus());
        dto.setStatusLabel(quiz.getStatus().getLabel());
        dto.setUserId(quiz.getUser().getId());
        dto.setUsername(quiz.getUser().getUsername());
        if (quiz.getCourse() != null) {
            dto.setCourseId(quiz.getCourse().getId());
            dto.setCourseName(quiz.getCourse().getName());
        }
        dto.setCreatedAt(quiz.getCreatedAt());
        dto.setUpdatedAt(quiz.getUpdatedAt());
        return dto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getTotalTime() {
        return totalTime;
    }

    public void setTotalTime(Integer totalTime) {
        this.totalTime = totalTime;
    }

    public Integer getQuestionCount() {
        return questionCount;
    }

    public void setQuestionCount(Integer questionCount) {
        this.questionCount = questionCount;
    }

    public Quiz.Type getType() {
        return type;
    }

    public void setType(Quiz.Type type) {
        this.type = type;
    }

    public String getTypeLabel() {
        return typeLabel;
    }

    public void setTypeLabel(String typeLabel) {
        this.typeLabel = typeLabel;
    }

    public Quiz.Status getStatus() {
        return status;
    }

    public void setStatus(Quiz.Status status) {
        this.status = status;
    }

    public String getStatusLabel() {
        return statusLabel;
    }

    public void setStatusLabel(String statusLabel) {
        this.statusLabel = statusLabel;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
