import axios from 'axios'
import type { AxiosRequestConfig } from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import router from '@/router'

declare module 'axios' {
  export interface AxiosInstance {
    request<T = any, R = any, D = any>(config: AxiosRequestConfig<D>): Promise<R>;
    get<T = any, R = any, D = any>(url: string, config?: AxiosRequestConfig<D>): Promise<R>;
    delete<T = any, R = any, D = any>(url: string, config?: AxiosRequestConfig<D>): Promise<R>;
    head<T = any, R = any, D = any>(url: string, config?: AxiosRequestConfig<D>): Promise<R>;
    options<T = any, R = any, D = any>(url: string, config?: AxiosRequestConfig<D>): Promise<R>;
    post<T = any, R = any, D = any>(url: string, data?: D, config?: AxiosRequestConfig<D>): Promise<R>;
    put<T = any, R = any, D = any>(url: string, data?: D, config?: AxiosRequestConfig<D>): Promise<R>;
    patch<T = any, R = any, D = any>(url: string, data?: D, config?: AxiosRequestConfig<D>): Promise<R>;
  }
}

const request = axios.create({
  baseURL: 'http://localhost:8080',
  timeout: 10000
})

const handleAuthError = () => {
  localStorage.removeItem('token')
  ElMessageBox.alert('登录已过期，请重新登录', '提示', {
    confirmButtonText: '确定',
    type: 'warning',
    showClose: false
  }).then(() => {
    router.push('/login')
  }).catch(() => {
    router.push('/login')
  })
}

request.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

request.interceptors.response.use(
  response => {
    return response.data
  },
  error => {
    if (error.response) {
      const status = error.response.status
      const message = error.response.data?.message || ''
      
      if (status === 401) {
        if (message.includes('过期') || message.includes('无效') || message.includes('token')) {
          handleAuthError()
        } else {
          ElMessage.error(message || '未授权，请先登录')
        }
      } else if (status === 403) {
        ElMessage.error('没有权限访问该资源')
      } else if (status >= 500) {
        ElMessage.error(message || '服务器错误，请稍后重试')
      } else {
        ElMessage.error(message || '请求失败')
      }
    } else if (error.code === 'ECONNABORTED') {
      ElMessage.error('请求超时，请检查网络连接')
    } else if (error.code === 'ERR_NETWORK') {
      ElMessage.error('网络错误，请检查网络连接')
    } else {
      ElMessage.error(error.message || '请求失败')
    }
    return Promise.reject(error)
  }
)

export default request
