<template>
  <div class="quiz-take-container">
    <nav class="navbar">
      <div class="nav-brand">学习辅助系统</div>
      <div class="nav-links">
        <router-link to="/" class="nav-link">首页</router-link>
        <router-link to="/quizzes" class="nav-link">在线测验</router-link>
        <router-link to="/profile" class="nav-link">个人中心</router-link>
        <button @click="handleLogout" class="btn-logout">退出</button>
      </div>
    </nav>

    <main class="take-content">
      <div v-if="loading" class="loading">加载中...</div>
      
      <div v-else-if="quiz" class="quiz-taking">
        <div class="quiz-header">
          <h2>{{ quiz.title }}</h2>
          <div class="timer" v-if="quiz.totalTime > 0">
            剩余时间: {{ formatTime(timeLeft) }}
          </div>
        </div>

        <div class="progress-bar">
          <div class="progress" :style="{ width: progress + '%' }"></div>
        </div>
        <div class="progress-text">{{ currentQuestionIndex + 1 }} / {{ questions.length }}</div>

        <div v-if="currentQuestion" class="question-area">
          <div class="question-header">
            <span class="question-number">第 {{ currentQuestionIndex + 1 }} 题</span>
            <span class="question-score">{{ currentQuestion.score }} 分</span>
          </div>
          <div class="question-content">{{ currentQuestion.content }}</div>
          
          <div class="answers">
            <div 
              v-for="(option, index) in currentOptions" 
              :key="index"
              :class="['option', { selected: userAnswers[currentQuestion.id] === getOptionKey(index) }]"
              @click="selectAnswer(getOptionKey(index))"
            >
              <span class="option-key">{{ getOptionKey(index) }}.</span>
              <span class="option-text">{{ option }}</span>
            </div>
          </div>
        </div>

        <div class="navigation">
          <button 
            @click="prevQuestion" 
            :disabled="currentQuestionIndex === 0"
            class="btn-nav"
          >
            上一题
          </button>
          <button 
            @click="nextQuestion" 
            :disabled="currentQuestionIndex === questions.length - 1"
            class="btn-nav"
          >
            下一题
          </button>
        </div>

        <div class="submit-section">
          <button 
            @click="submitQuiz" 
            :disabled="!canSubmit"
            class="btn-submit"
          >
            提交测验
          </button>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { quizApi } from '../api/quiz'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const quiz = ref(null)
const questions = ref([])
const userAnswers = ref({})
const currentQuestionIndex = ref(0)
const timeLeft = ref(0)
const timer = ref(null)
const loading = ref(false)

const currentQuestion = computed(() => questions.value[currentQuestionIndex.value])

const currentOptions = computed(() => {
  if (!currentQuestion.value) return []
  if (currentQuestion.value.type === 'SINGLE_CHOICE') {
    try {
      return JSON.parse(currentQuestion.value.options)
    } catch {
      return []
    }
  } else {
    return ['正确', '错误']
  }
})

const progress = computed(() => {
  return ((currentQuestionIndex.value + 1) / questions.value.length) * 100
})

const canSubmit = computed(() => {
  return Object.keys(userAnswers.value).length === questions.value.length
})

const fetchQuiz = async () => {
  loading.value = true
  try {
    const response = await quizApi.getQuizDetail(route.params.id, userStore.user.id)
    if (response.data.code === 200) {
      quiz.value = response.data.data.quiz
      questions.value = response.data.data.questions
      if (quiz.value.totalTime > 0) {
        timeLeft.value = quiz.value.totalTime * 60 // 转换为秒
        startTimer()
      }
    }
  } catch (error) {
    console.error('获取测验失败:', error)
  } finally {
    loading.value = false
  }
}

const startTimer = () => {
  timer.value = setInterval(() => {
    timeLeft.value--
    if (timeLeft.value <= 0) {
      clearInterval(timer.value)
      submitQuiz()
    }
  }, 1000)
}

const formatTime = (seconds) => {
  const mins = Math.floor(seconds / 60)
  const secs = seconds % 60
  return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`
}

const getOptionKey = (index) => {
  return currentQuestion.value.type === 'SINGLE_CHOICE' 
    ? String.fromCharCode(65 + index)
    : (index === 0 ? 'A' : 'B')
}

const selectAnswer = (answer) => {
  userAnswers.value[currentQuestion.value.id] = answer
}

const prevQuestion = () => {
  if (currentQuestionIndex.value > 0) {
    currentQuestionIndex.value--
  }
}

const nextQuestion = () => {
  if (currentQuestionIndex.value < questions.value.length - 1) {
    currentQuestionIndex.value++
  }
}

const submitQuiz = async () => {
  if (timer.value) {
    clearInterval(timer.value)
  }
  
  try {
    const response = await quizApi.submitQuiz(quiz.value.id, userStore.user.id, userAnswers.value)
    if (response.data.code === 200) {
      router.push(`/quiz/result/${response.data.data.recordId}`)
    }
  } catch (error) {
    console.error('提交失败:', error)
  }
}

const handleLogout = () => {
  userStore.logout()
  router.push('/login')
}

onMounted(() => {
  fetchQuiz()
})

onUnmounted(() => {
  if (timer.value) {
    clearInterval(timer.value)
  }
})
</script>

<style scoped>
.quiz-take-container {
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

.take-content {
  max-width: 800px;
  margin: 2rem auto;
  padding: 0 1rem;
}

.loading {
  text-align: center;
  padding: 3rem;
  color: #666;
}

.quiz-taking {
  background: white;
  border-radius: 12px;
  padding: 2rem;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.quiz-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
}

.quiz-header h2 {
  color: #333;
  margin: 0;
}

.timer {
  background: #ff6b6b;
  color: white;
  padding: 0.5rem 1rem;
  border-radius: 20px;
  font-weight: 500;
}

.progress-bar {
  width: 100%;
  height: 8px;
  background: #eee;
  border-radius: 4px;
  margin-bottom: 0.5rem;
  overflow: hidden;
}

.progress {
  height: 100%;
  background: linear-gradient(90deg, #42b883, #369870);
  transition: width 0.3s;
}

.progress-text {
  text-align: center;
  color: #666;
  margin-bottom: 2rem;
}

.question-area {
  margin-bottom: 2rem;
}

.question-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 1rem;
}

.question-number {
  font-weight: bold;
  color: #667eea;
}

.question-score {
  background: #ff6b6b;
  color: white;
  padding: 0.25rem 0.75rem;
  border-radius: 20px;
  font-size: 0.9rem;
}

.question-content {
  font-size: 1.2rem;
  margin-bottom: 1.5rem;
  line-height: 1.6;
  color: #333;
}

.answers {
  display: grid;
  gap: 0.75rem;
}

.option {
  display: flex;
  align-items: center;
  padding: 1rem;
  border: 2px solid #ddd;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
}

.option:hover {
  border-color: #667eea;
}

.option.selected {
  border-color: #667eea;
  background: rgba(102, 126, 234, 0.1);
}

.option-key {
  font-weight: bold;
  margin-right: 1rem;
  color: #667eea;
  min-width: 20px;
}

.navigation {
  display: flex;
  gap: 1rem;
  margin-bottom: 2rem;
}

.btn-nav {
  flex: 1;
  padding: 0.75rem;
  border: none;
  border-radius: 6px;
  background: #f0f0f0;
  color: #666;
  cursor: pointer;
  transition: all 0.3s;
}

.btn-nav:hover:not(:disabled) {
  background: #e0e0e0;
}

.btn-nav:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.submit-section {
  text-align: center;
}

.btn-submit {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  padding: 1rem 2rem;
  border-radius: 8px;
  font-size: 1.1rem;
  font-weight: 500;
  cursor: pointer;
  transition: transform 0.2s;
}

.btn-submit:hover:not(:disabled) {
  transform: translateY(-2px);
}

.btn-submit:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}
</style>