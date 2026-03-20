<template>
  <div class="course-card">
    <!-- 顶部大图区域 -->
    <div class="card-media">
      <a href="javascript:void(0)" >
        <img
          src="@/assets/imgs/CourseCard.jpg"
          alt="Course Image"
          class="course-image"
        />
      </a>
      <!-- 左上角状态角标（学生端显示“学习中”） -->
      <div class="status-badge" v-if="role === 'STUDENT'">
        学习中
      </div>
    </div>

    <!-- 卡片主体内容 -->
    <div class="card-body">
      <!-- 标题 + 课程类型标签 -->
      <div class="title-row">
        <h3 class="course-name" @click="handleNavigate">{{ course.name }}</h3>
        <span class="chip-type">核心课程</span>
      </div>

      <!-- 教师信息 -->
      <div class="teacher-row">
        <div class="avatar">
          <span class="avatar-letter">{{ teacherInitial }}</span>
        </div>
        <div class="teacher-meta">
          <div class="teacher-name">{{ course.teacherName || '未设置教师' }}</div>
          <div class="teacher-sub">
            开课时间：{{ displayTerm || ' ' }}
          </div>
        </div>
        <el-button
          v-if="showDelete"
          class="icon-delete"
          text
          type="danger"
          @click.stop="handleDelete"
        >
          删除
        </el-button>
      </div>

      <!-- 底部操作按钮 -->
      <div class="actions-row">
        <el-button
          type="primary"
          class="primary-btn"
          @click="handleNavigate"
        >
          继续学习
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { defineProps, defineEmits, computed } from 'vue';
import router from "@/router";

const props = defineProps({
  course: {
    type: Object,
    required: true
  },
  term: { type: String, default: '' },
  /** 是否显示删除按钮（仅管理员在课程列表可删） */
  showDelete: { type: Boolean, default: false },
  /** TEACHER | STUDENT，用于展示教/学标签 */
  role: { type: String, default: 'TEACHER' }
});

const displayTerm = computed(() => {
  const t = props.course?.term || props.term || ''
  return t.trim() || null
})

// 教师头像首字母
const teacherInitial = computed(() => {
  const name = props.course?.teacherName || ''
  if (!name) return '讲'
  return name.charAt(0).toUpperCase()
})

// 课程进度（如果后端有 progress 字段就用，没有就给一个默认值）
const progressPercent = computed(() => {
  const p = Number(props.course?.progress)
  if (!isNaN(p) && p > 0 && p <= 100) {
    return Math.round(p)
  }
  return 35
})

// 定义自定义事件
const emit = defineEmits(['move', 'delete']);

// 删除课程的方法，触发delete事件并传递props.course对象
const handleDelete = async () => {
  emit('delete', props.course);
};

// 导航到课程详情页的方法
const handleNavigate = () => {
  router.push({
    path: '/course/courseDetail',
    query: {
      id: props.course.id,
      courseName: props.course.name
    }
  });
};
</script>

<style lang="scss" scoped>
.course-card {
  background-color: #ffffff;
  border-radius: 18px;
  overflow: hidden;
  border: 1px solid #e5ecff;
  display: flex;
  flex-direction: column;
  margin: 10px;
}

.course-card:hover {
  box-shadow: 0 8px 20px rgba(90, 101, 114, 0.25);
  transform: translateY(-6px);
}

.card-media {
  position: relative;
}

.course-image {
  width: 100%;
  height: 120px;
  object-fit: cover;
  display: block;
}

.status-badge {
  position: absolute;
  top: 14px;
  left: 14px;
  background-color: #24b47e;
  color: #ffffff;
  padding: 4px 10px;
  border-radius: 999px;
  font-size: 12px;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.15);
}

.card-body {
  padding: 8px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.title-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.course-name {
  color: #174a9b;
  font-size: 18px;
  font-weight: 700;
  margin: 0;
  flex: 1;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.chip-type {
  flex-shrink: 0;
  padding: 2px 10px;
  border-radius: 999px;
  border: 1px solid #d1ddff;
  color: #3a63d8;
  font-size: 12px;
  background-color: #f3f6ff;
}

.teacher-row {
  display: flex;
  align-items: center;
  gap: 10px;
}

.avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background-color: #e1e7f5;
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar-letter {
  font-size: 16px;
  color: #6071a8;
  font-weight: 600;
}

.teacher-meta {
  flex: 1;
  min-width: 0;
}

.teacher-name {
  font-size: 14px;
  color: #3a3f58;
  margin: 0 0 2px;
}

.teacher-sub {
  font-size: 12px;
  color: #8b91aa;
}

 

.icon-delete {
  flex-shrink: 0;
  font-size: 12px;
}


.actions-row {
  margin-top: 2px;
  display: flex;
  justify-content: flex-start;
}

.primary-btn {
  width: 140px;
  border-radius: 999px;
}
</style>