import axios from 'axios'

const API_BASE_URL = 'http://localhost:8080/api'

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'multipart/form-data'
  }
})

export const materialApi = {
  uploadMaterial: (formData) => api.post('/material/upload', formData),
  getAllMaterials: (userId) => api.get(`/material/list?userId=${userId}`),
  getMaterialsByCourse: (courseId, userId) => api.get(`/material/list-by-course?courseId=${courseId}&userId=${userId}`),
  getMaterialById: (materialId, userId) => api.get(`/material/${materialId}?userId=${userId}`),
  downloadMaterial: (materialId, userId) => `${API_BASE_URL}/material/${materialId}/download?userId=${userId}`,
  toggleLike: (materialId, userId) => api.post(`/material/${materialId}/like?userId=${userId}`),
  toggleFavorite: (materialId, userId) => api.post(`/material/${materialId}/favorite?userId=${userId}`),
  deleteMaterial: (materialId, userId) => api.delete(`/material/${materialId}?userId=${userId}`),
  getComments: (materialId) => api.get(`/material/${materialId}/comments`),
  addComment: (materialId, userId, content) => api.post(`/material/${materialId}/comment?userId=${userId}&content=${encodeURIComponent(content)}`)
}

export default api
