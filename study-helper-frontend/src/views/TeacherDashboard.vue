<template>
  <div class="page-stack">
    <section class="page-intro">
      <div class="page-intro-copy">
        <span class="page-eyebrow">Teaching Studio</span>
        <h2 class="page-title">把教学事务收束到一个干净的工作台里</h2>
        <p class="page-subtitle">
          课程、任务、测验和资料不再分散在不同风格的页面里。现在你可以先看到整体节奏，再进入具体操作。
        </p>
      </div>
      <div class="page-actions">
        <router-link to="/teacher/course/create" class="edu-btn edu-btn-primary">创建课程</router-link>
        <router-link to="/teacher/homework/create" class="edu-btn edu-btn-secondary">创建作业</router-link>
        <router-link to="/teacher/quiz/create" class="edu-btn edu-btn-secondary">创建测验</router-link>
      </div>
    </section>

    <section class="stats-grid">
      <article class="stat-panel" v-for="card in overviewCards" :key="card.label">
        <div class="stat-kicker">{{ card.label }}</div>
        <div class="stat-value">{{ card.value }}</div>
        <p class="stat-copy">{{ card.copy }}</p>
      </article>
    </section>

    <section v-if="recentNotifications.length > 0" class="info-card">
      <div class="table-header-row">
        <div>
          <h3 class="section-title">最新提醒</h3>
          <p class="section-copy">把最需要你马上处理的事项放在首页，减少来回跳转。</p>
        </div>
        <router-link to="/messages" class="inline-link">进入消息中心</router-link>
      </div>

      <div class="alert-grid">
        <article v-for="item in recentNotifications" :key="item.id" class="alert-item">
          <div class="alert-head">
            <span class="chip" :class="getLevelClass(item.level)">{{ getLevelLabel(item.level) }}</span>
            <span class="alert-time">{{ formatDateTime(item.createdAt) }}</span>
          </div>
          <strong>{{ item.title }}</strong>
          <p>{{ item.message }}</p>
          <router-link :to="item.actionPath" class="inline-link">{{ item.actionLabel || '查看详情' }}</router-link>
        </article>
      </div>
    </section>

    <section class="panel-grid two-up">
      <article class="info-card">
        <div class="stack-md">
          <div>
            <h3 class="section-title">今日工作重点</h3>
            <p class="section-copy">从当前数据快速判断今天应该先推进哪一项教学事务。</p>
          </div>

          <div class="data-points">
            <div class="data-point">
              <span>课程维护</span>
              <strong>{{ stats.courses }} 门正在管理</strong>
            </div>
            <div class="data-point">
              <span>任务发布</span>
              <strong>{{ stats.tasks }} 个任务待跟进</strong>
            </div>
            <div class="data-point">
              <span>作业批改</span>
              <strong>{{ stats.homeworks }} 份作业待查看</strong>
            </div>
            <div class="data-point">
              <span>测验运行</span>
              <strong>{{ stats.quizzes }} 场测验可调整</strong>
            </div>
            <div class="data-point">
              <span>资料补充</span>
              <strong>{{ stats.materials }} 份教学资源</strong>
            </div>
          </div>
        </div>
      </article>

      <article class="info-card">
        <div class="stack-md">
          <div>
            <h3 class="section-title">快捷入口</h3>
            <p class="section-copy">把最常用的操作放到更短的路径里。</p>
          </div>

          <div class="action-grid">
            <router-link v-for="item in quickLinks" :key="item.path" :to="item.path" class="quick-link">
              <strong>{{ item.title }}</strong>
              <span>{{ item.copy }}</span>
            </router-link>
          </div>
        </div>
      </article>
    </section>

    <section v-if="error" class="message-banner error">
      {{ error }}
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useUserStore } from '../stores/user'
import { courseApi } from '../api/course'
import { homeworkApi } from '../api/homework'
import { taskApi } from '../api/task'
import { quizApi } from '../api/quiz'
import { materialApi } from '../api/material'
import { notificationApi } from '../api/notification'

const userStore = useUserStore()
const error = ref('')
const recentNotifications = ref([])

const stats = reactive({
  courses: 0,
  tasks: 0,
  homeworks: 0,
  quizzes: 0,
  materials: 0
})

const overviewCards = computed(() => [
  { label: '课程总量', value: stats.courses, copy: '本学期正在维护的课程数量' },
  { label: '任务总量', value: stats.tasks, copy: '可以继续完善说明与截止时间' },
  { label: '作业总量', value: stats.homeworks, copy: '正式作业已支持学生提交与自动判分' },
  { label: '测验总量', value: stats.quizzes, copy: '支持自动判分与测验发布' },
  { label: '资料总量', value: stats.materials, copy: '教学材料已统一收束到资料中心' }
])

const quickLinks = [
  { path: '/teacher/courses', title: '进入课程管理', copy: '查看课程信息与班级概况' },
  { path: '/teacher/tasks', title: '进入任务管理', copy: '调整任务安排和学习进度' },
  { path: '/teacher/homeworks', title: '进入作业中心', copy: '发布作业并查看学生提交记录' },
  { path: '/teacher/quizzes', title: '进入测验管理', copy: '配置题目、发布和关闭测验' },
  { path: '/teacher/materials', title: '进入资料中心', copy: '继续上传和整理教学资料' },
  { path: '/messages', title: '查看消息中心', copy: '统一处理待复核和截止提醒' }
]

const loadStats = async () => {
  error.value = ''
  if (!userStore.user?.id) return
  const userId = userStore.user.id

  try {
    const [coursesRes, tasksRes, homeworksRes, quizzesRes, materialsRes] = await Promise.allSettled([
      courseApi.getUserCourses(userId),
      taskApi.getUserTasks(userId),
      homeworkApi.getTeacherHomeworks(userId),
      quizApi.getUserQuizzes(userId),
      materialApi.getAllMaterials(userId)
    ])

    if (coursesRes.status === 'fulfilled' && coursesRes.value.data.code === 200) {
      stats.courses = Array.isArray(coursesRes.value.data.data) ? coursesRes.value.data.data.length : 0
    }
    if (tasksRes.status === 'fulfilled' && tasksRes.value.data.code === 200) {
      stats.tasks = Array.isArray(tasksRes.value.data.data) ? tasksRes.value.data.data.length : 0
    }
    if (homeworksRes.status === 'fulfilled' && homeworksRes.value.data.code === 200) {
      stats.homeworks = Array.isArray(homeworksRes.value.data.data) ? homeworksRes.value.data.data.length : 0
    }
    if (quizzesRes.status === 'fulfilled' && quizzesRes.value.data.code === 200) {
      stats.quizzes = Array.isArray(quizzesRes.value.data.data) ? quizzesRes.value.data.data.length : 0
    }
    if (materialsRes.status === 'fulfilled' && materialsRes.value.data.code === 200) {
      stats.materials = Array.isArray(materialsRes.value.data.data) ? materialsRes.value.data.data.length : 0
    }
  } catch (e) {
    error.value = '加载教学数据失败'
  }
}

const loadNotifications = async () => {
  if (!userStore.user?.id) return
  try {
    const response = await notificationApi.getNotifications(userStore.user.id, 3)
    if (response.data.code === 200) {
      recentNotifications.value = response.data.data.items || []
    }
  } catch (e) {
    recentNotifications.value = []
  }
}

const getLevelLabel = (level) => {
  if (level === 'urgent') return '紧急'
  if (level === 'warning') return '注意'
  if (level === 'success') return '已完成'
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

onMounted(async () => {
  await loadStats()
  await loadNotifications()
})
</script>

<style scoped>
.table-header-row {
  display: flex;
  justify-content: space-between;
  gap: var(--spacing-md);
  margin-bottom: var(--spacing-md);
  align-items: flex-start;
}

.alert-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 16px;
}

.alert-item {
  display: grid;
  gap: 10px;
  padding: 18px;
  border-radius: 20px;
  background: rgba(246, 248, 252, 0.92);
  border: 1px solid rgba(23, 32, 51, 0.06);
}

.alert-head {
  display: flex;
  justify-content: space-between;
  gap: 10px;
  align-items: center;
}

.alert-item p {
  margin: 0;
  color: var(--gray-700);
  line-height: 1.7;
}

.alert-time {
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

.quick-link {
  display: grid;
  gap: 4px;
  padding: 16px 18px;
  border-radius: 18px;
  border: 1px solid rgba(23, 32, 51, 0.08);
  background: rgba(255, 255, 255, 0.76);
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
</style>
