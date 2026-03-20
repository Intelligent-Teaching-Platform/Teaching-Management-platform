<template>
  <div style="width: 40%; margin: 30px auto">
    <div style="font-size: 22px; font-weight: bold; text-align: center">{{ data.testPaperData.name }}</div>
    <div style="margin-top: 15px; color: #666666; text-align: center">
      <span>课程名称：{{ data.testPaperData.courseName }}</span>
      <span style="margin: 0 30px">授课教师：{{ data.testPaperData.teacherName }}</span>
    </div>
    <div style="margin-top: 50px">
      <div v-for="item in data.testPaperData.questions" style="margin-bottom: 20px">
        <div style="font-weight: bold; font-size: 16px; background-color: #ddf1ec; line-height: 30px; padding: 5px; margin-bottom: 10px">
          {{ item.name }}（{{ item.typeScore }} 分）
        </div>
        <!--   题目下面要根据具体的题型来设计具体的样式了     -->
        <!--   typeId === 1：单选题     -->
        <div v-if="item.typeId === 1">
          <el-radio-group v-model="item.newAnswer" disabled>
            <el-radio value="A">A. {{ item.optionA }}</el-radio>
            <el-radio value="B">B. {{ item.optionB }}</el-radio>
            <el-radio value="C">C. {{ item.optionC }}</el-radio>
            <el-radio value="D">D. {{ item.optionD }}</el-radio>
          </el-radio-group>
          <div>标准答案：{{ item.answer }}</div>
        </div>
        <!--   typeId ===2：判断题     -->
        <div v-if="item.typeId === 2">
          <el-radio-group v-model="item.newAnswer" disabled>
            <el-radio value="正确">A. 正确</el-radio>
            <el-radio value="错误">B. 错误</el-radio>
          </el-radio-group>
          <div>标准答案：{{ item.answer }}</div>
        </div>
      </div>
    </div>
  </div>
</template>
<script setup>
import {reactive, onMounted} from "vue";
import request from "@/utils/request.js";
import router from "@/router/index.js";
import {ElMessage} from "element-plus";

const data = reactive({
  scoreId: router.currentRoute.value.query.id,
  testPaperData: {},
})


const loadTestPaper = () => {
  data.scoreId = router.currentRoute.value.query.id
  request.get('/score/selectById/' + data.scoreId).then(res => {
    if (res.code === '200') {
      data.testPaperData = res.data
    } else {
      ElMessage.error(res.msg)
    }
  })
}
loadTestPaper()
</script>
<style scoped>
.el-radio-group {
  display: block;
}
.el-radio {
  display: block;
}
.el-checkbox {
  display: block;
}
</style>