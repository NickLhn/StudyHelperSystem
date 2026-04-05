<template>
  <div class="login-page">
    <div class="page-container login-shell">
      <section class="login-brand">
        <span class="page-eyebrow">Campus Workspace</span>
        <h1 class="page-title">登录你的学习与教学工作区</h1>
        <p class="page-subtitle">新的登录页和系统内部 UI 采用同一套视觉语言，从入口开始就更统一、克制，也更专业。</p>

        <div class="brand-points">
          <div class="point-item">
            <strong>课程与资料一体化</strong>
            <span>学生、教师和管理员进入后看到的都是同一套产品语言。</span>
          </div>
          <div class="point-item">
            <strong>为自动批改预留空间</strong>
            <span>测验和作业流后续可以自然扩展到教师批改中心。</span>
          </div>
          <div class="point-item">
            <strong>更轻的操作负担</strong>
            <span>常用入口和高频信息已经被重新组织。</span>
          </div>
        </div>
      </section>

      <section class="info-card login-card">
        <div class="stack-md">
          <div>
            <span class="page-eyebrow">Sign In</span>
            <h2 class="section-title">欢迎回来</h2>
            <p class="section-copy">请输入账号和密码继续。</p>
          </div>

          <form class="form-grid" @submit.prevent="handleLogin">
            <div class="field">
              <label>账号</label>
              <input v-model="form.account" type="text" placeholder="用户名 / 邮箱 / 学号" required />
            </div>

            <div class="field">
              <label>密码</label>
              <div class="password-row">
                <input
                  v-model="form.password"
                  :type="showPassword ? 'text' : 'password'"
                  placeholder="请输入密码"
                  required
                />
                <button type="button" class="edu-btn edu-btn-secondary" @click="showPassword = !showPassword">
                  {{ showPassword ? '隐藏' : '显示' }}
                </button>
              </div>
            </div>

            <label class="checkbox-row">
              <input type="checkbox" v-model="rememberMe" />
              <span>记住当前账号</span>
            </label>

            <div v-if="error" class="message-banner error">{{ error }}</div>

            <button type="submit" class="edu-btn edu-btn-primary login-btn" :disabled="loading">
              {{ loading ? '登录中...' : '登录系统' }}
            </button>
          </form>

          <div class="stack-sm">
            <p class="section-copy">演示账号</p>
            <div class="toolbar-row">
              <button @click="fillDemoAccount('admin')" class="edu-btn edu-btn-secondary" type="button">管理员</button>
              <button @click="fillDemoAccount('teacher')" class="edu-btn edu-btn-secondary" type="button">教师</button>
              <button @click="fillDemoAccount('student')" class="edu-btn edu-btn-secondary" type="button">学生</button>
            </div>
          </div>

          <p class="section-copy">
            还没有账户？
            <router-link to="/register">立即注册</router-link>
          </p>
        </div>
      </section>
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
      }, 300)
    } else {
      error.value = result.message || '登录失败'
    }
  } catch (err) {
    error.value = '登录失败，请检查网络连接'
  } finally {
    loading.value = false
  }
}

const fillDemoAccount = (type) => {
  const accounts = {
    admin: { account: 'admin', password: 'ChangeMe123!' },
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
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  padding: clamp(20px, 4vw, 42px);
}

.login-shell {
  display: grid;
  grid-template-columns: 1.1fr minmax(320px, 480px);
  gap: 28px;
  align-items: stretch;
}

.login-brand,
.login-card {
  padding: clamp(24px, 4vw, 40px);
}

.login-brand {
  border-radius: 34px;
  background:
    radial-gradient(circle at top left, rgba(111, 152, 255, 0.22), transparent 34%),
    radial-gradient(circle at bottom right, rgba(15, 139, 132, 0.14), transparent 34%),
    linear-gradient(160deg, rgba(255, 255, 255, 0.94), rgba(246, 241, 231, 0.88));
  border: 1px solid rgba(23, 32, 51, 0.08);
  box-shadow: var(--shadow-md);
  display: grid;
  gap: 18px;
}

.brand-points {
  display: grid;
  gap: 14px;
}

.point-item {
  display: grid;
  gap: 4px;
  padding: 16px 18px;
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.68);
  border: 1px solid rgba(23, 32, 51, 0.06);
}

.point-item span {
  color: var(--gray-500);
  line-height: 1.7;
}

.password-row {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 10px;
}

.checkbox-row {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  color: var(--ink-soft);
  font-weight: var(--font-weight-semibold);
}

.checkbox-row input {
  width: 18px;
  height: 18px;
}

.login-btn {
  width: 100%;
}

@media (max-width: 980px) {
  .login-shell {
    grid-template-columns: 1fr;
  }
}
</style>
