<template>
  <div class="page-stack">
    <section class="page-intro">
      <div class="page-intro-copy">
        <span class="page-eyebrow">Notifications</span>
        <h2 class="page-title">把截止提醒、测验发布和资料更新收进一个消息中心</h2>
        <p class="page-subtitle">这里会根据你的身份动态生成提醒，先解决“需要我现在去做什么”这个问题。</p>
      </div>
      <div class="page-actions">
        <router-link :to="workspacePath" class="edu-btn edu-btn-secondary">返回工作区</router-link>
      </div>
    </section>

    <section class="stats-grid">
      <article class="stat-panel">
        <div class="stat-kicker">总提醒数</div>
        <div class="stat-value">{{ summary.totalCount || 0 }}</div>
        <p class="stat-copy">当前根据课程、作业、测验和资料动态生成。</p>
      </article>
      <article class="stat-panel">
        <div class="stat-kicker">紧急提醒</div>
        <div class="stat-value">{{ summary.urgentCount || 0 }}</div>
        <p class="stat-copy">优先处理即将截止或待复核任务。</p>
      </article>
      <article class="stat-panel">
        <div class="stat-kicker">作业相关</div>
        <div class="stat-value">{{ (summary.homeworkCount || 0) + (summary.reviewCount || 0) }}</div>
        <p class="stat-copy">包括作业截止、提交结果和教师复核提醒。</p>
      </article>
      <article class="stat-panel">
        <div class="stat-kicker">内容更新</div>
        <div class="stat-value">{{ (summary.quizCount || 0) + (summary.materialCount || 0) }}</div>
        <p class="stat-copy">测验发布和资料更新会集中展示在这里。</p>
      </article>
    </section>

    <section v-if="loading" class="loading-panel">
      <p class="loading-copy">消息加载中...</p>
    </section>

    <section v-else-if="notifications.length === 0" class="empty-panel">
      <h3 class="empty-title">当前没有需要处理的提醒</h3>
      <p class="empty-copy">后续当课程资料更新、作业临近截止或需要批改时，这里会自动出现动态消息。</p>
    </section>

    <section v-else class="info-card">
      <div class="table-header-row">
        <div>
          <h3 class="section-title">最新提醒</h3>
          <p class="section-copy">系统会按优先级和时间排序，把更紧急的事项放在更靠前的位置。</p>
        </div>
      </div>

      <div class="notification-list">
        <article v-for="item in notifications" :key="item.id" class="notification-item">
          <div class="notification-head">
            <div class="notification-tags">
              <span class="chip" :class="getLevelClass(item.level)">{{ getLevelLabel(item.level) }}</span>
              <span class="chip neutral">{{ item.categoryLabel }}</span>
              <span v-if="item.courseName" class="chip soft">{{ item.courseName }}</span>
            </div>
            <span class="notification-time">{{ formatDateTime(item.createdAt) }}</span>
          </div>
          <div class="notification-body">
            <h4>{{ item.title }}</h4>
            <p>{{ item.message }}</p>
          </div>
          <div v-if="item.actionPath" class="notification-actions">
            <router-link :to="item.actionPath" class="inline-link">
              {{ item.actionLabel || '查看详情' }}
            </router-link>
          </div>
        </article>
      </div>
    </section>

    <section v-if="error" class="message-banner error">
      {{ error }}
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useUserStore } from '../stores/user'
import { notificationApi } from '../api/notification'

const userStore = useUserStore()

const notifications = ref([])
const summary = ref({})
const loading = ref(false)
const error = ref('')

const workspacePath = computed(() => {
  if (userStore.isAdmin) return '/admin'
  if (userStore.isTeacher) return '/teacher'
  if (userStore.isStudent) return '/student'
  return '/'
})

const loadNotifications = async () => {
  if (!userStore.user?.id) return
  loading.value = true
  error.value = ''

  try {
    const response = await notificationApi.getNotifications(userStore.user.id, 24)
    if (response.data.code === 200) {
      notifications.value = response.data.data.items || []
      summary.value = response.data.data.summary || {}
    } else {
      error.value = response.data.message || '获取消息失败'
    }
  } catch (err) {
    error.value = '获取消息失败，请稍后再试'
  } finally {
    loading.value = false
  }
}

const getLevelLabel = (level) => {
  if (level === 'urgent') return '紧急'
  if (level === 'warning') return '注意'
  if (level === 'success') return '已更新'
  return '提醒'
}

const getLevelClass = (level) => {
  if (level === 'urgent') return 'danger'
  if (level === 'warning') return 'warning'
  if (level === 'success') return 'success'
  return 'primary'
}

const formatDateTime = (value) => {
  if (!value) return '刚刚'
  return String(value).replace('T', ' ').slice(0, 16)
}

onMounted(loadNotifications)
</script>

<style scoped>
.table-header-row {
  display: flex;
  justify-content: space-between;
  gap: var(--spacing-md);
  margin-bottom: var(--spacing-md);
}

.notification-list {
  display: grid;
  gap: 14px;
}

.notification-item {
  display: grid;
  gap: 12px;
  padding: 18px 20px;
  border-radius: 22px;
  background: rgba(246, 248, 252, 0.92);
  border: 1px solid rgba(23, 32, 51, 0.06);
}

.notification-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.notification-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.notification-time {
  color: var(--gray-500);
  font-size: 12px;
  white-space: nowrap;
}

.notification-body h4 {
  margin: 0 0 6px;
  font-size: 1rem;
  color: var(--gray-900);
}

.notification-body p {
  margin: 0;
  color: var(--gray-700);
  line-height: 1.75;
}

.notification-actions {
  display: flex;
  justify-content: flex-end;
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

.chip.soft {
  background: rgba(15, 139, 132, 0.12);
  color: #0f6962;
}
</style>
