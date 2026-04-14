<template>
  <div class="score-view-wrap">
    <!-- 顶部：筛选栏 -->
    <div class="toolbar-card">
      <div class="toolbar-left">
        <el-icon class="toolbar-icon"><DataAnalysis /></el-icon>
        <span class="toolbar-title">成绩分析</span>
        <span class="toolbar-sub" v-if="data.courseName">· {{ data.courseName }}</span>
      </div>
      <div class="toolbar-right">
        <el-select
          v-model="data.paperId"
          placeholder="请选择试卷"
          style="width: 260px"
          clearable
          @change="loadStats"
        >
          <el-option v-for="p in data.papers" :key="p.id" :label="p.name" :value="p.id" />
        </el-select>
        <el-button
          type="primary"
          :loading="data.loading"
          :disabled="!data.paperId"
          @click="loadStats"
        >
          <el-icon><Refresh /></el-icon>
          刷新统计
        </el-button>
      </div>
    </div>

    <!-- 未选试卷提示 -->
    <div v-if="!data.paperId" class="empty-tip">
      <el-empty description="请先从上方选择一份试卷，查看成绩分析" :image-size="100" />
    </div>

    <template v-else>
      <!-- 概览统计卡片 -->
      <div class="stat-row">
        <div class="stat-card stat-blue">
          <div class="stat-icon"><el-icon><User /></el-icon></div>
          <div class="stat-body">
            <div class="stat-value">{{ data.doneList.length }}</div>
            <div class="stat-label">已完成人数</div>
          </div>
        </div>
        <div class="stat-card stat-orange">
          <div class="stat-icon"><el-icon><Clock /></el-icon></div>
          <div class="stat-body">
            <div class="stat-value">{{ data.undoneList.length }}</div>
            <div class="stat-label">未完成人数</div>
          </div>
        </div>
        <div class="stat-card stat-green">
          <div class="stat-icon"><el-icon><TrendCharts /></el-icon></div>
          <div class="stat-body">
            <div class="stat-value">{{ data.avgScore }}</div>
            <div class="stat-label">平均分</div>
          </div>
        </div>
        <div class="stat-card stat-purple">
          <div class="stat-icon"><el-icon><Trophy /></el-icon></div>
          <div class="stat-body">
            <div class="stat-value">{{ data.maxScore }}</div>
            <div class="stat-label">最高分</div>
          </div>
        </div>
        <div class="stat-card stat-red">
          <div class="stat-icon"><el-icon><Warning /></el-icon></div>
          <div class="stat-body">
            <div class="stat-value">{{ data.minScore }}</div>
            <div class="stat-label">最低分</div>
          </div>
        </div>
        <div class="stat-card stat-teal">
          <div class="stat-icon"><el-icon><Finished /></el-icon></div>
          <div class="stat-body">
            <div class="stat-value">{{ data.passRate }}%</div>
            <div class="stat-label">及格率</div>
          </div>
        </div>
      </div>

      <!-- 图表区：两列 -->
      <div class="chart-row">
        <!-- 成绩分布柱状图 -->
        <div class="chart-card">
          <div class="chart-card-header">
            <el-icon class="header-icon green-icon"><PieChart /></el-icon>
            <span>成绩分布</span>
          </div>
          <div ref="scoreDistChart" class="chart-body"></div>
        </div>
        <!-- 完成状态饼图 -->
        <div class="chart-card">
          <div class="chart-card-header">
            <el-icon class="header-icon blue-icon"><DataLine /></el-icon>
            <span>完成情况</span>
          </div>
          <div ref="completionChart" class="chart-body"></div>
        </div>
      </div>

      <!-- 错题率横向柱状图 -->
      <div class="chart-card full-width-card" v-if="data.errorRankList.length">
        <div class="chart-card-header">
          <el-icon class="header-icon red-icon"><Warning /></el-icon>
          <span>客观题错误率排行 Top{{ data.errorRankList.length }}</span>
          <el-tag type="danger" size="small" style="margin-left: 8px">错误率越高需重点关注</el-tag>
        </div>
        <div ref="errorRankChart" style="height: 320px"></div>
      </div>

      <!-- 学生成绩明细 -->
      <div class="table-row">
        <!-- 已完成学生 -->
        <div class="table-card">
          <div class="table-card-header">
            <div class="header-left">
              <el-icon class="header-icon green-icon"><CircleCheck /></el-icon>
              <span>已完成学生</span>
              <el-badge :value="data.doneList.length" type="success" class="badge" />
            </div>
          </div>
          <el-table
            :data="data.doneList"
            stripe
            style="width: 100%"
            max-height="380"
            :row-class-name="tableRowClass"
          >
            <el-table-column type="index" label="#" width="50" />
            <el-table-column prop="studentName" label="学生姓名" show-overflow-tooltip />
            <el-table-column prop="score" label="得分" width="100" align="center">
              <template #default="scope">
                <el-tag
                  :type="scoreTagType(scope.row.score)"
                  size="small"
                  effect="light"
                >
                  {{ scope.row.score }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="评级" width="90" align="center">
              <template #default="scope">
                <span :class="gradeClass(scope.row.score)">{{ scoreGrade(scope.row.score) }}</span>
              </template>
            </el-table-column>
          </el-table>
          <div v-if="!data.doneList.length" class="table-empty">暂无已完成数据</div>
        </div>

        <!-- 未完成学生 -->
        <div class="table-card">
          <div class="table-card-header">
            <div class="header-left">
              <el-icon class="header-icon orange-icon"><Clock /></el-icon>
              <span>未完成学生</span>
              <el-badge :value="data.undoneList.length" type="warning" class="badge" />
            </div>
          </div>
          <el-table
            :data="data.undoneList"
            stripe
            style="width: 100%"
            max-height="380"
          >
            <el-table-column type="index" label="#" width="50" />
            <el-table-column prop="studentName" label="学生姓名" show-overflow-tooltip />
            <el-table-column label="状态" width="120" align="center">
              <template #default>
                <el-tag type="warning" size="small" effect="light">未提交</el-tag>
              </template>
            </el-table-column>
          </el-table>
          <div v-if="!data.undoneList.length" class="table-empty all-done-tip">
            <el-icon><CircleCheck /></el-icon>
            全部学生已完成！
          </div>
        </div>
      </div>

      <!-- 错题率明细表 -->
      <div class="table-card full-width-card" v-if="data.errorRankList.length">
        <div class="table-card-header">
          <div class="header-left">
            <el-icon class="header-icon red-icon"><List /></el-icon>
            <span>客观题错误率明细</span>
          </div>
        </div>
        <el-table :data="data.errorRankList" stripe style="width: 100%">
          <el-table-column type="index" label="排名" width="60" align="center" />
          <el-table-column prop="questionName" label="题目" show-overflow-tooltip />
          <el-table-column prop="answeredCount" label="作答人次" width="110" align="center" />
          <el-table-column prop="wrongCount" label="错误人次" width="110" align="center">
            <template #default="scope">
              <span style="color: #f56c6c; font-weight: 600">{{ scope.row.wrongCount }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="errorRate" label="错误率" width="160" align="center">
            <template #default="scope">
              <div class="error-rate-bar">
                <div
                  class="error-rate-fill"
                  :style="{ width: scope.row.errorRate + '%', background: errorRateColor(Number(scope.row.errorRate)) }"
                ></div>
                <span class="error-rate-text">{{ scope.row.errorRate }}%</span>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </template>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted, nextTick, watch } from "vue";
import { useRoute } from "vue-router";
import request from "@/utils/request.js";
import { ElMessage } from "element-plus";
import * as echarts from "echarts";
import {
  DataAnalysis, Refresh, User, Clock, TrendCharts, Trophy,
  Warning, Finished, PieChart, DataLine, CircleCheck, List
} from "@element-plus/icons-vue";

const route = useRoute();

// ECharts 实例引用
const scoreDistChart = ref(null);
const completionChart = ref(null);
const errorRankChart = ref(null);
let chartScoreDist = null;
let chartCompletion = null;
let chartErrorRank = null;

const round1 = (v) => {
  const num = Number(v);
  return Number.isNaN(num) ? 0 : Math.round(num * 10) / 10;
};
const formatScore1 = (v) => {
  if (v === null || v === undefined || v === "") return "";
  const num = Number(v);
  return Number.isNaN(num) ? "" : num.toFixed(1);
};
const safeParseArray = (text) => {
  if (!text) return [];
  try {
    const res = JSON.parse(text);
    return Array.isArray(res) ? res : [];
  } catch {
    return [];
  }
};

const data = reactive({
  user: JSON.parse(localStorage.getItem("system-user") || "{}"),
  papers: [],
  paperId: null,
  loading: false,
  courseName: route.query?.courseName ?? null,
  courseId: route.query?.id ?? route.params?.id ?? null,
  teacherId: null,
  doneList: [],
  undoneList: [],
  errorRankList: [],
  avgScore: "-",
  maxScore: "-",
  minScore: "-",
  passRate: "-",
});

data.teacherId = data.user?.id ?? null;

// 辅助方法
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
const tableRowClass = ({ row }) => {
  if (Number(row.score) < 60) return "row-fail";
  return "";
};
const errorRateColor = (rate) => {
  if (rate >= 70) return "#f56c6c";
  if (rate >= 40) return "#e6a23c";
  return "#67c23a";
};

// 计算概览统计
const calcStats = (scoreList) => {
  if (!scoreList.length) {
    data.avgScore = "-";
    data.maxScore = "-";
    data.minScore = "-";
    data.passRate = "-";
    return;
  }
  const scores = scoreList.map((s) => Number(s.score)).filter((n) => !Number.isNaN(n));
  data.avgScore = (scores.reduce((a, b) => a + b, 0) / scores.length).toFixed(1);
  data.maxScore = Math.max(...scores);
  data.minScore = Math.min(...scores);
  const passed = scores.filter((s) => s >= 60).length;
  data.passRate = ((passed / scores.length) * 100).toFixed(1);
};

// 成绩分布图
const renderScoreDistChart = () => {
  if (!scoreDistChart.value) return;
  if (!chartScoreDist) {
    chartScoreDist = echarts.init(scoreDistChart.value);
  }
  const buckets = [
    { label: "0-59\n不及格", range: [0, 59], color: "#f56c6c" },
    { label: "60-69\n及格", range: [60, 69], color: "#e6a23c" },
    { label: "70-79\n中等", range: [70, 79], color: "#409eff" },
    { label: "80-89\n良好", range: [80, 89], color: "#67c23a" },
    { label: "90-100\n优秀", range: [90, 100], color: "#7c3aed" },
  ];
  const counts = buckets.map((b) =>
    data.doneList.filter((s) => {
      const v = Number(s.score);
      return v >= b.range[0] && v <= b.range[1];
    }).length
  );
  chartScoreDist.setOption({
    tooltip: {
      trigger: "axis",
      formatter: (params) => `${params[0].name.replace("\n", " ")}<br/>人数：<b>${params[0].value}</b>`,
    },
    grid: { top: 20, right: 20, bottom: 50, left: 50 },
    xAxis: {
      type: "category",
      data: buckets.map((b) => b.label),
      axisLabel: { interval: 0, fontSize: 11 },
    },
    yAxis: {
      type: "value",
      minInterval: 1,
      name: "人数",
    },
    series: [
      {
        type: "bar",
        data: counts.map((v, i) => ({ value: v, itemStyle: { color: buckets[i].color, borderRadius: [6, 6, 0, 0] } })),
        barMaxWidth: 56,
        label: { show: true, position: "top", fontSize: 13, fontWeight: "bold" },
      },
    ],
  });
};

// 完成情况饼图
const renderCompletionChart = () => {
  if (!completionChart.value) return;
  if (!chartCompletion) {
    chartCompletion = echarts.init(completionChart.value);
  }
  const done = data.doneList.length;
  const undone = data.undoneList.length;
  chartCompletion.setOption({
    tooltip: { trigger: "item", formatter: "{b}: {c} 人 ({d}%)" },
    legend: { bottom: 10, left: "center" },
    series: [
      {
        type: "pie",
        radius: ["42%", "68%"],
        avoidLabelOverlap: false,
        padAngle: 3,
        itemStyle: { borderRadius: 8 },
        label: { show: false, position: "center" },
        emphasis: {
          label: { show: true, fontSize: 18, fontWeight: "bold" },
        },
        labelLine: { show: false },
        data: [
          { value: done, name: "已完成", itemStyle: { color: "#67c23a" } },
          { value: undone, name: "未完成", itemStyle: { color: "#e6a23c" } },
        ],
      },
    ],
  });
};

// 错误率横向柱状图
const renderErrorRankChart = () => {
  if (!errorRankChart.value || !data.errorRankList.length) return;
  if (!chartErrorRank) {
    chartErrorRank = echarts.init(errorRankChart.value);
  }
  const names = data.errorRankList.map((r) =>
    r.questionName.length > 12 ? r.questionName.substring(0, 12) + "…" : r.questionName
  );
  const rates = data.errorRankList.map((r) => Number(r.errorRate));
  chartErrorRank.setOption({
    tooltip: {
      trigger: "axis",
      formatter: (params) => `${data.errorRankList[params[0].dataIndex]?.questionName}<br/>错误率：<b>${params[0].value}%</b>`,
    },
    grid: { top: 10, right: 60, bottom: 20, left: 20, containLabel: true },
    xAxis: { type: "value", max: 100, axisLabel: { formatter: "{value}%" } },
    yAxis: { type: "category", data: [...names].reverse(), axisLabel: { fontSize: 12 } },
    series: [
      {
        type: "bar",
        data: [...rates].reverse().map((v) => ({
          value: v,
          itemStyle: {
            color: v >= 70 ? "#f56c6c" : v >= 40 ? "#e6a23c" : "#67c23a",
            borderRadius: [0, 6, 6, 0],
          },
        })),
        barMaxWidth: 28,
        label: { show: true, position: "right", formatter: "{c}%", fontSize: 12 },
      },
    ],
  });
};

const loadPapers = async () => {
  if (!data.teacherId || !data.courseName) return;
  const res = await request.get("/testPaper/selectPage", {
    params: { pageNum: 1, pageSize: 100, teacherId: data.teacherId, courseName: data.courseName },
  });
  if (res.code === "200") {
    data.papers = res.data?.list || [];
    if (!data.paperId && data.papers.length) {
      data.paperId = data.papers[0].id;
    }
  } else {
    ElMessage.error(res.msg || "试卷列表加载失败");
  }
};

const loadStats = async () => {
  if (!data.paperId || !data.courseId || !data.teacherId) return;
  data.loading = true;
  try {
    const [studentsRes, scoresRes, questionsRes] = await Promise.all([
      request.get("/student/selectByCourseId", { params: { courseId: data.courseId } }),
      request.get("/score/selectPage", {
        params: { pageNum: 1, pageSize: 1000, courseId: data.courseId, teacherId: data.teacherId, paperId: data.paperId },
      }),
      request.get("/question/selectPage", {
        params: { pageNum: 1, pageSize: 300, courseId: data.courseId, teacherId: data.teacherId },
      }),
    ]);

    const students = studentsRes.data || [];
    const scoreList = scoresRes.data?.list || [];
    const questionList = questionsRes.data?.list || [];

    const questionNameMap = new Map();
    for (const q of questionList) {
      if (q?.id != null) questionNameMap.set(q.id, q.name || `题目#${q.id}`);
    }

    const doneStudentIdSet = new Set();
    data.doneList = scoreList.map((s) => {
      doneStudentIdSet.add(s.studentId);
      return { studentId: s.studentId, studentName: s.studentName, score: formatScore1(s.score) };
    });

    data.undoneList = students
      .filter((stu) => !doneStudentIdSet.has(stu.id))
      .map((stu) => ({ studentId: stu.id, studentName: stu.name }));

    calcStats(scoreList);

    const objectiveTypes = new Set(["单选题", "判断题"]);
    const statMap = new Map();
    for (const s of scoreList) {
      const answerRows = safeParseArray(s.answer);
      for (const row of answerRows) {
        if (!row || row.questionId == null) continue;
        if (!objectiveTypes.has(row.typeName)) continue;
        const questionId = row.questionId;
        const studentAnswer = row.newAnswer;
        const correctAnswer = row.answer;
        const answered = studentAnswer != null && String(studentAnswer).trim() !== "";
        if (!answered) continue;
        const cur = statMap.get(questionId) || { answeredCount: 0, wrongCount: 0 };
        cur.answeredCount += 1;
        if (String(studentAnswer).trim() !== String(correctAnswer).trim()) cur.wrongCount += 1;
        statMap.set(questionId, cur);
      }
    }

    const rankList = [];
    for (const [questionId, v] of statMap.entries()) {
      const errorRate = v.answeredCount ? round1((100 * v.wrongCount) / v.answeredCount) : 0;
      rankList.push({
        questionId,
        questionName: questionNameMap.get(questionId) || `题目#${questionId}`,
        answeredCount: v.answeredCount,
        wrongCount: v.wrongCount,
        errorRate,
      });
    }
    rankList.sort((a, b) => b.errorRate !== a.errorRate ? b.errorRate - a.errorRate : b.wrongCount - a.wrongCount);
    data.errorRankList = rankList.slice(0, 10).map((x) => ({
      ...x,
      errorRate: `${x.errorRate.toFixed(1)}`,
    }));

    await nextTick();
    renderScoreDistChart();
    renderCompletionChart();
    renderErrorRankChart();
  } catch (e) {
    ElMessage.error(e?.response?.data?.msg || e?.message || "统计加载失败");
  } finally {
    data.loading = false;
  }
};

onMounted(async () => {
  await loadPapers();
  if (data.paperId) await loadStats();
});
</script>

<style scoped>
.score-view-wrap {
  padding: 16px;
  background: #f4f6fb;
  min-height: 100%;
}

/* 工具栏 */
.toolbar-card {
  background: #fff;
  border-radius: 12px;
  padding: 14px 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 12px;
  box-shadow: 0 2px 8px rgba(0,0,0,.06);
  margin-bottom: 16px;
}
.toolbar-left {
  display: flex;
  align-items: center;
  gap: 8px;
}
.toolbar-icon {
  font-size: 22px;
  color: #409eff;
}
.toolbar-title {
  font-size: 18px;
  font-weight: 700;
  color: #1a1a2e;
}
.toolbar-sub {
  font-size: 14px;
  color: #909399;
}
.toolbar-right {
  display: flex;
  align-items: center;
  gap: 10px;
}

/* 空状态 */
.empty-tip {
  background: #fff;
  border-radius: 12px;
  padding: 40px 0;
  text-align: center;
  box-shadow: 0 2px 8px rgba(0,0,0,.06);
}

/* 概览统计 */
.stat-row {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 12px;
  margin-bottom: 16px;
}
@media (max-width: 1200px) {
  .stat-row { grid-template-columns: repeat(3, 1fr); }
}
@media (max-width: 768px) {
  .stat-row { grid-template-columns: repeat(2, 1fr); }
}
.stat-card {
  border-radius: 12px;
  padding: 18px 16px;
  display: flex;
  align-items: center;
  gap: 14px;
  color: #fff;
  box-shadow: 0 4px 12px rgba(0,0,0,.1);
  transition: transform .2s, box-shadow .2s;
}
.stat-card:hover { transform: translateY(-3px); box-shadow: 0 8px 20px rgba(0,0,0,.15); }
.stat-blue   { background: linear-gradient(135deg, #409eff, #2b85e4); }
.stat-orange { background: linear-gradient(135deg, #e6a23c, #d4892a); }
.stat-green  { background: linear-gradient(135deg, #67c23a, #4caf22); }
.stat-purple { background: linear-gradient(135deg, #7c3aed, #6025d1); }
.stat-red    { background: linear-gradient(135deg, #f56c6c, #e44444); }
.stat-teal   { background: linear-gradient(135deg, #17c0b0, #0fa396); }
.stat-icon {
  background: rgba(255,255,255,.25);
  border-radius: 10px;
  padding: 10px;
  font-size: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.stat-value { font-size: 26px; font-weight: 800; line-height: 1; }
.stat-label { font-size: 12px; margin-top: 4px; opacity: .85; }

/* 图表行 */
.chart-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  margin-bottom: 16px;
}
@media (max-width: 900px) {
  .chart-row { grid-template-columns: 1fr; }
}
.chart-card {
  background: #fff;
  border-radius: 12px;
  padding: 16px 20px;
  box-shadow: 0 2px 8px rgba(0,0,0,.06);
}
.full-width-card {
  margin-bottom: 16px;
}
.chart-card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 700;
  font-size: 15px;
  color: #303133;
  margin-bottom: 12px;
}
.chart-body { height: 260px; }
.header-icon { font-size: 18px; }
.green-icon { color: #67c23a; }
.blue-icon  { color: #409eff; }
.red-icon   { color: #f56c6c; }
.orange-icon { color: #e6a23c; }

/* 学生表格行 */
.table-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  margin-bottom: 16px;
}
@media (max-width: 900px) {
  .table-row { grid-template-columns: 1fr; }
}
.table-card {
  background: #fff;
  border-radius: 12px;
  padding: 16px 20px;
  box-shadow: 0 2px 8px rgba(0,0,0,.06);
}
.table-card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}
.header-left {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 700;
  font-size: 15px;
  color: #303133;
}
.badge { margin-left: 4px; }
.table-empty {
  color: #999;
  text-align: center;
  padding: 24px 0;
  font-size: 14px;
}
.all-done-tip {
  color: #67c23a;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
}

/* 评级 */
.grade { font-size: 12px; font-weight: 600; padding: 2px 6px; border-radius: 4px; }
.grade-excellent { color: #7c3aed; background: #f3e8ff; }
.grade-good      { color: #67c23a; background: #f0f9eb; }
.grade-pass      { color: #409eff; background: #ecf5ff; }
.grade-fail      { color: #f56c6c; background: #fef0f0; }

/* 错误率进度条 */
.error-rate-bar {
  position: relative;
  height: 20px;
  background: #f5f5f5;
  border-radius: 10px;
  overflow: hidden;
}
.error-rate-fill {
  position: absolute;
  left: 0; top: 0; bottom: 0;
  border-radius: 10px;
  transition: width .4s;
}
.error-rate-text {
  position: absolute;
  right: 8px;
  top: 50%;
  transform: translateY(-50%);
  font-size: 12px;
  font-weight: 700;
  color: #303133;
  z-index: 1;
}

/* 不及格行高亮 */
:deep(.row-fail td) {
  background: #fff9f9 !important;
}
</style>
