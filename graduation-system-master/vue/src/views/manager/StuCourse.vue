<template>
  <div class="stu-course-page">
    <el-card class="box-card" style="width: 100%">
      <template #header>
        <div class="card-header">
          <div class="card-header-text">
            <span class="course-title">课程列表</span>
            <p class="course-subtitle">展示本系统的全部课程</p>
          </div>
        </div>
      </template>

      <div class="filter-container">
        <div class="filter-options">
          <el-input
              v-model="data.searchParams.name"
              placeholder="请输入课程名称"
              clearable
              style="width: 300px"
          />
          <el-button type="primary" @click="loadCourseData">查询</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </div>
      </div>

      <div class="course-list" v-loading="data.loading">
        <div
            v-for="(course, idx) in data.courseList"
            :key="course.id"
            class="course-card-wrap"
        >
          <el-popover
              trigger="hover"
              placement="right"
              :open-delay="200"
              width="280"
          >
            <template #default>
              <div class="popover-content">
                <div class="popover-title">{{ course.name }}</div>
                <div class="popover-row"><span class="popover-label">授课教师：</span><span>{{ course.teacherName || '-' }}</span></div>
                <div class="popover-row"><span class="popover-label">上课时间：</span><span>{{ course.time || '-' }}</span></div>
                <div class="popover-row"><span class="popover-label">上课地点：</span><span>{{ course.location || '-' }}</span></div>
                <div class="popover-row" v-if="course.className"><span class="popover-label">班级：</span><span>{{ course.className }}</span></div>
                <div class="popover-row" v-if="course.term"><span class="popover-label">学期：</span><span>{{ course.term }}</span></div>
              </div>
            </template>

            <template #reference>
              <div class="course-card" :class="`theme-${idx % 3}`">
                <div class="course-header">
                  <h3 class="course-card-title">{{ course.name }}</h3>
                </div>

                <div class="course-info">
                  <div class="info-item">授课教师：{{ course.teacherName || '-' }}</div>
                  <div class="info-item">上课时间：{{ course.time || '-' }}</div>
                  <div class="info-item">上课地点：{{ course.location || '-' }}</div>
                </div>
              </div>
            </template>
          </el-popover>
        </div>
      </div>

      <div class="pagination-container">
        <el-pagination
            v-model:current-page="data.pagination.current"
            v-model:page-size="data.pagination.size"
            :total="data.pagination.total"
            :page-sizes="[10, 20, 30, 50]"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="loadCourseData"
            @current-change="loadCourseData"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, onMounted, onBeforeUnmount } from 'vue';
import { ElMessage } from 'element-plus';
import request from '@/utils/request';

const data = reactive({
  user: JSON.parse(localStorage.getItem('system-user') || '{}'),
  searchParams: {
    name: ''
  },
  pagination: {
    current: 1,
    size: 10,
    total: 0
  },
  loading: false,
  submitting: false,
  signingIn: false,
  courseList: [],
  activeSignIn: null,
  signInStats: null,
  signInRecords: [],
  signInForm: {
    courseId: null,
    duration: 10,
    locationCheck: true,
    distance: 100
  },
  dialog: {
    createSignInVisible: false,
    statsVisible: false,
    recordsVisible: false,
    studentSignInVisible: false
  },
  countdown: 0,
  countdownInterval: null
});

// 数据加载
const loadCourseData = async () => {
  try {
    data.loading = true;
    const params = {
      ...data.searchParams,
      pageNum: data.pagination.current,
      pageSize: data.pagination.size,
      withSignIns: true
    };
    const res = await request.get('/course/selectPage', { params });
    data.courseList = res.data?.list || [];
    data.pagination.total = res.data?.total || 0;
  } catch (error) {
    ElMessage.error('加载课程数据失败: ' + (error.response?.data?.msg || error.message));
  } finally {
    data.loading = false;
  }
};

const resetSearch = () => {
  data.searchParams.name = '';
  loadCourseData();
};

onMounted(() => {
  loadCourseData();
});

onBeforeUnmount(() => {
  clearInterval(data.countdownInterval);
});
</script>

<style scoped>
.stu-course-page {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: 100vh;
}

.box-card {
  border: 1px solid #ebeef5;
  border-radius: 6px;
}

.card-header {
  font-weight: bold;
}

.card-header-text {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.course-title {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
}

.course-subtitle {
  margin: 0;
  font-size: 13px;
  color: #909399;
}

.filter-container {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 20px;
  background-color: #fff;
  border-bottom: 1px solid #eaeefb;
  border-top-left-radius: 6px;
  border-top-right-radius: 6px;
  margin: -20px -20px 12px -20px;
}

.filter-options {
  display: flex;
  align-items: center;
  gap: 10px;
}

.course-table {
  background: #fff;
}

.course-list {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 14px;
  width: 100%;
}

.course-card-wrap {
  min-width: 0;
}

.course-card {
  border: 1px solid #ebeef5;
  border-radius: 4px;
  padding: 15px;
  background: #ffffff;
  transition: all 0.3s ease;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  cursor: default;
}

.course-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  transform: translateY(-2px);
}

.theme-0 {
  background: #ffffff;
}

.theme-1 {
  background: linear-gradient(180deg, #f0f7ff 0%, #ffffff 100%);
  border-color: #d9ecff;
}

.theme-2 {
  background: linear-gradient(180deg, #f7f3ff 0%, #ffffff 100%);
  border-color: #eadbff;
}

.popover-content {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.popover-title {
  font-weight: 600;
  color: #303133;
  font-size: 14px;
}

.popover-row {
  display: flex;
  gap: 6px;
  color: #606266;
  font-size: 13px;
  line-height: 1.4;
}

.popover-label {
  color: #909399;
  width: 80px;
  flex-shrink: 0;
}

.course-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
}

.course-card-title {
  margin: 0;
  font-size: 16px;
  color: #333;
  font-weight: 500;
}

.course-info {
  font-size: 13px;
}

.info-item {
  margin: 6px 0;
  color: #666;
  line-height: 1.5;
}

.pagination-container {
  padding-top: 12px;
  display: flex;
  justify-content: center;
}


</style>