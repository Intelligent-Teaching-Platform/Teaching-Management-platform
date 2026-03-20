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

    <!-- 新建/编辑任务弹窗 -->
    <el-dialog
      :title="data.form.id ? '编辑任务' : '新建任务'"
      v-model="data.formVisible"
      width="600px"
      :close-on-click-modal="false"
      @close="resetForm"
    >
      <el-form :model="data.form" :rules="rules" ref="formRef" label-width="100px">
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
        <el-form-item label="任务内容" prop="content">
          <el-input
            v-model="data.form.content"
            type="textarea"
            :rows="4"
            placeholder="请输入任务内容描述"
            maxlength="500"
            show-word-limit
          />
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
      </el-form>
      <template #footer>
        <el-button @click="data.formVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus } from '@element-plus/icons-vue'
import TaskCard from '@/components/taskCard.vue'
import request from '@/utils/request'

const route = useRoute()
const formRef = ref(null)
const loading = ref(false)
const saving = ref(false)
const searchQuery = ref('')

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

const resetForm = () => {
  formRef.value?.resetFields()
  data.form = {}
}

const save = async () => {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
    saving.value = true
    data.form.teacherId = data.user.id
    data.form.courseId = courseId.value
    const url = data.form.id ? '/task/update' : '/task/add'
    const method = data.form.id ? 'put' : 'post'
    const res = await request[method](url, data.form)
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
      ElMessage.error('操作失败')
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
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20px;
  padding: 18px 24px;
  background: #fff;
  border-radius: 12px;
  border: 1px solid rgba(220, 223, 230, 0.7);
}

.header-left {
  display: flex;
  flex-direction: column;
}

.page-title {
  margin: 0 0 4px;
  font-size: 22px;
  font-weight: 600;
  color: #303133;
  letter-spacing: 0.03em;
}

.page-subtitle {
  margin: 0;
  font-size: 13px;
  color: #909399;
}

.filter-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  margin-bottom: 20px;
  padding: 14px 20px;
  background: #fff;
  border-radius: 12px;
  border: 1px solid rgba(220, 223, 230, 0.7);
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
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 20px;
  margin-bottom: 20px;
}

.add-task-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 220px;
  border: 1px dashed #dcdfe6;
  border-radius: 12px;
  background: #fafbff;
  cursor: pointer;
  transition: all 0.25s ease;
  position: relative;
  overflow: hidden;
}

.add-task-card::before {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, rgba(64, 158, 255, 0.08), rgba(103, 194, 58, 0.06));
  opacity: 0;
  transition: opacity 0.25s ease;
}

.add-task-card:hover {
  border-color: #409eff;
  background: #f0f7ff;
  transform: translateY(-2px);
  box-shadow: 0 10px 24px rgba(64, 158, 255, 0.15);
}

.add-task-card:hover::before {
  opacity: 1;
}

.add-icon {
  z-index: 1;
  font-size: 46px;
  color: #b1b3b8;
  margin-bottom: 10px;
}

.add-task-card:hover .add-icon {
  color: #409eff;
}

.add-text {
  z-index: 1;
  color: #909399;
  font-size: 14px;
}

.add-task-card:hover .add-text {
  color: #409eff;
}

.pagination {
  display: flex;
  justify-content: center;
  padding: 18px 20px;
  background: #fff;
  border-radius: 12px;
  border: 1px solid rgba(220, 223, 230, 0.7);
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
