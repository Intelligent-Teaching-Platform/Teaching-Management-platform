<template>
  <div class="home-page">
    <section class="welcome-hero card">
      <div class="welcome-hero__accent" aria-hidden="true" />
      <div class="welcome-hero__body">
        <p class="welcome-hero__greeting">
          欢迎您，<span class="welcome-hero__name">{{ data.user.name || '用户' }}</span>
        </p>
        <p class="welcome-hero__tagline">数智验舱 · 智辅实验管理平台</p>
        <p class="welcome-hero__wish">祝您今天工作愉快。</p>
      </div>
    </section>

    <section class="notice-panel card">
      <header class="notice-panel__head">
        <div class="notice-panel__icon" aria-hidden="true">
          <el-icon><Bell /></el-icon>
        </div>
        <div class="notice-panel__titles">
          <h2 class="notice-panel__title">系统公告</h2>
          <p class="notice-panel__sub">最新的平台通知与课程更新会展示在这里</p>
        </div>
      </header>

      <div v-if="data.noticeData.length" class="notice-panel__list">
        <el-timeline class="home-timeline">
          <el-timeline-item
            v-for="(item, index) in data.noticeData"
            :key="index"
            :timestamp="item.time"
            placement="top"
          >
            <div class="notice-item-title">{{ item.title }}</div>
            <div class="notice-item-content">{{ item.content }}</div>
          </el-timeline-item>
        </el-timeline>
      </div>
      <div v-else class="notice-empty">
        <el-icon class="notice-empty__icon"><Bell /></el-icon>
        <p class="notice-empty__text">暂无公告，祝你学习愉快</p>
      </div>
    </section>
  </div>
</template>

<script setup>
import { reactive } from 'vue'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'
import { Bell } from '@element-plus/icons-vue'

const data = reactive({
  user: JSON.parse(localStorage.getItem('system-user') || '{}'),
  noticeData: [],
})

const loadNotice = () => {
  request.get('/notice/selectAll').then((res) => {
    if (res.code === '200') {
      data.noticeData = res.data
    } else {
      ElMessage.error(res.msg)
    }
  })
}

loadNotice()
</script>

<style scoped>
.home-page {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 16px;
  font-family: var(--font-sans);
}

.welcome-hero {
  position: relative;
  display: flex;
  flex-direction: column;
  padding: 22px 24px 22px 26px;
  overflow: hidden;
  transition:
    transform var(--duration) var(--ease-out),
    border-color var(--duration) var(--ease-out),
    box-shadow var(--duration) var(--ease-out);
}

.welcome-hero:hover {
  transform: translateY(-1px);
  border-color: var(--color-primary-muted);
  box-shadow:
    0 16px 48px -20px rgba(15, 23, 42, 0.14),
    0 0 0 1px var(--color-primary-soft);
}

@media (prefers-reduced-motion: reduce) {
  .welcome-hero:hover {
    transform: none;
  }
}

.welcome-hero__accent {
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 4px;
  background: linear-gradient(180deg, var(--color-primary) 0%, var(--color-primary-hover) 100%);
  border-radius: var(--radius-md) 0 0 var(--radius-md);
  opacity: 0.95;
}

.welcome-hero__body {
  position: relative;
  padding-left: 8px;
}

.welcome-hero__greeting {
  margin: 0;
  font-size: 1.35rem;
  font-weight: 700;
  letter-spacing: -0.02em;
  color: var(--color-text);
  line-height: 1.35;
}

.welcome-hero__name {
  color: var(--color-primary-hover);
}

.welcome-hero__tagline {
  margin: 10px 0 0;
  font-size: 14px;
  font-weight: 600;
  color: var(--color-text-muted);
  letter-spacing: 0.02em;
}

.welcome-hero__wish {
  margin: 8px 0 0;
  font-size: 13px;
  color: var(--color-text-muted);
  line-height: 1.5;
}

.notice-panel {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 18px 20px 20px;
}

.notice-panel__head {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  margin-bottom: 8px;
}

.notice-panel__icon {
  flex-shrink: 0;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
  background: var(--color-primary-soft);
  color: var(--color-primary-hover);
  font-size: 20px;
}

.notice-panel__title {
  margin: 0;
  font-size: 1.15rem;
  font-weight: 700;
  letter-spacing: -0.02em;
  color: var(--color-text);
}

.notice-panel__sub {
  margin: 6px 0 0;
  font-size: 13px;
  color: var(--color-text-muted);
  line-height: 1.5;
  max-width: 56ch;
}

.notice-panel__list {
  margin-top: 8px;
  max-height: min(560px, 68vh);
  overflow-y: auto;
  padding-right: 4px;
  scrollbar-gutter: stable;
}

.notice-item-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--color-text);
  margin-bottom: 4px;
  line-height: 1.4;
}

.notice-item-content {
  font-size: 13px;
  line-height: 1.65;
  color: var(--color-text-muted);
  white-space: pre-wrap;
}

.home-timeline :deep(.el-timeline-item__node) {
  background-color: var(--color-primary) !important;
  border-color: var(--color-primary);
}

.home-timeline :deep(.el-timeline-item__tail) {
  border-left-color: var(--color-border);
}

.home-timeline :deep(.el-timeline-item__timestamp) {
  color: var(--color-text-subtle);
  font-size: 12px;
  font-weight: 500;
}

.notice-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 36px 20px;
  text-align: center;
}

.notice-empty__icon {
  font-size: 40px;
  color: var(--color-text-subtle);
  opacity: 0.85;
}

.notice-empty__text {
  margin: 0;
  font-size: 14px;
  color: var(--color-text-muted);
}

@media (max-width: 768px) {
  .welcome-hero__greeting {
    font-size: 1.2rem;
  }
}
</style>
