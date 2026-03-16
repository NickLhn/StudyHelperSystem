import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { userApi } from '../api/user'

export const useUserStore = defineStore('user', () => {
  const user = ref(JSON.parse(localStorage.getItem('user') || 'null'))
  const token = ref(localStorage.getItem('token') || '')
  const isLoggedIn = computed(() => !!token.value && !!user.value)
  
  // 角色判断计算属性
  const isStudent = computed(() => user.value?.role === 'STUDENT')
  const isTeacher = computed(() => user.value?.role === 'TEACHER')
  const isAdmin = computed(() => user.value?.role === 'ADMIN')

  const setUser = (userData) => {
    user.value = userData
    if (userData) {
      localStorage.setItem('user', JSON.stringify(userData))
    } else {
      localStorage.removeItem('user')
    }
  }

  const setToken = (newToken) => {
    token.value = newToken
    localStorage.setItem('token', newToken)
  }

  const login = async (credentials) => {
    try {
      console.log('发送登录请求:', credentials)
      const response = await userApi.login(credentials)
      console.log('登录响应:', response.data)
      
      if (response.data.code === 200) {
        const { user: userData, token: userToken } = response.data.data
        console.log('设置用户信息:', userData)
        console.log('设置token:', userToken)
        
        setUser(userData)
        setToken(userToken)
        
        console.log('登录后状态 - isLoggedIn:', isLoggedIn.value)
        return { success: true, data: response.data }
      }
      return { success: false, message: response.data.message }
    } catch (error) {
      console.error('登录请求失败:', error)
      return { success: false, message: error.message || '网络错误' }
    }
  }

  // 模拟教师登录（用于测试）
  const mockTeacherLogin = () => {
    const mockTeacher = {
      id: 1,
      name: '测试教师',
      account: 'teacher123',
      role: 'TEACHER',
      email: 'teacher@example.com'
    }
    const mockToken = 'mock-teacher-token-123456'
    
    setUser(mockTeacher)
    setToken(mockToken)
    console.log('模拟教师登录成功')
  }

  // 模拟管理员登录（用于测试）
  const mockAdminLogin = () => {
    const mockAdmin = {
      id: 2,
      name: '测试管理员',
      account: 'admin123',
      role: 'ADMIN',
      email: 'admin@example.com'
    }
    const mockToken = 'mock-admin-token-123456'
    
    setUser(mockAdmin)
    setToken(mockToken)
    console.log('模拟管理员登录成功')
  }

  const register = async (data) => {
    const response = await userApi.register(data)
    if (response.data.code === 200) {
      return { success: true, data: response.data }
    }
    return { success: false, message: response.data.message }
  }

  const logout = () => {
    user.value = null
    token.value = ''
    localStorage.removeItem('token')
    localStorage.removeItem('user')
  }

  const updateProfile = async (id, data) => {
    const response = await userApi.updateUser(id, data)
    if (response.data.code === 200) {
      setUser(response.data.data)
      return { success: true, data: response.data }
    }
    return { success: false, message: response.data.message }
  }

  return {
    user,
    token,
    isLoggedIn,
    isStudent,
    isTeacher,
    isAdmin,
    login,
    register,
    logout,
    updateProfile,
    setUser,
    setToken,
    mockTeacherLogin,
    mockAdminLogin
  }
})
