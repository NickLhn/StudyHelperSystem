<template>
  <div class="courses-page learning-environment">
    <main class="page-container">
      <!-- 页面标题 -->
      <div class="page-header">
        <h1 class="page-title">我的课程</h1>
        <router-link v-if="!isStudent" to="/course/create" class="edu-btn edu-btn-primary">
          <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <line x1="12" y1="5" x2="12" y2="19"></line>
            <line x1="5" y1="12" x2="19" y2="12"></line>
          </svg>
          添加课程
        </router-link>
      </div>

      <div v-if="isStudent" class="join-section edu-card">
        <div class="join-title">通过邀请码加入课程</div>
        <div class="join-row">
          <input v-model="joinCode" class="edu-input join-input" type="text" placeholder="请输入课程邀请码" />
          <button class="edu-btn edu-btn-primary" type="button" @click="handleJoin">加入</button>
        </div>
        <div v-if="joinError" class="join-error">{{ joinError }}</div>
      </div>

      <!-- 分类筛选 -->
      <div v-if="!isStudent" class="filter-section">
        <div class="filter-label">分类筛选：</div>
        <div class="filter-buttons">
          <button 
            v-for="cat in categories" 
            :key="cat.value"
            :class="['filter-btn edu-btn', { 'active': selectedCategory === cat.value }]"
            @click="filterByCategory(cat.value)"
          >
            {{ cat.label }}
          </button>
        </div>
      </div>

      <!-- 加载状态 -->
      <div v-if="loading" class="loading-container">
        <div class="loading-spinner"></div>
        <p>正在加载课程...</p>
      </div>
      
      <!-- 空状态 -->
      <div v-else-if="courses.length === 0" class="empty-state">
        <div class="empty-icon">📚</div>
        <h3>还没有添加任何课程</h3>
        <p>开始添加您的第一门课程吧！</p>
        <router-link v-if="!isStudent" to="/course/create" class="edu-btn edu-btn-primary">
          添加第一门课程
        </router-link>
      </div>

      <!-- 课程列表 -->
      <div v-else class="courses-grid">
        <div v-for="course in courses" :key="course.id" class="course-card edu-card">
          <div class="course-header">
            <h3 class="course-name">{{ course.name }}</h3>
            <span class="course-category" :class="getCategoryClass(course.categoryLabel)">
              {{ course.categoryLabel || getCategoryLabel(course.category) }}
            </span>
          </div>
          
          <div class="course-details">
            <div class="detail-item" v-if="course.teacher">
              <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
                <circle cx="12" cy="7" r="4"></circle>
              </svg>
              <span class="detail-label">教师：</span>
              <span class="detail-value">{{ course.teacher }}</span>
            </div>
            
            <div class="detail-item" v-if="course.schedule">
              <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <circle cx="12" cy="12" r="10"></circle>
                <polyline points="12 6 12 12 16 14"></polyline>
              </svg>
              <span class="detail-label">时间：</span>
              <span class="detail-value">{{ course.schedule }}</span>
            </div>
            
            <div class="detail-item" v-if="course.location">
              <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <circle cx="12" cy="12" r="10"></circle>
                <line x1="12" y1="16" x2="12" y2="12"></line>
                <line x1="12" y1="8" x2="12.01" y2="8"></line>
              </svg>
              <span class="detail-label">地点：</span>
              <span class="detail-value">{{ course.location }}</span>
            </div>
          </div>
          
          <div class="course-actions">
            <router-link v-if="isStudent" :to="`/student/course/${course.id}`" class="edu-btn edu-btn-primary">
              进入课程
            </router-link>
            <router-link v-if="!isStudent" :to="`/course/edit/${course.id}`" class="edu-btn edu-btn-secondary">
              编辑
            </router-link>
            <button v-if="!isStudent" @click="deleteCourse(course.id)" class="edu-btn edu-btn-danger">
              删除
            </button>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { computed, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { courseApi } from '../api/course'

const router = useRouter()
const userStore = useUserStore()

const courses = ref([])
const loading = ref(false)
const selectedCategory = ref('ALL')
const joinCode = ref('')
const joinError = ref('')

const isStudent = computed(() => userStore.isStudent)

const categories = [
  { value: 'ALL', label: '全部' },
  { value: 'REQUIRED', label: '必修' },
  { value: 'ELECTIVE', label: '选修' },
  { value: 'PUBLIC', label: '公共课' },
  { value: 'OTHER', label: '其他' }
]

// 获取分类标签
const getCategoryLabel = (type) => {
  const mapping = {
    REQUIRED: '必修',
    ELECTIVE: '选修',
    PUBLIC: '公共课',
    OTHER: '其他'
  }
  if (mapping[type]) return mapping[type]
  const category = categories.find(cat => cat.value === type)
  return category ? category.label : (type || '')
}

// 获取分类样式类
const getCategoryClass = (type) => {
  const label = ['必修', '选修', '公共课', '其他'].includes(type) ? type : getCategoryLabel(type)
  const classMap = {
    '必修': 'required',
    '选修': 'elective',
    '公共课': 'public',
    '其他': 'other'
  }
  return classMap[label] || 'other'
}

const fetchCourses = async () => {
  if (!userStore.user) return
  
  loading.value = true
  try {
    let response
    if (isStudent.value) {
      response = await courseApi.getStudentCourses(userStore.user.id)
    } else if (selectedCategory.value === 'ALL') {
      response = await courseApi.getUserCourses(userStore.user.id)
    } else {
      response = await courseApi.getUserCoursesByCategory(userStore.user.id, selectedCategory.value)
    }
    if (response.data.code === 200) {
      courses.value = response.data.data
    }
  } catch (error) {
    console.error('获取课程失败:', error)
  } finally {
    loading.value = false
  }
}

const handleJoin = async () => {
  joinError.value = ''
  const code = joinCode.value.trim().toUpperCase()
  if (!code) return
  try {
    const response = await courseApi.joinCourse(userStore.user.id, code)
    if (response.data.code === 200) {
      joinCode.value = ''
      await fetchCourses()
    } else {
      joinError.value = response.data.message || '加入失败'
    }
  } catch (e) {
    joinError.value = '加入失败，请检查网络连接'
  }
}

const filterByCategory = (category) => {
  selectedCategory.value = category
  fetchCourses()
}

const deleteCourse = async (courseId) => {
  if (!confirm('确定要删除这门课程吗？')) return
  
  try {
    const response = await courseApi.deleteCourse(courseId, userStore.user.id)
    if (response.data.code === 200) {
      courses.value = courses.value.filter(c => c.id !== courseId)
    } else {
      alert(response.data.message)
    }
  } catch (error) {
    alert('删除失败')
  }
}

onMounted(() => {
  fetchCourses()
})
</script>

<style scoped>
.courses-page {
  min-height: 100vh;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--spacing-xl);
  flex-wrap: wrap;
  gap: var(--spacing-md);
}

.page-header .page-title {
  margin: 0;
}

.join-section {
  padding: var(--spacing-lg);
  margin-bottom: var(--spacing-xl);
  border: 1px solid var(--gray-200);
}

.join-title {
  font-weight: var(--font-weight-bold);
  color: var(--gray-900);
  margin-bottom: var(--spacing-md);
}

.join-row {
  display: flex;
  gap: var(--spacing-md);
  align-items: center;
  flex-wrap: wrap;
}

.join-input {
  flex: 1;
  min-width: 220px;
}

.join-error {
  margin-top: var(--spacing-sm);
  color: var(--danger-color);
  font-weight: var(--font-weight-medium);
}

/* 分类筛选 */
.filter-section {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
  margin-bottom: var(--spacing-xl);
  flex-wrap: wrap;
}

.filter-label {
  font-weight: var(--font-weight-medium);
  color: var(--gray-700);
  white-space: nowrap;
}

.filter-buttons {
  display: flex;
  gap: var(--spacing-xs);
  flex-wrap: wrap;
}

.filter-btn {
  padding: var(--spacing-sm) var(--spacing-md);
  border: 1px solid var(--gray-300);
  background: var(--white);
  color: var(--gray-700);
  border-radius: var(--border-radius-full);
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-medium);
  transition: all 0.2s ease;
}

.filter-btn:hover,
.filter-btn.active {
  background: var(--primary-color);
  color: var(--white);
  border-color: var(--primary-color);
  transform: translateY(-1px);
}

/* 加载状态 */
.loading-container {
  text-align: center;
  padding: var(--spacing-xxl);
  color: var(--gray-700);
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 3px solid var(--gray-300);
  border-top: 3px solid var(--primary-color);
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto var(--spacing-md);
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: var(--spacing-xxl);
  background: var(--bg-secondary);
  border-radius: var(--border-radius-lg);
  border: 1px dashed var(--gray-300);
}

.empty-icon {
  font-size: 64px;
  margin-bottom: var(--spacing-md);
  opacity: 0.6;
}

.empty-state h3 {
  color: var(--gray-900);
  margin-bottom: var(--spacing-sm);
}

.empty-state p {
  color: var(--gray-700);
  margin-bottom: var(--spacing-lg);
}

/* 课程网格 */
.courses-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: var(--spacing-lg);
}

.course-card {
  display: flex;
  flex-direction: column;
  height: 100%;
  transition: all 0.3s ease;
  border: 1px solid var(--gray-200);
}

.course-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-lg);
  border-color: var(--primary-color);
}

.course-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: var(--spacing-md);
  gap: var(--spacing-sm);
}

.course-name {
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-semibold);
  color: var(--gray-900);
  margin: 0;
  flex: 1;
}

.course-category {
  padding: var(--spacing-xs) var(--spacing-sm);
  border-radius: var(--border-radius-full);
  font-size: var(--font-size-xs);
  font-weight: var(--font-weight-medium);
  white-space: nowrap;
}

.course-category.required {
  background: rgba(33, 150, 243, 0.1);
  color: var(--edu-blue);
}

.course-category.elective {
  background: rgba(156, 39, 176, 0.1);
  color: var(--edu-purple);
}

.course-category.public {
  background: rgba(76, 175, 80, 0.1);
  color: var(--edu-green);
}

.course-category.other {
  background: rgba(255, 193, 7, 0.1);
  color: var(--edu-yellow);
}

/* 课程详情 */
.course-details {
  flex: 1;
  margin-bottom: var(--spacing-md);
}

.detail-item {
  display: flex;
  align-items: flex-start;
  gap: var(--spacing-xs);
  margin-bottom: var(--spacing-sm);
  color: var(--gray-700);
  font-size: var(--font-size-sm);
}

.detail-item svg {
  flex-shrink: 0;
  margin-top: 2px;
  color: var(--gray-500);
}

.detail-label {
  font-weight: var(--font-weight-medium);
  color: var(--gray-700);
}

.detail-value {
  flex: 1;
  color: var(--gray-900);
}

/* 操作按钮 */
.course-actions {
  display: flex;
  gap: var(--spacing-sm);
  padding-top: var(--spacing-md);
  border-top: 1px solid var(--gray-200);
}

.edu-btn-danger {
  background: linear-gradient(135deg, var(--danger-color) 0%, #ff5252 100%);
  color: var(--white);
}

.edu-btn-danger:hover {
  background: linear-gradient(135deg, #ff5252 0%, #ff4444 100%);
  transform: translateY(-2px);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .courses-grid {
    grid-template-columns: 1fr;
  }
  
  .page-header {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .filter-section {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .course-header {
    flex-direction: column;
    align-items: flex-start;
    gap: var(--spacing-xs);
  }
  
  .course-category {
    align-self: flex-start;
  }
}

@media (max-width: 480px) {
  .courses-grid {
    gap: var(--spacing-md);
  }
  
  .course-card {
    padding: var(--spacing-md);
  }
  
  .detail-item {
    flex-direction: column;
    gap: var(--spacing-xs);
  }
  
  .course-actions {
    flex-direction: column;
  }
  
  .edu-btn {
    width: 100%;
    justify-content: center;
  }
}
</style>
