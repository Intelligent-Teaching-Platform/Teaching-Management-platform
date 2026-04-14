<template>
  <div class="export-page">

    <!-- 页头 -->
    <div class="page-header">
      <div class="header-icon">
        <el-icon size="28"><Download /></el-icon>
      </div>
      <div class="header-info">
        <h2 class="page-title">资料导出</h2>
        <p class="page-subtitle">
          <el-icon style="vertical-align: -2px; margin-right: 4px;"><Reading /></el-icon>
          {{ data.courseName || '未知课程' }}
        </p>
      </div>
    </div>

    <!-- 警告提示 -->
    <el-alert
        v-if="!data.courseId"
        title="未获取到课程信息，请从课程详情页进入本页面"
        type="warning"
        show-icon
        :closable="false"
        class="warn-bar"
    />

    <!-- 导出功能区 -->
    <div class="export-grid">

      <!-- 卡片1：一键导出Excel -->
      <div class="export-card" :class="{ disabled: !data.courseId }">
        <div class="card-icon excel-icon">
          <el-icon size="32"><DocumentChecked /></el-icon>
        </div>
        <div class="card-body">
          <div class="card-title">一键导出 Excel</div>
          <div class="card-desc">将课程全部数据汇总导出为多 Sheet Excel 文件，包含试卷成绩、作业记录及成绩汇总。</div>
          <div class="sheet-tags">
            <el-tag size="small" type="primary"  effect="plain">试卷成绩</el-tag>
            <el-tag size="small" type="success"  effect="plain">课后作业</el-tag>
            <el-tag size="small" type="warning"  effect="plain">实验作业</el-tag>
            <el-tag size="small" type="danger"   effect="plain">实验报告</el-tag>
            <el-tag size="small" type="info"     effect="plain">成绩汇总</el-tag>
          </div>
        </div>
        <div class="card-action">
          <el-button
              type="primary"
              size="large"
              :loading="data.loadingExcel"
              :disabled="!data.courseId"
              @click="downloadAll"
              class="action-btn"
          >
            <el-icon v-if="!data.loadingExcel"><Download /></el-icon>
            {{ data.loadingExcel ? '导出中…' : '导出 Excel' }}
          </el-button>
        </div>
      </div>

      <!-- 卡片2：实验报告ZIP -->
      <div class="export-card" :class="{ disabled: !data.courseId }">
        <div class="card-icon zip-icon">
          <el-icon size="32"><FolderOpened /></el-icon>
        </div>
        <div class="card-body">
          <div class="card-title">导出实验报告 ZIP</div>
          <div class="card-desc">选择一个实验作业，将该任务下所有学生的实验报告打包为 ZIP，按班级分目录存放。</div>
          <el-select
              v-model="data.exportTaskId"
              placeholder="请选择实验作业"
              class="task-select"
              clearable
              :disabled="!data.courseId || data.lab2Tasks.length === 0"
          >
            <el-option
                v-for="t in data.lab2Tasks"
                :key="t.id"
                :label="t.name"
                :value="t.id"
            />
          </el-select>
          <div v-if="data.lab2Tasks.length === 0 && data.courseId" class="no-task-tip">
            <el-icon><InfoFilled /></el-icon> 暂无实验作业
          </div>
        </div>
        <div class="card-action">
          <el-button
              type="success"
              size="large"
              :loading="data.loadingZip"
              :disabled="!data.courseId || !data.exportTaskId"
              @click="downloadExperimentZip"
              class="action-btn"
          >
            <el-icon v-if="!data.loadingZip"><FolderOpened /></el-icon>
            {{ data.loadingZip ? '打包中…' : '导出 ZIP' }}
          </el-button>
        </div>
      </div>

    </div>

    <!-- 导出说明 -->
    <div class="info-section">
      <div class="info-title">
        <el-icon><InfoFilled /></el-icon>
        Excel 文件 Sheet 说明
      </div>
      <el-table :data="sheetInfo" border stripe class="info-table" size="small">
        <el-table-column prop="sheet"  label="Sheet 名称" width="140" />
        <el-table-column prop="desc"   label="内容说明" />
        <el-table-column prop="remark" label="备注"  width="200" />
      </el-table>
    </div>

  </div>
</template>

<script setup>
import { reactive } from 'vue'
import { useRoute } from 'vue-router'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'
import {
  Download,
  DocumentChecked,
  FolderOpened,
  InfoFilled,
  Reading,
} from '@element-plus/icons-vue'

const route = useRoute()

const data = reactive({
  user: JSON.parse(localStorage.getItem('system-user') || '{}'),
  courseId: route.query?.id ?? route.params?.id ?? null,
  courseName: route.query?.courseName ?? null,
  lab2Tasks: [],
  exportTaskId: null,
  loadingExcel: false,
  loadingZip: false,
})

const sheetInfo = [
  { sheet: '试卷成绩',   desc: '所有学生的试卷作答记录与最终得分', remark: '含教师ID、课程ID' },
  { sheet: '课后作业',   desc: '课后作业提交内容与评分（lab=1）', remark: '含作业内容、提交文件' },
  { sheet: '实验作业',   desc: '实验作业各阶段提交内容（lab=2）', remark: '含实验环境/步骤/总结' },
  { sheet: '实验报告',   desc: '实验报告下载链接（相对路径）', remark: '可结合ZIP导出使用' },
  { sheet: '成绩汇总',   desc: '每位学生试卷、作业、实验的次数与均分', remark: '便于横向对比分析' },
]

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
  if (!data.courseId) { ElMessage.warning('缺少 courseId'); return }
  const teacherId = data.user?.id
  if (!teacherId) { ElMessage.warning('缺少 teacherId，请重新登录'); return }

  data.loadingExcel = true
  try {
    const res = await request.get('/export/courseData', {
      params: { courseId: Number(data.courseId), teacherId: Number(teacherId) },
      responseType: 'blob',
    })
    const safeName = (data.courseName || '课程').replace(/[\\/:*?"<>|]/g, '_')
    downloadBlob(res.data, `${safeName}-资料导出.xlsx`)
    ElMessage.success('导出成功')
  } catch (e) {
    ElMessage.error(e?.response?.data?.msg || e?.message || '导出失败')
  } finally {
    data.loadingExcel = false
  }
}

const loadLab2Tasks = async () => {
  const teacherId = data.user?.id
  if (!data.courseId || !teacherId) return
  try {
    const res = await request.get('/task/selectPage', {
      params: { pageNum: 1, pageSize: 200, courseId: Number(data.courseId), teacherId: Number(teacherId), lab: 2 },
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
  data.loadingZip = true
  try {
    const res = await request.get('/export/experimentReportsZip', {
      params: { courseId: Number(data.courseId), teacherId: Number(teacherId), taskId: Number(data.exportTaskId) },
      responseType: 'blob',
    })
    const task = data.lab2Tasks.find((x) => x.id === data.exportTaskId)
    const taskName   = (task?.name || '实验作业').replace(/[\\/:*?"<>|]/g, '_')
    const safeCourse = (data.courseName || '课程').replace(/[\\/:*?"<>|]/g, '_')
    downloadBlob(res.data, `${safeCourse}-${taskName}-实验报告.zip`)
    ElMessage.success('打包导出成功')
  } catch (e) {
    ElMessage.error(e?.response?.data?.msg || e?.message || '导出失败')
  } finally {
    data.loadingZip = false
  }
}

loadLab2Tasks()
</script>

<style scoped>
/* ========== 页面整体 ========== */
.export-page {
  padding: 24px;
  min-height: 100%;
  background: #f4f6fb;
}

/* ========== 页头 ========== */
.page-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 20px;
}
.header-icon {
  width: 56px;
  height: 56px;
  border-radius: 16px;
  background: linear-gradient(135deg, #4f7cff 0%, #2563eb 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  flex-shrink: 0;
  box-shadow: 0 4px 14px rgba(79, 124, 255, 0.4);
}
.page-title {
  margin: 0 0 4px;
  font-size: 22px;
  font-weight: 700;
  color: #1a1a2e;
}
.page-subtitle {
  margin: 0;
  font-size: 13px;
  color: #6b7280;
  display: flex;
  align-items: center;
}

/* ========== 警告栏 ========== */
.warn-bar {
  margin-bottom: 20px;
  border-radius: 10px;
}

/* ========== 导出卡片网格 ========== */
.export-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 18px;
  margin-bottom: 22px;
}
@media (max-width: 860px) {
  .export-grid { grid-template-columns: 1fr; }
}

.export-card {
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.07);
  display: flex;
  flex-direction: column;
  gap: 14px;
  transition: box-shadow 0.2s, transform 0.2s;
  border: 1.5px solid transparent;
}
.export-card:hover {
  box-shadow: 0 6px 24px rgba(0, 0, 0, 0.12);
  transform: translateY(-2px);
}
.export-card.disabled {
  opacity: 0.55;
  pointer-events: none;
}

/* 卡片图标 */
.card-icon {
  width: 52px;
  height: 52px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  flex-shrink: 0;
}
.excel-icon {
  background: linear-gradient(135deg, #22c55e 0%, #16a34a 100%);
  box-shadow: 0 4px 12px rgba(34, 197, 94, 0.35);
}
.zip-icon {
  background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);
  box-shadow: 0 4px 12px rgba(245, 158, 11, 0.35);
}

/* 卡片内容 */
.card-body { flex: 1; }
.card-title {
  font-size: 16px;
  font-weight: 700;
  color: #1a1a2e;
  margin-bottom: 6px;
}
.card-desc {
  font-size: 13px;
  color: #6b7280;
  line-height: 1.7;
  margin-bottom: 10px;
}
.sheet-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}
.task-select {
  width: 100%;
}
.no-task-tip {
  font-size: 12px;
  color: #9ca3af;
  margin-top: 6px;
  display: flex;
  align-items: center;
  gap: 4px;
}

/* 操作按钮 */
.card-action { display: flex; justify-content: flex-end; }
.action-btn { width: 140px; border-radius: 10px; font-weight: 600; }

/* ========== 说明表格 ========== */
.info-section {
  background: #fff;
  border-radius: 16px;
  padding: 20px 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.07);
}
.info-title {
  font-size: 14px;
  font-weight: 700;
  color: #374151;
  margin-bottom: 14px;
  display: flex;
  align-items: center;
  gap: 6px;
}
.info-title .el-icon { color: #4f7cff; }
.info-table { border-radius: 10px; overflow: hidden; }
</style>
