<template>
  <div class="page-stack">
    <section class="page-intro form-shell">
      <div class="page-intro-copy">
        <span class="page-eyebrow">Task Editor</span>
        <h2 class="page-title">{{ isEdit ? '编辑任务' : '发布新任务' }}</h2>
        <p class="page-subtitle">把任务描述、计划时间、课程关联和提醒设置放到同一张干净的工作表里，方便后续扩展成正式作业流。</p>
      </div>
    </section>

    <main class="form-shell">
      <section class="info-card form-panel">
        <form class="form-grid" @submit.prevent="handleSubmit">
          <div class="field">
            <label>任务标题</label>
            <input v-model="form.title" type="text" placeholder="请输入任务标题" required />
          </div>

          <div class="field">
            <label>任务描述</label>
            <textarea v-model="form.description" rows="4" placeholder="请输入任务详细描述"></textarea>
          </div>

          <div class="form-grid two-up">
            <div class="field">
              <label>状态</label>
              <select v-model="form.status">
                <option v-for="status in statuses" :key="status.value" :value="status.value">
                  {{ status.label }}
                </option>
              </select>
            </div>
            <div class="field">
              <label>优先级</label>
              <select v-model="form.priority">
                <option v-for="priority in priorities" :key="priority.value" :value="priority.value">
                  {{ priority.label }}
                </option>
              </select>
            </div>
          </div>

          <div class="form-grid two-up">
            <div class="field">
              <label>计划日期</label>
              <div class="date-field">
                <input type="text" :value="planDateText" placeholder="年/月/日" readonly />
                <button class="edu-btn edu-btn-secondary" type="button" @click="openPlanDatePicker">选择日期</button>
                <input ref="planDateInput" class="native-date" type="date" v-model="form.planDate" />
              </div>
            </div>
            <div class="field">
              <label>关联课程</label>
              <select v-model="form.courseId">
                <option value="">不关联课程</option>
                <option v-for="course in courses" :key="course.id" :value="course.id">
                  {{ course.name }}
                </option>
              </select>
            </div>
          </div>

          <div class="field">
            <label class="checkbox-row">
              <input type="checkbox" v-model="form.reminderEnabled" />
              <span>开启提醒</span>
            </label>
            <input v-if="form.reminderEnabled" type="time" v-model="form.reminderTime" class="time-input" />
          </div>

          <div v-if="error" class="message-banner error">{{ error }}</div>
          <div v-if="success" class="message-banner success">{{ success }}</div>

          <div class="toolbar-row">
            <button type="button" class="edu-btn edu-btn-secondary" @click="goBack">取消</button>
            <button type="submit" class="edu-btn edu-btn-primary" :disabled="loading">
              {{ loading ? '保存中...' : (isEdit ? '保存修改' : '创建任务') }}
            </button>
          </div>
        </form>
      </section>
    </main>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '../stores/user'
import { taskApi } from '../api/task'
import { courseApi } from '../api/course'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const isEdit = computed(() => !!route.params.id)
const taskId = computed(() => route.params.id)

const form = reactive({
  title: '',
  description: '',
  status: 'TODO',
  priority: 'MEDIUM',
  planDate: '',
  reminderTime: '',
  reminderEnabled: false,
  courseId: ''
})

const courses = ref([])
const error = ref('')
const success = ref('')
const loading = ref(false)
const planDateInput = ref(null)

const planDateText = computed(() => {
  if (!form.planDate) return ''
  const parts = String(form.planDate).split('-')
  if (parts.length !== 3) return form.planDate
  const [y, m, d] = parts
  return `${y}年${m}月${d}日`
})

const statuses = [
  { value: 'TODO', label: '未开始' },
  { value: 'IN_PROGRESS', label: '进行中' },
  { value: 'COMPLETED', label: '已完成' }
]

const priorities = [
  { value: 'HIGH', label: '高' },
  { value: 'MEDIUM', label: '中' },
  { value: 'LOW', label: '低' }
]

const fetchCourses = async () => {
  try {
    const response = await courseApi.getUserCourses(userStore.user.id)
    if (response.data.code === 200) {
      courses.value = response.data.data
    }
  } catch (err) {
    console.error('获取课程失败:', err)
  }
}

const fetchTask = async () => {
  if (!isEdit.value) return

  try {
    const response = await taskApi.getTaskById(taskId.value, userStore.user.id)
    if (response.data.code === 200) {
      const task = response.data.data
      Object.assign(form, task)
      if (task.courseId) form.courseId = task.courseId
    } else {
      error.value = '获取任务信息失败'
    }
  } catch (err) {
    error.value = '获取任务信息失败'
  }
}

const handleSubmit = async () => {
  error.value = ''
  success.value = ''
  loading.value = true

  const submitData = { ...form }
  if (!submitData.courseId) delete submitData.courseId

  try {
    const response = isEdit.value
      ? await taskApi.updateTask(taskId.value, userStore.user.id, submitData)
      : await taskApi.createTask(userStore.user.id, submitData)

    if (response.data.code === 200) {
      success.value = isEdit.value ? '任务更新成功！' : '任务创建成功！'
      setTimeout(() => {
        router.push(route.path.startsWith('/teacher/') ? '/teacher/tasks' : '/tasks')
      }, 900)
    } else {
      error.value = response.data.message
    }
  } catch (err) {
    error.value = isEdit.value ? '更新失败，请检查网络连接' : '创建失败，请检查网络连接'
  } finally {
    loading.value = false
  }
}

const goBack = () => {
  router.push(route.path.startsWith('/teacher/') ? '/teacher/tasks' : '/tasks')
}

const openPlanDatePicker = () => {
  const el = planDateInput.value
  if (!el) return
  if (typeof el.showPicker === 'function') {
    el.showPicker()
  } else {
    el.focus()
    el.click()
  }
}

onMounted(() => {
  fetchCourses()
  fetchTask()
  if (!route.params.id && typeof route.query.courseId === 'string' && route.query.courseId) {
    form.courseId = route.query.courseId
  }
})
</script>

<style scoped>
.date-field {
  position: relative;
  display: grid;
  gap: 10px;
}

.native-date {
  position: absolute;
  inset: auto 0 0 auto;
  opacity: 0;
  pointer-events: none;
  width: 1px;
  height: 1px;
}

.checkbox-row {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  font-weight: var(--font-weight-bold);
  color: var(--ink-soft);
}

.checkbox-row input {
  width: 18px;
  height: 18px;
}

.time-input {
  max-width: 220px;
}
</style>
