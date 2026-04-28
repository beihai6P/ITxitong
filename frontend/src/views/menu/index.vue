<template>
  <div class="menu-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>菜单管理</span>
          <el-button type="primary" @click="handleAdd">新增菜单</el-button>
        </div>
      </template>
      <el-table
        :data="tableData"
        style="width: 100%"
        row-key="id"
        border
        default-expand-all
        :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
        v-loading="loading"
      >
        <el-table-column prop="name" label="菜单名称" width="200" />
        <el-table-column prop="icon" label="图标" width="80">
          <template #default="{ row }">
            <el-icon v-if="row.icon"><component :is="row.icon" /></el-icon>
          </template>
        </el-table-column>
        <el-table-column prop="path" label="路由路径" width="180" />
        <el-table-column prop="component" label="组件路径" width="250" />
        <el-table-column prop="sort" label="排序" width="80" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '启用' : '禁用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogType === 'add' ? '新增菜单' : '编辑菜单'" width="600px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="上级菜单" prop="parentId">
          <el-tree-select
            v-model="form.parentId"
            :data="menuOptions"
            check-strictly
            placeholder="请选择上级菜单"
            value-key="id"
          />
        </el-form-item>
        <el-form-item label="菜单名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入菜单名称" />
        </el-form-item>
        <el-form-item label="图标" prop="icon">
          <el-input v-model="form.icon" placeholder="请输入图标名" />
        </el-form-item>
        <el-form-item label="路由路径" prop="path">
          <el-input v-model="form.path" placeholder="请输入路由路径" />
        </el-form-item>
        <el-form-item label="组件路径" prop="component">
          <el-input v-model="form.component" placeholder="请输入组件路径" />
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="form.sort" :min="1" />
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
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import request from '@/utils/request'

const loading = ref(false)
const tableData = ref<any[]>([])
const menuOptions = ref<any[]>([])

const fetchList = async () => {
  loading.value = true
  try {
    const res = await request.get('/api/v1/menus')
    tableData.value = res.data?.list || res.list || res.data || []
    updateMenuOptions()
  } catch (error) {
    console.error('Fetch menus error:', error)
    ElMessage.error('获取菜单列表失败：' + (error instanceof Error ? error.message : '未知错误'))
  } finally {
    loading.value = false
  }
}

const updateMenuOptions = () => {
  const options = tableData.value.map(item => ({
    id: item.id,
    label: item.name,
    children: item.children?.map((child: any) => ({
      id: child.id,
      label: child.name
    }))
  }))
  menuOptions.value = [{ id: 0, label: '顶级菜单', children: options }]
}

onMounted(() => {
  fetchList()
})

const dialogVisible = ref(false)
const dialogType = ref<'add' | 'edit'>('add')
const formRef = ref<FormInstance>()
const form = reactive({
  id: undefined as number | undefined,
  parentId: 0,
  name: '',
  icon: '',
  path: '',
  component: '',
  sort: 1,
  status: 1
})

const rules = reactive<FormRules>({
  name: [{ required: true, message: '请输入菜单名称', trigger: 'blur' }],
  path: [{ required: true, message: '请输入路由路径', trigger: 'blur' }]
})

const handleAdd = () => {
  dialogType.value = 'add'
  Object.assign(form, { id: undefined, parentId: 0, name: '', icon: '', path: '', component: '', sort: 1, status: 1 })
  dialogVisible.value = true
}

const handleEdit = (row: any) => {
  dialogType.value = 'edit'
  Object.assign(form, row)
  dialogVisible.value = true
}

const handleDelete = (row: any) => {
  ElMessageBox.confirm('确定要删除该菜单吗?', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(async () => {
    try {
      await request.delete(`/api/v1/menus/${row.id}`)
      ElMessage.success('删除成功')
      fetchList()
    } catch (error) {
      console.error('Delete menu error:', error)
      ElMessage.error('删除菜单失败：' + (error instanceof Error ? error.message : '未知错误'))
    }
  }).catch(() => {})
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        if (dialogType.value === 'add') {
          await request.post('/api/v1/menus', form)
          ElMessage.success('新增成功')
        } else {
          await request.put(`/api/v1/menus/${form.id}`, form)
          ElMessage.success('编辑成功')
        }
        dialogVisible.value = false
        fetchList()
      } catch (error) {
        console.error('Submit menu error:', error)
        ElMessage.error('操作失败：' + (error instanceof Error ? error.message : '未知错误'))
      }
    }
  })
}
</script>

<style scoped>
.menu-container {
  padding: 0;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
