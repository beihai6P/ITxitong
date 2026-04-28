<template>
  <div class="user-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>用户管理</span>
          <el-button type="primary" @click="handleAdd">新增用户</el-button>
        </div>
      </template>

      <div class="search-bar">
        <el-input v-model="searchForm.username" placeholder="用户名" style="width: 200px; margin-right: 10px" clearable @clear="fetchList" />
        <el-input v-model="searchForm.realName" placeholder="真实姓名" style="width: 200px; margin-right: 10px" clearable @clear="fetchList" />
        <el-button type="primary" @click="fetchList">搜索</el-button>
        <el-button @click="resetSearch">重置</el-button>
      </div>

      <el-table :data="tableData" style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" width="150" />
        <el-table-column prop="realName" label="真实姓名" width="120" />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column prop="email" label="邮箱" width="180" />
        <el-table-column prop="roleNames" label="角色" width="200">
          <template #default="{ row }">
            <el-tag v-for="role in row.roleNames" :key="role" size="small" style="margin-right: 5px">
              {{ role }}
            </el-tag>
            <span v-if="!row.roleNames || row.roleNames.length === 0" style="color: #999">未分配</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'"> {{ row.status === 1 ? '启用' : '禁用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" link @click="handleAssignRoles(row)">分配角色</el-button>
            <el-button size="small" type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button size="small" type="danger" link @click="handleDelete(row)">删除</el-button>
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

    <el-dialog v-model="dialogVisible" :title="dialogType === 'add' ? '新增用户' : '编辑用户'" width="600px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" :disabled="dialogType === 'edit'" />
        </el-form-item>
        <el-form-item label="密码" prop="password" v-if="dialogType === 'add'">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="form.realName" placeholder="请输入真实姓名" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item label="用户角色" prop="roleIds">
          <el-checkbox-group v-model="form.roleIds">
            <el-checkbox v-for="role in allRoles" :key="role.id" :label="role.id">
              {{ role.roleName }}
            </el-checkbox>
          </el-checkbox-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <el-dialog v-model="roleDialogVisible" title="分配角色" width="500px">
      <el-form label-width="80px">
        <el-form-item label="用户">
          <span>{{ currentUser?.username }}</span>
        </el-form-item>
        <el-form-item label="角色">
          <el-checkbox-group v-model="selectedRoleIds">
            <el-checkbox v-for="role in allRoles" :key="role.id" :label="role.id">
              {{ role.roleName }}
            </el-checkbox>
          </el-checkbox-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="roleDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSaveRoles">保存</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import request from '@/utils/request'

interface User {
  id?: number
  username: string
  password?: string
  realName?: string
  phone?: string
  email?: string
  status?: number
  roleIds?: number[]
  roleNames?: string[]
}

interface Role {
  id: number
  roleCode: string
  roleName: string
}

const loading = ref(false)
const tableData = ref<User[]>([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const searchForm = reactive({
  username: '',
  realName: ''
})

const allRoles = ref<Role[]>([])

const fetchList = async () => {
  loading.value = true
  try {
    const res: any = await request.get('/api/v1/users', {
      params: {
        current: currentPage.value,
        size: pageSize.value,
        username: searchForm.username || undefined,
        realName: searchForm.realName || undefined
      }
    })
    console.log('User list response:', res)
    tableData.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch (error) {
    console.error('Fetch users error:', error)
    ElMessage.error('获取用户列表失败')
  } finally {
    loading.value = false
  }
}

const fetchRoles = async () => {
  try {
    const res: any = await request.get('/role/all')
    allRoles.value = res.data || []
    console.log('Loaded roles:', allRoles.value)
  } catch (error) {
    console.error('Failed to load roles:', error)
    ElMessage.error('加载角色失败')
  }
}

onMounted(() => {
  fetchList()
  fetchRoles()
})

const resetSearch = () => {
  searchForm.username = ''
  searchForm.realName = ''
  fetchList()
}

const dialogVisible = ref(false)
const dialogType = ref<'add' | 'edit'>('add')
const formRef = ref<FormInstance>()
const form = reactive<User>({
  username: '',
  password: '',
  realName: '',
  phone: '',
  email: '',
  status: 1,
  roleIds: []
})

const rules = reactive<FormRules>({
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
})

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
  Object.assign(form, { username: '', password: '', realName: '', phone: '', email: '', status: 1, roleIds: [] })
  dialogVisible.value = true
}

const handleEdit = (row: any) => {
  dialogType.value = 'edit'
  Object.assign(form, row)
  dialogVisible.value = true
}

const handleDelete = (row: any) => {
  ElMessageBox.confirm('确定要删除该用户吗?', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(async () => {
    try {
      await request.delete(`/api/v1/users/${row.id}`)
      ElMessage.success('删除成功')
      fetchList()
    } catch (error) {
      console.error('Delete user error:', error)
      ElMessage.error('删除用户失败')
    }
  }).catch(() => {})
}

const handleSubmit = async () => {
  if (!formRef.value) return
  formRef.value.validate((valid) => {
    if (valid) {
      request.post('/api/v1/users', form).then(() => {
        ElMessage.success('新增成功')
        dialogVisible.value = false
        fetchList()
      }).catch(error => {
        console.error('Submit user error:', error)
        ElMessage.error('操作失败')
      })
    }
  })
}

const roleDialogVisible = ref(false)
const selectedRoleIds = ref<number[]>([])
const currentUser = ref<User | null>(null)

const handleAssignRoles = async (row: User) => {
  currentUser.value = row
  try {
    if (allRoles.value.length === 0) {
      await fetchRoles()
    }
    selectedRoleIds.value = row.roleIds || []
    roleDialogVisible.value = true
  } catch (error) {
    console.error('Failed to load roles:', error)
    ElMessage.error('加载角色数据失败')
  }
}

const handleSaveRoles = async () => {
  if (!currentUser.value?.id) return

  try {
    await request.put(`/api/v1/users/${currentUser.value.id}/roles`, selectedRoleIds.value)
    ElMessage.success('角色分配成功')
    roleDialogVisible.value = false
    fetchList()
  } catch (error) {
    console.error('Save roles error:', error)
    ElMessage.error('保存失败')
  }
}
</script>

<style scoped>
.user-container {
  padding: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-bar {
  margin-bottom: 20px;
  display: flex;
  align-items: center;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
