import api from './client'

export const notificationApi = {
  getNotifications: (userId, limit = 20) => api.get(`/notification/list?userId=${userId}&limit=${limit}`)
}

export default api
