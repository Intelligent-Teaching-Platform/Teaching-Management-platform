<template>
  <div class="ai-page">
    <header class="ai-hero card">
      <div class="ai-hero__accent" aria-hidden="true" />
      <div class="ai-hero__icon" aria-hidden="true">
        <el-icon><MagicStick /></el-icon>
      </div>
      <div class="ai-hero__text">
        <h1 class="ai-hero__title">DeepSeek AI 助教</h1>
        <p class="ai-hero__sub">
          本课一对一问答 · 仅回答「{{ courseTitle }}」相关内容。请在下方填写可用的 DeepSeek API Key（保存在本机浏览器）。
        </p>
      </div>
    </header>

    <section class="ai-key card" aria-label="API 密钥">
      <div class="ai-key__row">
        <el-input
          v-model="apiKey"
          type="password"
          show-password
          placeholder="sk-…  DeepSeek API Key"
          clearable
          class="ai-key__input"
        />
        <el-button type="primary" @click="saveApiKey">保存密钥</el-button>
      </div>
      <p class="ai-key__hint">密钥仅存于本地 <code>localStorage</code>，不会发送到本校后端。</p>
    </section>

    <section class="ai-chat card">
      <div ref="chatContainerRef" class="chat-container">
        <div v-if="messages.length === 0" class="empty-state">
          <div class="empty-title">欢迎使用 AI 助教</div>
          <div class="empty-hint">你可以试试这样问我：</div>
          <ul class="empty-list">
            <li>帮我梳理一下本次实验的关键步骤？</li>
            <li>根据「某次实验结果」帮我写一段结论说明。</li>
            <li>把下面这段实验目的优化成更正式的表述。</li>
          </ul>
        </div>
        <template v-else>
          <div
            v-for="(message, index) in messages"
            :key="index"
            :class="['message', message.isUser ? 'user-message' : 'ai-message']"
          >
            <div class="message-meta">
              <span class="message-role">{{ message.isUser ? '我' : 'AI 助教' }}</span>
            </div>
            <div class="message-content">
              {{ message.content }}
            </div>
          </div>
          <div v-if="isThinking" class="thinking-row">
            <div class="thinking-indicator">
              <span class="dot" />
              <span class="dot" />
              <span class="dot" />
              <span class="thinking-text">AI 正在回答中…</span>
            </div>
            <el-button size="small" plain type="warning" @click="stopGeneration">终止回答</el-button>
          </div>
        </template>
      </div>

      <div class="input-container">
        <div class="input-wrapper">
          <el-input
            v-model="userInput"
            type="textarea"
            :rows="3"
            resize="none"
            placeholder="输入问题，Enter 发送，Shift+Enter 换行"
            class="chat-textarea"
            @keydown="handleKeyPress"
          />
          <div class="input-actions">
            <el-button text type="primary" :icon="Delete" :disabled="!userInput.trim()" @click="clearInput">
              清空
            </el-button>
            <el-button
              type="primary"
              class="send-btn"
              :disabled="!userInput.trim() || isThinking"
              :loading="isThinking"
              @click="sendMessage"
            >
              {{ isThinking ? '思考中…' : '发送' }}
            </el-button>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, nextTick } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { MagicStick, Delete } from '@element-plus/icons-vue'

const route = useRoute()
const chatContainerRef = ref(null)

const apiKey = ref('')
const userInput = ref('')
const messages = ref([])
const isThinking = ref(false)
const streamAbort = ref(null)

const courseTitle = computed(() => {
  const n = route.query.courseName
  return n && String(n).trim() ? String(n).trim() : '当前课程'
})

const courseIdDisplay = computed(() => {
  const id = route.query.id
  return id != null && String(id).trim() !== '' ? String(id) : '未指定'
})

function buildSystemPrompt() {
  const name = courseTitle.value
  const cid = courseIdDisplay.value
  return [
    '你是「智辅实验管理平台」中的课程 AI 一对一助教，只能围绕指定课程为学生提供学习辅导。',
    '',
    '【当前课程上下文】',
    `- 课程名称：${name}`,
    `- 课程 ID：${cid}`,
    '',
    '【必须遵守的规则】',
    '1. 仅回答与上述课程直接相关的问题，包括：课程知识点、实验原理与步骤、实验报告结构与写作建议、课程作业/考核的解题思路（需引导学生独立思考，禁止代写可判定为抄袭的完整答案）、与课程相关的概念辨析等。',
    '2. 若用户问题与上述课程明显无关（如闲聊、其他课程、娱乐、政治、医疗、法律个案、通用编程代做整份作业、违法或作弊协助等），必须拒绝作答，并用一两句话说明：你只负责本课程相关答疑，请对方改为提出与本课程相关的问题。',
    '3. 不要编造本课程未提供过的具体评分标准、考试原题或内部政策；不确定时请明确说明并建议查阅教师/教务发布的官方说明。',
    '4. 默认使用简体中文，语气专业、友好、鼓励探究。',
    '5. 回答尽量条理清晰，必要时使用小标题或编号。',
  ].join('\n')
}

function buildApiMessages() {
  const list = [{ role: 'system', content: buildSystemPrompt() }]
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

function scrollChatToBottom() {
  nextTick(() => {
    const el = chatContainerRef.value
    if (el) el.scrollTop = el.scrollHeight
  })
}

function saveApiKey() {
  if (apiKey.value.trim()) {
    ElMessage.success('API 密钥已保存到本地')
    localStorage.setItem('deepseekApiKey', apiKey.value.trim())
  } else {
    ElMessage.warning('请输入有效的 API 密钥')
  }
}

onMounted(() => {
  const saved = localStorage.getItem('deepseekApiKey')
  if (saved) apiKey.value = saved
})

function addMessage(content, isUser = false) {
  messages.value.push({ content, isUser })
  scrollChatToBottom()
}

function stopGeneration() {
  streamAbort.value?.abort()
}

/**
 * 解析 DeepSeek SSE：处理分块截断、多行 data:
 */
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
      /* 忽略单行解析失败（聚合包） */
    }
  }
  return { done: false, remainder }
}

async function callDeepSeekAPIStream() {
  const controller = new AbortController()
  streamAbort.value = controller
  let sseBuffer = ''
  try {
    isThinking.value = true
    addMessage('思考中...', false)

    const apiMessages = buildApiMessages()

    const response = await fetch('https://api.deepseek.com/v1/chat/completions', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${apiKey.value.trim()}`,
      },
      body: JSON.stringify({
        model: 'deepseek-chat',
        messages: apiMessages,
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
    let accumulatedResponse = ''

    while (true) {
      const { value, done } = await reader.read()
      if (done) break
      sseBuffer += decoder.decode(value, { stream: true })
      const { done: streamDone, remainder } = parseSseLines(sseBuffer, (delta) => {
        accumulatedResponse += delta
        updateLastMessage(accumulatedResponse)
      })
      sseBuffer = remainder
      if (streamDone) break
    }
    if (sseBuffer.trim()) {
      const { remainder } = parseSseLines(sseBuffer + '\n', (delta) => {
        accumulatedResponse += delta
        updateLastMessage(accumulatedResponse)
      })
      sseBuffer = remainder
    }
  } catch (error) {
    console.error('DeepSeek:', error)
    if (error.name === 'AbortError') {
      const last = messages.value[messages.value.length - 1]
      if (last && !last.isUser) {
        const cur = (last.content || '').trim()
        if (!cur || cur === '思考中...') {
          last.content = '已终止回答。'
        } else {
          last.content = last.content.replace(/\n*（已停止生成）$/, '') + '\n\n（已停止生成）'
        }
      }
      ElMessage.info('已终止回答')
    } else {
      const msg = error?.message || String(error)
      updateLastMessage('抱歉，请求失败：' + msg)
    }
  } finally {
    isThinking.value = false
    streamAbort.value = null
    scrollChatToBottom()
  }
}

function updateLastMessage(content) {
  const lastMessage = messages.value[messages.value.length - 1]
  if (lastMessage) {
    lastMessage.content = content
    scrollChatToBottom()
  }
}

async function sendMessage() {
  if (!userInput.value.trim()) {
    ElMessage.warning('请输入要咨询的内容')
    return
  }
  if (!apiKey.value.trim()) {
    ElMessage.warning('请先设置 API 密钥')
    return
  }
  const message = userInput.value.trim()
  addMessage(message, true)
  userInput.value = ''
  await callDeepSeekAPIStream()
}

function clearInput() {
  userInput.value = ''
}

function handleKeyPress(event) {
  if (event.key === 'Enter' && !event.shiftKey) {
    event.preventDefault()
    sendMessage()
  }
}
</script>

<style scoped lang="scss">
.ai-page {
  padding: 16px 20px 24px;
  min-height: 100%;
  box-sizing: border-box;
  font-family: var(--font-sans);
  color: var(--color-text);
  background: transparent;
  max-width: 920px;
  margin: 0 auto;
}

.ai-hero {
  position: relative;
  display: flex;
  align-items: flex-start;
  gap: 16px;
  margin-bottom: 16px;
  overflow: hidden;
  border: 1px solid var(--color-border);
  box-shadow: var(--shadow-soft);
}

.ai-hero__accent {
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

.ai-hero__icon {
  width: 48px;
  height: 48px;
  border-radius: var(--radius-md);
  background: var(--color-primary-soft);
  color: var(--color-primary);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  flex-shrink: 0;
}

.ai-hero__text {
  min-width: 0;
}

.ai-hero__title {
  margin: 0 0 6px;
  font-size: 1.3rem;
  font-weight: 700;
  letter-spacing: -0.02em;
}

.ai-hero__sub {
  margin: 0;
  font-size: 0.88rem;
  color: var(--color-text-muted);
  line-height: 1.55;
}

.ai-key {
  margin-bottom: 16px;
  border: 1px solid var(--color-border);
}

.ai-key__row {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  align-items: center;
}

.ai-key__input {
  flex: 1 1 240px;
  min-width: 0;
}

.ai-key__hint {
  margin: 10px 0 0;
  font-size: 12px;
  color: var(--color-text-subtle);
}

.ai-key__hint code {
  font-size: 11px;
  padding: 1px 6px;
  border-radius: 4px;
  background: var(--color-bg-app);
  border: 1px solid var(--color-border);
}

.ai-chat {
  border: 1px solid var(--color-border);
  display: flex;
  flex-direction: column;
  min-height: min(82vh, 780px);
}

.chat-container {
  flex: 1;
  min-height: min(48vh, 420px);
  max-height: min(68vh, 720px);
  overflow-y: auto;
  padding: 16px;
  background: var(--color-bg-app);
  border-radius: var(--radius-md);
  border: 1px solid var(--color-border);
  margin-bottom: 14px;
  scroll-behavior: smooth;
}

.empty-state {
  color: var(--color-text-muted);
}

.empty-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--color-text);
  margin-bottom: 8px;
}

.empty-hint {
  font-size: 13px;
  margin-bottom: 6px;
}

.empty-list {
  padding-left: 18px;
  font-size: 13px;
  margin: 0;
}

.empty-list li + li {
  margin-top: 4px;
}

.message {
  margin-bottom: 14px;
  max-width: 92%;
  word-wrap: break-word;
}

.user-message {
  margin-left: auto;
}

.ai-message {
  margin-right: auto;
}

.message-meta {
  font-size: 11px;
  color: var(--color-text-subtle);
  margin-bottom: 4px;
}

.message-role {
  padding: 2px 8px;
  border-radius: 999px;
  background: var(--color-primary-soft);
  color: var(--color-text-muted);
}

.user-message .message-role {
  background: rgba(15, 23, 42, 0.06);
}

.message-content {
  font-size: 14px;
  line-height: 1.6;
  white-space: pre-wrap;
}

.user-message .message-content {
  background: var(--color-primary);
  color: #fff;
  border-radius: var(--radius-md) var(--radius-md) 4px var(--radius-md);
  padding: 10px 14px;
  box-shadow: var(--shadow-soft);
}

.ai-message .message-content {
  background: var(--color-bg-elevated);
  color: var(--color-text);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md) var(--radius-md) var(--radius-md) 4px;
  padding: 10px 14px;
}

.input-container {
  flex-shrink: 0;
}

.input-wrapper {
  display: flex;
  flex-direction: column;
  gap: 10px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  padding: 10px;
  background: var(--color-bg-elevated);
}

.chat-textarea :deep(.el-textarea__inner) {
  font-family: var(--font-sans);
  font-size: 14px;
  border: none;
  box-shadow: none;
  background: transparent;
  padding: 4px 0;
}

.input-actions {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 8px;
}

.send-btn {
  border-radius: 999px;
  padding-left: 20px;
  padding-right: 20px;
  font-weight: 600;
}

.thinking-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 4px;
  max-width: 92%;
}

.thinking-indicator {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: var(--color-text-muted);
}

.thinking-indicator .dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background-color: var(--color-primary);
  animation: blink 1.2s infinite ease-in-out both;
}

.thinking-indicator .dot:nth-child(2) {
  animation-delay: 0.2s;
}

.thinking-indicator .dot:nth-child(3) {
  animation-delay: 0.4s;
}

@media (prefers-reduced-motion: reduce) {
  .thinking-indicator .dot {
    animation: none;
    opacity: 0.7;
  }
}

@keyframes blink {
  0%,
  80%,
  100% {
    transform: scale(0);
    opacity: 0.3;
  }
  40% {
    transform: scale(1);
    opacity: 1;
  }
}
</style>
