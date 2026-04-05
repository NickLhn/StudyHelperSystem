<template>
  <div class="page-stack">
    <section class="page-intro">
      <div class="page-intro-copy">
        <span class="page-eyebrow">Platform Overview</span>
        <h2 class="page-title">管理员首页改成更像一个平台运营看板</h2>
        <p class="page-subtitle">用户增长、课程规模、资料数量和角色结构统一到同一套概览视图里，方便后续继续扩展运营模块。</p>
      </div>
      <div class="page-actions">
        <span class="chip">平台实时概览</span>
      </div>
    </section>

    <section class="stats-grid">
      <article class="stat-panel">
        <div class="stat-kicker">总用户数</div>
        <div class="stat-value">{{ stats.totalUsers || 0 }}</div>
        <p class="stat-copy">今日新增 {{ stats.todayNewUsers || 0 }}</p>
      </article>
      <article class="stat-panel">
        <div class="stat-kicker">总课程数</div>
        <div class="stat-value">{{ stats.totalCourses || 0 }}</div>
        <p class="stat-copy">今日新增 {{ stats.todayNewCourses || 0 }}</p>
      </article>
      <article class="stat-panel">
        <div class="stat-kicker">总资料数</div>
        <div class="stat-value">{{ stats.totalMaterials || 0 }}</div>
        <p class="stat-copy">今日新增 {{ stats.todayNewMaterials || 0 }}</p>
      </article>
      <article class="stat-panel">
        <div class="stat-kicker">角色结构</div>
        <div class="stat-value">{{ stats.studentCount || 0 }}</div>
        <p class="stat-copy">学生为主，教师 {{ stats.teacherCount || 0 }}，管理员 {{ stats.adminCount || 0 }}</p>
      </article>
    </section>

    <section class="panel-grid two-up">
      <article class="info-card">
        <div class="stack-md">
          <div>
            <h3 class="section-title">角色分布</h3>
            <p class="section-copy">用更易读的方式查看平台当前主要人群结构。</p>
          </div>
          <div class="data-points">
            <div class="data-point"><span>学生</span><strong>{{ stats.studentCount || 0 }}</strong></div>
            <div class="data-point"><span>教师</span><strong>{{ stats.teacherCount || 0 }}</strong></div>
            <div class="data-point"><span>管理员</span><strong>{{ stats.adminCount || 0 }}</strong></div>
          </div>
        </div>
      </article>

      <article class="info-card">
        <div class="stack-md">
          <div>
            <h3 class="section-title">管理入口</h3>
            <p class="section-copy">保留最常见的后台治理动作，进入路径更短。</p>
          </div>
          <div class="action-grid">
            <router-link to="/admin/users" class="quick-link">
              <strong>用户管理</strong>
              <span>搜索、筛选与角色调整</span>
            </router-link>
            <router-link to="/admin/content" class="quick-link">
              <strong>内容概览</strong>
              <span>课程和资料的整体质量</span>
            </router-link>
            <router-link to="/admin/invitations" class="quick-link">
              <strong>邀请码管理</strong>
              <span>控制注册入口状态</span>
            </router-link>
          </div>
        </div>
      </article>
    </section>

    <section v-if="loading" class="loading-panel">
      <p class="loading-copy">加载统计数据中...</p>
    </section>

    <section v-if="error" class="message-banner error">
      {{ error }}
    </section>
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
  } finally {
    loading.value = false
  }
}

onMounted(loadStats)
</script>

<style scoped>
.quick-link {
  display: grid;
  gap: 4px;
  padding: 16px 18px;
  border-radius: 18px;
  border: 1px solid rgba(23, 32, 51, 0.08);
  background: rgba(255, 255, 255, 0.78);
  color: var(--ink);
}

.quick-link:hover {
  background: rgba(44, 96, 214, 0.07);
  text-decoration: none;
}

.quick-link span {
  color: var(--gray-500);
  font-size: 13px;
}
</style>
