<template>
  <div class="sys-dict-container">
    <el-row :gutter="20">
      <!-- 字典类型列表 -->
      <el-col :span="10">
        <el-card shadow="never">
          <template #header>
            <div class="card-header">
              <span>字典类型</span>
              <el-button type="primary" size="small" @click="handleAddType">新增</el-button>
            </div>
          </template>
          <el-table :data="typeList" highlight-current-row @current-change="handleTypeChange" border>
            <el-table-column prop="dictName" label="字典名称" />
            <el-table-column prop="dictType" label="字典类型" />
            <el-table-column label="操作" width="120">
              <template #default="{ row }">
                <el-button link type="primary" size="small" @click.stop="handleEditType(row)">编辑</el-button>
                <el-button link type="danger" size="small" @click.stop="handleDeleteType(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
      <!-- 字典数据列表 -->
      <el-col :span="14">
        <el-card shadow="never">
          <template #header>
            <div class="card-header">
              <span>字典数据</span>
              <el-button type="primary" size="small" :disabled="!currentType" @click="handleAddData">新增</el-button>
            </div>
          </template>
          <el-table :data="dataList" border>
            <el-table-column prop="dictLabel" label="数据标签" />
            <el-table-column prop="dictValue" label="数据键值" />
            <el-table-column prop="dictSort" label="排序" width="80" />
            <el-table-column prop="status" label="状态" width="80">
              <template #default="{ row }">
                <el-tag :type="row.status === 1 ? 'success' : 'danger'">{{ row.status === 1 ? '正常' : '停用' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="120">
              <template #default="{ row }">
                <el-button link type="primary" size="small" @click="handleEditData(row)">编辑</el-button>
                <el-button link type="danger" size="small" @click="handleDeleteData(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <!-- 字典类型弹窗 -->
    <el-dialog :title="typeDialogTitle" v-model="typeDialogVisible" width="500px">
      <el-form :model="typeForm" label-width="80px">
        <el-form-item label="字典名称" required>
          <el-input v-model="typeForm.dictName" placeholder="请输入字典名称" />
        </el-form-item>
        <el-form-item label="字典类型" required>
          <el-input v-model="typeForm.dictType" placeholder="请输入字典类型" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="typeForm.remark" type="textarea" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="typeDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitType">确定</el-button>
      </template>
    </el-dialog>

    <!-- 字典数据弹窗 -->
    <el-dialog :title="dataDialogTitle" v-model="dataDialogVisible" width="500px">
      <el-form :model="dataForm" label-width="80px">
        <el-form-item label="数据标签" required>
          <el-input v-model="dataForm.dictLabel" placeholder="请输入数据标签" />
        </el-form-item>
        <el-form-item label="数据键值" required>
          <el-input v-model="dataForm.dictValue" placeholder="请输入数据键值" />
        </el-form-item>
        <el-form-item label="显示排序" required>
          <el-input-number v-model="dataForm.dictSort" :min="0" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="dataForm.status">
            <el-radio :label="1">正常</el-radio>
            <el-radio :label="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dataDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitData">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const typeList = ref<any[]>([])
const dataList = ref<any[]>([])
const currentType = ref<any>(null)

const fetchTypes = async () => {
  try {
    const res: any = await request.get('/api/v1/sys-dict/type/list')
    typeList.value = res.data || res.list || []
  } catch (error) {
    console.error(error)
  }
}

const fetchData = async (dictType: string) => {
  try {
    const res: any = await request.get(`/api/v1/sys-dict/data/list?dictType=${dictType}`)
    dataList.value = res.data || res.list || []
  } catch (error) {
    console.error(error)
  }
}

const handleTypeChange = (row: any) => {
  currentType.value = row
  if (row) {
    fetchData(row.dictType)
  } else {
    dataList.value = []
  }
}

onMounted(() => {
  fetchTypes()
})

// Type Dialog
const typeDialogVisible = ref(false)
const typeDialogTitle = ref('新增字典类型')
const typeForm = ref({ id: '', dictName: '', dictType: '', remark: '' })

const handleAddType = () => {
  typeDialogTitle.value = '新增字典类型'
  typeForm.value = { id: '', dictName: '', dictType: '', remark: '' }
  typeDialogVisible.value = true
}

const handleEditType = (row: any) => {
  typeDialogTitle.value = '编辑字典类型'
  typeForm.value = { ...row }
  typeDialogVisible.value = true
}

const handleDeleteType = (row: any) => {
  ElMessageBox.confirm('确认删除该字典类型?', '提示', { type: 'warning' }).then(async () => {
    await request.delete(`/api/v1/sys-dict/type/${row.id}`)
    ElMessage.success('删除成功')
    fetchTypes()
  }).catch(() => {})
}

const submitType = async () => {
  if (typeForm.value.id) {
    await request.put('/api/v1/sys-dict/type', typeForm.value)
  } else {
    await request.post('/api/v1/sys-dict/type', typeForm.value)
  }
  ElMessage.success('保存成功')
  typeDialogVisible.value = false
  fetchTypes()
}

// Data Dialog
const dataDialogVisible = ref(false)
const dataDialogTitle = ref('新增字典数据')
const dataForm = ref({ id: '', dictType: '', dictLabel: '', dictValue: '', dictSort: 0, status: 1 })

const handleAddData = () => {
  if (!currentType.value) return
  dataDialogTitle.value = '新增字典数据'
  dataForm.value = { id: '', dictType: currentType.value.dictType, dictLabel: '', dictValue: '', dictSort: 0, status: 1 }
  dataDialogVisible.value = true
}

const handleEditData = (row: any) => {
  dataDialogTitle.value = '编辑字典数据'
  dataForm.value = { ...row }
  dataDialogVisible.value = true
}

const handleDeleteData = (row: any) => {
  ElMessageBox.confirm('确认删除该字典数据?', '提示', { type: 'warning' }).then(async () => {
    await request.delete(`/api/v1/sys-dict/data/${row.id}`)
    ElMessage.success('删除成功')
    fetchData(currentType.value.dictType)
  }).catch(() => {})
}

const submitData = async () => {
  if (dataForm.value.id) {
    await request.put('/api/v1/sys-dict/data', dataForm.value)
  } else {
    await request.post('/api/v1/sys-dict/data', dataForm.value)
  }
  ElMessage.success('保存成功')
  dataDialogVisible.value = false
  fetchData(currentType.value.dictType)
}
</script>

<style scoped>
.sys-dict-container {
  padding: 20px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
