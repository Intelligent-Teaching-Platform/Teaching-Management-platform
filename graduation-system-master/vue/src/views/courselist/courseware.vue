<template>
  <div class="homework-page">
    <!-- 页面标题 -->
    <el-card class="box-card homework-card">
      <template #header>
        <div class="card-header">
          <div class="card-header-text">
            <span class="course-title">
              {{ courseName }}
            </span>
            <p class="course-subtitle">
              面向 {{ courseName }} 课程，用于查看学生课后作业完成情况并进行在线批改与相似度分析
            </p>
          </div>
        </div>
      </template>
      <div class="filter-container">
        <div class="filter-options">
          <span class="filter-label">
            <el-icon class="inline-icon"><Filter /></el-icon>
            筛选
          </span>
          <el-radio-group v-model="filterOption" size="small">
            <el-radio-button label="全部">全部</el-radio-button>
            <el-radio-button label="已完成">已完成</el-radio-button>
            <el-radio-button label="未完成">未完成</el-radio-button>
          </el-radio-group>
        </div>
        <div class="similarity-alert">
          <el-alert
            title="所有提交内容会进行相似度分析，请同学保持独立完成"
            type="info"
            :closable="false"
            show-icon
          />
        </div>
      </div>
      <el-table
        :data="filteredAssignments"
        style="width: 100%"
        @row-click="onRowClick"
      >
        <el-table-column label="序号" width="60" align="center" header-align="center">
          <template #default="scope">
            {{ (data.pageNum - 1) * data.pageSize + scope.$index + 1 }}
          </template>
        </el-table-column>
        <el-table-column label="课后作业名称">
          <template #default="scope">
            <div class="assignment-cell">
              <div class="assignment-row-top">
                <div class="assignment-name">
                  {{ scope.row.name }}
                </div>
                <div
                  class="student-name"
                  @click.stop="handleEdit(scope.row)"
                >
                  <el-icon class="inline-icon"><User /></el-icon>
                  <span class="student-name-text">{{ scope.row.studentName }}</span>
                </div>
              </div>
              <div v-if="scope.row.status === '未交'" class="status">{{ scope.row.state }}</div>
              <div v-else class="status completed">{{ scope.row.status }}</div>
              <div>
                <el-tag
                    :type="parseFloat(scope.row.tip1) > 80 ? 'danger' :
                        parseFloat(scope.row.tip1) > 50 ? 'warning' : 'success'"
                >
                  {{ scope.row.tip1 ? `相似度: ${scope.row.tip1}` : '未分析' }}
                </el-tag>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="最后提交或修改时间" width="170">
          <template #default="scope">
            {{ formatDateTime(scope.row.studentLastSubmitTime) }}
          </template>
        </el-table-column>
      </el-table>
    </el-card>
    <el-dialog
        title="任务信息"
        width="40%"
        v-model="data.formVisible"
        :close-on-click-modal="false"
        destroy-on-close
    >
      <el-form :model="data.form" label-width="100px" style="padding-right: 50px" >
        <el-form-item label="任务类型" prop="lab" disabled>
          <el-input v-model="data.form.lab" autocomplete="off"  disabled="data.user.role === 'STUDENT'" />
        </el-form-item>
        <div class="form-section">
          <el-form-item label="* 任务题目">
            <el-input
                v-model="data.form.name"
                type="textarea"
                :rows="2"
                show-word-limit
                maxlength="50"
                placeholder="请输入任务题目"
                :disabled="data.user.role === 'STUDENT'"
            />
          </el-form-item>
          <div class="form-section">
            <el-form-item label="* 任务要求">
              <el-input
                  v-model="data.form.content"
                  type="textarea"
                  :rows="4"
                  show-word-limit
                  maxlength="500"
                  placeholder="请输入任务要求"
                  :disabled="data.user.role === 'STUDENT'"
              />
            </el-form-item>
          </div>
        </div>
        <div class="form-section">
          <el-form-item label="* 提交任务内容">
            <el-input
                v-model="data.form.scontent"
                type="textarea"
                :rows="4"
                show-word-limit
                maxlength="1000"
                placeholder="请输入任务内容"
                @input="onContentChange"
            />
          </el-form-item>
          <el-form-item label="相似度分析">
            <el-alert
                :title="similarityAlertTitle"
                :type="similarityAlertType"
                :closable="false"
                show-icon
            ></el-alert>
          </el-form-item>
        </div>
        <el-form-item label="任务打分" prop="score" v-if="data.user.role === 'TEACHER'">
          <el-input v-model="data.form.score" autocomplete="off"/>
        </el-form-item>
        <el-form-item label="修改意见" prop="amendment" v-if="data.user.role === 'TEACHER'">
          <el-input v-model="data.form.amendment" autocomplete="off"/>
        </el-form-item>
        <el-form-item label="审核结果" prop="state" :rules="[{ required: true, message: '请选择审核是否通过', trigger: 'change' }]" v-if="data.user.role === 'TEACHER'">
          <el-select v-model="data.form.state" placeholder="审核是否通过">
            <el-option label="审核通过" value="审核通过"/>
            <el-option label="打回任务" value="未通过"/>
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="data.formVisible = false">取 消</el-button>
          <el-button type="primary" @click="save">提交</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, reactive, watch, nextTick } from 'vue';
import { useRoute } from 'vue-router';
import request from "@/utils/request";
import { ElMessage, ElMessageBox, ElAlert } from "element-plus";
import { Document, Filter, EditPen, User } from '@element-plus/icons-vue'
import cosineSimilarity from 'cosine-similarity';
import { getUploadUrl } from '@/utils/appConfig'

const route = useRoute();
const courseName = computed(() => route.query.courseName || '作业');
const courseId = computed(() => {
  const id = route.query.id || route.params.id;
  return id != null ? Number(id) : null;
});
const data = reactive({
  pageNum: 1,
  pageSize: 100,
  total: 0,
  formVisible: false,
  form: {
    id: null,
    name: '',
    lab: '',
    scontent: '',
    content: '',
    tip1: '',
    tip2: '',
    tip3: '',
    score: '',
    amendment: '',
    state: ''
  },
  tableData: [],
  allData: [], // 存储所有作业数据用于相似度分析
  name: null,
  collegeData: [],
  user: JSON.parse(localStorage.getItem("system-user") || "{}"),
  expFormVisible: false,
  expForm: {
    title: "",
    purpose: "",
    environment: "",
    contentAndSteps: "",
    summaryAndReflection: "",
    attachment: "",
  },
});

// 相似度分析相关状态
const similarityScore = ref(0);
const similarityAlertTitle = ref('相似度分析将在提交后进行');
const similarityAlertType = ref('info');

// 筛选选项
const filterOption = ref('全部');

// 根据筛选选项过滤作业
const filteredAssignments = computed(() => {
  if (filterOption.value === '全部') {
    return data.tableData;
  } else if (filterOption.value === '已完成') {
    return data.tableData.filter(item =>
        (item.state === '' || item.state === null) &&
        (item.scontent && item.scontent.trim() !== '')
    );
  } else if (filterOption.value === '未完成') {
    return data.tableData.filter(item =>
        ((item.state === '' || item.state === null) &&
            (item.scontent === '' || item.scontent === null)) ||
        ['未完成', '待提交', '待审核', '待修改', '未交'].includes(item.state)
    );
  }
  return [];
});

const uploadUrl = getUploadUrl()
const handleFileUpload = (response, fieldKey) => {
  data.expForm[fieldKey] = response.data;
};

// 获取所有作业数据用于相似度分析
const getAllData = async () => {
  try {
    const res = await request.get('/work/selectAllone');
    if (res.code === '200') {
      data.allData = res.data || []; // 确保使用小写allData
      return data.allData;
    } else {
      ElMessage.error(res.msg || '获取作业数据失败');
      return [];
    }
  } catch (error) {
    console.error('获取所有作业数据失败:', error);
    ElMessage.error('获取作业数据失败，请稍后重试');
    return [];
  }
};

// 改进的相似度计算函数
const calculateSimilarity = (scontent, allScontents) => {
  // 过滤掉当前正在编辑的作业（避免与自身比较）
  const currentId = data.form.id;
  const filteredContents = allScontents.filter(item =>
      !currentId || item.id !== currentId
  );

  // 进一步的数据预处理，如转换为小写，去除非字母数字字符
  const preprocessText = (text) => {
    return text.toLowerCase().replace(/[^a-zA-Z0-9\u4e00-\u9fa5 ]/g, '');
  };

  const validContents = filteredContents
      .filter(item => item.scontent && item.scontent.trim() !== '')
      .map(item => preprocessText(item.scontent));

  if (validContents.length === 0) {
    return 0;
  }

  const currentContent = preprocessText(scontent);
  if (!currentContent) {
    return 0;
  }

  // 简单的中文分词（实际应用中可使用更专业的分词库）
  const tokenize = (text) => {
    // 先按空格分割，再对中文进行单字分割
    return text.split(/\s+/).flatMap(word => {
      if (/[\u4e00-\u9fa5]/.test(word)) {
        return word.split('');
      }
      return [word];
    }).filter(word => word);
  };

  const currentTokens = tokenize(currentContent);
  if (currentTokens.length === 0) {
    return 0;
  }

  const similarityScores = validContents.map((text) => {
    const tokens = tokenize(text);
    if (tokens.length === 0) {
      return 0;
    }

    // 构建词频向量
    const vectorize = (tokens) => {
      const vector = {};
      tokens.forEach(token => {
        vector[token] = (vector[token] || 0) + 1;
      });
      return vector;
    };

    const currentVector = vectorize(currentTokens);
    const targetVector = vectorize(tokens);

    // 获取所有不重复的词
    const allTerms = [...new Set([...Object.keys(currentVector), ...Object.keys(targetVector)])];

    // 构建向量数组
    const currentArray = allTerms.map(term => currentVector[term] || 0);
    const targetArray = allTerms.map(term => targetVector[term] || 0);

    // 计算余弦相似度
    return cosineSimilarity(currentArray, targetArray);
  });

  const maxSimilarity = Math.max(...similarityScores);
  return maxSimilarity;
};

// 内容变化时更新相似度分析
const onContentChange = debounce(async () => {
  if (!data.form.scontent || data.form.scontent.length < 10) {
    similarityScore.value = 0;
    similarityAlertTitle.value = '内容太短，无法进行有效分析';
    similarityAlertType.value = 'info';
    return;
  }

  await getAllData();

  // 确保使用正确的数据源
  const similarity = calculateSimilarity(data.form.scontent, data.allData);
  similarityScore.value = similarity;

  // 更新提示信息
  if (similarity > 0.8) {
    similarityAlertTitle.value = `相似度较高: ${(similarity * 100).toFixed(2)}%，可能存在抄袭风险`;
    similarityAlertType.value = 'danger';
  } else if (similarity > 0.5) {
    similarityAlertTitle.value = `相似度中等: ${(similarity * 100).toFixed(2)}%，请注意避免雷同`;
    similarityAlertType.value = 'warning';
  } else {
    similarityAlertTitle.value = `相似度较低: ${(similarity * 100).toFixed(2)}%，内容原创性较好`;
    similarityAlertType.value = 'success';
  }
}, 500);

// 防抖函数
function debounce(func, delay) {
  let timer = null;
  return function(...args) {
    if (timer) clearTimeout(timer);
    timer = setTimeout(() => {
      func.apply(this, args);
    }, delay);
  };
}

// 格式化后端 LocalDateTime：常见为 "2026-03-20T10:11:12"
function formatDateTime(v) {
  if (!v) return '-'
  return String(v).replace('T', ' ').replace(/\.\d{3}Z?$/, '')
}

// 分页查询
const load = async () => {
  try {
    let teacherId = null;
    let studentId = null;
    let classId = null;
    if (data.user.role === "TEACHER") {
      teacherId = data.user.id;
    }
    if (data.user.role === "STUDENT") {
      studentId = data.user.id;
      classId = data.user.classId;
    }
    const res = await request.get("/work/selectPageone", {
      params: {
        pageNum: data.pageNum,
        pageSize: data.pageSize,
        name: data.name,
        teacherId: teacherId,
        studentId: studentId,
        classsId: classId,
        courseId: courseId.value || undefined,
      },
    });

    if (res && res.data) {
      data.tableData = res.data.list || [];
      data.total = res.data.total || 0;
    } else {
      console.error("请求数据失败，响应格式不符合预期");
      ElMessage.error("请求数据失败，请稍后重试");
    }
  } catch (error) {
    console.error("请求数据失败:", error);
    ElMessage.error("请求数据失败，请稍后重试");
  }
};

// 新增
const handleAdd = () => {
  // 重置表单为初始状态
  data.form = {
    id: null,
    name: '',
    lab: '',
    scontent: '',
    content: '',
    tip1: '',
    tip2: '',
    tip3: '',
    score: '',
    amendment: '',
    state: ''
  };

  // 设置学生ID
  if (data.user.role === "STUDENT") {
    data.form.studentId = data.user.id;
  }

  data.formVisible = true;

  // 重置相似度分析
  similarityScore.value = 0;
  similarityAlertTitle.value = '相似度分析将在提交后进行';
  similarityAlertType.value = 'info';
};

// 表格行点击（支持点击作业栏任意位置进入详情）
const onRowClick = (row) => {
  handleEdit(row);
};

// 编辑
const handleEdit = async (row) => {
  try {
    console.log('点击学生姓名，row数据:', row);

    // 验证数据完整性
    if (!row || !row.id) {
      console.error('编辑数据缺少必要字段:', row);
      ElMessage.warning('无法编辑此记录，数据不完整');
      return;
    }

    // 重置表单为初始状态
    data.form = {
      id: null,
      name: '',
      lab: '',
      scontent: '',
      content: '',
      tip1: '',
      tip2: '',
      tip3: '',
      score: '',
      amendment: '',
      state: ''
    };

    // 复制所有属性到表单
    Object.assign(data.form, row);

    // 确保表单字段映射正确
    console.log('设置表单数据:', data.form);

    // 显示对话框
    data.formVisible = true;
    console.log('表单可见性已设置为:', data.formVisible);

    // 如果已有内容，立即计算相似度
    if (data.form.scontent) {
      await onContentChange();
    }
  } catch (error) {
    console.error('处理编辑事件时出错:', error);
    ElMessage.error('处理编辑请求时发生错误');
  }
};

// 新增保存
const add = async () => {
  // 确保获取最新的相似度数据
  if (!data.allData.length) {
    await getAllData();
  }

  const scontent = data.form.scontent;
  const allScontents = data.allData;

  const similarity = calculateSimilarity(scontent, allScontents);
  // 只保存百分比值，不包含其他文本
  data.form.tip1 = `${(similarity * 100).toFixed(2)}%`;

  // 验证必填字段
  if (!data.form.name) {
    ElMessage.warning('请输入实验题目');
    return;
  }

  try {
    const res = await request.post("/work/add", data.form);
    if (res.code === "200") {
      load();
      ElMessage.success("操作成功");
      data.formVisible = false;
    } else {
      ElMessage.error(res.msg || '添加作业失败');
    }
  } catch (error) {
    console.error("添加作业失败:", error);
    ElMessage.error("添加作业失败，请稍后重试");
  }
};

// 编辑保存
const update = async () => {
  // 确保获取最新的相似度数据
  if (!data.allData.length) {
    await getAllData();
  }

  const scontent = data.form.scontent;
  const allScontents = data.allData;

  const similarity = calculateSimilarity(scontent, allScontents);
  // 只保存百分比值，不包含其他文本
  data.form.tip1 = `${(similarity * 100).toFixed(2)}%`;

  // 验证必填字段
  if (!data.form.name) {
    ElMessage.warning('请输入实验题目');
    return;
  }

  if (data.user.role === "STUDENT") {
    data.form.state = "";
  }

  try {
    const res = await request.put("/work/updateone", data.form);
    if (res.code === "200") {
      load();
      ElMessage.success("操作成功");
      data.formVisible = false;
    } else {
      ElMessage.error(res.msg || '更新作业失败');
    }
  } catch (error) {
    console.error("更新作业失败:", error);
    ElMessage.error("更新作业失败，请稍后重试");
  }
};

// 弹窗保存
const save = () => {
  data.form.id ? update() : add();
};

// 删除
const handleDelete = (id) => {
  ElMessageBox.confirm("删除后数据无法恢复，您确定删除吗?", "删除确认", {type: "warning"})
      .then(() => {
        request.delete("/work/delete/" + id).then((res) => {
          if (res.code === "200") {
            load();
            ElMessage.success("操作成功");
          } else {
            ElMessage.error(res.msg);
          }
        });
      })
      .catch(() => {
      });
};

// 重置
const reset = () => {
  data.name = null;
  filterOption.value = '全部';
  load();
};

// 分页
const changePage = (pageNum) => {
  data.pageNum = pageNum;
  load();
};

// 初始化加载数据
load();
</script>

<style scoped>

.card-header {
  font-weight: bold;
}

.card-header-text {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.course-title {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
}

.course-subtitle {
  margin: 0;
  font-size: 13px;
  color: #909399;
}

.filter-container {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 20px;
  background-color: #fff;
  border-bottom: 1px solid #ddd;
}

.filter-options {
  display: flex;
  align-items: center;
}

.filter-label {
  margin-right: 10px;
  font-size: 14px;
  color: #606266;
}

.inline-icon {
  margin-right: 4px;
}

.title-icon {
  margin-right: 6px;
}

.similarity-alert {
  flex: 1;
  text-align: right;
  margin-left: 100px;
}

.info {
  position: relative;
  right: 15px;
  width: 84px;
  height: 84px;
  transform: scale(0.5);
  background: url('@/assets/imgs/icons-act.png') -530px -426px;
}

/* 调整 Element-UI 组件样式 */
.el-radio-group {
  display: flex;
  gap: 10px;
  margin-left: 20px;
}

.el-radio {
  margin-right: 0;
}

.el-radio-button__inner {
  padding: 8px 15px;
  font-size: 14px;
  border-radius: 4px;
}

.el-radio-button__inner:hover {
  color: #409EFF;
}

.el-radio-button__inner.is-active {
  background-color: #F5F7FA;
  border-color: #DCDFE6;
  box-shadow: inset 0 0 0 2px var(--el-color-primary);
  color: #409EFF;
}

.el-table.cell {
  padding: 0;
}

.assignment-row-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.assignment-name {
  font-weight: 500;
}

.assignment-cell {
  width: 100%;
}

/* assignment-cell 用于撑满整列宽度 */
.assignment-cell {
  width: 100%;
}

.status {
  color: #999;
  font-size: 12px;
}

.completed {
  color: #67C23A;
}

.analysis-link {
  margin-top: 5px;
}

.time-remaining {
  color: #F56C6C;
  font-size: 12px;
}

/* 学生姓名样式（与作业标题一致，不再悬浮变色） */
.student-name {
  cursor: pointer;
  color: #303133;
  font-weight: 500;
}

/* 表单样式优化 */
.form-section {
  margin-bottom: 20px;
}

/* 进度条样式 */
.similarity-progress {
  margin-top: 10px;
}
</style>