package com.studyhelper.dto;

import com.studyhelper.entity.HomeworkSubmission;

import java.time.LocalDateTime;

public class HomeworkSubmissionDTO {

    private Long id;
    private Long homeworkId;
    private Long studentId;
    private String studentName;
    private HomeworkSubmission.Status status;
    private String statusLabel;
    private Integer objectiveScore;
    private Integer subjectiveScore;
    private Integer finalScore;
    private Double accuracy;
    private String teacherComment;
    private Boolean isLate;
    private Integer attemptNo;
    private LocalDateTime submittedAt;
    private LocalDateTime reviewedAt;

    public static HomeworkSubmissionDTO fromSubmission(HomeworkSubmission submission) {
        HomeworkSubmissionDTO dto = new HomeworkSubmissionDTO();
        dto.setId(submission.getId());
        dto.setHomeworkId(submission.getHomework().getId());
        dto.setStudentId(submission.getStudent().getId());
        dto.setStudentName(submission.getStudent().getNickname() != null ? submission.getStudent().getNickname() : submission.getStudent().getUsername());
        dto.setStatus(submission.getStatus());
        dto.setStatusLabel(submission.getStatus().getLabel());
        dto.setObjectiveScore(submission.getObjectiveScore());
        dto.setSubjectiveScore(submission.getSubjectiveScore());
        dto.setFinalScore(submission.getFinalScore());
        dto.setAccuracy(submission.getAccuracy());
        dto.setTeacherComment(submission.getTeacherComment());
        dto.setIsLate(submission.getIsLate());
        dto.setAttemptNo(submission.getAttemptNo());
        dto.setSubmittedAt(submission.getSubmittedAt());
        dto.setReviewedAt(submission.getReviewedAt());
        return dto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getHomeworkId() {
        return homeworkId;
    }

    public void setHomeworkId(Long homeworkId) {
        this.homeworkId = homeworkId;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public HomeworkSubmission.Status getStatus() {
        return status;
    }

    public void setStatus(HomeworkSubmission.Status status) {
        this.status = status;
    }

    public String getStatusLabel() {
        return statusLabel;
    }

    public void setStatusLabel(String statusLabel) {
        this.statusLabel = statusLabel;
    }

    public Integer getObjectiveScore() {
        return objectiveScore;
    }

    public void setObjectiveScore(Integer objectiveScore) {
        this.objectiveScore = objectiveScore;
    }

    public Integer getSubjectiveScore() {
        return subjectiveScore;
    }

    public void setSubjectiveScore(Integer subjectiveScore) {
        this.subjectiveScore = subjectiveScore;
    }

    public Integer getFinalScore() {
        return finalScore;
    }

    public void setFinalScore(Integer finalScore) {
        this.finalScore = finalScore;
    }

    public Double getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(Double accuracy) {
        this.accuracy = accuracy;
    }

    public String getTeacherComment() {
        return teacherComment;
    }

    public void setTeacherComment(String teacherComment) {
        this.teacherComment = teacherComment;
    }

    public Boolean getIsLate() {
        return isLate;
    }

    public void setIsLate(Boolean late) {
        isLate = late;
    }

    public Integer getAttemptNo() {
        return attemptNo;
    }

    public void setAttemptNo(Integer attemptNo) {
        this.attemptNo = attemptNo;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }

    public LocalDateTime getReviewedAt() {
        return reviewedAt;
    }

    public void setReviewedAt(LocalDateTime reviewedAt) {
        this.reviewedAt = reviewedAt;
    }
}
