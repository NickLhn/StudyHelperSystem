<template>
  <div class="page-stack">
    <section class="page-intro form-shell">
      <div class="page-intro-copy">
        <span class="page-eyebrow">Course Editor</span>
        <h2 class="page-title">{{ isEdit ? '编辑课程信息' : '创建一门新课程' }}</h2>
        <p class="page-subtitle">把课程名称、分类、教师、时间和地点整理到统一表单里，后续资料和任务才能挂得更清晰。</p>
      </div>
    </section>

    <main class="form-shell">
      <section class="info-card form-panel">
        <form class="form-grid" @submit.prevent="handleSubmit">
          <div class="field">
            <label>课程名称</label>
            <input v-model="form.name" type="text" placeholder="如：高等数学" required />
          </div>

          <div class="form-grid two-up">
            <div class="field">
              <label>课程分类</label>
              <select v-model="form.category" required>
                <option v-for="cat in categories" :key="cat.value" :value="cat.value">
                  {{ cat.label }}
                </option>
              </select>
            </div>
            <div class="field">
              <label>任课教师</label>
              <input v-model="form.teacher" type="text" placeholder="请输入教师姓名" />
            </div>
          </div>

          <div class="form-grid two-up">
            <div class="field">
              <label>学期标记</label>
              <input v-model="form.semesterLabel" type="text" placeholder="如：2026 春季学期" />
            </div>
            <div class="field">
              <label>课程状态</label>
              <input :value="isEdit ? (form.statusLabel || '进行中') : '创建后默认进行中'" type="text" disabled />
            </div>
          </div>

          <div class="form-grid two-up">
            <div class="field">
              <label>上课时间</label>
              <input v-model="form.schedule" type="text" placeholder="如：周一 8:00-9:40" />
            </div>
            <div class="field">
              <label>上课地点</label>
              <input v-model="form.location" type="text" placeholder="如：教学楼 A-101" />
            </div>
          </div>

          <div class="field">
            <label>备注</label>
            <textarea v-model="form.remark" rows="4" placeholder="补充课程说明、课堂要求或特殊安排"></textarea>
          </div>

          <div v-if="error" class="message-banner error">{{ error }}</div>
          <div v-if="success" class="message-banner success">{{ success }}</div>

          <div class="toolbar-row">
            <button type="button" class="edu-btn edu-btn-secondary" @click="goBack">取消</button>
            <button type="submit" class="edu-btn edu-btn-primary" :disabled="loading">
              {{ loading ? '保存中...' : (isEdit ? '保存修改' : '创建课程') }}
            </button>
          </div>
        </form>
      </section>
    </main>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '../stores/user'
import { courseApi } from '../api/course'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const isEdit = computed(() => !!route.params.id)
const courseId = computed(() => route.params.id)

const form = reactive({
  name: '',
  category: 'REQUIRED',
  teacher: '',
  semesterLabel: '',
  schedule: '',
  location: '',
  remark: '',
  statusLabel: '进行中'
})

const categories = [
  { value: 'REQUIRED', label: '必修' },
  { value: 'ELECTIVE', label: '选修' },
  { value: 'PUBLIC', label: '公共课' },
  { value: 'OTHER', label: '其他' }
]

const error = ref('')
const success = ref('')
const loading = ref(false)

const fetchCourse = async () => {
  if (!isEdit.value) return

  try {
    const response = await courseApi.getCourseById(courseId.value, userStore.user.id)
    if (response.data.code === 200) {
      Object.assign(form, response.data.data)
    } else {
      error.value = '获取课程信息失败'
    }
  } catch (err) {
    error.value = '获取课程信息失败'
  }
}

const handleSubmit = async () => {
  error.value = ''
  success.value = ''
  loading.value = true

  try {
    const response = isEdit.value
      ? await courseApi.updateCourse(courseId.value, userStore.user.id, form)
      : await courseApi.createCourse(userStore.user.id, form)

    if (response.data.code === 200) {
      success.value = isEdit.value ? '课程更新成功！' : '课程添加成功！'
      setTimeout(() => {
        router.push(route.path.startsWith('/teacher/') ? '/teacher/courses' : '/courses')
      }, 900)
    } else {
      error.value = response.data.message
    }
  } catch (err) {
    error.value = isEdit.value ? '更新失败，请检查网络连接' : '添加失败，请检查网络连接'
  } finally {
    loading.value = false
  }
}

const goBack = () => {
  router.push(route.path.startsWith('/teacher/') ? '/teacher/courses' : '/courses')
}

onMounted(fetchCourse)
</script>
