<template>
  <div class="statistics-container">
    <main class="statistics-content">
      <div class="header">
        <h2>学习统计分析</h2>
        <div class="controls">
          <select v-model="selectedPeriod" @change="loadStatistics" class="period-select">
            <option value="week">最近一周</option>
            <option value="month">最近一月</option>
            <option value="year">最近一年</option>
          </select>
        </div>
      </div>

      <div v-if="loading" class="loading">加载统计中...</div>
      
      <div v-else class="stats-dashboard">
        <!-- 关键指标卡片 -->
        <div class="metrics-grid">
          <div class="metric-card">
            <div class="metric-value">{{ stats.totalHours || 0 }} 小时</div>
            <div class="metric-label">总学习时长</div>
            <div class="metric-change positive" v-if="comparison.current && comparison.previous">
              {{ calculateChange('totalHours') }}
            </div>
          </div>
          
          <div class="metric-card">
            <div class="metric-value">{{ stats.studyDays || 0 }} 天</div>
            <div class="metric-label">学习天数</div>
            <div class="metric-change" v-if="comparison.current && comparison.previous">
              {{ calculateChange('studyDays') }}
            </div>
          </div>
          
          <div class="metric-card">
            <div class="metric-value">{{ stats.courseDistribution?.length || 0 }} 门</div>
            <div class="metric-label">涉及课程数</div>
          </div>
        </div>

        <!-- 图表区域 -->
        <div class="charts-grid">
          <!-- 课程时长分布饼图 -->
          <div class="chart-card">
            <h3>课程学习时长分布</h3>
            <div ref="pieChartRef" class="chart-container"></div>
          </div>

          <!-- 每日学习趋势折线图 -->
          <div class="chart-card">
            <h3>每日学习时长趋势</h3>
            <div ref="lineChartRef" class="chart-container"></div>
          </div>
        </div>

        <!-- 对比分析 -->
        <div class="comparison-section" v-if="comparison.current && comparison.previous">
          <h3>对比分析</h3>
          <div class="comparison-grid">
            <div class="comparison-card">
              <h4>本期 ({{ formatDateRange(selectedPeriod, 'current') }})</h4>
              <p>学习时长: {{ comparison.current.totalHours }} 小时</p>
            </div>
            <div class="comparison-card">
              <h4>上期 ({{ formatDateRange(selectedPeriod, 'previous') }})</h4>
              <p>学习时长: {{ comparison.previous.totalHours }} 小时</p>
            </div>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
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

  if (!pieChart) {
    pieChart = echarts.init(pieChartRef.value)
  }

  const courseData = stats.value.courseDistribution || []
  
  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c}小时 ({d}%)'
    },
    legend: {
      bottom: '5%',
      left: 'center'
    },
    series: [
      {
        name: '学习时长',
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: false,
          position: 'center'
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 20,
            fontWeight: 'bold'
          }
        },
        labelLine: {
          show: false
        },
        data: courseData.map(item => ({
          value: item.hours,
          name: item.courseName
        }))
      }
    ]
  }

  pieChart.setOption(option)
}

const renderLineChart = () => {
  if (!lineChartRef.value) return

  if (!lineChart) {
    lineChart = echarts.init(lineChartRef.value)
  }

  const dailyData = stats.value.dailyTrend || []
  const dates = dailyData.map(item => item.date.slice(5)) // 只显示月-日
  const hours = dailyData.map(item => item.hours)

  const option = {
    tooltip: {
      trigger: 'axis',
      formatter: (params) => {
        const param = params[0]
        return `${param.name}<br/>学习时长: ${param.value} 小时`
      }
    },
    xAxis: {
      type: 'category',
      data: dates
    },
    yAxis: {
      type: 'value',
      name: '小时'
    },
    series: [
      {
        data: hours,
        type: 'line',
        smooth: true,
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(66, 184, 131, 0.3)' },
            { offset: 1, color: 'rgba(66, 184, 131, 0.05)' }
          ])
        },
        lineStyle: {
          color: '#42b883',
          width: 3
        },
        itemStyle: {
          color: '#42b883',
          borderWidth: 2,
          borderColor: '#fff'
        }
      }
    ]
  }

  lineChart.setOption(option)
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
  let start, end

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
    end = new Date(start.getTime() - 1 * 24 * 60 * 60 * 1000)
    start = new Date(end.getTime() - (period === 'week' ? 6 : period === 'month' ? 29 : 364) * 24 * 60 * 60 * 1000)
  }

  return `${start.getMonth()+1}/${start.getDate()} - ${end.getMonth()+1}/${end.getDate()}`
}

// 响应式调整图表大小
window.addEventListener('resize', () => {
  if (pieChart) pieChart.resize()
  if (lineChart) lineChart.resize()
})

onMounted(() => {
  loadStatistics()
})
</script>

<style scoped>
.statistics-container {
  min-height: 100vh;
  background-color: #f5f5f5;
}

.statistics-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 2rem;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
}

.header h2 {
  color: #333;
  margin: 0;
}

.controls {
  display: flex;
  gap: 1rem;
  align-items: center;
}

.period-select {
  padding: 0.5rem 1rem;
  border: 1px solid #ddd;
  border-radius: 6px;
  background: white;
  font-size: 1rem;
}

.btn-sync {
  background: #667eea;
  color: white;
  border: none;
  padding: 0.5rem 1rem;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s;
}

.btn-sync:hover:not(:disabled) {
  background: #5a6fd8;
}

.btn-sync:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.loading {
  text-align: center;
  padding: 3rem;
  color: #666;
  font-size: 1.1rem;
}

.metrics-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 1.5rem;
  margin-bottom: 2rem;
}

.metric-card {
  background: white;
  border-radius: 12px;
  padding: 1.5rem;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  text-align: center;
  transition: transform 0.2s;
}

.metric-card:hover {
  transform: translateY(-4px);
}

.metric-value {
  font-size: 2rem;
  font-weight: bold;
  color: #42b883;
  margin-bottom: 0.5rem;
}

.metric-label {
  color: #666;
  font-size: 1rem;
  margin-bottom: 0.5rem;
}

.metric-change {
  font-size: 0.9rem;
  font-weight: 500;
}

.metric-change.positive {
  color: #27ae60;
}

.metric-change:not(.positive) {
  color: #e74c3c;
}

.charts-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1.5rem;
  margin-bottom: 2rem;
}

.chart-card {
  background: white;
  border-radius: 12px;
  padding: 1.5rem;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.chart-card h3 {
  margin-top: 0;
  color: #333;
  margin-bottom: 1rem;
  text-align: center;
}

.chart-container {
  width: 100%;
  height: 300px;
}

.comparison-section {
  background: white;
  border-radius: 12px;
  padding: 1.5rem;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.comparison-section h3 {
  margin-top: 0;
  color: #333;
  margin-bottom: 1rem;
}

.comparison-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1rem;
}

.comparison-card {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 1rem;
}

.comparison-card h4 {
  margin-top: 0;
  color: #42b883;
  margin-bottom: 0.75rem;
}

.comparison-card p {
  margin: 0.5rem 0;
  color: #555;
}

@media (max-width: 768px) {
  .charts-grid {
    grid-template-columns: 1fr;
  }
  
  .comparison-grid {
    grid-template-columns: 1fr;
  }
  
  .header {
    flex-direction: column;
    gap: 1rem;
    align-items: flex-start;
  }
  
  .controls {
    width: 100%;
    justify-content: space-between;
  }
}
</style>
