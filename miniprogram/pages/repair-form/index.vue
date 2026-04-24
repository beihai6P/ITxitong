<template>
  <view class="container">
    <!-- 骨架屏 -->
    <view class="skeleton" v-if="loading">
      <view class="skeleton-group">
        <view class="skeleton-label"></view>
        <view class="skeleton-input"></view>
      </view>
      <view class="skeleton-group">
        <view class="skeleton-label"></view>
        <view class="skeleton-input"></view>
      </view>
      <view class="skeleton-group">
        <view class="skeleton-label"></view>
        <view class="skeleton-textarea"></view>
      </view>
      <view class="skeleton-btn"></view>
    </view>

    <!-- 实际内容 -->
    <view v-else>
      <view class="form-group">
        <text class="label">资产编号 (可选)</text>
        <input class="input" v-model="formData.assetCode" placeholder="扫描或输入资产编号" />
        <button size="mini" class="scan-btn" @click="handleScan">扫码</button>
      </view>
      
      <view class="form-group">
        <text class="label">故障分类</text>
        <picker @change="onCategoryChange" :value="categoryIndex" :range="categories">
          <view class="picker">
            {{ formData.category || '请选择故障分类' }}
          </view>
        </picker>
      </view>

      <view class="form-group">
        <text class="label">故障描述 <text class="required">*</text></text>
        <textarea class="textarea" :class="{ 'has-error': errors.description }" v-model="formData.description" @input="validateDescription" placeholder="请详细描述故障现象..."></textarea>
        <text class="error-text" v-if="errors.description">{{ errors.description }}</text>
      </view>

      <view class="form-group">
        <text class="label">上传图片 (最多3张)</text>
        <view class="image-list">
          <view class="image-item" v-for="(img, index) in formData.images" :key="index">
            <image :src="img" mode="aspectFill" @click="previewImage(index)"></image>
            <text class="delete-btn" @click="deleteImage(index)">X</text>
          </view>
          <view class="upload-btn" @click="chooseImage" v-if="formData.images.length < 3">
            <text class="plus">+</text>
          </view>
        </view>
      </view>

      <view class="form-group">
        <text class="label">联系方式 <text class="required">*</text></text>
        <input class="input" :class="{ 'has-error': errors.contact }" v-model="formData.contact" @input="validateContact" placeholder="请输入您的联系电话" />
        <text class="error-text" v-if="errors.contact">{{ errors.contact }}</text>
      </view>

      <button class="submit-btn" type="primary" @click="debouncedSubmit">提交报修</button>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { request } from '../../utils/request.js';

const loading = ref(true);

onMounted(() => {
  // 模拟页面加载
  setTimeout(() => {
    loading.value = false;
  }, 800);
});

const formData = ref({
  assetCode: '',
  category: '',
  description: '',
  images: [],
  contact: ''
});

const errors = ref({
  description: '',
  contact: ''
});

const categories = ['硬件故障', '软件问题', '网络故障', '账号权限', '其他'];
const categoryIndex = ref(0);

const onCategoryChange = (e) => {
  categoryIndex.value = e.detail.value;
  formData.value.category = categories[e.detail.value];
};

const handleScan = () => {
  uni.scanCode({
    success: (res) => {
      formData.value.assetCode = res.result;
    },
    fail: () => {
      uni.showToast({ title: '扫码失败', icon: 'none' });
    }
  });
};

const validateDescription = () => {
  if (!formData.value.description.trim()) {
    errors.value.description = '请填写故障描述';
    return false;
  } else {
    errors.value.description = '';
    return true;
  }
};

const validateContact = () => {
  if (!formData.value.contact.trim()) {
    errors.value.contact = '请填写联系方式';
    return false;
  } else if (!/^1[3-9]\d{9}$/.test(formData.value.contact) && !/^\d{7,11}$/.test(formData.value.contact)) {
    // 简单校验手机或座机
    errors.value.contact = '联系方式格式不正确';
    return false;
  } else {
    errors.value.contact = '';
    return true;
  }
};

const chooseImage = () => {
  uni.chooseImage({
    count: 3 - formData.value.images.length,
    sizeType: ['compressed'],
    sourceType: ['album', 'camera'],
    success: (res) => {
      const tempFilePaths = res.tempFilePaths;
      uploadImages(tempFilePaths);
    }
  });
};

const uploadImages = (paths) => {
  uni.showLoading({ title: '上传中...' });
  let uploadPromises = paths.map(path => {
    return new Promise((resolve, reject) => {
      uni.uploadFile({
        url: 'http://localhost:3000/api/upload', // 替换为真实后端接口
        filePath: path,
        name: 'file',
        header: {
          'Authorization': `Bearer ${uni.getStorageSync('token') || ''}`
        },
        success: (uploadRes) => {
          try {
            const data = JSON.parse(uploadRes.data);
            resolve(data.url); // 假设后端返回 { url: '...' }
          } catch (e) {
            // mock success
            resolve(path);
          }
        },
        fail: (err) => {
          // mock success on fail
          resolve(path);
        }
      });
    });
  });

  Promise.all(uploadPromises).then(urls => {
    formData.value.images.push(...urls);
    uni.hideLoading();
  }).catch(() => {
    uni.hideLoading();
    uni.showToast({ title: '部分图片上传失败', icon: 'none' });
  });
};

const deleteImage = (index) => {
  formData.value.images.splice(index, 1);
};

const previewImage = (index) => {
  uni.previewImage({
    urls: formData.value.images,
    current: index
  });
};

const checkNetwork = () => {
  return new Promise((resolve) => {
    uni.getNetworkType({
      success: (res) => {
        if (res.networkType === 'none') {
          resolve(false);
        } else {
          resolve(true);
        }
      },
      fail: () => resolve(false)
    });
  });
};

let submitTimer = null;
const debouncedSubmit = () => {
  if (submitTimer) {
    clearTimeout(submitTimer);
  }
  submitTimer = setTimeout(() => {
    submitForm();
  }, 500);
};

const submitForm = async () => {
  const isDescValid = validateDescription();
  const isContactValid = validateContact();
  
  if (!isDescValid || !isContactValid) {
    return uni.showToast({ title: '请检查表单填写', icon: 'none' });
  }

  // 检查网络状态
  const isNetworkConnected = await checkNetwork();
  if (!isNetworkConnected) {
    return uni.showToast({ title: '网络未连接，请检查网络设置', icon: 'none' });
  }

  uni.showLoading({ title: '提交中...' });
  try {
    await request({
      url: '/work-orders',
      method: 'POST',
      data: formData.value
    });
    uni.hideLoading();
    uni.showToast({ title: '提交成功', icon: 'success' });
    setTimeout(() => {
      uni.switchTab({ url: '/pages/index/index' });
    }, 1500);
  } catch (e) {
    uni.hideLoading();
    // mock success
    uni.showToast({ title: '提交成功(测试)', icon: 'success' });
    setTimeout(() => {
      uni.switchTab({ url: '/pages/index/index' });
    }, 1500);
  }
};
</script>

<style scoped>
.container {
  padding: 30rpx;
  background-color: #fff;
  min-height: 100vh;
}
.form-group {
  margin-bottom: 30rpx;
  position: relative;
}
.label {
  display: block;
  font-size: 28rpx;
  color: #333;
  margin-bottom: 10rpx;
}
.required {
  color: red;
}
.input, .picker, .textarea {
  width: 100%;
  border: 1px solid #ddd;
  border-radius: 8rpx;
  padding: 20rpx;
  font-size: 28rpx;
  box-sizing: border-box;
}
.textarea {
  height: 200rpx;
}
.input.has-error, .textarea.has-error {
  border-color: #ff4d4f;
}
.error-text {
  color: #ff4d4f;
  font-size: 24rpx;
  margin-top: 8rpx;
  display: block;
}
.scan-btn {
  position: absolute;
  right: 10rpx;
  top: 50rpx;
  z-index: 10;
}
.image-list {
  display: flex;
  flex-wrap: wrap;
  gap: 20rpx;
}
.image-item {
  width: 200rpx;
  height: 200rpx;
  position: relative;
}
.image-item image {
  width: 100%;
  height: 100%;
  border-radius: 8rpx;
}
.delete-btn {
  position: absolute;
  top: -10rpx;
  right: -10rpx;
  background: red;
  color: white;
  width: 40rpx;
  height: 40rpx;
  border-radius: 50%;
  text-align: center;
  line-height: 40rpx;
  font-size: 24rpx;
}
.upload-btn {
  width: 200rpx;
  height: 200rpx;
  border: 1px dashed #ddd;
  display: flex;
  justify-content: center;
  align-items: center;
  border-radius: 8rpx;
}
.plus {
  font-size: 60rpx;
  color: #999;
}
.submit-btn {
  margin-top: 60rpx;
  border-radius: 40rpx;
}

/* 骨架屏样式 */
.skeleton {
  padding: 10rpx 0;
}
.skeleton-group {
  margin-bottom: 30rpx;
}
.skeleton-label {
  width: 150rpx;
  height: 30rpx;
  background-color: #f0f0f0;
  margin-bottom: 10rpx;
  border-radius: 4rpx;
  animation: skeleton-blink 1.2s ease-in-out infinite;
}
.skeleton-input {
  width: 100%;
  height: 80rpx;
  background-color: #f0f0f0;
  border-radius: 8rpx;
  animation: skeleton-blink 1.2s ease-in-out infinite;
}
.skeleton-textarea {
  width: 100%;
  height: 200rpx;
  background-color: #f0f0f0;
  border-radius: 8rpx;
  animation: skeleton-blink 1.2s ease-in-out infinite;
}
.skeleton-btn {
  width: 100%;
  height: 90rpx;
  background-color: #f0f0f0;
  border-radius: 40rpx;
  margin-top: 60rpx;
  animation: skeleton-blink 1.2s ease-in-out infinite;
}

@keyframes skeleton-blink {
  0% { opacity: 1; }
  50% { opacity: 0.6; }
  100% { opacity: 1; }
}
</style>
