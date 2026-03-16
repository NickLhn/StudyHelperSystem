-- 学习辅助系统数据库更新脚本
-- 适用于已有数据的数据库升级

-- 1. 重命名表（如果存在旧表）
RENAME TABLE material TO materials_backup;

-- 2. 创建新的materials表
CREATE TABLE `materials` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `course_id` BIGINT COMMENT '所属课程',
  `name` VARCHAR(150) NOT NULL COMMENT '资料名称',
  `file_path` VARCHAR(300) NOT NULL COMMENT '服务器存储路径',
  `file_name` VARCHAR(255) NOT NULL COMMENT '原始文件名',
  `file_type` VARCHAR(20) DEFAULT '' COMMENT '文件类型 如 pdf, pptx',
  `file_size` BIGINT NOT NULL COMMENT '文件大小(字节)',
  `description` TEXT COMMENT '资料描述',
  `download_count` INT DEFAULT 0 COMMENT '下载次数',
  `like_count` INT DEFAULT 0 COMMENT '点赞数',
  `favorite_count` INT DEFAULT 0 COMMENT '收藏数',
  `comment_count` INT DEFAULT 0 COMMENT '评论数',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  INDEX `idx_user_id` (`user_id`),
  INDEX `idx_course_id` (`course_id`),
  FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`course_id`) REFERENCES `course`(`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3. 迁移旧数据（如果需要保留）
INSERT INTO materials (id, user_id, course_id, name, file_path, file_name, file_type, file_size, description, download_count, created_at, updated_at)
SELECT id, user_id, course_id, title, file_path, original_filename, file_type, 0, NULL, download_count, upload_time, upload_time
FROM materials_backup;

-- 4. 创建其他新增的表
CREATE TABLE `comment` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `material_id` BIGINT NOT NULL,
  `user_id` BIGINT NOT NULL,
  `content` TEXT NOT NULL,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  INDEX `idx_material_id` (`material_id`),
  INDEX `idx_user_id` (`user_id`),
  FOREIGN KEY (`material_id`) REFERENCES `materials`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `user_material_action` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `material_id` BIGINT NOT NULL,
  `action_type` VARCHAR(20) NOT NULL COMMENT 'LIKE, FAVORITE',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_material_action` (`user_id`, `material_id`, `action_type`),
  INDEX `idx_user_id` (`user_id`),
  INDEX `idx_material_id` (`material_id`),
  FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`material_id`) REFERENCES `materials`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 5. 更新其他表的时间字段
ALTER TABLE `user` 
  CHANGE `create_time` `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  ADD `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;

ALTER TABLE `course` 
  CHANGE `create_time` `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  ADD `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;

ALTER TABLE `study_task` 
  CHANGE `create_time` `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  ADD `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;

-- 6. 为用户表添加教师相关字段
ALTER TABLE `user` 
  ADD COLUMN `employee_id` VARCHAR(50) NULL COMMENT '教职工编号' AFTER `student_id`,
  ADD COLUMN `department` VARCHAR(100) NULL COMMENT '所属院系' AFTER `grade`,
  ADD COLUMN `teacher_title` VARCHAR(50) NULL COMMENT '教师职称' AFTER `department`,
  ADD COLUMN `is_verified` TINYINT(1) DEFAULT 0 COMMENT '是否已验证' AFTER `teacher_title`,
  ADD COLUMN `verification_code` VARCHAR(10) NULL COMMENT '验证码' AFTER `is_verified`;

-- 7. 删除备份表（确认数据迁移成功后执行）
-- DROP TABLE materials_backup;