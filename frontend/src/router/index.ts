import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    redirect: '/dashboard'
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/login/index.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/',
    component: () => import('../layout/index.vue'),
    meta: { requiresAuth: true },
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('../views/dashboard/index.vue'),
        meta: { title: '监控大屏', icon: 'Odometer' }
      },
      {
        path: 'asset',
        name: 'Asset',
        component: () => import('../views/asset/index.vue'),
        meta: { title: '资产管理', icon: 'Monitor' }
      },
      {
        path: 'work-order',
        name: 'WorkOrder',
        component: () => import('../views/work-order/index.vue'),
        meta: { title: '工单管理', icon: 'Document' }
      },
      {
        path: 'system/role',
        name: 'Role',
        component: () => import('../views/role/index.vue'),
        meta: { title: '角色管理', icon: 'User' }
      },
      {
        path: 'system/menu',
        name: 'Menu',
        component: () => import('../views/menu/index.vue'),
        meta: { title: '菜单管理', icon: 'Menu' }
      },
      {
        path: 'system/user',
        name: 'User',
        component: () => import('../views/user/index.vue'),
        meta: { title: '用户管理', icon: 'UserFilled' }
      },
      {
        path: 'system/dict',
        name: 'SysDict',
        component: () => import('../views/sys-dict/index.vue'),
        meta: { title: '字典管理', icon: 'Reading' }
      },
      {
        path: 'system/config',
        name: 'SysConfig',
        component: () => import('../views/sys-config/index.vue'),
        meta: { title: '参数配置', icon: 'Operation' }
      },
      {
        path: 'system/log',
        name: 'Log',
        component: () => import('../views/log/index.vue'),
        meta: { title: '日志管理', icon: 'Tickets' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to) => {
  const token = localStorage.getItem('token')

  if (to.meta.requiresAuth === true && !token) {
    return '/login'
  }
  
  if (to.path === '/login' && token) {
    return '/dashboard'
  }
  
  return true
})

export default router
