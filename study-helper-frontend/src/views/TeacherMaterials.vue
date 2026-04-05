<template>
  <div class="page-stack">
    <section class="page-intro">
      <div class="page-intro-copy">
        <span class="page-eyebrow">Resource Library</span>
        <h2 class="page-title">资料页现在更像一座可维护的课程资源馆</h2>
        <p class="page-subtitle">教师可以按课程和分类筛选资源，并直接看到标签、版本和资料说明。</p>
      </div>
      <div class="page-actions">
        <span class="chip">共 {{ filteredMaterials.length }} 份资料</span>
        <router-link to="/teacher/material/upload" class="edu-btn edu-btn-primary">上传资料</router-link>
      </div>
    </section>

    <section class="filter-card">
      <div class="toolbar-row filters-wrap">
        <select v-model="selectedCourseId" class="edu-input select">
          <option value="">所有课程</option>
          <option v-for="course in courses" :key="course.id" :value="String(course.id)">
            {{ course.name }}
          </option>
        </select>

        <select v-model="selectedCategory" class="edu-input select">
          <option value="">所有分类</option>
          <option v-for="item in materialCategories" :key="item.value" :value="item.value">
            {{ item.label }}
          </option>
        </select>

        <input v-model="searchKeyword" type="text" class="edu-input search-input" placeholder="搜索资料名称、标签或版本" />
      </div>
    </section>

    <section v-if="loading" class="loading-panel">
      <p class="loading-copy">资料列表加载中...</p>
    </section>

    <section v-else-if="filteredMaterials.length === 0" class="empty-panel">
      <h3 class="empty-title">还没有上传资料</h3>
      <p class="empty-copy">现在开始把课件、讲义和图片资料统一收纳到资料中心。</p>
    </section>

    <section v-else class="card-grid cards-3">
      <article v-for="material in filteredMaterials" :key="material.id" class="info-card entity-card">
        <div class="entity-card-header">
          <div>
            <h3 class="entity-card-title">{{ material.name }}</h3>
            <p class="entity-card-subtitle">{{ material.courseName || '未关联课程' }}</p>
          </div>
          <span class="chip">{{ getFileLabel(material.fileType) }}</span>
        </div>

        <div class="material-meta">
          <span class="soft-badge">{{ getCategoryLabel(material.category) }}</span>
          <span v-if="material.versionLabel" class="soft-badge strong-badge">{{ material.versionLabel }}</span>
        </div>

        <div class="data-points">
          <div class="data-point">
            <span>文件大小</span>
            <strong>{{ material.fileSizeFormatted }}</strong>
          </div>
          <div class="data-point">
            <span>下载次数</span>
            <strong>{{ material.downloadCount }}</strong>
          </div>
          <div class="data-point">
            <span>上传时间</span>
            <strong>{{ formatDate(material.createdAt) }}</strong>
          </div>
          <div class="data-point">
            <span>互动情况</span>
            <strong>赞 {{ material.likeCount }} / 藏 {{ material.favoriteCount }}</strong>
          </div>
        </div>

        <p v-if="material.description" class="material-description">{{ material.description }}</p>
        <p v-if="material.versionNote" class="material-version-note">版本说明：{{ material.versionNote }}</p>

        <div v-if="material.tags?.length" class="tag-row">
          <span v-for="tag in material.tags" :key="tag" class="tag-chip">#{{ tag }}</span>
        </div>

        <div class="entity-card-actions">
          <router-link :to="`/material/${material.id}`" class="edu-btn edu-btn-secondary">详情</router-link>
          <a :href="material.fileUrl" target="_blank" rel="noreferrer" class="edu-btn edu-btn-primary">下载</a>
          <button type="button" class="edu-btn edu-btn-danger" @click="deleteMaterial(material.id)">删除</button>
        </div>
      </article>
    </section>

    <section v-if="error" class="message-banner error">
      {{ error }}
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useUserStore } from '../stores/user'
import { courseApi } from '../api/course'
import { materialApi } from '../api/material'

const userStore = useUserStore()

const courses = ref([])
const materials = ref([])
const selectedCourseId = ref('')
const selectedCategory = ref('')
const searchKeyword = ref('')
const loading = ref(false)
const error = ref('')

const materialCategories = [
  { value: 'LECTURE_NOTE', label: '讲义课件' },
  { value: 'ASSIGNMENT', label: '作业资料' },
  { value: 'REFERENCE', label: '参考资料' },
  { value: 'EXAM', label: '考试复习' },
  { value: 'LAB', label: '实验资料' },
  { value: 'OTHER', label: '其他' }
]

const filteredMaterials = computed(() => {
  const keyword = searchKeyword.value.trim().toLowerCase()
  return materials.value.filter((material) => {
    const courseMatch = !selectedCourseId.value || String(material.courseId || '') === selectedCourseId.value
    const categoryMatch = !selectedCategory.value || (material.category || '') === selectedCategory.value
    const searchPool = [
      material.name,
      material.description,
      material.courseName,
      material.versionLabel,
      material.versionNote,
      ...(material.tags || [])
    ]
      .filter(Boolean)
      .join(' ')
      .toLowerCase()

    return courseMatch && categoryMatch && (!keyword || searchPool.includes(keyword))
  })
})

const loadCourses = async () => {
  try {
    const response = await courseApi.getUserCourses(userStore.user.id)
    if (response.data.code === 200) {
      courses.value = response.data.data
    }
  } catch (err) {
    console.error('获取课程列表失败:', err)
  }
}

const loadMaterials = async () => {
  loading.value = true
  error.value = ''

  try {
    const response = await materialApi.getAllMaterials(userStore.user.id)
    if (response.data.code === 200) {
      materials.value = response.data.data.filter((item) => item.userId === userStore.user.id)
    } else {
      error.value = response.data.message
    }
  } catch (err) {
    error.value = '获取资料列表失败，请检查网络连接'
  } finally {
    loading.value = false
  }
}

const deleteMaterial = async (materialId) => {
  if (!confirm('确定要删除这份资料吗？此操作不可撤销。')) return

  try {
    const response = await materialApi.deleteMaterial(materialId, userStore.user.id)
    if (response.data.code === 200) {
      await loadMaterials()
    } else {
      error.value = response.data.message
    }
  } catch (err) {
    error.value = '删除资料失败'
  }
}

const getCategoryLabel = (category) => {
  const matched = materialCategories.find((item) => item.value === category)
  return matched ? matched.label : '未分类'
}

const getFileLabel = (fileType) => String(fileType || 'FILE').replace('.', '').toUpperCase()

const formatDate = (dateString) => {
  if (!dateString) return '未记录'
  return new Date(dateString).toLocaleString('zh-CN')
}

onMounted(() => {
  loadCourses()
  loadMaterials()
})
</script>

<style scoped>
.filters-wrap {
  flex-wrap: wrap;
}

.search-input {
  min-width: 260px;
  flex: 1;
}

.material-meta,
.tag-row {
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
