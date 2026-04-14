<template>
  <div class="notice-admin-page">
    <section class="notice-toolbar card">
      <div class="toolbar-head">
        <div class="toolbar-titles">
          <h1 class="page-title">公告管理</h1>
          <p class="page-sub">以卡片浏览全部公告，支持按标题检索与维护内容</p>
        </div>
        <el-button type="primary" @click="handleAdd">
          <el-icon class="btn-icon"><Plus /></el-icon>
          新增公告
        </el-button>
      </div>
      <div class="toolbar-search">
        <el-input
          v-model="data.title"
          class="search-input"
          placeholder="请输入公告标题查询"
          clearable
          @keyup.enter="load"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-button type="primary" @click="load">查询</el-button>
        <el-button @click="reset">重置</el-button>
      </div>
    </section>

    <section v-loading="loading" class="notice-body card">
      <div v-if="!loading && !(data.tableData?.length)" class="empty-wrap">
        <el-empty description="暂无公告数据" />
      </div>

      <div
        v-else
        class="notice-grid"
        role="list"
        aria-label="公告列表"
      >
        <article
          v-for="(row, index) in data.tableData"
          :key="row.id"
          class="notice-item card"
          :style="{ '--i': index }"
          role="listitem"
        >
          <div class="notice-item__accent" aria-hidden="true" />
          <div class="notice-item__main">
            <h2 class="notice-item__title">{{ row.title || '未命名公告' }}</h2>
            <time v-if="row.time" class="notice-item__time" :datetime="row.time">
              <el-icon class="time-icon"><Calendar /></el-icon>
              {{ row.time }}
            </time>
            <p class="notice-item__excerpt">{{ excerpt(row.content) }}</p>
          </div>
          <div class="notice-item__actions">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" link @click="handleDelete(row.id)">删除</el-button>
          </div>
        </article>
      </div>
    </section>

    <section class="notice-footer card">
      <el-pagination
        background
        layout="total, prev, pager, next, sizes"
        :page-sizes="[8, 12, 24]"
        v-model:page-size="data.pageSize"
        v-model:current-page="data.pageNum"
        :total="data.total"
        @current-change="load"
        @size-change="onSizeChange"
      />
    </section>

    <el-dialog
      title="公告信息"
      width="min(480px, 92vw)"
      v-model="data.formVisible"
      :close-on-click-modal="false"
      destroy-on-close
    >
      <el-form :model="data.form" label-width="100px" class="notice-form">
        <el-form-item label="公告标题" prop="title">
          <el-input v-model="data.form.title" autocomplete="off" />
        </el-form-item>
        <el-form-item label="公告内容" prop="content">
          <el-input type="textarea" :rows="6" v-model="data.form.content" autocomplete="off" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="data.formVisible = false">取 消</el-button>
          <el-button type="primary" @click="save">保 存</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import request from '@/utils/request'
import { reactive, ref } from 'vue'
import { ElMessageBox, ElMessage } from 'element-plus'
import { Plus, Search, Calendar } from '@element-plus/icons-vue'

const loading = ref(false)

const data = reactive({
  pageNum: 1,
  pageSize: 12,
  total: 0,
  formVisible: false,
  form: {},
  tableData: [],
  title: null,
})

function excerpt(text) {
  if (text == null || String(text).trim() === '') return '（暂无正文摘要）'
  const s = String(text).trim().replace(/\s+/g, ' ')
  if (s.length <= 200) return s
  return s.slice(0, 200) + '…'
}

const load = () => {
  loading.value = true
  request
    .get('/notice/selectPage', {
      params: {
        pageNum: data.pageNum,
        pageSize: data.pageSize,
        title: data.title,
      },
    })
    .then((res) => {
      data.tableData = res.data?.list || []
      data.total = res.data?.total ?? 0
    })
    .finally(() => {
      loading.value = false
    })
}

function onSizeChange() {
  data.pageNum = 1
  load()
}

const handleAdd = () => {
  data.form = {}
  data.formVisible = true
}

const handleEdit = (row) => {
  data.form = JSON.parse(JSON.stringify(row))
  data.formVisible = true
}

const add = () => {
  request.post('/notice/add', data.form).then((res) => {
    if (res.code === '200') {
      load()
      ElMessage.success('操作成功')
      data.formVisible = false
    } else {
      ElMessage.error(res.msg)
    }
  })
}

const update = () => {
  request.put('/notice/update', data.form).then((res) => {
    if (res.code === '200') {
      load()
      ElMessage.success('操作成功')
      data.formVisible = false
    } else {
      ElMessage.error(res.msg)
    }
  })
}

const save = () => {
  data.form.id ? update() : add()
}

const handleDelete = (id) => {
  ElMessageBox.confirm('删除后数据无法恢复，您确定删除吗?', '删除确认', { type: 'warning' })
    .then(() => {
      request.delete('/notice/delete/' + id).then((res) => {
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
  data.title = null
  data.pageNum = 1
  load()
}

load()
</script>

<style scoped>
.notice-admin-page {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 16px;
  font-family: var(--font-sans);
}

.notice-toolbar {
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

.notice-body {
  padding: 20px;
  min-height: 200px;
}

.empty-wrap {
  padding: 40px 20px;
}

.notice-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 18px;
}

.notice-item {
  position: relative;
  display: flex;
  flex-direction: column;
  padding: 18px 16px 14px;
  overflow: hidden;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-soft);
  transition:
    transform var(--duration, 220ms) var(--ease-out, cubic-bezier(0.16, 1, 0.3, 1)),
    border-color var(--duration, 220ms) var(--ease-out, cubic-bezier(0.16, 1, 0.3, 1)),
    box-shadow var(--duration, 220ms) var(--ease-out, cubic-bezier(0.16, 1, 0.3, 1));
  animation: notice-in var(--duration, 220ms) var(--ease-out, cubic-bezier(0.16, 1, 0.3, 1)) backwards;
  animation-delay: calc(min(var(--i, 0), 14) * 40ms);
}

@media (prefers-reduced-motion: reduce) {
  .notice-item {
    animation: none;
  }
}

.notice-item:hover {
  transform: translateY(-2px);
  border-color: var(--color-primary-muted);
  box-shadow:
    0 16px 48px -20px rgba(15, 23, 42, 0.14),
    0 0 0 1px var(--color-primary-soft);
}

.notice-item__accent {
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 4px;
  background: linear-gradient(180deg, var(--color-primary) 0%, var(--color-primary-hover) 100%);
  border-radius: var(--radius-md) 0 0 var(--radius-md);
  opacity: 0.95;
}

.notice-item__main {
  padding-left: 10px;
  flex: 1;
  min-width: 0;
}

.notice-item__title {
  margin: 0;
  font-size: 1.05rem;
  font-weight: 700;
  line-height: 1.35;
  color: var(--color-text);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.notice-item__time {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  margin-top: 10px;
  font-size: 12px;
  font-weight: 600;
  color: var(--color-primary-hover);
  background: var(--color-primary-soft);
  padding: 4px 10px;
  border-radius: 999px;
  font-variant-numeric: tabular-nums;
}

.time-icon {
  font-size: 14px;
}

.notice-item__excerpt {
  margin: 12px 0 0;
  font-size: 13px;
  line-height: 1.6;
  color: var(--color-text-muted);
  display: -webkit-box;
  -webkit-line-clamp: 4;
  -webkit-box-orient: vertical;
  overflow: hidden;
  word-break: break-word;
}

.notice-item__actions {
  display: flex;
  justify-content: flex-end;
  gap: 4px;
  margin-top: 14px;
  padding-top: 12px;
  border-top: 1px solid var(--color-border);
}

.notice-footer {
  display: flex;
  justify-content: center;
  padding: 14px 20px;
}

.notice-form {
  padding-right: 12px;
}

@keyframes notice-in {
  from {
    opacity: 0;
    transform: translateY(8px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
