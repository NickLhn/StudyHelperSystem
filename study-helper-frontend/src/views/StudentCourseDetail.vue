<template>
  <div class="page">
    <div class="header">
      <div class="left">
        <router-link to="/student/courses" class="back">返回</router-link>
        <div class="title">
          <div class="name">{{ course?.name || '课程' }}</div>
          <div class="meta">
            <span>{{ course?.categoryLabel || '-' }}</span>
            <span v-if="course?.teacher">· {{ course.teacher }}</span>
          </div>
        </div>
      </div>
    </div>

    <div class="info">
      <div class="info-item">
        <div class="label">时间</div>
        <div class="value">{{ course?.schedule || '-' }}</div>
      </div>
      <div class="info-item">
        <div class="label">地点</div>
        <div class="value">{{ course?.location || '-' }}</div>
      </div>
      <div class="info-item">
        <div class="label">备注</div>
        <div class="value">{{ course?.remark || '-' }}</div>
      </div>
    </div>

    <section class="card">
      <div class="card-title">课程资料</div>
      <div v-if="materialsLoading" class="muted">加载中...</div>
      <div v-else-if="materials.length === 0" class="muted">暂无资料</div>
      <ul v-else class="list">
        <li v-for="m in materials" :key="m.id" class="li">
          <div class="li-main">
            <div class="li-title">{{ m.name }}</div>
            <div class="li-sub muted">
              <span>{{ m.fileSizeFormatted }}</span>
              <span v-if="m.username">· {{ m.username }}</span>
            </div>
          </div>
          <div class="li-actions">
            <router-link class="btn" :to="`/material/${m.id}?from=course`">详情</router-link>
            <a class="btn primary" :href="downloadUrl(m.id)" target="_blank" rel="noreferrer">下载</a>
          </div>
        </li>
      </ul>
    </section>

    <div v-if="error" class="error">{{ error }}</div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { useUserStore } from '../stores/user'
import { courseApi } from '../api/course'
import { materialApi } from '../api/material'

const route = useRoute()
const userStore = useUserStore()

const courseId = computed(() => Number(route.params.id))

const error = ref('')

const course = ref(null)
const materials = ref([])

const materialsLoading = ref(false)

const downloadUrl = (materialId) => materialApi.downloadMaterial(materialId, userStore.user.id)

const loadCourse = async () => {
  error.value = ''
  try {
    const response = await courseApi.getCourseById(courseId.value, userStore.user.id)
    if (response.data.code === 200) {
      course.value = response.data.data
    } else {
      error.value = response.data.message || '加载课程失败'
    }
  } catch (e) {
    error.value = '加载课程失败'
  }
}

const loadMaterials = async () => {
  materialsLoading.value = true
  try {
    const response = await materialApi.getMaterialsByCourse(courseId.value, userStore.user.id)
    if (response.data.code === 200) {
      materials.value = response.data.data
    }
  } finally {
    materialsLoading.value = false
  }
}

onMounted(async () => {
  await loadCourse()
  await loadMaterials()
})
</script>

<style scoped>
.page {
  padding: 0.25rem 0;
}

.header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 1rem;
  margin-bottom: 1rem;
}

.left {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  min-width: 0;
}

.back {
  text-decoration: none;
  font-weight: 900;
  color: #2563eb;
}

.name {
  font-size: 1.35rem;
  font-weight: 900;
  color: #111827;
}

.meta {
  color: #6b7280;
  font-weight: 800;
}

.info {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 0.75rem;
  margin-bottom: 1rem;
}

.info-item {
  background: #fff;
  border: 1px solid #eef2f7;
  border-radius: 14px;
  padding: 0.85rem 1rem;
  box-shadow: 0 6px 18px rgba(15, 23, 42, 0.06);
}

.label {
  color: #6b7280;
  font-weight: 900;
  font-size: 0.9rem;
}

.value {
  margin-top: 0.25rem;
  color: #111827;
  font-weight: 800;
  word-break: break-word;
}

.tabs {
  display: flex;
  gap: 0.5rem;
  margin-bottom: 0.75rem;
}

.tab {
  height: 36px;
  padding: 0 0.85rem;
  border-radius: 999px;
  border: 1px solid #e5e7eb;
  background: #fff;
  font-weight: 900;
  cursor: pointer;
  color: #111827;
}

.tab.active {
  background: #111827;
  color: #fff;
  border-color: #111827;
}

.card {
  background: #fff;
  border: 1px solid #eef2f7;
  border-radius: 14px;
  padding: 1rem;
  box-shadow: 0 6px 18px rgba(15, 23, 42, 0.06);
}

.card-title {
  font-weight: 900;
  color: #111827;
  margin-bottom: 0.75rem;
}

.muted {
  color: #6b7280;
  font-weight: 700;
}

.list {
  list-style: none;
  padding: 0;
  margin: 0;
  display: grid;
  gap: 0.5rem;
}

.li {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 0.75rem;
  padding: 0.75rem;
  border: 1px solid #eef2f7;
  border-radius: 12px;
}

.li-main {
  min-width: 0;
}

.li-title {
  font-weight: 900;
  color: #111827;
}

.li-sub {
  margin-top: 0.15rem;
}

.desc {
  margin-top: 0.5rem;
  color: #374151;
  font-weight: 700;
  white-space: pre-wrap;
}

.li-actions {
  display: flex;
  gap: 0.5rem;
  flex: 0 0 auto;
}

.btn {
  height: 34px;
  padding: 0 0.75rem;
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  background: #fff;
  font-weight: 900;
  text-decoration: none;
  color: #111827;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
}

.btn.primary {
  background: #2563eb;
  border-color: #2563eb;
  color: #fff;
}

.error {
  margin-top: 1rem;
  padding: 0.75rem 1rem;
  border-radius: 12px;
  background: #fee2e2;
  color: #991b1b;
  font-weight: 900;
}

@media (max-width: 900px) {
  .info {
    grid-template-columns: 1fr;
  }
}
</style>
