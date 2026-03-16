<template>
  <div class="upload-container">
    <main class="upload-content">
      <div class="upload-card">
        <h2>上传学习资料</h2>
        
        <form @submit.prevent="handleSubmit">
          <div class="form-group">
            <label>资料名称 *</label>
            <input 
              v-model="form.name" 
              type="text" 
              placeholder="请输入资料名称（如：高数第三章课件）"
              required
            />
          </div>

          <div class="form-group">
            <label>关联课程</label>
            <select v-model="form.courseId">
              <option value="">不关联课程</option>
              <option v-for="course in courses" :key="course.id" :value="course.id">
                {{ course.name }}
              </option>
            </select>
          </div>

          <div class="form-group">
            <label>资料描述</label>
            <textarea 
              v-model="form.description" 
              rows="3"
              placeholder="请输入资料描述..."
            ></textarea>
          </div>

          <div class="form-group">
            <label>选择文件 *</label>
            <div class="file-upload-area" @dragover.prevent @drop.prevent="handleDrop">
              <input 
                type="file" 
                ref="fileInput"
                @change="handleFileSelect"
                accept=".pdf,.docx,.pptx,.jpg,.jpeg,.png"
                style="display: none"
              />
              <div v-if="!selectedFile" class="upload-prompt" @click="$refs.fileInput.click()">
                <div class="upload-icon">📁</div>
                <p>点击选择文件或将文件拖拽到这里</p>
                <p class="file-types">支持格式：PDF, DOCX, PPTX, JPG, PNG</p>
                <p class="file-limit">文件大小不超过 30MB</p>
              </div>
              <div v-else class="file-preview">
                <div class="file-info">
                  <span class="file-icon">{{ getFileIcon(selectedFile.type) }}</span>
                  <div>
                    <p class="file-name">{{ selectedFile.name }}</p>
                    <p class="file-size">{{ formatFileSize(selectedFile.size) }}</p>
                  </div>
                </div>
                <button type="button" class="btn-remove" @click="removeFile">×</button>
              </div>
            </div>
          </div>

          <div v-if="error" class="error-message">{{ error }}</div>
          <div v-if="success" class="success-message">{{ success }}</div>

          <div class="form-actions">
            <button type="button" class="btn-cancel" @click="goBack">
              取消
            </button>
            <button type="submit" class="btn-submit" :disabled="loading || !selectedFile">
              {{ loading ? '上传中...' : '上传资料' }}
            </button>
          </div>
        </form>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '../stores/user'
import { materialApi } from '../api/material'
import { courseApi } from '../api/course'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const form = reactive({
  name: '',
  courseId: '',
  description: ''
})

const courses = ref([])
const selectedFile = ref(null)
const error = ref('')
const success = ref('')
const loading = ref(false)

const fetchCourses = async () => {
  try {
    const response = await courseApi.getUserCourses(userStore.user.id)
    if (response.data.code === 200) {
      courses.value = response.data.data
    }
  } catch (err) {
    console.error('获取课程失败:', err)
  }
}

const handleFileSelect = (event) => {
  const file = event.target.files[0]
  validateAndSetFile(file)
}

const handleDrop = (event) => {
  const file = event.dataTransfer.files[0]
  validateAndSetFile(file)
}

const validateAndSetFile = (file) => {
  if (!file) return

  const allowedTypes = ['application/pdf', 'application/vnd.openxmlformats-officedocument.wordprocessingml.document', 
                       'application/vnd.openxmlformats-officedocument.presentationml.presentation', 
                       'image/jpeg', 'image/png']
  const maxSize = 30 * 1024 * 1024 // 30MB

  if (!allowedTypes.includes(file.type)) {
    error.value = '不支持的文件类型，请上传 PDF、DOCX、PPTX、JPG 或 PNG 格式文件'
    return
  }

  if (file.size > maxSize) {
    error.value = '文件大小不能超过 30MB'
    return
  }

  error.value = ''
  selectedFile.value = file

  // 自动填写资料名称（如果没有手动填写）
  if (!form.name) {
    form.name = file.name.replace(/\.[^/.]+$/, "") // 移除扩展名
  }
}

const removeFile = () => {
  selectedFile.value = null
  if (form.name && !form.description) {
    form.name = '' // 如果是自动生成的名称，则清空
  }
}

const handleSubmit = async () => {
  if (!selectedFile.value) {
    error.value = '请选择要上传的文件'
    return
  }

  error.value = ''
  success.value = ''
  loading.value = true

  try {
    const formData = new FormData()
    formData.append('userId', userStore.user.id)
    formData.append('name', form.name)
    formData.append('description', form.description || '')
    if (form.courseId) {
      formData.append('courseId', form.courseId)
    }
    formData.append('file', selectedFile.value)

    const response = await materialApi.uploadMaterial(formData)
    
    if (response.data.code === 200) {
      success.value = '资料上传成功！'
      setTimeout(() => {
        // 检查当前路由是否在教师端
        if (route.path.startsWith('/teacher/')) {
          router.push('/teacher/materials')
        } else {
          router.push('/materials')
        }
      }, 1500)
    } else {
      error.value = response.data.message
    }
  } catch (err) {
    error.value = '上传失败，请检查网络连接'
  } finally {
    loading.value = false
  }
}

const goBack = () => {
  // 检查当前路由是否在教师端
  if (route.path.startsWith('/teacher/')) {
    router.push('/teacher/materials')
  } else {
    router.push('/materials')
  }
}

const getFileIcon = (fileType) => {
  const icons = {
    'application/pdf': '📄',
    'application/vnd.openxmlformats-officedocument.wordprocessingml.document': '📝',
    'application/vnd.openxmlformats-officedocument.presentationml.presentation': '📊',
    'image/jpeg': '🖼️',
    'image/png': '🖼️'
  }
  return icons[fileType] || '📁'
}

const formatFileSize = (bytes) => {
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  return (bytes / (1024 * 1024)).toFixed(1) + ' MB'
}

const handleLogout = () => {
  userStore.logout()
  router.push('/login')
}

onMounted(() => {
  fetchCourses()
  if (typeof route.query.courseId === 'string' && route.query.courseId) {
    form.courseId = route.query.courseId
  }
})
</script>

<style scoped>
.upload-container {
  min-height: 100vh;
  background-color: #f5f5f5;
}

.navbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem 2rem;
  background-color: #42b883;
  color: white;
}

.nav-brand {
  font-size: 1.5rem;
  font-weight: bold;
}

.nav-links {
  display: flex;
  gap: 1rem;
  align-items: center;
}

.nav-link {
  color: white;
  text-decoration: none;
  padding: 0.5rem 1rem;
  border-radius: 4px;
  transition: background-color 0.3s;
}

.nav-link:hover {
  background-color: rgba(255, 255, 255, 0.2);
}

.btn-logout {
  background-color: transparent;
  border: 1px solid white;
  color: white;
  padding: 0.5rem 1rem;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
}

.btn-logout:hover {
  background-color: white;
  color: #42b883;
}

.upload-content {
  max-width: 600px;
  margin: 2rem auto;
  padding: 0 1rem;
}

.upload-card {
  background: white;
  border-radius: 12px;
  padding: 2rem;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.upload-card h2 {
  color: #333;
  margin-bottom: 1.5rem;
  text-align: center;
}

.form-group {
  margin-bottom: 1.25rem;
}

.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  color: #555;
  font-weight: 500;
}

.form-group input,
.form-group select,
.form-group textarea {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 1rem;
  transition: border-color 0.3s;
  font-family: inherit;
}

.form-group input:focus,
.form-group select:focus,
.form-group textarea:focus {
  outline: none;
  border-color: #667eea;
}

.form-group textarea {
  resize: vertical;
}

.file-upload-area {
  border: 2px dashed #ddd;
  border-radius: 8px;
  padding: 2rem;
  text-align: center;
  transition: border-color 0.3s;
  cursor: pointer;
}

.file-upload-area:hover {
  border-color: #667eea;
}

.upload-prompt .upload-icon {
  font-size: 3rem;
  margin-bottom: 1rem;
}

.upload-prompt p {
  margin: 0.5rem 0;
  color: #666;
}

.file-types {
  font-size: 0.9rem;
  color: #888;
}

.file-limit {
  font-size: 0.9rem;
  color: #e74c3c;
  font-weight: 500;
}

.file-preview {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem;
  background: #f8f9fa;
  border-radius: 6px;
}

.file-info {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.file-icon {
  font-size: 2rem;
}

.file-name {
  margin: 0;
  font-weight: 500;
  color: #333;
}

.file-size {
  margin: 0;
  font-size: 0.9rem;
  color: #666;
}

.btn-remove {
  background: #ff6b6b;
  color: white;
  border: none;
  width: 30px;
  height: 30px;
  border-radius: 50%;
  font-size: 1.2rem;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}

.error-message {
  color: #e74c3c;
  margin-bottom: 1rem;
  text-align: center;
  font-size: 0.9rem;
}

.success-message {
  color: #27ae60;
  margin-bottom: 1rem;
  text-align: center;
  font-size: 0.9rem;
}

.form-actions {
  display: flex;
  gap: 1rem;
  margin-top: 1.5rem;
}

.btn-cancel,
.btn-submit {
  flex: 1;
  padding: 0.875rem;
  border-radius: 6px;
  font-size: 1rem;
  cursor: pointer;
  transition: all 0.3s;
  border: none;
}

.btn-cancel {
  background: #f0f0f0;
  color: #666;
}

.btn-cancel:hover {
  background: #e0e0e0;
}

.btn-submit {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.btn-submit:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 5px 20px rgba(102, 126, 234, 0.4);
}

.btn-submit:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}
</style>
