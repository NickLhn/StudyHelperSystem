<template>
  <div class="page-stack">
    <section class="page-intro">
      <div class="page-intro-copy">
        <span class="page-eyebrow">Quiz Studio</span>
        <h2 class="page-title">把测验从“列表条目”升级成“可运营的发布单元”</h2>
        <p class="page-subtitle">课程筛选、状态切换、题量与时长信息都进入统一卡片视图，更适合后续接自动批改和成绩分析。</p>
      </div>
      <div class="page-actions">
        <span class="chip">共 {{ quizzes.length }} 个测验</span>
        <router-link to="/teacher/quiz/create" class="edu-btn edu-btn-primary">创建测验</router-link>
      </div>
    </section>

    <section class="filter-card">
      <div class="toolbar-row">
        <label class="sr-only" for="teacher-quiz-course">按课程筛选</label>
        <select id="teacher-quiz-course" v-model="selectedCourseId" @change="loadQuizzes">
          <option value="">所有课程</option>
          <option v-for="course in courses" :key="course.id" :value="course.id">
            {{ course.name }}
          </option>
        </select>
      </div>
    </section>

    <section v-if="loading" class="loading-panel">
      <p class="loading-copy">测验列表加载中...</p>
    </section>

    <section v-else-if="quizzes.length === 0" class="empty-panel">
      <h3 class="empty-title">还没有创建测验</h3>
      <p class="empty-copy">先创建一场测验，后续就能继续接自动判分与成绩分析模块。</p>
    </section>

    <section v-else class="card-grid cards-3">
      <article v-for="quiz in quizzes" :key="quiz.id" class="info-card entity-card">
        <div class="entity-card-header">
          <div>
            <h3 class="entity-card-title">{{ quiz.title }}</h3>
            <p class="entity-card-subtitle">{{ getCourseName(quiz.courseId) }}</p>
          </div>
          <span class="status-pill" :class="quiz.status.toLowerCase()">{{ quiz.statusLabel }}</span>
        </div>

        <div class="data-points">
          <div class="data-point">
            <span>题目数量</span>
            <strong>{{ quiz.questionCount }} 题</strong>
          </div>
          <div class="data-point">
            <span>作答时长</span>
            <strong>{{ quiz.totalTime > 0 ? `${quiz.totalTime} 分钟` : '不限时' }}</strong>
          </div>
          <div class="data-point">
            <span>创建时间</span>
            <strong>{{ formatDate(quiz.createdAt) }}</strong>
          </div>
          <div class="data-point">
            <span>防作弊</span>
            <strong>{{ quiz.maxAttempts }} 次 · {{ quiz.shuffleQuestions ? '随机题序' : '固定题序' }}</strong>
          </div>
        </div>

        <div class="entity-card-actions">
          <button v-if="quiz.status === 'DRAFT'" type="button" class="edu-btn edu-btn-primary" @click="setQuizStatus(quiz.id, 'PUBLISHED')">
            发布
          </button>
          <button v-if="quiz.status === 'PUBLISHED'" type="button" class="edu-btn edu-btn-secondary" @click="setQuizStatus(quiz.id, 'CLOSED')">
            关闭
          </button>
          <router-link :to="`/quiz/${quiz.id}`" class="edu-btn edu-btn-ghost">查看</router-link>
          <button type="button" class="edu-btn edu-btn-danger" @click="deleteQuiz(quiz.id)">删除</button>
        </div>
      </article>
    </section>

    <section v-if="error" class="message-banner error">
      {{ error }}
    </section>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useUserStore } from '../stores/user'
import { courseApi } from '../api/course'
import { quizApi } from '../api/quiz'

const userStore = useUserStore()
const courses = ref([])
const quizzes = ref([])
const selectedCourseId = ref('')
const loading = ref(false)
const error = ref('')

const loadCourses = async () => {
  try {
    const response = await courseApi.getUserCourses(userStore.user.id)
    if (response.data.code === 200) {
      courses.value = response.data.data
    }
  } catch (err) {
    console.error('获取课程列表失败:', err)
  }
}

const loadQuizzes = async () => {
  loading.value = true
  error.value = ''

  try {
    const response = selectedCourseId.value
      ? await quizApi.getCourseQuizzes(userStore.user.id, selectedCourseId.value)
      : await quizApi.getUserQuizzes(userStore.user.id)
    if (response.data.code === 200) {
      quizzes.value = response.data.data
    } else {
      error.value = response.data.message
    }
  } catch (err) {
    error.value = '获取测验列表失败，请确认后端服务与数据库已启动'
  } finally {
    loading.value = false
  }
}

const deleteQuiz = async (quizId) => {
  if (!confirm('确定要删除这个测验吗？此操作不可撤销。')) return

  try {
    const response = await quizApi.deleteQuiz(quizId, userStore.user.id)
    if (response.data.code === 200) {
      loadQuizzes()
    } else {
      error.value = response.data.message
    }
  } catch (err) {
    error.value = '删除测验失败'
  }
}

const setQuizStatus = async (quizId, status) => {
  try {
    const response = await quizApi.updateQuizStatus(quizId, userStore.user.id, status)
    if (response.data.code === 200) {
      loadQuizzes()
    } else {
      error.value = response.data.message
    }
  } catch (e) {
    error.value = '更新测验状态失败'
  }
}

const getCourseName = (courseId) => {
  const course = courses.value.find((c) => c.id === courseId)
  return course ? course.name : '未关联课程'
}

const formatDate = (dateString) => {
  if (!dateString) return '未记录'
  const d = new Date(dateString)
  if (Number.isNaN(d.getTime())) return dateString
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

onMounted(() => {
  loadCourses()
  loadQuizzes()
})
</script>
