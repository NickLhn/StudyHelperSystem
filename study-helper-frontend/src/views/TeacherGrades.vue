<template>
  <div class="page-stack">
    <section class="page-intro">
      <div class="page-intro-copy">
        <span class="page-eyebrow">Grade Center</span>
        <h2 class="page-title">教师成绩中心现在会同时读取测验和作业数据</h2>
        <p class="page-subtitle">你可以直接看到课程参与情况、作业提交率、待复核数量，以及当前最需要关注的低正确率题目。</p>
      </div>
      <div class="page-actions">
        <select v-model="selectedCourseId" @change="loadCourseStats">
          <option value="">请选择课程</option>
          <option v-for="course in courses" :key="course.id" :value="String(course.id)">
            {{ course.name }}
          </option>
        </select>
      </div>
    </section>

    <section class="stats-grid">
      <article class="stat-panel">
        <div class="stat-kicker">当前课程</div>
        <div class="stat-value stat-value-sm">{{ selectedCourse?.name || '未选择' }}</div>
        <p class="stat-copy">切换课程后会同步刷新全量成绩面板。</p>
      </article>
      <article class="stat-panel">
        <div class="stat-kicker">学生人数</div>
        <div class="stat-value">{{ stats.studentCount || 0 }}</div>
        <p class="stat-copy">按当前课程选课关系统计。</p>
      </article>
      <article class="stat-panel">
        <div class="stat-kicker">测验数量</div>
        <div class="stat-value">{{ stats.quizCount || 0 }}</div>
        <p class="stat-copy">已创建并归属本课程的测验总数。</p>
      </article>
      <article class="stat-panel">
        <div class="stat-kicker">作业数量</div>
        <div class="stat-value">{{ stats.homeworkCount || 0 }}</div>
        <p class="stat-copy">包括草稿、已发布和已关闭作业。</p>
      </article>
      <article class="stat-panel">
        <div class="stat-kicker">待复核</div>
        <div class="stat-value">{{ pendingHomeworkReviews }}</div>
        <p class="stat-copy">主观题或关闭自动批改后会汇总到这里。</p>
      </article>
    </section>

    <section v-if="loading" class="loading-panel">
      <p class="loading-copy">成绩数据加载中...</p>
    </section>

    <section v-else-if="!selectedCourseId" class="empty-panel">
      <h3 class="empty-title">先选择一门课程</h3>
      <p class="empty-copy">选择后即可查看该课程的测验表现、作业提交率和题目热点。</p>
    </section>

    <template v-else>
      <section class="panel-grid two-up">
        <article class="info-card">
          <div class="stack-md">
            <div>
              <h3 class="section-title">课程参与概览</h3>
              <p class="section-copy">这块把测验、作业和日常资源使用放到一处，方便你快速判断哪门课最需要跟进。</p>
            </div>
            <div class="data-points">
              <div class="data-point">
                <span>资料数</span>
                <strong>{{ stats.materialCount || 0 }}</strong>
              </div>
              <div class="data-point">
                <span>任务数</span>
                <strong>{{ stats.taskCount || 0 }}</strong>
              </div>
              <div class="data-point">
                <span>测验作答</span>
                <strong>{{ totalQuizAttempts }}</strong>
              </div>
              <div class="data-point">
                <span>作业提交</span>
                <strong>{{ totalHomeworkSubmissions }}</strong>
              </div>
              <div class="data-point">
                <span>测验均分</span>
                <strong>{{ averageQuizScoreLabel }}</strong>
              </div>
              <div class="data-point">
                <span>作业均分</span>
                <strong>{{ averageHomeworkScoreLabel }}</strong>
              </div>
              <div class="data-point">
                <span>测验正确率</span>
                <strong>{{ averageQuizAccuracyLabel }}</strong>
              </div>
              <div class="data-point">
                <span>作业正确率</span>
                <strong>{{ averageHomeworkAccuracyLabel }}</strong>
              </div>
            </div>
          </div>
        </article>

        <article class="info-card">
          <div class="stack-md">
            <div>
              <h3 class="section-title">最近学习趋势</h3>
              <p class="section-copy">这里先按天汇总课程学习分钟数，后面继续接通知和复习提醒时，这块还能直接复用。</p>
            </div>
            <div v-if="studyTrend.length === 0" class="message-banner">
              暂无学习记录，等学生产生学习时长后这里会自动更新。
            </div>
            <div v-else class="trend-list">
              <div v-for="item in studyTrend" :key="item.label" class="trend-item">
                <span>{{ item.label }}</span>
                <div class="trend-bar-track">
                  <div class="trend-bar-fill" :style="{ width: `${item.percent}%` }"></div>
                </div>
                <strong>{{ item.minutes }} 分钟</strong>
              </div>
            </div>
          </div>
        </article>
      </section>

      <section v-if="!hasAnyGradeData" class="empty-panel">
        <h3 class="empty-title">当前课程还没有可展示的成绩数据</h3>
        <p class="empty-copy">先发布测验或作业，学生提交后，这里会自动生成成绩概览。</p>
      </section>

      <template v-else>
        <section v-if="homeworkSummaries.length > 0" class="info-card section-stack">
          <div class="table-header-row">
            <div>
              <h3 class="section-title">作业提交与批改</h3>
              <p class="section-copy">这里重点看提交率、平均得分和待复核数量，方便你快速找到需要优先处理的作业。</p>
            </div>
          </div>

          <div class="table-shell">
            <table class="grade-table">
              <thead>
                <tr>
                  <th>作业名称</th>
                  <th>状态</th>
                  <th>提交率</th>
                  <th>提交人数</th>
                  <th>平均得分</th>
                  <th>平均正确率</th>
                  <th>待复核</th>
                  <th>操作</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="homework in homeworkSummaries" :key="homework.homeworkId">
                  <td>
                    <div class="table-main">{{ homework.title }}</div>
                    <div class="table-subtle">
                      总分 {{ homework.totalScore }}，截止 {{ formatDateTime(homework.deadlineAt) }}
                    </div>
                  </td>
                  <td>
                    <span class="chip" :class="getHomeworkStatusClass(homework.status)">
                      {{ homework.statusLabel }}
                    </span>
                  </td>
                  <td>{{ formatPercent(homework.submissionRate) }}</td>
                  <td>{{ homework.submissionCount }}/{{ homework.assignedCount }}</td>
                  <td>{{ homework.averageScore }}</td>
                  <td>{{ formatPercent(homework.averageAccuracy) }}</td>
                  <td>
                    <span class="chip" :class="homework.pendingReviewCount > 0 ? 'warning' : 'success'">
                      {{ homework.pendingReviewCount }}
                    </span>
                  </td>
                  <td>
                    <router-link class="inline-link" :to="`/teacher/homework/${homework.homeworkId}`">
                      查看详情
                    </router-link>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </section>

        <section v-if="quizSummaries.length > 0" class="info-card section-stack">
          <div class="table-header-row">
            <div>
              <h3 class="section-title">测验成绩明细</h3>
              <p class="section-copy">这里保留每场测验的参与人数、平均分和平均正确率，和作业数据分开展示会更清楚。</p>
            </div>
          </div>

          <div class="table-shell">
            <table class="grade-table">
              <thead>
                <tr>
                  <th>测验名称</th>
                  <th>作答次数</th>
                  <th>平均得分</th>
                  <th>平均正确率</th>
                  <th>表现等级</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="quiz in quizSummaries" :key="quiz.quizId">
                  <td>{{ quiz.title }}</td>
                  <td>{{ quiz.attempts }}</td>
                  <td>{{ quiz.avgScore }}</td>
                  <td>{{ formatPercent(quiz.avgAccuracy) }}</td>
                  <td>
                    <span class="chip" :class="getPerformanceClass(quiz.avgAccuracy)">
                      {{ getPerformanceLabel(quiz.avgAccuracy) }}
                    </span>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </section>

        <section v-if="homeworkQuestionInsights.length > 0" class="info-card section-stack">
          <div class="table-header-row">
            <div>
              <h3 class="section-title">作业易错题热点</h3>
              <p class="section-copy">这里优先展示当前课程里正确率最低的客观题，能更快帮助你判断学生卡在什么知识点。</p>
            </div>
          </div>

          <div class="insight-grid">
            <article v-for="item in homeworkQuestionInsights" :key="`${item.homeworkId}-${item.questionId}`" class="insight-item">
              <div class="insight-head">
                <span class="chip" :class="getQuestionRiskClass(item.accuracy)">
                  {{ getQuestionRiskLabel(item.accuracy) }}
                </span>
                <strong>{{ formatPercent(item.accuracy) }}</strong>
              </div>
              <div class="insight-course">{{ item.homeworkTitle }}</div>
              <p class="insight-content">{{ item.content }}</p>
              <div class="trend-bar-track">
                <div class="trend-bar-fill" :style="{ width: `${Math.max(10, item.accuracy)}%` }"></div>
              </div>
              <div class="insight-meta">作答 {{ item.attempts }} 次 · {{ getQuestionTypeLabel(item.type) }}</div>
            </article>
          </div>
        </section>
      </template>
    </template>

    <section v-if="feedback" class="message-banner" :class="feedbackType">
      {{ feedback }}
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useUserStore } from '../stores/user'
import { courseApi } from '../api/course'

const userStore = useUserStore()

const courses = ref([])
const selectedCourseId = ref('')
const stats = ref({})
const loading = ref(false)
const feedback = ref('')
const feedbackType = ref('success')

const selectedCourse = computed(() => courses.value.find((course) => String(course.id) === selectedCourseId.value) || null)
const quizSummaries = computed(() => stats.value.quizSummaries || [])
const homeworkSummaries = computed(() => stats.value.homeworkSummaries || [])
const homeworkQuestionInsights = computed(() => stats.value.homeworkQuestionInsights || [])
const totalQuizAttempts = computed(() => Number(stats.value.quizAttempts || 0))
const totalHomeworkSubmissions = computed(() => Number(stats.value.homeworkSubmissions || 0))
const pendingHomeworkReviews = computed(() => Number(stats.value.pendingHomeworkReviews || 0))
const hasAnyGradeData = computed(() => quizSummaries.value.length > 0 || homeworkSummaries.value.length > 0)

const averageQuizScore = computed(() => {
  if (quizSummaries.value.length === 0) return null
  const sum = quizSummaries.value.reduce((total, quiz) => total + Number(quiz.avgScore || 0), 0)
  return (sum / quizSummaries.value.length).toFixed(1)
})
const averageQuizAccuracy = computed(() => {
  if (quizSummaries.value.length === 0) return null
  const sum = quizSummaries.value.reduce((total, quiz) => total + Number(quiz.avgAccuracy || 0), 0)
  return (sum / quizSummaries.value.length).toFixed(1)
})
const averageQuizScoreLabel = computed(() => averageQuizScore.value ?? '--')
const averageQuizAccuracyLabel = computed(() => averageQuizAccuracy.value ? `${averageQuizAccuracy.value}%` : '--')
const averageHomeworkScoreLabel = computed(() => {
  const value = stats.value.homeworkAverageScore
  return value === undefined || value === null || homeworkSummaries.value.length === 0 ? '--' : Number(value).toFixed(1)
})
const averageHomeworkAccuracyLabel = computed(() => {
  const value = stats.value.homeworkAverageAccuracy
  return value === undefined || value === null || homeworkSummaries.value.length === 0 ? '--' : `${Number(value).toFixed(1)}%`
})
const studyTrend = computed(() => {
  const trend = stats.value.studyTrend || {}
  const labels = trend.labels || []
  const minutes = trend.minutes || []
  const max = Math.max(...minutes, 0)

  return labels.map((label, index) => ({
    label: label.slice(5),
    minutes: minutes[index] || 0,
    percent: max > 0 ? Math.max(8, Math.round(((minutes[index] || 0) / max) * 100)) : 0
  }))
})

const setFeedback = (message, type = 'success') => {
  feedback.value = message
  feedbackType.value = type
}

const loadCourses = async () => {
  try {
    const response = await courseApi.getUserCourses(userStore.user.id)
    if (response.data.code === 200) {
      courses.value = response.data.data
      if (!selectedCourseId.value && courses.value.length > 0) {
        selectedCourseId.value = String(courses.value[0].id)
      }
    } else {
      setFeedback(response.data.message || '获取课程列表失败', 'error')
    }
  } catch (error) {
    setFeedback('获取课程列表失败，请确认后端服务是否正常', 'error')
  }
}

const loadCourseStats = async () => {
  feedback.value = ''

  if (!selectedCourseId.value) {
    stats.value = {}
    return
  }

  loading.value = true
  try {
    const response = await courseApi.getCourseStats(Number(selectedCourseId.value), userStore.user.id, 'month')
    if (response.data.code === 200) {
      stats.value = response.data.data
    } else {
      stats.value = {}
      setFeedback(response.data.message || '获取成绩数据失败', 'error')
    }
  } catch (error) {
    stats.value = {}
    setFeedback('获取成绩数据失败，请稍后再试', 'error')
  } finally {
    loading.value = false
  }
}

const formatPercent = (value) => `${Number(value || 0).toFixed(1)}%`

const formatDateTime = (value) => {
  if (!value) return '未设置'
  const normalized = String(value).replace('T', ' ')
  return normalized.slice(0, 16)
}

const getPerformanceLabel = (accuracy) => {
  const value = Number(accuracy || 0)
  if (value >= 85) return '优秀'
  if (value >= 70) return '稳定'
  if (value >= 60) return '及格'
  return '需关注'
}

const getPerformanceClass = (accuracy) => {
  const value = Number(accuracy || 0)
  if (value >= 85) return 'success'
  if (value >= 70) return 'primary'
  if (value >= 60) return 'warning'
  return 'danger'
}

const getHomeworkStatusClass = (status) => {
  if (status === 'PUBLISHED') return 'primary'
  if (status === 'CLOSED') return 'neutral'
  return 'warning'
}

const getQuestionRiskLabel = (accuracy) => {
  const value = Number(accuracy || 0)
  if (value < 50) return '高风险'
  if (value < 75) return '待关注'
  return '较稳定'
}

const getQuestionRiskClass = (accuracy) => {
  const value = Number(accuracy || 0)
  if (value < 50) return 'danger'
  if (value < 75) return 'warning'
  return 'success'
}

const getQuestionTypeLabel = (type) => {
  if (type === 'SINGLE_CHOICE') return '单选题'
  if (type === 'TRUE_FALSE') return '判断题'
  if (type === 'FILL_BLANK') return '填空题'
  if (type === 'SHORT_ANSWER') return '简答题'
  return '题目'
}

onMounted(async () => {
  await loadCourses()
  await loadCourseStats()
})
</script>

<style scoped>
.stat-value-sm {
  font-size: clamp(1.2rem, 2vw, 1.8rem);
}

.section-stack {
  display: grid;
  gap: var(--spacing-md);
}

.table-header-row {
  display: flex;
  justify-content: space-between;
  gap: var(--spacing-md);
  margin-bottom: var(--spacing-md);
}

.trend-list {
  display: grid;
  gap: 12px;
}

.trend-item {
  display: grid;
  grid-template-columns: 64px minmax(0, 1fr) 80px;
  gap: 12px;
  align-items: center;
}

.trend-item span,
.trend-item strong {
  font-size: 13px;
}

.trend-bar-track {
  height: 10px;
  border-radius: 999px;
  background: rgba(23, 32, 51, 0.08);
  overflow: hidden;
}

.trend-bar-fill {
  height: 100%;
  border-radius: 999px;
  background: linear-gradient(90deg, #2c60d6, #66a3ff);
}

.table-shell {
  overflow-x: auto;
}

.grade-table {
  width: 100%;
  border-collapse: collapse;
}

.grade-table th,
.grade-table td {
  padding: 14px 12px;
  border-bottom: 1px solid rgba(23, 32, 51, 0.08);
  text-align: left;
  vertical-align: top;
}

.grade-table th {
  color: var(--gray-500);
  font-size: 12px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.08em;
}

.table-main {
  font-weight: 700;
  color: var(--gray-900);
}

.table-subtle {
  margin-top: 4px;
  color: var(--gray-500);
  font-size: 12px;
}

.insight-grid {
  display: grid;
  gap: 16px;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
}

.insight-item {
  display: grid;
  gap: 12px;
  padding: 18px;
  border-radius: 20px;
  background: rgba(246, 248, 252, 0.92);
  border: 1px solid rgba(23, 32, 51, 0.06);
}

.insight-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.insight-course {
  color: var(--gray-500);
  font-size: 13px;
  font-weight: 600;
}

.insight-content {
  margin: 0;
  color: var(--gray-900);
  line-height: 1.6;
}

.insight-meta {
  color: var(--gray-500);
  font-size: 12px;
}

.inline-link {
  color: #2c60d6;
  font-weight: 700;
  text-decoration: none;
}

.inline-link:hover {
  text-decoration: underline;
}

.chip.primary {
  background: rgba(44, 96, 214, 0.12);
  color: #2c60d6;
}

.chip.success {
  background: rgba(46, 134, 93, 0.14);
  color: #23754d;
}

.chip.warning {
  background: rgba(232, 141, 61, 0.15);
  color: #b86718;
}

.chip.danger {
  background: rgba(198, 76, 76, 0.14);
  color: #b73434;
}

.chip.neutral {
  background: rgba(88, 102, 126, 0.12);
  color: #536277;
}
</style>
