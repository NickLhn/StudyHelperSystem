import api from './client'

export const courseApi = {
  createCourse: (userId, data) => api.post(`/course/create?userId=${userId}`, data),
  getUserCourses: (userId) => api.get(`/course/list?userId=${userId}`),
  getStudentCourses: (userId) => api.get(`/course/student/list?userId=${userId}`),
  joinCourse: (userId, invitationCode) => api.post(`/course/join?userId=${userId}&invitationCode=${encodeURIComponent(invitationCode)}`),
  getUserCoursesByCategory: (userId, category) => api.get(`/course/list-by-category?userId=${userId}&category=${category}`),
  getCourseById: (courseId, userId) => api.get(`/course/${courseId}?userId=${userId}`),
  updateCourse: (courseId, userId, data) => api.put(`/course/${courseId}?userId=${userId}`, data),
  deleteCourse: (courseId, userId) => api.delete(`/course/${courseId}?userId=${userId}`),
  getCourseStudents: (courseId, userId) => api.get(`/course/${courseId}/students?userId=${userId}`),
  refreshInvitationCode: (courseId, userId) => api.post(`/course/${courseId}/invitation-code/refresh?userId=${userId}`),
  removeStudent: (courseId, userId, studentId) => api.delete(`/course/${courseId}/students/${studentId}?userId=${userId}`),
  getCourseStats: (courseId, userId, period = 'week') => api.get(`/course/${courseId}/stats?userId=${userId}&period=${period}`)
}

export default api
