<template>
  <div class="screen">
    <div class="screen-bg" />

    <header class="topbar">
      <div class="topbar-left">
        <div class="brand">综合校情</div>
        <div class="sub">教学 · 课程 · 班级 · 作业</div>
      </div>
      <div class="topbar-center">
        <div class="title">数智验舱 · 教学可视化大屏</div>
        <div class="date">{{ nowText }}</div>
      </div>
      <div class="topbar-right">
        <el-select v-model="selectedCourse" size="small" class="course-select" placeholder="课程">
          <el-option v-for="c in courses" :key="c.id" :label="c.name" :value="c.id" />
        </el-select>
      </div>
    </header>

    <main class="grid">
      <section class="col">
        <div class="panel">
          <div class="panel-hd">
            <span>教学概览</span>
          </div>
          <div class="kpi-grid">
            <div class="kpi">
              <div class="kpi-l">课程总数</div>
              <div class="kpi-v">{{ kpis.courses }}</div>
              <div class="kpi-s">本学期</div>
            </div>
            <div class="kpi">
              <div class="kpi-l">教师人数</div>
              <div class="kpi-v">{{ kpis.teachers }}</div>
              <div class="kpi-s">在岗</div>
            </div>
            <div class="kpi">
              <div class="kpi-l">学生人数</div>
              <div class="kpi-v">{{ kpis.students }}</div>
              <div class="kpi-s">覆盖班级</div>
            </div>
            <div class="kpi">
              <div class="kpi-l">班级数量</div>
              <div class="kpi-v">{{ kpis.clazz }}</div>
              <div class="kpi-s">教学班</div>
            </div>
          </div>
        </div>

        <div class="panel">
          <div class="panel-hd">
            <span>作业状态分布</span>
            <span class="hint">实验/课后任务</span>
          </div>
          <div class="bars">
            <div class="bar" v-for="b in workBars" :key="b.name">
              <div class="bar-meta">
                <span class="bar-name">{{ b.name }}</span>
                <span class="bar-value">{{ b.value }}</span>
              </div>
              <div class="bar-track">
                <div class="bar-fill" :style="{ width: b.pct + '%' }" />
              </div>
            </div>
          </div>
        </div>

        <div class="panel">
          <div class="panel-hd">
            <span>学生完成率 TOP</span>
            <span class="hint">示例</span>
          </div>
          <div class="rank">
            <div class="rank-row" v-for="(s, idx) in topStudents" :key="s.id">
              <div class="rank-idx">{{ idx + 1 }}</div>
              <div class="rank-name">{{ s.name }}</div>
              <div class="rank-id">{{ s.id }}</div>
              <div class="rank-rate">
                <div class="mini-track">
                  <div class="mini-fill" :style="{ width: s.rate + '%' }" />
                </div>
                <span class="mini-text">{{ s.rate }}%</span>
              </div>
            </div>
          </div>
        </div>
      </section>

      <section class="col center">
        <div class="panel hero">
          <div class="panel-hd">
            <span>教学运行态势</span>
            <span class="hint">近 7 天（静态）</span>
          </div>
          <div class="trend">
            <div class="trend-axis">
              <span v-for="d in trend.map(t => t.day)" :key="d">{{ d }}</span>
            </div>
            <div class="trend-bars">
              <div class="trend-col" v-for="t in trend" :key="t.day">
                <div class="trend-bar" :style="{ height: t.value + '%' }" />
                <div class="trend-val">{{ t.value }}</div>
              </div>
            </div>
          </div>
          <div class="trend-legend">
            <span class="dot" /> 活跃度（示例）
          </div>
        </div>

        <div class="panel">
          <div class="panel-hd">
            <span>课程画像</span>
            <span class="hint">当前课程</span>
          </div>
          <div class="course-profile">
            <div class="profile-item">
              <div class="p-label">课程名称</div>
              <div class="p-value">{{ currentCourse.name }}</div>
            </div>
            <div class="profile-item">
              <div class="p-label">授课教师</div>
              <div class="p-value">{{ currentCourse.teacher }}</div>
            </div>
            <div class="profile-item">
              <div class="p-label">开课班级</div>
              <div class="p-value">{{ currentCourse.clazz }}</div>
            </div>
            <div class="profile-item">
              <div class="p-label">作业发布</div>
              <div class="p-value">{{ currentCourse.works }}</div>
            </div>
          </div>
        </div>
      </section>

      <section class="col">
        <div class="panel">
          <div class="panel-hd">
            <span>教师队伍</span>
          </div>
          <div class="stat-grid">
            <div class="stat">
              <div class="stat-v">{{ teacherStats.senior }}</div>
              <div class="stat-l">高级职称</div>
            </div>
            <div class="stat">
              <div class="stat-v">{{ teacherStats.mid }}</div>
              <div class="stat-l">中级职称</div>
            </div>
            <div class="stat">
              <div class="stat-v">{{ teacherStats.junior }}</div>
              <div class="stat-l">初级职称</div>
            </div>
          </div>
        </div>

        <div class="panel">
          <div class="panel-hd">
            <span>作业预警</span>
            <span class="hint">需要关注</span>
          </div>
          <div class="alerts">
            <div class="alert" v-for="a in alerts" :key="a.title">
              <div class="alert-title">{{ a.title }}</div>
              <div class="alert-desc">{{ a.desc }}</div>
              <div class="alert-tag" :class="a.level">{{ a.levelText }}</div>
            </div>
          </div>
        </div>

        <div class="panel">
          <div class="panel-hd">
            <span>近期动态</span>
            <span class="hint">最近 7 天</span>
          </div>
          <div class="feed">
            <div class="feed-row" v-for="f in feed" :key="f.time + f.who">
              <div class="feed-time">{{ f.time }}</div>
              <div class="feed-main">
                <div class="feed-line">
                  <span class="feed-who">{{ f.who }}</span>
                  <span class="feed-action">{{ f.action }}</span>
                </div>
                <div class="feed-detail">{{ f.detail }}</div>
              </div>
              <div class="feed-status" :class="f.type">{{ f.status }}</div>
            </div>
          </div>
        </div>
      </section>
    </main>
  </div>
</template>

<script setup>
import { computed, onMounted, onUnmounted, ref } from 'vue'
import request from '@/utils/request'

const selectedCourse = ref(1)
const nowText = ref('')
let timer = null

const courses = computed(() => [
  { id: 1, name: 'Web 设计', teacher: '王老师', clazz: '计科 2301/2302', works: '实验 4 / 课后 6' },
  { id: 2, name: 'Java Web', teacher: '李老师', clazz: '软工 2301', works: '实验 3 / 课后 5' },
  { id: 3, name: '数据库原理', teacher: '张老师', clazz: '信管 2301', works: '实验 5 / 课后 4' },
])

const currentCourse = computed(() => courses.value.find((c) => c.id === selectedCourse.value) || courses.value[0])

const kpis = ref({
  courses: 0,
  teachers: 0,
  students: 0,
  clazz: 0,
})
const workBars = computed(() => [
  { name: '待提交', value: 87, pct: 58 },
  { name: '待审核', value: 27, pct: 18 },
  { name: '已通过', value: 32, pct: 22 },
  { name: '需修改', value: 4, pct: 6 },
])
const topStudents = computed(() => [
  { name: '赵健民', id: '2022342510', rate: 96 },
  { name: '吴晨豪', id: '2022266421', rate: 91 },
  { name: '冯玫', id: '2022390134', rate: 88 },
  { name: '李世海', id: '2022396721', rate: 84 },
  { name: '吴宦颜', id: '2022091421', rate: 79 },
])
const trend = computed(() => [
  { day: '周一', value: 42 },
  { day: '周二', value: 55 },
  { day: '周三', value: 63 },
  { day: '周四', value: 58 },
  { day: '周五', value: 71 },
  { day: '周六', value: 36 },
  { day: '周日', value: 49 },
])
const teacherStats = ref({
  senior: 0,
  mid: 0,
  junior: 0,
})
const alerts = computed(() => [
  { title: '相似度偏高', desc: '实验二有 3 份作业相似度 > 80%', level: 'danger', levelText: '高' },
  { title: '未按时提交', desc: '课后任务三仍有 12 人未提交', level: 'warning', levelText: '中' },
  { title: '复审待处理', desc: '打回修改待复审 4 份', level: 'info', levelText: '低' },
])
const feed = computed(() => [
  { time: '02-04 15:12', who: '张三', action: '提交实验作业', detail: '实验二：接口联调与异常处理', status: '待审核', type: 'warning' },
  { time: '02-04 14:20', who: '李四', action: '提交课后任务', detail: '任务三：单元测试与覆盖率', status: '已提交', type: 'info' },
  { time: '02-04 10:05', who: '王老师', action: '审核实验作业', detail: '实验一：环境搭建与规范', status: '已通过', type: 'success' },
  { time: '02-03 20:32', who: '赵五', action: '下载课程资料', detail: '《实验二指导书》', status: '已完成', type: 'success' },
])

function tick() {
  const d = new Date()
  const pad = (n) => String(n).padStart(2, '0')
  nowText.value = `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
}

async function loadKpis() {
  try {
    const [courseRes, teacherRes, studentRes, clazzRes] = await Promise.all([
      request.get('/course/selectAll'),
      request.get('/teacher/selectAll'),
      request.get('/student/selectAll'),
      request.get('/clazz/selectAll'),
    ])
    kpis.value = {
      courses: Array.isArray(courseRes?.data) ? courseRes.data.length : 0,
      teachers: Array.isArray(teacherRes?.data) ? teacherRes.data.length : 0,
      students: Array.isArray(studentRes?.data) ? studentRes.data.length : 0,
      clazz: Array.isArray(clazzRes?.data) ? clazzRes.data.length : 0,
    }

    // 统计教师职称分布：按 title 字段粗略归类为 高级 / 中级 / 初级
    const teachers = Array.isArray(teacherRes?.data) ? teacherRes.data : []
    let senior = 0
    let mid = 0
    let junior = 0
    teachers.forEach(t => {
      const title = (t.title || '').toString()
      if (!title) {
        junior++
        return
      }
      if (title.includes('教授') || title.includes('研究员') || title.includes('高级')) {
        senior++
      } else if (title.includes('副') || title.includes('讲师') || title.includes('中级')) {
        mid++
      } else {
        junior++
      }
    })
    teacherStats.value = { senior, mid, junior }
  } catch (e) {
    console.error('加载教学概览数据失败', e)
  }
}

onMounted(() => {
  tick()
  timer = window.setInterval(tick, 1000)
  loadKpis()
})

onUnmounted(() => {
  if (timer) window.clearInterval(timer)
})
</script>

<style scoped>
/* 大屏：红色主题、轻量静态“图表” */
.screen {
  position: relative;
  width: 100%;
  min-height: calc(100vh - 60px);
  background: #061524;
  color: #f5f9ff;
  overflow: hidden;
}

.screen-bg {
  position: absolute;
  inset: 0;
  background:
    radial-gradient(800px 400px at 20% 15%, rgba(64, 158, 255, 0.28), transparent 60%),
    radial-gradient(900px 500px at 80% 20%, rgba(80, 200, 255, 0.20), transparent 60%),
    linear-gradient(180deg, rgba(10, 44, 80, 0.85), rgba(2, 10, 24, 0.9));
  filter: saturate(1.1);
  pointer-events: none;
}

.topbar {
  position: relative;
  z-index: 1;
  display: grid;
  grid-template-columns: 1fr 1.4fr 1fr;
  gap: 12px;
  align-items: center;
  padding: 14px 18px;
  background: linear-gradient(90deg, rgba(22, 119, 255, 0.95), rgba(9, 54, 130, 0.9));
  border-bottom: 1px solid rgba(255, 255, 255, 0.12);
}

.brand {
  font-weight: 800;
  letter-spacing: 1px;
  font-size: 18px;
}

.sub {
  font-size: 12px;
  opacity: 0.85;
  margin-top: 4px;
}

.topbar-center {
  text-align: center;
}

.title {
  font-size: 20px;
  font-weight: 900;
  letter-spacing: 1px;
}

.date {
  margin-top: 6px;
  font-size: 12px;
  opacity: 0.85;
}

.topbar-right {
  display: flex;
  justify-content: flex-end;
}

.course-select {
  width: 220px;
}

.grid {
  position: relative;
  z-index: 1;
  display: grid;
  grid-template-columns: 1fr 1.25fr 1fr;
  gap: 12px;
  padding: 12px;
  box-sizing: border-box;
}

.col {
  display: flex;
  flex-direction: column;
  gap: 18px;
  min-width: 0;
}

.col.center .panel.hero {
  min-height: 320px;
}

.panel {
  background: rgba(255, 255, 255, 0.06);
  border: 1px solid rgba(135, 183, 255, 0.25);
  border-radius: 10px;
  padding: 12px 12px 14px;
  backdrop-filter: blur(6px);
}

.panel-hd {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 800;
  letter-spacing: 0.5px;
  margin-bottom: 10px;
}

.hint {
  font-size: 12px;
  opacity: 0.85;
  font-weight: 600;
}

.kpi-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
}

.kpi {
  background: rgba(0, 0, 0, 0.18);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 10px;
  padding: 10px 10px 8px;
}

.kpi-l {
  font-size: 12px;
  opacity: 0.88;
}

.kpi-v {
  margin-top: 6px;
  font-size: 26px;
  font-weight: 900;
  color: #e3f2ff;
  text-shadow: 0 0 16px rgba(64, 158, 255, 0.55);
}

.kpi-s {
  margin-top: 6px;
  font-size: 12px;
  opacity: 0.8;
}

.bars {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.bar-meta {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  opacity: 0.9;
}

.bar-track {
  margin-top: 6px;
  height: 8px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.10);
  overflow: hidden;
}

.bar-fill {
  height: 100%;
  border-radius: 999px;
  background: linear-gradient(90deg, rgba(64, 158, 255, 0.95), rgba(160, 220, 255, 0.95));
}

.rank {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.rank-row {
  display: grid;
  grid-template-columns: 26px 1fr 110px 120px;
  gap: 8px;
  align-items: center;
  padding: 8px;
  border-radius: 10px;
  background: rgba(0, 0, 0, 0.16);
  border: 1px solid rgba(255, 255, 255, 0.06);
}

.rank-idx {
  width: 26px;
  height: 26px;
  border-radius: 8px;
  display: grid;
  place-items: center;
  background: rgba(255, 255, 255, 0.10);
  font-weight: 900;
}

.rank-name {
  font-weight: 800;
}

.rank-id {
  font-size: 12px;
  opacity: 0.85;
}

.rank-rate {
  display: flex;
  align-items: center;
  gap: 8px;
  justify-content: flex-end;
}

.mini-track {
  width: 70px;
  height: 8px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.10);
  overflow: hidden;
}

.mini-fill {
  height: 100%;
  background: linear-gradient(90deg, rgba(255, 120, 120, 0.95), rgba(255, 210, 170, 0.9));
}

.mini-text {
  font-size: 12px;
  opacity: 0.9;
}

.trend {
  height: 85%;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.trend-axis {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 8px;
  font-size: 12px;
  opacity: 0.85;
}

.trend-bars {
  flex: 1;
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 10px;
  align-items: end;
  padding: 10px 6px 4px;
  background: rgba(0, 0, 0, 0.16);
  border-radius: 10px;
  border: 1px solid rgba(255, 255, 255, 0.06);
  min-height: 200px;
}

.trend-col {
  display: flex;
  flex-direction: column;
  gap: 6px;
  align-items: center;
  justify-content: flex-end;
  height: 100%;
}

.trend-bar {
  width: 18px;
  min-height: 8px;
  border-radius: 10px;
  background: linear-gradient(180deg, rgba(160, 220, 255, 0.98), rgba(64, 158, 255, 0.98));
  box-shadow: 0 0 18px rgba(64, 158, 255, 0.45);
  transition: height 0.3s ease;
}

.trend-val {
  font-size: 12px;
  opacity: 0.9;
}

.trend-legend {
  font-size: 12px;
  opacity: 0.85;
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top:5px;
}

.dot {
  width: 10px;
  height: 10px;
  border-radius: 999px;
  background: rgba(255, 120, 120, 0.95);
  box-shadow: 0 0 10px rgba(255, 120, 120, 0.35);
}

.course-profile {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
}

.profile-item {
  background: rgba(0, 0, 0, 0.16);
  border: 1px solid rgba(135, 183, 255, 0.25);
  border-radius: 10px;
  padding: 10px;
}

.p-label {
  font-size: 12px;
  opacity: 0.85;
}

.p-value {
  margin-top: 6px;
  font-weight: 900;
  color: #e3f2ff;
}

.stat-grid {
  display: grid;
  grid-template-columns: 1fr 1fr 1fr;
  gap: 10px;
}

.stat {
  text-align: center;
  padding: 10px 8px;
  background: rgba(0, 0, 0, 0.16);
  border: 1px solid rgba(135, 183, 255, 0.25);
  border-radius: 10px;
}

.stat-v {
  font-size: 22px;
  font-weight: 900;
  color: #e3f2ff;
}

.stat-l {
  margin-top: 6px;
  font-size: 12px;
  opacity: 0.85;
}

.alerts {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.alert {
  position: relative;
  padding: 10px 10px 10px 12px;
  border-radius: 10px;
  background: rgba(0, 0, 0, 0.16);
  border: 1px solid rgba(135, 183, 255, 0.25);
}

.alert-title {
  font-weight: 900;
}

.alert-desc {
  margin-top: 6px;
  font-size: 12px;
  opacity: 0.85;
}

.alert-tag {
  position: absolute;
  right: 10px;
  top: 10px;
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 999px;
  border: 1px solid rgba(135, 183, 255, 0.35);
  background: rgba(255, 255, 255, 0.08);
}

.alert-tag.danger {
  background: rgba(245, 108, 108, 0.18);
}

.alert-tag.warning {
  background: rgba(230, 162, 60, 0.18);
}

.alert-tag.info {
  background: rgba(64, 158, 255, 0.22);
}

.feed {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.feed-row {
  display: grid;
  grid-template-columns: 88px 1fr 64px;
  gap: 10px;
  align-items: start;
  padding: 10px;
  border-radius: 10px;
  background: rgba(0, 0, 0, 0.16);
  border: 1px solid rgba(135, 183, 255, 0.25);
}

.feed-time {
  font-size: 12px;
  opacity: 0.85;
}

.feed-line {
  display: flex;
  gap: 8px;
  align-items: baseline;
}

.feed-who {
  font-weight: 900;
  color: #e3f2ff;
}

.feed-action {
  opacity: 0.9;
}

.feed-detail {
  margin-top: 6px;
  font-size: 12px;
  opacity: 0.85;
}

.feed-status {
  justify-self: end;
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 999px;
  border: 1px solid rgba(135, 183, 255, 0.35);
  background: rgba(255, 255, 255, 0.05);
}

.feed-status.success {
  background: rgba(103, 194, 58, 0.20);
}

.feed-status.warning {
  background: rgba(230, 162, 60, 0.20);
}

.feed-status.info {
  background: rgba(64, 158, 255, 0.24);
}

@media (max-width: 1200px) {
  .grid {
    grid-template-columns: 1fr;
  }

  .topbar {
    grid-template-columns: 1fr;
    text-align: left;
  }

  .topbar-right {
    justify-content: flex-start;
  }
}
</style>
