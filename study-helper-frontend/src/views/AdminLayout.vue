<template>
  <div class="app-shell admin-shell">
    <button
      class="edu-btn edu-btn-secondary shell-menu-btn"
      type="button"
      @click="sidebarOpen = true"
    >
      菜单
    </button>

    <div v-if="sidebarOpen" class="shell-overlay" @click="sidebarOpen = false"></div>

    <aside class="app-sidebar admin-sidebar" :class="{ open: sidebarOpen }">
      <router-link to="/admin/dashboard" class="app-brand">
        <span class="app-brand-mark">AD</span>
        <span class="app-brand-copy">
          <span class="app-brand-title">学习辅助系统</span>
          <span class="app-brand-subtitle">Admin Console</span>
        </span>
      </router-link>

      <div class="admin-note">
        <strong>平台运营视图</strong>
        <span>统一查看用户、内容、邀请码和系统运行数据。</span>
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
        <button type="button" class="app-nav-link" @click="handleLogout">
          <span class="app-nav-badge">EX</span>
          <span class="app-nav-copy">
            <span class="app-nav-title">退出登录</span>
            <span class="app-nav-subtitle">返回安全登录状态</span>
          </span>
        </button>
      </div>
    </aside>

    <main class="app-main">
      <header class="app-topbar admin-topbar">
        <div class="app-topbar-meta">
          <h1 class="app-topbar-title">{{ pageMeta.title }}</h1>
          <p class="app-topbar-copy">{{ pageMeta.subtitle }}</p>
        </div>

        <div class="app-topbar-actions">
          <span class="chip">管理员端</span>
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
import { computed, ref, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '../stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const sidebarOpen = ref(false)

const navItems = [
  { path: '/admin/dashboard', label: '仪表盘', subtitle: '平台总览与增长数据', badge: 'DB' },
  { path: '/admin/users', label: '用户管理', subtitle: '成员、角色与搜索', badge: 'US' },
  { path: '/admin/content', label: '内容概览', subtitle: '课程、资料与内容质量', badge: 'CT' },
  { path: '/admin/invitations', label: '邀请码', subtitle: '发放、状态与治理', badge: 'IV' }
]

const pageMetaMap = {
  '/admin/dashboard': {
    title: '管理员仪表盘',
    subtitle: '用更清晰的管理视图掌握平台活跃度、资源量和用户结构。'
  },
  '/admin/users': {
    title: '用户管理',
    subtitle: '搜索、筛选并治理平台中的学生、教师与管理员账户。'
  },
  '/admin/content': {
    title: '内容概览',
    subtitle: '检查课程和资料生态，及时发现内容质量问题。'
  },
  '/admin/invitations': {
    title: '邀请码管理',
    subtitle: '控制邀请码状态，维持注册流程稳定和可追踪。'
  }
}

const pageMeta = computed(() => {
  const matchedKey = Object.keys(pageMetaMap).find((key) => route.path.startsWith(key))
  return matchedKey ? pageMetaMap[matchedKey] : {
    title: '管理员控制台',
    subtitle: '集中处理平台配置、用户和内容运营事项。'
  }
})

const isActive = (item) => route.path === item.path || route.path.startsWith(`${item.path}/`)

const handleLogout = () => {
  userStore.logout()
  router.push('/login')
}

watch(
  () => route.fullPath,
  () => {
    sidebarOpen.value = false
  }
)
</script>

<style scoped>
.admin-sidebar {
  background:
    radial-gradient(circle at top, rgba(111, 152, 255, 0.18), transparent 26%),
    linear-gradient(180deg, rgba(245, 241, 233, 0.96), rgba(238, 231, 218, 0.94));
}

.admin-topbar {
  background: rgba(246, 241, 231, 0.82);
}

.admin-note {
  padding: 16px;
  border-radius: 22px;
  border: 1px solid rgba(23, 32, 51, 0.08);
  background: rgba(255, 255, 255, 0.84);
  display: grid;
  gap: 6px;
  color: var(--gray-700);
  font-size: 14px;
}

.admin-note strong {
  color: var(--ink);
}
</style>
