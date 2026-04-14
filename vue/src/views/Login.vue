<template>
  <div class="login-page">
    <div class="login-backdrop" aria-hidden="true" />

    <main class="login-main">
      <div class="login-card">
        <header class="login-head">
          <div class="login-brand">
            <img src="@/assets/imgs/logo.png" alt="" class="login-logo" width="40" height="40" />
            <div class="login-brand-text">
              <p class="login-eyebrow">数智验舱</p>
              <h1 class="login-title">智辅实验管理平台</h1>
            </div>
          </div>
          <p class="login-subtitle">使用校内账号登录，请选择身份后输入密码</p>
        </header>

        <div
          class="role-segment"
          role="tablist"
          aria-label="登录身份"
        >
          <button
            v-for="role in roles"
            :key="role.value"
            type="button"
            role="tab"
            class="role-tab"
            :class="{ 'is-active': data.form.role === role.value }"
            :aria-selected="data.form.role === role.value"
            @click="data.form.role = role.value"
          >
            {{ role.label }}
          </button>
        </div>

        <el-form
          class="login-form"
          :model="data.form"
          ref="formRef"
          :rules="data.rules"
          label-position="top"
          @submit.prevent="login"
        >
          <el-form-item prop="username" label="用户名">
            <el-input
              :prefix-icon="User"
              size="large"
              v-model="data.form.username"
              placeholder="请输入用户名"
              autocomplete="username"
            />
          </el-form-item>
          <el-form-item prop="password" label="密码">
            <el-input
              :prefix-icon="Lock"
              size="large"
              v-model="data.form.password"
              placeholder="请输入密码"
              show-password
              autocomplete="current-password"
              @keyup.enter="login"
            />
          </el-form-item>
          <el-form-item class="login-form-actions">
            <el-button
              class="login-submit"
              size="large"
              type="primary"
              native-type="submit"
            >
              登录
            </el-button>
          </el-form-item>
        </el-form>

        <footer class="login-foot">
          <span class="login-foot-hint">还没有账号？</span>
          <router-link class="login-link" to="/register">注册</router-link>
          <span class="login-foot-dot" aria-hidden="true" />
          <button type="button" class="login-link login-link--btn" @click="handleForgotPassword">
            忘记密码
          </button>
        </footer>
      </div>
    </main>
  </div>
</template>

<script setup>
import { reactive, ref } from "vue";
import { User, Lock } from "@element-plus/icons-vue";
import request from "@/utils/request";
import { ElMessage } from "element-plus";
import router from "@/router";

const roles = [
  { value: 'TEACHER', label: '教师' },
  { value: 'ADMIN', label: '管理员' },
  { value: 'STUDENT', label: '学生' },
];

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
  },
});

const formRef = ref();

const handleForgotPassword = () => {
  ElMessage.warning("联系管理员修改密码");
};

const login = () => {
  formRef.value.validate((valid => {
    if (valid) {
      request.post('/login', data.form).then(res => {
        if (res.code === '200') {
          ElMessage.success("登录成功");
          const u = { ...res.data };
          if (!u.role && data.form.role) {
            u.role = data.form.role;
          }
          localStorage.setItem('system-user', JSON.stringify(u));
          router.push('/');
        } else {
          ElMessage.error(res.msg);
        }
      });
    }
  })).catch(error => {
    console.error(error);
  });
};
</script>

<style scoped>
.login-page {
  position: relative;
  min-height: 100dvh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: clamp(20px, 4vw, 40px) 16px;
  overflow-x: hidden;
  font-family: var(--font-sans);
}

.login-backdrop {
  position: fixed;
  inset: 0;
  z-index: 0;
  overflow: hidden;
  background-color: #0b1220;
  background-image:
    radial-gradient(ellipse 110% 85% at 10% -5%, rgba(45, 212, 191, 0.35), transparent 52%),
    radial-gradient(ellipse 90% 80% at 92% 8%, rgba(56, 189, 248, 0.28), transparent 48%),
    radial-gradient(ellipse 70% 55% at 50% 105%, rgba(13, 148, 136, 0.22), transparent 55%),
    radial-gradient(circle 700px at 30% 60%, rgba(99, 102, 241, 0.12), transparent 65%),
    url('@/assets/imgs/home.png');
  background-size:
    auto,
    auto,
    auto,
    auto,
    cover;
  background-position: center;
  background-repeat: no-repeat;
  filter: saturate(1.12) contrast(1.04);
}

/* 科技网格 + 微扫光 */
.login-backdrop::before {
  content: '';
  position: absolute;
  inset: 0;
  background-image:
    linear-gradient(rgba(94, 234, 212, 0.11) 1px, transparent 1px),
    linear-gradient(90deg, rgba(94, 234, 212, 0.09) 1px, transparent 1px),
    repeating-linear-gradient(
      -18deg,
      transparent,
      transparent 3px,
      rgba(56, 189, 248, 0.035) 3px,
      rgba(56, 189, 248, 0.035) 4px
    );
  background-size:
    52px 52px,
    52px 52px,
    100% 100%;
  background-position: 0 0, 0 0, 0 0;
  -webkit-mask-image: radial-gradient(ellipse 95% 90% at 50% 42%, #000 12%, transparent 72%);
  mask-image: radial-gradient(ellipse 95% 90% at 50% 42%, #000 12%, transparent 72%);
  pointer-events: none;
  animation: login-grid-drift 28s linear infinite;
}

.login-backdrop::after {
  content: '';
  position: absolute;
  inset: 0;
  pointer-events: none;
  background:
    radial-gradient(ellipse 120% 100% at 50% -10%, rgba(255, 255, 255, 0.22), transparent 48%),
    radial-gradient(ellipse 80% 70% at 50% 50%, rgba(248, 250, 252, 0.06) 0%, rgba(241, 245, 249, 0.72) 45%, rgba(226, 232, 240, 0.88) 100%),
    linear-gradient(125deg, rgba(13, 148, 136, 0.12) 0%, transparent 42%),
    linear-gradient(235deg, rgba(14, 165, 233, 0.1) 0%, transparent 45%),
    linear-gradient(180deg, rgba(15, 23, 42, 0.38) 0%, transparent 28%, transparent 72%, rgba(15, 23, 42, 0.28) 100%);
}

@keyframes login-grid-drift {
  0% {
    background-position: 0 0, 0 0, 0 0;
  }
  100% {
    background-position: 52px 52px, 52px 52px, 40px 0;
  }
}

@media (prefers-reduced-motion: reduce) {
  .login-backdrop::before {
    animation: none;
  }
}

.login-main {
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: 440px;
}

.login-card {
  position: relative;
  padding: clamp(32px, 5vw, 44px) clamp(24px, 4vw, 36px) 36px;
  border-radius: var(--radius-lg);
  background: var(--color-bg-elevated);
  border: 1px solid var(--color-border);
  box-shadow:
    var(--shadow-soft),
    0 0 0 1px rgba(255, 255, 255, 0.65) inset;
}

.login-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: var(--radius-lg);
  right: var(--radius-lg);
  height: 3px;
  border-radius: 0 0 3px 3px;
  background: linear-gradient(90deg, var(--color-primary) 0%, #14b8a6 50%, #0d9488 100%);
  opacity: 0.95;
}

.login-head {
  margin-bottom: 28px;
}

.login-brand {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-bottom: 12px;
}

.login-logo {
  flex-shrink: 0;
  width: 40px;
  height: 40px;
  object-fit: contain;
  border-radius: var(--radius-sm);
  border: 1px solid var(--color-border);
  background: #fff;
}

.login-brand-text {
  min-width: 0;
}

.login-eyebrow {
  margin: 0 0 2px;
  font-size: 0.75rem;
  font-weight: 600;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: var(--color-primary);
}

.login-title {
  margin: 0;
  font-size: clamp(1.2rem, 2.5vw, 1.45rem);
  font-weight: 800;
  letter-spacing: -0.03em;
  line-height: 1.25;
  color: var(--color-text);
}

.login-subtitle {
  margin: 0;
  font-size: 0.875rem;
  line-height: 1.55;
  color: var(--color-text-muted);
  max-width: 36ch;
}

.role-segment {
  display: flex;
  gap: 0;
  padding: 4px;
  margin-bottom: 24px;
  border-radius: var(--radius-md);
  background: rgba(15, 23, 42, 0.04);
  border: 1px solid var(--color-border);
}

.role-tab {
  flex: 1;
  min-height: 44px;
  margin: 0;
  padding: 10px 8px;
  border: none;
  border-radius: var(--radius-sm);
  background: transparent;
  font-family: inherit;
  font-size: 0.875rem;
  font-weight: 600;
  color: var(--color-text-muted);
  cursor: pointer;
  transition:
    background-color var(--duration) var(--ease-out),
    color var(--duration) var(--ease-out),
    box-shadow var(--duration) var(--ease-out);
}

.role-tab:hover {
  color: var(--color-text);
}

.role-tab:focus-visible {
  outline: 2px solid var(--color-primary);
  outline-offset: 2px;
}

.role-tab.is-active {
  background: var(--color-bg-elevated);
  color: var(--color-primary-hover);
  box-shadow: 0 1px 3px rgba(15, 23, 42, 0.08);
}

.login-form :deep(.el-form-item) {
  margin-bottom: 18px;
}

.login-form :deep(.el-form-item__label) {
  font-weight: 600;
  font-size: 0.8125rem;
  color: var(--color-text);
  margin-bottom: 6px;
}

/* 用户名、密码输入区：纯白底，与卡片区分 */
.login-form :deep(.el-input__wrapper) {
  background-color: #ffffff !important;
  box-shadow: 0 0 0 1px var(--color-border) inset !important;
}

.login-form :deep(.el-input__wrapper:hover) {
  background-color: #ffffff !important;
  box-shadow: 0 0 0 1px var(--color-border-strong) inset !important;
}

.login-form :deep(.el-input__wrapper.is-focus) {
  background-color: #ffffff !important;
  box-shadow:
    0 0 0 1px var(--color-primary) inset,
    0 0 0 3px var(--color-primary-soft) !important;
}

/* 覆盖 Chrome / Edge 自动填充淡蓝底（含 -internal-autofill-selected）→ 纯白 */
.login-form :deep(input:-webkit-autofill),
.login-form :deep(input:-webkit-autofill:hover),
.login-form :deep(input:-webkit-autofill:focus),
.login-form :deep(input:-webkit-autofill:active) {
  -webkit-box-shadow: 0 0 0 1000px #ffffff inset !important;
  box-shadow: 0 0 0 1000px #ffffff inset !important;
  -webkit-text-fill-color: var(--color-text) !important;
  caret-color: var(--color-text);
  transition: background-color 99999s ease-out 0s;
}

.login-form :deep(input:-internal-autofill-selected) {
  appearance: none !important;
  background-image: none !important;
  background-color: #ffffff !important;
  color: var(--color-text) !important;
}

.login-form :deep(input:autofill) {
  background-color: #ffffff !important;
}

.login-form-actions {
  margin-bottom: 0;
  margin-top: 8px;
}

.login-submit {
  width: 100%;
  height: 46px;
  font-weight: 700;
  letter-spacing: 0.02em;
  border-radius: var(--radius-sm);
}

.login-foot {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: center;
  gap: 6px 10px;
  margin-top: 24px;
  padding-top: 22px;
  border-top: 1px solid var(--color-border);
  font-size: 0.875rem;
  color: var(--color-text-muted);
}

.login-foot-hint {
  color: var(--color-text-subtle);
}

.login-foot-dot {
  width: 4px;
  height: 4px;
  border-radius: 50%;
  background: var(--color-border-strong);
  opacity: 0.8;
}

.login-link {
  color: var(--color-primary-hover);
  font-weight: 600;
  text-decoration: none;
  transition: color var(--duration) var(--ease-out);
}

.login-link:hover {
  color: var(--color-primary);
  text-decoration: underline;
}

.login-link--btn {
  padding: 0;
  border: none;
  background: none;
  font: inherit;
  cursor: pointer;
}

.login-link--btn:focus-visible {
  outline: 2px solid var(--color-primary);
  outline-offset: 2px;
  border-radius: 4px;
}

@media (prefers-reduced-motion: reduce) {
  .role-tab {
    transition: none;
  }
}
</style>
