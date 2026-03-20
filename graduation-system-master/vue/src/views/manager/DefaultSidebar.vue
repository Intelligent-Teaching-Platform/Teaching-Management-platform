<!-- src/components/DefaultSidebar.vue -->
<template>
  <div  style="width: 200px; border-right: 1px solid #ddd; min-height: calc(100vh - 60px)">
    <el-menu
        router
        style="border: none"
        :default-active="route.path"
        :default-openeds="['/home', '2']"
    >
      <el-menu-item index="/home">
        <el-icon><House /></el-icon>
        <span>系统首页</span>
      </el-menu-item>

      <el-menu-item index="/dashboard" v-if="data.user.role === 'ADMIN' || data.user.role === 'TEACHER'">
        <el-icon><Promotion /></el-icon>
        <span>总览驾驶舱</span>
      </el-menu-item>


      <el-sub-menu index="3" v-if="data.user.role === 'ADMIN'">
        <template #title>
          <el-icon></el-icon>
          <span>信息管理</span>
        </template>
        <el-menu-item index="/notice" v-if="data.user.role === 'ADMIN'"><el-icon><Bell /></el-icon><span>公告信息</span></el-menu-item>
        <el-menu-item index="/college"><el-icon><OfficeBuilding /></el-icon><span>学院信息</span></el-menu-item>
        <el-menu-item index="/speciality"><el-icon><School /></el-icon><span>专业信息</span></el-menu-item>
        <el-menu-item index="/clazz" v-if="data.user.role === 'ADMIN'"><el-icon><School /></el-icon><span>班级信息</span></el-menu-item>
        <el-menu-item index="/course" v-if="data.user.role === 'ADMIN'"><el-icon><Reading /></el-icon><span>课程管理</span></el-menu-item>
      </el-sub-menu>

      <el-menu-item index="/course" v-if="data.user.role === 'TEACHER'"><el-icon><Reading /></el-icon><span>我教的课</span></el-menu-item>
<!--      <el-menu-item index="/Test" v-if="data.user.role === 'TEACHER'"><el-icon><Reading /></el-icon><span>测试</span></el-menu-item>-->
      <el-menu-item index="/course" v-if="data.user.role === 'STUDENT'"><el-icon><Reading /></el-icon><span>我学的课</span></el-menu-item>

      <!-- 已移除：统计页面入口（教师统计） -->
      <el-menu-item index="/StuCourse" v-if="data.user.role === 'STUDENT'"><el-icon><Tickets /></el-icon><span>课程列表</span></el-menu-item>
<!--      <el-menu-item v-if="data.user.role ==='TEACHER'" index="/course"><el-icon><User /></el-icon><span>发布选课</span></el-menu-item>-->
<!--      <el-menu-item index="/work"><el-icon><DocumentCopy /></el-icon><span>文档下载</span></el-menu-item>-->


      <el-sub-menu index="2" v-if="data.user.role === 'ADMIN'">
        <template #title>
          <el-icon><Avatar /></el-icon>
          <span>用户管理</span>
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
  </div>
</template>

<script setup>
import {reactive, computed, onMounted,watch} from "vue";
import { ElMessage } from "element-plus";
import router from "@/router";
import { useRoute } from 'vue-router';
import { HomeFilled, Promotion, Bell, OfficeBuilding, School, Reading, User, Notebook, DocumentCopy, Avatar, Lock, SwitchButton } from '@element-plus/icons-vue';

const route = useRoute();

const data = reactive({
  user: JSON.parse(localStorage.getItem('system-user') || '{}')
});

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
/* 可以在这里添加特定的样式 */
</style>
