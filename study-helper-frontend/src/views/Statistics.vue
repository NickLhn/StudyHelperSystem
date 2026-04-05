<template>
  <div class="page-stack">
    <section class="page-intro">
      <div class="page-intro-copy">
        <span class="page-eyebrow">Learning Analytics</span>
        <h2 class="page-title">把学习统计整理成更容易阅读的分析视图</h2>
        <p class="page-subtitle">关键指标先看摘要，图表再看分布和趋势，避免一进页面就被图形堆满。</p>
      </div>
      <div class="page-actions">
        <select v-model="selectedPeriod" @change="loadStatistics" class="period-select">
          <option value="week">最近一周</option>
          <option value="month">最近一月</option>
          <option value="year">最近一年</option>
        </select>
      </div>
    </section>

    <section v-if="loading" class="loading-panel">
      <p class="loading-copy">统计数据加载中...</p>
    </section>

    <template v-else>
      <section class="stats-grid">
        <article class="stat-panel">
          <div class="stat-kicker">总学习时长</div>
          <div class="stat-value">{{ stats.totalHours || 0 }}h</div>
          <p class="stat-copy">{{ comparison.current && comparison.previous ? calculateChange('totalHours') : '暂无对比数据' }}</p>
        </article>
        <article class="stat-panel">
          <div class="stat-kicker">学习天数</div>
          <div class="stat-value">{{ stats.studyDays || 0 }}</div>
          <p class="stat-copy">{{ comparison.current && comparison.previous ? calculateChange('studyDays') : '暂无对比数据' }}</p>
        </article>
        <article class="stat-panel">
          <div class="stat-kicker">涉及课程</div>
          <div class="stat-value">{{ stats.courseDistribution?.length || 0 }}</div>
          <p class="stat-copy">当前周期中有学习记录的课程数量</p>
        </article>
      </section>

      <section class="panel-grid two-up">
        <article class="info-card chart-card">
          <div class="stack-md">
            <div>
              <h3 class="section-title">课程学习时长分布</h3>
              <p class="section-copy">看看你的时间主要花在哪些课程上。</p>
            </div>
            <div ref="pieChartRef" class="chart-container"></div>
          </div>
        </article>

        <article class="info-card chart-card">
          <div class="stack-md">
            <div>
              <h3 class="section-title">每日学习时长趋势</h3>
              <p class="section-copy">趋势图帮助你观察学习是否稳定持续。</p>
            </div>
            <div ref="lineChartRef" class="chart-container"></div>
          </div>
        </article>
      </section>

      <section v-if="comparison.current && comparison.previous" class="panel-grid two-up">
        <article class="info-card">
          <h3 class="section-title">本期</h3>
          <p class="section-copy">{{ formatDateRange(selectedPeriod, 'current') }}</p>
          <div class="data-points">
            <div class="data-point">
              <span>学习时长</span>
              <strong>{{ comparison.current.totalHours }} 小时</strong>
            </div>
          </div>
        </article>

        <article class="info-card">
          <h3 class="section-title">上期</h3>
          <p class="section-copy">{{ formatDateRange(selectedPeriod, 'previous') }}</p>
          <div class="data-points">
            <div class="data-point">
              <span>学习时长</span>
              <strong>{{ comparison.previous.totalHours }} 小时</strong>
            </div>
          </div>
        </article>
      </section>
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useUserStore } from '../stores/user'
import { statisticApi } from '../api/statistic'
import * as echarts from 'echarts'

const userStore = useUserStore()

const selectedPeriod = ref('week')
const stats = ref({})
const comparison = ref({})
const loading = ref(false)

const pieChartRef = ref()
const lineChartRef = ref()
let pieChart = null
let lineChart = null

const loadStatistics = async () => {
  loading.value = true
  try {
    const [statsRes, comparisonRes] = await Promise.all([
      statisticApi.getUserStatistics(userStore.user.id, selectedPeriod.value),
      statisticApi.getComparisonStatistics(userStore.user.id, selectedPeriod.value)
    ])

    if (statsRes.data.code === 200) {
      stats.value = statsRes.data.data
      renderPieChart()
      renderLineChart()
    }

    if (comparisonRes.data.code === 200) {
      comparison.value = comparisonRes.data.data
    }
  } catch (error) {
    console.error('加载统计失败:', error)
  } finally {
    loading.value = false
  }
}

const renderPieChart = () => {
  if (!pieChartRef.value) return
  if (!pieChart) pieChart = echarts.init(pieChartRef.value)

  const courseData = stats.value.courseDistribution || []
  pieChart.setOption({
    backgroundColor: 'transparent',
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c} 小时 ({d}%)'
    },
    legend: {
      bottom: 0,
      textStyle: { color: '#42506b' }
    },
    series: [
      {
        type: 'pie',
        radius: ['42%', '70%'],
        itemStyle: {
          borderRadius: 12,
          borderColor: '#ffffff',
          borderWidth: 3
        },
        data: courseData.map((item) => ({
          value: item.hours,
          name: item.courseName
        }))
      }
    ]
  })
}

const renderLineChart = () => {
  if (!lineChartRef.value) return
  if (!lineChart) lineChart = echarts.init(lineChartRef.value)

  const dailyData = stats.value.dailyTrend || []
  const dates = dailyData.map((item) => item.date.slice(5))
  const hours = dailyData.map((item) => item.hours)

  lineChart.setOption({
    backgroundColor: 'transparent',
    tooltip: {
      trigger: 'axis',
      formatter: (params) => {
        const param = params[0]
        return `${param.name}<br/>学习时长: ${param.value} 小时`
      }
    },
    xAxis: {
      type: 'category',
      data: dates,
      axisLine: { lineStyle: { color: 'rgba(23, 32, 51, 0.12)' } },
      axisLabel: { color: '#6d7890' }
    },
    yAxis: {
      type: 'value',
      name: '小时',
      axisLine: { show: false },
      splitLine: { lineStyle: { color: 'rgba(23, 32, 51, 0.08)' } },
      axisLabel: { color: '#6d7890' }
    },
    series: [
      {
        data: hours,
        type: 'line',
        smooth: true,
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(44, 96, 214, 0.24)' },
            { offset: 1, color: 'rgba(44, 96, 214, 0.02)' }
          ])
        },
        lineStyle: {
          color: '#2c60d6',
          width: 3
        },
        itemStyle: {
          color: '#2c60d6'
        }
      }
    ]
  })
}

const calculateChange = (field) => {
  const current = comparison.value.current[field] || 0
  const previous = comparison.value.previous[field] || 0

  if (previous === 0) return current > 0 ? '+∞%' : '0%'

  const change = ((current - previous) / previous) * 100
  const sign = change >= 0 ? '+' : ''
  return `${sign}${change.toFixed(1)}%`
}

const formatDateRange = (period, type) => {
  const now = new Date()
  let start
  let end

  if (period === 'week') {
    end = now
    start = new Date(now.getTime() - 6 * 24 * 60 * 60 * 1000)
  } else if (period === 'month') {
    end = now
    start = new Date(now.getTime() - 29 * 24 * 60 * 60 * 1000)
  } else {
    end = now
    start = new Date(now.getTime() - 364 * 24 * 60 * 60 * 1000)
  }

  if (type === 'previous') {
    end = new Date(start.getTime() - 24 * 60 * 60 * 1000)
    start = new Date(end.getTime() - (period === 'week' ? 6 : period === 'month' ? 29 : 364) * 24 * 60 * 60 * 1000)
  }

  return `${start.getMonth() + 1}/${start.getDate()} - ${end.getMonth() + 1}/${end.getDate()}`
}

const handleResize = () => {
  if (pieChart) pieChart.resize()
  if (lineChart) lineChart.resize()
}

onMounted(() => {
  loadStatistics()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  if (pieChart) pieChart.dispose()
  if (lineChart) lineChart.dispose()
})
</script>

<style scoped>
.period-select {
  min-width: 180px;
}

.chart-card {
  min-height: 420px;
}

.chart-container {
  width: 100%;
  height: 320px;
}
</style>
