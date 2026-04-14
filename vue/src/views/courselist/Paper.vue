<template>
  <div class="paper-page">
    <div class="paper-back-row">
      <el-button type="default" text class="paper-back-btn" @click="goBack">
        <el-icon class="paper-back-icon"><ArrowLeft /></el-icon>
        返回
      </el-button>
    </div>
    <div style="font-size: 20px; font-weight: bold; text-align: center">{{ data.testPaperData.name }}</div>
    <div style="margin-top: 15px; color: #666666; text-align: center">
      <span>课程名称：{{ data.testPaperData.courseName }}</span>
      <span style="margin: 0 30px">授课教师：{{ data.testPaperData.teacherName }}</span>
      <span>考试时间：{{ data.testPaperData.time }} 分钟</span>
    </div>
<!--    <div style="font-size: 20px; font-weight: bold; text-align: center">{{ data.testPaperData.name }}</div>-->
<!--    <div style="margin-top: 15px; color: #666666; text-align: center">-->
<!--      <span>课程名称：{{ data.courseName }}</span>-->
<!--      <span style="margin: 0 30px">授课教师：{{ data.teacherName }}</span>-->
<!--      <span>考试时间：{{ data.testPaperData.time }} 分钟</span>-->
<!--    </div>-->

    <div style="margin-top: 50px">
      <div v-for="item in data.testPaperData.questions" :key="item.id ?? item.name" style="margin-bottom: 20px">
        <div style="font-weight: bold; font-size: 16px; background-color: #ddf1ec; line-height: 30px; padding: 5px; margin-bottom: 10px">
          {{ item.name }}
        </div>
        <!--   题目下面要根据具体的题型来设计具体的样式了     -->
        <!--   typeId === 1：单选题     -->
        <div v-if="item.typeId === 1">
          <el-radio-group v-model="item.newAnswer">
            <el-radio label="A">A. {{ item.optionA }}</el-radio>
            <el-radio label="B">B. {{ item.optionB }}</el-radio>
            <el-radio label="C">C. {{ item.optionC }}</el-radio>
            <el-radio label="D">D. {{ item.optionD }}</el-radio>
          </el-radio-group>
        </div>
        <!--   typeId === 2：判断题     -->
        <div v-if="item.typeId === 2">
          <el-radio-group v-model="item.newAnswer">
            <el-radio label="正确">A. 正确</el-radio>
            <el-radio label="错误">B. 错误</el-radio>
          </el-radio-group>
        </div>
      </div>
      <div style="text-align: center; margin: 50px">
        <el-button style="padding: 20px 40px" type="primary" @click="submitPaper">提交试卷</el-button>
      </div>
    </div>
  </div>
</template>
<script setup>
import { reactive, onMounted } from 'vue'
import request from '@/utils/request.js'
import router from '@/router/index.js'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'

const route = useRoute()

const data = reactive({
  testPaperId: router.currentRoute.value.query.id,
  testPaperData: {},
  user: JSON.parse(localStorage.getItem('system-user') || '{}'),
})

const goBack = () => {
  if (typeof window !== 'undefined' && window.history.length > 1) {
    router.back()
    return
  }
  const courseId =
    route.query.courseId ?? data.testPaperData?.courseId ?? null
  const courseName =
    route.query.courseName ||
    data.testPaperData?.courseName ||
    ''
  router.push({
    path: '/course/courseDetail/exam',
    query: {
      ...(courseId != null && courseId !== '' ? { id: String(courseId) } : {}),
      ...(courseName ? { courseName } : {}),
    },
  })
}

onMounted(() => {
  data.testPaperId = router.currentRoute.value.query.id
  data.teacherName = router.currentRoute.value.query.teacherName || ''
  data.courseName = router.currentRoute.value.query.courseName || ''
  loadTestPaper()
})

const loadTestPaper = () => {
  data.testPaperId = router.currentRoute.value.query.id
  request.get('/testPaper/selectById/' + data.testPaperId).then(res => {
    if (res.code === '200') {
      data.testPaperData = res.data
    } else {
      ElMessage.error(res.msg)
    }
  })
}

const submitPaper = () => {
  // 提交试卷
  data.testPaperData.studentId= data.user.id;
  request.post('/score/add', data.testPaperData).then(res => {
    if (res.code === '200') {
      ElMessage.success('提交成功')
    } else {
      ElMessage.error(res.msg)
    }
  })
}
</script>
<style scoped>
.paper-page {
  max-width: 900px;
  margin: 24px auto 40px;
  padding: 0 16px;
}

.paper-back-row {
  margin-bottom: 16px;
}

.paper-back-btn {
  padding: 8px 10px 8px 4px;
  font-size: 15px;
  color: var(--color-primary-hover, #0f766e);
}

.paper-back-btn:hover {
  color: var(--color-primary, #0d9488);
}

.paper-back-icon {
  margin-right: 4px;
  vertical-align: middle;
}

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