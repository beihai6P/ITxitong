<template>
  <div class="log-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>日志管理</span>
          <div class="header-actions">
            <el-input v-model="searchQuery" placeholder="搜索操作人员" style="width: 200px; margin-right: 10px;" />
            <el-button type="primary" @click="handleSearch">搜索</el-button>
            <el-button @click="handleReset">重置</el-button>
          </div>
        </div>
      </template>
      <el-table :data="tableData" style="width: 100%" v-loading="loading">
        <el-table-column type="index" label="序号" width="60" />
        <el-table-column prop="module" label="操作模块" width="150" />
        <el-table-column prop="type" label="操作类型" width="120">
          <template #default="{ row }">
            <el-tag :type="row.type === 'INSERT' ? 'success' : row.type === 'UPDATE' ? 'warning' : row.type === 'DELETE' ? 'danger' : 'info'">
              {{ row.type }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="操作描述" />
        <el-table-column prop="operator" label="操作人员" width="120" />
        <el-table-column prop="ip" label="IP地址" width="150" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="操作时间" width="180" />
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
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const loading = ref(false)
const searchQuery = ref('')
const tableData = ref([
  { module: '用户管理', type: 'INSERT', description: '新增用户: test_user', operator: 'admin', ip: '192.168.1.100', status: 1, createTime: '2023-10-01 10:20:30' },
  { module: '角色管理', type: 'UPDATE', description: '修改角色权限', operator: 'admin', ip: '192.168.1.100', status: 1, createTime: '2023-10-01 11:20:30' },
  { module: '菜单管理', type: 'DELETE', description: '删除菜单: 测试菜单', operator: 'admin', ip: '192.168.1.100', status: 0, createTime: '2023-10-02 09:10:00' },
])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(3)

const handleSearch = () => {
  // 模拟搜索
  loading.value = true
  setTimeout(() => {
    loading.value = false
  }, 500)
}

const handleReset = () => {
  searchQuery.value = ''
  handleSearch()
}

const handleSizeChange = (val: number) => {
  pageSize.value = val
}

const handleCurrentChange = (val: number) => {
  currentPage.value = val
}
</script>

<style scoped>
.log-container {
  padding: 0;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.header-actions {
  display: flex;
  align-items: center;
}
.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
