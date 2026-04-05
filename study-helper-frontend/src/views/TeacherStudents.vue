<template>
  <div class="page-stack">
    <section class="page-intro">
      <div class="page-intro-copy">
        <span class="page-eyebrow">Student Hub</span>
        <h2 class="page-title">把学生管理从课程详情里抽出来做成独立工作台</h2>
        <p class="page-subtitle">先按课程查看班级成员，再处理移除、邮箱核对和人数变化，日常管理会轻松很多。</p>
      </div>
      <div class="page-actions">
        <span class="chip">共 {{ totalStudents }} 位学生</span>
      </div>
    </section>

    <section class="filter-card">
      <div class="toolbar-row">
        <label class="sr-only" for="teacher-student-course">选择课程</label>
        <select id="teacher-student-course" v-model="selectedCourseId" @change="loadStudents">
          <option value="">请选择课程</option>
          <option v-for="course in courses" :key="course.id" :value="String(course.id)">
            {{ course.name }}
          </option>
        </select>
      </div>
    </section>

    <section class="stats-grid">
      <article class="stat-panel">
        <div class="stat-kicker">当前课程</div>
        <div class="stat-value stat-value-sm">{{ selectedCourse?.name || '未选择' }}</div>
        <p class="stat-copy">先选课程，再查看对应班级学生名单。</p>
      </article>
      <article class="stat-panel">
        <div class="stat-kicker">学生人数</div>
        <div class="stat-value">{{ totalStudents }}</div>
        <p class="stat-copy">已从课程选课关系中读取真实数据。</p>
      </article>
      <article class="stat-panel">
        <div class="stat-kicker">已填写邮箱</div>
        <div class="stat-value">{{ emailCount }}</div>
        <p class="stat-copy">方便你确认后续通知是否有触达基础。</p>
      </article>
    </section>

    <section v-if="loading" class="loading-panel">
      <p class="loading-copy">学生列表加载中...</p>
    </section>

    <section v-else-if="!selectedCourseId" class="empty-panel">
      <h3 class="empty-title">先选择一门课程</h3>
      <p class="empty-copy">课程选择后，这里会显示该班级的学生信息和管理入口。</p>
    </section>

    <section v-else-if="students.length === 0" class="empty-panel">
      <h3 class="empty-title">当前课程还没有学生加入</h3>
      <p class="empty-copy">你可以先把邀请码发给学生，学生加入后这里会自动显示。</p>
    </section>

    <section v-else class="info-card">
      <div class="table-header-row">
        <div>
          <h3 class="section-title">班级学生名单</h3>
          <p class="section-copy">支持快速核对昵称、用户名和邮箱，并可直接移除无效成员。</p>
        </div>
      </div>

      <div class="table-shell">
        <table class="student-table">
          <thead>
            <tr>
              <th>学生</th>
              <th>账号</th>
              <th>邮箱</th>
              <th>学号</th>
              <th>年级</th>
              <th class="table-actions">操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="student in students" :key="student.id">
              <td>
                <div class="student-meta">
                  <strong>{{ student.nickname || student.username }}</strong>
                  <span>ID {{ student.id }}</span>
                </div>
              </td>
              <td>{{ student.username || '-' }}</td>
              <td>{{ student.email || '-' }}</td>
              <td>{{ student.studentId || '-' }}</td>
              <td>{{ student.grade || '-' }}</td>
              <td class="table-actions">
                <button
                  type="button"
                  class="edu-btn edu-btn-danger"
                  :disabled="removingId === student.id"
                  @click="removeStudent(student)"
                >
                  {{ removingId === student.id ? '移除中...' : '移除' }}
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </section>

    <section v-if="feedback" class="message-banner" :class="feedbackType">
      {{ feedback }}
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useUserStore } from '../stores/user'
import { courseApi } from '../api/course'

const userStore = useUserStore()

const courses = ref([])
const students = ref([])
const selectedCourseId = ref('')
const loading = ref(false)
const removingId = ref(null)
const feedback = ref('')
const feedbackType = ref('success')

const selectedCourse = computed(() => courses.value.find((course) => String(course.id) === selectedCourseId.value) || null)
const totalStudents = computed(() => students.value.length)
const emailCount = computed(() => students.value.filter((student) => !!student.email).length)

const setFeedback = (message, type = 'success') => {
  feedback.value = message
  feedbackType.value = type
}

const loadCourses = async () => {
  try {
    const response = await courseApi.getUserCourses(userStore.user.id)
    if (response.data.code === 200) {
      courses.value = response.data.data
      if (!selectedCourseId.value && courses.value.length > 0) {
        selectedCourseId.value = String(courses.value[0].id)
      }
    } else {
      setFeedback(response.data.message || '获取课程列表失败', 'error')
    }
  } catch (error) {
    setFeedback('获取课程列表失败，请确认后端服务是否已启动', 'error')
  }
}

const loadStudents = async () => {
  feedback.value = ''

  if (!selectedCourseId.value) {
    students.value = []
    return
  }

  loading.value = true
  try {
    const response = await courseApi.getCourseStudents(Number(selectedCourseId.value), userStore.user.id)
    if (response.data.code === 200) {
      students.value = response.data.data
    } else {
      students.value = []
      setFeedback(response.data.message || '获取学生列表失败', 'error')
    }
  } catch (error) {
    students.value = []
    setFeedback('获取学生列表失败，请稍后再试', 'error')
  } finally {
    loading.value = false
  }
}

const removeStudent = async (student) => {
  if (!selectedCourse.value) return
  if (!window.confirm(`确定要将 ${student.nickname || student.username} 移出 ${selectedCourse.value.name} 吗？`)) {
    return
  }

  removingId.value = student.id
  try {
    const response = await courseApi.removeStudent(Number(selectedCourseId.value), userStore.user.id, student.id)
    if (response.data.code === 200) {
      students.value = students.value.filter((item) => item.id !== student.id)
      setFeedback('学生移除成功')
    } else {
      setFeedback(response.data.message || '移除学生失败', 'error')
    }
  } catch (error) {
    setFeedback('移除学生失败，请稍后再试', 'error')
  } finally {
    removingId.value = null
  }
}

onMounted(async () => {
  await loadCourses()
  await loadStudents()
})
</script>

<style scoped>
.stat-value-sm {
  font-size: clamp(1.2rem, 2vw, 1.8rem);
}

.table-header-row {
  display: flex;
  justify-content: space-between;
  gap: var(--spacing-md);
  margin-bottom: var(--spacing-md);
}

.table-shell {
  overflow-x: auto;
}

.student-table {
  width: 100%;
  border-collapse: collapse;
}

.student-table th,
.student-table td {
  padding: 14px 12px;
  border-bottom: 1px solid rgba(23, 32, 51, 0.08);
  text-align: left;
  vertical-align: middle;
}

.student-table th {
  color: var(--gray-500);
  font-size: 12px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.08em;
}

.student-meta {
  display: grid;
  gap: 4px;
}

.student-meta span {
  color: var(--gray-500);
  font-size: 12px;
}

.table-actions {
  text-align: right;
}

@media (max-width: 900px) {
  .table-actions {
    min-width: 110px;
  }
}
</style>
