<template>
  <div class="page-stack">
    <section class="page-intro">
      <div class="page-intro-copy">
        <span class="page-eyebrow">Task Board</span>
        <h2 class="page-title">任务页终于像一块可管理的教学看板</h2>
        <p class="page-subtitle">按课程筛选、按状态查看、按优先级识别重点任务，让发布和追踪任务不再像一张纯表格。</p>
      </div>
      <div class="page-actions">
        <span class="chip">共 {{ tasks.length }} 个任务</span>
        <router-link to="/teacher/task/create" class="edu-btn edu-btn-primary">发布任务</router-link>
      </div>
    </section>

    <section class="filter-card">
      <div class="toolbar-row">
        <label class="sr-only" for="teacher-task-course">按课程筛选</label>
        <select id="teacher-task-course" v-model="selectedCourseId" @change="loadTasks">
          <option value="">所有课程</option>
          <option v-for="course in courses" :key="course.id" :value="course.id">
            {{ course.name }}
          </option>
        </select>
      </div>
    </section>

    <section v-if="loading" class="loading-panel">
      <p class="loading-copy">任务列表加载中...</p>
    </section>

    <section v-else-if="tasks.length === 0" class="empty-panel">
      <h3 class="empty-title">还没有发布任务</h3>
      <p class="empty-copy">先创建一项任务，后续就能围绕课程逐步扩展成完整作业流程。</p>
    </section>

    <section v-else class="card-grid cards-3">
      <article v-for="task in tasks" :key="task.id" class="info-card entity-card">
        <div class="entity-card-header">
          <div>
            <h3 class="entity-card-title">{{ task.title }}</h3>
            <p class="entity-card-subtitle">{{ getCourseName(task.courseId) }}</p>
          </div>
          <span class="status-pill" :class="task.status.toLowerCase()">
            {{ getStatusLabel(task.status) }}
          </span>
        </div>

        <p v-if="task.description" class="task-description">{{ task.description }}</p>

        <div class="data-points">
          <div class="data-point">
            <span>优先级</span>
            <strong>{{ getPriorityLabel(task.priority) }}</strong>
          </div>
          <div class="data-point">
            <span>计划日期</span>
            <strong>{{ formatDate(task.planDate) }}</strong>
          </div>
          <div class="data-point">
            <span>提醒设置</span>
            <strong>{{ task.reminderEnabled ? (task.reminderTime || '已开启') : '未开启' }}</strong>
          </div>
        </div>

        <div class="entity-card-actions">
          <button type="button" class="edu-btn edu-btn-secondary" @click="editTask(task.id)">编辑</button>
          <button type="button" class="edu-btn edu-btn-danger" @click="deleteTask(task.id)">删除</button>
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
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { courseApi } from '../api/course'
import { taskApi } from '../api/task'

const router = useRouter()
const userStore = useUserStore()
const courses = ref([])
const tasks = ref([])
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

const loadTasks = async () => {
  loading.value = true
  error.value = ''

  try {
    const response = selectedCourseId.value
      ? await taskApi.getCourseTasks(userStore.user.id, selectedCourseId.value)
      : await taskApi.getUserTasks(userStore.user.id)

    if (response.data.code === 200) {
      tasks.value = response.data.data
    } else {
      error.value = response.data.message
    }
  } catch (err) {
    error.value = '获取任务列表失败，请确认后端服务与数据库已启动'
  } finally {
    loading.value = false
  }
}

const editTask = (taskId) => {
  router.push(`/teacher/task/edit/${taskId}`)
}

const deleteTask = async (taskId) => {
  if (!confirm('确定要删除这个任务吗？此操作不可撤销。')) return

  try {
    const response = await taskApi.deleteTask(taskId, userStore.user.id)
    if (response.data.code === 200) {
      loadTasks()
    } else {
      error.value = response.data.message
    }
  } catch (err) {
    error.value = '删除任务失败'
  }
}

const getCourseName = (courseId) => {
  const course = courses.value.find((c) => c.id === courseId)
  return course ? course.name : '未关联课程'
}

const getStatusLabel = (status) => {
  const statusMap = {
    TODO: '未开始',
    IN_PROGRESS: '进行中',
    COMPLETED: '已完成'
  }
  return statusMap[status] || status
}

const getPriorityLabel = (priority) => {
  const map = {
    HIGH: '高',
    MEDIUM: '中',
    LOW: '低'
  }
  return map[priority] || priority
}

const formatDate = (dateString) => {
  if (!dateString) return '未设置'
  const d = new Date(dateString)
  if (Number.isNaN(d.getTime())) return dateString
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

onMounted(() => {
  loadCourses()
  loadTasks()
})
</script>

<style scoped>
.task-description {
  color: var(--gray-700);
  line-height: 1.7;
}
</style>
