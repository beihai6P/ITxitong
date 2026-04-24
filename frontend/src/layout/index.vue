<template>
  <el-container class="layout-container">
    <el-aside width="200px" class="aside">
      <div class="logo">IT运维管理系统</div>
      <el-menu
        :default-active="route.path"
        class="el-menu-vertical"
        router
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
      >
        <el-menu-item index="/dashboard">
          <el-icon><Odometer /></el-icon>
          <span>监控大屏</span>
        </el-menu-item>
        <el-menu-item index="/asset">
          <el-icon><Monitor /></el-icon>
          <span>资产管理</span>
        </el-menu-item>
        <el-menu-item index="/work-order">
          <el-icon><Document /></el-icon>
          <span>工单报修</span>
        </el-menu-item>
        <el-sub-menu index="/system">
          <template #title>
            <el-icon><Setting /></el-icon>
            <span>系统管理</span>
          </template>
          <el-menu-item index="/role">
            <el-icon><User /></el-icon>
            <span>角色管理</span>
          </el-menu-item>
          <el-menu-item index="/menu">
            <el-icon><MenuIcon /></el-icon>
            <span>菜单管理</span>
          </el-menu-item>
          <el-menu-item index="/sys-dict">
            <el-icon><Reading /></el-icon>
            <span>字典管理</span>
          </el-menu-item>
          <el-menu-item index="/sys-config">
            <el-icon><Operation /></el-icon>
            <span>参数配置</span>
          </el-menu-item>
          <el-menu-item index="/log">
            <el-icon><Tickets /></el-icon>
            <span>日志管理</span>
          </el-menu-item>
        </el-sub-menu>
      </el-menu>
    </el-aside>
    
    <el-container>
      <el-header class="header">
        <GlobalSearch />
        <div class="header-right">
          <div class="message-icon" @click="openMessageDrawer">
            <el-badge :value="unreadCount" :max="99" :hidden="unreadCount === 0">
              <el-icon :size="20"><Bell /></el-icon>
            </el-badge>
          </div>
          <el-dropdown>
            <span class="el-dropdown-link">
              Admin <el-icon class="el-icon--right"><arrow-down /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="handleLogout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      
      <el-main class="main">
        <router-view v-slot="{ Component }">
          <transition name="fade-transform" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
    
    <MessageDrawer v-model="drawerVisible" @read-success="fetchUnreadCount" />
  </el-container>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Odometer, Monitor, Document, ArrowDown, Setting, User, Menu as MenuIcon, Tickets, Bell, Reading, Operation } from '@element-plus/icons-vue'
import { ElNotification } from 'element-plus'
import GlobalSearch from './components/GlobalSearch.vue'
import MessageDrawer from './components/MessageDrawer.vue'
import { getUnreadCount } from '@/api/sysMessage'

const route = useRoute()
const router = useRouter()
const ws = ref<WebSocket | null>(null)

const unreadCount = ref(0)
const drawerVisible = ref(false)

const fetchUnreadCount = async () => {
  try {
    const res: any = await getUnreadCount()
    unreadCount.value = res.data?.count || 0
  } catch (e) {
    console.error(e)
  }
}

const openMessageDrawer = () => {
  drawerVisible.value = true
}

const initWebSocket = () => {
  const token = localStorage.getItem('token') || ''
  const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
  // Assuming standard API URL, adjust if needed
  const wsUrl = `${protocol}//${window.location.host}/api/ws?token=${token}`
  
  try {
    ws.value = new WebSocket(wsUrl)
    
    ws.value.onopen = () => {
      console.log('WebSocket connection established')
    }
    
    ws.value.onmessage = (event) => {
      try {
        const data = JSON.parse(event.data)
        ElNotification({
          title: data.title || '新通知',
          message: data.message || event.data,
          type: data.type || 'info',
          duration: 5000
        })
      } catch (e) {
        ElNotification({
          title: '系统通知',
          message: event.data,
          type: 'info',
          duration: 5000
        })
      }
    }
    
    ws.value.onerror = (error) => {
      console.error('WebSocket error:', error)
    }
    
    ws.value.onclose = () => {
      console.log('WebSocket connection closed')
      // Optional: implement reconnect logic
      setTimeout(initWebSocket, 5000)
    }
  } catch (error) {
    console.error('Failed to initialize WebSocket:', error)
  }
}

onMounted(() => {
  initWebSocket()
  fetchUnreadCount()
})

onUnmounted(() => {
  if (ws.value) {
    ws.value.close()
  }
})

const handleLogout = () => {
  localStorage.removeItem('token')
  router.push('/login')
}
</script>

<style scoped>
.layout-container {
  height: 100vh;
}

.aside {
  background-color: #304156;
  color: #fff;
  display: flex;
  flex-direction: column;
}

.logo {
  height: 60px;
  line-height: 60px;
  text-align: center;
  font-size: 18px;
  font-weight: bold;
  color: #fff;
  background-color: #2b3643;
}

.el-menu-vertical {
  border-right: none;
  flex: 1;
}

.header {
  background-color: #fff;
  display: flex;
  justify-content: flex-end;
  align-items: center;
  box-shadow: 0 1px 4px rgba(0,21,41,.08);
  z-index: 10;
}

.header-right {
  display: flex;
  align-items: center;
  padding-right: 20px;
}

.message-icon {
  margin-right: 20px;
  cursor: pointer;
  display: flex;
  align-items: center;
}
.message-icon:hover {
  color: #409EFF;
}

.el-dropdown-link {
  cursor: pointer;
  display: flex;
  align-items: center;
}

.main {
  background-color: #f0f2f5;
  padding: 20px;
  overflow-y: auto;
}

/* fade-transform */
.fade-transform-leave-active,
.fade-transform-enter-active {
  transition: all .3s;
}

.fade-transform-enter-from {
  opacity: 0;
  transform: translateX(-30px);
}

.fade-transform-leave-to {
  opacity: 0;
  transform: translateX(30px);
}
</style>
