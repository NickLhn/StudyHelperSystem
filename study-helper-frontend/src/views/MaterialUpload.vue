<template>
  <div class="page-stack">
    <section class="page-intro form-shell">
      <div class="page-intro-copy">
        <span class="page-eyebrow">Upload Resource</span>
        <h2 class="page-title">上传学习资料</h2>
        <p class="page-subtitle">把文件、课程关联和资料说明整合到一张更清楚的上传工作表里。</p>
      </div>
    </section>

    <main class="form-shell">
      <section class="info-card form-panel">
        <form class="form-grid" @submit.prevent="handleSubmit">
          <div class="field">
            <label>资料名称</label>
            <input v-model="form.name" type="text" placeholder="如：高数第三章课件" required />
          </div>

          <div class="field">
            <label>关联课程</label>
            <select v-model="form.courseId">
              <option value="">不关联课程</option>
              <option v-for="course in courses" :key="course.id" :value="course.id">
                {{ course.name }}
              </option>
            </select>
          </div>

          <div class="field">
            <label>资料描述</label>
            <textarea v-model="form.description" rows="4" placeholder="补充资料用途、章节或适用对象"></textarea>
          </div>

          <div class="field">
            <label>资料分类</label>
            <select v-model="form.category">
              <option value="">请选择分类</option>
              <option v-for="item in materialCategories" :key="item.value" :value="item.value">
                {{ item.label }}
              </option>
            </select>
          </div>

          <div class="field">
            <label>标签</label>
            <input v-model="form.tags" type="text" placeholder="例如：期末复习, 第三章, 重点难点" />
            <small class="field-hint">多个标签用逗号分隔，最多保留 8 个。</small>
          </div>

          <div class="field two-up">
            <div>
              <label>版本号</label>
              <input v-model="form.versionLabel" type="text" placeholder="例如：v1.0 / 2026春修订版" />
            </div>
            <div>
              <label>版本说明</label>
              <input v-model="form.versionNote" type="text" placeholder="例如：补充了习题答案与重点标注" />
            </div>
          </div>

          <div class="field">
            <label>选择文件</label>
            <div class="upload-dropzone" @dragover.prevent @drop.prevent="handleDrop">
              <input
                type="file"
                ref="fileInput"
                @change="handleFileSelect"
                accept=".pdf,.docx,.pptx,.jpg,.jpeg,.png"
                class="hidden-input"
              />

              <div v-if="!selectedFile" class="upload-prompt" @click="fileInput.click()">
                <strong>点击选择文件或直接拖拽到这里</strong>
                <span>支持 PDF、DOCX、PPTX、JPG、PNG，大小不超过 30MB。</span>
              </div>

              <div v-else class="upload-file-card">
                <div>
                  <strong>{{ selectedFile.name }}</strong>
                  <span>{{ formatFileSize(selectedFile.size) }}</span>
                </div>
                <button type="button" class="edu-btn edu-btn-danger" @click="removeFile">移除</button>
              </div>
            </div>
          </div>

          <div v-if="error" class="message-banner error">{{ error }}</div>
          <div v-if="success" class="message-banner success">{{ success }}</div>

          <div class="toolbar-row">
            <button type="button" class="edu-btn edu-btn-secondary" @click="goBack">取消</button>
            <button type="submit" class="edu-btn edu-btn-primary" :disabled="loading || !selectedFile">
              {{ loading ? '上传中...' : '上传资料' }}
            </button>
          </div>
        </form>
      </section>
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
  description: '',
  category: '',
  tags: '',
  versionLabel: '',
  versionNote: ''
})

const courses = ref([])
const selectedFile = ref(null)
const fileInput = ref(null)
const error = ref('')
const success = ref('')
const loading = ref(false)
const materialCategories = [
  { value: 'LECTURE_NOTE', label: '讲义课件' },
  { value: 'ASSIGNMENT', label: '作业资料' },
  { value: 'REFERENCE', label: '参考资料' },
  { value: 'EXAM', label: '考试复习' },
  { value: 'LAB', label: '实验资料' },
  { value: 'OTHER', label: '其他' }
]

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

  const allowedTypes = [
    'application/pdf',
    'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
    'application/vnd.openxmlformats-officedocument.presentationml.presentation',
    'image/jpeg',
    'image/png'
  ]
  const maxSize = 30 * 1024 * 1024

  if (!allowedTypes.includes(file.type)) {
    error.value = '不支持的文件类型，请上传 PDF、DOCX、PPTX、JPG 或 PNG 文件'
    return
  }

  if (file.size > maxSize) {
    error.value = '文件大小不能超过 30MB'
    return
  }

  error.value = ''
  selectedFile.value = file
  if (!form.name) form.name = file.name.replace(/\.[^/.]+$/, '')
}

const removeFile = () => {
  selectedFile.value = null
  if (form.name && !form.description) form.name = ''
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
    formData.append('category', form.category || '')
    formData.append('tags', form.tags || '')
    formData.append('versionLabel', form.versionLabel || '')
    formData.append('versionNote', form.versionNote || '')
    if (form.courseId) formData.append('courseId', form.courseId)
    formData.append('file', selectedFile.value)

    const response = await materialApi.uploadMaterial(formData)
    if (response.data.code === 200) {
      success.value = '资料上传成功！'
      setTimeout(() => {
        router.push(route.path.startsWith('/teacher/') ? '/teacher/materials' : '/materials')
      }, 900)
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
  router.push(route.path.startsWith('/teacher/') ? '/teacher/materials' : '/materials')
}

const formatFileSize = (bytes) => {
  if (bytes < 1024) return `${bytes} B`
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`
  return `${(bytes / (1024 * 1024)).toFixed(1)} MB`
}

onMounted(() => {
  fetchCourses()
  if (typeof route.query.courseId === 'string' && route.query.courseId) {
    form.courseId = route.query.courseId
  }
})
</script>

<style scoped>
.hidden-input {
  display: none;
}

.upload-dropzone {
  border: 1px dashed rgba(23, 32, 51, 0.18);
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.82);
  padding: 18px;
}

.upload-prompt,
.upload-file-card {
  display: grid;
  gap: 6px;
}

.upload-prompt {
  cursor: pointer;
}

.upload-prompt span,
.upload-file-card span {
  color: var(--gray-500);
  font-size: 13px;
}

.field-hint {
  color: var(--gray-500);
  font-size: 12px;
}

.two-up {
  display: grid;
  gap: 16px;
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.upload-file-card {
  grid-template-columns: 1fr auto;
  align-items: center;
  gap: 16px;
}

@media (max-width: 720px) {
  .two-up {
    grid-template-columns: 1fr;
  }
}
</style>
