<template>
  <div class="resource-page">
    <header class="resource-hero card">
      <div class="resource-hero__accent" aria-hidden="true" />
      <div class="resource-hero__icon" aria-hidden="true">
        <el-icon><FolderOpened /></el-icon>
      </div>
      <div class="resource-hero__text">
        <h1 class="resource-hero__title">资源中心</h1>
        <p class="resource-hero__sub">
          按课程浏览全校资料，支持搜索与在线预览（图片 / PDF）。上传请在各课程的「资料」页操作。
        </p>
      </div>
      <div class="resource-hero__stats" role="list">
        <div class="stat-pill" role="listitem">
          <span class="stat-pill__value">{{ stats.courseWithFiles }}</span>
          <span class="stat-pill__label">有资料课程</span>
        </div>
        <div class="stat-pill" role="listitem">
          <span class="stat-pill__value">{{ stats.fileTotal }}</span>
          <span class="stat-pill__label">文件总数</span>
        </div>
      </div>
    </header>

    <section class="resource-toolbar card" aria-label="筛选与搜索">
      <el-select
        v-model="filters.courseId"
        placeholder="全部课程"
        class="filter-select"
        clearable
        filterable
      >
        <el-option
          v-for="c in coursesSorted"
          :key="c.id"
          :label="c.name"
          :value="c.id"
        />
      </el-select>

      <el-input
        v-model="filters.keyword"
        placeholder="搜索课程名或文件名"
        class="search-input"
        clearable
        @keyup.enter="doSearch"
      >
        <template #prefix>
          <el-icon class="search-prefix-icon"><Search /></el-icon>
        </template>
        <template #suffix>
          <el-icon class="search-suffix-icon" @click="doSearch"><Search /></el-icon>
        </template>
      </el-input>

      <div class="toolbar-actions">
        <el-button @click="resetFilters">重置</el-button>
        <el-button type="primary" :loading="loading" @click="loadData">
          刷新
        </el-button>
        <el-button text type="primary" @click="showUploadHint">
          上传说明
        </el-button>
      </div>
    </section>

    <section
      class="resource-grid-section"
      v-loading="loading"
      aria-label="课程资料列表"
    >
      <div v-if="!loading && filteredFolders.length > 0" class="card-grid">
        <div
          v-for="folder in filteredFolders"
          :key="folder.courseId"
          class="resource-card"
          role="button"
          tabindex="0"
          @click="openFolder(folder)"
          @keydown.enter.prevent="openFolder(folder)"
          @keydown.space.prevent="openFolder(folder)"
        >
          <div class="card-cover">
            <img
              :src="coverForCourse(folder.courseId)"
              :alt="`「${folder.courseName}」资料封面`"
              loading="lazy"
              decoding="async"
            />
            <span class="cover-shade" aria-hidden="true" />
            <span class="file-badge">{{ folder.files.length }} 个文件</span>
          </div>
          <div class="card-body">
            <div class="card-title" :title="folder.courseName">
              {{ folder.courseName }}
            </div>
            <div class="card-meta">
              <span class="meta-size">约 {{ formatSize(folder.totalBytes) }}</span>
              <span class="meta-hint">查看列表</span>
            </div>
          </div>
        </div>
      </div>

      <el-empty
        v-else-if="!loading && filteredFolders.length === 0"
        class="resource-empty"
        description="暂无匹配的资料，可调整筛选或关键词后重试"
      >
        <el-button type="primary" @click="resetFilters">清空条件</el-button>
      </el-empty>
    </section>

    <!-- 文件列表 -->
    <el-dialog
      v-model="fileDialogVisible"
      :title="currentFolder?.courseName || '课程资料'"
      width="min(920px, 96vw)"
      top="6vh"
      class="resource-dialog"
      destroy-on-close
      @closed="onFileDialogClosed"
    >
      <template v-if="sortedCurrentFiles.length">
        <div class="dialog-toolbar">
          <span class="dialog-count">共 {{ sortedCurrentFiles.length }} 个文件</span>
          <el-input
            v-model="fileTableFilter"
            placeholder="在当前课程内筛选文件名"
            clearable
            size="small"
            class="dialog-filter-input"
          />
        </div>
        <el-table :data="displayedFilesInDialog" size="default" class="resource-table" stripe>
          <el-table-column label="文件名" min-width="260">
            <template #default="scope">
              <div class="name-cell">
                <el-icon class="name-cell__icon" :class="fileIconClass(scope.row)">
                  <component :is="fileIconComponent(scope.row)" />
                </el-icon>
                <span class="name-cell__text" :title="scope.row.name">{{ scope.row.name }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="大小" width="100">
            <template #default="scope">
              {{ formatSize(scope.row.size) }}
            </template>
          </el-table-column>
          <el-table-column label="上传时间" width="174">
            <template #default="scope">
              {{ formatDateTime(scope.row.createTime) }}
            </template>
          </el-table-column>
          <el-table-column prop="teacherName" label="上传者" width="112" show-overflow-tooltip />
          <el-table-column label="操作" width="140" align="right" fixed="right">
            <template #default="scope">
              <el-button type="primary" link @click.stop="previewFile(scope.row)">
                预览
              </el-button>
              <el-button
                type="primary"
                link
                :loading="downloadLoadingId === scope.row.id"
                @click.stop="downloadFile(scope.row)"
              >
                下载
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </template>
      <el-empty v-else description="该课程下暂无文件" />
    </el-dialog>

    <!-- 预览 -->
    <el-dialog
      v-model="previewVisible"
      :title="previewFileName"
      width="min(960px, 96vw)"
      top="4vh"
      class="resource-preview-dialog"
      destroy-on-close
      @close="closePreview"
    >
      <div v-if="previewUrl" class="preview-body">
        <img v-if="isPreviewImage" :src="previewUrl" alt="" class="preview-img" />
        <iframe v-else-if="isPreviewPdf" :src="previewUrl" title="PDF 预览" class="preview-iframe" />
        <p v-else class="preview-tip">该类型不支持在线预览，请使用「下载」到本地打开。</p>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  Search,
  FolderOpened,
  Document,
  Picture,
  Film,
  Headset,
  Files,
} from '@element-plus/icons-vue'
import request from '@/utils/request'
import { getCoverUrlForCourse } from '@/utils/courseCoverPool'

const filters = reactive({
  courseId: null,
  keyword: '',
})

const loading = ref(false)
const courses = ref([])
const resourceList = ref([])

const fileDialogVisible = ref(false)
const currentFolder = ref(null)
const fileTableFilter = ref('')

const previewVisible = ref(false)
const previewUrl = ref('')
const previewFileName = ref('')
const downloadLoadingId = ref(null)

const courseMap = computed(() => {
  const map = {}
  courses.value.forEach((c) => {
    map[c.id] = c.name
  })
  return map
})

const coursesSorted = computed(() =>
  [...courses.value].sort((a, b) =>
    String(a.name || '').localeCompare(String(b.name || ''), 'zh-CN')
  )
)

function timeValue(t) {
  const d = t ? new Date(t) : null
  return d && !Number.isNaN(d.getTime()) ? d.getTime() : 0
}

const folders = computed(() => {
  const grouped = new Map()
  resourceList.value.forEach((m) => {
    if (!m.courseId) return
    const id = m.courseId
    if (!grouped.has(id)) {
      grouped.set(id, {
        courseId: id,
        courseName: courseMap.value[id] || '未命名课程',
        files: [],
        totalBytes: 0,
      })
    }
    const entry = grouped.get(id)
    entry.files.push(m)
    entry.totalBytes += Number(m.size) || 0
  })
  return Array.from(grouped.values())
    .map((f) => ({
      ...f,
      files: [...f.files].sort((a, b) => timeValue(b.createTime) - timeValue(a.createTime)),
    }))
    .sort((a, b) => String(a.courseName).localeCompare(String(b.courseName), 'zh-CN'))
})

const filteredFolders = computed(() => {
  let list = folders.value
  if (filters.courseId) {
    list = list.filter((f) => f.courseId === filters.courseId)
  }
  if (filters.keyword.trim()) {
    const k = filters.keyword.trim().toLowerCase()
    list = list.filter(
      (f) =>
        String(f.courseName || '').toLowerCase().includes(k) ||
        f.files.some((file) => String(file.name || '').toLowerCase().includes(k))
    )
  }
  return list
})

const stats = computed(() => ({
  courseWithFiles: folders.value.length,
  fileTotal: resourceList.value.length,
}))

const sortedCurrentFiles = computed(() => {
  const files = currentFolder.value?.files || []
  return [...files].sort((a, b) => timeValue(b.createTime) - timeValue(a.createTime))
})

const displayedFilesInDialog = computed(() => {
  const q = fileTableFilter.value.trim().toLowerCase()
  if (!q) return sortedCurrentFiles.value
  return sortedCurrentFiles.value.filter((f) =>
    String(f.name || '').toLowerCase().includes(q)
  )
})

const isPreviewImage = computed(() => {
  const ext = (previewFileName.value || '').split('.').pop().toLowerCase()
  return ['jpg', 'jpeg', 'png', 'gif', 'webp', 'bmp', 'svg'].includes(ext)
})

const isPreviewPdf = computed(() => {
  const ext = (previewFileName.value || '').split('.').pop().toLowerCase()
  return ext === 'pdf'
})

function coverForCourse(courseId) {
  return getCoverUrlForCourse(courseId)
}

const doSearch = () => {}

function resetFilters() {
  filters.courseId = null
  filters.keyword = ''
}

function showUploadHint() {
  ElMessage.info({
    message: '请在「课程列表」进入课程详情，在资料页上传或管理文件。',
    duration: 4500,
  })
}

function formatSize(bytes) {
  if (!bytes && bytes !== 0) return ''
  const b = Number(bytes)
  if (Number.isNaN(b)) return ''
  if (b < 1024) return `${b} B`
  if (b < 1024 * 1024) return `${(b / 1024).toFixed(1)} KB`
  if (b < 1024 * 1024 * 1024) return `${(b / 1024 / 1024).toFixed(1)} MB`
  return `${(b / 1024 / 1024 / 1024).toFixed(1)} GB`
}

function formatDateTime(val) {
  if (!val) return ''
  const d = new Date(val)
  if (Number.isNaN(d.getTime())) return String(val)
  const pad = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(
    d.getMinutes()
  )}:${pad(d.getSeconds())}`
}

function fileExt(row) {
  return (row?.name || '').split('.').pop().toLowerCase()
}

function fileIconComponent(row) {
  const ext = fileExt(row)
  if (['jpg', 'jpeg', 'png', 'gif', 'webp', 'bmp', 'svg'].includes(ext)) return Picture
  if (['mp4', 'webm', 'mov', 'mkv'].includes(ext)) return Film
  if (['mp3', 'wav', 'ogg', 'm4a'].includes(ext)) return Headset
  if (['zip', 'rar', '7z', 'tar', 'gz'].includes(ext)) return Files
  return Document
}

function fileIconClass(row) {
  const ext = fileExt(row)
  if (ext === 'pdf') return 'is-pdf'
  if (['jpg', 'jpeg', 'png', 'gif', 'webp', 'bmp', 'svg'].includes(ext)) return 'is-img'
  if (['zip', 'rar', '7z', 'tar', 'gz'].includes(ext)) return 'is-zip'
  return ''
}

function openFolder(folder) {
  currentFolder.value = folder
  fileTableFilter.value = ''
  fileDialogVisible.value = true
}

function onFileDialogClosed() {
  currentFolder.value = null
  fileTableFilter.value = ''
}

function closePreview() {
  if (previewUrl.value) URL.revokeObjectURL(previewUrl.value)
  previewUrl.value = ''
  previewFileName.value = ''
}

async function previewFile(row) {
  previewFileName.value = row.name || ''
  const ext = fileExt(row)
  const canPreview = ['pdf', 'jpg', 'jpeg', 'png', 'gif', 'webp', 'bmp', 'svg'].includes(ext)
  if (!canPreview) {
    if (previewUrl.value) URL.revokeObjectURL(previewUrl.value)
    previewUrl.value = ''
    previewVisible.value = true
    return
  }
  try {
    const res = await request.get(`/material/download/${row.id}`, { responseType: 'blob' })
    const blob = res && res.data instanceof Blob ? res.data : new Blob([res])
    if (previewUrl.value) URL.revokeObjectURL(previewUrl.value)
    previewUrl.value = URL.createObjectURL(blob)
    previewVisible.value = true
  } catch (e) {
    console.error(e)
    ElMessage.error('预览失败，请尝试下载后查看')
  }
}

async function downloadFile(row) {
  if (!row?.id) return
  downloadLoadingId.value = row.id
  try {
    const res = await request.get(`/material/download/${row.id}`, { responseType: 'blob' })
    const blob = res && res.data instanceof Blob ? res.data : new Blob([res])
    const name = row.name || 'download'
    const a = document.createElement('a')
    a.href = URL.createObjectURL(blob)
    a.download = name
    a.click()
    URL.revokeObjectURL(a.href)
  } catch (e) {
    console.error(e)
    ElMessage.error('下载失败')
  } finally {
    downloadLoadingId.value = null
  }
}

async function loadData() {
  loading.value = true
  try {
    const [courseRes, materialRes] = await Promise.all([
      request.get('/course/selectAll'),
      request.get('/material/selectAll'),
    ])
    if (courseRes.code === '200') {
      courses.value = courseRes.data || []
    }
    if (materialRes.code === '200') {
      resourceList.value = materialRes.data || []
    }
  } catch (e) {
    console.error(e)
    ElMessage.error('加载资源失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
.resource-page {
  padding: 16px 20px 28px;
  min-height: 100%;
  min-width: 100%;
  box-sizing: border-box;
  font-family: var(--font-sans);
  color: var(--color-text);
  background: transparent;
}

.resource-hero {
  position: relative;
  display: grid;
  grid-template-columns: auto 1fr auto;
  grid-template-rows: auto auto;
  gap: 12px 20px;
  align-items: center;
  margin-bottom: 16px;
  overflow: hidden;
  border: 1px solid var(--color-border);
  box-shadow: var(--shadow-soft);
}

@media (max-width: 768px) {
  .resource-hero {
    grid-template-columns: 1fr;
  }
  .resource-hero__stats {
    grid-column: 1 / -1;
    justify-content: flex-start;
    flex-wrap: wrap;
  }
}

.resource-hero__accent {
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 4px;
  background: linear-gradient(
    180deg,
    var(--color-primary) 0%,
    color-mix(in srgb, var(--color-primary) 65%, #0f766e) 100%
  );
  border-radius: 2px;
  pointer-events: none;
}

.resource-hero__icon {
  grid-row: 1 / span 2;
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

.resource-hero__text {
  min-width: 0;
}

.resource-hero__title {
  margin: 0 0 6px;
  font-size: 1.35rem;
  font-weight: 700;
  letter-spacing: -0.02em;
  color: var(--color-text);
}

.resource-hero__sub {
  margin: 0;
  font-size: 0.9rem;
  color: var(--color-text-muted);
  line-height: 1.55;
  max-width: 52rem;
}

.resource-hero__stats {
  grid-column: 3;
  grid-row: 1 / span 2;
  display: flex;
  gap: 10px;
  align-items: center;
  flex-wrap: nowrap;
}

.stat-pill {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  padding: 10px 14px;
  border-radius: var(--radius-md);
  background: var(--color-bg-app);
  border: 1px solid var(--color-border);
  min-width: 92px;
}

.stat-pill__value {
  font-size: 1.15rem;
  font-weight: 700;
  font-variant-numeric: tabular-nums;
  color: var(--color-primary-hover);
}

.stat-pill__label {
  font-size: 11px;
  color: var(--color-text-subtle);
  margin-top: 2px;
}

.resource-toolbar {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
  border: 1px solid var(--color-border);
}

.filter-select {
  width: 200px;
}

.search-input {
  width: min(280px, 100%);
  flex: 1 1 200px;
}

.search-prefix-icon {
  color: var(--color-text-subtle);
}

.search-suffix-icon {
  cursor: pointer;
  color: var(--color-text-muted);
  transition: color var(--duration) var(--ease-out);
}

.search-suffix-icon:hover {
  color: var(--color-primary);
}

.toolbar-actions {
  margin-left: auto;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.resource-grid-section {
  min-height: 200px;
  border-radius: var(--radius-md);
}

.card-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(196px, 1fr));
  gap: 18px;
}

.resource-card {
  background: var(--color-bg-elevated);
  border-radius: var(--radius-lg);
  border: 1px solid var(--color-border);
  box-shadow: var(--shadow-soft);
  overflow: hidden;
  cursor: pointer;
  transition:
    transform var(--duration) var(--ease-out),
    box-shadow var(--duration) var(--ease-out),
    border-color var(--duration) var(--ease-out);
}

.resource-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 16px 40px -20px rgba(15, 23, 42, 0.25);
  border-color: var(--color-primary-muted);
}

.resource-card:focus-visible {
  outline: 2px solid var(--color-primary);
  outline-offset: 2px;
}

@media (prefers-reduced-motion: reduce) {
  .resource-card {
    transition: none;
  }
  .resource-card:hover {
    transform: none;
  }
}

.card-cover {
  position: relative;
  width: 100%;
  padding-top: 58%;
  overflow: hidden;
  background: linear-gradient(135deg, #e8ecf4 0%, #f0f3f8 100%);
}

.card-cover img {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.cover-shade {
  position: absolute;
  inset: 0;
  background: linear-gradient(180deg, transparent 40%, rgba(15, 23, 42, 0.45) 100%);
  pointer-events: none;
}

.file-badge {
  position: absolute;
  top: 10px;
  right: 10px;
  padding: 4px 10px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;
  color: #fff;
  background: rgba(15, 23, 42, 0.55);
  backdrop-filter: blur(8px);
}

.card-body {
  padding: 12px 14px 14px;
}

.card-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--color-text);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin-bottom: 8px;
}

.card-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
  color: var(--color-text-muted);
}

.meta-hint {
  color: var(--color-primary);
  font-weight: 500;
}

.resource-empty {
  padding: 48px 16px;
  background: var(--color-bg-elevated);
  border: 1px dashed var(--color-border-strong);
  border-radius: var(--radius-md);
}

/* —— 文件弹窗 —— */
.resource-dialog :deep(.el-dialog) {
  border-radius: var(--radius-lg);
  overflow: hidden;
  border: 1px solid var(--color-border);
  box-shadow: 0 24px 48px -24px rgba(15, 23, 42, 0.35);
}

.resource-dialog :deep(.el-dialog__header) {
  margin: 0;
  padding: 16px 20px;
  border-bottom: 1px solid var(--color-border);
  background: var(--color-bg-elevated);
}

.resource-dialog :deep(.el-dialog__title) {
  font-size: 16px;
  font-weight: 600;
  color: var(--color-text);
}

.resource-dialog :deep(.el-dialog__body) {
  padding: 16px 20px 20px;
  background: var(--color-bg-app);
}

.dialog-toolbar {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.dialog-count {
  font-size: 13px;
  color: var(--color-text-muted);
}

.dialog-filter-input {
  width: min(240px, 100%);
  margin-left: auto;
}

.name-cell {
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 0;
}

.name-cell__icon {
  flex-shrink: 0;
  font-size: 18px;
  color: var(--color-text-subtle);
}

.name-cell__icon.is-pdf {
  color: #c2410c;
}

.name-cell__icon.is-img {
  color: var(--color-primary);
}

.name-cell__icon.is-zip {
  color: #475569;
}

.name-cell__text {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.resource-table {
  border-radius: var(--radius-sm);
  overflow: hidden;
}

.resource-table :deep(.el-table__header th) {
  font-weight: 600;
  font-size: 13px;
}

/* 预览弹窗 */
.resource-preview-dialog :deep(.el-dialog__body) {
  padding: 12px 16px 20px;
  background: var(--color-bg-app);
}

.preview-body {
  min-height: 52vh;
  display: flex;
  align-items: center;
  justify-content: center;
}

.preview-img {
  max-width: 100%;
  max-height: 72vh;
  object-fit: contain;
  border-radius: var(--radius-sm);
}

.preview-iframe {
  width: 100%;
  height: 72vh;
  border: none;
  border-radius: var(--radius-sm);
  background: #fff;
}

.preview-tip {
  margin: 0;
  color: var(--color-text-muted);
  text-align: center;
  padding: 24px;
}
</style>
