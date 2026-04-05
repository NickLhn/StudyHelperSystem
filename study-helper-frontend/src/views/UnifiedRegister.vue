<template>
  <div class="register-page">
    <div class="page-container register-shell">
      <section class="register-story">
        <span class="page-eyebrow">Get Started</span>
        <h1 class="page-title">先选对身份，再进入你的工作区</h1>
        <p class="page-subtitle">
          注册页现在和首页、登录页保持同一套视觉语言。用户不会再从落地页跳进一个风格完全不同的旧表单。
        </p>

        <div class="story-cards">
          <article class="story-card">
            <strong>学生注册后</strong>
            <span>通过邀请码加入课程，进入资料中心和学习统计。</span>
          </article>
          <article class="story-card">
            <strong>教师注册后</strong>
            <span>创建课程、任务、测验和资料，后续继续接自动批改。</span>
          </article>
          <article class="story-card">
            <strong>统一入口</strong>
            <span>同一张注册表根据身份切换字段，减少理解成本。</span>
          </article>
        </div>
      </section>

      <section class="info-card register-card">
        <div class="stack-md">
          <div>
            <span class="page-eyebrow">Register</span>
            <h2 class="section-title">创建账户</h2>
            <p class="section-copy">选择你的身份，然后填写对应的信息。</p>
          </div>

          <div class="role-grid">
            <button
              type="button"
              class="role-card"
              :class="{ active: form.role === 'STUDENT' }"
              @click="selectRole('STUDENT')"
            >
              <strong>学生</strong>
              <span>参与课程学习与资料使用</span>
            </button>
            <button
              type="button"
              class="role-card"
              :class="{ active: form.role === 'TEACHER' }"
              @click="selectRole('TEACHER')"
            >
              <strong>教师</strong>
              <span>创建课程、管理测验和教学内容</span>
            </button>
          </div>

          <form class="form-grid" @submit.prevent="handleRegister">
            <div class="form-grid two-up">
              <div class="field">
                <label>用户名</label>
                <input
                  v-model="form.username"
                  type="text"
                  placeholder="请输入用户名"
                  required
                  minlength="2"
                />
              </div>
              <div class="field">
                <label>邮箱</label>
                <input v-model="form.email" type="email" placeholder="可选：输入邮箱地址" />
              </div>
            </div>

            <div class="field">
              <label>密码</label>
              <div class="password-row">
                <input
                  v-model="form.password"
                  :type="showPassword ? 'text' : 'password'"
                  placeholder="请输入至少 6 位密码"
                  required
                  minlength="6"
                />
                <button type="button" class="edu-btn edu-btn-secondary" @click="showPassword = !showPassword">
                  {{ showPassword ? '隐藏' : '显示' }}
                </button>
              </div>
            </div>

            <template v-if="form.role === 'TEACHER'">
              <div class="form-grid two-up">
                <div class="field">
                  <label>姓名</label>
                  <input v-model="form.nickname" type="text" placeholder="请输入真实姓名" required />
                </div>
                <div class="field">
                  <label>邀请码</label>
                  <input v-model="form.invitationCode" type="text" placeholder="请输入邀请码" required />
                  <div class="field-help">邀请码请联系管理员获取。</div>
                </div>
              </div>

              <div class="form-grid two-up">
                <div class="field">
                  <label>教师职称</label>
                  <select v-model="form.teacherTitle" required>
                    <option value="">请选择职称</option>
                    <option value="助教">助教</option>
                    <option value="讲师">讲师</option>
                    <option value="副教授">副教授</option>
                    <option value="教授">教授</option>
                    <option value="研究员">研究员</option>
                    <option value="副研究员">副研究员</option>
                  </select>
                </div>
                <div class="field">
                  <label>所在院系</label>
                  <select v-model="form.department" required>
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
            </template>

            <template v-else>
              <div class="form-grid two-up">
                <div class="field">
                  <label>学号</label>
                  <input v-model="form.studentId" type="text" placeholder="请输入学号" required />
                </div>
                <div class="field">
                  <label>专业</label>
                  <input v-model="form.major" type="text" placeholder="请输入专业" required />
                </div>
              </div>

              <div class="field">
                <label>年级</label>
                <input v-model="form.grade" type="text" placeholder="如：2024" required />
              </div>
            </template>

            <div v-if="error" class="message-banner error">{{ error }}</div>

            <button type="submit" class="edu-btn edu-btn-primary register-btn" :disabled="loading">
              {{ loading ? '注册中...' : '立即注册' }}
            </button>
          </form>

          <p class="section-copy">
            已有账号？
            <router-link to="/login">立即登录</router-link>
          </p>
        </div>
      </section>
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
  display: flex;
  align-items: center;
  padding: clamp(20px, 4vw, 42px);
}

.register-shell {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(360px, 560px);
  gap: 28px;
  align-items: stretch;
}

.register-story {
  padding: clamp(28px, 4vw, 48px);
  border-radius: 34px;
  background:
    radial-gradient(circle at top left, rgba(111, 152, 255, 0.24), transparent 36%),
    radial-gradient(circle at bottom right, rgba(15, 139, 132, 0.14), transparent 34%),
    linear-gradient(160deg, rgba(255, 255, 255, 0.95), rgba(246, 241, 231, 0.88));
  border: 1px solid rgba(23, 32, 51, 0.08);
  box-shadow: var(--shadow-md);
  display: grid;
  gap: 20px;
}

.story-cards {
  display: grid;
  gap: 14px;
}

.story-card {
  display: grid;
  gap: 4px;
  padding: 18px 20px;
  border-radius: 22px;
  background: rgba(255, 255, 255, 0.72);
  border: 1px solid rgba(23, 32, 51, 0.06);
}

.story-card span {
  color: var(--gray-500);
  line-height: 1.75;
}

.register-card {
  padding: clamp(24px, 4vw, 36px);
}

.role-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.role-card {
  display: grid;
  gap: 4px;
  padding: 16px 18px;
  border-radius: 22px;
  border: 1px solid rgba(23, 32, 51, 0.10);
  background: rgba(255, 255, 255, 0.72);
  color: var(--ink);
  text-align: left;
  cursor: pointer;
  transition: transform 180ms ease, border-color 180ms ease, background 180ms ease;
}

.role-card.active {
  background: rgba(44, 96, 214, 0.10);
  border-color: rgba(44, 96, 214, 0.26);
}

.role-card:hover {
  transform: translateY(-1px);
}

.role-card span {
  color: var(--gray-500);
  line-height: 1.65;
}

.password-row {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 10px;
}

.register-btn {
  width: 100%;
}

@media (max-width: 980px) {
  .register-shell {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 640px) {
  .role-grid {
    grid-template-columns: 1fr;
  }
}
</style>
