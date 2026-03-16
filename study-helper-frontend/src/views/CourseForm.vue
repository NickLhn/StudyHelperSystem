<template>
  <div class="course-form-container">
    <main class="form-content">
      <div class="form-card">
        <h2>{{ isEdit ? '编辑课程' : '添加课程' }}</h2>
        
        <form @submit.prevent="handleSubmit">
          <div class="form-group">
            <label>课程名称 *</label>
            <input 
              v-model="form.name" 
              type="text" 
              placeholder="请输入课程名称（如：高等数学）"
              required
            />
          </div>

          <div class="form-group">
            <label>课程分类 *</label>
            <select v-model="form.category" required>
              <option v-for="cat in categories" :key="cat.value" :value="cat.value">
                {{ cat.label }}
              </option>
            </select>
          </div>

          <div class="form-group">
            <label>任课教师</label>
            <input 
              v-model="form.teacher" 
              type="text" 
              placeholder="请输入教师姓名"
            />
          </div>

          <div class="form-group">
            <label>上课时间</label>
            <input 
              v-model="form.schedule" 
              type="text" 
              placeholder="如：周一 8:00-9:40"
            />
          </div>

          <div class="form-group">
            <label>上课地点</label>
            <input 
              v-model="form.location" 
              type="text" 
              placeholder="如：教学楼 A-101"
            />
          </div>

          <div class="form-group">
            <label>备注</label>
            <textarea 
              v-model="form.remark" 
              rows="4"
              placeholder="请输入课程备注信息..."
            ></textarea>
          </div>

          <div v-if="error" class="error-message">{{ error }}</div>
          <div v-if="success" class="success-message">{{ success }}</div>

          <div class="form-actions">
            <button type="button" class="btn-cancel" @click="goBack">
              取消
            </button>
            <button type="submit" class="btn-submit" :disabled="loading">
              {{ loading ? '保存中...' : (isEdit ? '保存修改' : '添加课程') }}
            </button>
          </div>
        </form>
      </div>
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
  schedule: '',
  location: '',
  remark: ''
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
      const course = response.data.data
      Object.assign(form, course)
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
    let response
    if (isEdit.value) {
      response = await courseApi.updateCourse(courseId.value, userStore.user.id, form)
    } else {
      response = await courseApi.createCourse(userStore.user.id, form)
    }

    if (response.data.code === 200) {
      success.value = isEdit.value ? '课程更新成功！' : '课程添加成功！'
      setTimeout(() => {
        // 检查当前路由是否在教师端
        if (route.path.startsWith('/teacher/')) {
          router.push('/teacher/courses')
        } else {
          router.push('/courses')
        }
      }, 1500)
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
  // 检查当前路由是否在教师端
  if (route.path.startsWith('/teacher/')) {
    router.push('/teacher/courses')
  } else {
    router.push('/courses')
  }
}

const handleLogout = () => {
  userStore.logout()
  router.push('/login')
}

onMounted(() => {
  fetchCourse()
})
</script>

<style scoped>
.course-form-container {
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

.form-content {
  max-width: 600px;
  margin: 2rem auto;
  padding: 0 1rem;
}

.form-card {
  background: white;
  border-radius: 12px;
  padding: 2rem;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.form-card h2 {
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

@media (max-width: 600px) {
  .form-content {
    margin: 1rem auto;
  }
  
  .form-card {
    padding: 1.5rem;
  }
}
</style>
