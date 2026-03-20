import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import ElementPlus from 'element-plus'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import axios from 'axios'
import { setAppConfig } from '@/utils/appConfig'
import '@/assets/css/global.css'

let globals = null

;(async function init() {
  // 1. 先加载配置再挂载，避免 request 首次请求时 baseURL 未就绪
  const configRes = await axios.get('/config.json')
  setAppConfig(configRes.data)
  const app = createApp(App)
  app.config.globalProperties.$config = configRes.data
  globals = app.config.globalProperties

  // 2. 注册插件与全局图标（按需组件由 unplugin-vue-components 处理）
  app.use(router)
  app.use(ElementPlus, { locale: zhCn })
  for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
    app.component(key, component)
  }

  app.mount('#app')
})()

export { globals }