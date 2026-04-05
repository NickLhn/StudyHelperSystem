<template>
  <div class="landing-page">
    <EduNavbar />

    <main class="landing-main">
      <section class="page-container hero-shell">
        <div class="hero-copy">
          <span class="page-eyebrow">Learning Operating System</span>
          <h1 class="hero-title">学习辅助系统</h1>
          <p class="hero-subtitle">
            把课程学习、资料沉淀、在线测验、自动批改与学习统计整合成一套清晰的教学协作空间。
            用户一进入系统，先看到的是产品价值，而不是后台页面。
          </p>

          <div class="hero-actions">
            <router-link v-if="!userStore.isLoggedIn" to="/register" class="edu-btn edu-btn-primary">
              立即开始
            </router-link>
            <router-link v-if="!userStore.isLoggedIn" to="/login" class="edu-btn edu-btn-secondary">
              登录系统
            </router-link>
            <router-link v-if="userStore.isLoggedIn" :to="workspacePath" class="edu-btn edu-btn-primary">
              进入我的工作区
            </router-link>
            <router-link v-if="userStore.isLoggedIn" to="/profile" class="edu-btn edu-btn-secondary">
              查看个人中心
            </router-link>
          </div>

          <div class="hero-metrics">
            <div class="metric-item">
              <strong>课程</strong>
              <span>学习、教学与管理统一组织</span>
            </div>
            <div class="metric-item">
              <strong>测验</strong>
              <span>支持在线答题与自动批改扩展</span>
            </div>
            <div class="metric-item">
              <strong>统计</strong>
              <span>用更直观的趋势面板展示学习效果</span>
            </div>
          </div>
        </div>

        <div class="hero-preview">
          <article class="preview-card preview-primary">
            <span class="preview-tag">学生端</span>
            <h3>课程、资料、统计</h3>
            <p>从加入课程到下载资料，再到查看学习趋势，全部收束到一套更顺手的学习界面里。</p>
          </article>

          <article class="preview-card preview-secondary">
            <span class="preview-tag">教师端</span>
            <h3>课程、任务、测验、资料</h3>
            <p>教学工作台已经围绕教师日常高频任务重构，后续接入作业自动批改会很自然。</p>
          </article>

          <article class="preview-card preview-tertiary">
            <span class="preview-tag">管理员端</span>
            <h3>用户、内容、邀请码</h3>
            <p>后台界面不再零散，平台运营信息已经集中成统一的治理视图。</p>
          </article>
        </div>
      </section>

      <section class="page-container feature-section">
        <div class="section-heading">
          <span class="page-eyebrow">Core Modules</span>
          <h2 class="section-title">为什么这个系统不只是一个“课程管理页”</h2>
          <p class="section-copy">
            首页改成落地页以后，产品能力会先被用户理解，再进入具体角色工作区。
          </p>
        </div>

        <div class="card-grid cards-4">
          <article class="feature-card">
            <strong>课程学习</strong>
            <span>按角色进入课程、组织教学信息和学习资料。</span>
          </article>
          <article class="feature-card">
            <strong>资料中心</strong>
            <span>支持上传、下载、搜索、收藏与内容沉淀。</span>
          </article>
          <article class="feature-card">
            <strong>在线测验</strong>
            <span>创建题目、发布测验，并为自动批改预留能力。</span>
          </article>
          <article class="feature-card">
            <strong>学习分析</strong>
            <span>把学习时长、趋势和课程分布做成更易读的图表页面。</span>
          </article>
        </div>
      </section>

      <section class="page-container journey-section">
        <div class="journey-grid">
          <article class="journey-card">
            <span class="page-eyebrow">Step 01</span>
            <h3>注册并选择身份</h3>
            <p>学生和教师在统一入口注册，减少用户第一次进入系统时的理解成本。</p>
          </article>
          <article class="journey-card">
            <span class="page-eyebrow">Step 02</span>
            <h3>进入专属工作区</h3>
            <p>登录后自动进入学生端、教师端或管理员端，而不是停留在公共页面。</p>
          </article>
          <article class="journey-card">
            <span class="page-eyebrow">Step 03</span>
            <h3>围绕课程展开完整流程</h3>
            <p>后续你要继续做作业与自动批改时，也可以自然接到这一套首页叙事里。</p>
          </article>
        </div>
      </section>
    </main>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import EduNavbar from '../components/EduNavbar.vue'
import { useUserStore } from '../stores/user'

const userStore = useUserStore()

const workspacePath = computed(() => {
  if (userStore.isAdmin) return '/admin'
  if (userStore.isTeacher) return '/teacher'
  if (userStore.isStudent) return '/student'
  return '/'
})
</script>

<style scoped>
.landing-page {
  min-height: 100vh;
}

.landing-main {
  display: grid;
  gap: 48px;
  padding: 18px 0 56px;
}

.hero-shell {
  display: grid;
  grid-template-columns: minmax(0, 1.15fr) minmax(320px, 0.85fr);
  gap: 28px;
  align-items: stretch;
  padding: 0 clamp(18px, 3vw, 32px);
}

.hero-copy {
  padding: clamp(28px, 4vw, 48px);
  border-radius: 34px;
  background:
    radial-gradient(circle at top left, rgba(111, 152, 255, 0.24), transparent 36%),
    radial-gradient(circle at bottom right, rgba(15, 139, 132, 0.14), transparent 34%),
    linear-gradient(160deg, rgba(255, 255, 255, 0.95), rgba(246, 241, 231, 0.88));
  border: 1px solid rgba(23, 32, 51, 0.08);
  box-shadow: var(--shadow-md);
  display: grid;
  gap: 22px;
}

.hero-title {
  margin: 6px 0 0;
  font-size: clamp(46px, 7vw, 76px);
  line-height: 0.98;
}

.hero-subtitle {
  max-width: 62ch;
  color: var(--gray-700);
  font-size: 17px;
  line-height: 1.85;
}

.hero-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.hero-metrics {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14px;
}

.metric-item {
  display: grid;
  gap: 4px;
  padding: 16px 18px;
  border-radius: 22px;
  background: rgba(255, 255, 255, 0.72);
  border: 1px solid rgba(23, 32, 51, 0.06);
}

.metric-item span {
  color: var(--gray-500);
  font-size: 13px;
  line-height: 1.7;
}

.hero-preview {
  display: grid;
  gap: 16px;
}

.preview-card {
  min-height: 170px;
  padding: 24px;
  border-radius: 28px;
  border: 1px solid rgba(23, 32, 51, 0.08);
  box-shadow: var(--shadow-sm);
  display: grid;
  align-content: start;
  gap: 10px;
}

.preview-primary {
  background: linear-gradient(160deg, rgba(255, 255, 255, 0.95), rgba(231, 240, 255, 0.92));
}

.preview-secondary {
  background: linear-gradient(160deg, rgba(255, 255, 255, 0.95), rgba(236, 247, 244, 0.92));
}

.preview-tertiary {
  background: linear-gradient(160deg, rgba(255, 255, 255, 0.95), rgba(245, 239, 230, 0.92));
}

.preview-tag {
  display: inline-flex;
  width: fit-content;
  min-height: 28px;
  align-items: center;
  padding: 0 12px;
  border-radius: 999px;
  background: rgba(23, 32, 51, 0.06);
  color: var(--primary-dark);
  font-size: 12px;
  font-weight: var(--font-weight-black);
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.preview-card p {
  color: var(--gray-700);
  line-height: 1.75;
}

.feature-section,
.journey-section {
  padding: 0 clamp(18px, 3vw, 32px);
}

.section-heading {
  max-width: 760px;
  margin-bottom: 22px;
}

.feature-card,
.journey-card {
  display: grid;
  gap: 8px;
  padding: 22px;
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.82);
  border: 1px solid rgba(23, 32, 51, 0.08);
  box-shadow: var(--shadow-sm);
}

.feature-card span,
.journey-card p {
  color: var(--gray-500);
  line-height: 1.75;
}

.journey-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 18px;
}

@media (max-width: 960px) {
  .hero-shell,
  .journey-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 720px) {
  .hero-metrics {
    grid-template-columns: 1fr;
  }
}
</style>
