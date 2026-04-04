<template>
  <div class="stu-course-page">
    <div class="schedule-card">
      <div class="toolbar">
        <div class="toolbar-left week-nav">
          <el-button
            :type="weekOffset.n < 0 ? 'primary' : 'default'"
            @click="shiftWeek(-1)"
          >
            上周
          </el-button>
          <el-button
            :type="weekOffset.n === 0 ? 'primary' : 'default'"
            @click="goThisWeek"
          >
            本周
          </el-button>
          <el-button
            :type="weekOffset.n > 0 ? 'primary' : 'default'"
            @click="shiftWeek(1)"
          >
            下周
          </el-button>
          <span class="range-label">上课时间：</span>
          <span class="range-value">{{ weekRangeText }}</span>
        </div>
        <div class="toolbar-right">
          <el-input
            v-model="data.searchParams.name"
            placeholder="搜索课程名称"
            clearable
            class="search-input"
            @keyup.enter="loadCourseData"
          />
          <el-button type="primary" @click="loadCourseData">查询</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </div>
      </div>

      <div class="schedule-scroll" v-loading="data.loading">
        <div class="schedule-grid">
          <div class="corner-cell" />
          <div
            v-for="(col, di) in weekColumns"
            :key="'head-' + di"
            class="day-header"
          >
            <div class="day-name">周{{ DAY_LABELS[di] }}</div>
            <div class="day-date">({{ col.md }})</div>
            <div class="day-count">共 {{ countInColumn(di) }} 门</div>
          </div>

          <div class="time-rail">
            <div
              v-for="h in bodyHours"
              :key="'t-' + h"
              class="time-tick"
            >
              {{ String(h).padStart(2, '0') }}:00
            </div>
          </div>

          <div
            v-for="(col, di) in weekColumns"
            :key="'col-' + di"
            class="day-column"
          >
            <div class="day-body">
              <div
                v-for="h in bodyHours"
                :key="'line-' + di + '-' + h"
                class="hour-line"
              />
              <div
                v-for="(course, ci) in coursesInColumn(di)"
                :key="course.id + '-' + ci"
                class="course-block"
                :class="'tone-' + (Number(course.id) % 6)"
                :style="blockStyle(course)"
              >
                <div class="block-time">{{ blockTimeText(course) }}</div>
                <div class="block-title">{{ course.name }}</div>
                <div class="block-meta">{{ course.teacherName || '教师待定' }}</div>
                <div class="block-loc">{{ course.location || '暂无教室' }}</div>
                <div class="block-cap">{{ course.alreadyNum ?? 0 }}/{{ course.num ?? '-' }}</div>
              </div>
            </div>
          </div>
        </div>

        <div v-if="unassignedList.length" class="unassigned-panel">
          <div class="unassigned-title">
            未显示在当前课表周内的课程（含未写周几/日期或「特定日期」不在本周者）；时间格式示例：周X HH:mm-HH:mm 或 YYYY-MM-DD HH:mm-HH:mm
          </div>
          <div class="unassigned-chips">
            <el-tag
              v-for="c in unassignedList"
              :key="'u-' + c.id"
              type="info"
              effect="plain"
              class="unassigned-tag"
            >
              {{ c.name }} · {{ c.time || '无时间' }}
            </el-tag>
          </div>
        </div>
      </div>

      <div class="pagination-container">
        <el-pagination
          v-model:current-page="data.pagination.current"
          v-model:page-size="data.pagination.size"
          :total="data.pagination.total"
          :page-sizes="[20, 50, 100, 200]"
          layout="total, sizes, prev, pager, next"
          @size-change="loadCourseData"
          @current-change="loadCourseData"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'
import {
  DAY_LABELS,
  SCHEDULE_START_HOUR,
  SCHEDULE_END_HOUR,
  parseCourseTime,
  layoutBlockPercent,
  courseBelongsToWeekColumn,
  parseYmd,
  sameCalendarDay,
} from '@/utils/courseSchedule'

const data = reactive({
  searchParams: { name: '' },
  pagination: { current: 1, size: 100, total: 0 },
  loading: false,
  courseList: [],
})

const weekOffset = reactive({ n: 0 })

function startOfMonday(base = new Date()) {
  const d = new Date(base)
  const day = d.getDay()
  const diff = day === 0 ? -6 : 1 - day
  d.setDate(d.getDate() + diff)
  d.setHours(0, 0, 0, 0)
  return d
}

const weekStart = computed(() => {
  const s = startOfMonday()
  s.setDate(s.getDate() + weekOffset.n * 7)
  return s
})

const weekColumns = computed(() => {
  const out = []
  for (let i = 0; i < 7; i++) {
    const d = new Date(weekStart.value)
    d.setDate(d.getDate() + i)
    const m = String(d.getMonth() + 1).padStart(2, '0')
    const day = String(d.getDate()).padStart(2, '0')
    out.push({ md: `${m}-${day}`, date: d })
  }
  return out
})

const weekRangeText = computed(() => {
  const a = weekColumns.value[0]?.date
  const b = weekColumns.value[6]?.date
  if (!a || !b) return ''
  const fmt = (d) =>
    `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
  return `${fmt(a)} ~ ${fmt(b)}`
})

const bodyHours = computed(() => {
  const list = []
  for (let h = SCHEDULE_START_HOUR; h < SCHEDULE_END_HOUR; h++) list.push(h)
  return list
})

function shiftWeek(delta) {
  weekOffset.n += delta
}

function goThisWeek() {
  weekOffset.n = 0
}

function coursesInColumn(dayIndex) {
  const col = weekColumns.value[dayIndex]
  if (!col) return []
  return data.courseList.filter((c) => courseBelongsToWeekColumn(c, dayIndex, col.date))
}

function countInColumn(dayIndex) {
  return coursesInColumn(dayIndex).length
}

function blockStyle(course) {
  const p = parseCourseTime(course?.time)
  if (!p) return { display: 'none' }
  const layout = layoutBlockPercent(p)
  if (!layout) return { display: 'none' }
  return {
    top: `${layout.top}%`,
    height: `${Math.max(layout.height, 6)}%`,
  }
}

function blockTimeText(course) {
  const p = parseCourseTime(course?.time)
  if (!p) return ''
  const pad = (n) => String(n).padStart(2, '0')
  const span = `${pad(p.startH)}:${pad(p.startM)}~${pad(p.endH)}:${pad(p.endM)}`
  if (p.mode === 'specific' && p.specificDate) {
    const parts = p.specificDate.split('-')
    if (parts.length === 3) return `${parts[1]}-${parts[2]} ${span}`
  }
  return span
}

const unassignedList = computed(() => {
  const cols = weekColumns.value
  return data.courseList.filter((c) => {
    const p = parseCourseTime(c?.time)
    if (!p || !layoutBlockPercent(p)) return true
    if (p.mode === 'specific' && p.specificDate) {
      const cd = parseYmd(p.specificDate)
      if (!cd) return true
      const inWeek = cols.some((col) => sameCalendarDay(col.date, cd))
      return !inWeek
    }
    if (p.mode === 'weekly' && p.dayIndex < 0) return true
    return false
  })
})

function readStoredStudentId() {
  try {
    const u = JSON.parse(localStorage.getItem('system-user') || '{}')
    const sid = Number(u.id)
    return Number.isFinite(sid) && sid > 0 ? sid : null
  } catch {
    return null
  }
}

const loadCourseData = async () => {
  try {
    data.loading = true
    const studentId = readStoredStudentId()
    if (!studentId) {
      ElMessage.error('未获取到学生信息，请重新登录')
      data.courseList = []
      data.pagination.total = 0
      return
    }
    const res = await request.get('/course/selectPage', {
      params: {
        ...data.searchParams,
        pageNum: data.pagination.current,
        pageSize: data.pagination.size,
        studentId,
      },
    })
    if (res.code !== '200') {
      ElMessage.error(res.msg || '加载课程失败')
      data.courseList = []
      data.pagination.total = 0
      return
    }
    data.courseList = res.data?.list || []
    data.pagination.total = res.data?.total || 0
  } catch (error) {
    ElMessage.error('加载课程数据失败: ' + (error.response?.data?.msg || error.message))
  } finally {
    data.loading = false
  }
}

const resetSearch = () => {
  data.searchParams.name = ''
  data.pagination.current = 1
  loadCourseData()
}

onMounted(() => {
  loadCourseData()
})
</script>

<style scoped>
.stu-course-page {
  min-height: 100%;
}

.schedule-card {
  background: #fff;
  border: 1px solid var(--color-border, rgba(15, 23, 42, 0.08));
  border-radius: 12px;
  overflow: hidden;
  box-shadow: var(--shadow-soft, 0 12px 40px -18px rgba(15, 23, 42, 0.12));
}

.toolbar {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 14px 18px;
  border-bottom: 1px solid var(--color-border, rgba(15, 23, 42, 0.08));
  background: linear-gradient(180deg, #fafbfc 0%, #fff 100%);
}

.toolbar-left,
.toolbar-right {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
}

.range-label {
  font-size: 13px;
  color: var(--color-text-muted, #64748b);
  margin-left: 8px;
}

.range-value {
  font-size: 13px;
  font-weight: 600;
  color: var(--color-text, #0f172a);
  font-variant-numeric: tabular-nums;
}

.search-input {
  width: 220px;
}

.schedule-scroll {
  overflow-x: auto;
  padding: 12px 12px 8px;
}

.schedule-grid {
  display: grid;
  grid-template-columns: 52px repeat(7, minmax(120px, 1fr));
  grid-template-rows: auto;
  min-width: 900px;
}

.corner-cell {
  grid-column: 1;
  grid-row: 1;
  border-bottom: 1px solid #e8eaef;
}

.day-header {
  text-align: center;
  padding: 10px 6px 8px;
  border-left: 1px solid #e8eaef;
  border-bottom: 1px solid #e8eaef;
  background: #f8fafc;
}

.day-name {
  font-weight: 700;
  font-size: 14px;
  color: #0f172a;
}

.day-date {
  font-size: 12px;
  color: #64748b;
  margin-top: 2px;
}

.day-count {
  font-size: 11px;
  color: #0d9488;
  margin-top: 4px;
  font-weight: 500;
}

.time-rail {
  grid-column: 1;
  grid-row: 2;
  display: flex;
  flex-direction: column;
  height: 480px;
  border-right: 1px solid #e8eaef;
  padding-top: 0;
  box-sizing: border-box;
}

.time-tick {
  flex: 1;
  min-height: 48px;
  font-size: 11px;
  color: #94a3b8;
  text-align: right;
  padding-right: 8px;
  padding-top: 4px;
  font-variant-numeric: tabular-nums;
  border-top: 1px solid #eef1f5;
  box-sizing: border-box;
}

.day-column {
  border-left: 1px solid #e8eaef;
  min-width: 0;
}

.day-body {
  position: relative;
  /* 8:00–18:00 共 10 格，每格 48px */
  height: 480px;
  background: #fff;
}

.hour-line {
  height: 48px;
  border-top: 1px solid #f1f5f9;
  box-sizing: border-box;
}

.hour-line:first-child {
  border-top-color: #e8eaef;
}

.course-block {
  position: absolute;
  left: 4px;
  right: 4px;
  min-height: 36px;
  border-radius: 8px;
  padding: 6px 8px 22px;
  overflow: hidden;
  box-shadow: 0 1px 3px rgba(15, 23, 42, 0.08);
  border: 1px solid rgba(255, 255, 255, 0.5);
  cursor: default;
  z-index: 2;
}

.course-block:hover {
  z-index: 4;
  box-shadow: 0 4px 14px rgba(15, 23, 42, 0.12);
}

.block-time {
  font-size: 10px;
  font-weight: 700;
  opacity: 0.95;
  margin-bottom: 4px;
  font-variant-numeric: tabular-nums;
}

.block-title {
  font-size: 13px;
  font-weight: 700;
  line-height: 1.25;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.block-meta,
.block-loc {
  font-size: 11px;
  opacity: 0.9;
  margin-top: 2px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.block-cap {
  position: absolute;
  right: 6px;
  bottom: 4px;
  font-size: 10px;
  font-weight: 600;
  opacity: 0.85;
  font-variant-numeric: tabular-nums;
}

.tone-0 {
  background: linear-gradient(145deg, #ede9fe 0%, #ddd6fe 100%);
  color: #4c1d95;
}
.tone-1 {
  background: linear-gradient(145deg, #dbeafe 0%, #bfdbfe 100%);
  color: #1e3a8a;
}
.tone-2 {
  background: linear-gradient(145deg, #cffafe 0%, #a5f3fc 100%);
  color: #155e75;
}
.tone-3 {
  background: linear-gradient(145deg, #fce7f3 0%, #fbcfe8 100%);
  color: #831843;
}
.tone-4 {
  background: linear-gradient(145deg, #d1fae5 0%, #a7f3d0 100%);
  color: #064e3b;
}
.tone-5 {
  background: linear-gradient(145deg, #fef3c7 0%, #fde68a 100%);
  color: #78350f;
}

.unassigned-panel {
  margin-top: 14px;
  padding: 12px 14px;
  background: #f8fafc;
  border-radius: 10px;
  border: 1px dashed #cbd5e1;
}

.unassigned-title {
  font-size: 12px;
  color: #64748b;
  margin-bottom: 8px;
}

.unassigned-chips {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.pagination-container {
  padding: 12px 16px 16px;
  display: flex;
  justify-content: center;
  border-top: 1px solid #f1f5f9;
}
</style>
