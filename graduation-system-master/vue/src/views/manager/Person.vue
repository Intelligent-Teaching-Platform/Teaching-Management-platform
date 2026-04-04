<template>
  <div class="person-panel">
    <div class="person-avatar-block">
      <el-upload
        :show-file-list="false"
        class="avatar-uploader"
        :action="uploadUrl"
        :on-success="handleFileUpload"
      >
        <img v-if="data.user.avatar" :src="resolveAvatarUrl(data.user.avatar)" class="avatar" alt="" />
        <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
      </el-upload>
      <p class="person-avatar-hint">点击上传头像，支持常见图片格式</p>
    </div>

    <el-form :model="data.user" label-width="100px" class="person-form">
      <el-form-item label="账号">
        <el-input v-model="data.user.username" disabled autocomplete="off" />
      </el-form-item>
      <el-form-item label="名称">
        <el-input v-model="data.user.name" autocomplete="off" />
      </el-form-item>
      <div class="person-actions">
        <el-button type="primary" @click="save">保 存</el-button>
      </div>
    </el-form>
  </div>
</template>

<script setup>
import { reactive } from 'vue'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getUploadUrl, resolveAvatarUrl } from '@/utils/appConfig'

const uploadUrl = getUploadUrl()

const data = reactive({
  user: JSON.parse(localStorage.getItem('system-user') || '{}'),
})

const handleFileUpload = (response) => {
  const url = response?.data ?? response
  if (url) data.user.avatar = typeof url === 'string' ? url : String(url)
}

const emit = defineEmits(['updateUser'])

const save = () => {
  if (data.user.role === 'ADMIN') {
    request.put('/admin/update', data.user).then((res) => {
      if (res.code === '200') {
        ElMessage.success('更新成功')
        localStorage.setItem('system-user', JSON.stringify(data.user))
        emit('updateUser')
      } else {
        ElMessage.error(res.msg)
      }
    })
  }
}
</script>

<style scoped>
.person-panel {
  font-family: var(--font-sans);
  max-width: min(480px, 100%);
  margin: 0 auto;
  padding: 8px 0 4px;
}

.person-avatar-block {
  text-align: center;
  margin-bottom: 24px;
}

.person-avatar-hint {
  margin: 12px 0 0;
  font-size: 13px;
  color: var(--color-text-muted);
  line-height: 1.5;
}

.person-form {
  padding-right: 0;
}

.person-form :deep(.el-form-item__label) {
  color: var(--color-text-muted);
  font-weight: 500;
}

.person-actions {
  display: flex;
  justify-content: center;
  padding-top: 8px;
}

.person-actions :deep(.el-button--primary) {
  min-width: 120px;
  transition:
    transform var(--duration) var(--ease-out),
    box-shadow var(--duration) var(--ease-out);
}

.person-actions :deep(.el-button--primary:active) {
  transform: scale(0.98);
}

.avatar-uploader .avatar {
  width: 120px;
  height: 120px;
  display: block;
  object-fit: cover;
  border-radius: var(--radius-md);
}

.avatar-uploader :deep(.el-upload) {
  border: 1px dashed var(--color-border-strong);
  border-radius: var(--radius-md);
  cursor: pointer;
  position: relative;
  overflow: hidden;
  background: var(--color-bg-app);
  transition:
    border-color var(--duration) var(--ease-out),
    background-color var(--duration) var(--ease-out),
    box-shadow var(--duration) var(--ease-out);
}

.avatar-uploader :deep(.el-upload:hover) {
  border-color: var(--color-primary-muted);
  background: var(--color-primary-soft);
  box-shadow: var(--shadow-soft);
}

.avatar-uploader-icon {
  font-size: 28px;
  color: var(--color-text-subtle);
  width: 120px;
  height: 120px;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>
