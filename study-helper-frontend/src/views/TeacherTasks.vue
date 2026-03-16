<template>
  <div class="teacher-tasks-container">
    <h1 class="page-title">📋 任务发布</h1>
    
    <!-- 任务操作 -->
    <div class="task-actions">
      <router-link to="/teacher/task/create" class="btn-create-task">
        <i class="icon">➕</i> 发布任务
      </router-link>
    </div>
    
    <!-- 课程选择 -->
    <div class="course-selector">
      <label for="course-select">选择课程：</label>
      <select id="course-select" v-model="selectedCourseId" @change="loadTasks">
        <option value="">所有课程</option>
        <option v-for="course in courses" :key="course.id" :value="course.id">
          {{ course.name }}
        </option>
      </select>
    </div>
    
    <!-- 任务列表 -->
    <div class="tasks-table-container">
      <table class="tasks-table">
        <thead>
          <tr>
            <th>任务名称</th>
            <th>所属课程</th>
            <th>状态</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="task in tasks" :key="task.id">
            <td>{{ task.title }}</td>
            <td>{{ getCourseName(task.courseId) }}</td>
            <td>
              <span class="status-badge" :class="task.status.toLowerCase()">
                {{ getStatusLabel(task.status) }}
              </span>
            </td>
            <td>
              <div class="action-buttons">
                <button @click="editTask(task.id)" class="btn-edit">
                  <i class="icon">✏️</i> 编辑
                </button>
                <button @click="deleteTask(task.id)" class="btn-delete">
                  <i class="icon">🗑️</i> 删除
                </button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
    
    <!-- 空状态 -->
    <div v-if="tasks.length === 0 && !loading" class="empty-state">
      <div class="empty-icon">📋</div>
      <h3>还没有发布任务</h3>
      <p>点击上方的"发布任务"按钮开始创建您的第一个任务</p>
    </div>
    
    <!-- 加载状态 -->
    <div v-if="loading" class="loading">
      <div class="spinner"></div>
      <p>加载任务列表中...</p>
    </div>
    
    <!-- 错误提示 -->
    <div v-if="error" class="error-message">
      <i class="icon">⚠️</i>
      {{ error }}
    </div>
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
    console.error('获取任务列表失败:', err)
  } finally {
    loading.value = false
  }
}

const createTask = () => {
  router.push('/teacher/task/create')
}

const editTask = (taskId) => {
  router.push(`/teacher/task/edit/${taskId}`)
}

const deleteTask = async (taskId) => {
  if (!confirm('确定要删除这个任务吗？此操作不可撤销。')) {
    return
  }
  
  try {
    const response = await taskApi.deleteTask(taskId, userStore.user.id)
    if (response.data.code === 200) {
      loadTasks() // 重新加载任务列表
    } else {
      error.value = response.data.message
    }
  } catch (err) {
    error.value = '删除任务失败'
    console.error('删除任务失败:', err)
  }
}

const getCourseName = (courseId) => {
  const course = courses.value.find(c => c.id === courseId)
  return course ? course.name : '未知课程'
}

const getStatusLabel = (status) => {
  const statusMap = {
    'TODO': '未开始',
    'IN_PROGRESS': '进行中',
    'COMPLETED': '已完成'
  }
  return statusMap[status] || status
}

const formatDate = (dateString) => {
  if (!dateString) return '-'
  const d = new Date(dateString)
  if (isNaN(d.getTime())) return dateString
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${y}年${m}月${day}日`
}

onMounted(() => {
  loadCourses()
  loadTasks()
})
</script>

<style scoped>
.teacher-tasks-container {
  padding: 2rem 0;
}

.page-title {
  color: #333;
  margin-bottom: 2rem;
  font-size: 2rem;
  text-align: center;
}

.task-actions {
  margin-bottom: 2rem;
  text-align: right;
}

.btn-create-task {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  color: white;
  padding: 0.75rem 1.5rem;
  border: none;
  border-radius: 6px;
  font-size: 1rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  text-decoration: none;
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
}

.btn-create-task:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(240, 147, 251, 0.3);
}

.course-selector {
  margin-bottom: 2rem;
  display: flex;
  align-items: center;
  gap: 1rem;
}

.course-selector label {
  font-weight: 500;
  color: #333;
}

.course-selector select {
  padding: 0.5rem;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 1rem;
  min-width: 200px;
}

.tasks-table-container {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.08);
}

.tasks-table {
  width: 100%;
  border-collapse: collapse;
}

.tasks-table th {
  background: #f8f9fa;
  padding: 1rem;
  text-align: left;
  font-weight: 600;
  color: #555;
  border-bottom: 2px solid #e9ecef;
}

.tasks-table td {
  padding: 1rem;
  border-bottom: 1px solid #e9ecef;
}

.tasks-table tr:hover {
  background: #f8f9fa;
}

.status-badge {
  padding: 0.25rem 0.75rem;
  border-radius: 20px;
  font-size: 0.85rem;
  font-weight: 500;
}

.status-badge.pending {
  background: #fff3e0;
  color: #f57c00;
}

.status-badge.completed {
  background: #e8f5e8;
  color: #2e7d32;
}

.status-badge.expired {
  background: #ffebee;
  color: #c62828;
}

.action-buttons {
  display: flex;
  gap: 0.5rem;
}

.btn-edit, .btn-delete, .btn-details {
  padding: 0.25rem 0.75rem;
  border: none;
  border-radius: 6px;
  font-size: 0.85rem;
  cursor: pointer;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  gap: 0.25rem;
}

.btn-edit {
  background: #e3f2fd;
  color: #1976d2;
}

.btn-edit:hover {
  background: #bbdefb;
}

.btn-delete {
  background: #ffebee;
  color: #c62828;
}

.btn-delete:hover {
  background: #ffcdd2;
}

.btn-details {
  background: #f5f5f5;
  color: #333;
}

.btn-details:hover {
  background: #e0e0e0;
}

.empty-state {
  text-align: center;
  padding: 4rem 2rem;
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.08);
  margin-top: 2rem;
}

.empty-icon {
  font-size: 4rem;
  margin-bottom: 1rem;
}

.empty-state h3 {
  color: #333;
  margin-bottom: 0.5rem;
}

.empty-state p {
  color: #666;
  margin: 0;
}

.loading {
  text-align: center;
  padding: 3rem;
  color: #666;
}

.spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid #f093fb;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 1rem;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.error-message {
  background: #ffebee;
  color: #c62828;
  padding: 1rem;
  border-radius: 8px;
  text-align: center;
  margin: 2rem 0;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
}

.icon {
  font-size: 1rem;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .teacher-tasks-container {
    padding: 1rem 0;
  }
  
  .page-title {
    font-size: 1.5rem;
  }
  
  .course-selector {
    flex-direction: column;
    align-items: stretch;
  }
  
  .tasks-table {
    font-size: 0.9rem;
  }
  
  .tasks-table th,
  .tasks-table td {
    padding: 0.5rem;
  }
  
  .action-buttons {
    flex-direction: column;
    gap: 0.25rem;
  }
  
  .btn-edit, .btn-delete, .btn-details {
    width: 100%;
  }
}
</style>
