<template>
  <div class="task-page">
    <header class="task-hero card">
      <div class="task-hero__accent" aria-hidden="true" />
      <div class="task-hero__icon" aria-hidden="true">
        <el-icon><List /></el-icon>
      </div>
      <div class="task-hero__text">
        <h1 class="task-hero__title">发布任务</h1>
        <p class="task-hero__sub">
          {{ courseSubtitle }}
        </p>
      </div>
      <div class="task-hero__actions">
        <el-button v-if="isTeacher" type="success" class="task-hero__cta" @click="goToAnalysis">
          <el-icon><DataAnalysis /></el-icon>
          可视化分析
        </el-button>
        <el-button v-if="isTeacher" type="primary" class="task-hero__cta" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          新建任务
        </el-button>
      </div>
    </header>

    <section class="task-toolbar card" aria-label="搜索">
      <el-input
        v-model="searchQuery"
        placeholder="搜索任务名称"
        clearable
        class="search-input"
        @keyup.enter="handleSearch"
        @clear="handleSearch"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
      <div class="toolbar-actions">
        <el-button type="primary" @click="handleSearch">搜索</el-button>
        <el-button @click="resetSearch">重置</el-button>
      </div>
    </section>

    <el-alert
      v-if="!courseId && isTeacher"
      type="warning"
      show-icon
      :closable="false"
      class="task-alert"
      title="当前未携带课程信息，发布任务时将无法写入课程维度；请从「课程列表」进入本课程后再使用发布任务。"
    />

    <section class="task-grid-wrap" v-loading="loading" aria-label="任务列表">
      <div
        class="task-grid"
        :class="{ 'task-grid--empty': !loading && tableList.length === 0 }"
      >
        <template v-if="tableList.length > 0">
          <TaskCard
            v-for="item in tableList"
            :key="item.id"
            :task="item"
            :can-manage="isTeacher"
            @delete-task="handleDelete"
            @view-task="handleViewTask"
          />
        </template>
        <div v-else-if="!loading" class="task-empty">
          <el-empty :description="emptyDescription" />
        </div>
      </div>
    </section>

    <div class="task-pagination card" v-if="data.total > 0">
      <el-pagination
        background
        layout="sizes, prev, pager, next, total"
        :current-page="data.pageNum"
        :page-size="data.pageSize"
        :total="data.total"
        :page-sizes="[5, 10, 20, 50]"
        @current-change="changePage"
        @size-change="handleSizeChange"
      />
    </div>

    <el-drawer
      v-model="data.formVisible"
      :title="data.form.id ? '编辑任务' : '新建任务'"
      direction="rtl"
      size="520px"
      :close-on-click-modal="false"
      destroy-on-close
      class="task-drawer"
      @closed="resetForm"
    >
      <div class="drawer-hero" aria-hidden="true">
        <div class="drawer-hero-text">
          <div class="drawer-hero-title">任务配置</div>
          <div class="drawer-hero-sub">填写信息后保存，系统将按班级为学生生成对应作业记录</div>
        </div>
      </div>

      <el-form class="drawer-form" :model="data.form" :rules="rules" ref="formRef" label-width="90px">
        <el-form-item label="任务名称" prop="name">
          <el-input
            v-model="data.form.name"
            placeholder="请输入任务名称"
            maxlength="50"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="任务类型" prop="lab">
          <el-select v-model="data.form.lab" placeholder="请选择任务类型" style="width: 100%">
            <el-option label="课后任务" :value="1" />
            <el-option label="实验任务" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="发放班级" prop="classIds">
          <el-select v-model="data.form.classIds" placeholder="请选择发放班级（可多选）" filterable multiple clearable style="width: 100%">
            <el-option
              v-for="item in data.classData"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <!-- 实验任务时显示Word模板相关字段 -->
        <template v-if="data.form.lab === 2">
          <!-- 上机信息 -->
          <div class="dynamic-section">
            <div class="section-header">
              <span class="section-title">📍 上机信息</span>
            </div>
            <el-form-item label="上机地点" prop="place">
              <el-input v-model="data.form.place" placeholder="请输入上机地点（如：实验楼301）" />
            </el-form-item>
            <el-form-item label="上机时间" prop="experimentTime">
              <el-date-picker
                v-model="data.form.experimentTime"
                type="datetime"
                placeholder="选择上机时间"
                style="width: 100%"
                value-format="YYYY-MM-DD HH:mm:ss"
              />
            </el-form-item>
            <el-form-item label="上机内容" prop="experimentContent">
              <el-input
                v-model="data.form.experimentContent"
                type="textarea"
                :rows="3"
                placeholder="请输入上机内容"
              />
            </el-form-item>
          </div>

          <!-- 实验要求 -->
          <div class="dynamic-section" style="margin-top: 20px;">
            <div class="section-header">
              <span class="section-title">📝 实验要求</span>
            </div>
            <el-form-item label="实验目的及要求" prop="experimentPurpose">
              <el-input
                v-model="data.form.experimentPurpose"
                type="textarea"
                :rows="4"
                placeholder="请输入实验目的及要求"
              />
            </el-form-item>
            <el-form-item label="实验环境及要求" prop="experimentEnvironment">
              <el-input
                v-model="data.form.experimentEnvironment"
                type="textarea"
                :rows="4"
                placeholder="请输入实验环境及要求（如：操作系统、软件版本等）"
              />
            </el-form-item>
          </div>

          <!-- 实验题目（最多9个，对应q1-q9） -->
          <div class="dynamic-section" style="margin-top: 20px;">
            <div class="section-header">
              <span class="section-title">📋 实验阶段（最多9个）</span>
              <el-button 
                type="primary" 
                size="small" 
                @click="addQuestion"
                :disabled="data.form.questions && data.form.questions.length >= 9"
              >
                <el-icon><Plus /></el-icon> 添加阶段
              </el-button>
            </div>
            <div v-for="(item, index) in data.form.questions" :key="index" class="question-item">
              <div class="question-header">
                <span class="question-number">阶段 {{ index + 1 }}（对应Word模板中的 q{{ index + 1 }}）</span>
                <el-button type="danger" size="small" @click="removeQuestion(index)">
                  <el-icon><Delete /></el-icon>
                </el-button>
              </div>
              <el-input
                v-model="item.question"
                type="textarea"
                :rows="3"
                :placeholder="`请输入第 ${index + 1} 阶段的内容`"
              />
            </div>
            <el-empty v-if="!data.form.questions || data.form.questions.length === 0" description="点击上方按钮添加实验阶段（最多9个）" />
            <div v-if="data.form.questions && data.form.questions.length >= 9" class="limit-tip">
              <el-alert type="info" :closable="false" show-icon>
                <template #title>已达到最大阶段数量（9个）</template>
              </el-alert>
            </div>
          </div>
        </template>

        <!-- 课后任务时显示普通任务内容 -->
        <el-form-item v-else label="任务内容" prop="content">
          <el-input
            v-model="data.form.content"
            type="textarea"
            :rows="6"
            placeholder="请输入任务内容描述"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="drawer-footer">
          <el-button @click="data.formVisible = false">取消</el-button>
          <el-button type="primary" :loading="saving" @click="save">确定</el-button>
        </div>
      </template>
    </el-drawer>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus, List, DataAnalysis } from '@element-plus/icons-vue'
import TaskCard from '@/components/taskCard.vue'
import request from '@/utils/request'

const route = useRoute()
const router = useRouter()
const formRef = ref(null)
const loading = ref(false)
const saving = ref(false)
const searchQuery = ref('')

const courseId = computed(() => {
  const id = route.query.id || route.params.id
  return id != null && id !== '' ? Number(id) : null
})

const courseName = computed(() => (route.query.courseName ? String(route.query.courseName) : ''))

const courseSubtitle = computed(() => {
  if (courseName.value) {
    return `当前课程：${courseName.value} · 为班级布置课后与实验任务，支持搜索与分页`
  }
  return '为班级布置课后与实验任务，支持搜索与分页'
})

const isTeacher = computed(() => data.user.role === 'TEACHER')

const emptyDescription = computed(() =>
  isTeacher.value ? '暂无任务，点击页面上方「新建任务」开始发布' : '暂无任务'
)

const data = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0,
  formVisible: false,
  form: {},
  tableData: [],
  classData: [],
  user: JSON.parse(localStorage.getItem('system-user') || '{}'),
  selectedTemplate: '',
  templateParams: [],
})

// 预设模板定义
const templates = {
  basic: {
    name: '基础实验模板',
    params: [
      { key: 'experimentName', placeholder: '实验名称（如：Java基础语法）', value: '' },
      { key: 'courseName', placeholder: '课程名称', value: '' },
    ],
    contents: [
      { title: '实验目的', content: '通过本次实验，掌握 {experimentName} 的基本概念和操作方法，加深对 {courseName} 课程内容的理解。' },
      { title: '实验原理', content: '本实验基于 {experimentName} 的核心原理，通过实际操作验证理论知识。' },
      { title: '实验环境', content: '操作系统：Windows/Linux\n开发工具：IDEA/Eclipse\n相关软件：JDK 1.8+' },
      { title: '实验总结', content: '通过本次实验，我对 {experimentName} 有了更深入的理解...' },
    ],
    steps: [
      { title: '准备工作', content: '打开开发环境，创建新项目，配置相关依赖。' },
      { title: '代码编写', content: '根据实验要求，编写 {experimentName} 相关代码。' },
      { title: '运行测试', content: '运行程序，观察输出结果，验证代码正确性。' },
      { title: '结果分析', content: '记录实验结果，分析可能出现的问题及解决方案。' },
    ]
  },
  coding: {
    name: '编程实验模板',
    params: [
      { key: 'language', placeholder: '编程语言（如：Java/Python）', value: '' },
      { key: 'topic', placeholder: '实验主题（如：多线程编程）', value: '' },
      { key: 'tool', placeholder: '开发工具（如：IDEA）', value: '' },
    ],
    contents: [
      { title: '实验目的', content: '掌握 {language} 语言中 {topic} 的实现方法，提高编程实践能力。' },
      { title: '实验原理', content: '{topic} 是 {language} 编程中的重要概念，本实验将通过实例演示其工作原理。' },
      { title: '实验环境', content: '编程语言：{language}\n开发工具：{tool}\n操作系统：Windows 10/11' },
      { title: '实验要求', content: '1. 理解 {topic} 的基本概念\n2. 独立完成代码编写\n3. 提交可运行的源代码' },
    ],
    steps: [
      { title: '需求分析', content: '分析 {topic} 的功能需求，设计程序结构。' },
      { title: '代码实现', content: '使用 {language} 编写 {topic} 相关代码。' },
      { title: '调试运行', content: '在 {tool} 中调试程序，修复可能的错误。' },
      { title: '代码优化', content: '优化代码结构，添加必要的注释。' },
      { title: '提交作业', content: '将源代码打包提交。' },
    ]
  },
  network: {
    name: '网络实验模板',
    params: [
      { key: 'protocol', placeholder: '网络协议（如：TCP/IP）', value: '' },
      { key: 'device', placeholder: '网络设备（如：路由器）', value: '' },
    ],
    contents: [
      { title: '实验目的', content: '理解 {protocol} 协议的工作原理，掌握 {device} 的基本配置方法。' },
      { title: '实验原理', content: '{protocol} 协议是互联网通信的基础，本实验将模拟真实网络环境进行配置。' },
      { title: '实验环境', content: '网络设备：{device}\n模拟软件：Packet Tracer/GNS3\n协议类型：{protocol}' },
      { title: '注意事项', content: '1. 注意网络拓扑的正确连接\n2. 记录每个配置步骤\n3. 保存配置文件' },
    ],
    steps: [
      { title: '拓扑搭建', content: '使用模拟软件搭建网络拓扑结构。' },
      { title: '设备配置', content: '对 {device} 进行基础配置，包括IP地址、子网掩码等。' },
      { title: '协议配置', content: '配置 {protocol} 协议相关参数。' },
      { title: '连通测试', content: '使用 ping 等命令测试网络连通性。' },
      { title: '抓包分析', content: '使用 Wireshark 抓取并分析 {protocol} 数据包。' },
    ]
  }
}

const tableList = computed(() => data.tableData || [])

const rules = {
  name: [{ required: true, message: '请输入任务名称', trigger: 'blur' }],
  lab: [{ required: true, message: '请选择任务类型', trigger: 'change' }],
  content: [{ required: true, message: '请输入任务内容', trigger: 'blur' }],
  classIds: [{ required: true, message: '请选择发放班级', trigger: 'change' }],
}

function normalizePagePayload(raw) {
  if (!raw || typeof raw !== 'object') return { list: [], total: 0 }
  const list = raw.list ?? raw.records ?? raw.rows ?? []
  const total = Number(raw.total ?? raw.totalCount ?? list.length) || 0
  return { list: Array.isArray(list) ? list : [], total }
}

const loadClass = async () => {
  try {
    const res = await request.get('/clazz/selectAll')
    if (res.code === '200') {
      data.classData = res.data || []
    }
  } catch (e) {
    console.error('加载班级失败:', e)
  }
}

const load = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: data.pageNum,
      pageSize: data.pageSize,
    }
    if (searchQuery.value.trim()) {
      params.name = searchQuery.value.trim()
    }
    if (data.user.role === 'TEACHER') {
      params.teacherId = data.user.id
    }
    if (courseId.value != null && !Number.isNaN(courseId.value)) {
      params.courseId = courseId.value
    }
    const res = await request.get('/task/selectPage', { params })
    if (res.code === '200') {
      const { list, total } = normalizePagePayload(res.data)
      data.tableData = list
      data.total = total
    } else {
      ElMessage.error(res.msg || '加载失败')
    }
  } catch (e) {
    console.error('加载任务失败:', e)
    ElMessage.error('加载失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  data.pageNum = 1
  load()
}

const resetSearch = () => {
  searchQuery.value = ''
  data.pageNum = 1
  load()
}

// 跳转到可视化分析页面
const goToAnalysis = () => {
  router.push({
    path: '/course/courseDetail/taskAnalysis',
    query: { 
      id: courseId.value,
      courseName: courseName.value 
    }
  })
}

const handleAdd = () => {
  if (!isTeacher.value) return
  if (courseId.value == null || Number.isNaN(courseId.value)) {
    ElMessage.warning('请先进入具体课程后再发布任务（需带上课程信息）')
    return
  }
  data.form = {
    courseId: courseId.value,
    classIds: [],
    lab: 2, // 默认实验任务
    questions: [], // 实验题目列表
    place: '', // 上机地点
    experimentTime: '', // 上机时间
    experimentContent: '', // 上机内容
    experimentPurpose: '', // 实验目的及要求
    experimentEnvironment: '', // 实验环境及要求
  }
  data.selectedTemplate = ''
  data.templateParams = []
  data.formVisible = true
}

const handleEdit = (row) => {
  data.form = JSON.parse(JSON.stringify(row))
  // 确保lab字段是数字类型（用于正确显示实验任务表单）
  if (data.form.lab != null) {
    data.form.lab = Number(data.form.lab)
  }
  // 将逗号分隔的classIds字符串转换为数组，用于多选组件回显
  if (data.form.classIds && typeof data.form.classIds === 'string') {
    data.form.classIds = data.form.classIds.split(',').map(id => parseInt(id.trim())).filter(id => !isNaN(id))
  } else if (data.form.classId && !data.form.classIds) {
    // 兼容旧数据，如果只有classId，转换为数组
    data.form.classIds = [data.form.classId]
  }
  // 将q1-q9字段转换为questions数组
  data.form.questions = []
  for (let i = 1; i <= 9; i++) {
    const q = data.form[`q${i}`]
    if (q && q.trim()) {
      data.form.questions.push({ id: i, question: q })
    }
  }
  data.formVisible = true
}

const handleViewTask = (row) => {
  if (isTeacher.value) {
    handleEdit(row)
    return
  }
  ElMessageBox.alert(row.content || '暂无内容描述', row.name || '任务详情', {
    confirmButtonText: '关闭',
    dangerouslyUseHTMLString: false,
  }).catch(() => {})
}

const resetForm = () => {
  data.form = {}
}

// 实验题目相关方法（最多9个，对应q1-q9）
const addQuestion = () => {
  if (!data.form.questions) {
    data.form.questions = []
  }
  if (data.form.questions.length >= 9) {
    ElMessage.warning('最多只能添加9个题目')
    return
  }
  data.form.questions.push({ id: data.form.questions.length + 1, question: '' })
}

const removeQuestion = (index) => {
  data.form.questions.splice(index, 1)
  // 重新编号
  data.form.questions.forEach((q, i) => q.id = i + 1)
}

// 模板相关方法
const applyTemplate = (templateKey) => {
  if (!templateKey || !templates[templateKey]) {
    data.templateParams = []
    return
  }
  const template = templates[templateKey]
  // 深拷贝参数，避免修改原模板
  data.templateParams = template.params.map(p => ({ ...p }))
}

// 替换模板中的参数
const replaceTemplateParams = (text, params) => {
  if (!text) return text
  let result = text
  params.forEach(param => {
    if (param.key && param.value) {
      const regex = new RegExp(`\\{${param.key}\\}`, 'g')
      result = result.replace(regex, param.value)
    }
  })
  return result
}

// 根据模板生成内容
const generateFromTemplate = () => {
  const templateKey = data.selectedTemplate
  if (!templateKey || !templates[templateKey]) {
    ElMessage.warning('请先选择一个模板')
    return
  }
  
  const template = templates[templateKey]
  const params = data.templateParams
  
  // 检查参数是否填写
  const emptyParams = params.filter(p => !p.value.trim())
  if (emptyParams.length > 0) {
    ElMessage.warning(`请填写参数：${emptyParams.map(p => p.placeholder).join('、')}`)
    return
  }
  
  // 生成实验内容
  data.form.experimentContents = template.contents.map(item => ({
    title: item.title,
    content: replaceTemplateParams(item.content, params)
  }))
  
  // 生成实验步骤
  data.form.experimentSteps = template.steps.map((item, index) => ({
    step: index + 1,
    title: replaceTemplateParams(item.title, params),
    content: replaceTemplateParams(item.content, params)
  }))
  
  ElMessage.success('模板内容已生成，可继续编辑修改')
}

const save = async () => {
  if (!formRef.value) return
  if (courseId.value == null || Number.isNaN(courseId.value)) {
    ElMessage.warning('缺少课程信息，无法保存')
    return
  }
  try {
    await formRef.value.validate()
    saving.value = true
    const payload = {
      name: data.form.name,
      content: data.form.content,
      cover: data.form.cover || null,
      classIds: Array.isArray(data.form.classIds) ? data.form.classIds.join(',') : data.form.classIds,
      courseId: courseId.value,
      lab: data.form.lab,
      teacherId: data.user.id,
    }
    // 实验任务时，添加Word模板相关字段
    if (data.form.lab === 2) {
      payload.place = data.form.place || ''
      payload.experimentTime = data.form.experimentTime || ''
      payload.experimentContent = data.form.experimentContent || ''
      payload.experimentPurpose = data.form.experimentPurpose || ''
      payload.experimentEnvironment = data.form.experimentEnvironment || ''
      // 将题目数组转换为独立字段q1-q9
      const questions = data.form.questions || []
      for (let i = 1; i <= 9; i++) {
        payload[`q${i}`] = questions[i - 1]?.question || ''
      }
      // 将题目拼接为content字段存储（兼容旧版本）
      const questionsText = questions.map((q, i) => `${i + 1}. ${q.question}`).join('\n')
      payload.content = `上机地点：${payload.place}\n上机时间：${payload.experimentTime}\n\n实验目的及要求：\n${payload.experimentPurpose}\n\n实验环境及要求：\n${payload.experimentEnvironment}\n\n实验题目：\n${questionsText}`
    }
    if (data.form.id != null && data.form.id !== '') {
      payload.id = data.form.id
    }
    const res =
      payload.id != null
        ? await request.put('/task/update', payload)
        : await request.post('/task/add', payload)
    if (res.code === '200') {
      ElMessage.success('操作成功')
      data.formVisible = false
      load()
    } else {
      ElMessage.error(res.msg || '操作失败')
    }
  } catch (e) {
    if (e !== false) {
      console.error('保存失败:', e)
      const msg = e?.response?.data?.msg || e?.message
      ElMessage.error(msg || '操作失败')
    }
  } finally {
    saving.value = false
  }
}

const handleDelete = (id) => {
  if (!isTeacher.value) return
  ElMessageBox.confirm('删除后数据无法恢复，您确定删除吗？', '删除确认', {
    type: 'warning',
  })
    .then(async () => {
      try {
        const res = await request.delete(`/task/delete/${id}`)
        if (res.code === '200') {
          ElMessage.success('删除成功')
          load()
        } else {
          ElMessage.error(res.msg || '删除失败')
        }
      } catch (e) {
        console.error(e)
        ElMessage.error(e?.response?.data?.msg || e?.message || '删除失败')
      }
    })
    .catch(() => {})
}

const changePage = (page) => {
  data.pageNum = page
  load()
}

const handleSizeChange = (size) => {
  data.pageSize = size
  data.pageNum = 1
  load()
}

watch(
  () => [route.query.id, route.params.id],
  () => {
    data.pageNum = 1
    load()
  }
)

onMounted(() => {
  loadClass()
  load()
})
</script>

<style scoped lang="scss">
.task-page {
  padding: 16px 20px 28px;
  min-height: 100%;
  box-sizing: border-box;
  font-family: var(--font-sans);
  color: var(--color-text);
  background: transparent;
}

.task-hero {
  position: relative;
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 14px 20px;
  margin-bottom: 16px;
  overflow: hidden;
  border: 1px solid var(--color-border);
  box-shadow: var(--shadow-soft);
}

.task-hero__accent {
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 4px;
  background: linear-gradient(
    180deg,
    var(--color-primary) 0%,
    color-mix(in srgb, var(--color-primary) 70%, #0f766e) 100%
  );
  border-radius: 2px;
  pointer-events: none;
}

.task-hero__icon {
  width: 48px;
  height: 48px;
  border-radius: var(--radius-md);
  background: var(--color-primary-soft);
  color: var(--color-primary);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
}

.task-hero__text {
  flex: 1;
  min-width: 200px;
}

.task-hero__title {
  margin: 0 0 6px;
  font-size: 1.35rem;
  font-weight: 700;
  letter-spacing: -0.02em;
  color: var(--color-text);
}

.task-hero__sub {
  margin: 0;
  font-size: 0.9rem;
  color: var(--color-text-muted);
  line-height: 1.55;
  max-width: 44rem;
}

.task-hero__actions {
  margin-left: auto;
  display: flex;
  gap: 12px;
}

.task-hero__cta {
  border-radius: 999px;
  font-weight: 600;
}

.task-toolbar {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
  border: 1px solid var(--color-border);
}

.search-input {
  flex: 1 1 220px;
  min-width: 180px;
  max-width: 360px;
}

.toolbar-actions {
  display: flex;
  gap: 8px;
  margin-left: auto;
}

.task-alert {
  margin-bottom: 16px;
  border-radius: var(--radius-md);
}

.task-grid-wrap {
  min-height: 200px;
  margin-bottom: 16px;
}

.task-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 16px;
  align-items: stretch;
}

.task-grid--empty {
  align-items: start;
  /* 空态下避免子项落到「单列窄轨」，由子元素自行占满宽度 */
  grid-template-columns: 1fr;
}

.task-empty {
  grid-column: 1 / -1;
  width: 100%;
  min-width: 0;
  padding: 28px 16px;
  background: var(--color-bg-elevated);
  border: 1px dashed var(--color-border-strong);
  border-radius: var(--radius-md);
}

.task-pagination {
  display: flex;
  justify-content: center;
  padding: 14px 16px;
  border: 1px solid var(--color-border);
}

.task-pagination :deep(.el-pagination) {
  flex-wrap: wrap;
  justify-content: center;
  row-gap: 8px;
}

.drawer-hero {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
  padding: 14px;
  border-radius: var(--radius-md);
  border: 1px solid var(--color-border);
  background: var(--color-bg-elevated);
  margin-bottom: 14px;
}

.drawer-hero-title {
  font-size: 16px;
  font-weight: 700;
  color: var(--color-text);
}

.drawer-hero-sub {
  margin-top: 4px;
  font-size: 12px;
  color: var(--color-text-muted);
  line-height: 1.45;
}

.drawer-form {
  padding: 4px 4px 0;
}

.drawer-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

// 动态实验内容和步骤样式
.dynamic-section {
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

  .dynamic-item {
    display: flex;
    align-items: flex-start;
    margin-bottom: 12px;
    padding: 12px;
    background-color: #fff;
    border-radius: 6px;
    border: 1px solid #e4e7ed;

    .step-number {
      width: 60px;
      font-size: 13px;
      font-weight: 500;
      color: #409eff;
      flex-shrink: 0;
      line-height: 32px;
    }

    .el-input {
      margin-right: 8px;
    }

    .el-button {
      flex-shrink: 0;
    }
  }

  .el-empty {
    padding: 20px 0;
  }

  // 实验题目样式
  .question-item {
    margin-bottom: 16px;
    padding: 16px;
    background-color: #fff;
    border-radius: 8px;
    border: 1px solid #e4e7ed;

    .question-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 12px;

      .question-number {
        font-size: 14px;
        font-weight: 600;
        color: #409eff;
      }
    }
  }

  .limit-tip {
    margin-top: 12px;
  }

  // 模板参数样式
  .template-params {
    margin-top: 12px;
    padding: 12px;
    background-color: #fff;
    border-radius: 6px;
    border: 1px dashed #91d5ff;

    .params-title {
      font-size: 13px;
      color: #606266;
      margin-bottom: 10px;
    }

    .param-item {
      margin-bottom: 8px;
    }
  }
}

@media (max-width: 768px) {
  .task-hero__actions {
    width: 100%;
    flex-direction: column;
  }
  
  .task-hero__cta {
    width: 100%;
  }

  .toolbar-actions {
    width: 100%;
    margin-left: 0;
    justify-content: flex-end;
  }

  .task-grid {
    grid-template-columns: 1fr;
  }
}
</style>
