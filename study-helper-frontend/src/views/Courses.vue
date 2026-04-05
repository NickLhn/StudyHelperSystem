<template>
  <div class="page-stack">
    <section class="page-intro">
      <div class="page-intro-copy">
        <span class="page-eyebrow">{{ isStudent ? 'Course Space' : 'Course Studio' }}</span>
        <h2 class="page-title">{{ isStudent ? '把课程学习做成一张清楚的学习地图' : '课程信息不该只是一串表单字段' }}</h2>
        <p class="page-subtitle">
          {{ isStudent
            ? '邀请码加入、课程信息和进入课程入口都收束到统一卡片里，页面更像学习空间。'
            : '课程分类、教师信息和操作入口统一到同一套课程卡片里，维护成本更低。' }}
        </p>
      </div>
      <div class="page-actions">
        <span class="chip">共 {{ courses.length }} 门课程</span>
        <router-link v-if="!isStudent" to="/course/create" class="edu-btn edu-btn-primary">添加课程</router-link>
      </div>
    </section>

    <section v-if="isStudent" class="info-card join-card">
      <div class="stack-md">
        <div>
          <h3 class="section-title">通过邀请码加入课程</h3>
          <p class="section-copy">输入教师给你的邀请码，系统会自动把课程加入到当前账户下。</p>
        </div>
        <div class="toolbar-row">
          <input v-model="joinCode" class="edu-input join-input" type="text" placeholder="请输入课程邀请码" />
          <button class="edu-btn edu-btn-primary" type="button" @click="handleJoin">加入课程</button>
        </div>
        <div v-if="joinError" class="message-banner error">{{ joinError }}</div>
      </div>
    </section>

    <section class="filter-card">
      <div class="toolbar-row">
        <span class="section-copy">分类筛选</span>
        <button
          v-if="!isStudent"
          v-for="cat in categories"
          :key="cat.value"
          type="button"
          class="edu-btn"
          :class="selectedCategory === cat.value ? 'edu-btn-primary' : 'edu-btn-secondary'"
          @click="filterByCategory(cat.value)"
        >
          {{ cat.label }}
        </button>
        <button
          v-for="filter in statusFilters"
          :key="filter.value"
          type="button"
          class="edu-btn"
          :class="selectedStatus === filter.value ? 'edu-btn-primary' : 'edu-btn-secondary'"
          @click="filterByStatus(filter.value)"
        >
          {{ filter.label }}
        </button>
      </div>
    </section>

    <section v-if="loading" class="loading-panel">
      <p class="loading-copy">正在加载课程...</p>
    </section>

    <section v-else-if="courses.length === 0" class="empty-panel">
      <h3 class="empty-title">还没有课程</h3>
      <p class="empty-copy">{{ isStudent ? '你可以先用邀请码加入课程。' : '先创建一门课程作为教学工作的起点。' }}</p>
    </section>

    <section v-else class="card-grid cards-3">
      <article v-for="course in courses" :key="course.id" class="info-card entity-card">
        <div class="entity-card-header">
          <div>
            <h3 class="entity-card-title">{{ course.name }}</h3>
            <p class="entity-card-subtitle">
              {{ course.teacher || '未填写教师信息' }}
              <span v-if="course.semesterLabel"> · {{ course.semesterLabel }}</span>
            </p>
          </div>
          <span class="status-pill" :class="course.status?.toLowerCase()">{{ course.statusLabel || '进行中' }}</span>
        </div>

        <div class="data-points">
          <div class="data-point">
            <span>上课时间</span>
            <strong>{{ course.schedule || '待补充' }}</strong>
          </div>
          <div class="data-point">
            <span>上课地点</span>
            <strong>{{ course.location || '待补充' }}</strong>
          </div>
          <div class="data-point">
            <span>课程分类</span>
            <strong>{{ course.categoryLabel || getCategoryLabel(course.category) }}</strong>
          </div>
        </div>

        <div class="entity-card-actions">
          <router-link v-if="isStudent" :to="`/student/course/${course.id}`" class="edu-btn edu-btn-primary">进入课程</router-link>
          <router-link v-else :to="`/course/edit/${course.id}`" class="edu-btn edu-btn-secondary">编辑</router-link>
          <button v-if="!isStudent" type="button" class="edu-btn edu-btn-danger" @click="deleteCourse(course.id)">删除</button>
        </div>
      </article>
    </section>
  </div>
</template>

<script setup>
import { computed, ref, onMounted } from 'vue'
import { useUserStore } from '../stores/user'
import { courseApi } from '../api/course'

const userStore = useUserStore()

const courses = ref([])
const loading = ref(false)
const selectedCategory = ref('ALL')
const selectedStatus = ref('ALL')
const joinCode = ref('')
const joinError = ref('')

const isStudent = computed(() => userStore.isStudent)

const categories = [
  { value: 'ALL', label: '全部' },
  { value: 'REQUIRED', label: '必修' },
  { value: 'ELECTIVE', label: '选修' },
  { value: 'PUBLIC', label: '公共课' },
  { value: 'OTHER', label: '其他' }
]

const statusFilters = [
  { value: 'ALL', label: '全部状态' },
  { value: 'ACTIVE', label: '进行中' },
  { value: 'ARCHIVED', label: '已归档' }
]

const getCategoryLabel = (type) => {
  const mapping = {
    REQUIRED: '必修',
    ELECTIVE: '选修',
    PUBLIC: '公共课',
    OTHER: '其他'
  }
  if (mapping[type]) return mapping[type]
  const category = categories.find((cat) => cat.value === type)
  return category ? category.label : (type || '')
}

const fetchCourses = async () => {
  if (!userStore.user) return

  loading.value = true
  try {
    let response
    if (isStudent.value) {
      response = selectedStatus.value === 'ALL'
        ? await courseApi.getStudentCourses(userStore.user.id)
        : await courseApi.getStudentCoursesByStatus(userStore.user.id, selectedStatus.value)
    } else if (selectedCategory.value === 'ALL') {
      response = selectedStatus.value === 'ALL'
        ? await courseApi.getUserCourses(userStore.user.id)
        : await courseApi.getUserCoursesByStatus(userStore.user.id, selectedStatus.value)
    } else if (selectedStatus.value === 'ALL') {
      response = await courseApi.getUserCoursesByCategory(userStore.user.id, selectedCategory.value)
    } else {
      response = await courseApi.getUserCoursesByCategoryAndStatus(userStore.user.id, selectedCategory.value, selectedStatus.value)
    }
    if (response.data.code === 200) {
      courses.value = response.data.data
    }
  } catch (error) {
    console.error('获取课程失败:', error)
  } finally {
    loading.value = false
  }
}

const handleJoin = async () => {
  joinError.value = ''
  const code = joinCode.value.trim().toUpperCase()
  if (!code) return
  try {
    const response = await courseApi.joinCourse(userStore.user.id, code)
    if (response.data.code === 200) {
      joinCode.value = ''
      await fetchCourses()
    } else {
      joinError.value = response.data.message || '加入失败'
    }
  } catch (e) {
    joinError.value = '加入失败，请检查网络连接'
  }
}

const filterByCategory = (category) => {
  selectedCategory.value = category
  fetchCourses()
}

const filterByStatus = (status) => {
  selectedStatus.value = status
  fetchCourses()
}

const deleteCourse = async (courseId) => {
  if (!confirm('确定要删除这门课程吗？')) return

  try {
    const response = await courseApi.deleteCourse(courseId, userStore.user.id)
    if (response.data.code === 200) {
      courses.value = courses.value.filter((c) => c.id !== courseId)
    } else {
      alert(response.data.message)
    }
  } catch (error) {
    alert('删除失败')
  }
}

onMounted(fetchCourses)
</script>

<style scoped>
.join-input {
  min-width: 220px;
  flex: 1;
}
</style>
