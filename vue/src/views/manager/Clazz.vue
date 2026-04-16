<template>
  <div class="container">
    <div class="card search-bar">
      <el-input v-model="data.name" class="search-bar__input" placeholder="请输入班级名称查询" clearable />
      <el-button type="primary" @click="load">查询</el-button>
      <el-button type="info" class="search-bar__reset" @click="reset">重置</el-button>
    </div>

    <div class="card table-view">
      <div class="table-toolbar" v-if="data.user.role === 'ADMIN'">
        <el-button type="primary" @click="handleAdd">新增</el-button>
      </div>
      <el-table :data="data.tableData" stripe class="scrollable-content">
        <el-table-column label="班级名称" prop="name" />
        <el-table-column label="开班时间" prop="createTime" />
        <el-table-column label="学生人数" prop="studentNum" />

        <el-table-column label="操作" align="center" width="180" v-if="data.user.role === 'ADMIN'">
          <template #default="scope">
            <el-button type="primary" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button type="danger" @click="handleDelete(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <div class="card pagination">
      <el-pagination
        background
        layout="prev, pager, next"
        :page-size="data.pageSize"
        :current-page="data.pageNum"
        :total="data.total"
        @current-change="changePage"
      />
    </div>

    <el-dialog title="班级名称" width="50%" v-model="data.formVisible" :close-on-click-modal="false" destroy-on-close>
      <el-form :model="data.form" label-width="120px" class="clazz-form">
        <el-form-item label="班级名称" prop="name">
          <el-input v-model="data.form.name" autocomplete="off" />
        </el-form-item>
        <el-form-item label="开班时间" prop="createTime">
          <el-date-picker v-model="data.form.createTime" type="date" placeholder="选择日期" style="width: 100%" />
        </el-form-item>
        <el-form-item label="学生人数" prop="studentNum">
          <el-input v-model="data.form.studentNum" autocomplete="off" />
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
import { reactive } from 'vue'
import { ElMessageBox, ElMessage } from 'element-plus'

const data = reactive({
  pageNum: 1,
  pageSize: 5,
  total: 0,
  formVisible: false,
  form: {},
  tableData: [],
  name: null,
  user: JSON.parse(localStorage.getItem('system-user') || '{}'),
})

// 分页查询
const load = () => {
  request
    .get('/clazz/selectPage', {
      params: {
        pageNum: data.pageNum,
        pageSize: data.pageSize,
        name: data.name,
      },
    })
    .then((res) => {
      data.tableData = res.data?.list
      data.total = res.data?.total
    })
}

// 新增
const handleAdd = () => {
  data.form = {}
  data.formVisible = true
}

// 编辑
const handleEdit = (row) => {
  data.form = JSON.parse(JSON.stringify(row))
  data.formVisible = true
}

// 新增保存
const add = () => {
  request.post('/clazz/add', data.form).then((res) => {
    if (res.code === '200') {
      load()
      ElMessage.success('操作成功')
      data.formVisible = false
    } else {
      ElMessage.error(res.msg)
    }
  })
}

// 编辑保存
const update = () => {
  request.put('/clazz/update', data.form).then((res) => {
    if (res.code === '200') {
      load()
      ElMessage.success('操作成功')
      data.formVisible = false
    } else {
      ElMessage.error(res.msg)
    }
  })
}

// 弹窗保存
const save = () => {
  if (data.form.createTime) {
    const date = new Date(data.form.createTime)
    const year = date.getFullYear()
    const month = String(date.getMonth() + 1).padStart(2, '0')
    const day = String(date.getDate()).padStart(2, '0')
    data.form.createTime = `${year}-${month}-${day}`
  }

  data.form.id ? update() : add()
}

// 删除
const handleDelete = (id) => {
  ElMessageBox.confirm('删除后数据无法恢复，您确定删除吗?', '删除确认', { type: 'warning' })
    .then(() => {
      request.delete('/clazz/delete/' + id).then((res) => {
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

// 重置
const reset = () => {
  data.name = null
  load()
}

const changePage = (pageNum) => {
  data.pageNum = pageNum
  load()
}

load()
</script>

<style scoped>
.container {
  height: 100vh;
  display: flex;
  flex: 1;
  flex-direction: column;
  min-height: 0;
}

.card {
  margin-bottom: 5px;
}

.search-bar {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 10px;
}

.search-bar__input {
  width: 300px;
  max-width: 100%;
}

.search-bar__reset {
  margin-left: 0;
}

.table-toolbar {
  margin-bottom: 10px;
}

.scrollable-content {
  max-height: 60vh;
  overflow-y: auto;
}

.pagination {
  display: flex;
  justify-content: center;
}

.clazz-form {
  padding-right: 50px;
}
</style>
