<template>
  <div class="exam-page">
    <!-- 页头 -->
    <div class="page-header">
      <div class="header-content">
        <div class="header-icon">
          <el-icon size="32"><DocumentChecked /></el-icon>
        </div>
        <div class="header-info">
          <h2 class="page-title">我的考试</h2>
          <p class="page-subtitle">共 {{ data.total }} 场考试</p>
        </div>
      </div>
    </div>

    <!-- 搜索栏 -->
    <div class="search-section">
      <div class="search-box">
        <el-input 
          size="large" 
          clearable 
          @clear="reset" 
          class="search-input"
          v-model="data.courseName" 
          placeholder="请输入课程名称搜索考试..."
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-button size="large" type="primary" @click="load" class="search-btn">
          <el-icon><Search /></el-icon>
          搜索
        </el-button>
      </div>
    </div>

    <!-- 考试卡片列表 -->
    <div class="exam-grid" v-if="data.examData.length > 0">
      <div 
        class="exam-card" 
        v-for="item in data.examData" 
        :key="item.id"
        @click="handleClick(item)"
      >
        <div class="card-header">
          <div class="exam-title">{{ item.name }}</div>
          <div class="exam-status" :class="getStatusClass(item)">
            {{ getStatusText(item) }}
          </div>
        </div>
        
        <div class="card-body">
          <div class="info-row">
            <div class="info-label">
              <el-icon><Reading /></el-icon>
              课程
            </div>
            <div class="info-value">{{ item.courseName }}</div>
          </div>
          
          <div class="info-row">
            <div class="info-label">
              <el-icon><User /></el-icon>
              教师
            </div>
            <div class="info-value teacher-info">
              <img :src="item.teacherAvatar || '/default-avatar.png'" alt="">
              <span>{{ item.teacherName }}</span>
            </div>
          </div>
          
          <div class="info-row">
            <div class="info-label">
              <el-icon><Clock /></el-icon>
              时间
            </div>
            <div class="info-value time-range">
              <div>{{ formatDate(item.start) }}</div>
              <div class="time-separator">至</div>
              <div>{{ formatDate(item.end) }}</div>
            </div>
          </div>
        </div>
        
        <div class="card-footer">
          <el-button type="primary" class="enter-btn">
            进入考试
            <el-icon class="btn-icon"><ArrowRight /></el-icon>
          </el-button>
        </div>
      </div>
    </div>

    <!-- 空状态 -->
    <div v-else class="empty-state">
      <el-empty description="暂无考试" :image-size="180">
        <template #image>
          <div class="empty-icon">
            <el-icon size="80" color="#dcdfe6"><DocumentChecked /></el-icon>
          </div>
        </template>
        <template #description>
          <div class="empty-text">
            <div class="empty-title">暂无考试</div>
            <div class="empty-desc">当前没有可参加的考试，请稍后再来</div>
          </div>
        </template>
      </el-empty>
    </div>

    <!-- 分页 -->
    <div v-if="data.total > data.pageSize" class="pagination-section">
      <el-pagination 
        @current-change="load" 
        background 
        layout="prev, pager, next" 
        :page-size="data.pageSize" 
        v-model:current-page="data.pageNum" 
        :total="data.total" 
      />
    </div>
  </div>
</template>

<script setup>
import { reactive } from "vue";
import request from "@/utils/request.js";
import { ElMessage } from "element-plus";
import router from '@/router'
import { 
  DocumentChecked, 
  Search, 
  Reading, 
  User, 
  Clock, 
  ArrowRight 
} from '@element-plus/icons-vue'

const data = reactive({
  courseName: null,
  pageNum: 1,
  pageSize: 8,
  total: 0,
  examData: []
})

// 获取考试状态
const getStatus = (item) => {
  const now = new Date().getTime()
  const start = new Date(item.start).getTime()
  const end = new Date(item.end).getTime()
  
  if (now < start) return 'upcoming'
  if (now > end) return 'ended'
  return 'ongoing'
}

// 获取状态文本
const getStatusText = (item) => {
  const status = getStatus(item)
  const map = {
    'upcoming': '未开始',
    'ongoing': '进行中',
    'ended': '已结束'
  }
  return map[status]
}

// 获取状态样式类
const getStatusClass = (item) => {
  const status = getStatus(item)
  return `status-${status}`
}

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const load = () => {
  request.get('/testPaper/selectPage', {
    params: {
      pageNum: data.pageNum,
      pageSize: data.pageSize,
      courseName: data.courseName,
    }
  }).then(res => {
    if (res.code === '200') {
      data.examData = res.data.list
      data.total = res.data.total
    } else {
      ElMessage.error(res.msg)
    }
  })
}

const handleClick = (item) => {
  router.push({
    path: '/course/courseDetail/paper',
    query: {
      id: item.id,
      teacherName: item.teacherName,
      courseName: item.courseName,
    }
  });
};

const reset = () => {
  data.courseName = null
  load()
}

load()
</script>

<style scoped>
.exam-page {
  padding: 24px;
  min-height: 100%;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e7ed 100%);
}

/* 页头 */
.page-header {
  margin-bottom: 24px;
}

.header-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.header-icon {
  width: 60px;
  height: 60px;
  border-radius: 16px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);
}

.header-info {
  flex: 1;
}

.page-title {
  margin: 0 0 6px;
  font-size: 24px;
  font-weight: 700;
  color: #1a1a2e;
}

.page-subtitle {
  margin: 0;
  font-size: 14px;
  color: #6b7280;
}

/* 搜索栏 */
.search-section {
  margin-bottom: 24px;
}

.search-box {
  display: flex;
  gap: 12px;
  max-width: 600px;
}

.search-input {
  flex: 1;
}

.search-input :deep(.el-input__wrapper) {
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  padding: 4px 16px;
}

.search-btn {
  border-radius: 12px;
  padding: 0 24px;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 6px;
}

/* 考试卡片网格 */
.exam-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 20px;
  margin-bottom: 24px;
}

/* 考试卡片 */
.exam-card {
  background: #fff;
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  transition: all 0.3s ease;
  cursor: pointer;
  border: 1px solid transparent;
  display: flex;
  flex-direction: column;
}

.exam-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12);
  border-color: #e0e7ff;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
  gap: 12px;
}

.exam-title {
  font-size: 17px;
  font-weight: 700;
  color: #1a1a2e;
  line-height: 1.4;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.exam-status {
  font-size: 12px;
  font-weight: 600;
  padding: 4px 12px;
  border-radius: 20px;
  white-space: nowrap;
  flex-shrink: 0;
}

.status-ongoing {
  background: #ecfdf5;
  color: #059669;
}

.status-upcoming {
  background: #fef3c7;
  color: #d97706;
}

.status-ended {
  background: #fee2e2;
  color: #dc2626;
}

.card-body {
  flex: 1;
  margin-bottom: 16px;
}

.info-row {
  display: flex;
  align-items: flex-start;
  margin-bottom: 12px;
}

.info-row:last-child {
  margin-bottom: 0;
}

.info-label {
  display: flex;
  align-items: center;
  gap: 6px;
  width: 70px;
  font-size: 13px;
  color: #6b7280;
  flex-shrink: 0;
}

.info-label .el-icon {
  font-size: 14px;
  color: #9ca3af;
}

.info-value {
  flex: 1;
  font-size: 13px;
  color: #374151;
  line-height: 1.5;
}

.teacher-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.teacher-info img {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  object-fit: cover;
  border: 1px solid #e5e7eb;
}

.time-range {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.time-separator {
  font-size: 11px;
  color: #9ca3af;
  padding-left: 4px;
}

.card-footer {
  margin-top: auto;
  padding-top: 16px;
  border-top: 1px solid #f3f4f6;
}

.enter-btn {
  width: 100%;
  border-radius: 10px;
  height: 40px;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  transition: all 0.3s ease;
}

.enter-btn:hover {
  transform: scale(1.02);
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);
}

.btn-icon {
  transition: transform 0.3s ease;
}

.exam-card:hover .btn-icon {
  transform: translateX(4px);
}

/* 空状态 */
.empty-state {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.empty-icon {
  margin-bottom: 16px;
}

.empty-text {
  text-align: center;
}

.empty-title {
  font-size: 18px;
  font-weight: 600;
  color: #374151;
  margin-bottom: 8px;
}

.empty-desc {
  font-size: 14px;
  color: #9ca3af;
}

/* 分页 */
.pagination-section {
  display: flex;
  justify-content: center;
  padding: 20px 0;
}

/* 响应式 */
@media (max-width: 768px) {
  .exam-page {
    padding: 16px;
  }
  
  .exam-grid {
    grid-template-columns: 1fr;
  }
  
  .search-box {
    flex-direction: column;
  }
  
  .search-btn {
    width: 100%;
  }
}
</style>
