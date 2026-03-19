import api, { API_BASE_URL } from './client'

export const materialApi = {
  uploadMaterial: (formData) => api.post('/material/upload', formData),
  getAllMaterials: (userId) => api.get(`/material/list?userId=${userId}`),
  getMaterialsByCourse: (courseId, userId) => api.get(`/material/list-by-course?courseId=${courseId}&userId=${userId}`),
  getMaterialById: (materialId, userId) => api.get(`/material/${materialId}?userId=${userId}`),
  downloadMaterial: (materialId, userId) => {
    const params = new URLSearchParams({ userId: String(userId) })
    const token = localStorage.getItem('token')
    if (token) {
      params.set('token', token)
    }
    return `${API_BASE_URL}/material/${materialId}/download?${params.toString()}`
  },
  toggleLike: (materialId, userId) => api.post(`/material/${materialId}/like?userId=${userId}`),
  toggleFavorite: (materialId, userId) => api.post(`/material/${materialId}/favorite?userId=${userId}`),
  deleteMaterial: (materialId, userId) => api.delete(`/material/${materialId}?userId=${userId}`),
  getComments: (materialId, userId) => api.get(`/material/${materialId}/comments?userId=${userId}`),
  addComment: (materialId, userId, content) => api.post(`/material/${materialId}/comment?userId=${userId}&content=${encodeURIComponent(content)}`)
}

export default api
