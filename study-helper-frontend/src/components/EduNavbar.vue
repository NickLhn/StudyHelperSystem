<template>
  <nav class="edu-navbar">
    <div class="navbar-container">
      <!-- 左侧品牌区域 -->
      <div class="nav-brand">
        <div class="brand-logo">
          <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20"></path>
            <path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z"></path>
          </svg>
        </div>
        <span class="brand-text">学习辅助系统</span>
      </div>

      <!-- 右侧导航链接 -->
      <div class="nav-menu">
        <template v-if="userStore.isLoggedIn">
          <span class="user-welcome">
            <span class="welcome-text">欢迎,</span>
            <span class="user-name">{{ userStore.user?.nickname || userStore.user?.username }}</span>
          </span>
          
          <router-link 
            v-for="item in currentNavItems" 
            :key="item.path"
            :to="item.path" 
            class="nav-link"
            active-class="active"
          >
            <span class="link-icon">{{ item.icon }}</span>
            <span class="link-text">{{ item.name }}</span>
          </router-link>
          
          <button @click="handleLogout" class="edu-btn edu-btn-secondary logout-btn">
            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"></path>
              <polyline points="16 17 21 12 16 7"></polyline>
              <line x1="21" y1="12" x2="9" y2="12"></line>
            </svg>
            退出
          </button>
        </template>
        
        <template v-else>
          <!-- 已删除顶部登录注册按钮 -->
        </template>
      </div>

      <!-- 移动端菜单按钮 -->
      <button 
        class="mobile-menu-toggle"
        @click="toggleMobileMenu"
        :aria-expanded="isMobileMenuOpen"
      >
        <svg v-if="!isMobileMenuOpen" xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <line x1="3" y1="12" x2="21" y2="12"></line>
          <line x1="3" y1="6" x2="21" y2="6"></line>
          <line x1="3" y1="18" x2="21" y2="18"></line>
        </svg>
        <svg v-else xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <line x1="18" y1="6" x2="6" y2="18"></line>
          <line x1="6" y1="6" x2="18" y2="18"></line>
        </svg>
      </button>
    </div>

    <!-- 移动端菜单 -->
    <div v-show="isMobileMenuOpen" class="mobile-menu">
      <template v-if="userStore.isLoggedIn">
        <div class="mobile-user-info">
          <span class="welcome-text">欢迎,</span>
          <span class="user-name">{{ userStore.user?.nickname || userStore.user?.username }}</span>
        </div>
        
        <router-link 
          v-for="item in currentNavItems" 
          :key="item.path"
          :to="item.path" 
          class="mobile-nav-link"
          active-class="active"
          @click="closeMobileMenu"
        >
          <span class="link-icon">{{ item.icon }}</span>
          <span class="link-text">{{ item.name }}</span>
        </router-link>
        
        <button @click="handleLogout" class="mobile-logout-btn">
          <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"></path>
            <polyline points="16 17 21 12 16 7"></polyline>
            <line x1="21" y1="12" x2="9" y2="12"></line>
          </svg>
          退出
        </button>
      </template>
      
      <template v-else>
        <!-- 已删除移动端登录注册按钮 -->
      </template>
    </div>
  </nav>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useUserStore } from '../stores/user'
import { useRouter } from 'vue-router'

const userStore = useUserStore()
const router = useRouter()

const isMobileMenuOpen = ref(false)

const navItems = [
  { name: '课程管理', path: '/courses', icon: '📚' },
  { name: '学习计划', path: '/tasks', icon: '📅' },
  { name: '资料中心', path: '/materials', icon: '📁' },
  { name: '在线测验', path: '/quizzes', icon: '📝' },
  { name: '学习统计', path: '/statistics', icon: '📊' },
  { name: '个人中心', path: '/profile', icon: '👤' }
]

// 教师专用菜单项
const teacherNavItems = [
  { name: '课程管理', path: '/courses', icon: '📚' },
  { name: '创建测验', path: '/quiz/create', icon: '📝' },
  { name: '资料中心', path: '/materials', icon: '📁' },
  { name: '学生管理', path: '/students', icon: '👥' },
  { name: '教学统计', path: '/teaching-statistics', icon: '📊' },
  { name: '个人中心', path: '/profile', icon: '👤' }
]

// 管理员专用菜单项
const adminNavItems = [
  { name: '仪表盘', path: '/admin/dashboard', icon: '📊' },
  { name: '用户管理', path: '/admin/users', icon: '👥' },
  { name: '内容概览', path: '/admin/content', icon: '📚' },
  { name: '邀请码管理', path: '/admin/invitations', icon: '🔑' },
  { name: '系统统计', path: '/statistics', icon: '📈' },
  { name: '个人中心', path: '/profile', icon: '👤' }
]

const currentNavItems = computed(() => {
  if (userStore.isAdmin) {
    return adminNavItems
  } else if (userStore.isTeacher) {
    return teacherNavItems
  } else {
    return navItems
  }
})

const toggleMobileMenu = () => {
  isMobileMenuOpen.value = !isMobileMenuOpen.value
}

const closeMobileMenu = () => {
  isMobileMenuOpen.value = false
}

const handleLogout = () => {
  userStore.logout()
  closeMobileMenu()
  router.push('/login')
}
</script>

<style scoped>
.edu-navbar {
  position: sticky;
  top: 0;
  z-index: 100;
  background: linear-gradient(135deg, #E6F7FF 0%, #F0F9FF 100%);
  border-bottom: 1px solid var(--gray-200);
  box-shadow: var(--shadow-sm);
}

.navbar-container {
  max-width: var(--card-max-width);
  margin: 0 auto;
  padding: 0 var(--spacing-lg);
  height: var(--navbar-height);
  display: flex;
  align-items: center;
  justify-content: space-between;
}

/* 品牌区域 */
.nav-brand {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
}

.brand-logo {
  color: var(--primary-color);
}

.brand-text {
  font-family: var(--font-family-heading);
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-bold);
  color: var(--gray-900);
}

/* 导航菜单 */
.nav-menu {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
}

.user-welcome {
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
  margin-right: var(--spacing-md);
  color: var(--gray-700);
  font-size: var(--font-size-sm);
}

.user-name {
  font-weight: var(--font-weight-medium);
  color: var(--primary-color);
}

.nav-link {
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
  padding: var(--spacing-sm) var(--spacing-md);
  border-radius: var(--border-radius-md);
  color: var(--gray-700);
  text-decoration: none;
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-medium);
  transition: all 0.2s ease;
  border: 1px solid transparent;
}

.nav-link:hover {
  background-color: rgba(74, 144, 226, 0.1);
  color: var(--primary-color);
  transform: translateY(-1px);
}

.nav-link.active {
  background-color: var(--primary-color);
  color: var(--white);
  border-color: var(--primary-color);
}

.link-icon {
  font-size: 16px;
}

.logout-btn {
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
  margin-left: var(--spacing-md);
}

/* 移动端菜单按钮 */
.mobile-menu-toggle {
  display: none;
  background: none;
  border: none;
  color: var(--gray-700);
  cursor: pointer;
  padding: var(--spacing-sm);
  border-radius: var(--border-radius-md);
  transition: all 0.2s ease;
}

.mobile-menu-toggle:hover {
  background-color: rgba(74, 144, 226, 0.1);
  color: var(--primary-color);
}

/* 移动端菜单 */
.mobile-menu {
  display: none;
  background: var(--bg-secondary);
  border-top: 1px solid var(--gray-200);
  padding: var(--spacing-md);
  box-shadow: var(--shadow-md);
}

.mobile-user-info {
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
  padding: var(--spacing-md);
  margin-bottom: var(--spacing-md);
  background-color: var(--bg-accent);
  border-radius: var(--border-radius-md);
  font-size: var(--font-size-sm);
}

.mobile-nav-link {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  padding: var(--spacing-md);
  margin-bottom: var(--spacing-xs);
  border-radius: var(--border-radius-md);
  color: var(--gray-700);
  text-decoration: none;
  font-size: var(--font-size-base);
  transition: all 0.2s ease;
}

.mobile-nav-link:hover {
  background-color: rgba(74, 144, 226, 0.1);
  color: var(--primary-color);
}

.mobile-nav-link.active {
  background-color: var(--primary-color);
  color: var(--white);
}

.mobile-logout-btn {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--spacing-xs);
  padding: var(--spacing-md);
  margin-top: var(--spacing-md);
  background-color: var(--danger-color);
  color: var(--white);
  border: none;
  border-radius: var(--border-radius-md);
  font-size: var(--font-size-base);
  font-weight: var(--font-weight-medium);
  cursor: pointer;
  transition: all 0.2s ease;
}

.mobile-logout-btn:hover {
  background-color: #ff5252;
  transform: translateY(-1px);
}

.mobile-register {
  background-color: var(--primary-color);
  color: var(--white);
}

.mobile-register:hover {
  background-color: var(--primary-dark);
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .nav-link .link-text {
    display: none;
  }
  
  .nav-link {
    padding: var(--spacing-sm);
  }
}

@media (max-width: 768px) {
  .navbar-container {
    padding: 0 var(--spacing-md);
  }
  
  .nav-menu {
    display: none;
  }
  
  .mobile-menu-toggle {
    display: block;
  }
  
  .mobile-menu {
    display: block;
  }
}

@media (max-width: 480px) {
  .navbar-container {
    padding: 0 var(--spacing-sm);
    height: 56px;
  }
  
  .brand-text {
    font-size: var(--font-size-base);
  }
  
  .mobile-nav-link,
  .mobile-logout-btn {
    padding: var(--spacing-sm);
    font-size: var(--font-size-sm);
  }
}
</style>