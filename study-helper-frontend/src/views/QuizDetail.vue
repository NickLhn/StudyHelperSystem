<template>
  <div class="quiz-detail-container">
    <nav class="navbar">
      <div class="nav-brand">学习辅助系统</div>
      <div class="nav-links">
        <router-link to="/" class="nav-link">首页</router-link>
        <router-link to="/quizzes" class="nav-link">在线测验</router-link>
        <router-link to="/profile" class="nav-link">个人中心</router-link>
        <button @click="handleLogout" class="btn-logout">退出</button>
      </div>
    </nav>

    <main class="detail-content">
      <div v-if="loading" class="loading">加载中...</div>
      
      <div v-else-if="quiz" class="quiz-detail">
        <div class="quiz-header">
          <h2>{{ quiz.title }}</h2>
          <div class="quiz-info">
            <span class="info-item">📚 {{ quiz.courseName || '无关联课程' }}</span>
            <span class="info-item">❓ {{ quiz.questionCount }} 题</span>
            <span v-if="quiz.totalTime > 0" class="info-item">⏱️ {{ quiz.totalTime }} 分钟</span>
            <span class="info-item status" :class="quiz.status.toLowerCase()">
              {{ quiz.statusLabel }}
            </span>
          </div>
        </div>

        <div v-if="quiz.description" class="quiz-description">
          <h3>测验说明</h3>
          <p>{{ quiz.description }}</p>
        </div>

        <div class="questions-preview">
          <h3>题目预览</h3>
          <div v-for="(question, index) in questions" :key="question.id" class="question-item">
            <div class="question-header">
              <span class="question-number">第 {{ index + 1 }} 题</span>
              <span class="question-type">{{ question.typeLabel }}</span>
              <span class="question-score">{{ question.score }} 分</span>
            </div>
            <div class="question-content">{{ question.content }}</div>
            
            <div v-if="question.type === 'SINGLE_CHOICE'" class="options">
              <div v-for="(option, i) in parseOptions(question.options)" :key="i" class="option">
                {{ String.fromCharCode(65 + i) }}. {{ option }}
              </div>
            </div>
            
            <div v-else class="true-false-options">
              <div class="option">A. 正确</div>
              <div class="option">B. 错误</div>
            </div>
          </div>
        </div>

        <div class="actions">
          <router-link :to="`/quiz/${quiz.id}/take`" class="btn-take">
            开始答题
          </router-link>
          <button @click="goBack" class="btn-back">返回</button>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { quizApi } from '../api/quiz'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const quiz = ref(null)
const questions = ref([])
const loading = ref(false)

const fetchQuizDetail = async () => {
  loading.value = true
  try {
    const response = await quizApi.getQuizDetail(route.params.id, userStore.user.id)
    if (response.data.code === 200) {
      quiz.value = response.data.data.quiz
      questions.value = response.data.data.questions
    }
  } catch (error) {
    console.error('获取测验详情失败:', error)
  } finally {
    loading.value = false
  }
}

const parseOptions = (optionsStr) => {
  try {
    return JSON.parse(optionsStr)
  } catch {
    return []
  }
}

const goBack = () => {
  router.push('/quizzes')
}

const handleLogout = () => {
  userStore.logout()
  router.push('/login')
}

onMounted(() => {
  fetchQuizDetail()
})
</script>

<style scoped>
.quiz-detail-container {
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

.detail-content {
  max-width: 800px;
  margin: 2rem auto;
  padding: 0 1rem;
}

.loading {
  text-align: center;
  padding: 3rem;
  color: #666;
}

.quiz-detail {
  background: white;
  border-radius: 12px;
  padding: 2rem;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.quiz-header h2 {
  color: #333;
  margin-bottom: 1rem;
}

.quiz-info {
  display: flex;
  flex-wrap: wrap;
  gap: 1rem;
  margin-bottom: 1.5rem;
}

.info-item {
  background: #f0f0f0;
  padding: 0.25rem 0.75rem;
  border-radius: 20px;
  font-size: 0.9rem;
}

.info-item.status.draft {
  background: #fff3cd;
  color: #856404;
}

.info-item.status.published {
  background: #d4edda;
  color: #155724;
}

.quiz-description {
  background: #f8f9fa;
  padding: 1.5rem;
  border-radius: 8px;
  margin-bottom: 2rem;
}

.quiz-description h3 {
  margin-top: 0;
  color: #333;
}

.questions-preview .question-item {
  border: 1px solid #eee;
  border-radius: 8px;
  padding: 1.5rem;
  margin-bottom: 1rem;
  background: #fafafa;
}

.question-header {
  display: flex;
  gap: 1rem;
  margin-bottom: 1rem;
  font-size: 0.9rem;
}

.question-number {
  font-weight: bold;
  color: #667eea;
}

.question-type {
  background: #e3f2fd;
  color: #1976d2;
  padding: 0.1rem 0.5rem;
  border-radius: 4px;
}

.question-score {
  color: #ff6b6b;
  font-weight: 500;
}

.question-content {
  font-size: 1.1rem;
  margin-bottom: 1rem;
  line-height: 1.6;
}

.options, .true-false-options {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 0.5rem;
}

.option {
  background: white;
  border: 1px solid #ddd;
  border-radius: 4px;
  padding: 0.5rem;
  font-size: 0.9rem;
}

.actions {
  display: flex;
  gap: 1rem;
  margin-top: 2rem;
}

.btn-take {
  flex: 1;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  text-decoration: none;
  padding: 1rem;
  border-radius: 8px;
  text-align: center;
  font-size: 1.1rem;
  font-weight: 500;
  transition: transform 0.2s;
}

.btn-take:hover {
  transform: translateY(-2px);
}

.btn-back {
  flex: 1;
  background: #f0f0f0;
  color: #666;
  border: none;
  padding: 1rem;
  border-radius: 8px;
  cursor: pointer;
  font-size: 1.1rem;
  transition: background-color 0.3s;
}

.btn-back:hover {
  background: #e0e0e0;
}
</style>