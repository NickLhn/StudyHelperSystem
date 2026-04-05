<template>
  <div class="page-stack">
    <section class="page-intro">
      <div class="page-intro-copy">
        <span class="page-eyebrow">Homework</span>
        <h2 class="page-title">{{ homework?.title || '作业作答' }}</h2>
        <p class="page-subtitle">{{ homework?.courseName || '正在加载作业信息' }}</p>
      </div>
      <div class="page-actions">
        <router-link to="/student/homeworks" class="edu-btn edu-btn-secondary">返回作业列表</router-link>
      </div>
    </section>

    <section v-if="loading" class="loading-panel">
      <p class="loading-copy">作业内容加载中...</p>
    </section>

    <template v-else-if="homework">
      <section class="stats-grid">
        <article class="stat-panel">
          <div class="stat-kicker">截止时间</div>
          <div class="stat-value stat-value-sm">{{ formatDateTime(homework.deadlineAt) }}</div>
          <p class="stat-copy">提交前请确认答题内容完整。</p>
        </article>
        <article class="stat-panel">
          <div class="stat-kicker">题目数</div>
          <div class="stat-value">{{ questions.length }}</div>
          <p class="stat-copy">总分 {{ homework.totalScore }} 分</p>
        </article>
        <article class="stat-panel">
          <div class="stat-kicker">自动批改</div>
          <div class="stat-value">{{ homework.autoGradeEnabled ? '开启' : '关闭' }}</div>
          <p class="stat-copy">主观题默认进入教师复核。</p>
        </article>
      </section>

      <section v-if="latestSubmission" class="info-card">
        <div class="stack-md">
          <div>
            <h3 class="section-title">你已经提交过这份作业</h3>
            <p class="section-copy">当前状态：{{ latestSubmission.statusLabel }}，得分 {{ latestSubmission.finalScore }} 分。</p>
          </div>
          <div class="toolbar-row">
            <router-link :to="`/student/homework/result/${latestSubmission.id}`" class="edu-btn edu-btn-primary">查看作业结果</router-link>
          </div>
        </div>
      </section>

      <section v-else class="info-card">
        <form class="form-grid" @submit.prevent="submitHomework">
          <article v-for="question in questions" :key="question.id" class="question-card">
            <div class="toolbar-row">
              <strong>{{ question.sortOrder }}. {{ question.content }}</strong>
              <span class="chip">{{ question.typeLabel }} · {{ question.score }} 分</span>
            </div>

            <div v-if="parseOptions(question).length > 0" class="option-list">
              <label v-for="(option, index) in parseOptions(question)" :key="`${question.id}-${index}`" class="option-item">
                <input
                  v-model="answers[question.id]"
                  type="radio"
                  :name="`question-${question.id}`"
                  :value="optionValue(question, index)"
                />
                <span>{{ optionLabel(question, index) }}. {{ option }}</span>
              </label>
            </div>

            <div v-else class="field">
              <label>{{ question.type === 'SHORT_ANSWER' ? '请输入你的作答内容' : '请输入答案' }}</label>
              <textarea
                v-model="answers[question.id]"
                :rows="question.type === 'SHORT_ANSWER' ? 4 : 2"
                :placeholder="question.type === 'SHORT_ANSWER' ? '请输入你的简答内容' : '请输入你的答案'"
              ></textarea>
            </div>
          </article>

          <div v-if="error" class="message-banner error">{{ error }}</div>
          <div class="toolbar-row">
            <button type="submit" class="edu-btn edu-btn-primary" :disabled="submitting">
              {{ submitting ? '提交中...' : '提交作业' }}
            </button>
          </div>
        </form>
      </section>
    </template>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { homeworkApi } from '../api/homework'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const submitting = ref(false)
const error = ref('')
const homework = ref(null)
const questions = ref([])
const latestSubmission = ref(null)
const answers = ref({})

const loadHomework = async () => {
  loading.value = true
  error.value = ''
  try {
    const response = await homeworkApi.getHomeworkDetail(route.params.id, userStore.user.id)
    if (response.data.code === 200) {
      homework.value = response.data.data.homework
      questions.value = response.data.data.questions || []
      latestSubmission.value = response.data.data.latestSubmission || null
    } else {
      error.value = response.data.message
    }
  } catch (err) {
    error.value = '加载作业失败'
  } finally {
    loading.value = false
  }
}

const parseOptions = (question) => {
  if (!question.options) return []
  try {
    const parsed = JSON.parse(question.options)
    return Array.isArray(parsed) ? parsed : []
  } catch {
    return []
  }
}

const optionLabel = (question, index) => {
  return question.type === 'TRUE_FALSE' ? (index === 0 ? 'A' : 'B') : String.fromCharCode(65 + index)
}

const optionValue = (question, index) => {
  return question.type === 'TRUE_FALSE' ? (index === 0 ? 'A' : 'B') : String.fromCharCode(65 + index)
}

const submitHomework = async () => {
  error.value = ''
  for (const question of questions.value) {
    if (!String(answers.value[question.id] || '').trim()) {
      error.value = `第 ${question.sortOrder} 题尚未作答`
      return
    }
  }

  submitting.value = true
  try {
    const payload = {
      answers: questions.value.map((question) => ({
        questionId: question.id,
        answer: answers.value[question.id]
      }))
    }

    const response = await homeworkApi.submitHomework(route.params.id, userStore.user.id, payload)
    if (response.data.code === 200) {
      router.push(`/student/homework/result/${response.data.data.submission.id}`)
    } else {
      error.value = response.data.message
    }
  } catch (err) {
    error.value = '提交作业失败'
  } finally {
    submitting.value = false
  }
}

const formatDateTime = (value) => {
  if (!value) return '未设置'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value
  return `${date.getMonth() + 1}/${date.getDate()} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

onMounted(loadHomework)
</script>

<style scoped>
.stat-value-sm {
  font-size: clamp(1.05rem, 2vw, 1.5rem);
}

.question-card {
  display: grid;
  gap: 16px;
  padding: 20px;
  border-radius: 22px;
  border: 1px solid rgba(23, 32, 51, 0.08);
  background: rgba(255, 255, 255, 0.76);
}

.option-list {
  display: grid;
  gap: 10px;
}

.option-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 14px;
  border-radius: 16px;
  background: rgba(23, 32, 51, 0.04);
}
</style>
