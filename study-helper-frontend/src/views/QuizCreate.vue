<template>
  <div class="page-stack">
    <section class="page-intro form-shell">
      <div class="page-intro-copy">
        <span class="page-eyebrow">Quiz Builder</span>
        <h2 class="page-title">创建测验</h2>
        <p class="page-subtitle">把测验信息和题目编辑器整理到同一条流里，后续接自动批改会更顺手。</p>
      </div>
    </section>

    <main class="form-shell">
      <section class="info-card form-panel">
        <form class="form-grid" @submit.prevent="handleSubmit">
          <div class="field">
            <label>测验标题</label>
            <input v-model="form.title" type="text" placeholder="请输入测验标题" required />
          </div>

          <div class="field">
            <label>测验描述</label>
            <textarea v-model="form.description" rows="4" placeholder="请输入测验说明"></textarea>
          </div>

          <div class="form-grid two-up">
            <div class="field">
              <label>总时长（分钟）</label>
              <input v-model.number="form.totalTime" type="number" min="0" placeholder="0 表示不限时" />
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
          </div>

          <div class="form-grid three-up">
            <div class="field">
              <label>最大作答次数</label>
              <input v-model.number="form.maxAttempts" type="number" min="1" max="10" />
            </div>
            <label class="toggle-field">
              <span>随机题序</span>
              <input v-model="form.shuffleQuestions" type="checkbox" />
            </label>
            <label class="toggle-field">
              <span>自动保存</span>
              <input v-model="form.autoSaveEnabled" type="checkbox" />
            </label>
          </div>

          <div class="field hint-block">
            <label>防作弊说明</label>
            <p>启用后，学生端会显示剩余作答次数、随机后的题目顺序，并在作答过程中自动保存草稿；限时测验刷新页面后也会继续倒计时。</p>
          </div>

          <div class="question-shell">
            <div class="toolbar-row question-header">
              <div>
                <h3 class="section-title">题目设置</h3>
                <p class="section-copy">目前支持单选题和判断题。</p>
              </div>
              <button type="button" class="edu-btn edu-btn-primary" @click="addQuestion">添加题目</button>
            </div>

            <div v-if="form.questions.length === 0" class="empty-panel">
              <h3 class="empty-title">还没有题目</h3>
              <p class="empty-copy">先添加第一道题，系统才可以创建测验。</p>
            </div>

            <div v-else class="question-list">
              <article v-for="(question, index) in form.questions" :key="index" class="question-card">
                <div class="toolbar-row question-card-head">
                  <strong>题目 {{ index + 1 }}</strong>
                  <button type="button" class="edu-btn edu-btn-danger" @click="removeQuestion(index)">删除</button>
                </div>

                <div class="field">
                  <label>题目内容</label>
                  <textarea v-model="question.content" rows="3" placeholder="请输入题目内容" required></textarea>
                </div>

                <div class="form-grid two-up">
                  <div class="field">
                    <label>题目类型</label>
                    <select v-model="question.type" required>
                      <option value="SINGLE_CHOICE">单选题</option>
                      <option value="TRUE_FALSE">判断题</option>
                    </select>
                  </div>
                  <div class="field">
                    <label>分值</label>
                    <input v-model.number="question.score" type="number" min="1" required />
                  </div>
                </div>

                <div v-if="question.type === 'SINGLE_CHOICE'" class="field">
                  <label>选项设置</label>
                  <div class="option-list">
                    <input
                      v-for="(letter, optIndex) in optionLetters"
                      :key="letter"
                      v-model="question.options[optIndex]"
                      type="text"
                      :placeholder="`选项 ${letter}`"
                      required
                    />
                  </div>
                  <select v-model="question.answer" required>
                    <option value="">请选择正确答案</option>
                    <option v-for="letter in optionLetters" :key="letter" :value="letter">{{ letter }}</option>
                  </select>
                </div>

                <div v-else class="field">
                  <label>正确答案</label>
                  <select v-model="question.answer" required>
                    <option value="">请选择正确答案</option>
                    <option value="A">正确</option>
                    <option value="B">错误</option>
                  </select>
                </div>

                <div class="field">
                  <label>解析</label>
                  <textarea v-model="question.analysis" rows="2" placeholder="可选：输入题目解析"></textarea>
                </div>
              </article>
            </div>
          </div>

          <div v-if="error" class="message-banner error">{{ error }}</div>
          <div v-if="success" class="message-banner success">{{ success }}</div>

          <div class="toolbar-row">
            <button type="button" class="edu-btn edu-btn-secondary" @click="goBack">取消</button>
            <button type="submit" class="edu-btn edu-btn-primary" :disabled="loading || form.questions.length === 0">
              {{ loading ? '创建中...' : '创建测验' }}
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
import { quizApi } from '../api/quiz'
import { courseApi } from '../api/course'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const form = reactive({
  title: '',
  description: '',
  totalTime: 0,
  maxAttempts: 1,
  shuffleQuestions: false,
  autoSaveEnabled: true,
  courseId: '',
  questions: []
})

const optionLetters = ['A', 'B', 'C', 'D']
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

  for (let i = 0; i < form.questions.length; i += 1) {
    const q = form.questions[i]
    if (!q.content.trim()) {
      error.value = `第${i + 1}题的内容不能为空`
      return
    }
    if (q.type === 'SINGLE_CHOICE') {
      if (q.options.some((opt) => !opt.trim())) {
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
    const questionsData = form.questions.map((q) => ({
      content: q.content,
      type: q.type,
      options: q.type === 'SINGLE_CHOICE' ? q.options : ['正确', '错误'],
      answer: q.answer,
      score: q.score,
      analysis: q.analysis || ''
    }))

    const requestData = {
      title: form.title,
      description: form.description,
      totalTime: form.totalTime,
      maxAttempts: form.maxAttempts,
      shuffleQuestions: form.shuffleQuestions,
      autoSaveEnabled: form.autoSaveEnabled,
      courseId: form.courseId || undefined,
      questions: questionsData
    }

    const response = await quizApi.createQuiz(userStore.user.id, requestData)
    if (response.data.code === 200) {
      success.value = '测验创建成功！'
      setTimeout(() => {
        router.push(route.path.startsWith('/teacher/') ? '/teacher/quizzes' : '/quizzes')
      }, 900)
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
  router.push(route.path.startsWith('/teacher/') ? '/teacher/quizzes' : '/quizzes')
}

onMounted(() => {
  fetchCourses()
  if (typeof route.query.courseId === 'string' && route.query.courseId) {
    form.courseId = route.query.courseId
  }
})
</script>

<style scoped>
.question-shell {
  display: grid;
  gap: 16px;
}

.question-list {
  display: grid;
  gap: 16px;
}

.question-card {
  display: grid;
  gap: 16px;
  padding: 20px;
  border-radius: 22px;
  border: 1px solid rgba(23, 32, 51, 0.08);
  background: rgba(255, 255, 255, 0.76);
}

.question-card-head {
  justify-content: space-between;
}

.option-list {
  display: grid;
  gap: 10px;
}

.three-up {
  grid-template-columns: repeat(3, minmax(0, 1fr));
  align-items: end;
}

.toggle-field {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 16px 18px;
  border-radius: 18px;
  border: 1px solid rgba(23, 32, 51, 0.1);
  background: rgba(255, 255, 255, 0.7);
  font-weight: 600;
  color: var(--text-main);
}

.toggle-field input {
  width: 18px;
  height: 18px;
}

.hint-block p {
  margin: 0;
  color: var(--text-muted);
  line-height: 1.6;
}

@media (max-width: 768px) {
  .three-up {
    grid-template-columns: 1fr;
  }
}
</style>
