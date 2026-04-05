package com.studyhelper.dto;

import com.studyhelper.entity.Question;

import java.time.LocalDateTime;

public class QuestionDTO {

    private Long id;
    private String content;
    private Question.Type type;
    private String typeLabel;
    private String options;
    private String answer;
    private Integer score;
    private String analysis;

    public static QuestionDTO fromQuestion(Question question) {
        return fromQuestion(question, true);
    }

    public static QuestionDTO fromQuestion(Question question, boolean includeAnswer) {
        QuestionDTO dto = new QuestionDTO();
        dto.setId(question.getId());
        dto.setContent(question.getContent());
        dto.setType(question.getType());
        dto.setTypeLabel(question.getType().getLabel());
        dto.setOptions(question.getOptions());
        dto.setAnswer(includeAnswer ? question.getAnswer() : null);
        dto.setScore(question.getScore());
        dto.setAnalysis(question.getAnalysis());
        return dto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Question.Type getType() {
        return type;
    }

    public void setType(Question.Type type) {
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
}
