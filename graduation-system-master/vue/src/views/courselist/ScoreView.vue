<template>
  <div>
    <div class="card" style="margin-bottom: 5px">
      <el-input v-model="data.name" prefix-icon="Search" style="width: 240px; margin-right: 10px" placeholder="请输入试卷名称查询"></el-input>
      <el-input v-model="data.courseName" prefix-icon="Search" style="width: 240px; margin-right: 10px" placeholder="请输入课程名称查询"></el-input>
      <el-input v-model="data.status" prefix-icon="Search" style="width: 240px; margin-right: 10px" placeholder="请输入阅卷状态查询"></el-input>
      <el-button type="info" plain @click="load">查询</el-button>
      <el-button type="warning" plain style="margin: 0 10px" @click="reset">重置</el-button>
    </div>

    <div class="card" style="margin-bottom: 5px">
      <el-table stripe :data="data.tableData">
        <el-table-column prop="name" label="试卷名称" show-overflow-tooltip/>
        <el-table-column prop="courseName" label="课程名称" show-overflow-tooltip/>
        <el-table-column prop="teacherName" label="授课教师" show-overflow-tooltip/>
        <el-table-column prop="studentName" label="学生姓名" show-overflow-tooltip/>
        <el-table-column prop="status" label="试卷状态" show-overflow-tooltip>
          <template v-slot="scope">
            <el-tag v-if="scope.row.status === '已阅卷'" type="success">{{ scope.row.status }}</el-tag>
            <el-tag v-if="scope.row.status === '待阅卷'" type="danger">{{ scope.row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="score" label="分数" show-overflow-tooltip/>
        <el-table-column label="操作" width="100" fixed="right">
          <template v-slot="scope">
            <el-button :disabled="scope.row.status === '已阅卷'" type="primary" @click="handleEdit(scope.row)">阅卷</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
    <div class="card" v-if="data.total">
      <el-pagination @current-change="load" background layout="prev, pager, next" :page-size="data.pageSize"
                     v-model:current-page="data.pageNum" :total="data.total"/>
    </div>

    <el-dialog title="答案信息" v-model="data.formVisible" width="85%" destroy-on-close>
      <el-table stripe :data="data.answerData">
        <el-table-column prop="questionName" label="题目" show-overflow-tooltip/>
        <el-table-column prop="score" label="分数" show-overflow-tooltip width="60"/>
        <el-table-column prop="typeName" label="题型" show-overflow-tooltip width="80"/>
        <el-table-column prop="answer" label="标准答案" show-overflow-tooltip width="350">
<!--          <template v-slot="scope">-->
<!--            <el-input v-if="scope.row.typeName === '简答题'" type="textarea" :rows="8" v-model="scope.row.answer"-->
<!--                      disabled></el-input>-->
<!--            <span v-else>{{ scope.row.answer }}</span>-->
<!--          </template>-->
        </el-table-column>
<!--        <el-table-column prop="newAnswer" label="学生答案" show-overflow-tooltip width="350">-->
<!--          <template v-slot="scope">-->
<!--            <el-input v-if="scope.row.typeName === '简答题'" type="textarea" :rows="8" v-model="scope.row.newAnswer"-->
<!--                      disabled></el-input>-->
<!--            <span v-else>{{ scope.row.newAnswer }}</span>-->
<!--          </template>-->
<!--        </el-table-column>-->
<!--        <el-table-column prop="result" label="得分" show-overflow-tooltip width="150">-->
<!--          <template v-slot="scope">-->
<!--            <el-input v-if="scope.row.typeName === '简答题'" v-model="scope.row.result"-->
<!--                      placeholder="输入分数"></el-input>-->
<!--            <span v-else>{{ scope.row.result }}</span>-->
<!--          </template>-->
<!--        </el-table-column>-->
      </el-table>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="data.formVisible = false">取 消</el-button>
          <el-button type="primary" @click="update">提 交</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>

import {reactive} from "vue";
import request from "@/utils/request.js";
import {ElMessage, ElMessageBox} from "element-plus";
import {Delete, Edit} from "@element-plus/icons-vue";


const data = reactive({
  user: JSON.parse(localStorage.getItem('xm-user') || '{}'),
  formVisible: false,
  tableData: [],
  pageNum: 1,
  pageSize: 10,
  total: 0,
  name: null,
  courseName: null,
  status: null,
  answerData: [],
  form: {}
})

const load = () => {
  request.get('/score/selectPage', {
    params: {
      pageNum: data.pageNum,
      pageSize: data.pageSize,
      name: data.name,
      courseName: data.courseName,
      status: data.status
    }
  }).then(res => {
    if (res.code === '200') {
      data.tableData = res.data?.list || []
      data.total = res.data?.total
    } else {
      ElMessage.error(res.msg)
    }
  })
}
const handleEdit = (row) => {
  data.form = JSON.parse(JSON.stringify(row))
  request.get('/score/selectAnswer/' + row.id).then(res => {
    if (res.code === '200') {
      data.answerData = res.data
      data.formVisible = true
    } else {
      ElMessage.error(res.msg)
    }
  })
}

const update = () => {
  data.form.answerData = data.answerData
  request.put('/score/update', data.form).then(res => {
    if (res.code === '200') {
      ElMessage.success('操作成功')
      data.formVisible = false
      load()
    }
  })
}

const reset = () => {
  data.name = null
  data.courseName = null
  data.status = null
  load()
}

load()
</script>