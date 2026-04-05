import api from './client'

export const courseApi = {
  createCourse: (userId, data) => api.post(`/course/create?userId=${userId}`, data),
  getUserCourses: (userId) => api.get(`/course/list?userId=${userId}`),
  getUserCoursesByStatus: (userId, status) => api.get(`/course/list-by-status?userId=${userId}&status=${status}`),
  getStudentCourses: (userId) => api.get(`/course/student/list?userId=${userId}`),
  getStudentCoursesByStatus: (userId, status) => api.get(`/course/student/list-by-status?userId=${userId}&status=${status}`),
  joinCourse: (userId, invitationCode) => api.post(`/course/join?userId=${userId}&invitationCode=${encodeURIComponent(invitationCode)}`),
  getUserCoursesByCategory: (userId, category) => api.get(`/course/list-by-category?userId=${userId}&category=${category}`),
  getUserCoursesByCategoryAndStatus: (userId, category, status) => api.get(`/course/list-by-category-and-status?userId=${userId}&category=${category}&status=${status}`),
  getCourseById: (courseId, userId) => api.get(`/course/${courseId}?userId=${userId}`),
  updateCourse: (courseId, userId, data) => api.put(`/course/${courseId}?userId=${userId}`, data),
  updateCourseStatus: (courseId, userId, status) => api.patch(`/course/${courseId}/status?userId=${userId}&status=${status}`),
  deleteCourse: (courseId, userId) => api.delete(`/course/${courseId}?userId=${userId}`),
  getCourseStudents: (courseId, userId) => api.get(`/course/${courseId}/students?userId=${userId}`),
  refreshInvitationCode: (courseId, userId) => api.post(`/course/${courseId}/invitation-code/refresh?userId=${userId}`),
  removeStudent: (courseId, userId, studentId) => api.delete(`/course/${courseId}/students/${studentId}?userId=${userId}`),
  getCourseStats: (courseId, userId, period = 'week') => api.get(`/course/${courseId}/stats?userId=${userId}&period=${period}`)
}

export default api
