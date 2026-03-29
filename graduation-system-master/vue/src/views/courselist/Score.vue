<template>
  <div class="score-wrap">
    <!-- 页头 -->
    <div class="page-header">
      <div class="header-left">
        <div class="header-icon-wrap">
          <el-icon><Trophy /></el-icon>
        </div>
        <div>
          <div class="header-title">我的成绩</div>
          <div class="header-sub">共参加 {{ data.total }} 次考试</div>
        </div>
      </div>
    </div>

    <!-- 概览 -->
    <div class="stat-row" v-if="data.tableData.length">
      <div class="stat-card stat-blue">
        <div class="stat-icon"><el-icon><Document /></el-icon></div>
        <div class="stat-body">
          <div class="stat-value">{{ data.total }}</div>
          <div class="stat-label">参考次数</div>
        </div>
      </div>
      <div class="stat-card stat-green">
        <div class="stat-icon"><el-icon><TrendCharts /></el-icon></div>
        <div class="stat-body">
          <div class="stat-value">{{ myAvgScore }}</div>
          <div class="stat-label">平均分</div>
        </div>
      </div>
      <div class="stat-card stat-purple">
        <div class="stat-icon"><el-icon><Trophy /></el-icon></div>
        <div class="stat-body">
          <div class="stat-value">{{ myMaxScore }}</div>
          <div class="stat-label">最高分</div>
        </div>
      </div>
      <div class="stat-card stat-teal">
        <div class="stat-icon"><el-icon><CircleCheck /></el-icon></div>
        <div class="stat-body">
          <div class="stat-value">{{ myGraded }}</div>
          <div class="stat-label">已批改</div>
        </div>
      </div>
    </div>

    <!-- 主内容区：表格 + 图表 -->
    <div class="main-grid" v-if="data.tableData.length">
      <!-- 成绩列表 -->
      <div class="table-card">
        <div class="table-card-header">
          <el-icon class="header-icon blue-icon"><List /></el-icon>
          <span>考试记录</span>
        </div>
        <el-table
          :data="data.tableData"
          stripe
          style="width: 100%"
          :row-class-name="rowClass"
        >
          <el-table-column type="index" label="#" width="50" align="center" />
          <el-table-column prop="name" label="试卷名称" show-overflow-tooltip min-width="160" />
          <el-table-column prop="courseName" label="课程名称" show-overflow-tooltip width="140" />
          <el-table-column prop="teacherName" label="授课教师" show-overflow-tooltip width="110" />
          <el-table-column label="状态" width="110" align="center">
            <template #default="scope">
              <el-tag
                v-if="scope.row.status === '已阅卷'"
                type="success"
                size="small"
                effect="light"
              >
                <el-icon style="margin-right:2px"><CircleCheck /></el-icon>已批改
              </el-tag>
              <el-tag
                v-else
                type="warning"
                size="small"
                effect="light"
              >
                <el-icon style="margin-right:2px"><Clock /></el-icon>待批改
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="分数" width="120" align="center">
            <template #default="scope">
              <template v-if="scope.row.score !== null && scope.row.score !== ''">
                <el-tag
                  :type="scoreTagType(scope.row.score)"
                  size="small"
                  effect="dark"
                >
                  {{ scope.row.score }} 分
                </el-tag>
              </template>
              <span v-else style="color: #bbb">-</span>
            </template>
          </el-table-column>
          <el-table-column label="评级" width="90" align="center">
            <template #default="scope">
              <span v-if="scope.row.score !== null && scope.row.score !== ''" :class="gradeClass(scope.row.score)">
                {{ scoreGrade(scope.row.score) }}
              </span>
              <span v-else style="color:#bbb">-</span>
            </template>
          </el-table-column>
        </el-table>

        <!-- 分页 -->
        <div class="pagination-wrap" v-if="data.total > data.pageSize">
          <el-pagination
            @current-change="load"
            background
            layout="prev, pager, next"
            :page-size="data.pageSize"
            v-model:current-page="data.pageNum"
            :total="data.total"
          />
        </div>
      </div>

      <!-- 右侧图表 -->
      <div class="chart-col">
        <!-- 成绩分布饼图 -->
        <div class="chart-card">
          <div class="chart-card-header">
            <el-icon class="header-icon green-icon"><PieChart /></el-icon>
            <span>成绩分布</span>
          </div>
          <div ref="gradeChartEl" style="height: 220px"></div>
        </div>
        <!-- 分数趋势折线图 -->
        <div class="chart-card">
          <div class="chart-card-header">
            <el-icon class="header-icon blue-icon"><DataLine /></el-icon>
            <span>分数趋势</span>
          </div>
          <div ref="trendChartEl" style="height: 220px"></div>
        </div>
      </div>
    </div>

    <!-- 空状态 -->
    <div v-if="!data.tableData.length && !data.loading" class="empty-wrap">
      <el-empty description="还没有参加过任何考试" :image-size="120" />
    </div>
  </div>
</template>

<script setup>
import { reactive, computed, ref, nextTick, onMounted } from "vue";
import request from "@/utils/request.js";
import { ElMessage } from "element-plus";
import * as echarts from "echarts";
import {
  Trophy, Document, TrendCharts, CircleCheck, Clock,
  List, PieChart, DataLine
} from "@element-plus/icons-vue";

const gradeChartEl = ref(null);
const trendChartEl = ref(null);
let chartGrade = null;
let chartTrend = null;

const data = reactive({
  user: JSON.parse(localStorage.getItem("system-user") || "{}"),
  tableData: [],
  pageNum: 1,
  pageSize: 8,
  total: 0,
  loading: false,
});

const scoreTagType = (score) => {
  const s = Number(score);
  if (s >= 90) return "success";
  if (s >= 60) return "primary";
  return "danger";
};
const scoreGrade = (score) => {
  const s = Number(score);
  if (s >= 90) return "优秀";
  if (s >= 80) return "良好";
  if (s >= 60) return "及格";
  return "不及格";
};
const gradeClass = (score) => {
  const s = Number(score);
  if (s >= 90) return "grade grade-excellent";
  if (s >= 80) return "grade grade-good";
  if (s >= 60) return "grade grade-pass";
  return "grade grade-fail";
};
const rowClass = ({ row }) => {
  if (row.score !== null && row.score !== "" && Number(row.score) < 60) return "row-fail";
  return "";
};

// 计算概览
const scoredList = computed(() =>
  data.tableData.filter((r) => r.score !== null && r.score !== "" && !Number.isNaN(Number(r.score)))
);
const myAvgScore = computed(() => {
  if (!scoredList.value.length) return "-";
  const s = scoredList.value.reduce((a, b) => a + Number(b.score), 0);
  return (s / scoredList.value.length).toFixed(1);
});
const myMaxScore = computed(() => {
  if (!scoredList.value.length) return "-";
  return Math.max(...scoredList.value.map((r) => Number(r.score)));
});
const myGraded = computed(() => data.tableData.filter((r) => r.status === "已阅卷").length);

// 饼图：评级分布
const renderGradeChart = () => {
  if (!gradeChartEl.value) return;
  if (!chartGrade) chartGrade = echarts.init(gradeChartEl.value);
  const grades = { 优秀: 0, 良好: 0, 及格: 0, 不及格: 0 };
  for (const r of scoredList.value) {
    grades[scoreGrade(r.score)]++;
  }
  chartGrade.setOption({
    tooltip: { trigger: "item", formatter: "{b}: {c} 次 ({d}%)" },
    legend: { bottom: 0, left: "center", textStyle: { fontSize: 12 } },
    series: [
      {
        type: "pie",
        radius: ["38%", "62%"],
        center: ["50%", "44%"],
        padAngle: 3,
        itemStyle: { borderRadius: 6 },
        label: { show: false },
        emphasis: { label: { show: true, fontSize: 16, fontWeight: "bold" } },
        data: [
          { value: grades["优秀"], name: "优秀", itemStyle: { color: "#7c3aed" } },
          { value: grades["良好"], name: "良好", itemStyle: { color: "#67c23a" } },
          { value: grades["及格"], name: "及格", itemStyle: { color: "#409eff" } },
          { value: grades["不及格"], name: "不及格", itemStyle: { color: "#f56c6c" } },
        ],
      },
    ],
  });
};

// 折线图：分数趋势
const renderTrendChart = () => {
  if (!trendChartEl.value) return;
  if (!chartTrend) chartTrend = echarts.init(trendChartEl.value);
  const list = [...scoredList.value].slice(-10);
  chartTrend.setOption({
    tooltip: { trigger: "axis", formatter: (p) => `${p[0].name}<br/>分数：<b>${p[0].value}</b>` },
    grid: { top: 16, right: 16, bottom: 36, left: 40 },
    xAxis: {
      type: "category",
      data: list.map((r, i) => r.name?.substring(0, 6) || `考试${i + 1}`),
      axisLabel: { interval: 0, rotate: 30, fontSize: 10 },
    },
    yAxis: { type: "value", min: 0, max: 100, splitLine: { lineStyle: { type: "dashed" } } },
    series: [
      {
        type: "line",
        data: list.map((r) => Number(r.score)),
        smooth: true,
        symbol: "circle",
        symbolSize: 8,
        lineStyle: { color: "#409eff", width: 3 },
        itemStyle: { color: "#409eff" },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: "rgba(64,158,255,.35)" },
            { offset: 1, color: "rgba(64,158,255,.02)" },
          ]),
        },
        markLine: {
          silent: true,
          data: [{ yAxis: 60, lineStyle: { color: "#f56c6c", type: "dashed" }, label: { formatter: "及格线" } }],
        },
      },
    ],
  });
};

const load = async () => {
  data.loading = true;
  const res = await request.get("/score/selectPage", {
    params: {
      pageNum: data.pageNum,
      pageSize: data.pageSize,
      studentId: data.user?.id,
    },
  });
  data.loading = false;
  if (res.code === "200") {
    data.tableData = res.data.list;
    data.total = res.data.total;
    await nextTick();
    renderGradeChart();
    renderTrendChart();
  } else {
    ElMessage.error(res.msg);
  }
};

onMounted(() => load());
</script>

<style scoped>
.score-wrap {
  padding: 16px;
  background: #f4f6fb;
  min-height: 100%;
}

/* 页头 */
.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}
.header-left {
  display: flex;
  align-items: center;
  gap: 14px;
}
.header-icon-wrap {
  width: 46px;
  height: 46px;
  border-radius: 12px;
  background: linear-gradient(135deg, #7c3aed, #6025d1);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 22px;
  box-shadow: 0 4px 12px rgba(124,58,237,.3);
}
.header-title {
  font-size: 20px;
  font-weight: 800;
  color: #1a1a2e;
}
.header-sub {
  font-size: 13px;
  color: #909399;
  margin-top: 2px;
}

/* 概览 */
.stat-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
  margin-bottom: 16px;
}
@media (max-width: 900px) {
  .stat-row { grid-template-columns: repeat(2, 1fr); }
}
.stat-card {
  border-radius: 12px;
  padding: 16px;
  display: flex;
  align-items: center;
  gap: 12px;
  color: #fff;
  box-shadow: 0 4px 12px rgba(0,0,0,.1);
  transition: transform .2s;
}
.stat-card:hover { transform: translateY(-2px); }
.stat-blue   { background: linear-gradient(135deg, #409eff, #2b85e4); }
.stat-green  { background: linear-gradient(135deg, #67c23a, #4caf22); }
.stat-purple { background: linear-gradient(135deg, #7c3aed, #6025d1); }
.stat-teal   { background: linear-gradient(135deg, #17c0b0, #0fa396); }
.stat-icon {
  background: rgba(255,255,255,.25);
  border-radius: 10px;
  padding: 10px;
  font-size: 22px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.stat-value { font-size: 24px; font-weight: 800; line-height: 1; }
.stat-label { font-size: 12px; margin-top: 4px; opacity: .85; }

/* 主内容网格 */
.main-grid {
  display: grid;
  grid-template-columns: 1fr 300px;
  gap: 16px;
  align-items: start;
}
@media (max-width: 1100px) {
  .main-grid { grid-template-columns: 1fr; }
}

/* 表格卡片 */
.table-card {
  background: #fff;
  border-radius: 12px;
  padding: 16px 20px;
  box-shadow: 0 2px 8px rgba(0,0,0,.06);
}
.table-card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 700;
  font-size: 15px;
  color: #303133;
  margin-bottom: 12px;
}
.pagination-wrap {
  display: flex;
  justify-content: center;
  margin-top: 16px;
}

/* 右侧图表列 */
.chart-col {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.chart-card {
  background: #fff;
  border-radius: 12px;
  padding: 16px;
  box-shadow: 0 2px 8px rgba(0,0,0,.06);
}
.chart-card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 700;
  font-size: 14px;
  color: #303133;
  margin-bottom: 8px;
}
.header-icon { font-size: 17px; }
.green-icon { color: #67c23a; }
.blue-icon  { color: #409eff; }

/* 评级 */
.grade { font-size: 12px; font-weight: 600; padding: 2px 6px; border-radius: 4px; }
.grade-excellent { color: #7c3aed; background: #f3e8ff; }
.grade-good      { color: #67c23a; background: #f0f9eb; }
.grade-pass      { color: #409eff; background: #ecf5ff; }
.grade-fail      { color: #f56c6c; background: #fef0f0; }

/* 不及格行高亮 */
:deep(.row-fail td) {
  background: #fff9f9 !important;
}

/* 空状态 */
.empty-wrap {
  background: #fff;
  border-radius: 12px;
  padding: 60px 0;
  text-align: center;
  box-shadow: 0 2px 8px rgba(0,0,0,.06);
}
</style>
