import api from './client'

export const statisticApi = {
  getUserStatistics: (userId, period = 'week') => 
    api.get(`/statistics/user?userId=${userId}&period=${period}`),
  getComparisonStatistics: (userId, period = 'week') => 
    api.get(`/statistics/comparison?userId=${userId}&period=${period}`),
  syncFromTasks: (userId) => 
    api.post(`/statistics/sync-from-tasks?userId=${userId}`)
}

export default api
