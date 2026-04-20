<template>
  <div class="task-exp-req dynamic-section">
    <div class="section-header">
      <span class="section-title">📝 实验说明</span>
    </div>
    <el-form-item label="实验目的" prop="experimentPurpose">
      <el-input
        v-model="model.experimentPurpose"
        type="textarea"
        :rows="4"
        placeholder="请输入实验目的（本实验要达成的目标）"
      />
    </el-form-item>
    <el-form-item label="实验要求" prop="experimentRequirement">
      <el-input
        v-model="model.experimentRequirement"
        type="textarea"
        :rows="4"
        placeholder="请输入实验要求（步骤、提交物、评分要点等）"
      />
    </el-form-item>
    <el-form-item label="实验环境" prop="experimentEnvironment">
      <el-input
        v-model="model.experimentEnvironment"
        type="textarea"
        :rows="4"
        placeholder="请输入实验环境（如：操作系统、软件版本等）"
      />
    </el-form-item>
  </div>
</template>

<script setup>
/**
 * 新建/编辑实验任务时：将「实验要求」从上机信息、实验阶段中抽离为独立区块，便于维护。
 * 与父级 el-form 共用校验；model 为 data.form。
 */
defineProps({
  model: {
    type: Object,
    required: true,
  },
})
</script>

<style scoped lang="scss">
.task-exp-req.dynamic-section {
  margin-top: 20px;
  margin-bottom: 20px;
  padding: 16px;
  background-color: #f5f7fa;
  border-radius: 8px;

  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 12px;

    .section-title {
      font-size: 14px;
      font-weight: 600;
      color: #303133;
    }
  }
}
</style>
