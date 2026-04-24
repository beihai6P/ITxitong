<template>
  <view class="container">
    <view class="scan-area">
      <view class="scan-button" @click="startScan">
        <image class="scan-icon" src="/static/scan-icon.png" mode="aspectFit"></image>
        <text class="text">点击扫码查询</text>
      </view>
    </view>

    <view class="result-area" v-if="scanResult">
      <view class="result-card">
        <view class="card-title">资产信息</view>
        <view class="info-list">
          <view class="info-item">
            <text class="label">资产编号：</text>
            <text class="value">{{ scanResult.assetCode || '无' }}</text>
          </view>
          <view class="info-item">
            <text class="label">资产名称：</text>
            <text class="value">{{ scanResult.assetName || '无' }}</text>
          </view>
          <view class="info-item">
            <text class="label">资产分类：</text>
            <text class="value">{{ scanResult.category || '无' }}</text>
          </view>
          <view class="info-item">
            <text class="label">当前状态：</text>
            <text class="value" :class="scanResult.status">{{ getStatusText(scanResult.status) }}</text>
          </view>
          <view class="info-item">
            <text class="label">购入时间：</text>
            <text class="value">{{ scanResult.purchaseDate || '无' }}</text>
          </view>
        </view>
      </view>

      <view class="action-card">
        <button class="action-btn" type="primary" @click="reportRepair">一键报修</button>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue';
import { request } from '../../utils/request.js';

const scanResult = ref(null);

const getStatusText = (status) => {
  const map = {
    'in_use': '使用中',
    'idle': '闲置',
    'repairing': '维修中',
    'scrapped': '已报废'
  };
  return map[status] || status || '未知';
};

const startScan = () => {
  uni.scanCode({
    success: (res) => {
      fetchAssetInfo(res.result);
    },
    fail: () => {
      uni.showToast({ title: '扫码失败', icon: 'none' });
    }
  });
};

const fetchAssetInfo = async (code) => {
  uni.showLoading({ title: '查询中...' });
  try {
    const { data } = await request({
      url: `/assets/scan`,
      method: 'POST',
      data: { code }
    });
    scanResult.value = data;
    uni.hideLoading();
  } catch (e) {
    uni.hideLoading();
    // mock data
    scanResult.value = {
      assetCode: code.includes('http') ? 'AST-2026-001' : code,
      assetName: 'ThinkPad T14 笔记本电脑',
      category: '办公设备',
      status: 'in_use',
      purchaseDate: '2023-05-10'
    };
  }
};

const reportRepair = () => {
  if (scanResult.value && scanResult.value.assetCode) {
    uni.navigateTo({
      url: `/pages/repair-form/index?assetCode=${scanResult.value.assetCode}`
    });
  }
};
</script>

<style scoped>
.container {
  padding: 30rpx;
  background-color: #f8f8f8;
  min-height: 100vh;
}
.scan-area {
  display: flex;
  justify-content: center;
  padding: 60rpx 0;
}
.scan-button {
  width: 300rpx;
  height: 300rpx;
  background: linear-gradient(135deg, #007AFF, #0056b3);
  border-radius: 50%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  box-shadow: 0 10rpx 30rpx rgba(0, 122, 255, 0.3);
  transition: all 0.3s;
}
.scan-button:active {
  transform: scale(0.95);
}
.scan-icon {
  width: 80rpx;
  height: 80rpx;
  margin-bottom: 20rpx;
}
.text {
  color: #fff;
  font-size: 32rpx;
  font-weight: 500;
}
.result-card, .action-card {
  background: #fff;
  border-radius: 16rpx;
  padding: 30rpx;
  margin-bottom: 30rpx;
  box-shadow: 0 4rpx 16rpx rgba(0,0,0,0.05);
}
.card-title {
  font-size: 32rpx;
  font-weight: bold;
  margin-bottom: 30rpx;
  border-bottom: 1px solid #eee;
  padding-bottom: 20rpx;
}
.info-item {
  display: flex;
  margin-bottom: 24rpx;
  font-size: 28rpx;
}
.info-item:last-child {
  margin-bottom: 0;
}
.label {
  color: #666;
  width: 160rpx;
}
.value {
  color: #333;
  flex: 1;
  font-weight: 500;
}
.value.in_use { color: #52c41a; }
.value.idle { color: #1890ff; }
.value.repairing { color: #faad14; }
.value.scrapped { color: #ff4d4f; }

.action-btn {
  border-radius: 40rpx;
  font-size: 32rpx;
}
</style>
