<template>
  <div class="login-container">
<!--    <img src="@/assets/imgs/OIP-C.jpg"/>-->
    <div class="login-box">
      <div style="font-weight: bold; font-size: 24px; text-align: center; margin-bottom: 30px; color: #1450aa">智辅实验管理平台</div>

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
        <router-link to="/forgot-password">忘记密码？</router-link>
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
          // 先写入用户信息，避免 Manager.vue 先渲染时拿不到 system-user.id 而回跳登录页
          localStorage.setItem('system-user', JSON.stringify(res.data))
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
  height: 100vh;
  overflow:hidden;
  display: flex;
  justify-content: center;
  align-items: center;
  //background: linear-gradient(to top, #7f7fd5, #86a8e7, #91eae4);
  background-size: cover;
  /* 使用图片作为背景 */
  background-image: url('@/assets/imgs/home.png');
  /* 设置背景图片的大小和位置 */
  background-size: cover;
  background-position: center;
  /* 添加透明背景色 */
  background-color: rgba(255, 255, 255, 0.8);
}
.login-box {
  width: 400px;
  padding: 50px 30px;
  border-radius: 20px;
  box-shadow: 0 0 10px rgba(0, 0, 0,.1);

  background:#ffffff;
}

.role-buttons {
  display: flex;
  margin-bottom: 20px;
  gap: 10px;
}

.role-button {
  flex: 1;
  text-align: center;
  padding: 10px 0;
  cursor: pointer;
  transition: all 0.3s;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  color: #606266;
  background-color: #fff;
}

.role-button:hover {
  color: #1450aa;
  border-color: #c6e2ff;
  background-color: #ecf5ff;
}

.role-button.active {
  color: #1450aa;
  font-weight: bold;
  border-color: #1450aa;
  background-color: #ecf5ff;
}

.agreement {
  font-size: 14px;
  color: #666;
}

a {
  color: #1450aa;
  text-decoration: none;
}
</style>