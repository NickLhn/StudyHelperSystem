-- Schema cleanup recommendation for StudyHelperSystem
-- Note:
-- 1. Run only after a full backup.
-- 2. These statements assume the current Spring Boot entities are the source of truth.

SET FOREIGN_KEY_CHECKS = 0;

-- ------------------------------------------------------------------
-- 1. Drop legacy duplicate tables after backup/archive
-- ------------------------------------------------------------------
DROP TABLE IF EXISTS `study_task`;
DROP TABLE IF EXISTS `study_log`;
DROP TABLE IF EXISTS `quiz_record`;
DROP TABLE IF EXISTS `quiz`;
DROP TABLE IF EXISTS `material`;
DROP TABLE IF EXISTS `course`;
DROP TABLE IF EXISTS `user`;

-- ------------------------------------------------------------------
-- 2. Remove redundant columns
-- ------------------------------------------------------------------
ALTER TABLE `student_courses`
  DROP COLUMN `joined_at`;

-- If you do not plan to keep teacher verification code inside users,
-- remove it and use a separate verification table later if needed.
ALTER TABLE `users`
  DROP COLUMN `verification_code`;

-- If study_records is only used for statistics, simplify it.
-- Uncomment if confirmed:
-- ALTER TABLE `study_records`
--   DROP COLUMN `start_time`,
--   DROP COLUMN `end_time`,
--   DROP COLUMN `task_count`;

-- ------------------------------------------------------------------
-- 3. Tighten null/default behavior for counters
-- ------------------------------------------------------------------
ALTER TABLE `materials`
  MODIFY COLUMN `download_count` INT NOT NULL DEFAULT 0,
  MODIFY COLUMN `like_count` INT NOT NULL DEFAULT 0,
  MODIFY COLUMN `favorite_count` INT NOT NULL DEFAULT 0,
  MODIFY COLUMN `comment_count` INT NOT NULL DEFAULT 0;

ALTER TABLE `quiz_records`
  MODIFY COLUMN `score` INT NOT NULL DEFAULT 0,
  MODIFY COLUMN `total_score` INT NOT NULL DEFAULT 0;

-- ------------------------------------------------------------------
-- 4. Add practical indexes
-- ------------------------------------------------------------------
CREATE INDEX `idx_users_role` ON `users` (`role`);
CREATE INDEX `idx_courses_user_created` ON `courses` (`user_id`, `created_at`);
CREATE INDEX `idx_materials_course_created` ON `materials` (`course_id`, `created_at`);
CREATE INDEX `idx_materials_user_created` ON `materials` (`user_id`, `created_at`);
CREATE INDEX `idx_quizzes_course_status` ON `quizzes` (`course_id`, `status`);
CREATE INDEX `idx_quiz_records_user_quiz` ON `quiz_records` (`user_id`, `quiz_id`);
CREATE INDEX `idx_tasks_user_status_plan_date` ON `tasks` (`user_id`, `status`, `plan_date`);
CREATE INDEX `idx_study_records_user_date` ON `study_records` (`user_id`, `study_date`);

-- Keep this only if employee_id remains in business flow.
CREATE INDEX `idx_users_employee_id` ON `users` (`employee_id`);

-- ------------------------------------------------------------------
-- 5. Optional JSON upgrades (MySQL 8+)
-- ------------------------------------------------------------------
-- These require validating historical data first.
-- ALTER TABLE `quiz_records`
--   MODIFY COLUMN `answers` JSON NULL,
--   MODIFY COLUMN `wrong_questions` JSON NULL;

-- ------------------------------------------------------------------
-- 6. Optional FK delete strategy adjustments
-- ------------------------------------------------------------------
-- Use carefully. If history must be preserved, prefer soft-delete/status instead.
--
-- Example only:
-- ALTER TABLE `comments` DROP FOREIGN KEY `FKql5qaf6x6gg4i3o7blf8a73fr`;
-- ALTER TABLE `comments`
--   ADD CONSTRAINT `fk_comments_materials`
--   FOREIGN KEY (`material_id`) REFERENCES `materials` (`id`)
--   ON DELETE CASCADE ON UPDATE RESTRICT;
--
-- ALTER TABLE `user_material_actions` DROP FOREIGN KEY `FKmw1xix7u77wey4hb5ub3f2m3`;
-- ALTER TABLE `user_material_actions`
--   ADD CONSTRAINT `fk_user_material_actions_materials`
--   FOREIGN KEY (`material_id`) REFERENCES `materials` (`id`)
--   ON DELETE CASCADE ON UPDATE RESTRICT;

SET FOREIGN_KEY_CHECKS = 1;
