<template>
  <div class="task-card">
    <div class="card-header">
      <el-tag
        :type="task.lab === 1 ? 'primary' : 'success'"
        size="small"
        effect="dark"
        class="task-type-tag"
      >
        {{ task.lab === 1 ? '课后任务' : task.lab === 2 ? '实验任务' : '未知类型' }}
      </el-tag>
    </div>
    <div class="card-body">
      <h3 class="task-name" :title="task.name">{{ task.name || '未命名任务' }}</h3>
      <p class="task-content" :title="task.content">
        {{ task.content ? (task.content.length > 60 ? task.content.substring(0, 60) + '...' : task.content) : '暂无内容描述' }}
      </p>
      <div class="task-meta" v-if="task.className">
        <el-icon><UserFilled /></el-icon>
        <span>{{ task.className }}</span>
      </div>
    </div>
    <div class="card-footer">
      <el-button type="primary" size="small" @click="$emit('view-task', task)">
        <el-icon><View /></el-icon>
        查看
      </el-button>
      <el-button type="danger" size="small" @click="$emit('delete-task', task.id)">
        <el-icon><Delete /></el-icon>
        删除
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { View, Delete, UserFilled } from '@element-plus/icons-vue'

defineProps({
  task: {
    type: Object,
    required: true,
  },
})

defineEmits(['view-task', 'delete-task'])
</script>

<style scoped>
.task-card {
  background: #ffffff;
  border-radius: 14px;
  overflow: hidden;
  transition: transform 0.22s ease, box-shadow 0.22s ease, border-color 0.22s ease;
  display: flex;
  flex-direction: column;
  min-height: 210px;
  border: 1px solid rgba(235, 238, 245, 0.9);
  position: relative;
}

.task-card::before {
  content: '';
  position: absolute;
  top: 54px;
  left: 0;
  right: 0;
  bottom: 56px;
  opacity: 0;
  pointer-events: none;
  transition: opacity 0.22s ease;
}

.task-card:hover {
  /* box-shadow: 0 18px 40px rgba(64, 158, 255, 0.18); */
  transform: translateY(-4px);
  /* border-color: rgba(64, 158, 255, 0.4); */
}

.task-card:hover::before {
  opacity: 1;
}

.card-header {
  padding: 8px 14px;
  background: linear-gradient(135deg, #3a7bd5 0%, #00c6ff 100%);
  display: flex;
  justify-content: flex-start;
  align-items: center;
  position: relative;
}

.task-type-tag {
  border-radius: 999px;
  padding: 0 14px;
  font-size: 12px;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.08);
}

.card-body {
  flex: 1;
  padding: 18px 18px 12px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.task-name {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  line-height: 1.5;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.task-content {
  margin: 0;
  font-size: 14px;
  color: #606266;
  line-height: 1.6;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
}

.task-meta {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #a0a3b0;
  margin-top: auto;
  padding-top: 4px;
}

.task-meta :deep(.el-icon) {
  font-size: 14px;
  color: #c0c4cc;
}

.card-footer {
  padding: 10px 16px 14px;
  border-top: 1px solid #ebeef5;
  display: flex;
  gap: 8px;
  justify-content: center;
  background-color: #f8fafc;
}

.card-footer :deep(.el-button--primary) {
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.35);
  border-radius: 999px;
  padding: 0 18px;
}

.card-footer :deep(.el-button--danger) {
  box-shadow: 0 4px 12px rgba(245, 108, 108, 0.25);
  border-radius: 999px;
  padding: 0 18px;
}

.card-footer :deep(.el-button .el-icon) {
  margin-right: 4px;
}
</style>
