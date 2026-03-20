<template>
  <iframe
      :src="reportUrl"
      width="100%"
      height="800px"
      frameborder="0"
      v-if="reportUrl"
  ></iframe>
</template>

<script setup>
import { ref, onMounted } from 'vue';

const reportUrl = ref('');

onMounted(() => {
  // 1. 构造参数（自动编码）
  const params = new URLSearchParams({
    page_number: 6,
    user_id: localStorage.getItem('userId') || 'guest'
  });

  // 2. 构建完整URL
  reportUrl.value = `http://localhost:8075/webroot/decision/view/report?viewlet=project/Test3.fvs&${params.toString()}`;

  // 3. 调试输出
  console.log('报表URL:', reportUrl.value);
});
</script>