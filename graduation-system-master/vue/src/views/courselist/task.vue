<template>
  <div class="task-page">
    <header class="task-hero card">
      <div class="task-hero__accent" aria-hidden="true" />
      <div class="task-hero__icon" aria-hidden="true">
        <el-icon><List /></el-icon>
      </div>
      <div class="task-hero__text">
        <h1 class="task-hero__title">发布任务</h1>
        <p class="task-hero__sub">
          {{ courseSubtitle }}
        </p>
      </div>
      <el-button v-if="isTeacher" type="primary" class="task-hero__cta" @click="handleAdd">
        <el-icon><Plus /></el-icon>
        新建任务
      </el-button>
    </header>

    <section class="task-toolbar card" aria-label="搜索">
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
      <div class="toolbar-actions">
        <el-button type="primary" @click="handleSearch">搜索</el-button>
        <el-button @click="resetSearch">重置</el-button>
      </div>
    </section>

    <el-alert
      v-if="!courseId && isTeacher"
      type="warning"
      show-icon
      :closable="false"
      class="task-alert"
      title="当前未携带课程信息，发布任务时将无法写入课程维度；请从「课程列表」进入本课程后再使用发布任务。"
    />

    <section class="task-grid-wrap" v-loading="loading" aria-label="任务列表">
      <div
        class="task-grid"
        :class="{ 'task-grid--empty': !loading && tableList.length === 0 }"
      >
        <template v-if="tableList.length > 0">
          <TaskCard
            v-for="item in tableList"
            :key="item.id"
            :task="item"
            :can-manage="isTeacher"
            @delete-task="handleDelete"
            @view-task="handleViewTask"
          />
        </template>
        <div v-else-if="!loading" class="task-empty">
          <el-empty :description="emptyDescription" />
        </div>
      </div>
    </section>

    <div class="task-pagination card" v-if="data.total > 0">
      <el-pagination
        background
        layout="sizes, prev, pager, next, total"
        :current-page="data.pageNum"
        :page-size="data.pageSize"
        :total="data.total"
        :page-sizes="[5, 10, 20, 50]"
        @current-change="changePage"
        @size-change="handleSizeChange"
      />
    </div>

    <el-drawer
      v-model="data.formVisible"
      :title="data.form.id ? '编辑任务' : '新建任务'"
      direction="rtl"
      size="520px"
      :close-on-click-modal="false"
      destroy-on-close
      class="task-drawer"
      @closed="resetForm"
    >
      <div class="drawer-hero" aria-hidden="true">
        <div class="drawer-hero-text">
          <div class="drawer-hero-title">任务配置</div>
          <div class="drawer-hero-sub">填写信息后保存，系统将按班级为学生生成对应作业记录</div>
        </div>
      </div>

      <el-form class="drawer-form" :model="data.form" :rules="rules" ref="formRef" label-width="90px">
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
          <el-select v-model="data.form.classId" placeholder="请选择发放班级" filterable style="width: 100%">
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
import { reactive, ref, onMounted, computed, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus, List } from '@element-plus/icons-vue'
import TaskCard from '@/components/taskCard.vue'
import request from '@/utils/request'

const route = useRoute()
const formRef = ref(null)
const loading = ref(false)
const saving = ref(false)
const searchQuery = ref('')

const courseId = computed(() => {
  const id = route.query.id || route.params.id
  return id != null && id !== '' ? Number(id) : null
})

const courseName = computed(() => (route.query.courseName ? String(route.query.courseName) : ''))

const courseSubtitle = computed(() => {
  if (courseName.value) {
    return `当前课程：${courseName.value} · 为班级布置课后与实验任务，支持搜索与分页`
  }
  return '为班级布置课后与实验任务，支持搜索与分页'
})

const isTeacher = computed(() => data.user.role === 'TEACHER')

const emptyDescription = computed(() =>
  isTeacher.value ? '暂无任务，点击页面上方「新建任务」开始发布' : '暂无任务'
)

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

const tableList = computed(() => data.tableData || [])

const rules = {
  name: [{ required: true, message: '请输入任务名称', trigger: 'blur' }],
  lab: [{ required: true, message: '请选择任务类型', trigger: 'change' }],
  content: [{ required: true, message: '请输入任务内容', trigger: 'blur' }],
  classId: [{ required: true, message: '请选择发放班级', trigger: 'change' }],
}

function normalizePagePayload(raw) {
  if (!raw || typeof raw !== 'object') return { list: [], total: 0 }
  const list = raw.list ?? raw.records ?? raw.rows ?? []
  const total = Number(raw.total ?? raw.totalCount ?? list.length) || 0
  return { list: Array.isArray(list) ? list : [], total }
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
    if (searchQuery.value.trim()) {
      params.name = searchQuery.value.trim()
    }
    if (data.user.role === 'TEACHER') {
      params.teacherId = data.user.id
    }
    if (courseId.value != null && !Number.isNaN(courseId.value)) {
      params.courseId = courseId.value
    }
    const res = await request.get('/task/selectPage', { params })
    if (res.code === '200') {
      const { list, total } = normalizePagePayload(res.data)
      data.tableData = list
      data.total = total
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
  if (!isTeacher.value) return
  if (courseId.value == null || Number.isNaN(courseId.value)) {
    ElMessage.warning('请先进入具体课程后再发布任务（需带上课程信息）')
    return
  }
  data.form = {
    courseId: courseId.value,
  }
  data.formVisible = true
}

const handleEdit = (row) => {
  data.form = JSON.parse(JSON.stringify(row))
  data.formVisible = true
}

const handleViewTask = (row) => {
  if (isTeacher.value) {
    handleEdit(row)
    return
  }
  ElMessageBox.alert(row.content || '暂无内容描述', row.name || '任务详情', {
    confirmButtonText: '关闭',
    dangerouslyUseHTMLString: false,
  }).catch(() => {})
}

const resetForm = () => {
  data.form = {}
}

const save = async () => {
  if (!formRef.value) return
  if (courseId.value == null || Number.isNaN(courseId.value)) {
    ElMessage.warning('缺少课程信息，无法保存')
    return
  }
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
    const res =
      payload.id != null
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
  if (!isTeacher.value) return
  ElMessageBox.confirm('删除后数据无法恢复，您确定删除吗？', '删除确认', {
    type: 'warning',
  })
    .then(async () => {
      try {
        const res = await request.delete(`/task/delete/${id}`)
        if (res.code === '200') {
          ElMessage.success('删除成功')
          load()
        } else {
          ElMessage.error(res.msg || '删除失败')
        }
      } catch (e) {
        console.error(e)
        ElMessage.error(e?.response?.data?.msg || e?.message || '删除失败')
      }
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

watch(
  () => [route.query.id, route.params.id],
  () => {
    data.pageNum = 1
    load()
  }
)

onMounted(() => {
  loadClass()
  load()
})
</script>

<style scoped lang="scss">
.task-page {
  padding: 16px 20px 28px;
  min-height: 100%;
  box-sizing: border-box;
  font-family: var(--font-sans);
  color: var(--color-text);
  background: transparent;
}

.task-hero {
  position: relative;
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 14px 20px;
  margin-bottom: 16px;
  overflow: hidden;
  border: 1px solid var(--color-border);
  box-shadow: var(--shadow-soft);
}

.task-hero__accent {
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

.task-hero__icon {
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

.task-hero__text {
  flex: 1;
  min-width: 200px;
}

.task-hero__title {
  margin: 0 0 6px;
  font-size: 1.35rem;
  font-weight: 700;
  letter-spacing: -0.02em;
  color: var(--color-text);
}

.task-hero__sub {
  margin: 0;
  font-size: 0.9rem;
  color: var(--color-text-muted);
  line-height: 1.55;
  max-width: 44rem;
}

.task-hero__cta {
  margin-left: auto;
  border-radius: 999px;
  font-weight: 600;
}

.task-toolbar {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
  border: 1px solid var(--color-border);
}

.search-input {
  flex: 1 1 220px;
  min-width: 180px;
  max-width: 360px;
}

.toolbar-actions {
  display: flex;
  gap: 8px;
  margin-left: auto;
}

.task-alert {
  margin-bottom: 16px;
  border-radius: var(--radius-md);
}

.task-grid-wrap {
  min-height: 200px;
  margin-bottom: 16px;
}

.task-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 16px;
  align-items: stretch;
}

.task-grid--empty {
  align-items: start;
  /* 空态下避免子项落到「单列窄轨」，由子元素自行占满宽度 */
  grid-template-columns: 1fr;
}

.task-empty {
  grid-column: 1 / -1;
  width: 100%;
  min-width: 0;
  padding: 28px 16px;
  background: var(--color-bg-elevated);
  border: 1px dashed var(--color-border-strong);
  border-radius: var(--radius-md);
}

.task-pagination {
  display: flex;
  justify-content: center;
  padding: 14px 16px;
  border: 1px solid var(--color-border);
}

.task-pagination :deep(.el-pagination) {
  flex-wrap: wrap;
  justify-content: center;
  row-gap: 8px;
}

.drawer-hero {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
  padding: 14px;
  border-radius: var(--radius-md);
  border: 1px solid var(--color-border);
  background: var(--color-bg-elevated);
  margin-bottom: 14px;
}

.drawer-hero-title {
  font-size: 16px;
  font-weight: 700;
  color: var(--color-text);
}

.drawer-hero-sub {
  margin-top: 4px;
  font-size: 12px;
  color: var(--color-text-muted);
  line-height: 1.45;
}

.drawer-form {
  padding: 4px 4px 0;
}

.drawer-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

@media (max-width: 768px) {
  .task-hero__cta {
    width: 100%;
  }

  .toolbar-actions {
    width: 100%;
    margin-left: 0;
    justify-content: flex-end;
  }

  .task-grid {
    grid-template-columns: 1fr;
  }
}
</style>
