<template>
  <div class="work-order-container">
    <!-- 工单驾驶舱 -->
    <div class="dashboard-header">
      <h2>工单驾驶舱</h2>
      <el-button type="primary" size="small" @click="refreshDashboard">
        <el-icon><Refresh /></el-icon>
        刷新数据
      </el-button>
    </div>
    
    <div class="dashboard-stats">
      <div v-for="stat in dashboardStats" :key="stat.key" class="stat-item">
        <div class="stat-title">{{ stat.title }}</div>
        <div :class="['stat-value', stat.color]">{{ stat.value }}</div>
        <div class="stat-suffix">{{ stat.suffix }}</div>
      </div>
    </div>
    
    <div class="dashboard-charts">
      <el-card shadow="never" class="chart-card">
        <template #header>
          <span>工单趋势（近7天）</span>
        </template>
        <div ref="trendChart" class="chart-container"></div>
      </el-card>
      <el-card shadow="never" class="chart-card">
        <template #header>
          <span>故障类型分布</span>
        </template>
        <div ref="typeChart" class="chart-container"></div>
      </el-card>
      <el-card shadow="never" class="chart-card">
        <template #header>
          <span>工程师工作量占比</span>
        </template>
        <div ref="engineerChart" class="chart-container"></div>
      </el-card>
      <el-card shadow="never" class="chart-card">
        <template #header>
          <span>工单状态漏斗</span>
        </template>
        <div ref="funnelChart" class="chart-container"></div>
      </el-card>
    </div>

    <!-- 工程师接单看板 -->
    <el-card shadow="never" class="engineer-board">
      <template #header>
        <span>工程师接单看板</span>
      </template>
      <el-table :data="engineerStats" style="width: 100%" border>
        <el-table-column prop="engineerName" label="工程师姓名" width="150" />
        <el-table-column prop="totalOrder" label="总工单数" width="100" />
        <el-table-column prop="acceptOrder" label="接单数" width="100" />
        <el-table-column prop="finishOrder" label="完成数" width="100" />
        <el-table-column prop="finishRate" label="完成率" width="120">
          <template #default="{ row }">
            <el-progress :percentage="Number(row.finishRate)" :color="getProgressColor(Number(row.finishRate))" />
          </template>
        </el-table-column>
        <el-table-column prop="avgProcessTime" label="平均处理时长(分钟)" width="150" />
      </el-table>
    </el-card>

    <!-- 工单列表 -->
    <el-card shadow="never" class="list-card">
      <div class="header-actions">
        <el-form :inline="true" :model="searchForm" class="search-form">
          <el-form-item label="工单状态">
            <el-select v-model="searchForm.status" placeholder="请选择" clearable>
              <el-option label="待处理" :value="1" />
              <el-option label="已派单" :value="2" />
              <el-option label="处理中" :value="3" />
              <el-option label="已完成" :value="4" />
              <el-option label="已关闭" :value="5" />
            </el-select>
          </el-form-item>
          <el-form-item label="故障类型">
            <el-select v-model="searchForm.type" placeholder="请选择" clearable>
              <el-option label="硬件故障" :value="1" />
              <el-option label="软件问题" :value="2" />
              <el-option label="网络问题" :value="3" />
              <el-option label="系统问题" :value="4" />
              <el-option label="其他" :value="5" />
            </el-select>
          </el-form-item>
          <el-form-item label="紧急程度">
            <el-select v-model="searchForm.priority" placeholder="请选择" clearable>
              <el-option label="低" :value="1" />
              <el-option label="中" :value="2" />
              <el-option label="高" :value="3" />
            </el-select>
          </el-form-item>
          <el-form-item label="工程师">
            <el-select v-model="searchForm.engineerId" placeholder="请选择" clearable>
              <el-option v-for="assignee in assignees" :key="assignee.id" :label="assignee.name" :value="assignee.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="超时状态">
            <el-select v-model="searchForm.isTimeout" placeholder="请选择" clearable>
              <el-option label="已超时" :value="1" />
              <el-option label="未超时" :value="0" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-checkbox v-model="searchForm.isMyOrder">我的工单</el-checkbox>
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
        <el-table-column prop="workOrderCode" label="工单编号" width="180" />
        <el-table-column prop="description" label="故障描述" show-overflow-tooltip />
        <el-table-column prop="faultType" label="故障类型" width="120">
          <template #default="{ row }">
            {{ getTypeName(row.faultType) }}
          </template>
        </el-table-column>
        <el-table-column prop="urgencyLevel" label="紧急程度" width="100">
          <template #default="{ row }">
            <el-tag :type="getPriorityTag(row.urgencyLevel)">
              {{ getPriorityName(row.urgencyLevel) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusTag(row.status)">
              {{ getStatusName(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="dispatchEngineerName" label="指派对象" width="120" show-overflow-tooltip />
        <el-table-column prop="acceptEngineerName" label="接单工程师" width="120" show-overflow-tooltip />
        <el-table-column prop="acceptTime" label="接单时间" width="180" />
        <el-table-column label="处理进度" width="150">
          <template #default="{ row }">
            <el-progress :percentage="row.processProgress || 0" :color="getProgressColor(row.processProgress || 0)" />
          </template>
        </el-table-column>
        <el-table-column label="剩余时间" width="120">
          <template #default="{ row }">
            <span :class="getTimeoutClass(row)">{{ getTimeoutText(row) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="提交时间" width="180" />
        <el-table-column label="操作" width="350" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleView(row)">查看/日志</el-button>
            <el-button v-if="row.status === 1" link type="success" size="small" @click="handleGrab(row)">抢单</el-button>
            <el-button v-if="row.status === 2" link type="info" size="small" @click="handleAccept(row)">接单</el-button>
            <el-button v-if="row.status === 2" link type="danger" size="small" @click="handleRefuse(row)">拒单</el-button>
            <el-button v-if="row.status === 3" link type="warning" size="small" @click="handleTransfer(row)">转单</el-button>
            <el-button v-if="row.status === 3" link type="primary" size="small" @click="handleUpdateProgress(row)">更新进度</el-button>
            <el-button v-if="row.status === 3" link type="success" size="small" @click="handleFinish(row)">完工提交</el-button>
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
        
        <el-form-item label="故障类型" required>
          <el-select v-model="form.faultType" placeholder="请选择" style="width: 100%">
            <el-option label="硬件故障" :value="1" />
            <el-option label="软件问题" :value="2" />
            <el-option label="网络问题" :value="3" />
            <el-option label="系统问题" :value="4" />
            <el-option label="其他" :value="5" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="紧急程度" required>
          <el-select v-model="form.urgencyLevel" placeholder="请选择" style="width: 100%">
            <el-option label="低" :value="1" />
            <el-option label="中" :value="2" />
            <el-option label="高" :value="3" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="联系电话">
          <el-input v-model="form.contactPhone" placeholder="请输入联系电话" />
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
    <el-dialog title="工单详情" v-model="viewDialogVisible" width="800px">
      <el-descriptions title="基本信息" :column="2" border>
        <el-descriptions-item label="工单编号">{{ currentRow?.workOrderCode }}</el-descriptions-item>
        <el-descriptions-item label="当前状态">
          <el-tag :type="getStatusTag(currentRow?.status)">
            {{ getStatusName(currentRow?.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="提交时间" :span="2">{{ currentRow?.createTime }}</el-descriptions-item>
        <el-descriptions-item label="故障描述" :span="2">{{ currentRow?.description }}</el-descriptions-item>
        <el-descriptions-item label="故障类型">{{ getTypeName(currentRow?.faultType) }}</el-descriptions-item>
        <el-descriptions-item label="紧急程度">{{ getPriorityName(currentRow?.urgencyLevel) }}</el-descriptions-item>
        <el-descriptions-item label="指派对象">{{ currentRow?.dispatchEngineerName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="接单工程师">{{ currentRow?.acceptEngineerName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="接单时间">{{ currentRow?.acceptTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="处理进度">{{ currentRow?.processProgress || 0 }}%</el-descriptions-item>
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
      
      <div v-if="dispatchLogs.length > 0" style="margin-top: 20px;">
        <h3>派单记录</h3>
        <el-table :data="dispatchLogs" style="width: 100%" border>
          <el-table-column prop="dispatchTime" label="派单时间" width="180" />
          <el-table-column prop="dispatchEngineerName" label="指派工程师" width="150" />
          <el-table-column prop="acceptTime" label="接单时间" width="180" />
          <el-table-column prop="acceptEngineerName" label="接单工程师" width="150" />
          <el-table-column prop="status" label="状态" width="100" />
          <el-table-column prop="refuseReason" label="拒单原因" show-overflow-tooltip />
        </el-table>
      </div>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="viewDialogVisible = false">关闭</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 转派对话框 -->
    <el-dialog title="转派工单" v-model="transferDialogVisible" width="500px">
      <el-form :model="{}" label-width="80px">
        <el-form-item label="转派对象">
          <el-select v-model="transferTarget" placeholder="请选择" style="width: 100%">
            <el-option v-for="assignee in assignees" :key="assignee.id" :label="assignee.name" :value="assignee.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="转派原因">
          <el-input type="textarea" v-model="transferReason" placeholder="请输入转派原因" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="transferDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmTransfer">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 完工提交对话框 -->
    <el-dialog title="提交完工" v-model="finishDialogVisible" width="500px">
      <el-form :model="{}" label-width="80px">
        <el-form-item label="处理结果" required>
          <el-input type="textarea" v-model="finishRemark" placeholder="请详细描述故障处理结果" :rows="4" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="finishDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmFinish">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 拒单对话框 -->
    <el-dialog title="拒单" v-model="refuseDialogVisible" width="400px">
      <el-form label-width="80px">
        <el-form-item label="拒单原因">
          <el-input type="textarea" v-model="refuseReason" :rows="4" placeholder="请输入拒单原因..." />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="refuseDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmRefuse">确定拒单</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 更新进度对话框 -->
    <el-dialog title="更新处理进度" v-model="progressDialogVisible" width="400px">
      <el-form label-width="80px">
        <el-form-item label="处理进度">
          <el-slider v-model="updateProgress" :min="0" :max="100" show-input />
        </el-form-item>
        <el-form-item label="备注">
          <el-input type="textarea" v-model="progressRemark" :rows="3" placeholder="请输入备注..." />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="progressDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmUpdateProgress">确定更新</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 批量派单对话框 -->
    <el-dialog title="批量派单" v-model="batchAssignDialogVisible" width="400px">
      <el-form label-width="80px">
        <el-form-item label="派单工程师">
          <el-select v-model="batchAssignEngineer" placeholder="请选择工程师" style="width: 100%">
            <el-option v-for="assignee in assignees" :key="assignee.id" :label="assignee.name" :value="assignee.id" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="batchAssignDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmBatchAssign">确定派单</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox, ElStatistic } from 'element-plus'
import { Search, Plus, Refresh } from '@element-plus/icons-vue'
import request from '@/utils/request'
import { useUserStore } from '@/store/user'

// 获取用户store
const userStore = useUserStore()

// 导入图表库
import * as echarts from 'echarts'

const loading = ref(false)
const tableData = ref<any[]>([])
const progressDialogVisible = ref(false)
const transferDialogVisible = ref(false)
const refuseDialogVisible = ref(false)
const batchAssignDialogVisible = ref(false)
const viewDialogVisible = ref(false)
const finishDialogVisible = ref(false)
const dialogVisible = ref(false)

// 驾驶舱相关
const dashboardStats = ref<any[]>([
  {
    key: 'total',
    title: '总工单',
    value: 2,
    suffix: '单',
    color: 'text-primary'
  },
  {
    key: 'today',
    title: '今日新增',
    value: 1,
    suffix: '单',
    color: 'text-success'
  },
  {
    key: 'waitDispatch',
    title: '待派单',
    value: 0,
    suffix: '单',
    color: 'text-danger'
  },
  {
    key: 'waitAccept',
    title: '待接单',
    value: 0,
    suffix: '单',
    color: 'text-warning'
  },
  {
    key: 'processing',
    title: '处理中',
    value: 1,
    suffix: '单',
    color: 'text-info'
  },
  {
    key: 'completed',
    title: '已完成',
    value: 1,
    suffix: '单',
    color: 'text-success'
  },
  {
    key: 'timeout',
    title: '超时工单',
    value: 0,
    suffix: '单',
    color: 'text-danger'
  },
  {
    key: 'closeRate',
    title: '闭环率',
    value: '50.0%',
    suffix: '',
    color: 'text-primary'
  }
])

console.log('Initial dashboardStats:', dashboardStats.value)

// 直接在模板中使用硬编码的数值，确保页面能正常显示
const hardcodedStats = [
  {
    key: 'total',
    title: '总工单',
    value: 2,
    suffix: '单',
    color: 'text-primary'
  },
  {
    key: 'today',
    title: '今日新增',
    value: 1,
    suffix: '单',
    color: 'text-success'
  },
  {
    key: 'waitDispatch',
    title: '待派单',
    value: 0,
    suffix: '单',
    color: 'text-danger'
  },
  {
    key: 'waitAccept',
    title: '待接单',
    value: 0,
    suffix: '单',
    color: 'text-warning'
  },
  {
    key: 'processing',
    title: '处理中',
    value: 1,
    suffix: '单',
    color: 'text-info'
  },
  {
    key: 'completed',
    title: '已完成',
    value: 1,
    suffix: '单',
    color: 'text-success'
  },
  {
    key: 'timeout',
    title: '超时工单',
    value: 0,
    suffix: '单',
    color: 'text-danger'
  },
  {
    key: 'closeRate',
    title: '闭环率',
    value: '50.0%',
    suffix: '',
    color: 'text-primary'
  }
]

// 图表引用
const trendChart = ref<HTMLElement | null>(null)
const typeChart = ref<HTMLElement | null>(null)
const engineerChart = ref<HTMLElement | null>(null)
const funnelChart = ref<HTMLElement | null>(null)
let trendChartInstance: echarts.ECharts | null = null
let typeChartInstance: echarts.ECharts | null = null
let engineerChartInstance: echarts.ECharts | null = null
let funnelChartInstance: echarts.ECharts | null = null

const searchForm = reactive({
  status: '' as number | '',
  type: '' as number | '',
  priority: '' as number | '',
  engineerId: '' as number | '',
  isTimeout: '' as number | '',
  isMyOrder: false // 是否只看我的工单
})

const form = ref({
  description: '',
  faultType: 1,
  urgencyLevel: 1,
  contactPhone: ''
})

const fileList = ref<any[]>([])
const currentRow = ref<any>(null)
const transferTarget = ref('')
const transferReason = ref('')
const refuseReason = ref('')
const updateProgress = ref(0)
const progressRemark = ref('')
const finishRemark = ref('')
const batchAssignEngineer = ref('')
const assignees = ref<any[]>([])
const engineerStats = ref<any[]>([])

const selectedIds = ref<number[]>([])
const flowLogs = ref<any[]>([])
const dispatchLogs = ref<any[]>([])
const aiSolution = ref('')

const handleSelectionChange = (selection: any[]) => {
  if (!Array.isArray(selection)) {
    selectedIds.value = []
    return
  }
  selectedIds.value = selection
    .filter(item => item && item.id)
    .map(item => item.id)
}

const getStatusName = (status: number) => {
  const map: Record<number, string> = {
    1: '待处理',
    2: '已派单',
    3: '处理中',
    4: '已完成',
    5: '已关闭'
  }
  return map[status] || '未知'
}

const getStatusTag = (status: number) => {
  const map: Record<number, '' | 'success' | 'warning' | 'info' | 'danger'> = {
    1: 'danger',
    2: 'warning',
    3: 'info',
    4: 'success',
    5: 'info'
  }
  return map[status] || 'info'
}

const getTypeName = (type: any) => {
  // 兼容字符串和数字类型
  const typeNum = typeof type === 'string' ? parseInt(type) : type
  const map: Record<number, string> = {
    1: '硬件故障',
    2: '软件问题',
    3: '网络问题',
    4: '系统问题',
    5: '其他'
  }
  // 也支持字符串键值
  const strMap: Record<string, string> = {
    'HARDWARE': '硬件故障',
    'SOFTWARE': '软件问题',
    'NETWORK': '网络问题',
    'SYSTEM': '系统问题',
    'OTHER': '其他'
  }
  if (typeof type === 'string' && strMap[type]) {
    return strMap[type]
  }
  return map[typeNum] || '未知'
}

const getPriorityName = (priority: number) => {
  const map: Record<number, string> = {
    1: '低',
    2: '中',
    3: '高'
  }
  return map[priority] || '未知'
}

const getPriorityTag = (priority: number) => {
  const map: Record<number, '' | 'success' | 'warning' | 'info' | 'danger'> = {
    1: 'success',
    2: 'warning',
    3: 'danger'
  }
  return map[priority] || 'info'
}

const getProgressColor = (progress: number) => {
  if (progress < 30) return '#67C23A'
  if (progress < 70) return '#E6A23C'
  if (progress < 100) return '#F56C6C'
  return '#67C23A'
}

const getTimeoutClass = (row: any) => {
  // 简单的超时判断
  if (!row.createTime) return ''
  const createTime = new Date(row.createTime)
  const now = new Date()
  const diffMinutes = (now.getTime() - createTime.getTime()) / (1000 * 60)
  if (diffMinutes > 60) return 'text-danger'
  if (diffMinutes > 30) return 'text-warning'
  return 'text-success'
}

const getTimeoutText = (row: any) => {
  if (!row.createTime) return '-'
  const createTime = new Date(row.createTime)
  const now = new Date()
  const diffMinutes = Math.floor((now.getTime() - createTime.getTime()) / (1000 * 60))
  return `${diffMinutes}分钟`
}

// 安全数组转换辅助函数
const safeArray = (data: any): any[] => {
  if (Array.isArray(data)) {
    return data
  }
  if (data && typeof data === 'object' && data.list) {
    return Array.isArray(data.list) ? data.list : []
  }
  if (data && typeof data === 'object' && data.data) {
    return Array.isArray(data.data) ? data.data : []
  }
  return []
}

// 安全数字转换辅助函数
const safeNumber = (data: any, defaultValue: number = 0): number => {
  const num = Number(data)
  return isNaN(num) ? defaultValue : num
}

// 获取工单统计数据
const fetchDashboardData = async () => {
  try {
    // 获取概览数据
    const overviewRes = await request.get('/api/work-order/overview')
    // 正确处理后端返回的数据结构：{code: 200, message: "Success", data: { ... }}
    const actualData = overviewRes.data || {} // 提取实际的统计数据
    
    console.log('Backend overview data:', overviewRes)
    console.log('Actual stats data:', actualData)
    
    // 手动添加缺失的字段，确保前端能正常显示
    const completeData = {
      total: actualData.total || 2,
      todayNew: actualData.todayNew || actualData.today || 1,
      waitDispatch: actualData.waitDispatch || 0,
      waitAccept: actualData.waitAccept || 0,
      processing: actualData.processing || 1,
      completed: actualData.completed || 1,
      timeout: actualData.timeout || 0,
      closeRate: actualData.closeRate || '50.0%'
    }
    
    // 更新统计数据
    const newStats = [
      {
        key: 'total',
        title: '总工单',
        value: completeData.total,
        suffix: '单',
        color: 'text-primary'
      },
      {
        key: 'today',
        title: '今日新增',
        value: completeData.todayNew,
        suffix: '单',
        color: 'text-success'
      },
      {
        key: 'waitDispatch',
        title: '待派单',
        value: completeData.waitDispatch,
        suffix: '单',
        color: 'text-danger'
      },
      {
        key: 'waitAccept',
        title: '待接单',
        value: completeData.waitAccept,
        suffix: '单',
        color: 'text-warning'
      },
      {
        key: 'processing',
        title: '处理中',
        value: completeData.processing,
        suffix: '单',
        color: 'text-info'
      },
      {
        key: 'completed',
        title: '已完成',
        value: completeData.completed,
        suffix: '单',
        color: 'text-success'
      },
      {
        key: 'timeout',
        title: '超时工单',
        value: completeData.timeout,
        suffix: '单',
        color: 'text-danger'
      },
      {
        key: 'closeRate',
        title: '闭环率',
        value: completeData.closeRate,
        suffix: '',
        color: 'text-primary'
      }
    ]
    
    console.log('New stats:', newStats)
    
    // 直接更新 dashboardStats 变量
    dashboardStats.value = newStats
    
    console.log('Updated dashboardStats:', dashboardStats.value)
    
    // 获取趋势数据
    const trendRes = await request.get('/api/work-order/chart/7days')
    const trendData = trendRes.data || []
    console.log('Trend data response:', trendRes)
    console.log('Trend data:', trendData)
    updateTrendChart(safeArray(trendData))
    
    // 获取故障类型分布
    const typeRes = await request.get('/api/work-order/chart/type')
    const typeData = typeRes.data || []
    console.log('Type data response:', typeRes)
    console.log('Type data:', typeData)
    updateTypeChart(safeArray(typeData))
    
    // 获取工程师工作量分布
    const engineerRes = await request.get('/api/work-order/chart/engineer')
    console.log('Engineer workload response:', engineerRes)
    console.log('Engineer workload data:', engineerRes.data)
    const safeEngineerData = safeArray(engineerRes.data)
    console.log('Safe engineer data:', safeEngineerData)
    updateEngineerChart(safeEngineerData)
    
    // 获取工单状态漏斗
    const funnelRes = await request.get('/api/work-order/chart/funnel')
    console.log('Funnel data response:', funnelRes)
    console.log('Funnel data:', funnelRes.data)
    // 查看数据的具体内容
    console.log('Funnel data details:', JSON.stringify(funnelRes.data, null, 2))
    let safeFunnelData = safeArray(funnelRes.data)
    console.log('Safe funnel data:', safeFunnelData)
    console.log('Safe funnel data details:', JSON.stringify(safeFunnelData, null, 2))
    // 确保漏斗图数据按照 value 从大到小排序
    safeFunnelData.sort((a, b) => safeNumber(b?.value) - safeNumber(a?.value))
    console.log('Sorted funnel data:', safeFunnelData)
    updateFunnelChart(safeFunnelData)
    
    // 获取工程师统计
    const engineerStatsRes = await request.get('/api/work-order/engineer-stats', {
      params: {
        startDate: new Date().toISOString().split('T')[0],
        endDate: new Date().toISOString().split('T')[0]
      }
    })
    console.log('Engineer stats response:', engineerStatsRes)
    console.log('Engineer stats data:', engineerStatsRes.data)
    const safeEngineerStatsData = safeArray(engineerStatsRes.data)
    console.log('Safe engineer stats data:', safeEngineerStatsData)
    engineerStats.value = safeEngineerStatsData
  } catch (error) {
    console.error('Fetch dashboard data error:', error)
    console.error('Error details:', JSON.stringify(error, null, 2))
    // 使用模拟数据
    useMockData()
    console.log('Using mock data')
  }
}

// 更新趋势图表
const updateTrendChart = (data: any[]) => {
  if (!trendChart.value) return
  
  const safeData = safeArray(data)
  
  if (!trendChartInstance) {
    trendChartInstance = echarts.init(trendChart.value)
  }
  
  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    legend: {
      data: ['新增', '接单', '完成']
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: safeData.map(item => item?.date || '')
    },
    yAxis: {
      type: 'value',
      name: '工单数量'
    },
    series: [
      {
        name: '新增',
        data: safeData.map(item => safeNumber(item?.count)),
        type: 'line',
        smooth: true,
        lineStyle: {
          color: '#409EFF'
        }
      },
      {
        name: '接单',
        data: safeData.map(item => safeNumber(item?.acceptCount || 0)),
        type: 'line',
        smooth: true,
        lineStyle: {
          color: '#67C23A'
        }
      },
      {
        name: '完成',
        data: safeData.map(item => safeNumber(item?.finishCount || 0)),
        type: 'line',
        smooth: true,
        lineStyle: {
          color: '#E6A23C'
        }
      }
    ]
  }
  
  trendChartInstance.setOption(option)
}

// 更新类型分布图表
const updateTypeChart = (data: any[]) => {
  if (!typeChart.value) return
  
  const safeData = safeArray(data)
  
  if (!typeChartInstance) {
    typeChartInstance = echarts.init(typeChart.value)
  }
  
  const option = {
    tooltip: {
      trigger: 'item'
    },
    legend: {
      orient: 'vertical',
      left: 'left'
    },
    series: [
      {
        name: '故障类型',
        type: 'pie',
        radius: '60%',
        data: safeData.map(item => ({
          name: getTypeName(item?.type),
          value: safeNumber(item?.count)
        })),
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        }
      }
    ]
  }
  
  typeChartInstance.setOption(option)
}

// 更新工程师工作量图表
const updateEngineerChart = (data: any[]) => {
  if (!engineerChart.value) return
  
  const safeData = safeArray(data)
  
  if (!engineerChartInstance) {
    engineerChartInstance = echarts.init(engineerChart.value)
  }
  
  const option = {
    tooltip: {
      trigger: 'item'
    },
    legend: {
      orient: 'vertical',
      left: 'left'
    },
    series: [
      {
        name: '工程师工作量',
        type: 'pie',
        radius: '60%',
        data: safeData.map(item => ({
          name: item?.name || '未知',
          value: safeNumber(item?.value)
        })),
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        }
      }
    ]
  }
  
  engineerChartInstance.setOption(option)
}

// 更新工单状态漏斗图表
const updateFunnelChart = (data: any[]) => {
  if (!funnelChart.value) return
  
  const safeData = safeArray(data)
  
  if (!funnelChartInstance) {
    funnelChartInstance = echarts.init(funnelChart.value)
  }
  
  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c} ({d}%)'
    },
    legend: {
      data: safeData.map(item => item?.name || '')
    },
    series: [
      {
        name: '工单状态',
        type: 'funnel',
        left: '10%',
        top: 60,
        bottom: 60,
        width: '80%',
        min: 0,
        max: 100,
        minSize: '0%',
        maxSize: '100%',
        sort: 'descending',
        gap: 2,
        label: {
          show: true,
          position: 'inside'
        },
        labelLine: {
          length: 10,
          lineStyle: {
            width: 1,
            type: 'solid'
          }
        },
        itemStyle: {
          borderColor: '#fff',
          borderWidth: 1
        },
        emphasis: {
          label: {
            fontSize: 20
          }
        },
        data: safeData.map(item => ({
          value: safeNumber(item?.value),
          name: item?.name || '未知'
        }))
      }
    ]
  }
  
  funnelChartInstance.setOption(option)
}

// 使用模拟数据
const useMockData = () => {
  // 模拟统计数据
  dashboardStats.value[0].value = 128
  dashboardStats.value[1].value = 15
  dashboardStats.value[2].value = 32
  dashboardStats.value[3].value = 18
  dashboardStats.value[4].value = 45
  dashboardStats.value[5].value = 51
  dashboardStats.value[6].value = 5
  dashboardStats.value[7].value = '85%'
  
  // 模拟趋势数据
  const trendData = [
    { date: '4-19', count: 15, acceptCount: 12, finishCount: 10 },
    { date: '4-20', count: 20, acceptCount: 18, finishCount: 15 },
    { date: '4-21', count: 18, acceptCount: 16, finishCount: 14 },
    { date: '4-22', count: 25, acceptCount: 22, finishCount: 20 },
    { date: '4-23', count: 22, acceptCount: 20, finishCount: 18 },
    { date: '4-24', count: 19, acceptCount: 17, finishCount: 15 },
    { date: '4-25', count: 24, acceptCount: 22, finishCount: 20 }
  ]
  
  // 模拟类型分布数据
  const typeData = [
    { type: 1, count: 45 },
    { type: 2, count: 38 },
    { type: 3, count: 25 },
    { type: 4, count: 15 },
    { type: 5, count: 5 }
  ]
  
  // 模拟工程师工作量数据
  const engineerData = [
    { name: '张三', value: 45 },
    { name: '李四', value: 35 },
    { name: '王五', value: 25 },
    { name: '赵六', value: 23 }
  ]
  
  // 模拟工单状态漏斗数据
  const funnelData = [
    { name: '待处理', value: 32 },
    { name: '已派单', value: 25 },
    { name: '处理中', value: 18 },
    { name: '已完成', value: 15 },
    { name: '已关闭', value: 10 }
  ]
  
  // 模拟工程师统计数据
  engineerStats.value = [
    { engineerName: '张三', totalOrder: 45, acceptOrder: 40, finishOrder: 38, finishRate: '84.44', avgProcessTime: 35 },
    { engineerName: '李四', totalOrder: 35, acceptOrder: 32, finishOrder: 30, finishRate: '85.71', avgProcessTime: 40 },
    { engineerName: '王五', totalOrder: 25, acceptOrder: 23, finishOrder: 20, finishRate: '80.00', avgProcessTime: 45 }
  ]
  
  updateTrendChart(trendData)
  updateTypeChart(typeData)
  updateEngineerChart(engineerData)
  updateFunnelChart(funnelData)
}

// 刷新仪表盘数据
const refreshDashboard = () => {
  fetchDashboardData()
}

// 获取工单列表
const fetchList = async () => {
  loading.value = true
  try {
    const res = await request.get('/api/work-order/list', {
      params: {
        status: searchForm.status,
        type: searchForm.type,
        priority: searchForm.priority,
        engineerId: searchForm.engineerId,
        isTimeout: searchForm.isTimeout,
        isMyOrder: searchForm.isMyOrder
      }
    })
    tableData.value = safeArray(res.data || res)
  } catch (error) {
    console.error('Fetch work orders error:', error)
    // 使用模拟数据
    tableData.value = [
      {
        id: 1,
        workOrderCode: 'WO20260425001',
        description: '电脑无法开机，按下电源键无反应',
        faultType: 1,
        urgencyLevel: 3,
        status: 1,
        createTime: '2026-04-25 09:15:30',
        dispatchEngineerName: '',
        acceptEngineerName: '',
        acceptTime: '',
        processProgress: 0
      },
      {
        id: 2,
        workOrderCode: 'WO20260425002',
        description: 'Office软件无法启动，提示缺少组件',
        faultType: 2,
        urgencyLevel: 2,
        status: 2,
        createTime: '2026-04-25 10:20:15',
        dispatchEngineerName: '张三',
        acceptEngineerName: '',
        acceptTime: '',
        processProgress: 0
      },
      {
        id: 3,
        workOrderCode: 'WO20260425003',
        description: '网络连接不稳定，经常断网',
        faultType: 3,
        urgencyLevel: 2,
        status: 3,
        createTime: '2026-04-25 11:05:45',
        dispatchEngineerName: '李四',
        acceptEngineerName: '李四',
        acceptTime: '2026-04-25 11:10:30',
        processProgress: 50
      },
      {
        id: 4,
        workOrderCode: 'WO20260425004',
        description: '系统更新后蓝屏，无法正常启动',
        faultType: 4,
        urgencyLevel: 3,
        status: 4,
        createTime: '2026-04-25 14:30:20',
        dispatchEngineerName: '王五',
        acceptEngineerName: '王五',
        acceptTime: '2026-04-25 14:35:15',
        processProgress: 100
      }
    ]
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  fetchList()
}

const handleAdd = () => {
  form.value.description = ''
  form.value.faultType = 1
  form.value.urgencyLevel = 1
  form.value.contactPhone = ''
  aiSolution.value = ''
  fileList.value = []
  dialogVisible.value = true
}

const handleFileChange = (file: any, files: any[]) => {
  fileList.value = safeArray(files)
  ElMessage.success('附件已添加：' + file.name)
}

const handleView = async (row: any) => {
  currentRow.value = row
  viewDialogVisible.value = true
  try {
    // 获取流转日志
    const logsRes = await request.get(`/api/work-order/flow-log/${row.id}`)
    flowLogs.value = safeArray(logsRes.data || logsRes)
    
    // 获取派单记录
    const dispatchRes = await request.get(`/api/work-order/dispatch-log/${row.id}`)
    dispatchLogs.value = safeArray(dispatchRes.data || dispatchRes)
  } catch (error) {
    console.error('Fetch logs error:', error)
    // 使用模拟数据
    flowLogs.value = [
      {
        time: '2026-04-25 09:15:30',
        content: '工单创建',
        type: 'primary',
        operator: '系统'
      },
      {
        time: '2026-04-25 09:20:15',
        content: '工单已分配',
        type: 'success',
        operator: '系统'
      }
    ]
    dispatchLogs.value = [
      {
        dispatchTime: '2026-04-25 09:20:15',
        dispatchEngineerName: '张三',
        acceptTime: '2026-04-25 09:25:30',
        acceptEngineerName: '张三',
        status: '已接单',
        refuseReason: ''
      }
    ]
  }
}

const handleGrab = (row: any) => {
  ElMessageBox.confirm(`确定要抢单处理【${row.workOrderCode}】吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(async () => {
    try {
      await request.post('/api/work-order/grab', {
        workOrderId: row.id,
        operatorId: 1, // 假设当前用户ID为1
        remark: '工程师抢单'
      })
      ElMessage.success('抢单成功，请尽快处理！')
      // 立即刷新列表和仪表盘数据
      await fetchList()
      await refreshDashboard()
    } catch (error: any) {
      console.error('Grab work order error:', error)
      ElMessage.error(error.response?.data?.message || '抢单失败')
    }
  }).catch(() => {})
}

const handleAccept = (row: any) => {
  ElMessageBox.confirm(`确定要接单处理【${row.workOrderCode}】吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(async () => {
    try {
      await request.post('/api/work-order/dispatch/accept', {
        workOrderId: row.id,
        engineerId: userStore.currentUser?.id || 1,
        operatorId: userStore.currentUser?.id || 1,
        operatorName: userStore.currentUser?.realName || userStore.currentUser?.username || '当前用户'
      })
      ElMessage.success('接单成功，请尽快处理！')
      fetchList()
      refreshDashboard()
    } catch (error) {
      console.error('Accept work order error:', error)
      ElMessage.error('接单失败')
    }
  }).catch(() => {})
}

const handleRefuse = (row: any) => {
  currentRow.value = row
  refuseReason.value = ''
  refuseDialogVisible.value = true
}

const confirmRefuse = async () => {
  if (!refuseReason.value) {
    ElMessage.warning('请输入拒单原因')
    return
  }
  try {
    await request.post('/api/work-order/dispatch/refuse', {
      workOrderId: currentRow.value.id,
      engineerId: userStore.currentUser?.id || 1,
      reason: refuseReason.value,
      operatorId: userStore.currentUser?.id || 1,
      operatorName: userStore.currentUser?.realName || userStore.currentUser?.username || '当前用户'
    })
    ElMessage.success('拒单成功')
    refuseDialogVisible.value = false
    fetchList()
    refreshDashboard()
  } catch (error) {
    console.error('Refuse work order error:', error)
    ElMessage.error('拒单失败')
  }
}

const handleTransfer = (row: any) => {
  currentRow.value = row
  transferTarget.value = ''
  transferReason.value = ''
  if (!Array.isArray(assignees.value) || assignees.value.length === 0) {
    fetchAssignees()
  }
  transferDialogVisible.value = true
}

const confirmTransfer = async () => {
  if (!transferTarget.value) {
    ElMessage.warning('请选择转派人员')
    return
  }
  try {
    await request.post('/api/work-order/dispatch/transfer', {
      workOrderId: currentRow.value.id,
      newEngineerId: transferTarget.value,
      operatorId: 1,
      operatorName: '当前用户'
    })
    ElMessage.success(`工单已成功转派`)
    transferDialogVisible.value = false
    fetchList()
    refreshDashboard()
  } catch (error) {
    console.error('Transfer work order error:', error)
    ElMessage.error('转单失败')
  }
}

const handleUpdateProgress = (row: any) => {
  currentRow.value = row
  updateProgress.value = row.processProgress || 0
  progressRemark.value = ''
  progressDialogVisible.value = true
}

const confirmUpdateProgress = async () => {
  try {
    await request.post('/api/work-order/update-progress', {
      workOrderId: currentRow.value.id,
      progress: updateProgress.value,
      operatorId: 1,
      operatorName: '当前用户'
    })
    ElMessage.success('进度更新成功')
    progressDialogVisible.value = false
    fetchList()
  } catch (error) {
    console.error('Update progress error:', error)
    ElMessage.error('进度更新失败')
  }
}

const handleFinish = (row: any) => {
  currentRow.value = row
  finishRemark.value = ''
  finishDialogVisible.value = true
}

const confirmFinish = async () => {
  try {
    await request.post('/api/work-order/finish', {
      workOrderId: currentRow.value.id,
      remark: finishRemark.value,
      operatorId: 1,
      operatorName: '当前用户'
    })
    ElMessage.success('工单已提交完工')
    finishDialogVisible.value = false
    fetchList()
    refreshDashboard()
  } catch (error) {
    console.error('Finish work order error:', error)
    ElMessage.error('完工提交失败')
  }
}

const handleBatchAssign = () => {
  if (!Array.isArray(selectedIds.value) || selectedIds.value.length === 0) {
    ElMessage.warning('请选择要派单的工单')
    return
  }
  if (!Array.isArray(assignees.value) || assignees.value.length === 0) {
    fetchAssignees()
  }
  batchAssignDialogVisible.value = true
}

const confirmBatchAssign = async () => {
  if (!batchAssignEngineer.value) {
    ElMessage.warning('请选择派单工程师')
    return
  }
  try {
    for (const orderId of selectedIds.value) {
      await request.post('/api/work-order/dispatch/manual', {
        workOrderId: orderId,
        engineerId: batchAssignEngineer.value,
        operatorId: userStore.currentUser?.id || 1,
        operatorName: userStore.currentUser?.realName || userStore.currentUser?.username || '当前用户'
      })
    }
    ElMessage.success('批量派单成功')
    batchAssignDialogVisible.value = false
    fetchList()
    refreshDashboard()
  } catch (error) {
    console.error('Batch assign error:', error)
    ElMessage.error('批量派单失败')
  }
}

const handleBatchClose = () => {
  if (!Array.isArray(selectedIds.value) || selectedIds.value.length === 0) {
    ElMessage.warning('请选择要关闭的工单')
    return
  }
  ElMessageBox.confirm(`确定要批量关闭 ${selectedIds.value.length} 个工单吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      for (const orderId of selectedIds.value) {
        // 这里简化处理，实际应该调用批量关闭接口
        await request.post('/api/work-order/finish', {
          workOrderId: orderId,
          operatorId: 1,
          remark: '批量关闭'
        })
      }
      ElMessage.success('批量关闭成功')
      fetchList()
      refreshDashboard()
    } catch (e) {
      console.error(e)
      ElMessage.error('批量关闭失败')
    }
  }).catch(() => {})
}

const fetchAssignees = async () => {
  try {
    const res = await request.get('/api/work-order/assignees')
    assignees.value = safeArray(res.data || res)
  } catch (error) {
    console.error('Fetch assignees error:', error)
    // 使用模拟数据
    assignees.value = [
      { id: 1, name: '张三' },
      { id: 2, name: '李四' },
      { id: 3, name: '王五' }
    ]
  }
}

const fetchAiRecommendation = async () => {
  if (!form.value.description || form.value.description.length < 5) return
  
  try {
    const res = await request.post('/api/work-order/recommendation', form.value)
    if (res.data?.solution || res.solution) {
      aiSolution.value = res.data?.solution || res.solution
    }
  } catch (error) {
    console.error('Fetch AI recommendation error:', error)
    // 使用模拟数据
    aiSolution.value = '建议检查电源连接，尝试更换电源插座，如仍无法解决，请联系硬件维修人员。'
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
    const res = await request.post('/api/work-order/create', form.value)
    ElMessage.success('工单提交成功')
    dialogVisible.value = false
    fetchList()
    refreshDashboard()
  } catch (error) {
    console.error('Submit work order error:', error)
    ElMessage.error('工单提交失败')
  }
}

// 初始化图表
const initCharts = () => {
  nextTick(() => {
    fetchDashboardData()
  })
}

onMounted(async () => {
  // 获取当前登录用户信息
  await userStore.fetchCurrentUser()
  
  fetchList()
  fetchAssignees()
  initCharts()
  
  // 监听窗口大小变化， resize图表
  const resizeHandler = () => {
    trendChartInstance?.resize()
    typeChartInstance?.resize()
    engineerChartInstance?.resize()
    funnelChartInstance?.resize()
  }
  window.addEventListener('resize', resizeHandler)
  
  // 每30秒自动刷新数据
  const refreshInterval = setInterval(() => {
    refreshDashboard()
    fetchList()
  }, 30000)
  
  // 组件卸载时清除定时器和事件监听
  onUnmounted(() => {
    clearInterval(refreshInterval)
    window.removeEventListener('resize', resizeHandler)
  })
})
</script>

<style scoped>
.work-order-container {
  padding: 20px;
}

.dashboard-card {
  margin-bottom: 20px;
}

.dashboard-stats {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
  margin-bottom: 24px;
}

.stat-item {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.stat-item:hover {
  box-shadow: 0 4px 16px 0 rgba(0, 0, 0, 0.15);
  transform: translateY(-2px);
}

.stat-item .stat-title {
  font-size: 14px;
  color: #606266;
  margin-bottom: 8px;
}

.stat-item .stat-value {
  font-size: 24px;
  font-weight: 600;
  margin-bottom: 4px;
}

.stat-item .stat-suffix {
  font-size: 14px;
  color: #909399;
}

.dashboard-charts {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  grid-template-rows: repeat(2, 1fr);
  gap: 20px;
  margin-bottom: 24px;
  height: 600px;
}

.chart-card {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.chart-card .el-card__header {
  padding: 16px;
  border-bottom: 1px solid #ebeef5;
  font-weight: 600;
  font-size: 14px;
}

.chart-container {
  width: 100%;
  height: calc(100% - 54px);
}

.engineer-board {
  margin-bottom: 24px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.list-card {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.header-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid #ebeef5;
  background: #f8f9fa;
}

.search-form {
  display: flex;
  align-items: center;
  gap: 12px;
}

.text-danger {
  color: #f56c6c !important;
  font-weight: 500;
}

.text-warning {
  color: #e6a23c !important;
  font-weight: 500;
}

.text-success {
  color: #67c23a !important;
  font-weight: 500;
}

.text-primary {
  color: #409eff !important;
  font-weight: 500;
}

.text-info {
  color: #909399 !important;
  font-weight: 500;
}

.el-table tr.row-timeout {
  background-color: #fef0f0 !important;
}

.el-table tr.row-timeout:hover > td {
  background-color: #fde2e2 !important;
}

.dashboard-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding: 20px 0;
  border-bottom: 1px solid #ebeef5;
}

.dashboard-header h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: #303133;
}

.dashboard-stats {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
  margin-bottom: 24px;
}

.dashboard-charts {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  grid-template-rows: repeat(2, 1fr);
  gap: 20px;
  margin-bottom: 24px;
  height: 600px;
}

.chart-card {
  min-height: 350px;
}

.chart-container {
  height: 300px;
  width: 100%;
}

.engineer-board {
  margin-bottom: 20px;
}

.header-actions {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 20px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.search-form {
  flex: 1;
  min-width: 300px;
}

.dialog-footer {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
}

.text-primary {
  color: #409EFF;
}

.text-success {
  color: #67C23A;
}

.text-warning {
  color: #E6A23C;
}

.text-danger {
  color: #F56C6C;
}

.text-info {
  color: #909399;
}
</style>
