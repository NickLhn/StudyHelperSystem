<template>
  <div class="page-stack">
    <section class="page-intro">
      <div class="page-intro-copy">
        <span class="page-eyebrow">Homework Detail</span>
        <h2 class="page-title">{{ homework?.title || '作业详情' }}</h2>
        <p class="page-subtitle">{{ homework?.courseName || '正在读取课程信息' }}</p>
      </div>
      <div class="page-actions">
        <router-link to="/teacher/homeworks" class="edu-btn edu-btn-secondary">返回作业列表</router-link>
      </div>
    </section>

    <section v-if="loading" class="loading-panel">
      <p class="loading-copy">作业详情加载中...</p>
    </section>

    <template v-else-if="homework">
      <section class="stats-grid">
        <article class="stat-panel">
          <div class="stat-kicker">提交率</div>
          <div class="stat-value">{{ summary.submissionRate || 0 }}%</div>
          <p class="stat-copy">{{ summary.submittedCount || 0 }} / {{ summary.assignedCount || 0 }} 人已提交</p>
        </article>
        <article class="stat-panel">
          <div class="stat-kicker">平均分</div>
          <div class="stat-value">{{ summary.averageScore || 0 }}</div>
          <p class="stat-copy">当前所有提交记录的平均得分</p>
        </article>
        <article class="stat-panel">
          <div class="stat-kicker">待复核</div>
          <div class="stat-value">{{ pendingReviewCount }}</div>
          <p class="stat-copy">主观题或关闭自动判分的提交会出现在这里</p>
        </article>
        <article class="stat-panel">
          <div class="stat-kicker">题目数</div>
          <div class="stat-value">{{ questions.length }}</div>
          <p class="stat-copy">作业总题数 {{ homework.totalScore }} 分</p>
        </article>
      </section>

      <section class="panel-grid two-up">
        <article class="info-card">
          <div class="stack-md">
            <div>
              <h3 class="section-title">题目概览</h3>
              <p class="section-copy">先看题目结构和自动批改范围，便于后面判断复核重点。</p>
            </div>
            <div class="question-overview-list">
              <div v-for="question in questions" :key="question.id" class="question-overview-item">
                <strong>{{ question.sortOrder }}. {{ question.content }}</strong>
                <span>{{ question.typeLabel }} · {{ question.score }} 分</span>
              </div>
            </div>
          </div>
        </article>

        <article class="info-card">
          <div class="stack-md">
            <div>
              <h3 class="section-title">题目正确率</h3>
              <p class="section-copy">先用题目维度判断哪几题最需要复讲或人工关注。</p>
            </div>
            <div v-if="questionAccuracy.length === 0" class="message-banner">当前还没有提交记录。</div>
            <div v-else class="accuracy-list">
              <div v-for="item in questionAccuracy" :key="item.questionId" class="accuracy-item">
                <span>{{ item.content }}</span>
                <strong>{{ item.accuracy }}%</strong>
              </div>
            </div>
          </div>
        </article>
      </section>

      <section class="info-card">
        <div class="table-header-row">
          <div>
            <h3 class="section-title">学生提交记录</h3>
            <p class="section-copy">点击某条提交记录可以查看答案，并对待复核题目进行教师评分。</p>
          </div>
        </div>

        <div v-if="submissions.length === 0" class="empty-panel compact">
          <h3 class="empty-title">还没有学生提交</h3>
        </div>

        <div v-else class="table-shell">
          <table class="detail-table">
            <thead>
              <tr>
                <th>学生</th>
                <th>状态</th>
                <th>总分</th>
                <th>客观分</th>
                <th>提交时间</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="submission in submissions" :key="submission.id">
                <td>{{ submission.studentName }}</td>
                <td>{{ submission.statusLabel }}</td>
                <td>{{ submission.finalScore }}</td>
                <td>{{ submission.objectiveScore }}</td>
                <td>{{ formatDateTime(submission.submittedAt) }}</td>
                <td>
                  <button type="button" class="edu-btn edu-btn-secondary" @click="selectSubmission(submission.id)">
                    查看详情
                  </button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>

      <section v-if="selectedSubmission" class="info-card">
        <div class="stack-md">
          <div>
            <h3 class="section-title">提交详情：{{ selectedSubmission.submission.studentName }}</h3>
            <p class="section-copy">
              当前状态：{{ selectedSubmission.submission.statusLabel }}，总分 {{ selectedSubmission.submission.finalScore }} 分
            </p>
          </div>

          <div class="answer-list">
            <article v-for="answer in selectedSubmission.answers" :key="answer.answerId" class="answer-card">
              <div class="toolbar-row">
                <strong>{{ answer.questionContent }}</strong>
                <span class="chip">{{ answer.questionTypeLabel }}</span>
              </div>
              <div class="answer-grid">
                <div class="answer-block">
                  <span class="answer-label">学生答案</span>
                  <strong>{{ answer.studentAnswer || '未作答' }}</strong>
                </div>
                <div class="answer-block">
                  <span class="answer-label">标准答案</span>
                  <strong>{{ answer.standardAnswer || '教师复核题' }}</strong>
                </div>
                <div class="answer-block">
                  <span class="answer-label">当前得分</span>
                  <strong>{{ answer.scoreAwarded }} / {{ answer.questionScore }}</strong>
                </div>
              </div>

              <div v-if="answer.analysis" class="analysis-block">
                <span class="answer-label">解析</span>
                <p>{{ answer.analysis }}</p>
              </div>

              <div v-if="answer.needsReview" class="review-grid">
                <div class="field">
                  <label>教师评分</label>
                  <input v-model.number="reviewForm[answer.answerId].teacherScore" type="number" min="0" :max="answer.questionScore" />
                </div>
                <div class="field">
                  <label>评语</label>
                  <textarea v-model="reviewForm[answer.answerId].teacherComment" rows="2" placeholder="可选：填写复核说明"></textarea>
                </div>
              </div>
            </article>
          </div>

          <div class="field">
            <label>总体评语</label>
            <textarea v-model="teacherComment" rows="3" placeholder="可选：输入整体评语"></textarea>
          </div>

          <div class="toolbar-row">
            <button type="button" class="edu-btn edu-btn-primary" :disabled="reviewLoading" @click="submitReview">
              {{ reviewLoading ? '保存中...' : '保存教师复核' }}
            </button>
          </div>
        </div>
      </section>
    </template>

    <section v-if="error" class="message-banner error">{{ error }}</section>
    <section v-if="success" class="message-banner success">{{ success }}</section>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { useUserStore } from '../stores/user'
import { homeworkApi } from '../api/homework'

const route = useRoute()
const userStore = useUserStore()

const loading = ref(false)
const reviewLoading = ref(false)
const error = ref('')
const success = ref('')
const homework = ref(null)
const questions = ref([])
const submissions = ref([])
const summary = ref({})
const selectedSubmission = ref(null)
const reviewForm = reactive({})
const teacherComment = ref('')

const questionAccuracy = computed(() => summary.value.questionAccuracy || [])
const pendingReviewCount = computed(() => submissions.value.filter((item) => item.status === 'REVIEW_PENDING').length)

const loadHomework = async () => {
  loading.value = true
  error.value = ''
  try {
    const [detailRes, submissionsRes, summaryRes] = await Promise.all([
      homeworkApi.getHomeworkDetail(route.params.id, userStore.user.id),
      homeworkApi.getHomeworkSubmissions(route.params.id, userStore.user.id),
      homeworkApi.getHomeworkSummary(route.params.id, userStore.user.id)
    ])

    if (detailRes.data.code === 200) {
      homework.value = detailRes.data.data.homework
      questions.value = detailRes.data.data.questions || []
    }
    if (submissionsRes.data.code === 200) {
      submissions.value = submissionsRes.data.data
    }
    if (summaryRes.data.code === 200) {
      summary.value = summaryRes.data.data
    }
  } catch (err) {
    error.value = '加载作业详情失败'
  } finally {
    loading.value = false
  }
}

const selectSubmission = async (submissionId) => {
  success.value = ''
  try {
    const response = await homeworkApi.getSubmissionDetail(submissionId, userStore.user.id)
    if (response.data.code === 200) {
      selectedSubmission.value = response.data.data
      teacherComment.value = response.data.data.submission.teacherComment || ''
      Object.keys(reviewForm).forEach((key) => delete reviewForm[key])
      response.data.data.answers.forEach((answer) => {
        reviewForm[answer.answerId] = {
          teacherScore: answer.teacherScore ?? answer.scoreAwarded ?? 0,
          teacherComment: answer.teacherComment || ''
        }
      })
    } else {
      error.value = response.data.message
    }
  } catch (err) {
    error.value = '加载提交详情失败'
  }
}

const submitReview = async () => {
  if (!selectedSubmission.value) return
  reviewLoading.value = true
  error.value = ''
  success.value = ''
  try {
    const payload = {
      teacherComment: teacherComment.value,
      answers: selectedSubmission.value.answers
        .filter((answer) => answer.needsReview)
        .map((answer) => ({
          answerId: answer.answerId,
          teacherScore: Number(reviewForm[answer.answerId]?.teacherScore ?? 0),
          teacherComment: reviewForm[answer.answerId]?.teacherComment || ''
        }))
    }

    const response = await homeworkApi.reviewSubmission(selectedSubmission.value.submission.id, userStore.user.id, payload)
    if (response.data.code === 200) {
      success.value = '教师复核已保存'
      selectedSubmission.value = response.data.data
      await loadHomework()
    } else {
      error.value = response.data.message
    }
  } catch (err) {
    error.value = '保存复核失败'
  } finally {
    reviewLoading.value = false
  }
}

const formatDateTime = (value) => {
  if (!value) return '未记录'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

onMounted(loadHomework)
</script>

<style scoped>
.question-overview-list,
.accuracy-list,
.answer-list {
  display: grid;
  gap: 12px;
}

.question-overview-item,
.accuracy-item,
.answer-card {
  display: grid;
  gap: 6px;
  padding: 14px 16px;
  border-radius: 18px;
  border: 1px solid rgba(23, 32, 51, 0.08);
  background: rgba(255, 255, 255, 0.72);
}

.table-shell {
  overflow-x: auto;
}

.detail-table {
  width: 100%;
  border-collapse: collapse;
}

.detail-table th,
.detail-table td {
  padding: 14px 12px;
  border-bottom: 1px solid rgba(23, 32, 51, 0.08);
  text-align: left;
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

.review-grid {
  display: grid;
  gap: 12px;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
}

.compact {
  padding: 20px;
}
</style>
