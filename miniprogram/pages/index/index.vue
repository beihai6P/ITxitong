<template>
  <view class="container">
    <view class="header">
      <view class="user-info">
        <text class="greeting">你好，{{ userInfo.name || '用户' }}</text>
        <text class="dept">{{ userInfo.department || '欢迎使用IT服务大厅' }}</text>
      </view>
    </view>
    
    <view class="banner">
      <text class="banner-title">IT服务大厅</text>
      <text class="banner-desc">高效、便捷的IT支持服务</text>
    </view>

    <view class="grid">
      <view class="grid-item" @click="navTo('/pages/repair-form/index')">
        <view class="icon-box bg-blue">
          <text class="icon">🔧</text>
        </view>
        <text class="text">故障报修</text>
      </view>
      <view class="grid-item" @click="navTo('/pages/work-order/list')">
        <view class="icon-box bg-orange">
          <text class="icon">📋</text>
        </view>
        <text class="text">我的工单</text>
      </view>
      <view class="grid-item" @click="navTo('/pages/asset/scan')">
        <view class="icon-box bg-green">
          <text class="icon">📷</text>
        </view>
        <text class="text">资产扫码</text>
      </view>
      <view class="grid-item" @click="navTo('/pages/knowledge/index')">
        <view class="icon-box bg-purple">
          <text class="icon">📚</text>
        </view>
        <text class="text">知识库</text>
      </view>
    </view>

    <view class="recent-notice">
      <view class="notice-header">
        <text class="title">最新公告</text>
      </view>
      <view class="notice-list">
        <view class="notice-item" v-for="item in notices" :key="item.id">
          <text class="notice-title">{{ item.title }}</text>
          <text class="notice-time">{{ item.time }}</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { onShow } from '@dcloudio/uni-app';

const userInfo = ref({});
const notices = ref([
  { id: 1, title: '关于周五晚上进行网络设备升级的通知', time: '2026-04-23' },
  { id: 2, title: '办公区打印机驱动更新说明', time: '2026-04-21' }
]);

const navTo = (url) => {
  if (!uni.getStorageSync('token') && url !== '/pages/knowledge/index') {
    uni.navigateTo({ url: '/pages/login/index' });
    return;
  }
  
  if (url === '/pages/work-order/list') {
    uni.switchTab({ url });
  } else {
    uni.navigateTo({ url });
  }
};

onShow(() => {
  userInfo.value = uni.getStorageSync('userInfo') || {};
});
</script>

<style scoped>
.container {
  background-color: #f8f8f8;
  min-height: 100vh;
}
.header {
  background: #fff;
  padding: 40rpx 30rpx;
}
.greeting {
  font-size: 40rpx;
  font-weight: bold;
  color: #333;
  display: block;
  margin-bottom: 10rpx;
}
.dept {
  font-size: 28rpx;
  color: #666;
}
.banner {
  margin: 30rpx;
  padding: 40rpx;
  background: linear-gradient(135deg, #007AFF, #0056b3);
  border-radius: 16rpx;
  color: #fff;
}
.banner-title {
  font-size: 36rpx;
  font-weight: bold;
  display: block;
  margin-bottom: 10rpx;
}
.banner-desc {
  font-size: 24rpx;
  opacity: 0.8;
}
.grid {
  display: flex;
  flex-wrap: wrap;
  background: #fff;
  padding: 30rpx 0;
  margin: 0 30rpx 30rpx;
  border-radius: 16rpx;
}
.grid-item {
  width: 25%;
  display: flex;
  flex-direction: column;
  align-items: center;
}
.icon-box {
  width: 80rpx;
  height: 80rpx;
  border-radius: 40rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 16rpx;
}
.bg-blue { background: #e6f7ff; color: #1890ff; }
.bg-orange { background: #fff7e6; color: #fa8c16; }
.bg-green { background: #f6ffed; color: #52c41a; }
.bg-purple { background: #f9f0ff; color: #722ed1; }
.icon {
  font-size: 40rpx;
}
.text {
  font-size: 24rpx;
  color: #333;
}
.recent-notice {
  background: #fff;
  margin: 0 30rpx 30rpx;
  border-radius: 16rpx;
  padding: 30rpx;
}
.notice-header {
  margin-bottom: 20rpx;
  border-bottom: 1px solid #eee;
  padding-bottom: 20rpx;
}
.title {
  font-size: 32rpx;
  font-weight: bold;
  border-left: 6rpx solid #007AFF;
  padding-left: 16rpx;
}
.notice-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20rpx 0;
  border-bottom: 1px dashed #f5f5f5;
}
.notice-item:last-child {
  border-bottom: none;
}
.notice-title {
  font-size: 28rpx;
  color: #333;
  flex: 1;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin-right: 20rpx;
}
.notice-time {
  font-size: 24rpx;
  color: #999;
}
</style>
