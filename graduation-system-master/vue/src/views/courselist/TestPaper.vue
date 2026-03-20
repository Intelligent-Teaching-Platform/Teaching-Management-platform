<template>
  <div>
    <div class="card" style="margin-bottom: 5px">
      <el-input v-model="data.name" prefix-icon="Search" style="width: 240px; margin-right: 10px" placeholder="请输入试卷名称查询"></el-input>
      <el-button type="info" plain @click="load">查询</el-button>
      <el-button type="warning" plain style="margin: 0 10px" @click="reset">重置</el-button>
    </div>
    <div class="card" style="margin-bottom: 5px">
      <el-button v-if="data.user.role === 'TEACHER'" type="primary" plain @click="handleAdd">出卷</el-button>
      <el-button type="danger" plain @click="delBatch">批量删除</el-button>
    </div>

    <div class="card" style="margin-bottom: 5px">
      <el-table stripe :data="data.tableData" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" />
        <el-table-column prop="name" label="试卷名称" />
        <el-table-column prop="courseName" label="课程名称" />
        <el-table-column prop="teacherName" label="授课教师" />
        <el-table-column prop="type" label="试卷类型">
          <template v-slot="scope">
            <el-tag v-if="scope.row.type === '手动选题'" type="success">{{ scope.row.type }}</el-tag>
<!--            <el-tag v-if="scope.row.type === '自动组卷'" type="primary">{{ scope.row.type }}</el-tag>-->
          </template>
        </el-table-column>
        <el-table-column prop="start" label="开始时间" />
        <el-table-column prop="end" label="结束时间" />
<!--        <el-table-column prop="time" label="考试时长">-->
<!--          <template v-slot="scope">-->
<!--            {{ scope.row.time }} 分钟-->
<!--          </template>-->
<!--        </el-table-column>-->
<!--        <el-table-column prop="status" label="考试状态">-->
<!--          <template v-slot="scope">-->
<!--            <el-tag v-if="scope.row.status === '进行中'" type="primary">{{ scope.row.status }}</el-tag>-->
<!--            <el-tag v-if="scope.row.status === '未开始'" type="warning">{{ scope.row.status }}</el-tag>-->
<!--            <el-tag v-if="scope.row.status === '已结束'" type="danger">{{ scope.row.status }}</el-tag>-->
<!--          </template>-->
<!--        </el-table-column>-->
        <el-table-column label="操作" width="100" fixed="right">
          <template v-slot="scope">
            <el-button type="danger" circle :icon="Delete" @click="del(scope.row.id)"></el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
    <div class="card" v-if="data.total">
      <el-pagination @current-change="load" background layout="prev, pager, next" :page-size="data.pageSize" v-model:current-page="data.pageNum" :total="data.total" />
    </div>

    <el-dialog title="试卷信息" v-model="data.formVisible" width="40%" destroy-on-close>
      <el-form ref="form" :model="data.form" label-width="70px" style="padding: 20px">
        <el-form-item prop="courseId" label="选择课程">
          <el-select v-model="data.form.courseId" placeholder="请选择课程" @change="loadQuestion">
            <el-option
                v-for="item in data.courseData"
                :key="item.id"
                :label="item.name"
                :value="item.id">
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item prop="name" label="试卷名称">
          <el-input v-model="data.form.name" placeholder="请输入试卷名称"></el-input>
        </el-form-item>
        <el-form-item prop="start" label="开始时间">
          <el-date-picker style="width: 100%"
                          v-model="data.form.start"
                          type="date"
                          placeholder="请选择日期"
                          value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item prop="end" label="结束时间">
          <el-date-picker style="width: 100%"
                          v-model="data.form.end"
                          type="date"
                          placeholder="请选择日期"
                          value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item prop="time" label="考试时长">
          <el-input v-model="data.form.time" placeholder="请输入考试时长（分钟）"></el-input>
        </el-form-item>
        <el-form-item prop="type" label="出题方式">
          <el-select v-model="data.form.type" placeholder="请选择出题方式">
            <el-option label="手动选题" value="手动选题"></el-option>
          </el-select>
        </el-form-item>

        <el-form-item prop="idList" label="选择题目" v-if="data.form.type === '手动选题'">
          <el-select
              v-model="data.form.idList"
              multiple
              placeholder="请选择题目"
              style="width: 100%"
          >
            <el-option
                v-for="item in data.questionData"
                :key="item.id"
                :label="item.name + '（' + item.typeName + '）'"
                :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item prop="choiceNum" label="单选个数" v-if="data.form.type === '自动组卷'">
          <el-input v-model="data.form.choiceNum" placeholder="请输入单选题个数"></el-input>
        </el-form-item>
        <el-form-item prop="checkNum" label="判断个数" v-if="data.form.type === '自动组卷'">
          <el-input v-model="data.form.checkNum" placeholder="请输入判断题个数"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="data.formVisible = false">取 消</el-button>
          <el-button type="primary" @click="add">确 定</el-button>
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
  user: JSON.parse(localStorage.getItem('system-user') || '{}'),
  formVisible: false,
  form: {},
  tableData: [],
  pageNum: 1,
  pageSize: 10,
  total: 0,
  name: null,
  ids: [],
  courseData: [],
  questionData: []
})
const loadCourse = () => {
  request.get('/course/selectAll', {
    params: {
      teacherId: data.user.id
    }
  }).then(res => {
    if (res.code === '200') {
      data.courseData = res.data
    } else {
      ElMessage.error(res.msg)
    }
  })
}
const loadQuestion = (courseId) => {
  request.get('/question/selectAll', {
    params: {
      courseId: courseId
    }
  }).then(res => {
    if (res.code === '200') {
      data.questionData = res.data
    } else {
      ElMessage.error(res.msg)
    }
  })
}
const load = () => {
  request.get('/testPaper/selectPage', {
    params: {
      pageNum: data.pageNum,
      pageSize: data.pageSize,
      name: data.name
    }
  }).then(res => {
    if (res.code === '200') {
      data.tableData = res.data?.list || []
      data.total = res.data?.total
    }
  })
}
const handleAdd = () => {
  data.form = {}
  data.formVisible = true
}
const add = () => {
  data.form.teacherId = data.user.id
  request.post('/testPaper/add', data.form).then(res => {
    if (res.code === '200') {
      ElMessage.success('操作成功')
      data.formVisible = false
      load()
    } else {
      ElMessage.error(res.msg)
    }
  })
}

const del = (id) => {
  ElMessageBox.confirm('删除后数据无法恢复，您确定删除吗？', '删除确认', { type: 'warning' }).then(res => {
    request.delete('/testPaper/delete/' + id).then(res => {
      if (res.code === '200') {
        ElMessage.success("删除成功")
        load()
      } else {
        ElMessage.error(res.msg)
      }
    })
  }).catch(err => {
    console.error(err)
  })
}
const delBatch = () => {
  if (!data.ids.length) {
    ElMessage.warning("请选择数据")
    return
  }
  ElMessageBox.confirm('删除后数据无法恢复，您确定删除吗？', '删除确认', { type: 'warning' }).then(res => {
    request.delete("/testPaper/delete/batch", {data: data.ids}).then(res => {
      if (res.code === '200') {
        ElMessage.success('操作成功')
        load()
      } else {
        ElMessage.error(res.msg)
      }
    })
  }).catch(err => {
    console.error(err)
  })
}
const handleSelectionChange = (rows) => {
  data.ids = rows.map(v => v.id)
}

const reset = () => {
  data.name = null
  load()
}

load()
loadCourse()
</script>