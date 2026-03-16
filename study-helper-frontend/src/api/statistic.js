import axios from 'axios'

const API_BASE_URL = 'http://localhost:8080/api'

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json'
  }
})

export const statisticApi = {
  getUserStatistics: (userId, period = 'week') => 
    api.get(`/statistics/user?userId=${userId}&period=${period}`),
  getComparisonStatistics: (userId, period = 'week') => 
    api.get(`/statistics/comparison?userId=${userId}&period=${period}`),
  syncFromTasks: (userId) => 
    api.post(`/statistics/sync-from-tasks?userId=${userId}`)
}

export default api
