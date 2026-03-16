import axios from 'axios'

const API_BASE_URL = 'http://localhost:8080/api'

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json'
  }
})

export const taskApi = {
  createTask: (userId, data) => api.post(`/task/create?userId=${userId}`, data),
  getUserTasks: (userId) => api.get(`/task/list?userId=${userId}`),
  getCourseTasks: (userId, courseId) => api.get(`/task/list-by-course?userId=${userId}&courseId=${courseId}`),
  getUserTasksByStatus: (userId, status) => api.get(`/task/list-by-status?userId=${userId}&status=${status}`),
  getUserTasksByDate: (userId, date) => api.get(`/task/list-by-date?userId=${userId}&date=${date}`),
  getUserTasksByDateRange: (userId, startDate, endDate) => 
    api.get(`/task/list-by-date-range?userId=${userId}&startDate=${startDate}&endDate=${endDate}`),
  getTodayReminders: (userId) => api.get(`/task/today-reminders?userId=${userId}`),
  getTaskStatistics: (userId) => api.get(`/task/statistics?userId=${userId}`),
  getTaskById: (taskId, userId) => api.get(`/task/${taskId}?userId=${userId}`),
  updateTask: (taskId, userId, data) => api.put(`/task/${taskId}?userId=${userId}`, data),
  updateTaskStatus: (taskId, userId, status) => api.patch(`/task/${taskId}/status?userId=${userId}&status=${status}`),
  deleteTask: (taskId, userId) => api.delete(`/task/${taskId}?userId=${userId}`)
}

export default api
