ALTER TABLE quizzes
    ADD COLUMN IF NOT EXISTS max_attempts INT NOT NULL DEFAULT 1 AFTER question_count,
    ADD COLUMN IF NOT EXISTS shuffle_questions TINYINT(1) NOT NULL DEFAULT 0 AFTER max_attempts,
    ADD COLUMN IF NOT EXISTS auto_save_enabled TINYINT(1) NOT NULL DEFAULT 1 AFTER shuffle_questions;
