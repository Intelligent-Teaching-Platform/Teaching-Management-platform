<template>
  <div class="dashboard-page">
    <div class="dashboard-viewport">
      <div class="dashboard-stage" :class="{ 'is-scaled': enableScale }">
        <header v-if="role === 'STUDENT'" class="student-dash-header card">
          <div class="student-dash-header__inner">
            <div class="student-dash-header__meta" role="group" aria-label="学生基本信息">
              <div class="student-dash-kv">
                <span class="student-dash-kv__k">姓名</span>
                <span class="student-dash-kv__v">{{ studentProfile.name }}</span>
              </div>
              <div class="student-dash-kv">
                <span class="student-dash-kv__k">专业</span>
                <span class="student-dash-kv__v">{{ studentProfile.major }}</span>
              </div>
              <div class="student-dash-kv">
                <span class="student-dash-kv__k">学号</span>
                <span class="student-dash-kv__v">{{ studentProfile.code }}</span>
              </div>
            </div>
            <div class="student-dash-header__aside">
              <el-dropdown trigger="click" placement="bottom-end">
                <button type="button" class="student-dash-credits">
                  <span class="student-dash-credits__label">实践学分</span>
                  <span class="student-dash-credits__value">{{ studentProfile.practiceEarned }}/{{ studentProfile.practiceTotal }}</span>
                  <el-icon class="student-dash-credits__icon"><ArrowDown /></el-icon>
                </button>
                <template #dropdown>
                  <el-dropdown-menu class="student-dash-credits-menu">
                    <el-dropdown-item disabled>学分构成与明细待与教务系统对接</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </div>
        </header>

        <header v-else class="dashboard-hero card">
          <div class="dashboard-hero__accent" aria-hidden="true" />
          <div class="dashboard-hero__icon" aria-hidden="true">
            <el-icon><DataBoard /></el-icon>
          </div>
          <div class="dashboard-hero__text">
            <h1 class="dashboard-hero__title">统计驾驶舱</h1>
            <p v-if="role === 'ADMIN'" class="dashboard-hero__sub">管理员总览 · 关键指标与趋势一目掌握</p>
            <p v-else-if="role === 'TEACHER'" class="dashboard-hero__sub">教师看板 · 按课程查看班级与任务数据</p>
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
      <div ref="dashGridRef" class="nine-grid nine-grid--admin">
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
          <div ref="adminProgressScrollEl" class="progress-list dash-scroll-y dash-scroll-y--fill">
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
          <div ref="adminCourseOverviewScrollEl" class="dash-scroll-y dash-scroll-y--fill dash-recent-table-scroll">
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
          <div class="title-pie-wrap" aria-label="教师职称分布图与明细">
            <div ref="mainRight1Ref" class="chart chart-panel chart-panel--pie title-pie-chart" />
            <div class="title-pie-legend" aria-label="教师职称分类说明">
              <div v-for="item in teacherTitleTextList" :key="item.name" class="title-pie-legend__item">
                <span class="title-pie-legend__name">{{ item.name }}：{{ item.value }}个</span>
              </div>
            </div>
          </div>
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
          <div ref="adminRecentWorkScrollEl" class="dash-scroll-y dash-scroll-y--fill dash-recent-table-scroll">
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
      <div ref="dashGridRef" class="nine-grid">
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
          <div ref="teacherCourseTimeScrollEl" class="course-time-location-cards dash-scroll-y dash-scroll-y--fill">
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
          <div ref="teacherProgressScrollEl" class="progress-list dash-scroll-y dash-scroll-y--fill">
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

        <el-card class="grid-block dash-card dash-card--scroll-inner" shadow="never">
          <template #header><div class="card-header">课程概览</div></template>
          <div ref="teacherCourseOverviewScrollEl" class="dash-scroll-y dash-scroll-y--fill dash-recent-table-scroll">
            <el-table :data="screen.mid2" size="small" class="dark-table" style="width: 100%">
              <el-table-column prop="courseName" label="课程" min-width="140" />
              <el-table-column prop="clazzName" label="班级" width="110" />
              <el-table-column prop="studentCount" label="学生数" width="80" />
              <el-table-column prop="taskCount" label="任务数" width="80" />
            </el-table>
          </div>
        </el-card>

        <el-card class="grid-block dash-card signin-pie-card dash-card--scroll-inner" shadow="never">
          <template #header><div class="card-header">签到情况（饼图）</div></template>

          <div class="signin-pie-outer">
            <div class="signin-pie-wrap">
              <div ref="screenRight1Ref" class="chart signin-chart" />

              <div class="signin-names">
                <div class="signin-col">
                  <div class="signin-col-title">
                    已签到（{{ screen.right1?.signedCount ?? 0 }}）
                  </div>
                  <div
                    ref="signinSignedScrollEl"
                    class="signin-name-list dash-scroll-y dash-scroll-y--fill"
                  >
                    <div
                      v-for="(n, idx) in signedNameList"
                      :key="'signed-' + idx + '-' + n"
                      class="signin-name-line signin-name-line--signed"
                      :title="n"
                    >
                      {{ n }}
                    </div>
                    <div v-if="signedNameList.length === 0" class="signin-empty">-</div>
                  </div>
                </div>

                <div class="signin-col">
                  <div class="signin-col-title">
                    未签到（{{ screen.right1?.unsignedCount ?? 0 }}）
                  </div>
                  <div
                    ref="signinUnsignedScrollEl"
                    class="signin-name-list dash-scroll-y dash-scroll-y--fill"
                  >
                    <div
                      v-for="(n, idx) in unsignedNameList"
                      :key="'unsigned-' + idx + '-' + n"
                      class="signin-name-line signin-name-line--unsigned"
                      :title="n"
                    >
                      {{ n }}
                    </div>
                    <div v-if="unsignedNameList.length === 0" class="signin-empty">-</div>
                  </div>
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

        <el-card class="grid-block dash-card bottom-block dash-card--scroll-inner" shadow="never">
          <template #header><div class="card-header">实验作业相似度预警</div></template>
          <div class="similarity-alert">
            <div class="similarity-header">
              阈值：{{ screen.right2.threshold ?? 0 }}%
            </div>

            <div
              ref="teacherSimilarityScrollEl"
              class="similarity-list dash-scroll-y dash-scroll-y--fill"
            >
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
      <div class="student-dash-main card">
        <div class="student-dash-columns">
          <section class="student-dash-col student-dash-col--timeline" aria-labelledby="student-journey-title">
            <div class="student-dash-section-head">
              <span id="student-journey-title" class="student-dash-section-head__title">学业时间轴</span>
              <span class="student-dash-section-head__hint">课程与实践节点（演示数据）</span>
            </div>

            <div class="student-timeline">
              <div
                v-for="(segment, si) in studentJourney"
                :key="segment.year"
                class="student-timeline-seg"
              >
                <div class="student-timeline-year" :aria-label="segment.year">{{ segment.year }}</div>
                <div class="student-timeline-body">
                  <div class="student-timeline-line" aria-hidden="true" />
                  <ul class="student-timeline-items">
                    <li
                      v-for="(node, ni) in segment.nodes"
                      :key="`${si}-${ni}-${node.title}`"
                      class="student-timeline-item"
                    >
                      <span class="student-timeline-dot" aria-hidden="true" />
                      <div class="student-timeline-card">
                        <span class="student-timeline-course">{{ node.title }}</span>
                        <div v-if="(node.detailLines || []).length" class="student-timeline-detail">
                          <p v-for="(line, li) in node.detailLines" :key="li">{{ line }}</p>
                        </div>
                      </div>
                    </li>
                  </ul>
                </div>
              </div>
            </div>
          </section>

          <section class="student-dash-col student-dash-col--growth" aria-labelledby="student-growth-title">
            <div class="student-dash-section-head student-dash-section-head--stack">
              <span id="student-growth-title" class="student-dash-section-head__title">能力成长曲线</span>
              <span class="student-dash-section-head__hint student-growth__hint">
                横轴与左侧「大一—大四」一致；仅右侧一条纵轴（综合能力 0–100）。实验报告提交数在悬停提示中查看（演示）
              </span>
            </div>
            <div ref="studentGrowthChartRef" class="student-growth__chart" role="img" aria-label="能力与实验报告统计图" />
          </section>

          <aside class="student-dash-col student-dash-col--footprint" aria-labelledby="student-footprint-title">
            <div class="student-dash-section-head student-dash-section-head--side">
              <span id="student-footprint-title" class="student-dash-section-head__bar" aria-hidden="true" />
              <span class="student-dash-section-head__title">学习足迹</span>
            </div>
            <p class="student-footprint__sub">按时间记录的学习行为（签到、作业等，演示数据）</p>
            <ul class="student-footprint-feed" aria-label="学习行为时间线">
              <li
                v-for="(ev, ei) in studentFootprintEvents"
                :key="`${ev.time}-${ev.tag}-${ei}`"
                class="student-footprint-feed__item"
              >
                <time class="student-footprint-feed__time" :datetime="ev.iso">{{ ev.time }}</time>
                <div class="student-footprint-feed__body">
                  <span class="student-footprint-feed__tag" :class="`student-footprint-feed__tag--${ev.kind}`">{{ ev.tag }}</span>
                  <span class="student-footprint-feed__detail">{{ ev.detail }}</span>
                </div>
              </li>
            </ul>
          </aside>
        </div>
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
import { ArrowDown, DataBoard } from '@element-plus/icons-vue'
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

/** 学生端：与登录信息对齐的展示字段（专业等后端未单独返回时用学院或占位） */
const studentProfile = computed(() => {
  const name = (user?.name || user?.username || '').trim() || '同学'
  const code = (user?.code != null && String(user.code).trim() !== '') ? String(user.code).trim() : '—'
  const majorRaw = user?.major || user?.specialityName || user?.collegeName
  const major = (majorRaw != null && String(majorRaw).trim() !== '')
    ? String(majorRaw).trim()
    : '计算机科学与技术'
  const scoreNum = Number(user?.score)
  const practiceEarned = Number.isFinite(scoreNum) && scoreNum > 0 ? Math.round(scoreNum) : 128
  return {
    name,
    code,
    major,
    practiceEarned,
    practiceTotal: 150,
  }
})

/**
 * 学生端学业时间轴；与能力成长曲线共用 stage（year）维度。
 * abilityIndex：演示用综合能力指数；labReports / homeworkDone：与本阶段课程相关的提交量（演示，可对接档案接口）
 */
const studentJourney = [
  {
    year: '大一',
    abilityIndex: 42,
    labReports: 4,
    homeworkDone: 14,
    nodes: [
      {
        title: '程序设计基础',
        detailLines: ['得分: 92/100', '考核结果: 优秀', '本阶段实验报告: 2 份 · 课后作业: 6 次'],
      },
    ],
  },
  {
    year: '大二',
    abilityIndex: 55,
    labReports: 7,
    homeworkDone: 18,
    nodes: [
      {
        title: '数字逻辑',
        detailLines: ['项目类型: 课程实验', '考核结果: 优秀', '本阶段实验报告: 3 份 · 课后作业: 8 次'],
      },
    ],
  },
  {
    year: '大三',
    abilityIndex: 72,
    labReports: 12,
    homeworkDone: 22,
    nodes: [
      {
        title: '计算机组成原理',
        detailLines: ['项目类型: 团队项目', '考核结果: 良好', '实验报告: 4 份'],
      },
      {
        title: '嵌入式与接口技术',
        detailLines: ['项目类型: 团队项目', '考核结果: 良好', '实验报告: 5 份'],
      },
      {
        title: '计算机组成与结构课程设计',
        detailLines: ['项目类型: 团队项目', '考核结果: 良好', '综合报告: 3 份'],
      },
    ],
  },
  {
    year: '大四',
    abilityIndex: 81,
    labReports: 15,
    homeworkDone: 9,
    nodes: [
      {
        title: '专业能力综合实训',
        detailLines: ['阶段: 开题与中期', '考核结果: 进行中', '已交实训报告: 3 份'],
      },
    ],
  },
]

/**
 * 学习足迹：按时间倒序的行为记录（演示数据，可对接签到/作业/测验等接口）
 * kind：用于标签配色 signin | homework | lab | quiz | resource | other
 */
const studentFootprintEvents = [
  { iso: '2026-04-15T09:12:00', time: '04-15 09:12', tag: '签到', detail: '高等数学 · 第 8 周课堂', kind: 'signin' },
  { iso: '2026-04-14T21:35:00', time: '04-14 21:35', tag: '提交作业', detail: '数据结构 · 第三章在线练习', kind: 'homework' },
  { iso: '2026-04-14T18:20:00', time: '04-14 18:20', tag: '实验提交', detail: '计算机组成原理 · 实验三报告', kind: 'lab' },
  { iso: '2026-04-13T16:05:00', time: '04-13 16:05', tag: '随堂测验', detail: '操作系统 · 进程调度小测', kind: 'quiz' },
  { iso: '2026-04-12T14:40:00', time: '04-12 14:40', tag: '资源学习', detail: '已查看课件：嵌入式接口技术 Week6', kind: 'resource' },
  { iso: '2026-04-11T08:58:00', time: '04-11 08:58', tag: '签到', detail: '大学英语 · 线下考勤', kind: 'signin' },
  { iso: '2026-04-10T19:22:00', time: '04-10 19:22', tag: '提交作业', detail: '概率论 · 习题册 Batch-2', kind: 'homework' },
  { iso: '2026-04-09T11:30:00', time: '04-09 11:30', tag: '讨论区回复', detail: '专业能力实训 · 开题答疑帖', kind: 'other' },
]

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
    { name: '工程师', value: 2 },
    { name: '高级工程师(副高级)', value: 1 },
    { name: '讲师(中级)', value: 6 },
    { name: '副教授', value: 3 },
    { name: '教授(正高级)', value: 2 },
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

const teacherTitleBuckets = [
  '工程师',
  '高级工程师(副高级)',
  '讲师(中级)',
  '副教授',
  '教授(正高级)',
]

const normalizeTeacherTitleBucket = (rawTitle) => {
  const t = String(rawTitle || '').replace(/\s+/g, '').trim()
  if (!t) return null

  if (t.includes('教授') && !t.includes('副教授')) return '教授(正高级)'
  if (t.includes('副教授')) return '副教授'
  if (t.includes('高级工程师') || (t.includes('工程师') && t.includes('副高级'))) return '高级工程师(副高级)'
  if (t.includes('讲师') || t.includes('中级')) return '讲师(中级)'
  if (t.includes('工程师')) return '工程师'

  return null
}

/**
 * 将后端教师 title 原始统计归并到指定 5 类；未知职称不纳入图表。
 */
const buildTeacherTitleStats = (rawList) => {
  const base = Object.fromEntries(teacherTitleBuckets.map((k) => [k, 0]))
  const list = Array.isArray(rawList) ? rawList : []

  list.forEach((item) => {
    const bucket = normalizeTeacherTitleBucket(item?.name)
    if (!bucket) return
    base[bucket] += Number(item?.value || 0)
  })

  return teacherTitleBuckets.map((name) => ({ name, value: base[name] }))
}

/** 职称饼图右侧说明：与 main.right1 顺序一致，保证始终有 5 行 */
const teacherTitleTextList = computed(() => {
  const stats = Array.isArray(main.right1) ? main.right1 : []
  const map = Object.fromEntries(stats.map((i) => [i?.name, Number(i?.value || 0)]))
  return teacherTitleBuckets.map((name) => ({ name, value: map[name] || 0 }))
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

/** 卡片内列表自动滚动（管理员：Top10·课程表·作业；教师：时间地点·Top10·课程表·签到名单·相似度预警；雷达图不参与） */
const adminProgressScrollEl = ref(null)
const adminCourseOverviewScrollEl = ref(null)
const adminRecentWorkScrollEl = ref(null)
const adminRadarScrollEl = ref(null)
const teacherRadarScrollEl = ref(null)
const teacherCourseTimeScrollEl = ref(null)
const teacherProgressScrollEl = ref(null)
const teacherCourseOverviewScrollEl = ref(null)
const signinSignedScrollEl = ref(null)
const signinUnsignedScrollEl = ref(null)
const teacherSimilarityScrollEl = ref(null)
const dashGridRef = ref(null)
const studentGrowthChartRef = ref(null)

let dashAutoScrollTeardowns = []
let dashGridResizeObserver = null
let autoScrollDebounceTimer = null

function scheduleSetupDashAutoScrolls() {
  if (autoScrollDebounceTimer != null) window.clearTimeout(autoScrollDebounceTimer)
  autoScrollDebounceTimer = window.setTimeout(() => {
    autoScrollDebounceTimer = null
    void setupDashAutoScrolls()
  }, 200)
}

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

function bindAutoVerticalScroll(el, speed = 0.32) {
  if (!el || el.nodeType !== 1) return null

  const getStyle = () => window.getComputedStyle(el)
  const canScroll = () => {
    const st = getStyle()
    const y = st.overflowY
    if (y !== 'auto' && y !== 'scroll' && y !== 'overlay') return false
    return el.scrollHeight > el.clientHeight + 2
  }
  if (!canScroll()) {
    return null
  }

  let raf = 0
  let paused = false
  let stopped = false

  const step = () => {
    if (stopped) return
    if (paused) {
      raf = requestAnimationFrame(step)
      return
    }
    if (!canScroll()) {
      raf = requestAnimationFrame(step)
      return
    }
    const sh = el.scrollHeight
    const ch = el.clientHeight
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
  await new Promise((r) => requestAnimationFrame(r))
  await new Promise((r) => requestAnimationFrame(r))
  await new Promise((r) => setTimeout(r, 120))
  const nodes = []
  if (role === 'ADMIN') {
    if (adminProgressScrollEl.value) nodes.push(adminProgressScrollEl.value)
    if (adminCourseOverviewScrollEl.value) nodes.push(adminCourseOverviewScrollEl.value)
    if (adminRecentWorkScrollEl.value) nodes.push(adminRecentWorkScrollEl.value)
  }
  if (role === 'TEACHER') {
    if (teacherCourseTimeScrollEl.value) nodes.push(teacherCourseTimeScrollEl.value)
    if (teacherProgressScrollEl.value) nodes.push(teacherProgressScrollEl.value)
    if (teacherCourseOverviewScrollEl.value) nodes.push(teacherCourseOverviewScrollEl.value)
    if (signinSignedScrollEl.value) nodes.push(signinSignedScrollEl.value)
    if (signinUnsignedScrollEl.value) nodes.push(signinUnsignedScrollEl.value)
    if (teacherSimilarityScrollEl.value) nodes.push(teacherSimilarityScrollEl.value)
  }
  nodes.forEach((el) => {
    const off = bindAutoVerticalScroll(el)
    if (off) dashAutoScrollTeardowns.push(off)
  })
}

let chartMainLeft2, chartMainMid1, chartMainMid3, chartMainRight1
let chartScreenLeft2, chartScreenMid3, chartScreenRight1, chartScreenRight3
let chartStudentGrowth

const safeSet = (chart, option) => {
  if (!chart) return
  chart.setOption(option, true)
}

/** 与全局主色（teal）协调的图表色组，避免高饱和紫/粉 */
const dashPalette = ['#0d9488', '#14b8a6', '#0e7490', '#f59e0b', '#0891b2', '#059669']
const chartTitleStyle = { color: '#64748b', fontSize: 12, fontWeight: 600 }
const axisLine = { lineStyle: { color: 'rgba(15, 23, 42, 0.12)' } }
const axisLabel = { color: '#64748b', fontSize: 11 }
/** 饼图悬浮提示：比默认 14px 更小，与轴标签协调 */
const pieTooltipTextStyle = { color: '#64748b', fontSize: 10, lineHeight: 14 }
/** 饼图 tooltip 外框：内边距与圆角略小于默认，整体更紧凑 */
const pieTooltip = {
  trigger: 'item',
  textStyle: pieTooltipTextStyle,
  padding: [3, 6],
  borderWidth: 1,
  borderRadius: 4,
}
const splitLine = { lineStyle: { color: 'rgba(15, 23, 42, 0.06)' } }

const readCssColor = (name, fallback) => {
  try {
    const v = getComputedStyle(document.documentElement).getPropertyValue(name).trim()
    return v || fallback
  } catch (_) {
    return fallback
  }
}

const renderStudentGrowthCurve = () => {
  if (!chartStudentGrowth) return
  const primary = readCssColor('--color-primary', '#0d9488')
  const primaryHover = readCssColor('--color-primary-hover', '#0f766e')
  const segments = Array.isArray(studentJourney) ? studentJourney : []
  const categories = segments.map((s) => s.year)
  const ability = segments.map((s) => Number(s.abilityIndex ?? 0))
  const labReports = segments.map((s) => Number(s.labReports ?? 0))
  const homeworkDone = segments.map((s) => Number(s.homeworkDone ?? 0))
  const courseCounts = segments.map((s) => (Array.isArray(s.nodes) ? s.nodes.length : 0))
  chartStudentGrowth.setOption({
    animationDuration: 480,
    color: [primary],
    legend: { show: false },
    grid: {
      left: 12,
      right: 12,
      top: 26,
      bottom: 28,
      containLabel: true,
    },
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(15, 23, 42, 0.92)',
      borderWidth: 0,
      textStyle: { color: '#f8fafc', fontSize: 12 },
      axisPointer: { type: 'line', lineStyle: { color: `${primary}66`, width: 1 } },
      formatter: (params) => {
        const list = Array.isArray(params) ? params : [params]
        if (!list.length) return ''
        const idx = list[0].dataIndex
        const stage = segments[idx]
        if (!stage) return ''
        const courses = (stage.nodes || []).map((n) => n.title).filter(Boolean)
        const courseLine = courses.length ? `课程：${courses.join('、')}` : ''
        const abilityVal = list[0].value != null ? list[0].value : ability[idx]
        return [
          `<div style="font-weight:700;margin-bottom:6px">${stage.year}（与左侧时间轴同阶段）</div>`,
          `<div>${list[0].marker}综合能力（右侧纵轴）：<b>${abilityVal}</b></div>`,
          `<div style="margin-top:6px;opacity:.95">实验报告累计提交：<b>${labReports[idx] ?? 0}</b> 份</div>`,
          `<div style="opacity:.95">课后作业提交：<b>${homeworkDone[idx] ?? 0}</b> 次</div>`,
          `<div style="opacity:.95">本阶段开课：<b>${courseCounts[idx] ?? 0}</b> 门</div>`,
          courseLine ? `<div style="margin-top:4px;font-size:11px;opacity:.85;max-width:240px">${courseLine}</div>` : '',
        ]
          .filter(Boolean)
          .join('<br/>')
      },
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: categories,
      axisLine,
      axisTick: { alignWithLabel: true },
      axisLabel: {
        ...axisLabel,
        color: primaryHover,
        fontWeight: 700,
        fontSize: 11,
        rotate: categories.length > 5 ? 24 : 0,
      },
    },
    yAxis: {
      type: 'value',
      position: 'right',
      name: '综合能力',
      nameLocation: 'middle',
      nameRotate: 90,
      nameGap: 40,
      min: 0,
      max: 100,
      splitNumber: 5,
      axisLine: { show: false },
      axisLabel: { ...axisLabel, color: primaryHover, margin: 10 },
      splitLine,
      nameTextStyle: { color: '#64748b', fontSize: 11, padding: [0, 0, 0, 4] },
    },
    series: [
      {
        name: '综合能力',
        type: 'line',
        smooth: 0.35,
        symbol: 'circle',
        symbolSize: 9,
        showSymbol: true,
        lineStyle: { width: 3, color: primary },
        itemStyle: { color: '#fff', borderColor: primaryHover, borderWidth: 2 },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: `${primary}44` },
            { offset: 1, color: `${primary}08` },
          ]),
        },
        data: ability,
      },
    ],
  }, true)
}

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
    tooltip: pieTooltip,
    series: [
      {
        type: 'pie',
        // 职称分布：略小的环，留白多一些（原 52%–97% 过满）
        radius: ['38%', '68%'],
        center: ['44%', '50%'],
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
    tooltip: pieTooltip,
    series: [
      {
        type: 'pie',
        radius: ['40%', '68%'],
        center: ['44%', '50%'],
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
    legend: { bottom: 22, type: 'scroll', textStyle: { ...axisLabel } },
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
  } else if (role === 'STUDENT' && studentGrowthChartRef.value) {
    chartStudentGrowth = echarts.init(studentGrowthChartRef.value)
  }
}

const disposeCharts = () => {
  ;[chartMainLeft2, chartMainMid1, chartMainMid3, chartMainRight1, chartScreenLeft2, chartScreenMid3, chartScreenRight1, chartScreenRight3, chartStudentGrowth]
    .filter(Boolean)
    .forEach(c => c.dispose())
  chartStudentGrowth = undefined
}

const resizeAllCharts = () => {
  ;[chartMainLeft2, chartMainMid1, chartMainMid3, chartMainRight1, chartScreenLeft2, chartScreenMid3, chartScreenRight1, chartScreenRight3, chartStudentGrowth]
    .filter(Boolean)
    .forEach(c => c.resize())
}

const handleResize = () => {
  viewport.w = window.innerWidth
  viewport.h = window.innerHeight
  stageScale.value = enableScale.value ? Math.min(viewport.w / DESIGN_W, viewport.h / DESIGN_H) : 1
  resizeAllCharts()
  scheduleSetupDashAutoScrolls()
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
    const adminTitleRaw =
      results[6].status === 'fulfilled'
        ? results[6].value?.data || adminFallback.right1
        : adminFallback.right1
    main.right1 = buildTeacherTitleStats(adminTitleRaw)
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
    scheduleSetupDashAutoScrolls()
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
    scheduleSetupDashAutoScrolls()
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
  } else if (role === 'STUDENT') {
    await nextTick()
    if (!chartStudentGrowth && studentGrowthChartRef.value) {
      chartStudentGrowth = echarts.init(studentGrowthChartRef.value)
    }
    renderStudentGrowthCurve()
  }
  await nextTick()
  resizeAllCharts()
  await setupDashAutoScrolls()
  scheduleSetupDashAutoScrolls()
  if (typeof ResizeObserver !== 'undefined' && dashGridRef.value) {
    dashGridResizeObserver = new ResizeObserver(() => scheduleSetupDashAutoScrolls())
    dashGridResizeObserver.observe(dashGridRef.value)
  }
})

onBeforeUnmount(() => {
  teardownDashAutoScrolls()
  if (dashGridResizeObserver) {
    dashGridResizeObserver.disconnect()
    dashGridResizeObserver = null
  }
  if (autoScrollDebounceTimer != null) {
    window.clearTimeout(autoScrollDebounceTimer)
    autoScrollDebounceTimer = null
  }
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
/* flex-basis:0 才能让子项在 flex 布局下低于内容高度，从而出现 overflow 与自动滚动 */
.grid-block.dash-card--scroll-inner :deep(.el-card__body) {
  overflow: hidden;
  min-height: 0;
  flex: 1 1 0;
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

/* 栅格卡片内铺满剩余高度，内容超出时纵向滚动（Top10 / 课程表 / 作业记录 / 签到整卡） */
.dash-scroll-y.dash-scroll-y--fill {
  max-height: none;
  flex: 1 1 0;
  min-height: 0;
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

.dash-radar-chart-scroll {
  width: 100%;
  flex: 1 1 auto;
  min-height: 0;
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

.title-pie-wrap {
  display: grid;
  grid-template-columns: minmax(128px, 0.9fr) minmax(140px, 1fr);
  gap: 12px;
  align-items: stretch;
  width: 100%;
  min-height: 0;
  flex: 1 1 auto;
}

.title-pie-chart {
  min-width: 0;
  justify-self: start;
  padding-left: 2px;
}

.title-pie-legend {
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  background: color-mix(in srgb, var(--color-primary-soft) 12%, var(--color-bg-elevated));
  padding: 9px;
  display: flex;
  flex-direction: column;
  gap: 7px;
  overflow: auto;
  min-height: 0;
  max-width: 100%;
}

.title-pie-legend__item {
  display: grid;
  grid-template-columns: minmax(0, 1fr);
  gap: 6px;
  align-items: baseline;
  padding: 7px 8px;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.7);
}

.title-pie-legend__name {
  font-size: 11.5px;
  line-height: 1.35;
  color: var(--color-text-muted);
  font-weight: 400;
  white-space: nowrap;
}



@media (max-width: 900px) {
  .title-pie-wrap {
    grid-template-columns: 1fr;
  }

  .title-pie-chart {
    min-height: 150px;
    height: 150px;
  }
}

/* 签到卡：左侧饼图与标题固定，仅两侧名单区域独立纵向滚动 */
.signin-pie-card :deep(.el-card__body) {
  overflow-x: hidden;
  overflow-y: hidden;
}

.signin-pie-card.dash-card--scroll-inner :deep(.el-card__body) {
  overflow-x: hidden;
  overflow-y: hidden;
}

.signin-pie-outer {
  flex: 1 1 0;
  min-height: 0;
  width: 100%;
  display: flex;
  flex-direction: column;
}

.signin-pie-wrap {
  display: flex;
  gap: 12px;
  align-items: stretch;
  flex: 1 1 0;
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
  align-self: flex-start;
  width: var(--signin-pie-size);
  min-width: var(--signin-pie-size);
  max-width: min(168px, 40vw);
  min-height: var(--signin-pie-size);
  height: var(--signin-pie-size);
}

.signin-names {
  flex: 1 1 0;
  min-width: 0;
  display: flex;
  gap: 12px;
  justify-content: stretch;
  align-items: stretch;
  min-height: 0;
}

.signin-col {
  flex: 1 1 0;
  min-width: 0;
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

/* 单列纵向列表：仅此处 overflow；每人单行一条，过长省略号 */
.signin-name-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding-right: 4px;
}

.signin-name-line {
  flex-shrink: 0;
  width: 100%;
  box-sizing: border-box;
  padding: 5px 8px;
  border-radius: var(--radius-sm);
  font-size: 12px;
  font-weight: 400;
  line-height: 1.35;
  color: var(--color-text);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.signin-name-line--signed {
  background: rgba(16, 185, 129, 0.1);
  border: 1px solid rgba(16, 185, 129, 0.28);
}

.signin-name-line--unsigned {
  background: rgba(148, 163, 184, 0.12);
  border: 1px solid rgba(148, 163, 184, 0.35);
}

.signin-empty {
  font-size: 12px;
  color: var(--color-text-muted);
  padding: 4px 0;
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
  flex-shrink: 0;
  font-size: 13px;
  color: var(--color-text-muted);
}

/* 阈值行固定，列表区单独滚动并参与自动滚动 */
.similarity-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding-right: 4px;
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

/* —— 学生端：个人成长看板（与全局主色协调） —— */
.student-dash-header {
  position: relative;
  overflow: hidden;
  margin-bottom: 14px;
  padding: 0;
  border: none;
  background: linear-gradient(
    105deg,
    var(--color-primary) 0%,
    var(--color-primary-hover) 48%,
    color-mix(in srgb, var(--color-primary) 72%, #0f172a) 100%
  );
  box-shadow:
    0 18px 40px -22px color-mix(in srgb, var(--color-primary) 55%, transparent),
    0 0 0 1px color-mix(in srgb, var(--color-primary) 35%, transparent);
}

.student-dash-header__inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
  padding: 16px 20px;
}

.student-dash-header__meta {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 10px 28px;
  min-width: 0;
}

.student-dash-kv {
  display: inline-flex;
  align-items: baseline;
  gap: 8px;
  font-size: 13px;
  color: rgba(248, 250, 252, 0.92);
}

.student-dash-kv__k {
  font-weight: 600;
  letter-spacing: 0.02em;
  opacity: 0.88;
}

.student-dash-kv__k::after {
  content: '：';
}

.student-dash-kv__v {
  font-weight: 700;
  letter-spacing: -0.01em;
  color: #fff;
}

.student-dash-header__aside {
  flex-shrink: 0;
}

.student-dash-credits {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  margin: 0;
  padding: 8px 14px;
  border: 1px solid rgba(255, 255, 255, 0.35);
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.12);
  color: #fff;
  font: inherit;
  cursor: pointer;
  transition:
    background var(--duration) var(--ease-out),
    border-color var(--duration) var(--ease-out);
}

.student-dash-credits:hover {
  background: rgba(255, 255, 255, 0.2);
  border-color: rgba(255, 255, 255, 0.55);
}

.student-dash-credits__label {
  font-size: 12px;
  font-weight: 600;
  opacity: 0.92;
}

.student-dash-credits__value {
  font-size: 14px;
  font-weight: 800;
  letter-spacing: -0.02em;
}

.student-dash-credits__icon {
  font-size: 14px;
  opacity: 0.85;
}

.student-dash-main {
  padding: 0;
  overflow: hidden;
}

.student-dash-columns {
  display: grid;
  grid-template-columns: minmax(0, 1.25fr) minmax(220px, 1fr) minmax(240px, 1.05fr);
  gap: 0;
  min-height: min(640px, 72vh);
  align-items: stretch;
}

.student-dash-col {
  padding: 20px 22px 24px;
  box-sizing: border-box;
  min-width: 0;
}

.student-dash-col--timeline {
  border-right: 1px solid var(--color-border);
  background: linear-gradient(180deg, var(--color-bg-elevated) 0%, rgba(248, 250, 252, 0.65) 100%);
}

.student-dash-col--growth {
  display: flex;
  flex-direction: column;
  min-width: 0;
  border-right: 1px solid var(--color-border);
  background: linear-gradient(180deg, rgba(248, 250, 252, 0.55) 0%, var(--color-bg-elevated) 100%);
}

.student-growth__hint {
  max-width: 42ch;
  line-height: 1.45;
}

.student-dash-col--growth .student-growth__chart {
  flex: 1;
  min-height: min(280px, 36vh);
  width: 100%;
  box-sizing: border-box;
  padding-right: 4px;
}

.student-dash-col--footprint {
  background: var(--color-bg-elevated);
  min-width: 0;
  overflow: hidden;
}

.student-dash-section-head {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}

.student-dash-section-head__title {
  font-size: 15px;
  font-weight: 800;
  letter-spacing: -0.02em;
  color: var(--color-text);
}

.student-dash-section-head__hint {
  font-size: 12px;
  color: var(--color-text-muted);
}

.student-dash-section-head--side {
  justify-content: flex-start;
  gap: 10px;
  margin-bottom: 10px;
}

.student-dash-section-head--stack {
  flex-direction: column;
  align-items: flex-start;
  justify-content: flex-start;
  gap: 4px;
  margin-bottom: 12px;
}

.student-dash-section-head--stack .student-dash-section-head__hint {
  margin-top: 0;
}

.student-dash-section-head__bar {
  width: 4px;
  height: 1.1em;
  border-radius: 999px;
  background: linear-gradient(180deg, var(--color-primary) 0%, var(--color-primary-hover) 100%);
}

.student-timeline {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.student-timeline-seg {
  display: grid;
  grid-template-columns: 52px minmax(0, 1fr);
  gap: 12px;
  align-items: start;
}

.student-timeline-year {
  flex-shrink: 0;
  padding: 6px 8px;
  border-radius: 8px;
  font-size: 11px;
  font-weight: 800;
  text-align: center;
  letter-spacing: 0.06em;
  color: #fff;
  background: linear-gradient(145deg, var(--color-primary) 0%, var(--color-primary-hover) 100%);
  box-shadow: 0 6px 16px -8px color-mix(in srgb, var(--color-primary) 65%, transparent);
}

.student-timeline-body {
  position: relative;
  padding-left: 14px;
  min-width: 0;
}

.student-timeline-line {
  position: absolute;
  left: 5px;
  top: 10px;
  bottom: 8px;
  width: 0;
  border-left: 2px dashed color-mix(in srgb, var(--color-primary) 38%, var(--color-border));
}

.student-timeline-items {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.student-timeline-item {
  position: relative;
  display: grid;
  grid-template-columns: 14px minmax(0, 1fr);
  gap: 10px;
  align-items: start;
}

.student-timeline-dot {
  position: relative;
  top: 8px;
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: #fff;
  border: 2px solid var(--color-primary-hover);
  box-shadow: 0 0 0 3px var(--color-primary-soft);
  z-index: 1;
}

.student-timeline-card {
  padding: 10px 12px;
  border-radius: var(--radius-md);
  border: 1px solid var(--color-border);
  background: rgba(255, 255, 255, 0.82);
  box-shadow: 0 10px 28px -22px rgba(15, 23, 42, 0.25);
}

.student-timeline-course {
  display: block;
  font-size: 13px;
  font-weight: 700;
  color: var(--color-text);
  letter-spacing: -0.01em;
}

.student-timeline-detail {
  margin-top: 8px;
  padding-top: 8px;
  border-top: 1px dashed var(--color-border);
}

.student-timeline-detail p {
  margin: 0;
  font-size: 12px;
  line-height: 1.55;
  color: var(--color-text-muted);
}

.student-timeline-detail p + p {
  margin-top: 2px;
}

.student-growth__chart {
  width: 100%;
  height: min(240px, 28vh);
  min-height: 200px;
}

.student-footprint__sub {
  margin: 0 0 14px;
  font-size: 12px;
  color: var(--color-text-muted);
  line-height: 1.5;
  overflow-wrap: anywhere;
  word-break: break-word;
}

.student-footprint-feed {
  list-style: none;
  margin: 0;
  padding: 12px 10px 14px;
  max-height: min(720px, 70vh);
  overflow-y: auto;
  border-radius: var(--radius-md);
  border: 1px solid color-mix(in srgb, var(--color-primary) 12%, var(--color-border));
  background:
    radial-gradient(100% 70% at 0% 0%, var(--color-primary-soft) 0%, transparent 55%),
    linear-gradient(180deg, rgba(248, 250, 252, 0.95) 0%, rgba(241, 245, 249, 0.5) 100%);
  scrollbar-gutter: stable;
}

.student-footprint-feed__item {
  display: grid;
  grid-template-columns: 76px minmax(0, 1fr);
  gap: 10px 12px;
  align-items: start;
  padding: 10px 0;
  border-bottom: 1px dashed var(--color-border);
}

.student-footprint-feed__item:last-child {
  border-bottom: none;
  padding-bottom: 4px;
}

.student-footprint-feed__time {
  font-size: 11px;
  font-weight: 700;
  font-variant-numeric: tabular-nums;
  color: var(--color-text-subtle);
  letter-spacing: 0.02em;
  white-space: nowrap;
}

.student-footprint-feed__body {
  display: flex;
  flex-direction: column;
  gap: 6px;
  min-width: 0;
}

.student-footprint-feed__tag {
  display: inline-flex;
  align-self: flex-start;
  padding: 3px 10px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.02em;
  border: 1px solid var(--color-border);
  background: var(--color-bg-elevated);
  color: var(--color-text);
}

.student-footprint-feed__tag--signin {
  color: #0f766e;
  border-color: color-mix(in srgb, var(--color-primary) 35%, var(--color-border));
  background: var(--color-primary-soft);
}

.student-footprint-feed__tag--homework {
  color: #0369a1;
  border-color: rgba(3, 105, 161, 0.28);
  background: rgba(14, 165, 233, 0.12);
}

.student-footprint-feed__tag--lab {
  color: #b45309;
  border-color: rgba(217, 119, 6, 0.35);
  background: rgba(217, 119, 6, 0.1);
}

.student-footprint-feed__tag--quiz {
  color: #0e7490;
  border-color: rgba(14, 116, 144, 0.32);
  background: rgba(6, 182, 212, 0.12);
}

.student-footprint-feed__tag--resource {
  color: #0f766e;
  border-color: color-mix(in srgb, var(--color-primary) 22%, var(--color-border));
  background: rgba(255, 255, 255, 0.85);
}

.student-footprint-feed__tag--other {
  color: var(--color-text-muted);
  background: rgba(248, 250, 252, 0.9);
}

.student-footprint-feed__detail {
  font-size: 12px;
  line-height: 1.5;
  color: var(--color-text-muted);
  overflow-wrap: anywhere;
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

  .student-dash-header__inner {
    flex-direction: column;
    align-items: flex-start;
  }

  .student-dash-columns {
    grid-template-columns: 1fr;
    min-height: 0;
  }

  .student-dash-col--timeline {
    border-right: none;
    border-bottom: 1px solid var(--color-border);
  }

  .student-dash-col--growth {
    border-right: none;
    border-bottom: 1px solid var(--color-border);
  }

  .student-dash-col--growth .student-growth__chart {
    min-height: 220px;
  }

  .student-footprint-feed {
    max-height: min(560px, 62vh);
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

