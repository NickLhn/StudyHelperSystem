<template>
  <div class="page-stack">
    <section class="page-intro">
      <div class="page-intro-copy">
        <span class="page-eyebrow">Material Detail</span>
        <h2 class="page-title">把资料详情整理成一页清楚的学习卡片</h2>
        <p class="page-subtitle">文件信息、版本说明、标签和互动记录集中在同一页，减少来回跳转的成本。</p>
      </div>
      <div class="page-actions">
        <button type="button" class="edu-btn edu-btn-secondary" @click="goBack">返回资料中心</button>
      </div>
    </section>

    <section v-if="loading" class="loading-panel">
      <p class="loading-copy">资料详情加载中...</p>
    </section>

    <template v-else-if="material">
      <section class="panel-grid two-up">
        <article class="info-card">
          <div class="stack-lg">
            <div class="detail-heading">
              <div class="file-mark">{{ getFileLabel(material.fileType) }}</div>
              <div>
                <h3 class="section-title">{{ material.name }}</h3>
                <p class="section-copy">{{ material.fileName }}</p>
              </div>
            </div>

            <div class="material-meta">
              <span class="chip">{{ getCategoryLabel(material.category) }}</span>
              <span v-if="material.versionLabel" class="chip primary">{{ material.versionLabel }}</span>
              <span v-if="material.courseName" class="chip soft">{{ material.courseName }}</span>
            </div>

            <div class="data-points">
              <div class="data-point">
                <span>上传者</span>
                <strong>{{ material.username }}</strong>
              </div>
              <div class="data-point">
                <span>文件大小</span>
                <strong>{{ material.fileSizeFormatted }}</strong>
              </div>
              <div class="data-point">
                <span>下载次数</span>
                <strong>{{ material.downloadCount }}</strong>
              </div>
              <div class="data-point">
                <span>上传时间</span>
                <strong>{{ formatDate(material.createdAt) }}</strong>
              </div>
            </div>

            <div class="action-row">
              <a :href="downloadUrl" class="edu-btn edu-btn-primary" @click="handleDownload">下载资料</a>
              <button
                type="button"
                class="edu-btn edu-btn-secondary"
                :class="{ active: material.likedByCurrentUser }"
                @click="toggleLike"
              >
                点赞 {{ material.likeCount }}
              </button>
              <button
                type="button"
                class="edu-btn edu-btn-secondary"
                :class="{ active: material.favoritedByCurrentUser }"
                @click="toggleFavorite"
              >
                收藏 {{ material.favoriteCount }}
              </button>
              <button
                v-if="material.userId === userStore.user?.id"
                type="button"
                class="edu-btn edu-btn-danger"
                @click="deleteMaterial"
              >
                删除资料
              </button>
            </div>
          </div>
        </article>

        <article class="info-card">
          <div class="stack-lg">
            <div>
              <h3 class="section-title">资料说明</h3>
              <p class="section-copy">{{ material.description || '这份资料暂时没有补充说明。' }}</p>
            </div>

            <div v-if="material.versionNote" class="version-panel">
              <h4>版本说明</h4>
              <p>{{ material.versionNote }}</p>
            </div>

            <div v-if="material.tags?.length" class="tag-row">
              <span v-for="tag in material.tags" :key="tag" class="tag-chip">#{{ tag }}</span>
            </div>
          </div>
        </article>
      </section>

      <section class="info-card">
        <div class="table-header-row">
          <div>
            <h3 class="section-title">评论区</h3>
            <p class="section-copy">把使用反馈、补充问题和学习交流留在资料旁边，后续复习时更容易回看。</p>
          </div>
        </div>

        <div class="comment-form">
          <label class="sr-only" for="material-comment">评论内容</label>
          <textarea
            id="material-comment"
            v-model="newComment"
            class="edu-input"
            placeholder="写下你的评论..."
            rows="4"
          ></textarea>
          <div class="action-row">
            <button
              type="button"
              class="edu-btn edu-btn-primary"
              :disabled="!newComment.trim() || commentLoading"
              @click="submitComment"
            >
              {{ commentLoading ? '发布中...' : '发布评论' }}
            </button>
          </div>
        </div>

        <div v-if="comments.length === 0" class="empty-panel compact-empty">
          <h3 class="empty-title">还没有评论</h3>
          <p class="empty-copy">第一条反馈会帮助后面的同学更快理解这份资料。</p>
        </div>

        <div v-else class="comment-list">
          <article v-for="comment in comments" :key="comment.id" class="comment-item">
            <div class="comment-avatar">{{ getAvatarText(comment.username) }}</div>
            <div class="comment-body">
              <div class="comment-head">
                <strong>{{ comment.username }}</strong>
                <span>{{ formatTime(comment.createdAt) }}</span>
              </div>
              <p>{{ comment.content }}</p>
            </div>
          </article>
        </div>
      </section>
    </template>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
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

const materialCategories = [
  { value: 'LECTURE_NOTE', label: '讲义课件' },
  { value: 'ASSIGNMENT', label: '作业资料' },
  { value: 'REFERENCE', label: '参考资料' },
  { value: 'EXAM', label: '考试复习' },
  { value: 'LAB', label: '实验资料' },
  { value: 'OTHER', label: '其他' }
]

const fetchMaterial = async () => {
  loading.value = true
  try {
    const response = await materialApi.getMaterialById(materialId.value, userStore.user.id)
    if (response.data.code === 200) {
      material.value = response.data.data
      await fetchComments()
    }
  } catch (error) {
    console.error('获取资料详情失败:', error)
    goBack()
  } finally {
    loading.value = false
  }
}

const fetchComments = async () => {
  try {
    const response = await materialApi.getComments(materialId.value, userStore.user.id)
    if (response.data.code === 200) {
      comments.value = response.data.data
    }
  } catch (error) {
    console.error('获取评论失败:', error)
  }
}

const handleDownload = () => {}

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
    const response = await materialApi.addComment(materialId.value, userStore.user.id, newComment.value)
    if (response.data.code === 200) {
      comments.value.unshift(response.data.data)
      newComment.value = ''
      if (material.value) {
        material.value.commentCount = Number(material.value.commentCount || 0) + 1
      }
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
      goBack()
    }
  } catch (error) {
    alert('删除失败')
  }
}

const getFileLabel = (fileType) => String(fileType || 'FILE').replace('.', '').toUpperCase()

const getCategoryLabel = (category) => {
  const matched = materialCategories.find((item) => item.value === category)
  return matched ? matched.label : '未分类'
}

const getAvatarText = (username) => (username ? username.charAt(0).toUpperCase() : 'U')

const formatDate = (dateStr) => (dateStr ? new Date(dateStr).toLocaleString('zh-CN') : '')
const formatTime = (dateStr) => (dateStr ? new Date(dateStr).toLocaleString('zh-CN') : '')

const goBack = () => {
  const target = userStore.isTeacher ? '/teacher/materials' : '/materials'
  router.push(target)
}

onMounted(fetchMaterial)
</script>

<style scoped>
.detail-heading {
  display: grid;
  grid-template-columns: auto 1fr;
  gap: 16px;
  align-items: center;
}

.file-mark {
  min-width: 72px;
  min-height: 72px;
  border-radius: 24px;
  display: grid;
  place-items: center;
  background: linear-gradient(145deg, rgba(44, 96, 214, 0.14), rgba(102, 163, 255, 0.18));
  color: #2c60d6;
  font-weight: 800;
}

.material-meta,
.tag-row,
.action-row {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.version-panel {
  padding: 16px 18px;
  border-radius: 18px;
  background: rgba(246, 248, 252, 0.92);
  border: 1px solid rgba(23, 32, 51, 0.06);
}

.version-panel h4,
.version-panel p {
  margin: 0;
}

.version-panel p {
  margin-top: 6px;
  color: var(--gray-700);
  line-height: 1.7;
}

.table-header-row {
  display: flex;
  justify-content: space-between;
  gap: var(--spacing-md);
  margin-bottom: var(--spacing-md);
}

.comment-form {
  display: grid;
  gap: 14px;
}

.compact-empty {
  margin-top: 18px;
}

.comment-list {
  display: grid;
  gap: 14px;
  margin-top: 18px;
}

.comment-item {
  display: grid;
  grid-template-columns: 44px 1fr;
  gap: 14px;
  padding: 16px 18px;
  border-radius: 20px;
  background: rgba(246, 248, 252, 0.92);
  border: 1px solid rgba(23, 32, 51, 0.06);
}

.comment-avatar {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  display: grid;
  place-items: center;
  background: rgba(44, 96, 214, 0.12);
  color: #2c60d6;
  font-weight: 800;
}

.comment-body {
  display: grid;
  gap: 6px;
}

.comment-head {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  color: var(--gray-500);
  font-size: 12px;
}

.comment-body p {
  margin: 0;
  color: var(--gray-800);
  line-height: 1.7;
}

.tag-chip {
  display: inline-flex;
  min-height: 28px;
  align-items: center;
  padding: 0 12px;
  border-radius: 999px;
  background: rgba(15, 139, 132, 0.10);
  color: #0f6962;
  font-size: 12px;
  font-weight: 700;
}

.chip.primary {
  background: rgba(44, 96, 214, 0.12);
  color: #2c60d6;
}

.chip.soft {
  background: rgba(15, 139, 132, 0.10);
  color: #0f6962;
}
</style>
