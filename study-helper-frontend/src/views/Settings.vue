<template>
  <div class="page">
    <header class="header">
      <h1 class="title">设置</h1>
      <router-link to="/" class="back">返回</router-link>
    </header>

    <main class="content">
      <div class="card">
        <div class="row">
          <span class="label">主题</span>
          <span class="value">默认</span>
        </div>
        <div class="row">
          <span class="label">通知</span>
          <span class="value">默认</span>
        </div>
      </div>
    </main>
  </div>
</template>

<style scoped>
.page {
  min-height: 100vh;
  padding: 1rem;
  background: #f7f9fc;
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  max-width: 980px;
  margin: 0 auto 1rem auto;
}

.title {
  margin: 0;
  font-size: 1.25rem;
  font-weight: 700;
  color: #1f2937;
}

.back {
  text-decoration: none;
  color: #2563eb;
  font-weight: 600;
}

.content {
  max-width: 980px;
  margin: 0 auto;
}

.card {
  background: #fff;
  border-radius: 12px;
  padding: 1rem;
  box-shadow: 0 6px 18px rgba(15, 23, 42, 0.06);
}

.row {
  display: flex;
  justify-content: space-between;
  padding: 0.75rem 0;
  border-bottom: 1px solid #eef2f7;
}

.row:last-child {
  border-bottom: 0;
}

.label {
  color: #374151;
  font-weight: 600;
}

.value {
  color: #6b7280;
}
</style>
