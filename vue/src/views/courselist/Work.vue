<template>
  <div class="homework-page">
    <el-card class="box-card homework-card">
      <template #header>
        <div class="card-header">
          <div class="card-header-text">
            <span class="card-title">实验作业</span>
            <p class="card-subtitle">按状态快速筛选、批改和导出实验作业，提高实验教学管理效率</p>
          </div>
        </div>
      </template>
      <!-- 筛选与搜索 -->
      <div class="filter-container">
        <div class="filter-options">
          <span class="filter-label">筛选</span>
          <el-radio-group v-model="filterOption" size="small">
            <el-radio-button label="全部">全部</el-radio-button>
            <el-radio-button label="待提交">待提交</el-radio-button>
            <el-radio-button label="待审核">待审核</el-radio-button>
            <el-radio-button label="待修改">待修改</el-radio-button>
            <el-radio-button label="审核通过">审核通过</el-radio-button>
          </el-radio-group>
        </div>

        <div class="search-options">
          <el-input
            v-model="data.name"
            class="search-input"
            placeholder="请输入实验名称查询"
            clearable
            @keyup.enter="load"
          />
          <el-button type="primary" @click="load">查询</el-button>
          <el-button @click="reset">重置</el-button>
          <el-button type="primary" @click="handleAdd" v-if="isTeacherUser">新增</el-button>
        </div>
      </div>
      <el-table :data="filteredAssignments" style="width: 100%" stripe>
        <el-table-column label="" width="60" align="center">
          <template #default="scope">
            <el-tag class="info" type="info"></el-tag>
          </template>
        </el-table-column>
        <el-table-column label="实验名称" min-width="160" :show-overflow-tooltip="{ popperClass: 'work-tooltip-popper' }">
          <template #default="scope">
            <div style="display: flex; flex-direction: column; gap: 6px;">
              <div>{{ scope.row.name }}</div>
              <el-tag
                  class="exp-similarity-tag"
                  v-if="experimentSimilarityMap[scope.row.id]"
                  :type="parseFloat(experimentSimilarityMap[scope.row.id]) > 80 ? 'danger' : parseFloat(experimentSimilarityMap[scope.row.id]) > 50 ? 'warning' : 'success'"
              >
                相似度: {{ experimentSimilarityMap[scope.row.id] }}
              </el-tag>
              <el-tag class="exp-similarity-tag" v-else type="info">未分析</el-tag>
            </div>
          </template>
        </el-table-column>
        <!-- 隐藏：实验内容（content） -->
        <el-table-column label="授课教师" prop="teacherName" width="120"></el-table-column>
        <el-table-column label="学生名称" prop="studentName" width="120"></el-table-column>
        <!-- 隐藏：提交内容（scontent）与学分（score） -->
        <el-table-column label="最后提交或修改时间" prop="studentLastSubmitTime" width="170">
          <template #default="scope">
            {{ formatDateTime(scope.row.studentLastSubmitTime) }}
          </template>
        </el-table-column>
        <el-table-column label="修改意见" prop="amendment" :show-overflow-tooltip="{ popperClass: 'work-tooltip-popper' }"></el-table-column>
        <el-table-column label="分段进度" width="120" show-overflow-tooltip>
          <template #default="scope">
            <span>{{ phaseProgressLabel(scope.row) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="审核状态">
          <template #default="scope">
            <span v-if="!scope.row.state && !isContentDone(scope.row)">待提交</span>
            <span v-else-if="!scope.row.state && isContentDone(scope.row)">待审核</span>
            <span v-else-if="scope.row.state === '审核通过'">审核通过</span>
            <span v-else-if="scope.row.state === '未通过'">待修改</span>
            <span v-else>{{ scope.row.state }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" width="220">
          <template #default="scope">
            <div class="op-actions">
              <div class="op-actions-left">
                <!-- 学生按钮：提交/修改 -->
                <template v-if="isStudentUser">
                  <el-button
                      type="primary"
                      size="small"
                      class="op-btn"
                      @click="handleEdit(scope.row)"
                      v-if="!scope.row.state && !isContentDone(scope.row)"
                  >
                    提交
                  </el-button>
                  <el-button
                      type="primary"
                      size="small"
                      class="op-btn"
                      @click="handleEdit(scope.row)"
                      v-else-if="!scope.row.state && isContentDone(scope.row)"
                  >
                    修改
                  </el-button>
                  <el-button
                      type="primary"
                      size="small"
                      class="op-btn"
                      @click="handleEdit(scope.row)"
                      v-else-if="scope.row.state === '未通过'"
                  >
                    修改
                  </el-button>
                </template>
                <!-- 教师按钮：审核、删除 -->
                <template v-if="isTeacherUser">
                  <el-button
                      type="warning"
                      size="small"
                      class="op-btn"
                      @click="handleEdit(scope.row)"
                  >
                    评审
                  </el-button>
                  <el-button
                      type="danger"
                      size="small"
                      class="op-btn"
                      @click="handleDelete(scope.row.id)"
                  >
                    删除
                  </el-button>
                </template>
              </div>
              <div class="op-actions-right">
                <el-tooltip content="下载实验报告" placement="top" :show-after="150">
                  <el-button
                      type="primary"
                      size="small"
                      class="op-btn op-btn-icon op-btn-download"
                      circle
                      @click="exportToWord(scope.row)"
                  >
                    <el-icon><Download /></el-icon>
                  </el-button>
                </el-tooltip>
              </div>
            </div>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-container">
        <el-pagination
            background
            layout="total, sizes, prev, pager, next"
            v-model:page-size="data.pageSize"
            v-model:current-page="data.pageNum"
            :total="data.total"
            :page-sizes="[5, 10, 20, 50, 100]"
            @current-change="changePage"
            @size-change="handleSizeChange"
        />
      </div>
    </el-card>
    <el-dialog
        title="作业信息"
        width="40%"
        v-model="data.formVisible"
        :close-on-click-modal="false"
        destroy-on-close
    >
      <el-form :model="data.form" label-width="120px" style="padding-right: 24px">
      <el-form-item v-if="isTeacherUser && similarityHint.visible" label="查重提示">
        <el-alert
            :title="similarityHint.title"
            :type="similarityHint.type"
            :closable="false"
            show-icon
            style="width: 100%;"
        />
      </el-form-item>

        <!-- 学生 · 实验任务：按教师题目作答（tip1-tip9） -->
        <template v-if="isStudentUser && data.form.lab === 2">
          <el-alert
            type="info"
            :closable="false"
            show-icon
            :title="`请根据教师发布的实验阶段逐一作答，完成当前阶段后才能进行下一阶段。共 ${taskQuestions.length} 个阶段。`"
            style="margin-bottom: 16px"
          />
          <div class="form-section">
            <div class="section-title">📋 教师发布的实验信息</div>
            <el-form-item label="任务名称">
              <el-input v-model="data.form.name" type="textarea" :rows="2" disabled />
            </el-form-item>
            <el-form-item label="上机地点">
              <el-input v-model="data.form.taskPlace" disabled />
            </el-form-item>
            <el-form-item label="上机时间">
              <el-input v-model="data.form.taskTime" disabled />
            </el-form-item>
            <el-form-item label="上机内容">
              <el-input v-model="data.form.taskContent" type="textarea" :rows="3" disabled />
            </el-form-item>
            <el-form-item label="实验目的">
              <el-input v-model="data.form.taskPurpose" type="textarea" :rows="4" disabled />
            </el-form-item>
            <el-form-item label="实验要求">
              <el-input v-model="data.form.taskRequirement" type="textarea" :rows="4" disabled />
            </el-form-item>
            <el-form-item label="实验环境">
              <el-input v-model="data.form.taskEnvironment" type="textarea" :rows="4" disabled />
            </el-form-item>
          </div>

          <!-- 动态实验阶段作答区（逐阶段提交模式） -->
          <div class="form-section" v-if="taskQuestions && taskQuestions.length > 0">
            <div class="section-title">📝 实验阶段作答（共 {{ taskQuestions.length }} 个阶段）</div>
            
            <!-- 逐题显示：只显示当前题目或已完成的题目 -->
            <div v-for="(q, index) in taskQuestions" :key="index" class="question-answer-section" 
                 v-show="isQuestionVisible(index)">
              <el-divider v-if="index > 0" />
              <el-form-item :label="`阶段 ${index + 1}`">
                <div class="question-text">{{ q.question }}</div>
              </el-form-item>
              <el-form-item :label="`我的解答`" required>
                <el-input
                  v-model="studentAnswers[index]"
                  type="textarea"
                  :rows="4"
                  maxlength="2000"
                  show-word-limit
                  :placeholder="`请回答阶段 ${index + 1}`"
                  :disabled="isQuestionSubmitted(index) || studentStageLocked"
                />
              </el-form-item>
              <!-- 当前题目的提交按钮 -->
              <el-form-item v-if="!isQuestionSubmitted(index) && !studentStageLocked && isCurrentQuestion(index)">
                <el-button 
                  type="primary" 
                  :loading="submittingQuestionIndex === index"
                  @click="submitSingleQuestion(index)"
                  :disabled="!studentAnswers[index] || !studentAnswers[index].trim()"
                >
                  提交阶段 {{ index + 1 }}
                </el-button>
                <span class="submit-hint" v-if="index < taskQuestions.length - 1">
                  提交后可继续作答下一题
                </span>
                <span class="submit-hint" v-else>
                  最后一题，提交后请填写心得体会
                </span>
              </el-form-item>
              <!-- 已提交标记 -->
              <el-form-item v-else-if="isQuestionSubmitted(index)">
                <el-tag type="success">
                  <el-icon><Check /></el-icon> 已提交
                </el-tag>
                <el-button 
                  type="warning" 
                  size="small" 
                  style="margin-left: 10px;"
                  @click="editSubmittedQuestion(index)"
                >
                  修改提交
                </el-button>
              </el-form-item>
            </div>
          </div>

          <!-- 心得体会区域：只有所有题目都提交后才显示 -->
          <div class="form-section" v-if="allQuestionsSubmitted && !studentStageLocked">
            <div class="section-title">💭 实验总结与心得体会</div>
            <el-form-item label="心得体会" required>
              <el-input
                v-model="data.form.experience"
                type="textarea"
                :rows="6"
                maxlength="2000"
                show-word-limit
                placeholder="请填写本次实验的心得体会和收获"
              />
            </el-form-item>
            <el-form-item>
              <el-button type="success" :loading="stageSaving === 1" @click="saveStudentAnswers">
                提交作业
              </el-button>
            </el-form-item>
          </div>

          <!-- 提示信息：还有题目未完成 -->
          <div class="form-section" v-else-if="!allQuestionsSubmitted && !studentStageLocked">
            <el-alert
              type="warning"
              :closable="false"
              show-icon
              :title="`请先完成题目 ${currentQuestionIndex + 1} 的作答并提交`"
            />
          </div>
        </template>

        <!-- 教师 / 非分段 -->
        <template v-else>
        <div class="form-section">
          <el-form-item label="* 实验题目">
            <el-input
                v-model="data.form.name"
                type="textarea"
                :rows="2"
                show-word-limit
                maxlength="50"
                placeholder="请输入实验题目"
                :disabled="isStudentUser"
            />
          </el-form-item>
        </div>
        <!-- 非实验作业显示任务内容 -->
        <el-form-item label="任务内容" prop="content" v-if="data.form.lab !== 2">
          <el-input v-model="data.form.content" type="textarea" :rows="3" autocomplete="off" :disabled="isStudentUser" />
        </el-form-item>
        <!-- 教师评审：和学生提交作业相同的布局，但添加评分功能 -->
        <template v-if="isTeacherUser && data.form.lab === 2">
          <!-- 实验任务信息（只读） -->
          <div class="form-section">
            <div class="section-title">📋 实验任务信息</div>
            <el-form-item label="上机内容">
              <el-input v-model="data.form.taskContent" type="textarea" :rows="3" disabled />
            </el-form-item>
            <el-form-item label="实验目的">
              <el-input v-model="data.form.taskPurpose" type="textarea" :rows="4" disabled />
            </el-form-item>
            <el-form-item label="实验要求">
              <el-input v-model="data.form.taskRequirement" type="textarea" :rows="4" disabled />
            </el-form-item>
            <el-form-item label="实验环境">
              <el-input v-model="data.form.taskEnvironment" type="textarea" :rows="4" disabled />
            </el-form-item>
          </div>

          <!-- 动态实验阶段（和学生端一样，但添加评分） -->
          <div class="form-section" v-if="taskQuestions && taskQuestions.length > 0">
            <div class="section-title">📝 实验阶段作答（共 {{ taskQuestions.length }} 个阶段）</div>
            
            <div v-for="(q, index) in taskQuestions" :key="index" class="question-answer-section">
              <el-divider v-if="index > 0" />
              <!-- 阶段题目 -->
              <el-form-item :label="`阶段 ${index + 1}`">
                <div class="question-text">{{ q.question }}</div>
              </el-form-item>
              <!-- 学生作答（只读） -->
              <el-form-item :label="`学生作答`">
                <el-input
                  v-model="studentAnswers[index]"
                  type="textarea"
                  :rows="4"
                  disabled
                />
              </el-form-item>
              <!-- 阶段评分 -->
              <el-form-item :label="`阶段 ${index + 1} 评分`">
                <el-input-number 
                  v-model="stageScores['stage' + (index + 1)]" 
                  :min="0" 
                  :max="100" 
                  :precision="1"
                  placeholder="请输入分数"
                  style="width: 150px;"
                />
                <span class="score-hint">分</span>
              </el-form-item>
            </div>
          </div>

          <!-- 心得体会 -->
          <div class="form-section">
            <div class="section-title">💭 实验总结与心得体会</div>
            <el-form-item label="心得体会">
              <el-input
                v-model="data.form.experience"
                type="textarea"
                :rows="6"
                disabled
              />
            </el-form-item>
          </div>
        </template>
        
        <!-- 学生编辑/提交 -->
        <template v-if="isStudentUser && data.form.lab === 2">
          <div class="form-section">
            <el-form-item label="* 实验目的">
              <el-input
                  v-model="data.form.scontent"
                  type="textarea"
                  :rows="4"
                  show-word-limit
                  maxlength="500"
                  placeholder="学生无需填写"
                  disabled
              />
            </el-form-item>
            <el-form-item label="* 实验环境">
              <el-input
                  v-model="data.form.tip1"
                  type="textarea"
                  :rows="4"
                  show-word-limit
                  maxlength="500"
                  placeholder="请输入实验环境"
              />
            </el-form-item>
            <el-form-item label="* 实验内容和步骤">
              <el-input
                  v-model="data.form.tip2"
                  type="textarea"
                  :rows="6"
                  show-word-limit
                  maxlength="1000"
                  placeholder="请输入实验内容和步骤"
              />
            </el-form-item>
          </div>
          <div class="form-section">
            <el-form-item label="* 实验总结和心得体会">
              <el-input
                  v-model="data.form.tip3"
                  type="textarea"
                  :rows="6"
                  show-word-limit
                  maxlength="1000"
                  placeholder="请输入实验总结和心得体会"
              />
            </el-form-item>
          </div>
        </template>
        </template>

        <!-- 教师评分汇总（实验作业） -->
        <div class="form-section" v-if="isTeacherUser && data.form.lab === 2">
          <div class="section-title">📊 评分汇总</div>
          <el-form-item label="总分">
            <el-input 
              v-model="data.form.score" 
              disabled
              style="width: 150px;"
            />
            <span class="score-hint">分（各阶段分数自动累加）</span>
          </el-form-item>
        </div>

        <!-- 非实验作业或旧版评分 -->
        <el-form-item label="评分" prop="score" v-if="isTeacherUser && data.form.lab !== 2">
          <el-input v-model="data.form.score" autocomplete="off"/>
        </el-form-item>
        <el-form-item label="修改意见" prop="amendment" v-if="isTeacherUser">
          <el-input v-model="data.form.amendment" autocomplete="off"/>
        </el-form-item>
        <el-form-item label="教师评价" prop="teacherComment" v-if="isTeacherUser">
          <div class="teacher-comment-row">
            <el-input
                v-model="data.form.teacherComment"
                type="textarea"
                :rows="3"
                maxlength="800"
                show-word-limit
                placeholder="请输入教师评价（可点击右侧 AI 润色）"
            />
            <el-tooltip content="AI润色教师评价" placement="top" :show-after="150">
              <el-button
                  class="op-btn op-btn-icon"
                  type="primary"
                  :loading="polishLoading"
                  @click="polishTeacherComment"
              >
                <el-icon><MagicStick /></el-icon>
              </el-button>
            </el-tooltip>
          </div>
        </el-form-item>
        <el-form-item label="审核结果" prop="state" :rules="[{ required: true, message: '请选择审核是否通过', trigger: 'change' }]" v-if="isTeacherUser">
          <el-select v-model="data.form.state" placeholder="审核是否通过">
            <el-option label="审核通过" value="审核通过"/>
            <el-option label="未通过" value="未通过"/>
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="data.formVisible = false">取 消</el-button>
          <el-button
              type="primary"
              v-if="!(isStudentUser && data.form.lab === 2)"
              @click="save"
          >
            提交
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 已移除：分段提交统计（阶段统计）弹窗 -->
  </div>
</template>

<script setup>
import request from "@/utils/request";
import { ElMessage, ElMessageBox } from "element-plus";
import Docxtemplater from "docxtemplater";
import PizZip from "pizzip";
import ImageModule from "docxtemplater-image-module-free";
import { saveAs } from "file-saver";
import { reactive, computed, ref, watch } from "vue";
import { useRouter, useRoute } from "vue-router";
import { Plus, Download, MagicStick, Check } from "@element-plus/icons-vue";
import { getUploadUrl } from '@/utils/appConfig'

const router = useRouter();
const route = useRoute();

const showExpFormDialog = () => {
  router.push({
    path: "/detail/document",
    query: {
      name: data.form.name,
      purpose: data.form.content,
      environment: data.expForm.environment,
      contentAndSteps: data.expForm.contentAndSteps,
      summaryAndReflection: data.expForm.summaryAndReflection,
      attachment: data.expForm.attachment,
    },
  });
};

const data = reactive({
  pageNum: 1,
  pageSize: 5,
  total: 0,
  formVisible: false,
  form: {},
  tableData: [],
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

// 教师发布的题目列表
const taskQuestions = ref([]);
// 学生的答案数组（对应tip1-tip9）
const studentAnswers = ref([]);
// 记录已提交的题目索引
const submittedQuestions = ref([]);
// 当前正在提交的题目索引
const submittingQuestionIndex = ref(-1);

// 教师评审阶段分数（不保存到数据库，仅用于计算总分）
const stageScores = ref({
  stage1: null,
  stage2: null,
  stage3: null,
  stage4: null,
  stage5: null,
  stage6: null,
  stage7: null,
  stage8: null,
  stage9: null,
});

// 计算总分（阶段分数之和）
const totalScore = computed(() => {
  const s1 = Number(stageScores.value.stage1) || 0;
  const s2 = Number(stageScores.value.stage2) || 0;
  const s3 = Number(stageScores.value.stage3) || 0;
  const s4 = Number(stageScores.value.stage4) || 0;
  const s5 = Number(stageScores.value.stage5) || 0;
  const s6 = Number(stageScores.value.stage6) || 0;
  const s7 = Number(stageScores.value.stage7) || 0;
  const s8 = Number(stageScores.value.stage8) || 0;
  const s9 = Number(stageScores.value.stage9) || 0;
  return s1 + s2 + s3 + s4 + s5 + s6 + s7 + s8 + s9;
});

// 监听阶段分数变化，自动更新总分到表单
watch(totalScore, (newScore) => {
  if (isTeacherUser.value && data.form && data.formVisible) {
    data.form.score = newScore;
  }
});

const isStudentUser = computed(() => String(data.user?.role ?? '').toUpperCase() === 'STUDENT');
const isTeacherUser = computed(() => String(data.user?.role ?? '').toUpperCase() === 'TEACHER');

function resolveLoginUserId() {
  const u = data.user || {};
  const raw = u.id ?? u.userId ?? u.studentId;
  if (raw == null || raw === '') return null;
  const n = Number(raw);
  return Number.isFinite(n) ? n : null;
}

// 当前课程 ID（来自外部课程卡片）
const courseId = computed(() => {
  const id = route.query.id || route.params.id;
  return id != null ? Number(id) : null;
});

// 教师列表展示：实验作业相似度（最大相似度文本，如 "85.23%"）
const experimentSimilarityMap = reactive({})

// taskId → 该任务发布的题目数量（q1-q9 中非空的数量）
const taskQCountMap = reactive({})

async function fetchExperimentSimilarity(workId) {
  if (!workId) return
  if (experimentSimilarityMap[workId]) return
  try {
    const res = await request.get(`/work/experimentSimilarity/${workId}`)
    if (res?.code === '200') {
      experimentSimilarityMap[workId] = res.data?.maxSimilarityText || null
    }
  } catch (e) {
    console.error('获取实验相似度失败:', e)
  }
}

async function preloadExperimentSimilarities(list) {
  if (!Array.isArray(list) || list.length === 0) return
  const items = list.filter(x => x && x.id)
  if (items.length === 0) return

  // 限制并发，避免一次性打爆接口
  const concurrency = 5
  let idx = 0
  const workers = new Array(concurrency).fill(0).map(async () => {
    while (idx < items.length) {
      const current = items[idx++]
      await fetchExperimentSimilarity(current.id)
    }
  })
  await Promise.all(workers)
}

/**
 * 获取指定 taskId 对应的题目数量（q1-q9 非空计数），结果缓存到 taskQCountMap。
 * 仅在 lab=2 的 work 行中调用。
 */
async function fetchTaskQCount(taskId) {
  if (!taskId) return
  if (taskQCountMap[taskId] != null) return // 已缓存
  try {
    const res = await request.get(`/task/selectById/${taskId}`)
    if (res?.code === '200' && res.data) {
      const task = res.data
      let count = 0
      for (let i = 1; i <= 9; i++) {
        if (task[`q${i}`] && String(task[`q${i}`]).trim()) count++
      }
      taskQCountMap[taskId] = count
    }
  } catch (e) {
    console.error('获取任务题目数量失败:', e)
  }
}

/**
 * 批量预加载 lab=2 的 work 列表对应的 task q数量
 */
async function preloadTaskQCounts(list) {
  if (!Array.isArray(list) || list.length === 0) return
  const taskIds = [...new Set(
    list.filter(x => Number(x?.lab) === 2 && x?.taskId).map(x => x.taskId)
  )]
  if (taskIds.length === 0) return
  const concurrency = 5
  let idx = 0
  const workers = new Array(concurrency).fill(0).map(async () => {
    while (idx < taskIds.length) {
      const id = taskIds[idx++]
      await fetchTaskQCount(id)
    }
  })
  await Promise.all(workers)
}

const similarityHint = reactive({
  visible: false,
  title: '',
  type: 'info',
})

const polishLoading = ref(false)
const getDeepSeekApiKey = () => localStorage.getItem('deepseekApiKey') || ''

const polishTeacherComment = async () => {
  const apiKey = getDeepSeekApiKey().trim()
  if (!apiKey) {
    ElMessage.warning('请先在 AI 助教页面保存 DeepSeek API 密钥')
    return
  }
  const text = (data.form.teacherComment || '').trim()
  if (!text) {
    ElMessage.warning('请先输入教师评价，再进行润色')
    return
  }
  polishLoading.value = true
  try {
    const resp = await fetch('https://api.deepseek.com/v1/chat/completions', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${apiKey}`,
      },
      body: JSON.stringify({
        model: 'deepseek-chat',
        stream: false,
        messages: [
          {
            role: 'user',
            content:
              '请对下面这段"教师评价"进行中文润色。场景：教师给学生当前实验作业的评价/反馈。\n' +
              '要求：保持原意；针对学生作品的表述更专业、客观、具体，给出可执行的改进建议，避免空泛或评价老师自己的话；输出仅给出润色后的文本，不要添加前缀或解释。\n\n教师评价：\n' +
              text,
          },
        ],
      }),
    })
    if (!resp.ok) {
      const t = await resp.text().catch(() => '')
      throw new Error(t || `HTTP ${resp.status}`)
    }
    const json = await resp.json()
    const out = json?.choices?.[0]?.message?.content || ''
    if (!out.trim()) throw new Error('AI 未返回内容')
    data.form.teacherComment = out.trim()
    ElMessage.success('已润色')
  } catch (e) {
    console.error(e)
    ElMessage.error('润色失败，请稍后重试')
  } finally {
    polishLoading.value = false
  }
}

// ------------------------------
// 实验任务（lab=2）分段提交逻辑
// ------------------------------
const stageSaving = ref(0)
const studentStageLocked = ref(false) // 阶段一不强制锁定，关键约束在阶段二/阶段三

const hasText = (s) => s !== undefined && s !== null && String(s).trim() !== ''

function isContentDone(row) {
  if (!row) return false
  if (Number(row.lab) === 2) {
    // lab=2：按教师发布的题目数量（qCount）判断
    // qCount 从 taskQCountMap 中取，若尚未加载则降级为检查 tip1
    const qCount = row.taskId != null ? (taskQCountMap[row.taskId] ?? null) : null
    if (qCount != null && qCount > 0) {
      // 统计学生已填写的 tip 数量
      let filledCount = 0
      for (let i = 1; i <= 9; i++) {
        if (hasText(row[`tip${i}`])) filledCount++
      }
      // 必须完成所有阶段题目 AND 填写心得体会才算完成
      return filledCount >= qCount && hasText(row.experience)
    }
    // qCount 未加载时：兜底检查 tip1（至少有一个答案）
    return hasText(row.tip1) && hasText(row.experience)
  }
  // 非 lab=2：原逻辑（tip1/tip2/tip3 均有内容）
  const sp = row.submitPhase != null && row.submitPhase !== '' ? Number(row.submitPhase) : null
  if (sp != null && !Number.isNaN(sp) && sp >= 3) {
    return hasText(row.tip1) && hasText(row.tip2) && hasText(row.tip3)
  }
  return hasText(row.tip1) && hasText(row.tip2) && hasText(row.tip3)
}

function getSubmitPhase(row) {
  if (!row) return 0
  const sp = row.submitPhase != null && row.submitPhase !== '' ? Number(row.submitPhase) : null
  // submit_phase=0 也应当被视为"已进入阶段体系"，避免误回退到 studentStageTitle/Requirement
  if (sp != null && !Number.isNaN(sp)) return Math.min(3, Math.max(0, sp))

  let p = 0
  if (hasText(row.studentStageTitle)) p = 1
  if (hasText(row.studentStageRequirement)) p = 2
  if (isContentDone(row)) p = 3
  return p
}

function phaseProgressLabel(row) {
  if (!row) return '-'
  
  // 获取教师发布的题目数量
  const qCount = row.taskId != null ? (taskQCountMap[row.taskId] ?? null) : null
  const totalQuestions = qCount != null && qCount > 0 ? qCount : 3 // 默认3个阶段
  
  // 统计已完成的阶段数
  let completedStages = 0
  for (let i = 1; i <= 9; i++) {
    if (hasText(row[`tip${i}`])) completedStages++
  }
  
  // 如果所有阶段都完成了
  if (completedStages >= totalQuestions) {
    // 检查是否填写了心得体会
    if (hasText(row.experience)) {
      return '已完成'
    } else {
      return '心得体会'
    }
  }
  
  // 返回下一个待完成的阶段
  return `阶段${completedStages + 1}`
}

function formatDateTime(v) {
  if (!v) return '-'
  // 后端 LocalDateTime 可能序列化为 "2026-03-20T10:11:12"；做一次简单展示处理
  return String(v).replace('T', ' ').replace(/\\.\\d{3}Z?$/, '')
}

const canEditStage2 = computed(() => {
  if (!data.form || data.form.lab !== 2) return false
  const p = getSubmitPhase(data.form)
  return p >= 1 && p < 2
})

const canEditStage3 = computed(() => {
  if (!data.form || data.form.lab !== 2) return false
  const p = getSubmitPhase(data.form)
  // 未通过后允许再次修改阶段三
  if (data.form.state === '未通过') return p >= 2
  return p >= 2 && p < 3
})

async function saveStudentStage(phase) {
  if (!data.form?.id) {
    ElMessage.error('缺少作业ID，无法分段保存')
    return
  }

  stageSaving.value = phase
  try {
    const payload = {
      id: data.form.id,
      studentId: data.user.id,
      lab: 2,
    }

    if (phase === 1) {
      payload.tip1 = data.form.tip1
    } else if (phase === 2) {
      payload.tip2 = data.form.tip2
    } else if (phase === 3) {
      payload.tip3 = data.form.tip3
    }

    const res = await request.put(`/work/studentStage?phase=${phase}`, payload)
    if (res?.code === '200') {
      ElMessage.success('保存成功')
      data.form.submitPhase = phase
      if (phase === 3) data.form.state = null
      load()
    } else {
      ElMessage.error(res?.msg || '保存失败')
    }
  } catch (e) {
    console.error(e)
    ElMessage.error('保存失败，请稍后重试')
  } finally {
    stageSaving.value = 0
  }
}

// 筛选选项 - 学生默认显示待提交，教师默认显示待审核
const filterOption = ref(isStudentUser.value ? '待提交' : '待审核');

// 根据筛选选项过滤作业
const filteredAssignments = computed(() => {
  if (filterOption.value === '全部') {
    return data.tableData;
  } else if (filterOption.value === '待提交') {
    return data.tableData.filter(item => {
      const lab = Number(item.lab)
      if (lab === 2) return (!item.state && !isContentDone(item))
      return (!item.state && !item.scontent)
    });
  } else if (filterOption.value === '待审核') {
    return data.tableData.filter(item => {
      const lab = Number(item.lab)
      if (lab === 2) return (!item.state && isContentDone(item))
      return (!item.state && item.scontent)
    });
  } else if (filterOption.value === '待修改') {
    return data.tableData.filter(item => item.state === '未通过');
  } else if (filterOption.value === '审核通过') {
    return data.tableData.filter(item => item.state === '审核通过');
  }
  return [];
});

const uploadUrl = getUploadUrl()
const handleFileUpload = (response, fieldKey) => {
  data.expForm[fieldKey] = response.data;
};

// 分页查询
const load = async () => {
  try {
    let teacherId = null;
    let studentId = null;
    let classId = null;
    const loginId = resolveLoginUserId();
    if (isTeacherUser.value) {
      // 清空上一页缓存，避免显示错页数据
      Object.keys(experimentSimilarityMap).forEach(k => delete experimentSimilarityMap[k])
      Object.keys(taskQCountMap).forEach(k => delete taskQCountMap[k])
    }
    if (isTeacherUser.value) {
      teacherId = loginId;
    }
    if (isStudentUser.value) {
      if (loginId == null) {
        ElMessage.error('无法识别当前学生账号，请重新登录');
        data.tableData = [];
        return;
      }
      studentId = loginId;
      classId = data.user.classId;
    }
    const res = await request.get("/work/selectPage", {
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
      let list = res.data.list || [];
      if (isStudentUser.value && loginId != null) {
        list = list.filter((row) => Number(row.studentId) === loginId);
      }
      data.tableData = list;
      data.total = res.data.total || 0;
      // 仅教师列表需要"实验名称下的相似度"，只预加载当前页
      if (isTeacherUser.value) {
        preloadExperimentSimilarities(data.tableData)
      }
      // 预加载 lab=2 的 work 对应 task 的题目数量（用于审核按钮显示判断）
      preloadTaskQCounts(data.tableData)
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
  data.form = {};
  data.form.studentId = data.user.id;
  data.formVisible = true;
};

// 编辑
const handleEdit = async (row) => {
  data.form = JSON.parse(JSON.stringify(row));
  data.formVisible = true;

  // 重置阶段评分（教师评审时使用）
  if (isTeacherUser.value && Number(row?.lab) === 2) {
    stageScores.value = {
      stage1: null,
      stage2: null,
      stage3: null,
      stage4: null,
      stage5: null,
      stage6: null,
      stage7: null,
      stage8: null,
      stage9: null,
    };
  }

  // 加载任务详情（获取教师发布的题目）- 教师和学生都需要
  if (Number(row?.lab) === 2 && row?.taskId) {
    await loadTaskDetail(row.taskId);
    // 加载学生已有的答案（教师评审时需要显示学生作答）
    loadStudentAnswers(row);
  }

  // 教师审核时：请求查重提示（最大相似度 + 对应学生）
  similarityHint.visible = false;
  similarityHint.title = '';
  similarityHint.type = 'info';
  if (isTeacherUser.value && (Number(row?.lab) !== 2 ? row?.scontent : hasText(row?.tip2))) {
    request.get('/work/experimentSimilarity/' + row.id).then((res) => {
      if (res.code === '200') {
        const d = res.data || {};
        if (!d.maxSimilarityText) {
          similarityHint.title = '查重提示：未分析';
          similarityHint.type = 'info';
          similarityHint.visible = true;
          return;
        }
        const p = d.maxSimilarityText || '0.00%';
        const name = d.matchedStudentName;
        if (name) {
          similarityHint.title = `查重提示：与学生「${name}」相似度最高，约 ${p}（对比 ${d.comparedCount || 0} 份提交）`;
          const v = Number(d.maxSimilarity || 0);
          similarityHint.type = v >= 0.8 ? 'error' : v >= 0.5 ? 'warning' : 'success';
        } else {
          similarityHint.title = `查重提示：当前暂无可对比的其它提交（或内容过短），相似度 ${p}`;
          similarityHint.type = 'info';
        }
        similarityHint.visible = true;
      } else {
        similarityHint.title = `查重提示：${res.msg || '获取失败'}`;
        similarityHint.type = 'warning';
        similarityHint.visible = true;
      }
    }).catch(() => {});
  }
};

// 加载任务详情
const loadTaskDetail = async (taskId) => {
  try {
    const res = await request.get(`/task/selectById/${taskId}`);
    if (res.code === '200' && res.data) {
      const task = res.data;
      // 设置任务信息到表单
      data.form.taskPlace = task.place || '';
      data.form.taskTime = task.experimentTime || '';
      data.form.taskContent = task.experimentContent || '';
      data.form.taskPurpose = task.experimentPurpose || '';
      data.form.taskRequirement = task.experimentRequirement || '';
      data.form.taskEnvironment = task.experimentEnvironment || '';
      // 将q1-q9字段转换为题目数组
      taskQuestions.value = [];
      for (let i = 1; i <= 9; i++) {
        const q = task[`q${i}`];
        if (q && q.trim()) {
          taskQuestions.value.push({ id: i, question: q });
        }
      }
    }
  } catch (e) {
    console.error('加载任务详情失败:', e);
  }
};

// 加载学生已有的答案
const loadStudentAnswers = (row) => {
  // 从row中提取tip1-tip9到studentAnswers数组
  const answers = [];
  const submitted = [];
  for (let i = 1; i <= 9; i++) {
    const answer = row[`tip${i}`] || '';
    answers.push(answer);
    // 如果答案不为空，标记为已提交
    submitted.push(!!answer.trim());
  }
  studentAnswers.value = answers;
  submittedQuestions.value = submitted;
  // 确保数组长度与题目数量一致
  while (studentAnswers.value.length < taskQuestions.value.length) {
    studentAnswers.value.push('');
    submittedQuestions.value.push(false);
  }
};

// 判断题目是否已提交
const isQuestionSubmitted = (index) => {
  return submittedQuestions.value[index] === true;
};

// 判断题目是否锁定（前面的题目未提交）
const isQuestionLocked = (index) => {
  if (index === 0) return false;
  return !submittedQuestions.value[index - 1];
};

// 判断是否是当前题目（前面的已提交，当前未提交）
const isCurrentQuestion = (index) => {
  if (index === 0) return !submittedQuestions.value[0];
  return submittedQuestions.value[index - 1] && !submittedQuestions.value[index];
};

// 判断题目是否可见（已提交或是当前题目）
const isQuestionVisible = (index) => {
  return isQuestionSubmitted(index) || !isQuestionLocked(index);
};

// 计算当前应该作答的题目索引
const currentQuestionIndex = computed(() => {
  for (let i = 0; i < taskQuestions.value.length; i++) {
    if (!submittedQuestions.value[i]) return i;
  }
  return taskQuestions.value.length;
});

// 判断是否所有题目都已提交
const allQuestionsSubmitted = computed(() => {
  if (taskQuestions.value.length === 0) return false;
  return taskQuestions.value.every((_, index) => submittedQuestions.value[index]);
});

// 编辑已提交的题目（重新启用编辑）
const editSubmittedQuestion = (index) => {
  submittedQuestions.value[index] = false;
  ElMessage.info(`现在可以修改阶段 ${index + 1} 的内容，修改后请重新提交`);
};

// 提交单个题目
const submitSingleQuestion = async (index) => {
  if (!studentAnswers.value[index]?.trim()) {
    ElMessage.warning(`请先填写阶段 ${index + 1} 的答案`);
    return;
  }

  submittingQuestionIndex.value = index;
  try {
    // 构建payload，保留原有字段防止被置空
    const payload = {
      id: data.form.id,
      name: data.form.name || '',
      content: data.form.content || '',
      file: data.form.file || '',
      score: data.form.score,
      teacherId: data.form.teacherId,
      studentId: data.form.studentId,
      state: data.form.state,
      scontent: data.form.scontent || '',
      amendment: data.form.amendment || '',
      teacherComment: data.form.teacherComment || '',
      lab: data.form.lab,
      taskId: data.form.taskId,
      courseId: data.form.courseId,
      studentStageTitle: data.form.studentStageTitle || '',
      studentStageRequirement: data.form.studentStageRequirement || '',
      submitPhase: data.form.submitPhase,
      experience: data.form.experience || '',
    };

    // 保存所有已填写的阶段答案
    for (let i = 0; i < 9; i++) {
      payload[`tip${i + 1}`] = studentAnswers.value[i] || '';
    }

    const res = await request.put('/work/update', payload);
    if (res.code === '200') {
      submittedQuestions.value[index] = true;
      ElMessage.success(`阶段 ${index + 1} 提交成功`);

      // 更新本地 form 数据
      for (let i = 0; i < 9; i++) {
        data.form[`tip${i + 1}`] = studentAnswers.value[i] || '';
      }

      // 刷新列表数据，确保再次打开时显示正确的阶段状态
      load();
    } else {
      ElMessage.error(res.msg || '提交失败');
    }
  } catch (e) {
    console.error('提交失败:', e);
    ElMessage.error('提交失败，请稍后重试');
  } finally {
    submittingQuestionIndex.value = -1;
  }
};

// 保存学生答案
const saveStudentAnswers = async () => {
  // 检查是否所有题目都已回答
  const unanswered = studentAnswers.value.findIndex((a, i) => i < taskQuestions.value.length && !a.trim());
  if (unanswered !== -1) {
    ElMessage.warning(`请完成阶段 ${unanswered + 1} 的作答`);
    return;
  }
  if (!data.form.experience || !data.form.experience.trim()) {
    ElMessage.warning('请填写心得体会');
    return;
  }
  
  stageSaving.value = 1;
  try {
    // 组装答案到tip1-tip9，保留原有字段防止被置空
    const payload = {
      id: data.form.id,
      name: data.form.name, // 保留原有任务名称
      content: data.form.content, // 保留原有任务内容
      file: data.form.file, // 保留作业文件
      score: data.form.score, // 保留作业学分
      lab: data.form.lab, // 保留任务类型
      teacherId: data.form.teacherId, // 保留教师ID
      studentId: data.form.studentId, // 保留学生ID
      courseId: data.form.courseId, // 保留课程ID
      taskId: data.form.taskId, // 保留任务ID
      scontent: data.form.scontent, // 保留学生作业
      studentStageTitle: data.form.studentStageTitle, // 保留阶段标题
      studentStageRequirement: data.form.studentStageRequirement, // 保留阶段要求
      tip1: studentAnswers.value[0] || '',
      tip2: studentAnswers.value[1] || '',
      tip3: studentAnswers.value[2] || '',
      tip4: studentAnswers.value[3] || '',
      tip5: studentAnswers.value[4] || '',
      tip6: studentAnswers.value[5] || '',
      tip7: studentAnswers.value[6] || '',
      tip8: studentAnswers.value[7] || '',
      tip9: studentAnswers.value[8] || '',
      experience: data.form.experience || '',
      state: null, // 提交后变为待审核
      submitPhase: 3, // 已完成
    };
    
    const res = await request.put('/work/update', payload);
    if (res.code === '200') {
      ElMessage.success('作业提交成功');
      data.formVisible = false;
      load();
    } else {
      ElMessage.error(res.msg || '提交失败');
    }
  } catch (e) {
    console.error('提交失败:', e);
    ElMessage.error('提交失败，请稍后重试');
  } finally {
    stageSaving.value = 0;
  }
};

// 新增保存
const add = () => {
  request.post("/work/add", data.form).then((res) => {
    if (res.code === "200") {
      load();
      ElMessage.success("操作成功");
      data.formVisible = false;
    } else {
      ElMessage.error(res.msg);
    }
  });
};

// 编辑保存
const update = () => {
  if (isStudentUser.value) {
    data.form.state = "";
  }
  request.put("/work/update", data.form).then((res) => {
    if (res.code === "200") {
      load();
      ElMessage.success("操作成功");
      data.formVisible = false;
    } else {
      ElMessage.error(res.msg);
    }
  });
};

// 弹窗保存
const save = () => {
  data.form.id ? update() : add();
};

// 删除
const handleDelete = (id) => {
  ElMessageBox.confirm("删除后数据无法恢复，您确定删除吗?", "删除确认", { type: "warning" })
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
      .catch(() => {});
};

// 重置
const reset = () => {
  data.name = null;
  filterOption.value = '全部';
  load();
};

/**
 * 分页
 */
const changePage = (pageNum) => {
  data.pageNum = pageNum;
  load();
};

// 分页大小变化
const handleSizeChange = (pageSize) => {
  data.pageSize = pageSize;
  data.pageNum = 1; // 重置到第一页
  load();
};

// 使用 public/word.docx 模板生成并下载实验报告
const exportToWord = async (row) => {
  const templateUrl = "/word.docx";
  try {
    const response = await fetch(templateUrl);
    if (!response.ok) {
      ElMessage.error("未找到实验报告模板 word.docx（请确认已放在 vue/public 下）");
      return;
    }
    const buffer = await response.arrayBuffer();
    const zip = new PizZip(buffer);

    const imageOpts = {
      centered: false,
      getImage: async (tagValue) => {
        if (!tagValue) return null;
        const res = await fetch(tagValue);
        if (!res.ok) throw new Error("Image load failed");
        return await res.arrayBuffer();
      },
      getSize: () => [300, 200],
    };

    const imageModule = new ImageModule(imageOpts);
    const doc = new Docxtemplater(zip, {
      paragraphLoop: true,
      linebreaks: true,
      modules: [imageModule],
    });

    // 从路由获取课程名称和教师名称
    const courseName = row.courseName || route.query.courseName || route.query.name || "";
    const teacherName = row.teacherName || route.query.teacherName || "";
    
    // 从work关联表获取的信息
    const studentCode = row.studentCode || "";
    const studentName = row.studentName || "";
    const place = row.place || "";
    const className = row.className || "";
    
    // 时间处理：只取年月日，去掉时分秒
    const experimentTime = row.experimentTime || "";
    let time = "";
    if (experimentTime) {
      // 如果task表有experimentTime，只取年月日部分
      time = experimentTime.split(' ')[0] || experimentTime.split('T')[0] || experimentTime;
    }
    
    const purpose = row.experimentPurpose || "";
    const requirement = row.experimentRequirement || "";
    const environment = row.experimentEnvironment || "";
    
    // 调试日志：打印完整的 row 对象
    console.log("Word导出 - 完整的row数据:", JSON.stringify(row, null, 2));
    console.log("Word导出 - 关键字段:", {
      taskId: row.taskId,
      experimentTime: row.experimentTime,
      experimentPurpose: row.experimentPurpose,
      experimentRequirement: row.experimentRequirement,
      experimentEnvironment: row.experimentEnvironment,
      q1: row.q1, q2: row.q2, q3: row.q3,
      tip1: row.tip1, tip2: row.tip2, tip3: row.tip3,
    });
    
    // 构建题目和解答数据（如果qn或tipn为空则不显示）
    const questionAnswerData = {};
    for (let i = 1; i <= 9; i++) {
      const q = row[`q${i}`];
      const tip = row[`tip${i}`];
      // 只有当题目不为空时才显示题目和答案
      if (q && String(q).trim()) {
        questionAnswerData[`q${i}`] = q;
        questionAnswerData[`tip${i}`] = tip || "";
      } else {
        questionAnswerData[`q${i}`] = "";
        questionAnswerData[`tip${i}`] = "";
      }
    }
    
    // 心得体会和教师评语
    const experience = row.experience || "";
    const teacherComment = row.teacherComment || "";
    
    doc.render({
      // 表格循环标记（模板结构需要）
      tableData: [{}],
      // 课程/教师/学生信息
      courseName,
      teacherName,
      studentCode,
      studentName,
      place,
      classIds: className,  // 使用className作为班级名称
      time,
      // 实验基本信息（使用task表的experiment_content作为上机内容）
      name: row.experimentContent || "",
      purpose,
      requirement,
      environment,
      // 题目与答案（q1-q9 是题目，tip1-tip9 是学生答案）
      ...questionAnswerData,
      // 心得与评语
      experience,
      teacherComment,
    });

    const out = doc.getZip().generate({ type: "blob" });
    saveAs(out, "实验报告.docx");
    ElMessage.success("实验报告已下载");
  } catch (error) {
    console.error("生成实验报告失败:", error);
    // docxtemplater 会把详细错误放在 error.properties.errors
    const detail = error?.properties?.errors?.map(e => e.message).join('; ') || error?.message || '';
    ElMessage.error("生成实验报告失败：" + (detail || "请检查模板占位符或稍后重试"));
  }
};

// 初始化加载数据
load();
</script>
<style scoped>
:global(.work-tooltip-popper) {
  max-width: 420px !important;
  white-space: normal !important;
  line-height: 1.45;
  word-break: break-word;
  overflow-wrap: anywhere;
}

:global(.work-tooltip-popper .el-tooltip__content) {
  white-space: normal !important;
  max-width: 420px !important;
}

.work-page {
  padding: 20px 24px;
  min-height: calc(100vh - 60px);
  box-sizing: border-box;
  background: linear-gradient(135deg, #f5f7fa 0%, #eef3ff 45%, #f5f7fa 100%);
}

.box-card {
  border-radius: 14px;
  box-shadow: 0 12px 30px rgba(31, 45, 61, 0.08);
  border: 1px solid rgba(220, 223, 230, 0.7);
}

.card-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
}

.card-header-text {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.card-title {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
}

.card-subtitle {
  margin: 0;
  font-size: 13px;
  color: #909399;
}

.filter-container {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: space-between;
  padding: 14px 20px;
  background-color: #fff;
  border-bottom: 1px solid #ebeef5;
  gap: 10px;
}

.filter-options, .search-options {
  display: flex;
  align-items: center;
  margin: 5px 0;
  gap: 10px;
}

.filter-label {
  margin-right: 10px;
  font-size: 14px;
  color: #606266;
}

.search-input {
  width: 260px;
}

.info {
  position: relative;
  right: 15px;
  width: 84px;
  height: 84px;
  transform: scale(0.5);
  background: url('@/assets/imgs/icons-act.png') -530px -426px;
}

.el-radio-group {
  display: flex;
  gap: 10px;
  margin-left: 20px;
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

.op-actions {
  display: flex;
  gap: 8px;
  justify-content: space-between;
  align-items: center;
  flex-wrap: nowrap;
}

.op-actions-left {
  display: flex;
  gap: 8px;
  align-items: center;
  flex-wrap: nowrap;
  justify-content: center;
}

.op-actions-right {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  margin-left: auto;
}

.op-btn {
  height: 32px;
  padding: 0 12px;
}

.op-btn-icon {
  width: 32px;
  min-width: 32px;
  padding: 0;
}

.op-btn-download {
  font-weight: 600;
}

.teacher-comment-row {
  display: flex;
  gap: 10px;
  align-items: flex-start;
  width: 100%;
}

.teacher-comment-row .op-btn-icon {
  margin-top: 2px;
}

.el-table .cell {
  padding: 0;
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

.pagination-container {
  padding: 10px 20px;
  background-color: #fff;
}

.tip1-uploader, .tip2-uploader, .tip3-uploader {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  width: 200px;
  height: 200px;
  display: inline-flex;
  justify-content: center;
  align-items: center;
}

.tip1-uploader:hover, .tip2-uploader:hover, .tip3-uploader:hover {
  border-color: #409EFF;
}

.tip1, .tip2, .tip3 {
  max-width: 100%;
  max-height: 100%;
  object-fit: cover;
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #8c8c8c;
  width: 200px;
  height: 200px;
  line-height: 200px;
  text-align: center;
}

/* 固定"相似度"标签宽度，避免列表布局因文本长度抖动 */
.exp-similarity-tag {
  width: 120px;
  display: inline-flex;
  justify-content: center;
  align-items: center;
  white-space: nowrap;
}

/* 阶段评分样式 */
.score-hint {
  margin-left: 8px;
  color: #909399;
  font-size: 14px;
}
</style>