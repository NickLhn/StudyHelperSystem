<template>
  <div class="teacher-materials-container">
    <h1 class="page-title">📁 资料中心</h1>
    
    <!-- 资料操作 -->
    <div class="material-actions">
      <router-link to="/teacher/material/upload" class="btn-upload-material">
        <i class="icon">📤</i> 上传资料
      </router-link>
    </div>
    
    <!-- 课程选择 -->
    <div class="course-selector">
      <label for="course-select">选择课程：</label>
      <select id="course-select" v-model="selectedCourseId" @change="loadMaterials">
        <option value="">所有课程</option>
        <option v-for="course in courses" :key="course.id" :value="course.id">
          {{ course.name }}
        </option>
      </select>
    </div>
    
    <!-- 资料列表 -->
    <div class="materials-grid">
      <div v-for="material in materials" :key="material.id" class="material-card">
        <div class="material-header">
          <div class="material-icon" :class="getMaterialIconClass(material.fileType)">
            {{ getMaterialIcon(material.fileType) }}
          </div>
          <h3 class="material-title">{{ material.name }}</h3>
        </div>
        <div class="material-info">
          <p class="material-course"><strong>所属课程：</strong>{{ getCourseName(material.courseId) }}</p>
          <p class="material-uploader"><strong>上传者：</strong>{{ material.username }}</p>
          <p class="material-size"><strong>文件大小：</strong>{{ formatFileSize(material.fileSize) }}</p>
          <p class="material-downloads"><strong>下载次数：</strong>{{ material.downloadCount }}</p>
          <p class="material-date"><strong>上传时间：</strong>{{ formatDate(material.createdAt) }}</p>
        </div>
        <div class="material-actions">
          <a :href="material.fileUrl" target="_blank" class="btn-download">
            <i class="icon">⬇️</i> 下载
          </a>
          <button @click="deleteMaterial(material.id)" class="btn-delete">
            <i class="icon">🗑️</i> 删除
          </button>
        </div>
      </div>
    </div>
    
    <!-- 空状态 -->
    <div v-if="materials.length === 0 && !loading" class="empty-state">
      <div class="empty-icon">📁</div>
      <h3>还没有上传资料</h3>
      <p>点击上方的"上传资料"按钮开始上传您的第一份资料</p>
    </div>
    
    <!-- 加载状态 -->
    <div v-if="loading" class="loading">
      <div class="spinner"></div>
      <p>加载资料列表中...</p>
    </div>
    
    <!-- 错误提示 -->
    <div v-if="error" class="error-message">
      <i class="icon">⚠️</i>
      {{ error }}
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { adminApi } from '../api/admin'

const courses = ref([])
const materials = ref([])
const selectedCourseId = ref('')
const loading = ref(false)
const error = ref('')

const loadCourses = async () => {
  try {
    const response = await adminApi.getCourses()
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
    const params = {}
    if (selectedCourseId.value) {
      params.courseId = selectedCourseId.value
    }
    
    const response = await adminApi.getMaterials(params)
    if (response.data.code === 200) {
      materials.value = response.data.data
    } else {
      error.value = response.data.message
    }
  } catch (err) {
    error.value = '获取资料列表失败，请检查网络连接'
    console.error('获取资料列表失败:', err)
  } finally {
    loading.value = false
  }
}

const deleteMaterial = async (materialId) => {
  if (!confirm('确定要删除这份资料吗？此操作不可撤销。')) {
    return
  }
  
  try {
    const response = await adminApi.deleteMaterial(materialId)
    if (response.data.code === 200) {
      loadMaterials() // 重新加载资料列表
    } else {
      error.value = response.data.message
    }
  } catch (err) {
    error.value = '删除资料失败'
    console.error('删除资料失败:', err)
  }
}

const getCourseName = (courseId) => {
  const course = courses.value.find(c => c.id === courseId)
  return course ? course.name : '未知课程'
}

const getMaterialIcon = (fileType) => {
  const iconMap = {
    'pdf': '📄',
    'doc': '📃',
    'docx': '📃',
    'ppt': '📊',
    'pptx': '📊',
    'xls': '📈',
    'xlsx': '📈',
    'txt': '📝',
    'jpg': '🖼️',
    'jpeg': '🖼️',
    'png': '🖼️',
    'gif': '🖼️',
    'mp4': '🎬',
    'mp3': '🎵',
    'zip': '📦',
    'rar': '📦'
  }
  const extension = fileType.toLowerCase()
  return iconMap[extension] || '📄'
}

const getMaterialIconClass = (fileType) => {
  const classMap = {
    'pdf': 'pdf',
    'doc': 'doc',
    'docx': 'doc',
    'ppt': 'ppt',
    'pptx': 'ppt',
    'xls': 'xls',
    'xlsx': 'xls',
    'txt': 'txt',
    'jpg': 'img',
    'jpeg': 'img',
    'png': 'img',
    'gif': 'img',
    'mp4': 'video',
    'mp3': 'audio',
    'zip': 'zip',
    'rar': 'zip'
  }
  const extension = fileType.toLowerCase()
  return classMap[extension] || 'other'
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

const formatDate = (dateString) => {
  if (!dateString) return '-'
  return new Date(dateString).toLocaleString('zh-CN')
}

onMounted(() => {
  loadCourses()
  loadMaterials()
})
</script>

<style scoped>
.teacher-materials-container {
  padding: 2rem 0;
}

.page-title {
  color: #333;
  margin-bottom: 2rem;
  font-size: 2rem;
  text-align: center;
}

.material-actions {
  margin-bottom: 2rem;
  text-align: right;
}

.btn-upload-material {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  color: white;
  padding: 0.75rem 1.5rem;
  border: none;
  border-radius: 6px;
  font-size: 1rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  text-decoration: none;
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
}

.btn-upload-material:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(240, 147, 251, 0.3);
}

.course-selector {
  margin-bottom: 2rem;
  display: flex;
  align-items: center;
  gap: 1rem;
}

.course-selector label {
  font-weight: 500;
  color: #333;
}

.course-selector select {
  padding: 0.5rem;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 1rem;
  min-width: 200px;
}

.materials-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 2rem;
}

.material-card {
  background: white;
  border-radius: 12px;
  padding: 1.5rem;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.08);
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.material-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
}

.material-header {
  display: flex;
  align-items: center;
  margin-bottom: 1rem;
  gap: 1rem;
}

.material-icon {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 2rem;
  flex-shrink: 0;
}

.material-icon.pdf {
  background: #ff5252;
  color: white;
}

.material-icon.doc {
  background: #4285f4;
  color: white;
}

.material-icon.ppt {
  background: #fbbc05;
  color: white;
}

.material-icon.xls {
  background: #34a853;
  color: white;
}

.material-icon.txt {
  background: #9e9e9e;
  color: white;
}

.material-icon.img {
  background: #ff9800;
  color: white;
}

.material-icon.video {
  background: #9c27b0;
  color: white;
}

.material-icon.audio {
  background: #e91e63;
  color: white;
}

.material-icon.zip {
  background: #607d8b;
  color: white;
}

.material-icon.other {
  background: #795548;
  color: white;
}

.material-title {
  margin: 0;
  font-size: 1.1rem;
  font-weight: 600;
  color: #333;
  flex: 1;
}

.material-info {
  margin-bottom: 1.5rem;
}

.material-info p {
  margin: 0.5rem 0;
  font-size: 0.9rem;
  color: #666;
}

.material-actions {
  display: flex;
  gap: 1rem;
  justify-content: space-between;
}

.btn-download, .btn-delete {
  padding: 0.5rem 1rem;
  border: none;
  border-radius: 6px;
  font-size: 0.9rem;
  cursor: pointer;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  text-decoration: none;
  flex: 1;
  justify-content: center;
}

.btn-download {
  background: #e3f2fd;
  color: #1976d2;
}

.btn-download:hover {
  background: #bbdefb;
}

.btn-delete {
  background: #ffebee;
  color: #c62828;
}

.btn-delete:hover {
  background: #ffcdd2;
}

.empty-state {
  text-align: center;
  padding: 4rem 2rem;
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.08);
  margin-top: 2rem;
}

.empty-icon {
  font-size: 4rem;
  margin-bottom: 1rem;
}

.empty-state h3 {
  color: #333;
  margin-bottom: 0.5rem;
}

.empty-state p {
  color: #666;
  margin: 0;
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
  border-top: 4px solid #f093fb;
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
  margin: 2rem 0;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
}

.icon {
  font-size: 1rem;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .teacher-materials-container {
    padding: 1rem 0;
  }
  
  .page-title {
    font-size: 1.5rem;
  }
  
  .course-selector {
    flex-direction: column;
    align-items: stretch;
  }
  
  .materials-grid {
    grid-template-columns: 1fr;
    gap: 1rem;
  }
  
  .material-card {
    padding: 1rem;
  }
  
  .material-actions {
    flex-direction: column;
  }
}
</style>