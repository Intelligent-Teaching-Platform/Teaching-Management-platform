<template>
  <div style = "flex:1">

    <div class="card" style="margin-bottom: 5px;">
      <el-input v-model="data.name" style="width: 300px; margin-right: 10px" placeholder="请输入课程名称查询"></el-input>
      <el-button type="primary" @click="load">查询</el-button>
      <el-button type="info" style="margin: 0 10px" @click="reset">重置</el-button>
    </div>

    <div class="card" style="margin-bottom: 5px">
      <el-table :data="data.tableData" stripe>
        <el-table-column label="课程名称" prop="name"></el-table-column>
        <el-table-column label="授课教师" prop="teacherName"></el-table-column>
        <el-table-column label="学生姓名" prop="studentName"></el-table-column>
        <el-table-column label="学生学号" prop="studentId"></el-table-column>



        <el-table-column label="操作" align="center" width="220" v-if="data.user.role !== 'ADMIN'">
          <template #default="scope" v-if="data.user.role === 'STUDENT'">
            <el-button type="danger" @click="handleDelete(scope.row.id)">取消选课</el-button>
          </template>
          <template #default="scope" v-if="data.user.role === 'TEACHER'">
            <el-button-group>
              <el-button type="primary"  @click="handleAdd(scope.row)">发布任务</el-button>
              <el-button type="success"  @click="handleSignIn(scope.row)">发布签到</el-button>
            </el-button-group>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <div class="card">
      <el-pagination
          background
          layout="prev, pager, next"
          v-model:page-size="data.pageSize"
          v-model:current-page="data.pageNum"
          :total="data.total"
          @current-change="changePage"
      />
    </div>

    <el-dialog title="任务发布" width="40%" v-model="data.formVisible" :close-on-click-modal="false" destroy-on-close>
      <el-form :model="data.form" label-width="100px" style="padding-right: 50px">
        <el-form-item label="任务名称" prop="name">
          <el-input v-model="data.form.name" autocomplete="off" />
        </el-form-item>
        <el-form-item label="任务内容" prop="content">
          <el-input type="textarea" :rows="4" v-model="data.form.content" autocomplete="off" />
        </el-form-item>
        <el-form-item label="任务文件" prop="file">
          <el-upload
              class="upload-demo"
              :action="uploadUrl"
              :on-preview="handlePreview"
              :on-remove="handleRemove"
              :before-upload="beforeUpload"
              :on-success="handleSuccess"
              :file-list="data.form.file"
              :auto-upload="false"
          >
            <el-button slot="trigger" size="small" type="primary">选取文件</el-button>
          </el-upload>
        </el-form-item>

        <el-form-item label="任务学分" prop="score">
          <el-input v-model="data.form.score" autocomplete="off" />
        </el-form-item>


      </el-form>
      <template #footer>
      <span class="dialog-footer">
        <el-button @click="data.formVisible = false">取 消</el-button>
        <el-button type="primary" @click="add">保 存</el-button>
      </span>
      </template>
    </el-dialog>

    <el-dialog title="发布签到" width="40%" v-model="data.signInVisible" :close-on-click-modal="false" destroy-on-close>
      <el-form :model="data.signInForm" label-width="100px" style="padding-right: 50px">
        <el-form-item label="签到时间" prop="duration">
          <el-input v-model="data.signInForm.duration" autocomplete="off" placeholder="请输入签到有效时间（分钟）" />
        </el-form-item>
      </el-form>
      <template #footer>
    <span class="dialog-footer">
      <el-button @click="data.signInVisible = false">取 消</el-button>
      <el-button type="primary" @click="addSignIn">发 布</el-button>
    </span>
      </template>
    </el-dialog>

  </div>
</template>



<script setup>
import request from "@/utils/request";
import {reactive} from "vue";
import {ElMessageBox, ElMessage} from "element-plus";
//文件上传接口

import { getUploadUrl } from '@/utils/appConfig'
const uploadUrl = getUploadUrl()
const beforeUpload = () => {
  return true
}
const handleSuccess = (res) => {
  data.form.file = res.data  // res.data就是文件上传返回的文件路径，获取到路径后赋值表单的属性
}


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
  let teacherId = null
  let studentId = null
  if(data.user.role === 'TEACHER'){
    teacherId = data.user.id
  }
  if(data.user.role === 'STUDENT'){
    studentId = data.user.id
  }
  request.get('/choice/selectPage', {
    params: {
      pageNum: data.pageNum,
      pageSize: data.pageSize,
      name: data.name,
      teacherId: teacherId,
      studentId: studentId,
    }
  }).then(res => {
    data.tableData = res.data?.list
    data.total = res.data?.total
  })
}


const handleAdd = (row) => {
  data.form = {}
  data.formVisible = true
  data.form.studentId = row.studentId

}

// 编辑
const handleEdit = (row) => {
  data.form = JSON.parse(JSON.stringify(row))
  data.formVisible = true
}

// 删除
const handleDelete = (id) => {
  ElMessageBox.confirm('选课容量有限，请确认是否取消', '确认取消', { type: 'warning' }).then(res => {
    request.delete('/choice/delete/' + id).then(res => {
      if (res.code === '200') {
        load()
        ElMessage.success('操作成功')
      } else {
        ElMessage.error(res.msg)
      }
    })
  }).catch(err => {})
}

// 重置
const reset = () => {
  data.name = null
  load()
}
/**
 * 分页
 */
const changePage = (pageNum) => {
  data.pageNum =pageNum
  load()
}
const add = () => {
  data.form.teacherId = data.user.id
  request.post('/work/add', data.form).then(res => {
    if (res.code === '200') {
      ElMessage.success('任务发布成功')
      data.formVisible = false
      load()
    } else {
      ElMessage.error(res.msg)
    }
  })
}

const handleSignIn = (row) => {
  data.signInForm = {}; // 初始化表单
  data.signInVisible = true; // 打开弹窗
  data.signInForm.courseId = row.courseId; // 设置课程 ID
};

const addSignIn = () => {
  if (!data.signInForm.duration) {
    ElMessage.error('请输入签到时间');
    return;
  }

  // 调用后端接口发布签到
  request.post('/signIn/add', data.signInForm).then(res => {
    if (res.code === '200') {
      ElMessage.success('签到发布成功');
      data.signInVisible = false;
    } else {
      ElMessage.error(res.msg);
    }
  });
};

load()
</script>