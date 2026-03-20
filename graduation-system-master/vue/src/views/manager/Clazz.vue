<template>
  <div class="container" style="flex:1">
    <div class="card" style="margin-bottom: 10px;">
      <el-input v-model="data.name" style="width: 400px; margin-right: 20px" placeholder="请输入班级名称查询"></el-input>
      <el-button type="primary" @click="load" style="height: 40px; font-size: 16px;">查询</el-button>
      <el-button type="info" style="height: 40px; font-size: 16px; margin: 0 20px;" @click="reset">重置</el-button>
    </div>

    <div class="card" style="margin-bottom: 10px">
      <div style="margin-bottom: 15px" v-if="data.user.role === 'ADMIN'">
        <el-button type="primary" @click="handleAdd" style="height: 40px; font-size: 16px;">新增</el-button>
      </div>
      <el-table :data="data.tableData" stripe style="font-size: 16px;">
        <el-table-column label="班级名称" prop="name"></el-table-column>
        <el-table-column label="开班时间" prop="createTime"></el-table-column>

        <el-table-column label="操作" align="center" width="180" v-if="data.user.role === 'ADMIN'">
          <template #default="scope">
            <el-button type="primary" @click="handleEdit(scope.row)" style="height: 35px; font-size: 14px;">编辑</el-button>
            <el-button type="danger" @click="handleDelete(scope.row.id)" style="height: 35px; font-size: 14px;">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <div class="card">
      <el-pagination background layout="prev, pager, next" v-model:page-size="data.pageSize" v-model:current-page="data.pageNum" :total="data.total" @current-change="changePage" style="font-size: 16px;"></el-pagination>
    </div>

    <el-dialog title="班级名称" width="50%" v-model="data.formVisible" :close-on-click-modal="false" destroy-on-close>
      <el-form :model="data.form" label-width="120px" style="padding-right: 80px; font-size: 16px;">
        <el-form-item label="班级名称" prop="name">
          <el-input v-model="data.form.name" autocomplete="off" style="height: 40px; font-size: 16px;"></el-input>
        </el-form-item>
        <el-form-item label="开班时间" prop="createTime">
          <el-date-picker v-model="data.form.createTime" type="date" placeholder="选择日期" style="width: 100%; height: 40px; font-size: 16px;"></el-date-picker>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="data.formVisible = false" style="height: 40px; font-size: 16px;">取 消</el-button>
          <el-button type="primary" @click="save" style="height: 40px; font-size: 16px;">保 存</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import request from "@/utils/request";
import {reactive} from "vue";
import {ElMessageBox, ElMessage} from "element-plus";

const data = reactive({
  pageNum: 1,
  pageSize: 5,
  total: 0,
  formVisible: false,
  form: {},
  tableData: [],
  name: null,
  user: JSON.parse(localStorage.getItem('system-user') || '{}')
});

// 分页查询
const load = () => {
  request.get('/clazz/selectPage', {
    params: {
      pageNum: data.pageNum,
      pageSize: data.pageSize,
      name: data.name
    }
  }).then(res => {
    data.tableData = res.data?.list;
    data.total = res.data?.total;
  });
};

// 新增
const handleAdd = () => {
  data.form = {};
  data.formVisible = true;
};

// 编辑
const handleEdit = (row) => {
  data.form = JSON.parse(JSON.stringify(row));
  data.formVisible = true;
};

// 新增保存
const add = () => {
  request.post('/clazz/add', data.form).then(res => {
    if (res.code === '200') {
      load();
      ElMessage.success('操作成功');
      data.formVisible = false;
    } else {
      ElMessage.error(res.msg);
    }
  });
};

// 编辑保存
const update = () => {
  request.put('/clazz/update', data.form).then(res => {
    if (res.code === '200') {
      load();
      ElMessage.success('操作成功');
      data.formVisible = false;
    } else {
      ElMessage.error(res.msg);
    }
  });
};

// 弹窗保存
const save = () => {
  // 对createTime进行格式处理，确保只保留年-月-日
  if (data.form.createTime) {
    const date = new Date(data.form.createTime);
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    data.form.createTime = `${year}-${month}-${day}`;
  }

  // data.form有id就是更新，没有就是新增
  data.form.id ? update() : add();
};

// 删除
const handleDelete = (id) => {
  ElMessageBox.confirm('删除后数据无法恢复，您确定删除吗?', '删除确认', { type: 'warning' }).then(res => {
    request.delete('/clazz/delete/' + id).then(res => {
      if (res.code === '200') {
        load();
        ElMessage.success('操作成功');
      } else {
        ElMessage.error(res.msg);
      }
    });
  }).catch(err => {});
};

// 重置
const reset = () => {
  data.name = null;
  load();
};

/**
 * 分页
 */
const changePage = (pageNum) => {
  data.pageNum = pageNum;
  load();
};

load();
</script>

<style scoped>
.container {
  padding: 20px;
}

.card {
  background-color: white;
  padding: 20px;
  box-shadow: 0 0 5px rgba(0, 0, 0, 0.1);
  border-radius: 5px;
}
</style>