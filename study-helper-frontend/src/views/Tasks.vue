<template>
  <div class="tasks-page learning-environment">
    <main class="page-container">
      <div class="page-header">
        <h1 class="page-title page-title-left">学习计划</h1>
        <router-link to="/task/create" class="edu-btn edu-btn-primary">新建任务</router-link>
      </div>

      <!-- 统计卡片 -->
      <div class="stats-cards">
        <div class="stat-card edu-card todo">
          <div class="stat-number">{{ stats.todo }}</div>
          <div class="stat-label">未开始</div>
        </div>
        <div class="stat-card edu-card in-progress">
          <div class="stat-number">{{ stats.inProgress }}</div>
          <div class="stat-label">进行中</div>
        </div>
        <div class="stat-card edu-card completed">
          <div class="stat-number">{{ stats.completed }}</div>
          <div class="stat-label">已完成</div>
        </div>
      </div>

      <!-- 筛选栏 -->
      <div class="filter-bar">
        <div class="filter-group">
          <span>状态：</span>
          <button 
            v-for="filter in statusFilters" 
            :key="filter.value"
            :class="['filter-btn edu-btn', { active: selectedStatus === filter.value }]"
            @click="filterByStatus(filter.value)"
          >
            {{ filter.label }}
          </button>
        </div>
        <div class="filter-group">
          <span>优先级：</span>
          <button 
            v-for="filter in priorityFilters" 
            :key="filter.value"
            :class="['filter-btn edu-btn', { active: selectedPriority === filter.value }]"
            @click="filterByPriority(filter.value)"
          >
            {{ filter.label }}
          </button>
        </div>
      </div>

      <!-- 日期筛选 -->
      <div class="date-filter">
        <label>计划日期：</label>
        <input class="edu-input date-input" type="date" v-model="selectedDate" @change="filterByDate" />
        <button class="edu-btn edu-btn-secondary btn-clear" @click="clearDateFilter" v-if="selectedDate">清除</button>
      </div>

      <div v-if="loading" class="loading">加载中...</div>
      
      <div v-else-if="filteredTasks.length === 0" class="empty-state">
        <p>暂无任务</p>
        <router-link to="/task/create" class="edu-btn edu-btn-primary">创建第一个任务</router-link>
      </div>

      <!-- 看板视图 -->
      <div v-else class="kanban-board">
        <div class="kanban-column" v-for="column in columns" :key="column.status">
          <div class="column-header" :class="column.status">
            <h3>{{ column.title }}</h3>
            <span class="task-count">{{ getTasksByStatus(column.status).length }}</span>
          </div>
          <div class="column-content">
            <div 
              v-for="task in getTasksByStatus(column.status)" 
              :key="task.id" 
              class="task-card edu-card"
              :class="task.priority.toLowerCase()"
            >
              <div class="task-header">
                <h4 class="task-title">{{ task.title }}</h4>
                <span class="priority-badge" :class="task.priority.toLowerCase()">
                  {{ task.priorityLabel }}
                </span>
              </div>
              
              <p v-if="task.description" class="task-desc">{{ task.description }}</p>
              
              <div class="task-meta">
                <span v-if="task.planDate" class="meta-item">
                  📅 {{ formatDate(task.planDate) }}
                </span>
                <span v-if="task.courseName" class="meta-item">
                  📚 {{ task.courseName }}
                </span>
                <span v-if="task.reminderEnabled" class="meta-item reminder">
                  ⏰ {{ task.reminderTime }}
                </span>
              </div>

              <div class="task-actions">
                <button 
                  v-if="task.status !== 'COMPLETED'" 
                  @click="updateStatus(task.id, getNextStatus(task.status))"
                  class="edu-btn edu-btn-success"
                >
                  {{ getNextStatusLabel(task.status) }}
                </button>
                <router-link :to="`/task/edit/${task.id}`" class="edu-btn edu-btn-secondary">编辑</router-link>
                <button @click="deleteTask(task.id)" class="edu-btn edu-btn-danger">删除</button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </main>

    <!-- 提醒弹窗 -->
    <div v-if="showReminder" class="reminder-modal">
      <div class="reminder-content">
        <h3>⏰ 任务提醒</h3>
        <div v-for="task in todayReminders" :key="task.id" class="reminder-item">
          <p class="reminder-title">{{ task.title }}</p>
          <p class="reminder-time">计划时间：{{ task.reminderTime }}</p>
        </div>
        <button @click="showReminder = false" class="edu-btn edu-btn-primary btn-close">知道了</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { taskApi } from '../api/task'

const router = useRouter()
const userStore = useUserStore()

const tasks = ref([])
const loading = ref(false)
const selectedStatus = ref('ALL')
const selectedPriority = ref('ALL')
const selectedDate = ref('')
const stats = reactive({ todo: 0, inProgress: 0, completed: 0 })
const todayReminders = ref([])
const showReminder = ref(false)

let reminderInterval = null

const statusFilters = [
  { value: 'ALL', label: '全部' },
  { value: 'TODO', label: '未开始' },
  { value: 'IN_PROGRESS', label: '进行中' },
  { value: 'COMPLETED', label: '已完成' }
]

const priorityFilters = [
  { value: 'ALL', label: '全部' },
  { value: 'HIGH', label: '高' },
  { value: 'MEDIUM', label: '中' },
  { value: 'LOW', label: '低' }
]

const columns = [
  { status: 'TODO', title: '未开始' },
  { status: 'IN_PROGRESS', title: '进行中' },
  { status: 'COMPLETED', title: '已完成' }
]

const filteredTasks = computed(() => {
  let result = tasks.value
  
  if (selectedStatus.value !== 'ALL') {
    result = result.filter(t => t.status === selectedStatus.value)
  }
  
  if (selectedPriority.value !== 'ALL') {
    result = result.filter(t => t.priority === selectedPriority.value)
  }
  
  if (selectedDate.value) {
    result = result.filter(t => t.planDate === selectedDate.value)
  }
  
  return result
})

const getTasksByStatus = (status) => {
  return filteredTasks.value.filter(t => t.status === status)
}

const getNextStatus = (currentStatus) => {
  const statusFlow = { 'TODO': 'IN_PROGRESS', 'IN_PROGRESS': 'COMPLETED' }
  return statusFlow[currentStatus]
}

const getNextStatusLabel = (currentStatus) => {
  const labels = { 'TODO': '开始', 'IN_PROGRESS': '完成' }
  return labels[currentStatus]
}

const fetchTasks = async () => {
  if (!userStore.user) return
  
  loading.value = true
  try {
    const response = await taskApi.getUserTasks(userStore.user.id)
    if (response.data.code === 200) {
      tasks.value = response.data.data
    }
  } catch (error) {
    console.error('获取任务失败:', error)
  } finally {
    loading.value = false
  }
}

const fetchStats = async () => {
  if (!userStore.user) return
  
  try {
    const response = await taskApi.getTaskStatistics(userStore.user.id)
    if (response.data.code === 200) {
      Object.assign(stats, response.data.data)
    }
  } catch (error) {
    console.error('获取统计失败:', error)
  }
}

const fetchTodayReminders = async () => {
  if (!userStore.user) return
  
  try {
    const response = await taskApi.getTodayReminders(userStore.user.id)
    if (response.data.code === 200) {
      todayReminders.value = response.data.data
      if (todayReminders.value.length > 0) {
        showReminder.value = true
      }
    }
  } catch (error) {
    console.error('获取提醒失败:', error)
  }
}

const checkReminders = () => {
  const now = new Date()
  const currentTime = `${String(now.getHours()).padStart(2, '0')}:${String(now.getMinutes()).padStart(2, '0')}`
  
  todayReminders.value.forEach(task => {
    if (task.reminderTime === currentTime && task.status !== 'COMPLETED') {
      showNotification(task)
    }
  })
}

const showNotification = (task) => {
  if ('Notification' in window && Notification.permission === 'granted') {
    new Notification('任务提醒', {
      body: task.title,
      icon: '/vite.svg'
    })
  }
}

const updateStatus = async (taskId, newStatus) => {
  try {
    const response = await taskApi.updateTaskStatus(taskId, userStore.user.id, newStatus)
    if (response.data.code === 200) {
      await fetchTasks()
      await fetchStats()
    }
  } catch (error) {
    alert('状态更新失败')
  }
}

const deleteTask = async (taskId) => {
  if (!confirm('确定要删除这个任务吗？')) return
  
  try {
    const response = await taskApi.deleteTask(taskId, userStore.user.id)
    if (response.data.code === 200) {
      await fetchTasks()
      await fetchStats()
    }
  } catch (error) {
    alert('删除失败')
  }
}

const filterByStatus = (status) => {
  selectedStatus.value = status
}

const filterByPriority = (priority) => {
  selectedPriority.value = priority
}

const filterByDate = () => {
  // 日期筛选通过 computed 自动生效
}

const clearDateFilter = () => {
  selectedDate.value = ''
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return `${date.getMonth() + 1}月${date.getDate()}日`
}

const handleLogout = () => {
  userStore.logout()
  router.push('/login')
}

onMounted(async () => {
  await fetchTasks()
  await fetchStats()
  await fetchTodayReminders()
  
  // 请求通知权限
  if ('Notification' in window && Notification.permission === 'default') {
    Notification.requestPermission()
  }
  
  // 每分钟检查一次提醒
  reminderInterval = setInterval(checkReminders, 60000)
})

onUnmounted(() => {
  if (reminderInterval) {
    clearInterval(reminderInterval)
  }
})
</script>

<style scoped>
.tasks-page {
  min-height: 100vh;
}

.page-header {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: var(--spacing-md);
  flex-wrap: wrap;
  margin-bottom: var(--spacing-lg);
}

.page-title-left {
  text-align: left;
  margin: 0;
}

/* 统计卡片 */
.stats-cards {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 1rem;
  margin-bottom: 1.5rem;
}

.stat-card {
  text-align: center;
  padding: 1.25rem;
}

.stat-card.todo {
  border-top: 4px solid var(--danger-color);
}

.stat-card.in-progress {
  border-top: 4px solid var(--warning-color);
}

.stat-card.completed {
  border-top: 4px solid var(--secondary-color);
}

.stat-number {
  font-size: 2.5rem;
  font-weight: bold;
  color: var(--gray-900);
}

.stat-label {
  color: var(--gray-700);
  margin-top: 0.5rem;
}

/* 筛选栏 */
.filter-bar {
  display: flex;
  gap: 2rem;
  margin-bottom: 1rem;
  flex-wrap: wrap;
}

.filter-group {
  display: flex;
  gap: 0.5rem;
  align-items: center;
}

.filter-btn {
  border-radius: 20px;
  height: 36px;
  padding: 0 0.75rem;
  background: rgba(255, 255, 255, 0.62);
  border: 1px solid var(--gray-300);
  color: var(--gray-700);
}

.filter-btn:hover,
.filter-btn.active {
  background: rgba(47, 111, 237, 0.12);
  border-color: rgba(47, 111, 237, 0.35);
  color: var(--primary-dark);
}

.date-filter {
  display: flex;
  gap: 0.5rem;
  align-items: center;
  margin-bottom: 1.5rem;
  flex-wrap: wrap;
}

.date-input {
  max-width: 220px;
}

.btn-clear {
  height: 40px;
}

.loading,
.empty-state {
  text-align: center;
  padding: 3rem;
  color: var(--gray-700);
}

/* 看板 */
.kanban-board {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 1.5rem;
}

.kanban-column {
  background: rgba(255, 255, 255, 0.35);
  border-radius: 12px;
  padding: 1rem;
  min-height: 400px;
  border: 1px solid var(--gray-200);
  box-shadow: var(--shadow-sm);
}

.column-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.75rem;
  border-radius: 8px;
  margin-bottom: 1rem;
  border: 1px solid var(--gray-200);
}

.column-header.todo {
  background: rgba(214, 69, 69, 0.10);
  color: #8a1f1f;
}

.column-header.in-progress {
  background: rgba(227, 155, 6, 0.12);
  color: #7a4e00;
}

.column-header.completed {
  background: rgba(31, 157, 85, 0.12);
  color: #0f5a33;
}

.column-header h3 {
  margin: 0;
  font-size: 1rem;
}

.task-count {
  background: rgba(22, 35, 58, 0.10);
  padding: 0.25rem 0.75rem;
  border-radius: 12px;
  font-size: 0.875rem;
}

.column-content {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.task-card {
  border-left: 4px solid #ddd;
}

.task-card.high {
  border-left-color: var(--danger-color);
}

.task-card.medium {
  border-left-color: var(--warning-color);
}

.task-card.low {
  border-left-color: var(--secondary-color);
}

.task-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 0.5rem;
}

.task-title {
  margin: 0;
  font-size: 1rem;
  color: var(--gray-900);
  flex: 1;
}

.priority-badge {
  padding: 0.25rem 0.5rem;
  border-radius: 4px;
  font-size: 0.75rem;
  font-weight: 500;
}

.priority-badge.high {
  background: rgba(214, 69, 69, 0.12);
  color: #8a1f1f;
}

.priority-badge.medium {
  background: rgba(227, 155, 6, 0.14);
  color: #7a4e00;
}

.priority-badge.low {
  background: rgba(31, 157, 85, 0.14);
  color: #0f5a33;
}

.task-desc {
  color: var(--gray-700);
  font-size: 0.875rem;
  margin: 0.5rem 0;
  line-height: 1.4;
}

.task-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
  margin-bottom: 0.75rem;
}

.meta-item {
  font-size: 0.8rem;
  color: var(--gray-700);
  background: rgba(22, 35, 58, 0.06);
  padding: 0.25rem 0.5rem;
  border-radius: 4px;
}

.meta-item.reminder {
  background: rgba(227, 155, 6, 0.14);
  color: #7a4e00;
}

.task-actions {
  display: flex;
  gap: 0.5rem;
}

.task-actions :deep(.edu-btn) {
  flex: 1;
  height: 38px;
}

/* 提醒弹窗 */
.reminder-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.reminder-content {
  background: white;
  padding: 2rem;
  border-radius: 12px;
  max-width: 400px;
  width: 90%;
  border: 1px solid var(--gray-200);
  box-shadow: var(--shadow-lg);
}

.reminder-content h3 {
  margin-bottom: 1rem;
  color: #333;
}

.reminder-item {
  padding: 1rem;
  background: #f8f9fa;
  border-radius: 8px;
  margin-bottom: 0.5rem;
}

.reminder-title {
  font-weight: 500;
  margin-bottom: 0.25rem;
}

.reminder-time {
  color: #666;
  font-size: 0.875rem;
}

.btn-close {
  width: 100%;
  margin-top: 1rem;
}

@media (max-width: 1024px) {
  .kanban-board {
    grid-template-columns: 1fr;
  }
  
  .stats-cards {
    grid-template-columns: 1fr;
  }
}
</style>
