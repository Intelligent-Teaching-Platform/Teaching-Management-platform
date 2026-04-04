<template>
  <div class="manager-layout">
    <header class="manager-header" role="banner">
      <div class="manager-header__brand">
        <img class="manager-header__logo" src="@/assets/imgs/logo.png" alt="平台标志">
        <div class="manager-header__titles">
          <div class="manager-header__title">数智验舱 · 智辅实验管理平台</div>
          <div class="manager-header__tagline">Teaching Lab Console</div>
        </div>
      </div>
      <div class="manager-header__user" aria-label="当前用户">
        <img
          class="manager-header__avatar"
          :src="avatarSrc"
          alt=""
        >
        <span class="manager-header__name">{{ data.user.name }}</span>
      </div>
    </header>
    <div class="manager-body">
      <component :is="currentSidebar" />
      <main class="manager-main" id="main-content" role="main">
        <router-view @update-user="updateUser" />
      </main>
    </div>
  </div>
</template>

<script setup>
import { reactive, computed } from 'vue'
import router from '@/router'
import { ElMessage } from 'element-plus'
import { useRoute } from 'vue-router'
import CourseSidebar from '@/views/manager/CourseSidebar.vue'
import DefaultSidebar from '@/views/manager/DefaultSidebar.vue'
import { resolveAvatarUrl } from '@/utils/appConfig'

const route = useRoute()

const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'
const avatarSrc = computed(() => resolveAvatarUrl(data.user?.avatar) || defaultAvatar)

const isCourseDetail = computed(() => route.path.startsWith('/course/courseDetail'))

const currentSidebar = computed(() => (isCourseDetail.value ? CourseSidebar : DefaultSidebar))

const data = reactive({
  user: JSON.parse(localStorage.getItem('system-user') || '{}'),
})

if (!data.user?.id) {
  ElMessage.error('请登录！')
  router.push('/login')
}

const updateUser = () => {
  data.user = JSON.parse(localStorage.getItem('system-user') || '{}')
}
</script>
