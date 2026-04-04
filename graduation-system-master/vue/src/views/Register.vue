<template>
  <div class="login-container">
    <div class="login-box">
      <div class="page-title">智辅实验管理平台</div>
      <div class="page-subtitle">欢迎注册</div>
      <div class="register-hint">注册为学生账号，注册成功后请使用「学生」身份登录</div>
      <el-form :model="data.form" ref="formRef" :rules="data.rules">
        <el-form-item prop="username">
          <el-input :prefix-icon="User" size="large" v-model="data.form.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input :prefix-icon="Lock" size="large" v-model="data.form.password" placeholder="输入密码" show-password />
        </el-form-item>
        <el-form-item prop="confirmPassword">
          <el-input :prefix-icon="Lock" size="large" v-model="data.form.confirmPassword" placeholder="请再次输入密码" show-password />
        </el-form-item>
        <el-form-item>
          <el-button size="large" type="primary" style="width: 100%" @click="register">注 册</el-button>
        </el-form-item>
      </el-form>
      <div class="form-footer">
        已有账号？请 <router-link to="/login">登录</router-link>
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

  const validatePass = (rule, value, callback) => {
    if (!value) {
      callback(new Error('请确认密码'))
    } else if (value !== data.form.password) {
      callback(new Error('两次输入密码不一致'))
    } else {
      callback()
    }
  }

  const data = reactive({
    form: { role: 'STUDENT' },
    rules: {
      username: [
        { required: true, message: '请输入用户名', trigger: 'blur' },
      ],
      password: [
        { required: true, message: '请输入密码', trigger: 'blur' },
      ],
      confirmPassword: [
        { validator: validatePass, trigger: 'blur' },
      ],
    }
  })


  const formRef = ref()

  // 点击注册按钮的时候会触发这个方法
  const register = () => {
    formRef.value.validate((valid => {
      if (valid) {
        // 调用后台的接口
        request.post('/register', data.form).then(res => {
          if (res.code === '200') {
            ElMessage.success("注册成功")
            router.push('/login')
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

.page-title {
  font-weight: 700;
  font-size: 1.35rem;
  letter-spacing: -0.02em;
  text-align: center;
  margin: 0 0 8px;
  color: var(--color-text, #0f172a);
}

.page-subtitle {
  text-align: center;
  font-size: 1rem;
  font-weight: 600;
  color: var(--color-primary-hover, #0f766e);
  margin: 0 0 10px;
}

.register-hint {
  text-align: center;
  font-size: 13px;
  color: var(--color-text-muted, #64748b);
  line-height: 1.5;
  margin-bottom: 22px;
  padding: 0 4px;
}

.form-footer {
  text-align: right;
  font-size: 14px;
  color: var(--color-text-muted, #64748b);
}

.login-box :deep(a) {
  color: var(--color-primary-hover, #0f766e);
  font-weight: 500;
}
</style>