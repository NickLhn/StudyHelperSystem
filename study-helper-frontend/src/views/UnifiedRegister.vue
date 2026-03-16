<template>
  <div class="register-page learning-environment">
    <!-- 装饰性背景元素 -->
    <div class="background-decoration">
      <div class="decoration-circle circle-1"></div>
      <div class="decoration-circle circle-2"></div>
      <div class="decoration-circle circle-3"></div>
    </div>
    
    <div class="register-container">
      <div class="register-card edu-card">
        <!-- Logo和标题区域 -->
        <div class="register-header">
          <div class="logo-wrapper">
            <div class="logo-icon">📚</div>
          </div>
          <h1 class="register-title">用户注册</h1>
          <p class="register-subtitle">加入学习平台</p>
        </div>

        <!-- 角色选择 -->
        <div class="role-selection">
          <div 
            class="role-option" 
            :class="{ active: form.role === 'STUDENT' }"
            @click="selectRole('STUDENT')"
          >
            <div class="role-icon">🎓</div>
            <div class="role-info">
              <h3>我是学生</h3>
              <p>参与课程学习</p>
            </div>
          </div>
          <div 
            class="role-option" 
            :class="{ active: form.role === 'TEACHER' }"
            @click="selectRole('TEACHER')"
          >
            <div class="role-icon">👨‍🏫</div>
            <div class="role-info">
              <h3>我是教师</h3>
              <p>创建和管理课程</p>
            </div>
          </div>
        </div>

        <!-- 注册表单 -->
        <form @submit.prevent="handleRegister" class="register-form">
          <!-- 公共字段：用户名 -->
          <div class="form-group">
            <label class="form-label">用户名 *</label>
            <div class="input-wrapper">
              <svg class="input-icon" xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
                <circle cx="12" cy="7" r="4"></circle>
              </svg>
              <input 
                v-model="form.username" 
                type="text" 
                class="edu-input"
                placeholder="请输入2个字符的用户名"
                required
                minlength="2"
                maxlength="2"
              />
            </div>
          </div>

          <!-- 公共字段：密码 -->
          <div class="form-group">
            <label class="form-label">密码 *</label>
            <div class="input-wrapper">
              <svg class="input-icon" xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <rect x="3" y="11" width="18" height="11" rx="2" ry="2"></rect>
                <path d="M7 11V7a5 5 0 0 1 10 0v4"></path>
              </svg>
              <input 
                v-model="form.password" 
                :type="showPassword ? 'text' : 'password'"
                class="edu-input"
                placeholder="请输入密码（至少6位）"
                required
                minlength="6"
              />
              <button 
                type="button" 
                class="password-toggle"
                @click="showPassword = !showPassword"
              >
                <svg v-if="showPassword" xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"></path>
                  <line x1="1" y1="1" x2="23" y2="23"></line>
                </svg>
                <svg v-else xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path>
                  <circle cx="12" cy="12" r="3"></circle>
                </svg>
              </button>
            </div>
          </div>

          <!-- 公共字段：邮箱（选填） -->
          <div class="form-group">
            <label class="form-label">邮箱（选填）</label>
            <div class="input-wrapper">
              <svg class="input-icon" xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"></path>
                <polyline points="22,6 12,13 2,6"></polyline>
              </svg>
              <input 
                v-model="form.email" 
                type="email" 
                class="edu-input"
                placeholder="请输入邮箱地址（选填）"
              />
            </div>
          </div>

          <!-- 教师专属字段：姓名 -->
          <div class="form-group" v-if="form.role === 'TEACHER'">
            <label class="form-label">姓名 *</label>
            <div class="input-wrapper">
              <svg class="input-icon" xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
                <circle cx="12" cy="7" r="4"></circle>
              </svg>
              <input 
                v-model="form.nickname" 
                type="text" 
                class="edu-input"
                placeholder="请输入真实姓名"
                :required="form.role === 'TEACHER'"
              />
            </div>
          </div>

          <!-- 邀请码（教师必填） -->
          <div class="form-group" v-if="form.role === 'TEACHER'">
            <label class="form-label">邀请码 *</label>
            <div class="input-wrapper">
              <svg class="input-icon" xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <rect x="3" y="11" width="18" height="11" rx="2" ry="2"></rect>
                <path d="M7 11V7a5 5 0 0 1 10 0v4"></path>
              </svg>
              <input 
                v-model="form.invitationCode" 
                type="text" 
                class="edu-input"
                placeholder="请输入邀请码"
                :required="form.role === 'TEACHER'"
              />
            </div>
            <p class="form-hint">邀请码请联系管理员获取</p>
          </div>

          <!-- 教师专属字段：职称 -->
          <div class="form-group" v-if="form.role === 'TEACHER'">
            <label class="form-label">教师职称 *</label>
            <div class="input-wrapper">
              <svg class="input-icon" xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="12" cy="8" r="7"></circle>
                <polyline points="8.21 13.89 7 23 12 20 17 23 15.79 13.88"></polyline>
              </svg>
              <select v-model="form.teacherTitle" class="edu-input" :required="form.role === 'TEACHER'">
                <option value="">请选择职称</option>
                <option value="助教">助教</option>
                <option value="讲师">讲师</option>
                <option value="副教授">副教授</option>
                <option value="教授">教授</option>
                <option value="研究员">研究员</option>
                <option value="副研究员">副研究员</option>
              </select>
            </div>
          </div>

          <!-- 教师专属字段：院系 -->
          <div class="form-group" v-if="form.role === 'TEACHER'">
            <label class="form-label">所在院系 *</label>
            <div class="input-wrapper">
              <svg class="input-icon" xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path>
                <polyline points="14 2 14 8 20 8"></polyline>
                <line x1="16" y1="13" x2="8" y2="13"></line>
                <line x1="16" y1="17" x2="8" y2="17"></line>
              </svg>
              <select v-model="form.department" class="edu-input" :required="form.role === 'TEACHER'">
                <option value="">请选择院系</option>
                <option value="计算机科学与技术学院">计算机科学与技术学院</option>
                <option value="软件学院">软件学院</option>
                <option value="电子信息工程学院">电子信息工程学院</option>
                <option value="数学与统计学院">数学与统计学院</option>
                <option value="物理学院">物理学院</option>
                <option value="化学化工学院">化学化工学院</option>
                <option value="外国语学院">外国语学院</option>
                <option value="文学院">文学院</option>
                <option value="商学院">商学院</option>
                <option value="其他">其他</option>
              </select>
            </div>
          </div>

          <!-- 学生专属字段：学号 -->
          <div class="form-group" v-if="form.role === 'STUDENT'">
            <label class="form-label">学号 *</label>
            <div class="input-wrapper">
              <svg class="input-icon" xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
                <circle cx="12" cy="7" r="4"></circle>
              </svg>
              <input 
                v-model="form.studentId" 
                type="text" 
                class="edu-input"
                placeholder="请输入学号"
                :required="form.role === 'STUDENT'"
              />
            </div>
          </div>

          <!-- 学生专属字段：专业 -->
          <div class="form-group" v-if="form.role === 'STUDENT'">
            <label class="form-label">专业 *</label>
            <div class="input-wrapper">
              <svg class="input-icon" xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path>
                <polyline points="14 2 14 8 20 8"></polyline>
                <line x1="16" y1="13" x2="8" y2="13"></line>
                <line x1="16" y1="17" x2="8" y2="17"></line>
              </svg>
              <input 
                v-model="form.major" 
                type="text" 
                class="edu-input"
                placeholder="请输入专业"
                :required="form.role === 'STUDENT'"
              />
            </div>
          </div>

          <!-- 学生专属字段：年级 -->
          <div class="form-group" v-if="form.role === 'STUDENT'">
            <label class="form-label">年级 *</label>
            <div class="input-wrapper">
              <svg class="input-icon" xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="12" cy="12" r="10"></circle>
                <polyline points="12 6 12 12 16 14"></polyline>
              </svg>
              <input 
                v-model="form.grade" 
                type="text" 
                class="edu-input"
                placeholder="请输入年级（如：2024）"
                :required="form.role === 'STUDENT'"
              />
            </div>
          </div>

          <!-- 错误提示 -->
          <div v-if="error" class="error-message">
            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="12" cy="12" r="10"></circle>
              <line x1="12" y1="8" x2="12" y2="12"></line>
              <line x1="12" y1="16" x2="12.01" y2="16"></line>
            </svg>
            {{ error }}
          </div>

          <!-- 注册按钮 -->
          <button type="submit" class="edu-btn edu-btn-primary register-btn" :disabled="loading">
            <span v-if="loading" class="loading-spinner"></span>
            {{ loading ? '注册中...' : '立即注册' }}
          </button>
        </form>

        <!-- 底部链接 -->
        <div class="register-footer">
          <p class="auth-link">
            已有账号？
            <router-link to="/login" class="link-primary">立即登录</router-link>
          </p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

const API_BASE_URL = 'http://localhost:8080/api'

const form = reactive({
  role: 'STUDENT',
  username: '',
  password: '',
  email: '',
  nickname: '',
  invitationCode: '',
  teacherTitle: '',
  department: '',
  studentId: '',
  major: '',
  grade: ''
})

const showPassword = ref(false)
const loading = ref(false)
const error = ref('')

const selectRole = (role) => {
  form.role = role
}

const handleRegister = async () => {
  error.value = ''
  loading.value = true

  try {
    const endpoint = form.role === 'TEACHER' 
      ? `${API_BASE_URL}/auth/teacher/register`
      : `${API_BASE_URL}/user/register`

    const payload = form.role === 'TEACHER'
      ? {
          username: form.username,
          password: form.password,
          email: form.email || null,
          nickname: form.nickname || null,
          invitationCode: form.invitationCode || null,
          teacherTitle: form.teacherTitle || null,
          department: form.department || null
        }
      : {
          username: form.username,
          password: form.password,
          email: form.email || null,
          studentId: form.studentId || null,
          major: form.major || null,
          grade: form.grade || null
        }

    const response = await fetch(endpoint, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(payload)
    })

    const result = await response.json()
    
    if (result.code === 200 || result.success === true) {
      alert('注册成功！请登录')
      router.push('/login')
    } else {
      error.value = result.message || '注册失败'
    }
  } catch (err) {
    error.value = '注册失败，请检查网络连接'
    console.error('注册失败:', err)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.register-page {
  min-height: 100vh;
  position: relative;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 2rem;
}

.background-decoration {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  overflow: hidden;
  z-index: 1;
}

.decoration-circle {
  position: absolute;
  border-radius: 50%;
  background: linear-gradient(135deg, rgba(74, 144, 226, 0.1) 0%, rgba(97, 179, 210, 0.1) 100%);
}

.circle-1 {
  width: 300px;
  height: 300px;
  top: -100px;
  right: -100px;
}

.circle-2 {
  width: 200px;
  height: 200px;
  bottom: -50px;
  left: -50px;
}

.circle-3 {
  width: 150px;
  height: 150px;
  top: 50%;
  left: 10%;
}

.register-container {
  position: relative;
  z-index: 2;
  width: 100%;
  max-width: 500px;
}

.register-card {
  padding: 2rem;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
}

.register-header {
  text-align: center;
  margin-bottom: 2rem;
}

.logo-icon {
  font-size: 48px;
  margin-bottom: 0.5rem;
}

.register-title {
  font-size: 1.8rem;
  font-weight: 700;
  color: #333;
  margin-bottom: 0.5rem;
}

.register-subtitle {
  color: #666;
  font-size: 1rem;
  margin: 0;
}

.role-selection {
  display: flex;
  gap: 1rem;
  margin-bottom: 2rem;
}

.role-option {
  flex: 1;
  padding: 1rem;
  border: 2px solid #ddd;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.role-option:hover {
  border-color: #4a90e2;
}

.role-option.active {
  border-color: #4a90e2;
  background: #f0f7ff;
}

.role-icon {
  font-size: 2rem;
}

.role-info h3 {
  margin: 0;
  font-size: 1rem;
  font-weight: 600;
  color: #333;
}

.role-info p {
  margin: 0;
  font-size: 0.8rem;
  color: #666;
}

.register-form {
  margin-bottom: 1.5rem;
}

.form-group {
  margin-bottom: 1.25rem;
}

.form-label {
  display: block;
  margin-bottom: 0.5rem;
  color: #555;
  font-weight: 500;
  font-size: 0.9rem;
}

.input-wrapper {
  position: relative;
}

.input-icon {
  position: absolute;
  left: 12px;
  top: 50%;
  transform: translateY(-50%);
  color: #999;
}

.edu-input {
  width: 100%;
  height: 45px;
  padding: 0 40px 0 40px;
  border: 2px solid #ddd;
  border-radius: 8px;
  font-size: 0.95rem;
  transition: all 0.3s ease;
  background: white;
}

.edu-input:focus {
  outline: none;
  border-color: #4a90e2;
  box-shadow: 0 0 0 3px rgba(74, 144, 226, 0.1);
}

select.edu-input {
  appearance: none;
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='12' height='12' viewBox='0 0 12 12'%3E%3Cpath fill='%23999' d='M6 8L1 3h10z'/%3E%3C/svg%3E");
  background-repeat: no-repeat;
  background-position: right 12px center;
}

.password-toggle {
  position: absolute;
  right: 12px;
  top: 50%;
  transform: translateY(-50%);
  background: none;
  border: none;
  color: #999;
  cursor: pointer;
  padding: 4px;
}

.form-hint {
  margin-top: 0.5rem;
  font-size: 0.8rem;
  color: #888;
}

.error-message {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem;
  background: #fff0f0;
  color: #e53935;
  border-radius: 8px;
  font-size: 0.9rem;
  margin-bottom: 1rem;
}

.register-btn {
  width: 100%;
  height: 48px;
  font-size: 1rem;
  font-weight: 600;
  margin-top: 0.5rem;
}

.loading-spinner {
  display: inline-block;
  width: 16px;
  height: 16px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-radius: 50%;
  border-top-color: white;
  animation: spin 1s linear infinite;
  margin-right: 0.5rem;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.register-footer {
  text-align: center;
  padding-top: 1.5rem;
  border-top: 1px solid #eee;
}

.auth-link {
  color: #666;
  font-size: 0.9rem;
}

.link-primary {
  color: #4a90e2;
  font-weight: 500;
  text-decoration: none;
}

.link-primary:hover {
  text-decoration: underline;
}

@media (max-width: 480px) {
  .register-page {
    padding: 1rem;
  }
  
  .role-selection {
    flex-direction: column;
  }
  
  .role-option {
    padding: 0.75rem;
  }
}
</style>
