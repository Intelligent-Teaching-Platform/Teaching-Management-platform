// 应用配置，由 main.js 在启动时注入，避免 request.js 与 main 循环依赖
const config = { serverUrl: 'http://localhost:9090' }

export function setAppConfig(c) {
  if (c?.serverUrl) config.serverUrl = c.serverUrl
}

export function getServerUrl() {
  return config.serverUrl
}

/** 文件上传接口完整地址（el-upload 不会走 axios baseURL，需直连后端，否则易 404） */
export function getUploadUrl() {
  const base = String(getServerUrl() || '').replace(/\/$/, '')
  return base ? `${base}/files/upload` : '/files/upload'
}

/**
 * 解析头像/文件展示 URL：相对路径会拼上后端地址，否则原样返回
 */
export function resolveAvatarUrl(avatar) {
  if (!avatar || typeof avatar !== 'string') return ''
  const s = avatar.trim()
  if (s.startsWith('http://') || s.startsWith('https://')) return s
  const base = getServerUrl()
  return s.startsWith('/') ? base + s : base + '/' + s
}
