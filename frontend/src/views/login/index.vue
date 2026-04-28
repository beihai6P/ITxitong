<template>
  <div class="login-container">
    <el-card class="login-card">
      <template #header>
        <div class="login-header">
          <h2>IT运维综合管理系统</h2>
        </div>
      </template>
      <el-form :model="loginForm" :rules="rules" ref="loginFormRef" @keyup.enter="handleLogin">
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="用户名"
            :prefix-icon="User"
            size="large"
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="密码"
            :prefix-icon="Lock"
            size="large"
            show-password
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" class="login-btn" size="large" @click="handleLogin">
            登录
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import request from '@/utils/request'

const router = useRouter()
const loginFormRef = ref<FormInstance>()
const loading = ref(false)

const loginForm = reactive({
  username: '',
  password: ''
})

const rules = reactive<FormRules>({
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
})

const handleLogin = async () => {
  if (!loginFormRef.value) return
  loginFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const res: any = await request.post('/api/v1/auth/login', {
          username: loginForm.username,
          password: loginForm.password
        })

        if (res?.data?.token) {
          localStorage.setItem('token', res.data.token)
          localStorage.setItem('user', JSON.stringify({
            id: res.data.userId,
            username: res.data.username,
            role: res.data.role
          }))

          const permissions = res.data.permissions || []
          localStorage.setItem('permissions', JSON.stringify(permissions))

          ElMessage.success('登录成功')
          router.push('/dashboard')
        } else {
          ElMessage.error('登录失败：未获取到 token')
        }
      } catch (error: any) {
        console.error('Login error:', error)
        let errorMessage = '登录失败：未知错误'
        if (error.response?.data?.message) {
          errorMessage = error.response.data.message
        } else if (error.message) {
          errorMessage = error.message
        }
        ElMessage.error(errorMessage)
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background-color: #2d3a4b;
}

.login-card {
  width: 400px;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.login-header {
  text-align: center;
  margin: 0;
}

.login-header h2 {
  margin: 0;
  color: #303133;
  font-size: 22px;
}

.login-btn {
  width: 100%;
  margin-top: 10px;
}
</style>
