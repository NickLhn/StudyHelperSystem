package com.studyhelper.dto;

import com.studyhelper.entity.HomeworkQuestion;

public class HomeworkQuestionDTO {

    private Long id;
    private Integer sortOrder;
    private String content;
    private HomeworkQuestion.Type type;
    private String typeLabel;
    private String options;
    private String answer;
    private Integer score;
    private String analysis;
    private Boolean manualReviewRequired;

    public static HomeworkQuestionDTO fromQuestion(HomeworkQuestion question, boolean includeAnswer) {
        HomeworkQuestionDTO dto = new HomeworkQuestionDTO();
        dto.setId(question.getId());
        dto.setSortOrder(question.getSortOrder());
        dto.setContent(question.getContent());
        dto.setType(question.getType());
        dto.setTypeLabel(question.getType().getLabel());
        dto.setOptions(question.getOptions());
        dto.setScore(question.getScore());
        dto.setAnalysis(question.getAnalysis());
        dto.setManualReviewRequired(question.getManualReviewRequired());
        if (includeAnswer) {
            dto.setAnswer(question.getAnswer());
        }
        return dto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public HomeworkQuestion.Type getType() {
        return type;
    }

    public void setType(HomeworkQuestion.Type type) {
        this.type = type;
    }

    public String getTypeLabel() {
        return typeLabel;
    }

    public void setTypeLabel(String typeLabel) {
        this.typeLabel = typeLabel;
    }

    public String getOptions() {
        return options;
    }

    public void setOptions(String options) {
        this.options = options;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public String getAnalysis() {
        return analysis;
    }

    public void setAnalysis(String analysis) {
        this.analysis = analysis;
    }

    public Boolean getManualReviewRequired() {
        return manualReviewRequired;
    }

    public void setManualReviewRequired(Boolean manualReviewRequired) {
        this.manualReviewRequired = manualReviewRequired;
    }
}
