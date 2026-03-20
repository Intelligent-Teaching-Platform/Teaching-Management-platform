<template>
  <div class="account-page">
    <!-- 头部标题 -->
    <header class="account-header">
      <div class="account-icon">
        <el-icon><UserFilled /></el-icon>
      </div>
      <div class="account-title-wrap">
        <h1 class="account-title">账号管理</h1>
        <p class="account-subtitle">管理你的基本资料、安全信息和账号设置</p>
      </div>
    </header>

    <!-- 主体内容 -->
    <el-card class="account-card" shadow="never">
      <el-tabs v-model="activeTab" class="account-tabs">
        <!-- 基本资料 -->
        <el-tab-pane label="个人资料" name="basic">
            <component :is="currentComponent" @updateUser="emit('updateUser')"></component>
        </el-tab-pane>

        <!-- 密码管理 -->
        <el-tab-pane label="密码管理" name="password">
          <component :is="Password"></component>
        </el-tab-pane>

        <!--  -->
        <el-tab-pane label="注销账号" name="close">
          <div class="danger-section">
            <h3>注销账号</h3>
            <p>账号注销后，相关数据会被清理且不可恢复，请谨慎操作。</p>
            <el-button type="danger" plain @click="onCloseAccount">
              申请注销账号
            </el-button>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref, computed, onMounted } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import { UserFilled } from "@element-plus/icons-vue";
import router from "@/router";
import Person from "./Person.vue";
import TPerson from "./TPerson.vue";
import SPerson from "./SPerson.vue";
import Password from "./Password.vue";
import { defineEmits } from "vue";

const activeTab = ref("basic");
const emit = defineEmits(['updateUser'])
const rawUser = JSON.parse(localStorage.getItem("system-user") || "{}");
const user = reactive(JSON.parse(localStorage.getItem("system-user") || "{}"));
const currentComponent = computed(() => {
  if (user.role === "ADMIN") return Person;
  if (user.role === "TEACHER") return TPerson;
  if (user.role === "STUDENT") return SPerson;
  return SPerson;
});

const goPasswordPage = () => {
  router.push("/password");
};

const onCloseAccount = () => {
  ElMessageBox.confirm("账号注销后将无法恢复，确定继续吗？", "确认注销", {
    type: "warning",
    confirmButtonText: "确定",
    cancelButtonText: "取消",
  })
    .then(() => {
      ElMessage.info("已收到注销申请（示例占位，待接入后端实现）");
    })
    .catch(() => {});
};

onMounted(() => {
  if (!rawUser || !rawUser.id) {
    ElMessage.error("请先登录");
    router.push("/login");
  }
});
</script>

<style scoped>
.account-page {
  padding: 16px 20px;
  background-color: #f5f7fb;
  min-height: 100%;
  box-sizing: border-box;
  min-width:100%;
}

.account-header {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
}

.account-icon {
  width: 40px;
  height: 40px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 10px;
  background: linear-gradient(135deg, #4e8dff, #1f66ff);
  color: #fff;
  font-size: 20px;
}

.account-title-wrap {
  display: flex;
  flex-direction: column;
}

.account-title {
  font-size: 18px;
  font-weight: 600;
  margin: 0;
  color: #1f2937;
}

.account-subtitle {
  margin: 2px 0 0;
  font-size: 12px;
  color: #6b7280;
}

.account-card {
  border-radius: 14px;
  width:60%;
}

.account-tabs {
  --el-color-primary: #4e8dff;
}


.password-section {
  padding: 12px 0;
}

.password-tip {
  font-size: 13px;
  color: #6b7280;
  margin-bottom: 12px;
}

.danger-section {
  padding: 12px 0;
}

.danger-section h3 {
  margin: 0 0 6px;
  font-size: 15px;
  color: #b91c1c;
}

.danger-section p {
  margin: 0 0 12px;
  font-size: 13px;
  color: #6b7280;
}
</style>
