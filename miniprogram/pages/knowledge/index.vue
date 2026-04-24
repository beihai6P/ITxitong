<template>
  <view class="container">
    <view class="search-box">
      <input class="search-input" v-model="keyword" placeholder="搜索知识库..." @confirm="onSearch" />
      <icon type="search" size="18" class="search-icon" @click="onSearch" />
    </view>
    
    <view class="category-list">
      <scroll-view scroll-x class="scroll-view">
        <view 
          class="category-item" 
          v-for="cat in categories" 
          :key="cat.id"
          :class="{ active: currentCategory === cat.id }"
          @click="switchCategory(cat.id)"
        >
          {{ cat.name }}
        </view>
      </scroll-view>
    </view>

    <view class="article-list">
      <view class="article-item" v-for="item in articles" :key="item.id" @click="viewArticle(item)">
        <text class="article-title">{{ item.title }}</text>
        <text class="article-desc">{{ item.summary }}</text>
        <view class="article-meta">
          <text class="views">阅读 {{ item.views }}</text>
          <text class="time">{{ item.updateTime }}</text>
        </view>
      </view>
      <view class="empty" v-if="articles.length === 0">
        <text>未找到相关文章</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { request } from '../../utils/request.js';

const keyword = ref('');
const categories = ref([
  { id: 'all', name: '全部' },
  { id: 'network', name: '网络问题' },
  { id: 'software', name: '软件安装' },
  { id: 'hardware', name: '硬件故障' },
  { id: 'account', name: '账号权限' }
]);
const currentCategory = ref('all');
const articles = ref([]);

const fetchArticles = async () => {
  uni.showLoading({ title: '加载中...' });
  try {
    const { data } = await request({
      url: `/knowledge?category=${currentCategory.value}&keyword=${keyword.value}`,
      method: 'GET'
    });
    articles.value = data;
    uni.hideLoading();
  } catch (e) {
    uni.hideLoading();
    // mock data
    const mockData = [
      { id: 1, category: 'network', title: '公司WiFi连接指南', summary: '详细介绍如何连接公司内部WiFi以及常见问题排查', views: 1250, updateTime: '2026-04-20' },
      { id: 2, category: 'software', title: '常用办公软件下载地址', summary: '提供Office、钉钉、企业微信等常用软件的官方下载链接', views: 890, updateTime: '2026-04-21' },
      { id: 3, category: 'hardware', title: '打印机卡纸怎么处理？', summary: '图文并茂教你解决办公区打印机卡纸问题', views: 560, updateTime: '2026-04-22' }
    ];
    articles.value = mockData.filter(item => {
      const matchCat = currentCategory.value === 'all' || item.category === currentCategory.value;
      const matchKey = item.title.includes(keyword.value) || item.summary.includes(keyword.value);
      return matchCat && matchKey;
    });
  }
};

const onSearch = () => {
  fetchArticles();
};

const switchCategory = (id) => {
  currentCategory.value = id;
  fetchArticles();
};

const viewArticle = (item) => {
  uni.showModal({
    title: item.title,
    content: item.summary + '\n\n（更多详细内容请在PC端查看）',
    showCancel: false
  });
};

onMounted(() => {
  fetchArticles();
});
</script>

<style scoped>
.container {
  background-color: #f8f8f8;
  min-height: 100vh;
}
.search-box {
  background: #fff;
  padding: 20rpx 30rpx;
  position: relative;
}
.search-input {
  background: #f5f5f5;
  height: 72rpx;
  border-radius: 36rpx;
  padding: 0 80rpx 0 30rpx;
  font-size: 28rpx;
}
.search-icon {
  position: absolute;
  right: 50rpx;
  top: 36rpx;
  z-index: 2;
}
.category-list {
  background: #fff;
  border-bottom: 1px solid #eee;
}
.scroll-view {
  white-space: nowrap;
  padding: 0 20rpx;
}
.category-item {
  display: inline-block;
  padding: 20rpx 30rpx;
  font-size: 28rpx;
  color: #666;
}
.category-item.active {
  color: #007AFF;
  font-weight: bold;
  position: relative;
}
.category-item.active::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 30rpx;
  right: 30rpx;
  height: 4rpx;
  background: #007AFF;
  border-radius: 2rpx;
}
.article-list {
  padding: 20rpx;
}
.article-item {
  background: #fff;
  border-radius: 12rpx;
  padding: 30rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
}
.article-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 16rpx;
  display: block;
}
.article-desc {
  font-size: 28rpx;
  color: #666;
  margin-bottom: 20rpx;
  display: block;
  line-height: 1.5;
}
.article-meta {
  display: flex;
  justify-content: space-between;
  font-size: 24rpx;
  color: #999;
}
.empty {
  text-align: center;
  padding: 100rpx 0;
  color: #999;
}
</style>
