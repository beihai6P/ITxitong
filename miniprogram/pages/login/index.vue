<template>
  <view class="container">
    <view class="logo-box">
      <text class="title">IT服务大厅</text>
    </view>
    <button class="login-btn" type="primary" @click="handleLogin">微信一键登录</button>
  </view>
</template>

<script setup>
import { request } from '../../utils/request.js';

const handleLogin = () => {
  uni.showLoading({ title: '登录中...' });
  uni.login({
    provider: 'weixin',
    success: async (res) => {
      if (res.code) {
        try {
          // 调用后端登录接口
          const { data } = await request({
            url: '/auth/wx-login',
            method: 'POST',
            data: { code: res.code }
          });
          
          // 假设后端返回 token 和 userInfo
          uni.setStorageSync('token', data.token);
          uni.setStorageSync('userInfo', data.userInfo);
          
          uni.showToast({ title: '登录成功' });
          setTimeout(() => {
            uni.switchTab({ url: '/pages/index/index' });
          }, 1500);
        } catch (error) {
          console.error('登录接口失败', error);
          // 为了演示，假装登录成功
          mockLoginSuccess();
        }
      } else {
        uni.hideLoading();
        uni.showToast({ title: '获取code失败', icon: 'none' });
      }
    },
    fail: (err) => {
      uni.hideLoading();
      uni.showToast({ title: '微信登录失败', icon: 'none' });
      console.error(err);
      // 为了演示，假装登录成功
      mockLoginSuccess();
    }
  });
};

const mockLoginSuccess = () => {
  uni.hideLoading();
  uni.setStorageSync('token', 'mock-token-123');
  uni.setStorageSync('userInfo', { name: '张三', department: '研发部' });
  uni.showToast({ title: '测试登录成功' });
  setTimeout(() => {
    uni.switchTab({ url: '/pages/index/index' });
  }, 1500);
};
</script>

<style scoped>
.container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100vh;
  padding: 40rpx;
  background-color: #fff;
}
.logo-box {
  margin-bottom: 100rpx;
}
.title {
  font-size: 48rpx;
  font-weight: bold;
  color: #333;
}
.login-btn {
  width: 100%;
  border-radius: 40rpx;
}
</style>
