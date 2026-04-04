<template>
  <aside class="app-sidebar course-sidebar" aria-label="课程导航">
  <el-menu
      router
      class="el-menu-vertical-demo"
      :default-active="activeIndex"
      @open="handleOpen"
      @close="handleClose"
  >
    <el-menu-item >
      <div @click="goBack " class="flex-box">
        <el-icon class="header-left" size="100">
          <ArrowLeftBold />
        </el-icon>
        <span class="title"> {{ route.query.courseName}}</span>
      </div>
    </el-menu-item>
    <el-menu-item :index="menuPath('/course/courseDetail/ai')">
      <el-icon><MagicStick /></el-icon>
      <span>AI助教</span>
    </el-menu-item>
    <el-menu-item :index="menuPath('/course/courseDetail/task')" v-if="data.user.role === 'TEACHER'">
      <el-icon><List /></el-icon>
      <span>发布任务</span>
    </el-menu-item>
    <el-menu-item :index="menuPath('/course/courseDetail/class')">
      <el-icon><Menu /></el-icon>
      <span>班级活动</span>
    </el-menu-item>
    <el-menu-item :index="menuPath('/course/courseDetail/material')">
      <el-icon><Notebook /></el-icon>
      <span>课程资料</span>
    </el-menu-item>
   <!-- <el-menu-item :index="`/course/courseDetail/chapter?courseName=${route.query.courseName}`">
     <el-icon><DocumentChecked /></el-icon>
     <span>章节</span>
   </el-menu-item> -->
    <el-menu-item :index="menuPath('/course/courseDetail/homework')">
      <el-icon><Tickets /></el-icon>
      <span>作业</span>
    </el-menu-item>
    <!-- <el-menu-item :index="`/course/courseDetail/test?courseName=${route.query.courseName}`">
     <el-icon><ChatLineRound /></el-icon>
     <span>讨论</span>
    </el-menu-item> -->

    <el-menu-item v-if="data.user.role === 'STUDENT'" :index="menuPath('/course/courseDetail/exam')">
      <el-icon><Tickets /></el-icon>
      <span>考试</span>
    </el-menu-item>
    <el-menu-item v-if="data.user.role === 'STUDENT'" :index="menuPath('/course/courseDetail/score')">
      <el-icon><Tickets /></el-icon>
      <span>试卷</span>
    </el-menu-item>

    <el-menu-item v-if="data.user.role === 'TEACHER'" :index="menuPath('/course/courseDetail/testPaper')">
      <el-icon><Document /></el-icon>
      <span>出卷</span>
    </el-menu-item>
    <el-menu-item v-if="data.user.role === 'TEACHER'" :index="menuPath('/course/courseDetail/scoreView')">
      <el-icon><Document /></el-icon>
      <span>批改</span>
    </el-menu-item>
    <el-menu-item v-if="data.user.role === 'TEACHER'" :index="menuPath('/course/courseDetail/question')">
      <el-icon><Box /></el-icon>
      <span>题库</span>
    </el-menu-item>
  </el-menu>
  </aside>
</template>

<script setup>
import {
  ArrowLeftBold,
  MagicStick,
  List,
  Menu,
  Notebook,
  Tickets,
  Document,
  Box,
} from '@element-plus/icons-vue'
import {onMounted, reactive, ref, watch} from 'vue'
import router from "@/router";
import {useRoute} from "vue-router";
const route = useRoute();
const activeIndex = ref(' ');
const data = reactive({
  user: JSON.parse(localStorage.getItem('system-user') || '{}')
});
onMounted(() => {
  console.log('CourseSidebar.vue has been mounted');
  // 自动导航到AI工作台
  router.push({ path: '/course/courseDetail/ai', query: { id: route.query.id, courseName: route.query.courseName } });
  setActiveIndex();
  console.log('activeIndex:', activeIndex.value);
})
watch(
    ()=>route.path,
    ()=>{
      setActiveIndex();
    }
);
function menuPath(path) {
  const q = new URLSearchParams();
  const id = route.query.id ?? route.params.id;
  if (id != null) q.set('id', String(id));
  if (route.query.courseName) q.set('courseName', route.query.courseName);
  const query = q.toString();
  return query ? `${path}?${query}` : path;
}

const setActiveIndex = () => {
  const path = route.path;
  const courseName = route.query.courseName;
  if (path.includes('/course/courseDetail')) {
    const rid = route.query.id ?? route.params.id;
    activeIndex.value = rid != null ? `${path}?id=${rid}&courseName=${courseName || ''}` : `${path}?courseName=${courseName || ''}`;
  } else {
    activeIndex.value = path;
  }
}
const goBack = () => {
  router.push({ path: '/course',query: { courseName: route.query.courseName } });
}

const handleOpen = () => {
}
const handleClose = () => {
}

</script>

<style scoped>

.flex-box {
  display:flex;
  align-items:center;
  height:100%;
}
.title{
  align-items:center;
  justify-content: center;
}
.course-sidebar {
  display: flex;
}

</style>