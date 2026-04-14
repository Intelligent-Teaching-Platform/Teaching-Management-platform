import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
// 导入对应包

import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'

import ElementPlus from 'unplugin-element-plus/vite'

// https://vitejs.dev/config/
export default defineConfig({
  server: {
      open: '/',
      port: 4000, // 项目端口（可自定义）
    proxy: {
      '/files': {
        target: 'http://localhost:9090',
        changeOrigin: true,
      },
      '/material': { target: 'http://localhost:9090', changeOrigin: true },
      '/admin': { target: 'http://localhost:9090', changeOrigin: true },
      '/teacher': { target: 'http://localhost:9090', changeOrigin: true },
      '/student': { target: 'http://localhost:9090', changeOrigin: true },
      '/course': { target: 'http://localhost:9090', changeOrigin: true },
      '/college': { target: 'http://localhost:9090', changeOrigin: true },
      '/speciality': { target: 'http://localhost:9090', changeOrigin: true },
      '/class': { target: 'http://localhost:9090', changeOrigin: true },
      '/clazz': { target: 'http://localhost:9090', changeOrigin: true },
      '/work': { target: 'http://localhost:9090', changeOrigin: true },
      '/notice': { target: 'http://localhost:9090', changeOrigin: true },
      '/question': { target: 'http://localhost:9090', changeOrigin: true },
      '/questionType': { target: 'http://localhost:9090', changeOrigin: true },
      '/testPaper': { target: 'http://localhost:9090', changeOrigin: true },
      '/score': { target: 'http://localhost:9090', changeOrigin: true },
      '/signIn': { target: 'http://localhost:9090', changeOrigin: true },
      '/choice': { target: 'http://localhost:9090', changeOrigin: true },
      '/task': { target: 'http://localhost:9090', changeOrigin: true },
      '/echarts': { target: 'http://localhost:9090', changeOrigin: true },
      '/web': { target: 'http://localhost:9090', changeOrigin: true },
    },
  },
  build: {
    target: 'es2015',
    rollupOptions: {
      output: {
        manualChunks: {
          'vue-vendor': ['vue', 'vue-router'],
          'element-plus': ['element-plus'],
        },
        chunkFileNames: 'js/[name]-[hash].js',
        assetFileNames: 'assets/[name]-[hash][extname]',
      },
    },
    chunkSizeWarningLimit: 800,
  },
  plugins: [
    vue(),
    AutoImport({
      resolvers: [ElementPlusResolver(
          { importStyle: 'sass' }
      )],
    }),
    Components({
      resolvers: [ElementPlusResolver(
          { importStyle: 'sass' }
      )],
    }),

    // 按需定制主题配置
    ElementPlus({
      useSource: true,
    }),
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },
  css: {
    preprocessorOptions: {
      scss: {
        // 自动导入定制化样式文件进行样式覆盖
        additionalData: `
          @use "@/assets/css/index.scss" as *;
        `,
      }
    }
  }
})
