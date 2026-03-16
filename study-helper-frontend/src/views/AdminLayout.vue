<template>
  <div class="admin-container">
    <div class="admin-layout">
      <!-- 左侧导航栏 -->
      <aside class="admin-sidebar">
        <div class="sidebar-header">
          <div class="logo-container">
            <router-link to="/admin/dashboard" class="logo-link">
              <div class="logo-icon">🎓</div>
              <div class="logo-text">学习辅助系统</div>
            </router-link>
          </div>
        </div>
        
        <nav class="sidebar-nav">
          <router-link to="/admin/dashboard" class="nav-item">
            <span class="nav-icon">📊</span>
            <span class="nav-text">仪表盘</span>
          </router-link>
          <router-link to="/admin/users" class="nav-item">
            <span class="nav-icon">👥</span>
            <span class="nav-text">用户管理</span>
          </router-link>
          <router-link to="/admin/content" class="nav-item">
            <span class="nav-icon">📚</span>
            <span class="nav-text">内容概览</span>
          </router-link>
          <router-link to="/admin/invitations" class="nav-item">
            <span class="nav-icon">🔑</span>
            <span class="nav-text">邀请码</span>
          </router-link>
        </nav>
        
        <div class="sidebar-footer">
          <button class="nav-item logout" type="button" @click="handleLogout">
            <span class="nav-icon">🚪</span>
            <span class="nav-text">退出</span>
          </button>
        </div>
      </aside>
      
      <!-- 右侧内容区 -->
      <main class="admin-content">
        <header class="content-header">
          <h1 class="page-title">{{ pageTitle }}</h1>
        </header>
        
        <div class="content-body">
          <router-view />
        </div>
      </main>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '../stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const pageTitle = computed(() => {
  const path = route.path
  if (path.includes('/admin/dashboard')) return '仪表盘'
  if (path.includes('/admin/users')) return '用户管理'
  if (path.includes('/admin/content')) return '内容概览'
  if (path.includes('/admin/invitations')) return '邀请码管理'
  return '管理员控制台'
})

const handleLogout = () => {
  userStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.admin-container {
  min-height: 100vh;
  background-color: #f5f7fa;
}

/* 左右布局样式 */
.admin-layout {
  display: flex;
  min-height: 100vh;
  background: #f5f7fa;
}

/* 左侧导航栏 */
.admin-sidebar {
  width: 280px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  display: flex;
  flex-direction: column;
  position: fixed;
  left: 0;
  top: 0;
  bottom: 0;
  z-index: 100;
  box-shadow: 0 0 20px rgba(0, 0, 0, 0.1);
}

.sidebar-header {
  padding: 1.5rem 1.25rem;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.logo-container {
  width: 100%;
}

.logo-link {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  text-decoration: none;
  color: white;
}

.logo-icon {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  flex: 0 0 auto;
  backdrop-filter: blur(10px);
}

.logo-text {
  font-weight: 800;
  color: white;
  font-size: 1.1rem;
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
  color: rgba(255, 255, 255, 0.9);
  border: 0;
  background: transparent;
  cursor: pointer;
  transition: all 0.2s ease;
  width: 100%;
  text-align: left;
}

.nav-item:hover {
  background: rgba(255, 255, 255, 0.1);
  color: white;
}

.nav-item.active {
  background: rgba(255, 255, 255, 0.2);
  color: white;
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
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  padding: 1rem 0;
}

.nav-item.logout {
  color: rgba(255, 255, 255, 0.9);
}

.nav-item.logout:hover {
  background: rgba(255, 255, 255, 0.1);
  color: white;
}

/* 右侧内容区 */
.admin-content {
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

.content-body {
  flex: 1;
  padding: 2rem;
  max-width: 1400px;
  margin: 0 auto;
  width: 100%;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .content-body {
    padding: 1.5rem;
  }
}

@media (max-width: 768px) {
  .admin-sidebar {
    width: 240px;
  }
  
  .admin-content {
    margin-left: 240px;
  }
  
  .content-body {
    padding: 1.25rem;
  }
}

@media (max-width: 640px) {
  .admin-sidebar {
    width: 100%;
    transform: translateX(-100%);
    transition: transform 0.3s ease;
  }
  
  .admin-sidebar.open {
    transform: translateX(0);
  }
  
  .admin-content {
    margin-left: 0;
  }
}
</style>