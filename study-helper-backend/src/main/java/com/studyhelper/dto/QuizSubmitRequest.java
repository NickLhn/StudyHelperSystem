package com.studyhelper.dto;

import java.util.HashMap;
import java.util.Map;

public class QuizSubmitRequest {

    private Map<String, String> answers = new HashMap<>();
    private Integer timeUsed;
    private Boolean autoSubmitted = false;

    public Map<String, String> getAnswers() {
        return answers;
    }

    public void setAnswers(Map<String, String> answers) {
        this.answers = answers == null ? new HashMap<>() : answers;
    }

    public Integer getTimeUsed() {
        return timeUsed;
    }

    public void setTimeUsed(Integer timeUsed) {
        this.timeUsed = timeUsed;
    }

    public Boolean getAutoSubmitted() {
        return autoSubmitted;
    }

    public void setAutoSubmitted(Boolean autoSubmitted) {
        this.autoSubmitted = autoSubmitted;
    }
}
