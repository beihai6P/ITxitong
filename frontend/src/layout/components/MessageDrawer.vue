<template>
  <el-drawer
    v-model="visible"
    title="消息中心"
    direction="rtl"
    size="400px"
  >
    <el-tabs v-model="activeTab" @tab-change="fetchMessages">
      <el-tab-pane label="未读消息" name="unread">
        <div class="message-list" v-loading="loading">
          <div v-for="msg in messages" :key="msg.id" class="message-item">
            <div class="msg-header">
              <span class="msg-title">{{ msg.title }}</span>
              <el-tag size="small" :type="msg.type === 'alert' ? 'danger' : 'info'">{{ msg.type }}</el-tag>
            </div>
            <div class="msg-content">{{ msg.content }}</div>
            <div class="msg-footer">
              <span class="msg-time">{{ msg.createTime }}</span>
              <el-button link type="primary" @click="handleRead(msg.id)">标为已读</el-button>
            </div>
          </div>
          <el-empty v-if="messages.length === 0" description="暂无消息" />
        </div>
      </el-tab-pane>
      <el-tab-pane label="已读消息" name="read">
        <div class="message-list" v-loading="loading">
          <div v-for="msg in messages" :key="msg.id" class="message-item is-read">
            <div class="msg-header">
              <span class="msg-title">{{ msg.title }}</span>
              <el-tag size="small" type="info">{{ msg.type }}</el-tag>
            </div>
            <div class="msg-content">{{ msg.content }}</div>
            <div class="msg-footer">
              <span class="msg-time">{{ msg.createTime }}</span>
            </div>
          </div>
          <el-empty v-if="messages.length === 0" description="暂无消息" />
        </div>
      </el-tab-pane>
    </el-tabs>
  </el-drawer>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { getMessageList, markAsRead } from '@/api/sysMessage'
import type { SysMessage } from '@/api/sysMessage'
import { ElMessage } from 'element-plus'

const props = defineProps<{
  modelValue: boolean
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
  (e: 'read-success'): void
}>()

const visible = ref(props.modelValue)
watch(() => props.modelValue, (val) => {
  visible.value = val
  if (val) {
    fetchMessages()
  }
})
watch(visible, (val) => {
  emit('update:modelValue', val)
})

const activeTab = ref('unread')
const loading = ref(false)
const messages = ref<SysMessage[]>([])

const fetchMessages = async () => {
  loading.value = true
  try {
    const isRead = activeTab.value === 'read'
    const res: any = await getMessageList({ isRead })
    messages.value = res.data?.list || []
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const handleRead = async (id: number) => {
  try {
    await markAsRead(id)
    ElMessage.success('已标记为已读')
    fetchMessages()
    emit('read-success')
  } catch (error) {
    console.error(error)
  }
}
</script>

<style scoped>
.message-list {
  min-height: 200px;
}
.message-item {
  padding: 12px;
  border-bottom: 1px solid #ebeef5;
}
.message-item.is-read .msg-title,
.message-item.is-read .msg-content {
  color: #909399;
}
.msg-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}
.msg-title {
  font-weight: bold;
  font-size: 14px;
}
.msg-content {
  font-size: 13px;
  color: #606266;
  margin-bottom: 8px;
}
.msg-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.msg-time {
  font-size: 12px;
  color: #909399;
}
</style>
