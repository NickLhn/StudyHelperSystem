<template>
  <div class="admin-invitations-container">
    <h1 class="page-title">🔑 邀请码管理</h1>
    
    <div class="invitations-section">
      <div class="generate-section">
        <h2>生成邀请码</h2>
        <div class="generate-form">
          <input 
            v-model="newCode" 
            type="text" 
            placeholder="邀请码（留空自动生成）"
          />
          <input 
            v-model="description" 
            type="text" 
            placeholder="描述"
          />
          <button @click="generateCode" class="btn-generate">
            生成邀请码
          </button>
        </div>
      </div>
      
      <div class="codes-list">
        <h2>邀请码列表</h2>
        <div class="codes-content">
          <div v-if="loading" class="loading">
            <div class="spinner"></div>
            <p>加载邀请码列表中...</p>
          </div>
          <div v-else-if="error" class="error-message">
            <i class="icon">⚠️</i>
            {{ error }}
          </div>
          <table v-else class="codes-table">
            <thead>
              <tr>
                <th>邀请码</th>
                <th>描述</th>
                <th>生成时间</th>
                <th>使用状态</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="code in invitationCodes" :key="code.id">
                <td>{{ code.code }}</td>
                <td>{{ code.description }}</td>
                <td>{{ formatDate(code.createdAt) }}</td>
                <td>
                  <span class="status-badge" :class="code.used ? 'used' : 'unused'">
                    {{ code.used ? '已使用' : '未使用' }}
                  </span>
                </td>
                <td>
                  <button 
                    v-if="!code.used" 
                    @click="deleteCode(code.id)" 
                    class="btn-delete"
                  >
                    删除
                  </button>
                  <span v-else class="used-tag">已使用</span>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { adminApi } from '../api/admin'

const newCode = ref('')
const description = ref('')
const invitationCodes = ref([])
const loading = ref(false)
const error = ref('')

const generateCode = async () => {
  try {
    const response = await adminApi.generateInvitationCode({
      code: newCode.value || undefined,
      description: description.value || '教师邀请码'
    })
    if (response.data.code === 200) {
      alert('邀请码生成成功！')
      newCode.value = ''
      description.value = ''
      loadInvitationCodes() // 重新加载邀请码列表
    } else {
      alert('生成失败：' + response.data.message)
    }
  } catch (err) {
    alert('生成邀请码失败')
    console.error('生成邀请码失败:', err)
  }
}

const loadInvitationCodes = async () => {
  loading.value = true
  error.value = ''
  
  try {
    const response = await adminApi.getInvitationCodes()
    if (response.data.code === 200) {
      invitationCodes.value = response.data.data
    } else {
      error.value = response.data.message
    }
  } catch (err) {
    error.value = '获取邀请码列表失败，请检查网络连接'
    console.error('获取邀请码列表失败:', err)
  } finally {
    loading.value = false
  }
}

const deleteCode = async (codeId) => {
  if (!confirm('确定要删除这个邀请码吗？此操作不可撤销。')) {
    return
  }
  
  try {
    const response = await adminApi.deleteInvitationCode(codeId)
    if (response.data.code === 200) {
      loadInvitationCodes() // 重新加载邀请码列表
    } else {
      error.value = response.data.message
    }
  } catch (err) {
    error.value = '删除邀请码失败'
    console.error('删除邀请码失败:', err)
  }
}

const formatDate = (dateString) => {
  if (!dateString) return '-'
  return new Date(dateString).toLocaleString('zh-CN')
}

onMounted(() => {
  loadInvitationCodes()
})
</script>

<style scoped>
.admin-invitations-container {
  padding: 2rem 0;
}

.page-title {
  color: #333;
  margin-bottom: 2rem;
  font-size: 2rem;
  text-align: center;
}

.invitations-section {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 2rem;
}

.generate-section, .codes-list {
  background: white;
  padding: 2rem;
  border-radius: 12px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.08);
}

.generate-section h2, .codes-list h2 {
  color: #333;
  margin-bottom: 1rem;
  font-size: 1.5rem;
}

.generate-form {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.generate-form input {
  padding: 0.75rem;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 1rem;
}

.btn-generate {
  padding: 0.75rem;
  background: #667eea;
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 1rem;
  cursor: pointer;
  transition: background 0.3s ease;
}

.btn-generate:hover {
  background: #5a6fd8;
}

.codes-content {
  overflow-x: auto;
}

.codes-table {
  width: 100%;
  border-collapse: collapse;
}

.codes-table th {
  background: #f8f9fa;
  padding: 0.75rem;
  text-align: left;
  font-weight: 600;
  color: #555;
  border-bottom: 2px solid #e9ecef;
}

.codes-table td {
  padding: 0.75rem;
  border-bottom: 1px solid #e9ecef;
}

.codes-table tr:hover {
  background: #f8f9fa;
}

.status-badge {
  padding: 0.25rem 0.75rem;
  border-radius: 20px;
  font-size: 0.85rem;
  font-weight: 500;
}

.status-badge.unused {
  background: #e8f5e8;
  color: #2e7d32;
}

.status-badge.used {
  background: #ffebee;
  color: #c62828;
}

.btn-delete {
  padding: 0.25rem 0.75rem;
  background: #f44336;
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 0.85rem;
  cursor: pointer;
  transition: background 0.2s ease;
}

.btn-delete:hover {
  background: #d32f2f;
}

.used-tag {
  font-size: 0.85rem;
  color: #888;
  font-style: italic;
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
  margin: 1rem 0;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .invitations-section {
    grid-template-columns: 1fr;
  }
  
  .codes-table {
    font-size: 0.9rem;
  }
  
  .codes-table th,
  .codes-table td {
    padding: 0.5rem;
  }
}
</style>