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
  height: 100vh;
  overflow: hidden;
  display: flex;
  justify-content: center;
  align-items: center;
  background-size: cover;
  background-image: url('@/assets/imgs/home.png');
  background-position: center;
  background-color: rgba(255, 255, 255, 0.8);
}

.login-box {
  width: 400px;
  padding: 50px 30px;
  border-radius: 20px;
  box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
  background: #ffffff;
}

.page-title {
  font-weight: bold;
  font-size: 24px;
  text-align: center;
  margin-bottom: 8px;
  color: #1450aa;
}

.page-subtitle {
  text-align: center;
  font-size: 16px;
  font-weight: bold;
  color: #1450aa;
  margin-bottom: 10px;
}

.register-hint {
  text-align: center;
  font-size: 13px;
  color: #909399;
  line-height: 1.5;
  margin-bottom: 20px;
  padding: 0 4px;
}

.form-footer {
  text-align: right;
  font-size: 14px;
  color: #606266;
}

a {
  color: #1450aa;
  text-decoration: none;
}
</style>