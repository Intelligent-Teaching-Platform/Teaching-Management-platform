<template>
  <div>
    <div class="card" style="margin-bottom: 5px">
      <el-input v-model="data.name" prefix-icon="Search" style="width: 240px; margin-right: 10px" placeholder="请输入题目名称查询"></el-input>
      <el-button type="info" plain @click="load">查询</el-button>
      <el-button type="warning" plain style="margin: 0 10px" @click="reset">重置</el-button>
    </div>
    <div class="card" style="margin-bottom: 5px">
      <el-button type="primary" plain @click="handleAdd">新增</el-button>
      <el-button type="success" plain @click="importVisible = true">批量导入</el-button>
      <el-button type="danger" plain @click="delBatch">批量删除</el-button>
    </div>

    <div class="card" style="margin-bottom: 5px">
      <el-table stripe :data="data.tableData" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" />
        <el-table-column prop="name" label="题目名称" />
        <el-table-column prop="teacherName" label="授课教师" />
        <el-table-column prop="typeName" label="题型" width="100">
          <template v-slot="scope">
            <el-tag v-if="scope.row.typeName === '单选题'" type="success">{{ scope.row.typeName }}</el-tag>
            <el-tag v-if="scope.row.typeName === '判断题'" type="warning">{{ scope.row.typeName }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="typeScore" label="分数" />
        <el-table-column label="选项">
          <template v-slot="scope">
            <span v-if="scope.row.typeName !== '单选题'">无</span>
            <div v-else>
              <div>A. {{ scope.row.optionA }}</div>
              <div>B. {{ scope.row.optionB }}</div>
              <div>C. {{ scope.row.optionC }}</div>
              <div>D. {{ scope.row.optionD }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="answer" label="答案" show-overflow-tooltip/>
        <el-table-column label="操作" width="100" fixed="right">
          <template v-slot="scope">
            <el-button type="primary" circle :icon="Edit" @click="handleEdit(scope.row)"></el-button>
            <el-button type="danger" circle :icon="Delete" @click="del(scope.row.id)"></el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
    <div class="card" v-if="data.total">
      <el-pagination @current-change="load" background layout="prev, pager, next" :page-size="data.pageSize" v-model:current-page="data.pageNum" :total="data.total" />
    </div>

    <el-dialog title="题目信息" v-model="data.formVisible" width="40%" destroy-on-close>
      <el-form ref="form" :model="data.form" label-width="70px" style="padding: 20px">
        <el-form-item prop="name" label="题目名称">
          <el-input v-model="data.form.name" placeholder="请输入题目名称"></el-input>
        </el-form-item>
        <el-form-item prop="typeId" label="选择题型">
          <el-select v-model="data.form.typeId" placeholder="请选择题型">
            <el-option
                v-for="item in data.typeData"
                :key="item.id"
                :label="item.name"
                :value="item.id">
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item prop="optionA" label="选项A：" v-if="data.form.typeId === 1">
          <el-input v-model="data.form.optionA" placeholder="请输入选项A"></el-input>
        </el-form-item>
        <el-form-item prop="optionB" label="选项B：" v-if="data.form.typeId === 1">
          <el-input v-model="data.form.optionB" placeholder="请输入选项B"></el-input>
        </el-form-item>
        <el-form-item prop="optionC" label="选项C：" v-if="data.form.typeId === 1">
          <el-input v-model="data.form.optionC" placeholder="请输入选项C"></el-input>
        </el-form-item>
        <el-form-item prop="optionD" label="选项D：" v-if="data.form.typeId === 1">
          <el-input v-model="data.form.optionD" placeholder="请输入选项D"></el-input>
        </el-form-item>
        <el-form-item prop="answer" label="题目答案" v-if="data.form.typeId === 1">
          <el-input v-model="data.form.answer" placeholder="请输入题目答案"></el-input>
        </el-form-item>

        <el-form-item prop="answer" label="题目答案" v-if="data.form.typeId === 2">
          <el-select v-model="data.form.answer" placeholder="请选择答案">
            <el-option label="正确" value="正确"></el-option>
            <el-option label="错误" value="错误"></el-option>
          </el-select>
        </el-form-item>
<!--        <el-form-item prop="answer" label="题目答案" v-if="data.form.typeId === 4">-->
<!--          <el-input v-model="data.form.answer" placeholder="请输入题目答案"></el-input>-->
<!--        </el-form-item>-->
<!--        <el-form-item prop="answer" label="题目答案" v-if="data.form.typeId === 5">-->
<!--          <el-input type="textarea" :rows="8" v-model="data.form.answer" placeholder="请输入题目答案"></el-input>-->
<!--        </el-form-item>-->
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="data.formVisible = false">取 消</el-button>
          <el-button type="primary" @click="save">确 定</el-button>
        </span>
      </template>
    </el-dialog>

    <el-dialog title="批量导入题目" v-model="importVisible" width="60%" destroy-on-close>
      <div style="margin-bottom: 10px">
        <el-alert type="info" :closable="false">
          <template #title>
            <div style="font-size: 13px">
              <strong>支持两种格式（可混合使用）：</strong><br><br>
              <strong>格式一（行内）：</strong>每行一条题目<br>
              • <strong>单选题：</strong>[单选]题目内容|A选项|B选项|C选项|D选项<br>
              • <strong>判断题：</strong>[判断]题目内容<br>
              • 示例：<code>[单选]Java中用于定义类的关键字是？|public|class|interface|abstract</code><br><br>
              <strong>格式二（标准试卷格式）：</strong>直接粘贴题目内容即可<br>
              • 题目以 <strong>（单选题）</strong>、<strong>（判断题）</strong> 开头<br>
              • 选项以 <strong>A.</strong>、<strong>B.</strong>、<strong>C.</strong>、<strong>D.</strong> 开头<br>
              • 示例：<br>
              <code>1. （单选题）题目内容是什么？<br>
A. 选项A<br>
B. 选项B<br>
C. 选项C<br>
D. 选项D</code><br><br>
              <strong>提示：</strong>题目导入后可在列表中手动填写答案
            </div>
          </template>
        </el-alert>
      </div>
      <el-input
        v-model="importText"
        type="textarea"
        :rows="12"
        placeholder="请粘贴题目..."
        style="font-family: Consolas, monospace; font-size: 13px"
      />
      <template #footer>
        <span style="margin-right: 10px; color: #909399">将导入到当前课程</span>
        <el-button @click="importVisible = false">取 消</el-button>
        <el-button type="primary" @click="handleImport" :loading="importing">导 入</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>

import {reactive, ref, computed, watch} from "vue";
import request from "@/utils/request.js";
import {ElMessage, ElMessageBox} from "element-plus";
import {Delete, Edit} from "@element-plus/icons-vue";
import {useRoute} from "vue-router";

const route = useRoute()

// 获取当前课程ID
const courseId = computed(() => {
  const id = route.query.id || route.params.id
  return id != null ? Number(id) : null
})

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

const importVisible = ref(false)
const importText = ref('')
const importing = ref(false)

const loadType = () => {
  request.get('/questionType/selectAll').then(res => {
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

  const lines = text.split('\n').filter(l => l.trim())
  if (lines.length === 0) {
    ElMessage.warning('没有有效的题目行')
    return
  }

  // 先尝试格式一（行内格式）
  const format1Lines = lines.filter(l => l.startsWith('[单选]') || l.startsWith('[判断]'))
  
  // 剩余的作为格式二（标准试卷格式）处理
  const format2Text = lines.filter(l => !l.startsWith('[单选]') && !l.startsWith('[判断]')).join('\n')

  const questions = []
  const errors = []

  // 解析格式一
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

  // 解析格式二（标准试卷格式）
  if (format2Text) {
    const parsed = parsePaperFormat(format2Text)
    if (parsed.errors.length > 0) {
      errors.push(...parsed.errors)
    }
    questions.push(...parsed.questions)
  }

  if (errors.length > 0) {
    ElMessage.error(`解析失败：\n${errors.slice(0, 5).join('\n')}${errors.length > 5 ? '\n...等' + errors.length + '条错误' : ''}`)
    return
  }

  if (questions.length === 0) {
    ElMessage.warning('没有有效的题目')
    return
  }

  importing.value = true
  request.post('/question/import/batch', questions).then(res => {
    if (res.code === '200') {
      ElMessage.success(`成功导入 ${res.data} 道题目`)
      importVisible.value = false
      importText.value = ''
      load()
    } else {
      ElMessage.error(res.msg || '导入失败')
    }
  }).catch(err => {
    ElMessage.error('导入失败：' + err.message)
  }).finally(() => {
    importing.value = false
  })
}

// 解析标准试卷格式
function parsePaperFormat(text) {
  const questions = []
  const errors = []

  // 按行分割，处理各种编号格式：1. 2. **1.** **2.** **1. ** 等
  const lines = text.split('\n')
  
  let currentBlock = []
  const blocks = []
  
  // 题号正则：匹配各种格式的题号
  const questionNumPattern = /^(\*\*)?\d+[\.．、](\*\*)?\s*/
  
  for (const line of lines) {
    const trimmed = line.trim()
    if (!trimmed) continue
    
    // 检测是否是新的题目开始（以题号开头）
    if (questionNumPattern.test(trimmed)) {
      // 保存之前的 block
      if (currentBlock.length > 0) {
        blocks.push(currentBlock.join('\n'))
      }
      currentBlock = [trimmed]
    } else {
      // 继续当前题目
      currentBlock.push(trimmed)
    }
  }
  // 保存最后一个 block
  if (currentBlock.length > 0) {
    blocks.push(currentBlock.join('\n'))
  }

  for (const block of blocks) {
    const trimmed = block.trim()
    if (!trimmed) continue

    // 检测题型 - 支持多种格式
    let typeId = null
    // 先尝试匹配带括号的题型标记（单选题/判断题/多选题）
    let typeMatch = trimmed.match(/[（(](单选题|判断题|多选题)[)）]/)
    if (!typeMatch) {
      // 再尝试匹配不带括号的题型标记
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
      // 如果没有明确标注题型，根据选项判断
      // 判断题通常只有正确/错误两种答案，不会有A/B/C/D选项
      const hasChoiceOptions = /[A-D][\.．、)\s][^正确错误]/.test(trimmed)
      if (hasChoiceOptions) {
        typeId = 1 // 有ABCD选项，默认为单选题
      } else {
        typeId = 2 // 没有ABCD选项，默认为判断题
      }
    }

    // 提取题目内容（去除题号和题型标记）
    let questionText = trimmed
      .replace(/^(\*\*)?\d+[\.．、](\*\*)?\s*/, '') // 去除题号
      .replace(/\*\*[（(](单选题|判断题|多选题)[)）]\*\*/g, '') // 去除带**包裹的题型标记
      .replace(/[（(](单选题|判断题|多选题)[)）]/g, '') // 去除题型标记
      .trim()
    
    // 提取选项（只在题型为单选题时提取）
    const options = {}
    if (typeId === 1) {
      const optionMatches = [...trimmed.matchAll(/([A-D])[\.．、)\s]\s*(\S[^\n]*)/g)]
      for (const match of optionMatches) {
        const letter = match[1]
        const content = match[2].trim().replace(/^\*\*/, '').replace(/\*\*$/, '') // 去除可能的**
        options[letter] = content
      }
    }

    // 清理题目文本中的选项部分
    questionText = questionText.replace(/★?\s*[A-D][\.．、)\s].*/g, '').trim()

    if (!questionText) {
      continue
    }

    // 判断题不检查选项
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
  request.get('/question/selectPage', {
    params: {
      pageNum: data.pageNum,
      pageSize: data.pageSize,
      name: data.name,
      courseId: courseId.value
    }
  }).then(res => {
    if (res.code === '200') {
      data.tableData = res.data?.list || []
      data.total = res.data?.total
    }
  })
}
const handleAdd = () => {
  data.form = {
    courseId: courseId.value
  }
  data.formVisible = true
}
const handleEdit = (row) => {
  data.form = JSON.parse(JSON.stringify(row))
  data.formVisible = true
}
const add = () => {
  data.form.teacherId = data.user.id
  request.post('/question/add', data.form).then(res => {
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
  request.put('/question/update', data.form).then(res => {
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
  ElMessageBox.confirm('删除后数据无法恢复，您确定删除吗？', '删除确认', { type: 'warning' }).then(res => {
    request.delete('/question/delete/' + id).then(res => {
      if (res.code === '200') {
        ElMessage.success("删除成功")
        load()
      } else {
        ElMessage.error(res.msg)
      }
    })
  }).catch(err => {
    console.error(err)
  })
}
const delBatch = () => {
  if (!data.ids.length) {
    ElMessage.warning("请选择数据")
    return
  }
  ElMessageBox.confirm('删除后数据无法恢复，您确定删除吗？', '删除确认', { type: 'warning' }).then(res => {
    request.delete("/question/delete/batch", {data: data.ids}).then(res => {
      if (res.code === '200') {
        ElMessage.success('操作成功')
        load()
      } else {
        ElMessage.error(res.msg)
      }
    })
  }).catch(err => {
    console.error(err)
  })
}
const handleSelectionChange = (rows) => {
  data.ids = rows.map(v => v.id)
}

const reset = () => {
  data.name = null
  load()
}

load()
loadType()

// 监听课程ID变化，重新加载数据
watch(courseId, () => {
  if (courseId.value) {
    data.pageNum = 1
    load()
  }
})
</script>