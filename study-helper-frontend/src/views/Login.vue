<template>
  <div class="login-page">
    <!-- 动态背景 -->
    <div class="background-layer">
      <div class="gradient-overlay"></div>
      <div class="floating-shapes">
        <div class="shape shape-1"></div>
        <div class="shape shape-2"></div>
        <div class="shape shape-3"></div>
        <div class="shape shape-4"></div>
      </div>
    </div>
    
    <!-- 主内容区 -->
    <div class="content-layer">
      <!-- 左侧品牌区 -->
      <div class="brand-section">
        <div class="brand-content">
          <div class="logo-mark">
            <svg viewBox="0 0 48 48" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M24 4L4 14v20l20 10 20-10V14L24 4z" stroke="currentColor" stroke-width="2" fill="none"/>
              <path d="M24 24v20M4 14l20 10 20-10" stroke="currentColor" stroke-width="2"/>
              <circle cx="24" cy="24" r="6" fill="currentColor"/>
            </svg>
          </div>
          <h1 class="brand-title">StudyHelper</h1>
          <p class="brand-tagline">智慧学习，从这里开始</p>
          <div class="brand-features">
            <div class="feature-item">
              <span class="feature-icon">◆</span>
              <span>智能课程管理</span>
            </div>
            <div class="feature-item">
              <span class="feature-icon">◆</span>
              <span>在线作业提交</span>
            </div>
            <div class="feature-item">
              <span class="feature-icon">◆</span>
              <span>学习数据分析</span>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 右侧登录区 -->
      <div class="login-section">
        <div class="login-card">
          <!-- 卡片头部 -->
          <div class="card-header">
            <h2 class="card-title">欢迎回来</h2>
            <p class="card-subtitle">请登录您的账户</p>
          </div>
          
          <!-- 登录表单 -->
          <form @submit.prevent="handleLogin" class="login-form">
            <!-- 账号输入 -->
            <div class="input-group" :class="{ 'focused': focusedField === 'account', 'filled': form.account }">
              <label class="input-label">账号</label>
              <div class="input-wrapper">
                <input 
                  v-model="form.account" 
                  type="text" 
                  class="form-input"
                  placeholder="用户名 / 邮箱 / 学号"
                  @focus="focusedField = 'account'"
                  @blur="focusedField = null"
                  required
                />
                <div class="input-line"></div>
              </div>
            </div>
            
            <!-- 密码输入 -->
            <div class="input-group" :class="{ 'focused': focusedField === 'password', 'filled': form.password }">
              <label class="input-label">密码</label>
              <div class="input-wrapper">
                <input 
                  v-model="form.password" 
                  :type="showPassword ? 'text' : 'password'"
                  class="form-input"
                  placeholder="请输入密码"
                  @focus="focusedField = 'password'"
                  @blur="focusedField = null"
                  required
                />
                <button 
                  type="button" 
                  class="toggle-password"
                  @click="showPassword = !showPassword"
                >
                  <svg v-if="!showPassword" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/>
                    <circle cx="12" cy="12" r="3"/>
                  </svg>
                  <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"/>
                    <line x1="1" y1="1" x2="23" y2="23"/>
                  </svg>
                </button>
                <div class="input-line"></div>
              </div>
            </div>
            
            <!-- 记住我 & 忘记密码 -->
            <div class="form-options">
              <label class="remember-me">
                <input type="checkbox" v-model="rememberMe" />
                <span class="checkmark"></span>
                <span class="label-text">记住我</span>
              </label>
              <a href="#" class="forgot-password">忘记密码？</a>
            </div>
            
            <!-- 错误提示 -->
            <div v-if="error" class="alert alert-error">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="12" cy="12" r="10"/>
                <line x1="12" y1="8" x2="12" y2="12"/>
                <line x1="12" y1="16" x2="12.01" y2="16"/>
              </svg>
              <span>{{ error }}</span>
            </div>
            
            <!-- 登录按钮 -->
            <button type="submit" class="login-btn" :disabled="loading">
              <span v-if="loading" class="btn-loader"></span>
              <span v-else>登 录</span>
            </button>
          </form>
          
          <!-- 分割线 -->
          <div class="divider">
            <span>或</span>
          </div>
          
          <!-- 快速登录 -->
          <div class="quick-actions">
            <button @click="fillDemoAccount('admin')" class="quick-btn">
              <span class="btn-icon">👤</span>
              <span>管理员演示</span>
            </button>
            <button @click="fillDemoAccount('teacher')" class="quick-btn">
              <span class="btn-icon">👨‍🏫</span>
              <span>教师演示</span>
            </button>
            <button @click="fillDemoAccount('student')" class="quick-btn">
              <span class="btn-icon">👨‍🎓</span>
              <span>学生演示</span>
            </button>
          </div>
          
          <!-- 注册链接 -->
          <div class="register-link">
            <span>还没有账户？</span>
            <router-link to="/register">立即注册</router-link>
          </div>
        </div>
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
  account: '',
  password: ''
})

const focusedField = ref(null)
const showPassword = ref(false)
const rememberMe = ref(false)
const error = ref('')
const loading = ref(false)

const handleLogin = async () => {
  error.value = ''
  loading.value = true
  
  try {
    const result = await userStore.login(form)
    
    if (result.success) {
      // 保存记住我状态
      if (rememberMe.value) {
        localStorage.setItem('rememberAccount', form.account)
      }
      
      setTimeout(() => {
        if (userStore.isAdmin) {
          router.push('/admin')
        } else if (userStore.isTeacher) {
          router.push('/teacher')
        } else {
          router.push('/')
        }
      }, 500)
    } else {
      error.value = result.message || '登录失败'
    }
  } catch (err) {
    error.value = '登录失败，请检查网络连接'
  } finally {
    loading.value = false
  }
}

// 演示账号填充
const fillDemoAccount = (type) => {
  const accounts = {
    admin: { account: 'admin', password: 'admin' },
    teacher: { account: 'teacher', password: 'teacher' },
    student: { account: 'student', password: 'student' }
  }
  
  const acc = accounts[type]
  if (acc) {
    form.account = acc.account
    form.password = acc.password
  }
}
</script>

<style scoped>
/* 字体导入 */
@import url('https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700&display=swap');

/* CSS变量 */
:root {
  --primary: #6366f1;
  --primary-dark: #4f46e5;
  --primary-light: #818cf8;
  --secondary: #ec4899;
  --background: #0f172a;
  --surface: #1e293b;
  --surface-light: #334155;
  --text-primary: #f8fafc;
  --text-secondary: #94a3b8;
  --text-muted: #64748b;
  --error: #ef4444;
  --success: #10b981;
  --gradient-start: #6366f1;
  --gradient-end: #ec4899;
}

/* 页面容器 */
.login-page {
  min-height: 100vh;
  display: flex;
  position: relative;
  font-family: 'Plus Jakarta Sans', -apple-system, BlinkMacSystemFont, sans-serif;
  overflow: hidden;
}

/* 背景层 */
.background-layer {
  position: fixed;
  inset: 0;
  z-index: 0;
}

.gradient-overlay {
  position: absolute;
  inset: 0;
  background: 
    radial-gradient(ellipse at 20% 20%, rgba(99, 102, 241, 0.15) 0%, transparent 50%),
    radial-gradient(ellipse at 80% 80%, rgba(236, 72, 153, 0.15) 0%, transparent 50%),
    linear-gradient(135deg, #0f172a 0%, #1e1b4b 50%, #0f172a 100%);
}

.floating-shapes {
  position: absolute;
  inset: 0;
  overflow: hidden;
}

.shape {
  position: absolute;
  border-radius: 50%;
  filter: blur(60px);
  opacity: 0.4;
  animation: float 20s ease-in-out infinite;
}

.shape-1 {
  width: 400px;
  height: 400px;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  top: -100px;
  right: -100px;
  animation-delay: 0s;
}

.shape-2 {
  width: 300px;
  height: 300px;
  background: linear-gradient(135deg, #ec4899, #f43f5e);
  bottom: -50px;
  left: -50px;
  animation-delay: -5s;
}

.shape-3 {
  width: 200px;
  height: 200px;
  background: linear-gradient(135deg, #06b6d4, #3b82f6);
  top: 40%;
  left: 20%;
  animation-delay: -10s;
}

.shape-4 {
  width: 250px;
  height: 250px;
  background: linear-gradient(135deg, #8b5cf6, #6366f1);
  bottom: 30%;
  right: 30%;
  animation-delay: -15s;
}

@keyframes float {
  0%, 100% { transform: translate(0, 0) scale(1); }
  25% { transform: translate(30px, -30px) scale(1.05); }
  50% { transform: translate(-20px, 20px) scale(0.95); }
  75% { transform: translate(20px, 10px) scale(1.02); }
}

/* 内容层 */
.content-layer {
  position: relative;
  z-index: 1;
  width: 100%;
  display: grid;
  grid-template-columns: 1fr 1fr;
  min-height: 100vh;
}

/* 品牌区 */
.brand-section {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 4rem;
}

.brand-content {
  max-width: 480px;
}

.logo-mark {
  width: 64px;
  height: 64px;
  color: #6366f1;
  margin-bottom: 2rem;
}

.logo-mark svg {
  width: 100%;
  height: 100%;
}

.brand-title {
  font-size: 3.5rem;
  font-weight: 700;
  color: #f8fafc;
  margin: 0 0 0.5rem 0;
  letter-spacing: -0.02em;
  background: linear-gradient(135deg, #f8fafc 0%, #94a3b8 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.brand-tagline {
  font-size: 1.25rem;
  color: #94a3b8;
  margin: 0 0 3rem 0;
  font-weight: 400;
}

.brand-features {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  color: #cbd5e1;
  font-size: 1rem;
}

.feature-icon {
  color: #6366f1;
  font-size: 0.75rem;
}

/* 登录区 */
.login-section {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 2rem;
}

.login-card {
  width: 100%;
  max-width: 420px;
  background: rgba(30, 41, 59, 0.8);
  backdrop-filter: blur(20px);
  border-radius: 24px;
  padding: 3rem;
  border: 1px solid rgba(255, 255, 255, 0.1);
  box-shadow: 
    0 25px 50px -12px rgba(0, 0, 0, 0.5),
    0 0 0 1px rgba(255, 255, 255, 0.05);
}

.card-header {
  text-align: center;
  margin-bottom: 2.5rem;
}

.card-title {
  font-size: 1.875rem;
  font-weight: 700;
  color: #f8fafc;
  margin: 0 0 0.5rem 0;
}

.card-subtitle {
  font-size: 1rem;
  color: #94a3b8;
  margin: 0;
}

/* 表单样式 */
.login-form {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.input-group {
  position: relative;
}

.input-label {
  display: block;
  font-size: 0.875rem;
  font-weight: 500;
  color: #94a3b8;
  margin-bottom: 0.5rem;
  transition: color 0.2s;
}

.input-group.focused .input-label {
  color: #818cf8;
}

.input-wrapper {
  position: relative;
}

.form-input {
  width: 100%;
  height: 52px;
  padding: 0 1rem;
  background: rgba(15, 23, 42, 0.6);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  color: #f8fafc;
  font-size: 1rem;
  font-family: inherit;
  transition: all 0.2s;
}

.form-input::placeholder {
  color: #64748b;
}

.form-input:focus {
  outline: none;
  border-color: #6366f1;
  background: rgba(15, 23, 42, 0.8);
  box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.2);
}

.input-line {
  position: absolute;
  bottom: 0;
  left: 50%;
  right: 50%;
  height: 2px;
  background: linear-gradient(90deg, #6366f1, #ec4899);
  transition: all 0.3s;
  border-radius: 0 0 12px 12px;
}

.input-group.focused .input-line {
  left: 0;
  right: 0;
}

.toggle-password {
  position: absolute;
  right: 1rem;
  top: 50%;
  transform: translateY(-50%);
  background: none;
  border: none;
  color: #64748b;
  cursor: pointer;
  padding: 0.25rem;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: color 0.2s;
}

.toggle-password:hover {
  color: #94a3b8;
}

.toggle-password svg {
  width: 20px;
  height: 20px;
}

/* 表单选项 */
.form-options {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: -0.5rem;
}

.remember-me {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  cursor: pointer;
}

.remember-me input {
  display: none;
}

.checkmark {
  width: 18px;
  height: 18px;
  border: 2px solid rgba(255, 255, 255, 0.2);
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}

.remember-me input:checked + .checkmark {
  background: #6366f1;
  border-color: #6366f1;
}

.remember-me input:checked + .checkmark::after {
  content: '✓';
  color: white;
  font-size: 12px;
  font-weight: bold;
}

.label-text {
  font-size: 0.875rem;
  color: #94a3b8;
}

.forgot-password {
  font-size: 0.875rem;
  color: #818cf8;
  text-decoration: none;
  transition: color 0.2s;
}

.forgot-password:hover {
  color: #a5b4fc;
}

/* 警告提示 */
.alert {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.875rem 1rem;
  border-radius: 10px;
  font-size: 0.875rem;
}

.alert svg {
  width: 18px;
  height: 18px;
  flex-shrink: 0;
}

.alert-error {
  background: rgba(239, 68, 68, 0.1);
  border: 1px solid rgba(239, 68, 68, 0.2);
  color: #fca5a5;
}

/* 登录按钮 */
.login-btn {
  height: 52px;
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%);
  border: none;
  border-radius: 12px;
  color: white;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
  position: relative;
  overflow: hidden;
}

.login-btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
  transition: left 0.5s;
}

.login-btn:hover::before {
  left: 100%;
}

.login-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 30px -10px rgba(99, 102, 241, 0.5);
}

.login-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.btn-loader {
  display: inline-block;
  width: 20px;
  height: 20px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: white;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* 分割线 */
.divider {
  display: flex;
  align-items: center;
  margin: 1.5rem 0;
  color: #64748b;
  font-size: 0.875rem;
}

.divider::before,
.divider::after {
  content: '';
  flex: 1;
  height: 1px;
  background: rgba(255, 255, 255, 0.1);
}

.divider span {
  padding: 0 1rem;
}

/* 快速操作 */
.quick-actions {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 0.75rem;
}

.quick-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.5rem;
  padding: 1rem 0.5rem;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  color: #94a3b8;
  font-size: 0.75rem;
  cursor: pointer;
  transition: all 0.2s;
}

.quick-btn:hover {
  background: rgba(99, 102, 241, 0.1);
  border-color: rgba(99, 102, 241, 0.3);
  color: #f8fafc;
  transform: translateY(-2px);
}

.btn-icon {
  font-size: 1.5rem;
}

/* 注册链接 */
.register-link {
  text-align: center;
  margin-top: 1.5rem;
  padding-top: 1.5rem;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  color: #94a3b8;
  font-size: 0.875rem;
}

.register-link a {
  color: #818cf8;
  text-decoration: none;
  font-weight: 500;
  margin-left: 0.25rem;
  transition: color 0.2s;
}

.register-link a:hover {
  color: #a5b4fc;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .content-layer {
    grid-template-columns: 1fr;
  }
  
  .brand-section {
    display: none;
  }
  
  .login-section {
    padding: 1.5rem;
  }
  
  .login-card {
    padding: 2rem;
  }
}

@media (max-width: 640px) {
  .login-card {
    padding: 1.5rem;
    border-radius: 20px;
  }
  
  .card-title {
    font-size: 1.5rem;
  }
  
  .quick-actions {
    grid-template-columns: 1fr;
  }
  
  .quick-btn {
    flex-direction: row;
    justify-content: center;
    padding: 0.75rem;
  }
}
</style>
