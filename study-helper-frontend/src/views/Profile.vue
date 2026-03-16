<template>
  <div class="profile-page learning-environment">
    <EduNavbar />

    <main class="page-container profile-content">
      <div class="profile-card edu-card">
        <div class="page-header">
          <h1 class="page-title page-title-left">个人中心</h1>
          <button @click="handleLogout" class="edu-btn edu-btn-secondary">退出登录</button>
        </div>
        
        <div class="avatar-section">
          <div class="avatar">
            <img v-if="userStore.user?.avatar" :src="userStore.user.avatar" alt="头像" />
            <div v-else class="avatar-placeholder">{{ avatarText }}</div>
          </div>
          <input 
            type="text" 
            v-model="avatarUrl" 
            placeholder="输入头像URL"
            class="edu-input avatar-input"
          />
          <button @click="updateAvatar" class="edu-btn edu-btn-primary">更新头像</button>
        </div>

        <form @submit.prevent="handleUpdate" class="profile-form">
          <div class="form-row">
            <div class="form-group">
              <label>用户名</label>
              <input class="edu-input" v-model="form.username" type="text" disabled />
            </div>
            <div class="form-group">
              <label>角色</label>
              <input class="edu-input" v-model="form.role" type="text" disabled />
            </div>
          </div>

          <div class="form-row">
            <div class="form-group">
              <label>邮箱</label>
              <input class="edu-input" v-model="form.email" type="email" placeholder="请输入邮箱" />
            </div>
            <div class="form-group">
              <label>学号</label>
              <input class="edu-input" v-model="form.studentId" type="text" disabled />
            </div>
          </div>

          <div class="form-row">
            <div class="form-group">
              <label>专业</label>
              <input class="edu-input" v-model="form.major" type="text" placeholder="请输入专业" />
            </div>
            <div class="form-group">
              <label>年级</label>
              <input class="edu-input" v-model="form.grade" type="text" placeholder="请输入年级" />
            </div>
          </div>

          <div v-if="error" class="error-message">{{ error }}</div>
          <div v-if="success" class="success-message">{{ success }}</div>

          <button type="submit" class="edu-btn edu-btn-primary btn-save" :disabled="loading">
            {{ loading ? '保存中...' : '保存修改' }}
          </button>
        </form>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { userApi } from '../api/user'
import EduNavbar from '../components/EduNavbar.vue'

const router = useRouter()
const userStore = useUserStore()

const avatarUrl = ref('')
const error = ref('')
const success = ref('')
const loading = ref(false)

const form = reactive({
  username: '',
  email: '',
  studentId: '',
  major: '',
  grade: '',
  role: '',
  createdAt: ''
})

const avatarText = computed(() => {
  const username = userStore.user?.username
  const name = username || 'U'
  return name.charAt(0).toUpperCase()
})

onMounted(() => {
  if (userStore.user) {
    Object.assign(form, userStore.user)
    if (userStore.user.createdAt) {
      form.createdAt = new Date(userStore.user.createdAt).toLocaleString()
    }
  }
})

const handleUpdate = async () => {
  error.value = ''
  success.value = ''
  loading.value = true

  try {
    const result = await userStore.updateProfile(userStore.user.id, {
      email: form.email,
      major: form.major,
      grade: form.grade
    })
    if (result.success) {
      success.value = '个人信息更新成功！'
    } else {
      error.value = result.message
    }
  } catch (err) {
    error.value = '更新失败，请检查网络连接'
  } finally {
    loading.value = false
  }
}

const updateAvatar = async () => {
  if (!avatarUrl.value) {
    error.value = '请输入头像URL'
    return
  }
  
  try {
    const response = await userApi.updateAvatar(userStore.user.id, avatarUrl.value)
    if (response.data.code === 200) {
      userStore.user.avatar = avatarUrl.value
      success.value = '头像更新成功！'
      avatarUrl.value = ''
    } else {
      error.value = response.data.message
    }
  } catch (err) {
    error.value = '头像更新失败'
  }
}

const handleLogout = () => {
  userStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.profile-page {
  min-height: 100vh;
}

.profile-content {
  max-width: 900px;
}

.profile-card {
  padding: 2rem;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  gap: var(--spacing-md);
  flex-wrap: wrap;
  margin-bottom: var(--spacing-lg);
}

.page-title-left {
  text-align: left;
  margin: 0;
}

.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 2rem;
  gap: 1rem;
}

.avatar {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  overflow: hidden;
  background: radial-gradient(circle at 35% 25%, rgba(47, 111, 237, 0.95), rgba(31, 79, 191, 0.95));
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: var(--shadow-md);
}

.avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-placeholder {
  color: white;
  font-size: 2rem;
  font-weight: bold;
}

.avatar-input {
  width: min(520px, 92%);
}

.profile-form {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1rem;
}

.form-group {
  display: flex;
  flex-direction: column;
}

.form-group label {
  margin-bottom: 0.5rem;
  color: var(--gray-700);
  font-weight: 700;
}

.form-group :deep(input.edu-input:disabled) {
  background: rgba(22, 35, 58, 0.06);
  color: rgba(22, 35, 58, 0.70);
}

.error-message {
  color: var(--danger-color);
  text-align: center;
  font-size: 0.9rem;
}

.success-message {
  color: var(--secondary-color);
  text-align: center;
  font-size: 0.9rem;
}

.btn-save {
  margin-top: 1rem;
  height: 48px;
  font-size: 1rem;
  font-weight: 800;
}

.btn-save:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

@media (max-width: 600px) {
  .form-row {
    grid-template-columns: 1fr;
  }
}
</style>
