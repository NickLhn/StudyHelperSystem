<template>
  <div class="student-container">
    <div class="student-layout">
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
          <router-link to="/student/courses" class="nav-item">
            <span class="nav-icon">📚</span>
            <span class="nav-text">课程学习</span>
          </router-link>
          <router-link to="/student/materials" class="nav-item">
            <span class="nav-icon">📁</span>
            <span class="nav-text">资料中心</span>
          </router-link>
          <router-link to="/student/statistics" class="nav-item">
            <span class="nav-icon">📊</span>
            <span class="nav-text">学习统计</span>
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
          <button class="nav-item logout" type="button" @click="handleLogout">
            <span class="nav-icon">⏻</span>
            <span class="nav-text">退出</span>
          </button>
        </div>
      </aside>

      <main class="student-content">
        <header class="content-header">
          <h1 class="page-title">{{ pageTitle }}</h1>
          <div class="header-actions">
            <router-link to="/messages" class="icon-btn" aria-label="消息中心">
              <span class="icon">💬</span>
              <span v-if="unreadCount > 0" class="badge">{{ unreadCount }}</span>
            </router-link>
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
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const unreadCount = ref(0)

const userDisplayName = computed(() => userStore.user?.nickname || userStore.user?.username || '')
const avatarUrl = computed(() => userStore.user?.avatar || '')
const avatarFallback = computed(() => (userDisplayName.value || 'U').slice(0, 1).toUpperCase())

const pageTitle = computed(() => {
  const path = route.path
  if (path.includes('/student/courses')) return '课程学习'
  if (path.includes('/student/materials')) return '资料中心'
  if (path.includes('/student/statistics')) return '学习统计'
  return '学生端'
})

const handleLogout = () => {
  userStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.student-container {
  min-height: 100vh;
  background-color: #f7f9fc;
}

.student-layout {
  display: flex;
  min-height: 100vh;
  background: #f7f9fc;
}

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
  background: #f3f4f6;
  border: 1px solid #e5e7eb;
  text-decoration: none;
  flex: 0 0 auto;
}

.avatar {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-fallback {
  font-weight: 900;
  color: #111827;
}

.welcome-sub {
  color: #6b7280;
  font-weight: 800;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.sidebar-nav {
  padding: 1rem 0.75rem;
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
  flex: 1;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.75rem 0.9rem;
  border-radius: 12px;
  text-decoration: none;
  color: #111827;
  font-weight: 800;
  transition: background 0.15s ease;
}

.nav-item:hover {
  background: rgba(37, 99, 235, 0.08);
}

.nav-item.router-link-active {
  background: rgba(37, 99, 235, 0.14);
  color: #1d4ed8;
}

.nav-icon {
  width: 26px;
  display: inline-flex;
  justify-content: center;
}

.sidebar-footer {
  padding: 0.75rem;
  border-top: 1px solid #eef2f7;
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.nav-item.logout {
  background: none;
  border: none;
  cursor: pointer;
  text-align: left;
}

.student-content {
  flex: 1;
  margin-left: 280px;
  min-width: 0;
  display: flex;
  flex-direction: column;
}

.content-header {
  height: 72px;
  padding: 0 1.25rem;
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: rgba(255, 255, 255, 0.75);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid #eef2f7;
  position: sticky;
  top: 0;
  z-index: 50;
}

.page-title {
  margin: 0;
  font-size: 1.15rem;
  font-weight: 900;
  color: #111827;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.icon-btn {
  width: 40px;
  height: 40px;
  border-radius: 12px;
  border: 1px solid #e5e7eb;
  background: #fff;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  text-decoration: none;
  position: relative;
}

.badge {
  position: absolute;
  top: -6px;
  right: -6px;
  background: #ef4444;
  color: #fff;
  font-size: 12px;
  font-weight: 900;
  line-height: 18px;
  min-width: 18px;
  height: 18px;
  padding: 0 6px;
  border-radius: 999px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.content-body {
  padding: 1.25rem;
}

@media (max-width: 900px) {
  .student-sidebar {
    position: static;
    width: 100%;
    height: auto;
    flex-direction: row;
  }
  .student-content {
    margin-left: 0;
  }
  .sidebar-nav {
    flex-direction: row;
    overflow-x: auto;
  }
  .sidebar-footer {
    display: none;
  }
}
</style>
