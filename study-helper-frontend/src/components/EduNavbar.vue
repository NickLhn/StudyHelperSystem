<template>
  <nav class="edu-navbar">
    <div class="page-container navbar-inner">
      <router-link to="/" class="brand">
        <span class="brand-mark">SH</span>
        <span class="brand-copy">
          <strong>学习辅助系统</strong>
          <small>Study Helper System</small>
        </span>
      </router-link>

      <div class="nav-actions">
        <template v-if="userStore.isLoggedIn">
          <span class="chip">{{ userStore.user?.nickname || userStore.user?.username }}</span>
          <router-link :to="homePath" class="edu-btn edu-btn-secondary">进入工作区</router-link>
          <button @click="handleLogout" class="edu-btn edu-btn-primary">退出</button>
        </template>

        <template v-else>
          <router-link to="/login" class="edu-btn edu-btn-secondary">登录</router-link>
          <router-link to="/register" class="edu-btn edu-btn-primary">注册</router-link>
        </template>
      </div>
    </div>
  </nav>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'

const router = useRouter()
const userStore = useUserStore()

const homePath = computed(() => {
  if (userStore.isAdmin) return '/admin'
  if (userStore.isTeacher) return '/teacher'
  if (userStore.isStudent) return '/student'
  return '/'
})

const handleLogout = () => {
  userStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.edu-navbar {
  position: sticky;
  top: 0;
  z-index: 50;
  backdrop-filter: blur(18px);
  background: rgba(251, 248, 241, 0.82);
  border-bottom: 1px solid rgba(23, 32, 51, 0.08);
}

.navbar-inner {
  min-height: 78px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 0 clamp(18px, 3vw, 32px);
}

.brand {
  display: flex;
  align-items: center;
  gap: 14px;
  color: var(--ink);
}

.brand:hover {
  text-decoration: none;
}

.brand-mark {
  width: 46px;
  height: 46px;
  border-radius: 16px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, var(--primary-dark), var(--primary-light));
  color: var(--white);
  font-weight: var(--font-weight-black);
  letter-spacing: 0.08em;
}

.brand-copy {
  display: grid;
  gap: 2px;
}

.brand-copy strong {
  font-size: 18px;
}

.brand-copy small {
  color: var(--gray-500);
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.nav-actions {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
  justify-content: flex-end;
}

@media (max-width: 640px) {
  .navbar-inner {
    align-items: flex-start;
    padding-top: 14px;
    padding-bottom: 14px;
    flex-direction: column;
  }

  .nav-actions {
    width: 100%;
    justify-content: flex-start;
  }
}
</style>
