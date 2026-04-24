<template>
  <div class="work-order-container">
    <el-card shadow="never">
      <div class="header-actions">
        <el-form :inline="true" :model="searchForm" class="search-form">
          <el-form-item label="工单状态">
            <el-select v-model="searchForm.status" placeholder="请选择" clearable>
              <el-option label="待处理" :value="1" />
              <el-option label="处理中" :value="2" />
              <el-option label="待验收" :value="3" />
              <el-option label="已完成" :value="4" />
              <el-option label="已关闭" :value="5" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch" :icon="Search">查询</el-button>
          </el-form-item>
        </el-form>
        <div>
          <el-button type="warning" :disabled="selectedIds.length === 0" @click="handleBatchAssign">批量派单</el-button>
          <el-button type="danger" :disabled="selectedIds.length === 0" @click="handleBatchClose">批量关闭</el-button>
          <el-button type="primary" :icon="Plus" @click="handleAdd">提交报修</el-button>
        </div>
      </div>

      <el-table :data="tableData" v-loading="loading" style="width: 100%" border @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" />
        <el-table-column prop="id" label="工单编号" width="150" />
        <el-table-column prop="description" label="故障描述" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusTag(row.status)">
              {{ getStatusName(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="提交时间" width="180" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleView(row)">查看/日志</el-button>
            <el-button v-if="row.status === 1" link type="success" size="small" @click="handleGrab(row)">抢单</el-button>
            <el-button v-if="row.status === 2" link type="warning" size="small" @click="handleTransfer(row)">转单</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 提交报修对话框 -->
    <el-dialog title="提交故障报修工单" v-model="dialogVisible" width="600px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="故障描述" required>
          <el-input 
            type="textarea" 
            v-model="form.description" 
            :rows="4" 
            placeholder="请详细描述故障现象..."
            @blur="fetchAiRecommendation" 
          />
        </el-form-item>
        
        <el-form-item label="上传附件">
          <el-upload
            class="upload-demo"
            action="#"
            :auto-upload="false"
            :on-change="handleFileChange"
            multiple
            :limit="3"
          >
            <el-button type="primary" plain size="small">点击上传图片/文件</el-button>
            <template #tip>
              <div class="el-upload__tip">
                支持 jpg/png/pdf 文件，不超过 5MB
              </div>
            </template>
          </el-upload>
        </el-form-item>

        <el-alert
          v-if="aiSolution"
          title="AI智能推荐解决方案"
          type="success"
          :description="aiSolution"
          show-icon
          style="margin-bottom: 20px"
        >
          <template #default>
            <div style="margin-top:10px;">
              <p>{{ aiSolution }}</p>
              <el-button size="small" type="success" @click="acceptSolution">采纳建议并关闭</el-button>
              <el-button size="small" @click="submitOrder">仍需报修</el-button>
            </div>
          </template>
        </el-alert>
      </el-form>
      <template #footer v-if="!aiSolution">
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitOrder">提交工单</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 工单详情与流转日志对话框 -->
    <el-dialog title="工单详情" v-model="viewDialogVisible" width="700px">
      <el-descriptions title="基本信息" :column="2" border>
        <el-descriptions-item label="工单编号">{{ currentRow?.id }}</el-descriptions-item>
        <el-descriptions-item label="当前状态">
          <el-tag :type="getStatusTag(currentRow?.status)">
            {{ getStatusName(currentRow?.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="提交时间" :span="2">{{ currentRow?.createTime }}</el-descriptions-item>
        <el-descriptions-item label="故障描述" :span="2">{{ currentRow?.description }}</el-descriptions-item>
      </el-descriptions>
      
      <div style="margin-top: 20px;">
        <h3>流转日志</h3>
        <el-timeline>
          <el-timeline-item
            v-for="(log, index) in flowLogs"
            :key="index"
            :timestamp="log.time"
            :type="log.type"
          >
            {{ log.content }} (操作人: {{ log.operator }})
          </el-timeline-item>
        </el-timeline>
      </div>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="viewDialogVisible = false">关闭</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 转单对话框 -->
    <el-dialog title="工单转派" v-model="transferDialogVisible" width="400px">
      <el-form label-width="80px">
        <el-form-item label="接单人">
          <el-select v-model="transferTarget" placeholder="请选择转派人员" style="width: 100%">
            <el-option label="张工 (网络组)" value="zhang" />
            <el-option label="李工 (系统组)" value="li" />
            <el-option label="王工 (硬件组)" value="wang" />
          </el-select>
        </el-form-item>
        <el-form-item label="转派原因">
          <el-input type="textarea" v-model="transferReason" :rows="3" placeholder="请输入转派原因..." />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="transferDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmTransfer">确定转派</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus } from '@element-plus/icons-vue'
import request from '@/utils/request'

const loading = ref(false)
const tableData = ref<any[]>([])
const dialogVisible = ref(false)
const viewDialogVisible = ref(false)
const transferDialogVisible = ref(false)

const searchForm = reactive({
  status: '' as number | ''
})

const form = ref({
  description: ''
})

const fileList = ref<any[]>([])
const currentRow = ref<any>(null)
const transferTarget = ref('')
const transferReason = ref('')

const selectedIds = ref<number[]>([])

const handleSelectionChange = (selection: any[]) => {
  selectedIds.value = selection.map(item => item.id)
}

const handleBatchAssign = () => {
  ElMessageBox.confirm(`确定要批量派单 ${selectedIds.value.length} 个工单吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await request.post('/api/v1/work-orders/batch-assign', { ids: selectedIds.value })
      ElMessage.success('批量派单成功')
      fetchList()
    } catch (e) {
      console.error(e)
    }
  }).catch(() => {})
}

const handleBatchClose = () => {
  ElMessageBox.confirm(`确定要批量关闭 ${selectedIds.value.length} 个工单吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await request.post('/api/v1/work-orders/batch-close', { ids: selectedIds.value })
      ElMessage.success('批量关闭成功')
      fetchList()
    } catch (e) {
      console.error(e)
    }
  }).catch(() => {})
}

const flowLogs = ref<any[]>([])

const aiSolution = ref('')

const getStatusName = (status: number) => {
  const map: Record<number, string> = {
    1: '待处理',
    2: '处理中',
    3: '待验收',
    4: '已完成',
    5: '已关闭'
  }
  return map[status] || '未知'
}

const getStatusTag = (status: number) => {
  const map: Record<number, string> = {
    1: 'danger',
    2: 'warning',
    3: 'primary',
    4: 'success',
    5: 'info'
  }
  return map[status] || 'info'
}

const fetchList = async () => {
  loading.value = true
  try {
    const res = await request.get('/api/v1/work-orders', {
      params: {
        status: searchForm.status
      }
    })
    tableData.value = res.data?.list || res.list || res.data || []
  } catch (error) {
    console.error('Fetch work orders error:', error)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  fetchList()
}

const handleAdd = () => {
  form.value.description = ''
  aiSolution.value = ''
  fileList.value = []
  dialogVisible.value = true
}

const handleFileChange = (file: any, files: any[]) => {
  fileList.value = files
  ElMessage.success('附件已添加：' + file.name)
}

const handleView = async (row: any) => {
  currentRow.value = row
  viewDialogVisible.value = true
  try {
    const res = await request.get(`/api/v1/work-orders/${row.id}/logs`)
    flowLogs.value = res.data?.logs || res.logs || res.data || []
  } catch (error) {
    console.error('Fetch logs error:', error)
    flowLogs.value = []
  }
}

const handleGrab = (row: any) => {
  ElMessageBox.confirm(`确定要抢单处理【${row.id}】吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(async () => {
    try {
      await request.post(`/api/v1/work-orders/${row.id}/grab`)
      ElMessage.success('抢单成功，请尽快处理！')
      fetchList()
    } catch (error) {
      console.error('Grab work order error:', error)
    }
  }).catch(() => {})
}

const handleTransfer = (row: any) => {
  currentRow.value = row
  transferTarget.value = ''
  transferReason.value = ''
  transferDialogVisible.value = true
}

const confirmTransfer = async () => {
  if (!transferTarget.value) {
    ElMessage.warning('请选择转派人员')
    return
  }
  try {
    await request.post(`/api/v1/work-orders/${currentRow.value.id}/transfer`, {
      target: transferTarget.value,
      reason: transferReason.value
    })
    ElMessage.success(`工单已成功转派给 ${transferTarget.value}`)
    transferDialogVisible.value = false
    fetchList()
  } catch (error) {
    console.error('Transfer work order error:', error)
  }
}

const fetchAiRecommendation = async () => {
  if (!form.value.description || form.value.description.length < 5) return
  
  try {
    const res = await request.post('/api/v1/work-orders/ai-recommendation', {
      description: form.value.description
    })
    if (res.data?.solution || res.solution) {
      aiSolution.value = res.data?.solution || res.solution
    }
  } catch (error) {
    console.error('Fetch AI recommendation error:', error)
  }
}

const acceptSolution = () => {
  ElMessage.success('已采纳AI建议，问题解决！')
  dialogVisible.value = false
}

const submitOrder = async () => {
  if (!form.value.description) {
    ElMessage.warning('请输入故障描述')
    return
  }
  try {
    const formData = new FormData()
    formData.append('description', form.value.description)
    fileList.value.forEach(file => {
      formData.append('files', file.raw || file)
    })
    
    await request.post('/api/v1/work-orders', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
    ElMessage.success('工单提交成功')
    dialogVisible.value = false
    fetchList()
  } catch (error) {
    console.error('Submit work order error:', error)
  }
}

onMounted(() => {
  fetchList()
})
</script>

<style scoped>
.work-order-container {
  padding: 0;
}
.header-actions {
  display: flex;
  justify-content: space-between;
  margin-bottom: 20px;
}
</style>
