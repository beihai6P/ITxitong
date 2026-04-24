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
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch" :icon="Search">查询</el-button>
            <el-button @click="resetSearch" :icon="Refresh">重置</el-button>
          </el-form-item>
        </el-form>
        <div>
          <el-button type="warning" :disabled="selectedIds.length === 0" @click="handleBatchAssign">批量分配</el-button>
          <el-button type="danger" :disabled="selectedIds.length === 0" @click="handleBatchClose">批量关闭</el-button>
          <el-button type="primary" :icon="Camera" @click="handleScan">扫码解析</el-button>
          <el-button type="success" :icon="Plus" @click="handleAdd">新增资产</el-button>
        </div>
      </div>

      <el-table :data="tableData" v-loading="loading" style="width: 100%" border @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" />
        <el-table-column prop="id" label="资产编号" width="120" />
        <el-table-column prop="name" label="资产名称" />
        <el-table-column prop="type" label="类型" width="120">
          <template #default="{ row }">
            <el-tag :type="getTypeTag(row.type)">{{ getTypeName(row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="ip" label="IP地址" width="140" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'active' ? 'success' : 'danger'">
              {{ row.status === 'active' ? '正常' : '故障' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="purchaseDate" label="购入日期" width="120" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="danger" size="small" @click="handleDelete(row)">删除</el-button>
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

    <el-dialog :title="dialogType === 'add' ? '新增资产' : '编辑资产'" v-model="dialogVisible" width="500px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="资产名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入资产名称" />
        </el-form-item>
        <el-form-item label="类型" prop="type">
          <el-select v-model="form.type" placeholder="请选择资产类型" style="width: 100%">
            <el-option label="服务器" value="server" />
            <el-option label="网络设备" value="network" />
            <el-option label="终端设备" value="terminal" />
          </el-select>
        </el-form-item>
        <el-form-item label="IP地址" prop="ip">
          <el-input v-model="form.ip" placeholder="请输入IP地址" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio label="active">正常</el-radio>
            <el-radio label="error">故障</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="购入日期" prop="purchaseDate">
          <el-date-picker v-model="form.purchaseDate" type="date" placeholder="选择日期" value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
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
        <el-input v-model="scanCode" placeholder="请输入资产编码（模拟扫码）" style="width: 300px; margin-right: 10px;" />
        <el-button type="primary" @click="submitScan">模拟扫码</el-button>
      </div>

      <div class="scan-result" v-if="scanResult">
        <el-divider>资产详情</el-divider>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="资产编码">{{ scanResult.asset?.assetCode }}</el-descriptions-item>
          <el-descriptions-item label="资产名称">{{ scanResult.asset?.assetName }}</el-descriptions-item>
          <el-descriptions-item label="已用年限">{{ scanResult.asset?.age }} 年</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="scanResult.asset?.isDeleted === 0 ? 'success' : 'danger'">
              {{ scanResult.asset?.isDeleted === 0 ? '正常' : '已删' }}
            </el-tag>
          </el-descriptions-item>
        </el-descriptions>

        <el-divider>近期历史记录</el-divider>
        <el-timeline v-if="scanResult.recentHistory && scanResult.recentHistory.length">
          <el-timeline-item
            v-for="(log, index) in scanResult.recentHistory"
            :key="index"
            :timestamp="Array.isArray(log.createTime) ? log.createTime.join('-').replace(/-(\d+)-(\d+)-(\d+)-(\d+)-(\d+)/, '-$1-$2 $3:$4:$5') : log.createTime"
          >
            {{ log.action }} - {{ log.remark }}
          </el-timeline-item>
        </el-timeline>
        <div v-else style="text-align: center; color: #999;">暂无历史记录</div>
      </div>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="scanDialogVisible = false">关闭</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { Search, Refresh, Plus, Camera } from '@element-plus/icons-vue'
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
  type: ''
})

const scanDialogVisible = ref(false)
const scanCode = ref('')
const scanResult = ref<any>(null)

const selectedIds = ref<number[]>([])

const handleSelectionChange = (selection: any[]) => {
  selectedIds.value = selection.map(item => item.id)
}

const handleBatchAssign = () => {
  ElMessageBox.confirm(`确定要批量分配 ${selectedIds.value.length} 个资产吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await request.post('/api/v1/assets/batch-assign', { ids: selectedIds.value })
      ElMessage.success('批量分配成功')
      fetchList()
    } catch (e) {
      console.error(e)
    }
  }).catch(() => {})
}

const handleBatchClose = () => {
  ElMessageBox.confirm(`确定要批量关闭(报废) ${selectedIds.value.length} 个资产吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await request.post('/api/v1/assets/batch-close', { ids: selectedIds.value })
      ElMessage.success('批量关闭成功')
      fetchList()
    } catch (e) {
      console.error(e)
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
    ElMessage.warning('请输入资产编码')
    return
  }
  try {
    const res: any = await request.get('/api/asset/scan/resolve', {
      params: {
        assetCode: scanCode.value,
        operatorId: 1
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
  type: '',
  ip: '',
  status: 'active',
  purchaseDate: ''
})

const rules = reactive<FormRules>({
  name: [{ required: true, message: '请输入资产名称', trigger: 'blur' }],
  type: [{ required: true, message: '请选择资产类型', trigger: 'change' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
})

const getTypeName = (type: string) => {
  const map: Record<string, string> = {
    server: '服务器',
    network: '网络设备',
    terminal: '终端设备'
  }
  return map[type] || '未知'
}

const getTypeTag = (type: string) => {
  const map: Record<string, string> = {
    server: '',
    network: 'warning',
    terminal: 'info'
  }
  return map[type] || 'info'
}

const fetchList = async () => {
  loading.value = true
  try {
    const res = await request.get('/api/v1/assets', {
      params: {
        page: currentPage.value,
        pageSize: pageSize.value,
        name: searchForm.name,
        type: searchForm.type
      }
    })
    tableData.value = res.data?.list || res.list || res.data || []
    total.value = res.data?.total || res.total || 0
  } catch (error) {
    console.error('Fetch assets error:', error)
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
    type: '',
    ip: '',
    status: 'active',
    purchaseDate: ''
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
  margin-bottom: 20px;
}

.search-form {
  display: flex;
  flex-wrap: wrap;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
