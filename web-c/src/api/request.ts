import axios from 'axios'
import { showNotify } from 'vant'

const request = axios.create({
  baseURL: '/api',
  timeout: 10000
})

// 请求拦截器 — 携带 token
request.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

// 响应拦截器 — 统一错误处理
request.interceptors.response.use(
  (response) => {
    const res = response.data
    if (res.code !== 200) {
      showNotify({ type: 'danger', message: res.message || '请求失败' })
      return Promise.reject(new Error(res.message))
    }
    return res
  },
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token')
      showNotify({ type: 'danger', message: '登录已过期，请重新登录' })
    } else if (error.response?.status === 403) {
      showNotify({ type: 'danger', message: '无权限访问' })
    } else {
      showNotify({ type: 'danger', message: error.message || '网络错误' })
    }
    return Promise.reject(error)
  }
)

export default request
