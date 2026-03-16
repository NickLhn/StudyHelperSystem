<template>
  <div class="admin-users-container">
    <h1 class="page-title">👥 用户管理</h1>
    
    <!-- 筛选和搜索 -->
    <div class="filters-section">
      <div class="filter-row">
        <div class="filter-group">
          <label>角色筛选：</label>
          <select v-model="selectedRole" @change="loadUsers">
            <option value="">全部角色</option>
            <option value="STUDENT">学生</option>
            <option value="TEACHER">教师</option>
            <option value="ADMIN">管理员</option>
          </select>
        </div>
        
        <div class="search-group">
          <input 
            v-model="searchKeyword" 
            type="text" 
            placeholder="搜索用户名或邮箱..."
            @input="debouncedSearch"
          />
        </div>
      </div>
    </div>
    
    <!-- 用户列表 -->
    <div class="users-table-container">
      <table class="users-table">
        <thead>
          <tr>
            <th>ID</th>
            <th>用户名</th>
            <th>邮箱</th>
            <th>学号/工号</th>
            <th>角色</th>
            <th>专业/部门</th>
            <th>注册时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="user in users" :key="user.id">
            <td>{{ user.id }}</td>
            <td>{{ user.username }}</td>
            <td>{{ user.email || '-' }}</td>
            <td>{{ user.studentId || user.employeeId || '-' }}</td>
            <td>
              <span class="role-badge" :class="user.role.toLowerCase()">
                {{ getRoleLabel(user.role) }}
              </span>
            </td>
            <td>{{ user.major || user.department || '-' }}</td>
            <td>{{ formatDate(user.createdAt) }}</td>
            <td>
              <div class="action-buttons">
                <button 
                  v-if="user.role !== 'ADMIN'" 
                  @click="changeUserRole(user)" 
                  class="btn-change-role"
                >
                  更改角色
                </button>
                <button 
                  v-if="user.role !== 'ADMIN'" 
                  @click="deleteUser(user.id)" 
                  class="btn-delete"
                >
                  删除
                </button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
      
      <!-- 分页 -->
      <div class="pagination">
        <button 
          @click="prevPage" 
          :disabled="currentPage <= 0"
          class="btn-pagination"
        >
          上一页
        </button>
        <span class="page-info">
          第 {{ currentPage + 1 }} 页，共 {{ totalPages }} 页
        </span>
        <button 
          @click="nextPage" 
          :disabled="currentPage >= totalPages - 1"
          class="btn-pagination"
        >
          下一页
        </button>
      </div>
    </div>
    
    <!-- 加载状态 -->
    <div v-if="loading" class="loading">
      <div class="spinner"></div>
      <p>加载用户列表中...</p>
    </div>
    
    <!-- 错误提示 -->
    <div v-if="error" class="error-message">
      <i class="icon">⚠️</i>
      {{ error }}
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { adminApi } from '../api/admin'

const users = ref([])
const loading = ref(false)
const error = ref('')
const selectedRole = ref('')
const searchKeyword = ref('')
const currentPage = ref(0)
const pageSize = ref(20)
const totalPages = ref(1)

const loadUsers = async () => {
  loading.value = true
  error.value = ''
  
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value
    }
    
    if (selectedRole.value) {
      params.role = selectedRole.value
    }
    
    if (searchKeyword.value) {
      params.keyword = searchKeyword.value
    }
    
    const response = await adminApi.getUsers(params)
    if (response.data.code === 200) {
      // 旧接口返回数组，新接口返回包含 content 和 totalPages 的对象
      const data = response.data.data
      if (Array.isArray(data)) {
        users.value = data
        totalPages.value = Math.ceil(data.length / pageSize.value) || 1
      } else {
        users.value = data.content
        totalPages.value = data.totalPages
      }
    } else {
      error.value = response.data.message
    }
  } catch (err) {
    error.value = '获取用户列表失败，请检查网络连接'
    console.error('获取用户列表失败:', err)
  } finally {
    loading.value = false
  }
}

const deleteUser = async (userId) => {
  if (!confirm('确定要删除这个用户吗？此操作不可撤销。')) {
    return
  }
  
  try {
    const response = await adminApi.deleteUser(userId)
    if (response.data.code === 200) {
      loadUsers() // 重新加载用户列表
    } else {
      error.value = response.data.message
    }
  } catch (err) {
    error.value = '删除用户失败'
    console.error('删除用户失败:', err)
  }
}

const changeUserRole = async (user) => {
  const newRole = prompt(`请输入新的角色 (STUDENT/TEACHER/ADMIN):`, user.role)
  if (!newRole || newRole === user.role) return
  
  try {
    const response = await adminApi.updateUserRole(user.id, newRole)
    if (response.data.code === 200) {
      loadUsers() // 重新加载用户列表
    } else {
      error.value = response.data.message
    }
  } catch (err) {
    error.value = '更新用户角色失败'
    console.error('更新用户角色失败:', err)
  }
}

const getRoleLabel = (role) => {
  const roleMap = {
    'STUDENT': '学生',
    'TEACHER': '教师',
    'ADMIN': '管理员'
  }
  return roleMap[role] || role
}

const formatDate = (dateString) => {
  if (!dateString) return '-'
  return new Date(dateString).toLocaleDateString('zh-CN')
}

const prevPage = () => {
  if (currentPage.value > 0) {
    currentPage.value--
    loadUsers()
  }
}

const nextPage = () => {
  if (currentPage.value < totalPages.value - 1) {
    currentPage.value++
    loadUsers()
  }
}

// 防抖搜索
let searchTimeout
const debouncedSearch = () => {
  clearTimeout(searchTimeout)
  searchTimeout = setTimeout(() => {
    currentPage.value = 0
    loadUsers()
  }, 500)
}

onMounted(() => {
  loadUsers()
})
</script>

<style scoped>
.admin-users-container {
  padding: 2rem 0;
}

.page-title {
  color: #333;
  margin-bottom: 2rem;
  font-size: 2rem;
  text-align: center;
}

.filters-section {
  background: white;
  padding: 1.5rem;
  border-radius: 12px;
  margin-bottom: 2rem;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.08);
}

.filter-row {
  display: flex;
  gap: 1rem;
  align-items: center;
  flex-wrap: wrap;
}

.filter-group, .search-group {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.filter-group label {
  font-weight: 500;
  color: #555;
}

.filter-group select, .search-group input {
  padding: 0.5rem;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 1rem;
}

.search-group input {
  min-width: 250px;
}

.users-table-container {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.08);
}

.users-table {
  width: 100%;
  border-collapse: collapse;
}

.users-table th {
  background: #f8f9fa;
  padding: 1rem;
  text-align: left;
  font-weight: 600;
  color: #555;
  border-bottom: 2px solid #e9ecef;
}

.users-table td {
  padding: 1rem;
  border-bottom: 1px solid #e9ecef;
}

.users-table tr:hover {
  background: #f8f9fa;
}

.role-badge {
  padding: 0.25rem 0.75rem;
  border-radius: 20px;
  font-size: 0.85rem;
  font-weight: 500;
}

.role-badge.student {
  background: #e3f2fd;
  color: #1976d2;
}

.role-badge.teacher {
  background: #e8f5e8;
  color: #2e7d32;
}

.role-badge.admin {
  background: #fff3e0;
  color: #f57c00;
}

.action-buttons {
  display: flex;
  gap: 0.5rem;
}

.btn-change-role, .btn-delete {
  padding: 0.25rem 0.75rem;
  border: none;
  border-radius: 6px;
  font-size: 0.85rem;
  cursor: pointer;
  transition: all 0.2s ease;
}

.btn-change-role {
  background: #667eea;
  color: white;
}

.btn-change-role:hover {
  background: #5a6fd8;
}

.btn-delete {
  background: #f44336;
  color: white;
}

.btn-delete:hover {
  background: #d32f2f;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 1rem;
  padding: 1.5rem;
  background: #f8f9fa;
}

.btn-pagination {
  padding: 0.5rem 1rem;
  border: 1px solid #ddd;
  background: white;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.btn-pagination:hover:not(:disabled) {
  background: #667eea;
  color: white;
  border-color: #667eea;
}

.btn-pagination:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.page-info {
  color: #666;
  font-weight: 500;
}

.loading {
  text-align: center;
  padding: 3rem;
  color: #666;
}

.spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid #667eea;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 1rem;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.error-message {
  background: #ffebee;
  color: #c62828;
  padding: 1rem;
  border-radius: 8px;
  text-align: center;
  margin: 2rem 0;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .filter-row {
    flex-direction: column;
    align-items: stretch;
  }
  
  .users-table {
    font-size: 0.9rem;
  }
  
  .users-table th,
  .users-table td {
    padding: 0.5rem;
  }
  
  .action-buttons {
    flex-direction: column;
    gap: 0.25rem;
  }
  
  .btn-change-role, .btn-delete {
    width: 100%;
  }
}
</style>