# 数据库表优化建议

## 结论先说

这份 `studyhelper.sql` 最大的问题不是单个字段，而是 **旧表和新表同时存在**，导致职责重复、命名混乱、后续维护风险很高。

当前后端实体真正对应的是这套表：

- `users`
- `courses`
- `materials`
- `comments`
- `quizzes`
- `questions`
- `quiz_records`
- `tasks`
- `study_records`
- `student_courses`
- `user_material_actions`
- `invitation_code`

建议保留上面这 12 张表，其他旧表全部归档后删除。

## 一、建议删除的旧表

这些表是旧版本残留，和当前 Spring Boot 实体不一致：

- `user`
- `course`
- `material`
- `quiz`
- `quiz_record`
- `study_log`
- `study_task`

删除原因：

- 与当前实体类不匹配
- 字段设计比新表更粗糙
- 会误导后续开发和数据迁移
- 容易出现“代码查 `users`，库里又有 `user`”这种双表混乱

## 二、建议保留的表及字段优化

### 1. `users`

建议保留字段：

- `id`
- `username`
- `password`
- `email`
- `role`
- `nickname`
- `avatar`
- `student_id`
- `major`
- `grade`
- `teacher_title`
- `department`
- `created_at`
- `updated_at`

建议视业务保留字段：

- `employee_id`
- `is_verified`

建议删除或迁出的字段：

- `verification_code`

原因：

- `verification_code` 现在只是被写入用户表，但没有完整的验证码生命周期管理，比如过期时间、验证次数、发送渠道、状态流转。
- 如果以后真的做教师工号验证，应该新建独立表，例如 `teacher_verifications`，不要把一次性验证码塞在 `users` 主表里。

额外建议：

- `email` 允许为空没问题，但要统一前后端策略。
- 如果项目后续完全改成“邀请码注册教师”，那 `employee_id` 和 `is_verified` 也可以一起去掉。

### 2. `courses`

建议保留字段：

- `id`
- `user_id`
- `name`
- `category`
- `teacher`
- `schedule`
- `location`
- `remark`
- `invitation_code`
- `created_at`
- `updated_at`

建议考虑删除的字段：

- `teacher`

删除条件：

- 如果课程的教师永远就是 `user_id` 对应的用户，那么 `teacher` 是冗余字段。
- 如果你允许“课程创建者”和“授课教师显示名”不一致，那就保留。

建议新增字段：

- `status`

可选值示例：

- `DRAFT`
- `ACTIVE`
- `ARCHIVED`

原因：

- 课程后期会有“停用/归档”需求，不建议靠删除课程来处理历史数据。

### 3. `materials`

建议保留字段：

- `id`
- `user_id`
- `course_id`
- `name`
- `file_name`
- `file_type`
- `file_size`
- `file_path`
- `description`
- `download_count`
- `like_count`
- `favorite_count`
- `comment_count`
- `created_at`
- `updated_at`

建议说明：

- `download_count`、`like_count`、`favorite_count`、`comment_count` 这些字段属于“冗余计数字段”。
- 它们可以保留，但只能当缓存值，不能当唯一真相来源。

如果保留这些字段，必须保证：

- 点赞/收藏/评论/下载时同步更新
- 允许定时校正

如果你不想维护缓存一致性：

- 可以删除 `like_count`
- 可以删除 `favorite_count`
- 可以删除 `comment_count`

然后通过关联表实时统计。

我的建议：

- `download_count` 保留
- `like_count` 保留
- `favorite_count` 保留
- `comment_count` 保留

前提是业务层统一维护，不然就删。

### 4. `comments`

建议保留字段：

- `id`
- `material_id`
- `user_id`
- `content`
- `created_at`
- `updated_at`

当前结构基本够用。

如果以后要做楼中楼，再新增：

- `parent_id`

### 5. `quizzes`

建议保留字段：

- `id`
- `user_id`
- `course_id`
- `title`
- `description`
- `question_count`
- `total_time`
- `type`
- `status`
- `created_at`
- `updated_at`

建议新增字段：

- `pass_score`

原因：

- 现在及格线在代码里写死为 60%，这应该是数据配置，不该硬编码在服务层。

可选新增：

- `published_at`
- `closed_at`

### 6. `questions`

建议保留字段：

- `id`
- `quiz_id`
- `content`
- `type`
- `options`
- `answer`
- `score`
- `analysis`
- `created_at`
- `updated_at`

当前这张表设计是合理的。

建议：

- `options` 如果继续用 JSON 字符串也可以，但长期建议改成 `json` 类型。

### 7. `quiz_records`

建议保留字段：

- `id`
- `user_id`
- `quiz_id`
- `score`
- `total_score`
- `accuracy`
- `time_used`
- `is_passed`
- `started_at`
- `finished_at`
- `created_at`
- `answers`
- `wrong_questions`

建议优化：

- `answers` 从 `text` 改为 `json`
- `wrong_questions` 从 `text` 改为 `json`

原因：

- 这两个字段本质就是结构化数据
- MySQL 8 已支持 `json`
- 后续查询和校验更方便

### 8. `tasks`

建议保留字段：

- `id`
- `user_id`
- `course_id`
- `title`
- `description`
- `status`
- `priority`
- `plan_date`
- `reminder_enabled`
- `reminder_time`
- `completed_at`
- `created_at`
- `updated_at`

如果未来要升级成“正式作业”系统，建议不要继续往 `tasks` 上硬堆字段，而是新建：

- `homeworks`
- `homework_questions`
- `homework_submissions`
- `homework_answers`

### 9. `study_records`

建议保留字段：

- `id`
- `user_id`
- `course_id`
- `study_date`
- `duration_minutes`
- `note`
- `created_at`

建议视业务保留字段：

- `start_time`
- `end_time`
- `task_count`

我的建议：

- 如果你只是做“学习统计”，保留 `duration_minutes` 就够了
- `start_time` 和 `end_time` 可以删除
- `task_count` 当前代码几乎没真正用起来，也可以删除

也就是说，`study_records` 可以精简成更偏统计型日志表。

### 10. `student_courses`

建议保留字段：

- `id`
- `student_id`
- `course_id`
- `created_at`

建议删除字段：

- `joined_at`

原因：

- 现在 `joined_at` 和 `created_at` 语义重复
- 实体类也只映射了 `created_at`

### 11. `user_material_actions`

建议保留字段：

- `id`
- `user_id`
- `material_id`
- `action_type`
- `created_at`

这张表设计合理，不需要精简。

### 12. `invitation_code`

建议保留字段：

- `id`
- `code`
- `description`
- `created_at`
- `expires_at`
- `is_used`
- `used_by_user_id`
- `used_at`

建议新增字段：

- `type`

例如：

- `TEACHER_REGISTER`
- `COURSE_JOIN`
- `ADMIN_ONLY`

原因：

- 现在靠 `description` 判断邀请码用途不够稳。

## 三、建议新增或调整的索引

建议补充：

- `users(role)`
- `users(employee_id)` 唯一索引，如果这个字段继续保留
- `courses(user_id, created_at)`
- `materials(course_id, created_at)`
- `materials(user_id, created_at)`
- `quizzes(course_id, status)`
- `quiz_records(user_id, quiz_id)`
- `tasks(user_id, status, plan_date)`
- `study_records(user_id, study_date)`

## 四、建议调整的外键删除策略

你现在很多外键是 `RESTRICT`，但代码里删除父对象时并没有总是先删子记录，这会导致运行时报错。

重点关注：

- `comments.material_id`
- `user_material_actions.material_id`
- `questions.quiz_id`
- `quiz_records.quiz_id`

建议：

- `comments.material_id` -> `ON DELETE CASCADE`
- `user_material_actions.material_id` -> `ON DELETE CASCADE`
- `questions.quiz_id` -> `ON DELETE CASCADE`
- `quiz_records.quiz_id` 视业务决定：
  - 如果允许删除测验连同记录一起删，用 `CASCADE`
  - 如果成绩记录必须保留，就不要删测验，而是加 `status=ARCHIVED`

## 五、最终推荐的“保留表”清单

- `users`
- `courses`
- `student_courses`
- `materials`
- `comments`
- `user_material_actions`
- `tasks`
- `study_records`
- `quizzes`
- `questions`
- `quiz_records`
- `invitation_code`

## 六、最终推荐的“删除表”清单

- `user`
- `course`
- `material`
- `quiz`
- `quiz_record`
- `study_log`
- `study_task`

## 七、最关键的三个优化动作

1. 先删除旧版重复表，只保留当前后端实体对应的表。
2. 再清理明显冗余字段：`student_courses.joined_at`、`users.verification_code`、以及不需要的旧统计字段。
3. 最后补索引和修正外键删除策略，不然以后删课程、删测验、删资料时会频繁踩坑。

## 八、我的最终判断

如果你现在要做毕业设计或继续开发，我建议你以 **当前 Spring Boot 实体对应的表结构为准**，不要再保留旧表。

真正“不需要”的，不是某一个小字段，而是整套旧表：

- `user/course/material/quiz/quiz_record/study_log/study_task`

真正值得优先清理的字段是：

- `student_courses.joined_at`
- `users.verification_code`
- `study_records.task_count`（如果你不做这个统计）
- `study_records.start_time`
- `study_records.end_time`（如果你只做时长统计）

如果你愿意，我下一步可以继续直接帮你：

1. 生成一份“最终版规范建表 SQL”
2. 或者基于这份 `studyhelper.sql` 给你写一份“迁移脚本”

