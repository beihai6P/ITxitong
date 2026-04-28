<template>
  <div class="role-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>角色管理</span>
          <el-button type="primary" @click="handleAdd">新增角色</el-button>
        </div>
      </template>

      <div class="search-bar">
        <el-input v-model="searchForm.roleName" placeholder="角色名称" style="width: 200px; margin-right: 10px" clearable @clear="fetchList" />
        <el-input v-model="searchForm.roleCode" placeholder="角色编码" style="width: 200px; margin-right: 10px" clearable @clear="fetchList" />
        <el-select v-model="searchForm.status" placeholder="状态" style="width: 120px; margin-right: 10px" clearable @clear="fetchList">
          <el-option label="启用" :value="1" />
          <el-option label="禁用" :value="0" />
        </el-select>
        <el-button type="primary" @click="fetchList">搜索</el-button>
        <el-button @click="resetSearch">重置</el-button>
      </div>

      <el-table :data="tableData" style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="roleName" label="角色名称" width="150" />
        <el-table-column prop="roleCode" label="角色编码" width="150" />
        <el-table-column prop="dataScope" label="数据范围" width="180">
          <template #default="{ row }">
            <el-tag :type="row.dataScope === 'ALL' ? 'success' : row.dataScope === 'DEPT_AND_CHILD' ? 'primary' : row.dataScope === 'DEPT' ? 'warning' : row.dataScope === 'CUSTOM' ? 'danger' : 'info'">
              {{ row.dataScope === 'ALL' ? '全部数据' : row.dataScope === 'DEPT_AND_CHILD' ? '本部门及下级' : row.dataScope === 'DEPT' ? '本部门' : row.dataScope === 'CUSTOM' ? '自定义' : '本人' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-switch v-model="row.status" :active-value="1" :inactive-value="0" @change="handleStatusChange(row)" :disabled="row.id === 1" />
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" />
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" link @click="handleEditMenu(row)">分配权限</el-button>
            <el-button size="small" type="primary" link @click="handleEdit(row)" :disabled="row.id === 1">编辑</el-button>
            <el-button size="small" type="danger" link @click="handleDelete(row)" :disabled="row.id === 1">删除</el-button>
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

    <el-dialog v-model="dialogVisible" :title="dialogType === 'add' ? '新增角色' : '编辑角色'" width="600px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="角色名称" prop="roleName">
          <el-input v-model="form.roleName" placeholder="请输入角色名称" />
        </el-form-item>
        <el-form-item label="角色编码" prop="roleCode" :disabled="dialogType === 'edit'">
          <el-input v-model="form.roleCode" placeholder="请输入角色编码" :disabled="dialogType === 'edit'" />
        </el-form-item>
        <el-form-item label="数据范围" prop="dataScope">
          <el-select v-model="form.dataScope" placeholder="请选择数据范围">
            <el-option label="全部数据" value="ALL" />
            <el-option label="本部门及下级数据" value="DEPT_AND_CHILD" />
            <el-option label="本部门数据" value="DEPT" />
            <el-option label="本人数据" value="SELF" />
            <el-option label="自定义数据" value="CUSTOM" />
          </el-select>
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" placeholder="请输入描述" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <el-dialog v-model="menuDialogVisible" title="分配菜单权限" width="600px">
      <el-form label-width="80px">
        <el-form-item label="角色">
          <span>{{ currentRole?.roleName }}</span>
        </el-form-item>
        <el-form-item label="菜单权限">
          <el-tree
            :data="menuTree"
            show-checkbox
            node-key="id"
            ref="menuTreeRef"
            :default-checked-keys="defaultCheckedKeys"
            :props="{
              children: 'children',
              label: 'menuName',
              disabled: 'disabled'
            }"
            @check-change="handleCheckChange"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="menuDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSaveMenus">保存</el-button>
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

interface Role {
  id?: number
  roleName: string
  roleCode: string
  dataScope: string
  status: number
  description?: string
  menuIds?: number[]
}

interface Menu {
  id: number
  menuName: string
  parentId: number
  menuType: string
  path: string
  component: string
  icon: string
  sort: number
  status: number
  children?: Menu[]
  disabled?: boolean
  checked?: boolean
}

const loading = ref(false)
const tableData = ref<Role[]>([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const searchForm = reactive({
  roleName: '',
  roleCode: '',
  status: undefined
})

const fetchList = async () => {
  loading.value = true
  try {
    const res: any = await request.get('/role/list', {
      params: {
        current: currentPage.value,
        size: pageSize.value,
        roleName: searchForm.roleName || undefined,
        roleCode: searchForm.roleCode || undefined,
        status: searchForm.status
      }
    })
    console.log('Role list response:', res)
    tableData.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch (error) {
    console.error('Fetch roles error:', error)
    ElMessage.error('获取角色列表失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchList()
})

const resetSearch = () => {
  searchForm.roleName = ''
  searchForm.roleCode = ''
  searchForm.status = undefined
  fetchList()
}

const dialogVisible = ref(false)
const dialogType = ref<'add' | 'edit'>('add')
const formRef = ref<FormInstance>()
const form = reactive<Role>({
  roleName: '',
  roleCode: '',
  dataScope: 'SELF',
  status: 1,
  description: ''
})

const rules = reactive<FormRules>({
  roleName: [{ required: true, message: '请输入角色名称', trigger: 'blur' }],
  roleCode: [{ required: true, message: '请输入角色编码', trigger: 'blur' }]
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
  Object.assign(form, { roleName: '', roleCode: '', dataScope: 'SELF', status: 1, description: '' })
  dialogVisible.value = true
}

const handleEdit = (row: any) => {
  dialogType.value = 'edit'
  Object.assign(form, row)
  dialogVisible.value = true
}

const handleDelete = (row: any) => {
  ElMessageBox.confirm('确定要删除该角色吗?', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(async () => {
    try {
      await request.delete(`/role/${row.id}`)
      ElMessage.success('删除成功')
      fetchList()
    } catch (error) {
      console.error('Delete role error:', error)
      ElMessage.error('删除角色失败')
    }
  }).catch(() => {})
}

const handleStatusChange = async (row: any) => {
  try {
    await request.put(`/role/${row.id}/status`, { status: row.status })
    ElMessage.success('状态更新成功')
  } catch (error) {
    console.error('Update status error:', error)
    ElMessage.error('状态更新失败')
    // 恢复原状态
    row.status = row.status === 1 ? 0 : 1
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        if (dialogType.value === 'add') {
          await request.post('/role', form)
          ElMessage.success('新增成功')
          // 刷新列表后自动打开权限分配弹窗
          await fetchList()
          const newRole = tableData.value.find(r => r.roleCode === form.roleCode)
          if (newRole) {
            handleEditMenu(newRole)
          }
        } else {
          await request.put('/role', form)
          ElMessage.success('编辑成功')
        }
        dialogVisible.value = false
      } catch (error) {
        console.error('Submit role error:', error)
        ElMessage.error('操作失败')
      }
    }
  })
}

const menuDialogVisible = ref(false)
const menuTree = ref<Menu[]>([])
const menuTreeRef = ref<any>(null)
const defaultCheckedKeys = ref<number[]>([])
const currentRole = ref<Role | null>(null)

const handleEditMenu = async (row: Role) => {
  currentRole.value = row
  try {
    // 获取菜单树
    const menuRes: any = await request.get('/menu/tree')
    menuTree.value = menuRes.data || []

    // 获取角色已分配的菜单
    const roleMenuRes: any = await request.get(`/role/${row.id}/menus`)
    defaultCheckedKeys.value = roleMenuRes.data?.map((menu: Menu) => menu.id) || []

    menuDialogVisible.value = true
  } catch (error) {
    console.error('Failed to load menu data:', error)
    ElMessage.error('加载菜单数据失败')
  }
}

const handleCheckChange = () => {
  // 可以在这里处理勾选逻辑
}

const handleSaveMenus = async () => {
  if (!currentRole.value?.id) return

  try {
    const checkedKeys = menuTreeRef.value.getCheckedKeys()
    await request.put(`/role/${currentRole.value.id}/menus`, checkedKeys)
    ElMessage.success('权限分配成功')
    menuDialogVisible.value = false
  } catch (error) {
    console.error('Save menu permissions error:', error)
    ElMessage.error('保存失败')
  }
}
</script>

<style scoped>
.role-container {
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
