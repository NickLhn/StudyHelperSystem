package com.studyhelper.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public class HomeworkReviewRequest {

    private String teacherComment;

    @Valid
    private List<ReviewedAnswerItem> answers;

    public static class ReviewedAnswerItem {
        @NotNull(message = "答案不能为空")
        private Long answerId;

        @NotNull(message = "请填写教师评分")
        @Min(value = 0, message = "教师评分不能小于0")
        private Integer teacherScore;

        private String teacherComment;

        public Long getAnswerId() {
            return answerId;
        }

        public void setAnswerId(Long answerId) {
            this.answerId = answerId;
        }

        public Integer getTeacherScore() {
            return teacherScore;
        }

        public void setTeacherScore(Integer teacherScore) {
            this.teacherScore = teacherScore;
        }

        public String getTeacherComment() {
            return teacherComment;
        }

        public void setTeacherComment(String teacherComment) {
            this.teacherComment = teacherComment;
        }
    }

    public String getTeacherComment() {
        return teacherComment;
    }

    public void setTeacherComment(String teacherComment) {
        this.teacherComment = teacherComment;
    }

    public List<ReviewedAnswerItem> getAnswers() {
        return answers;
    }

    public void setAnswers(List<ReviewedAnswerItem> answers) {
        this.answers = answers;
    }
}
