# 学习辅助系统下一阶段功能路线图

## 1. 总体结论

这 6 项需求里，最应该优先做的是：

1. 作业自动批改模块
2. 教师成绩分析
3. 测验防作弊
4. 通知提醒
5. 资料中心增强
6. 学期/课程归档

原因很明确：

- `作业自动批改` 是当前最缺的核心业务闭环，直接补齐“教师发布 -> 学生提交 -> 系统判分 -> 教师复核 -> 学生查看成绩”。
- `教师成绩分析` 与自动批改高度耦合，做完作业记录后就能立即产出数据价值。
- `测验防作弊` 是答辩亮点功能，技术展示效果强，而且可以复用到后续作业考试化场景。
- `通知提醒` 能把前面几个模块串起来，不然很多功能会停留在“做了，但没人感知”。
- `资料中心增强` 和 `课程归档` 更偏产品完善度，适合放到第二阶段。

## 2. 推荐开发顺序

### Phase 1：核心闭环

目标：先把系统从“教学工具集合”变成“可交付的作业系统”。

包含：

- 作业自动批改模块
- 教师成绩分析
- 基础通知提醒

### Phase 2：亮点增强

目标：增强系统专业度和答辩说服力。

包含：

- 测验防作弊
- 资料中心增强

### Phase 3：运营与生命周期

目标：让课程和内容管理更接近真实教学平台。

包含：

- 学期/课程归档
- 归档课程统计与历史数据查看

## 3. 模块拆解

---

## 3.1 作业自动批改模块

### 当前现状

- 现有 `tasks` 更像教师个人教学任务，不是正式作业。
- 现有 `quizzes / questions / quiz_records` 已经具备“题目 + 标准答案 + 自动评分”的雏形。
- 最合理的做法不是继续扩展 `tasks`，而是新增独立 `homework` 体系，并复用测验自动判分逻辑。

### 建议数据表

#### `homeworks`

- `id`
- `title`
- `description`
- `course_id`
- `teacher_id`
- `status`
  - `DRAFT`
  - `PUBLISHED`
  - `CLOSED`
- `homework_type`
  - `PRACTICE`
  - `ASSIGNMENT`
- `total_score`
- `question_count`
- `allow_late_submit`
- `auto_grade_enabled`
- `deadline_at`
- `published_at`
- `created_at`
- `updated_at`

#### `homework_questions`

- `id`
- `homework_id`
- `sort_order`
- `type`
  - `SINGLE_CHOICE`
  - `TRUE_FALSE`
  - `FILL_BLANK`
  - `SHORT_ANSWER`
- `content`
- `options_json`
- `answer_json`
- `analysis`
- `score`
- `keyword_rules_json`
- `manual_review_required`
- `created_at`
- `updated_at`

#### `homework_submissions`

- `id`
- `homework_id`
- `student_id`
- `status`
  - `DRAFT`
  - `SUBMITTED`
  - `AUTO_GRADED`
  - `REVIEWED`
- `submitted_at`
- `auto_graded_at`
- `reviewed_at`
- `objective_score`
- `subjective_score`
- `final_score`
- `accuracy`
- `teacher_comment`
- `is_late`
- `attempt_no`
- `created_at`
- `updated_at`

#### `homework_answers`

- `id`
- `submission_id`
- `question_id`
- `student_answer_json`
- `is_correct`
- `score_awarded`
- `auto_score`
- `teacher_score`
- `needs_review`
- `review_status`
- `match_detail_json`
- `teacher_comment`
- `created_at`
- `updated_at`

### 自动批改规则

#### 客观题

- 单选题：答案完全匹配
- 判断题：答案完全匹配
- 填空题：
  - 忽略首尾空格
  - 可选忽略大小写
  - 支持多个等价答案
  - 支持部分得分规则

#### 主观题

- 先不直接自动给最终分
- 系统给出：
  - 关键词命中情况
  - 参考答案匹配摘要
  - 建议分值
- 最终分数由教师确认

### 后端接口建议

- `POST /api/homework/create`
- `GET /api/homework/my`
- `GET /api/homework/course`
- `GET /api/homework/{id}`
- `PATCH /api/homework/{id}/status`
- `POST /api/homework/{id}/submit`
- `GET /api/homework/submissions`
- `GET /api/homework/submissions/{id}`
- `PATCH /api/homework/submissions/{id}/review`
- `GET /api/homework/grade-summary`

### 前端页面建议

- 教师端：
  - 作业列表页
  - 作业创建页
  - 提交记录页
  - 批改中心页
- 学生端：
  - 作业列表页
  - 作业答题页
  - 提交结果页

### 优先级

- `P0`

---

## 3.2 教师成绩分析

### 当前现状

- 现在教师成绩页已经接入了课程测验统计，但数据维度偏浅。
- 下一步应该把成绩分析从“页面展示”升级成“分析模块”。

### 必做指标

- 班级提交率
- 平均分
- 最高分 / 最低分
- 通过率
- 每题正确率
- 错题热力图
- 主观题待复核数
- 学生个人成绩趋势

### 可复用数据来源

- `quiz_records`
- `homework_submissions`
- `homework_answers`

### 页面结构建议

- 顶部摘要卡：
  - 提交率
  - 平均分
  - 待复核
  - 错题率最高题目
- 中部图表：
  - 成绩分布柱状图
  - 每题正确率条形图
  - 错题热力图
- 底部表格：
  - 学生成绩明细
  - 每次提交记录

### 后端接口建议

- `GET /api/analytics/course-overview`
- `GET /api/analytics/homework-score-distribution`
- `GET /api/analytics/question-accuracy`
- `GET /api/analytics/wrong-question-heatmap`
- `GET /api/analytics/student-progress`

### 优先级

- `P0`

---

## 3.3 资料中心增强

### 当前现状

- 现有 `materials` 只有基础文件属性和课程关联。
- 缺少分类、标签、版本说明、筛选与搜索支撑。

### 建议字段扩展

在 `materials` 表上新增：

- `category`
  - `SLIDE`
  - `ASSIGNMENT`
  - `REFERENCE`
  - `VIDEO`
  - `OTHER`
- `tags_json`
- `version_no`
- `version_note`
- `is_latest`
- `published_at`

如要做标准化版本历史，新增：

#### `material_versions`

- `id`
- `material_id`
- `version_no`
- `file_path`
- `file_name`
- `file_size`
- `version_note`
- `created_by`
- `created_at`

### 页面能力建议

- 分类筛选
- 标签搜索
- 关键字搜索
- 最新版本标识
- 版本历史抽屉
- 资料更新说明

### 优先级

- `P1`

---

## 3.4 测验防作弊

### 当前现状

- 已有基础计时字段：`Quiz.totalTime`、`QuizRecord.timeUsed`
- 但缺少真正的考试过程控制。

### 建议能力

#### 第一阶段

- 倒计时作答
- 自动保存答案
- 限制作答次数
- 超时自动提交

#### 第二阶段

- 题目随机排序
- 选项随机排序
- 切屏告警
- 多次切屏记录到提交记录

### 建议字段扩展

在 `quizzes` 表新增：

- `max_attempts`
- `random_question_order`
- `random_option_order`
- `auto_save_enabled`
- `anti_cheat_enabled`
- `switch_warning_limit`

在 `quiz_records` 表新增：

- `attempt_no`
- `switch_count`
- `auto_submitted`
- `last_saved_at`
- `submitted_from`

### 优先级

- `P1`

---

## 3.5 通知提醒

### 当前现状

- 前端有 `Messages.vue` 页面，但后端没有消息模型。
- 这块应该作为连接多个业务模块的横向能力来做。

### 建议数据表

#### `notifications`

- `id`
- `user_id`
- `type`
  - `HOMEWORK_DUE`
  - `QUIZ_PUBLISHED`
  - `MATERIAL_UPDATED`
  - `GRADE_RELEASED`
  - `SYSTEM`
- `title`
- `content`
- `related_type`
- `related_id`
- `is_read`
- `read_at`
- `created_at`

### 触发场景

- 作业发布
- 作业临近截止
- 测验发布
- 资料更新
- 成绩发布
- 教师复核完成

### 页面能力建议

- 消息列表
- 未读徽标
- 按类型筛选
- 一键已读
- 跳转到关联页面

### 优先级

- `P1`

---

## 3.6 学期/课程归档

### 当前现状

- 课程只有基础属性，没有生命周期状态。
- 当课程增多后，教师端和学生端都会越来越混乱。

### 建议字段扩展

在 `courses` 表新增：

- `status`
  - `ACTIVE`
  - `ARCHIVED`
- `term`
  - 如 `2026-Spring`
- `archived_at`

### 页面能力建议

- 课程筛选：
  - 进行中
  - 已归档
  - 历史课程
- 归档课程只读化
- 历史课程统计可查看但默认不展示

### 优先级

- `P2`

## 4. 推荐实际开发批次

### 批次 A

- Homework 实体与表结构
- 学生提交
- 客观题自动判分
- 教师复核主观题

### 批次 B

- 教师成绩分析
- 作业提交率与平均分
- 每题正确率
- 错题热力图

### 批次 C

- 通知表与消息中心
- 作业截止提醒
- 测验发布提醒
- 成绩发布提醒

### 批次 D

- 资料分类、标签、搜索
- 版本说明

### 批次 E

- 测验防作弊
- 课程归档

## 5. 最适合当前项目的下一步

如果只选一个模块立刻开工，推荐：

### 第一优先

- `作业自动批改模块`

理由：

- 最能补齐系统核心闭环
- 最容易和现有测验判分逻辑复用
- 最能带动成绩分析和消息提醒
- 最适合作为毕业设计主亮点

### 紧随其后

- `教师成绩分析`

理由：

- 有了提交记录后，做分析的边际成本很低
- 页面展示效果强，答辩演示最直观

## 6. 建议的下一次开发动作

下一轮直接开做以下内容：

1. 新增 `Homework / HomeworkQuestion / HomeworkSubmission / HomeworkAnswer` 后端模型
2. 完成客观题自动批改
3. 补教师端作业列表、学生端作业提交页
4. 在教师成绩中心接入作业统计数据

这套顺序最稳，也最能尽快看到成果。
