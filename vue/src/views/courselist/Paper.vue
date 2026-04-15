<template>
  <div class="paper-page">
    <div class="paper-back-row">
      <el-button type="default" text class="paper-back-btn" @click="goBack">
        <el-icon class="paper-back-icon"><ArrowLeft /></el-icon>
        返回
      </el-button>
    </div>
    <div style="font-size: 20px; font-weight: bold; text-align: center">{{ data.testPaperData.name }}</div>
    <div style="margin-top: 15px; color: #666666; text-align: center">
      <span>课程名称：{{ data.testPaperData.courseName }}</span>
      <span style="margin: 0 30px">授课教师：{{ data.testPaperData.teacherName }}</span>
      <span>考试时间：{{ data.testPaperData.time }} 分钟</span>
    </div>

    <div style="margin-top: 50px">
      <div v-for="item in (data.testPaperData.questions || [])" :key="item.id ?? item.name" style="margin-bottom: 20px">
        <div style="font-weight: bold; font-size: 16px; background-color: #ddf1ec; line-height: 30px; padding: 5px; margin-bottom: 10px">
          {{ item.name }}
        </div>
        <div v-if="item.typeId === 1">
          <el-radio-group v-model="item.newAnswer">
            <el-radio label="A">A. {{ item.optionA }}</el-radio>
            <el-radio label="B">B. {{ item.optionB }}</el-radio>
            <el-radio label="C">C. {{ item.optionC }}</el-radio>
            <el-radio label="D">D. {{ item.optionD }}</el-radio>
          </el-radio-group>
        </div>
        <div v-if="item.typeId === 2">
          <el-radio-group v-model="item.newAnswer">
            <el-radio label="正确">A. 正确</el-radio>
            <el-radio label="错误">B. 错误</el-radio>
          </el-radio-group>
        </div>
      </div>
      <div style="text-align: center; margin: 50px">
        <el-button style="padding: 20px 40px" type="primary" @click="submitPaper">提交试卷</el-button>
      </div>
    </div>

    <teleport to="body">
      <div class="paper-ai-dock" :class="{ 'is-expanded': assistantExpanded }">
        <button
          v-if="!assistantExpanded"
          type="button"
          class="paper-ai-fab"
          aria-label="打开 AI 题目讲解"
          @click="openAssistant"
        >
          <el-icon class="paper-ai-fab__icon"><ChatDotRound /></el-icon>
          <span class="paper-ai-fab__text">AI 讲解</span>
        </button>

        <div v-else class="paper-ai-panel" role="dialog" aria-label="AI 题目讲解">
          <header class="paper-ai-panel__head">
            <div class="paper-ai-panel__title">
              <el-icon><MagicStick /></el-icon>
              <span>题目讲解助手</span>
            </div>
            <div class="paper-ai-panel__actions">
              <el-button text circle type="primary" aria-label="收起" @click="assistantExpanded = false">
                <el-icon><Close /></el-icon>
              </el-button>
            </div>
          </header>

          <div class="paper-ai-panel__key">
            <el-input
              v-model="apiKey"
              type="password"
              show-password
              size="small"
              placeholder="DeepSeek API Key（与 AI 助教页相同）"
              clearable
            />
            <el-button size="small" type="primary" @click="saveApiKey">保存</el-button>
          </div>
          <p class="paper-ai-panel__key-hint">密钥仅存本机，请求直连 DeepSeek，不经本校服务器。</p>

          <div class="paper-ai-panel__quick">
            <el-select
              v-model="assistantQuestionIndex"
              size="small"
              placeholder="选题讲解"
              class="paper-ai-panel__select"
              teleported
              placement="bottom-start"
              popper-class="paper-ai-select-popper"
            >
              <el-option label="整卷（逐题概要）" :value="-1" />
              <el-option
                v-for="(q, i) in (data.testPaperData.questions || [])"
                :key="q.id ?? i"
                :label="`第 ${i + 1} 题`"
                :value="i"
              />
            </el-select>
            <el-button size="small" type="primary" plain :disabled="isThinking" @click="sendExplainSelected">
              讲解选中题目
            </el-button>
          </div>

          <div ref="chatContainerRef" class="paper-ai-panel__messages">
            <div v-if="messages.length === 0" class="paper-ai-panel__empty">
              可选中题目后点「讲解选中题目」，或在下方输入自定义问题（如「第 1 题考查什么知识点」）。
            </div>
            <template v-else>
              <div
                v-for="(message, index) in messages"
                :key="index"
                :class="['paper-ai-msg', message.isUser ? 'paper-ai-msg--user' : 'paper-ai-msg--ai']"
              >
                <span class="paper-ai-msg__role">{{ message.isUser ? '我' : 'AI' }}</span>
                <div class="paper-ai-msg__body">{{ message.content }}</div>
              </div>
              <div v-if="isThinking" class="paper-ai-thinking">
                <span>思考中…</span>
                <el-button size="small" text type="warning" @click="stopGeneration">终止</el-button>
              </div>
            </template>
          </div>

          <div class="paper-ai-panel__input">
            <el-input
              v-model="userInput"
              type="textarea"
              :rows="2"
              resize="none"
              placeholder="输入问题，Enter 发送"
              @keydown="handleKeyPress"
            />
            <el-button type="primary" :disabled="!userInput.trim() || isThinking" :loading="isThinking" @click="sendMessage">
              发送
            </el-button>
          </div>
        </div>
      </div>
    </teleport>
  </div>
</template>
<script setup>
import { reactive, onMounted, ref, nextTick } from 'vue'
import request from '@/utils/request.js'
import router from '@/router/index.js'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, ChatDotRound, Close, MagicStick } from '@element-plus/icons-vue'

const route = useRoute()

const data = reactive({
  testPaperId: router.currentRoute.value.query.id,
  testPaperData: {},
  user: JSON.parse(localStorage.getItem('system-user') || '{}'),
})

const assistantExpanded = ref(false)
const assistantQuestionIndex = ref(-1)
const apiKey = ref('')
const userInput = ref('')
const messages = ref([])
const isThinking = ref(false)
const streamAbort = ref(null)
const chatContainerRef = ref(null)

const goBack = () => {
  if (typeof window !== 'undefined' && window.history.length > 1) {
    router.back()
    return
  }
  const courseId =
    route.query.courseId ?? data.testPaperData?.courseId ?? null
  const courseName =
    route.query.courseName ||
    data.testPaperData?.courseName ||
    ''
  router.push({
    path: '/course/courseDetail/exam',
    query: {
      ...(courseId != null && courseId !== '' ? { id: String(courseId) } : {}),
      ...(courseName ? { courseName } : {}),
    },
  })
}

onMounted(() => {
  data.testPaperId = router.currentRoute.value.query.id
  data.teacherName = router.currentRoute.value.query.teacherName || ''
  data.courseName = router.currentRoute.value.query.courseName || ''
  const saved = localStorage.getItem('deepseekApiKey')
  if (saved) apiKey.value = saved
  loadTestPaper()
})

const loadTestPaper = () => {
  data.testPaperId = router.currentRoute.value.query.id
  request.get('/testPaper/selectById/' + data.testPaperId).then(res => {
    if (res.code === '200') {
      data.testPaperData = res.data
    } else {
      ElMessage.error(res.msg)
    }
  })
}

const submitPaper = () => {
  data.testPaperData.studentId = data.user.id
  request.post('/score/add', data.testPaperData).then(res => {
    if (res.code === '200') {
      ElMessage.success('提交成功')
    } else {
      ElMessage.error(res.msg)
    }
  })
}

function openAssistant() {
  assistantExpanded.value = true
  scrollChatToBottom()
}

function saveApiKey() {
  if (apiKey.value.trim()) {
    localStorage.setItem('deepseekApiKey', apiKey.value.trim())
    ElMessage.success('已保存到本地')
  } else {
    ElMessage.warning('请输入有效密钥')
  }
}

function buildPaperSystemPrompt() {
  const course = data.testPaperData?.courseName || '当前课程'
  const paperName = data.testPaperData?.name || '试卷'
  return [
    '你是「智辅实验管理平台」中的试卷题目讲解助手。',
    `【卷面上下文】课程：${course}；试卷名称：${paperName}。`,
    '',
    '【职责】用简体中文帮助学生理解题干在考什么、涉及哪些知识点，可提示思考方向与易错点。',
    '【重要】若用户处于闭卷考试场景，不要直接给出选择题/判断题的最终答案或明确选项代号；引导学生自行推理。概念性说明可以多讲。',
    '回答分段清晰，必要时用编号或小标题。',
  ].join('\n')
}

function formatOneQuestion(q, index) {
  const n = index + 1
  const lines = [`【第 ${n} 题】${q.name || ''}`]
  if (q.typeId === 1) {
    lines.push('题型：单选题')
    if (q.optionA != null) lines.push(`A. ${q.optionA}`)
    if (q.optionB != null) lines.push(`B. ${q.optionB}`)
    if (q.optionC != null) lines.push(`C. ${q.optionC}`)
    if (q.optionD != null) lines.push(`D. ${q.optionD}`)
  } else if (q.typeId === 2) {
    lines.push('题型：判断题（正确 / 错误）')
  } else {
    lines.push(`题型 ID：${q.typeId}`)
  }
  return lines.join('\n')
}

function buildExamContextForIndex(idx) {
  const qs = data.testPaperData?.questions
  if (!qs?.length) return ''
  if (idx === -1) {
    const header = [`试卷：${data.testPaperData.name || ''}`, `课程：${data.testPaperData.courseName || ''}`, '']
    return header.concat(qs.map((q, i) => formatOneQuestion(q, i))).join('\n')
  }
  const q = qs[idx]
  if (!q) return ''
  return formatOneQuestion(q, idx)
}

function scrollChatToBottom() {
  nextTick(() => {
    const el = chatContainerRef.value
    if (el) el.scrollTop = el.scrollHeight
  })
}

function addMessage(content, isUser = false) {
  messages.value.push({ content, isUser })
  scrollChatToBottom()
}

function stopGeneration() {
  streamAbort.value?.abort()
}

function parseSseLines(buffer, onDelta) {
  const lines = buffer.split('\n')
  const remainder = lines.pop() ?? ''
  for (const raw of lines) {
    const line = raw.trimEnd()
    if (!line.startsWith('data:')) continue
    const payload = line.slice(5).trimStart()
    if (payload === '[DONE]') return { done: true, remainder }
    if (!payload) continue
    try {
      const json = JSON.parse(payload)
      const piece = json?.choices?.[0]?.delta?.content
      if (piece) onDelta(piece)
    } catch {
      /* ignore */
    }
  }
  return { done: false, remainder }
}

function buildApiMessages() {
  const list = [{ role: 'system', content: buildPaperSystemPrompt() }]
  for (const m of messages.value) {
    const text = (m.content || '').trim()
    if (!text || text === '思考中...') continue
    list.push({
      role: m.isUser ? 'user' : 'assistant',
      content: m.content,
    })
  }
  return list
}

function updateLastMessage(content) {
  const last = messages.value[messages.value.length - 1]
  if (last) {
    last.content = content
    scrollChatToBottom()
  }
}

async function callDeepSeekAPIStream() {
  const controller = new AbortController()
  streamAbort.value = controller
  let sseBuffer = ''
  try {
    isThinking.value = true
    addMessage('思考中...', false)

    const response = await fetch('https://api.deepseek.com/v1/chat/completions', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${apiKey.value.trim()}`,
      },
      body: JSON.stringify({
        model: 'deepseek-chat',
        messages: buildApiMessages(),
        stream: true,
      }),
      signal: controller.signal,
    })

    if (!response.ok) {
      const errText = await response.text().catch(() => '')
      throw new Error(errText ? `HTTP ${response.status}: ${errText.slice(0, 200)}` : `HTTP ${response.status}`)
    }

    const reader = response.body?.getReader()
    if (!reader) throw new Error('无法读取响应流')

    const decoder = new TextDecoder()
    let acc = ''

    while (true) {
      const { value, done } = await reader.read()
      if (done) break
      sseBuffer += decoder.decode(value, { stream: true })
      const { done: streamDone, remainder } = parseSseLines(sseBuffer, (delta) => {
        acc += delta
        updateLastMessage(acc)
      })
      sseBuffer = remainder
      if (streamDone) break
    }
    if (sseBuffer.trim()) {
      parseSseLines(sseBuffer + '\n', (delta) => {
        acc += delta
        updateLastMessage(acc)
      })
    }
  } catch (error) {
    console.error('DeepSeek:', error)
    if (error.name === 'AbortError') {
      const last = messages.value[messages.value.length - 1]
      if (last && !last.isUser) {
        const cur = (last.content || '').trim()
        if (!cur || cur === '思考中...') last.content = '已终止回答。'
        else last.content = last.content.replace(/\n*（已停止生成）$/, '') + '\n\n（已停止生成）'
      }
      ElMessage.info('已终止回答')
    } else {
      updateLastMessage('抱歉，请求失败：' + (error?.message || String(error)))
    }
  } finally {
    isThinking.value = false
    streamAbort.value = null
    scrollChatToBottom()
  }
}

async function sendMessage() {
  if (!userInput.value.trim()) {
    ElMessage.warning('请输入内容')
    return
  }
  if (!apiKey.value.trim()) {
    ElMessage.warning('请先填写并保存 DeepSeek API Key')
    return
  }
  const message = userInput.value.trim()
  addMessage(message, true)
  userInput.value = ''
  await callDeepSeekAPIStream()
}

function sendExplainSelected() {
  if (!apiKey.value.trim()) {
    ElMessage.warning('请先填写并保存 DeepSeek API Key')
    return
  }
  const ctx = buildExamContextForIndex(assistantQuestionIndex.value)
  if (!ctx.trim()) {
    ElMessage.warning('暂无题目内容，请等待试卷加载完成')
    return
  }
  const hint =
    assistantQuestionIndex.value === -1
      ? '请根据下面整卷内容，逐题用通俗语言说明考查点与相关知识点，并给出思考提示（闭卷场景请勿直接给出选项答案）。'
      : '请根据下面这一题，讲解题干含义、考点与思考提示（闭卷场景请勿直接给出选项答案）。'
  const full = `${hint}\n\n----------\n${ctx}`
  addMessage(full, true)
  void callDeepSeekAPIStream()
}

function handleKeyPress(event) {
  if (event.key === 'Enter' && !event.shiftKey) {
    event.preventDefault()
    sendMessage()
  }
}
</script>
<style scoped>
.paper-page {
  max-width: 900px;
  margin: 24px auto 40px;
  padding: 0 16px;
}

.paper-back-row {
  margin-bottom: 16px;
}

.paper-back-btn {
  padding: 8px 10px 8px 4px;
  font-size: 15px;
  color: var(--color-primary-hover, #0f766e);
}

.paper-back-btn:hover {
  color: var(--color-primary, #0d9488);
}

.paper-back-icon {
  margin-right: 4px;
  vertical-align: middle;
}

.el-radio-group {
  display: block;
}
.el-radio {
  display: block;
}
.el-checkbox {
  display: block;
}
</style>

<style scoped>
/* 浮窗挂载到 body，仍用同一组件 scoped：第二段 style 保证选择器生效 */
.paper-ai-dock {
  position: fixed;
  z-index: 5000;
  right: max(16px, env(safe-area-inset-right, 0px));
  top: max(72px, env(safe-area-inset-top, 0px) + 56px);
  font-family: var(--font-sans, system-ui, sans-serif);
}

.paper-ai-fab {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 10px 14px 10px 12px;
  border: none;
  border-radius: 999px;
  cursor: pointer;
  color: #fff;
  background: linear-gradient(135deg, var(--color-primary, #0d9488) 0%, var(--color-primary-hover, #0f766e) 100%);
  box-shadow:
    0 10px 28px -8px color-mix(in srgb, var(--color-primary, #0d9488) 45%, transparent),
    0 0 0 1px rgba(255, 255, 255, 0.2) inset;
  transition: transform 0.15s ease, box-shadow 0.15s ease;
}

.paper-ai-fab:hover {
  transform: translateY(-1px);
  box-shadow:
    0 14px 32px -10px color-mix(in srgb, var(--color-primary, #0d9488) 50%, transparent),
    0 0 0 1px rgba(255, 255, 255, 0.25) inset;
}

.paper-ai-fab__icon {
  font-size: 20px;
}

.paper-ai-fab__text {
  font-size: 14px;
  font-weight: 700;
  letter-spacing: 0.02em;
}

.paper-ai-panel {
  width: min(360px, calc(100vw - 32px));
  max-height: min(520px, calc(100vh - 100px));
  display: flex;
  flex-direction: column;
  border-radius: 14px;
  background: var(--color-bg-elevated, #fff);
  border: 1px solid var(--color-border, #e2e8f0);
  box-shadow:
    0 24px 48px -20px rgba(15, 23, 42, 0.25),
    0 0 0 1px rgba(255, 255, 255, 0.06) inset;
  overflow: hidden;
}

.paper-ai-panel__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  padding: 10px 8px 10px 14px;
  background: linear-gradient(180deg, var(--color-primary-soft, rgba(13, 148, 136, 0.1)) 0%, transparent 100%);
  border-bottom: 1px solid var(--color-border, #e2e8f0);
}

.paper-ai-panel__title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
  font-weight: 700;
  color: var(--color-text, #0f172a);
}

.paper-ai-panel__key {
  display: flex;
  gap: 8px;
  padding: 10px 12px 0;
  align-items: center;
}

.paper-ai-panel__key :deep(.el-input) {
  flex: 1;
  min-width: 0;
}

.paper-ai-panel__key-hint {
  margin: 6px 12px 0;
  font-size: 11px;
  color: var(--color-text-muted, #64748b);
  line-height: 1.4;
}

.paper-ai-panel__quick {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  padding: 10px 12px;
  align-items: center;
}

.paper-ai-panel__select {
  flex: 1;
  min-width: 140px;
}

.paper-ai-panel__messages {
  flex: 1;
  min-height: 160px;
  max-height: 240px;
  overflow-y: auto;
  padding: 8px 12px;
  border-top: 1px solid var(--color-border, #e2e8f0);
  border-bottom: 1px solid var(--color-border, #e2e8f0);
  background: rgba(248, 250, 252, 0.6);
}

.paper-ai-panel__empty {
  font-size: 12px;
  color: var(--color-text-muted, #64748b);
  line-height: 1.5;
  padding: 12px 4px;
}

.paper-ai-msg {
  margin-bottom: 10px;
  font-size: 12px;
  line-height: 1.45;
}

.paper-ai-msg__role {
  display: block;
  font-size: 11px;
  font-weight: 700;
  color: var(--color-text-muted, #64748b);
  margin-bottom: 4px;
}

.paper-ai-msg__body {
  white-space: pre-wrap;
  word-break: break-word;
  padding: 8px 10px;
  border-radius: 10px;
  background: #fff;
  border: 1px solid var(--color-border, #e2e8f0);
  color: var(--color-text, #0f172a);
}

.paper-ai-msg--user .paper-ai-msg__body {
  background: color-mix(in srgb, var(--color-primary-soft, rgba(13, 148, 136, 0.12)) 80%, #fff);
  border-color: color-mix(in srgb, var(--color-primary, #0d9488) 22%, var(--color-border, #e2e8f0));
}

.paper-ai-thinking {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  font-size: 12px;
  color: var(--color-text-muted, #64748b);
  padding: 4px 0;
}

.paper-ai-panel__input {
  display: flex;
  gap: 8px;
  padding: 10px 12px 12px;
  align-items: flex-end;
}

.paper-ai-panel__input :deep(.el-textarea__inner) {
  font-size: 13px;
}

.paper-ai-panel__input .el-button {
  flex-shrink: 0;
}

@media (max-width: 640px) {
  .paper-ai-dock {
    right: 12px;
    left: auto;
    top: auto;
    bottom: max(20px, env(safe-area-inset-bottom, 0px) + 12px);
  }

  .paper-ai-dock.is-expanded .paper-ai-panel {
    max-height: min(70vh, 480px);
  }
}
</style>

<!-- 下拉挂载在 body 时须高于 .paper-ai-dock(5000)，否则选项被浮窗挡住 -->
<style>
.paper-ai-select-popper {
  z-index: 10060 !important;
}
</style>
