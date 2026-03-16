<template>
  <div class="dashboard-container">
    <h1 class="page-title">📊 管理员仪表盘</h1>
    
    <!-- 统计卡片 -->
    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-icon users">👥</div>
        <div class="stat-content">
          <h3>总用户数</h3>
          <p class="stat-number">{{ stats.totalUsers || 0 }}</p>
          <div class="stat-footer">
            <span class="today-new">今日新增: {{ stats.todayNewUsers || 0 }}</span>
          </div>
        </div>
      </div>
      
      <div class="stat-card">
        <div class="stat-icon courses">📚</div>
        <div class="stat-content">
          <h3>总课程数</h3>
          <p class="stat-number">{{ stats.totalCourses || 0 }}</p>
          <div class="stat-footer">
            <span class="today-new">今日新增: {{ stats.todayNewCourses || 0 }}</span>
          </div>
        </div>
      </div>
      
      <div class="stat-card">
        <div class="stat-icon materials">📁</div>
        <div class="stat-content">
          <h3>总资料数</h3>
          <p class="stat-number">{{ stats.totalMaterials || 0 }}</p>
          <div class="stat-footer">
            <span class="today-new">今日新增: {{ stats.todayNewMaterials || 0 }}</span>
          </div>
        </div>
      </div>
      
      <div class="stat-card">
        <div class="stat-icon roles">🎭</div>
        <div class="stat-content">
          <h3>角色分布</h3>
          <div class="role-stats">
            <div class="role-item">
              <span class="role-label">学生:</span>
              <span class="role-count">{{ stats.studentCount || 0 }}</span>
            </div>
            <div class="role-item">
              <span class="role-label">教师:</span>
              <span class="role-count">{{ stats.teacherCount || 0 }}</span>
            </div>
            <div class="role-item">
              <span class="role-label">管理员:</span>
              <span class="role-count">{{ stats.adminCount || 0 }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 加载状态 -->
    <div v-if="loading" class="loading">
      <div class="spinner"></div>
      <p>加载统计数据中...</p>
    </div>
    
    <!-- 错误提示 -->
    <div v-if="error" class="error-message">
      <i class="icon">⚠️</i>
      {{ error }}
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { adminApi } from '../api/admin'

const stats = ref({})
const loading = ref(false)
const error = ref('')

const loadStats = async () => {
  loading.value = true
  error.value = ''
  
  try {
    const response = await adminApi.getStats()
    if (response.data.code === 200) {
      stats.value = response.data.data
    } else {
      error.value = response.data.message
    }
  } catch (err) {
    error.value = '获取统计数据失败，请检查网络连接'
    console.error('获取统计数据失败:', err)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadStats()
})
</script>

<style scoped>
.dashboard-container {
  padding: 2rem 0;
}

.page-title {
  color: #333;
  margin-bottom: 2rem;
  font-size: 2rem;
  text-align: center;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 1.5rem;
  margin-bottom: 3rem;
}

.stat-card {
  background: white;
  border-radius: 12px;
  padding: 1.5rem;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.08);
  display: flex;
  align-items: center;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 2rem;
  margin-right: 1rem;
  flex-shrink: 0;
}

.stat-icon.users {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.stat-icon.courses {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  color: white;
}

.stat-icon.materials {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
  color: white;
}

.stat-icon.roles {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
  color: white;
}

.stat-content h3 {
  margin: 0 0 0.5rem 0;
  color: #555;
  font-size: 1rem;
  font-weight: 500;
}

.stat-number {
  font-size: 2rem;
  font-weight: 700;
  color: #333;
  margin: 0 0 0.5rem 0;
}

.stat-footer {
  font-size: 0.85rem;
  color: #888;
}

.today-new {
  background: #e8f5e8;
  color: #2e7d32;
  padding: 0.2rem 0.5rem;
  border-radius: 12px;
  font-weight: 500;
}

.role-stats {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.role-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.25rem 0;
}

.role-label {
  color: #666;
  font-size: 0.9rem;
}

.role-count {
  font-weight: 600;
  color: #333;
  background: #f0f0f0;
  padding: 0.1rem 0.5rem;
  border-radius: 8px;
}

.quick-actions {
  margin-top: 3rem;
}

.quick-actions h2 {
  color: #333;
  margin-bottom: 1.5rem;
  font-size: 1.5rem;
}

.actions-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 1.5rem;
}

.action-card {
  background: white;
  border-radius: 12px;
  padding: 2rem 1.5rem;
  text-align: center;
  text-decoration: none;
  color: inherit;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
  border: 2px solid transparent;
}

.action-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
  border-color: #667eea;
}

.action-icon {
  font-size: 3rem;
  margin-bottom: 1rem;
}

.action-card h3 {
  margin: 0 0 0.5rem 0;
  color: #333;
  font-size: 1.2rem;
}

.action-card p {
  margin: 0;
  color: #666;
  font-size: 0.9rem;
}

.loading {
  text-align: center;
  padding: 3rem;
  color: #666;
}

.spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid #667eea;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 1rem;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.error-message {
  background: #ffebee;
  color: #c62828;
  padding: 1rem;
  border-radius: 8px;
  text-align: center;
  margin: 2rem 0;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .dashboard-container {
    padding: 1rem 0;
  }
  
  .page-title {
    font-size: 1.5rem;
  }
  
  .stats-grid {
    grid-template-columns: 1fr;
    gap: 1rem;
  }
  
  .stat-card {
    padding: 1rem;
  }
  
  .actions-grid {
    grid-template-columns: 1fr;
  }
  
  .action-card {
    padding: 1.5rem 1rem;
  }
}
</style>