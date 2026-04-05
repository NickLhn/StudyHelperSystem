package com.studyhelper.dto;

import com.studyhelper.entity.HomeworkAnswer;

public class HomeworkAnswerDTO {

    private Long id;
    private Long answerId;
    private Long questionId;
    private String questionContent;
    private String questionType;
    private String questionTypeLabel;
    private Integer questionScore;
    private String studentAnswer;
    private Boolean isCorrect;
    private Integer scoreAwarded;
    private Integer autoScore;
    private Integer teacherScore;
    private Boolean needsReview;
    private String reviewStatus;
    private String reviewStatusLabel;
    private String matchDetail;
    private String teacherComment;
    private String analysis;
    private String standardAnswer;

    public static HomeworkAnswerDTO fromAnswer(HomeworkAnswer answer, boolean includeStandardAnswer) {
        HomeworkAnswerDTO dto = new HomeworkAnswerDTO();
        dto.setId(answer.getId());
        dto.setAnswerId(answer.getId());
        dto.setQuestionId(answer.getQuestion().getId());
        dto.setQuestionContent(answer.getQuestion().getContent());
        dto.setQuestionType(answer.getQuestion().getType().name());
        dto.setQuestionTypeLabel(answer.getQuestion().getType().getLabel());
        dto.setQuestionScore(answer.getQuestion().getScore());
        dto.setStudentAnswer(answer.getStudentAnswer());
        dto.setIsCorrect(answer.getIsCorrect());
        dto.setScoreAwarded(answer.getScoreAwarded());
        dto.setAutoScore(answer.getAutoScore());
        dto.setTeacherScore(answer.getTeacherScore());
        dto.setNeedsReview(answer.getNeedsReview());
        dto.setReviewStatus(answer.getReviewStatus().name());
        dto.setReviewStatusLabel(answer.getReviewStatus().getLabel());
        dto.setMatchDetail(answer.getMatchDetail());
        dto.setTeacherComment(answer.getTeacherComment());
        dto.setAnalysis(answer.getQuestion().getAnalysis());
        if (includeStandardAnswer) {
            dto.setStandardAnswer(answer.getQuestion().getAnswer());
        }
        return dto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAnswerId() {
        return answerId;
    }

    public void setAnswerId(Long answerId) {
        this.answerId = answerId;
    }

    public Long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Long questionId) {
        this.questionId = questionId;
    }

    public String getQuestionContent() {
        return questionContent;
    }

    public void setQuestionContent(String questionContent) {
        this.questionContent = questionContent;
    }

    public String getQuestionType() {
        return questionType;
    }

    public void setQuestionType(String questionType) {
        this.questionType = questionType;
    }

    public String getQuestionTypeLabel() {
        return questionTypeLabel;
    }

    public void setQuestionTypeLabel(String questionTypeLabel) {
        this.questionTypeLabel = questionTypeLabel;
    }

    public Integer getQuestionScore() {
        return questionScore;
    }

    public void setQuestionScore(Integer questionScore) {
        this.questionScore = questionScore;
    }

    public String getStudentAnswer() {
        return studentAnswer;
    }

    public void setStudentAnswer(String studentAnswer) {
        this.studentAnswer = studentAnswer;
    }

    public Boolean getIsCorrect() {
        return isCorrect;
    }

    public void setIsCorrect(Boolean correct) {
        isCorrect = correct;
    }

    public Integer getScoreAwarded() {
        return scoreAwarded;
    }

    public void setScoreAwarded(Integer scoreAwarded) {
        this.scoreAwarded = scoreAwarded;
    }

    public Integer getAutoScore() {
        return autoScore;
    }

    public void setAutoScore(Integer autoScore) {
        this.autoScore = autoScore;
    }

    public Integer getTeacherScore() {
        return teacherScore;
    }

    public void setTeacherScore(Integer teacherScore) {
        this.teacherScore = teacherScore;
    }

    public Boolean getNeedsReview() {
        return needsReview;
    }

    public void setNeedsReview(Boolean needsReview) {
        this.needsReview = needsReview;
    }

    public String getReviewStatus() {
        return reviewStatus;
    }

    public void setReviewStatus(String reviewStatus) {
        this.reviewStatus = reviewStatus;
    }

    public String getReviewStatusLabel() {
        return reviewStatusLabel;
    }

    public void setReviewStatusLabel(String reviewStatusLabel) {
        this.reviewStatusLabel = reviewStatusLabel;
    }

    public String getMatchDetail() {
        return matchDetail;
    }

    public void setMatchDetail(String matchDetail) {
        this.matchDetail = matchDetail;
    }

    public String getTeacherComment() {
        return teacherComment;
    }

    public void setTeacherComment(String teacherComment) {
        this.teacherComment = teacherComment;
    }

    public String getAnalysis() {
        return analysis;
    }

    public void setAnalysis(String analysis) {
        this.analysis = analysis;
    }

    public String getStandardAnswer() {
        return standardAnswer;
    }

    public void setStandardAnswer(String standardAnswer) {
        this.standardAnswer = standardAnswer;
    }
}
