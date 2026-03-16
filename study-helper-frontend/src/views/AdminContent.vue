<template>
  <div class="admin-content-container">
    <h1 class="page-title">📚 内容概览</h1>
    
    <div class="content-sections">
      <div class="section">
        <h2>📘 课程列表</h2>
        <div class="content-list">
          <div v-if="loading.courses" class="loading">
            <div class="spinner"></div>
            <p>加载课程列表中...</p>
          </div>
          <div v-else-if="error.courses" class="error-message">
            <i class="icon">⚠️</i>
            {{ error.courses }}
          </div>
          <table v-else class="content-table">
            <thead>
              <tr>
                <th>ID</th>
                <th>课程名称</th>
                <th>类别</th>
                <th>教师</th>
                <th>创建者</th>
                <th>创建时间</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="course in courses" :key="course.id">
                <td>{{ course.id }}</td>
                <td>{{ course.name }}</td>
                <td>{{ course.category }}</td>
                <td>{{ course.teacher || '-' }}</td>
                <td>{{ course.username }}</td>
                <td>{{ formatDate(course.createdAt) }}</td>
              </tr>
            </tbody>
          </table>
        </div>
        
        <!-- 课程分页 -->
        <div v-if="!loading.courses && !error.courses" class="pagination">
          <button 
            @click="prevPage('courses')" 
            :disabled="currentPage.courses <= 0"
            class="btn-pagination"
          >
            上一页
          </button>
          <span class="page-info">
            第 {{ currentPage.courses + 1 }} 页，共 {{ totalPages.courses }} 页
          </span>
          <button 
            @click="nextPage('courses')" 
            :disabled="currentPage.courses >= totalPages.courses - 1"
            class="btn-pagination"
          >
            下一页
          </button>
        </div>
      </div>
      
      <div class="section">
        <h2>📁 资料列表</h2>
        <div class="content-list">
          <div v-if="loading.materials" class="loading">
            <div class="spinner"></div>
            <p>加载资料列表中...</p>
          </div>
          <div v-else-if="error.materials" class="error-message">
            <i class="icon">⚠️</i>
            {{ error.materials }}
          </div>
          <table v-else class="content-table">
            <thead>
              <tr>
                <th>ID</th>
                <th>资料名称</th>
                <th>文件类型</th>
                <th>文件大小</th>
                <th>上传者</th>
                <th>课程</th>
                <th>下载次数</th>
                <th>创建时间</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="material in materials" :key="material.id">
                <td>{{ material.id }}</td>
                <td>{{ material.name }}</td>
                <td>{{ material.fileType }}</td>
                <td>{{ formatFileSize(material.fileSize) }}</td>
                <td>{{ material.username }}</td>
                <td>{{ material.courseName || '-' }}</td>
                <td>{{ material.downloadCount }}</td>
                <td>{{ formatDate(material.createdAt) }}</td>
              </tr>
            </tbody>
          </table>
        </div>
        
        <!-- 资料分页 -->
        <div v-if="!loading.materials && !error.materials" class="pagination">
          <button 
            @click="prevPage('materials')" 
            :disabled="currentPage.materials <= 0"
            class="btn-pagination"
          >
            上一页
          </button>
          <span class="page-info">
            第 {{ currentPage.materials + 1 }} 页，共 {{ totalPages.materials }} 页
          </span>
          <button 
            @click="nextPage('materials')" 
            :disabled="currentPage.materials >= totalPages.materials - 1"
            class="btn-pagination"
          >
            下一页
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { adminApi } from '../api/admin'

const courses = ref([])
const materials = ref([])
const loading = ref({ courses: false, materials: false })
const error = ref({ courses: '', materials: '' })
const currentPage = ref({ courses: 0, materials: 0 })
const pageSize = ref(10)
const totalPages = ref({ courses: 1, materials: 1 })

const loadCourses = async () => {
  loading.value.courses = true
  error.value.courses = ''
  
  try {
    const params = {
      page: currentPage.value.courses,
      size: pageSize.value
    }
    
    const response = await adminApi.getCourses(params)
    if (response.data.code === 200) {
      const data = response.data.data
      if (Array.isArray(data)) {
        courses.value = data
        totalPages.value.courses = Math.ceil(data.length / pageSize.value) || 1
      } else {
        courses.value = data.content
        totalPages.value.courses = data.totalPages
      }
    } else {
      error.value.courses = response.data.message
    }
  } catch (err) {
    error.value.courses = '获取课程列表失败，请检查网络连接'
    console.error('获取课程列表失败:', err)
  } finally {
    loading.value.courses = false
  }
}

const loadMaterials = async () => {
  loading.value.materials = true
  error.value.materials = ''
  
  try {
    const params = {
      page: currentPage.value.materials,
      size: pageSize.value
    }
    
    const response = await adminApi.getMaterials(params)
    if (response.data.code === 200) {
      const data = response.data.data
      if (Array.isArray(data)) {
        materials.value = data
        totalPages.value.materials = Math.ceil(data.length / pageSize.value) || 1
      } else {
        materials.value = data.content
        totalPages.value.materials = data.totalPages
      }
    } else {
      error.value.materials = response.data.message
    }
  } catch (err) {
    error.value.materials = '获取资料列表失败，请检查网络连接'
    console.error('获取资料列表失败:', err)
  } finally {
    loading.value.materials = false
  }
}

const prevPage = (type) => {
  if (currentPage.value[type] > 0) {
    currentPage.value[type]--
    if (type === 'courses') {
      loadCourses()
    } else {
      loadMaterials()
    }
  }
}

const nextPage = (type) => {
  if (currentPage.value[type] < totalPages.value[type] - 1) {
    currentPage.value[type]++
    if (type === 'courses') {
      loadCourses()
    } else {
      loadMaterials()
    }
  }
}

const formatDate = (dateString) => {
  if (!dateString) return '-'
  return new Date(dateString).toLocaleDateString('zh-CN')
}

const formatFileSize = (size) => {
  if (!size) return '0 B'
  const units = ['B', 'KB', 'MB', 'GB']
  let i = 0
  let currentSize = size
  while (currentSize >= 1024 && i < units.length - 1) {
    currentSize /= 1024
    i++
  }
  return `${currentSize.toFixed(2)} ${units[i]}`
}

onMounted(() => {
  loadCourses()
  loadMaterials()
})
</script>

<style scoped>
.admin-content-container {
  padding: 2rem 0;
}

.page-title {
  color: #333;
  margin-bottom: 2rem;
  font-size: 2rem;
  text-align: center;
}

.content-sections {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 2rem;
}

.section {
  background: white;
  padding: 2rem;
  border-radius: 12px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.08);
}

.section h2 {
  color: #333;
  margin-bottom: 1rem;
  font-size: 1.5rem;
}

.content-list {
  margin-bottom: 1.5rem;
  overflow-x: auto;
}

.content-table {
  width: 100%;
  border-collapse: collapse;
}

.content-table th {
  background: #f8f9fa;
  padding: 0.75rem;
  text-align: left;
  font-weight: 600;
  color: #555;
  border-bottom: 2px solid #e9ecef;
}

.content-table td {
  padding: 0.75rem;
  border-bottom: 1px solid #e9ecef;
}

.content-table tr:hover {
  background: #f8f9fa;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 1rem;
  padding-top: 1rem;
  border-top: 1px solid #e9ecef;
}

.btn-pagination {
  padding: 0.5rem 1rem;
  border: 1px solid #ddd;
  background: white;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.btn-pagination:hover:not(:disabled) {
  background: #667eea;
  color: white;
  border-color: #667eea;
}

.btn-pagination:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.page-info {
  color: #666;
  font-weight: 500;
}

.loading {
  text-align: center;
  padding: 3rem;
  color: #666;
}

.spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid #667eea;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 1rem;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.error-message {
  background: #ffebee;
  color: #c62828;
  padding: 1rem;
  border-radius: 8px;
  text-align: center;
  margin: 1rem 0;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .content-sections {
    grid-template-columns: 1fr;
  }
  
  .content-table {
    font-size: 0.9rem;
  }
  
  .content-table th,
  .content-table td {
    padding: 0.5rem;
  }
}
</style>