import api from './client'

export const userApi = {
  register: (data) => api.post('/user/register', data),
  login: (data) => api.post('/user/login', data),
  getUserById: (id) => api.get(`/user/${id}`),
  updateUser: (id, data) => api.put(`/user/${id}`, data),
  updateAvatar: (id, avatarUrl) => api.post(`/user/${id}/avatar`, null, { params: { avatarUrl } })
}

export default api
