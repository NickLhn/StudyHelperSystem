package com.studyhelper.dto;

import com.studyhelper.entity.Homework;
import com.studyhelper.entity.HomeworkSubmission;

import java.time.LocalDateTime;

public class HomeworkDTO {

    private Long id;
    private String title;
    private String description;
    private Homework.Status status;
    private String statusLabel;
    private Integer questionCount;
    private Integer totalScore;
    private Boolean autoGradeEnabled;
    private Boolean allowLateSubmit;
    private Long teacherId;
    private String teacherName;
    private Long courseId;
    private String courseName;
    private LocalDateTime deadlineAt;
    private LocalDateTime publishedAt;
    private LocalDateTime createdAt;
    private HomeworkSubmission.Status submissionStatus;
    private String submissionStatusLabel;
    private Integer finalScore;
    private LocalDateTime submittedAt;

    public static HomeworkDTO fromHomework(Homework homework) {
        HomeworkDTO dto = new HomeworkDTO();
        dto.setId(homework.getId());
        dto.setTitle(homework.getTitle());
        dto.setDescription(homework.getDescription());
        dto.setStatus(homework.getStatus());
        dto.setStatusLabel(homework.getStatus().getLabel());
        dto.setQuestionCount(homework.getQuestionCount());
        dto.setTotalScore(homework.getTotalScore());
        dto.setAutoGradeEnabled(homework.getAutoGradeEnabled());
        dto.setAllowLateSubmit(homework.getAllowLateSubmit());
        dto.setTeacherId(homework.getTeacher().getId());
        dto.setTeacherName(homework.getTeacher().getNickname() != null ? homework.getTeacher().getNickname() : homework.getTeacher().getUsername());
        dto.setCourseId(homework.getCourse().getId());
        dto.setCourseName(homework.getCourse().getName());
        dto.setDeadlineAt(homework.getDeadlineAt());
        dto.setPublishedAt(homework.getPublishedAt());
        dto.setCreatedAt(homework.getCreatedAt());
        return dto;
    }

    public void applySubmission(HomeworkSubmission submission) {
        if (submission == null) {
            return;
        }
        setSubmissionStatus(submission.getStatus());
        setSubmissionStatusLabel(submission.getStatus().getLabel());
        setFinalScore(submission.getFinalScore());
        setSubmittedAt(submission.getSubmittedAt());
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

    public Homework.Status getStatus() {
        return status;
    }

    public void setStatus(Homework.Status status) {
        this.status = status;
    }

    public String getStatusLabel() {
        return statusLabel;
    }

    public void setStatusLabel(String statusLabel) {
        this.statusLabel = statusLabel;
    }

    public Integer getQuestionCount() {
        return questionCount;
    }

    public void setQuestionCount(Integer questionCount) {
        this.questionCount = questionCount;
    }

    public Integer getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(Integer totalScore) {
        this.totalScore = totalScore;
    }

    public Boolean getAutoGradeEnabled() {
        return autoGradeEnabled;
    }

    public void setAutoGradeEnabled(Boolean autoGradeEnabled) {
        this.autoGradeEnabled = autoGradeEnabled;
    }

    public Boolean getAllowLateSubmit() {
        return allowLateSubmit;
    }

    public void setAllowLateSubmit(Boolean allowLateSubmit) {
        this.allowLateSubmit = allowLateSubmit;
    }

    public Long getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(Long teacherId) {
        this.teacherId = teacherId;
    }

    public String getTeacherName() {
        return teacherName;
    }

    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
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

    public LocalDateTime getDeadlineAt() {
        return deadlineAt;
    }

    public void setDeadlineAt(LocalDateTime deadlineAt) {
        this.deadlineAt = deadlineAt;
    }

    public LocalDateTime getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(LocalDateTime publishedAt) {
        this.publishedAt = publishedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public HomeworkSubmission.Status getSubmissionStatus() {
        return submissionStatus;
    }

    public void setSubmissionStatus(HomeworkSubmission.Status submissionStatus) {
        this.submissionStatus = submissionStatus;
    }

    public String getSubmissionStatusLabel() {
        return submissionStatusLabel;
    }

    public void setSubmissionStatusLabel(String submissionStatusLabel) {
        this.submissionStatusLabel = submissionStatusLabel;
    }

    public Integer getFinalScore() {
        return finalScore;
    }

    public void setFinalScore(Integer finalScore) {
        this.finalScore = finalScore;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }
}
