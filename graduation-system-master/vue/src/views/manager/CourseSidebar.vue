<template>
<div class ="courseSide" >
  <el-menu
      router
      class="el-menu-vertical-demo"
      default-active="activeIndex"
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
      <svg t="1742620291898" class="icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="3106" width="200" height="200"><path d="M0 0h1024v1024H0z" fill="#FFFFFF" p-id="3107"></path><path d="M398.647 803.9H511.43c109.525-6.293 180.183-54.067 201.822-77.167 83.103-83.103 77.167-158.339 77.167-201.822 0-110.851-94.975-219.63-249.309-189.95C400.513 362 365.011 485.338 363.033 554.59c-53.422 47.487-52.888 73.468-35.994 79.099 17.808 5.936 47.868 15.876 65.675 21.812l5.933 148.399z" fill="#3081FC" fill-opacity=".14" p-id="3108"></path><path d="M746.081 500.334v-1.075c-13.364-125.101-126.832-215.849-253.432-202.64-118.518 12.364-207.977 111.969-206.294 229.749v1.485C271.463 543.647 238 580.995 238 608.079c0.596 12.415 6.294 24.037 15.747 32.203 9.272 8.345 21.601 12.595 34.111 11.724l19.425 4.48 4.092 106.67c0.596 23.73 20.228 42.673 44.237 42.673h83.528c10.904-1.126 18.829-10.777 17.689-21.554-0.984-9.216-8.339-16.511-17.689-17.484h-83.528c-2.694-0.23-4.766-2.457-4.74-5.12L345.9 624.718l-51.153-11.315h-4.325a31.439 31.439 0 0 1-9.273-2.355c-3.03-1.28-3.03-1.715-3.03-2.764 9.946-22.271 24.683-42.136 43.176-58.237 5.05-4.864 7.096-12.006 5.387-18.764a39.128 39.128 0 0 0 0-4.915c-0.622-104.264 84.408-189.277 189.899-189.892 75.006-0.435 143.305 42.546 174.644 109.896 9.22 21.81 14.996 44.926 17.042 68.476 4.973 67.325-20.383 133.37-69.283 180.471a23.952 23.952 0 0 0 4.74 21.324c3.859 3.507 9.013 5.274 14.245 4.915a51.617 51.617 0 0 0 11.215-1.485 218.738 218.738 0 0 0 44.47-55.882c28.024-49.943 39.55-107.31 33.023-164.036l-0.648 0.205 0.052-0.026z" fill="#3081FC" p-id="3109"></path><path d="M608.598 454.256a21.746 21.746 0 0 0-14.452-4.48c-9.713-1.024-18.441 5.939-19.477 15.539a16.485 16.485 0 0 0 0.052 4.07v148.037a19.183 19.183 0 0 0 4.325 14.08 19.137 19.137 0 0 0 15.1 5.554c9.816 1.255 18.777-5.606 20.047-15.308a17.3 17.3 0 0 0 0-4.326V468.105a18.022 18.022 0 0 0-5.621-13.875l0.026 0.026z m-98.86 8.089a18.05 18.05 0 0 0-6.475-8.959c-8.237-4.915-18.545-4.915-26.755 0a17.005 17.005 0 0 0-6.475 8.959l-52.007 150.828a15.657 15.657 0 0 0 2.149 18.124c3.911 3.788 9.221 5.785 14.686 5.555 5.18 0.307 10.282-1.357 14.245-4.685 3.185-3.021 5.646-6.758 7.122-10.879l10.36-26.444h46.413l11.215 27.954a27.187 27.187 0 0 0 6.682 9.804c3.548 3.098 8.236 4.634 12.95 4.275h1.088c5.387 0.103 10.619-1.817 14.685-5.324a15.822 15.822 0 0 0 2.59-17.484l-52.447-151.673-0.026-0.051z m-43.176 98.35l23.31-46.717 23.31 46.717h-46.62z" fill="#FF7733" p-id="3110"></path></svg>
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
      <span>阅卷</span>
    </el-menu-item>
    <el-menu-item v-if="data.user.role === 'TEACHER'" :index="menuPath('/course/courseDetail/question')">
      <el-icon><Box /></el-icon>
      <span>题库</span>
    </el-menu-item>
  </el-menu>

</div>
</template>

<script setup>
import {ArrowLeftBold} from '@element-plus/icons-vue'
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
.courseSide {
  display:flex;
  .icon{
    width:30px;
    height:30px;
  }
}

</style>