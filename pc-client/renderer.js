const { ipcRenderer } = require('electron');

const { createApp, ref, onMounted, watch } = Vue;

const app = createApp({
  setup() {
    const savedOrders = localStorage.getItem('workOrders');
    const defaultOrders = [
      {
        id: 1,
        title: '网络故障 - 办公区WiFi无法连接',
        description: '一直显示获取IP地址中',
        status: 'pending',
        time: '2026-04-24 10:00:00'
      },
      {
        id: 2,
        title: '硬件故障 - 电脑无法开机',
        description: '按电源键没反应',
        status: 'processing',
        time: '2026-04-24 09:30:00'
      }
    ];

    const workOrders = ref(savedOrders ? JSON.parse(savedOrders) : defaultOrders);

    watch(workOrders, (newVal) => {
      localStorage.setItem('workOrders', JSON.stringify(newVal));
    }, { deep: true });

    const unreadCount = ref(0);

    const getStatusText = (status) => {
      const map = {
        'pending': '待处理',
        'processing': '处理中',
        'resolved': '已解决'
      };
      return map[status] || status;
    };

    const updateUnread = (count) => {
      unreadCount.value = count;
      ipcRenderer.send('update-unread', count);
    };

    const simulateNewOrder = () => {
      const newOrder = {
        id: Date.now(),
        title: '软件问题 - 无法安装打印机驱动',
        description: '提示系统权限不足',
        status: 'pending',
        time: new Date().toLocaleString('zh-CN', { hour12: false })
      };
      
      workOrders.value.unshift(newOrder);
      updateUnread(unreadCount.value + 1);

      // Trigger notification
      ipcRenderer.send('show-notification', {
        title: '新工单提醒',
        body: `收到新的工单: ${newOrder.title}`
      });
    };

    onMounted(() => {
      // Listen to clear unread from main process (e.g. when tray double-clicked)
      ipcRenderer.on('clear-unread', () => {
        updateUnread(0);
      });
    });

    return {
      workOrders,
      unreadCount,
      getStatusText,
      simulateNewOrder
    };
  }
});

app.mount('#app');
