<template>
  <div class="class-page">
    <header class="class-hero card">
      <div class="class-hero__accent" aria-hidden="true" />
      <div class="class-hero__icon" aria-hidden="true">
        <el-icon><UserFilled /></el-icon>
      </div>
      <div class="class-hero__text">
        <h1 class="class-hero__title">班级活动 · 签到</h1>
        <p class="class-hero__sub">{{ pageSubtitle }}</p>
      </div>
    </header>

    <section class="class-panel card" v-loading="pageLoading">
      <div class="panel-head">
        <h2 class="panel-title">当前签到</h2>
      </div>

      <div class="signin-chart-wrap">
        <div ref="chartRef" class="sign-chart" />
        <div class="sign-lists">
          <div class="sign-list">
            <div class="sign-list-title">已签到（{{ signedNames.length }}）</div>
            <div class="sign-list-body">
              <el-tag
                v-for="name in signedNames"
                :key="`signed-${name}`"
                class="sign-tag"
                type="success"
                effect="light"
              >
                {{ name }}
              </el-tag>
              <div v-if="!signedNames.length" class="sign-empty">暂无</div>
            </div>
          </div>
          <div class="sign-list">
            <div class="sign-list-title">未签到（{{ unsignedNames.length }}）</div>
            <div class="sign-list-body sign-list-body--unsigned">
              <el-tag
                v-for="name in unsignedNames"
                :key="`unsigned-${name}`"
                class="sign-tag"
                type="danger"
                effect="plain"
              >
                {{ name }}
              </el-tag>
              <div v-if="!unsignedNames.length" class="sign-empty">暂无</div>
            </div>
          </div>
        </div>
      </div>

      <div
        v-for="(item, index) in classes"
        :key="item.name + '-' + index"
        class="class-row"
      >
        <div class="class-row__main">
          <span class="class-row__name">{{ item.name }}</span>
        </div>
        <div class="class-row__meta">选课人数：{{ displayStudentCount(item) }}</div>
        <div class="class-row__actions">
          <el-button
            v-if="data.user.role === 'TEACHER'"
            type="primary"
            round
            @click="openSigninDialog"
          >
            发起签到
          </el-button>
          <el-button
            v-if="data.user.role === 'STUDENT'"
            type="success"
            round
            :disabled="hasSigned"
            @click="clickSign"
          >
            {{ hasSigned ? '已签到' : '签到' }}
          </el-button>
          <el-button
            v-if="data.user.role === 'TEACHER'"
            type="primary"
            plain
            round
            @click="openSelectDialog"
          >
            随机选人
          </el-button>
        </div>
      </div>
    </section>

    <section v-if="data.user.role === 'TEACHER'" class="class-panel card">
      <h2 class="panel-title">历史签到</h2>
      <el-table :data="data.tableData" stripe class="history-table" v-loading="historyLoading">
        <el-table-column prop="teacherId" label="教师 ID" width="90" />
        <el-table-column prop="id" label="签到 ID" width="90" />
        <el-table-column prop="distance" label="距离限制(m)" width="110" />
        <el-table-column label="开始时间" align="center" min-width="120">
          <template #default="scope">
            <div class="time-cell">
              <div>{{ formatDate(scope.row.startTime) }}</div>
              <div class="time-cell__sub">{{ formatTime(scope.row.startTime) }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="结束时间" align="center" min-width="120">
          <template #default="scope">
            <div class="time-cell">
              <div>{{ formatDate(scope.row.endTime) }}</div>
              <div class="time-cell__sub">{{ formatTime(scope.row.endTime) }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" align="center" min-width="120">
          <template #default="scope">
            <div class="time-cell">
              <div>{{ formatDate(scope.row.createTime) }}</div>
              <div class="time-cell__sub">{{ formatTime(scope.row.createTime) }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="num" label="已签人数" width="100" />
        <el-table-column prop="snum" label="课程人数" width="100" />
      </el-table>
      <div class="history-pagination" v-if="data.total > 0">
        <el-pagination
          background
          layout="prev, pager, next, total"
          :current-page="data.pageNum"
          :page-size="data.pageSize"
          :total="data.total"
          @current-change="changeHistoryPage"
        />
      </div>
    </section>

    <el-dialog
      v-model="signinDialogVisible"
      title="发起签到"
      width="420px"
      class="class-dialog"
      destroy-on-close
      @closed="resetSigninForm"
    >
      <el-form label-position="top">
        <el-form-item label="活动时长（分钟）">
          <el-input-number v-model="activityDuration" :min="1" :max="60" style="width: 100%" />
          <p class="form-hint">后端限制为 1～60 分钟</p>
        </el-form-item>
        <el-form-item label="允许定位误差（米）">
          <el-input-number v-model="signDistance" :min="50" :max="1000" :step="50" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="signinDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="creatingSignIn" @click="handleCreateSignIn">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="selectDialogVisible"
      title="随机选人"
      width="440px"
      class="class-dialog"
      destroy-on-close
      @closed="selectedStudentName = ''"
    >
      <el-form label-position="top">
        <el-form-item label="随机抽取">
          <el-button type="primary" @click="randomSelectStudent">开始随机</el-button>
          <el-input v-model="selectedStudentName" readonly class="random-result" placeholder="结果将显示在这里" />
        </el-form-item>
        <el-form-item label="指定学生">
          <el-select v-model="selectedStudent" placeholder="请选择学生" filterable style="width: 100%">
            <el-option
              v-for="student in students"
              :key="student.id"
              :label="student.name"
              :value="student.id"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="selectDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted, nextTick, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { UserFilled, MoreFilled } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import request from '@/utils/request'
import { createSignIn, recordSignIn, selectAll, selectByName } from '@/api/signin'

const route = useRoute()
const chartRef = ref(null)
let chartInstance = null
let resizeHandler = null
let refreshTimer = null
let rollIntervalId = null

const pageLoading = ref(true)
const historyLoading = ref(false)
const creatingSignIn = ref(false)

const signinDialogVisible = ref(false)
const activityDuration = ref(30)
const signDistance = ref(100)
const selectDialogVisible = ref(false)
const selectedStudent = ref(undefined)
const selectedStudentName = ref('')
const hasSigned = ref(false)

const students = ref([])
const classes = ref([])
const courseId = ref(null)

const chartData = reactive({ signed: 0, unsigned: 0 })
const signedNames = ref([])
const unsignedNames = ref([])

const data = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0,
  tableData: [],
  user: JSON.parse(localStorage.getItem('system-user') || '{}'),
})

const pageSubtitle = computed(() => {
  const name = route.query.courseName ? String(route.query.courseName) : ''
  if (name) return `课程：${name} · 教师发起签到，学生在签到有效期内点击签到`
  return '教师发起签到，学生在有效期内参与；图表与名单随轮询刷新'
})

function displayStudentCount(item) {
  if (item?.students != null && item.students !== '—') return item.students
  const n = chartData.signed + chartData.unsigned
  return n > 0 ? n : '—'
}

function normalizePagePayload(raw) {
  if (!raw || typeof raw !== 'object') return { list: [], total: 0 }
  const list = raw.list ?? raw.records ?? raw.rows ?? []
  const total = Number(raw.total ?? raw.totalCount ?? list.length) || 0
  return { list: Array.isArray(list) ? list : [], total }
}

const formatDate = (cellValue) => {
  if (!cellValue) return ''
  const d = new Date(cellValue)
  if (Number.isNaN(d.getTime())) return String(cellValue)
  const pad = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`
}

const formatTime = (cellValue) => {
  if (!cellValue) return ''
  const d = new Date(cellValue)
  if (Number.isNaN(d.getTime())) return ''
  const pad = (n) => String(n).padStart(2, '0')
  return `${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
}

function getTeacherId() {
  const id = data.user?.id
  if (id == null) {
    ElMessage.error('无法获取当前用户')
    throw new Error('MISSING_USER_ID')
  }
  return Number(id)
}

function getStudentId() {
  return getTeacherId()
}

async function resolveCourseContext() {
  const qid = route.query.id
  const courseName = route.query.courseName ? String(route.query.courseName) : ''

  if (qid != null && qid !== '') {
    const id = Number(qid)
    if (!Number.isNaN(id)) {
      courseId.value = id
      classes.value = [
        {
          name: courseName || `课程 #${id}`,
          students: '—',
        },
      ]
      return
    }
  }

  if (courseName) {
    const response = await selectByName(courseName)
    if (response.code === '200' && response.data?.id != null) {
      courseId.value = response.data.id
      classes.value = [
        {
          name: response.data.name || courseName,
          students: response.data.alreadyNum ?? '—',
        },
      ]
      return
    }
    throw new Error(response.msg || '根据课程名称解析失败')
  }

  throw new Error('缺少课程参数')
}

async function loadStudentsByCourse() {
  if (!courseId.value) return
  try {
    const res = await request.get('/student/selectByCourseId', {
      params: { courseId: courseId.value },
    })
    if (res.code === '200' && Array.isArray(res.data)) {
      students.value = res.data.map((item) => ({ id: item.id, name: item.name }))
    } else {
      students.value = []
    }
  } catch (e) {
    console.error(e)
    students.value = []
  }
}

function initChart() {
  const el = chartRef.value
  if (!el) return
  if (chartInstance) {
    chartInstance.dispose()
    chartInstance = null
  }
  chartInstance = echarts.init(el)
  if (resizeHandler) {
    window.removeEventListener('resize', resizeHandler)
    resizeHandler = null
  }

  chartInstance.setOption({
    color: ['#0d9488', '#f97316'],
    title: {
      text: '签到情况',
      left: 'center',
      top: 8,
      textStyle: { fontSize: 14, color: '#0f172a', fontWeight: 600 },
    },
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c} 人 ({d}%)',
    },
    legend: {
      orient: 'vertical',
      left: 'left',
      top: 'middle',
      textStyle: { color: '#64748b' },
    },
    series: [
      {
        name: '签到',
        type: 'pie',
        radius: ['42%', '68%'],
        center: ['58%', '55%'],
        avoidLabelOverlap: true,
        itemStyle: { borderRadius: 8, borderColor: '#fff', borderWidth: 2 },
        label: { formatter: '{b}\n{c} 人 ({d}%)', color: '#475569' },
        data: [
          { value: chartData.signed, name: '已签到' },
          { value: chartData.unsigned, name: '未签到' },
        ],
      },
    ],
  })
  resizeHandler = () => chartInstance?.resize()
  window.addEventListener('resize', resizeHandler)
}

function updateChart() {
  if (!chartInstance) return
  chartInstance.setOption({
    series: [
      {
        data: [
          { value: chartData.signed, name: '已签到' },
          { value: chartData.unsigned, name: '未签到' },
        ],
      },
    ],
  })
}

function syncStudentSignedState() {
  if (data.user.role !== 'STUDENT') return
  const name = (data.user.name || '').trim()
  if (!name) {
    hasSigned.value = false
    return
  }
  hasSigned.value = signedNames.value.some((n) => (n || '').trim() === name)
}

async function fetchSignInData() {
  if (!courseId.value) return
  try {
    const response = await selectAll(courseId.value)
    if (response.code === '200' && response.data) {
      chartData.signed = response.data.num ?? 0
      chartData.unsigned = response.data.unum ?? 0
      signedNames.value = response.data.signedNames || []
      unsignedNames.value = response.data.unsignedNames || []
      if (classes.value[0]) {
        const total = chartData.signed + chartData.unsigned
        if (total > 0) classes.value[0].students = total
      }
      updateChart()
      syncStudentSignedState()
    }
  } catch (e) {
    console.error(e)
  }
}

async function loadHistory() {
  if (!courseId.value || data.user.role !== 'TEACHER') return
  historyLoading.value = true
  try {
    const res = await request.get('/signIn/selectPage', {
      params: {
        pageNum: data.pageNum,
        pageSize: data.pageSize,
        courseId: courseId.value,
      },
    })
    if (res.code === '200') {
      const { list, total } = normalizePagePayload(res.data)
      data.tableData = list
      data.total = total
    }
  } catch (e) {
    console.error(e)
    ElMessage.error('历史签到加载失败')
  } finally {
    historyLoading.value = false
  }
}

function changeHistoryPage(p) {
  data.pageNum = p
  loadHistory()
}

const openSelectDialog = () => {
  selectDialogVisible.value = true
  selectedStudentName.value = ''
}

const randomSelectStudent = () => {
  if (rollIntervalId) clearInterval(rollIntervalId)
  if (!students.value.length) {
    selectedStudentName.value = '本课程暂无学生数据'
    return
  }
  let count = 0
  rollIntervalId = setInterval(() => {
    const idx = Math.floor(Math.random() * students.value.length)
    selectedStudentName.value = students.value[idx].name
    count++
    if (count >= 12) {
      clearInterval(rollIntervalId)
      rollIntervalId = null
    }
  }, 80)
}

const openSigninDialog = () => {
  signinDialogVisible.value = true
}

const resetSigninForm = () => {
  activityDuration.value = 30
  signDistance.value = 100
}

const handleCreateSignIn = async () => {
  if (!courseId.value) {
    ElMessage.warning('缺少课程')
    return
  }
  creatingSignIn.value = true
  try {
    const payload = {
      courseId: courseId.value,
      teacherId: getTeacherId(),
      duration: activityDuration.value,
      distance: signDistance.value,
    }
    const response = await createSignIn(payload)
    if (response.code === '200') {
      ElMessage.success('签到已发起')
      signinDialogVisible.value = false
      await fetchSignInData()
      await loadHistory()
    } else {
      ElMessage.error(response.msg || '发起失败')
    }
  } catch (e) {
    console.error(e)
    ElMessage.error(e?.response?.data?.msg || e?.message || '发起失败')
  } finally {
    creatingSignIn.value = false
  }
}

const clickSign = async () => {
  if (hasSigned.value || !courseId.value) return
  try {
    const payload = {
      courseId: courseId.value,
      studentId: getStudentId(),
      latitude: 39.9042,
      longitude: 116.4074,
    }
    const response = await recordSignIn(payload)
    if (response.code === '200') {
      ElMessage.success('签到成功')
      hasSigned.value = true
      await fetchSignInData()
    } else {
      ElMessage.error(response.msg || '签到失败')
    }
  } catch (e) {
    console.error(e)
    ElMessage.error(e?.response?.data?.msg || e?.message || '签到失败')
  }
}

watch(
  () => [route.query.id, route.query.courseName],
  async () => {
    try {
      pageLoading.value = true
      await resolveCourseContext()
      await loadStudentsByCourse()
      await fetchSignInData()
      await loadHistory()
      nextTick(() => {
        if (!chartInstance) initChart()
        else updateChart()
      })
    } catch (e) {
      ElMessage.error(e?.message || '课程信息无效')
    } finally {
      pageLoading.value = false
    }
  }
)

onMounted(async () => {
  try {
    await resolveCourseContext()
    await loadStudentsByCourse()
    await nextTick()
    initChart()
    await fetchSignInData()
    await loadHistory()
    refreshTimer = setInterval(fetchSignInData, 10000)
  } catch (e) {
    console.error(e)
    ElMessage.error(e?.message || '页面加载失败，请从课程导航进入')
  } finally {
    pageLoading.value = false
  }
})

onUnmounted(() => {
  if (refreshTimer) clearInterval(refreshTimer)
  if (rollIntervalId) clearInterval(rollIntervalId)
  if (resizeHandler) {
    window.removeEventListener('resize', resizeHandler)
    resizeHandler = null
  }
  if (chartInstance) {
    chartInstance.dispose()
    chartInstance = null
  }
})
</script>

<style scoped lang="scss">
.class-page {
  padding: 16px 20px 28px;
  min-height: 100%;
  box-sizing: border-box;
  font-family: var(--font-sans);
  color: var(--color-text);
  background: transparent;
}

.class-hero {
  position: relative;
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 16px;
  overflow: hidden;
  border: 1px solid var(--color-border);
  box-shadow: var(--shadow-soft);
}

.class-hero__accent {
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 4px;
  background: linear-gradient(
    180deg,
    var(--color-primary) 0%,
    color-mix(in srgb, var(--color-primary) 70%, #0f766e) 100%
  );
  border-radius: 2px;
  pointer-events: none;
}

.class-hero__icon {
  width: 48px;
  height: 48px;
  border-radius: var(--radius-md);
  background: var(--color-primary-soft);
  color: var(--color-primary);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
}

.class-hero__text {
  min-width: 0;
}

.class-hero__title {
  margin: 0 0 6px;
  font-size: 1.35rem;
  font-weight: 700;
  letter-spacing: -0.02em;
}

.class-hero__sub {
  margin: 0;
  font-size: 0.9rem;
  color: var(--color-text-muted);
  line-height: 1.55;
  max-width: 46rem;
}

.class-panel {
  margin-bottom: 16px;
  border: 1px solid var(--color-border);
}

.panel-head {
  margin-bottom: 12px;
  padding-bottom: 10px;
  border-bottom: 1px solid var(--color-border);
}

.panel-title {
  margin: 0;
  font-size: 1.05rem;
  font-weight: 700;
  color: var(--color-text);
}

.signin-chart-wrap {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  align-items: stretch;
  margin-bottom: 20px;
}

.sign-chart {
  flex: 1 1 280px;
  min-height: 300px;
  min-width: 0;
  background: var(--color-bg-app);
  border-radius: var(--radius-md);
  border: 1px solid var(--color-border);
}

.sign-lists {
  flex: 1 1 260px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  min-width: 0;
}

.sign-list {
  background: var(--color-bg-app);
  border-radius: var(--radius-md);
  padding: 12px;
  border: 1px solid var(--color-border);
}

.sign-list-title {
  font-size: 13px;
  font-weight: 600;
  color: var(--color-text);
  margin-bottom: 8px;
}

.sign-list-body {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  max-height: 200px;
  overflow-y: auto;
}

.sign-list-body--unsigned {
  align-content: flex-start;
}

.sign-tag {
  font-size: 12px;
}

.sign-empty {
  font-size: 12px;
  color: var(--color-text-subtle);
}

.class-row {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 12px;
  padding: 14px 0;
  border-top: 1px solid var(--color-border);
}

.class-row__main {
  display: flex;
  align-items: center;
  gap: 10px;
  flex: 1 1 200px;
  min-width: 0;
}

.class-row__name {
  font-weight: 600;
  color: var(--color-text);
}

.class-row__meta {
  font-size: 13px;
  color: var(--color-text-muted);
}

.class-row__actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
  margin-left: auto;
}

.history-table {
  margin-top: 12px;
  border-radius: var(--radius-sm);
  overflow: hidden;
}

.time-cell {
  display: flex;
  flex-direction: column;
  align-items: center;
  line-height: 1.25;
}

.time-cell__sub {
  font-size: 12px;
  color: var(--color-text-muted);
}

.history-pagination {
  display: flex;
  justify-content: center;
  margin-top: 16px;
}

.form-hint {
  margin: 6px 0 0;
  font-size: 12px;
  color: var(--color-text-muted);
}

.random-result {
  margin-top: 10px;
  width: 100%;
}

.class-dialog :deep(.el-dialog__header) {
  border-bottom: 1px solid var(--color-border);
  margin-right: 0;
  padding-bottom: 14px;
}

@media (max-width: 768px) {
  .class-row__actions {
    width: 100%;
    margin-left: 0;
    justify-content: flex-start;
  }
}
</style>
