import axios from 'axios'

const API_BASE_URL = 'http://localhost:8080/api'

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器，添加认证令牌
api.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    console.log('请求拦截器 - 获取token:', token)
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
      console.log('请求拦截器 - 设置Authorization头')
    } else {
      console.log('请求拦截器 - 未找到token!')
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器，用于调试
api.interceptors.response.use(
  response => {
    console.log('响应拦截器 - 收到响应:', response.status)
    return response
  },
  error => {
    console.log('响应拦截器 - 错误:', error.response?.status, error.response?.data)
    return Promise.reject(error)
  }
)

// 管理员相关API

// 获取管理员统计数据
export const getAdminStats = () => {
  return api.get('/admin/stats')
}

// 获取所有用户列表
export const getAllUsers = (params = {}) => {
  return api.get('/admin/users', { params })
}

// 删除用户
export const deleteUser = (userId) => {
  return api.delete(`/admin/users/${userId}`)
}

// 更新用户角色
export const updateUserRole = (userId, role) => {
  return api.put(`/admin/users/${userId}/role`, { role })
}

// 获取所有课程列表
export const getAllCourses = (params = {}) => {
  return api.get('/admin/courses', { params })
}

// 获取所有资料列表
export const getAllMaterials = (params = {}) => {
  return api.get('/admin/materials', { params })
}

// 生成邀请码
export const generateInvitationCode = (data) => {
  return api.post('/admin/invitation-codes', data)
}

// 获取所有邀请码
export const getAllInvitationCodes = () => {
  return api.get('/admin/invitation-codes')
}

// 删除邀请码
export const deleteInvitationCode = (code) => {
  return api.delete(`/admin/invitation-codes/${code}`)
}

// 统一导出
export const adminApi = {
  getStats: getAdminStats,
  getUsers: getAllUsers,
  deleteUser,
  updateUserRole,
  getCourses: getAllCourses,
  getMaterials: getAllMaterials,
  generateInvitationCode,
  getInvitationCodes: getAllInvitationCodes,
  deleteInvitationCode
}

export default api