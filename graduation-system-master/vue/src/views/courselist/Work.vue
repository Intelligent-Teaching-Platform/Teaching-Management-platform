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
          <el-button type="primary" @click="handleAdd" v-if="data.user.role === 'TEACHER'">新增</el-button>
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
                <el-button
                    type="primary"
                    size="small"
                    class="op-btn"
                    @click="handleEdit(scope.row)"
                    v-if="data.user.role === 'STUDENT' && !scope.row.state && !isContentDone(scope.row)"
                >
                  提交
                </el-button>
                <el-button
                    type="primary"
                    size="small"
                    class="op-btn"
                    @click="handleEdit(scope.row)"
                    v-if="data.user.role === 'STUDENT' && !scope.row.state && isContentDone(scope.row)"
                >
                  修改
                </el-button>
                <el-button
                    type="primary"
                    size="small"
                    class="op-btn"
                    @click="handleEdit(scope.row)"
                    v-if="data.user.role === 'STUDENT' && scope.row.state === '未通过'"
                >
                  修改
                </el-button>
                <el-button
                    type="warning"
                    size="small"
                    class="op-btn"
                    @click="handleEdit(scope.row)"
                    v-if="data.user.role === 'TEACHER' && !scope.row.state && isContentDone(scope.row)"
                >
                  审核
                </el-button>
                <el-button
                    type="danger"
                    size="small"
                    class="op-btn"
                    @click="handleDelete(scope.row.id)"
                    v-if="data.user.role === 'TEACHER'"
                >
                  删除
                </el-button>
              </div>
              <div class="op-actions-right">
                <el-tooltip content="下载实验报告" placement="top" :show-after="150">
                  <el-button
                      type="primary"
                      size="small"
                      class="op-btn op-btn-icon op-btn-download"
                      circle
                      @click="exportToWord(scope.row)"
                      v-if="isContentDone(scope.row)"
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
            layout="prev, pager, next"
            v-model:page-size="data.pageSize"
            v-model:current-page="data.pageNum"
            :total="data.total"
            @current-change="changePage"
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
      <el-form-item v-if="data.user.role === 'TEACHER' && similarityHint.visible" label="查重提示">
        <el-alert
            :title="similarityHint.title"
            :type="similarityHint.type"
            :closable="false"
            show-icon
            style="width: 100%;"
        />
      </el-form-item>

        <!-- 学生 · 实验任务：分段提交（题目来自教师发布，学生仅分段填：环境→步骤→总结） -->
        <template v-if="data.user.role === 'STUDENT' && data.form.lab === 2">
          <el-alert
            type="info"
            :closable="false"
            show-icon
            title="请按顺序完成：① 实验环境 → ② 实验内容与步骤 → ③ 实验总结与心得。每一阶段点击保存后，才能填写下一阶段。"
            style="margin-bottom: 16px"
          />
          <div class="form-section">
            <div class="section-title">教师下发的任务</div>
            <el-form-item label="任务名称">
              <el-input v-model="data.form.name" type="textarea" :rows="2" disabled />
            </el-form-item>
            <el-form-item label="任务内容">
              <el-input v-model="data.form.content" type="textarea" :rows="3" disabled />
            </el-form-item>
          </div>

          <div class="form-section">
            <div class="section-title">阶段一 · 实验环境</div>
            <el-form-item label="实验环境" required>
              <el-input
                v-model="data.form.tip1"
                type="textarea"
                :rows="4"
                maxlength="500"
                show-word-limit
                placeholder="请填写实验环境（如软件版本、硬件环境、依赖说明等）"
                :disabled="studentStageLocked"
              />
              <el-button type="primary" class="stage-btn" :loading="stageSaving === 1" :disabled="studentStageLocked" @click="saveStudentStage(1)">
                保存实验环境
              </el-button>
            </el-form-item>
          </div>

          <div class="form-section">
            <div class="section-title">阶段二 · 实验内容与步骤</div>
            <el-form-item label="实验内容与步骤" required>
              <el-input
                v-model="data.form.tip2"
                type="textarea"
                :rows="6"
                maxlength="1000"
                show-word-limit
                placeholder="请填写实验内容与步骤"
                :disabled="studentStageLocked || !canEditStage2"
              />
              <el-button type="primary" class="stage-btn" :loading="stageSaving === 2" :disabled="studentStageLocked || !canEditStage2" @click="saveStudentStage(2)">
                保存实验步骤
              </el-button>
            </el-form-item>
          </div>

          <div class="form-section">
            <el-form-item label="实验总结与心得" required>
              <el-input
                v-model="data.form.tip3"
                type="textarea"
                :rows="6"
                maxlength="1000"
                show-word-limit
                placeholder="实验总结和心得体会"
                :disabled="studentStageLocked || !canEditStage3"
              />
            </el-form-item>
            <el-form-item>
              <el-button type="success" :loading="stageSaving === 3" :disabled="studentStageLocked || !canEditStage3" @click="saveStudentStage(3)">
                提交实验总结
              </el-button>
            </el-form-item>
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
                :disabled="data.user.role === 'STUDENT'"
            />
          </el-form-item>
        </div>
        <el-form-item label="任务内容" prop="content">
          <el-input v-model="data.form.content" type="textarea" :rows="3" autocomplete="off" :disabled="data.user.role === 'STUDENT'" />
        </el-form-item>
        <template v-if="data.user.role === 'TEACHER' && data.form.lab === 2">
          <el-form-item label="学生·题目">
            <el-input v-model="data.form.studentStageTitle" type="textarea" :rows="2" disabled placeholder="学生未填写" />
          </el-form-item>
          <el-form-item label="学生·要求">
            <el-input v-model="data.form.studentStageRequirement" type="textarea" :rows="3" disabled placeholder="学生未填写" />
          </el-form-item>
        </template>
        <div class="form-section">
          <el-form-item label="* 实验目的">
            <el-input
                v-model="data.form.scontent"
                type="textarea"
                :rows="4"
                show-word-limit
                maxlength="500"
                placeholder="请输入实验目的"
                :disabled="data.form.lab === 2"
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
                :disabled="data.user.role === 'STUDENT' && data.form.lab === 2"
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
                :disabled="data.user.role === 'STUDENT' && data.form.lab === 2"
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
                :disabled="data.user.role === 'STUDENT' && data.form.lab === 2"
            />
          </el-form-item>
        </div>
        </template>

        <el-form-item label="任务打分" prop="score" v-if="data.user.role === 'TEACHER'">
          <el-input v-model="data.form.score" autocomplete="off"/>
        </el-form-item>
        <el-form-item label="修改意见" prop="amendment" v-if="data.user.role === 'TEACHER'">
          <el-input v-model="data.form.amendment" autocomplete="off"/>
        </el-form-item>
        <el-form-item label="教师评价" prop="teacherComment" v-if="data.user.role === 'TEACHER'">
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
        <el-form-item label="审核结果" prop="state" :rules="[{ required: true, message: '请选择审核是否通过', trigger: 'change' }]" v-if="data.user.role === 'TEACHER'">
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
              v-if="!(data.user.role === 'STUDENT' && data.form.lab === 2)"
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
import { reactive, computed, ref } from "vue";
import { useRouter, useRoute } from "vue-router";
import { Plus, Download, MagicStick } from "@element-plus/icons-vue";
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
  pageSize: 100,
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

// 当前课程 ID（来自外部课程卡片）
const courseId = computed(() => {
  const id = route.query.id || route.params.id;
  return id != null ? Number(id) : null;
});

// 教师列表展示：实验作业相似度（最大相似度文本，如 "85.23%"）
const experimentSimilarityMap = reactive({})

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
              '请对下面这段“教师评价”进行中文润色。场景：教师给学生当前实验作业的评价/反馈。\n' +
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
  const sp = row.submitPhase != null && row.submitPhase !== '' ? Number(row.submitPhase) : null
  if (sp != null && !Number.isNaN(sp) && sp >= 3) {
    return hasText(row.tip1) && hasText(row.tip2) && hasText(row.tip3)
  }
  return hasText(row.tip1) && hasText(row.tip2) && hasText(row.tip3)
}

function getSubmitPhase(row) {
  if (!row) return 0
  const sp = row.submitPhase != null && row.submitPhase !== '' ? Number(row.submitPhase) : null
  // submit_phase=0 也应当被视为“已进入阶段体系”，避免误回退到 studentStageTitle/Requirement
  if (sp != null && !Number.isNaN(sp)) return Math.min(3, Math.max(0, sp))

  let p = 0
  if (hasText(row.studentStageTitle)) p = 1
  if (hasText(row.studentStageRequirement)) p = 2
  if (isContentDone(row)) p = 3
  return p
}

function phaseProgressLabel(row) {
  const p = getSubmitPhase(row)
  if (p <= 0) return '未开始'
  if (p === 1) return '已完成题目（1/3）'
  if (p === 2) return '已完成题目+要求（2/3）'
  return '已完成提交（3/3）'
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

// 筛选选项
const filterOption = ref('全部');

// 根据筛选选项过滤作业
const filteredAssignments = computed(() => {
  if (filterOption.value === '全部') {
    return data.tableData;
  } else if (filterOption.value === '待提交') {
    return data.tableData.filter(item => {
      const lab = Number(item.lab)
      const sp = item.submitPhase != null && item.submitPhase !== '' ? Number(item.submitPhase) : 0
      if (lab === 2) return (!item.state && sp < 3)
      return (!item.state && !item.scontent)
    });
  } else if (filterOption.value === '待审核') {
    return data.tableData.filter(item => {
      const lab = Number(item.lab)
      const sp = item.submitPhase != null && item.submitPhase !== '' ? Number(item.submitPhase) : 0
      if (lab === 2) return (!item.state && sp >= 3)
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
    if (data.user.role === 'TEACHER') {
      // 清空上一页缓存，避免显示错页数据
      Object.keys(experimentSimilarityMap).forEach(k => delete experimentSimilarityMap[k])
    }
    if (data.user.role === "TEACHER") {
      teacherId = data.user.id;
    }
    if (data.user.role === "STUDENT") {
      studentId = data.user.id;
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
      data.tableData = res.data.list || [];
      data.total = res.data.total || 0;
      // 仅教师列表需要“实验名称下的相似度”，只预加载当前页
      if (data.user.role === 'TEACHER') {
        preloadExperimentSimilarities(data.tableData)
      }
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
const handleEdit = (row) => {
  data.form = JSON.parse(JSON.stringify(row));
  data.formVisible = true;

  // 教师审核时：请求查重提示（最大相似度 + 对应学生）
  similarityHint.visible = false;
  similarityHint.title = '';
  similarityHint.type = 'info';
  if (data.user.role === 'TEACHER' && (Number(row?.lab) !== 2 ? row?.scontent : hasText(row?.tip2))) {
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
  if (data.user.role === "STUDENT") {
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

    // 兼容两类模板：
    // 1) 字段占位符版（detail/document.vue 使用的：title/purpose/environment/...）
    // 2) 表格循环版（历史 Work.vue 使用过的：tableData 循环）
    const tableData = [
      {
        name: row.name || "",
        student: row.studentName || "",
        teacher: row.teacherName || "",
        content: row.content || "",
        scontent: row.scontent || "",
        tip1: row.tip1 || "",
        tip2: row.tip2 || "",
        tip3: row.tip3 || "",
        amendment: row.amendment || "",
        teacherComment: row.teacherComment || "",
        score: row.score ?? "",
      },
    ];
    doc.render({
      // 字段占位符版
      title: row.name || "",
      purpose: row.scontent || "",
      environment: row.tip1 || "",
      contentAndSteps: row.tip2 || "",
      summaryAndReflection: row.tip3 || "",
      attachment: row.file || "",
      teacherComment: row.teacherComment || "",
      amendment: row.amendment || "",
      // 表格循环版
      tableData,
    });

    const out = doc.getZip().generate({ type: "blob" });
    saveAs(out, "实验报告.docx");
    ElMessage.success("实验报告已下载");
  } catch (error) {
    console.error("生成实验报告失败:", error);
    ElMessage.error("生成实验报告失败，请检查模板占位符或稍后重试");
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

/* 固定“相似度”标签宽度，避免列表布局因文本长度抖动 */
.exp-similarity-tag {
  width: 120px;
  display: inline-flex;
  justify-content: center;
  align-items: center;
  white-space: nowrap;
}
</style>