<template>
  <div class="home-wrapper">
    <!-- 原来的欢迎卡片结构，去掉内联样式，整体美化 -->
    <div class="card welcome-card">
      <div class="welcome-main-text">
        欢迎您，{{ data.user.name }}，祝您今天过得开心！
      </div>
      <div class="welcome-sub-text">
        数智验舱 · 智辅实验管理平台
      </div>
    </div>

    <!-- 原来的系统公告结构，去掉内联样式，整体美化 -->
    <div class="card notice-card">
      <div class="notice-header">
        <div class="notice-title">系统公告</div>
        <div class="notice-subtitle">最新的平台通知与课程更新会展示在这里</div>
      </div>

      <div v-if="data.noticeData.length" class="notice-content">
        <el-timeline>
          <el-timeline-item
              v-for="(item,index) in data.noticeData"
              :key="index"
              :timestamp="item.time" 
              color="#9ab2e6"  
          >
            <div class="notice-item-title">{{ item.title }}</div>
            <div class="notice-item-content">{{ item.content }}</div>
          </el-timeline-item>
        </el-timeline>
      </div>
      <div v-else class="notice-empty">
        暂无公告，祝你学习愉快～
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive } from "vue";
import request from "@/utils/request";
import { ElMessage } from "element-plus";


const data = reactive({
  user: JSON.parse(localStorage.getItem('system-user') || '{}'),
  noticeData: []
})
const loadNotice = () => {
  request.get('/notice/selectAll').then(res => {
    if(res.code === '200') {
      data.noticeData = res.data
    }else {
      ElMessage.error(res.msg)
    }
  })
}


loadNotice()
</script>

<style scoped>
.home-wrapper {
  flex: 1;
  padding: 16px 18px 20px;
  box-sizing: border-box;
  background-color: #ffffff;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.card {
  background-color: #ffffff;
  border-radius: 12px;
  box-shadow: 0 4px 16px rgba(15, 40, 80, 0.06);
  padding: 14px 18px;
}

.welcome-card {
  display: flex;
  flex-direction: column;
  gap: 4px;
  border-left: 7px solid #9ab2e6;
}

.welcome-main-text {
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
}

.welcome-sub-text {
  font-size: 13px;
  color: #6b7280;
  margin-top: 2px;
}

.notice-card {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.notice-header {
  margin-bottom: 4px;
}

.notice-title {
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
}

.notice-subtitle {
  font-size: 12px;
  color: #6b7280;
  margin-top: 2px;
}

.notice-content {
  margin-top: 8px;
  max-height: 360px;
  overflow-y: auto;
}

.notice-item-title {
  font-size: 14px;
  font-weight: 500;
  color: #111827;
  margin-bottom: 2px;
}

.notice-item-content {
  font-size: 13px;
  color: #4b5563;
}

.notice-empty {
  margin-top: 12px;
  font-size: 13px;
  color: #9ca3af;
}

</style>