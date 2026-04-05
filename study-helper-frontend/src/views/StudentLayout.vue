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
      <router-link to="/student/courses" class="app-brand">
        <span class="app-brand-mark">ST</span>
        <span class="app-brand-copy">
          <span class="app-brand-title">学习辅助系统</span>
          <span class="app-brand-subtitle">Student Space</span>
        </span>
      </router-link>

      <div class="app-user-card">
        <router-link to="/profile" class="app-avatar" aria-label="个人中心">
          <img v-if="avatarUrl" :src="avatarUrl" alt="avatar" />
          <span v-else>{{ avatarFallback }}</span>
        </router-link>
        <div class="app-user-copy">
          <div class="app-user-name">{{ userDisplayName }}</div>
          <div class="app-user-role">学生空间</div>
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
        <router-link to="/profile" class="app-nav-link" :class="{ active: route.path === '/profile' }">
          <span class="app-nav-badge">PF</span>
          <span class="app-nav-copy">
            <span class="app-nav-title">个人中心</span>
            <span class="app-nav-subtitle">资料与头像设置</span>
          </span>
        </router-link>
        <router-link to="/settings" class="app-nav-link" :class="{ active: route.path === '/settings' }">
          <span class="app-nav-badge">ST</span>
          <span class="app-nav-copy">
            <span class="app-nav-title">系统设置</span>
            <span class="app-nav-subtitle">通知与偏好</span>
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
          <span class="chip">学生端</span>
          <router-link to="/messages" class="edu-btn edu-btn-secondary message-link">
            <span>消息中心</span>
            <span v-if="notificationCount > 0" class="message-count">{{ notificationCount }}</span>
          </router-link>
          <router-link to="/profile" class="edu-btn edu-btn-ghost">个人中心</router-link>
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
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { notificationApi } from '../api/notification'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const sidebarOpen = ref(false)
const notificationCount = ref(0)

const navItems = [
  { path: '/student/courses', label: '课程学习', subtitle: '课程、班级与资料', badge: 'CR' },
  { path: '/student/homeworks', label: '我的作业', subtitle: '待完成与已提交结果', badge: 'HW' },
  { path: '/student/materials', label: '资料中心', subtitle: '查找和管理学习资源', badge: 'MT' },
  { path: '/student/wrong-book', label: '错题本', subtitle: '错题整理与复习', badge: 'WB' },
  { path: '/student/statistics', label: '学习统计', subtitle: '时长、进度与趋势', badge: 'AN' }
]

const pageMetaMap = {
  '/student/courses': {
    title: '课程学习',
    subtitle: '用更清楚的界面整理课程、进入班级并快速获取关键信息。'
  },
  '/student/homeworks': {
    title: '我的作业',
    subtitle: '把待完成作业、已提交状态和得分结果集中收纳到一个页面里。'
  },
  '/student/homework': {
    title: '作业学习',
    subtitle: '支持在线作答、自动批改和教师复核后的结果查看。'
  },
  '/student/materials': {
    title: '资料中心',
    subtitle: '把下载、收藏和搜索资料这件事做得更顺手。'
  },
  '/student/wrong-book': {
    title: '错题本',
    subtitle: '把错题沉淀成可回顾、可复习、可持续清理的个人题单。'
  },
  '/student/statistics': {
    title: '学习统计',
    subtitle: '从学习时长、课程分布到趋势变化，一屏读懂。'
  }
}

const userDisplayName = computed(() => userStore.user?.nickname || userStore.user?.username || '学生')
const avatarUrl = computed(() => userStore.user?.avatar || '')
const avatarFallback = computed(() => (userDisplayName.value || 'S').slice(0, 1).toUpperCase())

const pageMeta = computed(() => {
  const matchedKey = Object.keys(pageMetaMap).find((key) => route.path.startsWith(key))
  return matchedKey ? pageMetaMap[matchedKey] : {
    title: '学生空间',
    subtitle: '把课程、资料与学习进度放到一个更舒展的工作区里。'
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
</style>
