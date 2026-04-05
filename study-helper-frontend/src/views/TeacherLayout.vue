<template>
  <div class="app-shell">
    <button
      class="edu-btn edu-btn-secondary shell-menu-btn"
      type="button"
      @click="sidebarOpen = true"
    >
      菜单
    </button>

    <div v-if="sidebarOpen" class="shell-overlay" @click="sidebarOpen = false"></div>

    <aside class="app-sidebar" :class="{ open: sidebarOpen }">
      <router-link to="/teacher/dashboard" class="app-brand">
        <span class="app-brand-mark">TH</span>
        <span class="app-brand-copy">
          <span class="app-brand-title">学习辅助系统</span>
          <span class="app-brand-subtitle">Teacher Studio</span>
        </span>
      </router-link>

      <div class="app-user-card">
        <router-link to="/profile" class="app-avatar" aria-label="个人中心">
          <img v-if="avatarUrl" :src="avatarUrl" alt="avatar" />
          <span v-else>{{ avatarFallback }}</span>
        </router-link>
        <div class="app-user-copy">
          <div class="app-user-name">{{ userDisplayName }}</div>
          <div class="app-user-role">教师工作台</div>
        </div>
      </div>

      <nav class="app-nav">
        <router-link
          v-for="item in navItems"
          :key="item.path"
          :to="item.path"
          class="app-nav-link"
          :class="{ active: isActive(item) }"
        >
          <span class="app-nav-badge">{{ item.badge }}</span>
          <span class="app-nav-copy">
            <span class="app-nav-title">{{ item.label }}</span>
            <span class="app-nav-subtitle">{{ item.subtitle }}</span>
          </span>
        </router-link>
      </nav>

      <div class="app-sidebar-footer">
        <router-link to="/settings" class="app-nav-link" :class="{ active: route.path === '/settings' }">
          <span class="app-nav-badge">ST</span>
          <span class="app-nav-copy">
            <span class="app-nav-title">系统设置</span>
            <span class="app-nav-subtitle">账户与通知</span>
          </span>
        </router-link>
        <button type="button" class="app-nav-link" @click="handleLogout">
          <span class="app-nav-badge">EX</span>
          <span class="app-nav-copy">
            <span class="app-nav-title">退出登录</span>
            <span class="app-nav-subtitle">安全结束当前会话</span>
          </span>
        </button>
      </div>
    </aside>

    <main class="app-main">
      <header class="app-topbar">
        <div class="app-topbar-meta">
          <h1 class="app-topbar-title">{{ pageMeta.title }}</h1>
          <p class="app-topbar-copy">{{ pageMeta.subtitle }}</p>
        </div>

        <div class="app-topbar-actions">
          <span class="chip">教师端</span>
          <router-link to="/messages" class="edu-btn edu-btn-secondary message-link">
            <span>消息中心</span>
            <span v-if="notificationCount > 0" class="message-count">{{ notificationCount }}</span>
          </router-link>

          <div class="create-box">
            <button type="button" class="edu-btn edu-btn-primary" @click="createOpen = !createOpen">
              快捷创建
            </button>
            <div v-if="createOpen" class="create-panel">
              <router-link v-for="item in createItems" :key="item.path" :to="item.path" class="create-item">
                <strong>{{ item.label }}</strong>
                <span>{{ item.subtitle }}</span>
              </router-link>
            </div>
          </div>
        </div>
      </header>

      <div class="app-content">
        <div class="page-container">
          <router-view />
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '../stores/user'
import { notificationApi } from '../api/notification'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const sidebarOpen = ref(false)
const createOpen = ref(false)
const notificationCount = ref(0)

const navItems = [
  { path: '/teacher/dashboard', label: '仪表盘', subtitle: '教学总览与节奏', badge: 'DB' },
  { path: '/teacher/courses', label: '课程管理', subtitle: '课程与班级信息', badge: 'CR' },
  { path: '/teacher/students', label: '学生管理', subtitle: '班级成员与名单', badge: 'ST' },
  { path: '/teacher/tasks', label: '任务管理', subtitle: '发布任务与跟进', badge: 'TK' },
  { path: '/teacher/homeworks', label: '作业中心', subtitle: '发布作业与批改', badge: 'HW' },
  { path: '/teacher/quizzes', label: '测验管理', subtitle: '测验与自动判分', badge: 'QZ' },
  { path: '/teacher/grades', label: '成绩中心', subtitle: '测验成绩与趋势', badge: 'GD' },
  { path: '/teacher/materials', label: '资料中心', subtitle: '资源上传与管理', badge: 'MT' }
]

const createItems = [
  { path: '/teacher/course/create', label: '创建课程', subtitle: '新建教学课程' },
  { path: '/teacher/task/create', label: '发布任务', subtitle: '安排任务与截止时间' },
  { path: '/teacher/homework/create', label: '创建作业', subtitle: '配置自动批改规则' },
  { path: '/teacher/quiz/create', label: '创建测验', subtitle: '配置题目与分值' },
  { path: '/teacher/material/upload', label: '上传资料', subtitle: '补充课程资源' }
]

const pageMetaMap = {
  '/teacher/dashboard': {
    title: '教学仪表盘',
    subtitle: '把课程、任务、测验和资料放进同一条教学工作流里。'
  },
  '/teacher/courses': {
    title: '课程管理',
    subtitle: '在统一的课程画布里整理班级信息、资源和教学节奏。'
  },
  '/teacher/tasks': {
    title: '任务管理',
    subtitle: '发布任务、追踪执行状态，并让课程安排更清晰。'
  },
  '/teacher/homeworks': {
    title: '作业中心',
    subtitle: '让作业发布、自动批改、学生提交和教师复核进入同一条工作流。'
  },
  '/teacher/students': {
    title: '学生管理',
    subtitle: '集中查看各课程学生名单，减少在多个详情页之间来回切换。'
  },
  '/teacher/quizzes': {
    title: '测验管理',
    subtitle: '管理题目、发布时间和自动批改的测验流程。'
  },
  '/teacher/grades': {
    title: '成绩中心',
    subtitle: '先用真实测验数据构建成绩概览，为后续作业批改中心打基础。'
  },
  '/teacher/homework': {
    title: '作业详情',
    subtitle: '查看提交情况、题目正确率，并对主观题进行教师复核。'
  },
  '/teacher/materials': {
    title: '资料中心',
    subtitle: '让教学资源像资料馆一样清楚、易找、可追踪。'
  }
}

const userDisplayName = computed(() => userStore.user?.nickname || userStore.user?.username || '教师')
const avatarUrl = computed(() => userStore.user?.avatar || '')
const avatarFallback = computed(() => (userDisplayName.value || 'T').slice(0, 1).toUpperCase())

const pageMeta = computed(() => {
  const matchedKey = Object.keys(pageMetaMap).find((key) => route.path.startsWith(key))
  return matchedKey ? pageMetaMap[matchedKey] : {
    title: '教师工作台',
    subtitle: '在这里组织你的教学内容与课堂事务。'
  }
})

const isActive = (item) => route.path === item.path || route.path.startsWith(`${item.path}/`)

const handleLogout = () => {
  userStore.logout()
  router.push('/login')
}

const loadNotificationSummary = async () => {
  if (!userStore.user?.id) return
  try {
    const response = await notificationApi.getNotifications(userStore.user.id, 8)
    if (response.data.code === 200) {
      notificationCount.value = Number(response.data.data.summary?.totalCount || 0)
    }
  } catch (error) {
    notificationCount.value = 0
  }
}

watch(
  () => route.fullPath,
  () => {
    sidebarOpen.value = false
    createOpen.value = false
    loadNotificationSummary()
  }
)

onMounted(loadNotificationSummary)
</script>

<style scoped>
.message-link {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.message-count {
  min-width: 20px;
  height: 20px;
  padding: 0 6px;
  border-radius: 999px;
  background: #c64c4c;
  color: #fff;
  font-size: 12px;
  font-weight: 700;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.create-box {
  position: relative;
}

.create-panel {
  position: absolute;
  right: 0;
  top: calc(100% + 10px);
  width: 260px;
  padding: 10px;
  border-radius: 20px;
  border: 1px solid rgba(23, 32, 51, 0.10);
  background: rgba(255, 255, 255, 0.96);
  box-shadow: var(--shadow-lg);
  display: grid;
  gap: 6px;
  z-index: 30;
}

.create-item {
  display: grid;
  gap: 2px;
  padding: 12px 14px;
  border-radius: 14px;
  color: var(--ink);
}

.create-item:hover {
  background: rgba(44, 96, 214, 0.08);
  text-decoration: none;
}

.create-item span {
  color: var(--gray-500);
  font-size: 12px;
}
</style>
