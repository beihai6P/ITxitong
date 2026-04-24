import axios from 'axios'
import type { AxiosRequestConfig } from 'axios'
import { ElMessage } from 'element-plus'

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
  baseURL: '',
  timeout: 10000
})

// Request interceptor
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

// Response interceptor
request.interceptors.response.use(
  response => {
    return response.data
  },
  error => {
    ElMessage.error(error.response?.data?.message || '请求失败')
    return Promise.reject(error)
  }
)

export default request
