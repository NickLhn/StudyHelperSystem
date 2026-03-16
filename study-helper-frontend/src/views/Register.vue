<template>
  <div class="auth-container">
    <div class="auth-box">
      <h2>用户注册</h2>
      <form @submit.prevent="handleRegister">
        <div class="form-group">
          <label>用户名 *</label>
          <input 
            v-model="form.username" 
            type="text" 
            placeholder="请输入用户名（3-20字符）"
            required
            minlength="3"
            maxlength="20"
          />
        </div>
        <div class="form-group">
          <label>密码 *</label>
          <input 
            v-model="form.password" 
            type="password" 
            placeholder="请输入密码（至少6位）"
            required
            minlength="6"
          />
        </div>
        <div class="form-group">
          <label>邮箱</label>
          <input 
            v-model="form.email" 
            type="email" 
            placeholder="请输入邮箱"
          />
        </div>
        <div class="form-group">
          <label>学号 *</label>
          <input 
            v-model="form.studentId" 
            type="text" 
            placeholder="请输入学号"
            required
          />
        </div>
        <div class="form-group">
          <label>专业 *</label>
          <input 
            v-model="form.major" 
            type="text" 
            placeholder="请输入专业"
            required
          />
        </div>
        <div class="form-group">
          <label>年级 *</label>
          <input 
            v-model="form.grade" 
            type="text" 
            placeholder="请输入年级（如：2022级）"
            required
          />
        </div>
        <div v-if="error" class="error-message">{{ error }}</div>
        <div v-if="success" class="success-message">{{ success }}</div>
        <button type="submit" class="btn-submit" :disabled="loading">
          {{ loading ? '注册中...' : '注册' }}
        </button>
      </form>
      <div class="auth-links">
        <p class="auth-link">
          已有账号？<router-link to="/login">立即登录</router-link>
        </p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'

const router = useRouter()
const userStore = useUserStore()

const form = reactive({
  username: '',
  password: '',
  email: '',
  studentId: '',
  major: '',
  grade: ''
})

const error = ref('')
const success = ref('')
const loading = ref(false)

const handleRegister = async () => {
  error.value = ''
  success.value = ''
  loading.value = true
  
  try {
    const result = await userStore.register(form)
    if (result.success) {
      success.value = '注册成功！即将跳转到登录页面...'
      setTimeout(() => {
        router.push('/login')
      }, 1500)
    } else {
      error.value = result.message
    }
  } catch (err) {
    error.value = '注册失败，请检查网络连接'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-container {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 2rem 0;
}

.auth-box {
  background: white;
  padding: 2.5rem;
  border-radius: 12px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
  width: 100%;
  max-width: 450px;
  max-height: 90vh;
  overflow-y: auto;
}

.auth-box h2 {
  text-align: center;
  color: #333;
  margin-bottom: 1.5rem;
}

.form-group {
  margin-bottom: 1.25rem;
}

.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  color: #555;
  font-weight: 500;
}

.form-group label::after {
  content: '';
}

.form-group input {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 1rem;
  transition: border-color 0.3s;
}

.form-group input:focus {
  outline: none;
  border-color: #667eea;
}

.error-message {
  color: #e74c3c;
  margin-bottom: 1rem;
  text-align: center;
  font-size: 0.9rem;
}

.success-message {
  color: #27ae60;
  margin-bottom: 1rem;
  text-align: center;
  font-size: 0.9rem;
}

.btn-submit {
  width: 100%;
  padding: 0.875rem;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 1rem;
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
}

.btn-submit:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 5px 20px rgba(102, 126, 234, 0.4);
}

.btn-submit:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.auth-links {
  text-align: center;
  margin-top: 1.5rem;
}

.auth-link {
  color: #666;
  margin: 0.5rem 0;
}

.auth-link a {
  color: #667eea;
  text-decoration: none;
  font-weight: 500;
}

.auth-link a:hover {
  text-decoration: underline;
}

.teacher-link a {
  color: #42b883;
}
</style>
