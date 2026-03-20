<template>
  <div>
    <div class="card" style="margin-bottom: 12px; display: flex; gap: 12px; align-items: center; flex-wrap: wrap;">
      <el-select
          v-model="data.paperId"
          placeholder="请选择试卷"
          style="width: 280px"
          clearable
          @change="loadStats"
      >
        <el-option
            v-for="p in data.papers"
            :key="p.id"
            :label="p.name"
            :value="p.id"
        />
      </el-select>

      <el-button type="info" plain @click="loadStats" :disabled="!data.paperId">
        刷新统计
      </el-button>
    </div>

    <el-alert
        v-if="!data.paperId"
        title="请先选择试卷"
        type="info"
        show-icon
        :closable="false"
    />

    <div v-else>
      <div class="card" style="margin-bottom: 12px;">
        <div style="font-weight: 700; margin-bottom: 8px;">已做学生</div>
        <el-table stripe :data="data.doneList" style="width: 100%;">
          <el-table-column prop="studentName" label="学生姓名" show-overflow-tooltip/>
          <el-table-column prop="score" label="得分" width="120" show-overflow-tooltip/>
        </el-table>
        <div v-if="!data.doneList.length" style="color: #999; padding: 12px 0;">暂无已做数据</div>
      </div>

      <div class="card" style="margin-bottom: 12px;">
        <div style="font-weight: 700; margin-bottom: 8px;">未做学生</div>
        <el-table stripe :data="data.undoneList" style="width: 100%;">
          <el-table-column prop="studentName" label="学生姓名" show-overflow-tooltip/>
        </el-table>
        <div v-if="!data.undoneList.length" style="color: #999; padding: 12px 0;">暂无未做数据</div>
      </div>

      <div class="card">
        <div style="font-weight: 700; margin-bottom: 8px;">客观题错误率排行</div>
        <el-table
            v-if="data.errorRankList.length"
            stripe
            :data="data.errorRankList"
            style="width: 100%;"
        >
          <el-table-column prop="questionName" label="题目" show-overflow-tooltip/>
          <el-table-column prop="answeredCount" label="作答次数" width="120"/>
          <el-table-column prop="wrongCount" label="错误次数" width="120"/>
          <el-table-column prop="errorRate" label="错误率(%)" width="140"/>
        </el-table>
        <div v-else style="color: #999; padding: 12px 0;">暂无错误率排行数据</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, onMounted } from "vue";
import { useRoute } from "vue-router";
import request from "@/utils/request.js";
import { ElMessage } from "element-plus";

const route = useRoute();

const round1 = (v) => {
  const num = Number(v);
  if (Number.isNaN(num)) return 0;
  return Math.round(num * 10) / 10;
};

const formatScore1 = (v) => {
  if (v === null || v === undefined || v === "") return "";
  const num = Number(v);
  if (Number.isNaN(num)) return "";
  return num.toFixed(1);
};

const safeParseArray = (text) => {
  if (!text) return [];
  try {
    const res = JSON.parse(text);
    return Array.isArray(res) ? res : [];
  } catch (e) {
    return [];
  }
};

const data = reactive({
  user: JSON.parse(localStorage.getItem("system-user") || "{}"),
  papers: [],
  paperId: null,

  courseName: route.query?.courseName ?? null,
  courseId: route.query?.id ?? route.params?.id ?? null,
  teacherId: null,

  doneList: [],
  undoneList: [],
  errorRankList: [],
});

data.teacherId = data.user?.id ?? null;

const loadPapers = async () => {
  if (!data.teacherId || !data.courseName) return;

  const res = await request.get("/testPaper/selectPage", {
    params: {
      pageNum: 1,
      pageSize: 100,
      teacherId: data.teacherId,
      courseName: data.courseName,
    },
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

  try {
    const [studentsRes, scoresRes, questionsRes] = await Promise.all([
      request.get("/student/selectByCourseId", {
        params: { courseId: data.courseId },
      }),
      request.get("/score/selectPage", {
        params: {
          pageNum: 1,
          pageSize: 1000,
          courseId: data.courseId,
          teacherId: data.teacherId,
          paperId: data.paperId,
        },
      }),
      request.get("/question/selectPage", {
        params: {
          pageNum: 1,
          pageSize: 300,
          courseId: data.courseId,
          teacherId: data.teacherId,
        },
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
      return {
        studentId: s.studentId,
        studentName: s.studentName,
        score: formatScore1(s.score),
      };
    });

    data.undoneList = students
        .filter((stu) => !doneStudentIdSet.has(stu.id))
        .map((stu) => ({ studentId: stu.id, studentName: stu.name }));

    // 客观题错误率排行：只统计已作答客观题（未作答不参与分母）
    const objectiveTypes = new Set(["单选题", "判断题"]);
    const statMap = new Map(); // questionId -> { answeredCount, wrongCount }

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
        if (String(studentAnswer).trim() !== String(correctAnswer).trim()) {
          cur.wrongCount += 1;
        }
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
        errorRate: errorRate,
      });
    }

    rankList.sort((a, b) => {
      if (b.errorRate !== a.errorRate) return b.errorRate - a.errorRate;
      return b.wrongCount - a.wrongCount;
    });

    data.errorRankList = rankList.slice(0, 10).map((x) => ({
      questionId: x.questionId,
      questionName: x.questionName,
      answeredCount: x.answeredCount,
      wrongCount: x.wrongCount,
      errorRate: `${x.errorRate.toFixed(1)}`,
    }));
  } catch (e) {
    ElMessage.error(e?.response?.data?.msg || e?.message || "统计加载失败");
  }
};

onMounted(async () => {
  await loadPapers();
  await loadStats();
});
</script>