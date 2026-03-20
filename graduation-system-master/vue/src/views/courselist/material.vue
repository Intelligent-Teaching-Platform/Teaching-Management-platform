<template>
  <div class="container">
    <!-- 标题与操作（教师可上传） -->
    <div class="header">
      <div class="header-left">
        <h3 class="title">课程资料</h3>
        <p class="subtitle">集中管理课件、实验文档等学习资源，支持分文件夹归档与预览下载</p>
      </div>
      <el-button v-if="data.user.role === 'TEACHER'" type="primary" @click="openUploadDialog">
        上传文件
      </el-button>
    </div>

    <!-- 无课程时提示 -->
    <el-empty v-if="!courseId && !loading" description="请从课程列表进入某一课程后再查看资料" />

    <!-- 面包屑（进入子文件夹时显示） -->
    <div v-if="courseId && breadcrumb.length > 0" class="breadcrumb">
      <el-breadcrumb separator="/">
        <el-breadcrumb-item>
          <span class="breadcrumb-link" @click="goToFolder(null)">全部资料</span>
        </el-breadcrumb-item>
        <el-breadcrumb-item v-for="(item, index) in breadcrumb" :key="item.id">
          <span
            class="breadcrumb-link"
            @click="goToFolder(item.id)"
          >{{ item.name }}</span>
        </el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <!-- 搜索 -->
    <div v-if="courseId" class="search-bar">
      <el-input
        v-model="searchQuery"
        placeholder="搜索文件名"
        class="search-input"
        clearable
        @keyup.enter="loadData"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
      <el-button type="primary" @click="loadData">搜索</el-button>
    </div>

    <!-- 资料列表（文件夹 + 文件） -->
    <div v-if="courseId" class="table-card" v-loading="loading">
      <el-table
        :data="displayList"
        stripe
        style="width: 100%"
        @row-click="onRowClick"
      >
        <el-table-column label="名称" min-width="320">
          <template #default="scope">
            <div class="name-cell">
              <el-icon v-if="scope.row.type === 'folder'" class="icon folder-icon"><Folder /></el-icon>
              <el-icon v-else class="icon file-icon"><Document /></el-icon>
              <div class="name-main">
                <span class="name" :title="scope.row.name">{{ scope.row.name }}</span>
                <div
                  v-if="scope.row.type === 'file'"
                  class="size"
                >大小：{{ formatFileSize(scope.row.size) }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column v-if="currentFolderId == null" label="所在文件夹" width="160">
          <template #default="scope">
            <template v-if="scope.row.type === 'file'">{{ getFolderName(scope.row.folderId) }}</template>
            <template v-else>—</template>
          </template>
        </el-table-column>
        <el-table-column prop="teacherName" label="上传者" width="140">
          <template #default="scope">
            {{ scope.row.type === 'file' ? scope.row.teacherName : '—' }}
          </template>
        </el-table-column>
        <el-table-column label="上传时间" width="200">
          <template #default="scope">
            {{ scope.row.type === 'file' && scope.row.createTime ? formatDateTime(scope.row.createTime) : '—' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right" @click.stop>
          <template #default="scope">
            <template v-if="scope.row.type === 'file'">
              <el-button type="primary" link @click="handleDownload(scope.row)">下载</el-button>
              <el-button
                v-if="data.user.role === 'TEACHER'"
                type="danger"
                link
                @click="handleDelete(scope.row)"
              >
                删除
              </el-button>
            </template>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-empty v-else-if="courseId && !loading && displayList.length === 0" description="当前目录为空，可上传文件或新建文件夹" />

    <!-- 文件预览弹窗 -->
    <el-dialog
      v-model="previewVisible"
      :title="previewFileName"
      width="80%"
      top="5vh"
      class="preview-dialog"
      destroy-on-close
      @close="closePreview"
    >
      <div v-if="previewUrl" class="preview-body">
        <img v-if="isPreviewImage" :src="previewUrl" class="preview-img" />
        <iframe v-else-if="isPreviewPdf" :src="previewUrl" class="preview-iframe" />
        <div v-else class="preview-tip">该类型不支持在线预览，请点击「下载」保存到本地查看。</div>
      </div>
      <template #footer>
        <el-button @click="previewVisible = false">关闭</el-button>
        <el-button type="primary" @click="previewFileRow && handleDownload(previewFileRow); previewVisible = false">下载</el-button>
      </template>
    </el-dialog>

    <!-- 上传弹窗 -->
    <el-dialog
      v-model="uploadVisible"
      title="上传文件"
      width="520px"
      :close-on-click-modal="false"
      @close="resetUpload"
    >
      <div class="upload-options">
        <div class="upload-option-row">
          <span class="label">保存位置：</span>
          <el-select
            v-model="selectedFolderId"
            placeholder="根目录（直接上传）"
            clearable
            class="folder-select"
          >
            <el-option label="根目录" :value="null" />
            <el-option
              v-for="f in folders"
              :key="f.id"
              :label="f.name"
              :value="f.id"
            />
          </el-select>
          <el-button type="primary" link @click="showNewFolder = true">新建文件夹</el-button>
        </div>
        <div v-if="showNewFolder" class="new-folder-row">
          <el-input
            v-model="newFolderName"
            placeholder="输入文件夹名称"
            size="small"
            style="width: 200px; margin-right: 8px"
            @keyup.enter="createFolder"
          />
          <el-button size="small" @click="createFolder" :loading="creatingFolder">确定</el-button>
          <el-button size="small" @click="showNewFolder = false; newFolderName = ''">取消</el-button>
        </div>
      </div>
      <el-upload
        ref="uploadRef"
        drag
        :auto-upload="false"
        :multiple="true"
        :on-change="onFileChange"
        :file-list="fileList"
      >
        <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
        <div class="el-upload__text">将文件拖到此处，或<em>点击上传</em></div>
        <template #tip>
          <div class="el-upload__tip">支持任意格式，单文件不超过 100MB</div>
        </template>
      </el-upload>
      <template #footer>
        <el-button @click="uploadVisible = false">取消</el-button>
        <el-button type="primary" :loading="uploading" @click="submitUpload">确定上传</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, reactive, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Document, UploadFilled, Folder } from '@element-plus/icons-vue'
import request from '@/utils/request'

const route = useRoute()

const data = reactive({
  user: JSON.parse(localStorage.getItem('system-user') || '{}'),
})

const loading = ref(false)
const uploadVisible = ref(false)
const uploading = ref(false)
const searchQuery = ref('')
const fileList = ref([])
const uploadFiles = ref([])
const list = ref([])
const folders = ref([])
const selectedFolderId = ref(null)
const showNewFolder = ref(false)
const newFolderName = ref('')
const creatingFolder = ref(false)
const currentFolderId = ref(null)
const breadcrumb = ref([])
const previewVisible = ref(false)
const previewUrl = ref('')
const previewFileName = ref('')
const previewFileRow = ref(null)

const courseId = computed(() => {
  const id = route.query.id || route.params.id
  return id != null ? Number(id) : null
})

const currentLevelFolders = computed(() => {
  const pid = currentFolderId.value
  return folders.value.filter((f) => (f.parentId == null && pid == null) || f.parentId === pid)
})

const currentLevelFiles = computed(() => {
  const fid = currentFolderId.value
  let files = list.value.filter((m) => (m.folderId == null && fid == null) || m.folderId === fid)
  if (searchQuery.value) {
    const q = searchQuery.value.toLowerCase()
    files = files.filter((item) => item.name && item.name.toLowerCase().includes(q))
  }
  return files
})

const displayList = computed(() => {
  const folderRows = currentLevelFolders.value.map((f) => ({ type: 'folder', ...f }))
  const fileRows = currentLevelFiles.value.map((m) => ({ type: 'file', ...m }))
  return [...folderRows, ...fileRows]
})

const isPreviewImage = computed(() => {
  const name = (previewFileName.value || '').toLowerCase()
  return /\.(jpg|jpeg|png|gif|webp|bmp|svg)$/i.test(name)
})

const isPreviewPdf = computed(() => {
  const name = (previewFileName.value || '').toLowerCase()
  return name.endsWith('.pdf')
})

async function loadData() {
  if (!courseId.value) return
  loading.value = true
  try {
    const [listRes, foldersRes] = await Promise.all([
      request.get('/material/listByCourse', { params: { courseId: courseId.value } }),
      request.get('/material/folders', { params: { courseId: courseId.value } }),
    ])
    list.value = Array.isArray(listRes?.data) ? listRes.data : []
    folders.value = Array.isArray(foldersRes?.data) ? foldersRes.data : []
  } catch (e) {
    console.error(e)
    ElMessage.error('加载失败')
    list.value = []
  } finally {
    loading.value = false
  }
}

async function openUploadDialog() {
  if (!courseId.value) {
    ElMessage.warning('无法获取课程信息')
    return
  }
  fileList.value = []
  uploadFiles.value = []
  selectedFolderId.value = null
  showNewFolder.value = false
  newFolderName.value = ''
  uploadVisible.value = true
  await loadFolders()
}

async function loadFolders() {
  if (!courseId.value) return
  try {
    const res = await request.get('/material/folders', { params: { courseId: courseId.value } })
    folders.value = Array.isArray(res?.data) ? res.data : []
  } catch (e) {
    folders.value = []
  }
}

async function createFolder() {
  const name = newFolderName.value?.trim()
  if (!name) {
    ElMessage.warning('请输入文件夹名称')
    return
  }
  creatingFolder.value = true
  try {
    const res = await request.post('/material/folder', null, {
      params: {
        name,
        courseId: courseId.value,
        teacherId: data.user.id,
      },
    })
    const created = res?.data
    if (created?.id) {
      await loadFolders()
      selectedFolderId.value = created.id
      showNewFolder.value = false
      newFolderName.value = ''
      ElMessage.success('文件夹已创建')
    }
  } catch (e) {
    ElMessage.error(e?.msg || e?.message || '创建失败')
  } finally {
    creatingFolder.value = false
  }
}

function getFolderName(folderId) {
  if (folderId == null) return '根目录'
  const f = folders.value.find((x) => x.id === folderId)
  return f ? f.name : '—'
}

function pathToFolder(fid) {
  if (fid == null) return []
  const f = folders.value.find((x) => x.id === fid)
  if (!f) return []
  return [...pathToFolder(f.parentId), { id: f.id, name: f.name }]
}

function goToFolder(folderId) {
  currentFolderId.value = folderId
  breadcrumb.value = pathToFolder(folderId)
}

function onRowClick(row) {
  if (row.type === 'folder') {
    goToFolder(row.id)
  } else {
    handleFilePreview(row)
  }
}

function closePreview() {
  if (previewUrl.value) URL.revokeObjectURL(previewUrl.value)
  previewUrl.value = ''
  previewFileRow.value = null
}

async function handleFilePreview(row) {
  previewFileName.value = row.name || ''
  previewFileRow.value = row
  const ext = (row.name || '').split('.').pop().toLowerCase()
  const canPreview = ['pdf', 'jpg', 'jpeg', 'png', 'gif', 'webp', 'bmp', 'svg'].includes(ext)
  if (!canPreview) {
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
    ElMessage.error('预览失败')
  }
}

function onFileChange(file, files) {
  const maxSize = 100 * 1024 * 1024
  if (file.size > maxSize) {
    ElMessage.error(`文件 ${file.name} 超过 100MB`)
    return
  }
  fileList.value = files
  uploadFiles.value = files.map((f) => f.raw).filter(Boolean)
}

function resetUpload() {
  fileList.value = []
  uploadFiles.value = []
  selectedFolderId.value = null
  showNewFolder.value = false
  newFolderName.value = ''
  uploading.value = false
}

async function submitUpload() {
  if (uploadFiles.value.length === 0) {
    ElMessage.warning('请选择文件')
    return
  }
  uploading.value = true
  try {
    const formData = new FormData()
    formData.append('courseId', courseId.value)
    formData.append('teacherId', data.user.id)
    formData.append('teacherName', data.user.name || '')
    if (selectedFolderId.value != null) {
      formData.append('folderId', selectedFolderId.value)
    }
    uploadFiles.value.forEach((f) => formData.append('files', f))
    const res = await request.post('/material/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    })
    if (res && res.code === '200') {
      ElMessage.success('上传成功')
      uploadVisible.value = false
      loadData()
    } else {
      throw new Error(res?.msg || '上传失败')
    }
  } catch (e) {
    ElMessage.error(e?.message || e?.msg || '上传失败')
  } finally {
    uploading.value = false
  }
}

async function handleDownload(row) {
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
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确定删除「${row.name}」吗？`, '提示', {
      type: 'warning',
    })
    await request.delete(`/material/delete/${row.id}`)
    ElMessage.success('已删除')
    loadData()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('删除失败')
  }
}

function formatFileSize(bytes) {
  if (bytes == null) return '0 B'
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(2) + ' KB'
  if (bytes < 1024 * 1024 * 1024) return (bytes / (1024 * 1024)).toFixed(2) + ' MB'
  return (bytes / (1024 * 1024 * 1024)).toFixed(2) + ' GB'
}

function formatDateTime(str) {
  if (!str) return ''
  return new Date(str).toLocaleString()
}

watch(courseId, () => {
  if (courseId.value) loadData()
})

onMounted(() => {
  if (courseId.value) loadData()
})
</script>

<style scoped>
.container {
  padding: 20px 24px;
  box-sizing: border-box;
  min-height: calc(100vh - 60px);
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding: 16px 20px;
  background: #fff;
  border-radius: 12px;
  border: 1px solid rgba(220, 223, 230, 0.7);
}

.header-left {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.title {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: #303133;
}

.subtitle {
  margin: 0;
  font-size: 13px;
  color: #909399;
}

.search-bar {
  display: flex;
  gap: 10px;
  margin-bottom: 16px;
  margin-top: 12px;
  padding: 12px 16px;
  background: #fff;
  border-radius: 10px;
  border: 1px solid rgba(220, 223, 230, 0.6);
}

.search-input {
  max-width: 360px;
}

.file-name-cell {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.file-icon {
  margin-right: 8px;
  font-size: 20px;
  color: #409eff;
}

.name {
  cursor: pointer;
  color: #409eff;
}

.name:hover {
  text-decoration: underline;
}

.size {
  font-size: 12px;
  color: #999;
}

.upload-options {
  margin-bottom: 16px;
}

.upload-option-row {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
}

.upload-option-row .label {
  color: #606266;
  white-space: nowrap;
}

.folder-select {
  width: 220px;
}

.new-folder-row {
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px solid #eee;
}

.breadcrumb {
  margin-bottom: 12px;
  padding: 8px 0;
}

.breadcrumb-link {
  cursor: pointer;
  color: #409eff;
}
.breadcrumb-link:hover {
  text-decoration: underline;
}

.name-cell {
  display: flex;
  align-items: center;
}
.name-cell .icon {
  margin-right: 8px;
  font-size: 20px;
  vertical-align: middle;
}
.name-cell .folder-icon {
  color: #e6a23c;
}
.name-cell .file-icon {
  color: #409eff;
}

.name-main {
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.name-cell .name {
  font-weight: 500;
  color: #303133;
}
.name-cell .size {
  font-size: 12px;
  color: #999;
}

:deep(.el-table) {
  --el-table-header-bg-color: #f5f7fa;
  --el-table-border-color: #ebeef5;
}

:deep(.el-table__row) {
  cursor: pointer;
}

.table-card {
  margin-top: 12px;
  background: #fff;
  border-radius: 12px;
  border: 1px solid rgba(220, 223, 230, 0.7);
  padding: 4px 4px 8px;
}

.preview-body {
  min-height: 60vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: #f5f5f5;
}
.preview-img {
  max-width: 100%;
  max-height: 75vh;
  object-fit: contain;
}
.preview-iframe {
  width: 100%;
  height: 75vh;
  border: none;
}
.preview-tip {
  color: #909399;
  padding: 24px;
}
</style>
