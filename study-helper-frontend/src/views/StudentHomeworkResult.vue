<template>
  <div class="page-stack">
    <section class="page-intro">
      <div class="page-intro-copy">
        <span class="page-eyebrow">Homework Result</span>
        <h2 class="page-title">{{ homework?.title || '作业结果' }}</h2>
        <p class="page-subtitle">{{ submission?.statusLabel || '正在加载结果' }}</p>
      </div>
      <div class="page-actions">
        <router-link to="/student/homeworks" class="edu-btn edu-btn-secondary">返回作业列表</router-link>
      </div>
    </section>

    <section v-if="loading" class="loading-panel">
      <p class="loading-copy">作业结果加载中...</p>
    </section>

    <template v-else-if="submission">
      <section class="stats-grid">
        <article class="stat-panel">
          <div class="stat-kicker">总分</div>
          <div class="stat-value">{{ submission.finalScore }}</div>
          <p class="stat-copy">满分 {{ homework.totalScore }} 分</p>
        </article>
        <article class="stat-panel">
          <div class="stat-kicker">客观题得分</div>
          <div class="stat-value">{{ submission.objectiveScore }}</div>
          <p class="stat-copy">自动批改已完成的分值</p>
        </article>
        <article class="stat-panel">
          <div class="stat-kicker">主观题得分</div>
          <div class="stat-value">{{ submission.subjectiveScore }}</div>
          <p class="stat-copy">需要教师复核的题目会在这里更新</p>
        </article>
        <article class="stat-panel">
          <div class="stat-kicker">提交时间</div>
          <div class="stat-value stat-value-sm">{{ formatDateTime(submission.submittedAt) }}</div>
          <p class="stat-copy">状态：{{ submission.statusLabel }}</p>
        </article>
      </section>

      <section class="info-card">
        <div class="stack-md">
          <div>
            <h3 class="section-title">作答明细</h3>
            <p class="section-copy">你可以看到每道题的作答情况、标准答案和当前得分。</p>
          </div>

          <div class="answer-list">
            <article v-for="answer in answers" :key="answer.answerId" class="answer-card">
              <div class="toolbar-row">
                <strong>{{ answer.questionContent }}</strong>
                <span class="chip">{{ answer.questionTypeLabel }}</span>
              </div>

              <div class="answer-grid">
                <div class="answer-block">
                  <span class="answer-label">你的答案</span>
                  <strong>{{ answer.studentAnswer || '未作答' }}</strong>
                </div>
                <div class="answer-block">
                  <span class="answer-label">标准答案</span>
                  <strong>{{ answer.standardAnswer || '待教师复核' }}</strong>
                </div>
                <div class="answer-block">
                  <span class="answer-label">得分</span>
                  <strong>{{ answer.scoreAwarded }} / {{ answer.questionScore }}</strong>
                </div>
              </div>

              <div v-if="answer.analysis" class="analysis-block">
                <span class="answer-label">解析</span>
                <p>{{ answer.analysis }}</p>
              </div>

              <div v-if="answer.teacherComment" class="analysis-block">
                <span class="answer-label">教师备注</span>
                <p>{{ answer.teacherComment }}</p>
              </div>
            </article>
          </div>
        </div>
      </section>
    </template>

    <section v-if="error" class="message-banner error">{{ error }}</section>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { useUserStore } from '../stores/user'
import { homeworkApi } from '../api/homework'

const route = useRoute()
const userStore = useUserStore()

const loading = ref(false)
const error = ref('')
const homework = ref(null)
const submission = ref(null)
const answers = ref([])

const loadSubmission = async () => {
  loading.value = true
  error.value = ''
  try {
    const response = await homeworkApi.getSubmissionDetail(route.params.submissionId, userStore.user.id)
    if (response.data.code === 200) {
      homework.value = response.data.data.homework
      submission.value = response.data.data.submission
      answers.value = response.data.data.answers || []
    } else {
      error.value = response.data.message
    }
  } catch (err) {
    error.value = '加载作业结果失败'
  } finally {
    loading.value = false
  }
}

const formatDateTime = (value) => {
  if (!value) return '未记录'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

onMounted(loadSubmission)
</script>

<style scoped>
.stat-value-sm {
  font-size: clamp(1.05rem, 2vw, 1.4rem);
}

.answer-list {
  display: grid;
  gap: 12px;
}

.answer-card {
  display: grid;
  gap: 12px;
  padding: 16px 18px;
  border-radius: 18px;
  border: 1px solid rgba(23, 32, 51, 0.08);
  background: rgba(255, 255, 255, 0.74);
}

.answer-grid {
  display: grid;
  gap: 12px;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
}

.answer-block,
.analysis-block {
  display: grid;
  gap: 6px;
}

.answer-label {
  color: var(--gray-500);
  font-size: 12px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.08em;
}

.analysis-block p {
  margin: 0;
  color: var(--gray-700);
  line-height: 1.7;
}
</style>
