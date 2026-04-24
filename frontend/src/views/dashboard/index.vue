<template>
  <div class="screen-wrapper">
    <div class="tech-dashboard" ref="dashboardRef">
      <div class="header">
        <div class="title">ITOMS 智慧可视化数据大屏</div>
      <div class="subtitle">INTELLIGENT OPERATION MANAGEMENT SYSTEM</div>
      <div class="header-right-action">
        <el-button type="primary" plain size="small" @click="fetchMetrics" :loading="loading">
          <el-icon><Refresh /></el-icon>
          刷新数据
        </el-button>
      </div>
    </div>

    <el-row :gutter="20" class="panel-group">
      <el-col :span="6" class="card-panel-col">
        <div class="tech-card">
          <div class="tech-icon">
            <el-icon><Monitor /></el-icon>
          </div>
          <div class="tech-info">
            <div class="tech-label">资产总数</div>
            <div class="tech-value highlight-blue">{{ metrics.totalAssets || 0 }}</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6" class="card-panel-col">
        <div class="tech-card">
          <div class="tech-icon">
            <el-icon><DocumentChecked /></el-icon>
          </div>
          <div class="tech-info">
            <div class="tech-label">总工单量</div>
            <div class="tech-value highlight-cyan">{{ metrics.totalWorkOrders || 0 }}</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6" class="card-panel-col">
        <div class="tech-card">
          <div class="tech-icon">
            <el-icon><Warning /></el-icon>
          </div>
          <div class="tech-info">
            <div class="tech-label">待处理工单</div>
            <div class="tech-value highlight-orange">{{ metrics.pendingWorkOrders || 0 }}</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6" class="card-panel-col">
        <div class="tech-card">
          <div class="tech-icon">
            <el-icon><CircleCheck /></el-icon>
          </div>
          <div class="tech-info">
            <div class="tech-label">已完成工单</div>
            <div class="tech-value highlight-green">{{ metrics.completedWorkOrders || 0 }}</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="chart-row">
      <el-col :span="12">
        <div class="tech-chart-container">
          <div class="tech-chart-title">工单趋势 (近7天)</div>
          <div ref="lineChartRef" class="chart-box"></div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="tech-chart-container">
          <div class="tech-chart-title">资产健康度分布</div>
          <div ref="pieHealthRef" class="chart-box"></div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="tech-chart-container">
          <div class="tech-chart-title">工单故障类型分布</div>
          <div ref="pieFaultRef" class="chart-box"></div>
        </div>
      </el-col>
    </el-row>
  </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { Monitor, DocumentChecked, Warning, CircleCheck, Refresh } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import axios from 'axios'

const router = useRouter()
const loading = ref(false)

const dashboardRef = ref<HTMLElement>()
const lineChartRef = ref<HTMLElement>()
const pieHealthRef = ref<HTMLElement>()
const pieFaultRef = ref<HTMLElement>()

let lineChart: echarts.ECharts | null = null
let pieHealthChart: echarts.ECharts | null = null
let pieFaultChart: echarts.ECharts | null = null

const metrics = ref({
  totalAssets: 0,
  totalWorkOrders: 0,
  pendingWorkOrders: 0,
  completedWorkOrders: 0,
  workOrderTrend: [] as any[],
  assetHealthDistribution: [] as any[],
  faultTypeDistribution: [] as any[]
})

const fetchMetrics = async () => {
  loading.value = true
  try {
    const res = await axios.get('/api/dashboard/metrics')
    if (res.data && res.data.code === 200) {
      metrics.value = res.data.data
    }
  } catch (error) {
    console.error('Failed to fetch dashboard metrics:', error)
    metrics.value = {
      totalAssets: 1024,
      totalWorkOrders: 512,
      pendingWorkOrders: 23,
      completedWorkOrders: 400,
      workOrderTrend: [
        { date: 'Mon', count: 12 },
        { date: 'Tue', count: 15 },
        { date: 'Wed', count: 10 },
        { date: 'Thu', count: 18 },
        { date: 'Fri', count: 14 },
        { date: 'Sat', count: 8 },
        { date: 'Sun', count: 5 }
      ],
      assetHealthDistribution: [
        { name: 'Healthy', value: 800, status: 'active' },
        { name: 'Warning', value: 150, status: 'warning' },
        { name: 'Critical', value: 74, status: 'error' }
      ],
      faultTypeDistribution: [
        { name: 'HARDWARE', value: 120 },
        { name: 'SOFTWARE', value: 80 },
        { name: 'NETWORK', value: 60 },
        { name: 'OTHER', value: 30 }
      ]
    }
  } finally {
    loading.value = false
    updateCharts()
  }
}

const updateCharts = () => {
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
}

const handleResize = () => {
  initScale()
  lineChart?.resize()
  pieHealthChart?.resize()
  pieFaultChart?.resize()
}

const initScale = () => {
  if (!dashboardRef.value) return
  const designWidth = 1920
  const designHeight = 1080
  const clientWidth = document.documentElement.clientWidth || window.innerWidth
  const clientHeight = document.documentElement.clientHeight || window.innerHeight
  const scaleX = clientWidth / designWidth
  const scaleY = clientHeight / designHeight
  const scale = Math.min(scaleX, scaleY)
  dashboardRef.value.style.transform = `scale(${scale}) translate(-50%, -50%)`
}

onMounted(() => {
  fetchMetrics()
  initScale()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  lineChart?.dispose()
  pieHealthChart?.dispose()
  pieFaultChart?.dispose()
})
</script>

<style scoped>
.screen-wrapper {
  width: 100vw;
  height: 100vh;
  overflow: hidden;
  background-color: #0b0f19;
  position: relative;
}

.tech-dashboard {
  position: absolute;
  top: 50%;
  left: 50%;
  width: 1920px;
  height: 1080px;
  transform-origin: 0 0;
  background-color: #0b0f19;
  background-image: 
    linear-gradient(rgba(0, 243, 255, 0.03) 1px, transparent 1px),
    linear-gradient(90deg, rgba(0, 243, 255, 0.03) 1px, transparent 1px);
  background-size: 30px 30px;
  padding: 20px;
  color: #fff;
  font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif;
}

.header {
  text-align: center;
  margin-bottom: 30px;
  padding: 20px 0;
  position: relative;
  background: url('data:image/svg+xml;utf8,<svg width="100%" height="100%" xmlns="http://www.w3.org/2000/svg"><linearGradient id="g" x1="0%" y1="0%" x2="100%" y2="0%"><stop offset="0%" stop-color="rgba(0,243,255,0)"/><stop offset="50%" stop-color="rgba(0,243,255,0.2)"/><stop offset="100%" stop-color="rgba(0,243,255,0)"/></linearGradient><rect width="100%" height="2" y="100%" fill="url(%23g)"/></svg>') bottom center no-repeat;
}

.header-right-action {
  position: absolute;
  right: 0;
  top: 50%;
  transform: translateY(-50%);
}

.title {
  font-size: 32px;
  font-weight: bold;
  color: #00f3ff;
  text-shadow: 0 0 10px rgba(0, 243, 255, 0.5);
  letter-spacing: 2px;
}

.subtitle {
  font-size: 14px;
  color: #8b9baf;
  margin-top: 5px;
  letter-spacing: 4px;
}

.panel-group {
  margin-bottom: 20px;
}

.tech-card {
  display: flex;
  align-items: center;
  background: rgba(13, 25, 50, 0.6);
  border: 1px solid #14274d;
  border-radius: 4px;
  padding: 20px;
  box-shadow: inset 0 0 20px rgba(0, 243, 255, 0.05);
  position: relative;
  overflow: hidden;
}

.tech-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 4px;
  height: 100%;
  background: #00f3ff;
  box-shadow: 0 0 10px #00f3ff;
}

.tech-icon {
  font-size: 48px;
  color: #00f3ff;
  margin-right: 20px;
  text-shadow: 0 0 10px rgba(0, 243, 255, 0.4);
}

.tech-info {
  flex: 1;
}

.tech-label {
  font-size: 14px;
  color: #8b9baf;
  margin-bottom: 10px;
}

.tech-value {
  font-size: 28px;
  font-weight: bold;
  font-family: 'Courier New', Courier, monospace;
}

.highlight-blue { color: #00f3ff; text-shadow: 0 0 10px rgba(0, 243, 255, 0.4); }
.highlight-cyan { color: #34bfa3; text-shadow: 0 0 10px rgba(52, 191, 163, 0.4); }
.highlight-orange { color: #f4516c; text-shadow: 0 0 10px rgba(244, 81, 108, 0.4); }
.highlight-green { color: #52c41a; text-shadow: 0 0 10px rgba(82, 196, 26, 0.4); }

.chart-row {
  margin-top: 20px;
}

.tech-chart-container {
  background: rgba(13, 25, 50, 0.6);
  border: 1px solid #14274d;
  border-radius: 4px;
  padding: 15px;
  box-shadow: inset 0 0 20px rgba(0, 243, 255, 0.05);
  position: relative;
}

/* Corner decorations */
.tech-chart-container::before, .tech-chart-container::after {
  content: '';
  position: absolute;
  width: 10px;
  height: 10px;
  border: 2px solid #00f3ff;
}

.tech-chart-container::before {
  top: -1px;
  left: -1px;
  border-right: none;
  border-bottom: none;
}

.tech-chart-container::after {
  bottom: -1px;
  right: -1px;
  border-left: none;
  border-top: none;
}

.tech-chart-title {
  font-size: 16px;
  color: #fff;
  margin-bottom: 15px;
  padding-left: 10px;
  border-left: 3px solid #00f3ff;
}

.chart-box {
  height: 350px;
  width: 100%;
}
</style>
