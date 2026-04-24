const { app, BrowserWindow, Tray, Menu, ipcMain, Notification } = require('electron');
const path = require('path');

let mainWindow = null;
let tray = null;
let isQuiting = false;

// Auto start logic
app.setLoginItemSettings({
  openAtLogin: true,
  openAsHidden: true
});

function createWindow() {
  mainWindow = new BrowserWindow({
    width: 1000,
    height: 700,
    webPreferences: {
      nodeIntegration: true,
      contextIsolation: false
    },
    show: false // Start hidden for background run
  });

  mainWindow.loadFile('index.html');

  mainWindow.once('ready-to-show', () => {
    // Only show if not hidden (e.g. from auto start)
    if (!process.argv.includes('--hidden')) {
      mainWindow.show();
    }
  });

  mainWindow.on('close', (event) => {
    if (!isQuiting) {
      event.preventDefault();
      mainWindow.hide();
    }
  });

  mainWindow.on('closed', () => {
    mainWindow = null;
  });
}

function createTray() {
  const iconPath = path.join(__dirname, 'icon.png');
  try {
    tray = new Tray(iconPath);
  } catch (e) {
    // mock tray
    console.error("Icon not found, tray may not work", e);
  }

  if (!tray) return;

  const updateMenu = (unreadCount = 0) => {
    const contextMenu = Menu.buildFromTemplate([
      {
        label: unreadCount > 0 ? `显示主窗口 (${unreadCount}条未读)` : '显示主窗口',
        click: () => {
          if (mainWindow) {
            mainWindow.show();
            mainWindow.focus();
            // Clear badge on show
            app.setBadgeCount(0);
            updateMenu(0);
            mainWindow.webContents.send('clear-unread');
          }
        }
      },
      { type: 'separator' },
      {
        label: '退出',
        click: () => {
          isQuiting = true;
          app.quit();
        }
      }
    ]);
    tray.setContextMenu(contextMenu);
    tray.setToolTip(unreadCount > 0 ? `IT服务大厅 (${unreadCount}条新消息)` : 'IT服务大厅 PC客户端');
  };

  updateMenu(0);

  tray.on('double-click', () => {
    if (mainWindow) {
      mainWindow.show();
      mainWindow.focus();
      app.setBadgeCount(0);
      updateMenu(0);
      mainWindow.webContents.send('clear-unread');
    }
  });

  // Listen to unread updates from renderer
  ipcMain.on('update-unread', (event, count) => {
    app.setBadgeCount(count);
    updateMenu(count);
  });
}

function setupIPC() {
  ipcMain.on('show-notification', (event, { title, body }) => {
    if (Notification.isSupported()) {
      const notification = new Notification({
        title: title || 'IT服务大厅',
        body: body || '您有新的工单消息'
      });

      notification.on('click', () => {
        if (mainWindow) {
          mainWindow.show();
          mainWindow.focus();
        }
      });

      notification.show();
    }
  });
}

const gotTheLock = app.requestSingleInstanceLock();

if (!gotTheLock) {
  app.quit();
} else {
  app.on('second-instance', () => {
    if (mainWindow) {
      if (mainWindow.isMinimized()) mainWindow.restore();
      mainWindow.show();
      mainWindow.focus();
    }
  });

  app.whenReady().then(() => {
    createWindow();
    createTray();
    setupIPC();

    app.on('activate', () => {
      if (BrowserWindow.getAllWindows().length === 0) {
        createWindow();
      } else if (mainWindow) {
        mainWindow.show();
      }
    });
  });

  app.on('before-quit', () => {
    isQuiting = true;
  });

  app.on('window-all-closed', () => {
    if (process.platform !== 'darwin') {
      app.quit();
    }
  });
}
