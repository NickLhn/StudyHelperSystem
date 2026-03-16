# StudyHelperSystem 技术架构文档

> **学习辅助系统** - 面向师生的在线教学管理平台
> 
> 生成日期: 2026-03-16

---

## 目录

1. [项目概述](#1-项目概述)
2. [系统架构总览](#2-系统架构总览)
3. [后端架构](#3-后端架构)
4. [前端架构](#4-前端架构)
5. [数据库设计](#5-数据库设计)
6. [安全设计](#6-安全设计)
7. [部署架构](#7-部署架构)

---

## 1. 项目概述

### 1.1 项目简介

**StudyHelper（学习辅助系统）** 是一个面向教育场景的在线教学管理平台，支持学生、教师、管理员三种角色，提供完整的课程管理、作业管理、测验考试、学习统计等功能。

### 1.2 核心功能

| 功能模块 | 描述 |
|----------|------|
| **用户管理** | 学生/教师/管理员注册登录、权限管理 |
| **课程管理** | 课程创建、编辑、发布、选课 |
| **作业管理** | 作业发布、提交、批改、成绩统计 |
| **测验考试** | 在线测验、自动批改、成绩分析 |
| **资料管理** | 学习资料上传、下载、分类管理 |
| **学习统计** | 学习时长、进度追踪、成绩分析 |
| **错题本** | 错题记录、标记、重练 |
| **消息中心** | 系统通知、作业提醒 |

### 1.3 用户角色

| 角色 | 权限 |
|------|------|
| **学生** | 选课学习、提交作业、参加测验、查看成绩 |
| **教师** | 创建课程、发布作业/测验、批改作业、管理学生 |
| **管理员** | 用户管理、内容审核、系统监控、邀请码管理 |

---

## 2. 系统架构总览

### 2.1 整体架构

```
┌─────────────────────────────────────────────────────────────────┐
│                        前端层 (Vue 3)                            │
├───────────────────┬───────────────────┬─────────────────────────┤
│     学生端         │     教师端         │       管理端            │
│   /student/*      │   /teacher/*      │       /admin/*          │
└───────────────────┴───────────────────┴─────────────────────────┘
                              │
                              ▼ HTTPS + JWT
┌─────────────────────────────────────────────────────────────────┐
│                    API网关层 (Spring Security)                   │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                   应用服务层 (Spring Boot)                       │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐           │
│  │ 认证服务  │ │ 用户服务  │ │ 课程服务  │ │ 作业服务  │           │
│  └──────────┘ └──────────┘ └──────────┘ └──────────┘           │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐           │
│  │ 测验服务  │ │ 资料服务  │ │ 统计服务  │ │ 管理服务  │           │
│  └──────────┘ └──────────┘ └──────────┘ └──────────┘           │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                   数据存储层 (MySQL + OSS)                       │
└─────────────────────────────────────────────────────────────────┘
```

### 2.2 技术栈

| 层级 | 技术 | 版本 |
|------|------|------|
| **前端** | Vue | 3.5.25 |
| | Vue Router | 4.6.4 |
| | Pinia | 3.0.4 |
| | Vite | 7.3.1 |
| | Axios | 1.13.5 |
| | ECharts | 6.0.0 |
| **后端** | Spring Boot | 3.2.2 |
| | Spring Security | 6.x |
| | Spring Data JPA | 3.x |
| | MySQL | 8.0.33 |
| | 阿里云OSS | 3.17.2 |
| **Java** | JDK | 17 |

---

## 3. 后端架构

### 3.1 项目结构

```
study-helper-backend/
├── pom.xml
├── src/
│   ├── main/
│   │   ├── java/com/studyhelper/
│   │   │   ├── StudyHelperApplication.java
│   │   │   ├── config/              # 配置类
│   │   │   │   ├── SecurityConfig.java
│   │   │   │   ├── CorsConfig.java
│   │   │   │   └── AliyunOssConfig.java
│   │   │   ├── controller/          # 控制器层
│   │   │   │   ├── AuthController.java
│   │   │   │   ├── UserController.java
│   │   │   │   ├── CourseController.java
│   │   │   │   ├── TaskController.java
│   │   │   │   ├── QuizController.java
│   │   │   │   ├── MaterialController.java
│   │   │   │   ├── StatisticController.java
│   │   │   │   └── AdminController.java
│   │   │   ├── service/             # 服务层
│   │   │   ├── repository/          # 数据访问层
│   │   │   ├── entity/              # 实体类
│   │   │   └── dto/                 # DTO
│   │   └── resources/
│   │       └── application.properties
│   └── test/
└── sql/                             # 数据库脚本
```

### 3.2 核心依赖

```xml
<dependencies>
    <!-- Web -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    
    <!-- Data JPA -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    
    <!-- Security -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>
    
    <!-- MySQL -->
    <dependency>
        <groupId>mysql</groupId>
        <artifactId>mysql-connector-java</artifactId>
        <version>8.0.33</version>
    </dependency>
    
    <!-- Aliyun OSS -->
    <dependency>
        <groupId>com.aliyun.oss</groupId>
        <artifactId>aliyun-sdk-oss</artifactId>
        <version>3.17.2</version>
    </dependency>
</dependencies>
```

### 3.3 配置说明

```properties
# Server
server.port=8080
spring.application.name=study-helper-backend

# Database
spring.datasource.url=jdbc:mysql://localhost:3306/studyhelper?useSSL=false&serverTimezone=Asia/Shanghai
spring.datasource.username=root
spring.datasource.password=root
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect

# File Upload
spring.servlet.multipart.max-file-size=30MB
spring.servlet.multipart.max-request-size=30MB
```

---

## 4. 前端架构

### 4.1 项目结构

```
study-helper-frontend/
├── package.json
├── vite.config.js
├── src/
│   ├── main.js
│   ├── App.vue
│   ├── router/
│   │   └── index.js
│   ├── stores/
│   │   └── user.js
│   ├── api/
│   │   ├── user.js
│   │   ├── course.js
│   │   ├── task.js
│   │   ├── quiz.js
│   │   ├── material.js
│   │   └── admin.js
│   ├── views/
│   │   ├── Home.vue
│   │   ├── Login.vue
│   │   ├── Register.vue
│   │   ├── StudentLayout.vue    # 学生端布局
│   │   ├── TeacherLayout.vue    # 教师端布局
│   │   ├── AdminLayout.vue      # 管理端布局
│   │   └── ...
│   ├── components/
│   └── assets/
└── public/
```

### 4.2 依赖说明

```json
{
  "dependencies": {
    "vue": "^3.5.25",
    "vue-router": "^4.6.4",
    "pinia": "^3.0.4",
    "axios": "^1.13.5",
    "echarts": "^6.0.0"
  },
  "devDependencies": {
    "vite": "^7.3.1",
    "@vitejs/plugin-vue": "^6.0.2"
  }
}
```

### 4.3 路由设计

```javascript
// 核心路由结构
├── /                      # 首页
├── /login                 # 登录
├── /register              # 注册
├── /profile               # 个人中心
├── /student               # 学生端
│   ├── /student/courses   # 我的课程
│   ├── /student/materials # 学习资料
│   └── /student/statistics# 学习统计
├── /teacher               # 教师端
│   ├── /teacher/dashboard # 仪表盘
│   ├── /teacher/courses   # 课程管理
│   ├── /teacher/tasks     # 作业管理
│   └── /teacher/quizzes   # 测验管理
├── /admin                 # 管理端
│   ├── /admin/dashboard   # 仪表盘
│   ├── /admin/users       # 用户管理
│   └── /admin/content     # 内容管理
```

---

## 5. 数据库设计

### 5.1 核心实体关系

| 实体 | 说明 |
|------|------|
| **User** | 用户(学生/教师/管理员) |
| **Course** | 课程 |
| **StudentCourse** | 学生选课关系 |
| **Task** | 作业 |
| **TaskSubmission** | 作业提交 |
| **Quiz** | 测验 |
| **Question** | 题目 |
| **QuizRecord** | 测验记录 |
| **WrongQuestion** | 错题 |
| **Material** | 学习资料 |
| **StudyRecord** | 学习记录 |
| **InvitationCode** | 邀请码 |

### 5.2 核心表结构

```sql
-- 用户表
CREATE TABLE sys_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role ENUM('STUDENT', 'TEACHER', 'ADMIN') NOT NULL,
    email VARCHAR(100),
    phone VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 课程表
CREATE TABLE course (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    teacher_id BIGINT NOT NULL,
    status ENUM('DRAFT', 'PUBLISHED', 'ARCHIVED') DEFAULT 'DRAFT',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 学生选课表
CREATE TABLE student_course (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    student_id BIGINT NOT NULL,
    course_id BIGINT NOT NULL,
    status ENUM('ACTIVE', 'COMPLETED', 'DROPPED') DEFAULT 'ACTIVE',
    progress INT DEFAULT 0
);

-- 作业表
CREATE TABLE task (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    course_id BIGINT NOT NULL,
    title VARCHAR(100) NOT NULL,
    description TEXT,
    deadline TIMESTAMP,
    total_score INT DEFAULT 100
);

-- 测验表
CREATE TABLE quiz (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    course_id BIGINT NOT NULL,
    title VARCHAR(100) NOT NULL,
    time_limit INT,
    total_score INT DEFAULT 100
);

-- 题目表
CREATE TABLE question (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    quiz_id BIGINT NOT NULL,
    type ENUM('SINGLE', 'MULTIPLE', 'FILL', 'JUDGE', 'ESSAY'),
    content TEXT NOT NULL,
    options JSON,
    answer TEXT NOT NULL,
    score INT DEFAULT 10
);
```

---

## 6. 安全设计

### 6.1 认证机制

- **JWT Token**: 无状态认证，支持Token刷新
- **Spring Security**: 权限控制、角色隔离
- **密码加密**: BCrypt加密存储

### 6.2 安全防护

- **XSS防护**: 输入过滤、输出转义
- **CSRF防护**: JWT天然防护
- **SQL注入**: JPA参数化查询
- **CORS**: 配置跨域白名单

---

## 7. 部署架构

### 7.1 开发环境启动

```bash
# 1. 启动数据库
mysql -u root -p < sql/init.sql

# 2. 启动后端
cd study-helper-backend
mvn spring-boot:run

# 3. 启动前端
cd study-helper-frontend
npm install
npm run dev
```

### 7.2 服务端口

| 服务 | 端口 |
|------|------|
| Backend | 8080 |
| Frontend | 5173 |
| MySQL | 3306 |

### 7.3 生产部署架构

```
Nginx (80/443)
├── /api/*  →  proxy_pass to localhost:8080 (Backend)
└── /*      →  static files (Frontend)
```

---

*文档版本: 1.0 | 生成日期: 2026-03-16*
