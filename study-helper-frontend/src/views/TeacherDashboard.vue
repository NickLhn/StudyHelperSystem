<template>
  <div class="page">
    <h1 class="page-title">教学仪表盘</h1>

    <section class="stats">
      <div class="stat-card">
        <div class="sicon">📚</div>
        <div class="sbody">
          <div class="slabel">我的课程</div>
          <div class="svalue">{{ stats.courses }}</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="sicon">📋</div>
        <div class="sbody">
          <div class="slabel">任务管理</div>
          <div class="svalue">{{ stats.tasks }}</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="sicon">📝</div>
        <div class="sbody">
          <div class="slabel">测验管理</div>
          <div class="svalue">{{ stats.quizzes }}</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="sicon">📁</div>
        <div class="sbody">
          <div class="slabel">资料中心</div>
          <div class="svalue">{{ stats.materials }}</div>
        </div>
      </div>
    </section>

    <section class="core">
      <router-link to="/teacher/courses" class="core-card">
        <div class="core-icon">📚</div>
        <div class="core-info">
          <div class="core-title">我的课程</div>
          <div class="core-sub">创建与管理教学课程</div>
        </div>
      </router-link>

      <router-link to="/teacher/tasks" class="core-card">
        <div class="core-icon">📋</div>
        <div class="core-info">
          <div class="core-title">任务管理</div>
          <div class="core-sub">发布任务与查看进度</div>
        </div>
      </router-link>

      <router-link to="/teacher/quizzes" class="core-card">
        <div class="core-icon">📝</div>
        <div class="core-info">
          <div class="core-title">测验管理</div>
          <div class="core-sub">创建测验与查看记录</div>
        </div>
      </router-link>

      <router-link to="/teacher/materials" class="core-card">
        <div class="core-icon">📁</div>
        <div class="core-info">
          <div class="core-title">资料中心</div>
          <div class="core-sub">上传资料与管理资源</div>
        </div>
      </router-link>
    </section>

    <div v-if="error" class="error">{{ error }}</div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useUserStore } from '../stores/user'
import { courseApi } from '../api/course'
import { taskApi } from '../api/task'
import { quizApi } from '../api/quiz'
import { materialApi } from '../api/material'

const userStore = useUserStore()
const error = ref('')

const stats = reactive({
  courses: 0,
  tasks: 0,
  quizzes: 0,
  materials: 0
})

const loadStats = async () => {
  error.value = ''
  if (!userStore.user?.id) return
  const userId = userStore.user.id

  try {
    const [coursesRes, tasksRes, quizzesRes, materialsRes] = await Promise.allSettled([
      courseApi.getUserCourses(userId),
      taskApi.getUserTasks(userId),
      quizApi.getUserQuizzes(userId),
      materialApi.getAllMaterials(userId)
    ])

    if (coursesRes.status === 'fulfilled' && coursesRes.value.data.code === 200) {
      stats.courses = Array.isArray(coursesRes.value.data.data) ? coursesRes.value.data.data.length : 0
    }
    if (tasksRes.status === 'fulfilled' && tasksRes.value.data.code === 200) {
      stats.tasks = Array.isArray(tasksRes.value.data.data) ? tasksRes.value.data.data.length : 0
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

onMounted(() => {
  loadStats()
})
</script>

<style scoped>
.page {
  padding: 0.5rem 0;
}

.page-title {
  margin: 0.25rem 0 1rem 0;
  font-size: 1.5rem;
  font-weight: 900;
  color: #111827;
}

.stats {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0.75rem;
  margin-bottom: 1rem;
}

.stat-card {
  display: flex;
  gap: 0.75rem;
  align-items: center;
  background: #ffffff;
  border: 1px solid #eef2f7;
  border-radius: 14px;
  padding: 1rem;
  box-shadow: 0 6px 18px rgba(15, 23, 42, 0.06);
}

.sicon {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fff1f2;
  font-size: 20px;
  flex: 0 0 auto;
}

.slabel {
  color: #6b7280;
  font-weight: 800;
  font-size: 0.9rem;
}

.svalue {
  color: #111827;
  font-weight: 900;
  font-size: 1.6rem;
  line-height: 1.1;
}

.core {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0.75rem;
}

.core-card {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 1rem;
  background: #ffffff;
  border-radius: 14px;
  text-decoration: none;
  color: inherit;
  box-shadow: 0 6px 18px rgba(15, 23, 42, 0.06);
  border: 1px solid #eef2f7;
}

.core-icon {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #eef2ff;
  font-size: 20px;
  flex: 0 0 auto;
}

.core-title {
  font-weight: 900;
  color: #111827;
  margin-bottom: 2px;
}

.core-sub {
  color: #6b7280;
  font-size: 0.9rem;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.error {
  margin-top: 1rem;
  padding: 0.75rem 1rem;
  border-radius: 12px;
  background: #fee2e2;
  color: #991b1b;
  font-weight: 700;
}

@media (max-width: 768px) {
  .stats,
  .core {
    grid-template-columns: 1fr;
  }
}
</style>
