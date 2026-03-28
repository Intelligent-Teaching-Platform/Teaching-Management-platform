<template>
  <div class="page">
    <div class="card header">
      <div class="title">资料导出</div>
      <div class="desc">
        课程：{{ data.courseName || '-' }}（ID：{{ data.courseId ?? '-' }}）
      </div>
    </div>

    <div class="card actions">
      <el-button type="primary" @click="downloadAll" :disabled="!data.courseId">
        一键导出（Excel）
      </el-button>

      <div style="height: 12px;"></div>

      <el-select
          v-model="data.exportTaskId"
          placeholder="请选择要导出的实验作业"
          style="width: 360px; margin-right: 12px;"
          clearable
      >
        <el-option
            v-for="t in data.lab2Tasks"
            :key="t.id"
            :label="t.name"
            :value="t.id"
        />
      </el-select>
      <el-button
          type="success"
          @click="downloadExperimentZip"
          :disabled="!data.courseId || !data.exportTaskId"
      >
        导出实验报告ZIP
      </el-button>
      <el-alert
          v-if="!data.courseId"
          title="缺少 courseId（请从课程详情进入该页面）"
          type="warning"
          show-icon
          :closable="false"
          style="margin-top: 12px"
      />
    </div>

    <div class="card tip">
      <div style="font-weight: 700; margin-bottom: 6px;">导出内容（同一个 Excel 多个 Sheet）</div>
      <div class="tip-list">
        <div>1）试卷成绩（按试卷/学生）</div>
        <div>2）课后作业（lab=1）</div>
        <div>3）实验作业（lab=2）</div>
        <div>4）实验报告（下载链接/作业ID）</div>
        <div>5）学生成绩汇总（均分等）</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive } from 'vue'
import { useRoute } from 'vue-router'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'

const route = useRoute()

const data = reactive({
  user: JSON.parse(localStorage.getItem('system-user') || '{}'),
  courseId: route.query?.id ?? route.params?.id ?? null,
  courseName: route.query?.courseName ?? null,
  lab2Tasks: [],
  exportTaskId: null,
})

const downloadBlob = (blob, filename) => {
  const url = window.URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = filename
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  window.URL.revokeObjectURL(url)
}

const downloadAll = async () => {
  if (!data.courseId) {
    ElMessage.warning('缺少 courseId')
    return
  }
  const teacherId = data.user?.id
  if (!teacherId) {
    ElMessage.warning('缺少 teacherId（请重新登录后重试）')
    return
  }

  try {
    const res = await request.get('/export/courseData', {
      params: {
        courseId: Number(data.courseId),
        teacherId: Number(teacherId),
      },
      responseType: 'blob',
    })

    const safeName = (data.courseName || '课程').replace(/[\\\\/:*?"<>|]/g, '_')
    downloadBlob(res.data, `${safeName}-资料导出.xlsx`)
  } catch (e) {
    const msg = e?.response?.data?.msg || e?.message || '导出失败'
    ElMessage.error(msg)
  }
}

const loadLab2Tasks = async () => {
  const teacherId = data.user?.id
  if (!data.courseId || !teacherId) return
  try {
    const res = await request.get('/task/selectPage', {
      params: {
        pageNum: 1,
        pageSize: 200,
        courseId: Number(data.courseId),
        teacherId: Number(teacherId),
        lab: 2,
      },
    })
    if (res.code === '200') {
      data.lab2Tasks = res.data?.list || []
      if (!data.exportTaskId && data.lab2Tasks.length) {
        data.exportTaskId = data.lab2Tasks[0].id
      }
    } else {
      ElMessage.error(res.msg || '实验作业列表加载失败')
    }
  } catch (e) {
    ElMessage.error(e?.response?.data?.msg || e?.message || '实验作业列表加载失败')
  }
}

const downloadExperimentZip = async () => {
  const teacherId = data.user?.id
  if (!data.courseId || !teacherId || !data.exportTaskId) {
    ElMessage.warning('请先选择要导出的实验作业')
    return
  }
  try {
    const res = await request.get('/export/experimentReportsZip', {
      params: {
        courseId: Number(data.courseId),
        teacherId: Number(teacherId),
        taskId: Number(data.exportTaskId),
      },
      responseType: 'blob',
    })
    const task = data.lab2Tasks.find((x) => x.id === data.exportTaskId)
    const taskName = (task?.name || '实验作业').replace(/[\\\\/:*?"<>|]/g, '_')
    const safeCourse = (data.courseName || '课程').replace(/[\\\\/:*?"<>|]/g, '_')
    downloadBlob(res.data, `${safeCourse}-${taskName}-实验报告.zip`)
  } catch (e) {
    const msg = e?.response?.data?.msg || e?.message || '导出失败'
    ElMessage.error(msg)
  }
}

loadLab2Tasks()
</script>

<style scoped>
.page {
  padding: 12px;
}
.card {
  background: #fff;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 12px;
}
.header .title {
  font-weight: 800;
  font-size: 18px;
  margin-bottom: 6px;
}
.header .desc {
  color: #666;
}
.tip-list {
  color: #555;
  line-height: 1.9;
}
</style>

