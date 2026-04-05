package com.studyhelper.dto;

import com.studyhelper.entity.HomeworkQuestion;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;

public class HomeworkRequest {

    @NotBlank(message = "作业标题不能为空")
    private String title;

    private String description;

    @NotNull(message = "必须选择课程")
    private Long courseId;

    @NotNull(message = "请设置截止时间")
    private LocalDateTime deadlineAt;

    private Boolean autoGradeEnabled = true;

    private Boolean allowLateSubmit = false;

    @Valid
    @NotEmpty(message = "请至少添加一道题目")
    private List<QuestionItem> questions;

    public static class QuestionItem {
        @NotBlank(message = "题目内容不能为空")
        private String content;

        @NotNull(message = "题目类型不能为空")
        private HomeworkQuestion.Type type;

        private List<String> options;

        private String answer;

        @NotNull(message = "题目分值不能为空")
        @Min(value = 1, message = "题目分值至少为1")
        private Integer score;

        private String analysis;

        private Boolean manualReviewRequired = false;

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

        public List<String> getOptions() {
            return options;
        }

        public void setOptions(List<String> options) {
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

    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }

    public LocalDateTime getDeadlineAt() {
        return deadlineAt;
    }

    public void setDeadlineAt(LocalDateTime deadlineAt) {
        this.deadlineAt = deadlineAt;
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

    public List<QuestionItem> getQuestions() {
        return questions;
    }

    public void setQuestions(List<QuestionItem> questions) {
        this.questions = questions;
    }
}
