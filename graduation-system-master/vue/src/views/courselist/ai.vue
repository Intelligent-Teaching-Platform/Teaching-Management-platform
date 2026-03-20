<template>
  <div class="container">
    <div class="main">
      <div class="header">
        <div class="title-row">
          <span class="title">DeepSeek 智辅助教</span>
          <span class="subtitle">本课一对一问答 · 仅回答「{{ courseTitle }}」相关内容</span>
        </div>
        <div class="api-key-section">
          <input class="input-area" type="password" v-model="apiKey" placeholder="请输入您的API密钥"/>
          <button @click="saveApiKey" class="button-area">保存密钥</button>
        </div>
      </div>
      <div class="content">
        <div class="chat-container" id="chatContainer">
          <div v-if="messages.length === 0" class="empty-state">
            <div class="empty-title">👋 欢迎使用智辅实验管理平台 · AI 助教</div>
            <div class="empty-hint">你可以试试这样问我：</div>
            <ul class="empty-list">
              <li>帮我梳理一下本次实验的关键步骤？</li>
              <li>根据「某次实验结果」帮我写一段结论说明。</li>
              <li>把下面这段实验目的优化成更正式的表述。</li>
            </ul>
          </div>
          <template v-else>
            <!-- 对话内容 -->
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
                <span class="dot"></span>
                <span class="dot"></span>
                <span class="dot"></span>
                <span class="thinking-text">AI 正在回答中…</span>
              </div>
              <button type="button" class="button-stop" @click="stopGeneration">终止回答</button>
            </div>
          </template>
        </div>
        <div class="input-container">
          <div class="input-wrapper">
            <textarea
              class="text-area"
              v-model="userInput"
              placeholder="在此输入您想了解的内容，按 Enter 发送，Shift+Enter 换行"
              @keydown="handleKeyPress"
            ></textarea>
            <div class="icon-buttons">
              <i class="icon-clear" @click="clearInput"></i>
              <i class="icon-attachment"></i>
              <i class="icon-image"></i>
              <button
                class="button-send"
                :class="{ 'is-disabled': !userInput.trim() || isThinking }"
                :disabled="!userInput.trim() || isThinking"
                @click="sendMessage"
              >
                {{ isThinking ? '思考中…' : '发送' }}
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { useRoute } from 'vue-router';
import { ElMessage } from 'element-plus';

const route = useRoute();

const apiKey = ref('');
const userInput = ref('');
const messages = ref([]);
const isThinking = ref(false);
/** 用于中断当前流式请求 */
const streamAbort = ref(null);

/** 当前课程展示名（来自课程详情路由 query） */
const courseTitle = computed(() => {
  const n = route.query.courseName;
  return n && String(n).trim() ? String(n).trim() : '当前课程';
});

const courseIdDisplay = computed(() => {
  const id = route.query.id;
  return id != null && String(id).trim() !== '' ? String(id) : '未指定';
});

/**
 * 每次请求都会携带的固定系统提示，约束模型仅作本课程相关答疑。
 */
function buildSystemPrompt() {
  const name = courseTitle.value;
  const cid = courseIdDisplay.value;
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
  ].join('\n');
}

/** 将界面会话转为 API messages（含 system，排除占位文案） */
function buildApiMessages() {
  const list = [{ role: 'system', content: buildSystemPrompt() }];
  for (const m of messages.value) {
    const text = (m.content || '').trim();
    if (!text || text === '思考中...') continue;
    list.push({
      role: m.isUser ? 'user' : 'assistant',
      content: m.content,
    });
  }
  return list;
}


function saveApiKey() {
  if (apiKey.value.trim()) {
    ElMessage.success('API 密钥已保存');
    localStorage.setItem('deepseekApiKey', apiKey.value);
  } else {
    ElMessage.warning('请输入有效的 API 密钥');
  }
}

onMounted(() => {
  const savedApiKey = localStorage.getItem('deepseekApiKey');
  if (savedApiKey) {
    apiKey.value = savedApiKey;
  }
  const greet = `你好，我是课程「${courseTitle.value}」的 AI 助教，只回答与本课程学习、实验与作业相关的问题。你可以问我实验步骤、概念理解或报告写作思路等。`;
  addMessage(greet, false);
});

function addMessage(content, isUser = false) {
  messages.value.push({ content, isUser });
  const chatContainer = document.getElementById('chatContainer');
  chatContainer.scrollTop = chatContainer.scrollHeight;
}

function stopGeneration() {
  streamAbort.value?.abort();
}

async function callDeepSeekAPIStream() {
  const controller = new AbortController();
  streamAbort.value = controller;
  try {
    isThinking.value = true;
    addMessage('思考中...', false);

    const apiMessages = buildApiMessages();

    const response = await fetch('https://api.deepseek.com/v1/chat/completions', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${apiKey.value}`
      },
      body: JSON.stringify({
        model: "deepseek-chat",
        messages: apiMessages,
        stream: true
      }),
      signal: controller.signal
    });

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    const reader = response.body.getReader();
    const decoder = new TextDecoder();
    let accumulatedResponse = '';

    while (true) {
      const { value, done } = await reader.read();
      if (done) break;

      const chunk = decoder.decode(value);
      const lines = chunk.split('\n');

      for (const line of lines) {
        if (line.startsWith('data: ') && line !== 'data: [DONE]') {
          try {
            const jsonData = JSON.parse(line.slice(6));
            const content = jsonData.choices[0].delta.content || '';
            accumulatedResponse += content;
            updateLastMessage(accumulatedResponse);
          } catch (e) {
            console.error('解析响应数据时出错：', e);
          }
        } else if (line === 'data: [DONE]') {
          break;
        }
      }
    }
  } catch (error) {
    console.error('Error:', error);
    if (error.name === 'AbortError') {
      const last = messages.value[messages.value.length - 1];
      if (last && !last.isUser) {
        const cur = (last.content || '').trim();
        if (!cur || cur === '思考中...') {
          last.content = '已终止回答。';
        } else {
          last.content = last.content.replace(/\n*（已停止生成）$/, '') + '\n\n（已停止生成）';
        }
      }
      ElMessage.info('已终止回答');
    } else {
      updateLastMessage('抱歉，发生错误：' + error.message);
    }
  } finally {
    isThinking.value = false;
    streamAbort.value = null;
  }
}

function updateLastMessage(content) {
  const lastMessage = messages.value[messages.value.length - 1];
  if (lastMessage) {
    lastMessage.content = content;
    const chatContainer = document.getElementById('chatContainer');
    chatContainer.scrollTop = chatContainer.scrollHeight;
  }
}

async function sendMessage() {
  if (!userInput.value.trim()) {
    ElMessage.warning('请输入要咨询的内容');
    return;
  }

  if (!apiKey.value.trim()) {
    ElMessage.warning('请先设置 API 密钥');
    return;
  }

  const message = userInput.value.trim();
  addMessage(message, true);
  userInput.value = '';

  await callDeepSeekAPIStream();
}

function clearInput() {
  userInput.value = '';
}

function handleKeyPress(event) {
  if (event.key === 'Enter' && !event.shiftKey) {
    event.preventDefault();
    sendMessage();
  }
}
</script>

<style scoped lang="scss">
.container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  width: 100%;
  margin: 0;
  padding: 0;
  box-sizing: border-box;
  background: radial-gradient(circle at top left, #e3f2fd 0, #f5f7ff 40%, #eef2ff 100%);
}

.main {
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
  line-height: 1.6;
  background-color: #fff;
  color: #333;
  max-width: 800px;
  margin: 0 auto;
  border-radius: 16px;
  box-shadow: 0 18px 45px rgba(15, 23, 42, 0.18);
  overflow: hidden;
  padding: 20px;
  width: 100%;

  .header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 30px;

    .title-row {
      display: flex;
      flex-direction: column;
      align-items: flex-start;

      .title {
        color: #111827;
        font-size: 22px;
        font-weight: 600;
        letter-spacing: 0.04em;
      }

      .subtitle {
        margin-top: 4px;
        font-size: 13px;
        color: #6b7280;
      }
    }

    .api-key-section {
      display: flex;
      justify-content: center;
      align-items: center;
      gap: 8px;

      .input-area {
        padding: 8px 12px;
        width: 300px;
        border: 1px solid #ddd;
        border-radius: 4px;
        font-size: 14px;
        transition: all 0.2s;

        &:focus {
          outline: none;
          border-color: #6366f1;
          box-shadow: 0 0 0 2px rgba(99, 102, 241, 0.15);
        }
      }

      .button-area {
        padding: 8px 16px;
        background: linear-gradient(90deg, #6366f1, #8b5cf6);
        color: white;
        border: none;
        border-radius: 4px;
        cursor: pointer;
        transition: background-color 0.3s;
        font-size: 14px;

        &:hover {
          background: linear-gradient(90deg, #4f46e5, #7c3aed);
        }
      }
    }
  }

  .content {
    padding: 20px;
  }

  .chat-container {
    background: linear-gradient(to bottom, #f9fafb, #ffffff);
    border-radius: 8px;
    height: 600px;
    overflow-y: auto;
    margin-bottom: 20px;
    box-shadow: inset 0 0 0 1px rgba(148, 163, 184, 0.15);
    padding: 20px;
  }

  .empty-state {
    text-align: left;
    color: #4b5563;

    .empty-title {
      font-size: 15px;
      font-weight: 600;
      margin-bottom: 8px;
    }

    .empty-hint {
      font-size: 13px;
      color: #6b7280;
      margin-bottom: 6px;
    }

    .empty-list {
      padding-left: 18px;
      font-size: 13px;
      color: #4b5563;

      li + li {
        margin-top: 4px;
      }
    }
  }

  .message {
    margin-bottom: 10px;
    padding: 6px 10px;
    font-size: 16px;
    max-width: 80%;
    word-wrap: break-word;
  }

  .user-message {
    margin-left: auto;
    margin-right: 20px;
    .message-content {
      background: linear-gradient(135deg, #4f46e5, #6366f1);
      color: #ffffff;
      border-radius: 16px 4px 16px 16px;
      padding: 10px 14px;
      box-shadow: 0 8px 18px rgba(79, 70, 229, 0.25);
    }
  }

  .ai-message {
    margin-right: auto;
    margin-left: 20px;
    .message-content {
      background: #f3f4ff;
      border-radius: 4px 16px 16px 16px;
      padding: 10px 14px;
      box-shadow: 0 6px 14px rgba(148, 163, 184, 0.3);
      border: 1px solid rgba(129, 140, 248, 0.35);
    }
  }

  .message-meta {
    font-size: 11px;
    color: #9ca3af;
    margin-bottom: 4px;

    .message-role {
      padding: 2px 6px;
      border-radius: 999px;
      background: rgba(148, 163, 184, 0.12);
    }
  }

  .input-container {
    .input-wrapper {
      display: flex;
      background-color: #fff;
      border-radius: 16px;
      overflow: hidden;
      border: none;
      box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);

      .text-area {
        flex: 1;
        padding: 12px;
        border: none;
        resize: none;
        min-height: 64px;
        font-family: inherit;
        color: #333;
        outline: none;
        background-color: #f9fafb;

        &::placeholder {
          color: #9ca3af;
        }
      }

      .icon-buttons {
        display: flex;
        align-items: center;
        padding: 0 12px;

        .icon-clear,
        .icon-attachment,
        .icon-image {
          width: 24px;
          height: 24px;
          margin-right: 12px;
          cursor: pointer;
          background-repeat: no-repeat;
          background-position: center;
          background-size: contain;

          &:last-child {
            margin-right: 0;
          }

          &.icon-clear {
            background-image: url('data:image/svg+xml;base64,...');
          }

          &.icon-attachment {
            background-image: url('data:image/svg+xml;base64,...');
          }

          &.icon-image {
            background-image: url('data:image/svg+xml;base64,...');
          }
        }

        .button-send {
          padding: 8px 16px;
          background: linear-gradient(90deg, #6a5acd, #86a8e7);
          color: white;
          border: none;
          border-radius: 24px;
          cursor: pointer;
          font-size: 14px;
          transition: background 0.3s;

          &:hover {
            background: linear-gradient(90deg, #533483, #825098);
          }

          &.is-disabled {
            cursor: not-allowed;
            opacity: 0.6;
          }
        }
      }
    }
  }

  .thinking-row {
    display: flex;
    align-items: center;
    justify-content: space-between;
    flex-wrap: wrap;
    gap: 12px;
    margin: 8px 20px 0;
  }

  .button-stop {
    padding: 6px 14px;
    font-size: 13px;
    color: #9a3412;
    background: #fff7ed;
    border: 1px solid #fdba74;
    border-radius: 8px;
    cursor: pointer;
    flex-shrink: 0;
    transition: background 0.2s, border-color 0.2s;

    &:hover {
      background: #ffedd5;
      border-color: #fb923c;
    }
  }

  .thinking-indicator {
    display: inline-flex;
    align-items: center;
    gap: 4px;
    margin: 0;
    font-size: 12px;
    color: #6b7280;

    .dot {
      width: 6px;
      height: 6px;
      border-radius: 50%;
      background-color: #6366f1;
      animation: blink 1.2s infinite ease-in-out both;

      &:nth-child(2) {
        animation-delay: 0.2s;
      }

      &:nth-child(3) {
        animation-delay: 0.4s;
      }
    }

    .thinking-text {
      margin-left: 2px;
    }
  }
}

@keyframes blink {
  0%, 80%, 100% {
    transform: scale(0);
    opacity: 0.3;
  }
  40% {
    transform: scale(1);
    opacity: 1;
  }
}
</style>
