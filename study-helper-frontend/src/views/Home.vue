<template>
  <div class="home">
    <template v-if="userStore.isLoggedIn && userStore.isStudent">
      <div class="student-layout">
        <!-- 左侧导航栏 -->
        <aside class="student-sidebar">
          <div class="sidebar-header">
            <div class="logo-container">
              <router-link to="/" class="logo-link">
                <div class="logo-icon">📚</div>
                <div class="logo-text">学习辅助系统</div>
              </router-link>
            </div>
            <div class="user-info">
              <router-link to="/profile" class="avatar-btn">
                <img v-if="avatarUrl" :src="avatarUrl" alt="avatar" class="avatar" />
                <div v-else class="avatar-fallback">{{ avatarFallback }}</div>
              </router-link>
              <div class="welcome-sub">{{ userDisplayName }}</div>
            </div>
          </div>
          
          <nav class="sidebar-nav">
            <router-link to="/" class="nav-item active">
              <span class="nav-icon">🏠</span>
              <span class="nav-text">首页</span>
            </router-link>
            <router-link to="/courses" class="nav-item">
              <span class="nav-icon">📚</span>
              <span class="nav-text">课程学习</span>
            </router-link>
            <router-link to="/materials" class="nav-item">
              <span class="nav-icon">📁</span>
              <span class="nav-text">资料下载</span>
            </router-link>
            <router-link to="/statistics" class="nav-item">
              <span class="nav-icon">📊</span>
              <span class="nav-text">成绩查询</span>
            </router-link>
          </nav>
          
          <div class="sidebar-footer">
            <router-link to="/profile" class="nav-item">
              <span class="nav-icon">👤</span>
              <span class="nav-text">个人中心</span>
            </router-link>
            <router-link to="/settings" class="nav-item">
              <span class="nav-icon">⚙️</span>
              <span class="nav-text">设置</span>
            </router-link>
            <button class="nav-item logout" type="button" @click="logout">
              <span class="nav-icon">⏻</span>
              <span class="nav-text">退出</span>
            </button>
          </div>
        </aside>
        
        <!-- 右侧内容区 -->
        <main class="student-content">
          <header class="content-header">
            <h1 class="page-title">学习看板</h1>
            <div class="header-actions">
              <router-link to="/messages" class="icon-btn" aria-label="消息中心">
                <span class="icon">💬</span>
                <span v-if="unreadCount > 0" class="badge">{{ unreadCount }}</span>
              </router-link>
              <div class="search">
                <input
                  v-model="searchText"
                  class="search-input"
                  type="text"
                  placeholder="搜索资料…"
                  @keydown.enter="submitSearch"
                />
                <button class="search-btn" type="button" @click="submitSearch">搜索</button>
              </div>
            </div>
          </header>
          
          <section class="core">
            <!-- 学习看板 -->
            <div class="core-card dashboard-card">
              <div class="core-icon">✅</div>
              <div class="core-info">
                <div class="core-title">学习看板</div>
                <div class="core-sub">{{ todayTodoText }}</div>
              </div>
            </div>

            <!-- 我的课程 -->
            <router-link to="/courses" class="core-card">
              <div class="core-icon">📚</div>
              <div class="core-info">
                <div class="core-title">我的课程</div>
                <div class="core-sub">管理课程与上课信息</div>
              </div>
            </router-link>

            <router-link to="/materials" class="core-card">
              <div class="core-icon">📁</div>
              <div class="core-info">
                <div class="core-title">资料中心</div>
                <div class="core-sub">上传、下载与收藏</div>
              </div>
            </router-link>

            <router-link to="/statistics" class="core-card">
              <div class="core-icon">📊</div>
              <div class="core-info">
                <div class="core-title">学习统计</div>
                <div class="core-sub">学习时长与完成情况</div>
              </div>
            </router-link>
          </section>

          <!-- 学习统计卡片 -->
          <section class="stats-section">
            <h2 class="section-title">学习统计</h2>
            <div class="stats-grid">
              <div class="stat-card">
                <div class="stat-icon">⏰</div>
                <div class="stat-info">
                  <div class="stat-value">{{ studyStats.totalStudyTime }}h</div>
                  <div class="stat-label">总学习时长</div>
                </div>
              </div>
            </div>
          </section>

          <!-- 最近课程 -->
          <section class="courses-section">
            <div class="section-header">
              <h2 class="section-title">最近课程</h2>
              <router-link to="/courses" class="section-link">查看全部</router-link>
            </div>
            <div class="courses-grid">
              <div v-if="recentCourses.length === 0" class="empty-state">
                <div class="empty-icon">📚</div>
                <p>暂无课程，快去添加课程吧！</p>
                <router-link to="/courses" class="btn-primary">添加课程</router-link>
              </div>
              <router-link v-else v-for="course in recentCourses" :key="course.id" :to="`/courses`" class="course-card">
                <div class="course-icon">{{ course.name.charAt(0) }}</div>
                <div class="course-info">
                  <h3 class="course-name">{{ course.name }}</h3>
                  <p class="course-teacher">{{ course.teacherName || '未知教师' }}</p>
                </div>
              </router-link>
            </div>
          </section>
        </main>
      </div>
    </template>

    <template v-else>
      <EduNavbar />
      <main class="main-content education-theme">
        <div class="hero-section">
          <h1 class="page-title">欢迎使用学习辅助系统</h1>
          <p class="subtitle">这是一个帮助学生和教师高效教学的平台</p>

          <div class="auth-buttons">
            <router-link to="/login" class="auth-btn unified-btn secondary">
              <span class="btn-icon">🔑</span>
              <span class="btn-text">立即登录</span>
            </router-link>
            <router-link to="/register" class="auth-btn unified-btn">
              <span class="btn-icon">🎓</span>
              <span class="btn-text">立即注册</span>
            </router-link>
          </div>

          <div class="features-grid">
            <div class="feature-card edu-card">
              <div class="feature-icon">📚</div>
              <h3>课程管理</h3>
              <p>轻松管理你的所有课程，设置上课时间和教师信息</p>
            </div>
            <div class="feature-card edu-card">
              <div class="feature-icon">📁</div>
              <h3>资料中心</h3>
              <p>上传下载学习资料，与同学分享优质资源</p>
            </div>
            <div class="feature-card edu-card">
              <div class="feature-icon">📊</div>
              <h3>学习统计</h3>
              <p>查看学习数据统计，了解学习进度和效果</p>
            </div>
            <div class="feature-card edu-card">
              <div class="feature-icon">💬</div>
              <h3>讨论交流</h3>
              <p>与同学和教师进行讨论，解决学习问题</p>
            </div>
          </div>
        </div>
      </main>
    </template>
  </div>
</template>

<script setup>
import EduNavbar from '../components/EduNavbar.vue'
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { courseApi } from '../api/course'

const router = useRouter()
const userStore = useUserStore()

const searchText = ref('')
const unreadCount = ref(0)
const recentCourses = ref([])
const studyStats = ref({
  totalStudyTime: 0
})

const userDisplayName = computed(() => userStore.user?.nickname || userStore.user?.username || '')
const avatarUrl = computed(() => userStore.user?.avatar || '')
const avatarFallback = computed(() => (userDisplayName.value || 'U').slice(0, 1).toUpperCase())
const todayTodoText = computed(() => '欢迎回来，开始今天的学习吧')

const submitSearch = () => {
  const q = searchText.value.trim()
  router.push(q ? `/materials?keyword=${encodeURIComponent(q)}` : '/materials')
}

const logout = () => {
  userStore.logout()
  router.push('/login')
}

const loadRecentCourses = async () => {
  if (!userStore.user?.id) return
  try {
    const response = await courseApi.getUserCourses(userStore.user.id)
    if (response.data.code === 200) {
      const courses = response.data.data
      recentCourses.value = Array.isArray(courses) ? courses.slice(0, 3) : []
    }
  } catch (e) {
    recentCourses.value = []
  }
}

const loadStudyStats = async () => {
  if (!userStore.user?.id) return
  try {
    studyStats.value = {
      totalStudyTime: 120
    }
  } catch (e) {
    studyStats.value = {
      totalStudyTime: 0
    }
  }
}

onMounted(() => {
  loadRecentCourses()
  loadStudyStats()
})
</script>

<style scoped>
.home {
  min-height: 100vh;
}

/* 左右布局样式 */
.student-layout {
  display: flex;
  min-height: 100vh;
  background: #f7f9fc;
}

/* 左侧导航栏 */
.student-sidebar {
  width: 280px;
  background: #ffffff;
  border-right: 1px solid #eef2f7;
  display: flex;
  flex-direction: column;
  position: fixed;
  left: 0;
  top: 0;
  bottom: 0;
  z-index: 100;
  box-shadow: 0 0 20px rgba(0, 0, 0, 0.05);
}

.sidebar-header {
  padding: 1.5rem 1.25rem;
  border-bottom: 1px solid #eef2f7;
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.logo-container {
  width: 100%;
}

.logo-link {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  text-decoration: none;
  color: inherit;
}

.logo-icon {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  background: linear-gradient(135deg, #2563eb, #60a5fa);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  flex: 0 0 auto;
}

.logo-text {
  font-weight: 800;
  color: #111827;
  font-size: 1.1rem;
  flex: 1;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 1rem;
  width: 100%;
}

.avatar-btn {
  width: 48px;
  height: 48px;
  border-radius: 999px;
  overflow: hidden;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  text-decoration: none;
  background: #f3f4f6;
  flex: 0 0 auto;
}

.avatar {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-fallback {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 800;
  color: #111827;
  font-size: 1.25rem;
}

.welcome-sub {
  color: #6b7280;
  font-size: 0.9rem;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex: 1;
}

.sidebar-nav {
  flex: 1;
  padding: 1rem 0;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.75rem 1.25rem;
  text-decoration: none;
  color: #4b5563;
  border: 0;
  background: transparent;
  cursor: pointer;
  transition: all 0.2s ease;
  width: 100%;
  text-align: left;
}

.nav-item:hover {
  background: #f3f4f6;
  color: #111827;
}

.nav-item.active {
  background: #eef2ff;
  color: #2563eb;
  font-weight: 700;
}

.nav-icon {
  font-size: 1.1rem;
  flex: 0 0 auto;
}

.nav-text {
  font-size: 0.95rem;
  font-weight: 500;
}

.sidebar-footer {
  border-top: 1px solid #eef2f7;
  padding: 1rem 0;
}

.nav-item.logout {
  color: #ef4444;
}

.nav-item.logout:hover {
  background: #fef2f2;
  color: #dc2626;
}

/* 右侧内容区 */
.student-content {
  flex: 1;
  margin-left: 280px;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.content-header {
  background: #ffffff;
  border-bottom: 1px solid #eef2f7;
  padding: 1rem 1.5rem;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
  position: sticky;
  top: 0;
  z-index: 10;
}

.content-header .page-title {
  font-size: 1.5rem;
  font-weight: 800;
  color: #111827;
  margin: 0;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.icon-btn {
  width: 44px;
  height: 44px;
  border-radius: 10px;
  background: #f3f4f6;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  position: relative;
  text-decoration: none;
  color: inherit;
  transition: all 0.2s ease;
}

.icon-btn:hover {
  background: #e5e7eb;
}

.icon {
  font-size: 18px;
}

.badge {
  position: absolute;
  top: -6px;
  right: -6px;
  min-width: 18px;
  height: 18px;
  padding: 0 6px;
  border-radius: 999px;
  background: #ef4444;
  color: #fff;
  font-size: 12px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
}

.search {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.search-input {
  width: min(42vw, 340px);
  height: 44px;
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  padding: 0 0.75rem;
  outline: none;
  background: #fff;
  transition: all 0.2s ease;
}

.search-input:focus {
  border-color: #2563eb;
  box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.1);
}

.search-btn {
  height: 44px;
  padding: 0 1rem;
  border: 0;
  border-radius: 10px;
  background: #2563eb;
  color: #fff;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.2s ease;
}

.search-btn:hover {
  background: #1d4ed8;
}

/* 核心内容区域 */
.student-content > section {
  flex: 1;
  padding: 1.5rem;
}

.core {
  max-width: 1200px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 1rem;
}

.core-card {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 1.25rem;
  background: #ffffff;
  border-radius: 14px;
  text-decoration: none;
  color: inherit;
  box-shadow: 0 6px 18px rgba(15, 23, 42, 0.06);
  border: 1px solid #eef2f7;
  transition: all 0.3s ease;
}

.core-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 25px rgba(15, 23, 42, 0.1);
  border-color: #2563eb;
}

.core-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #eef2ff;
  font-size: 20px;
  flex: 0 0 auto;
}

.core-info {
  min-width: 0;
}

.core-title {
  font-weight: 800;
  color: #111827;
  margin-bottom: 2px;
}

.core-sub {
  color: #6b7280;
  font-size: 0.9rem;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 非学生端样式 */
.main-content {
  min-height: calc(100vh - var(--navbar-height));
  display: flex;
  align-items: center;
  justify-content: center;
  padding: var(--spacing-xl) var(--spacing-lg);
}


.hero-section {
  text-align: center;
  max-width: 800px;
  width: 100%;
}

.hero-section .page-title {
  font-size: var(--font-size-xxxl);
  font-weight: var(--font-weight-bold);
  color: var(--gray-900);
  margin-bottom: var(--spacing-md);
  background: linear-gradient(135deg, var(--primary-color), var(--primary-light));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.subtitle {
  font-size: var(--font-size-lg);
  color: var(--gray-700);
  margin-bottom: var(--spacing-xxl);
  line-height: var(--line-height-relaxed);
}

/* 注册入口按钮 */
.auth-buttons {
  display: flex;
  gap: var(--spacing-lg);
  justify-content: center;
  margin-bottom: var(--spacing-xxl);
  flex-wrap: wrap;
}

.auth-btn {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  padding: var(--spacing-md) var(--spacing-xl);
  border-radius: var(--border-radius-lg);
  text-decoration: none;
  font-weight: var(--font-weight-semibold);
  font-size: var(--font-size-lg);
  transition: all 0.3s ease;
  box-shadow: var(--shadow-md);
  background: linear-gradient(135deg, var(--primary-color), var(--primary-light));
  color: white;
}

.auth-btn.secondary {
  background: white;
  color: var(--primary-color);
  border: 2px solid var(--primary-color);
}

.auth-btn.secondary:hover {
  background: var(--primary-color);
  color: white;
}

.auth-btn:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-lg);
}

.btn-icon {
  font-size: 24px;
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 1.5rem;
  margin-top: 4rem;
  max-width: 1000px;
  margin-left: auto;
  margin-right: auto;
}

.feature-card {
  text-align: center;
  padding: 2rem;
  transition: all 0.3s ease;
  border: 1px solid var(--gray-200);
  border-radius: var(--border-radius-lg);
  background: white;
  box-shadow: var(--shadow-sm);
}

.feature-card:hover {
  transform: translateY(-8px);
  box-shadow: var(--shadow-lg);
  border-color: var(--primary-color);
}

.feature-icon {
  font-size: 48px;
  margin-bottom: var(--spacing-md);
}

.feature-card h3 {
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-semibold);
  color: var(--gray-900);
  margin-bottom: var(--spacing-sm);
}

.feature-card p {
  color: var(--gray-700);
  line-height: var(--line-height-relaxed);
  font-size: var(--font-size-sm);
}

/* 学习统计部分 */
.stats-section {
  margin-top: 2rem;
  padding: 1.5rem;
  background: #ffffff;
  border-radius: 14px;
  box-shadow: 0 6px 18px rgba(15, 23, 42, 0.06);
  border: 1px solid #eef2f7;
}

.section-title {
  font-size: 1.25rem;
  font-weight: 800;
  color: #111827;
  margin-bottom: 1.25rem;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 1rem;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 1rem;
  background: #f9fafb;
  border-radius: 10px;
  border: 1px solid #e5e7eb;
  transition: all 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(15, 23, 42, 0.1);
  border-color: #2563eb;
}

.stat-icon {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #eef2ff;
  font-size: 18px;
  flex: 0 0 auto;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-weight: 800;
  color: #111827;
  font-size: 1.25rem;
  margin-bottom: 2px;
}

.stat-label {
  color: #6b7280;
  font-size: 0.875rem;
}

/* 最近课程部分 */
.courses-section {
  margin-top: 2rem;
  padding: 1.5rem;
  background: #ffffff;
  border-radius: 14px;
  box-shadow: 0 6px 18px rgba(15, 23, 42, 0.06);
  border: 1px solid #eef2f7;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.25rem;
}

.section-link {
  color: #2563eb;
  font-weight: 700;
  text-decoration: none;
  font-size: 0.875rem;
  transition: color 0.2s ease;
}

.section-link:hover {
  color: #1d4ed8;
  text-decoration: underline;
}

.courses-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 1rem;
}

.empty-state {
  grid-column: 1 / -1;
  text-align: center;
  padding: 3rem 1rem;
  background: #f9fafb;
  border-radius: 10px;
  border: 2px dashed #e5e7eb;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 1rem;
}

.empty-state p {
  color: #6b7280;
  margin-bottom: 1.5rem;
}

.btn-primary {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem 1.5rem;
  border-radius: 8px;
  background: #2563eb;
  color: white;
  font-weight: 700;
  text-decoration: none;
  transition: all 0.2s ease;
}

.btn-primary:hover {
  background: #1d4ed8;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(37, 99, 235, 0.3);
}

.course-card {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 1rem;
  background: #f9fafb;
  border-radius: 10px;
  border: 1px solid #e5e7eb;
  text-decoration: none;
  color: inherit;
  transition: all 0.3s ease;
}

.course-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(15, 23, 42, 0.1);
  border-color: #2563eb;
}

.course-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #eef2ff;
  font-size: 20px;
  font-weight: 800;
  color: #2563eb;
  flex: 0 0 auto;
}

.course-info {
  flex: 1;
  min-width: 0;
}

.course-name {
  font-weight: 700;
  color: #111827;
  margin-bottom: 2px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.course-teacher {
  color: #6b7280;
  font-size: 0.875rem;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .core {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
  
  .stats-grid {
    grid-template-columns: 1fr;
  }
  
  .courses-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .student-sidebar {
    width: 240px;
  }
  
  .student-content {
    margin-left: 240px;
  }
  
  .core {
    grid-template-columns: 1fr;
  }

  .search-input {
    width: 44vw;
  }

  .main-content {
    padding: var(--spacing-lg) var(--spacing-md);
  }
  
  .hero-section .page-title {
    font-size: var(--font-size-xxl);
  }
  
  .subtitle {
    font-size: var(--font-size-base);
  }
  
  .features-grid {
    grid-template-columns: 1fr;
    gap: var(--spacing-md);
  }
  
  .feature-card {
    padding: var(--spacing-md);
  }
}

@media (max-width: 640px) {
  .student-sidebar {
    width: 100%;
    transform: translateX(-100%);
    transition: transform 0.3s ease;
  }
  
  .student-sidebar.open {
    transform: translateX(0);
  }
  
  .student-content {
    margin-left: 0;
  }
}

@media (max-width: 480px) {
  .main-content {
    padding: var(--spacing-md) var(--spacing-sm);
  }
  
  .hero-section .page-title {
    font-size: var(--font-size-xl);
  }
  
  .feature-icon {
    font-size: 36px;
  }
}
</style>
