<template>
  <div class="container">
    <!-- 页面标题 -->
    <div class="header">
      <div class="header-left" @click="$router.back() ">
        <el-icon size="25px">
          <ArrowLeftBold />
        </el-icon>
        <div>web设计</div>
      </div>

    </div>

    <!-- 内容区域 -->
    <div class="content">
      <!-- 班级统计、资源统计、课程报告、课程统计 -->
      <div class="stats-tabs">
        <el-tabs v-model="activeTab">
          <el-tab-pane label="班级统计" name="classStats"></el-tab-pane>
        </el-tabs>
      </div>

      <!-- 数据展示区域 -->
      <div class="data-display">
        <div class="data-item" style="background-color: #FF6C60;">
          <div class="icon"><el-icon><Calendar /></el-icon></div>
          <div class="text">
            <div class="title">已发布任务点</div>
            <div class="value">32</div>
          </div>
        </div>
        <div class="data-item" style="background-color: #5FB878;">
          <div class="icon"><el-icon><User /></el-icon></div>
          <div class="text">
            <div class="title">学生管理</div>
          </div>
        </div>
        <div class="data-item" style="background-color: #5CACEE;">
          <div class="icon"><el-icon><ChatDotRound /></el-icon></div>
          <div class="text">
            <div class="title">讨论</div>
          </div>
        </div>
        <div class="data-item" style="background-color: #F7BA2A;">
          <div class="icon"><el-icon><Clock /></el-icon></div>
          <div class="text">
            <div class="title">课堂活动</div>
          </div>
        </div>
        <div class="data-item" style="background-color: #FFB900;">
          <div class="icon"><el-icon><Document /></el-icon></div>
          <div class="text">
            <div class="title">作业统计</div>
          </div>
        </div>
        <div class="data-item" style="background-color: #64B5F6;">
          <div class="icon"><el-icon><Timer /></el-icon></div>
          <div class="text">
            <div class="title">章节学习</div>
            <div class="value">610</div>
            <div class="sub-value">本月：2</div>
          </div>
        </div>
        <div class="data-item" style="background-color: #9A66E4;">
          <div class="icon"><el-icon><Folder /></el-icon></div>
          <div class="text">
            <div class="title">成绩管理</div>
          </div>
        </div>
        <div class="data-item" style="background-color: #20A0FF;">
          <div class="icon"><el-icon><Histogram /></el-icon></div>
          <div class="text">
            <div class="title">课程积分</div>
          </div>
        </div>
        <div class="data-item" style="background-color: #FF5746;">
          <div class="icon"><el-icon><PieChart /></el-icon></div>
          <div class="text">
            <div class="title">章节测验</div>
            <div class="value">1</div>
          </div>
        </div>
        <div class="data-item" style="background-color: #323232;">
          <div class="icon"><el-icon><Setting /></el-icon></div>
          <div class="text">
            <div class="title">查看详细</div>
          </div>
        </div>
        <div class="data-item" style="background-color: #1D8CE0;">
          <div class="icon"><el-icon><DataAnalysis /></el-icon></div>
          <div class="text">
            <div class="title">考试统计</div>
          </div>
        </div>
      </div>

      <!-- 一键导出按钮 -->
      <el-button type="success" plain class="export-btn">一键导出</el-button>
    </div>
  </div>
</template>


<script setup>
import { ref } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import {
  ArrowLeftBold,
  Calendar,
  User,
  ChatDotRound,
  Clock,
  Document,
  Timer,
  Folder,
  Histogram,
  PieChart,
  Setting,
  DataAnalysis,
} from '@element-plus/icons-vue';

const router = useRouter();
const route = useRoute();
const lessonName = route.query.courseName;

const goBack = () => {
  router.go(-1);
};

const activeTab = ref('classStats');

const navigateTo = (path) => {
  // 这里可以根据 path 实现具体的路由跳转逻辑
  console.log(`Navigate to ${path}`);
};
</script>



<style lang="scss" scoped>
.container {
  background-color: #fff;
  padding: 20px;
  font-family: Arial, sans-serif;

  .header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    border-bottom: 1px solid #ccc;
    height: 50px;
    margin-bottom: 20px;

    .header-left {
      display: flex;
      align-items: center;
      font-weight: 600;

      .el-icon {
        margin-right: 10px;
      }
    }

    .header-right {
      display: flex;

      .list {
        margin-right: 20px;
        cursor: pointer;

        &:hover {
          color: #409EFF;
        }
      }
    }
  }

  .stats-tabs {
    margin-bottom: 20px;
  }

  .data-display {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 20px;

    .data-item {
      display: flex;
      align-items: center;
      padding: 20px;
      border-radius: 4px;
      color: #fff;
      cursor: pointer;

      .icon {
        margin-right: 10px;
        font-size: 24px;
      }

      .text {
        .title {
          font-size: 16px;
          margin-bottom: 5px;
        }

        .value {
          font-size: 20px;
          font-weight: bold;
        }

        .sub-value {
          font-size: 14px;
          color: rgba(255, 255, 255, 0.7);
        }
      }

      &:hover {
        opacity: 0.8;
      }
    }
  }

  .export-btn {
    margin-top: 20px;
    float: right;
  }
}
</style>
