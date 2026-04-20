<template>
  <aside class="app-sidebar" aria-label="主导航">
    <el-menu
      router
      :default-active="route.path"
      :default-openeds="['/home', '2']"
    >
      <el-menu-item index="/home">
        <el-icon><House /></el-icon>
        <span>系统首页</span>
      </el-menu-item>

      <el-menu-item index="/dashboard" v-if="isAdmin || isTeacher || isStudent">
        <el-icon><Promotion /></el-icon>
        <span>总览驾驶舱</span>
      </el-menu-item>

      <el-sub-menu index="3" v-if="isAdmin">
        <template #title>
          <el-icon><Management /></el-icon>
          <span class="sidebar-group-title">信息管理</span>
        </template>
        <el-menu-item index="/notice" v-if="isAdmin"><el-icon><Bell /></el-icon><span>公告信息</span></el-menu-item>
        <el-menu-item index="/college"><el-icon><OfficeBuilding /></el-icon><span>学院与专业</span></el-menu-item>
        <el-menu-item index="/clazz" v-if="isAdmin"><el-icon><School /></el-icon><span>班级信息</span></el-menu-item>
        <el-menu-item index="/course" v-if="isAdmin"><el-icon><Reading /></el-icon><span>课程管理</span></el-menu-item>
      </el-sub-menu>

      <el-menu-item index="/course" v-if="isTeacher"><el-icon><Reading /></el-icon><span>我教的课</span></el-menu-item>
      <el-menu-item index="/course" v-if="isStudent"><el-icon><Reading /></el-icon><span>我学的课</span></el-menu-item>

      <!-- 已移除：统计页面入口（教师统计） -->
      <el-menu-item index="/StuCourse" v-if="isStudent"><el-icon><Tickets /></el-icon><span>课程列表</span></el-menu-item>
<!--      <el-menu-item v-if="data.user.role ==='TEACHER'" index="/course"><el-icon><User /></el-icon><span>发布选课</span></el-menu-item>-->
<!--      <el-menu-item index="/work"><el-icon><DocumentCopy /></el-icon><span>文档下载</span></el-menu-item>-->


      <el-sub-menu index="2" v-if="isAdmin">
        <template #title>
          <el-icon><Avatar /></el-icon>
          <span class="sidebar-group-title">用户管理</span>
        </template>
        <el-menu-item index="/admin"><el-icon><User /></el-icon><span>管理员信息</span></el-menu-item>
        <el-menu-item index="/teacher"><el-icon><User /></el-icon><span>教师信息</span></el-menu-item>
        <el-menu-item index="/student"><el-icon><User /></el-icon><span>学生信息</span></el-menu-item>
      </el-sub-menu>
      <el-menu-item index="/resource">
        <el-icon><FolderAdd /></el-icon>
        <span>资源</span>
      </el-menu-item>
      <el-menu-item index="/account">
        <el-icon><User /></el-icon>
        <span>账户管理</span>
      </el-menu-item>
      <el-menu-item index="login" @click="logout">
        <el-icon><SwitchButton /></el-icon>
        <span>退出系统</span>
      </el-menu-item>
      
    </el-menu>
  </aside>
</template>

<script setup>
import { reactive, computed, onMounted } from 'vue'
import { ElMessage } from "element-plus";
import router from "@/router";
import { useRoute } from 'vue-router';
import {
  House,
  Promotion,
  Bell,
  OfficeBuilding,
  School,
  Reading,
  User,
  Avatar,
  SwitchButton,
  FolderAdd,
  Tickets,
  Management,
} from '@element-plus/icons-vue'

const route = useRoute();

const data = reactive({
  user: JSON.parse(localStorage.getItem('system-user') || '{}'),
})

/** 与登录页一致的大写角色；库中 role 为空时登录接口会补全为 STUDENT */
const userRole = computed(() => {
  const r = data.user?.role
  if (r == null || String(r).trim() === '') return ''
  return String(r).trim().toUpperCase()
})

const isAdmin = computed(() => userRole.value === 'ADMIN')
const isTeacher = computed(() => userRole.value === 'TEACHER')
const isStudent = computed(() => userRole.value === 'STUDENT')

if (!data.user?.id) {
  ElMessage.error('请登录！');
  router.push('/login');
}

onMounted(() => {
  console.log('Data:', data); // 打印 data 对象
  if (!data.user?.id) {
    ElMessage.error('请登录！');
    router.push('/login');
  }
});

const logout = () => {
  ElMessage.success('退出成功');
  localStorage.removeItem('system-user');
  router.push('/login');
};
</script>

<style scoped>
.app-sidebar :deep(.el-sub-menu__title .sidebar-group-title) {
  font-size: 14px;
  font-weight: 500;
  color: var(--color-text);
  letter-spacing: 0.01em;
}

.app-sidebar :deep(.el-sub-menu__title:hover .sidebar-group-title),
.app-sidebar :deep(.el-sub-menu.is-active > .el-sub-menu__title .sidebar-group-title) {
  color: var(--color-primary-hover);
}
</style>
