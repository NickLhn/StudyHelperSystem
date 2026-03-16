<template>
  <div class="teacher-quizzes-container">
    <h1 class="page-title">📝 小测验管理</h1>
    
    <!-- 测验操作 -->
    <div class="quiz-actions">
      <router-link to="/teacher/quiz/create" class="btn-create-quiz">
        <i class="icon">➕</i> 创建测验
      </router-link>
    </div>
    
    <!-- 课程选择 -->
    <div class="course-selector">
      <label for="course-select">选择课程：</label>
      <select id="course-select" v-model="selectedCourseId" @change="loadQuizzes">
        <option value="">所有课程</option>
        <option v-for="course in courses" :key="course.id" :value="course.id">
          {{ course.name }}
        </option>
      </select>
    </div>
    
    <!-- 测验列表 -->
    <div class="quizzes-grid">
      <div v-for="quiz in quizzes" :key="quiz.id" class="quiz-card">
        <div class="quiz-header">
          <h3 class="quiz-title">{{ quiz.title }}</h3>
          <div class="quiz-meta">
            <span class="question-count">{{ quiz.questionCount }} 题</span>
            <span v-if="quiz.totalTime > 0" class="quiz-duration">{{ quiz.totalTime }} 分钟</span>
          </div>
        </div>
        <div class="quiz-info">
          <p class="quiz-course"><strong>所属课程：</strong>{{ getCourseName(quiz.courseId) }}</p>
          <p class="quiz-status"><strong>状态：</strong>{{ quiz.statusLabel }}</p>
          <p class="quiz-date"><strong>创建时间：</strong>{{ formatDate(quiz.createdAt) }}</p>
        </div>
        <div class="quiz-actions">
          <button v-if="quiz.status === 'DRAFT'" @click="setQuizStatus(quiz.id, 'PUBLISHED')" class="btn-stats">
            <i class="icon">📢</i> 发布
          </button>
          <button v-if="quiz.status === 'PUBLISHED'" @click="setQuizStatus(quiz.id, 'CLOSED')" class="btn-edit">
            <i class="icon">⛔</i> 关闭
          </button>
          <router-link :to="`/quiz/${quiz.id}`" class="btn-stats">
            <i class="icon">👁️</i> 查看
          </router-link>
          <button @click="deleteQuiz(quiz.id)" class="btn-delete">
            <i class="icon">🗑️</i> 删除
          </button>
        </div>
      </div>
    </div>
    
    <!-- 空状态 -->
    <div v-if="quizzes.length === 0 && !loading" class="empty-state">
      <div class="empty-icon">📝</div>
      <h3>还没有创建测验</h3>
      <p>点击上方的"创建测验"按钮开始创建您的第一个测验</p>
    </div>
    
    <!-- 加载状态 -->
    <div v-if="loading" class="loading">
      <div class="spinner"></div>
      <p>加载测验列表中...</p>
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
import { quizApi } from '../api/quiz'

const router = useRouter()
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
    console.error('获取测验列表失败:', err)
  } finally {
    loading.value = false
  }
}

const createQuiz = () => {
  router.push('/teacher/quiz/create')
}

const editQuiz = (quizId) => {
  router.push(`/teacher/quiz/edit/${quizId}`)
}

const deleteQuiz = async (quizId) => {
  if (!confirm('确定要删除这个测验吗？此操作不可撤销。')) {
    return
  }
  
  try {
    const response = await quizApi.deleteQuiz(quizId, userStore.user.id)
    if (response.data.code === 200) {
      loadQuizzes() // 重新加载测验列表
    } else {
      error.value = response.data.message
    }
  } catch (err) {
    error.value = '删除测验失败'
    console.error('删除测验失败:', err)
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
  const course = courses.value.find(c => c.id === courseId)
  return course ? course.name : '未知课程'
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
  loadQuizzes()
})
</script>

<style scoped>
.teacher-quizzes-container {
  padding: 2rem 0;
}

.page-title {
  color: #333;
  margin-bottom: 2rem;
  font-size: 2rem;
  text-align: center;
}

.quiz-actions {
  margin-bottom: 2rem;
  text-align: right;
}

.btn-create-quiz {
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

.btn-create-quiz:hover {
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

.quizzes-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 2rem;
}

.quiz-card {
  background: white;
  border-radius: 12px;
  padding: 1.5rem;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.08);
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.quiz-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
}

.quiz-header {
  margin-bottom: 1rem;
}

.quiz-title {
  margin: 0 0 0.5rem 0;
  font-size: 1.2rem;
  font-weight: 600;
  color: #333;
}

.quiz-meta {
  display: flex;
  gap: 1rem;
  font-size: 0.9rem;
  color: #666;
}

.question-count, .quiz-duration {
  background: #f5f5f5;
  padding: 0.25rem 0.75rem;
  border-radius: 20px;
}

.quiz-info {
  margin-bottom: 1.5rem;
}

.quiz-info p {
  margin: 0.5rem 0;
  font-size: 0.9rem;
  color: #666;
}

.quiz-actions {
  display: flex;
  gap: 1rem;
  justify-content: space-between;
}

.btn-stats, .btn-edit, .btn-delete {
  padding: 0.5rem 1rem;
  border: none;
  border-radius: 6px;
  font-size: 0.9rem;
  cursor: pointer;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  flex: 1;
  justify-content: center;
}

.btn-stats {
  background: #e8f5e8;
  color: #2e7d32;
}

.btn-stats:hover {
  background: #c8e6c9;
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
  .teacher-quizzes-container {
    padding: 1rem 0;
  }
  
  .page-title {
    font-size: 1.5rem;
  }
  
  .course-selector {
    flex-direction: column;
    align-items: stretch;
  }
  
  .quizzes-grid {
    grid-template-columns: 1fr;
    gap: 1rem;
  }
  
  .quiz-card {
    padding: 1rem;
  }
  
  .quiz-actions {
    flex-direction: column;
  }
}
</style>
