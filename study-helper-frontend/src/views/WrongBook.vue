<template>
  <div class="page-stack">
    <section class="page-intro">
      <div class="page-intro-copy">
        <span class="page-eyebrow">Wrong Question Book</span>
        <h2 class="page-title">把错题整理成一份能持续复习的个人题单</h2>
        <p class="page-subtitle">现在已经能自动读取测验错题，你可以按测验筛选、标记已掌握，并快速回到测验页继续训练。</p>
      </div>
      <div class="page-actions">
        <span class="chip">共 {{ filteredQuestions.length }} 题</span>
      </div>
    </section>

    <section class="filter-card">
      <div class="toolbar-row">
        <label class="sr-only" for="wrong-book-filter">按测验筛选</label>
        <select id="wrong-book-filter" v-model="selectedSubject">
          <option value="">全部测验</option>
          <option v-for="subject in subjects" :key="subject" :value="subject">
            {{ subject }}
          </option>
        </select>
        <router-link to="/student/courses" class="edu-btn edu-btn-secondary">继续学习</router-link>
      </div>
    </section>

    <section v-if="loading" class="loading-panel">
      <p class="loading-copy">错题记录加载中...</p>
    </section>

    <section v-else-if="filteredQuestions.length === 0" class="empty-panel">
      <h3 class="empty-title">当前还没有错题记录</h3>
      <p class="empty-copy">完成测验后，答错的题目会自动收录到这里，方便你后续集中复习。</p>
    </section>

    <section v-else class="card-grid cards-2">
      <article v-for="item in filteredQuestions" :key="item.recordKey" class="info-card question-card">
        <div class="entity-card-header">
          <div>
            <h3 class="entity-card-title">{{ item.quizTitle }}</h3>
            <p class="entity-card-subtitle">{{ formatDate(item.recordedAt) }}</p>
          </div>
          <span class="chip danger">错题</span>
        </div>

        <div class="stack-sm">
          <p class="question-text">{{ item.question.content }}</p>

          <div v-if="parsedOptions(item.question).length > 0" class="option-list">
            <div v-for="(option, index) in parsedOptions(item.question)" :key="`${item.recordKey}-${index}`" class="option-item">
              <strong>{{ String.fromCharCode(65 + index) }}.</strong>
              <span>{{ option }}</span>
            </div>
          </div>

          <div class="answer-block">
            <span class="answer-label">正确答案</span>
            <strong>{{ item.question.answer || '未设置' }}</strong>
          </div>

          <div v-if="item.question.analysis" class="analysis-block">
            <span class="answer-label">解析</span>
            <p>{{ item.question.analysis }}</p>
          </div>
        </div>

        <div class="entity-card-actions">
          <button type="button" class="edu-btn edu-btn-success" @click="markAsMastered(item.recordKey)">
            标记已掌握
          </button>
          <button type="button" class="edu-btn edu-btn-secondary" @click="addToReview(item.quizTitle)">
            加入复习
          </button>
        </div>
      </article>
    </section>

    <section v-if="feedback" class="message-banner" :class="feedbackType">
      {{ feedback }}
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useUserStore } from '../stores/user'
import { quizApi } from '../api/quiz'

const userStore = useUserStore()

const wrongQuestions = ref([])
const selectedSubject = ref('')
const loading = ref(false)
const feedback = ref('')
const feedbackType = ref('success')

const subjects = computed(() => [...new Set(wrongQuestions.value.map((item) => item.quizTitle))])
const filteredQuestions = computed(() => {
  const list = selectedSubject.value
    ? wrongQuestions.value.filter((item) => item.quizTitle === selectedSubject.value)
    : wrongQuestions.value

  return list.map((item, index) => ({
    ...item,
    recordKey: `${item.quizTitle}-${item.recordedAt}-${item.question?.id || index}`
  }))
})

const setFeedback = (message, type = 'success') => {
  feedback.value = message
  feedbackType.value = type
}

const fetchWrongQuestions = async () => {
  loading.value = true
  feedback.value = ''
  try {
    const response = await quizApi.getWrongQuestions(userStore.user.id)
    if (response.data.code === 200) {
      wrongQuestions.value = response.data.data
    } else {
      setFeedback(response.data.message || '获取错题失败', 'error')
    }
  } catch (error) {
    setFeedback('获取错题失败，请稍后再试', 'error')
  } finally {
    loading.value = false
  }
}

const parsedOptions = (question) => {
  if (!question?.options) return []
  try {
    const parsed = JSON.parse(question.options)
    return Array.isArray(parsed) ? parsed : []
  } catch {
    return []
  }
}

const formatDate = (dateStr) => {
  if (!dateStr) return '未记录时间'
  const date = new Date(dateStr)
  if (Number.isNaN(date.getTime())) return dateStr
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

const markAsMastered = (recordKey) => {
  wrongQuestions.value = wrongQuestions.value.filter((item, index) => {
    const key = `${item.quizTitle}-${item.recordedAt}-${item.question?.id || index}`
    return key !== recordKey
  })
  setFeedback('已从当前错题列表中移除，后续可以再做持久化掌握状态。')
}

const addToReview = (quizTitle) => {
  setFeedback(`已加入复习计划建议：下一轮优先回顾“${quizTitle}”相关题目。`)
}

onMounted(() => {
  fetchWrongQuestions()
})
</script>

<style scoped>
.question-card {
  display: grid;
  gap: var(--spacing-md);
}

.stack-sm {
  display: grid;
  gap: 14px;
}

.question-text {
  margin: 0;
  color: var(--ink);
  line-height: 1.8;
  font-weight: 600;
}

.option-list {
  display: grid;
  gap: 10px;
}

.option-item {
  display: flex;
  gap: 10px;
  padding: 12px 14px;
  border-radius: 16px;
  background: rgba(23, 32, 51, 0.04);
  color: var(--gray-700);
}

.answer-block,
.analysis-block {
  display: grid;
  gap: 6px;
  padding: 14px 16px;
  border-radius: 18px;
  border: 1px solid rgba(23, 32, 51, 0.08);
  background: rgba(255, 255, 255, 0.72);
}

.analysis-block p {
  margin: 0;
  color: var(--gray-700);
  line-height: 1.7;
}

.answer-label {
  color: var(--gray-500);
  font-size: 12px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.08em;
}

.chip.danger {
  background: rgba(198, 76, 76, 0.14);
  color: #b73434;
}
</style>
