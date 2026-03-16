<template>
  <div class="quizzes-page learning-environment">
    <main class="page-container">
      <div class="page-header">
        <h1 class="page-title page-title-left">在线测验</h1>
        <div class="header-actions">
          <router-link to="/quiz/create" class="edu-btn edu-btn-primary">创建测验</router-link>
          <router-link to="/wrong-book" class="edu-btn edu-btn-danger">错题本</router-link>
        </div>
      </div>

      <div class="tabs">
        <button 
          :class="['tab', { active: activeTab === 'my' }]"
          @click="activeTab = 'my'"
        >
          我创建的
        </button>
        <button 
          :class="['tab', { active: activeTab === 'available' }]"
          @click="activeTab = 'available'"
        >
          可参加的
        </button>
        <button 
          :class="['tab', { active: activeTab === 'records' }]"
          @click="activeTab = 'records'"
        >
          我的成绩
        </button>
      </div>

      <div v-if="loading" class="loading">加载中...</div>
      
      <!-- 我创建的测验 -->
      <div v-else-if="activeTab === 'my'" class="quiz-list">
        <div v-if="myQuizzes.length === 0" class="empty-state">
          <p>还没有创建任何测验</p>
          <router-link to="/quiz/create" class="edu-btn edu-btn-primary">创建第一个测验</router-link>
        </div>
        <div v-else class="quizzes-grid">
          <div v-for="quiz in myQuizzes" :key="quiz.id" class="quiz-card edu-card">
            <div class="quiz-header">
              <h3>{{ quiz.title }}</h3>
              <span class="status" :class="quiz.status.toLowerCase()">
                {{ quiz.statusLabel }}
              </span>
            </div>
            <p v-if="quiz.description" class="description">{{ quiz.description }}</p>
            <div class="quiz-meta">
              <span>📚 {{ quiz.courseName || '无关联课程' }}</span>
              <span>❓ {{ quiz.questionCount }} 题</span>
              <span v-if="quiz.totalTime > 0">⏱️ {{ quiz.totalTime }} 分钟</span>
            </div>
            <div class="quiz-actions">
              <router-link :to="`/quiz/${quiz.id}`" class="edu-btn edu-btn-secondary">查看详情</router-link>
              <button 
                v-if="quiz.status === 'DRAFT'" 
                @click="publishQuiz(quiz)"
                class="edu-btn edu-btn-success"
              >
                发布
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- 可参加的测验 -->
      <div v-else-if="activeTab === 'available'" class="quiz-list">
        <div v-if="availableQuizzes.length === 0" class="empty-state">
          <p>暂无可参加的测验</p>
        </div>
        <div v-else class="quizzes-grid">
          <div v-for="quiz in availableQuizzes" :key="quiz.id" class="quiz-card edu-card available">
            <div class="quiz-header">
              <h3>{{ quiz.title }}</h3>
              <span class="type">{{ quiz.typeLabel }}</span>
            </div>
            <p v-if="quiz.description" class="description">{{ quiz.description }}</p>
            <div class="quiz-meta">
              <span>👤 {{ quiz.username }}</span>
              <span>📚 {{ quiz.courseName || '无关联课程' }}</span>
              <span>❓ {{ quiz.questionCount }} 题</span>
            </div>
            <div class="quiz-actions">
              <router-link :to="`/quiz/${quiz.id}/take`" class="edu-btn edu-btn-primary">
                立即答题
              </router-link>
            </div>
          </div>
        </div>
      </div>

      <!-- 我的成绩 -->
      <div v-else-if="activeTab === 'records'" class="records-list">
        <div v-if="records.length === 0" class="empty-state">
          <p>还没有测验记录</p>
        </div>
        <div v-else class="records-table edu-card">
          <div class="table-header">
            <div>测验名称</div>
            <div>得分</div>
            <div>正确率</div>
            <div>状态</div>
            <div>时间</div>
          </div>
          <div v-for="record in records" :key="record.id" class="table-row">
            <div>{{ record.quizTitle }}</div>
            <div class="score">{{ record.score }}/{{ record.totalScore }}</div>
            <div class="accuracy">{{ (record.accuracy * 100).toFixed(1) }}%</div>
            <div>
              <span :class="['status-badge', { passed: record.isPassed }]">
                {{ record.isPassed ? '通过' : '未通过' }}
              </span>
            </div>
            <div>{{ formatDate(record.createdAt) }}</div>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { quizApi } from '../api/quiz'

const router = useRouter()
const userStore = useUserStore()

const activeTab = ref('my')
const myQuizzes = ref([])
const availableQuizzes = ref([])
const records = ref([])
const loading = ref(false)

const fetchMyQuizzes = async () => {
  loading.value = true
  try {
    const response = await quizApi.getUserQuizzes(userStore.user.id)
    if (response.data.code === 200) {
      myQuizzes.value = response.data.data
    }
  } catch (error) {
    console.error('获取测验失败:', error)
  } finally {
    loading.value = false
  }
}

const fetchAvailableQuizzes = async () => {
  loading.value = true
  try {
    const response = await quizApi.getAvailableQuizzes(userStore.user.id)
    if (response.data.code === 200) {
      availableQuizzes.value = response.data.data
    }
  } catch (error) {
    console.error('获取可参加测验失败:', error)
  } finally {
    loading.value = false
  }
}

const fetchRecords = async () => {
  loading.value = true
  try {
    const response = await quizApi.getUserRecords(userStore.user.id)
    if (response.data.code === 200) {
      records.value = response.data.data
    }
  } catch (error) {
    console.error('获取成绩失败:', error)
  } finally {
    loading.value = false
  }
}

const publishQuiz = (quiz) => {
  // 这里可以调用发布接口
  alert('发布功能待实现')
}

const formatDate = (dateStr) => {
  return new Date(dateStr).toLocaleDateString('zh-CN')
}

const handleLogout = () => {
  userStore.logout()
  router.push('/login')
}

onMounted(() => {
  fetchMyQuizzes()
})
</script>

<style scoped>
.quizzes-page {
  min-height: 100vh;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  gap: var(--spacing-md);
  flex-wrap: wrap;
  margin-bottom: var(--spacing-lg);
}

.page-title-left {
  text-align: left;
  margin: 0;
}

.header-actions {
  display: flex;
  gap: 1rem;
  flex-wrap: wrap;
}

.tabs {
  display: flex;
  gap: 0.5rem;
  margin-bottom: 1.5rem;
  border-bottom: 1px solid var(--gray-200);
}

.tab {
  padding: 0.75rem 1.5rem;
  background: transparent;
  border: 0;
  border-bottom: 3px solid transparent;
  cursor: pointer;
  font-size: 1rem;
  transition: all 0.3s;
  color: var(--gray-700);
}

.tab.active {
  color: var(--primary-dark);
  border-bottom-color: var(--primary-color);
}

.loading,
.empty-state {
  text-align: center;
  padding: 3rem;
  color: var(--gray-700);
}

.quizzes-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 1.5rem;
}

.quiz-card {
  transition: transform 0.2s, box-shadow 0.2s;
}

.quiz-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-lg);
}

.quiz-card.available {
  border-left: 4px solid var(--secondary-color);
}

.quiz-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 1rem;
}

.quiz-header h3 {
  margin: 0;
  color: var(--gray-900);
  font-size: 1.2rem;
}

.status {
  padding: 0.25rem 0.75rem;
  border-radius: 20px;
  font-size: 0.8rem;
  font-weight: 500;
}

.status.draft {
  background: rgba(227, 155, 6, 0.14);
  color: #7a4e00;
}

.status.published {
  background: rgba(31, 157, 85, 0.14);
  color: #0f5a33;
}

.type {
  background: rgba(47, 111, 237, 0.12);
  color: var(--primary-dark);
  padding: 0.25rem 0.75rem;
  border-radius: 20px;
  font-size: 0.8rem;
}

.description {
  color: var(--gray-700);
  margin-bottom: 1rem;
  line-height: 1.5;
}

.quiz-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 1rem;
  font-size: 0.9rem;
  color: var(--gray-700);
  margin-bottom: 1rem;
}

.quiz-actions {
  display: flex;
  gap: 0.75rem;
}

.quiz-actions :deep(.edu-btn) {
  flex: 1;
  height: 40px;
}

.records-table {
  overflow: hidden;
  padding: 0;
}

.table-header {
  display: grid;
  grid-template-columns: 2fr 1fr 1fr 1fr 1fr;
  background: rgba(22, 35, 58, 0.04);
  padding: 1rem;
  font-weight: 500;
  border-bottom: 1px solid var(--gray-200);
}

.table-row {
  display: grid;
  grid-template-columns: 2fr 1fr 1fr 1fr 1fr;
  padding: 1rem;
  border-bottom: 1px solid var(--gray-200);
}

.table-row:last-child {
  border-bottom: none;
}

.score {
  font-weight: 500;
  color: var(--gray-900);
}

.accuracy {
  font-weight: 500;
  color: var(--secondary-color);
}

.status-badge {
  padding: 0.25rem 0.75rem;
  border-radius: 20px;
  font-size: 0.8rem;
}

.status-badge.passed {
  background: rgba(31, 157, 85, 0.14);
  color: #0f5a33;
}

.status-badge:not(.passed) {
  background: rgba(214, 69, 69, 0.12);
  color: #8a1f1f;
}

@media (max-width: 768px) {
  .quizzes-grid {
    grid-template-columns: 1fr;
  }
  
  .table-header,
  .table-row {
    grid-template-columns: 1fr;
    gap: 0.5rem;
  }
  
  .quizzes-header {
    flex-direction: column;
    gap: 1rem;
    align-items: flex-start;
  }
}
</style>
