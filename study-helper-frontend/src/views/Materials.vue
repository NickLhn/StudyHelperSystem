<template>
  <div class="page-stack">
    <section class="page-intro">
      <div class="page-intro-copy">
        <span class="page-eyebrow">Material Library</span>
        <h2 class="page-title">把资料中心做成可筛选、可搜索、可追踪版本的资源目录</h2>
        <p class="page-subtitle">现在可以按课程、分类、文件类型和关键词筛选，也能直接看到标签和版本说明。</p>
      </div>
      <div class="page-actions">
        <span class="chip">共 {{ filteredMaterials.length }} 份资料</span>
        <router-link to="/material/upload" class="edu-btn edu-btn-primary">上传资料</router-link>
      </div>
    </section>

    <section class="filter-card">
      <div class="toolbar-row filters-wrap">
        <select class="edu-input select" v-model="selectedCourse">
          <option value="">全部课程</option>
          <option v-for="course in courses" :key="course.id" :value="String(course.id)">
            {{ course.name }}
          </option>
        </select>

        <select class="edu-input select" v-model="selectedCategory">
          <option value="">全部分类</option>
          <option v-for="item in materialCategories" :key="item.value" :value="item.value">
            {{ item.label }}
          </option>
        </select>

        <select class="edu-input select" v-model="selectedType">
          <option value="">全部类型</option>
          <option v-for="item in fileTypeOptions" :key="item" :value="item">
            {{ item }}
          </option>
        </select>

        <input
          type="text"
          v-model="searchKeyword"
          class="edu-input search-input"
          placeholder="搜索资料名、描述、标签或版本"
        />
      </div>
    </section>

    <section v-if="loading" class="loading-panel">
      <p class="loading-copy">资料加载中...</p>
    </section>

    <section v-else-if="filteredMaterials.length === 0" class="empty-panel">
      <h3 class="empty-title">没有匹配到资料</h3>
      <p class="empty-copy">可以换个课程、分类或关键词再试试，或者先上传第一份资源。</p>
    </section>

    <section v-else class="card-grid cards-3">
      <article
        v-for="material in filteredMaterials"
        :key="material.id"
        class="info-card entity-card material-card"
        @click="viewMaterial(material.id)"
      >
        <div class="entity-card-header">
          <div>
            <h3 class="entity-card-title">{{ material.name }}</h3>
            <p class="entity-card-subtitle">{{ material.fileName }}</p>
          </div>
          <span class="chip">{{ getFileLabel(material.fileType) }}</span>
        </div>

        <div class="material-meta">
          <span class="soft-badge">{{ getCategoryLabel(material.category) }}</span>
          <span v-if="material.versionLabel" class="soft-badge strong-badge">{{ material.versionLabel }}</span>
          <span v-if="material.courseName" class="soft-badge">{{ material.courseName }}</span>
        </div>

        <div class="data-points">
          <div class="data-point">
            <span>文件大小</span>
            <strong>{{ material.fileSizeFormatted }}</strong>
          </div>
          <div class="data-point">
            <span>上传者</span>
            <strong>{{ material.username }}</strong>
          </div>
        </div>

        <p v-if="material.description" class="material-description">{{ material.description }}</p>
        <p v-if="material.versionNote" class="material-version-note">版本说明：{{ material.versionNote }}</p>

        <div v-if="material.tags?.length" class="tag-row">
          <span v-for="tag in material.tags" :key="tag" class="tag-chip">#{{ tag }}</span>
        </div>

        <div class="material-stats">
          <button type="button" class="soft-badge" @click.stop="toggleLike(material)">赞 {{ material.likeCount }}</button>
          <button type="button" class="soft-badge" @click.stop="toggleFavorite(material)">藏 {{ material.favoriteCount }}</button>
          <span class="soft-badge">评 {{ material.commentCount }}</span>
          <span class="soft-badge">下 {{ material.downloadCount }}</span>
        </div>
      </article>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '../stores/user'
import { materialApi } from '../api/material'
import { courseApi } from '../api/course'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const materials = ref([])
const courses = ref([])
const loading = ref(false)
const selectedCourse = ref('')
const selectedCategory = ref('')
const selectedType = ref('')
const searchKeyword = ref('')

const materialCategories = [
  { value: 'LECTURE_NOTE', label: '讲义课件' },
  { value: 'ASSIGNMENT', label: '作业资料' },
  { value: 'REFERENCE', label: '参考资料' },
  { value: 'EXAM', label: '考试复习' },
  { value: 'LAB', label: '实验资料' },
  { value: 'OTHER', label: '其他' }
]

const fileTypeOptions = computed(() => {
  const values = new Set(materials.value.map((item) => getFileLabel(item.fileType)))
  return Array.from(values).sort()
})

const filteredMaterials = computed(() => {
  const keyword = searchKeyword.value.trim().toLowerCase()

  return materials.value.filter((material) => {
    const courseMatch = !selectedCourse.value || String(material.courseId || '') === selectedCourse.value
    const categoryMatch = !selectedCategory.value || (material.category || '') === selectedCategory.value
    const typeMatch = !selectedType.value || getFileLabel(material.fileType) === selectedType.value

    const searchPool = [
      material.name,
      material.fileName,
      material.description,
      material.courseName,
      material.versionLabel,
      material.versionNote,
      ...(material.tags || [])
    ]
      .filter(Boolean)
      .join(' ')
      .toLowerCase()

    const keywordMatch = !keyword || searchPool.includes(keyword)
    return courseMatch && categoryMatch && typeMatch && keywordMatch
  })
})

const applyKeywordFromRoute = () => {
  const keyword = typeof route.query.keyword === 'string' ? route.query.keyword : ''
  if (keyword) {
    searchKeyword.value = keyword
  }
}

const fetchMaterials = async () => {
  if (!userStore.user) return

  loading.value = true
  try {
    const response = await materialApi.getAllMaterials(userStore.user.id)
    if (response.data.code === 200) {
      materials.value = response.data.data
    }
  } catch (error) {
    console.error('获取资料失败:', error)
  } finally {
    loading.value = false
  }
}

const fetchCourses = async () => {
  if (!userStore.user) return

  try {
    const apiCall = userStore.isTeacher ? courseApi.getUserCourses : courseApi.getStudentCourses
    const response = await apiCall(userStore.user.id)
    if (response.data.code === 200) {
      courses.value = response.data.data
    }
  } catch (error) {
    console.error('获取课程失败:', error)
  }
}

const toggleLike = async (material) => {
  try {
    const response = await materialApi.toggleLike(material.id, userStore.user.id)
    if (response.data.code === 200) {
      Object.assign(material, response.data.data)
    }
  } catch (error) {
    console.error('点赞操作失败:', error)
  }
}

const toggleFavorite = async (material) => {
  try {
    const response = await materialApi.toggleFavorite(material.id, userStore.user.id)
    if (response.data.code === 200) {
      Object.assign(material, response.data.data)
    }
  } catch (error) {
    console.error('收藏操作失败:', error)
  }
}

const viewMaterial = (materialId) => {
  router.push(`/material/${materialId}`)
}

const getFileLabel = (fileType) => String(fileType || 'FILE').replace('.', '').toUpperCase()

const getCategoryLabel = (category) => {
  const matched = materialCategories.find((item) => item.value === category)
  return matched ? matched.label : '未分类'
}

onMounted(() => {
  fetchMaterials()
  fetchCourses()
  applyKeywordFromRoute()
})
</script>

<style scoped>
.filters-wrap {
  flex-wrap: wrap;
}

.search-input {
  min-width: 240px;
  flex: 1;
}

.material-card {
  cursor: pointer;
}

.material-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.material-description,
.material-version-note {
  margin: 0;
  color: var(--gray-700);
  line-height: 1.7;
}

.material-version-note {
  color: var(--gray-600);
  font-size: 13px;
}

.material-stats,
.tag-row {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.soft-badge {
  background: rgba(23, 32, 51, 0.06);
  color: var(--ink-soft);
  border-color: rgba(23, 32, 51, 0.06);
}

.strong-badge {
  background: rgba(44, 96, 214, 0.12);
  color: #2c60d6;
}

.tag-chip {
  display: inline-flex;
  min-height: 28px;
  align-items: center;
  padding: 0 12px;
  border-radius: 999px;
  background: rgba(15, 139, 132, 0.10);
  color: #0f6962;
  font-size: 12px;
  font-weight: 700;
}
</style>
