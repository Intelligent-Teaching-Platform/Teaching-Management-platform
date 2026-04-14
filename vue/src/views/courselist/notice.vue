<template>
  <div class="course-notice-page">
    <header class="notice-hero card">
      <div class="hero-top">
        <div class="hero-icon" aria-hidden="true">
          <el-icon><Bell /></el-icon>
        </div>
        <div class="hero-text">
          <h1 class="hero-title">公告通知</h1>
          <p class="hero-sub">
            集中查看教学平台发布的公告，支持按标题筛选。从课程空间进入时，亦可作本课程相关的通用信息发布墙使用。
          </p>
        </div>
      </div>
      <div class="hero-toolbar">
        <el-input
          v-model="filters.title"
          class="search-input"
          placeholder="按公告标题搜索"
          clearable
          @keyup.enter="load"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-button type="primary" @click="load">查询</el-button>
        <el-button @click="resetFilters">重置</el-button>
        <router-link v-if="isAdmin" to="/notice" class="admin-link">
          <el-button>管理端编辑</el-button>
        </router-link>
      </div>
    </header>

    <div v-loading="loading" class="notice-body">
      <div v-if="!loading && !list.length" class="empty-wrap card">
        <el-empty description="暂无公告，或请调整搜索条件" />
      </div>

      <div v-else class="notice-grid" role="list" aria-label="公告列表">
        <article
          v-for="(item, index) in list"
          :key="item.id"
          class="notice-card card"
          :style="{ '--n': index }"
          role="listitem"
        >
          <div class="notice-card__accent" aria-hidden="true" />
          <div class="notice-card__head">
            <h2 class="notice-card__title">{{ item.title || '未命名公告' }}</h2>
            <time v-if="item.time" class="notice-card__time" :datetime="item.time">
              <el-icon class="time-icon"><Calendar /></el-icon>
              {{ item.time }}
            </time>
          </div>
          <p class="notice-card__excerpt">{{ excerpt(item.content) }}</p>
          <div class="notice-card__foot">
            <el-button type="primary" link @click="openDetail(item)">查看全文</el-button>
          </div>
        </article>
      </div>
    </div>

    <footer v-if="total > 0" class="notice-footer card">
      <el-pagination
        background
        layout="total, prev, pager, next, sizes"
        :page-sizes="[6, 12, 24]"
        v-model:page-size="pagination.size"
        v-model:current-page="pagination.current"
        :total="total"
        @current-change="load"
        @size-change="onSizeChange"
      />
    </footer>

    <el-dialog
      v-model="detailOpen"
      :title="current?.title || '公告详情'"
      width="min(560px, 92vw)"
      class="notice-detail-dialog"
      destroy-on-close
    >
      <p v-if="current?.time" class="detail-meta">
        <el-icon><Calendar /></el-icon>
        {{ current.time }}
      </p>
      <div class="detail-content">{{ current?.content || '暂无正文' }}</div>
      <template #footer>
        <el-button type="primary" @click="detailOpen = false">关 闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import request from '@/utils/request'
import { Bell, Search, Calendar } from '@element-plus/icons-vue'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const detailOpen = ref(false)
const current = ref(null)

const filters = reactive({
  title: '',
})

const pagination = reactive({
  current: 1,
  size: 12,
})

const userRole = computed(() => {
  try {
    const u = JSON.parse(localStorage.getItem('system-user') || '{}')
    const r = u?.role
    if (r == null || String(r).trim() === '') return ''
    return String(r).trim().toUpperCase()
  } catch {
    return ''
  }
})

const isAdmin = computed(() => userRole.value === 'ADMIN')

function excerpt(text) {
  if (text == null || String(text).trim() === '') return '（无摘要，请点击查看全文）'
  const s = String(text).trim().replace(/\s+/g, ' ')
  if (s.length <= 160) return s
  return s.slice(0, 160) + '…'
}

function load() {
  loading.value = true
  request
    .get('/notice/selectPage', {
      params: {
        pageNum: pagination.current,
        pageSize: pagination.size,
        title: filters.title?.trim() || undefined,
      },
    })
    .then((res) => {
      if (String(res?.code) !== '200' && res?.code !== 200) {
        list.value = []
        total.value = 0
        return
      }
      list.value = res.data?.list || []
      total.value = res.data?.total ?? 0
    })
    .finally(() => {
      loading.value = false
    })
}

function resetFilters() {
  filters.title = ''
  pagination.current = 1
  load()
}

function onSizeChange() {
  pagination.current = 1
  load()
}

function openDetail(row) {
  current.value = row ? { ...row } : null
  detailOpen.value = true
}

onMounted(load)
</script>

<style scoped>
.course-notice-page {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 16px;
  font-family: var(--font-sans);
}

.notice-hero {
  padding: 20px 22px;
}

.hero-top {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  margin-bottom: 18px;
}

.hero-icon {
  flex-shrink: 0;
  width: 52px;
  height: 52px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--color-primary-soft);
  color: var(--color-primary-hover);
  font-size: 26px;
}

.hero-title {
  margin: 0;
  font-size: 1.4rem;
  font-weight: 700;
  letter-spacing: -0.02em;
  color: var(--color-text);
}

.hero-sub {
  margin: 8px 0 0;
  font-size: 13px;
  line-height: 1.55;
  color: var(--color-text-muted);
  max-width: 64ch;
}

.hero-toolbar {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 10px;
}

.search-input {
  width: min(320px, 100%);
}

.admin-link {
  margin-left: auto;
  text-decoration: none;
}

.hero-toolbar .admin-link :deep(.el-button) {
  border-color: var(--color-border);
}

.notice-body {
  min-height: 200px;
}

.empty-wrap {
  padding: 48px 24px;
}

.notice-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 16px;
}

.notice-card {
  position: relative;
  display: flex;
  flex-direction: column;
  padding: 18px 18px 14px;
  overflow: hidden;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-soft);
  transition:
    transform var(--duration, 220ms) var(--ease-out, cubic-bezier(0.16, 1, 0.3, 1)),
    border-color var(--duration, 220ms) var(--ease-out, cubic-bezier(0.16, 1, 0.3, 1)),
    box-shadow var(--duration, 220ms) var(--ease-out, cubic-bezier(0.16, 1, 0.3, 1));
  animation: notice-in var(--duration, 220ms) var(--ease-out, cubic-bezier(0.16, 1, 0.3, 1)) backwards;
  animation-delay: calc(min(var(--n, 0), 12) * 45ms);
}

@media (prefers-reduced-motion: reduce) {
  .notice-card {
    animation: none;
  }
}

.notice-card:hover {
  transform: translateY(-2px);
  border-color: var(--color-primary-muted);
  box-shadow:
    0 16px 48px -20px rgba(15, 23, 42, 0.14),
    0 0 0 1px var(--color-primary-soft);
}

.notice-card__accent {
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 4px;
  background: linear-gradient(180deg, var(--color-primary) 0%, var(--color-primary-hover) 100%);
  border-radius: var(--radius-md) 0 0 var(--radius-md);
  opacity: 0.95;
}

.notice-card__head {
  padding-left: 10px;
}

.notice-card__title {
  margin: 0;
  font-size: 1.05rem;
  font-weight: 700;
  line-height: 1.35;
  color: var(--color-text);
  letter-spacing: -0.01em;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.notice-card__time {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  margin-top: 10px;
  font-size: 12px;
  font-weight: 600;
  color: var(--color-primary-hover);
  background: var(--color-primary-soft);
  padding: 4px 10px;
  border-radius: 999px;
  font-variant-numeric: tabular-nums;
}

.time-icon {
  font-size: 14px;
}

.notice-card__excerpt {
  margin: 14px 0 0;
  padding-left: 10px;
  font-size: 13px;
  line-height: 1.6;
  color: var(--color-text-muted);
  flex: 1;
  display: -webkit-box;
  -webkit-line-clamp: 4;
  -webkit-box-orient: vertical;
  overflow: hidden;
  word-break: break-word;
}

.notice-card__foot {
  margin-top: 14px;
  padding-top: 12px;
  border-top: 1px solid var(--color-border);
  padding-left: 6px;
}

.notice-footer {
  display: flex;
  justify-content: center;
  padding: 14px 18px;
}

.detail-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0 0 14px;
  font-size: 13px;
  color: var(--color-text-muted);
}

.detail-content {
  font-size: 14px;
  line-height: 1.75;
  color: var(--color-text);
  white-space: pre-wrap;
  word-break: break-word;
}

@keyframes notice-in {
  from {
    opacity: 0;
    transform: translateY(8px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
