<template>
  <div>
    <div class="card" style="margin-bottom: 5px">
      <el-input v-model="data.name" prefix-icon="Search" style="width: 240px; margin-right: 10px" placeholder="请输入题目名称查询"></el-input>
      <el-button type="info" plain @click="load">查询</el-button>
      <el-button type="warning" plain style="margin: 0 10px" @click="reset">重置</el-button>
    </div>
    <div class="card" style="margin-bottom: 5px">
      <el-button type="primary" plain @click="handleAdd">新增</el-button>
      <el-button type="danger" plain @click="delBatch">批量删除</el-button>
    </div>

    <div class="card" style="margin-bottom: 5px">
      <el-table stripe :data="data.tableData" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" />
        <el-table-column prop="name" label="题目名称" />
        <el-table-column prop="courseName" label="课程名称" />
        <el-table-column prop="teacherName" label="授课教师" />
        <el-table-column prop="typeName" label="题型" width="100">
          <template v-slot="scope">
            <el-tag v-if="scope.row.typeName === '单选题'" type="success">{{ scope.row.typeName }}</el-tag>
            <el-tag v-if="scope.row.typeName === '判断题'" type="warning">{{ scope.row.typeName }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="typeScore" label="分数" />
        <el-table-column label="选项">
          <template v-slot="scope">
            <span v-if="scope.row.typeName !== '单选题'">无</span>
            <div v-else>
              <div>A. {{ scope.row.optionA }}</div>
              <div>B. {{ scope.row.optionB }}</div>
              <div>C. {{ scope.row.optionC }}</div>
              <div>D. {{ scope.row.optionD }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="answer" label="答案" show-overflow-tooltip/>
        <el-table-column label="操作" width="100" fixed="right">
          <template v-slot="scope">
            <el-button type="primary" circle :icon="Edit" @click="handleEdit(scope.row)"></el-button>
            <el-button type="danger" circle :icon="Delete" @click="del(scope.row.id)"></el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
    <div class="card" v-if="data.total">
      <el-pagination @current-change="load" background layout="prev, pager, next" :page-size="data.pageSize" v-model:current-page="data.pageNum" :total="data.total" />
    </div>

    <el-dialog title="题目信息" v-model="data.formVisible" width="40%" destroy-on-close>
      <el-form ref="form" :model="data.form" label-width="70px" style="padding: 20px">
        <el-form-item prop="courseId" label="选择课程">
          <el-select v-model="data.form.courseId" placeholder="请选择课程">
            <el-option
                v-for="item in data.courseData"
                :key="item.id"
                :label="item.name"
                :value="item.id">
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item prop="name" label="题目名称">
          <el-input v-model="data.form.name" placeholder="请输入题目名称"></el-input>
        </el-form-item>
        <el-form-item prop="typeId" label="选择题型">
          <el-select v-model="data.form.typeId" placeholder="请选择题型">
            <el-option
                v-for="item in data.typeData"
                :key="item.id"
                :label="item.name"
                :value="item.id">
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item prop="optionA" label="选项A：" v-if="data.form.typeId === 1">
          <el-input v-model="data.form.optionA" placeholder="请输入选项A"></el-input>
        </el-form-item>
        <el-form-item prop="optionB" label="选项B：" v-if="data.form.typeId === 1">
          <el-input v-model="data.form.optionB" placeholder="请输入选项B"></el-input>
        </el-form-item>
        <el-form-item prop="optionC" label="选项C：" v-if="data.form.typeId === 1">
          <el-input v-model="data.form.optionC" placeholder="请输入选项C"></el-input>
        </el-form-item>
        <el-form-item prop="optionD" label="选项D：" v-if="data.form.typeId === 1">
          <el-input v-model="data.form.optionD" placeholder="请输入选项D"></el-input>
        </el-form-item>
        <el-form-item prop="answer" label="题目答案" v-if="data.form.typeId === 1">
          <el-input v-model="data.form.answer" placeholder="请输入题目答案"></el-input>
        </el-form-item>

        <el-form-item prop="answer" label="题目答案" v-if="data.form.typeId === 2">
          <el-select v-model="data.form.answer" placeholder="请选择答案">
            <el-option label="正确" value="正确"></el-option>
            <el-option label="错误" value="错误"></el-option>
          </el-select>
        </el-form-item>
<!--        <el-form-item prop="answer" label="题目答案" v-if="data.form.typeId === 4">-->
<!--          <el-input v-model="data.form.answer" placeholder="请输入题目答案"></el-input>-->
<!--        </el-form-item>-->
<!--        <el-form-item prop="answer" label="题目答案" v-if="data.form.typeId === 5">-->
<!--          <el-input type="textarea" :rows="8" v-model="data.form.answer" placeholder="请输入题目答案"></el-input>-->
<!--        </el-form-item>-->
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="data.formVisible = false">取 消</el-button>
          <el-button type="primary" @click="save">确 定</el-button>
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
  typeData: [],
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
const loadType = () => {
  request.get('/questionType/selectAll').then(res => {
    if (res.code === '200') {
      data.typeData = res.data
    } else {
      ElMessage.error(res.msg)
    }
  })
}

const load = () => {
  request.get('/question/selectPage', {
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
const handleEdit = (row) => {
  data.form = JSON.parse(JSON.stringify(row))
  data.formVisible = true
}
const add = () => {
  data.form.teacherId = data.user.id
  request.post('/question/add', data.form).then(res => {
    if (res.code === '200') {
      ElMessage.success('操作成功')
      data.formVisible = false
      load()
    } else {
      ElMessage.error(res.msg)
    }
  })
}

const update = () => {
  request.put('/question/update', data.form).then(res => {
    if (res.code === '200') {
      ElMessage.success('操作成功')
      data.formVisible = false
      load()
    }
  })
}

const save = () => {
  data.form.id ? update() : add()
}

const del = (id) => {
  ElMessageBox.confirm('删除后数据无法恢复，您确定删除吗？', '删除确认', { type: 'warning' }).then(res => {
    request.delete('/question/delete/' + id).then(res => {
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
    request.delete("/question/delete/batch", {data: data.ids}).then(res => {
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
loadType()
</script>