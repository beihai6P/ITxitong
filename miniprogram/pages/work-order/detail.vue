<template>
  <view class="container">
    <view v-if="detail" class="detail-card">
      <view class="header">
        <text class="title">{{ detail.category || '故障报修' }}</text>
        <text class="status" :class="detail.status">{{ getStatusText(detail.status) }}</text>
      </view>
      
      <view class="info-list">
        <view class="info-item">
          <text class="label">资产编号：</text>
          <text class="value">{{ detail.assetCode || '无' }}</text>
        </view>
        <view class="info-item">
          <text class="label">提交时间：</text>
          <text class="value">{{ detail.createTime }}</text>
        </view>
        <view class="info-item">
          <text class="label">联系方式：</text>
          <text class="value">{{ detail.contact || '无' }}</text>
        </view>
        <view class="info-item">
          <text class="label">故障描述：</text>
          <text class="value desc">{{ detail.description }}</text>
        </view>
      </view>

      <view class="image-section" v-if="detail.images && detail.images.length">
        <text class="section-title">故障图片</text>
        <view class="image-list">
          <image 
            v-for="(img, idx) in detail.images" 
            :key="idx" 
            :src="img" 
            mode="aspectFill"
            @click="previewImage(idx)"
          />
        </view>
      </view>

      <view class="process-section" v-if="detail.logs && detail.logs.length">
        <text class="section-title">处理进度</text>
        <view class="timeline">
          <view class="timeline-item" v-for="(log, index) in detail.logs" :key="index">
            <view class="timeline-dot"></view>
            <view class="timeline-content">
              <text class="log-time">{{ log.time }}</text>
              <text class="log-desc">{{ log.action }}</text>
            </view>
          </view>
        </view>
      </view>
    </view>
    <view v-else class="loading">
      <text>加载中...</text>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import { request } from '../../utils/request.js';

const detail = ref(null);

const getStatusText = (status) => {
  const statusMap = {
    'pending': '待处理',
    'processing': '处理中',
    'resolved': '已解决',
    'closed': '已关闭'
  };
  return statusMap[status] || status;
};

const fetchDetail = async (id) => {
  uni.showLoading({ title: '加载中...' });
  try {
    const { data } = await request({
      url: `/work-orders/${id}`,
      method: 'GET'
    });
    detail.value = data;
    uni.hideLoading();
  } catch (e) {
    uni.hideLoading();
    // mock data
    detail.value = {
      id: id,
      category: '网络故障',
      assetCode: 'NET-001',
      description: '办公区WiFi无法连接，一直显示获取IP地址中',
      contact: '13800138000',
      status: 'processing',
      createTime: '2026-04-24 10:00:00',
      images: [
        'https://via.placeholder.com/150',
        'https://via.placeholder.com/150'
      ],
      logs: [
        { time: '2026-04-24 10:00:00', action: '用户提交报修工单' },
        { time: '2026-04-24 10:15:00', action: 'IT运维张三接单，正在处理' }
      ]
    };
  }
};

const previewImage = (index) => {
  uni.previewImage({
    urls: detail.value.images,
    current: index
  });
};

onLoad((options) => {
  if (options.id) {
    fetchDetail(options.id);
  }
});
</script>

<style scoped>
.container {
  padding: 20rpx;
  background-color: #f8f8f8;
  min-height: 100vh;
}
.detail-card {
  background: #fff;
  border-radius: 12rpx;
  padding: 30rpx;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
}
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid #eee;
  padding-bottom: 20rpx;
  margin-bottom: 20rpx;
}
.title {
  font-size: 36rpx;
  font-weight: bold;
}
.status {
  font-size: 24rpx;
  padding: 6rpx 16rpx;
  border-radius: 8rpx;
}
.status.pending { background: #fff0f0; color: #ff4d4f; }
.status.processing { background: #e6f7ff; color: #1890ff; }
.status.resolved { background: #f6ffed; color: #52c41a; }

.info-list {
  margin-bottom: 30rpx;
}
.info-item {
  display: flex;
  margin-bottom: 16rpx;
  font-size: 28rpx;
}
.label {
  color: #999;
  width: 140rpx;
  flex-shrink: 0;
}
.value {
  color: #333;
  flex: 1;
}
.desc {
  line-height: 1.5;
}

.section-title {
  font-size: 32rpx;
  font-weight: bold;
  margin: 30rpx 0 20rpx;
  display: block;
  border-left: 6rpx solid #007AFF;
  padding-left: 16rpx;
}
.image-list {
  display: flex;
  flex-wrap: wrap;
  gap: 20rpx;
}
.image-list image {
  width: 200rpx;
  height: 200rpx;
  border-radius: 8rpx;
}

.timeline {
  padding: 10rpx 0;
}
.timeline-item {
  display: flex;
  margin-bottom: 30rpx;
  position: relative;
}
.timeline-item::before {
  content: '';
  position: absolute;
  left: 10rpx;
  top: 30rpx;
  bottom: -30rpx;
  width: 2px;
  background: #eee;
}
.timeline-item:last-child::before {
  display: none;
}
.timeline-dot {
  width: 20rpx;
  height: 20rpx;
  border-radius: 50%;
  background: #007AFF;
  margin-top: 8rpx;
  position: relative;
  z-index: 1;
}
.timeline-content {
  margin-left: 20rpx;
  flex: 1;
}
.log-time {
  font-size: 24rpx;
  color: #999;
  display: block;
  margin-bottom: 8rpx;
}
.log-desc {
  font-size: 28rpx;
  color: #333;
}
.loading {
  text-align: center;
  padding: 100rpx;
  color: #999;
}
</style>
