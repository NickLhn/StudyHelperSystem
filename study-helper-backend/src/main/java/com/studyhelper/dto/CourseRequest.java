package com.studyhelper.dto;

import com.studyhelper.entity.Course;
import jakarta.validation.constraints.NotBlank;

public class CourseRequest {

    @NotBlank(message = "课程名称不能为空")
    private String name;

    private Course.Category category = Course.Category.REQUIRED;

    private String teacher;

    private String semesterLabel;

    private String schedule;

    private String location;

    private String remark;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Course.Category getCategory() {
        return category;
    }

    public void setCategory(Course.Category category) {
        this.category = category;
    }

    public String getTeacher() {
        return teacher;
    }

    public void setTeacher(String teacher) {
        this.teacher = teacher;
    }

    public String getSemesterLabel() {
        return semesterLabel;
    }

    public void setSemesterLabel(String semesterLabel) {
        this.semesterLabel = semesterLabel;
    }

    public String getSchedule() {
        return schedule;
    }

    public void setSchedule(String schedule) {
        this.schedule = schedule;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
