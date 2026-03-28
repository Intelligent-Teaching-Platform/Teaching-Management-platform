<template>
  <div class="homework-shell">
    <div class="homework-topbar">
      <div class="homework-title">
        <div class="homework-title-main"></div>
      </div>
      <el-radio-group v-model="active" size="default" class="homework-switch" @change="onChange">
        <el-radio-button label="courseware">课后作业</el-radio-button>
        <el-radio-button label="work">实验作业</el-radio-button>
      </el-radio-group>
    </div>

    <router-view />
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()

const active = computed({
  get() {
    // 兼容直接访问 /homework 或其它未知子路由时的兜底
    const p = String(route.path || '')
    if (p.endsWith('/work')) return 'work'
    return 'courseware'
  },
  set() {},
})

function onChange(val) {
  // 统一入口固定在 /course/courseDetail/homework/<child>
  const base = '/course/courseDetail/homework'
  router.push({ path: `${base}/${val}`, query: route.query })
}
</script>

<style scoped>
.homework-shell {
  min-height: calc(100vh - 60px);
  box-sizing: border-box;
  padding: 18px 24px;
  background: linear-gradient(135deg, #f5f7fa 0%, #eef3ff 45%, #f5f7fa 100%);
}

.homework-topbar {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 10px;
  margin-bottom: 12px;
}

.homework-title {
  display: flex;
  flex-direction: column;
}

.homework-title-main {
  font-size: 22px;
  font-weight: 700;
  color: #1f2d3d;
  letter-spacing: 0.5px;
}

/* 顶部切换：更显眼的胶囊按钮（替代 el-segmented / tabs） */
.homework-switch {
  background: rgba(255, 255, 255, 0.9);
  padding: 6px;
  border-radius: 999px;
  box-shadow: 0 10px 24px rgba(31, 45, 61, 0.08);
  border: 1px solid rgba(220, 223, 230, 0.7);
}

.homework-switch :deep(.el-radio-button__inner) {
  border: none;
  border-radius: 999px !important;
  padding: 8px 16px;
  font-weight: 700;
  color: #344054;
  background: transparent;
}

.homework-switch :deep(.el-radio-button__original-radio:checked + .el-radio-button__inner) {
  background: linear-gradient(135deg, #409eff 0%, #2f6bff 100%);
  color: #fff;
  box-shadow: 0 8px 16px rgba(47, 107, 255, 0.25);
}

/* -----------------------------
   统一子页面外壳（courseware / work）
   通过 :deep 覆盖 router-view 内部
------------------------------ */
:deep(.homework-page) {
  padding: 0 !important;
  background: transparent !important;
}

:deep(.homework-card) {
  border-radius: 14px;
  box-shadow: 0 12px 30px rgba(31, 45, 61, 0.08);
  border: 1px solid rgba(220, 223, 230, 0.7);
}

:deep(.homework-card .el-card__header) {
  padding: 18px 20px;
}

:deep(.homework-card .el-card__body) {
  padding: 0;
}

:deep(.homework-card .filter-container) {
  padding: 14px 20px;
  background-color: #fff;
  border-bottom: 1px solid #ebeef5;
}

:deep(.homework-card .el-table) {
  border-radius: 0 0 14px 14px;
}
</style>

