<template>
  <div class="sign-container">
    <button
        v-if="!hasSigned"
        @click="handleSign"
        :disabled="!canSign"
        class="sign-button">
      {{ buttonText }}
    </button>
    <div v-else class="signed-message">
      已签到 ({{ signTime }})
    </div>
  </div>
</template>

<script>
export default {
  props: {
    courseId: Number,
    sessionId: Number,
    startTime: String,
    endTime: String
  },
  data() {
    return {
      hasSigned: false,
      signTime: '',
      currentTime: new Date()
    }
  },
  computed: {
    canSign() {
      const now = this.currentTime;
      const start = new Date(this.startTime);
      const end = new Date(this.endTime);
      return now >= start && now <= end;
    },
    buttonText() {
      if (!this.canSign) {
        const now = this.currentTime;
        const start = new Date(this.startTime);
        return now < start ? '签到未开始' : '签到已结束';
      }
      return '点击签到';
    }
  },
  methods: {
    async handleSign() {
      try {
        const res = await this.$axios.post('/api/sign/do', {
          courseId: this.courseId,
          sessionId: this.sessionId,
          studentId: this.$store.state.user.id
        });
        this.hasSigned = true;
        this.signTime = this.formatTime(new Date());
        this.$message.success('签到成功');
      } catch (error) {
        this.$message.error(error.response?.data?.message || '签到失败');
      }
    },
    formatTime(date) {
      return `${date.getHours()}:${date.getMinutes().toString().padStart(2, '0')}`;
    }
  },
  async created() {
    // 检查是否已签到
    const res = await this.$axios.get('/api/sign/result', {
      params: {
        courseId: this.courseId,
        studentId: this.$store.state.user.id
      }
    });
    if (res.data.data && res.data.data.length > 0) {
      this.hasSigned = true;
      this.signTime = this.formatTime(new Date(res.data.data[0].signTime));
    }

    // 每秒更新当前时间
    setInterval(() => {
      this.currentTime = new Date();
    }, 1000);
  }
}
</script>

<style>
.sign-button {
  padding: 10px 20px;
  background-color: #409eff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.sign-button:disabled {
  background-color: #c0c4cc;
  cursor: not-allowed;
}

.signed-message {
  color: #67c23a;
  font-weight: bold;
}
</style>