# StudyHelperSystem - 技术产品需求文档 (Technical PRD)

**文档版本**: v1.0.0  
**最后更新**: 2026-03-16  
**产品状态**: 开发中  
**编写者**: AI 技术专家  

---

## 文档目录

1. [项目概述](#1-项目概述)
2. [技术架构](#2-技术架构)
3. [功能模块详解](#3-功能模块详解)
4. [数据模型设计](#4-数据模型设计)
5. [API 接口规范](#5-api-接口规范)
6. [安全设计](#6-安全设计)
7. [前端架构](#7-前端架构)
8. [部署与运维](#8-部署与运维)
9. [开发规范](#9-开发规范)
10. [项目路线图](#10-项目路线图)

---

## 1. 项目概述

### 1.1 产品定位

**StudyHelperSystem** 是一款面向高校师生的学习辅助管理系统，旨在通过数字化手段提升教学效率和学习体验。

### 1.2 目标用户

| 角色 | 描述 | 核心需求 |
|-----|------|---------|
| **学生** | 高校在读学生 | 课程学习、作业提交、测验练习、资料下载 |
| **教师** | 授课教师 | 课程管理、作业发布、测验创建、学生管理 |
| **管理员** | 系统管理员 | 用户管理、内容审核、系统配置、数据统计 |

### 1.3 核心功能

- **课程管理**: 课程创建、加入课程、课程资料管理
- **作业系统**: 作业发布、提交、批改、反馈
- **测验系统**: 在线测验、自动评分、错题记录
- **资料中心**: 学习资料上传、下载、分类管理
- **学习统计**: 学习时长记录、成绩分析、进度跟踪
- **消息通知**: 系统通知、作业提醒、考试提醒

---

## 2. 技术架构

### 2.1 整体架构

```
┌─────────────────────────────────────────────────────────────────┐
│                         前端层 (Frontend)                        │
├─────────────────────────────────────────────────────────────────┤
│  Vue 3 + Vite + Pinia + Vue Router + Axios + ECharts           │
│  - 学生端界面                                                   │
│  - 教师端界面                                                   │
│  - 管理端界面                                                   │
└──────────────────────────┬──────────────────────────────────────┘
                           │ HTTPS
┌──────────────────────────┴──────────────────────────────────────┐
│                      API 网关层 (Gateway)                        │
├─────────────────────────────────────────────────────────────────┤
│  - 请求路由                                                      │
│  - 认证鉴权 (JWT)                                               │
│  - 限流熔断                                                      │
│  - 日志记录                                                      │
└──────────────────────────┬──────────────────────────────────────┘
                           │
┌──────────────────────────┴──────────────────────────────────────┐
│                     业务服务层 (Backend)                         │
├─────────────────────────────────────────────────────────────────┤
│  Spring Boot 3.2.2 + Spring Data JPA + Spring Security         │
│  ┌──────────┬──────────┬──────────┬──────────┬──────────┐      │
│  │ 用户服务  │ 课程服务  │ 作业服务  │ 测验服务  │ 资料服务  │      │
│  └──────────┴──────────┴──────────┴──────────┴──────────┘      │
└──────────────────────────┬──────────────────────────────────────┘
                           │
┌──────────────────────────┴──────────────────────────────────────┐
│                      数据存储层 (Data Layer)                     │
├──────────────────┬──────────────────┬───────────────────────────┤
│     MySQL 8.0    │   阿里云 OSS     │      (未来扩展)           │
│   - 用户数据      │   - 文件存储      │   - Redis 缓存           │
│   - 课程数据      │   - 资料文件      │   - Elasticsearch        │
│   - 作业数据      │   - 图片资源      │   - MongoDB              │
└──────────────────┴──────────────────┴───────────────────────────┘
```

### 2.2 技术栈详情

| 层级 | 技术选型 | 版本 | 用途 |
|-----|---------|------|------|
| **后端框架** | Spring Boot | 3.2.2 | 核心应用框架 |
| **数据访问** | Spring Data JPA | 3.2.2 | ORM框架 |
| **安全框架** | Spring Security | 3.2.2 | 认证授权 |
| **数据库** | MySQL | 8.0.33 | 关系型数据库 |
| **对象存储** | 阿里云 OSS | 3.17.2 | 文件存储 |
| **前端框架** | Vue | 3.5.25 | 用户界面 |
| **构建工具** | Vite | 7.3.1 | 前端构建 |
| **状态管理** | Pinia | 3.0.4 | 前端状态 |
| **图表库** | ECharts | 6.0.0 | 数据可视化 |
| **JDK版本** | Java | 17 | 运行环境 |

### 2.3 项目结构

```
StudyHelperSystem/
├── study-helper-backend/          # 后端项目
│   ├── src/main/java/com/studyhelper/
│   │   ├── controller/            # 控制器层 (9个)
│   │   ├── service/               # 业务层 (7个)
│   │   ├── repository/            # 数据访问层 (11个)
│   │   ├── entity/                # 实体类 (11个)
│   │   ├── config/                # 配置类 (8个)
│   │   └── StudyHelperApplication.java
│   ├── src/main/resources/
│   │   └── application.yml
│   └── pom.xml
│
├── study-helper-frontend/         # 前端项目
│   ├── src/
│   │   ├── views/                 # 页面组件 (25+)
│   │   ├── components/            # 公共组件
│   │   ├── router/                # 路由配置
│   │   ├── stores/                # Pinia状态管理
│   │   ├── api/                   # API接口封装
│   │   ├── assets/                # 静态资源
│   │   ├── styles/                # 样式文件
│   │   ├── App.vue
│   │   └── main.js
│   ├── public/
│   ├── index.html
│   ├── package.json
│   └── vite.config.js
│
└── docs/                          # 项目文档
    ├── 功能开发文档.md
    └── 功能评估报告.md
```

---

## 3. 功能模块详解

### 3.1 用户管理模块

#### 3.1.1 功能列表

| 功能 | 描述 | 角色 |
|-----|------|------|
| 学生注册 | 学号/用户名/密码注册 | 学生 |
| 教师注册 | 邀请码或教职工验证注册 | 教师 |
| 用户登录 | 账号密码登录 | 全部 |
| 信息修改 | 修改个人资料 | 全部 |
| 密码重置 | 忘记密码重置 | 全部 |
| 身份验证 | 教师身份验证 | 教师 |

#### 3.1.2 用户角色权限矩阵

| 功能 | 学生 | 教师 | 管理员 |
|-----|------|------|--------|
| 课程浏览 | ✓ | ✓ | ✓ |
| 加入课程 | ✓ | ✗ | ✗ |
| 创建课程 | ✗ | ✓ | ✓ |
| 发布作业 | ✗ | ✓ | ✗ |
| 提交作业 | ✓ | ✗ | ✗ |
| 批改作业 | ✗ | ✓ | ✗ |
| 创建测验 | ✗ | ✓ | ✗ |
| 参加测验 | ✓ | ✗ | ✗ |
| 用户管理 | ✗ | ✗ | ✓ |
| 系统配置 | ✗ | ✗ | ✓ |

### 3.2 课程管理模块

#### 3.2.1 功能列表

| 功能 | 描述 | 优先级 |
|-----|------|-------|
| 创建课程 | 教师创建课程，生成邀请码 | P0 |
| 加入课程 | 学生通过邀请码加入课程 | P0 |
| 课程列表 | 查看已加入/创建的课程 | P0 |
| 课程详情 | 查看课程信息、资料、作业 | P0 |
| 课程分类 | 必修/选修/公共课/其他 | P1 |
| 学生管理 | 查看课程学生列表 | P1 |

#### 3.2.2 课程分类枚举

```java
public enum Category {
    REQUIRED("必修"),
    ELECTIVE("选修"),
    PUBLIC("公共课"),
    OTHER("其他");
}
```

### 3.3 作业管理模块

#### 3.3.1 功能列表

| 功能 | 描述 | 角色 | 状态 |
|-----|------|------|------|
| 发布作业 | 创建作业，设置截止日期 | 教师 | 已实现 |
| 编辑作业 | 修改作业内容 | 教师 | 已实现 |
| 删除作业 | 删除已发布作业 | 教师 | 已实现 |
| 提交作业 | 学生提交作业内容/附件 | 学生 | **待实现** |
| 批改作业 | 教师评分和评语 | 教师 | **待实现** |
| 查看成绩 | 学生查看作业成绩 | 学生 | **待实现** |

### 3.4 测验系统模块

#### 3.4.1 功能列表

| 功能 | 描述 | 角色 | 状态 |
|-----|------|------|------|
| 创建测验 | 选择题目、设置时间 | 教师 | 已实现 |
| 题目管理 | 单选/多选/填空题 | 教师 | 已实现 |
| 参加测验 | 在线答题 | 学生 | 已实现 |
| 自动评分 | 客观题自动评分 | 系统 | 已实现 |
| 查看结果 | 查看得分和解析 | 学生 | 已实现 |
| 错题记录 | 自动记录错题 | 学生 | **待实现** |

---

## 4. 数据模型设计

### 4.1 核心实体定义

#### User (用户表)

| 字段 | 类型 | 约束 | 说明 |
|-----|------|------|------|
| id | Long | PK | 主键 |
| username | String | UNIQUE, NOT NULL | 用户名 |
| password | String | NOT NULL | 加密密码 |
| email | String | UNIQUE | 邮箱 |
| student_id | String | UNIQUE | 学号 |
| nickname | String | | 昵称 |
| avatar | String | | 头像URL |
| major | String | | 专业 |
| grade | String | | 年级 |
| role | Enum | NOT NULL | STUDENT/TEACHER/ADMIN |
| teacher_title | String | | 教师职称 |
| department | String | | 所属院系 |
| employee_id | String | | 教职工编号 |
| is_verified | Boolean | DEFAULT false | 是否已验证 |
| created_at | LocalDateTime | | 创建时间 |
| updated_at | LocalDateTime | | 更新时间 |

#### Course (课程表)

| 字段 | 类型 | 约束 | 说明 |
|-----|------|------|------|
| id | Long | PK | 主键 |
| name | String | NOT NULL | 课程名称 |
| category | Enum | NOT NULL | 课程分类 |
| teacher | String | | 授课教师 |
| schedule | String | | 上课时间安排 |
| location | String | | 上课地点 |
| remark | String | | 课程备注 |
| invitation_code | String | UNIQUE | 邀请码 |
| user_id | Long | FK | 创建者ID |

### 4.2 数据库表关系

```sql
-- 用户表
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) UNIQUE,
    student_id VARCHAR(50) UNIQUE,
    nickname VARCHAR(50),
    avatar VARCHAR(255),
    major VARCHAR(100),
    grade VARCHAR(20),
    role VARCHAR(20) NOT NULL DEFAULT 'STUDENT',
    teacher_title VARCHAR(50),
    department VARCHAR(100),
    employee_id VARCHAR(50),
    is_verified BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 课程表
CREATE TABLE courses (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(200) NOT NULL,
    category VARCHAR(20) NOT NULL DEFAULT 'REQUIRED',
    teacher VARCHAR(100),
    schedule VARCHAR(200),
    location VARCHAR(200),
    remark TEXT,
    invitation_code VARCHAR(20) UNIQUE,
    user_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- 学生课程关联表
CREATE TABLE student_courses (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    student_id BIGINT NOT NULL,
    course_id BIGINT NOT NULL,
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_student_course (student_id, course_id),
    FOREIGN KEY (student_id) REFERENCES users(id),
    FOREIGN KEY (course_id) REFERENCES courses(id)
);
```

---

## 5. API 接口规范

### 5.1 统一响应格式

```json
{
  "code": 200,
  "message": "success",
  "data": {},
  "timestamp": 1710567890123,
  "requestId": "req_abc123"
}
```

### 5.2 核心接口列表

| 接口 | 方法 | 路径 | 说明 |
|-----|------|------|------|
| 学生注册 | POST | /api/auth/register | 学生账号注册 |
| 学生登录 | POST | /api/auth/login | 学生账号登录 |
| 教师注册 | POST | /api/auth/teacher/register | 教师账号注册 |
| 教师登录 | POST | /api/auth/teacher/login | 教师账号登录 |
| 获取课程列表 | GET | /api/courses | 获取当前用户课程列表 |
| 创建课程 | POST | /api/courses | 教师创建新课程 |
| 加入课程 | POST | /api/courses/join | 学生通过邀请码加入课程 |
| 获取作业列表 | GET | /api/courses/{courseId}/tasks | 获取课程作业列表 |
| 创建作业 | POST | /api/courses/{courseId}/tasks | 教师创建作业 |
| 提交作业 | POST | /api/tasks/{id}/submit | 学生提交作业 |
| 获取测验列表 | GET | /api/courses/{courseId}/quizzes | 获取课程测验列表 |
| 创建测验 | POST | /api/courses/{courseId}/quizzes | 教师创建测验 |
| 获取资料列表 | GET | /api/courses/{courseId}/materials | 获取课程资料列表 |
| 上传资料 | POST | /api/courses/{courseId}/materials | 上传学习资料 |

---

## 6. 安全设计

### 6.1 当前认证机制

当前系统使用简单的Token机制：
```java
String token = "teacher_token_" + user.getId() + "_" + System.currentTimeMillis();
```

**存在的问题**:
- Token未加密，容易被伪造
- 无过期时间机制
- 未实现Token刷新
- 服务端无法强制失效

### 6.2 JWT认证方案（建议实现）

```java
// JWT Token结构
{
  "header": { "alg": "HS256", "typ": "JWT" },
  "payload": {
    "sub": "user_id",
    "role": "STUDENT",
    "iat": 1710567890,
    "exp": 1710654290
  },
  "signature": "..."
}
```

### 6.3 数据安全措施

| 安全措施 | 实现方式 | 状态 |
|---------|---------|------|
| 密码加密 | BCryptPasswordEncoder | 已实现 |
| SQL注入防护 | JPA参数化查询 | 已实现 |
| XSS防护 | 前端输入过滤 | 待实现 |
| CSRF防护 | Spring Security配置 | 待实现 |

---

## 7. 前端架构

### 7.1 技术栈

| 技术 | 版本 | 用途 |
|-----|------|------|
| Vue | 3.5.25 | 前端框架 |
| Vue Router | 4.6.4 | 路由管理 |
| Pinia | 3.0.4 | 状态管理 |
| Axios | 1.13.5 | HTTP请求 |
| ECharts | 6.0.0 | 数据可视化 |
| Vite | 7.3.1 | 构建工具 |

### 7.2 路由设计

| 路由 | 组件 | 说明 | 权限 |
|-----|------|------|------|
| /login | Login.vue | 登录页 | 公开 |
| /register | Register.vue | 注册页 | 公开 |
| / | Home.vue | 首页 | 已登录 |
| /courses | Courses.vue | 课程列表 | 已登录 |
| /courses/:id | CourseDetail.vue | 课程详情 | 已登录 |
| /teacher | TeacherOverview.vue | 教师工作台 | 教师 |
| /admin | AdminDashboard.vue | 管理后台 | 管理员 |

---

## 8. 开发计划

### 8.1 功能优先级

| 优先级 | 功能模块 | 工作量 | 状态 |
|--------|---------|--------|------|
| P0 | 作业提交功能 | 2人周 | 待实现 |
| P0 | JWT认证机制 | 1人周 | 待实现 |
| P0 | 错题本功能 | 1人周 | 待实现 |
| P1 | 消息中心 | 1.5人周 | 待实现 |
| P1 | 学习计时 | 1人周 | 待实现 |

### 8.2 代码统计

| 模块 | 文件数量 | 说明 |
|-----|---------|------|
| Controller | 9个 | 控制器层 |
| Service | 7个 | 业务层 |
| Repository | 11个 | 数据访问层 |
| Entity | 11个 | 实体类 |
| Config | 8个 | 配置类 |
| 前端Views | 25+ | 页面组件 |

---

## 9. 附录

### 9.1 后端依赖

```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>
    <dependency>
        <groupId>mysql</groupId>
        <artifactId>mysql-connector-java</artifactId>
        <version>8.0.33</version>
    </dependency>
    <dependency>
        <groupId>com.aliyun.oss</groupId>
        <artifactId>aliyun-sdk-oss</artifactId>
        <version>3.17.2</version>
    </dependency>
</dependencies>
```

### 9.2 前端依赖

```json
{
  "dependencies": {
    "axios": "^1.13.5",
    "echarts": "^6.0.0",
    "pinia": "^3.0.4",
    "vue": "^3.5.25",
    "vue-router": "^4.6.4"
  },
  "devDependencies": {
    "@vitejs/plugin-vue": "^6.0.2",
    "vite": "^7.3.1"
  }
}
```

---

**文档结束**
