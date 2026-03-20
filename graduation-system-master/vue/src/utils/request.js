import { ElMessage } from 'element-plus'
import router from '../router'
import axios from 'axios'
import { getServerUrl } from '@/utils/appConfig'

const request = axios.create({
  baseURL: getServerUrl(),
  timeout: 30000
})


// 请求拦截器
request.interceptors.request.use(config => {
    // 1. 自动添加 Token
    const token = localStorage.getItem('token') // 假设token存储在localStorage
    if (token) {
        config.headers['Authorization'] = `Bearer ${token}`
    }

    // 2. 处理 Content-Type
    if (!(config.data instanceof FormData)) {
        config.headers['Content-Type'] = 'application/json;charset=utf-8'
    }

    return config
}, error => {
    return Promise.reject(error)
})

// 响应拦截器
request.interceptors.response.use(
    response => {
        // 1. 处理二进制响应（如文件下载）
        if (response.config.responseType === 'blob') {
            return response
        }

        // 2. 处理JSON响应
        let res = response.data
        if (typeof res === 'string') {
            try {
                res = res ? JSON.parse(res) : res
            } catch (e) {
                console.error('JSON解析错误', e)
            }
        }

        // 3. 处理业务错误码
        if (res?.code === '401') {
            ElMessage.error(res.msg || '登录已过期')
            router.push('/login')
            return Promise.reject(new Error('未授权访问'))
        }

        // 4. 其他成功响应
        return res
    },
    error => {
        // 1. 处理HTTP错误
        if (error.response) {
            const status = error.response.status
            const cfg = error.config || {}
            switch (status) {
                case 401:
                    ElMessage.error('登录已过期，请重新登录')
                    router.push('/login')
                    break
                case 403:
                    ElMessage.error('没有权限访问')
                    break
                case 404:
                    // 允许某些请求静默处理 404（不弹提示），例如驾驶舱等演示数据接口
                    if (!cfg._ignoreNotFound) {
                        ElMessage.error('资源不存在')
                    }
                    break
                case 500:
                    ElMessage.error('服务器错误')
                    break
                default:
                    ElMessage.error(error.response.data?.msg || '请求失败')
            }
        } else if (error.request) {
            // 2. 请求已发出但没有响应
            ElMessage.error('请求超时，请检查网络')
        } else {
            // 3. 其他错误
            ElMessage.error('请求错误：' + error.message)
        }

        return Promise.reject(error)
    }
)

export default request