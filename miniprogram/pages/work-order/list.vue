<template>
  <view class="container">
    <view class="tabs">
      <view 
        class="tab-item" 
        :class="{ active: currentTab === 'all' }" 
        @click="switchTab('all')">全部</view>
      <view 
        class="tab-item" 
        :class="{ active: currentTab === 'pending' }" 
        @click="switchTab('pending')">待处理</view>
      <view 
        class="tab-item" 
        :class="{ active: currentTab === 'processing' }" 
        @click="switchTab('processing')">处理中</view>
      <view 
        class="tab-item" 
        :class="{ active: currentTab === 'resolved' }" 
        @click="switchTab('resolved')">已解决</view>
    </view>
    
    <view class="list">
      <view class="list-item" v-for="item in workOrders" :key="item.id" @click="goToDetail(item.id)">
        <view class="item-header">
          <text class="item-title">{{ item.category || '报修' }} - {{ item.assetCode || '无编号' }}</text>
          <text class="item-status" :class="item.status">{{ getStatusText(item.status) }}</text>
        </view>
        <view class="item-body">
          <text class="item-desc">{{ item.description }}</text>
          <text class="item-time">{{ item.createTime }}</text>
        </view>
      </view>
      <view class="empty" v-if="workOrders.length === 0">
        <text>暂无工单</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { request } from '../../utils/request.js';
import { onShow } from '@dcloudio/uni-app';

const workOrders = ref([]);
const currentTab = ref('all');

const getStatusText = (status) => {
  const statusMap = {
    'pending': '待处理',
    'processing': '处理中',
    'resolved': '已解决',
    'closed': '已关闭'
  };
  return statusMap[status] || status;
};

const switchTab = (tab) => {
  currentTab.value = tab;
  fetchData();
};

const fetchData = async () => {
  uni.showLoading({ title: '加载中...' });
  try {
    const { data } = await request({
      url: `/work-orders?status=${currentTab.value === 'all' ? '' : currentTab.value}`,
      method: 'GET'
    });
    workOrders.value = data;
    uni.hideLoading();
  } catch (error) {
    uni.hideLoading();
    // mock data
    workOrders.value = [
      {
        id: 1,
        category: '网络故障',
        assetCode: 'NET-001',
        description: '办公区WiFi无法连接',
        status: 'pending',
        createTime: '2026-04-24 10:00:00'
      },
      {
        id: 2,
        category: '硬件故障',
        assetCode: 'PC-002',
        description: '二楼会议室显示器无信号',
        status: 'processing',
        createTime: '2026-04-24 09:30:00'
      }
    ].filter(i => currentTab.value === 'all' || i.status === currentTab.value);
  }
};

const goToDetail = (id) => {
  uni.navigateTo({ url: `/pages/work-order/detail?id=${id}` });
};

onShow(() => {
  fetchData();
});
</script>

<style scoped>
.container {
  background-color: #f8f8f8;
  min-height: 100vh;
}
.tabs {
  display: flex;
  background-color: #fff;
  padding: 0 20rpx;
  border-bottom: 1px solid #eee;
  position: sticky;
  top: 0;
  z-index: 10;
}
.tab-item {
  flex: 1;
  text-align: center;
  padding: 20rpx 0;
  font-size: 28rpx;
  color: #666;
}
.tab-item.active {
  color: #007AFF;
  border-bottom: 2px solid #007AFF;
  font-weight: bold;
}
.list {
  padding: 20rpx;
}
.list-item {
  background-color: #fff;
  border-radius: 12rpx;
  padding: 30rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
}
.item-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16rpx;
}
.item-title {
  font-size: 32rpx;
  font-weight: 500;
  color: #333;
}
.item-status {
  font-size: 24rpx;
  padding: 4rpx 12rpx;
  border-radius: 8rpx;
}
.item-status.pending {
  background-color: #fff0f0;
  color: #ff4d4f;
}
.item-status.processing {
  background-color: #e6f7ff;
  color: #1890ff;
}
.item-status.resolved {
  background-color: #f6ffed;
  color: #52c41a;
}
.item-body {
  display: flex;
  flex-direction: column;
}
.item-desc {
  font-size: 28rpx;
  color: #666;
  margin-bottom: 16rpx;
  line-height: 1.5;
}
.item-time {
  font-size: 24rpx;
  color: #999;
}
.empty {
  text-align: center;
  padding: 60rpx 0;
  color: #999;
  font-size: 28rpx;
}
</style>
