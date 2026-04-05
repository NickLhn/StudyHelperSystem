<template>
  <div class="page-stack">
    <section class="page-intro">
      <div class="page-intro-copy">
        <span class="page-eyebrow">Homework Space</span>
        <h2 class="page-title">把待完成作业和已提交结果放到一个清楚的学习视图里</h2>
        <p class="page-subtitle">你可以直接看到截止时间、提交状态和得分结果，不用在课程详情里来回寻找。</p>
      </div>
      <div class="page-actions">
        <span class="chip">共 {{ homeworks.length }} 份作业</span>
      </div>
    </section>

    <section v-if="loading" class="loading-panel">
      <p class="loading-copy">作业列表加载中...</p>
    </section>

    <section v-else-if="homeworks.length === 0" class="empty-panel">
      <h3 class="empty-title">当前还没有可做作业</h3>
      <p class="empty-copy">等教师发布作业后，这里会自动显示。</p>
    </section>

    <section v-else class="card-grid cards-3">
      <article v-for="homework in homeworks" :key="homework.id" class="info-card entity-card">
        <div class="entity-card-header">
          <div>
            <h3 class="entity-card-title">{{ homework.title }}</h3>
            <p class="entity-card-subtitle">{{ homework.courseName }}</p>
          </div>
          <span class="status-pill" :class="(homework.submissionStatus || homework.status).toLowerCase()">
            {{ homework.submissionStatusLabel || homework.statusLabel }}
          </span>
        </div>

        <div class="data-points">
          <div class="data-point">
            <span>题目数</span>
            <strong>{{ homework.questionCount }} 题</strong>
          </div>
          <div class="data-point">
            <span>总分</span>
            <strong>{{ homework.totalScore }} 分</strong>
          </div>
          <div class="data-point">
            <span>截止时间</span>
            <strong>{{ formatDateTime(homework.deadlineAt) }}</strong>
          </div>
        </div>

        <div class="entity-card-actions">
          <router-link :to="`/student/homework/${homework.id}`" class="edu-btn edu-btn-primary">
            {{ homework.submissionStatus ? '查看结果' : '开始作答' }}
          </router-link>
        </div>
      </article>
    </section>

    <section v-if="error" class="message-banner error">{{ error }}</section>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useUserStore } from '../stores/user'
import { homeworkApi } from '../api/homework'

const userStore = useUserStore()
const homeworks = ref([])
const loading = ref(false)
const error = ref('')

const loadHomeworks = async () => {
  loading.value = true
  error.value = ''
  try {
    const response = await homeworkApi.getStudentHomeworks(userStore.user.id)
    if (response.data.code === 200) {
      homeworks.value = response.data.data
    } else {
      error.value = response.data.message
    }
  } catch (err) {
    error.value = '获取作业列表失败'
  } finally {
    loading.value = false
  }
}

const formatDateTime = (value) => {
  if (!value) return '未设置'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value
  return `${date.getMonth() + 1}/${date.getDate()} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

onMounted(loadHomeworks)
</script>
