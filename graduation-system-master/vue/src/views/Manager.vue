<template>
  <div>
    <div style="height: 60px; background-color: #fff; display: flex; align-items: center; border-bottom: 1px solid #ddd">
      <div style="flex: 1">
        <div style="padding-left: 20px; display: flex; align-items: center">
          <img src="@/assets/imgs/logo.png" alt="" style="width: 40px">
          <div style="font-weight: bold; font-size: 24px; margin-left: 5px">数智验舱--智辅实验管理平台</div>
        </div>
      </div>
      <div style="width: fit-content; padding-right: 10px; display: flex; align-items: center;">
        <img style="width: 40px; height: 40px; border-radius: 50%; object-fit: cover" :src="avatarSrc" alt="">
        <span style="margin-left: 5px">{{ data.user.name }}</span>
      </div>
    </div>
    <div style="display:flex">
        <component :is="currentSidebar" />
        <router-view @updateUser="updateUser"/>
    </div>
  </div>
</template>

<script setup>
import { reactive, computed, onMounted, ref } from 'vue'
import router from '@/router'
import { ElMessage } from 'element-plus'
import { useRoute } from 'vue-router'
import CourseSidebar from '@/views/manager/CourseSidebar.vue'
import DefaultSidebar from '@/views/manager/DefaultSidebar.vue'
import { resolveAvatarUrl } from '@/utils/appConfig'

const route = useRoute()

const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'
const avatarSrc = computed(() => resolveAvatarUrl(data.user?.avatar) || defaultAvatar)

const isCourseDetail = computed(() => {
  return route.path.startsWith('/course/courseDetail');
})
const comId = ref();

const currentSidebar = computed(() => {
    return isCourseDetail.value ? CourseSidebar : DefaultSidebar;
});


const data = reactive({
  user: JSON.parse(localStorage.getItem('system-user') || '{}')
})

if (!data.user?.id) {
  ElMessage.error('请登录！')
  router.push('/login')
}

 const updateUser = () => {
   data.user = JSON.parse(localStorage.getItem('system-user') || '{}')
 }

</script>

<style scoped>
.el-menu-item.is-active {
  background-color: #e0edfd !important;
}
.el-menu-item:hover {
  color: #1967e3;
}
th {
  color: #333;
}
</style>