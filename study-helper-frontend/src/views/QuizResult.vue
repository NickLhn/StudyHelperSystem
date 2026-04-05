<template>
  <div class="page-stack">
    <section class="page-intro">
      <div class="page-intro-copy">
        <span class="page-eyebrow">Quiz Result</span>
        <h2 class="page-title">{{ record?.quiz?.title || '测验结果' }}</h2>
        <p class="page-subtitle">
          {{ loading ? '正在加载成绩结果…' : '本次作答结果已经保存，你可以返回列表继续查看其他测验。' }}
        </p>
      </div>
      <div v-if="record" class="page-actions">
        <span class="chip" :class="record.isPassed ? 'success' : 'danger'">
          {{ record.isPassed ? '通过' : '未通过' }}
        </span>
      </div>
    </section>

    <section v-if="loading" class="loading-panel">
      <p class="loading-copy">结果加载中...</p>
    </section>

    <section v-else-if="error" class="message-banner error">
      {{ error }}
    </section>

    <template v-else-if="record">
      <section class="result-hero">
        <div class="score-orb" :class="record.isPassed ? 'passed' : 'failed'">
          <strong>{{ record.score }}/{{ record.totalScore }}</strong>
          <span>总分</span>
        </div>
        <div class="result-metrics">
          <article class="metric-card">
            <span>正确率</span>
            <strong>{{ (record.accuracy * 100).toFixed(1) }}%</strong>
          </article>
          <article class="metric-card">
            <span>耗时</span>
            <strong>{{ formatTime(record.timeUsed || 0) }}</strong>
          </article>
          <article class="metric-card">
            <span>错题数</span>
            <strong>{{ record.wrongQuestions?.length || 0 }}</strong>
          </article>
        </div>
      </section>

      <section class="result-actions">
        <router-link to="/quizzes" class="edu-btn edu-btn-primary">返回测验列表</router-link>
        <router-link to="/student/wrong-book" class="edu-btn edu-btn-secondary">查看错题本</router-link>
      </section>
    </template>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { quizApi } from '../api/quiz'
import { useUserStore } from '../stores/user'

const route = useRoute()
const userStore = useUserStore()

const loading = ref(false)
const error = ref('')
const record = ref(null)

const formatTime = (seconds = 0) => {
  const mins = Math.floor(seconds / 60)
  const secs = seconds % 60
  return `${String(mins).padStart(2, '0')}:${String(secs).padStart(2, '0')}`
}

const fetchResult = async () => {
  loading.value = true
  error.value = ''
  try {
    const response = await quizApi.getRecordDetail(route.params.recordId, userStore.user.id)
    if (response.data.code === 200) {
      record.value = response.data.data
    } else {
      error.value = response.data.message
    }
  } catch (err) {
    error.value = '结果加载失败，请稍后重试'
  } finally {
    loading.value = false
  }
}

onMounted(fetchResult)
</script>

<style scoped>
.result-hero {
  display: grid;
  grid-template-columns: 260px minmax(0, 1fr);
  gap: 24px;
  align-items: center;
}

.score-orb,
.metric-card {
  border-radius: 26px;
  border: 1px solid rgba(23, 32, 51, 0.08);
  background: rgba(255, 255, 255, 0.86);
  box-shadow: 0 20px 45px rgba(15, 23, 42, 0.08);
}

.score-orb {
  min-height: 240px;
  display: grid;
  place-items: center;
  text-align: center;
}

.score-orb strong {
  display: block;
  font-size: 2.4rem;
  margin-bottom: 10px;
}

.score-orb.passed {
  background: linear-gradient(135deg, rgba(22, 163, 74, 0.14), rgba(15, 118, 110, 0.14));
}

.score-orb.failed {
  background: linear-gradient(135deg, rgba(220, 38, 38, 0.1), rgba(217, 119, 6, 0.14));
}

.result-metrics {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
}

.metric-card {
  padding: 22px;
  display: grid;
  gap: 8px;
}

.metric-card span {
  color: var(--text-muted);
}

.metric-card strong {
  font-size: 1.5rem;
}

.result-actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

@media (max-width: 860px) {
  .result-hero {
    grid-template-columns: 1fr;
  }

  .result-metrics {
    grid-template-columns: 1fr;
  }
}
</style>
