<template>
  <div class="dashboard-page">
    <div class="dashboard-viewport">
      <div class="dashboard-stage" :class="{ 'is-scaled': enableScale }">
        <header class="dashboard-hero card">
          <div class="dashboard-hero__accent" aria-hidden="true" />
          <div class="dashboard-hero__icon" aria-hidden="true">
            <el-icon><DataBoard /></el-icon>
          </div>
          <div class="dashboard-hero__text">
            <h1 class="dashboard-hero__title">统计驾驶舱</h1>
            <p v-if="role === 'ADMIN'" class="dashboard-hero__sub">管理员总览 · 关键指标与趋势一目掌握</p>
            <p v-else-if="role === 'TEACHER'" class="dashboard-hero__sub">教师看板 · 按课程查看班级与任务数据</p>
            <p v-else-if="role === 'STUDENT'" class="dashboard-hero__sub">学习看板</p>
            <p v-else class="dashboard-hero__sub">当前角色暂无统计看板</p>
          </div>
          <div v-if="role === 'TEACHER'" class="dashboard-hero__actions">
            <el-select
              v-model="teacherCourseId"
              size="small"
              class="dashboard-course-select"
              placeholder="选择课程"
            >
              <el-option v-for="c in teacherCourses" :key="c.id" :label="c.name" :value="c.id" />
            </el-select>
          </div>
        </header>

    <template v-if="role === 'ADMIN'">
      <div class="nine-grid nine-grid--admin">
        <!-- 第一行：KPI 收窄；两侧图表等宽，避免「数字卡与图」抢同宽 -->
        <el-card class="grid-block dash-card" shadow="never">
          <template #header><div class="card-header">数据概览</div></template>
          <div class="kpi-grid kpi-grid--quad">
            <div class="kpi-item">
              <div class="kpi-label">课程总数</div>
              <div class="kpi-value">{{ main.kpi.courseCount ?? '-' }}</div>
            </div>
            <div class="kpi-item">
              <div class="kpi-label">教师总数</div>
              <div class="kpi-value">{{ main.kpi.teacherCount ?? '-' }}</div>
            </div>
            <div class="kpi-item">
              <div class="kpi-label">学生总数</div>
              <div class="kpi-value">{{ main.kpi.studentCount ?? '-' }}</div>
            </div>
            <div class="kpi-item">
              <div class="kpi-label">班级总数</div>
              <div class="kpi-value">{{ main.kpi.clazzCount ?? '-' }}</div>
            </div>
          </div>
        </el-card>

        <el-card class="grid-block dash-card dash-chart-card" shadow="never">
          <template #header><div class="card-header">作业状态分布</div></template>
          <div ref="mainLeft2Ref" class="chart chart-panel" />
        </el-card>

        <el-card class="grid-block dash-card dash-chart-card" shadow="never">
          <template #header><div class="card-header">课程数量趋势（按学期/时间）</div></template>
          <div ref="mainMid1Ref" class="chart chart-panel" />
        </el-card>

        <!-- 第二行 -->
        <el-card class="grid-block dash-card dash-card--scroll-inner" shadow="never">
          <template #header><div class="card-header">学生作业完成率 Top10</div></template>
          <div ref="adminProgressScrollEl" class="progress-list dash-scroll-y">
            <div v-for="s in main.left3" :key="s.studentId" class="progress-item">
              <div class="progress-meta">
                <span class="progress-name">{{ s.studentName }}（{{ s.code || '—' }}）</span>
                <span class="progress-value">{{ s.completionRate ?? 0 }}%</span>
              </div>
              <el-progress :percentage="Number(s.completionRate || 0)" :stroke-width="10" :show-text="false" />
              <div class="progress-count">
                已完成：{{ s.completedCount ?? 0 }} / {{ s.totalCount ?? 0 }}
              </div>
            </div>
          </div>
        </el-card>

        <el-card class="grid-block dash-card dash-card--scroll-inner" shadow="never">
          <template #header><div class="card-header">课程概览（最近）</div></template>
          <div ref="adminCourseOverviewScrollEl" class="dash-scroll-y dash-recent-table-scroll">
            <el-table :data="main.mid2" size="small" class="dark-table" style="width: 100%">
              <el-table-column prop="courseName" label="课程" min-width="140" />
              <el-table-column prop="teacherName" label="教师" width="90" />
              <el-table-column prop="clazzName" label="班级" width="110" />
              <el-table-column prop="studentCount" label="学生数" width="80" />
              <el-table-column prop="taskCount" label="任务数" width="80" />
            </el-table>
          </div>
        </el-card>

        <el-card class="grid-block dash-card dash-chart-card" shadow="never">
          <template #header><div class="card-header">教师职称分布</div></template>
          <div ref="mainRight1Ref" class="chart chart-panel chart-panel--pie" />
        </el-card>

        <!-- 第三行 -->
        <el-card class="grid-block dash-card bottom-block dash-card--scroll-inner" shadow="never">
          <template #header><div class="card-header">学生综合得分分析（雷达）</div></template>
          <div ref="adminRadarScrollEl" class="dash-scroll-y dash-radar-chart-scroll">
            <div ref="mainMid3Ref" class="chart dash-radar-chart-inner" />
          </div>
        </el-card>

        <el-card class="grid-block dash-card bottom-block" shadow="never">
          <template #header><div class="card-header">低完成率预警</div></template>
          <div class="warn-box">
            <div class="warn-line">阈值：{{ main.right2.threshold }}%</div>
            <div class="warn-line">Top10 中低于阈值：{{ main.right2.lowRateCountInTop10 }} 人</div>
          </div>
        </el-card>

        <el-card class="grid-block dash-card bottom-block dash-card--scroll-inner" shadow="never">
          <template #header><div class="card-header">近期作业记录</div></template>
          <div ref="adminRecentWorkScrollEl" class="dash-scroll-y dash-recent-table-scroll">
            <el-table :data="main.right3" size="small" class="dark-table" style="width: 100%">
              <el-table-column prop="studentName" label="学生" width="90" />
              <el-table-column prop="workName" label="作业" min-width="140" show-overflow-tooltip />
              <el-table-column prop="state" label="状态" width="90" />
            </el-table>
          </div>
        </el-card>
      </div>
    </template>

    <template v-else-if="role === 'TEACHER'">
      <div class="nine-grid">
        <!-- 第一行 -->
        <el-card class="grid-block dash-card" shadow="never">
          <template #header><div class="card-header">数据概览</div></template>
          <div class="kpi-grid kpi-grid--triple">
            <div class="kpi-item">
              <div class="kpi-label">授课课程数</div>
              <div class="kpi-value">{{ screen.kpi.courseCount ?? '-' }}</div>
            </div>
            <div class="kpi-item">
              <div class="kpi-label">学生覆盖数</div>
              <div class="kpi-value">{{ screen.kpi.studentCount ?? '-' }}</div>
            </div>
            <div class="kpi-item">
              <div class="kpi-label">班级数</div>
              <div class="kpi-value">{{ screen.kpi.clazzCount ?? '-' }}</div>
            </div>
          </div>
        </el-card>

        <el-card class="grid-block dash-card" shadow="never">
          <template #header><div class="card-header">作业状态分布</div></template>
          <div ref="screenLeft2Ref" class="chart" />
        </el-card>

        <el-card class="grid-block dash-card dash-card--scroll-inner" shadow="never">
          <template #header><div class="card-header">课程上课时间地点（本教师）</div></template>
          <div ref="teacherCourseTimeScrollEl" class="course-time-location-cards dash-scroll-y">
            <div
              v-for="c in screen.mid1"
              :key="c.courseId ?? c.courseName"
              class="course-time-location-card"
            >
              <div class="course-time-location-name">{{ c.courseName || '-' }}</div>
              <div class="course-time-location-row">
                <span class="course-time-location-label">上课时间：</span>
                <span class="course-time-location-value">{{ c.time || '-' }}</span>
              </div>
              <div class="course-time-location-row">
                <span class="course-time-location-label">上课地点：</span>
                <span class="course-time-location-value">{{ c.location || '-' }}</span>
              </div>
            </div>

            <div v-if="screen.mid1?.length === 0" class="course-time-location-empty">-</div>
          </div>
        </el-card>

        <!-- 第二行 -->
        <el-card class="grid-block dash-card dash-card--scroll-inner" shadow="never">
          <template #header><div class="card-header">学生作业完成率 Top10</div></template>
          <div ref="teacherProgressScrollEl" class="progress-list dash-scroll-y">
            <div v-for="s in screen.left3" :key="s.studentId" class="progress-item">
              <div class="progress-meta">
                <span class="progress-name">{{ s.studentName }}（{{ s.code || '—' }}）</span>
                <span class="progress-value">{{ s.completionRate ?? 0 }}%</span>
              </div>
              <el-progress :percentage="Number(s.completionRate || 0)" :stroke-width="10" :show-text="false" />
              <div class="progress-count">
                已完成：{{ s.completedCount ?? 0 }} / {{ s.totalCount ?? 0 }}
              </div>
            </div>
          </div>
        </el-card>

        <el-card class="grid-block dash-card" shadow="never">
          <template #header><div class="card-header">课程概览</div></template>
          <el-table :data="screen.mid2" size="small" class="dark-table" style="width: 100%">
            <el-table-column prop="courseName" label="课程" min-width="140" />
            <el-table-column prop="clazzName" label="班级" width="110" />
            <el-table-column prop="studentCount" label="学生数" width="80" />
            <el-table-column prop="taskCount" label="任务数" width="80" />
          </el-table>
        </el-card>

        <el-card class="grid-block dash-card signin-pie-card dash-card--scroll-inner" shadow="never">
          <template #header><div class="card-header">签到情况（饼图）</div></template>

          <div class="signin-pie-wrap">
            <div ref="screenRight1Ref" class="chart signin-chart" />

            <div class="signin-names">
              <div class="signin-col">
                <div class="signin-col-title">
                  已签到（{{ screen.right1?.signedCount ?? 0 }}）
                </div>
                <div
                  ref="signinSignedScrollEl"
                  class="signin-tags signin-tags--list dash-scroll-y"
                >
                  <el-tag
                    v-for="(n, idx) in signedNameList"
                    :key="'signed-' + idx + '-' + n"
                    type="success"
                    size="small"
                    effect="light"
                    class="signin-tag"
                  >
                    {{ n }}
                  </el-tag>
                  <div v-if="signedNameList.length === 0" class="signin-empty">-</div>
                </div>
              </div>

              <div class="signin-col">
                <div class="signin-col-title">
                  未签到（{{ screen.right1?.unsignedCount ?? 0 }}）
                </div>
                <div
                  ref="signinUnsignedScrollEl"
                  class="signin-tags signin-tags--list signin-tags--unsigned dash-scroll-y"
                >
                  <el-tag
                    v-for="(n, idx) in unsignedNameList"
                    :key="'unsigned-' + idx + '-' + n"
                    type="info"
                    size="small"
                    effect="light"
                    class="signin-tag"
                  >
                    {{ n }}
                  </el-tag>
                  <div v-if="unsignedNameList.length === 0" class="signin-empty">-</div>
                </div>
              </div>
            </div>
          </div>
        </el-card>

        <!-- 第三行 -->
        <el-card class="grid-block dash-card bottom-block dash-card--scroll-inner" shadow="never">
          <template #header><div class="card-header">学生综合得分分析（雷达）</div></template>
          <div ref="teacherRadarScrollEl" class="dash-scroll-y dash-radar-chart-scroll">
            <div ref="screenMid3Ref" class="chart dash-radar-chart-inner" />
          </div>
        </el-card>

        <el-card class="grid-block dash-card bottom-block" shadow="never">
          <template #header><div class="card-header">实验作业相似度预警</div></template>
          <div class="similarity-alert">
            <div class="similarity-header">
              阈值：{{ screen.right2.threshold ?? 0 }}%
            </div>

            <div class="similarity-list">
              <div
                v-for="s in screen.right2.highSimilarityList || []"
                :key="s.studentId ?? s.studentName"
                class="similarity-item"
              >
                <div class="similarity-top">
                  <span class="similarity-student">{{ s.studentName || '-' }}</span>
                  <span class="similarity-compared">
                    对比：{{ s.comparedStudentName || '-' }}
                  </span>
                </div>

                <el-progress
                  :percentage="Number(s.similarityPercent || 0)"
                  :stroke-width="10"
                  :show-text="false"
                />
                <div class="similarity-meta">
                  相似度：{{
                    s.similarityText ||
                    Number(s.similarityPercent || 0).toFixed(2) + '%'
                  }}
                </div>
              </div>

              <div v-if="(screen.right2.highSimilarityList || []).length === 0" class="similarity-empty">-</div>
            </div>
          </div>
        </el-card>

        <el-card class="grid-block dash-card bottom-block" shadow="never">
          <template #header><div class="card-header">实验任务阶段人数</div></template>
          <div ref="screenRight3Ref" class="chart" />
        </el-card>
      </div>
    </template>

    <template v-else-if="role === 'STUDENT'">
      <div class="dashboard-empty">
        <el-empty description="学生个人学习统计暂未接入，请使用左侧「我学的课」「课程列表」查看课程与选课。" />
      </div>
    </template>
    <template v-else>
      <div class="dashboard-empty">
        <el-empty description="当前角色暂无统计页面" />
      </div>
    </template>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, nextTick, reactive, ref, watch } from 'vue'
import * as echarts from 'echarts'
import { DataBoard } from '@element-plus/icons-vue'
import request from '@/utils/request'
import {
  mainLeft1,
  mainLeft2,
  mainLeft3,
  mainMid1,
  mainMid2,
  mainMid3,
  mainRight1,
  mainRight2,
  mainRight3,
  screenLeft1,
  screenLeft2,
  screenLeft3,
  screenMid1,
  screenMid2,
  screenMid3,
  screenRight1,
  screenRight2,
  screenRight3,
} from '@/api/dashboard'

const user = JSON.parse(localStorage.getItem('system-user') || '{}')
const role = user?.role != null && String(user.role).trim() !== ''
  ? String(user.role).trim().toUpperCase()
  : ''

// cockpit scaling（双向适应：宽高同时约束等比缩放；小屏走响应式不缩放）
const DESIGN_W = 1920
const DESIGN_H = 1080
const viewport = reactive({ w: window.innerWidth, h: window.innerHeight })
const stageScale = ref(1)
const enableScale = computed(() => viewport.w >= 1200 && viewport.h >= 700)
const stageStyle = computed(() => {
  // 小屏下走正常响应式布局，不做整体缩放
  if (!enableScale.value) return {}
  // 仅做视觉缩放，不再写死宽高为设计稿尺寸，保证可以按照视窗高度铺满
  return {
    transform: `scale(${stageScale.value})`,
  }
})


const teacherCourses = ref([])
const teacherCourseId = ref(null)

// ===================== 静态兜底数据=====================
const adminFallback = {
  kpi: {
    courseCount: 12,
    teacherCount: 8,
    studentCount: 260,
    clazzCount: 6,
  },
  left2: [
    { name: '未发布', value: 4 },
    { name: '进行中', value: 18 },
    { name: '已截止', value: 12 },
    { name: '已批阅', value: 20 },
  ],
  left3: [
    {
      studentId: 1,
      studentName: '张三',
      code: '2023001',
      completionRate: 96,
      completedCount: 24,
      totalCount: 25,
    },
    {
      studentId: 2,
      studentName: '李四',
      code: '2023002',
      completionRate: 92,
      completedCount: 23,
      totalCount: 25,
    },
    {
      studentId: 3,
      studentName: '王五',
      code: '2023003',
      completionRate: 88,
      completedCount: 22,
      totalCount: 25,
    },
  ],
  mid1: [
    { label: '2023上', value: 6 },
    { label: '2023下', value: 8 },
    { label: '2024上', value: 10 },
    { label: '2024下', value: 12 },
  ],
  mid2: [
    {
      courseName: '数据结构与算法',
      teacherName: '张老师',
      clazzName: '计科2201',
      studentCount: 45,
      taskCount: 8,
    },
    {
      courseName: '人工智能导论',
      teacherName: '李老师',
      clazzName: '信安2202',
      studentCount: 40,
      taskCount: 6,
    },
  ],
  mid3: {
    indicators: ['实验完成度', '考勤情况', '报告质量', '课堂参与', '综合表现'],
    series: [
      {
        studentName: '张三',
        values: [9.5, 9, 9, 8.5, 9.2],
      },
      {
        studentName: '李四',
        values: [9, 8.5, 8.8, 8.2, 8.7],
      },
    ],
  },
  right1: [
    { name: '教授', value: 2 },
    { name: '副教授', value: 3 },
    { name: '讲师', value: 6 },
    { name: '助教', value: 2 },
  ],
  right2: {
    threshold: 80,
    lowRateCountInTop10: 2,
  },
  right3: [
    { studentName: '张三', workName: '实验一·环境配置', state: '已提交' },
    { studentName: '李四', workName: '实验二·数据采集', state: '未提交' },
    { studentName: '王五', workName: '实验三·可视化分析', state: '批阅中' },
  ],
}

const teacherFallback = {
  kpi: {
    courseCount: 3,
    studentCount: 120,
    clazzCount: 3,
  },
  left2: [
    { name: '课后任务', unsubmitted: 8, submitted: 5 },
    { name: '实验任务', unsubmitted: 6, submitted: 7 },
  ],
  left3: [
    {
      studentId: 11,
      studentName: '赵六',
      code: '2023101',
      completionRate: 95,
      completedCount: 19,
      totalCount: 20,
    },
    {
      studentId: 12,
      studentName: '钱七',
      code: '2023102',
      completionRate: 90,
      completedCount: 18,
      totalCount: 20,
    },
  ],
  mid1: [
    { courseId: 1, courseName: '智能实验管理', time: '周一第1-2节', location: 'A101' },
    { courseId: 2, courseName: '机器学习实践', time: '周三第3-4节', location: 'B202' },
  ],
  mid2: [
    {
      courseName: '智能实验管理',
      clazzName: '物联网2201',
      studentCount: 40,
      taskCount: 6,
    },
    {
      courseName: '机器学习实践',
      clazzName: '计科2203',
      studentCount: 42,
      taskCount: 5,
    },
  ],
  mid3: {
    indicators: ['作业完成度', '课堂互动', '测验成绩', '实验表现', '综合评价'],
    series: [
      {
        studentName: '赵六',
        values: [9, 8.8, 9.2, 9.0, 9.1],
      },
      {
        studentName: '钱七',
        values: [8.8, 8.5, 8.9, 8.6, 8.8],
      },
    ],
  },
  right1: {
    signedCount: 0,
    unsignedCount: 0,
    signedNames: '',
    unsignedNames: '',
  },
  right2: {
    threshold: 60,
    highSimilarityList: [
      {
        studentId: 11,
        studentName: '赵六',
        comparedStudentName: '钱七',
        similarityPercent: 92.3,
        similarityText: '92.30%',
      },
      {
        studentId: 12,
        studentName: '钱七',
        comparedStudentName: '赵六',
        similarityPercent: 88.1,
        similarityText: '88.10%',
      },
    ],
  },
  right3: [
    { label: '实验环境', value: 45 },
    { label: '实验内容步骤', value: 36 },
    { label: '实验总结', value: 28 },
  ],
}

const main = reactive({
  kpi: {},
  left2: [],
  left3: [],
  mid1: [],
  mid2: [],
  mid3: { indicators: [], series: [] },
  right1: [],
  right2: {},
  right3: [],
})

const screen = reactive({
  kpi: {},
  left2: [],
  left3: [],
  mid1: [],
  mid2: [],
  mid3: { indicators: [], series: [] },
  right1: {},
  right2: {},
  right3: [],
})

const signedNameList = computed(() => {
  const v = screen.right1?.signedNames
  if (!v) return []
  return String(v)
    .split(',')
    .map(s => s.trim())
    .filter(Boolean)
})

const unsignedNameList = computed(() => {
  const v = screen.right1?.unsignedNames
  if (!v) return []
  return String(v)
    .split(',')
    .map(s => s.trim())
    .filter(Boolean)
})

// chart refs
const mainLeft2Ref = ref()
const mainMid1Ref = ref()
const mainMid3Ref = ref()
const mainRight1Ref = ref()

const screenLeft2Ref = ref()
const screenMid3Ref = ref()
const screenRight1Ref = ref()
const screenRight3Ref = ref()

/** 卡片内列表自动滚动（管理员 Top10 / 教师课程时间地点、Top10、签到名单） */
const adminProgressScrollEl = ref(null)
const adminCourseOverviewScrollEl = ref(null)
const adminRecentWorkScrollEl = ref(null)
const adminRadarScrollEl = ref(null)
const teacherRadarScrollEl = ref(null)
const teacherCourseTimeScrollEl = ref(null)
const teacherProgressScrollEl = ref(null)
const signinSignedScrollEl = ref(null)
const signinUnsignedScrollEl = ref(null)

let dashAutoScrollTeardowns = []

function teardownDashAutoScrolls() {
  dashAutoScrollTeardowns.forEach((fn) => {
    try {
      fn()
    } catch (_) {
      /* noop */
    }
  })
  dashAutoScrollTeardowns = []
}

function bindAutoVerticalScroll(el, speed = 0.18) {
  if (!el || el.nodeType !== 1) return null
  
  // 检查是否有滚动条（内容高度大于容器高度）
  const sh = el.scrollHeight
  const ch = el.clientHeight
  if (sh <= ch + 2) {
    // 没有滚动条，不需要自动滚动
    console.log('Auto scroll skipped: no overflow', el.className, 'scrollHeight:', sh, 'clientHeight:', ch)
    return null
  }
  
  console.log('Auto scroll enabled:', el.className, 'scrollHeight:', sh, 'clientHeight:', ch)
  
  let raf = 0
  let paused = false
  let stopped = false

  const step = () => {
    if (stopped) return
    if (paused) {
      raf = requestAnimationFrame(step)
      return
    }
    const sh = el.scrollHeight
    const ch = el.clientHeight
    if (sh <= ch + 2) {
      raf = requestAnimationFrame(step)
      return
    }
    el.scrollTop += speed
    if (el.scrollTop >= sh - ch - 2) el.scrollTop = 0
    raf = requestAnimationFrame(step)
  }

  const onEnter = () => {
    paused = true
  }
  const onLeave = () => {
    paused = false
  }

  raf = requestAnimationFrame(step)
  el.addEventListener('mouseenter', onEnter)
  el.addEventListener('mouseleave', onLeave)

  return () => {
    stopped = true
    cancelAnimationFrame(raf)
    el.removeEventListener('mouseenter', onEnter)
    el.removeEventListener('mouseleave', onLeave)
  }
}

async function setupDashAutoScrolls() {
  teardownDashAutoScrolls()
  await nextTick()
  // 延迟一点确保 DOM 完全渲染
  await new Promise((r) => setTimeout(r, 500))
  const nodes = []
  if (role === 'ADMIN') {
    if (adminProgressScrollEl.value) nodes.push(adminProgressScrollEl.value)
    // el-table 的滚动在 .el-table__body-wrapper 上
    if (adminCourseOverviewScrollEl.value) {
      const tableWrapper = adminCourseOverviewScrollEl.value.querySelector('.el-table__body-wrapper')
      if (tableWrapper) nodes.push(tableWrapper)
    }
    if (adminRecentWorkScrollEl.value) {
      const tableWrapper = adminRecentWorkScrollEl.value.querySelector('.el-table__body-wrapper')
      if (tableWrapper) nodes.push(tableWrapper)
    }
    if (adminRadarScrollEl.value) nodes.push(adminRadarScrollEl.value)
  }
  if (role === 'TEACHER') {
    if (teacherCourseTimeScrollEl.value) nodes.push(teacherCourseTimeScrollEl.value)
    if (teacherProgressScrollEl.value) nodes.push(teacherProgressScrollEl.value)
    if (teacherRadarScrollEl.value) nodes.push(teacherRadarScrollEl.value)
    if (signinSignedScrollEl.value) nodes.push(signinSignedScrollEl.value)
    if (signinUnsignedScrollEl.value) nodes.push(signinUnsignedScrollEl.value)
  }
  console.log('Setting up auto scroll for', nodes.length, 'elements')
  nodes.forEach((el) => {
    const off = bindAutoVerticalScroll(el)
    if (off) {
      dashAutoScrollTeardowns.push(off)
      console.log('Auto scroll bound to:', el.className)
    }
  })
}

let chartMainLeft2, chartMainMid1, chartMainMid3, chartMainRight1
let chartScreenLeft2, chartScreenMid3, chartScreenRight1, chartScreenRight3

const safeSet = (chart, option) => {
  if (!chart) return
  chart.setOption(option, true)
}

/** 与全局主色（teal）协调的图表色组，避免高饱和紫/粉 */
const dashPalette = ['#0d9488', '#14b8a6', '#0e7490', '#f59e0b', '#0891b2', '#059669']
const chartTitleStyle = { color: '#64748b', fontSize: 12, fontWeight: 600 }
const axisLine = { lineStyle: { color: 'rgba(15, 23, 42, 0.12)' } }
const axisLabel = { color: '#64748b', fontSize: 11 }
const splitLine = { lineStyle: { color: 'rgba(15, 23, 42, 0.06)' } }

const renderBar = (chart, list, title) => {
  const x = list.map(i => i.name)
  const y = list.map(i => Number(i.value || 0))
  safeSet(chart, {
    color: dashPalette,
    title: { text: title, left: 'center', textStyle: chartTitleStyle },
    tooltip: { trigger: 'axis' },
    grid: { left: 24, right: 18, top: 40, bottom: 30, containLabel: true },
    xAxis: { type: 'category', data: x, axisLabel: { ...axisLabel, rotate: 30 }, axisLine, axisTick: { show: false } },
    yAxis: { type: 'value', axisLabel, axisLine, splitLine },
    series: [
      {
        type: 'bar',
        data: y,
        barMaxWidth: 28,
        itemStyle: {
          borderRadius: [6, 6, 0, 0],
          shadowBlur: 8,
          shadowColor: 'rgba(13, 148, 136, 0.18)',
        },
      },
    ],
  })
}

// 教师端：作业状态分布（课后任务/实验任务 × 未提交/已提交的学生人数）
const renderWorkStatusSplitBar = (chart, list, title) => {
  const safeList = Array.isArray(list) ? list : []
  const x = safeList.map(i => i.name)
  const unsubmitted = safeList.map(i => Number(i.unsubmitted || 0))
  const submitted = safeList.map(i => Number(i.submitted || 0))

  safeSet(chart, {
    color: ['#f59e0b', '#0d9488'],
    title: title ? { text: title, left: 'center', textStyle: chartTitleStyle } : { show: false },
    tooltip: { trigger: 'axis' },
    legend: { top: 10, textStyle: { ...axisLabel } },
    // 让柱状图在卡片内更“铺满”，并尽量居中（右侧留白再减小一点）
    grid: { left: 12, right: 6, top: 32, bottom: 32, containLabel: true },
    xAxis: { type: 'category', data: x, axisLabel, axisLine, axisTick: { show: false } },
    yAxis: { type: 'value', axisLabel, axisLine, splitLine },
    series: [
      {
        name: '未提交',
        type: 'bar',
        data: unsubmitted,
        barMaxWidth: 44,
        barCategoryGap: '25%',
        barGap: '15%',
        itemStyle: {
          borderRadius: [6, 6, 0, 0],
          shadowBlur: 8,
          shadowColor: 'rgba(245, 158, 11, 0.2)',
        },
      },
      {
        name: '已提交',
        type: 'bar',
        data: submitted,
        barMaxWidth: 44,
        barCategoryGap: '25%',
        barGap: '15%',
        itemStyle: {
          borderRadius: [6, 6, 0, 0],
          shadowBlur: 8,
          shadowColor: 'rgba(13, 148, 136, 0.2)',
        },
      },
    ],
  })
}

const renderLine = (chart, list, title) => {
  const x = list.map(i => i.label)
  const y = list.map(i => Number(i.value || 0))
  safeSet(chart, {
    color: dashPalette,
    title: { text: title, left: 'center', textStyle: chartTitleStyle },
    tooltip: { trigger: 'axis' },
    grid: { left: 24, right: 18, top: 40, bottom: 30, containLabel: true },
    xAxis: { type: 'category', data: x, axisLabel, axisLine, axisTick: { show: false } },
    yAxis: { type: 'value', axisLabel, axisLine, splitLine },
    series: [
      {
        type: 'line',
        data: y,
        smooth: true,
        symbol: 'circle',
        symbolSize: 6,
        lineStyle: { width: 2 },
        areaStyle: { color: 'rgba(13, 148, 136, 0.12)' },
      },
    ],
  })
}

const renderPie = (chart, list, title) => {
  safeSet(chart, {
    color: dashPalette,
    title: { text: title, left: 'center', textStyle: chartTitleStyle },
    tooltip: { trigger: 'item' },
    series: [
      {
        type: 'pie',
        // 职称分布：略小的环，留白多一些（原 52%–97% 过满）
        radius: ['38%', '68%'],
        center: ['50%', '50%'],
        // 去掉连接线与标签状态
        label: { show: false },
        labelLine: { show: false },
        emphasis: { scale: false },
        data: list.map(i => ({ name: i.name, value: Number(i.value || 0) })),
      },
    ],
  })
}

/** 教师端「签到」圆环：更小半径，不占满格子（与管理员职称饼图 renderPie 分离） */
const renderSigninPie = (chart, list, title) => {
  safeSet(chart, {
    color: dashPalette,
    title: { text: title, left: 'center', textStyle: chartTitleStyle },
    tooltip: { trigger: 'item' },
    series: [
      {
        type: 'pie',
        radius: ['40%', '68%'],
        center: ['50%', '50%'],
        label: { show: false },
        labelLine: { show: false },
        emphasis: { scale: false },
        data: list.map(i => ({ name: i.name, value: Number(i.value || 0) })),
      },
    ],
  })
}

const renderRadar = (chart, data, title) => {
  const indicators = (data?.indicators || []).map(name => ({ name, max: 10 }))
  const series = (data?.series || []).map(s => ({
    name: s.studentName || s.name,
    value: s.values || [],
  }))
  safeSet(chart, {
    color: dashPalette,
    title: { text: title, left: 'center', textStyle: chartTitleStyle },
    tooltip: {},
    legend: { bottom: 8, type: 'scroll', textStyle: { ...axisLabel } },
    radar: {
      indicator: indicators,
      radius: '65%',
      center: ['50%', '50%'],
      axisName: axisLabel,
      splitLine: { lineStyle: { color: 'rgba(15, 23, 42, 0.08)' } },
      splitArea: { areaStyle: { color: ['rgba(13, 148, 136, 0.06)', 'rgba(15, 23, 42, 0.03)'] } },
      axisLine: { lineStyle: { color: 'rgba(15, 23, 42, 0.1)' } },
    },
    series: [{ type: 'radar', data: series }],
  })
}

const initCharts = async () => {
  await nextTick()
  if (role === 'ADMIN') {
    chartMainLeft2 = echarts.init(mainLeft2Ref.value)
    chartMainMid1 = echarts.init(mainMid1Ref.value)
    chartMainMid3 = echarts.init(mainMid3Ref.value)
    chartMainRight1 = echarts.init(mainRight1Ref.value)
  } else if (role === 'TEACHER') {
    chartScreenLeft2 = echarts.init(screenLeft2Ref.value)
    chartScreenMid3 = echarts.init(screenMid3Ref.value)
    chartScreenRight1 = echarts.init(screenRight1Ref.value)
    chartScreenRight3 = echarts.init(screenRight3Ref.value)
  }
}

const disposeCharts = () => {
  ;[chartMainLeft2, chartMainMid1, chartMainMid3, chartMainRight1, chartScreenLeft2, chartScreenMid3, chartScreenRight1, chartScreenRight3]
    .filter(Boolean)
    .forEach(c => c.dispose())
}

const resizeAllCharts = () => {
  ;[chartMainLeft2, chartMainMid1, chartMainMid3, chartMainRight1, chartScreenLeft2, chartScreenMid3, chartScreenRight1, chartScreenRight3]
    .filter(Boolean)
    .forEach(c => c.resize())
}

const handleResize = () => {
  viewport.w = window.innerWidth
  viewport.h = window.innerHeight
  stageScale.value = enableScale.value ? Math.min(viewport.w / DESIGN_W, viewport.h / DESIGN_H) : 1
  resizeAllCharts()
  void setupDashAutoScrolls()
}

const loadAdmin = async () => {
  try {
    const results = await Promise.allSettled([
      mainLeft1(),
      mainLeft2(),
      mainLeft3(),
      mainMid1(),
      mainMid2(),
      mainMid3(),
      mainRight1(),
      mainRight2(),
      mainRight3(),
    ])

    // 处理每个请求的结果，失败的使用默认值
    main.kpi =
      results[0].status === 'fulfilled'
        ? results[0].value?.data || adminFallback.kpi
        : adminFallback.kpi
    main.left2 =
      results[1].status === 'fulfilled'
        ? results[1].value?.data || adminFallback.left2
        : adminFallback.left2
    main.left3 =
      results[2].status === 'fulfilled'
        ? results[2].value?.data || adminFallback.left3
        : adminFallback.left3
    main.mid1 =
      results[3].status === 'fulfilled'
        ? results[3].value?.data || adminFallback.mid1
        : adminFallback.mid1
    main.mid2 =
      results[4].status === 'fulfilled'
        ? results[4].value?.data || adminFallback.mid2
        : adminFallback.mid2
    main.mid3 =
      results[5].status === 'fulfilled'
        ? results[5].value?.data || adminFallback.mid3
        : adminFallback.mid3
    main.right1 =
      results[6].status === 'fulfilled'
        ? results[6].value?.data || adminFallback.right1
        : adminFallback.right1
    main.right2 =
      results[7].status === 'fulfilled'
        ? results[7].value?.data || adminFallback.right2
        : adminFallback.right2
    main.right3 =
      results[8].status === 'fulfilled'
        ? results[8].value?.data || adminFallback.right3
        : adminFallback.right3

    // 记录失败的请求（可选：用于调试或提示）
    const failedRequests = results
      .map((r, i) => ({ index: i, reason: r.status === 'rejected' ? r.reason : null }))
      .filter(r => r.reason)
    if (failedRequests.length > 0) {
      console.warn('部分数据加载失败:', failedRequests)
    }

    await nextTick()
    renderBar(chartMainLeft2, main.left2, '')
    renderLine(chartMainMid1, main.mid1, '')
    renderRadar(chartMainMid3, main.mid3, '')
    renderPie(chartMainRight1, main.right1, '')
    await setupDashAutoScrolls()
  } catch (error) {
    console.error('加载管理员数据时发生错误:', error)
  }
}

const loadTeacherCourses = async () => {
  const teacherName = user?.name
  if (!teacherName) return
  const res = await request.get('/course/selectByTeacherName', {
    params: { teacherName },
    _ignoreNotFound: true,
  })
  if (res.code === '200') {
    teacherCourses.value = res.data || []
    if (!teacherCourseId.value && teacherCourses.value.length) {
      teacherCourseId.value = teacherCourses.value[0].id
    }
  }
}

const loadTeacher = async () => {
  const teacherId = user?.id
  const courseId = teacherCourseId.value
  if (!teacherId || !courseId) return

  try {
    const results = await Promise.allSettled([
      screenLeft1(teacherId),
      screenLeft2(teacherId, courseId),
      screenLeft3(teacherId, courseId),
      screenMid1(teacherId),
      screenMid2(teacherId),
      screenMid3(teacherId, courseId),
      screenRight1(courseId),
      screenRight2(teacherId, courseId),
      screenRight3(teacherId, courseId),
    ])

    screen.kpi =
      results[0].status === 'fulfilled'
        ? results[0].value?.data || teacherFallback.kpi
        : teacherFallback.kpi
    screen.left2 =
      results[1].status === 'fulfilled'
        ? results[1].value?.data || teacherFallback.left2
        : teacherFallback.left2
    screen.left3 =
      results[2].status === 'fulfilled'
        ? results[2].value?.data || teacherFallback.left3
        : teacherFallback.left3
    screen.mid1 =
      results[3].status === 'fulfilled'
        ? results[3].value?.data || teacherFallback.mid1
        : teacherFallback.mid1
    screen.mid2 =
      results[4].status === 'fulfilled'
        ? results[4].value?.data || teacherFallback.mid2
        : teacherFallback.mid2
    screen.mid3 =
      results[5].status === 'fulfilled'
        ? results[5].value?.data || teacherFallback.mid3
        : teacherFallback.mid3
    screen.right1 =
      results[6].status === 'fulfilled'
        ? results[6].value?.data || teacherFallback.right1
        : teacherFallback.right1
    screen.right2 =
      results[7].status === 'fulfilled'
        ? results[7].value?.data || teacherFallback.right2
        : teacherFallback.right2
    screen.right3 =
      results[8].status === 'fulfilled'
        ? results[8].value?.data || teacherFallback.right3
        : teacherFallback.right3

    const failedRequests = results
      .map((r, i) => ({ index: i, reason: r.status === 'rejected' ? r.reason : null }))
      .filter(r => r.reason)
    if (failedRequests.length > 0) {
      console.warn('部分数据加载失败:', failedRequests)
    }

    await nextTick()
    renderWorkStatusSplitBar(chartScreenLeft2, screen.left2, '')
    renderRadar(chartScreenMid3, screen.mid3, '')

    const pieData = [
      { name: '已签到', value: Number(screen.right1?.signedCount || 0) },
      { name: '未签到', value: Number(screen.right1?.unsignedCount || 0) },
    ]
    renderSigninPie(chartScreenRight1, pieData, '')
    renderLine(chartScreenRight3, screen.right3, '')
    await setupDashAutoScrolls()
  } catch (error) {
    console.error('加载教师数据时发生错误:', error)
  }
}

watch(teacherCourseId, () => {
  if (role === 'TEACHER') loadTeacher()
})

onMounted(async () => {
  handleResize()
  window.addEventListener('resize', handleResize)
  await initCharts()
  if (role === 'ADMIN') {
    await loadAdmin()
  } else if (role === 'TEACHER') {
    await loadTeacherCourses()
    await loadTeacher()
  }
  await nextTick()
  resizeAllCharts()
  await setupDashAutoScrolls()
})

onBeforeUnmount(() => {
  teardownDashAutoScrolls()
  window.removeEventListener('resize', handleResize)
  disposeCharts()
})
</script>

<style scoped>
.dash-card :deep(.el-card) {
  --el-card-bg-color: var(--color-bg-elevated);
  --el-fill-color-blank: var(--color-bg-elevated);
}

:deep(.el-progress__text) {
  color: var(--color-primary-hover) !important;
}

:deep(.el-progress-bar__outer) {
  background: var(--color-primary-soft);
}

:deep(.el-progress-bar__inner) {
  background: linear-gradient(90deg, var(--color-primary) 0%, var(--color-primary-hover) 100%);
}

:deep(.el-table) {
  --el-table-border-color: var(--color-border);
  --el-table-header-bg-color: rgba(13, 148, 136, 0.06);
  --el-table-row-hover-bg-color: var(--color-primary-soft);
  --el-table-bg-color: var(--color-bg-elevated);
  color: var(--color-text);
}

:deep(.el-table th.el-table__cell) {
  color: var(--color-text);
  font-weight: 600;
}

:deep(.el-table td.el-table__cell) {
  border-bottom: 1px solid var(--color-border);
}

.dark-table :deep(th.el-table__cell),
.dark-table :deep(td.el-table__cell) {
  background-color: transparent !important;
}

.dark-table :deep(.el-table__header-wrapper),
.dark-table :deep(.el-table__body-wrapper),
.dark-table :deep(table) {
  background-color: transparent !important;
}

/* 整页可滚；单卡限制高度，内容在卡片内滚动 */
.dashboard-page {
  --dash-row-short: min(292px, 33vh);
  --dash-row-tall: min(308px, 35vh);
  --dash-chart-h: 200px;
  --dash-chart-pie-h: 172px;
  width: 100%;
  min-width: 0;
  display: flex;
  flex-direction: column;
  font-family: var(--font-sans);
}

.dashboard-viewport {
  display: flex;
  flex-direction: column;
  box-sizing: border-box;
}

.dashboard-stage {
  display: flex;
  flex-direction: column;
  box-sizing: border-box;
}

.dashboard-stage.is-scaled {
  transform-origin: top center;
}

.dashboard-hero {
  position: relative;
  display: flex;
  align-items: flex-start;
  gap: 14px;
  padding: 18px 20px;
  margin-bottom: 14px;
  overflow: hidden;
  transition:
    border-color var(--duration) var(--ease-out),
    box-shadow var(--duration) var(--ease-out);
}

.dashboard-hero:hover {
  border-color: var(--color-primary-muted);
  box-shadow:
    0 16px 48px -20px rgba(15, 23, 42, 0.12),
    0 0 0 1px var(--color-primary-soft);
}

@media (prefers-reduced-motion: reduce) {
  .dashboard-hero {
    transition: none;
  }
}

.dashboard-hero__accent {
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 4px;
  background: linear-gradient(180deg, var(--color-primary) 0%, var(--color-primary-hover) 100%);
  border-radius: var(--radius-md) 0 0 var(--radius-md);
  opacity: 0.95;
}

.dashboard-hero__icon {
  flex-shrink: 0;
  margin-left: 8px;
  width: 44px;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
  background: var(--color-primary-soft);
  color: var(--color-primary-hover);
  font-size: 22px;
  position: relative;
  z-index: 1;
}

.dashboard-hero__text {
  flex: 1;
  min-width: 0;
  position: relative;
  z-index: 1;
}

.dashboard-hero__title {
  margin: 0;
  font-size: 1.35rem;
  font-weight: 700;
  letter-spacing: -0.02em;
  color: var(--color-text);
  line-height: 1.25;
}

.dashboard-hero__sub {
  margin: 8px 0 0;
  font-size: 13px;
  color: var(--color-text-muted);
  line-height: 1.5;
  max-width: 60ch;
}

.dashboard-hero__actions {
  flex-shrink: 0;
  align-self: center;
  position: relative;
  z-index: 1;
}

.dashboard-course-select {
  width: min(320px, 72vw);
}

.nine-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  /* 三行固定略矮高度，卡片对齐；超出部分在卡片 body 内滚动 */
  grid-template-rows: var(--dash-row-short) var(--dash-row-short) var(--dash-row-tall);
  gap: 12px;
  align-items: stretch;
  justify-items: stretch;
  padding-bottom: 20px;
}

/* 管理员：第一列固定为 KPI 宽度，第二三两列均分给柱状图/折线图/饼图，比例更协调 */
.nine-grid--admin {
  grid-template-columns: minmax(248px, 0.92fr) minmax(0, 1.04fr) minmax(0, 1.04fr);
  grid-template-rows: var(--dash-row-short) var(--dash-row-short) var(--dash-row-tall);
  gap: 14px;
}

@media (min-width: 1200px) {
  .nine-grid--admin {
    grid-template-columns: minmax(268px, 300px) minmax(0, 1fr) minmax(0, 1fr);
  }
}

/* el-card 根节点带 .grid-block：铺满网格单元，便于行内对齐 */
.grid-block {
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 0;
  overflow: hidden;
}

.nine-grid > .grid-block:nth-child(1),
.nine-grid > .grid-block:nth-child(2),
.nine-grid > .grid-block:nth-child(3) {
  grid-row: 1;
}

.nine-grid > .grid-block:nth-child(4),
.nine-grid > .grid-block:nth-child(5),
.nine-grid > .grid-block:nth-child(6) {
  grid-row: 2;
}

.nine-grid > .grid-block.bottom-block {
  grid-row: 3;
}

.kpi-grid {
  display: grid;
  gap: 10px;
  padding: 2px 0 4px;
  flex: 1;
  min-height: 0;
  align-content: stretch;
}

.kpi-grid--quad {
  grid-template-columns: repeat(2, 1fr);
  grid-template-rows: repeat(2, 1fr);
}

.kpi-grid--quad .kpi-item {
  justify-content: center;
  min-height: 0;
}

.kpi-grid--triple {
  grid-template-columns: repeat(3, 1fr);
  grid-template-rows: 1fr;
}

.kpi-grid--triple .kpi-item {
  justify-content: center;
}

@media (max-width: 640px) {
  .kpi-grid--triple {
    grid-template-columns: 1fr;
    grid-template-rows: auto;
  }
}

.kpi-item {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  justify-content: flex-start;
  padding: 10px 12px;
  border-radius: var(--radius-sm);
  background: var(--color-primary-soft);
  border: 1px solid var(--color-border);
  transition:
    border-color var(--duration) var(--ease-out),
    box-shadow var(--duration) var(--ease-out),
    transform var(--duration) var(--ease-out);
}

.kpi-item:hover {
  border-color: var(--color-primary-muted);
  box-shadow: var(--shadow-soft);
}

.kpi-item:active {
  transform: scale(0.99);
}

@media (prefers-reduced-motion: reduce) {
  .kpi-item,
  .kpi-item:hover,
  .kpi-item:active {
    transition: none;
    transform: none;
  }
}

.kpi-label {
  font-size: 12px;
  font-weight: 600;
  color: var(--color-text-muted);
  margin-bottom: 8px;
}

.kpi-value {
  font-size: clamp(20px, 1.45vw, 26px);
  font-weight: 800;
  font-variant-numeric: tabular-nums;
  color: var(--color-primary-hover);
  letter-spacing: -0.02em;
}

.card-header {
  font-weight: 700;
  color: var(--color-text);
  letter-spacing: -0.01em;
  position: relative;
  padding-left: 12px;
}

.card-header::before {
  content: '';
  position: absolute;
  left: 0;
  top: 2px;
  width: 4px;
  height: 14px;
  border-radius: 6px;
  background: linear-gradient(180deg, var(--color-primary) 0%, var(--color-primary-hover) 100%);
}

.progress-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding-right: 6px;
  flex: 1 1 auto;
  min-height: 0;
  max-height: 260px;
  overflow-y: auto;
  scrollbar-width: thin;
  -webkit-overflow-scrolling: touch;
}

.progress-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
  color: var(--color-text-muted);
  font-size: 13px;
}

.progress-value {
  font-weight: 700;
  font-variant-numeric: tabular-nums;
  color: var(--color-primary-hover);
}

.progress-count {
  margin-top: 4px;
  font-size: 11px;
  color: var(--color-text-subtle);
  text-align: right;
}

.grid-block :deep(.el-card__body) {
  display: flex;
  flex-direction: column;
  flex: 1;
  min-height: 0;
  overflow-x: hidden;
  overflow-y: auto;
  padding: 8px 10px;
  -webkit-overflow-scrolling: touch;
}

.grid-block :deep(.el-card__header) {
  flex-shrink: 0;
}

/* 由内层 .dash-scroll-y 承担滚动，避免与卡片 body 双滚动条 */
.grid-block.dash-card--scroll-inner :deep(.el-card__body) {
  overflow: hidden;
  min-height: 0;
}

.dash-scroll-y {
  max-height: 260px;
  overflow-y: auto;
  overflow-x: hidden;
  flex: 1 1 auto;
  min-height: 0;
  scrollbar-width: thin;
  -webkit-overflow-scrolling: touch;
}

.dash-scroll-y::-webkit-scrollbar {
  width: 5px;
}

.dash-scroll-y::-webkit-scrollbar-track {
  background: var(--color-primary-soft);
  border-radius: 3px;
}

.dash-scroll-y::-webkit-scrollbar-thumb {
  background: var(--color-primary-muted);
  border-radius: 3px;
}

/* 管理员「近期作业记录」表：外层纵向滚动 + 自动滚动脚本 */
.dash-recent-table-scroll {
  width: 100%;
  flex: 1 1 auto;
  min-height: 0;
}

/* 雷达图：固定高度，不产生滚动条 */
.dash-radar-chart-scroll {
  width: 100%;
  flex: 1 1 auto;
  min-height: 0;
  overflow: hidden;
}

.chart.dash-radar-chart-inner {
  flex: none;
  min-height: 240px;
  height: 240px;
}

.progress-list::-webkit-scrollbar {
  width: 4px;
}

.progress-list::-webkit-scrollbar-track {
  background: var(--color-primary-soft);
  border-radius: 2px;
}

.progress-list::-webkit-scrollbar-thumb {
  background: var(--color-primary-muted);
  border-radius: 2px;
}

.dark-table {
  flex: 0 1 auto;
  min-height: 0;
  overflow: visible;
  width: 100%;
}

.dark-table :deep(.el-table__body-wrapper) {
  overflow: visible;
}

.chart {
  width: 100%;
  flex: none;
  min-height: var(--dash-chart-h);
  height: var(--dash-chart-h);
}

/* 管理员图表卡：统一可视高度，折线/柱状/饼图在同一栅格内对齐 */
.dash-chart-card :deep(.el-card__body) {
  padding: 4px 10px 10px;
  justify-content: flex-start;
}

.chart-panel {
  min-height: var(--dash-chart-h);
  height: var(--dash-chart-h);
}

.chart-panel--pie {
  min-height: var(--dash-chart-pie-h);
  height: var(--dash-chart-pie-h);
}

/* 签到卡：列表由内层 dash-scroll-y 自动滚动；禁止横向滚动 */
.signin-pie-card :deep(.el-card__body) {
  overflow-x: hidden;
  overflow-y: hidden;
}

.signin-pie-card.dash-card--scroll-inner :deep(.el-card__body) {
  overflow-x: hidden;
  overflow-y: hidden;
}

.signin-pie-wrap {
  display: flex;
  gap: 12px;
  align-items: flex-start;
  flex: 1 1 auto;
  min-height: 0;
  width: 100%;
  min-width: 0;
  max-width: 100%;
  box-sizing: border-box;
  padding: 10px 2px;
}

.signin-chart {
  --signin-pie-size: min(148px, 36vw);
  flex: 0 0 auto;
  width: var(--signin-pie-size);
  min-width: var(--signin-pie-size);
  max-width: min(168px, 40vw);
  min-height: var(--signin-pie-size);
  height: var(--signin-pie-size);
}

.signin-names {
  flex: 1 1 auto;
  min-width: 0;
  max-width: none;
  width: auto;
  display: flex;
  gap: 12px;
  justify-content: flex-start;
  min-height: 0;
  overflow: hidden;
}

.signin-col {
  flex: 1 1 0;
  min-width: 0;
  max-width: none;
  min-height: 0;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.signin-col-title {
  flex-shrink: 0;
  font-size: 13px;
  font-weight: 700;
  color: var(--color-text);
  white-space: nowrap;
}

.signin-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  padding-right: 6px;
  flex: 0 1 auto;
  align-content: flex-start;
  overflow: visible;
}

/* 签到名单：标题固定，仅此处纵向滚动；每行两个 */
.signin-tags.signin-tags--list {
  flex-direction: row;
  flex-wrap: wrap;
  align-items: flex-start;
  align-content: flex-start;
  flex: 1 1 auto;
  min-height: 0;
  gap: 6px;
  padding-right: 4px;
}

.signin-tags.signin-tags--list.dash-scroll-y {
  overflow-x: hidden;
  overflow-y: auto;
}

.signin-tags.signin-tags--list :deep(.signin-tag) {
  flex: 0 0 calc(50% - 3px);
  width: calc(50% - 3px);
  max-width: none;
  height: auto !important;
  min-height: 26px;
  margin: 0;
  justify-content: center;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  box-sizing: border-box;
  font-size: 11px;
  padding: 0 4px;
}

.signin-tags.signin-tags--list :deep(.signin-tag .el-tag__content) {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.signin-tag {
  max-width: 100%;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.signin-empty {
  font-size: 12px;
  color: var(--color-text-muted);
}

.course-time-location-cards {
  flex: 0 1 auto;
  min-height: 0;
  overflow: visible;
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 12px;
  padding-right: 4px;
  align-content: start;
}

/* 与 .dash-scroll-y 同节点；须晚于上一段，避免 overflow:visible 盖掉纵向滚动与自动滚动 */
.course-time-location-cards.dash-scroll-y {
  overflow-x: hidden;
  overflow-y: auto;
}

.course-time-location-card {
  border-radius: var(--radius-md);
  border: 1px solid var(--color-border);
  background: var(--color-bg-elevated);
  padding: 14px;
  box-shadow: var(--shadow-soft);
  transition:
    border-color var(--duration) var(--ease-out),
    box-shadow var(--duration) var(--ease-out);
}

.course-time-location-card:hover {
  border-color: var(--color-primary-muted);
}

.course-time-location-name {
  font-size: 14px;
  font-weight: 700;
  color: var(--color-text);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin-bottom: 10px;
}

.course-time-location-row {
  font-size: 13px;
  color: var(--color-text-muted);
  line-height: 1.6;
  display: flex;
  gap: 8px;
}

.course-time-location-label {
  color: var(--color-text-subtle);
  flex-shrink: 0;
  min-width: 74px;
}

.course-time-location-value {
  color: var(--color-text);
  flex: 1;
  word-break: break-word;
}

.course-time-location-empty {
  font-size: 12px;
  color: var(--color-text-muted);
  padding: 10px 2px;
}

.dash-card {
  border-radius: var(--radius-md);
  border: 1px solid var(--color-border);
  background: var(--color-bg-elevated);
  box-shadow: var(--shadow-soft);
  overflow: hidden;
  transition:
    border-color var(--duration) var(--ease-out),
    box-shadow var(--duration) var(--ease-out);
}

.dash-card:hover {
  border-color: var(--color-primary-muted);
  box-shadow:
    0 16px 48px -20px rgba(15, 23, 42, 0.12),
    0 0 0 1px var(--color-primary-soft);
}

.dash-card :deep(.el-card__header) {
  border-bottom: 1px solid var(--color-border);
  background: linear-gradient(90deg, var(--color-primary-soft) 0%, transparent 100%);
  padding: 10px 12px;
}

.warn-box {
  font-size: 13px;
  color: var(--color-text-muted);
  display: flex;
  flex-direction: column;
  gap: 8px;
  flex: 1;
}

.similarity-alert {
  display: flex;
  flex-direction: column;
  gap: 10px;
  flex: 1;
  min-height: 0;
}

.similarity-header {
  font-size: 13px;
  color: var(--color-text-muted);
}

.similarity-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding-right: 4px;
  flex: 0 0 auto;
  overflow: visible;
}

.similarity-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 12px;
  border-radius: var(--radius-md);
  border: 1px solid var(--color-border);
  background: rgba(248, 250, 252, 0.85);
}

.similarity-top {
  display: flex;
  justify-content: space-between;
  gap: 10px;
  align-items: center;
}

.similarity-student {
  font-size: 13px;
  font-weight: 700;
  color: var(--color-text);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.similarity-compared {
  font-size: 12px;
  color: var(--color-text-muted);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.similarity-meta {
  font-size: 12px;
  font-weight: 600;
  color: var(--color-primary-hover);
}

.similarity-empty {
  font-size: 12px;
  color: var(--color-text-muted);
  padding: 10px 2px;
}

.dashboard-empty {
  margin-top: 12px;
  padding: 24px;
  background: var(--color-bg-elevated);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-soft);
}

@media (max-width: 992px) {
  .dashboard-page {
    --dash-row-short: min(268px, 48vh);
    --dash-row-tall: min(288px, 52vh);
    --dash-chart-h: 190px;
    --dash-chart-pie-h: 156px;
  }

  .dashboard-hero {
    flex-wrap: wrap;
  }

  .dashboard-hero__actions {
    width: 100%;
    margin-top: 8px;
  }

  .dashboard-course-select {
    width: 100%;
  }

  .nine-grid {
    grid-template-columns: 1fr;
    grid-template-rows: repeat(9, var(--dash-row-short));
    height: auto;
    min-height: 0;
    gap: 10px;
  }

  .nine-grid--admin {
    grid-template-columns: 1fr;
    grid-template-rows: repeat(9, var(--dash-row-short));
  }

  /* 窄屏单列：每格固定矮高度，内容在卡片内滚 */
  .nine-grid > .grid-block:nth-child(1),
  .nine-grid > .grid-block:nth-child(2),
  .nine-grid > .grid-block:nth-child(3),
  .nine-grid > .grid-block:nth-child(4),
  .nine-grid > .grid-block:nth-child(5),
  .nine-grid > .grid-block:nth-child(6),
  .nine-grid > .grid-block:nth-child(7),
  .nine-grid > .grid-block:nth-child(8),
  .nine-grid > .grid-block:nth-child(9) {
    grid-column: 1;
    grid-row: auto;
  }

  .grid-block {
    height: var(--dash-row-short);
    max-height: var(--dash-row-short);
    min-height: 0;
  }

  .nine-grid > .grid-block.bottom-block {
    height: var(--dash-row-tall);
    max-height: var(--dash-row-tall);
  }

  .kpi-grid--quad {
    grid-template-rows: repeat(2, 1fr);
  }
}
</style>

