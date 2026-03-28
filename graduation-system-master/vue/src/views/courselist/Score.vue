<template>
  <div class="page">
    <div style="font-weight: bold; font-size: 17px">我的考试（{{ data.tableData.length }}）</div>
    <div style="margin: 20px 0">
      <el-table stripe :data="data.tableData">
        <el-table-column prop="name" label="试卷名称" show-overflow-tooltip/>
        <el-table-column prop="courseName" label="课程名称" show-overflow-tooltip/>
        <el-table-column prop="teacherName" label="授课教师" show-overflow-tooltip/>
        <el-table-column prop="status" label="试卷状态" show-overflow-tooltip>
          <template v-slot="scope">
            <el-tag v-if="scope.row.status === '已阅卷'" type="success">已批改</el-tag>
            <el-tag v-if="scope.row.status === '待阅卷'" type="danger">待批改</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="score" label="分数" show-overflow-tooltip/>
<!--        <el-table-column label="操作" width="200" fixed="right">-->
<!--          <template v-slot="scope">-->
<!--            <el-button type="primary" @click="handleClick(item)">查看试卷</el-button>-->
<!--          </template>-->
<!--        </el-table-column>-->
      </el-table>
    </div>
    <div class="card" v-if="data.total">
      <el-pagination @current-change="load" background layout="prev, pager, next" :page-size="data.pageSize" v-model:current-page="data.pageNum" :total="data.total" />
    </div>
  </div>
</template>
<script setup>

import {reactive} from "vue";
import request from "@/utils/request.js";
import {ElMessage} from "element-plus";
import router from "@/router";

const data = reactive({
  user: JSON.parse(localStorage.getItem('system-user') || '{}'),
  tableData: [],
  pageNum: 1,
  pageSize: 5,
  total: 0
})

const load = () => {
  request.get('/score/selectPage', {
    params: {
      pageNum: data.pageNum,
      pageSize: data.pageSize,
      // 学生端只看自己的成绩
      studentId: data.user?.id
    }
  }).then(res => {
    if (res.code === '200') {
      data.tableData = res.data.list
      data.total = res.data.total
    } else {
      ElMessage.error(res.msg)
    }
  })
}

const handleClick = (item) => {
  router.push({
    path: '/course/courseDetail/testPaperView',
    query: {
      id:item.id,
      teacherName:item.teacherName,
      courseName:item.courseName,
    }
  });
};
load()
</script>
<style scoped>
.page{
  min-width:100%;
  padding:10px;
}
.card {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}
</style>