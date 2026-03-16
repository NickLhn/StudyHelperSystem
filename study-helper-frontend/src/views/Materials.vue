<template>
  <div class="materials-page learning-environment">
    <main class="page-container">
      <div class="page-header">
        <h1 class="page-title page-title-left">资料中心</h1>
        <router-link to="/material/upload" class="edu-btn edu-btn-primary">上传资料</router-link>
      </div>

      <div class="filters">
        <div class="filter-group">
          <label>课程筛选：</label>
          <select class="edu-input select" v-model="selectedCourse" @change="filterMaterials">
            <option value="">全部课程</option>
            <option v-for="course in courses" :key="course.id" :value="course.id">
              {{ course.name }}
            </option>
          </select>
        </div>
        <div class="search-box">
          <input 
            type="text" 
            v-model="searchKeyword" 
            class="edu-input search-input"
            placeholder="搜索资料（名称 / 文件名）…"
            @input="debouncedSearch"
          />
        </div>
      </div>

      <div v-if="loading" class="loading">加载中...</div>
      
      <div v-else-if="filteredMaterials.length === 0" class="empty-state">
        <p>暂无资料</p>
        <router-link to="/material/upload" class="edu-btn edu-btn-primary">上传第一份资料</router-link>
      </div>

      <div v-else class="materials-grid">
        <div 
          v-for="material in filteredMaterials" 
          :key="material.id" 
          class="material-card edu-card"
          @click="viewMaterial(material.id)"
        >
          <div class="file-icon">
            <span class="icon">{{ getFileIcon(material.fileType) }}</span>
          </div>
          
          <div class="material-info">
            <h3 class="material-name">{{ material.name }}</h3>
            <p class="file-name">{{ material.fileName }}</p>
            <p class="file-meta">
              <span>{{ material.fileSizeFormatted }}</span>
              <span v-if="material.courseName">📚 {{ material.courseName }}</span>
            </p>
            <p v-if="material.description" class="description">
              {{ material.description }}
            </p>
          </div>

          <div class="material-stats">
            <div class="stat-item">
              <span class="stat-icon">📥</span>
              <span>{{ material.downloadCount }}</span>
            </div>
            <div class="stat-item like" :class="{ active: material.likedByCurrentUser }" @click.stop="toggleLike(material)">
              <span class="stat-icon">👍</span>
              <span>{{ material.likeCount }}</span>
            </div>
            <div class="stat-item favorite" :class="{ active: material.favoritedByCurrentUser }" @click.stop="toggleFavorite(material)">
              <span class="stat-icon">⭐</span>
              <span>{{ material.favoriteCount }}</span>
            </div>
            <div class="stat-item">
              <span class="stat-icon">💬</span>
              <span>{{ material.commentCount }}</span>
            </div>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '../stores/user'
import { materialApi } from '../api/material'
import { courseApi } from '../api/course'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const materials = ref([])
const filteredMaterials = ref([])
const courses = ref([])
const loading = ref(false)
const selectedCourse = ref('')
const searchKeyword = ref('')

const applyKeywordFromRoute = () => {
  const keyword = typeof route.query.keyword === 'string' ? route.query.keyword : ''
  if (!keyword) return
  searchKeyword.value = keyword
  debouncedSearch()
}

const fetchMaterials = async () => {
  if (!userStore.user) return
  
  loading.value = true
  try {
    const response = await materialApi.getAllMaterials(userStore.user.id)
    if (response.data.code === 200) {
      materials.value = response.data.data
      filteredMaterials.value = [...materials.value]
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
    const response = await courseApi.getUserCourses(userStore.user.id)
    if (response.data.code === 200) {
      courses.value = response.data.data
    }
  } catch (error) {
    console.error('获取课程失败:', error)
  }
}

const filterMaterials = () => {
  if (!selectedCourse.value) {
    filteredMaterials.value = [...materials.value]
    return
  }
  
  filteredMaterials.value = materials.value.filter(m => 
    m.courseId === parseInt(selectedCourse.value)
  )
}

const debouncedSearch = () => {
  clearTimeout(window.searchTimer)
  window.searchTimer = setTimeout(() => {
    if (!searchKeyword.value.trim()) {
      filterMaterials()
      return
    }
    
    const keyword = searchKeyword.value.toLowerCase()
    filteredMaterials.value = materials.value.filter(m => 
      m.name.toLowerCase().includes(keyword) || 
      m.fileName.toLowerCase().includes(keyword)
    )
  }, 300)
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

const getFileIcon = (fileType) => {
  const icons = {
    '.pdf': '📄',
    '.docx': '📝',
    '.pptx': '📊',
    '.jpg': '🖼️',
    '.jpeg': '🖼️',
    '.png': '🖼️'
  }
  return icons[fileType] || '📁'
}

const handleLogout = () => {
  userStore.logout()
  router.push('/login')
}

onMounted(() => {
  fetchMaterials()
  fetchCourses()
  applyKeywordFromRoute()
})
</script>

<style scoped>
.materials-page {
  min-height: 100vh;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  gap: var(--spacing-md);
  flex-wrap: wrap;
  margin-bottom: var(--spacing-lg);
}

.page-title-left {
  text-align: left;
  margin: 0;
}

.filters {
  display: flex;
  gap: 1rem;
  margin-bottom: 1.5rem;
  flex-wrap: wrap;
}

.filter-group {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.select {
  max-width: 260px;
}

.search-input {
  width: min(52vw, 320px);
}

.loading,
.empty-state {
  text-align: center;
  padding: 3rem;
  color: var(--gray-700);
}

.materials-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 1.5rem;
}

.material-card {
  cursor: pointer;
}

.material-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-lg);
}

.file-icon {
  text-align: center;
  margin-bottom: 1rem;
}

.icon {
  font-size: 3rem;
}

.material-info {
  margin-bottom: 1rem;
}

.material-name {
  color: var(--gray-900);
  margin: 0 0 0.5rem 0;
  font-size: 1.1rem;
}

.file-name {
  color: var(--gray-700);
  font-size: 0.9rem;
  margin: 0.25rem 0;
}

.file-meta {
  display: flex;
  gap: 1rem;
  font-size: 0.8rem;
  color: var(--gray-700);
  margin: 0.5rem 0;
}

.description {
  color: var(--gray-700);
  font-size: 0.9rem;
  margin: 0.75rem 0 0 0;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.material-stats {
  display: flex;
  justify-content: space-around;
  padding-top: 1rem;
  border-top: 1px solid var(--gray-200);
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.25rem;
  cursor: pointer;
  padding: 0.5rem;
  border-radius: 6px;
  transition: background-color 0.2s;
}

.stat-item:hover {
  background-color: rgba(22, 35, 58, 0.06);
}

.stat-item.like.active {
  color: var(--danger-color);
}

.stat-item.favorite.active {
  color: var(--warning-color);
}

.stat-icon {
  font-size: 1.2rem;
}

@media (max-width: 768px) {
  .materials-grid {
    grid-template-columns: 1fr;
  }
  
  .filters {
    flex-direction: column;
  }
  
  .search-input {
    width: 100%;
  }
}
</style>
