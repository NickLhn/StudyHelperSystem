import api from './client'

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
