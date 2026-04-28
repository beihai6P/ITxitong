<template>
  <div 
    class="screen-wrapper" 
    :class="{ 'fullscreen-mode': isFullscreen, 'window-mode': isWindowMode }"
    ref="wrapperRef"
  >
    <div class="tech-dashboard" ref="dashboardRef">
      <div class="header">
        <div class="title">ITOMS 智慧可视化数据大屏</div>
        <div class="subtitle">INTELLIGENT OPERATION MANAGEMENT SYSTEM</div>
        <div class="header-info">
          <div class="info-item">
            <span class="info-label">当前时间：</span>
            <span class="info-value">{{ currentTime }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">系统状态：</span>
            <span class="info-value status-normal">正常运行</span>
          </div>
          <div class="info-item device-info">
            <span class="info-label">当前设备：</span>
            <span class="info-value">{{ deviceType }}</span>
          </div>
        </div>
        <div class="header-right-action">
          <el-button-group>
            <el-button type="primary" plain size="small" @click="toggleWindowMode">
              {{ isWindowMode ? '退出窗口' : '窗口化' }}
            </el-button>
            <el-button type="primary" plain size="small" @click="toggleFullscreen">
              {{ isFullscreen ? '退出全屏' : '全屏' }}
            </el-button>
            <el-button type="primary" plain size="small" @click="fetchMetrics" :loading="loading">
              <el-icon><Refresh /></el-icon>
              刷新数据
            </el-button>
          </el-button-group>
        </div>
      </div>

      <!-- 核心指标面板 -->
      <div class="metrics-grid">
        <div class="metrics-card">
          <div class="metrics-icon">
            <el-icon><Monitor /></el-icon>
          </div>
          <div class="metrics-content">
            <div class="metrics-label">资产总数</div>
            <div class="metrics-value highlight-blue">{{ metrics.totalAssets || 0 }}</div>
          </div>
        </div>
        <div class="metrics-card">
          <div class="metrics-icon">
            <el-icon><DocumentChecked /></el-icon>
          </div>
          <div class="metrics-content">
            <div class="metrics-label">总工单量</div>
            <div class="metrics-value highlight-cyan">{{ metrics.totalWorkOrders || 0 }}</div>
          </div>
        </div>
        <div class="metrics-card">
          <div class="metrics-icon">
            <el-icon><Warning /></el-icon>
          </div>
          <div class="metrics-content">
            <div class="metrics-label">待处理工单</div>
            <div class="metrics-value highlight-orange">{{ metrics.pendingWorkOrders || 0 }}</div>
          </div>
        </div>
        <div class="metrics-card">
          <div class="metrics-icon">
            <el-icon><CircleCheck /></el-icon>
          </div>
          <div class="metrics-content">
            <div class="metrics-label">已完成工单</div>
            <div class="metrics-value highlight-green">{{ metrics.completedWorkOrders || 0 }}</div>
          </div>
        </div>
        <div class="metrics-card">
          <div class="metrics-icon">
            <el-icon><User /></el-icon>
          </div>
          <div class="metrics-content">
            <div class="metrics-label">系统用户数</div>
            <div class="metrics-value highlight-blue">{{ metrics.totalUsers || 0 }}</div>
          </div>
        </div>
        <div class="metrics-card">
          <div class="metrics-icon">
            <el-icon><Timer /></el-icon>
          </div>
          <div class="metrics-content">
            <div class="metrics-label">维修完成率</div>
            <div class="metrics-value highlight-cyan">{{ metrics.completionRate || 0 }}%</div>
          </div>
        </div>
        <div class="metrics-card">
          <div class="metrics-icon">
            <el-icon><Bell /></el-icon>
          </div>
          <div class="metrics-content">
            <div class="metrics-label">AI预测故障</div>
            <div class="metrics-value highlight-orange">{{ metrics.aiPredictedFaults || 0 }}</div>
          </div>
        </div>
        <div class="metrics-card">
          <div class="metrics-icon">
            <el-icon><Trophy /></el-icon>
          </div>
          <div class="metrics-content">
            <div class="metrics-label">AI推荐成功率</div>
            <div class="metrics-value highlight-green">{{ metrics.aiSuccessRate || 0 }}%</div>
          </div>
        </div>
      </div>

      <!-- 图表区域 -->
      <div class="charts-container">
        <!-- 第一行图表 -->
        <div class="chart-row">
          <div class="chart-item chart-item-2x">
            <div class="chart-wrapper">
              <div class="chart-title">工单趋势 (近7天)</div>
              <div ref="lineChartRef" class="chart-box"></div>
            </div>
          </div>
          <div class="chart-item">
            <div class="chart-wrapper">
              <div class="chart-title">资产健康度分布</div>
              <div ref="pieHealthRef" class="chart-box"></div>
            </div>
          </div>
          <div class="chart-item">
            <div class="chart-wrapper">
              <div class="chart-title">工单故障类型分布</div>
              <div ref="pieFaultRef" class="chart-box"></div>
            </div>
          </div>
        </div>

        <!-- 第二行图表 -->
        <div class="chart-row">
          <div class="chart-item">
            <div class="chart-wrapper">
              <div class="chart-title">部门工单分布</div>
              <div ref="barDeptRef" class="chart-box"></div>
            </div>
          </div>
          <div class="chart-item">
            <div class="chart-wrapper">
              <div class="chart-title">资产类型分布</div>
              <div ref="pieAssetTypeRef" class="chart-box"></div>
            </div>
          </div>
          <div class="chart-item">
            <div class="chart-wrapper">
              <div class="chart-title">AI预测趋势</div>
              <div ref="lineAiRef" class="chart-box"></div>
            </div>
          </div>
        </div>

        <!-- 第三行图表 -->
        <div class="chart-row">
          <div class="chart-item chart-item-2x">
            <div class="chart-wrapper">
              <div class="chart-title">故障高发时段</div>
              <div ref="heatmapRef" class="chart-box"></div>
            </div>
          </div>
          <div class="chart-item chart-item-2x">
            <div class="chart-wrapper">
              <div class="chart-title">维修响应时长排行</div>
              <div ref="barResponseRef" class="chart-box"></div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed, watch, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { Monitor, DocumentChecked, Warning, CircleCheck, Refresh, User, Timer, Bell, Trophy } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import request from '@/utils/request'

const router = useRouter()
const loading = ref(false)

const dashboardRef = ref<HTMLElement>()
const wrapperRef = ref<HTMLElement>()
const lineChartRef = ref<HTMLElement>()
const pieHealthRef = ref<HTMLElement>()
const pieFaultRef = ref<HTMLElement>()
const barDeptRef = ref<HTMLElement>()
const pieAssetTypeRef = ref<HTMLElement>()
const lineAiRef = ref<HTMLElement>()
const heatmapRef = ref<HTMLElement>()
const barResponseRef = ref<HTMLElement>()

let lineChart: echarts.ECharts | null = null
let pieHealthChart: echarts.ECharts | null = null
let pieFaultChart: echarts.ECharts | null = null
let barDeptChart: echarts.ECharts | null = null
let pieAssetTypeChart: echarts.ECharts | null = null
let lineAiChart: echarts.ECharts | null = null
let heatmapChart: echarts.ECharts | null = null
let barResponseChart: echarts.ECharts | null = null

const isFullscreen = ref(false)
const isWindowMode = ref(false)
const currentDeviceType = ref('desktop')

const deviceType = computed(() => {
  const typeMap: Record<string, string> = {
    mobile: '手机',
    tablet: '平板',
    desktop: '桌面'
  }
  return typeMap[currentDeviceType.value] || '桌面'
})

const detectDeviceType = () => {
  const width = window.innerWidth
  if (width < 768) {
    currentDeviceType.value = 'mobile'
  } else if (width < 1024) {
    currentDeviceType.value = 'tablet'
  } else {
    currentDeviceType.value = 'desktop'
  }
}

const toggleFullscreen = async () => {
  try {
    if (!document.fullscreenElement) {
      await wrapperRef.value?.requestFullscreen()
      isFullscreen.value = true
    } else {
      await document.exitFullscreen()
      isFullscreen.value = false
    }
  } catch (error) {
    ElMessage.error('全屏操作失败：' + (error instanceof Error ? error.message : '未知错误'))
  }
}

const toggleWindowMode = () => {
  isWindowMode.value = !isWindowMode.value
  if (isWindowMode.value) {
    isFullscreen.value = false
    if (document.fullscreenElement) {
      document.exitFullscreen()
    }
  }
  nextTick(() => {
    handleResize()
  })
}

const handleFullscreenChange = () => {
  isFullscreen.value = !!document.fullscreenElement
}

const currentTime = computed(() => {
  const now = new Date()
  return now.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
})

const metrics = ref({
  totalAssets: 0,
  totalWorkOrders: 0,
  pendingWorkOrders: 0,
  completedWorkOrders: 0,
  processingWorkOrders: 0,
  totalUsers: 0,
  completionRate: 0,
  aiPredictedFaults: 0,
  aiSuccessRate: 0,
  workOrderTrend: [] as any[],
  assetHealthDistribution: [] as any[],
  faultTypeDistribution: [] as any[],
  departmentWorkOrders: [] as any[],
  assetTypeDistribution: [] as any[],
  aiPredictionTrend: [] as any[],
  faultTimeDistribution: [] as any[],
  responseTimeRanking: [] as any[]
})

const fetchMetrics = async (retryCount = 0) => {
  loading.value = true
  try {
    const response = await request.get('/api/dashboard/metrics')
    if (response.data) {
      const data = response.data
      metrics.value = {
        totalAssets: data.totalAssets || 0,
        totalWorkOrders: data.totalWorkOrders || 0,
        pendingWorkOrders: data.pendingWorkOrders || 0,
        completedWorkOrders: data.completedWorkOrders || 0,
        processingWorkOrders: data.processingWorkOrders || 0,
        totalUsers: data.totalUsers || 0,
        completionRate: data.completionRate || 0,
        aiPredictedFaults: data.aiPredictedFaults || 0,
        aiSuccessRate: data.aiSuccessRate || 0,
        workOrderTrend: data.workOrderTrend || [],
        assetHealthDistribution: data.assetHealthDistribution || [],
        faultTypeDistribution: data.faultTypeDistribution || [],
        departmentWorkOrders: data.departmentWorkOrders || [],
        assetTypeDistribution: data.assetTypeDistribution || [],
        aiPredictionTrend: data.aiPredictionTrend || [],
        faultTimeDistribution: data.faultTimeDistribution || [],
        responseTimeRanking: data.responseTimeRanking || []
      }
    }
  } catch (error: any) {
    console.warn('Dashboard metrics fetch failed, using fallback data:', error.message)
    if (retryCount < 2) {
      await new Promise(resolve => setTimeout(resolve, 1000))
      return fetchMetrics(retryCount + 1)
    }
    ElMessage.warning('数据加载失败，正在使用模拟数据展示')
    simulateData()
  } finally {
    loading.value = false
    updateCharts()
  }
}

const simulateData = () => {
  metrics.value = {
    totalAssets: 1258,
    totalWorkOrders: 892,
    pendingWorkOrders: 45,
    completedWorkOrders: 847,
    processingWorkOrders: 45,
    totalUsers: 236,
    completionRate: 95.2,
    aiPredictedFaults: 23,
    aiSuccessRate: 87.5,
    workOrderTrend: [
      { date: '04-19', count: 120 },
      { date: '04-20', count: 132 },
      { date: '04-21', count: 101 },
      { date: '04-22', count: 134 },
      { date: '04-23', count: 90 },
      { date: '04-24', count: 110 },
      { date: '04-25', count: 125 }
    ],
    assetHealthDistribution: [
      { value: 850, name: '良好' },
      { value: 320, name: '一般' },
      { value: 88, name: '较差' }
    ],
    faultTypeDistribution: [
      { value: 350, name: '硬件故障' },
      { value: 280, name: '软件问题' },
      { value: 180, name: '网络故障' },
      { value: 82, name: '其他' }
    ],
    departmentWorkOrders: [
      { name: '技术部', value: 280 },
      { name: '市场部', value: 150 },
      { name: '行政部', value: 120 },
      { name: '财务部', value: 90 },
      { name: '人力资源部', value: 80 },
      { name: '销售部', value: 172 }
    ],
    assetTypeDistribution: [
      { value: 450, name: '电脑设备' },
      { value: 320, name: '网络设备' },
      { value: 280, name: '办公设备' },
      { value: 120, name: '其他设备' },
      { value: 88, name: '服务器' }
    ],
    aiPredictionTrend: [
      { date: '04-19', count: 5 },
      { date: '04-20', count: 8 },
      { date: '04-21', count: 3 },
      { date: '04-22', count: 10 },
      { date: '04-23', count: 6 },
      { date: '04-24', count: 4 },
      { date: '04-25', count: 7 }
    ],
    faultTimeDistribution: [
      [0, 0, 1], [0, 1, 0], [0, 2, 0], [0, 3, 0], [0, 4, 0], [0, 5, 0], [0, 6, 0], [0, 7, 2], [0, 8, 5], [0, 9, 8], [0, 10, 12], [0, 11, 15],
      [1, 0, 0], [1, 1, 0], [1, 2, 0], [1, 3, 0], [1, 4, 0], [1, 5, 0], [1, 6, 0], [1, 7, 3], [1, 8, 6], [1, 9, 10], [1, 10, 14], [1, 11, 18],
      [2, 0, 0], [2, 1, 0], [2, 2, 0], [2, 3, 0], [2, 4, 0], [2, 5, 0], [2, 6, 0], [2, 7, 2], [2, 8, 7], [2, 9, 11], [2, 10, 16], [2, 11, 12],
      [3, 0, 0], [3, 1, 0], [3, 2, 0], [3, 3, 0], [3, 4, 0], [3, 5, 0], [3, 6, 0], [3, 7, 4], [3, 8, 8], [3, 9, 12], [3, 10, 15], [3, 11, 10],
      [4, 0, 0], [4, 1, 0], [4, 2, 0], [4, 3, 0], [4, 4, 0], [4, 5, 0], [4, 6, 0], [4, 7, 3], [4, 8, 9], [4, 9, 13], [4, 10, 17], [4, 11, 14],
      [5, 0, 0], [5, 1, 0], [5, 2, 0], [5, 3, 0], [5, 4, 0], [5, 5, 0], [5, 6, 0], [5, 7, 1], [5, 8, 4], [5, 9, 6], [5, 10, 8], [5, 11, 5],
      [6, 0, 0], [6, 1, 0], [6, 2, 0], [6, 3, 0], [6, 4, 0], [6, 5, 0], [6, 6, 0], [6, 7, 0], [6, 8, 2], [6, 9, 3], [6, 10, 5], [6, 11, 3]
    ],
    responseTimeRanking: [
      { name: '技术部', value: 15 },
      { name: '市场部', value: 22 },
      { name: '行政部', value: 28 },
      { name: '财务部', value: 35 },
      { name: '人力资源部', value: 40 },
      { name: '销售部', value: 25 }
    ]
  }
}

const updateCharts = () => {
  // 工单趋势图
  if (lineChartRef.value) {
    if (!lineChart) {
      lineChart = echarts.init(lineChartRef.value)
      lineChart.on('click', (params) => {
        router.push({ path: '/work-order', query: { date: params.name } })
      })
    }
    const dates = metrics.value.workOrderTrend.map((i: any) => i.date)
    const counts = metrics.value.workOrderTrend.map((i: any) => i.count)
    lineChart.setOption({
      backgroundColor: 'transparent',
      tooltip: { trigger: 'axis', backgroundColor: 'rgba(0,0,0,0.7)', textStyle: { color: '#fff' } },
      grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
      xAxis: {
        type: 'category',
        data: dates,
        axisLine: { lineStyle: { color: '#314467' } },
        axisLabel: { color: '#8b9baf' }
      },
      yAxis: {
        type: 'value',
        splitLine: { lineStyle: { color: '#14274d', type: 'dashed' } },
        axisLabel: { color: '#8b9baf' }
      },
      series: [{
        name: '工单数量',
        type: 'line',
        smooth: true,
        data: counts,
        itemStyle: { color: '#00f3ff' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(0, 243, 255, 0.3)' },
            { offset: 1, color: 'rgba(0, 243, 255, 0)' }
          ])
        }
      }]
    })
  }

  // 资产健康度分布图
  if (pieHealthRef.value) {
    if (!pieHealthChart) {
      pieHealthChart = echarts.init(pieHealthRef.value)
      pieHealthChart.on('click', (params: any) => {
        router.push({ path: '/asset', query: { status: params.data.status || params.name } })
      })
    }
    pieHealthChart.setOption({
      backgroundColor: 'transparent',
      tooltip: { trigger: 'item', backgroundColor: 'rgba(0,0,0,0.7)', textStyle: { color: '#fff' } },
      legend: { bottom: '0', textStyle: { color: '#8b9baf' } },
      color: ['#00f3ff', '#f39c12', '#ff4d4f'],
      series: [{
        name: '资产健康度',
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#0b0f19',
          borderWidth: 2
        },
        label: { show: false, position: 'center' },
        emphasis: {
          label: { show: true, fontSize: 20, fontWeight: 'bold', color: '#fff' }
        },
        labelLine: { show: false },
        data: metrics.value.assetHealthDistribution
      }]
    })
  }

  // 工单故障类型分布图
  if (pieFaultRef.value) {
    if (!pieFaultChart) {
      pieFaultChart = echarts.init(pieFaultRef.value)
      pieFaultChart.on('click', (params) => {
        router.push({ path: '/work-order', query: { faultType: params.name } })
      })
    }
    pieFaultChart.setOption({
      backgroundColor: 'transparent',
      tooltip: { trigger: 'item', backgroundColor: 'rgba(0,0,0,0.7)', textStyle: { color: '#fff' } },
      legend: { bottom: '0', textStyle: { color: '#8b9baf' } },
      color: ['#36a3f7', '#34bfa3', '#f4516c', '#ffb822'],
      series: [{
        name: '故障类型',
        type: 'pie',
        radius: '60%',
        data: metrics.value.faultTypeDistribution,
        label: { color: '#8b9baf' },
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        }
      }]
    })
  }

  // 部门工单分布图
  if (barDeptRef.value) {
    if (!barDeptChart) {
      barDeptChart = echarts.init(barDeptRef.value)
      barDeptChart.on('click', (params) => {
        router.push({ path: '/work-order', query: { department: params.name } })
      })
    }
    const depts = metrics.value.departmentWorkOrders.map((i: any) => i.name)
    const values = metrics.value.departmentWorkOrders.map((i: any) => i.value)
    barDeptChart.setOption({
      backgroundColor: 'transparent',
      tooltip: { trigger: 'axis', backgroundColor: 'rgba(0,0,0,0.7)', textStyle: { color: '#fff' } },
      grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
      xAxis: {
        type: 'category',
        data: depts,
        axisLine: { lineStyle: { color: '#314467' } },
        axisLabel: { color: '#8b9baf', rotate: 45 }
      },
      yAxis: {
        type: 'value',
        splitLine: { lineStyle: { color: '#14274d', type: 'dashed' } },
        axisLabel: { color: '#8b9baf' }
      },
      series: [{
        name: '工单数量',
        type: 'bar',
        data: values,
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#36a3f7' },
            { offset: 1, color: '#00f3ff' }
          ])
        },
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 243, 255, 0.5)'
          }
        }
      }]
    })
  }

  // 资产类型分布图
  if (pieAssetTypeRef.value) {
    if (!pieAssetTypeChart) {
      pieAssetTypeChart = echarts.init(pieAssetTypeRef.value)
      pieAssetTypeChart.on('click', (params) => {
        router.push({ path: '/asset', query: { type: params.name } })
      })
    }
    pieAssetTypeChart.setOption({
      backgroundColor: 'transparent',
      tooltip: { trigger: 'item', backgroundColor: 'rgba(0,0,0,0.7)', textStyle: { color: '#fff' } },
      legend: { bottom: '0', textStyle: { color: '#8b9baf' } },
      color: ['#36a3f7', '#34bfa3', '#f4516c', '#ffb822', '#722ed1'],
      series: [{
        name: '资产类型',
        type: 'pie',
        radius: '60%',
        data: metrics.value.assetTypeDistribution,
        label: { color: '#8b9baf' },
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        }
      }]
    })
  }

  // AI预测趋势图
  if (lineAiRef.value) {
    if (!lineAiChart) {
      lineAiChart = echarts.init(lineAiRef.value)
    }
    const dates = metrics.value.aiPredictionTrend.map((i: any) => i.date)
    const counts = metrics.value.aiPredictionTrend.map((i: any) => i.count)
    lineAiChart.setOption({
      backgroundColor: 'transparent',
      tooltip: { trigger: 'axis', backgroundColor: 'rgba(0,0,0,0.7)', textStyle: { color: '#fff' } },
      grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
      xAxis: {
        type: 'category',
        data: dates,
        axisLine: { lineStyle: { color: '#314467' } },
        axisLabel: { color: '#8b9baf' }
      },
      yAxis: {
        type: 'value',
        splitLine: { lineStyle: { color: '#14274d', type: 'dashed' } },
        axisLabel: { color: '#8b9baf' }
      },
      series: [{
        name: 'AI预测故障数',
        type: 'line',
        smooth: true,
        data: counts,
        itemStyle: { color: '#722ed1' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(114, 46, 209, 0.3)' },
            { offset: 1, color: 'rgba(114, 46, 209, 0)' }
          ])
        }
      }]
    })
  }

  // 故障高发时段热力图
  if (heatmapRef.value) {
    if (!heatmapChart) {
      heatmapChart = echarts.init(heatmapRef.value)
    }
    heatmapChart.setOption({
      backgroundColor: 'transparent',
      tooltip: { position: 'top', backgroundColor: 'rgba(0,0,0,0.7)', textStyle: { color: '#fff' } },
      grid: { height: '60%', top: '10%' },
      xAxis: {
        type: 'category',
        data: ['0时', '1时', '2时', '3时', '4时', '5时', '6时', '7时', '8时', '9时', '10时', '11时'],
        splitArea: { show: true },
        axisLine: { lineStyle: { color: '#314467' } },
        axisLabel: { color: '#8b9baf' }
      },
      yAxis: {
        type: 'category',
        data: ['周日', '周一', '周二', '周三', '周四', '周五', '周六'],
        splitArea: { show: true },
        axisLine: { lineStyle: { color: '#314467' } },
        axisLabel: { color: '#8b9baf' }
      },
      visualMap: {
        min: 0,
        max: 20,
        calculable: true,
        orient: 'horizontal',
        left: 'center',
        bottom: '5%',
        inRange: {
          color: ['#14274d', '#36a3f7', '#00f3ff']
        },
        textStyle: { color: '#8b9baf' }
      },
      series: [{
        name: '故障数',
        type: 'heatmap',
        data: metrics.value.faultTimeDistribution,
        label: { show: true, color: '#fff' },
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        }
      }]
    })
  }

  // 维修响应时长排行
  if (barResponseRef.value) {
    if (!barResponseChart) {
      barResponseChart = echarts.init(barResponseRef.value)
    }
    const depts = metrics.value.responseTimeRanking.map((i: any) => i.name)
    const values = metrics.value.responseTimeRanking.map((i: any) => i.value)
    barResponseChart.setOption({
      backgroundColor: 'transparent',
      tooltip: { trigger: 'axis', backgroundColor: 'rgba(0,0,0,0.7)', textStyle: { color: '#fff' } },
      grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
      xAxis: {
        type: 'category',
        data: depts,
        axisLine: { lineStyle: { color: '#314467' } },
        axisLabel: { color: '#8b9baf', rotate: 45 }
      },
      yAxis: {
        type: 'value',
        name: '响应时长(分钟)',
        splitLine: { lineStyle: { color: '#14274d', type: 'dashed' } },
        axisLabel: { color: '#8b9baf' }
      },
      series: [{
        name: '响应时长',
        type: 'bar',
        data: values,
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#f4516c' },
            { offset: 1, color: '#ffb822' }
          ])
        },
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(244, 81, 108, 0.5)'
          }
        }
      }]
    })
  }
}

// 防抖函数
const debounce = (func: (...args: any[]) => void, wait: number) => {
  let timeout: number | null = null
  return (...args: any[]) => {
    if (timeout) clearTimeout(timeout)
    timeout = window.setTimeout(() => {
      func(...args)
    }, wait)
  }
}

const handleResize = debounce(() => {
  lineChart?.resize()
  pieHealthChart?.resize()
  pieFaultChart?.resize()
  barDeptChart?.resize()
  pieAssetTypeChart?.resize()
  lineAiChart?.resize()
  heatmapChart?.resize()
  barResponseChart?.resize()
}, 100)

const initScale = () => {
  if (!dashboardRef.value) return
  
  detectDeviceType()
  
  if (isWindowMode.value) {
    dashboardRef.value.style.width = '100%'
    dashboardRef.value.style.height = '100%'
    dashboardRef.value.style.position = 'relative'
    dashboardRef.value.style.top = '0'
    dashboardRef.value.style.left = '0'
  } else {
    if (isFullscreen.value) {
      dashboardRef.value.style.width = '100%'
      dashboardRef.value.style.height = '100%'
      dashboardRef.value.style.position = 'relative'
      dashboardRef.value.style.top = '0'
      dashboardRef.value.style.left = '0'
    } else {
      dashboardRef.value.style.width = '100%'
      dashboardRef.value.style.height = '100%'
      dashboardRef.value.style.position = 'relative'
      dashboardRef.value.style.top = '0'
      dashboardRef.value.style.left = '0'
    }
  }
}

watch(isFullscreen, () => {
  nextTick(() => {
    handleResize()
  })
})

onMounted(() => {
  fetchMetrics()
  initScale()
  detectDeviceType()
  window.addEventListener('resize', handleResize)
  document.addEventListener('fullscreenchange', handleFullscreenChange)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  document.removeEventListener('fullscreenchange', handleFullscreenChange)
  lineChart?.dispose()
  pieHealthChart?.dispose()
  pieFaultChart?.dispose()
  barDeptChart?.dispose()
  pieAssetTypeChart?.dispose()
  lineAiChart?.dispose()
  heatmapChart?.dispose()
  barResponseChart?.dispose()
})
</script>

<style scoped>
/* 全局样式重置 */
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

/* 响应式字体大小 */
:root {
  font-size: 16px;
}

@media (max-width: 1920px) {
  :root {
    font-size: 14px;
  }
}

@media (max-width: 1024px) {
  :root {
    font-size: 12px;
  }
}

@media (max-width: 768px) {
  :root {
    font-size: 10px;
  }
}

/* 全局视口重置 */
.screen-wrapper {
  width: 100vw;
  height: 100vh;
  overflow: hidden;
  background-color: #0b0f19;
  position: relative;
  background-image: 
    radial-gradient(circle at 10% 20%, rgba(0, 243, 255, 0.1) 0%, transparent 20%),
    radial-gradient(circle at 90% 80%, rgba(114, 46, 209, 0.1) 0%, transparent 20%);
  transition: all 0.3s ease;
}

.screen-wrapper.fullscreen-mode {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  z-index: 9999;
  background-color: #0b0f19;
}

.screen-wrapper.window-mode {
  background-color: rgba(11, 15, 25, 0.95);
  backdrop-filter: blur(10px);
}

/* 主容器 */
.tech-dashboard {
  width: 100%;
  height: 100%;
  background-color: #0b0f19;
  background-image: 
    linear-gradient(rgba(0, 243, 255, 0.03) 1px, transparent 1px),
    linear-gradient(90deg, rgba(0, 243, 255, 0.03) 1px, transparent 1px);
  background-size: 30px 30px;
  padding: 1rem;
  color: #fff;
  font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif;
  box-shadow: 0 0 50px rgba(0, 243, 255, 0.1);
  border-radius: 8px;
  overflow: hidden;
  transition: all 0.3s ease;
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.tech-dashboard::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(0, 243, 255, 0.5), transparent);
  box-shadow: 0 0 10px rgba(0, 243, 255, 0.5);
}

/* 头部 */
.header {
  text-align: center;
  padding: 1rem 0;
  position: relative;
  background: url('data:image/svg+xml;utf8,<svg width="100%" height="100%" xmlns="http://www.w3.org/2000/svg"><linearGradient id="g" x1="0%" y1="0%" x2="100%" y2="0%"><stop offset="0%" stop-color="rgba(0,243,255,0)"/><stop offset="50%" stop-color="rgba(0,243,255,0.2)"/><stop offset="100%" stop-color="rgba(0,243,255,0)"/></linearGradient><rect width="100%" height="2" y="100%" fill="url(%23g)"/></svg>') bottom center no-repeat;
  flex-shrink: 0;
}

.header-info {
  position: absolute;
  left: 1rem;
  top: 50%;
  transform: translateY(-50%);
  display: flex;
  gap: 1rem;
  flex-wrap: wrap;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 0.25rem;
}

.info-label {
  font-size: 0.875rem;
  color: #8b9baf;
}

.info-value {
  font-size: 0.875rem;
  color: #00f3ff;
  font-weight: 500;
}

.status-normal {
  color: #52c41a;
  text-shadow: 0 0 5px rgba(82, 196, 26, 0.5);
}

.header-right-action {
  position: absolute;
  right: 1rem;
  top: 50%;
  transform: translateY(-50%);
}

.title {
  font-size: 2rem;
  font-weight: bold;
  color: #00f3ff;
  text-shadow: 0 0 15px rgba(0, 243, 255, 0.6);
  letter-spacing: 0.125rem;
  margin-bottom: 0.25rem;
}

.subtitle {
  font-size: 0.75rem;
  color: #8b9baf;
  margin-top: 0.25rem;
  letter-spacing: 0.2rem;
  text-transform: uppercase;
}

/* 指标卡片网格 */
.metrics-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  grid-template-rows: repeat(2, 1fr);
  gap: 1rem;
  flex-shrink: 0;
  min-height: 160px;
}

.metrics-card {
  display: flex;
  align-items: center;
  background: rgba(13, 25, 50, 0.8);
  border: 1px solid #14274d;
  border-radius: 8px;
  padding: 1.25rem;
  box-shadow: 
    inset 0 0 20px rgba(0, 243, 255, 0.05),
    0 4px 12px rgba(0, 0, 0, 0.3);
  position: relative;
  overflow: hidden;
  transition: all 0.3s ease;
  min-height: 70px;
}

.metrics-card:hover {
  transform: translateY(-2px);
  box-shadow: 
    inset 0 0 30px rgba(0, 243, 255, 0.1),
    0 6px 16px rgba(0, 0, 0, 0.4);
}

.metrics-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 4px;
  height: 100%;
  background: linear-gradient(to bottom, #00f3ff, #36a3f7);
  box-shadow: 0 0 15px rgba(0, 243, 255, 0.5);
}

.metrics-icon {
  font-size: 2.5rem;
  color: #00f3ff;
  margin-right: 1.25rem;
  text-shadow: 0 0 15px rgba(0, 243, 255, 0.6);
  transition: all 0.3s ease;
}

.metrics-card:hover .metrics-icon {
  transform: scale(1.1);
}

.metrics-content {
  flex: 1;
}

.metrics-label {
  font-size: 0.875rem;
  color: #8b9baf;
  margin-bottom: 0.5rem;
  text-transform: uppercase;
  letter-spacing: 0.05rem;
}

.metrics-value {
  font-size: 1.75rem;
  font-weight: bold;
  font-family: 'Courier New', Courier, monospace;
  transition: all 0.3s ease;
}

.metrics-card:hover .metrics-value {
  transform: scale(1.05);
}

/* 高亮颜色 */
.highlight-blue { color: #00f3ff; text-shadow: 0 0 15px rgba(0, 243, 255, 0.6); }
.highlight-cyan { color: #34bfa3; text-shadow: 0 0 15px rgba(52, 191, 163, 0.6); }
.highlight-orange { color: #f4516c; text-shadow: 0 0 15px rgba(244, 81, 108, 0.6); }
.highlight-green { color: #52c41a; text-shadow: 0 0 15px rgba(82, 196, 26, 0.6); }

/* 图表容器 */
.charts-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 1rem;
  min-height: 0;
}

.chart-row {
  display: flex;
  gap: 1rem;
  flex: 1;
  min-height: 0;
}

.chart-item {
  flex: 1;
  min-width: 0;
  min-height: 0;
}

.chart-item-2x {
  flex: 2;
}

.chart-wrapper {
  height: 100%;
  background: rgba(13, 25, 50, 0.8);
  border: 1px solid #14274d;
  border-radius: 8px;
  padding: 1rem;
  box-shadow: 
    inset 0 0 20px rgba(0, 243, 255, 0.05),
    0 4px 12px rgba(0, 0, 0, 0.3);
  position: relative;
  transition: all 0.3s ease;
  display: flex;
  flex-direction: column;
  min-height: 0;
}

.chart-wrapper:hover {
  box-shadow: 
    inset 0 0 30px rgba(0, 243, 255, 0.1),
    0 6px 16px rgba(0, 0, 0, 0.4);
}

/* 角落装饰 */
.chart-wrapper::before, .chart-wrapper::after {
  content: '';
  position: absolute;
  width: 15px;
  height: 15px;
  border: 2px solid #00f3ff;
  border-radius: 2px;
}

.chart-wrapper::before {
  top: -1px;
  left: -1px;
  border-right: none;
  border-bottom: none;
  box-shadow: 0 0 10px rgba(0, 243, 255, 0.5);
}

.chart-wrapper::after {
  bottom: -1px;
  right: -1px;
  border-left: none;
  border-top: none;
  box-shadow: 0 0 10px rgba(0, 243, 255, 0.5);
}

.chart-title {
  font-size: 0.875rem;
  color: #fff;
  margin-bottom: 0.75rem;
  padding-left: 0.5rem;
  border-left: 3px solid #00f3ff;
  font-weight: 500;
  text-shadow: 0 0 5px rgba(0, 243, 255, 0.3);
  flex-shrink: 0;
}

.chart-box {
  flex: 1;
  width: 100%;
  position: relative;
  min-height: 0;
  padding: 0.5rem;
}

/* 响应式调整 */
@media (max-width: 1920px) {
  .tech-dashboard {
    padding: 0.75rem;
    gap: 0.75rem;
  }
  
  .metrics-grid {
    gap: 0.75rem;
    min-height: 140px;
  }
  
  .metrics-card {
    padding: 1rem;
    min-height: 60px;
  }
  
  .metrics-icon {
    font-size: 2.25rem;
    margin-right: 1rem;
  }
  
  .metrics-value {
    font-size: 1.5rem;
  }
  
  .charts-container {
    gap: 0.75rem;
  }
  
  .chart-row {
    gap: 0.75rem;
  }
  
  .chart-wrapper {
    padding: 0.75rem;
  }
  
  .chart-title {
    font-size: 0.8125rem;
    margin-bottom: 0.625rem;
  }
  
  .chart-box {
    padding: 0.375rem;
  }
}

@media (max-width: 1024px) {
  .title {
    font-size: 1.5rem;
  }
  
  .subtitle {
    font-size: 0.625rem;
    letter-spacing: 0.125rem;
  }
  
  .header-info {
    position: relative;
    left: 0;
    top: 0;
    transform: none;
    justify-content: center;
    margin-top: 0.5rem;
    gap: 0.75rem;
  }
  
  .header-right-action {
    position: relative;
    right: 0;
    top: 0;
    transform: none;
    margin-top: 0.5rem;
  }
  
  .metrics-grid {
    grid-template-columns: repeat(2, 1fr);
    grid-template-rows: repeat(4, 1fr);
    min-height: 280px;
  }
  
  .metrics-card {
    padding: 0.75rem;
    min-height: 50px;
  }
  
  .metrics-icon {
    font-size: 2rem;
    margin-right: 0.75rem;
  }
  
  .metrics-value {
    font-size: 1.25rem;
  }
  
  .metrics-label {
    font-size: 0.75rem;
  }
  
  .chart-row {
    flex-direction: column;
  }
  
  .chart-item-2x {
    flex: 1;
  }
  
  .chart-wrapper {
    padding: 0.625rem;
  }
  
  .chart-title {
    font-size: 0.75rem;
    margin-bottom: 0.5rem;
  }
  
  .chart-box {
    padding: 0.3125rem;
  }
}

@media (max-width: 768px) {
  .screen-wrapper {
    height: auto;
    min-height: 100vh;
    overflow: auto;
  }
  
  .tech-dashboard {
    height: auto;
    min-height: 100vh;
  }
  
  .title {
    font-size: 1.25rem;
    letter-spacing: 0.0625rem;
  }
  
  .subtitle {
    font-size: 0.5rem;
    letter-spacing: 0.0625rem;
  }
  
  .header {
    margin-bottom: 0.75rem;
    padding: 0.5rem 0;
  }
  
  .header-info {
    flex-direction: column;
    gap: 0.375rem;
    align-items: center;
  }
  
  .info-label, .info-value {
    font-size: 0.75rem;
  }
  
  .metrics-grid {
    grid-template-columns: 1fr;
    grid-template-rows: repeat(8, 1fr);
    min-height: 560px;
  }
  
  .metrics-card {
    padding: 0.625rem;
    flex-direction: column;
    text-align: center;
    min-height: 60px;
  }
  
  .metrics-icon {
    font-size: 1.75rem;
    margin-right: 0;
    margin-bottom: 0.5rem;
  }
  
  .metrics-value {
    font-size: 1.125rem;
  }
  
  .metrics-label {
    font-size: 0.6875rem;
    margin-bottom: 0.375rem;
  }
  
  .el-button-group {
    display: flex;
    flex-direction: column;
    gap: 0.25rem;
  }
  
  .el-button {
    width: 100%;
  }
}

/* 滚动条样式 */
::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}

::-webkit-scrollbar-track {
  background: rgba(13, 25, 50, 0.6);
}

::-webkit-scrollbar-thumb {
  background: rgba(0, 243, 255, 0.5);
  border-radius: 4px;
}

::-webkit-scrollbar-thumb:hover {
  background: rgba(0, 243, 255, 0.8);
}

/* 全屏模式特殊样式 */
:fullscreen .screen-wrapper,
:-webkit-full-screen .screen-wrapper {
  background-color: #0b0f19;
}

:fullscreen .tech-dashboard,
:-webkit-full-screen .tech-dashboard {
  border-radius: 0;
}
</style>
