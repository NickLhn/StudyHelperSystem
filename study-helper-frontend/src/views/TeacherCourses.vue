<template>
  <div class="teacher-courses-container">
    <h1 class="page-title">📚 我的课程</h1>
    
    <!-- 课程操作 -->
    <div class="course-actions">
      <router-link to="/teacher/course/create" class="btn-create-course">
        <i class="icon">➕</i> 创建课程
      </router-link>
    </div>
    
    <!-- 课程列表 -->
    <div class="courses-grid">
      <div v-for="course in courses" :key="course.id" class="course-card">
        <div class="course-header">
          <h3 class="course-title">{{ course.name }}</h3>
          <div class="course-actions">
            <button @click="editCourse(course.id)" class="btn-edit">
              <i class="icon">✏️</i> 编辑
            </button>
            <button @click="deleteCourse(course.id)" class="btn-delete">
              <i class="icon">🗑️</i> 删除
            </button>
          </div>
        </div>
        <div class="course-info">
          <p class="course-category"><strong>类别：</strong>{{ course.categoryLabel || course.category }}</p>
          <p class="course-teacher"><strong>教师：</strong>{{ course.teacher || '-' }}</p>
          <p class="course-students"><strong>学生人数：</strong>{{ course.studentCount || 0 }}</p>
          <p class="course-materials"><strong>资料数：</strong>{{ course.materialCount || 0 }}</p>
          <p class="course-tasks"><strong>任务数：</strong>{{ course.taskCount || 0 }}</p>
          <p class="course-code"><strong>邀请码：</strong>{{ course.invitationCode || '-' }}</p>
        </div>
        <div class="course-footer">
          <router-link :to="`/teacher/course/${course.id}/overview`" class="btn-overview">
            <i class="icon">📊</i> 课程管理
          </router-link>
        </div>
      </div>
    </div>
    
    <!-- 空状态 -->
    <div v-if="courses.length === 0 && !loading" class="empty-state">
      <div class="empty-icon">📚</div>
      <h3>还没有创建课程</h3>
      <p>点击上方的"创建课程"按钮开始创建您的第一门课程</p>
    </div>
    
    <!-- 加载状态 -->
    <div v-if="loading" class="loading">
      <div class="spinner"></div>
      <p>加载课程列表中...</p>
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
import { useRouter } from 'vue-router'
import { courseApi } from '../api/course'
import { useUserStore } from '../stores/user'

const router = useRouter()
const userStore = useUserStore()
const courses = ref([])
const loading = ref(false)
const error = ref('')

const loadCourses = async () => {
  loading.value = true
  error.value = ''
  
  try {
    if (!userStore.user?.id) {
      error.value = '用户未登录'
      return
    }
    
    const response = await courseApi.getUserCourses(userStore.user.id)
    if (response.data.code === 200) {
      courses.value = response.data.data
    } else {
      error.value = response.data.message
    }
  } catch (err) {
    error.value = '获取课程列表失败，请检查网络连接'
    console.error('获取课程列表失败:', err)
  } finally {
    loading.value = false
  }
}

const createCourse = () => {
  router.push('/teacher/course/create')
}

const editCourse = (courseId) => {
  router.push(`/teacher/course/edit/${courseId}`)
}

const deleteCourse = async (courseId) => {
  if (!confirm('确定要删除这门课程吗？此操作不可撤销。')) {
    return
  }
  
  try {
    if (!userStore.user?.id) {
      error.value = '用户未登录'
      return
    }
    
    const response = await courseApi.deleteCourse(courseId, userStore.user.id)
    if (response.data.code === 200) {
      loadCourses() // 重新加载课程列表
    } else {
      error.value = response.data.message
    }
  } catch (err) {
    error.value = '删除课程失败'
    console.error('删除课程失败:', err)
  }
}

onMounted(() => {
  loadCourses()
})
</script>

<style scoped>
.teacher-courses-container {
  padding: 2rem 0;
}

.page-title {
  color: #333;
  margin-bottom: 2rem;
  font-size: 2rem;
  text-align: center;
}

.course-actions {
  margin-bottom: 2rem;
  text-align: right;
}

.btn-create-course {
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

.btn-create-course:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(240, 147, 251, 0.3);
}

.courses-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 2rem;
}

.course-card {
  background: white;
  border-radius: 12px;
  padding: 1.5rem;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.08);
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.course-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
}

.course-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 1rem;
}

.course-title {
  margin: 0;
  font-size: 1.2rem;
  font-weight: 600;
  color: #333;
  flex: 1;
}

.course-actions {
  display: flex;
  gap: 0.5rem;
  text-align: right;
}

.btn-edit, .btn-delete {
  padding: 0.25rem 0.5rem;
  border: none;
  border-radius: 4px;
  font-size: 0.8rem;
  cursor: pointer;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  gap: 0.25rem;
}

.btn-edit {
  background: #e3f2fd;
  color: #1976d2;
}

.btn-edit:hover {
  background: #bbdefb;
}

.btn-delete {
  background: #ffebee;
  color: #c62828;
}

.btn-delete:hover {
  background: #ffcdd2;
}

.course-info {
  margin-bottom: 1.5rem;
}

.course-info p {
  margin: 0.5rem 0;
  font-size: 0.9rem;
  color: #666;
}

.course-footer {
  text-align: center;
}

.btn-overview {
  background: #f5f5f5;
  color: #333;
  padding: 0.5rem 1rem;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 0.9rem;
  cursor: pointer;
  transition: all 0.2s ease;
  text-decoration: none;
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
}

.btn-overview:hover {
  background: #e0e0e0;
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
  .teacher-courses-container {
    padding: 1rem 0;
  }
  
  .page-title {
    font-size: 1.5rem;
  }
  
  .courses-grid {
    grid-template-columns: 1fr;
    gap: 1rem;
  }
  
  .course-card {
    padding: 1rem;
  }
}
</style>
