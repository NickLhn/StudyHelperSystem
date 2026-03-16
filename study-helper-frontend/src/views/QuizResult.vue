<template>
  <div class="quiz-result-container">
    <nav class="navbar">
      <div class="nav-brand">学习辅助系统</div>
      <div class="nav-links">
        <router-link to="/" class="nav-link">首页</router-link>
        <router-link to="/quizzes" class="nav-link">在线测验</router-link>
        <router-link to="/profile" class="nav-link">个人中心</router-link>
        <button @click="handleLogout" class="btn-logout">退出</button>
      </div>
    </nav>

    <main class="result-content">
      <div class="result-card">
        <div class="result-header">
          <h2>测验结果</h2>
          <div class="score-display">
            <div class="score-circle" :class="{ passed: isPassed }">
              <div class="score-text">{{ score }}/{{ totalScore }}</div>
              <div class="score-label">得分</div>
            </div>
          </div>
          <div class="result-stats">
            <div class="stat-item">
              <div class="stat-value">{{ accuracy }}%</div>
              <div class="stat-label">正确率</div>
            </div>
            <div class="stat-item">
              <div class="stat-value">{{ isPassed ? '通过' : '未通过' }}</div>
              <div class="stat-label">状态</div>
            </div>
          </div>
        </div>

        <div class="actions">
          <router-link to="/quizzes" class="btn-primary">返回测验列表</router-link>
          <router-link to="/wrong-book" class="btn-secondary">查看错题本</router-link>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 模拟数据（实际应该从API获取）
const score = ref(0)
const totalScore = ref(0)
const accuracy = ref(0)
const isPassed = ref(false)

const handleLogout = () => {
  userStore.logout()
  router.push('/login')
}

onMounted(() => {
  // 这里应该调用API获取具体的成绩记录
  // 暂时使用模拟数据
  score.value = 85
  totalScore.value = 100
  accuracy.value = 85
  isPassed.value = true
})
</script>

<style scoped>
.quiz-result-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.navbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem 2rem;
  background-color: rgba(255, 255, 255, 0.1);
  color: white;
  backdrop-filter: blur(10px);
}

.nav-brand {
  font-size: 1.5rem;
  font-weight: bold;
}

.nav-links {
  display: flex;
  gap: 1rem;
  align-items: center;
}

.nav-link {
  color: white;
  text-decoration: none;
  padding: 0.5rem 1rem;
  border-radius: 4px;
  transition: background-color 0.3s;
}

.nav-link:hover {
  background-color: rgba(255, 255, 255, 0.2);
}

.btn-logout {
  background-color: transparent;
  border: 1px solid white;
  color: white;
  padding: 0.5rem 1rem;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
}

.btn-logout:hover {
  background-color: white;
  color: #667eea;
}

.result-content {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: calc(100vh - 80px);
  padding: 2rem;
}

.result-card {
  background: white;
  border-radius: 20px;
  padding: 3rem;
  width: 100%;
  max-width: 500px;
  text-align: center;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
}

.result-header h2 {
  color: #333;
  margin-bottom: 2rem;
  font-size: 2rem;
}

.score-display {
  margin-bottom: 2rem;
}

.score-circle {
  width: 180px;
  height: 180px;
  border-radius: 50%;
  background: #ff6b6b;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  margin: 0 auto;
  transition: all 0.3s;
}

.score-circle.passed {
  background: #42b883;
  box-shadow: 0 0 30px rgba(66, 184, 131, 0.3);
}

.score-text {
  font-size: 2.5rem;
  font-weight: bold;
  color: white;
  margin-bottom: 0.5rem;
}

.score-label {
  color: rgba(255, 255, 255, 0.9);
  font-size: 1.1rem;
}

.result-stats {
  display: flex;
  justify-content: center;
  gap: 2rem;
  margin-bottom: 2rem;
}

.stat-item {
  text-align: center;
}

.stat-value {
  font-size: 1.5rem;
  font-weight: bold;
  color: #333;
  margin-bottom: 0.5rem;
}

.stat-label {
  color: #666;
  font-size: 0.9rem;
}

.actions {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.btn-primary, .btn-secondary {
  padding: 1rem;
  border-radius: 8px;
  text-decoration: none;
  font-size: 1.1rem;
  font-weight: 500;
  transition: all 0.3s;
}

.btn-primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.btn-primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 20px rgba(102, 126, 234, 0.3);
}

.btn-secondary {
  background: #f0f0f0;
  color: #666;
}

.btn-secondary:hover {
  background: #e0e0e0;
  transform: translateY(-2px);
}
</style>