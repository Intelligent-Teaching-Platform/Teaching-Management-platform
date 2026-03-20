<template>
  <div class="document-container">

    <!-- 主要内容 -->
    <div class="dialog-card">

      <!-- 标题 -->
      <h2 class="page-title">实验文件详情</h2>

      <!-- 表单 -->
      <el-form label-position="top" label-width="100px">
        <!-- 基本信息部分 -->
        <div class="form-section">


          <el-form-item label="* 实验题目">
            <el-input
                v-model="formData.title"
                type="textarea"
                :rows="2"
                show-word-limit
                maxlength="50"
                placeholder="请输入实验题目"
            />
          </el-form-item>
        </div>

        <!-- 实验内容部分 -->
        <div class="form-section">

          <el-form-item label="* 实验目的">
            <el-input
                v-model="formData.purpose"
                type="textarea"
                :rows="4"
                show-word-limit
                maxlength="500"
                placeholder="请输入实验目的"
            />
          </el-form-item>

          <el-form-item label="* 实验环境">
            <el-input
                v-model="formData.environment"
                type="textarea"
                :rows="4"
                show-word-limit
                maxlength="500"
                placeholder="请输入实验环境"
            />
          </el-form-item>

          <el-form-item label="* 实验内容和步骤">
            <el-input
                v-model="formData.contentAndSteps"
                type="textarea"
                :rows="6"
                show-word-limit
                maxlength="1000"
                placeholder="请输入实验内容和步骤"
            />
          </el-form-item>
        </div>

        <!-- 总结部分 -->
        <div class="form-section">


          <el-form-item label="* 实验总结和心得体会">
            <el-input
                v-model="formData.summaryAndReflection"
                type="textarea"
                :rows="6"
                show-word-limit
                maxlength="1000"
                placeholder="请输入实验总结和心得体会"
            />
          </el-form-item>

        </div>

        <!-- 操作按钮 -->
        <div class="form-actions">
          <el-button @click="$router.back()">返回</el-button>
          <el-button type="primary" @click="generateWord">生成 Word 文件</el-button>
        </div>
      </el-form>
    </div>
  </div>
</template>



<script setup>
import { reactive } from 'vue';
import { useRoute } from 'vue-router';
import Docxtemplater from 'docxtemplater';
import PizZip from 'pizzip';
import { saveAs } from 'file-saver';
import ImageModule from 'docxtemplater-image-module-free';
import {ArrowLeftBold} from "@element-plus/icons-vue";

const route = useRoute();

const formData = reactive({
  title: route.query.title || '',
  purpose: route.query.purpose || '',
  environment: route.query.environment || '',
  contentAndSteps: route.query.contentAndSteps || '',
  summaryAndReflection: route.query.summaryAndReflection || '',
  attachment: route.query.attachment || ''
});

const generateWord = async () => {
  const templateUrl = '/word.docx';
  try {
    const response = await fetch(templateUrl);
    const buffer = await response.arrayBuffer();
    const zip = new PizZip(buffer);

    // 图片模块配置
    const imageOpts = {
      centered: false,
      getImage: async (tagValue) => {
        if (!tagValue) return null;
        const res = await fetch(tagValue);
        if (!res.ok) throw new Error('Image load failed');
        return await res.arrayBuffer();
      },
      getSize: () => [300, 200]
    };

    const imageModule = new ImageModule(imageOpts);
    const doc = new Docxtemplater(zip, {
      paragraphLoop: true,
      linebreaks: true,
      modules: [imageModule]
    });

    // 替换占位符
    doc.render({
      title: formData.title,
      purpose: formData.purpose,
      environment: formData.environment,
      contentAndSteps: formData.contentAndSteps,
      summaryAndReflection: formData.summaryAndReflection,
      attachment: formData.attachment
    });

    // 生成文档
    const out = doc.getZip().generate({ type: 'blob' });
    saveAs(out, '实验报告.docx');

  } catch (error) {
    console.error('生成 Word 失败:', error);
  }
};
</script>





<style scoped>
.document-container {
  padding: 40px 20px;
  background-color: #f5f7fa;
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: flex-start;
}
.form-actions {
  margin-top: 30px;
  text-align: center;
}

.dialog-card {
  width: 60%;
  max-width: 800px;
  background: #fff;
  border-radius: 10px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  padding: 30px;
  box-sizing: border-box;
}

h2 {
  text-align: center;
  margin-bottom: 20px;
  font-size: 20px;
  color: #333;
}

.attachment-link {
  display: inline-block;
  margin-top: 6px;
  color: #409EFF;
  text-decoration: none;
}
.attachment-link:hover {
  text-decoration: underline;
}
.form-actions {
  margin-top: 30px;
  text-align: center;
}

.form-actions .el-button {
  min-width: 120px;
  margin: 0 10px;
}
.form-actions {
  margin-top: 30px;
  text-align: center;
}

.form-actions .el-button {
  min-width: 120px;
  margin: 0 10px;
}
:deep(.bold-label .el-form-item__label) {
  font-weight: 600;
}

</style>



<!--<script setup>-->
<!--import { reactive } from 'vue';-->
<!--import { useRoute } from 'vue-router';-->
<!--import Docxtemplater from 'docxtemplater';-->
<!--import PizZip from 'pizzip';-->
<!--import { saveAs } from 'file-saver';-->
<!--import ImageModule from 'docxtemplater-image-module-free';-->
<!--import {ArrowLeftBold} from "@element-plus/icons-vue";-->

<!--const route = useRoute();-->

<!--const formData = reactive({-->
<!--  title: route.query.title || '',-->
<!--  purpose: route.query.purpose || '',-->
<!--  environment: route.query.environment || '',-->
<!--  contentAndSteps: route.query.contentAndSteps || '',-->
<!--  summaryAndReflection: route.query.summaryAndReflection || '',-->
<!--  attachment: route.query.attachment || ''-->
<!--});-->

<!--const generateWord = async () => {-->
<!--  const templateUrl = '/word.docx';-->
<!--  try {-->
<!--    const response = await fetch(templateUrl);-->
<!--    const buffer = await response.arrayBuffer();-->
<!--    const zip = new PizZip(buffer);-->

<!--    // 图片模块配置-->
<!--    const imageOpts = {-->
<!--      centered: false,-->
<!--      getImage: async (tagValue) => {-->
<!--        if (!tagValue) return null;-->
<!--        const res = await fetch(tagValue);-->
<!--        if (!res.ok) throw new Error('Image load failed');-->
<!--        return await res.arrayBuffer();-->
<!--      },-->
<!--      getSize: () => [300, 200]-->
<!--    };-->

<!--    const imageModule = new ImageModule(imageOpts);-->
<!--    const doc = new Docxtemplater(zip, {-->
<!--      paragraphLoop: true,-->
<!--      linebreaks: true,-->
<!--      modules: [imageModule]-->
<!--    });-->

<!--    // 替换占位符-->
<!--    doc.render({-->
<!--      title: formData.title,-->
<!--      purpose: formData.purpose,-->
<!--      environment: formData.environment,-->
<!--      contentAndSteps: formData.contentAndSteps,-->
<!--      summaryAndReflection: formData.summaryAndReflection,-->
<!--      attachment: formData.attachment-->
<!--    });-->

<!--    // 生成文档-->
<!--    const out = doc.getZip().generate({ type: 'blob' });-->
<!--    saveAs(out, '实验报告.docx');-->

<!--  } catch (error) {-->
<!--    console.error('生成 Word 失败:', error);-->
<!--  }-->
<!--};-->
<!--</script>-->





<!--<style scoped>-->
<!--.document-container {-->
<!--  padding: 40px 20px;-->
<!--  background-color: #f5f7fa;-->
<!--  min-height: 100vh;-->
<!--  display: flex;-->
<!--  justify-content: center;-->
<!--  align-items: flex-start;-->
<!--}-->

<!--.dialog-card {-->
<!--  width: 60%;-->
<!--  max-width: 800px;-->
<!--  background: #fff;-->
<!--  border-radius: 10px;-->
<!--  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);-->
<!--  padding: 30px;-->
<!--  box-sizing: border-box;-->
<!--}-->

<!--h2 {-->
<!--  text-align: center;-->
<!--  margin-bottom: 20px;-->
<!--  font-size: 20px;-->
<!--  color: #333;-->
<!--}-->

<!--.char-count {-->
<!--  font-size: 12px;-->
<!--  color: #999;-->
<!--  margin-top: 4px;-->
<!--}-->

<!--.attachment-link {-->
<!--  display: inline-block;-->
<!--  margin-top: 6px;-->
<!--  color: #409EFF;-->
<!--  text-decoration: none;-->
<!--}-->
<!--.attachment-link:hover {-->
<!--  text-decoration: underline;-->
<!--}-->
<!--</style>-->
