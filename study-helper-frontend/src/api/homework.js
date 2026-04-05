import api from './client'

export const homeworkApi = {
  createHomework: (userId, data) => api.post(`/homework/create?userId=${userId}`, data),
  getTeacherHomeworks: (userId, courseId) =>
    api.get(courseId ? `/homework/teacher?userId=${userId}&courseId=${courseId}` : `/homework/teacher?userId=${userId}`),
  getStudentHomeworks: (userId) => api.get(`/homework/student?userId=${userId}`),
  getHomeworkDetail: (homeworkId, userId) => api.get(`/homework/${homeworkId}?userId=${userId}`),
  updateHomeworkStatus: (homeworkId, userId, status) => api.patch(`/homework/${homeworkId}/status?userId=${userId}&status=${status}`),
  deleteHomework: (homeworkId, userId) => api.delete(`/homework/${homeworkId}?userId=${userId}`),
  submitHomework: (homeworkId, userId, data) => api.post(`/homework/${homeworkId}/submit?userId=${userId}`, data),
  getHomeworkSubmissions: (homeworkId, userId) => api.get(`/homework/${homeworkId}/submissions?userId=${userId}`),
  getSubmissionDetail: (submissionId, userId) => api.get(`/homework/submissions/${submissionId}?userId=${userId}`),
  reviewSubmission: (submissionId, userId, data) => api.patch(`/homework/submissions/${submissionId}/review?userId=${userId}`, data),
  getHomeworkSummary: (homeworkId, userId) => api.get(`/homework/${homeworkId}/summary?userId=${userId}`)
}

export default api
