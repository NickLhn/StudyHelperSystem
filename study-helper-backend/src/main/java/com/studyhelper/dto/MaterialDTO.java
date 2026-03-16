package com.studyhelper.dto;

import com.studyhelper.entity.Material;

import java.time.LocalDateTime;

public class MaterialDTO {

    private Long id;
    private String name;
    private String fileName;
    private String fileType;
    private String filePath;
    private Long fileSize;
    private String fileSizeFormatted;
    private String description;
    private Long userId;
    private String username;
    private Long courseId;
    private String courseName;
    private Integer downloadCount;
    private Integer likeCount;
    private Integer favoriteCount;
    private Integer commentCount;
    private boolean likedByCurrentUser;
    private boolean favoritedByCurrentUser;
    private LocalDateTime createdAt;

    public static MaterialDTO fromMaterial(Material material) {
        MaterialDTO dto = new MaterialDTO();
        dto.setId(material.getId());
        dto.setName(material.getName());
        dto.setFileName(material.getFileName());
        dto.setFileType(material.getFileType());
        dto.setFilePath(material.getFilePath());
        dto.setFileSize(material.getFileSize());
        dto.setFileSizeFormatted(formatFileSize(material.getFileSize()));
        dto.setDescription(material.getDescription());
        dto.setUserId(material.getUser().getId());
        dto.setUsername(material.getUser().getUsername());
        if (material.getCourse() != null) {
            dto.setCourseId(material.getCourse().getId());
            dto.setCourseName(material.getCourse().getName());
        }
        dto.setDownloadCount(material.getDownloadCount());
        dto.setLikeCount(material.getLikeCount());
        dto.setFavoriteCount(material.getFavoriteCount());
        dto.setCreatedAt(material.getCreatedAt());
        return dto;
    }

    private static String formatFileSize(Long size) {
        if (size == null) return "0 B";
        if (size < 1024) return size + " B";
        if (size < 1024 * 1024) return String.format("%.2f KB", size / 1024.0);
        if (size < 1024 * 1024 * 1024) return String.format("%.2f MB", size / (1024.0 * 1024));
        return String.format("%.2f GB", size / (1024.0 * 1024 * 1024));
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

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public String getFileSizeFormatted() {
        return fileSizeFormatted;
    }

    public void setFileSizeFormatted(String fileSizeFormatted) {
        this.fileSizeFormatted = fileSizeFormatted;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public Integer getDownloadCount() {
        return downloadCount;
    }

    public void setDownloadCount(Integer downloadCount) {
        this.downloadCount = downloadCount;
    }

    public Integer getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(Integer likeCount) {
        this.likeCount = likeCount;
    }

    public Integer getFavoriteCount() {
        return favoriteCount;
    }

    public void setFavoriteCount(Integer favoriteCount) {
        this.favoriteCount = favoriteCount;
    }

    public Integer getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(Integer commentCount) {
        this.commentCount = commentCount;
    }

    public boolean isLikedByCurrentUser() {
        return likedByCurrentUser;
    }

    public void setLikedByCurrentUser(boolean likedByCurrentUser) {
        this.likedByCurrentUser = likedByCurrentUser;
    }

    public boolean isFavoritedByCurrentUser() {
        return favoritedByCurrentUser;
    }

    public void setFavoritedByCurrentUser(boolean favoritedByCurrentUser) {
        this.favoritedByCurrentUser = favoritedByCurrentUser;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
