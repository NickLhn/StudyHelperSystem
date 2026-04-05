<template>
  <div class="page-stack quiz-taking-page">
    <section class="page-intro">
      <div class="page-intro-copy">
        <span class="page-eyebrow">Quiz Session</span>
        <h2 class="page-title">{{ quiz?.title || '测验作答' }}</h2>
        <p class="page-subtitle">
          {{ error || '系统会自动保存答题进度，限时测验刷新页面后仍会继续计时。' }}
        </p>
      </div>
      <div v-if="quiz" class="page-actions">
        <span class="chip" :class="timeLeft <= 300 && quiz.totalTime > 0 ? 'danger' : 'neutral'">
          {{ quiz.totalTime > 0 ? `剩余 ${formatTime(timeLeft)}` : '不限时' }}
        </span>
        <span class="chip">剩余次数 {{ quiz.remainingAttempts }}</span>
        <span class="chip" v-if="quiz.shuffleQuestions">随机题序</span>
        <span class="chip" v-if="quiz.autoSaveEnabled">自动保存</span>
      </div>
    </section>

    <section v-if="loading" class="loading-panel">
      <p class="loading-copy">测验加载中...</p>
    </section>

    <section v-else-if="error" class="message-banner error">
      {{ error }}
    </section>

    <template v-else-if="quiz && questions.length">
      <section class="anti-cheat-summary">
        <div class="summary-card">
          <span class="summary-label">进度</span>
          <strong>{{ answeredCount }}/{{ questions.length }}</strong>
        </div>
        <div class="summary-card">
          <span class="summary-label">限制次数</span>
          <strong>{{ quiz.maxAttempts }} 次</strong>
        </div>
        <div class="summary-card">
          <span class="summary-label">预计用时</span>
          <strong>{{ quiz.totalTime > 0 ? `${quiz.totalTime} 分钟` : '自由作答' }}</strong>
        </div>
        <div class="summary-card">
          <span class="summary-label">当前耗时</span>
          <strong>{{ formatTime(timeUsed) }}</strong>
        </div>
      </section>

      <section class="quiz-shell">
        <aside class="navigator-card">
          <div class="navigator-head">
            <h3>答题导航</h3>
            <p>点击题号可快速定位</p>
          </div>
          <div class="question-dots">
            <button
              v-for="(question, index) in questions"
              :key="question.id"
              type="button"
              :class="['question-dot', {
                active: index === currentQuestionIndex,
                answered: Boolean(userAnswers[question.id]),
              }]"
              @click="goToQuestion(index)"
            >
              {{ index + 1 }}
            </button>
          </div>
          <div class="navigator-actions">
            <button type="button" class="edu-btn edu-btn-secondary" @click="saveDraftNow" :disabled="!quiz.autoSaveEnabled">
              {{ quiz.autoSaveEnabled ? '立即保存' : '未启用自动保存' }}
            </button>
            <button type="button" class="edu-btn edu-btn-primary" @click="submitQuiz(false)" :disabled="submitting">
              {{ submitting ? '提交中...' : '提交测验' }}
            </button>
          </div>
        </aside>

        <article class="question-card">
          <div class="question-card-head">
            <div>
              <span class="question-index">第 {{ currentQuestionIndex + 1 }} 题</span>
              <h3>{{ currentQuestion.content }}</h3>
            </div>
            <span class="score-pill">{{ currentQuestion.score }} 分</span>
          </div>

          <div class="answer-list">
            <button
              v-for="(option, index) in currentOptions"
              :key="`${currentQuestion.id}-${index}`"
              type="button"
              :class="['answer-option', { selected: userAnswers[currentQuestion.id] === getOptionKey(index, currentQuestion.type) }]"
              @click="selectAnswer(getOptionKey(index, currentQuestion.type))"
            >
              <span class="option-key">{{ getOptionKey(index, currentQuestion.type) }}</span>
              <span>{{ option }}</span>
            </button>
          </div>

          <div class="question-footer">
            <button type="button" class="edu-btn edu-btn-secondary" @click="prevQuestion" :disabled="currentQuestionIndex === 0">
              上一题
            </button>
            <div class="footer-copy">
              <span v-if="quiz.autoSaveEnabled">上次保存：{{ lastSavedLabel }}</span>
            </div>
            <button type="button" class="edu-btn edu-btn-primary" @click="nextQuestion" :disabled="currentQuestionIndex === questions.length - 1">
              下一题
            </button>
          </div>
        </article>
      </section>
    </template>
  </div>
</template>

<script setup>
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { quizApi } from '../api/quiz'
import { useUserStore } from '../stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const quiz = ref(null)
const questions = ref([])
const userAnswers = ref({})
const currentQuestionIndex = ref(0)
const loading = ref(false)
const error = ref('')
const submitting = ref(false)
const timeLeft = ref(0)
const timer = ref(null)
const startTimestamp = ref(null)
const lastSavedAt = ref(null)

const currentQuestion = computed(() => questions.value[currentQuestionIndex.value] || {})
const answeredCount = computed(() => Object.keys(userAnswers.value).filter((key) => userAnswers.value[key]).length)
const timeUsed = computed(() => {
  if (!startTimestamp.value) return 0
  return Math.max(0, Math.floor((Date.now() - startTimestamp.value) / 1000))
})
const lastSavedLabel = computed(() => {
  if (!lastSavedAt.value) return '未保存'
  return new Date(lastSavedAt.value).toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit', second: '2-digit' })
})
const currentOptions = computed(() => {
  if (!currentQuestion.value?.type) return []
  if (currentQuestion.value.type === 'TRUE_FALSE') {
    return ['正确', '错误']
  }
  try {
    return JSON.parse(currentQuestion.value.options || '[]')
  } catch {
    return []
  }
})

const draftKey = () => `quiz-progress-${userStore.user?.id}-${route.params.id}`

const getOptionKey = (index, type) => {
  if (type === 'TRUE_FALSE') {
    return index === 0 ? 'A' : 'B'
  }
  return String.fromCharCode(65 + index)
}

const formatTime = (seconds = 0) => {
  const mins = Math.floor(seconds / 60)
  const secs = seconds % 60
  return `${String(mins).padStart(2, '0')}:${String(secs).padStart(2, '0')}`
}

const restoreDraft = () => {
  if (!quiz.value?.autoSaveEnabled) {
    startTimestamp.value = Date.now()
    return
  }

  try {
    const raw = localStorage.getItem(draftKey())
    if (!raw) {
      startTimestamp.value = Date.now()
      return
    }

    const parsed = JSON.parse(raw)
    userAnswers.value = parsed.answers || {}
    currentQuestionIndex.value = Math.min(parsed.currentQuestionIndex || 0, Math.max(questions.value.length - 1, 0))
    startTimestamp.value = parsed.startedAt || Date.now()
    lastSavedAt.value = parsed.updatedAt || null
  } catch {
    startTimestamp.value = Date.now()
  }
}

const saveDraftNow = () => {
  if (!quiz.value?.autoSaveEnabled) return
  const payload = {
    answers: userAnswers.value,
    currentQuestionIndex: currentQuestionIndex.value,
    startedAt: startTimestamp.value || Date.now(),
    updatedAt: Date.now(),
  }
  localStorage.setItem(draftKey(), JSON.stringify(payload))
  lastSavedAt.value = payload.updatedAt
}

const clearDraft = () => {
  localStorage.removeItem(draftKey())
}

const syncTimer = () => {
  if (!quiz.value || quiz.value.totalTime <= 0) return
  const elapsed = timeUsed.value
  const remaining = Math.max(0, quiz.value.totalTime * 60 - elapsed)
  timeLeft.value = remaining
  if (remaining === 0 && !submitting.value) {
    submitQuiz(true)
  }
}

const startTimer = () => {
  if (!quiz.value || quiz.value.totalTime <= 0) return
  syncTimer()
  timer.value = window.setInterval(syncTimer, 1000)
}

const fetchQuiz = async () => {
  loading.value = true
  error.value = ''

  try {
    const response = await quizApi.getQuizDetail(route.params.id, userStore.user.id)
    if (response.data.code !== 200) {
      error.value = response.data.message
      return
    }

    quiz.value = response.data.data.quiz
    questions.value = response.data.data.questions
    restoreDraft()
    if (!startTimestamp.value) {
      startTimestamp.value = Date.now()
    }
    if (quiz.value.autoSaveEnabled) {
      saveDraftNow()
    }
    startTimer()
  } catch (err) {
    error.value = '测验加载失败，请稍后重试'
  } finally {
    loading.value = false
  }
}

const selectAnswer = (answer) => {
  userAnswers.value = {
    ...userAnswers.value,
    [currentQuestion.value.id]: answer,
  }
  saveDraftNow()
}

const goToQuestion = (index) => {
  currentQuestionIndex.value = index
  saveDraftNow()
}

const prevQuestion = () => {
  if (currentQuestionIndex.value > 0) {
    currentQuestionIndex.value -= 1
    saveDraftNow()
  }
}

const nextQuestion = () => {
  if (currentQuestionIndex.value < questions.value.length - 1) {
    currentQuestionIndex.value += 1
    saveDraftNow()
  }
}

const submitQuiz = async (forced) => {
  if (!quiz.value || submitting.value) return
  if (!forced && !window.confirm('确认提交当前测验吗？提交后本次作答将计入次数。')) {
    return
  }

  submitting.value = true
  if (timer.value) {
    window.clearInterval(timer.value)
    timer.value = null
  }

  try {
    const response = await quizApi.submitQuiz(quiz.value.id, userStore.user.id, {
      answers: userAnswers.value,
      timeUsed: timeUsed.value,
      autoSubmitted: forced,
    })

    if (response.data.code !== 200) {
      error.value = response.data.message
      startTimer()
      return
    }

    clearDraft()
    router.push(`/quiz/result/${response.data.data.recordId}`)
  } catch (err) {
    error.value = forced ? '自动交卷失败，请重新进入测验确认结果' : '提交失败，请稍后重试'
    startTimer()
  } finally {
    submitting.value = false
  }
}

const handleBeforeUnload = () => {
  saveDraftNow()
}

onMounted(() => {
  fetchQuiz()
  window.addEventListener('beforeunload', handleBeforeUnload)
})

onUnmounted(() => {
  if (timer.value) {
    window.clearInterval(timer.value)
  }
  window.removeEventListener('beforeunload', handleBeforeUnload)
})
</script>

<style scoped>
.quiz-taking-page {
  padding-bottom: 32px;
}

.anti-cheat-summary {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
}

.summary-card,
.navigator-card,
.question-card {
  border-radius: 24px;
  border: 1px solid rgba(23, 32, 51, 0.08);
  background: rgba(255, 255, 255, 0.86);
  box-shadow: 0 20px 45px rgba(15, 23, 42, 0.08);
}

.summary-card {
  padding: 18px 20px;
  display: grid;
  gap: 8px;
}

.summary-label {
  color: var(--text-muted);
  font-size: 0.9rem;
}

.quiz-shell {
  display: grid;
  grid-template-columns: 280px minmax(0, 1fr);
  gap: 20px;
}

.navigator-card {
  padding: 20px;
  display: grid;
  gap: 18px;
  align-self: start;
  position: sticky;
  top: 24px;
}

.navigator-head h3 {
  margin: 0 0 8px;
}

.navigator-head p,
.footer-copy {
  margin: 0;
  color: var(--text-muted);
  font-size: 0.92rem;
}

.question-dots {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 10px;
}

.question-dot {
  border: 1px solid rgba(23, 32, 51, 0.12);
  background: white;
  border-radius: 14px;
  min-height: 42px;
  font-weight: 700;
  cursor: pointer;
}

.question-dot.answered {
  background: rgba(15, 118, 110, 0.12);
  color: #0f766e;
}

.question-dot.active {
  background: var(--brand-strong);
  color: white;
  border-color: var(--brand-strong);
}

.navigator-actions {
  display: grid;
  gap: 10px;
}

.question-card {
  padding: 24px;
  display: grid;
  gap: 20px;
}

.question-card-head {
  display: flex;
  justify-content: space-between;
  gap: 16px;
}

.question-card-head h3 {
  margin: 8px 0 0;
  font-size: 1.35rem;
  line-height: 1.6;
}

.question-index {
  color: var(--brand-strong);
  font-weight: 700;
}

.score-pill {
  align-self: start;
  padding: 8px 12px;
  border-radius: 999px;
  background: rgba(15, 23, 42, 0.06);
  font-weight: 700;
}

.answer-list {
  display: grid;
  gap: 12px;
}

.answer-option {
  display: flex;
  gap: 14px;
  width: 100%;
  text-align: left;
  padding: 16px 18px;
  border-radius: 18px;
  border: 1px solid rgba(23, 32, 51, 0.12);
  background: white;
  cursor: pointer;
  transition: transform 0.16s ease, border-color 0.16s ease, box-shadow 0.16s ease;
}

.answer-option:hover {
  transform: translateY(-1px);
  border-color: rgba(22, 163, 74, 0.4);
}

.answer-option.selected {
  border-color: rgba(22, 163, 74, 0.56);
  background: rgba(22, 163, 74, 0.08);
  box-shadow: 0 10px 24px rgba(22, 163, 74, 0.1);
}

.option-key {
  min-width: 28px;
  font-weight: 800;
  color: var(--brand-strong);
}

.question-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
}

@media (max-width: 960px) {
  .anti-cheat-summary {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .quiz-shell {
    grid-template-columns: 1fr;
  }

  .navigator-card {
    position: static;
  }
}

@media (max-width: 640px) {
  .anti-cheat-summary {
    grid-template-columns: 1fr;
  }

  .question-card-head,
  .question-footer {
    flex-direction: column;
    align-items: stretch;
  }
}
</style>
