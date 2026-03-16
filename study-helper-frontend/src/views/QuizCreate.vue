<template>
  <div class="quiz-create-container">
    <main class="create-content">
      <div class="create-card">
        <h2>创建测验</h2>
        
        <form @submit.prevent="handleSubmit">
          <div class="form-group">
            <label>测验标题 *</label>
            <input 
              v-model="form.title" 
              type="text" 
              placeholder="请输入测验标题"
              required
            />
          </div>

          <div class="form-group">
            <label>测验描述</label>
            <textarea 
              v-model="form.description" 
              rows="3"
              placeholder="请输入测验描述..."
            ></textarea>
          </div>

          <div class="form-row">
            <div class="form-group">
              <label>总时间（分钟）</label>
              <input 
                v-model.number="form.totalTime" 
                type="number" 
                min="0"
                placeholder="0表示不限时"
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
          </div>

          <div class="questions-section">
            <div class="section-header">
              <h3>题目设置</h3>
              <button type="button" @click="addQuestion" class="btn-add-question">
                + 添加题目
              </button>
            </div>

            <div v-if="form.questions.length === 0" class="no-questions">
              点击上方按钮添加题目
            </div>

            <div v-else class="questions-list">
              <div 
                v-for="(question, index) in form.questions" 
                :key="index" 
                class="question-item"
              >
                <div class="question-header">
                  <span>题目 {{ index + 1 }}</span>
                  <button type="button" @click="removeQuestion(index)" class="btn-remove">
                    ×
                  </button>
                </div>

                <div class="form-group">
                  <label>题目内容 *</label>
                  <textarea 
                    v-model="question.content" 
                    rows="2"
                    placeholder="请输入题目内容"
                    required
                  ></textarea>
                </div>

                <div class="form-row">
                  <div class="form-group">
                    <label>题目类型 *</label>
                    <select v-model="question.type" required>
                      <option value="SINGLE_CHOICE">单选题</option>
                      <option value="TRUE_FALSE">判断题</option>
                    </select>
                  </div>
                  <div class="form-group">
                    <label>分值 *</label>
                    <input 
                      v-model.number="question.score" 
                      type="number" 
                      min="1"
                      required
                    />
                  </div>
                </div>

                <!-- 单选题选项 -->
                <div v-if="question.type === 'SINGLE_CHOICE'" class="options-section">
                  <label>选项 *</label>
                  <div v-for="(option, optIndex) in 4" :key="optIndex" class="option-item">
                    <span class="option-letter">{{ String.fromCharCode(65 + optIndex) }}.</span>
                    <input 
                      v-model="question.options[optIndex]" 
                      type="text" 
                      :placeholder="`选项${String.fromCharCode(65 + optIndex)}`"
                      required
                    />
                  </div>
                  <div class="form-group">
                    <label>正确答案 *</label>
                    <select v-model="question.answer" required>
                      <option value="">请选择正确答案</option>
                      <option v-for="(option, optIndex) in 4" :key="optIndex" :value="String.fromCharCode(65 + optIndex)">
                        {{ String.fromCharCode(65 + optIndex) }}
                      </option>
                    </select>
                  </div>
                </div>

                <!-- 判断题 -->
                <div v-else-if="question.type === 'TRUE_FALSE'" class="true-false-section">
                  <div class="form-group">
                    <label>正确答案 *</label>
                    <select v-model="question.answer" required>
                      <option value="">请选择正确答案</option>
                      <option value="A">正确</option>
                      <option value="B">错误</option>
                    </select>
                  </div>
                </div>

                <div class="form-group">
                  <label>解析</label>
                  <textarea 
                    v-model="question.analysis" 
                    rows="2"
                    placeholder="请输入题目解析（可选）"
                  ></textarea>
                </div>
              </div>
            </div>
          </div>

          <div v-if="error" class="error-message">{{ error }}</div>
          <div v-if="success" class="success-message">{{ success }}</div>

          <div class="form-actions">
            <button type="button" class="btn-cancel" @click="goBack">
              取消
            </button>
            <button type="submit" class="btn-submit" :disabled="loading || form.questions.length === 0">
              {{ loading ? '创建中...' : '创建测验' }}
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
import { quizApi } from '../api/quiz'
import { courseApi } from '../api/course'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const form = reactive({
  title: '',
  description: '',
  totalTime: 0,
  courseId: '',
  questions: []
})

const courses = ref([])
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

const addQuestion = () => {
  form.questions.push({
    content: '',
    type: 'SINGLE_CHOICE',
    options: ['', '', '', ''],
    answer: '',
    score: 1,
    analysis: ''
  })
}

const removeQuestion = (index) => {
  form.questions.splice(index, 1)
}

const handleSubmit = async () => {
  if (form.questions.length === 0) {
    error.value = '请至少添加一道题目'
    return
  }

  // 验证题目
  for (let i = 0; i < form.questions.length; i++) {
    const q = form.questions[i]
    if (!q.content.trim()) {
      error.value = `第${i + 1}题的内容不能为空`
      return
    }
    if (q.type === 'SINGLE_CHOICE') {
      if (q.options.some(opt => !opt.trim())) {
        error.value = `第${i + 1}题的选项不能为空`
        return
      }
      if (!q.answer) {
        error.value = `第${i + 1}题请选择正确答案`
        return
      }
    }
  }

  error.value = ''
  success.value = ''
  loading.value = true

  try {
    // 转换题目格式
    const questionsData = form.questions.map(q => ({
      content: q.content,
      type: q.type,
      options: q.type === 'SINGLE_CHOICE' ? JSON.stringify(q.options) : JSON.stringify(['正确', '错误']),
      answer: q.answer,
      score: q.score,
      analysis: q.analysis || ''
    }))

    const requestData = {
      title: form.title,
      description: form.description,
      totalTime: form.totalTime,
      courseId: form.courseId || undefined,
      questions: questionsData
    }

    const response = await quizApi.createQuiz(userStore.user.id, requestData)
    
    if (response.data.code === 200) {
      success.value = '测验创建成功！'
      setTimeout(() => {
        // 检查当前路由是否在教师端
        if (route.path.startsWith('/teacher/')) {
          router.push('/teacher/quizzes')
        } else {
          router.push('/quizzes')
        }
      }, 1500)
    } else {
      error.value = response.data.message
    }
  } catch (err) {
    error.value = '创建失败，请检查网络连接'
  } finally {
    loading.value = false
  }
}

const goBack = () => {
  // 检查当前路由是否在教师端
  if (route.path.startsWith('/teacher/')) {
    router.push('/teacher/quizzes')
  } else {
    router.push('/quizzes')
  }
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
.quiz-create-container {
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

.create-content {
  max-width: 800px;
  margin: 2rem auto;
  padding: 0 1rem;
}

.create-card {
  background: white;
  border-radius: 12px;
  padding: 2rem;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.create-card h2 {
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

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1rem;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}

.section-header h3 {
  color: #333;
  margin: 0;
}

.btn-add-question {
  background: #42b883;
  color: white;
  border: none;
  padding: 0.5rem 1rem;
  border-radius: 6px;
  cursor: pointer;
  font-size: 0.9rem;
  transition: background-color 0.3s;
}

.btn-add-question:hover {
  background: #369870;
}

.no-questions {
  text-align: center;
  color: #888;
  padding: 2rem;
  border: 2px dashed #ddd;
  border-radius: 8px;
}

.questions-list {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.question-item {
  border: 1px solid #eee;
  border-radius: 8px;
  padding: 1.5rem;
  background: #fafafa;
}

.question-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
  padding-bottom: 0.75rem;
  border-bottom: 1px solid #eee;
}

.btn-remove {
  background: #ff6b6b;
  color: white;
  border: none;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  cursor: pointer;
  font-size: 1rem;
  display: flex;
  align-items: center;
  justify-content: center;
}

.options-section,
.true-false-section {
  margin: 1rem 0;
}

.option-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 0.75rem;
}

.option-letter {
  font-weight: bold;
  color: #667eea;
  min-width: 20px;
}

.option-item input {
  flex: 1;
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
  .form-row {
    grid-template-columns: 1fr;
  }
}
</style>
