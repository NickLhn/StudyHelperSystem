<template>
  <div class="page-stack">
    <section class="page-intro">
      <div class="page-intro-copy">
        <span class="page-eyebrow">Course Atlas</span>
        <h2 class="page-title">把课程页做成真正的教学总表</h2>
        <p class="page-subtitle">课程信息、学生规模、任务数量和邀请码统一放在同一种卡片里，一眼就能看懂哪门课还需要继续完善。</p>
      </div>
      <div class="page-actions">
        <span class="chip">共 {{ courses.length }} 门课程</span>
        <router-link to="/teacher/course/create" class="edu-btn edu-btn-primary">创建课程</router-link>
      </div>
    </section>

    <section class="filter-card">
      <div class="toolbar-row">
        <button
          v-for="filter in statusFilters"
          :key="filter.value"
          type="button"
          class="edu-btn"
          :class="selectedStatus === filter.value ? 'edu-btn-primary' : 'edu-btn-secondary'"
          @click="changeStatusFilter(filter.value)"
        >
          {{ filter.label }}
        </button>
      </div>
    </section>

    <section v-if="loading" class="loading-panel">
      <p class="loading-copy">课程列表加载中...</p>
    </section>

    <section v-else-if="courses.length === 0" class="empty-panel">
      <h3 class="empty-title">还没有创建课程</h3>
      <p class="empty-copy">先建立课程，再把任务、测验和资料逐步挂进去。</p>
    </section>

    <section v-else class="card-grid cards-3">
      <article v-for="course in courses" :key="course.id" class="info-card entity-card">
        <div class="entity-card-header">
          <div>
            <h3 class="entity-card-title">{{ course.name }}</h3>
            <p class="entity-card-subtitle">
              {{ course.teacher || '未填写任课教师' }}
              <span v-if="course.semesterLabel"> · {{ course.semesterLabel }}</span>
            </p>
          </div>
          <span class="status-pill" :class="course.status?.toLowerCase()">{{ course.statusLabel || '进行中' }}</span>
        </div>

        <div class="data-points">
          <div class="data-point">
            <span>学生人数</span>
            <strong>{{ course.studentCount || 0 }}</strong>
          </div>
          <div class="data-point">
            <span>资料数量</span>
            <strong>{{ course.materialCount || 0 }}</strong>
          </div>
          <div class="data-point">
            <span>任务数量</span>
            <strong>{{ course.taskCount || 0 }}</strong>
          </div>
          <div class="data-point">
            <span>邀请码</span>
            <strong>{{ course.invitationCode || '未生成' }}</strong>
          </div>
          <div class="data-point">
            <span>课程分类</span>
            <strong>{{ course.categoryLabel || course.category }}</strong>
          </div>
        </div>

        <div class="entity-card-actions">
          <router-link :to="`/teacher/course/${course.id}/overview`" class="edu-btn edu-btn-primary">课程管理</router-link>
          <button type="button" class="edu-btn edu-btn-secondary" @click="editCourse(course.id)">编辑</button>
          <button
            type="button"
            class="edu-btn edu-btn-secondary"
            @click="toggleArchive(course)"
          >
            {{ course.status === 'ARCHIVED' ? '恢复课程' : '归档课程' }}
          </button>
          <button type="button" class="edu-btn edu-btn-danger" @click="deleteCourse(course.id)">删除</button>
        </div>
      </article>
    </section>

    <section v-if="error" class="message-banner error">
      {{ error }}
    </section>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { courseApi } from '../api/course'
import { useUserStore } from '../stores/user'

const router = useRouter()
const userStore = useUserStore()
const courses = ref([])
const loading = ref(false)
const error = ref('')
const selectedStatus = ref('ALL')

const statusFilters = [
  { value: 'ALL', label: '全部课程' },
  { value: 'ACTIVE', label: '进行中' },
  { value: 'ARCHIVED', label: '已归档' }
]

const loadCourses = async () => {
  loading.value = true
  error.value = ''

  try {
    if (!userStore.user?.id) {
      error.value = '用户未登录'
      return
    }

    const response = selectedStatus.value === 'ALL'
      ? await courseApi.getUserCourses(userStore.user.id)
      : await courseApi.getUserCoursesByStatus(userStore.user.id, selectedStatus.value)
    if (response.data.code === 200) {
      courses.value = response.data.data
    } else {
      error.value = response.data.message
    }
  } catch (err) {
    error.value = '获取课程列表失败，请检查网络连接'
  } finally {
    loading.value = false
  }
}

const editCourse = (courseId) => {
  router.push(`/teacher/course/edit/${courseId}`)
}

const changeStatusFilter = (status) => {
  selectedStatus.value = status
  loadCourses()
}

const toggleArchive = async (course) => {
  const targetStatus = course.status === 'ARCHIVED' ? 'ACTIVE' : 'ARCHIVED'
  const message = targetStatus === 'ARCHIVED' ? '确定归档这门课程吗？归档后新学生将不能加入。' : '确定恢复这门课程吗？'
  if (!confirm(message)) return

  try {
    const response = await courseApi.updateCourseStatus(course.id, userStore.user.id, targetStatus)
    if (response.data.code === 200) {
      await loadCourses()
    } else {
      error.value = response.data.message
    }
  } catch (err) {
    error.value = '更新课程状态失败'
  }
}

const deleteCourse = async (courseId) => {
  if (!confirm('确定要删除这门课程吗？此操作不可撤销。')) return

  try {
    const response = await courseApi.deleteCourse(courseId, userStore.user.id)
    if (response.data.code === 200) {
      loadCourses()
    } else {
      error.value = response.data.message
    }
  } catch (err) {
    error.value = '删除课程失败'
  }
}

onMounted(loadCourses)
</script>
