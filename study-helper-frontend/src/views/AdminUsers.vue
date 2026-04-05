<template>
  <div class="page-stack">
    <section class="page-intro">
      <div class="page-intro-copy">
        <span class="page-eyebrow">User Governance</span>
        <h2 class="page-title">用户管理页重新梳理成更专业的后台表格</h2>
        <p class="page-subtitle">筛选、搜索、分页和角色操作统一成一块工具区，阅读成本会比原来低很多。</p>
      </div>
      <div class="page-actions">
        <span class="chip">当前第 {{ currentPage + 1 }} 页</span>
      </div>
    </section>

    <section class="filter-card">
      <div class="toolbar-row">
        <select v-model="selectedRole" @change="loadUsers">
          <option value="">全部角色</option>
          <option value="STUDENT">学生</option>
          <option value="TEACHER">教师</option>
          <option value="ADMIN">管理员</option>
        </select>

        <input
          v-model="searchKeyword"
          type="text"
          placeholder="搜索用户名或邮箱"
          class="edu-input admin-search"
          @input="debouncedSearch"
        />
      </div>
    </section>

    <section v-if="loading" class="loading-panel">
      <p class="loading-copy">加载用户列表中...</p>
    </section>

    <section v-else class="table-shell">
      <table>
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
              <span class="status-pill" :class="roleClass(user.role)">
                {{ getRoleLabel(user.role) }}
              </span>
            </td>
            <td>{{ user.major || user.department || '-' }}</td>
            <td>{{ formatDate(user.createdAt) }}</td>
            <td>
              <div class="table-actions">
                <button v-if="user.role !== 'ADMIN'" type="button" class="edu-btn edu-btn-secondary" @click="changeUserRole(user)">
                  更改角色
                </button>
                <button v-if="user.role !== 'ADMIN'" type="button" class="edu-btn edu-btn-danger" @click="deleteUser(user.id)">
                  删除
                </button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>

      <div class="table-pagination">
        <button @click="prevPage" :disabled="currentPage <= 0" class="edu-btn edu-btn-secondary">上一页</button>
        <span class="section-copy">第 {{ currentPage + 1 }} 页，共 {{ totalPages }} 页</span>
        <button @click="nextPage" :disabled="currentPage >= totalPages - 1" class="edu-btn edu-btn-secondary">下一页</button>
      </div>
    </section>

    <section v-if="error" class="message-banner error">
      {{ error }}
    </section>
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

    if (selectedRole.value) params.role = selectedRole.value
    if (searchKeyword.value) params.keyword = searchKeyword.value

    const response = await adminApi.getUsers(params)
    if (response.data.code === 200) {
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
  } finally {
    loading.value = false
  }
}

const deleteUser = async (userId) => {
  if (!confirm('确定要删除这个用户吗？此操作不可撤销。')) return

  try {
    const response = await adminApi.deleteUser(userId)
    if (response.data.code === 200) {
      loadUsers()
    } else {
      error.value = response.data.message
    }
  } catch (err) {
    error.value = '删除用户失败'
  }
}

const changeUserRole = async (user) => {
  const newRole = prompt('请输入新的角色 (STUDENT/TEACHER/ADMIN):', user.role)
  if (!newRole || newRole === user.role) return

  try {
    const response = await adminApi.updateUserRole(user.id, newRole)
    if (response.data.code === 200) {
      loadUsers()
    } else {
      error.value = response.data.message
    }
  } catch (err) {
    error.value = '更新用户角色失败'
  }
}

const getRoleLabel = (role) => ({
  STUDENT: '学生',
  TEACHER: '教师',
  ADMIN: '管理员'
}[role] || role)

const roleClass = (role) => ({
  STUDENT: 'active',
  TEACHER: 'medium',
  ADMIN: 'high'
}[role] || 'todo')

const formatDate = (dateString) => {
  if (!dateString) return '-'
  return new Date(dateString).toLocaleDateString('zh-CN')
}

const prevPage = () => {
  if (currentPage.value > 0) {
    currentPage.value -= 1
    loadUsers()
  }
}

const nextPage = () => {
  if (currentPage.value < totalPages.value - 1) {
    currentPage.value += 1
    loadUsers()
  }
}

let searchTimeout
const debouncedSearch = () => {
  clearTimeout(searchTimeout)
  searchTimeout = setTimeout(() => {
    currentPage.value = 0
    loadUsers()
  }, 500)
}

onMounted(loadUsers)
</script>

<style scoped>
.admin-search {
  min-width: 260px;
  flex: 1;
}
</style>
