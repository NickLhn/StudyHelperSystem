<template>
  <div class="teacher-container">
    <div class="teacher-layout">
      <!-- 左侧导航栏 -->
      <aside class="teacher-sidebar">
        <div class="sidebar-header">
          <div class="logo-container">
            <router-link to="/teacher/dashboard" class="logo-link">
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
          <router-link to="/teacher/dashboard" class="nav-item">
            <span class="nav-icon">📊</span>
            <span class="nav-text">仪表盘</span>
          </router-link>
          <router-link to="/teacher/courses" class="nav-item">
            <span class="nav-icon">📚</span>
            <span class="nav-text">课程管理</span>
          </router-link>
          <router-link to="/teacher/tasks" class="nav-item">
            <span class="nav-icon">✅</span>
            <span class="nav-text">作业管理</span>
          </router-link>
          <router-link to="/teacher/quizzes" class="nav-item">
            <span class="nav-icon">📝</span>
            <span class="nav-text">测验管理</span>
          </router-link>
          <router-link to="/teacher/materials" class="nav-item">
            <span class="nav-icon">📁</span>
            <span class="nav-text">资料管理</span>
          </router-link>
        </nav>
        
        <div class="sidebar-footer">
          <router-link to="/settings" class="nav-item">
            <span class="nav-icon">⚙️</span>
            <span class="nav-text">设置</span>
          </router-link>
          <button class="nav-item logout" type="button" @click="handleLogout">
            <span class="nav-icon">⏻</span>
            <span class="nav-text">退出</span>
          </button>
        </div>
      </aside>
      
      <!-- 右侧内容区 -->
      <main class="teacher-content">
        <header class="content-header">
          <h1 class="page-title">{{ pageTitle }}</h1>
          <div class="header-actions">
            <router-link to="/messages" class="icon-btn" aria-label="消息中心">
              <span class="icon">💬</span>
              <span v-if="unreadCount > 0" class="badge">{{ unreadCount }}</span>
            </router-link>
            
            <div class="create-wrap">
              <button class="icon-btn" type="button" aria-label="快捷创建" @click="toggleCreate">
                <span class="icon">＋</span>
              </button>
              <div v-if="createOpen" class="create-panel">
                <router-link class="create-item" to="/course/create" @click="closeCreate">创建课程</router-link>
                <router-link class="create-item" to="/task/create" @click="closeCreate">创建任务</router-link>
                <router-link class="create-item" to="/quiz/create" @click="closeCreate">创建测验</router-link>
                <router-link class="create-item" to="/material/upload" @click="closeCreate">上传资料</router-link>
              </div>
            </div>
          </div>
        </header>
        
        <div class="content-body">
          <router-view />
        </div>
      </main>
    </div>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '../stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const unreadCount = ref(0)
const createOpen = ref(false)

const userDisplayName = computed(() => userStore.user?.nickname || userStore.user?.username || '')
const avatarUrl = computed(() => userStore.user?.avatar || '')
const avatarFallback = computed(() => (userDisplayName.value || 'U').slice(0, 1).toUpperCase())

const pageTitle = computed(() => {
  const path = route.path
  if (path.includes('/teacher/dashboard')) return '仪表盘'
  if (path.includes('/teacher/courses')) return '课程管理'
  if (path.includes('/teacher/tasks')) return '作业管理'
  if (path.includes('/teacher/quizzes')) return '测验管理'
  if (path.includes('/teacher/materials')) return '资料管理'
  return '教师端'
})

const toggleCreate = () => {
  createOpen.value = !createOpen.value
}

const closeCreate = () => {
  createOpen.value = false
}

const handleLogout = () => {
  userStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.teacher-container {
  min-height: 100vh;
  background-color: #f7f9fc;
}

/* 左右布局样式 */
.teacher-layout {
  display: flex;
  min-height: 100vh;
  background: #f7f9fc;
}

/* 左侧导航栏 */
.teacher-sidebar {
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
.teacher-content {
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
  border: 0;
  cursor: pointer;
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

.create-wrap {
  position: relative;
}

.create-panel {
  position: absolute;
  right: 0;
  top: 48px;
  min-width: 180px;
  background: #ffffff;
  border: 1px solid #eef2f7;
  border-radius: 12px;
  box-shadow: 0 12px 30px rgba(15, 23, 42, 0.12);
  padding: 0.5rem;
  display: grid;
  gap: 0.25rem;
  z-index: 1000;
}

.create-item {
  padding: 0.6rem 0.75rem;
  border-radius: 10px;
  text-decoration: none;
  color: #111827;
  font-weight: 700;
  transition: all 0.2s ease;
}

.create-item:hover {
  background: #f3f4f6;
}

.content-body {
  flex: 1;
  padding: 1.5rem;
  max-width: 1200px;
  margin: 0 auto;
  width: 100%;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .content-body {
    padding: 1.25rem;
  }
}

@media (max-width: 768px) {
  .teacher-sidebar {
    width: 240px;
  }
  
  .teacher-content {
    margin-left: 240px;
  }
  
  .content-body {
    padding: 1rem;
  }
}

@media (max-width: 640px) {
  .teacher-sidebar {
    width: 100%;
    transform: translateX(-100%);
    transition: transform 0.3s ease;
  }
  
  .teacher-sidebar.open {
    transform: translateX(0);
  }
  
  .teacher-content {
    margin-left: 0;
  }
}
</style>
