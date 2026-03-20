<template>
  <div class="cockpit">
    <div class="cockpit-viewport">
      <div class="screen-bg" />
      <div class="cockpit-stage" :class="{ 'is-scaled': enableScale }" >
    <div class="page-header">
      <div>
        <div class="page-title">统计驾驶舱</div>
        <div class="page-subtitle" v-if="role === 'ADMIN'">管理员总览</div>
        <div class="page-subtitle" v-else-if="role === 'TEACHER'">教师看板</div>
        <div class="page-subtitle" v-else>当前角色暂无统计看板</div>
      </div>

      <div class="header-actions" v-if="role === 'TEACHER'">
        <el-select v-model="teacherCourseId" size="small" class="dark-select" placeholder="选择课程">
          <el-option v-for="c in teacherCourses" :key="c.id" :label="c.name" :value="c.id" />
        </el-select>
      </div>
    </div>

    <template v-if="role === 'ADMIN'">
      <div class="nine-grid">
        <!-- 第一行 -->
        <el-card class="grid-block cockpit-card" shadow="never">
          <template #header><div class="card-header">数据概览</div></template>
          <div class="kpi-grid">
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

        <el-card class="grid-block cockpit-card" shadow="never">
          <template #header><div class="card-header">作业状态分布</div></template>
          <div ref="mainLeft2Ref" class="chart" />
        </el-card>

        <el-card class="grid-block cockpit-card" shadow="never">
          <template #header><div class="card-header">课程数量趋势（按学期/时间）</div></template>
          <div ref="mainMid1Ref" class="chart" />
        </el-card>

        <!-- 第二行 -->
        <el-card class="grid-block cockpit-card" shadow="never">
          <template #header><div class="card-header">学生作业完成率 Top10</div></template>
          <div class="progress-list">
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

        <el-card class="grid-block cockpit-card" shadow="never">
          <template #header><div class="card-header">课程概览（最近）</div></template>
          <el-table :data="main.mid2" size="small" class="dark-table" style="width: 100%">
            <el-table-column prop="courseName" label="课程" min-width="140" />
            <el-table-column prop="teacherName" label="教师" width="90" />
            <el-table-column prop="clazzName" label="班级" width="110" />
            <el-table-column prop="studentCount" label="学生数" width="80" />
            <el-table-column prop="taskCount" label="任务数" width="80" />
          </el-table>
        </el-card>

        <el-card class="grid-block cockpit-card" shadow="never">
          <template #header><div class="card-header">教师职称分布</div></template>
          <div ref="mainRight1Ref" class="chart" />
        </el-card>

        <!-- 第三行 -->
        <el-card class="grid-block cockpit-card bottom-block" shadow="never">
          <template #header><div class="card-header">学生综合得分分析（雷达）</div></template>
          <div ref="mainMid3Ref" class="chart" />
        </el-card>

        <el-card class="grid-block cockpit-card bottom-block" shadow="never">
          <template #header><div class="card-header">低完成率预警</div></template>
          <div class="warn-box">
            <div class="warn-line">阈值：{{ main.right2.threshold }}%</div>
            <div class="warn-line">Top10 中低于阈值：{{ main.right2.lowRateCountInTop10 }} 人</div>
          </div>
        </el-card>

        <el-card class="grid-block cockpit-card bottom-block" shadow="never">
          <template #header><div class="card-header">近期作业记录</div></template>
          <el-table :data="main.right3" size="small" class="dark-table" style="width: 100%">
            <el-table-column prop="studentName" label="学生" width="90" />
            <el-table-column prop="workName" label="作业" min-width="140" show-overflow-tooltip />
            <el-table-column prop="state" label="状态" width="90" />
          </el-table>
        </el-card>
      </div>
    </template>

    <template v-else-if="role === 'TEACHER'">
      <div class="nine-grid">
        <!-- 第一行 -->
        <el-card class="grid-block cockpit-card" shadow="never">
          <template #header><div class="card-header">数据概览</div></template>
          <div class="kpi-grid">
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

        <el-card class="grid-block cockpit-card" shadow="never">
          <template #header><div class="card-header">作业状态分布</div></template>
          <div ref="screenLeft2Ref" class="chart" />
        </el-card>

        <el-card class="grid-block cockpit-card" shadow="never">
          <template #header><div class="card-header">课程上课时间地点（本教师）</div></template>
          <div class="course-time-location-cards">
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
        <el-card class="grid-block cockpit-card" shadow="never">
          <template #header><div class="card-header">学生作业完成率 Top10</div></template>
          <div class="progress-list">
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

        <el-card class="grid-block cockpit-card" shadow="never">
          <template #header><div class="card-header">课程概览</div></template>
          <el-table :data="screen.mid2" size="small" class="dark-table" style="width: 100%">
            <el-table-column prop="courseName" label="课程" min-width="140" />
            <el-table-column prop="clazzName" label="班级" width="110" />
            <el-table-column prop="studentCount" label="学生数" width="80" />
            <el-table-column prop="taskCount" label="任务数" width="80" />
          </el-table>
        </el-card>

        <el-card class="grid-block cockpit-card" shadow="never">
          <template #header><div class="card-header">签到情况（饼图）</div></template>

          <div class="signin-pie-wrap">
            <div ref="screenRight1Ref" class="chart signin-chart" />

            <div class="signin-names">
              <div class="signin-col">
                <div class="signin-col-title">
                  已签到（{{ screen.right1?.signedCount ?? 0 }}）
                </div>
                <div class="signin-tags">
                  <el-tag
                    v-for="n in signedNameList"
                    :key="n"
                    type="success"
                    size="small"
                    effect="dark"
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
                <div class="signin-tags">
                  <el-tag
                    v-for="n in unsignedNameList"
                    :key="n"
                    type="info"
                    size="small"
                    effect="dark"
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
        <el-card class="grid-block cockpit-card bottom-block" shadow="never">
          <template #header><div class="card-header">学生综合得分分析（雷达）</div></template>
          <div ref="screenMid3Ref" class="chart" />
        </el-card>

        <el-card class="grid-block cockpit-card bottom-block" shadow="never">
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

        <el-card class="grid-block cockpit-card bottom-block" shadow="never">
          <template #header><div class="card-header">实验任务阶段人数</div></template>
          <div ref="screenRight3Ref" class="chart" />
        </el-card>
      </div>
    </template>

    <template v-else>
      <el-empty description="当前角色暂无统计页面" />
    </template>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, nextTick, reactive, ref, watch } from 'vue'
import * as echarts from 'echarts'
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
const role = user?.role

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

let chartMainLeft2, chartMainMid1, chartMainMid3, chartMainRight1
let chartScreenLeft2, chartScreenMid3, chartScreenRight1, chartScreenRight3

const safeSet = (chart, option) => {
  if (!chart) return
  chart.setOption(option, true)
}

const cockpitPalette = ['#2DE2E6', '#1B74FF', '#A742FF', '#FFB74A', '#2AD37D', '#FF4D7D']
const axisLine = { lineStyle: { color: 'rgba(124, 199, 255, 0.25)' } }
const axisLabel = { color: 'rgba(215, 240, 255, 0.78)', fontSize: 11 }
const splitLine = { lineStyle: { color: 'rgba(124, 199, 255, 0.10)' } }

const renderBar = (chart, list, title) => {
  const x = list.map(i => i.name)
  const y = list.map(i => Number(i.value || 0))
  safeSet(chart, {
    color: cockpitPalette,
    title: { text: title, left: 'center', textStyle: { fontSize: 12 } },
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
          shadowBlur: 12,
          shadowColor: 'rgba(45, 226, 230, 0.18)',
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
    color: [cockpitPalette[3], cockpitPalette[4]],
    title: title ? { text: title, left: 'center', textStyle: { fontSize: 12 } } : { show: false },
    tooltip: { trigger: 'axis' },
    legend: { top: 10, textStyle: axisLabel },
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
          shadowBlur: 12,
          shadowColor: 'rgba(255, 183, 74, 0.18)',
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
          shadowBlur: 12,
          shadowColor: 'rgba(42, 211, 125, 0.18)',
        },
      },
    ],
  })
}

const renderLine = (chart, list, title) => {
  const x = list.map(i => i.label)
  const y = list.map(i => Number(i.value || 0))
  safeSet(chart, {
    color: cockpitPalette,
    title: { text: title, left: 'center', textStyle: { fontSize: 12 } },
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
        areaStyle: { opacity: 0.10 },
      },
    ],
  })
}

const renderPie = (chart, list, title) => {
  safeSet(chart, {
    color: cockpitPalette,
    title: { text: title, left: 'center', textStyle: { fontSize: 12 } },
    tooltip: { trigger: 'item' },
    series: [
      {
        type: 'pie',
        // 放大约 1.5 倍：35%->~52.5%，65%->~97.5%
        radius: ['52%', '97%'],
        center: ['50%', '45%'],
        // 去掉连接线与标签状态
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
    color: cockpitPalette,
    title: { text: title, left: 'center', textStyle: { fontSize: 12 } },
    tooltip: {},
    legend: { bottom: 8, type: 'scroll', textStyle: axisLabel },
    radar: {
      indicator: indicators,
      radius: '65%',
      center: ['50%', '50%'],
      axisName: axisLabel,
      splitLine: { lineStyle: { color: 'rgba(124, 199, 255, 0.14)' } },
      splitArea: { areaStyle: { color: ['rgba(10, 24, 44, 0.20)', 'rgba(10, 24, 44, 0.35)'] } },
      axisLine: { lineStyle: { color: 'rgba(124, 199, 255, 0.20)' } },
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
    renderPie(chartScreenRight1, pieData, '')
    renderLine(chartScreenRight3, screen.right3, '')
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
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  disposeCharts()
})
</script>

<style scoped>
:deep(.el-card) {
  --el-card-bg-color: transparent;
  --el-fill-color-blank: transparent;
}

:deep(.el-progress__text) {
  color: rgba(12, 119, 185, 0.989) !important;
}

:deep(.el-progress-bar__outer) {
  background: rgba(124, 199, 255, 0.12);
}

:deep(.el-progress-bar__inner) {
  background: linear-gradient(90deg, rgba(45, 226, 230, 1), rgba(27, 116, 255, 1));
}

:deep(.el-table) {
  --el-bg-color: rgb(38 166 177 / 19%);
  --el-table-border-color: rgba(124, 199, 255, 0.18);
  --el-table-header-bg-color: rgba(9, 24, 48, 0.65);
  --el-table-row-hover-bg-color: rgba(45, 226, 230, 0.08);
  --el-table-bg-color: rgba(8, 18, 36, 0.35);
  color: rgba(22, 132, 199, 0.854);
}

:deep(.el-table th.el-table__cell) {
  color: rgba(13, 204, 229, 0.968);
  font-weight: 800;
}

:deep(.el-table td.el-table__cell) {
  border-bottom: 1px solid rgba(124, 199, 255, 0.12);
}

/* 深色表格：去掉白色底色，统一成柔和深蓝 */
.dark-table :deep(th.el-table__cell),
.dark-table :deep(td.el-table__cell) {
  background-color: transparent !important;
}

.dark-table :deep(.el-table__header-wrapper),
.dark-table :deep(.el-table__body-wrapper),
.dark-table :deep(table) {
  background-color: transparent !important;
}

.cockpit {
  width: 100%;
  min-height: calc(100vh - 60px);
  background:
    radial-gradient(900px 500px at 20% 10%, rgba(45, 226, 230, 0.10), transparent 60%),
    radial-gradient(900px 500px at 80% 25%, rgba(27, 116, 255, 0.12), transparent 62%),
    radial-gradient(700px 420px at 55% 80%, rgba(167, 66, 255, 0.10), transparent 60%),
    linear-gradient(180deg, #061227 0%, #040b18 100%);
  overflow: hidden;
}

.cockpit-viewport {
  position: relative;
  width: 100%;
  height: 100%;
  min-height: calc(100vh - 60px);
  display: flex;
  /* 让内容区域铺满垂直方向，而不是整体垂直居中 */
  align-items: stretch;
  justify-content: center;
  padding: 12px;
  box-sizing: border-box;
}
/* .screen-bg {
  position: absolute;
  inset: 0;
  background:
    radial-gradient(800px 400px at 20% 15%, rgba(64, 158, 255, 0.28), transparent 60%),
    radial-gradient(900px 500px at 80% 20%, rgba(80, 200, 255, 0.20), transparent 60%),
    linear-gradient(180deg, rgba(10, 44, 80, 0.85), rgba(2, 10, 24, 0.9));
  filter: saturate(1.1);
  pointer-events: none;
} */

.cockpit-stage {
  inset: 0;
  box-sizing: border-box;
  padding: 16px;
}

.cockpit-stage.is-scaled {
  transform-origin: top left;
  padding: 18px 22px;
}

.page-header {
  display: flex;
  align-items: flex-start;
  justify-content: center;
  padding: 14px 16px;
  border-radius: 14px;
  margin-bottom: 14px;
  border: 1px solid rgba(124, 199, 255, 0.22);
  background: linear-gradient(180deg, rgba(9, 24, 48, 0.82) 0%, rgba(6, 18, 39, 0.58) 100%);
  box-shadow:
    inset 0 0 0 1px rgba(45, 226, 230, 0.06),
    0 10px 30px rgba(0, 0, 0, 0.35);
  position: relative;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 10px;
  position: absolute;
  right: 16px;
  top: 50%;
  transform: translateY(-50%);
}

.page-title {
  font-size: clamp(18px, 1.2vw, 22px);
  font-weight: 800;
  letter-spacing: 0.6px;
  color: rgba(232, 251, 255, 0.92);
  line-height: 1.2;
}

.page-subtitle {
  margin-top: 6px;
  font-size: 12px;
  color: rgba(215, 240, 255, 0.68);
}

.nine-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  grid-template-rows: 1fr 1fr 1.5fr;
  gap: 8px;
  height: calc(100vh - 80px);
  min-height: 0;
}

.grid-block {
  display: flex;
  flex-direction: column;
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
  height: 100%;
}

.kpi-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  grid-template-rows: repeat(2, 1fr);
  gap: 8px;
  height: 100%;
  padding: 4px 0;
  min-height: 0;
}

.kpi-item {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  justify-content: flex-start;
  padding: 10px;
  border-radius: 8px;
  background: rgba(9, 24, 48, 0.4);
  border: 1px solid rgba(124, 199, 255, 0.15);
}

.kpi-label {
  font-size: 12px;
  color: rgba(215, 240, 255, 0.70);
  margin-bottom: 8px;
}

.kpi-value {
  font-size: clamp(24px, 1.8vw, 32px);
  font-weight: 900;
  color: rgba(45, 226, 230, 0.95);
  text-shadow: 0 0 22px rgba(45, 226, 230, 0.20);
  letter-spacing: 0.5px;
}

.card-header {
  font-weight: 600;
  color: rgba(232, 251, 255, 0.90);
  letter-spacing: 0.3px;
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
  background: linear-gradient(180deg, rgba(45, 226, 230, 1), rgba(27, 116, 255, 1));
}

.progress-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
  flex: 1;
  overflow-y: auto;
  padding-right: 4px;
  min-height: 0;
}

.progress-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
  color: rgba(215, 240, 255, 0.72);
  font-size: 13px;
}

.progress-count {
  margin-top: 4px;
  font-size: 11px;
  color: rgba(124, 199, 255, 0.65);
  text-align: right;
}

.grid-block :deep(.el-card__body) {
  display: flex;
  flex-direction: column;
  flex: 1;
  overflow: hidden;
  padding: 10px 12px;
  min-height: 0;
}

.progress-list::-webkit-scrollbar {
  width: 4px;
}

.progress-list::-webkit-scrollbar-track {
  background: rgba(124, 199, 255, 0.1);
  border-radius: 2px;
}

.progress-list::-webkit-scrollbar-thumb {
  background: rgba(124, 199, 255, 0.3);
  border-radius: 2px;
}

.dark-table {
  flex: 1;
  overflow: hidden;
}

.dark-table :deep(.el-table__body-wrapper) {
  max-height: calc(100% - 40px);
  overflow-y: auto;
}

.chart {
  width: 100%;
  height: 100%;
  min-height: 0;
  flex: 1;
}

.signin-pie-wrap {
  display: flex;
  gap: 14px;
  width: 100%;
  height: 100%;
  min-height: 0;
  padding: 10px 2px;
}

.signin-chart {
  flex: 1;
  min-width: 0;
}

.signin-names {
  width: 240px;
  max-width: 40%;
  display: flex;
  gap: 14px;
  justify-content: space-between;
  overflow: hidden;
}

.signin-col {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.signin-col-title {
  font-size: 13px;
  font-weight: 700;
  color: rgba(232, 251, 255, 0.86);
  white-space: nowrap;
}

.signin-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  overflow-y: auto;
  padding-right: 6px;
  min-height: 0;
  max-height: 100%;
}

.signin-tag {
  max-width: 100%;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.signin-empty {
  font-size: 12px;
  color: rgba(215, 240, 255, 0.55);
}

.course-time-location-cards {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 12px;
  padding-right: 4px;
}

.course-time-location-card {
  border-radius: 12px;
  border: 1px solid rgba(124, 199, 255, 0.18);
  background: rgba(9, 24, 48, 0.35);
  padding: 12px 12px;
  box-shadow: 0 10px 24px rgba(0, 0, 0, 0.18);
}

.course-time-location-name {
  font-size: 14px;
  font-weight: 800;
  color: rgba(232, 251, 255, 0.92);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin-bottom: 10px;
}

.course-time-location-row {
  font-size: 13px;
  color: rgba(215, 240, 255, 0.72);
  line-height: 1.6;
  display: flex;
  gap: 8px;
}

.course-time-location-label {
  color: rgba(124, 199, 255, 0.75);
  flex-shrink: 0;
  min-width: 74px;
}

.course-time-location-value {
  color: rgba(232, 251, 255, 0.86);
  flex: 1;
  word-break: break-word;
}

.course-time-location-empty {
  font-size: 12px;
  color: rgba(215, 240, 255, 0.55);
  padding: 10px 2px;
}

.cockpit-card {
  border-radius: 14px;
  border: 1px solid rgba(124, 199, 255, 0.20);
  background: linear-gradient(180deg, rgba(9, 24, 48, 0.72) 0%, rgba(6, 18, 39, 0.45) 100%);
  box-shadow:
    inset 0 0 0 1px rgba(45, 226, 230, 0.05),
    0 12px 28px rgba(0, 0, 0, 0.35);
  overflow: hidden;
}

.cockpit-card :deep(.el-card__header) {
  border-bottom: 1px solid rgba(124, 199, 255, 0.14);
  background: linear-gradient(90deg, rgba(45, 226, 230, 0.08), rgba(27, 116, 255, 0.04));
  padding: 10px 12px;
}

.dark-select {
  width: 320px;
}

.dark-select :deep(.el-input__wrapper) {
  background: rgba(9, 24, 48, 0.65);
  box-shadow: inset 0 0 0 1px rgba(124, 199, 255, 0.25);
}

.dark-select :deep(.el-input__inner) {
  color: rgba(232, 251, 255, 0.90);
}

.warn-box {
  font-size: 13px;
  color: rgba(215, 240, 255, 0.72);
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.similarity-alert {
  display: flex;
  flex-direction: column;
  gap: 10px;
  height: 100%;
}

.similarity-header {
  font-size: 13px;
  color: rgba(215, 240, 255, 0.72);
}

.similarity-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  overflow-y: auto;
  min-height: 0;
  padding-right: 4px;
}

.similarity-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 10px 10px;
  border-radius: 12px;
  border: 1px solid rgba(124, 199, 255, 0.16);
  background: rgba(9, 24, 48, 0.30);
}

.similarity-top {
  display: flex;
  justify-content: space-between;
  gap: 10px;
  align-items: center;
}

.similarity-student {
  font-size: 13px;
  font-weight: 800;
  color: rgba(232, 251, 255, 0.92);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.similarity-compared {
  font-size: 12px;
  color: rgba(215, 240, 255, 0.65);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.similarity-meta {
  font-size: 12px;
  color: rgba(124, 199, 255, 0.75);
}

.similarity-empty {
  font-size: 12px;
  color: rgba(215, 240, 255, 0.55);
  padding: 10px 2px;
}

@media (max-width: 992px) {
  .cockpit-viewport {
    padding: 8px;
  }
  .cockpit-stage {
    padding: 8px;
  }
  .dark-select {
    width: 240px;
  }
  .nine-grid {
    grid-template-columns: 1fr;
    grid-template-rows: auto;
    height: auto;
    gap: 8px;
  }
  .grid-block {
    height: auto;
    min-height: 250px;
  }
  .kpi-grid {
    grid-template-columns: repeat(2, 1fr);
    height: auto;
    min-height: 180px;
  }
}
</style>

