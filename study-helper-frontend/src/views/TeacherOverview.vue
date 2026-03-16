<template>
  <div class="teacher-overview-container">
    <h1 class="page-title">📊 课程概览</h1>
    
    <!-- 课程选择 -->
    <div class="course-selector">
      <label for="course-select">选择课程：</label>
      <select id="course-select" v-model="selectedCourseId" @change="loadCourseOverview">
        <option value="">请选择课程</option>
        <option v-for="course in courses" :key="course.id" :value="course.id">
          {{ course.name }}
        </option>
      </select>
    </div>
    
    <!-- 课程概览卡片 -->
    <div v-if="courseOverview" class="overview-cards">
      <div class="overview-card">
        <div class="card-icon">👥</div>
        <div class="card-content">
          <h3>学生人数</h3>
          <p class="card-value">{{ courseOverview.studentCount || 0 }}</p>
        </div>
      </div>
      <div class="overview-card">
        <div class="card-icon">📋</div>
        <div class="card-content">
          <h3>任务总数</h3>
          <p class="card-value">{{ courseOverview.taskCount || 0 }}</p>
        </div>
      </div>
      <div class="overview-card">
        <div class="card-icon">📁</div>
        <div class="card-content">
          <h3>资料总数</h3>
          <p class="card-value">{{ courseOverview.materialCount || 0 }}</p>
        </div>
      </div>
      <div class="overview-card">
        <div class="card-icon">📝</div>
        <div class="card-content">
          <h3>测验总数</h3>
          <p class="card-value">{{ courseOverview.quizCount || 0 }}</p>
        </div>
      </div>
    </div>
    
    <!-- 任务完成情况 -->
    <div v-if="courseOverview && courseOverview.tasks.length > 0" class="tasks-overview">
      <h2 class="section-title">任务完成情况</h2>
      <div class="tasks-table-container">
        <table class="tasks-table">
          <thead>
            <tr>
              <th>任务名称</th>
              <th>截止时间</th>
              <th>完成人数</th>
              <th>未完成人数</th>
              <th>完成率</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="task in courseOverview.tasks" :key="task.id">
              <td>{{ task.title }}</td>
              <td>{{ formatDate(task.deadline) }}</td>
              <td>{{ task.completedCount || 0 }}</td>
              <td>{{ task.totalCount - (task.completedCount || 0) }}</td>
              <td>
                <div class="progress-bar">
                  <div 
                    class="progress-fill" 
                    :style="{ width: `${getCompletionRate(task)}%` }"
                  ></div>
                  <span class="progress-text">{{ getCompletionRate(task) }}%</span>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
    
    <!-- 学生完成情况 -->
    <div v-if="courseOverview && courseOverview.students.length > 0" class="students-overview">
      <h2 class="section-title">学生完成情况</h2>
      <div class="students-table-container">
        <table class="students-table">
          <thead>
            <tr>
              <th>学生姓名</th>
              <th>任务完成数</th>
              <th>任务总数</th>
              <th>完成率</th>
              <th>平均成绩</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="student in courseOverview.students" :key="student.id">
              <td>{{ student.name }}</td>
              <td>{{ student.completedTasks }}</td>
              <td>{{ student.totalTasks }}</td>
              <td>
                <div class="progress-bar">
                  <div 
                    class="progress-fill" 
                    :style="{ width: `${getStudentCompletionRate(student)}%` }"
                  ></div>
                  <span class="progress-text">{{ getStudentCompletionRate(student) }}%</span>
                </div>
              </td>
              <td>{{ student.averageScore || 0 }} 分</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
    
    <!-- 空状态 -->
    <div v-if="!courseOverview && !loading" class="empty-state">
      <div class="empty-icon">📊</div>
      <h3>请选择一门课程</h3>
      <p>从上方的下拉菜单中选择一门课程来查看其概览信息</p>
    </div>
    
    <!-- 加载状态 -->
    <div v-if="loading" class="loading">
      <div class="spinner"></div>
      <p>加载课程概览中...</p>
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
import { adminApi } from '../api/admin'

const courses = ref([])
const courseOverview = ref(null)
const selectedCourseId = ref('')
const loading = ref(false)
const error = ref('')

const loadCourses = async () => {
  try {
    const response = await adminApi.getCourses()
    if (response.data.code === 200) {
      courses.value = response.data.data
    }
  } catch (err) {
    console.error('获取课程列表失败:', err)
  }
}

const loadCourseOverview = async () => {
  if (!selectedCourseId.value) {
    courseOverview.value = null
    return
  }
  
  loading.value = true
  error.value = ''
  
  try {
    const response = await adminApi.getCourseOverview(selectedCourseId.value)
    if (response.data.code === 200) {
      courseOverview.value = response.data.data
    } else {
      error.value = response.data.message
    }
  } catch (err) {
    error.value = '获取课程概览失败，请检查网络连接'
    console.error('获取课程概览失败:', err)
  } finally {
    loading.value = false
  }
}

const getCompletionRate = (task) => {
  if (!task.totalCount || task.totalCount === 0) return 0
  return Math.round(((task.completedCount || 0) / task.totalCount) * 100)
}

const getStudentCompletionRate = (student) => {
  if (!student.totalTasks || student.totalTasks === 0) return 0
  return Math.round((student.completedTasks / student.totalTasks) * 100)
}

const formatDate = (dateString) => {
  if (!dateString) return '-'
  return new Date(dateString).toLocaleString('zh-CN')
}

onMounted(() => {
  loadCourses()
})
</script>

<style scoped>
.teacher-overview-container {
  padding: 2rem 0;
}

.page-title {
  color: #333;
  margin-bottom: 2rem;
  font-size: 2rem;
  text-align: center;
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

.overview-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 1.5rem;
  margin-bottom: 3rem;
}

.overview-card {
  background: white;
  border-radius: 12px;
  padding: 1.5rem;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.08);
  display: flex;
  align-items: center;
  gap: 1rem;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.overview-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
}

.card-icon {
  font-size: 2.5rem;
  flex-shrink: 0;
}

.card-content h3 {
  margin: 0 0 0.5rem 0;
  font-size: 1rem;
  font-weight: 500;
  color: #666;
}

.card-value {
  margin: 0;
  font-size: 2rem;
  font-weight: 700;
  color: #333;
}

.section-title {
  color: #333;
  margin-bottom: 1.5rem;
  font-size: 1.5rem;
  border-bottom: 2px solid #f093fb;
  padding-bottom: 0.5rem;
}

.tasks-overview,
.students-overview {
  margin-bottom: 3rem;
}

.tasks-table-container,
.students-table-container {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.08);
}

.tasks-table,
.students-table {
  width: 100%;
  border-collapse: collapse;
}

.tasks-table th,
.students-table th {
  background: #f8f9fa;
  padding: 1rem;
  text-align: left;
  font-weight: 600;
  color: #555;
  border-bottom: 2px solid #e9ecef;
}

.tasks-table td,
.students-table td {
  padding: 1rem;
  border-bottom: 1px solid #e9ecef;
}

.tasks-table tr:hover,
.students-table tr:hover {
  background: #f8f9fa;
}

.progress-bar {
  width: 100%;
  height: 20px;
  background: #f5f5f5;
  border-radius: 10px;
  position: relative;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #f093fb, #f5576c);
  border-radius: 10px;
  transition: width 0.3s ease;
}

.progress-text {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.8rem;
  font-weight: 500;
  color: #333;
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
  .teacher-overview-container {
    padding: 1rem 0;
  }
  
  .page-title {
    font-size: 1.5rem;
  }
  
  .course-selector {
    flex-direction: column;
    align-items: stretch;
  }
  
  .overview-cards {
    grid-template-columns: 1fr;
    gap: 1rem;
  }
  
  .section-title {
    font-size: 1.2rem;
  }
  
  .tasks-table,
  .students-table {
    font-size: 0.9rem;
  }
  
  .tasks-table th,
  .tasks-table td,
  .students-table th,
  .students-table td {
    padding: 0.5rem;
  }
}
</style>