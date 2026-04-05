<template>
  <div class="page-stack">
    <section class="page-intro">
      <div class="page-intro-copy">
        <span class="page-eyebrow">Homework Studio</span>
        <h2 class="page-title">让作业发布、自动批改和提交查看进入同一条工作流</h2>
        <p class="page-subtitle">现在教师可以正式发布作业，而不再把作业和个人任务混在一起。</p>
      </div>
      <div class="page-actions">
        <span class="chip">共 {{ homeworks.length }} 份作业</span>
        <router-link to="/teacher/homework/create" class="edu-btn edu-btn-primary">创建作业</router-link>
      </div>
    </section>

    <section class="filter-card">
      <div class="toolbar-row">
        <select v-model="selectedCourseId" @change="loadHomeworks">
          <option value="">所有课程</option>
          <option v-for="course in courses" :key="course.id" :value="String(course.id)">
            {{ course.name }}
          </option>
        </select>
      </div>
    </section>

    <section v-if="loading" class="loading-panel">
      <p class="loading-copy">作业列表加载中...</p>
    </section>

    <section v-else-if="homeworks.length === 0" class="empty-panel">
      <h3 class="empty-title">还没有发布作业</h3>
      <p class="empty-copy">先创建第一份作业，后面就能接学生提交、自动判分和教师复核。</p>
    </section>

    <section v-else class="card-grid cards-3">
      <article v-for="homework in homeworks" :key="homework.id" class="info-card entity-card">
        <div class="entity-card-header">
          <div>
            <h3 class="entity-card-title">{{ homework.title }}</h3>
            <p class="entity-card-subtitle">{{ homework.courseName }}</p>
          </div>
          <span class="status-pill" :class="homework.status.toLowerCase()">{{ homework.statusLabel }}</span>
        </div>

        <p v-if="homework.description" class="entity-card-copy">{{ homework.description }}</p>

        <div class="data-points">
          <div class="data-point">
            <span>题目数</span>
            <strong>{{ homework.questionCount }} 题</strong>
          </div>
          <div class="data-point">
            <span>总分</span>
            <strong>{{ homework.totalScore }} 分</strong>
          </div>
          <div class="data-point">
            <span>截止时间</span>
            <strong>{{ formatDateTime(homework.deadlineAt) }}</strong>
          </div>
        </div>

        <div class="entity-card-actions">
          <button
            v-if="homework.status === 'DRAFT'"
            type="button"
            class="edu-btn edu-btn-primary"
            @click="setStatus(homework.id, 'PUBLISHED')"
          >
            发布
          </button>
          <button
            v-if="homework.status === 'PUBLISHED'"
            type="button"
            class="edu-btn edu-btn-secondary"
            @click="setStatus(homework.id, 'CLOSED')"
          >
            关闭
          </button>
          <router-link :to="`/teacher/homework/${homework.id}`" class="edu-btn edu-btn-ghost">查看</router-link>
          <button type="button" class="edu-btn edu-btn-danger" @click="removeHomework(homework.id)">删除</button>
        </div>
      </article>
    </section>

    <section v-if="error" class="message-banner error">{{ error }}</section>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useUserStore } from '../stores/user'
import { courseApi } from '../api/course'
import { homeworkApi } from '../api/homework'

const userStore = useUserStore()
const courses = ref([])
const homeworks = ref([])
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
    error.value = '获取课程列表失败'
  }
}

const loadHomeworks = async () => {
  loading.value = true
  error.value = ''
  try {
    const response = await homeworkApi.getTeacherHomeworks(userStore.user.id, selectedCourseId.value || undefined)
    if (response.data.code === 200) {
      homeworks.value = response.data.data
    } else {
      error.value = response.data.message
    }
  } catch (err) {
    error.value = '获取作业列表失败，请确认后端服务是否已启动'
  } finally {
    loading.value = false
  }
}

const setStatus = async (homeworkId, status) => {
  try {
    const response = await homeworkApi.updateHomeworkStatus(homeworkId, userStore.user.id, status)
    if (response.data.code === 200) {
      await loadHomeworks()
    } else {
      error.value = response.data.message
    }
  } catch (err) {
    error.value = '更新作业状态失败'
  }
}

const removeHomework = async (homeworkId) => {
  if (!window.confirm('确定要删除这份作业吗？已提交记录也会一并删除。')) return
  try {
    const response = await homeworkApi.deleteHomework(homeworkId, userStore.user.id)
    if (response.data.code === 200) {
      await loadHomeworks()
    } else {
      error.value = response.data.message
    }
  } catch (err) {
    error.value = '删除作业失败'
  }
}

const formatDateTime = (value) => {
  if (!value) return '未设置'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

onMounted(async () => {
  await loadCourses()
  await loadHomeworks()
})
</script>
