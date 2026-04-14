<template>
  <div class="college-page" :class="{ 'college-page--detail': isCollegeDetail }">
    <!-- 学院列表 -->
    <template v-if="!isCollegeDetail">
      <section class="college-toolbar card">
        <div class="toolbar-head">
          <div class="toolbar-titles">
            <h1 class="page-title">学院与专业</h1>
            <p class="page-sub">浏览各学院概况；点击学院卡片进入该院系下的专业介绍</p>
          </div>
          <div v-if="userRole === 'ADMIN'" class="toolbar-actions">
            <el-button type="primary" @click="handleAdd">
              <el-icon class="btn-icon"><Plus /></el-icon>
              新增学院
            </el-button>
          </div>
        </div>
        <div class="toolbar-search">
          <el-input
            v-model="data.name"
            class="search-input"
            placeholder="输入学院名称查询"
            clearable
            @keyup.enter="load"
          />
          <el-button type="primary" @click="load">查询</el-button>
          <el-button @click="reset">重置</el-button>
        </div>
      </section>

      <section class="college-body card">
        <div v-loading="data.loading" class="grid-wrap">
          <div v-if="!data.loading && !data.tableData.length" class="empty-state">
            <el-icon class="empty-icon"><OfficeBuilding /></el-icon>
            <p class="empty-title">暂无学院数据</p>
            <p class="empty-hint">调整筛选条件或联系管理员维护基础数据</p>
          </div>

          <div
            v-else
            class="college-grid"
            role="list"
            aria-label="学院列表"
          >
            <article
              v-for="(row, index) in data.tableData"
              :key="row.id"
              class="college-card college-card--open"
              :style="{ '--i': index }"
              role="listitem"
              tabindex="0"
              @click="openCollegeSpecialities(row)"
              @keydown.enter="openCollegeSpecialities(row)"
            >
              <div class="college-card__accent" aria-hidden="true" />
              <div class="college-card__top">
                <div class="college-card__icon" aria-hidden="true">
                  <el-icon><School /></el-icon>
                </div>
                <div class="college-card__head">
                  <h2 class="college-card__name">{{ row.name }}</h2>
                  <span class="college-card__badge">
                    <el-icon class="badge-icon"><User /></el-icon>
                    {{ row.num ?? 0 }} 人
                  </span>
                </div>
              </div>
              <p
                class="college-card__desc college-intro-copy"
                :title="collegeIntroTitle(row.content)"
              >
                {{ collegeIntroExcerpt(row.content) }}
              </p>
              <p class="college-card__hint">点击进入专业介绍</p>
              <div v-if="userRole === 'ADMIN'" class="college-card__foot" @click.stop>
                <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
                <el-button link type="danger" @click="handleDelete(row.id)">删除</el-button>
              </div>
            </article>
          </div>
        </div>
      </section>

      <section class="college-footer card">
        <el-pagination
          background
          layout="total, prev, pager, next"
          v-model:page-size="data.pageSize"
          v-model:current-page="data.pageNum"
          :total="data.total"
          @current-change="changePage"
        />
      </section>
    </template>

    <!-- 学院下专业：左列表 + 右详情 -->
    <template v-else>
      <section class="detail-head card">
        <div class="detail-head__row">
          <el-button class="back-btn" @click="goBackToColleges">
            <el-icon><ArrowLeft /></el-icon>
            返回学院列表
          </el-button>
          <div v-if="userRole === 'ADMIN'" class="detail-head__actions">
            <el-button type="primary" @click="handleAddSpeciality">
              <el-icon class="btn-icon"><Plus /></el-icon>
              新增专业
            </el-button>
          </div>
        </div>
        <h1 class="detail-title">{{ detailCollegeHeading }}</h1>
        <p class="detail-sub">左侧首项为学院介绍，其余为该学院专业；点选后在右侧查看全文（管理员可维护）</p>
      </section>

      <div class="detail-split">
        <aside class="detail-aside card" aria-label="学院与专业导航">
          <div class="aside-title-bar">学院与专业</div>
          <div class="aside-search">
            <el-input
              v-model="specPane.search"
              placeholder="筛选专业名称"
              clearable
              size="small"
              @keyup.enter="loadSpecialities"
            />
            <el-button size="small" type="primary" @click="loadSpecialities">筛选</el-button>
          </div>
          <div v-loading="specPane.loading" class="aside-list-wrap">
            <el-scrollbar class="aside-scroll">
              <ul class="spec-list" role="list">
                <li role="listitem">
                  <button
                    type="button"
                    class="spec-item spec-item--intro"
                    :class="{ 'spec-item--active': specPane.activeSidebarKey === SIDEBAR_COLLEGE_INTRO }"
                    @click="selectCollegeIntro"
                  >
                    学院介绍
                  </button>
                </li>
                <li v-for="s in specPane.list" :key="s.id" role="listitem">
                  <button
                    type="button"
                    class="spec-item"
                    :class="{ 'spec-item--active': specPane.activeSidebarKey === s.id }"
                    @click="selectSpeciality(s)"
                  >
                    {{ s.name }}
                  </button>
                </li>
              </ul>
              <div v-if="!specPane.loading && !specPane.list.length" class="aside-empty">
                该院系下暂无专业（仍可通过「学院介绍」查看学院信息）
              </div>
            </el-scrollbar>
          </div>
        </aside>

        <main
          class="detail-main card"
          v-loading="detailCollegeMeta.loading"
          aria-live="polite"
        >
          <template v-if="specPane.activeSidebarKey === SIDEBAR_COLLEGE_INTRO">
            <header class="main-head">
              <h2 class="main-title">学院介绍</h2>
              <p class="main-meta">
                <el-icon><OfficeBuilding /></el-icon>
                {{ detailCollegeHeading }}
              </p>
            </header>
            <div class="main-body prose">
              {{ collegeIntroExcerpt(detailCollegeMeta.content) }}
            </div>
            <footer v-if="userRole === 'ADMIN'" class="main-foot">
              <el-button type="primary" @click="handleEditCollegeFromDetail">编辑学院信息</el-button>
            </footer>
          </template>
          <template v-else-if="specPane.selected">
            <header class="main-head">
              <h2 class="main-title">{{ specPane.selected.name }}</h2>
              <p class="main-meta">
                <el-icon><OfficeBuilding /></el-icon>
                {{ specPane.selected.collegeName || detailCollegeHeading }}
              </p>
            </header>
            <div class="main-body prose">
              {{ specPane.selected.content?.trim() || '暂无专业介绍，管理员可在编辑中补充。' }}
            </div>
            <footer v-if="userRole === 'ADMIN'" class="main-foot">
              <el-button type="primary" @click="handleEditSpeciality(specPane.selected)">编辑</el-button>
              <el-button type="danger" @click="handleDeleteSpeciality(specPane.selected.id)">删除</el-button>
            </footer>
          </template>
        </main>
      </div>
    </template>

    <!-- 学院弹窗 -->
    <el-dialog
      title="学院信息"
      width="min(480px, 92vw)"
      v-model="data.formVisible"
      :close-on-click-modal="false"
      destroy-on-close
      class="college-dialog"
    >
      <el-form :model="data.form" label-width="100px" class="college-form">
        <el-form-item label="学院名称" prop="name">
          <el-input v-model="data.form.name" autocomplete="off" />
        </el-form-item>
        <el-form-item label="学院介绍" prop="content">
          <el-input type="textarea" :rows="4" v-model="data.form.content" autocomplete="off" />
        </el-form-item>
        <el-form-item label="学生人数" prop="num">
          <el-input v-model="data.form.num" autocomplete="off" disabled />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="data.formVisible = false">取 消</el-button>
          <el-button type="primary" @click="saveCollege">保 存</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 专业弹窗（沿用原 Speciality 逻辑，学院固定为当前页） -->
    <el-dialog
      title="专业信息"
      width="min(480px, 92vw)"
      v-model="specPane.formVisible"
      :close-on-click-modal="false"
      destroy-on-close
    >
      <el-form :model="specPane.form" label-width="100px" class="college-form">
        <el-form-item label="专业名称" prop="name">
          <el-input v-model="specPane.form.name" autocomplete="off" />
        </el-form-item>
        <el-form-item label="所属学院" prop="collegeId">
          <el-input :model-value="detailCollegeHeading" disabled />
        </el-form-item>
        <el-form-item label="专业介绍" prop="content">
          <el-input type="textarea" :rows="8" v-model="specPane.form.content" placeholder="专业培养目标、课程特色等" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="specPane.formVisible = false">取 消</el-button>
          <el-button type="primary" @click="saveSpeciality">保 存</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import request from '@/utils/request'
import { reactive, computed, watch, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import {
  Plus,
  School,
  OfficeBuilding,
  User,
  ArrowLeft,
} from '@element-plus/icons-vue'

/** 左侧栏「学院介绍」项与右侧主区选中态的标识 */
const SIDEBAR_COLLEGE_INTRO = 'college-intro'

const route = useRoute()
const router = useRouter()

const data = reactive({
  pageNum: 1,
  pageSize: 9,
  total: 0,
  formVisible: false,
  form: {},
  tableData: [],
  name: null,
  user: JSON.parse(localStorage.getItem('system-user') || '{}'),
  loading: false,
})

const specPane = reactive({
  list: [],
  loading: false,
  search: '',
  /** @type {'college-intro'|number} */
  activeSidebarKey: SIDEBAR_COLLEGE_INTRO,
  selected: null,
  formVisible: false,
  form: {},
})

const detailCollegeMeta = reactive({
  loading: false,
  name: '',
  content: '',
  num: 0,
})

const userRole = computed(() => {
  const r = data.user?.role
  if (r == null || String(r).trim() === '') return ''
  return String(r).trim().toUpperCase()
})

const currentCollegeId = computed(() => {
  const raw = route.query.collegeId
  if (raw == null || raw === '') return null
  const n = Number(raw)
  return Number.isFinite(n) && n > 0 ? n : null
})

const isCollegeDetail = computed(() => currentCollegeId.value != null)

const detailCollegeTitle = computed(() => {
  const q = route.query.collegeName
  if (q != null && String(q).trim() !== '') return String(q).trim()
  return '学院'
})

const detailCollegeHeading = computed(() => {
  const n = detailCollegeMeta.name?.trim()
  if (n) return n
  return detailCollegeTitle.value
})

function resetDetailCollegeMeta() {
  detailCollegeMeta.loading = false
  detailCollegeMeta.name = ''
  detailCollegeMeta.content = ''
  detailCollegeMeta.num = 0
}

function collegeBriefStorageKey(collegeId) {
  return `college-brief-${collegeId}`
}

/** 从学院列表点进详情时写入，详情页优先展示，避免 selectById 未部署/失败时出现空白 */
function saveCollegeBriefToSession(row) {
  if (row == null || row.id == null) return
  try {
    sessionStorage.setItem(
      collegeBriefStorageKey(row.id),
      JSON.stringify({
        id: row.id,
        name: row.name ?? '',
        content: row.content ?? '',
        num: row.num,
      })
    )
  } catch (_) {}
}

function readCollegeBriefFromSession(collegeId) {
  if (collegeId == null) return null
  try {
    const raw = sessionStorage.getItem(collegeBriefStorageKey(collegeId))
    if (!raw) return null
    return JSON.parse(raw)
  } catch (_) {
    return null
  }
}

function applyCollegeBriefPayload(payload) {
  if (!payload || typeof payload !== 'object') return
  if (payload.name != null) detailCollegeMeta.name = String(payload.name)
  if (payload.content != null) detailCollegeMeta.content = String(payload.content)
  if (payload.num != null && payload.num !== '') {
    const n = Number(payload.num)
    if (!Number.isNaN(n)) detailCollegeMeta.num = n
  }
}

function loadCollegeBrief() {
  const cid = currentCollegeId.value
  if (cid == null) {
    resetDetailCollegeMeta()
    return
  }

  const cached = readCollegeBriefFromSession(cid)
  if (cached) {
    applyCollegeBriefPayload(cached)
  }

  const hasPreview = String(detailCollegeMeta.content || '').trim().length > 0
  detailCollegeMeta.loading = !hasPreview
  request
    .get(`/college/selectById/${cid}`)
    .then((res) => {
      const ok =
        (String(res?.code) === '200' || res?.code === 200) &&
        res?.data != null &&
        typeof res.data === 'object'
      if (ok) {
        const d = res.data
        detailCollegeMeta.name = d.name != null ? String(d.name) : ''
        detailCollegeMeta.content = d.content != null ? String(d.content) : ''
        detailCollegeMeta.num = d.num == null || d.num === '' ? 0 : Number(d.num)
      } else if (res?.msg) {
        ElMessage.warning(res.msg)
      }

      if (!String(detailCollegeMeta.content || '').trim() && cached) {
        applyCollegeBriefPayload(cached)
      }
    })
    .catch(() => {
      if (cached) {
        applyCollegeBriefPayload(cached)
      }
      if (!String(detailCollegeMeta.content || '').trim()) {
        ElMessage.error('加载学院信息失败，已尝试使用列表缓存。请确认后端已启用 GET /college/selectById/{id} 并重启服务。')
      }
    })
    .finally(() => {
      detailCollegeMeta.loading = false
    })
}

function refreshUserFromStorage() {
  try {
    const raw = localStorage.getItem('system-user')
    if (!raw) return
    Object.assign(data.user, JSON.parse(raw))
  } catch (_) {}
}

function collegeIntroExcerpt(content) {
  if (content == null || String(content).trim() === '') return '暂无学院介绍'
  return String(content).trim()
}

function collegeIntroTitle(content) {
  if (content == null || String(content).trim() === '') return undefined
  return String(content).trim()
}

const load = () => {
  refreshUserFromStorage()
  data.loading = true
  request
    .get('/college/selectPage', {
      params: {
        pageNum: data.pageNum,
        pageSize: data.pageSize,
        name: data.name,
      },
    })
    .then((res) => {
      const list = res.data?.list || []
      data.tableData = list.map((r) => ({
        ...r,
        num: r.num == null ? 0 : r.num,
      }))
      data.total = res.data?.total ?? 0
    })
    .finally(() => {
      data.loading = false
    })
}

function openCollegeSpecialities(row) {
  saveCollegeBriefToSession(row)
  router.push({
    path: '/college',
    query: {
      collegeId: String(row.id),
      collegeName: row.name || '',
    },
  })
}

function goBackToColleges() {
  router.push({ path: '/college' })
  specPane.selected = null
  specPane.list = []
  specPane.activeSidebarKey = SIDEBAR_COLLEGE_INTRO
  resetDetailCollegeMeta()
  load()
}

function loadSpecialities() {
  const cid = currentCollegeId.value
  if (cid == null) return
  specPane.loading = true
  const prevKey = specPane.activeSidebarKey
  request
    .get('/speciality/selectPage', {
      params: {
        pageNum: 1,
        pageSize: 500,
        collegeId: cid,
        name: specPane.search?.trim() || undefined,
      },
    })
    .then((res) => {
      const list = res.data?.list || []
      specPane.list = list
      if (prevKey === SIDEBAR_COLLEGE_INTRO) {
        specPane.selected = null
        return
      }
      const again = list.find((x) => x.id === prevKey)
      if (again) {
        specPane.selected = again
      } else {
        specPane.activeSidebarKey = SIDEBAR_COLLEGE_INTRO
        specPane.selected = null
      }
    })
    .finally(() => {
      specPane.loading = false
    })
}

function selectCollegeIntro() {
  specPane.activeSidebarKey = SIDEBAR_COLLEGE_INTRO
  specPane.selected = null
}

function selectSpeciality(s) {
  specPane.activeSidebarKey = s.id
  specPane.selected = s
}

function handleEditCollegeFromDetail() {
  const cid = currentCollegeId.value
  if (cid == null) return
  data.form = {
    id: cid,
    name: detailCollegeMeta.name || detailCollegeHeading.value,
    content: detailCollegeMeta.content || '',
    num: detailCollegeMeta.num ?? 0,
  }
  data.formVisible = true
}

watch(
  () => currentCollegeId.value,
  (cid) => {
    if (cid != null) {
      resetDetailCollegeMeta()
      specPane.search = ''
      specPane.activeSidebarKey = SIDEBAR_COLLEGE_INTRO
      specPane.selected = null
      loadCollegeBrief()
      loadSpecialities()
    } else {
      resetDetailCollegeMeta()
    }
  },
  { immediate: true }
)

const handleAdd = () => {
  data.form = {}
  data.form.num = 0
  data.formVisible = true
}

const handleEdit = (row) => {
  data.form = JSON.parse(JSON.stringify(row))
  data.form.num = data.form.num == null ? 0 : data.form.num
  data.formVisible = true
}

const addCollege = () => {
  request.post('/college/add', data.form).then((res) => {
    if (res.code === '200') {
      load()
      ElMessage.success('操作成功')
      data.formVisible = false
    } else {
      ElMessage.error(res.msg)
    }
  })
}

const updateCollege = () => {
  request.put('/college/update', data.form).then((res) => {
    if (res.code === '200') {
      load()
      ElMessage.success('操作成功')
      data.formVisible = false
      const cid = currentCollegeId.value
      if (cid != null && data.form.id === cid) {
        loadCollegeBrief()
      }
    } else {
      ElMessage.error(res.msg)
    }
  })
}

const saveCollege = () => {
  data.form.id ? updateCollege() : addCollege()
}

const handleDelete = (id) => {
  ElMessageBox.confirm('删除后数据无法恢复，您确定删除吗?', '删除确认', { type: 'warning' })
    .then(() => {
      request.delete('/college/delete/' + id).then((res) => {
        if (res.code === '200') {
          load()
          ElMessage.success('操作成功')
        } else {
          ElMessage.error(res.msg)
        }
      })
    })
    .catch(() => {})
}

const reset = () => {
  data.name = null
  data.pageNum = 1
  load()
}

const changePage = (pageNum) => {
  data.pageNum = pageNum
  load()
}

function handleAddSpeciality() {
  const cid = currentCollegeId.value
  if (cid == null) return
  specPane.form = {
    name: '',
    collegeId: cid,
    content: '',
  }
  specPane.formVisible = true
}

function handleEditSpeciality(row) {
  specPane.form = JSON.parse(JSON.stringify(row))
  specPane.formVisible = true
}

function addSpeciality() {
  request.post('/speciality/add', specPane.form).then((res) => {
    if (res.code === '200') {
      loadSpecialities()
      ElMessage.success('操作成功')
      specPane.formVisible = false
    } else {
      ElMessage.error(res.msg)
    }
  })
}

function updateSpeciality() {
  request.put('/speciality/update', specPane.form).then((res) => {
    if (res.code === '200') {
      loadSpecialities()
      ElMessage.success('操作成功')
      specPane.formVisible = false
    } else {
      ElMessage.error(res.msg)
    }
  })
}

function saveSpeciality() {
  const cid = currentCollegeId.value
  if (cid != null) specPane.form.collegeId = cid
  specPane.form.id ? updateSpeciality() : addSpeciality()
}

function handleDeleteSpeciality(id) {
  ElMessageBox.confirm('删除后数据无法恢复，您确定删除吗?', '删除确认', { type: 'warning' })
    .then(() => {
      request.delete('/speciality/delete/' + id).then((res) => {
        if (res.code === '200') {
          if (specPane.selected?.id === id) {
            specPane.selected = null
            specPane.activeSidebarKey = SIDEBAR_COLLEGE_INTRO
          }
          loadSpecialities()
          ElMessage.success('操作成功')
        } else {
          ElMessage.error(res.msg)
        }
      })
    })
    .catch(() => {})
}

onMounted(() => {
  if (currentCollegeId.value == null) {
    load()
  }
})
</script>

<style scoped>
.college-page {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 16px;
  font-family: var(--font-sans);
}

.college-toolbar {
  padding: 18px 20px;
}

.toolbar-head {
  display: flex;
  flex-wrap: wrap;
  align-items: flex-start;
  justify-content: space-between;
  gap: 14px;
  margin-bottom: 16px;
}

.page-title {
  margin: 0;
  font-size: 1.35rem;
  font-weight: 700;
  letter-spacing: -0.02em;
  color: var(--color-text);
}

.page-sub {
  margin: 6px 0 0;
  font-size: 13px;
  color: var(--color-text-muted);
  line-height: 1.5;
  max-width: 52ch;
}

.toolbar-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.btn-icon {
  margin-right: 4px;
  vertical-align: middle;
}

.toolbar-search {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 10px;
}

.search-input {
  width: min(320px, 100%);
}

.college-body {
  padding: 20px;
  flex: 1;
  min-height: 200px;
}

.grid-wrap {
  min-height: 120px;
}

.college-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 18px;
}

.college-card {
  position: relative;
  display: flex;
  flex-direction: column;
  padding: 18px 18px 14px;
  background: var(--color-bg-elevated);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-soft);
  overflow: hidden;
  transition:
    transform var(--duration) var(--ease-out),
    box-shadow var(--duration) var(--ease-out),
    border-color var(--duration) var(--ease-out);
  animation: college-in var(--duration) var(--ease-out) backwards;
  animation-delay: calc(min(var(--i, 0), 14) * 40ms);
}

.college-card--open {
  cursor: pointer;
}

@media (prefers-reduced-motion: reduce) {
  .college-card {
    animation: none;
  }
}

.college-card:hover {
  transform: translateY(-2px);
  border-color: var(--color-primary-muted);
  box-shadow:
    0 16px 48px -20px rgba(15, 23, 42, 0.14),
    0 0 0 1px var(--color-primary-soft);
}

.college-card:focus-visible {
  outline: 2px solid var(--color-primary);
  outline-offset: 2px;
}

.college-card__accent {
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 4px;
  background: linear-gradient(180deg, var(--color-primary) 0%, var(--color-primary-hover) 100%);
  border-radius: var(--radius-md) 0 0 var(--radius-md);
  opacity: 0.92;
}

.college-card__top {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding-left: 8px;
}

.college-card__icon {
  flex-shrink: 0;
  width: 44px;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
  background: var(--color-primary-soft);
  color: var(--color-primary-hover);
  font-size: 22px;
}

.college-card__head {
  flex: 1;
  min-width: 0;
}

.college-card__name {
  margin: 0;
  font-size: 1.05rem;
  font-weight: 700;
  line-height: 1.35;
  color: var(--color-text);
  letter-spacing: -0.01em;
}

.college-card__badge {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  margin-top: 8px;
  padding: 4px 10px;
  font-size: 12px;
  font-weight: 600;
  color: var(--color-primary-hover);
  background: var(--color-primary-soft);
  border-radius: 999px;
  font-variant-numeric: tabular-nums;
}

.badge-icon {
  font-size: 14px;
}

/* 学院列表卡片节选正文 */
.college-intro-copy {
  font-size: 13px;
  line-height: 1.55;
  color: var(--color-text-muted);
  word-break: break-word;
  overflow-wrap: anywhere;
}

.college-card__desc {
  margin: 14px 0 0;
  padding-left: 8px;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
  flex: 1;
  min-height: 3.1em;
}

.college-card__hint {
  margin: 10px 0 0;
  padding-left: 8px;
  font-size: 12px;
  font-weight: 600;
  color: var(--color-primary-hover);
}

.college-card__foot {
  display: flex;
  justify-content: flex-end;
  gap: 4px;
  margin-top: 10px;
  padding-top: 12px;
  border-top: 1px solid var(--color-border);
}

.college-footer {
  display: flex;
  justify-content: center;
  padding: 14px 20px;
}

.empty-state {
  text-align: center;
  padding: 48px 20px;
  color: var(--color-text-muted);
}

.empty-icon {
  font-size: 48px;
  color: var(--color-text-subtle);
  margin-bottom: 12px;
}

.empty-title {
  margin: 0;
  font-size: 1rem;
  font-weight: 600;
  color: var(--color-text);
}

.empty-hint {
  margin: 8px 0 0;
  font-size: 13px;
}

@keyframes college-in {
  from {
    opacity: 0;
    transform: translateY(8px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.college-form {
  padding-right: 12px;
}

/* —— 专业主从页 —— */
.detail-head {
  padding: 18px 20px;
}

.detail-head__row {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 10px;
}

.back-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  color: var(--color-text-muted);
}

.detail-head__actions {
  display: flex;
  gap: 8px;
}

.detail-title {
  margin: 0;
  font-size: 1.35rem;
  font-weight: 700;
  letter-spacing: -0.02em;
  color: var(--color-text);
}

.detail-sub {
  margin: 8px 0 0;
  font-size: 13px;
  color: var(--color-text-muted);
  max-width: 60ch;
  line-height: 1.5;
}

/* 详情模式：在 flex 主区内撑满高度，仅右侧正文区域滚动（高度由 .manager-main 决定） */
.college-page--detail {
  flex: 1;
  min-height: 0;
  overflow: hidden;
}

.college-page--detail > .detail-head {
  flex-shrink: 0;
}

.detail-split {
  display: grid;
  grid-template-columns: minmax(240px, 300px) 1fr;
  gap: 16px;
  align-items: stretch;
  min-height: 420px;
}

.college-page--detail .detail-split {
  flex: 1;
  min-height: 0;
  overflow: hidden;
}

@media (max-width: 900px) {
  .college-page--detail {
    height: auto;
    max-height: none;
    overflow: visible;
  }

  .college-page--detail .detail-split {
    flex: none;
    min-height: 420px;
    overflow: visible;
  }

  .detail-split {
    grid-template-columns: 1fr;
  }

  .college-page--detail .detail-main {
    overflow-y: visible;
    min-height: 360px;
  }

  .aside-scroll {
    max-height: min(48vh, 360px);
  }
}

.detail-aside {
  padding: 0;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  min-height: 0;
}

.aside-title-bar {
  padding: 12px 16px;
  font-size: 15px;
  font-weight: 700;
  color: #fff;
  background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-primary-hover) 100%);
  letter-spacing: 0.02em;
}

.aside-search {
  display: flex;
  gap: 8px;
  padding: 12px;
  border-bottom: 1px solid var(--color-border);
  background: var(--color-bg-elevated);
}

.aside-search .el-input {
  flex: 1;
  min-width: 0;
}

.aside-list-wrap {
  flex: 1;
  min-height: 0;
  background: rgba(248, 250, 252, 0.85);
}

.aside-scroll {
  height: 100%;
  max-height: none;
}

.spec-list {
  list-style: none;
  margin: 0;
  padding: 8px;
}

.spec-item {
  display: block;
  width: 100%;
  text-align: left;
  padding: 10px 12px;
  margin-bottom: 4px;
  border: none;
  border-radius: var(--radius-sm);
  background: transparent;
  font-family: var(--font-sans);
  font-size: 14px;
  font-weight: 500;
  color: var(--color-text);
  cursor: pointer;
  transition:
    background var(--duration) var(--ease-out),
    color var(--duration) var(--ease-out);
}

.spec-item:hover {
  background: var(--color-primary-soft);
  color: var(--color-primary-hover);
}

.spec-item--intro {
  font-weight: 700;
  margin-bottom: 10px;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--color-border);
  border-radius: var(--radius-sm) var(--radius-sm) 0 0;
}

.spec-item--active {
  background: var(--color-primary-soft);
  color: var(--color-primary-hover);
  font-weight: 700;
  box-shadow: inset 0 0 0 1px var(--color-primary-muted);
}

.aside-empty {
  padding: 24px 16px;
  text-align: center;
  font-size: 13px;
  color: var(--color-text-muted);
}

.detail-main {
  padding: 22px 24px;
  display: flex;
  flex-direction: column;
  min-height: 360px;
  min-width: 0;
}

.college-page--detail .detail-main {
  min-height: 0;
  overflow-y: auto;
  -webkit-overflow-scrolling: touch;
}

.college-page--detail .main-body.prose {
  flex: 0 1 auto;
}

.main-head {
  margin-bottom: 16px;
  padding-bottom: 14px;
  border-bottom: 1px solid var(--color-border);
}

.main-title {
  margin: 0;
  font-size: 1.25rem;
  font-weight: 700;
  color: var(--color-text);
  letter-spacing: -0.02em;
}

.main-meta {
  margin: 10px 0 0;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: var(--color-text-muted);
}

.main-body.prose {
  flex: 1;
  font-size: 14px;
  line-height: 1.75;
  color: var(--color-text);
  white-space: pre-wrap;
}

.main-foot {
  margin-top: 20px;
  padding-top: 16px;
  border-top: 1px solid var(--color-border);
  display: flex;
  gap: 10px;
}

.main-placeholder {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  color: var(--color-text-muted);
  font-size: 14px;
}

.ph-icon {
  font-size: 40px;
  color: var(--color-text-subtle);
}
</style>
