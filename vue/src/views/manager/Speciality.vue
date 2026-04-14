<template>
  <div class="speciality-redirect" aria-busy="true" />
</template>

<script setup>
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

onMounted(() => {
  router.replace({ path: '/college' })
})
</script>

<style scoped>
.speciality-redirect {
  min-height: 120px;
}
</style>
