<template>
  <div class="task-card" :class="typeClass">
    <div class="card-thumb">
      <img
        v-if="coverUrl"
        class="thumb-img"
        :src="coverUrl"
        alt=""
        loading="lazy"
      />
      <div v-else class="thumb-placeholder" aria-hidden="true">
        <div class="thumb-placeholder-inner">
          <el-icon class="thumb-ph-icon"><component :is="typeIcon" /></el-icon>
          <span class="thumb-ph-label">{{ typeLabel }}</span>
        </div>
      </div>
      <div class="thumb-overlay" aria-hidden="true"></div>
      <div class="thumb-badge">
        <el-tag :type="task.lab === 1 ? 'primary' : 'success'" size="small" effect="dark" class="task-type-tag">
          {{ typeLabel }}
        </el-tag>
      </div>
    </div>

    <div class="card-body">
      <div class="body-top">
        <div class="task-title" :title="task.name">{{ task.name || '未命名任务' }}</div>
        <div class="task-sub" v-if="task.className">
          <el-icon class="sub-icon"><UserFilled /></el-icon>
          <span>{{ task.className }}</span>
        </div>
      </div>

      <div class="task-desc" :title="task.content">
        {{ task.content ? task.content : '暂无内容描述' }}
      </div>

      <div class="chips" v-if="task.teacherName">
        <span class="chip">{{ task.teacherName }}</span>
      </div>

      <div class="card-actions">
        <el-button
          class="action-primary"
          type="primary"
          size="small"
          round
          @click="$emit('view-task', task)"
        >
          <el-icon><View /></el-icon>
          {{ canManage ? '查看 / 编辑' : '查看' }}
        </el-button>
        <el-tooltip v-if="canManage" content="删除任务" placement="top" :show-after="200">
          <el-button class="action-danger" type="danger" plain size="small" circle @click.stop="$emit('delete-task', task.id)">
            <el-icon><Delete /></el-icon>
          </el-button>
        </el-tooltip>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { View, Delete, UserFilled, Document, Notebook, Monitor } from '@element-plus/icons-vue'
import { resolveAvatarUrl } from '@/utils/appConfig'

const props = defineProps({
  task: {
    type: Object,
    required: true,
  },
  /** 是否可编辑、删除（教师为 true；仅浏览时为 false） */
  canManage: {
    type: Boolean,
    default: true,
  },
})

defineEmits(['view-task', 'delete-task'])

const typeLabel = computed(() => {
  if (props.task?.lab === 1) return '课后作业'
  if (props.task?.lab === 2) return '实验作业'
  return '任务'
})

const typeIcon = computed(() => {
  if (props.task?.lab === 1) return Document
  if (props.task?.lab === 2) return Monitor
  return Notebook
})

const typeClass = computed(() => (props.task?.lab === 2 ? 'is-lab' : 'is-homework'))

const coverUrl = computed(() => {
  const raw = props.task?.cover || props.task?.image || props.task?.img || ''
  return resolveAvatarUrl(raw)
})
</script>

<style scoped>
.task-card {
  position: relative;
  overflow: hidden;
  border-radius: 12px;
  border: 1px solid rgba(20, 80, 170, 0.1);
  background: #fff;
  box-shadow:
    0 1px 2px rgba(16, 24, 40, 0.04),
    0 8px 22px rgba(20, 63, 140, 0.06);
  transition: transform 0.22s ease, box-shadow 0.22s ease, border-color 0.22s ease;
  display: flex;
  flex-direction: column;
  min-height: 0;
  max-width: 100%;
}

.task-card:hover {
  transform: translateY(-2px);
  border-color: rgba(64, 158, 255, 0.28);
  box-shadow:
    0 4px 10px rgba(64, 158, 255, 0.1),
    0 14px 32px rgba(31, 45, 61, 0.08);
}

.task-card.is-lab:hover {
  border-color: rgba(13, 148, 136, 0.3);
  box-shadow:
    0 4px 10px rgba(13, 148, 136, 0.08),
    0 14px 32px rgba(31, 45, 61, 0.08);
}

/* 顶部封面：固定高度使卡片更紧凑 */
.card-thumb {
  position: relative;
  height: 112px;
  flex-shrink: 0;
  overflow: hidden;
  background: linear-gradient(145deg, #e8f1ff 0%, #f0f4ff 50%, #e0f2fe 100%);
}

.task-card.is-lab .card-thumb {
  background: linear-gradient(145deg, #ecfeff 0%, #e0fdfa 45%, #cffafe 100%);
}

.thumb-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
  transform: scale(1.01);
  transition: transform 0.35s ease;
}

.task-card:hover .thumb-img {
  transform: scale(1.05);
}

.thumb-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 16px;
}

.thumb-placeholder-inner {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  padding: 12px 16px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.55);
  border: 1px dashed rgba(64, 158, 255, 0.35);
  backdrop-filter: blur(6px);
}

.task-card.is-lab .thumb-placeholder-inner {
  border-color: rgba(13, 148, 136, 0.35);
}

.thumb-ph-icon {
  font-size: 36px;
  color: rgba(47, 107, 255, 0.85);
}

.task-card.is-lab .thumb-ph-icon {
  color: rgba(13, 148, 136, 0.9);
}

.thumb-ph-label {
  font-size: 12px;
  font-weight: 700;
  color: #175cd3;
  letter-spacing: 0.02em;
}

.task-card.is-lab .thumb-ph-label {
  color: #0f766e;
}

.thumb-overlay {
  position: absolute;
  inset: 0;
  pointer-events: none;
  background: linear-gradient(
    180deg,
    rgba(15, 23, 42, 0) 40%,
    rgba(15, 23, 42, 0.35) 100%
  );
  opacity: 0.85;
}

.thumb-badge {
  position: absolute;
  left: 8px;
  bottom: 8px;
  z-index: 1;
}

.task-type-tag {
  border-radius: 999px;
  padding: 0 8px;
  font-weight: 600;
  letter-spacing: 0.02em;
  border: none;
  box-shadow: 0 4px 14px rgba(0, 0, 0, 0.18);
}

/* 正文区 */
.card-body {
  padding: 10px 12px 12px;
  display: flex;
  flex-direction: column;
  gap: 8px;
  flex: 1;
  min-height: 0;
}

.body-top {
  display: flex;
  flex-direction: column;
  gap: 6px;
  min-width: 0;
}

.task-title {
  margin: 0;
  font-size: 16px;
  font-weight: 800;
  color: #0f172a;
  line-height: 1.35;
  letter-spacing: -0.01em;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  line-clamp: 2;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.task-sub {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #64748b;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.sub-icon {
  font-size: 14px;
  color: #94a3b8;
  flex-shrink: 0;
}

.task-desc {
  margin: 0;
  font-size: 12px;
  line-height: 1.55;
  color: #475569;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  line-clamp: 2;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  flex: 1;
  min-height: 2.8em;
}

.chips {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.chip {
  font-size: 11px;
  color: #475569;
  background: #f1f5f9;
  border: 1px solid rgba(15, 23, 42, 0.06);
  border-radius: 999px;
  padding: 4px 10px;
  font-weight: 600;
}

.chip-soft {
  background: linear-gradient(135deg, rgba(64, 158, 255, 0.12), rgba(99, 102, 241, 0.08));
  border-color: rgba(64, 158, 255, 0.2);
  color: #1d4ed8;
}

.task-card.is-lab .chip-soft {
  background: linear-gradient(135deg, rgba(13, 148, 136, 0.14), rgba(34, 211, 238, 0.1));
  border-color: rgba(13, 148, 136, 0.22);
  color: #0f766e;
}

.card-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 2px;
  padding-top: 8px;
  border-top: 1px solid rgba(15, 23, 42, 0.06);
}

.action-primary {
  flex: 1;
  font-weight: 700;
  box-shadow: 0 4px 14px rgba(64, 158, 255, 0.28);
}

.action-primary :deep(.el-icon) {
  margin-right: 4px;
}

.action-danger {
  flex-shrink: 0;
}
</style>
