<template>
  <div class="page">
    <div class="header">
      <div class="hleft">
        <router-link to="/teacher/courses" class="back">返回</router-link>
        <div class="title">
          <div class="name">{{ course?.name || '课程' }}</div>
          <div class="meta">
            <span>{{ course?.categoryLabel || '-' }}</span>
            <span v-if="course?.statusLabel">· {{ course.statusLabel }}</span>
            <span v-if="course?.semesterLabel">· {{ course.semesterLabel }}</span>
            <span v-if="course?.teacher">· {{ course.teacher }}</span>
          </div>
        </div>
      </div>

      <div class="hright">
        <button class="btn" type="button" @click="toggleArchive" :disabled="loading || !course">
          {{ course?.status === 'ARCHIVED' ? '恢复课程' : '归档课程' }}
        </button>
        <div class="code-card">
          <div class="code-label">课程邀请码</div>
          <div class="code-row">
            <div class="code">{{ course?.invitationCode || '-' }}</div>
            <button class="btn" type="button" @click="copyCode" :disabled="!course?.invitationCode">复制</button>
            <button class="btn" type="button" @click="refreshCode" :disabled="loading">刷新</button>
          </div>
        </div>
      </div>
    </div>

    <div class="stats">
      <div class="stat">
        <div class="slabel">学生人数</div>
        <div class="svalue">{{ course?.studentCount || 0 }}</div>
      </div>
      <div class="stat">
        <div class="slabel">资料数</div>
        <div class="svalue">{{ course?.materialCount || 0 }}</div>
      </div>
      <div class="stat">
        <div class="slabel">作业数</div>
        <div class="svalue">{{ course?.taskCount || 0 }}</div>
      </div>
      <div class="stat">
        <div class="slabel">测验数</div>
        <div class="svalue">{{ course?.quizCount || 0 }}</div>
      </div>
    </div>

    <div class="actions">
      <router-link class="action" :to="`/teacher/material/upload?courseId=${courseId}`">发布资料</router-link>
      <router-link class="action" :to="`/teacher/task/create?courseId=${courseId}`">新建任务</router-link>
      <router-link class="action" :to="`/teacher/homework/create?courseId=${courseId}`">发布作业</router-link>
      <router-link class="action" :to="`/teacher/quiz/create?courseId=${courseId}`">发布考试</router-link>
    </div>

    <div class="grid">
      <section class="card">
        <div class="card-title">学生列表</div>
        <div v-if="studentsLoading" class="muted">加载中...</div>
        <div v-else-if="students.length === 0" class="muted">暂无学生加入</div>
        <table v-else class="table">
          <thead>
            <tr>
              <th>ID</th>
              <th>用户名</th>
              <th>邮箱</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="s in students" :key="s.id">
              <td>{{ s.id }}</td>
              <td>{{ s.nickname || s.username }}</td>
              <td>{{ s.email || '-' }}</td>
              <td class="tr">
                <button class="btn danger" type="button" @click="removeStudent(s.id)">移除</button>
              </td>
            </tr>
          </tbody>
        </table>
      </section>

      <section class="card">
        <div class="card-title">课程资源</div>
        <div class="tabs">
          <button class="tab" :class="{ active: tab === 'materials' }" @click="tab = 'materials'">资料</button>
          <button class="tab" :class="{ active: tab === 'tasks' }" @click="tab = 'tasks'">作业</button>
          <button class="tab" :class="{ active: tab === 'quizzes' }" @click="tab = 'quizzes'">考试</button>
        </div>

        <div v-if="tab === 'materials'">
          <div v-if="materialsLoading" class="muted">加载中...</div>
          <div v-else-if="materials.length === 0" class="muted">暂无资料</div>
          <ul v-else class="list">
            <li v-for="m in materials" :key="m.id" class="li">
              <span class="li-title">{{ m.name }}</span>
              <span class="muted">{{ m.fileType }}</span>
            </li>
          </ul>
        </div>

        <div v-else-if="tab === 'tasks'">
          <div v-if="tasksLoading" class="muted">加载中...</div>
          <div v-else-if="tasks.length === 0" class="muted">暂无作业</div>
          <ul v-else class="list">
            <li v-for="t in tasks" :key="t.id" class="li">
              <span class="li-title">{{ t.title }}</span>
              <span class="muted">{{ t.statusLabel }}</span>
            </li>
          </ul>
        </div>

        <div v-else>
          <div v-if="quizzesLoading" class="muted">加载中...</div>
          <div v-else-if="quizzes.length === 0" class="muted">暂无考试</div>
          <ul v-else class="list">
            <li v-for="q in quizzes" :key="q.id" class="li">
              <span class="li-title">{{ q.title }}</span>
              <span class="muted">{{ q.statusLabel }}</span>
              <span class="grow"></span>
              <button class="btn" type="button" @click="setQuizStatus(q.id, 'PUBLISHED')">发布</button>
              <button class="btn" type="button" @click="setQuizStatus(q.id, 'CLOSED')">关闭</button>
              <button class="btn danger" type="button" @click="deleteQuiz(q.id)">删除</button>
            </li>
          </ul>
        </div>
      </section>

      <section class="card full">
        <div class="card-title">
          学习情况统计
          <select v-model="period" class="select" @change="loadStats">
            <option value="week">最近一周</option>
            <option value="month">最近一月</option>
          </select>
        </div>

        <div class="charts">
          <div ref="studyChartRef" class="chart"></div>
          <div ref="quizChartRef" class="chart"></div>
        </div>
      </section>
    </div>

    <div v-if="error" class="error">{{ error }}</div>
  </div>
</template>

<script setup>
import { computed, nextTick, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import * as echarts from 'echarts'
import { useUserStore } from '../stores/user'
import { courseApi } from '../api/course'
import { materialApi } from '../api/material'
import { taskApi } from '../api/task'
import { quizApi } from '../api/quiz'

const route = useRoute()
const userStore = useUserStore()

const courseId = computed(() => Number(route.params.courseId || route.params.id))

const course = ref(null)
const students = ref([])
const materials = ref([])
const tasks = ref([])
const quizzes = ref([])

const loading = ref(false)
const studentsLoading = ref(false)
const materialsLoading = ref(false)
const tasksLoading = ref(false)
const quizzesLoading = ref(false)

const error = ref('')
const tab = ref('materials')
const period = ref('week')

const stats = ref(null)
const studyChartRef = ref(null)
const quizChartRef = ref(null)
let studyChart
let quizChart

const loadCourse = async () => {
  error.value = ''
  loading.value = true
  try {
    const response = await courseApi.getCourseById(courseId.value, userStore.user.id)
    if (response.data.code === 200) {
      course.value = response.data.data
    } else {
      error.value = response.data.message
    }
  } catch (e) {
    error.value = '加载课程失败'
  } finally {
    loading.value = false
  }
}

const loadStudents = async () => {
  studentsLoading.value = true
  try {
    const response = await courseApi.getCourseStudents(courseId.value, userStore.user.id)
    if (response.data.code === 200) {
      students.value = response.data.data
    }
  } finally {
    studentsLoading.value = false
  }
}

const loadMaterials = async () => {
  materialsLoading.value = true
  try {
    const response = await materialApi.getMaterialsByCourse(courseId.value, userStore.user.id)
    if (response.data.code === 200) {
      materials.value = response.data.data
    }
  } finally {
    materialsLoading.value = false
  }
}

const loadTasks = async () => {
  tasksLoading.value = true
  try {
    const response = await taskApi.getCourseTasks(userStore.user.id, courseId.value)
    if (response.data.code === 200) {
      tasks.value = response.data.data
    }
  } finally {
    tasksLoading.value = false
  }
}

const loadQuizzes = async () => {
  quizzesLoading.value = true
  try {
    const response = await quizApi.getCourseQuizzes(userStore.user.id, courseId.value)
    if (response.data.code === 200) {
      quizzes.value = response.data.data
    }
  } finally {
    quizzesLoading.value = false
  }
}

const removeStudent = async (studentId) => {
  if (!confirm('确定要移除该学生吗？')) return
  await courseApi.removeStudent(courseId.value, userStore.user.id, studentId)
  await loadCourse()
  await loadStudents()
}

const refreshCode = async () => {
  const response = await courseApi.refreshInvitationCode(courseId.value, userStore.user.id)
  if (response.data.code === 200) {
    course.value = { ...(course.value || {}), invitationCode: response.data.data.invitationCode }
  }
}

const toggleArchive = async () => {
  if (!course.value) return
  const targetStatus = course.value.status === 'ARCHIVED' ? 'ACTIVE' : 'ARCHIVED'
  const ok = confirm(targetStatus === 'ARCHIVED' ? '确定归档这门课程吗？归档后新学生无法通过邀请码加入。' : '确定恢复这门课程吗？')
  if (!ok) return

  const response = await courseApi.updateCourseStatus(courseId.value, userStore.user.id, targetStatus)
  if (response.data.code === 200) {
    course.value = response.data.data
  } else {
    error.value = response.data.message
  }
}

const copyCode = async () => {
  if (!course.value?.invitationCode) return
  try {
    await navigator.clipboard.writeText(course.value.invitationCode)
  } catch (e) {
  }
}

const setQuizStatus = async (quizId, status) => {
  await quizApi.updateQuizStatus(quizId, userStore.user.id, status)
  await loadQuizzes()
}

const deleteQuiz = async (quizId) => {
  if (!confirm('确定要删除该测验吗？')) return
  await quizApi.deleteQuiz(quizId, userStore.user.id)
  await loadCourse()
  await loadQuizzes()
}

const initCharts = async () => {
  await nextTick()
  if (studyChartRef.value && !studyChart) {
    studyChart = echarts.init(studyChartRef.value)
  }
  if (quizChartRef.value && !quizChart) {
    quizChart = echarts.init(quizChartRef.value)
  }
}

const renderCharts = async () => {
  await initCharts()
  if (!stats.value) return

  const trend = stats.value.studyTrend || { labels: [], minutes: [] }
  studyChart?.setOption({
    title: { text: '学习时长趋势（分钟）', left: 'center' },
    tooltip: { trigger: 'axis' },
    grid: { left: 40, right: 20, top: 50, bottom: 40 },
    xAxis: { type: 'category', data: trend.labels, axisLabel: { rotate: 35 } },
    yAxis: { type: 'value' },
    series: [{ type: 'line', data: trend.minutes, smooth: true, areaStyle: {} }]
  })

  const quizSummaries = stats.value.quizSummaries || []
  quizChart?.setOption({
    title: { text: '测验平均分（按测验）', left: 'center' },
    tooltip: { trigger: 'axis' },
    grid: { left: 40, right: 20, top: 50, bottom: 80 },
    xAxis: { type: 'category', data: quizSummaries.map(q => q.title), axisLabel: { rotate: 35 } },
    yAxis: { type: 'value' },
    series: [{ type: 'bar', data: quizSummaries.map(q => q.avgScore) }]
  })
}

const loadStats = async () => {
  try {
    const response = await courseApi.getCourseStats(courseId.value, userStore.user.id, period.value)
    if (response.data.code === 200) {
      stats.value = response.data.data
      await renderCharts()
    }
  } catch (e) {
  }
}

onMounted(async () => {
  await loadCourse()
  await Promise.all([loadStudents(), loadMaterials(), loadTasks(), loadQuizzes()])
  await loadStats()
})
</script>

<style scoped>
.page {
  padding: 0.5rem 0;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 1rem;
  margin-bottom: 1rem;
}

.hleft {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  min-width: 0;
}

.back {
  text-decoration: none;
  font-weight: 800;
  color: #2563eb;
}

.name {
  font-size: 1.4rem;
  font-weight: 900;
  color: #111827;
}

.meta {
  color: #6b7280;
  font-weight: 700;
}

.code-card {
  background: #fff;
  border: 1px solid #eef2f7;
  border-radius: 14px;
  padding: 0.75rem 1rem;
  box-shadow: 0 6px 18px rgba(15, 23, 42, 0.06);
}

.code-label {
  color: #6b7280;
  font-weight: 800;
  font-size: 0.9rem;
}

.code-row {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-top: 0.5rem;
}

.code {
  font-weight: 900;
  color: #111827;
  letter-spacing: 1px;
  min-width: 110px;
}

.btn {
  height: 34px;
  padding: 0 0.75rem;
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  background: #fff;
  font-weight: 800;
  cursor: pointer;
}

.btn.danger {
  border-color: #fecaca;
  color: #b91c1c;
}

.stats {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 0.75rem;
  margin-bottom: 1rem;
}

.stat {
  background: #fff;
  border: 1px solid #eef2f7;
  border-radius: 14px;
  padding: 0.9rem 1rem;
  box-shadow: 0 6px 18px rgba(15, 23, 42, 0.06);
}

.slabel {
  color: #6b7280;
  font-weight: 800;
  font-size: 0.9rem;
}

.svalue {
  color: #111827;
  font-weight: 900;
  font-size: 1.5rem;
  margin-top: 0.25rem;
}

.actions {
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
  margin-bottom: 1rem;
}

.action {
  text-decoration: none;
  background: #2563eb;
  color: #fff;
  font-weight: 900;
  padding: 0.6rem 0.85rem;
  border-radius: 12px;
}

.grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 0.75rem;
}

.card {
  background: #fff;
  border: 1px solid #eef2f7;
  border-radius: 14px;
  padding: 1rem;
  box-shadow: 0 6px 18px rgba(15, 23, 42, 0.06);
}

.card.full {
  grid-column: 1 / -1;
}

.card-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-weight: 900;
  color: #111827;
  margin-bottom: 0.75rem;
}

.select {
  height: 34px;
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  padding: 0 0.5rem;
  font-weight: 800;
  background: #fff;
}

.muted {
  color: #6b7280;
  font-weight: 700;
}

.table {
  width: 100%;
  border-collapse: collapse;
}

.table th,
.table td {
  border-bottom: 1px solid #eef2f7;
  padding: 0.6rem 0.4rem;
  text-align: left;
  font-weight: 700;
  color: #111827;
}

.tr {
  text-align: right;
}

.tabs {
  display: flex;
  gap: 0.5rem;
  margin-bottom: 0.75rem;
}

.tab {
  height: 34px;
  padding: 0 0.75rem;
  border-radius: 999px;
  border: 1px solid #e5e7eb;
  background: #fff;
  font-weight: 900;
  cursor: pointer;
  color: #111827;
}

.tab.active {
  background: #111827;
  color: #fff;
  border-color: #111827;
}

.list {
  list-style: none;
  padding: 0;
  margin: 0;
  display: grid;
  gap: 0.5rem;
}

.li {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.6rem 0.75rem;
  border: 1px solid #eef2f7;
  border-radius: 12px;
}

.li-title {
  font-weight: 900;
  color: #111827;
}

.grow {
  flex: 1;
}

.charts {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 0.75rem;
}

.chart {
  width: 100%;
  height: 360px;
}

.error {
  margin-top: 1rem;
  padding: 0.75rem 1rem;
  border-radius: 12px;
  background: #fee2e2;
  color: #991b1b;
  font-weight: 800;
}

@media (max-width: 900px) {
  .stats {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
  .grid {
    grid-template-columns: 1fr;
  }
  .charts {
    grid-template-columns: 1fr;
  }
}
</style>
