<template>
  <div class="login-container">
<!--    <img src="@/assets/imgs/OIP-C.jpg"/>-->
    <div class="login-box">
      <h1 class="login-title">智辅实验管理平台</h1>

      <!-- Role selection buttons -->
      <div class="role-buttons">
        <div
            v-for="role in roles"
            :key="role.value"
            class="role-button"
            :class="{ 'active': data.form.role === role.value }"
            @click="data.form.role = role.value"
        >
          {{ role.label }}
        </div>
      </div>

      <el-form :model="data.form" ref="formRef" :rules="data.rules">
        <el-form-item prop="username">
          <el-input :prefix-icon="User" size="large" v-model="data.form.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input :prefix-icon="Lock" size="large" v-model="data.form.password" placeholder="输入密码" show-password />
        </el-form-item>
<!--        <el-form-item>-->
<!--          <div class="agreement">-->
<!--            <el-checkbox v-model="data.agreed">我已阅读《隐私政策》和《使用协议》</el-checkbox>-->
<!--          </div>-->
<!--        </el-form-item>-->
        <el-form-item>
          <el-button size="large" type="primary" style="width: 100%" @click="login">登 录</el-button>
        </el-form-item>
      </el-form>
      <div style="text-align: right;">
        还没有账号？请 <router-link to="/register">注册</router-link> |
        <a href="javascript:void(0)" @click="handleForgotPassword">忘记密码？</a>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from "vue";
import { User, Lock } from "@element-plus/icons-vue";
import request from "@/utils/request";
import {ElMessage} from "element-plus";
import router from "@/router";

const roles = [
  { value: 'TEACHER', label: '教师' },
  { value: 'ADMIN', label: '管理员' },
  { value: 'STUDENT', label: '学生' }
]

const data = reactive({
  form: { role: 'STUDENT' },
  agreed: false,
  rules: {
    username: [
      { required: true, message: '请输入用户名', trigger: 'blur' },
    ],
    password: [
      { required: true, message: '请输入密码', trigger: 'blur' },
    ],
  }
})

const formRef = ref()

const handleForgotPassword = () => {
  ElMessage.warning("联系管理员修改密码")
}

const login = () => {
  // if (!data.agreed) {
  //   ElMessage.warning("请先阅读并同意《隐私政策》和《使用协议》")
  //   return
  // }

  formRef.value.validate((valid => {
    if (valid) {
      request.post('/login', data.form).then(res => {
        if (res.code === '200') {
          ElMessage.success("登录成功")
          const u = { ...res.data }
          if (!u.role && data.form.role) {
            u.role = data.form.role
          }
          localStorage.setItem('system-user', JSON.stringify(u))
          router.push('/')
        } else {
          ElMessage.error(res.msg)
        }
      })
    }
  })).catch(error => {
    console.error(error)
  })
}
</script>

<style scoped>
.login-container {
  min-height: 100dvh;
  overflow: hidden;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 24px 16px;
  background-image: url('@/assets/imgs/home.png');
  background-size: cover;
  background-position: center;
  background-color: rgba(244, 245, 247, 0.88);
}

.login-box {
  width: 100%;
  max-width: 420px;
  padding: 44px 32px 40px;
  border-radius: var(--radius-lg, 16px);
  background: rgba(255, 255, 255, 0.78);
  border: 1px solid var(--color-border, rgba(15, 23, 42, 0.08));
  box-shadow: var(--shadow-soft, 0 12px 40px -18px rgba(15, 23, 42, 0.12));
  backdrop-filter: blur(14px);
  -webkit-backdrop-filter: blur(14px);
}

.login-title {
  margin: 0 0 28px;
  text-align: center;
  font-size: 1.35rem;
  font-weight: 700;
  letter-spacing: -0.02em;
  color: var(--color-text, #0f172a);
}

.role-buttons {
  display: flex;
  margin-bottom: 22px;
  gap: 10px;
}

.role-button {
  flex: 1;
  text-align: center;
  padding: 11px 8px;
  min-height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition:
    border-color 0.22s cubic-bezier(0.16, 1, 0.3, 1),
    background-color 0.22s cubic-bezier(0.16, 1, 0.3, 1),
    color 0.22s cubic-bezier(0.16, 1, 0.3, 1),
    transform 0.22s cubic-bezier(0.16, 1, 0.3, 1);
  border: 1px solid var(--color-border, rgba(15, 23, 42, 0.08));
  border-radius: var(--radius-sm, 8px);
  color: var(--color-text-muted, #64748b);
  background-color: rgba(255, 255, 255, 0.65);
}

.role-button:hover {
  color: var(--color-primary-hover, #0f766e);
  border-color: rgba(13, 148, 136, 0.35);
  background-color: var(--color-primary-soft, rgba(13, 148, 136, 0.1));
}

.role-button:active {
  transform: scale(0.98);
}

.role-button.active {
  color: var(--color-primary-hover, #0f766e);
  font-weight: 600;
  border-color: rgba(13, 148, 136, 0.45);
  background-color: var(--color-primary-muted, rgba(13, 148, 136, 0.16));
  box-shadow: inset 0 0 0 1px rgba(13, 148, 136, 0.12);
}

.agreement {
  font-size: 14px;
  color: var(--color-text-muted, #64748b);
}

.login-box :deep(a) {
  color: var(--color-primary-hover, #0f766e);
  font-weight: 500;
}
</style>