<template>
  <div class="asset-container">
    <el-card shadow="never">
      <div class="header-actions">
        <el-form :inline="true" :model="searchForm" class="search-form">
          <el-form-item label="资产名称">
            <el-input v-model="searchForm.name" placeholder="请输入名称" clearable />
          </el-form-item>
          <el-form-item label="资产类型">
            <el-select v-model="searchForm.type" placeholder="请选择" clearable>
              <el-option label="服务器" value="server" />
              <el-option label="网络设备" value="network" />
              <el-option label="终端设备" value="terminal" />
              <el-option label="办公设备" value="office" />
            </el-select>
          </el-form-item>
          <el-form-item label="资产状态">
            <el-select v-model="searchForm.status" placeholder="请选择" clearable>
              <el-option label="在用" value="IN_USE" />
              <el-option label="闲置" value="IDLE" />
              <el-option label="维修中" value="REPAIR" />
              <el-option label="报废" value="SCRAP" />
            </el-select>
          </el-form-item>
          <el-form-item label="使用部门">
            <el-input v-model="searchForm.department" placeholder="请输入部门" clearable />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch" :icon="Search">查询</el-button>
            <el-button @click="resetSearch">重置</el-button>
          </el-form-item>
        </el-form>
        <div class="action-buttons">
          <el-button type="warning" :disabled="selectedIds.length === 0" @click="handleBatchAssign">批量分配</el-button>
          <el-button type="danger" :disabled="selectedIds.length === 0" @click="handleBatchClose">批量报废</el-button>
          <el-button type="primary" @click="handleScan">扫码解析</el-button>
          <el-button type="success" :icon="Plus" @click="handleAdd">新增资产</el-button>
        </div>
      </div>

      <el-table :data="tableData" v-loading="loading" style="width: 100%" border @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" />
        <el-table-column prop="id" label="资产编号" width="120" />
        <el-table-column prop="assetCode" label="资产编码" width="150" />
        <el-table-column prop="snCode" label="SN序列号" width="180" />
        <el-table-column prop="name" label="资产名称" min-width="120" />
        <el-table-column prop="brand" label="品牌" width="120" />
        <el-table-column prop="type" label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getTypeTag(row.type)">{{ getTypeName(row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="二级分类" width="120">
          <template #default="{ row }">
            {{ getSubtypeName(row.type, row.subtype) }}
          </template>
        </el-table-column>
        <el-table-column prop="department" label="使用部门" width="120" />
        <el-table-column prop="user" label="使用人" width="100" />
        <el-table-column prop="location" label="存放位置" width="120" />
        <el-table-column prop="ip" label="IP地址" width="140" />
        <el-table-column prop="assetStatus" label="资产状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusTag(row.assetStatus)">{{ getStatusName(row.assetStatus) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="purchaseDate" label="采购日期" width="120" />
        <el-table-column prop="warrantyExpireDate" label="质保到期" width="120">
          <template #default="{ row }">
            <span :class="{ 'text-warning': isWarrantyExpiring(row.warrantyExpireDate) }">
              {{ row.warrantyExpireDate || '-' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="info" size="small" @click="handleReceive(row)" v-if="row.assetStatus === 'IDLE'">领用</el-button>
            <el-button link type="warning" size="small" @click="handleTransfer(row)" v-if="row.assetStatus === 'IN_USE'">调拨</el-button>
            <el-button link type="success" size="small" @click="handleReturn(row)" v-if="row.assetStatus === 'IN_USE'">归还</el-button>
            <el-button link type="danger" size="small" @click="handleRepair(row)">报修</el-button>
            <el-button link type="danger" size="small" @click="handleScrap(row)" v-if="row.assetStatus !== 'SCRAP'">报废</el-button>
            <el-button link type="primary" size="small" @click="handleCreateWorkOrder(row)">报工单</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <el-dialog :title="dialogType === 'add' ? '新增资产' : '编辑资产'" v-model="dialogVisible" width="800px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="资产名称" prop="name">
              <el-input v-model="form.name" placeholder="请输入资产名称" />
            </el-form-item>
            <el-form-item label="品牌" prop="brand">
              <el-input v-model="form.brand" placeholder="请输入品牌" />
            </el-form-item>
            <el-form-item label="配置" prop="config">
              <el-input v-model="form.config" type="textarea" :rows="2" placeholder="请输入配置" />
            </el-form-item>
            <el-form-item label="类型" prop="type">
              <el-select v-model="form.type" placeholder="请选择资产类型" style="width: 100%" @change="handleTypeChange">
                <el-option label="服务器" value="server" />
                <el-option label="网络设备" value="network" />
                <el-option label="终端设备" value="terminal" />
                <el-option label="办公设备" value="office" />
              </el-select>
            </el-form-item>
            <el-form-item label="二级分类" prop="subtype" v-if="form.type">
              <el-select v-model="form.subtype" placeholder="请选择二级分类" style="width: 100%">
                <el-option v-for="item in getSubtypeOptions(form.type)" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
            <el-form-item label="使用部门" prop="department">
              <el-input v-model="form.department" placeholder="请输入使用部门" />
            </el-form-item>
            <el-form-item label="使用人" prop="user">
              <el-input v-model="form.user" placeholder="请输入使用人" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="SN序列号" prop="snCode">
              <el-input v-model="form.snCode" placeholder="请输入SN序列号" />
            </el-form-item>
            <el-form-item label="采购日期" prop="purchaseDate">
              <el-date-picker v-model="form.purchaseDate" type="date" placeholder="选择日期" value-format="YYYY-MM-DD" style="width: 100%" />
            </el-form-item>
            <el-form-item label="采购金额" prop="purchasePrice">
              <el-input v-model="form.purchasePrice" type="number" placeholder="请输入采购金额" />
            </el-form-item>
            <el-form-item label="质保到期" prop="warrantyExpireDate">
              <el-date-picker v-model="form.warrantyExpireDate" type="date" placeholder="选择日期" value-format="YYYY-MM-DD" style="width: 100%" />
            </el-form-item>
            <el-form-item label="资产状态" prop="assetStatus">
              <el-select v-model="form.assetStatus" placeholder="请选择资产状态" style="width: 100%">
                <el-option label="在用" value="IN_USE" />
                <el-option label="闲置" value="IDLE" />
                <el-option label="维修中" value="REPAIR" />
                <el-option label="报废" value="SCRAP" />
              </el-select>
            </el-form-item>
            <el-form-item label="存放位置" prop="location">
              <el-input v-model="form.location" placeholder="请输入存放位置" />
            </el-form-item>
            <el-form-item label="IP地址" prop="ip">
              <el-input v-model="form.ip" placeholder="请输入IP地址" />
            </el-form-item>
            <el-form-item label="管理员" prop="managerName">
              <el-input v-model="form.managerName" placeholder="请输入管理员" />
            </el-form-item>
            <el-form-item label="维保联系人" prop="maintenanceContact">
              <el-input v-model="form.maintenanceContact" placeholder="请输入维保联系人" />
            </el-form-item>
            <el-form-item label="备注" prop="remark">
              <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="请输入备注" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitForm">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <el-dialog title="资产扫码解析" v-model="scanDialogVisible" width="600px">
      <div class="scan-input-area">
        <el-input v-model="scanCode" placeholder="请输入资产编码或SN序列号（模拟扫码）" style="width: 300px; margin-right: 10px;" />
        <el-button type="primary" @click="submitScan">模拟扫码</el-button>
      </div>

      <div class="scan-result" v-if="scanResult">
        <el-divider>资产详情</el-divider>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="资产编码">{{ scanResult.asset?.assetCode }}</el-descriptions-item>
          <el-descriptions-item label="SN序列号">{{ scanResult.asset?.snCode }}</el-descriptions-item>
          <el-descriptions-item label="资产名称">{{ scanResult.asset?.assetName }}</el-descriptions-item>
          <el-descriptions-item label="品牌">{{ scanResult.asset?.brand }}</el-descriptions-item>
          <el-descriptions-item label="使用部门">{{ scanResult.asset?.department }}</el-descriptions-item>
          <el-descriptions-item label="使用人">{{ scanResult.asset?.userName }}</el-descriptions-item>
          <el-descriptions-item label="资产状态">
            <el-tag :type="getStatusTag(scanResult.asset?.assetStatus)">{{ getStatusName(scanResult.asset?.assetStatus) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="质保到期">{{ scanResult.asset?.warrantyExpireDate }}</el-descriptions-item>
        </el-descriptions>

        <el-divider>近期操作记录</el-divider>
        <el-timeline v-if="scanResult.recentHistory && scanResult.recentHistory.length">
          <el-timeline-item
            v-for="(log, index) in scanResult.recentHistory"
            :key="index"
            :timestamp="log.operateTime"
          >
            {{ getOperateTypeName(log.operateType) }} - {{ log.detail }}
            <span v-if="log.operatorName" style="margin-left: 10px; color: #999;">操作人：{{ log.operatorName }}</span>
          </el-timeline-item>
        </el-timeline>
        <div v-else style="text-align: center; color: #999;">暂无操作记录</div>
      </div>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="scanDialogVisible = false">关闭</el-button>
        </span>
      </template>
    </el-dialog>

    <el-dialog title="资产操作" v-model="operateDialogVisible" width="500px">
      <el-form :model="operateForm" :rules="operateRules" ref="operateFormRef" label-width="100px">
        <el-form-item v-if="operateType === 'receive' || operateType === 'transfer'" label="目标部门" prop="targetDepartment">
          <el-input v-model="operateForm.targetDepartment" placeholder="请输入目标部门" />
        </el-form-item>
        <el-form-item v-if="operateType === 'receive' || operateType === 'transfer'" label="目标使用人" prop="targetUser">
          <el-input v-model="operateForm.targetUser" placeholder="请输入目标使用人" />
        </el-form-item>
        <el-form-item label="操作人" prop="operatorName">
          <el-input v-model="operateForm.operatorName" placeholder="请输入操作人" />
        </el-form-item>
        <el-form-item label="操作原因" prop="reason">
          <el-input v-model="operateForm.reason" type="textarea" :rows="2" placeholder="请输入操作原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="operateDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitOperate">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <el-dialog title="报工单" v-model="workOrderDialogVisible" width="500px">
      <el-form :model="workOrderForm" :rules="workOrderRules" ref="workOrderFormRef" label-width="100px">
        <el-form-item label="故障描述" prop="description">
          <el-input v-model="workOrderForm.description" type="textarea" :rows="3" placeholder="请输入故障描述" />
        </el-form-item>
        <el-form-item label="紧急程度" prop="urgencyLevel">
          <el-select v-model="workOrderForm.urgencyLevel" placeholder="请选择紧急程度" style="width: 100%">
            <el-option label="低" value="1" />
            <el-option label="中" value="2" />
            <el-option label="高" value="3" />
            <el-option label="紧急" value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="操作人" prop="operatorName">
          <el-input v-model="workOrderForm.operatorName" placeholder="请输入操作人" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="workOrderDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitWorkOrder">提交</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { Search, Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import request from '@/utils/request'

const loading = ref(false)
const tableData = ref<any[]>([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(100)

const searchForm = reactive({
  name: '',
  type: '',
  status: '',
  department: ''
})

const scanDialogVisible = ref(false)
const scanCode = ref('')
const scanResult = ref<any>(null)

const selectedIds = ref<number[]>([])

const handleSelectionChange = (selection: any[]) => {
  selectedIds.value = selection.map(item => item.id)
}

const handleBatchAssign = () => {
  if (selectedIds.value.length === 0) {
    ElMessage.warning('请选择要分配的资产')
    return
  }
  ElMessageBox.confirm(`确定要批量分配 ${selectedIds.value.length} 个资产吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await request.post('/api/v1/assets/batch-assign', { 
        ids: selectedIds.value,
        department: '技术部',
        user: '管理员',
        operatorId: parseInt(localStorage.getItem('userId') || '1')
      })
      ElMessage.success('批量分配成功')
      fetchList()
    } catch (e) {
      console.error(e)
      ElMessage.error('批量分配失败：' + (e instanceof Error ? e.message : '未知错误'))
    }
  }).catch(() => {})
}

const handleBatchClose = () => {
  if (selectedIds.value.length === 0) {
    ElMessage.warning('请选择要报废的资产')
    return
  }
  ElMessageBox.confirm(`确定要批量报废 ${selectedIds.value.length} 个资产吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await request.post('/api/v1/assets/batch-close', { 
        ids: selectedIds.value,
        operatorId: parseInt(localStorage.getItem('userId') || '1')
      })
      ElMessage.success('批量报废成功')
      fetchList()
    } catch (e) {
      console.error(e)
      ElMessage.error('批量报废失败：' + (e instanceof Error ? e.message : '未知错误'))
    }
  }).catch(() => {})
}

const handleScan = () => {
  scanDialogVisible.value = true
  scanCode.value = ''
  scanResult.value = null
}

const submitScan = async () => {
  if (!scanCode.value) {
    ElMessage.warning('请输入资产编码或SN序列号')
    return
  }
  try {
    const res: any = await request.get('/api/asset/scan/resolve', {
      params: {
        assetCode: scanCode.value,
        operatorId: parseInt(localStorage.getItem('userId') || '1')
      }
    })
    if (res.code === 200 && res.data) {
      scanResult.value = res.data
      ElMessage.success('解析成功')
    } else {
      ElMessage.error(res.message || '未找到该资产')
      scanResult.value = null
    }
  } catch (error: any) {
    console.error('Scan error:', error)
  }
}

const dialogVisible = ref(false)
const dialogType = ref<'add' | 'edit'>('add')
const formRef = ref<FormInstance>()
const form = reactive({
  id: '',
  name: '',
  brand: '',
  config: '',
  department: '',
  user: '',
  type: '',
  subtype: '',
  ip: '',
  status: 'active',
  purchaseDate: '',
  snCode: '',
  purchasePrice: '',
  warrantyExpireDate: '',
  assetStatus: 'IN_USE',
  location: '',
  managerName: '',
  maintenanceContact: '',
  remark: ''
})

const rules = reactive<FormRules>({
  name: [{ required: true, message: '请输入资产名称', trigger: 'blur' }],
  type: [{ required: true, message: '请选择资产类型', trigger: 'change' }],
  assetStatus: [{ required: true, message: '请选择资产状态', trigger: 'change' }]
})

const operateDialogVisible = ref(false)
const operateType = ref<string>('')
const currentAssetId = ref<number>(0)
const operateFormRef = ref<FormInstance>()
const operateForm = reactive({
  targetDepartment: '',
  targetUser: '',
  operatorName: '',
  reason: ''
})

const operateRules = reactive<FormRules>({
  operatorName: [{ required: true, message: '请输入操作人', trigger: 'blur' }],
  reason: [{ required: true, message: '请输入操作原因', trigger: 'blur' }]
})

const workOrderDialogVisible = ref(false)
const workOrderFormRef = ref<FormInstance>()
const workOrderForm = reactive({
  description: '',
  urgencyLevel: 2,
  operatorName: ''
})

const workOrderRules = reactive<FormRules>({
  description: [{ required: true, message: '请输入故障描述', trigger: 'blur' }],
  operatorName: [{ required: true, message: '请输入操作人', trigger: 'blur' }]
})

const getTypeName = (type: string) => {
  const map: Record<string, string> = {
    server: '服务器',
    network: '网络设备',
    terminal: '终端设备',
    office: '办公设备'
  }
  return map[type] || '未知'
}

const getSubtypeOptions = (type: string) => {
  const map: Record<string, Array<{ label: string; value: string }>> = {
    server: [
      { label: '塔式服务器', value: 'tower' },
      { label: '机架式服务器', value: 'rack' },
      { label: '刀片服务器', value: 'blade' }
    ],
    network: [
      { label: '路由器', value: 'router' },
      { label: '交换机', value: 'switch' },
      { label: '防火墙', value: 'firewall' },
      { label: '无线AP', value: 'ap' }
    ],
    terminal: [
      { label: '组装机', value: 'assembled' },
      { label: '笔记本', value: 'laptop' },
      { label: '移动端', value: 'mobile' },
      { label: '一体机', value: 'all-in-one' }
    ],
    office: [
      { label: '打印机', value: 'printer' },
      { label: '扫描仪', value: 'scanner' },
      { label: '复印机', value: 'copier' },
      { label: '投影仪', value: 'projector' }
    ]
  }
  return map[type] || []
}

const handleTypeChange = () => {
  form.subtype = ''
}

const getTypeTag = (type: string) => {
  const map: Record<string, '' | 'success' | 'warning' | 'info' | 'danger'> = {
    server: '',
    network: 'warning',
    terminal: 'info',
    office: 'danger'
  }
  return map[type] || 'info'
}

const getStatusTag = (status: string) => {
  const map: Record<string, '' | 'success' | 'warning' | 'info' | 'danger'> = {
    IN_USE: 'success',
    IDLE: 'warning',
    REPAIR: 'info',
    SCRAP: 'danger'
  }
  return map[status] || 'info'
}

const getStatusName = (status: string) => {
  const map: Record<string, string> = {
    IN_USE: '在用',
    IDLE: '闲置',
    REPAIR: '维修中',
    SCRAP: '报废'
  }
  return map[status] || status
}

const getOperateTypeName = (type: string) => {
  const map: Record<string, string> = {
    CREATE: '创建',
    RECEIVE: '领用',
    TRANSFER: '调拨',
    RETURN: '归还',
    REPAIR: '维修',
    SCRAP: '报废',
    UPDATE: '更新',
    SCAN: '扫码'
  }
  return map[type] || type
}

const getSubtypeName = (type: string, subtype: string) => {
  const map: Record<string, Record<string, string>> = {
    server: {
      tower: '塔式服务器',
      rack: '机架式服务器',
      blade: '刀片服务器'
    },
    network: {
      router: '路由器',
      switch: '交换机',
      firewall: '防火墙',
      ap: '无线AP'
    },
    terminal: {
      assembled: '组装机',
      laptop: '笔记本',
      mobile: '移动端',
      'all-in-one': '一体机'
    },
    office: {
      printer: '打印机',
      scanner: '扫描仪',
      copier: '复印机',
      projector: '投影仪'
    }
  }
  return map[type]?.[subtype] || subtype
}

const isWarrantyExpiring = (date: string) => {
  if (!date) return false
  const expireDate = new Date(date)
  const today = new Date()
  const diffTime = expireDate.getTime() - today.getTime()
  const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24))
  return diffDays <= 30 && diffDays >= 0
}

const fetchList = async () => {
  loading.value = true
  try {
    const res = await request.get('/api/v1/assets', {
      params: {
        page: currentPage.value,
        pageSize: pageSize.value,
        name: searchForm.name,
        type: searchForm.type,
        status: searchForm.status,
        department: searchForm.department
      }
    })
    console.log('Asset list response:', res)
    tableData.value = res.data?.list || res.list || []
    total.value = res.data?.total || res.total || 0
  } catch (error) {
    console.error('Fetch assets error:', error)
    ElMessage.error('获取资产列表失败：' + (error instanceof Error ? error.message : '未知错误'))
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  currentPage.value = 1
  fetchList()
}

const resetSearch = () => {
  searchForm.name = ''
  searchForm.type = ''
  searchForm.status = ''
  searchForm.department = ''
  handleSearch()
}

const handleSizeChange = (val: number) => {
  pageSize.value = val
  fetchList()
}

const handleCurrentChange = (val: number) => {
  currentPage.value = val
  fetchList()
}

const handleAdd = () => {
  dialogType.value = 'add'
  Object.assign(form, {
    id: '',
    name: '',
    brand: '',
    config: '',
    department: '',
    user: '',
    type: '',
    subtype: '',
    ip: '',
    status: 'active',
    purchaseDate: '',
    snCode: '',
    purchasePrice: '',
    warrantyExpireDate: '',
    assetStatus: 'IN_USE',
    location: '',
    managerName: '',
    maintenanceContact: '',
    remark: ''
  })
  dialogVisible.value = true
}

const handleEdit = (row: any) => {
  dialogType.value = 'edit'
  Object.assign(form, row)
  dialogVisible.value = true
}

const handleDelete = (row: any) => {
  ElMessageBox.confirm(`确定要删除资产 ${row.name} 吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(async () => {
    try {
      await request.delete(`/api/v1/assets/${row.id}`)
      ElMessage.success('删除成功')
      fetchList()
    } catch (error) {
      console.error('Delete asset error:', error)
      ElMessage.error('删除资产失败：' + (error instanceof Error ? error.message : '未知错误'))
    }
  }).catch(() => {})
}

const submitForm = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        if (dialogType.value === 'add') {
          await request.post('/api/v1/assets', form)
          ElMessage.success('添加成功')
        } else {
          await request.put(`/api/v1/assets/${form.id}`, form)
          ElMessage.success('修改成功')
        }
        dialogVisible.value = false
        fetchList()
      } catch (error) {
        console.error('Submit asset error:', error)
        ElMessage.error('操作失败：' + (error instanceof Error ? error.message : '未知错误'))
      }
    }
  })
}

const handleReceive = (row: any) => {
  operateType.value = 'receive'
  currentAssetId.value = row.id
  Object.assign(operateForm, {
    targetDepartment: '',
    targetUser: '',
    operatorName: '',
    reason: ''
  })
  operateDialogVisible.value = true
}

const handleTransfer = (row: any) => {
  operateType.value = 'transfer'
  currentAssetId.value = row.id
  Object.assign(operateForm, {
    targetDepartment: '',
    targetUser: '',
    operatorName: '',
    reason: ''
  })
  operateDialogVisible.value = true
}

const handleReturn = (row: any) => {
  operateType.value = 'return'
  currentAssetId.value = row.id
  Object.assign(operateForm, {
    targetDepartment: '',
    targetUser: '',
    operatorName: '',
    reason: ''
  })
  operateDialogVisible.value = true
}

const handleRepair = (row: any) => {
  operateType.value = 'repair'
  currentAssetId.value = row.id
  Object.assign(operateForm, {
    targetDepartment: '',
    targetUser: '',
    operatorName: '',
    reason: ''
  })
  operateDialogVisible.value = true
}

const handleScrap = (row: any) => {
  operateType.value = 'scrap'
  currentAssetId.value = row.id
  Object.assign(operateForm, {
    targetDepartment: '',
    targetUser: '',
    operatorName: '',
    reason: ''
  })
  operateDialogVisible.value = true
}

const handleCreateWorkOrder = (row: any) => {
  currentAssetId.value = row.id
  Object.assign(workOrderForm, {
    description: '',
    urgencyLevel: 2,
    operatorName: ''
  })
  workOrderDialogVisible.value = true
}

const submitOperate = async () => {
  if (!operateFormRef.value) return
  await operateFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const userId = parseInt(localStorage.getItem('userId') || '1')
        let url = ''
        const data: any = {
          operatorId: userId,
          operatorName: operateForm.operatorName,
          reason: operateForm.reason
        }

        switch (operateType.value) {
          case 'receive':
            url = `/api/v1/assets/${currentAssetId.value}/receive`
            data.targetDepartment = operateForm.targetDepartment
            data.targetUser = operateForm.targetUser
            break
          case 'transfer':
            url = `/api/v1/assets/${currentAssetId.value}/transfer`
            data.targetDepartment = operateForm.targetDepartment
            data.targetUser = operateForm.targetUser
            break
          case 'return':
            url = `/api/v1/assets/${currentAssetId.value}/return`
            break
          case 'repair':
            url = `/api/v1/assets/${currentAssetId.value}/repair`
            break
          case 'scrap':
            url = `/api/v1/assets/${currentAssetId.value}/scrap`
            break
        }

        await request.post(url, data)
        ElMessage.success('操作成功')
        operateDialogVisible.value = false
        fetchList()
      } catch (error) {
        console.error('Submit operate error:', error)
        ElMessage.error('操作失败：' + (error instanceof Error ? error.message : '未知错误'))
      }
    }
  })
}

const submitWorkOrder = async () => {
  if (!workOrderFormRef.value) return
  await workOrderFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const userId = parseInt(localStorage.getItem('userId') || '1')
        const res = await request.post(`/api/v1/assets/${currentAssetId.value}/create-workorder`, {
          operatorId: userId,
          description: workOrderForm.description,
          urgencyLevel: workOrderForm.urgencyLevel
        })
        ElMessage.success('工单创建成功，工单ID：' + res.data?.workOrderId)
        workOrderDialogVisible.value = false
        fetchList()
      } catch (error) {
        console.error('Submit work order error:', error)
        ElMessage.error('工单创建失败：' + (error instanceof Error ? error.message : '未知错误'))
      }
    }
  })
}

onMounted(() => {
  fetchList()
})
</script>

<style scoped>
.asset-container {
  padding: 20px;
}

.header-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  flex-wrap: wrap;
  gap: 10px;
}

.search-form {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.action-buttons {
  display: flex;
  gap: 10px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.text-warning {
  color: #f56c6c;
  font-weight: 500;
}

.scan-input-area {
  margin-bottom: 20px;
}

.scan-result {
  margin-top: 20px;
}

.dialog-footer {
  text-align: right;
}
</style>