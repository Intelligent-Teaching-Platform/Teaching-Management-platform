<template>
  <div class="container">
    <div class="header" style="display: flex;justify-content: space-between;margin-top:30px">
      <!--      <el-button type="primary" style="display: flex;margin-left:30px;font-size:16px;box-shadow: 4px 4px 6px rgba(0, 0, 0, 0.1);padding:18px" round v-if="data.user.role === 'TEACHER'">+新建班级</el-button>-->
      <div class="xsearchInput">
        <el-icon class="search"><Search /></el-icon>
      </div>
    </div>
    <div class="class-list">
      <el-card class="box-card" style="width:100%">
        <div class="card-header">
          <span style="font-size:16px;font-weight: bold;margin-top: 30px;">签到列表</span>
          <div class="setting">
          </div>
        </div>
        <!--可视化签到饼图-->
        <div class="signIn-chart-container">
          <div id="signInChart" class="sign-chart"></div>
          <div class="sign-lists">
            <div class="sign-list">
              <div class="sign-list-title">已签到学生（{{ signedNames.length }}）</div>
              <div class="sign-list-body">
                <el-tag
                  v-for="name in signedNames"
                  :key="`signed-${name}`"
                  class="sign-tag"
                  type="success"
                  effect="dark"
                >
                  {{ name }}
                </el-tag>
                <div v-if="!signedNames.length" class="sign-empty">暂无已签到学生</div>
              </div>
            </div>
            <div class="sign-list">
              <div class="sign-list-title">未签到学生（{{ unsignedNames.length }}）</div>
              <div class="sign-list-body">
                <el-tag
                  v-for="name in unsignedNames"
                  :key="`unsigned-${name}`"
                  class="sign-tag"
                  type="danger"
                  effect="plain"
                >
                  {{ name }}
                </el-tag>
                <div v-if="!unsignedNames.length" class="sign-empty">暂无未签到学生</div>
              </div>
            </div>
          </div>
        </div>


        <el-text v-for="(item, index) in classes" :key="index" style="margin-bottom: 20px;display: block;">
          <div style="font-size:16px;display: flex;justify-content:space-between;align-items: center;">
            <div style="display: flex; align-items: center;">
              <el-checkbox v-if="showCheckboxes" v-model="selectedClasses[index]" style="margin-right: 10px;"></el-checkbox>
              <div>{{ item.name }}</div>
            </div>
            <div>加入班级人数：{{ item.students }}人</div>
            <div style="display: flex; align-items: center;">
              <el-button
                  v-if="data.user.role === 'TEACHER'"
                  type="primary" @click="openSigninDialog">签到</el-button>

              <el-button
                  v-if="data.user.role === 'STUDENT'"
                  type="success"
                  @click="clickSign"
                  :disabled="hasSigned"
                  :class="{ 'signed-button': hasSigned }"
              >
                {{ hasSigned ? '已签到' : '签到' }}
              </el-button>

              <el-button type="primary" @click="openSelectDialog" v-if="data.user.role === 'TEACHER'">选人</el-button>
              <!--              <el-button type="primary" @click="openQuizDialog">抢答</el-button>-->
              <el-popover
                  placement="bottom"
                  trigger="hover"
              >
                <template #default>
                  <div style="display: flex; flex-direction: column; align-items: center;">
                    <el-button type="primary" link @click="renameClass">重命名</el-button>
                    <el-button type="primary" link @click="deleteClass">删除</el-button>
                  </div>
                </template>
                <template #reference>
                  <div class="icon-more"><img src="@/assets/imgs/dian.png"/></div>
                </template>
              </el-popover>
            </div>
          </div>
        </el-text>
      </el-card>
    </div>
    <!--    管理对话框-->

    <div class="history-signIn" v-if="data.user.role === 'TEACHER'">
      <el-card class="box-card" style="width:100%">
        <span style="font-size:16px;font-weight: bold;margin-top: 50px;">历史签到</span>
        <hr style="margin:30px">
        <div class="card-header">

          <el-table :data="data.tableData" stripe style="font-size: 16px;">
            <el-table-column prop="teacherId" label="教师ID"></el-table-column>
            <el-table-column prop="id" label="签到ID"></el-table-column>
            <el-table-column prop="distance" label="距离限制"></el-table-column>
            <el-table-column label="开始时间" align="center">
              <template #default="scope">
                <div class="time-cell">
                  <div>{{ formatDate(scope.row.startTime) }}</div>
                  <div>{{ formatTime(scope.row.startTime) }}</div>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="结束时间" align="center">
              <template #default="scope">
                <div class="time-cell">
                  <div>{{ formatDate(scope.row.endTime) }}</div>
                  <div>{{ formatTime(scope.row.endTime) }}</div>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="创建时间" align="center">
              <template #default="scope">
                <div class="time-cell">
                  <div>{{ formatDate(scope.row.createTime) }}</div>
                  <div>{{ formatTime(scope.row.createTime) }}</div>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="num" label="已签人数"></el-table-column>
            <el-table-column prop="snum" label="课程总人数"></el-table-column>
          </el-table>

        </div>
      </el-card>
    </div>


    <!-- 签到方式选择对话框 -->
    <el-dialog v-model="signinDialogVisible" title="签到" width="400px">
      <el-form>
        <el-form-item label="签到方式">
          <el-radio-group v-model="signinMethod">
            <el-radio label="普通">签到</el-radio>
            <el-radio label="签到码">签到码</el-radio>
            <el-input
                v-if="signinMethod === '签到码'"
                v-model="checkInCode"
                readonly
                style="width: 100px; margin-left: 5px;" />
          </el-radio-group>
        </el-form-item>
        <el-form-item label="活动时长 (分钟)">
          <el-input-number v-model="activityDuration" :min="1" :max="120" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="resetForm">重置</el-button>
          <el-button type="primary" @click="handleCreateSignIn">确定</el-button>
        </div>
      </template>
    </el-dialog>

    <!--    抽人对话框-->
    <el-dialog v-model="selectDialogVisible" title="抽人" width="400px">
      <el-form>
        <el-form-item >
          <el-button type="primary" @click="randomSelectStudent">随机选人</el-button>
          <el-input v-model="selectedStudentName"  readonly style="margin-left: 10px; width: 200px;"/>
        </el-form-item>
        <el-form-item label="指定学生">
          <el-select v-model="selectedStudent" placeholder="请选择学生">
            <el-option
                v-for="student in students"
                :key="student.id"
                :label="student.name"
                :value="student.id"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="selectDialogVisible = false">关闭</el-button>
        </div>
      </template>
    </el-dialog>
  </div>


</template>

<script setup>
import { ref,reactive} from 'vue';
import {ElInput, ElCheckbox, ElMessage} from 'element-plus';
import * as echarts from 'echarts';
import { onMounted, nextTick } from 'vue';



const input = ref('');
const showCheckboxes = ref(false);
const selectedClasses = ref([]);
const dialogVisible = ref(false);
const signinDialogVisible = ref(false);
const signinMethod = ref('普通');
const requirePhoto = ref(false);
const activityDuration = ref(30);
const selectDialogVisible = ref(false);
const selectedClass = reactive({ name: '', students: 0 });
const selectedStudent = ref('');
const selectedStudentName = ref('');
const checkInCode = ref(''); // 新增字段，保存签到码
const hasSigned = ref(false);



// const students = ref([
//   { id: 1, name: '张三' },
//   { id: 2, name: '李四' },
//   { id: 3, name: '王五' },
//   { id: 4, name: '赵六' },
// ]);

const students = ref([]);

// 时间格式化：分行显示 日期 / 时间
const formatDate = (cellValue) => {
  if (!cellValue) return '';
  const d = new Date(cellValue);
  if (Number.isNaN(d.getTime())) return cellValue;
  const pad = (n) => String(n).padStart(2, '0');
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`;
};
const formatTime = (cellValue) => {
  if (!cellValue) return '';
  const d = new Date(cellValue);
  if (Number.isNaN(d.getTime())) return cellValue;
  const pad = (n) => String(n).padStart(2, '0');
  return `${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`;
};

const loading = ref(false);
const error = ref(null);

const loadStudent = async () => {
  loading.value = true;
  error.value = null;

  try {
    const res = await request.get('/student/selectAll');

    console.log('原始响应数据:', res); // 调试用，查看完整响应结构

    // 根据实际响应结构调整
    if (res.code === '200' && Array.isArray(res.data)) {
      // 提取需要的字段(id和name)
      students.value = res.data.map(item => ({
        id: item.id,
        name: item.name
      }));
    } else {
      throw new Error(res.msg || '获取数据失败');
    }

    console.log('处理后的学生数据:', students.value); // 调试用

  } catch (err) {
    error.value = err.message || '获取数据失败';
    console.error('获取学生数据失败:', err);
  } finally {
    loading.value = false;
  }
};

// const classes = [
//   { name: '计科2203', students: 34 },
//   { name: '计科2202', students: 30 }
// ];
const classes = ref([]); // 初始化空数组

const defaultLocation = {
  latitude: 39.9042,
  longitude: 116.4074,
  distance: 100 // 签到距离限制(米)
};
import { watch } from 'vue';
import {createSignIn, recordSignIn, selectAll, selectByName} from '@/api/signin';
import {useRoute} from "vue-router";
import request from "@/utils/request";

watch(signinMethod, (newVal) => {
  if (newVal === '签到码') {
    const code = Math.floor(1000 + Math.random() * 9000); // 生成 1000-9999 的随机数
    checkInCode.value = code.toString(); // 赋值给响应式数据
    console.log('自动生成的签到码:', checkInCode.value);
  }
});
const props = defineProps({
  currentCourseId: {
    type: Number,
    required: true
  },
  currentUserId: {
    type: Number,
    required: true
  }
});

const data = reactive({
  tableData: [],
  user: JSON.parse(localStorage.getItem('system-user') || '{}'),
});

const route = useRoute();
const courseId = ref(null)
// 简化的用户ID获取（优先使用props中的currentUserId）
const getTeacherId = () => {
  // 优先使用props中的ID
  if (props.currentUserId) {
    return Number(props.currentUserId);
  }

  // 备选方案：从本地存储获取
  if (data.user?.id) {
    return Number(data.user.id);
  }

  ElMessage.error('无法获取用户ID');
  throw new Error('MISSING_USER_ID');
};

const getStudentId = () => {
  // 优先使用props中的ID
  if (props.currentUserId) {
    return Number(props.currentUserId);
  }

  // 备选方案：从本地存储获取
  if (data.user?.id) {
    return Number(data.user.id);
  }

  ElMessage.error('无法获取用户ID');
  throw new Error('MISSING_USER_ID');
};


const openSelectDialog = () => {
  dialogVisible.value = false;
  selectDialogVisible.value = true;
  selectedStudentName.value = '';
};
let intervalId = null;
const randomSelectStudent = () => {
  if (students.value.length === 0) {
    selectedStudentName.value = '没有学生可供选择';
    return;
  }
  // 清除之前的定时器
  clearInterval(intervalId);

  let count = 0;
  const totalRolls = 10; // 滚动次数

  intervalId = setInterval(() => {
    const randomIndex = Math.floor(Math.random() * students.value.length);
    const selected = students.value[randomIndex];
    selectedStudentName.value = selected.name;
    count++;
    if (count >= totalRolls) {
      clearInterval(intervalId);
      console.log('随机选中的学生:', selected.name);
    }
  }, 100); // 每100毫秒滚动一次
};
const toggleSettings = () => {
  showCheckboxes.value = !showCheckboxes.value;
  if (!showCheckboxes.value) {
    selectedClasses.value = [];
  }
};
const openSigninDialog = () => {
  dialogVisible.value = false;
  signinDialogVisible.value = true;
};


// 分页查询
const load = () => {
  request.get('/signIn/selectPage', {
    params: {
      pageNum: data.pageNum,
      pageSize: data.pageSize,
      courseId: courseId.value,
    }
  }).then(res => {
    data.tableData = res.data?.list;
    data.total = res.data?.total;
  });
};


const renameClass = () => {
  // 重命名班级的逻辑
  console.log('重命名班级');
};

const deleteClass = () => {
  // 删除班级的逻辑
  console.log('删除班级');
};

const handleCreateSignIn = async () => {
  try {
    const params = {
      courseId: courseId.value,
      teacherId: getTeacherId(),
      latitude: 39.9042,
      longitude: 116.4074,
      duration: activityDuration.value,
      distance: 100,
    };

    console.log('提交参数:', params);

    const response = await createSignIn(params);

    // 更宽松的成功判断条件
    if (response.code === "200") {
      ElMessage.success('签到创建成功');
      signinDialogVisible.value = false;
      // 更新图表数据
      chartData.signed = response.data.num;
      chartData.unsigned = response.data.unum;
      updateChart();

      return response.data;
    }

    // 尝试解析可能的错误信息
    const errorMsg = response.data?.message ||
        response.message ||
        '签到请求已完成，但返回状态异常';
    throw new Error(errorMsg);

  } catch (error) {
    console.error('完整错误:', {
      error,
      response: error.response,
      config: error.config
    });

    // 更友好的错误提示
    const msg = error.response?.data?.message ||
        error.message ||
        '签到过程出现异常';
    ElMessage.error(msg);
  }
};

const resetForm = () => {
  signinMethod.value = '签到';

  activityDuration.value = 30;
};

const handleDraw = () => {
  // 抽人逻辑
  console.log('抽人');
};


const clickSign = async () => {
  try {
    if (hasSigned.value) return; // 如果已签到，直接返回

    const params = {
      courseId: courseId.value,
      studentId: getStudentId(),
      latitude: 39.9042,
      longitude: 116.4074,
    };

    console.log('提交参数:', params);

    const response = await recordSignIn(params);

    if (response.code === "200") {
      ElMessage.success('学生签到成功');
      hasSigned.value = true; // 签到成功后更新状态
      signinDialogVisible.value = false;
      return;
    }

    const errorMsg = response.data?.message ||
        response.message ||
        '你已签到，不要重复签到';
    throw new Error(errorMsg);

  } catch (error) {
    console.error('完整错误:', error);
    const msg = error.response?.data?.message ||
        error.message ||
        '签到过程出现异常';
    ElMessage.error(msg);
  }
};

const checkSignStatus = async () => {
  try {
    const response = await checkSignInStatus(courseId.value, getStudentId());
    if (response.code === "200") {
      hasSigned.value = response.data.hasSigned;
    }
  } catch (error) {
    console.error('检查签到状态错误:', error);
  }
};

const chartData = reactive({
  signed: 0,
  unsigned: 0
});

const signedNames = ref([]);
const unsignedNames = ref([]);

// 初始化图表
const REFRESH_INTERVAL = 1000;
let chartInstance = null;
const initChart = () => {
  const chartDom = document.getElementById('signInChart');
  if (!chartDom) return;

  // 销毁旧实例
  if (chartInstance) {
    chartInstance.dispose();
  }

  chartInstance = echarts.init(chartDom);

  const option = {
    title: {
      text: '班级签到情况',
      left: 'center'
    },
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c}人 ({d}%)'
    },
    legend: {
      orient: 'vertical',
      left: 'left',
      data: ['已签到', '未签到']
    },
    series: [
      {
        name: '签到情况',
        type: 'pie',
        radius: ['50%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: true,
          formatter: '{b}: {c}人\n({d}%)'
        },
        emphasis: {
          label: {
            show: true,
            fontSize: '18',
            fontWeight: 'bold'
          }
        },
        labelLine: {
          show: true
        },
        data: [
          { value: chartData.signed, name: '已签到', itemStyle: { color: '#67C23A' } },
          { value: chartData.unsigned, name: '未签到', itemStyle: { color: '#F56C6C' } }
        ]
      }
    ]
  };

  chartInstance.setOption(option);

  // 响应式调整
  window.addEventListener('resize', function() {
    chartInstance && chartInstance.resize();
  });
};

const fetchSignInData = async () => {
  try {
    if (!courseId.value) return;
    const response = await selectAll(courseId.value);
    if (response.code === "200") {
      chartData.signed = response.data.num || 0;
      chartData.unsigned = response.data.unum || 0;
      signedNames.value = response.data.signedNames || [];
      unsignedNames.value = response.data.unsignedNames || [];
      updateChart();
    } else {
      const errorMsg = response.data?.message || response.message || '获取签到数据失败';
      ElMessage.error(errorMsg);
    }
  } catch (error) {
    console.error('获取签到数据错误:', error);
    ElMessage.error('获取签到数据时出现异常');
  }
};

// 更新图表数据
const updateChart = () => {
  if (chartInstance) {
    const option = {
      series: [{
        data: [
          { value: chartData.signed, name: '已签到', itemStyle: { color: '#67C23A' } },
          { value: chartData.unsigned, name: '未签到', itemStyle: { color: '#F56C6C' } }
        ]
      }]
    };
    chartInstance.setOption(option);
  }
};

const fetchCourseIdByName = async(name)  => {
  try{
    console.log('请求参数:', { name });
    const response = await selectByName(name)
    console.log('响应结果:', {
      status: response.status,
      data: response.data,
      fullResponse: response
    });
    if(response.code === "200") {
      courseId.value=response.data.id;
      classes.value = [ // 假设只有一个班级（根据业务场景调整）
        {
          name: response.data.name,
          students: response.data.alreadyNum
        }
      ];
    } else {
      ElMessage.error('获取课程id失败')
    }
  } catch (error) {
    ElMessage.error('获取课程id失败')
  }
};

const fetchStudents = async (courseId) => {
  try {
    const response = await selectAllStudents(courseId);
    if (response.code === "200") {
      classes.value = [ // 假设只有一个班级（根据业务场景调整）
        {
          name: response.data.name,
          students: response.data.alreadyNum
        }
      ];
    } else {
      ElMessage.error('获取学生列表失败')
    }
  } catch (error) {
    ElMessage.error('获取学生列表失败')
  }
};
onMounted(async () => {
  nextTick(() => {
    initChart();
  });

  console.log('路由参数中的 id:', route.query.courseName);
  const courseName = route.query.courseName;
  if (courseName) {
    await fetchCourseIdByName(courseName);
  } else {
    ElMessage.error('课程名称缺失');
    return;
  }

  if (!courseId.value) {
    ElMessage.error('无法获取课程ID');
    return;
  }

  // 初始加载数据
  fetchSignInData();
  load();
  loadStudent();

  // 设置3秒定时刷新
  const refreshInterval = setInterval(fetchSignInData, 10000);

  // 组件卸载时清除定时器
  onUnmounted(() => {
    clearInterval(refreshInterval);
  });
});

// 在签到成功时刷新数据
const handleSignInSuccess = () => {

  checkSignStatus();
  fetchSignInData();
};

</script>
<style scoped>
.container {
  width: 92%;
  height: 100%;
}

/* 统一卡片基础样式 */
.box-card {
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  margin-bottom: 1px;
  transition: all 0.3s ease;
}

.box-card:hover {
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.12);
}

.xsearchInput {
  display: inline-block;
  position: relative;
  margin-right: 20px;
}

.xsearchInput input {
  width: 200px;
  height: 36px;
  line-height: 34px;
  border-radius: 50px;
  border: 1px solid #D4D6D9;
  box-sizing: border-box;
  padding: 0 44px 0 14px;
  font-size: 14px;
  color: #474C59;
  transition: border-color 0.2s cubic-bezier(0.645, 0.045, 0.355, 1);
}

.search {
  position: absolute;
  top: 10px;
  right: 14px;
  width: 16px;
  height: 16px;
  cursor: pointer;
}

.notes {
  display: block;
  margin-top: 20px;
  margin-left: 30px;
  color: #a9a9a9;
  white-space: normal;
  font-size: 14px;
}

/* 统一标题样式 */
.card-header, .history-signIn .box-card > span {
  margin-bottom: 20px;
  padding-bottom: 15px;
  display: flex;
  align-items: center;
  border-bottom: 2px solid #F3F3F3;
  width: 100%;
}

.card-header span, .history-signIn .box-card > span {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.setting {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  margin-right: 30px;
}

.more-setting {
  margin-right: 15px;
  margin-top:30px;
  color: #2d53d0;
  cursor: pointer;
  width:100px;
}

.input{
  width: 240px;
  height: 30px;
  margin-top: 30px;
  border-radius: 20px;
}

.class-list {
  margin-left: 30px;
  margin-top: 30px;
  margin-right: 30px;
}

.icon-more {
  padding-left: 10px;
  width: 20px;
  height: 20px;
  background: no-repeat center center;
  display: flex;
  align-items: center;
  justify-content: center;
}

.icon-more img {
  transition: filter 0.3s;
}

.icon-more:hover img {
  filter: brightness(0) saturate(100%) invert(55%) sepia(100%) saturate(102%) hue-rotate(180deg) brightness(105%) contrast(101%);
}

.signIn-chart-container {
  margin: 24px 30px 16px;
  padding: 16px 20px;
  background: #ffffff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.06);
  display: flex;
  gap: 16px;
  align-items: stretch;
}

.sign-chart {
  flex: 1.2;
  min-height: 320px;
}

.sign-lists {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.sign-list {
  background: #f8fafc;
  border-radius: 10px;
  padding: 10px 12px;
  border: 1px solid #e5e9f2;
}

.sign-list-title {
  font-size: 13px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 6px;
}

.sign-list-body {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.sign-tag {
  font-size: 12px;
}

.sign-empty {
  font-size: 12px;
  color: #909399;
}

@media (max-width: 992px) {
  .signIn-chart-container {
    flex-direction: column;
  }

  .sign-chart {
    min-height: 260px;
  }
}

/* 历史签到样式优化 */
.history-signIn {
  margin-left: 30px;
  margin-right: 30px;
}

.history-signIn .box-card {
  padding: 20px;
}

.history-signIn .box-card > span {
  display: block;
  margin-bottom: 20px;
}

/* 表格样式优化 */
.history-signIn .el-table {
  margin-top: 10px;
  border-radius: 8px;
  overflow: hidden;
}

.time-cell {
  display: flex;
  flex-direction: column;
  align-items: center;
  line-height: 1.2;
}

.time-cell > div:first-child {
  font-size: 13px;
}

.time-cell > div:last-child {
  font-size: 12px;
  color: #909399;
}
</style>