<template>
  <div class="mock-login">
    <h1>模拟教师登录</h1>
    <button class="btn" @click="handleLogin">一键登录为教师</button>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'

const router = useRouter()
const userStore = useUserStore()

const handleLogin = () => {
  userStore.mockTeacherLogin()
  router.push('/teacher')
}
</script>

<style scoped>
.mock-login {
  padding: 2rem 0;
  text-align: center;
}

.btn {
  padding: 0.75rem 1rem;
  border: 0;
  border-radius: 8px;
  background: #667eea;
  color: #fff;
  cursor: pointer;
}
</style>
