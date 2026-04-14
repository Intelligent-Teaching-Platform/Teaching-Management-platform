<template>
  <div class="account-page">
    <header class="account-hero card">
      <div class="account-hero__icon" aria-hidden="true">
        <el-icon><UserFilled /></el-icon>
      </div>
      <div class="account-hero__text">
        <h1 class="account-hero__title">账号管理</h1>
        <p class="account-hero__sub">管理你的基本资料、安全信息和账号设置</p>
      </div>
    </header>

    <section class="account-panel card">
      <el-tabs v-model="activeTab" class="account-tabs">
        <el-tab-pane label="个人资料" name="basic">
          <component :is="currentComponent" @update-user="emit('updateUser')" />
        </el-tab-pane>

        <el-tab-pane label="密码管理" name="password">
          <component :is="Password" />
        </el-tab-pane>

        <el-tab-pane label="注销账号" name="close">
          <div class="danger-section">
            <h3 class="danger-section__title">注销账号</h3>
            <p class="danger-section__desc">账号注销后，相关数据会被清理且不可恢复，请谨慎操作。</p>
            <el-button type="danger" plain @click="onCloseAccount">申请注销账号</el-button>
          </div>
        </el-tab-pane>
      </el-tabs>
    </section>
  </div>
</template>

<script setup>
import { reactive, ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { UserFilled } from '@element-plus/icons-vue'
import router from '@/router'
import Person from './Person.vue'
import TPerson from './TPerson.vue'
import SPerson from './SPerson.vue'
import Password from './Password.vue'

const activeTab = ref('basic')
const emit = defineEmits(['updateUser'])

const rawUser = JSON.parse(localStorage.getItem('system-user') || '{}')
const user = reactive(JSON.parse(localStorage.getItem('system-user') || '{}'))

const currentComponent = computed(() => {
  if (user.role === 'ADMIN') return Person
  if (user.role === 'TEACHER') return TPerson
  if (user.role === 'STUDENT') return SPerson
  return SPerson
})

const onCloseAccount = () => {
  ElMessageBox.confirm('账号注销后将无法恢复，确定继续吗？', '确认注销', {
    type: 'warning',
    confirmButtonText: '确定',
    cancelButtonText: '取消',
  })
    .then(() => {
      ElMessage.info('已收到注销申请（示例占位，待接入后端实现）')
    })
    .catch(() => {})
}

onMounted(() => {
  if (!rawUser || !rawUser.id) {
    ElMessage.error('请先登录')
    router.push('/login')
  }
})
</script>

<style scoped>
.account-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
  flex: 1;
  min-width: 0;
  font-family: var(--font-sans);
}

.account-hero {
  display: flex;
  align-items: flex-start;
  gap: 14px;
  padding: 18px 20px;
}

.account-hero__icon {
  flex-shrink: 0;
  width: 44px;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
  background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-primary-hover) 100%);
  color: #fff;
  font-size: 22px;
  box-shadow: 0 8px 24px -8px rgba(13, 148, 136, 0.45);
}

.account-hero__text {
  min-width: 0;
}

.account-hero__title {
  margin: 0;
  font-size: 1.35rem;
  font-weight: 700;
  letter-spacing: -0.02em;
  color: var(--color-text);
}

.account-hero__sub {
  margin: 6px 0 0;
  font-size: 13px;
  color: var(--color-text-muted);
  line-height: 1.5;
  max-width: 52ch;
}

.account-panel {
  padding: 8px 12px 20px;
  max-width: min(720px, 100%);
}

.account-tabs :deep(.el-tabs__header) {
  margin-bottom: 8px;
}

.account-tabs :deep(.el-tabs__nav-wrap::after) {
  height: 1px;
  background-color: var(--color-border);
}

.account-tabs :deep(.el-tabs__item) {
  font-weight: 500;
  color: var(--color-text-muted);
  transition: color var(--duration) var(--ease-out);
}

.account-tabs :deep(.el-tabs__item:hover) {
  color: var(--color-primary-hover);
}

.account-tabs :deep(.el-tabs__item.is-active) {
  color: var(--color-primary-hover);
  font-weight: 600;
}

.account-tabs :deep(.el-tabs__active-bar) {
  background-color: var(--color-primary);
  border-radius: 2px;
}

.danger-section {
  padding: 12px 8px 8px;
}

.danger-section__title {
  margin: 0 0 8px;
  font-size: 1rem;
  font-weight: 700;
  color: var(--el-color-danger);
}

.danger-section__desc {
  margin: 0 0 16px;
  font-size: 13px;
  line-height: 1.55;
  color: var(--color-text-muted);
  max-width: 56ch;
}

.danger-section :deep(.el-button--danger.is-plain) {
  transition:
    transform var(--duration) var(--ease-out),
    box-shadow var(--duration) var(--ease-out);
}

.danger-section :deep(.el-button--danger.is-plain:active) {
  transform: scale(0.99);
}
</style>
