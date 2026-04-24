<template>
  <div class="sys-config-container">
    <el-card shadow="never">
      <div class="header-actions">
        <el-form :inline="true" :model="searchForm" class="search-form">
          <el-form-item label="参数名称">
            <el-input v-model="searchForm.configName" placeholder="请输入参数名称" clearable />
          </el-form-item>
          <el-form-item label="参数键名">
            <el-input v-model="searchForm.configKey" placeholder="请输入参数键名" clearable />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">查询</el-button>
            <el-button @click="resetSearch">重置</el-button>
          </el-form-item>
        </el-form>
        <el-button type="success" @click="handleAdd">新增配置</el-button>
      </div>

      <el-table :data="tableData" v-loading="loading" border style="width: 100%">
        <el-table-column prop="configName" label="参数名称" />
        <el-table-column prop="configKey" label="参数键名" />
        <el-table-column prop="configValue" label="参数键值" />
        <el-table-column prop="remark" label="备注" show-overflow-tooltip />
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

    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="500px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="参数名称" required>
          <el-input v-model="form.configName" placeholder="请输入参数名称" />
        </el-form-item>
        <el-form-item label="参数键名" required>
          <el-input v-model="form.configKey" placeholder="请输入参数键名" />
        </el-form-item>
        <el-form-item label="参数键值" required>
          <el-input v-model="form.configValue" placeholder="请输入参数键值" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitForm">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const loading = ref(false)
const tableData = ref<any[]>([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const searchForm = reactive({
  configName: '',
  configKey: ''
})

const dialogVisible = ref(false)
const dialogTitle = ref('新增参数')
const form = ref({ id: '', configName: '', configKey: '', configValue: '', remark: '' })

const fetchList = async () => {
  loading.value = true
  try {
    const res: any = await request.get('/api/v1/sys-config/list', {
      params: {
        page: currentPage.value,
        pageSize: pageSize.value,
        configName: searchForm.configName,
        configKey: searchForm.configKey
      }
    })
    tableData.value = res.data?.list || res.list || []
    total.value = res.data?.total || res.total || 0
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  currentPage.value = 1
  fetchList()
}

const resetSearch = () => {
  searchForm.configName = ''
  searchForm.configKey = ''
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
  dialogTitle.value = '新增参数配置'
  form.value = { id: '', configName: '', configKey: '', configValue: '', remark: '' }
  dialogVisible.value = true
}

const handleEdit = (row: any) => {
  dialogTitle.value = '编辑参数配置'
  form.value = { ...row }
  dialogVisible.value = true
}

const handleDelete = (row: any) => {
  ElMessageBox.confirm(`确认删除参数 "${row.configName}"?`, '提示', { type: 'warning' }).then(async () => {
    await request.delete(`/api/v1/sys-config/${row.id}`)
    ElMessage.success('删除成功')
    fetchList()
  }).catch(() => {})
}

const submitForm = async () => {
  if (form.value.id) {
    await request.put('/api/v1/sys-config', form.value)
  } else {
    await request.post('/api/v1/sys-config', form.value)
  }
  ElMessage.success('保存成功')
  dialogVisible.value = false
  fetchList()
}

onMounted(() => {
  fetchList()
})
</script>

<style scoped>
.sys-config-container {
  padding: 20px;
}
.header-actions {
  display: flex;
  justify-content: space-between;
  margin-bottom: 20px;
}
.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
