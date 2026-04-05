package com.studyhelper.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public class HomeworkSubmitRequest {

    @Valid
    @NotEmpty(message = "请至少提交一道题目的答案")
    private List<AnswerItem> answers;

    public static class AnswerItem {
        @NotNull(message = "题目不能为空")
        private Long questionId;

        private String answer;

        public Long getQuestionId() {
            return questionId;
        }

        public void setQuestionId(Long questionId) {
            this.questionId = questionId;
        }

        public String getAnswer() {
            return answer;
        }

        public void setAnswer(String answer) {
            this.answer = answer;
        }
    }

    public List<AnswerItem> getAnswers() {
        return answers;
    }

    public void setAnswers(List<AnswerItem> answers) {
        this.answers = answers;
    }
}
