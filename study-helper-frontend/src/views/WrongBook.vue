<template>
  <div class="wrong-book-container">
    <nav class="navbar">
      <div class="nav-brand">学习辅助系统</div>
      <div class="nav-links">
        <router-link to="/" class="nav-link">首页</router-link>
        <router-link to="/quizzes" class="nav-link">在线测验</router-link>
        <router-link to="/profile" class="nav-link">个人中心</router-link>
        <button @click="handleLogout" class="btn-logout">退出</button>
      </div>
    </nav>

    <main class="book-content">
      <div class="header">
        <h2>错题本</h2>
        <p>记录你的知识盲点，反复练习直到掌握</p>
      </div>

      <div v-if="loading" class="loading">加载中...</div>
      
      <div v-else-if="wrongQuestions.length === 0" class="empty-state">
        <div class="empty-icon">📚</div>
        <h3>暂无错题记录</h3>
        <p>完成测验后，错题会自动收录到这里</p>
        <router-link to="/quizzes" class="btn-go-quizzes">去参加测验</router-link>
      </div>

      <div v-else class="wrong-questions">
        <div class="filters">
          <select v-model="selectedSubject" class="filter-select">
            <option value="">全部科目</option>
            <option v-for="subject in subjects" :key="subject" :value="subject">
              {{ subject }}
            </option>
          </select>
        </div>

        <div class="questions-list">
          <div 
            v-for="(item, index) in filteredQuestions" 
            :key="index" 
            class="question-card"
          >
            <div class="question-header">
              <span class="subject-tag">{{ item.quizTitle }}</span>
              <span class="date">{{ formatDate(item.recordedAt) }}</span>
            </div>
            
            <div class="question-content">
              <div class="question-text">{{ item.question.content }}</div>
              
              <div v-if="item.question.type === 'SINGLE_CHOICE'" class="options">
                <div 
                  v-for="(option, i) in parseOptions(item.question.options)" 
                  :key="i" 
                  class="option"
                >
                  {{ String.fromCharCode(65 + i) }}. {{ option }}
                </div>
              </div>
              
              <div v-else class="true-false-options">
                <div class="option">A. 正确</div>
                <div class="option">B. 错误</div>
              </div>
            </div>

            <div class="correct-answer">
              <strong>正确答案：</strong>
              <span class="answer">{{ item.question.answer }}</span>
            </div>

            <div v-if="item.question.analysis" class="analysis">
              <strong>解析：</strong>
              <p>{{ item.question.analysis }}</p>
            </div>

            <div class="card-actions">
              <button @click="markAsMastered(index)" class="btn-mastered">
                ✅ 已掌握
              </button>
              <button @click="addToReview(index)" class="btn-review">
                🔄 加入复习
              </button>
            </div>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { quizApi } from '../api/quiz'

const router = useRouter()
const userStore = useUserStore()

const wrongQuestions = ref([])
const selectedSubject = ref('')
const loading = ref(false)

const subjects = computed(() => {
  const uniqueSubjects = [...new Set(wrongQuestions.value.map(item => item.quizTitle))]
  return uniqueSubjects
})

const filteredQuestions = computed(() => {
  if (!selectedSubject.value) {
    return wrongQuestions.value
  }
  return wrongQuestions.value.filter(item => item.quizTitle === selectedSubject.value)
})

const fetchWrongQuestions = async () => {
  loading.value = true
  try {
    const response = await quizApi.getWrongQuestions(userStore.user.id)
    if (response.data.code === 200) {
      wrongQuestions.value = response.data.data
    }
  } catch (error) {
    console.error('获取错题失败:', error)
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

const formatDate = (dateStr) => {
  return new Date(dateStr).toLocaleDateString('zh-CN')
}

const markAsMastered = (index) => {
  // 这里可以调用API标记为已掌握
  wrongQuestions.value.splice(index, 1)
}

const addToReview = (index) => {
  // 这里可以加入复习计划
  alert('已加入复习计划')
}

const handleLogout = () => {
  userStore.logout()
  router.push('/login')
}

onMounted(() => {
  fetchWrongQuestions()
})
</script>

<style scoped>
.wrong-book-container {
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

.book-content {
  max-width: 1000px;
  margin: 0 auto;
  padding: 2rem;
}

.header {
  text-align: center;
  margin-bottom: 2rem;
}

.header h2 {
  color: #333;
  margin-bottom: 0.5rem;
}

.header p {
  color: #666;
  font-size: 1.1rem;
}

.loading, .empty-state {
  text-align: center;
  padding: 3rem;
  color: #666;
}

.empty-icon {
  font-size: 4rem;
  margin-bottom: 1rem;
}

.empty-state h3 {
  color: #333;
  margin-bottom: 1rem;
}

.btn-go-quizzes {
  display: inline-block;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 0.75rem 1.5rem;
  border-radius: 6px;
  text-decoration: none;
  margin-top: 1rem;
  transition: transform 0.2s;
}

.btn-go-quizzes:hover {
  transform: translateY(-2px);
}

.filters {
  margin-bottom: 1.5rem;
  text-align: center;
}

.filter-select {
  padding: 0.5rem 1rem;
  border: 1px solid #ddd;
  border-radius: 6px;
  background: white;
  font-size: 1rem;
}

.questions-list {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.question-card {
  background: white;
  border-radius: 12px;
  padding: 1.5rem;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  transition: transform 0.2s;
}

.question-card:hover {
  transform: translateY(-2px);
}

.question-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}

.subject-tag {
  background: #e3f2fd;
  color: #1976d2;
  padding: 0.25rem 0.75rem;
  border-radius: 20px;
  font-size: 0.9rem;
}

.date {
  color: #888;
  font-size: 0.9rem;
}

.question-content {
  margin-bottom: 1rem;
}

.question-text {
  font-size: 1.1rem;
  margin-bottom: 1rem;
  line-height: 1.6;
  color: #333;
}

.options, .true-false-options {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 0.5rem;
  margin-bottom: 1rem;
}

.option {
  background: #f8f9fa;
  border: 1px solid #ddd;
  border-radius: 4px;
  padding: 0.5rem;
  font-size: 0.9rem;
}

.correct-answer {
  background: #d4edda;
  padding: 0.75rem;
  border-radius: 6px;
  margin-bottom: 1rem;
}

.correct-answer .answer {
  color: #155724;
  font-weight: bold;
  font-size: 1.1rem;
}

.analysis {
  background: #fff3cd;
  padding: 0.75rem;
  border-radius: 6px;
  margin-bottom: 1rem;
}

.analysis p {
  margin: 0.5rem 0 0 0;
  color: #856404;
  line-height: 1.5;
}

.card-actions {
  display: flex;
  gap: 0.75rem;
}

.btn-mastered, .btn-review {
  flex: 1;
  padding: 0.5rem;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.9rem;
  transition: all 0.3s;
}

.btn-mastered {
  background: #28a745;
  color: white;
}

.btn-mastered:hover {
  background: #218838;
}

.btn-review {
  background: #ffc107;
  color: #212529;
}

.btn-review:hover {
  background: #e0a800;
}

@media (max-width: 768px) {
  .options, .true-false-options {
    grid-template-columns: 1fr;
  }
  
  .question-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 0.5rem;
  }
  
  .card-actions {
    flex-direction: column;
  }
}
</style>