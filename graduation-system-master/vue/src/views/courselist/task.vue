<template>
  <div class="task-container">
    <!-- 页面头部 -->
    <div class="header">
      <div class="header-left">
        <h2 class="page-title">发布任务</h2>
        <p class="page-subtitle">为班级布置课后与实验任务，支持筛选与快速管理</p>
      </div>
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>
        新建任务
      </el-button>
    </div>

    <!-- 搜索和筛选区域 -->
    <div class="filter-bar">
      <div class="filter-left">
        <el-input
          v-model="searchQuery"
          placeholder="搜索任务名称"
          clearable
          class="search-input"
          @keyup.enter="handleSearch"
          @clear="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
      </div>
      <div class="filter-actions">
        <el-button type="primary" @click="handleSearch">搜索</el-button>
        <el-button @click="resetSearch">重置</el-button>
      </div>
    </div>

    <!-- 任务卡片列表 -->
    <div class="content" v-loading="loading">
      <TaskCard
        v-for="item in data.tableData"
        :key="item.id"
        :task="item"
        @delete-task="handleDelete"
        @view-task="handleEdit"
      />
      <!-- 新建任务卡片 -->
      <div class="add-task-card" @click="handleAdd">
        <el-icon class="add-icon"><Plus /></el-icon>
        <span class="add-text">新建任务</span>
      </div>
    </div>

    <!-- 空状态 -->
    <el-empty v-if="!loading && data.tableData.length === 0" description="暂无任务，点击新建任务开始" />

    <!-- 分页 -->
    <div class="pagination" v-if="data.total > 0">
      <el-pagination
        background
        layout="prev, pager, next, total"
        v-model:current-page="data.pageNum"
        v-model:page-size="data.pageSize"
        :total="data.total"
        :page-sizes="[5, 10, 20, 50]"
        @current-change="changePage"
        @size-change="handleSizeChange"
      />
    </div>

    <!-- 新建/编辑任务抽屉（右侧打开） -->
    <el-drawer
      v-model="data.formVisible"
      :title="data.form.id ? '编辑任务' : '新建任务'"
      direction="rtl"
      size="520px"
      :close-on-click-modal="false"
      destroy-on-close
      @close="resetForm"
    >
      <div class="drawer-hero" aria-hidden="true">
        <div class="drawer-hero-text">
          <div class="drawer-hero-title">任务配置</div>
          <div class="drawer-hero-sub">在这里填写任务信息并快速发布到班级</div>
        </div>
        <div class="drawer-hero-art"></div>
      </div>

      <el-form class="drawer-form" :model="data.form" :rules="rules" ref="formRef" label-width="90px">
        <div class="drawer-cover">
          <div class="drawer-cover-label">封面</div>
          <div class="drawer-cover-body">
            <div class="cover-preview" v-if="data.form.cover">
              <div class="cover-preview-frame">
                <img class="cover-img" :src="getCoverPreviewUrl(data.form.cover)" alt="任务封面预览" />
                <div class="cover-preview-shine" aria-hidden="true"></div>
              </div>
              <div class="cover-meta">
                <span class="cover-meta-hint">发布后将以 16:9 比例展示在任务卡片顶部</span>
                <el-button type="danger" plain size="small" @click="removeCover">移除封面</el-button>
              </div>
            </div>
            <el-upload
              v-else
              class="cover-uploader"
              :action="uploadUrl"
              name="file"
              :show-file-list="false"
              accept="image/*"
              :on-success="handleCoverSuccess"
              :on-error="handleUploadError"
            >
              <div class="cover-uploader-inner">
                <div class="cover-uploader-title">上传封面图</div>
                <div class="cover-uploader-sub">建议 16:9，≤ 4MB</div>
                <el-button type="primary" plain size="small">选择图片</el-button>
              </div>
            </el-upload>
          </div>
        </div>

        <el-form-item label="任务名称" prop="name">
          <el-input
            v-model="data.form.name"
            placeholder="请输入任务名称"
            maxlength="50"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="任务类型" prop="lab">
          <el-select v-model="data.form.lab" placeholder="请选择任务类型" style="width: 100%">
            <el-option label="课后任务" :value="1" />
            <el-option label="实验任务" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="发放班级" prop="classId">
          <el-select v-model="data.form.classId" placeholder="请选择发放班级" style="width: 100%">
            <el-option
              v-for="item in data.classData"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="任务内容" prop="content">
          <el-input
            v-model="data.form.content"
            type="textarea"
            :rows="6"
            placeholder="请输入任务内容描述"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="drawer-footer">
          <el-button @click="data.formVisible = false">取消</el-button>
          <el-button type="primary" :loading="saving" @click="save">确定</el-button>
        </div>
      </template>
    </el-drawer>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus } from '@element-plus/icons-vue'
import TaskCard from '@/components/taskCard.vue'
import request from '@/utils/request'
import { getUploadUrl, resolveAvatarUrl } from '@/utils/appConfig'

const route = useRoute()
const formRef = ref(null)
const loading = ref(false)
const saving = ref(false)
const searchQuery = ref('')
const uploadUrl = computed(() => getUploadUrl())

const courseId = computed(() => {
  const id = route.query.id || route.params.id
  return id != null ? Number(id) : null
})

const data = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0,
  formVisible: false,
  form: {},
  tableData: [],
  classData: [],
  user: JSON.parse(localStorage.getItem('system-user') || '{}'),
})

const rules = {
  name: [{ required: true, message: '请输入任务名称', trigger: 'blur' }],
  lab: [{ required: true, message: '请选择任务类型', trigger: 'change' }],
  content: [{ required: true, message: '请输入任务内容', trigger: 'blur' }],
  classId: [{ required: true, message: '请选择发放班级', trigger: 'change' }],
}

const loadClass = async () => {
  try {
    const res = await request.get('/clazz/selectAll')
    if (res.code === '200') {
      data.classData = res.data || []
    }
  } catch (e) {
    console.error('加载班级失败:', e)
  }
}

const load = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: data.pageNum,
      pageSize: data.pageSize,
    }
    if (searchQuery.value) {
      params.name = searchQuery.value
    }
    if (data.user.role === 'TEACHER') {
      params.teacherId = data.user.id
    }
    // 关键：按当前课程过滤，避免跨课程查看到其它课程的任务
    if (courseId.value != null) {
      params.courseId = courseId.value
    }
    const res = await request.get('/task/selectPage', { params })
    if (res.code === '200') {
      data.tableData = res.data?.list || []
      data.total = res.data?.total || 0
    } else {
      ElMessage.error(res.msg || '加载失败')
    }
  } catch (e) {
    console.error('加载任务失败:', e)
    ElMessage.error('加载失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  data.pageNum = 1
  load()
}

const resetSearch = () => {
  searchQuery.value = ''
  data.pageNum = 1
  load()
}

const handleAdd = () => {
  data.form = {}
  data.form.courseId = courseId.value
  data.formVisible = true
}

const handleEdit = (row) => {
  data.form = JSON.parse(JSON.stringify(row))
  data.formVisible = true
}

const handleCoverSuccess = (res) => {
  let body = res
  if (typeof body === 'string') {
    try {
      body = JSON.parse(body)
    } catch {
      body = null
    }
  }
  if (body?.code === '200' && body?.data != null && body.data !== '') {
    data.form.cover = body.data
    return
  }
  ElMessage.error(body?.msg || '封面上传失败')
}

const handleUploadError = () => {
  ElMessage.error('上传失败（多为接口 404）：请确认后端已启动，且 vue/public/config.json 中 serverUrl 与后端一致')
}

const removeCover = () => {
  data.form.cover = ''
}

const getCoverPreviewUrl = (url) => resolveAvatarUrl(url || '')

const resetForm = () => {
  formRef.value?.resetFields()
  data.form = {}
}

const save = async () => {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
    saving.value = true
    const payload = {
      name: data.form.name,
      content: data.form.content,
      cover: data.form.cover || null,
      classId: data.form.classId,
      courseId: courseId.value,
      lab: data.form.lab,
      teacherId: data.user.id,
    }
    if (data.form.id != null && data.form.id !== '') {
      payload.id = data.form.id
    }
    const res = payload.id != null
      ? await request.put('/task/update', payload)
      : await request.post('/task/add', payload)
    if (res.code === '200') {
      ElMessage.success('操作成功')
      data.formVisible = false
      load()
    } else {
      ElMessage.error(res.msg || '操作失败')
    }
  } catch (e) {
    if (e !== false) {
      console.error('保存失败:', e)
      const msg = e?.response?.data?.msg || e?.message
      ElMessage.error(msg || '操作失败')
    }
  } finally {
    saving.value = false
  }
}

const handleDelete = (id) => {
  ElMessageBox.confirm('删除后数据无法恢复，您确定删除吗？', '删除确认', {
    type: 'warning',
  })
    .then(() => {
      request.delete(`/task/delete/${id}`).then((res) => {
        if (res.code === '200') {
          ElMessage.success('删除成功')
          load()
        } else {
          ElMessage.error(res.msg || '删除失败')
        }
      })
    })
    .catch(() => {})
}

const changePage = (page) => {
  data.pageNum = page
  load()
}

const handleSizeChange = (size) => {
  data.pageSize = size
  data.pageNum = 1
  load()
}

onMounted(() => {
  loadClass()
  load()
})
</script>

<style scoped>
  .task-container {
  padding: 24px 32px;
  min-height: calc(100vh - 60px);
  box-sizing: border-box;
  background:
    radial-gradient(1200px 480px at 12% -10%, rgba(64, 158, 255, 0.14), transparent 55%),
    radial-gradient(900px 400px at 92% 0%, rgba(34, 211, 238, 0.1), transparent 50%),
    linear-gradient(180deg, #eef2f9 0%, #f4f6f9 38%, #f6f7fb 100%);
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20px;
  padding: 18px 24px;
  background: rgba(255, 255, 255, 0.94);
  border-radius: 16px;
  border: 1px solid rgba(255, 255, 255, 0.7);
  box-shadow:
    0 1px 2px rgba(15, 23, 42, 0.04),
    0 16px 40px rgba(20, 63, 140, 0.07);
  backdrop-filter: blur(8px);
}

.header-left {
  display: flex;
  flex-direction: column;
}

.page-title {
  margin: 0 0 4px;
  font-size: 22px;
  font-weight: 700;
  color: #0f172a;
  letter-spacing: -0.02em;
}

.page-subtitle {
  margin: 0;
  font-size: 13px;
  color: #909399;
}

.header :deep(.el-button--primary) {
  box-shadow: 0 4px 14px rgba(64, 158, 255, 0.35);
  font-weight: 600;
}

.filter-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  margin-bottom: 20px;
  padding: 14px 20px;
  background: rgba(255, 255, 255, 0.92);
  border-radius: 16px;
  border: 1px solid rgba(255, 255, 255, 0.65);
  box-shadow: 0 8px 28px rgba(15, 23, 42, 0.06);
  backdrop-filter: blur(6px);
}

.filter-left {
  flex: 1;
}

.filter-actions {
  display: flex;
  gap: 10px;
  flex-shrink: 0;
}

.search-input {
  width: 100%;
}

.content {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 16px;
  margin-bottom: 20px;
}

.add-task-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 300px;
  height: 100%;
  align-self: stretch;
  border: 2px dashed rgba(64, 158, 255, 0.35);
  border-radius: 16px;
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.55) 0%, rgba(240, 247, 255, 0.92) 100%);
  cursor: pointer;
  transition: all 0.28s ease;
  position: relative;
  overflow: hidden;
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.8);
}

.add-task-card::before {
  content: '';
  position: absolute;
  inset: 0;
  background:
    radial-gradient(500px 200px at 50% 0%, rgba(64, 158, 255, 0.16), transparent 60%),
    linear-gradient(135deg, rgba(64, 158, 255, 0.06), rgba(34, 211, 238, 0.05));
  opacity: 0.85;
  transition: opacity 0.28s ease;
}

.add-task-card:hover {
  border-color: rgba(64, 158, 255, 0.85);
  background: linear-gradient(180deg, #fff 0%, #f0f7ff 100%);
  transform: translateY(-4px);
  box-shadow:
    0 12px 32px rgba(64, 158, 255, 0.18),
    0 4px 12px rgba(15, 23, 42, 0.06);
}

.add-task-card:hover::before {
  opacity: 1;
}

.add-icon {
  z-index: 1;
  font-size: 40px;
  color: #409eff;
  margin-bottom: 12px;
  width: 72px;
  height: 72px;
  border-radius: 50%;
  display: grid;
  place-items: center;
  background: rgba(255, 255, 255, 0.9);
  border: 1px solid rgba(64, 158, 255, 0.25);
  box-shadow: 0 10px 24px rgba(64, 158, 255, 0.2);
  transition: transform 0.28s ease, box-shadow 0.28s ease;
}

.add-task-card:hover .add-icon {
  transform: scale(1.06) rotate(90deg);
  box-shadow: 0 14px 32px rgba(64, 158, 255, 0.28);
}

.add-text {
  z-index: 1;
  color: #475569;
  font-size: 15px;
  font-weight: 700;
  letter-spacing: 0.04em;
}

.add-task-card:hover .add-text {
  color: #1450aa;
}

.pagination {
  display: flex;
  justify-content: center;
  padding: 18px 20px;
  background: rgba(255, 255, 255, 0.94);
  border-radius: 16px;
  border: 1px solid rgba(255, 255, 255, 0.65);
  box-shadow: 0 8px 28px rgba(15, 23, 42, 0.06);
}

/* -----------------------------
   Drawer（右侧抽屉）美化：上方插画 + 更紧凑的表单
------------------------------ */
.drawer-hero {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
  padding: 14px 14px 12px;
  border-radius: 12px;
  border: 1px solid rgba(220, 223, 230, 0.7);
  background:
    radial-gradient(900px 220px at 0% 0%, rgba(64, 158, 255, 0.16), transparent 55%),
    radial-gradient(700px 200px at 100% 0%, rgba(34, 211, 238, 0.12), transparent 60%),
    linear-gradient(180deg, rgba(255, 255, 255, 0.95), rgba(255, 255, 255, 0.86));
  margin-bottom: 14px;
}

.drawer-hero-title {
  font-size: 16px;
  font-weight: 700;
  color: #101828;
}

.drawer-hero-sub {
  margin-top: 4px;
  font-size: 12px;
  color: #667085;
}

.drawer-hero-art {
  width: 88px;
  height: 64px;
  border-radius: 12px;
  background:
    linear-gradient(135deg, rgba(47, 107, 255, 0.92), rgba(0, 198, 255, 0.86));
  position: relative;
  overflow: hidden;
  box-shadow: 0 10px 22px rgba(64, 158, 255, 0.18);
}

.drawer-hero-art::before {
  content: '';
  position: absolute;
  inset: -40px;
  background:
    radial-gradient(circle at 35% 35%, rgba(255, 255, 255, 0.35), transparent 55%),
    linear-gradient(to right, rgba(255, 255, 255, 0.18) 1px, transparent 1px),
    linear-gradient(to bottom, rgba(255, 255, 255, 0.14) 1px, transparent 1px);
  background-size: auto, 14px 14px, 14px 14px;
  transform: rotate(8deg);
}

.drawer-form {
  padding: 4px 4px 0;
}

.drawer-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.drawer-cover {
  margin-bottom: 12px;
  padding: 14px;
  border-radius: 16px;
  border: 1px solid rgba(20, 80, 170, 0.1);
  background: linear-gradient(180deg, #ffffff 0%, #f8fafc 100%);
  box-shadow: 0 8px 26px rgba(15, 23, 42, 0.05);
}

.drawer-cover-label {
  font-size: 13px;
  font-weight: 700;
  color: #101828;
  margin-bottom: 8px;
}

.drawer-cover-body {
  display: flex;
  align-items: stretch;
}

.cover-preview {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.cover-preview-frame {
  position: relative;
  width: 100%;
  aspect-ratio: 16 / 9;
  border-radius: 14px;
  overflow: hidden;
  background: linear-gradient(145deg, #e8f1ff, #f0f4ff);
  box-shadow:
    0 1px 0 rgba(255, 255, 255, 0.9) inset,
    0 12px 36px rgba(20, 63, 140, 0.12);
  border: 1px solid rgba(64, 158, 255, 0.18);
}

.cover-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
  transition: transform 0.35s ease;
}

.cover-preview-frame:hover .cover-img {
  transform: scale(1.03);
}

.cover-preview-shine {
  position: absolute;
  inset: 0;
  pointer-events: none;
  background: linear-gradient(
    125deg,
    rgba(255, 255, 255, 0.45) 0%,
    transparent 42%,
    transparent 58%,
    rgba(255, 255, 255, 0.08) 100%
  );
  opacity: 0.9;
}

.cover-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
}

.cover-meta-hint {
  font-size: 12px;
  color: #64748b;
  line-height: 1.5;
  flex: 1;
  min-width: 160px;
}

.cover-uploader {
  width: 100%;
}

.cover-uploader-inner {
  width: 100%;
  aspect-ratio: 16 / 9;
  min-height: 140px;
  border-radius: 14px;
  border: 2px dashed rgba(64, 158, 255, 0.45);
  background:
    radial-gradient(700px 220px at 20% 0%, rgba(64, 158, 255, 0.14), transparent 55%),
    linear-gradient(180deg, rgba(255, 255, 255, 0.95), rgba(241, 245, 249, 0.85));
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10px;
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease, border-color 0.2s ease;
  box-shadow: 0 6px 20px rgba(20, 63, 140, 0.06);
}

.cover-uploader-inner:hover {
  transform: translateY(-2px);
  box-shadow: 0 16px 36px rgba(64, 158, 255, 0.16);
  border-color: rgba(64, 158, 255, 0.9);
}

.cover-uploader-title {
  font-size: 14px;
  font-weight: 800;
  color: #175cd3;
}

.cover-uploader-sub {
  font-size: 12px;
  color: #667085;
}

/* 响应式适配 */
@media (max-width: 992px) {
  .task-container {
    padding: 16px;
  }

  .header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
}

@media (max-width: 768px) {
  .filter-bar {
    flex-direction: column;
    align-items: flex-start;
  }

  .filter-actions {
    width: 100%;
    justify-content: flex-end;
  }

  .content {
    grid-template-columns: 1fr;
  }
}
</style>
