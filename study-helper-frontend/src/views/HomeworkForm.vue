<template>
  <div class="page-stack">
    <section class="page-intro form-shell">
      <div class="page-intro-copy">
        <span class="page-eyebrow">Homework Builder</span>
        <h2 class="page-title">发布作业</h2>
        <p class="page-subtitle">先支持客观题自动批改和主观题教师复核，后续可以在这套结构上继续扩展评分规则。</p>
      </div>
    </section>

    <main class="form-shell">
      <section class="info-card form-panel">
        <form class="form-grid" @submit.prevent="handleSubmit">
          <div class="field">
            <label>作业标题</label>
            <input v-model="form.title" type="text" placeholder="请输入作业标题" required />
          </div>

          <div class="field">
            <label>作业说明</label>
            <textarea v-model="form.description" rows="4" placeholder="请输入作业说明"></textarea>
          </div>

          <div class="form-grid two-up">
            <div class="field">
              <label>关联课程</label>
              <select v-model="form.courseId" required>
                <option value="">请选择课程</option>
                <option v-for="course in courses" :key="course.id" :value="course.id">
                  {{ course.name }}
                </option>
              </select>
            </div>
            <div class="field">
              <label>截止时间</label>
              <input v-model="form.deadlineAt" type="datetime-local" required />
            </div>
          </div>

          <div class="form-grid two-up">
            <label class="checkbox-row">
              <input v-model="form.autoGradeEnabled" type="checkbox" />
              <span>开启自动批改</span>
            </label>
            <label class="checkbox-row">
              <input v-model="form.allowLateSubmit" type="checkbox" />
              <span>允许逾期提交</span>
            </label>
          </div>

          <div class="question-shell">
            <div class="toolbar-row question-header">
              <div>
                <h3 class="section-title">题目设置</h3>
                <p class="section-copy">当前支持单选、判断、填空和简答，简答题会自动进入教师复核。</p>
              </div>
              <button type="button" class="edu-btn edu-btn-primary" @click="addQuestion">添加题目</button>
            </div>

            <div v-if="form.questions.length === 0" class="empty-panel">
              <h3 class="empty-title">还没有题目</h3>
              <p class="empty-copy">先添加题目后，系统才能生成作业并开始自动批改。</p>
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
                    <select v-model="question.type">
                      <option value="SINGLE_CHOICE">单选题</option>
                      <option value="TRUE_FALSE">判断题</option>
                      <option value="FILL_BLANK">填空题</option>
                      <option value="SHORT_ANSWER">简答题</option>
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
                      v-for="(letter, optionIndex) in optionLetters"
                      :key="letter"
                      v-model="question.options[optionIndex]"
                      type="text"
                      :placeholder="`选项 ${letter}`"
                    />
                  </div>
                  <select v-model="question.answer" required>
                    <option value="">请选择正确答案</option>
                    <option v-for="letter in optionLetters" :key="letter" :value="letter">{{ letter }}</option>
                  </select>
                </div>

                <div v-else-if="question.type === 'TRUE_FALSE'" class="field">
                  <label>正确答案</label>
                  <select v-model="question.answer" required>
                    <option value="">请选择正确答案</option>
                    <option value="A">正确</option>
                    <option value="B">错误</option>
                  </select>
                </div>

                <div v-else class="field">
                  <label>{{ question.type === 'SHORT_ANSWER' ? '参考答案' : '标准答案' }}</label>
                  <textarea
                    v-model="question.answer"
                    rows="2"
                    :placeholder="question.type === 'SHORT_ANSWER' ? '可填写参考答案，便于教师复核' : '支持填写单个答案或 JSON 数组答案'"
                  ></textarea>
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
              {{ loading ? '创建中...' : '创建作业' }}
            </button>
          </div>
        </form>
      </section>
    </main>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { courseApi } from '../api/course'
import { homeworkApi } from '../api/homework'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const optionLetters = ['A', 'B', 'C', 'D']
const courses = ref([])
const loading = ref(false)
const error = ref('')
const success = ref('')

const form = reactive({
  title: '',
  description: '',
  courseId: '',
  deadlineAt: '',
  autoGradeEnabled: true,
  allowLateSubmit: false,
  questions: []
})

const addQuestion = () => {
  form.questions.push({
    content: '',
    type: 'SINGLE_CHOICE',
    options: ['', '', '', ''],
    answer: '',
    score: 5,
    analysis: ''
  })
}

const removeQuestion = (index) => {
  form.questions.splice(index, 1)
}

const fetchCourses = async () => {
  try {
    const response = await courseApi.getUserCourses(userStore.user.id)
    if (response.data.code === 200) {
      courses.value = response.data.data
    }
  } catch (err) {
    error.value = '获取课程失败'
  }
}

const handleSubmit = async () => {
  error.value = ''
  success.value = ''

  if (form.questions.length === 0) {
    error.value = '请至少添加一道题目'
    return
  }

  for (let index = 0; index < form.questions.length; index += 1) {
    const question = form.questions[index]
    if (!question.content.trim()) {
      error.value = `第 ${index + 1} 题内容不能为空`
      return
    }
    if (question.type === 'SINGLE_CHOICE' && question.options.some((item) => !item.trim())) {
      error.value = `第 ${index + 1} 题选项不能为空`
      return
    }
    if ((question.type === 'SINGLE_CHOICE' || question.type === 'TRUE_FALSE' || question.type === 'FILL_BLANK') && !String(question.answer || '').trim()) {
      error.value = `第 ${index + 1} 题请填写标准答案`
      return
    }
  }

  loading.value = true
  try {
    const payload = {
      title: form.title,
      description: form.description,
      courseId: Number(form.courseId),
      deadlineAt: form.deadlineAt,
      autoGradeEnabled: form.autoGradeEnabled,
      allowLateSubmit: form.allowLateSubmit,
      questions: form.questions.map((question) => ({
        content: question.content,
        type: question.type,
        options: question.type === 'SINGLE_CHOICE'
          ? question.options
          : question.type === 'TRUE_FALSE'
            ? ['正确', '错误']
            : [],
        answer: question.answer || '',
        score: question.score,
        analysis: question.analysis || '',
        manualReviewRequired: question.type === 'SHORT_ANSWER'
      }))
    }

    const response = await homeworkApi.createHomework(userStore.user.id, payload)
    if (response.data.code === 200) {
      success.value = '作业创建成功！'
      setTimeout(() => router.push('/teacher/homeworks'), 900)
    } else {
      error.value = response.data.message
    }
  } catch (err) {
    error.value = '创建作业失败，请检查网络连接'
  } finally {
    loading.value = false
  }
}

const goBack = () => {
  router.push('/teacher/homeworks')
}

onMounted(() => {
  fetchCourses()
  if (typeof route.query.courseId === 'string' && route.query.courseId) {
    form.courseId = route.query.courseId
  }
})
</script>

<style scoped>
.checkbox-row {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  font-weight: var(--font-weight-bold);
  color: var(--ink-soft);
}

.checkbox-row input {
  width: 18px;
  height: 18px;
}

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
</style>
