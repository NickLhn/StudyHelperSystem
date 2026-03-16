package com.studyhelper.dto;

public class AdminStatsDTO {
    private Long totalUsers;
    private Long totalCourses;
    private Long totalMaterials;
    private Long todayNewUsers;
    private Long todayNewCourses;
    private Long todayNewMaterials;
    private Long studentCount;
    private Long teacherCount;
    private Long adminCount;

    // Getters and Setters
    public Long getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(Long totalUsers) {
        this.totalUsers = totalUsers;
    }

    public Long getTotalCourses() {
        return totalCourses;
    }

    public void setTotalCourses(Long totalCourses) {
        this.totalCourses = totalCourses;
    }

    public Long getTotalMaterials() {
        return totalMaterials;
    }

    public void setTotalMaterials(Long totalMaterials) {
        this.totalMaterials = totalMaterials;
    }

    public Long getTodayNewUsers() {
        return todayNewUsers;
    }

    public void setTodayNewUsers(Long todayNewUsers) {
        this.todayNewUsers = todayNewUsers;
    }

    public Long getTodayNewCourses() {
        return todayNewCourses;
    }

    public void setTodayNewCourses(Long todayNewCourses) {
        this.todayNewCourses = todayNewCourses;
    }

    public Long getTodayNewMaterials() {
        return todayNewMaterials;
    }

    public void setTodayNewMaterials(Long todayNewMaterials) {
        this.todayNewMaterials = todayNewMaterials;
    }

    public Long getStudentCount() {
        return studentCount;
    }

    public void setStudentCount(Long studentCount) {
        this.studentCount = studentCount;
    }

    public Long getTeacherCount() {
        return teacherCount;
    }

    public void setTeacherCount(Long teacherCount) {
        this.teacherCount = teacherCount;
    }

    public Long getAdminCount() {
        return adminCount;
    }

    public void setAdminCount(Long adminCount) {
        this.adminCount = adminCount;
    }
}