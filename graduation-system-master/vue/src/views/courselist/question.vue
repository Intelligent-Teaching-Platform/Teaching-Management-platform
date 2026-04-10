<template>
  <div class="question-page">
    <header class="question-hero card">
      <div class="question-hero__accent" aria-hidden="true" />
      <div class="question-hero__icon" aria-hidden="true">
        <el-icon><Collection /></el-icon>
      </div>
      <div class="question-hero__text">
        <h1 class="question-hero__title">题库</h1>
        <p class="question-hero__sub">
          {{ courseSubtitle }}
        </p>
      </div>
    </header>

    <section class="question-toolbar card" aria-label="筛选与操作">
      <div class="question-toolbar__search">
        <el-input
          v-model="data.name"
          clearable
          placeholder="按题目名称搜索"
          class="question-search-input"
          @keyup.enter="load"
          @clear="load"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-button type="primary" @click="load">查询</el-button>
        <el-button @click="reset">重置</el-button>
      </div>
      <div class="question-toolbar__actions">
        <el-button type="primary" plain @click="handleAdd">
          <el-icon class="btn-icon"><Plus /></el-icon>
          新增
        </el-button>
        <el-button plain @click="importVisible = true">
          <el-icon class="btn-icon"><Upload /></el-icon>
          批量导入
        </el-button>
        <el-button type="danger" plain @click="delBatch">
          <el-icon class="btn-icon"><Delete /></el-icon>
          批量删除
        </el-button>
      </div>
    </section>

    <section class="question-table-card card" v-loading="loading" aria-label="题目列表">
      <el-table
        :data="data.tableData"
        stripe
        row-key="id"
        class="question-table"
        @selection-change="handleSelectionChange"
      >
        <template #empty>
          <el-empty description="暂无题目，可使用「新增」或「批量导入」" :image-size="80" />
        </template>
        <el-table-column type="expand" width="44">
          <template #default="props">
            <div class="question-expand" role="region" :aria-label="'题目「' + (props.row.name || '') + '」详情'">
              <template v-if="props.row.typeName === '单选题'">
                <div class="question-expand__title">选项</div>
                <ul class="question-option-list">
                  <li><span class="opt-key">A</span><span class="opt-val">{{ props.row.optionA || '—' }}</span></li>
                  <li><span class="opt-key">B</span><span class="opt-val">{{ props.row.optionB || '—' }}</span></li>
                  <li><span class="opt-key">C</span><span class="opt-val">{{ props.row.optionC || '—' }}</span></li>
                  <li><span class="opt-key">D</span><span class="opt-val">{{ props.row.optionD || '—' }}</span></li>
                </ul>
              </template>
              <p v-else class="question-expand__muted">判断题无 A–D 选项，请在「答案」列查看。</p>
            </div>
          </template>
        </el-table-column>
        <el-table-column type="selection" width="48" />
        <el-table-column prop="name" label="题目" min-width="200" show-overflow-tooltip />
        <el-table-column prop="teacherName" label="教师" width="100" show-overflow-tooltip />
        <el-table-column prop="typeName" label="题型" width="104">
          <template #default="scope">
            <el-tag :type="typeTagType(scope.row.typeName)" size="small" effect="light" class="type-tag">
              {{ scope.row.typeName || '—' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="typeScore" label="分值" width="72" align="right">
          <template #default="scope">
            <span class="num-cell">{{ scope.row.typeScore ?? '—' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="answer" label="答案" min-width="100" show-overflow-tooltip />
        <el-table-column label="操作" width="112" fixed="right" align="center">
          <template #default="scope">
            <el-button type="primary" link @click="handleEdit(scope.row)">
              <el-icon><Edit /></el-icon>
            </el-button>
            <el-button type="danger" link @click="del(scope.row.id)">
              <el-icon><Delete /></el-icon>
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </section>

    <div v-if="data.total > 0" class="question-pagination card">
      <el-pagination
        background
        layout="sizes, prev, pager, next, total"
        :current-page="data.pageNum"
        :page-size="data.pageSize"
        :total="data.total"
        :page-sizes="[10, 20, 50]"
        @current-change="onPageChange"
        @size-change="onPageSizeChange"
      />
    </div>

    <el-dialog
      v-model="data.formVisible"
      :title="data.form.id ? '编辑题目' : '新增题目'"
      width="min(520px, 94vw)"
      class="question-dialog"
      destroy-on-close
      :close-on-click-modal="false"
    >
      <el-form :model="data.form" label-position="top" class="question-form">
        <el-form-item prop="name" label="题目名称">
          <el-input v-model="data.form.name" placeholder="请输入题目名称" />
        </el-form-item>
        <el-form-item prop="typeId" label="题型">
          <el-select v-model="data.form.typeId" placeholder="请选择题型" class="question-form__full">
            <el-option
              v-for="item in data.typeData"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <template v-if="data.form.typeId === 1">
          <el-form-item prop="optionA" label="选项 A">
            <el-input v-model="data.form.optionA" placeholder="选项 A" />
          </el-form-item>
          <el-form-item prop="optionB" label="选项 B">
            <el-input v-model="data.form.optionB" placeholder="选项 B" />
          </el-form-item>
          <el-form-item prop="optionC" label="选项 C">
            <el-input v-model="data.form.optionC" placeholder="选项 C" />
          </el-form-item>
          <el-form-item prop="optionD" label="选项 D">
            <el-input v-model="data.form.optionD" placeholder="选项 D" />
          </el-form-item>
          <el-form-item prop="answer" label="答案">
            <el-input v-model="data.form.answer" placeholder="正确答案" />
          </el-form-item>
        </template>
        <el-form-item v-if="data.form.typeId === 2" prop="answer" label="答案">
          <el-select v-model="data.form.answer" placeholder="请选择" class="question-form__full">
            <el-option label="正确" value="正确" />
            <el-option label="错误" value="错误" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="data.formVisible = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="importVisible"
      title="批量导入题目"
      width="min(720px, 96vw)"
      destroy-on-close
      class="question-dialog question-import-dialog"
    >
      <el-collapse class="import-collapse">
        <el-collapse-item title="查看导入格式说明" name="help">
          <div class="import-help">
            <p><strong>格式一（行内）</strong>每行一题。</p>
            <ul>
              <li>单选题：<code>[单选]题干|A|B|C|D</code></li>
              <li>判断题：<code>[判断]题干</code></li>
            </ul>
            <p><strong>格式二（试卷）</strong>题号开头，含「（单选题）」「（判断题）」及 <code>A.</code>…<code>D.</code> 选项。</p>
            <p class="import-help__note">导入后可在列表中补全答案。</p>
          </div>
        </el-collapse-item>
      </el-collapse>
      <el-input
        v-model="importText"
        type="textarea"
        :rows="14"
        placeholder="在此粘贴题目…"
        class="import-textarea"
      />
      <template #footer>
        <div class="import-footer">
          <span class="import-footer-hint">将写入当前课程题库</span>
          <div class="import-footer-btns">
            <el-button @click="importVisible = false">取消</el-button>
            <el-button type="primary" :loading="importing" @click="handleImport">导入</el-button>
          </div>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, computed, watch } from 'vue'
import request from '@/utils/request.js'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Delete, Edit, Search, Plus, Upload, Collection } from '@element-plus/icons-vue'
import { useRoute } from 'vue-router'

const route = useRoute()

const courseId = computed(() => {
  const id = route.query.id || route.params.id
  return id != null ? Number(id) : null
})

const courseSubtitle = computed(() => {
  const name = route.query.courseName
  if (name && courseId.value) return `课程「${name}」· 维护与批量导入试题`
  if (courseId.value) return `课程 ID ${courseId.value} · 题库`
  return '请从课程入口进入以关联课程'
})

function typeTagType(typeName) {
  if (typeName === '单选题') return 'success'
  if (typeName === '判断题') return 'warning'
  return 'info'
}

const data = reactive({
  user: JSON.parse(localStorage.getItem('system-user') || '{}'),
  formVisible: false,
  form: {},
  tableData: [],
  pageNum: 1,
  pageSize: 10,
  total: 0,
  name: null,
  ids: [],
  typeData: [],
})

const loading = ref(false)
const importVisible = ref(false)
const importText = ref('')
const importing = ref(false)

const loadType = () => {
  request.get('/questionType/selectAll').then((res) => {
    if (res.code === '200') {
      data.typeData = res.data
    } else {
      ElMessage.error(res.msg)
    }
  })
}

const handleImport = () => {
  const text = importText.value.trim()
  if (!text) {
    ElMessage.warning('请粘贴题目内容')
    return
  }
  if (!courseId.value) {
    ElMessage.warning('无法获取当前课程信息')
    return
  }

  const lines = text.split('\n').filter((l) => l.trim())
  if (lines.length === 0) {
    ElMessage.warning('没有有效的题目行')
    return
  }

  const format1Lines = lines.filter((l) => l.startsWith('[单选]') || l.startsWith('[判断]'))
  const format2Text = lines.filter((l) => !l.startsWith('[单选]') && !l.startsWith('[判断]')).join('\n')

  const questions = []
  const errors = []

  format1Lines.forEach((line, index) => {
    const parts = line.split('|')
    if (parts.length < 2) {
      errors.push(`格式一行${index + 1}格式错误`)
      return
    }

    const typeLabel = parts[0]
    let typeId = null

    if (typeLabel.startsWith('[单选]')) {
      typeId = 1
    } else if (typeLabel.startsWith('[判断]')) {
      typeId = 2
    } else {
      errors.push(`未知类型：${typeLabel}`)
      return
    }

    const questionName = typeLabel.substring(4).trim()
    if (!questionName) {
      errors.push('题目内容不能为空')
      return
    }

    const question = {
      name: questionName,
      courseId: courseId.value,
      teacherId: data.user.id,
      typeId: typeId,
    }

    if (typeId === 1) {
      if (parts.length < 5) {
        errors.push(`单选题需要5段：题目|A|B|C|D`)
        return
      }
      question.optionA = parts[1].trim()
      question.optionB = parts[2].trim()
      question.optionC = parts[3].trim()
      question.optionD = parts[4].trim()
    }

    questions.push(question)
  })

  if (format2Text) {
    const parsed = parsePaperFormat(format2Text)
    if (parsed.errors.length > 0) {
      errors.push(...parsed.errors)
    }
    questions.push(...parsed.questions)
  }

  if (errors.length > 0) {
    ElMessage.error(
      `解析失败：\n${errors.slice(0, 5).join('\n')}${errors.length > 5 ? '\n...等' + errors.length + '条错误' : ''}`
    )
    return
  }

  if (questions.length === 0) {
    ElMessage.warning('没有有效的题目')
    return
  }

  importing.value = true
  request
    .post('/question/import/batch', questions)
    .then((res) => {
      if (res.code === '200') {
        ElMessage.success(`成功导入 ${res.data} 道题目`)
        importVisible.value = false
        importText.value = ''
        load()
      } else {
        ElMessage.error(res.msg || '导入失败')
      }
    })
    .catch((err) => {
      ElMessage.error('导入失败：' + err.message)
    })
    .finally(() => {
      importing.value = false
    })
}

function parsePaperFormat(text) {
  const questions = []
  const errors = []

  const lines = text.split('\n')
  let currentBlock = []
  const blocks = []
  const questionNumPattern = /^(\*\*)?\d+[\.．、](\*\*)?\s*/

  for (const line of lines) {
    const trimmed = line.trim()
    if (!trimmed) continue

    if (questionNumPattern.test(trimmed)) {
      if (currentBlock.length > 0) {
        blocks.push(currentBlock.join('\n'))
      }
      currentBlock = [trimmed]
    } else {
      currentBlock.push(trimmed)
    }
  }
  if (currentBlock.length > 0) {
    blocks.push(currentBlock.join('\n'))
  }

  for (const block of blocks) {
    const trimmed = block.trim()
    if (!trimmed) continue

    let typeId = null
    let typeMatch = trimmed.match(/[（(](单选题|判断题|多选题)[)）]/)
    if (!typeMatch) {
      typeMatch = trimmed.match(/(单选题|判断题|多选题)/)
    }
    if (typeMatch) {
      const typeStr = typeMatch[1]
      if (typeStr === '单选题') typeId = 1
      else if (typeStr === '判断题') typeId = 2
      else if (typeStr === '多选题') {
        errors.push(`跳过多选题（暂不支持）：${trimmed.substring(0, 50)}...`)
        continue
      }
    } else {
      const hasChoiceOptions = /[A-D][\.．、)\s][^正确错误]/.test(trimmed)
      if (hasChoiceOptions) {
        typeId = 1
      } else {
        typeId = 2
      }
    }

    let questionText = trimmed
      .replace(/^(\*\*)?\d+[\.．、](\*\*)?\s*/, '')
      .replace(/\*\*[（(](单选题|判断题|多选题)[)）]\*\*/g, '')
      .replace(/[（(](单选题|判断题|多选题)[)）]/g, '')
      .trim()

    const options = {}
    if (typeId === 1) {
      const optionMatches = [...trimmed.matchAll(/([A-D])[\.．、)\s]\s*(\S[^\n]*)/g)]
      for (const match of optionMatches) {
        const letter = match[1]
        const content = match[2].trim().replace(/^\*\*/, '').replace(/\*\*$/, '')
        options[letter] = content
      }
    }

    questionText = questionText.replace(/★?\s*[A-D][\.．、)\s].*/g, '').trim()

    if (!questionText) {
      continue
    }

    if (typeId === 1 && Object.keys(options).length === 0) {
      continue
    }

    const question = {
      name: questionText,
      courseId: courseId.value,
      teacherId: data.user.id,
      typeId: typeId,
    }

    if (typeId === 1) {
      question.optionA = options['A'] || ''
      question.optionB = options['B'] || ''
      question.optionC = options['C'] || ''
      question.optionD = options['D'] || ''
    }

    questions.push(question)
  }

  return { questions, errors }
}

const load = () => {
  loading.value = true
  request
    .get('/question/selectPage', {
      params: {
        pageNum: data.pageNum,
        pageSize: data.pageSize,
        name: data.name,
        courseId: courseId.value,
      },
    })
    .then((res) => {
      if (res.code === '200') {
        data.tableData = res.data?.list || []
        data.total = res.data?.total ?? 0
      }
    })
    .finally(() => {
      loading.value = false
    })
}

const onPageChange = (page) => {
  data.pageNum = page
  load()
}

const onPageSizeChange = (size) => {
  data.pageSize = size
  data.pageNum = 1
  load()
}

const handleAdd = () => {
  data.form = {
    courseId: courseId.value,
  }
  data.formVisible = true
}

const handleEdit = (row) => {
  data.form = JSON.parse(JSON.stringify(row))
  data.formVisible = true
}

const add = () => {
  data.form.teacherId = data.user.id
  request.post('/question/add', data.form).then((res) => {
    if (res.code === '200') {
      ElMessage.success('操作成功')
      data.formVisible = false
      load()
    } else {
      ElMessage.error(res.msg)
    }
  })
}

const update = () => {
  request.put('/question/update', data.form).then((res) => {
    if (res.code === '200') {
      ElMessage.success('操作成功')
      data.formVisible = false
      load()
    }
  })
}

const save = () => {
  data.form.id ? update() : add()
}

const del = (id) => {
  ElMessageBox.confirm('删除后数据无法恢复，您确定删除吗？', '删除确认', { type: 'warning' })
    .then(() => {
      request.delete('/question/delete/' + id).then((res) => {
        if (res.code === '200') {
          ElMessage.success('删除成功')
          load()
        } else {
          ElMessage.error(res.msg)
        }
      })
    })
    .catch(() => {})
}

const delBatch = () => {
  if (!data.ids.length) {
    ElMessage.warning('请选择数据')
    return
  }
  ElMessageBox.confirm('删除后数据无法恢复，您确定删除吗？', '删除确认', { type: 'warning' })
    .then(() => {
      request.delete('/question/delete/batch', { data: data.ids }).then((res) => {
        if (res.code === '200') {
          ElMessage.success('操作成功')
          load()
        } else {
          ElMessage.error(res.msg)
        }
      })
    })
    .catch(() => {})
}

const handleSelectionChange = (rows) => {
  data.ids = rows.map((v) => v.id)
}

const reset = () => {
  data.name = null
  data.pageNum = 1
  load()
}

load()
loadType()

watch(courseId, () => {
  if (courseId.value) {
    data.pageNum = 1
    load()
  }
})
</script>

<style scoped lang="scss">
.question-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 16px 20px 28px;
  font-family: var(--font-sans);
  color: var(--color-text);
}

.question-hero {
  position: relative;
  display: flex;
  align-items: flex-start;
  gap: 16px;
  margin-bottom: 14px;
  overflow: hidden;
  border: 1px solid var(--color-border);
  box-shadow: var(--shadow-soft);
}

.question-hero__accent {
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 4px;
  border-radius: 0 4px 4px 0;
  background: linear-gradient(180deg, var(--color-primary) 0%, var(--color-primary-hover) 100%);
}

.question-hero__icon {
  flex-shrink: 0;
  width: 44px;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--radius-sm);
  background: var(--color-primary-soft);
  color: var(--color-primary-hover);
  font-size: 22px;
}

.question-hero__text {
  flex: 1;
  min-width: 0;
}

.question-hero__title {
  margin: 0 0 4px;
  font-size: 22px;
  font-weight: 700;
  letter-spacing: -0.02em;
  color: var(--color-text);
}

.question-hero__sub {
  margin: 0;
  font-size: 13px;
  color: var(--color-text-muted);
  line-height: 1.5;
}

.question-toolbar {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
  margin-bottom: 14px;
}

.question-toolbar__search {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 10px;
}

.question-search-input {
  width: min(280px, 100%);
}

.question-toolbar__actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.btn-icon {
  margin-right: 4px;
}

.question-table-card {
  position: relative;
  padding: 0;
  overflow: hidden;
}

.question-table-card :deep(.el-loading-mask) {
  border-radius: var(--radius-md);
}

.question-table {
  width: 100%;
}

.question-table :deep(.el-table__header th.el-table__cell) {
  background: rgba(13, 148, 136, 0.06) !important;
  color: var(--color-text);
  font-weight: 600;
  font-size: 13px;
}

.question-table :deep(.el-table__row) {
  transition: background-color var(--duration) var(--ease-out);
}

.question-table :deep(.el-table__expand-icon) {
  color: var(--color-primary-hover);
}

.type-tag {
  border-radius: 999px;
  font-weight: 500;
}

.num-cell {
  font-family: var(--font-mono);
  font-variant-numeric: tabular-nums;
  color: var(--color-text-muted);
}

.question-expand {
  padding: 12px 16px 14px 48px;
  background: var(--color-bg-app);
  border-top: 1px solid var(--color-border);
}

.question-expand__title {
  font-size: 11px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.06em;
  color: var(--color-text-subtle);
  margin-bottom: 8px;
}

.question-option-list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: grid;
  gap: 6px;
}

.question-option-list li {
  display: grid;
  grid-template-columns: 28px 1fr;
  gap: 10px;
  align-items: start;
  font-size: 13px;
  line-height: 1.45;
}

.opt-key {
  font-family: var(--font-mono);
  font-weight: 700;
  font-size: 12px;
  color: var(--color-primary-hover);
  background: var(--color-primary-soft);
  border-radius: 6px;
  text-align: center;
  padding: 2px 0;
}

.opt-val {
  color: var(--color-text);
  word-break: break-word;
}

.question-expand__muted {
  margin: 0;
  font-size: 13px;
  color: var(--color-text-muted);
}

.question-pagination {
  margin-top: 14px;
  display: flex;
  justify-content: flex-end;
}

.question-pagination :deep(.el-pagination) {
  flex-wrap: wrap;
  justify-content: flex-end;
}

.question-dialog :deep(.el-dialog__header) {
  border-bottom: 1px solid var(--color-border);
  margin-right: 0;
  padding-bottom: 14px;
}

.question-form {
  padding-top: 8px;
}

.question-form__full {
  width: 100%;
}

.import-collapse {
  margin-bottom: 12px;
  border: none;
  --el-collapse-border-color: var(--color-border);
}

.import-collapse :deep(.el-collapse-item__header) {
  font-weight: 600;
  color: var(--color-primary-hover);
}

.import-help {
  font-size: 13px;
  color: var(--color-text-muted);
  line-height: 1.6;
}

.import-help ul {
  margin: 8px 0;
  padding-left: 18px;
}

.import-help code {
  font-family: var(--font-mono);
  font-size: 12px;
  padding: 2px 6px;
  background: var(--color-bg-app);
  border: 1px solid var(--color-border);
  border-radius: 4px;
  color: var(--color-text);
}

.import-help__note {
  margin: 10px 0 0;
  font-size: 12px;
  color: var(--color-text-subtle);
}

.import-textarea :deep(textarea) {
  font-family: var(--font-mono);
  font-size: 13px;
  line-height: 1.5;
}

.import-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 12px;
  width: 100%;
}

.import-footer-btns {
  display: flex;
  gap: 8px;
}

.import-footer-hint {
  font-size: 12px;
  color: var(--color-text-muted);
}

@media (max-width: 768px) {
  .question-toolbar {
    flex-direction: column;
    align-items: stretch;
  }

  .question-toolbar__search,
  .question-toolbar__actions {
    width: 100%;
  }

  .question-search-input {
    width: 100%;
  }
}
</style>
