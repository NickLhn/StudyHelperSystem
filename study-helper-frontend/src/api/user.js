import axios from 'axios'

const API_BASE_URL = 'http://localhost:8080/api'

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json'
  }
})

export const userApi = {
  register: (data) => api.post('/user/register', data),
  login: (data) => api.post('/user/login', data),
  getUserById: (id) => api.get(`/user/${id}`),
  updateUser: (id, data) => api.put(`/user/${id}`, data),
  updateAvatar: (id, avatarUrl) => api.post(`/user/${id}/avatar`, null, { params: { avatarUrl } })
}

export default api
