<template>
  <div class="detail-container">
    <nav class="navbar">
      <div class="nav-brand">学习辅助系统</div>
      <div class="nav-links">
        <router-link to="/" class="nav-link">首页</router-link>
        <router-link to="/courses" class="nav-link">课程管理</router-link>
        <router-link to="/tasks" class="nav-link">学习计划</router-link>
        <router-link to="/materials" class="nav-link">资料中心</router-link>
        <router-link to="/profile" class="nav-link">个人中心</router-link>
        <button @click="handleLogout" class="btn-logout">退出</button>
      </div>
    </nav>

    <main class="detail-content">
      <div v-if="loading" class="loading">加载中...</div>
      
      <div v-else-if="material" class="material-detail">
        <div class="detail-header">
          <div class="file-icon-large">
            <span class="icon">{{ getFileIcon(material.fileType) }}</span>
          </div>
          <div class="header-info">
            <h1>{{ material.name }}</h1>
            <p class="file-name">{{ material.fileName }}</p>
            <div class="meta-info">
              <span>📁 {{ material.fileSizeFormatted }}</span>
              <span v-if="material.courseName">📚 {{ material.courseName }}</span>
              <span>👤 {{ material.username }}</span>
              <span>🕒 {{ formatDate(material.createdAt) }}</span>
            </div>
          </div>
          <div class="actions">
            <a :href="downloadUrl" class="btn-download" @click="handleDownload">
              📥 下载 ({{ material.downloadCount }})
            </a>
            <button 
              class="btn-like" 
              :class="{ active: material.likedByCurrentUser }"
              @click="toggleLike"
            >
              👍 {{ material.likeCount }}
            </button>
            <button 
              class="btn-favorite" 
              :class="{ active: material.favoritedByCurrentUser }"
              @click="toggleFavorite"
            >
              ⭐ {{ material.favoriteCount }}
            </button>
            <button 
              v-if="material.userId === userStore.user?.id"
              class="btn-delete"
              @click="deleteMaterial"
            >
              🗑️ 删除
            </button>
          </div>
        </div>

        <div v-if="material.description" class="description-section">
          <h3>资料描述</h3>
          <p>{{ material.description }}</p>
        </div>

        <div class="comments-section">
          <h3>评论 ({{ comments.length }})</h3>
          
          <div class="comment-form">
            <textarea 
              v-model="newComment"
              placeholder="写下你的评论..."
              rows="3"
            ></textarea>
            <button 
              @click="submitComment" 
              :disabled="!newComment.trim() || commentLoading"
              class="btn-comment"
            >
              {{ commentLoading ? '发布中...' : '发布评论' }}
            </button>
          </div>

          <div v-if="comments.length === 0" class="no-comments">
            暂无评论，快来抢沙发吧！
          </div>

          <div v-else class="comments-list">
            <div 
              v-for="comment in comments" 
              :key="comment.id" 
              class="comment-item"
            >
              <div class="comment-avatar">
                <span>{{ getAvatarText(comment.username) }}</span>
              </div>
              <div class="comment-content">
                <div class="comment-header">
                  <span class="comment-author">{{ comment.username }}</span>
                  <span class="comment-time">{{ formatTime(comment.createdAt) }}</span>
                </div>
                <p class="comment-text">{{ comment.content }}</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { materialApi } from '../api/material'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const material = ref(null)
const comments = ref([])
const loading = ref(false)
const commentLoading = ref(false)
const newComment = ref('')

const materialId = computed(() => route.params.id)
const downloadUrl = computed(() => 
  material.value ? materialApi.downloadMaterial(material.value.id, userStore.user.id) : '#'
)

const fetchMaterial = async () => {
  loading.value = true
  try {
    const response = await materialApi.getMaterialById(materialId.value, userStore.user.id)
    if (response.data.code === 200) {
      material.value = response.data.data
      fetchComments()
    }
  } catch (error) {
    console.error('获取资料详情失败:', error)
    router.push('/materials')
  } finally {
    loading.value = false
  }
}

const fetchComments = async () => {
  try {
    const response = await materialApi.getComments(materialId.value)
    if (response.data.code === 200) {
      comments.value = response.data.data
    }
  } catch (error) {
    console.error('获取评论失败:', error)
  }
}

const handleDownload = () => {
  // 下载统计已在后端处理
}

const toggleLike = async () => {
  try {
    const response = await materialApi.toggleLike(material.value.id, userStore.user.id)
    if (response.data.code === 200) {
      Object.assign(material.value, response.data.data)
    }
  } catch (error) {
    console.error('点赞操作失败:', error)
  }
}

const toggleFavorite = async () => {
  try {
    const response = await materialApi.toggleFavorite(material.value.id, userStore.user.id)
    if (response.data.code === 200) {
      Object.assign(material.value, response.data.data)
    }
  } catch (error) {
    console.error('收藏操作失败:', error)
  }
}

const submitComment = async () => {
  if (!newComment.value.trim()) return

  commentLoading.value = true
  try {
    const response = await materialApi.addComment(
      materialId.value, 
      userStore.user.id, 
      newComment.value
    )
    if (response.data.code === 200) {
      comments.value.unshift(response.data.data)
      newComment.value = ''
    }
  } catch (error) {
    console.error('发表评论失败:', error)
  } finally {
    commentLoading.value = false
  }
}

const deleteMaterial = async () => {
  if (!confirm('确定要删除这份资料吗？')) return

  try {
    const response = await materialApi.deleteMaterial(material.value.id, userStore.user.id)
    if (response.data.code === 200) {
      router.push('/materials')
    }
  } catch (error) {
    alert('删除失败')
  }
}

const getFileIcon = (fileType) => {
  const icons = {
    '.pdf': '📄',
    '.docx': '📝',
    '.pptx': '📊',
    '.jpg': '🖼️',
    '.jpeg': '🖼️',
    '.png': '🖼️'
  }
  return icons[fileType] || '📁'
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleDateString('zh-CN')
}

const formatTime = (dateStr) => {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleString('zh-CN')
}

const getAvatarText = (username) => {
  return username ? username.charAt(0).toUpperCase() : 'U'
}

const handleLogout = () => {
  userStore.logout()
  router.push('/login')
}

onMounted(() => {
  fetchMaterial()
})
</script>

<style scoped>
.detail-container {
  min-height: 100vh;
  background-color: #f5f5f5;
}

.navbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem 2rem;
  background-color: #42b883;
  color: white;
}

.nav-brand {
  font-size: 1.5rem;
  font-weight: bold;
}

.nav-links {
  display: flex;
  gap: 1rem;
  align-items: center;
}

.nav-link {
  color: white;
  text-decoration: none;
  padding: 0.5rem 1rem;
  border-radius: 4px;
  transition: background-color 0.3s;
}

.nav-link:hover {
  background-color: rgba(255, 255, 255, 0.2);
}

.btn-logout {
  background-color: transparent;
  border: 1px solid white;
  color: white;
  padding: 0.5rem 1rem;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
}

.btn-logout:hover {
  background-color: white;
  color: #42b883;
}

.detail-content {
  max-width: 800px;
  margin: 0 auto;
  padding: 2rem;
}

.loading {
  text-align: center;
  padding: 3rem;
  color: #666;
}

.material-detail {
  background: white;
  border-radius: 12px;
  padding: 2rem;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.detail-header {
  display: flex;
  gap: 1.5rem;
  margin-bottom: 2rem;
  padding-bottom: 1.5rem;
  border-bottom: 1px solid #eee;
}

.file-icon-large .icon {
  font-size: 4rem;
}

.header-info {
  flex: 1;
}

.header-info h1 {
  margin: 0 0 0.5rem 0;
  color: #333;
}

.file-name {
  color: #666;
  margin: 0.5rem 0;
}

.meta-info {
  display: flex;
  flex-wrap: wrap;
  gap: 1rem;
  font-size: 0.9rem;
  color: #888;
}

.actions {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.btn-download,
.btn-like,
.btn-favorite,
.btn-delete {
  padding: 0.5rem 1rem;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  text-decoration: none;
  text-align: center;
  font-size: 0.9rem;
  transition: all 0.3s;
}

.btn-download {
  background: #42b883;
  color: white;
}

.btn-download:hover {
  background: #369870;
}

.btn-like,
.btn-favorite {
  background: #f0f0f0;
  color: #666;
}

.btn-like.active {
  background: #e3f2fd;
  color: #1976d2;
}

.btn-favorite.active {
  background: #fff8e1;
  color: #f57c00;
}

.btn-delete {
  background: #ff6b6b;
  color: white;
}

.btn-delete:hover {
  background: #ee5a5a;
}

.description-section {
  margin-bottom: 2rem;
  padding-bottom: 1.5rem;
  border-bottom: 1px solid #eee;
}

.description-section h3 {
  color: #333;
  margin-bottom: 0.75rem;
}

.description-section p {
  color: #666;
  line-height: 1.6;
}

.comments-section h3 {
  color: #333;
  margin-bottom: 1rem;
}

.comment-form {
  margin-bottom: 1.5rem;
}

.comment-form textarea {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-family: inherit;
  font-size: 1rem;
  margin-bottom: 0.75rem;
  resize: vertical;
}

.comment-form textarea:focus {
  outline: none;
  border-color: #667eea;
}

.btn-comment {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  padding: 0.5rem 1.5rem;
  border-radius: 6px;
  cursor: pointer;
  font-size: 0.9rem;
  transition: all 0.3s;
}

.btn-comment:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(102, 126, 234, 0.4);
}

.btn-comment:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.no-comments {
  text-align: center;
  color: #888;
  padding: 2rem;
}

.comments-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.comment-item {
  display: flex;
  gap: 1rem;
  padding: 1rem;
  background: #f8f9fa;
  border-radius: 8px;
}

.comment-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: bold;
  flex-shrink: 0;
}

.comment-content {
  flex: 1;
}

.comment-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 0.5rem;
}

.comment-author {
  font-weight: 500;
  color: #333;
}

.comment-time {
  font-size: 0.8rem;
  color: #888;
}

.comment-text {
  color: #555;
  line-height: 1.5;
  margin: 0;
}

@media (max-width: 768px) {
  .detail-header {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .actions {
    flex-direction: row;
    flex-wrap: wrap;
  }
}
</style>
