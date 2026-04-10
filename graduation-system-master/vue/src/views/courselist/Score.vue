<template>
  <div class="score-page">
    <header class="score-hero card">
      <div class="score-hero__accent" aria-hidden="true" />
      <div class="score-hero__icon" aria-hidden="true">
        <el-icon><Trophy /></el-icon>
      </div>
      <div class="score-hero__text">
        <h1 class="score-hero__title">我的成绩</h1>
        <p class="score-hero__sub">
          共参加 <strong class="score-hero__num">{{ data.total }}</strong> 次考试
          <span v-if="courseHint" class="score-hero__course">{{ courseHint }}</span>
        </p>
      </div>
    </header>

    <section v-if="data.tableData.length" class="stat-grid" aria-label="成绩概览">
      <div class="stat-card stat-card--primary">
        <div class="stat-card__icon" aria-hidden="true">
          <el-icon><Document /></el-icon>
        </div>
        <div class="stat-card__body">
          <div class="stat-card__value">{{ data.total }}</div>
          <div class="stat-card__label">参考次数</div>
        </div>
      </div>
      <div class="stat-card stat-card--cyan">
        <div class="stat-card__icon" aria-hidden="true">
          <el-icon><TrendCharts /></el-icon>
        </div>
        <div class="stat-card__body">
          <div class="stat-card__value">{{ myAvgScore }}</div>
          <div class="stat-card__label">平均分</div>
        </div>
      </div>
      <div class="stat-card stat-card--amber">
        <div class="stat-card__icon" aria-hidden="true">
          <el-icon><Trophy /></el-icon>
        </div>
        <div class="stat-card__body">
          <div class="stat-card__value">{{ myMaxScore }}</div>
          <div class="stat-card__label">最高分</div>
        </div>
      </div>
      <div class="stat-card stat-card--success">
        <div class="stat-card__icon" aria-hidden="true">
          <el-icon><CircleCheck /></el-icon>
        </div>
        <div class="stat-card__body">
          <div class="stat-card__value">{{ myGraded }}</div>
          <div class="stat-card__label">已批改</div>
        </div>
      </div>
    </section>

    <div v-if="data.tableData.length" class="main-grid">
      <section class="table-panel card" v-loading="data.loading" aria-label="考试记录">
        <div class="panel-head">
          <el-icon class="panel-head__icon"><List /></el-icon>
          <span class="panel-head__title">考试记录</span>
        </div>
        <el-table
          :data="data.tableData"
          stripe
          row-key="id"
          class="score-table"
          :row-class-name="rowClass"
        >
          <el-table-column type="index" label="#" width="50" align="center" />
          <el-table-column prop="name" label="试卷名称" show-overflow-tooltip min-width="160" />
          <el-table-column prop="courseName" label="课程" show-overflow-tooltip width="130" />
          <el-table-column prop="teacherName" label="教师" show-overflow-tooltip width="100" />
          <el-table-column label="状态" width="108" align="center">
            <template #default="scope">
              <el-tag
                v-if="scope.row.status === '已阅卷'"
                type="success"
                size="small"
                effect="light"
                class="status-tag"
              >
                <el-icon class="tag-ic"><CircleCheck /></el-icon>
                已批改
              </el-tag>
              <el-tag v-else type="warning" size="small" effect="light" class="status-tag">
                <el-icon class="tag-ic"><Clock /></el-icon>
                待批改
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="分数" width="108" align="center">
            <template #default="scope">
              <template v-if="scope.row.score !== null && scope.row.score !== ''">
                <el-tag :type="scoreTagType(scope.row.score)" size="small" effect="light" class="score-tag">
                  <span class="score-tag__num">{{ scope.row.score }}</span> 分
                </el-tag>
              </template>
              <span v-else class="cell-empty">—</span>
            </template>
          </el-table-column>
          <el-table-column label="评级" width="88" align="center">
            <template #default="scope">
              <span v-if="scope.row.score !== null && scope.row.score !== ''" :class="gradeClass(scope.row.score)">
                {{ scoreGrade(scope.row.score) }}
              </span>
              <span v-else class="cell-empty">—</span>
            </template>
          </el-table-column>
        </el-table>

        <div v-if="data.total > data.pageSize" class="pagination-wrap">
          <el-pagination
            background
            layout="sizes, prev, pager, next, total"
            :current-page="data.pageNum"
            :page-size="data.pageSize"
            :total="data.total"
            :page-sizes="[8, 12, 20]"
            @current-change="onPageChange"
            @size-change="onPageSizeChange"
          />
        </div>
      </section>

      <aside class="chart-aside" aria-label="图表">
        <div class="chart-panel card">
          <div class="panel-head">
            <el-icon class="panel-head__icon panel-head__icon--chart"><PieChart /></el-icon>
            <span class="panel-head__title">成绩分布</span>
          </div>
          <div ref="gradeChartEl" class="chart-box" />
        </div>
        <div class="chart-panel card">
          <div class="panel-head">
            <el-icon class="panel-head__icon panel-head__icon--chart"><DataLine /></el-icon>
            <span class="panel-head__title">分数趋势</span>
          </div>
          <div ref="trendChartEl" class="chart-box" />
        </div>
      </aside>
    </div>

    <div v-if="!data.tableData.length && !data.loading" class="empty-panel card">
      <el-empty description="还没有参加过任何考试" :image-size="100" />
    </div>
  </div>
</template>

<script setup>
import { reactive, computed, ref, nextTick, onMounted, onBeforeUnmount } from 'vue'
import { useRoute } from 'vue-router'
import request from '@/utils/request.js'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import {
  Trophy,
  Document,
  TrendCharts,
  CircleCheck,
  Clock,
  List,
  PieChart,
  DataLine,
} from '@element-plus/icons-vue'

const route = useRoute()

const courseHint = computed(() => {
  const n = route.query.courseName
  return n ? `· ${n}` : ''
})

const gradeChartEl = ref(null)
const trendChartEl = ref(null)
let chartGrade = null
let chartTrend = null

const dashColors = {
  primary: '#0d9488',
  primaryLight: '#14b8a6',
  cyan: '#0891b2',
  amber: '#d97706',
  danger: '#dc2626',
  muted: '#64748b',
  grid: 'rgba(15, 23, 42, 0.06)',
}

const data = reactive({
  user: JSON.parse(localStorage.getItem('system-user') || '{}'),
  tableData: [],
  pageNum: 1,
  pageSize: 8,
  total: 0,
  loading: false,
})

const scoreTagType = (score) => {
  const s = Number(score)
  if (s >= 90) return 'success'
  if (s >= 60) return 'primary'
  return 'danger'
}

const scoreGrade = (score) => {
  const s = Number(score)
  if (s >= 90) return '优秀'
  if (s >= 80) return '良好'
  if (s >= 60) return '及格'
  return '不及格'
}

const gradeClass = (score) => {
  const s = Number(score)
  if (s >= 90) return 'grade grade-excellent'
  if (s >= 80) return 'grade grade-good'
  if (s >= 60) return 'grade grade-pass'
  return 'grade grade-fail'
}

const rowClass = ({ row }) => {
  if (row.score !== null && row.score !== '' && Number(row.score) < 60) return 'row-fail'
  return ''
}

const scoredList = computed(() =>
  data.tableData.filter((r) => r.score !== null && r.score !== '' && !Number.isNaN(Number(r.score)))
)

const myAvgScore = computed(() => {
  if (!scoredList.value.length) return '—'
  const s = scoredList.value.reduce((a, b) => a + Number(b.score), 0)
  return (s / scoredList.value.length).toFixed(1)
})

const myMaxScore = computed(() => {
  if (!scoredList.value.length) return '—'
  return Math.max(...scoredList.value.map((r) => Number(r.score)))
})

const myGraded = computed(() => data.tableData.filter((r) => r.status === '已阅卷').length)

const renderGradeChart = () => {
  if (!gradeChartEl.value) return
  if (!chartGrade) chartGrade = echarts.init(gradeChartEl.value)
  const grades = { 优秀: 0, 良好: 0, 及格: 0, 不及格: 0 }
  for (const r of scoredList.value) {
    grades[scoreGrade(r.score)]++
  }
  chartGrade.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c} 次 ({d}%)' },
    legend: { bottom: 0, left: 'center', textStyle: { fontSize: 11, color: dashColors.muted } },
    series: [
      {
        type: 'pie',
        radius: ['40%', '68%'],
        center: ['50%', '44%'],
        padAngle: 2,
        itemStyle: { borderRadius: 6 },
        label: { show: false },
        emphasis: { label: { show: true, fontSize: 14, fontWeight: 'bold' } },
        data: [
          { value: grades['优秀'], name: '优秀', itemStyle: { color: dashColors.primary } },
          { value: grades['良好'], name: '良好', itemStyle: { color: dashColors.primaryLight } },
          { value: grades['及格'], name: '及格', itemStyle: { color: dashColors.cyan } },
          { value: grades['不及格'], name: '不及格', itemStyle: { color: dashColors.danger } },
        ],
      },
    ],
  })
}

const renderTrendChart = () => {
  if (!trendChartEl.value) return
  if (!chartTrend) chartTrend = echarts.init(trendChartEl.value)
  const list = [...scoredList.value].slice(-10)
  chartTrend.setOption({
    tooltip: {
      trigger: 'axis',
      formatter: (p) => `${p[0].name}<br/>分数：<b>${p[0].value}</b>`,
    },
    grid: { top: 14, right: 12, bottom: 40, left: 36 },
    xAxis: {
      type: 'category',
      data: list.map((r, i) => r.name?.substring(0, 6) || `考试${i + 1}`),
      axisLabel: { interval: 0, rotate: 28, fontSize: 10, color: dashColors.muted },
      axisLine: { lineStyle: { color: dashColors.grid } },
    },
    yAxis: {
      type: 'value',
      min: 0,
      max: 100,
      splitLine: { lineStyle: { type: 'dashed', color: dashColors.grid } },
      axisLabel: { fontSize: 10, color: dashColors.muted },
    },
    series: [
      {
        type: 'line',
        data: list.map((r) => Number(r.score)),
        smooth: true,
        symbol: 'circle',
        symbolSize: 7,
        lineStyle: { color: dashColors.primary, width: 2.5 },
        itemStyle: { color: dashColors.primary },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(13, 148, 136, 0.22)' },
            { offset: 1, color: 'rgba(13, 148, 136, 0.02)' },
          ]),
        },
        markLine: {
          silent: true,
          data: [
            {
              yAxis: 60,
              lineStyle: { color: dashColors.danger, type: 'dashed', opacity: 0.85 },
              label: { formatter: '及格 60', fontSize: 10, color: dashColors.danger },
            },
          ],
        },
      },
    ],
  })
}

const resizeCharts = () => {
  chartGrade?.resize()
  chartTrend?.resize()
}

const load = async () => {
  data.loading = true
  try {
    const res = await request.get('/score/selectPage', {
      params: {
        pageNum: data.pageNum,
        pageSize: data.pageSize,
        studentId: data.user?.id,
      },
    })
    if (res.code === '200') {
      data.tableData = res.data.list || []
      data.total = res.data.total ?? 0
      await nextTick()
      renderGradeChart()
      renderTrendChart()
      resizeCharts()
    } else {
      ElMessage.error(res.msg)
    }
  } finally {
    data.loading = false
  }
}

const onPageChange = (page) => {
  data.pageNum = page
  load()
}

const onPageSizeChange = (size) => {
  data.pageSize = size
  data.pageNum = 1
  load()
}

onMounted(() => {
  load()
  window.addEventListener('resize', resizeCharts)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', resizeCharts)
  chartGrade?.dispose()
  chartTrend?.dispose()
  chartGrade = null
  chartTrend = null
})
</script>

<style scoped lang="scss">
.score-page {
  max-width: 1280px;
  margin: 0 auto;
  padding: 16px 20px 32px;
  font-family: var(--font-sans);
  color: var(--color-text);
  min-height: 100%;
  box-sizing: border-box;
}

.score-hero {
  position: relative;
  display: flex;
  align-items: flex-start;
  gap: 16px;
  margin-bottom: 16px;
  overflow: hidden;
  border: 1px solid var(--color-border);
  box-shadow: var(--shadow-soft);
}

.score-hero__accent {
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 4px;
  border-radius: 0 4px 4px 0;
  background: linear-gradient(180deg, var(--color-primary) 0%, var(--color-primary-hover) 100%);
}

.score-hero__icon {
  flex-shrink: 0;
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--radius-sm);
  background: var(--color-primary-soft);
  color: var(--color-primary-hover);
  font-size: 24px;
}

.score-hero__text {
  flex: 1;
  min-width: 0;
}

.score-hero__title {
  margin: 0 0 6px;
  font-size: 22px;
  font-weight: 700;
  letter-spacing: -0.02em;
  color: var(--color-text);
}

.score-hero__sub {
  margin: 0;
  font-size: 14px;
  color: var(--color-text-muted);
  line-height: 1.5;
}

.score-hero__num {
  font-family: var(--font-mono);
  font-variant-numeric: tabular-nums;
  color: var(--color-primary-hover);
}

.score-hero__course {
  color: var(--color-text-subtle);
  font-size: 13px;
}

.stat-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
  margin-bottom: 16px;
}

@media (max-width: 900px) {
  .stat-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 16px 18px;
  border-radius: var(--radius-md);
  border: 1px solid var(--color-border);
  background: var(--color-bg-elevated);
  box-shadow: var(--shadow-soft);
  transition:
    border-color var(--duration) var(--ease-out),
    box-shadow var(--duration) var(--ease-out),
    transform var(--duration) var(--ease-out);
}

.stat-card:hover {
  border-color: var(--color-primary-muted);
  box-shadow:
    0 14px 40px -20px rgba(15, 23, 42, 0.12),
    0 0 0 1px var(--color-primary-soft);
  transform: translateY(-1px);
}

.stat-card__icon {
  width: 44px;
  height: 44px;
  border-radius: var(--radius-sm);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  flex-shrink: 0;
}

.stat-card--primary .stat-card__icon {
  background: var(--color-primary-soft);
  color: var(--color-primary-hover);
}

.stat-card--cyan .stat-card__icon {
  background: rgba(8, 145, 178, 0.12);
  color: #0e7490;
}

.stat-card--amber .stat-card__icon {
  background: rgba(217, 119, 6, 0.12);
  color: #b45309;
}

.stat-card--success .stat-card__icon {
  background: rgba(5, 150, 105, 0.12);
  color: #047857;
}

.stat-card__value {
  font-family: var(--font-mono);
  font-variant-numeric: tabular-nums;
  font-size: 22px;
  font-weight: 700;
  line-height: 1.1;
         color: var(--color-text);
}

.stat-card__label {
  margin-top: 4px;
  font-size: 12px;
  color: var(--color-text-muted);
}

.main-grid {
  display: grid;
  grid-template-columns: 1fr minmax(280px, 320px);
  gap: 16px;
  align-items: start;
}

@media (max-width: 1100px) {
  .main-grid {
    grid-template-columns: 1fr;
  }
}

.table-panel {
  padding: 0 0 16px;
  overflow: hidden;
}

.panel-head {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 14px 18px 10px;
  border-bottom: 1px solid var(--color-border);
  margin-bottom: 0;
}

.panel-head__icon {
  font-size: 18px;
  color: var(--color-primary-hover);
}

.panel-head__icon--chart {
  color: var(--color-primary);
}

.panel-head__title {
  font-size: 15px;
  font-weight: 700;
  color: var(--color-text);
}

.score-table {
  width: 100%;
  padding: 0 12px;
}

.score-table :deep(.el-table__header th.el-table__cell) {
  background: rgba(13, 148, 136, 0.06) !important;
  color: var(--color-text);
  font-weight: 600;
  font-size: 13px;
}

.status-tag {
  border-radius: 999px;
}

.tag-ic {
  margin-right: 4px;
  vertical-align: middle;
}

.score-tag {
  border-radius: 8px;
  font-weight: 600;
}

.score-tag__num {
  font-family: var(--font-mono);
  font-variant-numeric: tabular-nums;
}

.cell-empty {
  color: var(--color-text-subtle);
}

.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  flex-wrap: wrap;
  gap: 8px;
  padding: 16px 18px 0;
}

.chart-aside {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.chart-panel {
  padding: 0 0 12px;
}

.chart-panel .panel-head {
  margin-bottom: 0;
}

.chart-box {
  height: 220px;
  width: 100%;
}

.grade {
  display: inline-block;
  font-size: 12px;
  font-weight: 600;
  padding: 3px 8px;
  border-radius: 6px;
}

.grade-excellent {
  color: var(--color-primary-hover);
  background: var(--color-primary-soft);
}

.grade-good {
  color: #047857;
  background: rgba(5, 150, 105, 0.12);
}

.grade-pass {
  color: #0e7490;
  background: rgba(8, 145, 178, 0.12);
}

.grade-fail {
  color: #b91c1c;
  background: rgba(220, 38, 38, 0.1);
}

:deep(.row-fail td) {
  background: rgba(220, 38, 38, 0.04) !important;
}

.empty-panel {
  padding: 48px 24px;
  text-align: center;
}
</style>
