<template>
  <div class="global-search">
    <el-input
      v-model="keyword"
      placeholder="全局搜索..."
      prefix-icon="Search"
      clearable
      @keyup.enter="handleSearch"
      class="search-input"
    >
      <template #append>
        <el-button icon="Search" @click="handleSearch" />
      </template>
    </el-input>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'

const keyword = ref('')

const handleSearch = () => {
  if (!keyword.value.trim()) {
    ElMessage.warning('请输入搜索关键字')
    return
  }
  // Implement your global search routing or logic here
  ElMessage.info(`搜索: ${keyword.value}`)
  // router.push({ path: '/search', query: { q: keyword.value } })
}
</script>

<style scoped>
.global-search {
  margin-right: 20px;
  width: 250px;
}
.search-input {
  border-radius: 20px;
}
</style>
