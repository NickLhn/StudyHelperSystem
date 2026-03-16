<template>
  <div class="task-form-container">
    <main class="form-content">
      <div class="form-card">
        <h2>{{ isEdit ? '编辑任务' : '新建任务' }}</h2>
        
        <form @submit.prevent="handleSubmit">
          <div class="form-group">
            <label>任务标题 *</label>
            <input 
              v-model="form.title" 
              type="text" 
              placeholder="请输入任务标题（如：复习高等数学第3章）"
              required
            />
          </div>

          <div class="form-group">
            <label>任务描述</label>
            <textarea 
              v-model="form.description" 
              rows="3"
              placeholder="请输入任务详细描述..."
            ></textarea>
          </div>

          <div class="form-row">
            <div class="form-group">
              <label>状态</label>
              <select v-model="form.status">
                <option v-for="status in statuses" :key="status.value" :value="status.value">
                  {{ status.label }}
                </option>
              </select>
            </div>

            <div class="form-group">
              <label>优先级</label>
              <select v-model="form.priority">
                <option v-for="priority in priorities" :key="priority.value" :value="priority.value">
                  {{ priority.label }}
                </option>
              </select>
            </div>
          </div>

          <div class="form-row">
            <div class="form-group">
              <label>计划日期</label>
              <div class="date-field">
                <div class="date-wrap">
                  <input
                    class="date-display"
                    type="text"
                    :value="planDateText"
                    placeholder="年/月/日"
                    readonly
                  />
                  <input ref="planDateInput" class="date-native" type="date" v-model="form.planDate" />
                </div>
                <button class="date-btn" type="button" @click="openPlanDatePicker">📅</button>
              </div>
            </div>

            <div class="form-group">
              <label>关联课程</label>
              <select v-model="form.courseId">
                <option value="">不关联课程</option>
                <option v-for="course in courses" :key="course.id" :value="course.id">
                  {{ course.name }}
                </option>
              </select>
            </div>
          </div>

          <div class="form-group reminder-section">
            <label class="checkbox-label">
              <input type="checkbox" v-model="form.reminderEnabled" />
              开启提醒
            </label>
            <input 
              v-if="form.reminderEnabled"
              type="time" 
              v-model="form.reminderTime"
              class="time-input"
            />
          </div>

          <div v-if="error" class="error-message">{{ error }}</div>
          <div v-if="success" class="success-message">{{ success }}</div>

          <div class="form-actions">
            <button type="button" class="btn-cancel" @click="goBack">
              取消
            </button>
            <button type="submit" class="btn-submit" :disabled="loading">
              {{ loading ? '保存中...' : (isEdit ? '保存修改' : '创建任务') }}
            </button>
          </div>
        </form>
      </div>
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
      if (task.courseId) {
        form.courseId = task.courseId
      }
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
  if (!submitData.courseId) {
    delete submitData.courseId
  }

  try {
    let response
    if (isEdit.value) {
      response = await taskApi.updateTask(taskId.value, userStore.user.id, submitData)
    } else {
      response = await taskApi.createTask(userStore.user.id, submitData)
    }

    if (response.data.code === 200) {
      success.value = isEdit.value ? '任务更新成功！' : '任务创建成功！'
      setTimeout(() => {
        // 检查当前路由是否在教师端
        if (route.path.startsWith('/teacher/')) {
          router.push('/teacher/tasks')
        } else {
          router.push('/tasks')
        }
      }, 1500)
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
  // 检查当前路由是否在教师端
  if (route.path.startsWith('/teacher/')) {
    router.push('/teacher/tasks')
  } else {
    router.push('/tasks')
  }
}

const handleLogout = () => {
  userStore.logout()
  router.push('/login')
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
.task-form-container {
  min-height: 100vh;
  background-color: #f5f5f5;
}

.navbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem 2rem;
  background-color: #42b883;
  color: white;
}

.nav-brand {
  font-size: 1.5rem;
  font-weight: bold;
}

.nav-links {
  display: flex;
  gap: 1rem;
  align-items: center;
}

.nav-link {
  color: white;
  text-decoration: none;
  padding: 0.5rem 1rem;
  border-radius: 4px;
  transition: background-color 0.3s;
}

.nav-link:hover {
  background-color: rgba(255, 255, 255, 0.2);
}

.btn-logout {
  background-color: transparent;
  border: 1px solid white;
  color: white;
  padding: 0.5rem 1rem;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
}

.btn-logout:hover {
  background-color: white;
  color: #42b883;
}

.form-content {
  max-width: 600px;
  margin: 2rem auto;
  padding: 0 1rem;
}

.form-card {
  background: white;
  border-radius: 12px;
  padding: 2rem;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.form-card h2 {
  color: #333;
  margin-bottom: 1.5rem;
  text-align: center;
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

.form-group input,
.form-group select,
.form-group textarea {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 1rem;
  transition: border-color 0.3s;
  font-family: inherit;
}

.form-group input:focus,
.form-group select:focus,
.form-group textarea:focus {
  outline: none;
  border-color: #667eea;
}

.form-group textarea {
  resize: vertical;
}

.date-field {
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 0.5rem;
  align-items: center;
}

.date-wrap {
  position: relative;
}

.date-btn {
  height: 44px;
  width: 44px;
  border: 1px solid #ddd;
  border-radius: 6px;
  background: #fff;
  cursor: pointer;
  font-size: 18px;
}

.date-display {
  pointer-events: none;
}

.date-native {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  opacity: 0;
  cursor: pointer;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1rem;
}

.reminder-section {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.checkbox-label {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  cursor: pointer;
}

.checkbox-label input {
  width: auto;
}

.time-input {
  width: 150px !important;
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

.form-actions {
  display: flex;
  gap: 1rem;
  margin-top: 1.5rem;
}

.btn-cancel,
.btn-submit {
  flex: 1;
  padding: 0.875rem;
  border-radius: 6px;
  font-size: 1rem;
  cursor: pointer;
  transition: all 0.3s;
  border: none;
}

.btn-cancel {
  background: #f0f0f0;
  color: #666;
}

.btn-cancel:hover {
  background: #e0e0e0;
}

.btn-submit {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.btn-submit:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 5px 20px rgba(102, 126, 234, 0.4);
}

.btn-submit:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

@media (max-width: 600px) {
  .form-row {
    grid-template-columns: 1fr;
  }
  
  .reminder-section {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
