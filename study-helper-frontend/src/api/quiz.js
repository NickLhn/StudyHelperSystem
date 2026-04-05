import api from './client'

export const quizApi = {
  createQuiz: (userId, data) => {
    const params = new URLSearchParams()
    params.append('userId', userId)
    params.append('title', data.title)
    if (data.description) params.append('description', data.description)
    if (data.totalTime) params.append('totalTime', data.totalTime)
    params.append('maxAttempts', data.maxAttempts || 1)
    params.append('shuffleQuestions', data.shuffleQuestions ? 'true' : 'false')
    params.append('autoSaveEnabled', data.autoSaveEnabled === false ? 'false' : 'true')
    if (data.courseId) params.append('courseId', data.courseId)
    
    return api.post('/quiz/create', data.questions, { params })
  },
  getUserQuizzes: (userId) => api.get(`/quiz/my?userId=${userId}`),
  getCourseQuizzes: (userId, courseId) => api.get(`/quiz/list-by-course?userId=${userId}&courseId=${courseId}`),
  getAvailableQuizzes: (userId) => api.get(`/quiz/available?userId=${userId}`),
  getQuizDetail: (quizId, userId) => api.get(`/quiz/${quizId}?userId=${userId}`),
  submitQuiz: (quizId, userId, payload) => api.post(`/quiz/${quizId}/submit?userId=${userId}`, payload),
  getUserRecords: (userId) => api.get(`/quiz/records?userId=${userId}`),
  getRecordDetail: (recordId, userId) => api.get(`/quiz/records/${recordId}?userId=${userId}`),
  getWrongQuestions: (userId) => api.get(`/quiz/wrong-questions?userId=${userId}`),
  updateQuizStatus: (quizId, userId, status) => api.patch(`/quiz/${quizId}/status?userId=${userId}&status=${status}`),
  deleteQuiz: (quizId, userId) => api.delete(`/quiz/${quizId}?userId=${userId}`)
}

export default api
