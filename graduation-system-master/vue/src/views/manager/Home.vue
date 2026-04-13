<template>
  <div class="home-page">
    <div class="home-top">
      <section class="home-hero card" aria-labelledby="home-hero-title">
        <div class="home-hero__mesh" aria-hidden="true" />
        <div class="home-hero__grid" aria-hidden="true" />
        <div class="home-hero__content">
          <p class="home-hero__eyebrow">工作台 · 数智门户</p>
          <h1 id="home-hero-title" class="home-hero__title">一站式实验教学协同</h1>
          <p class="home-hero__lead">
            将公告、课程与资源入口集中在一屏，减少跳转成本，适合日常开机后的第一站。
          </p>
          <ul class="home-hero__bullets">
            <li>实验资源与课内任务在同一平台维护</li>
            <li>公告时间线按发布时间聚合展示</li>
            <li>常用模块可一键抵达，适配管理员 / 教师 / 学生</li>
          </ul>
          <div class="home-hero__actions">
            <el-button type="primary" class="home-hero__cta" @click="go('/dashboard')">
              打开数据驾驶舱
            </el-button>
            <el-button class="home-hero__ghost" @click="go('/notice')">浏览全部公告</el-button>
          </div>
        </div>
      </section>

      <aside class="home-profile card" aria-labelledby="home-profile-name">
        <div class="home-profile__header">
          <img class="home-profile__avatar" :src="avatarSrc" alt="" width="64" height="64" />
          <div class="home-profile__meta">
            <h2 id="home-profile-name" class="home-profile__name">{{ displayName }}</h2>
            <p class="home-profile__id">账号 {{ data.user.username || '—' }}</p>
            <span class="home-profile__role">{{ roleLabel }}</span>
          </div>
        </div>
        <p class="home-profile__hint">上次登录后若有资料变更，可在个人中心同步。</p>
        <div class="home-profile__actions">
          <el-button type="primary" plain class="home-profile__btn" @click="go(personPath)">
            个人中心
          </el-button>
          <el-button class="home-profile__btn home-profile__btn--warm" @click="go('/password')">
            账号安全
          </el-button>
        </div>
      </aside>
    </div>

    <el-row class="home-bottom" :gutter="16">
      <el-col :xs="24" :lg="16">
        <section class="home-main card" aria-label="日程与公告">
          <el-tabs v-model="mainTab" class="home-tabs">
            <el-tab-pane label="工作台日历" name="calendar">
              <div class="home-calendar-wrap">
                <el-calendar v-model="calendarDate" class="home-calendar">
                  <template #date-cell="{ data }">
                    <div class="home-cal-cell">
                      <span class="home-cal-cell__day">{{ dayOfCell(data.day) }}</span>
                      <span v-if="hasNoticeOnDay(data.day)" class="home-cal-cell__dot" aria-hidden="true" />
                    </div>
                  </template>
                </el-calendar>
              </div>
              <p class="home-calendar__hint">
                有公告发布的日期会在格内显示标记；点击日期可在下方摘要中看到当日首条公告标题。
              </p>
              <div v-if="selectedDayNoticeTitle" class="home-day-summary">
                <span class="home-day-summary__label">当日摘要</span>
                <span class="home-day-summary__text">{{ selectedDayNoticeTitle }}</span>
              </div>
            </el-tab-pane>
            <el-tab-pane label="系统公告" name="notice">
              <header class="home-notice-head">
                <div class="home-notice-head__icon" aria-hidden="true">
                  <el-icon><Bell /></el-icon>
                </div>
                <div>
                  <h3 class="home-notice-head__title">最新通知</h3>
                  <p class="home-notice-head__sub">平台通知与课程更新</p>
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
            </el-tab-pane>
          </el-tabs>
        </section>
      </el-col>
      <el-col :xs="24" :lg="8">
        <div class="home-side">
          <section class="home-stats card" aria-label="关键概览">
            <div class="home-stat">
              <div class="home-stat__icon home-stat__icon--teal" aria-hidden="true">
                <el-icon><Bell /></el-icon>
              </div>
              <div class="home-stat__body">
                <div class="home-stat__value">{{ data.noticeData.length }}</div>
                <div class="home-stat__label">公告条数</div>
              </div>
            </div>
            <div class="home-stat">
              <div class="home-stat__icon home-stat__icon--sky" aria-hidden="true">
                <el-icon><Grid /></el-icon>
              </div>
              <div class="home-stat__body">
                <div class="home-stat__value">{{ quickLinkCount }}</div>
                <div class="home-stat__label">快捷入口</div>
              </div>
            </div>
            <div class="home-stat">
              <div class="home-stat__icon home-stat__icon--amber" aria-hidden="true">
                <el-icon><Calendar /></el-icon>
              </div>
              <div class="home-stat__body">
                <div class="home-stat__value">{{ todayLabel }}</div>
                <div class="home-stat__label">今日日期</div>
              </div>
            </div>
          </section>

          <section class="home-quick card" aria-label="快捷入口">
            <h3 class="home-quick__title">常用功能</h3>
            <p class="home-quick__sub">高频模块固定在此，支持键盘与触控操作。</p>
            <div class="home-quick__grid">
              <button
                v-for="item in quickLinks"
                :key="item.path"
                type="button"
                class="home-tile"
                :class="item.tone"
                @click="go(item.path)"
              >
                <el-icon class="home-tile__icon" aria-hidden="true"><component :is="item.icon" /></el-icon>
                <span class="home-tile__label">{{ item.label }}</span>
              </button>
            </div>
          </section>

          <div v-if="tickerText" class="home-ticker card" role="status" aria-live="polite">
            <span class="home-ticker__tag">动态</span>
            <span class="home-ticker__text">{{ tickerText }}</span>
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { reactive, ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'
import { Bell, Grid, Calendar, DataBoard, Reading, FolderOpened, Notebook } from '@element-plus/icons-vue'
import { resolveAvatarUrl } from '@/utils/appConfig'

const router = useRouter()
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

const data = reactive({
  user: JSON.parse(localStorage.getItem('system-user') || '{}'),
  noticeData: [],
})

const mainTab = ref('calendar')
const calendarDate = ref(new Date())

const avatarSrc = computed(() => resolveAvatarUrl(data.user?.avatar) || defaultAvatar)
const displayName = computed(() => data.user.name || '用户')

const roleLabel = computed(() => {
  const r = data.user.role
  if (r === 'ADMIN') return '管理员'
  if (r === 'TEACHER') return '教师'
  if (r === 'STUDENT') return '学生'
  return '访客'
})

const personPath = computed(() => {
  const r = data.user.role
  if (r === 'TEACHER') return '/tPerson'
  if (r === 'STUDENT') return '/sPerson'
  return '/person'
})

const courseEntry = computed(() => {
  const r = data.user.role
  if (r === 'STUDENT') return { path: '/myCourse', label: '我的课程', icon: Notebook, tone: 'home-tile--course' }
  if (r === 'TEACHER') return { path: '/myTeach', label: '我的授课', icon: Notebook, tone: 'home-tile--course' }
  return { path: '/course', label: '课程管理', icon: Notebook, tone: 'home-tile--course' }
})

const quickLinks = computed(() => [
  { path: '/dashboard', label: '数据驾驶舱', icon: DataBoard, tone: 'home-tile--teal' },
  { path: '/notice', label: '通知公告', icon: Reading, tone: 'home-tile--sky' },
  { path: '/resource', label: '资源中心', icon: FolderOpened, tone: 'home-tile--amber' },
  courseEntry.value,
])

const quickLinkCount = computed(() => quickLinks.value.length)

const todayLabel = computed(() => {
  const d = new Date()
  return `${String(d.getMonth() + 1).padStart(2, '0')}/${String(d.getDate()).padStart(2, '0')}`
})

const noticeDaySet = computed(() => {
  const set = new Set()
  for (const n of data.noticeData) {
    const key = normalizeDayKey(n.time)
    if (key) set.add(key)
  }
  return set
})

const tickerText = computed(() => {
  const first = data.noticeData[0]
  if (!first) return ''
  return `最新公告：${first.title}`
})

const selectedDayKey = computed(() => normalizeDayKey(calendarDate.value))

const selectedDayNoticeTitle = computed(() => {
  const key = selectedDayKey.value
  if (!key) return ''
  const match = data.noticeData.find((n) => normalizeDayKey(n.time) === key)
  return match ? match.title : ''
})

const go = (path) => {
  router.push(path)
}

const normalizeDayKey = (raw) => {
  if (raw == null || raw === '') return ''
  const s = String(raw).trim()
  const iso = s.match(/^(\d{4})-(\d{2})-(\d{2})/)
  if (iso) return `${iso[1]}-${iso[2]}-${iso[3]}`
  const t = Date.parse(s.replace(/-/g, '/'))
  if (Number.isNaN(t)) return ''
  const d = new Date(t)
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${y}-${m}-${day}`
}

const dayOfCell = (dayStr) => {
  if (!dayStr || typeof dayStr !== 'string') return ''
  const parts = dayStr.split('-')
  return parts.length >= 3 ? parts[2] : dayStr
}

const hasNoticeOnDay = (dayStr) => noticeDaySet.value.has(dayStr)

const loadNotice = () => {
  request.get('/notice/selectAll').then((res) => {
    if (res.code === '200') {
      data.noticeData = res.data || []
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

.home-top {
  display: grid;
  grid-template-columns: 1fr minmax(260px, 320px);
  gap: 16px;
  align-items: stretch;
}

@media (max-width: 960px) {
  .home-top {
    grid-template-columns: 1fr;
  }
}

/* —— Hero —— */
.home-hero {
  position: relative;
  overflow: hidden;
  padding: 24px 26px 26px;
  min-height: 200px;
  transition:
    transform var(--duration) var(--ease-out),
    border-color var(--duration) var(--ease-out),
    box-shadow var(--duration) var(--ease-out);
}

.home-hero:hover {
  transform: translateY(-1px);
  border-color: var(--color-primary-muted);
  box-shadow:
    0 16px 48px -20px rgba(15, 23, 42, 0.14),
    0 0 0 1px var(--color-primary-soft);
}

@media (prefers-reduced-motion: reduce) {
  .home-hero:hover {
    transform: none;
  }
}

.home-hero__mesh {
  position: absolute;
  inset: -40% -20% auto -20%;
  height: 140%;
  background:
    radial-gradient(ellipse 80% 60% at 20% 20%, var(--color-primary-soft), transparent 55%),
    radial-gradient(ellipse 70% 50% at 85% 30%, rgba(14, 165, 233, 0.12), transparent 50%),
    radial-gradient(ellipse 50% 40% at 60% 90%, rgba(217, 119, 6, 0.08), transparent 45%);
  pointer-events: none;
}

.home-hero__grid {
  position: absolute;
  inset: 0;
  opacity: 0.22;
  background-image:
    linear-gradient(rgba(15, 23, 42, 0.06) 1px, transparent 1px),
    linear-gradient(90deg, rgba(15, 23, 42, 0.06) 1px, transparent 1px);
  background-size: 24px 24px;
  pointer-events: none;
  mask-image: linear-gradient(105deg, black 35%, transparent 78%);
}

.home-hero__content {
  position: relative;
  max-width: min(52ch, 100%);
}

.home-hero__eyebrow {
  margin: 0 0 8px;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  color: var(--color-primary-hover);
}

.home-hero__title {
  margin: 0;
  font-size: clamp(1.35rem, 2.2vw, 1.75rem);
  font-weight: 800;
  letter-spacing: -0.03em;
  line-height: 1.2;
  color: var(--color-text);
}

.home-hero__lead {
  margin: 12px 0 0;
  font-size: 14px;
  line-height: 1.6;
  color: var(--color-text-muted);
  max-width: 50ch;
}

.home-hero__bullets {
  margin: 14px 0 0;
  padding-left: 1.15rem;
  font-size: 13px;
  line-height: 1.65;
  color: var(--color-text-muted);
}

.home-hero__bullets li {
  margin-bottom: 4px;
}

.home-hero__actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 18px;
}

.home-hero__cta {
  border-radius: var(--radius-sm);
}

.home-hero__ghost {
  border-radius: var(--radius-sm);
  border-color: var(--color-border-strong);
  background: rgba(255, 255, 255, 0.72);
}

.home-hero__ghost:hover {
  border-color: var(--color-primary-muted);
  color: var(--color-primary-hover);
}

/* —— Profile —— */
.home-profile {
  display: flex;
  flex-direction: column;
  padding: 20px;
  gap: 12px;
}

.home-profile__header {
  display: flex;
  gap: 14px;
  align-items: flex-start;
}

.home-profile__avatar {
  width: 64px;
  height: 64px;
  border-radius: 14px;
  object-fit: cover;
  border: 1px solid var(--color-border);
  flex-shrink: 0;
}

.home-profile__name {
  margin: 0;
  font-size: 1.15rem;
  font-weight: 800;
  letter-spacing: -0.02em;
  color: var(--color-text);
}

.home-profile__id {
  margin: 4px 0 0;
  font-size: 12px;
  color: var(--color-text-subtle);
  font-family: var(--font-mono);
}

.home-profile__role {
  display: inline-flex;
  margin-top: 8px;
  padding: 2px 10px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;
  color: var(--color-primary-hover);
  background: var(--color-primary-soft);
  border: 1px solid var(--color-primary-muted);
}

.home-profile__hint {
  margin: 0;
  font-size: 12px;
  line-height: 1.5;
  color: var(--color-text-muted);
}

.home-profile__actions {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
  margin-top: auto;
}

.home-profile__btn {
  border-radius: var(--radius-sm);
}

.home-profile__btn--warm {
  --el-button-hover-bg-color: rgba(217, 119, 6, 0.12);
  --el-button-hover-border-color: rgba(217, 119, 6, 0.35);
  --el-button-hover-text-color: #b45309;
}

/* —— Bottom —— */
.home-bottom {
  margin: 0 !important;
  flex: 1;
  min-height: 0;
}

.home-main {
  padding: 0 4px 16px;
  min-height: 520px;
  display: flex;
  flex-direction: column;
}

.home-tabs {
  width: 100%;
}

.home-tabs :deep(.el-tabs__header) {
  margin: 0 12px;
  padding-top: 12px;
}

.home-tabs :deep(.el-tabs__nav-wrap::after) {
  height: 1px;
  background-color: var(--color-border);
}

.home-tabs :deep(.el-tabs__item) {
  font-weight: 600;
  color: var(--color-text-muted);
}

.home-tabs :deep(.el-tabs__item.is-active) {
  color: var(--color-primary-hover);
}

.home-tabs :deep(.el-tabs__active-bar) {
  background-color: var(--color-primary);
  height: 3px;
  border-radius: 3px 3px 0 0;
}

.home-calendar-wrap {
  padding: 8px 12px 0;
}

.home-calendar :deep(.el-calendar__header) {
  padding: 8px 4px 12px;
  border-bottom: 1px solid var(--color-border);
}

.home-calendar :deep(.el-calendar__body) {
  padding: 8px 0 12px;
}

.home-calendar :deep(.el-calendar-table .el-calendar-day) {
  height: 64px;
  padding: 4px;
  transition: background-color var(--duration) var(--ease-out);
}

.home-calendar :deep(.el-calendar-table td.is-selected .el-calendar-day) {
  background-color: var(--color-primary-soft);
}

.home-cal-cell {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-start;
  gap: 6px;
  height: 100%;
}

.home-cal-cell__day {
  font-size: 14px;
  font-weight: 600;
  color: var(--color-text);
}

.home-cal-cell__dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--color-primary);
  box-shadow: 0 0 0 2px var(--color-primary-soft);
}

.home-calendar__hint {
  margin: 0 16px 12px;
  font-size: 12px;
  line-height: 1.55;
  color: var(--color-text-subtle);
}

.home-day-summary {
  margin: 0 16px 8px;
  padding: 10px 12px;
  border-radius: var(--radius-sm);
  background: var(--color-bg-app);
  border: 1px solid var(--color-border);
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: baseline;
}

.home-day-summary__label {
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.08em;
  color: var(--color-text-subtle);
}

.home-day-summary__text {
  font-size: 13px;
  font-weight: 600;
  color: var(--color-text);
}

.home-notice-head {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 12px 16px 8px;
}

.home-notice-head__icon {
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

.home-notice-head__title {
  margin: 0;
  font-size: 1.05rem;
  font-weight: 800;
  color: var(--color-text);
}

.home-notice-head__sub {
  margin: 4px 0 0;
  font-size: 13px;
  color: var(--color-text-muted);
}

.notice-panel__list {
  margin-top: 4px;
  max-height: min(480px, 58vh);
  overflow-y: auto;
  padding: 0 12px 12px;
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

/* —— Side —— */
.home-side {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.home-stats {
  display: grid;
  grid-template-columns: 1fr;
  gap: 12px;
  padding: 16px;
}

@media (min-width: 400px) and (max-width: 1199px) {
  .home-stats {
    grid-template-columns: repeat(3, 1fr);
  }
}

.home-stat {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 12px;
  border-radius: var(--radius-sm);
  border: 1px solid var(--color-border);
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.96), var(--color-bg-app));
}

.home-stat__icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  flex-shrink: 0;
}

.home-stat__icon--teal {
  background: var(--color-primary-soft);
  color: var(--color-primary-hover);
}

.home-stat__icon--sky {
  background: rgba(14, 165, 233, 0.12);
  color: #0369a1;
}

.home-stat__icon--amber {
  background: rgba(217, 119, 6, 0.12);
  color: #b45309;
}

.home-stat__value {
  font-size: 1.35rem;
  font-weight: 800;
  letter-spacing: -0.02em;
  font-variant-numeric: tabular-nums;
  color: var(--color-text);
  line-height: 1.1;
}

.home-stat__label {
  font-size: 12px;
  color: var(--color-text-muted);
  margin-top: 2px;
}

.home-quick {
  padding: 16px;
}

.home-quick__title {
  margin: 0;
  font-size: 1rem;
  font-weight: 800;
  letter-spacing: -0.02em;
  color: var(--color-text);
}

.home-quick__sub {
  margin: 6px 0 14px;
  font-size: 12px;
  color: var(--color-text-muted);
  line-height: 1.5;
}

.home-quick__grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
}

.home-tile {
  border: none;
  border-radius: var(--radius-sm);
  min-height: 72px;
  padding: 12px 10px;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  justify-content: center;
  gap: 8px;
  cursor: pointer;
  text-align: left;
  color: #fff;
  font: inherit;
  transition:
    transform var(--duration) var(--ease-out),
    filter var(--duration) var(--ease-out);
  box-shadow: 0 10px 24px -16px rgba(15, 23, 42, 0.35);
}

.home-tile:focus-visible {
  outline: 2px solid var(--color-primary);
  outline-offset: 2px;
}

.home-tile:hover {
  transform: translateY(-2px);
  filter: brightness(1.03);
}

.home-tile:active {
  transform: translateY(0) scale(0.98);
}

@media (prefers-reduced-motion: reduce) {
  .home-tile:hover,
  .home-tile:active {
    transform: none;
  }
}

.home-tile__icon {
  font-size: 22px;
  opacity: 0.95;
}

.home-tile__label {
  font-size: 13px;
  font-weight: 700;
  letter-spacing: 0.01em;
}

.home-tile--teal {
  background: linear-gradient(135deg, var(--color-primary) 0%, #0f766e 100%);
}

.home-tile--sky {
  background: linear-gradient(135deg, #0284c7 0%, #0ea5e9 100%);
}

.home-tile--amber {
  background: linear-gradient(135deg, #c2410c 0%, #ea580c 100%);
}

.home-tile--course {
  background: linear-gradient(135deg, #0f766e 0%, #0d9488 55%, #115e59 100%);
}

.home-ticker {
  padding: 12px 14px;
  display: flex;
  align-items: flex-start;
  gap: 10px;
  font-size: 13px;
  line-height: 1.5;
  color: var(--color-text-muted);
}

.home-ticker__tag {
  flex-shrink: 0;
  font-size: 11px;
  font-weight: 800;
  letter-spacing: 0.06em;
  padding: 2px 8px;
  border-radius: 6px;
  background: var(--color-primary-soft);
  color: var(--color-primary-hover);
}
</style>
