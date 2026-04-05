package com.studyhelper.dto;

import com.studyhelper.entity.Course;

import java.time.LocalDateTime;

public class CourseDTO {

    private Long id;
    private String name;
    private Course.Category category;
    private String categoryLabel;
    private Course.Status status;
    private String statusLabel;
    private String teacher;
    private String semesterLabel;
    private String schedule;
    private String location;
    private String remark;
    private LocalDateTime archivedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String invitationCode;
    private Long studentCount;
    private Long materialCount;
    private Long taskCount;
    private Long quizCount;

    public static CourseDTO fromCourse(Course course) {
        CourseDTO dto = new CourseDTO();
        dto.setId(course.getId());
        dto.setName(course.getName());
        dto.setCategory(course.getCategory());
        dto.setCategoryLabel(course.getCategory().getLabel());
        dto.setStatus(course.getStatus());
        dto.setStatusLabel(course.getStatus().getLabel());
        dto.setTeacher(course.getTeacher());
        dto.setSemesterLabel(course.getSemesterLabel());
        dto.setSchedule(course.getSchedule());
        dto.setLocation(course.getLocation());
        dto.setRemark(course.getRemark());
        dto.setArchivedAt(course.getArchivedAt());
        dto.setCreatedAt(course.getCreatedAt());
        dto.setUpdatedAt(course.getUpdatedAt());
        dto.setInvitationCode(course.getInvitationCode());
        return dto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public String getCategoryLabel() {
        return categoryLabel;
    }

    public void setCategoryLabel(String categoryLabel) {
        this.categoryLabel = categoryLabel;
    }

    public Course.Status getStatus() {
        return status;
    }

    public void setStatus(Course.Status status) {
        this.status = status;
    }

    public String getStatusLabel() {
        return statusLabel;
    }

    public void setStatusLabel(String statusLabel) {
        this.statusLabel = statusLabel;
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

    public LocalDateTime getArchivedAt() {
        return archivedAt;
    }

    public void setArchivedAt(LocalDateTime archivedAt) {
        this.archivedAt = archivedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getInvitationCode() {
        return invitationCode;
    }

    public void setInvitationCode(String invitationCode) {
        this.invitationCode = invitationCode;
    }

    public Long getStudentCount() {
        return studentCount;
    }

    public void setStudentCount(Long studentCount) {
        this.studentCount = studentCount;
    }

    public Long getMaterialCount() {
        return materialCount;
    }

    public void setMaterialCount(Long materialCount) {
        this.materialCount = materialCount;
    }

    public Long getTaskCount() {
        return taskCount;
    }

    public void setTaskCount(Long taskCount) {
        this.taskCount = taskCount;
    }

    public Long getQuizCount() {
        return quizCount;
    }

    public void setQuizCount(Long quizCount) {
        this.quizCount = quizCount;
    }
}
