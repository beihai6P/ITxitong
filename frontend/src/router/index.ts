import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'

const routes: Array<RouteRecordRaw> = [
  {
    path: '/',
    redirect: '/dashboard'
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/login/index.vue')
  },
  {
    path: '/',
    component: () => import('../layout/index.vue'),
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('../views/dashboard/index.vue')
      },
      {
        path: 'asset',
        name: 'Asset',
        component: () => import('../views/asset/index.vue')
      },
      {
        path: 'work-order',
        name: 'WorkOrder',
        component: () => import('../views/work-order/index.vue')
      },
      {
        path: 'role',
        name: 'Role',
        component: () => import('../views/role/index.vue')
      },
      {
        path: 'menu',
        name: 'Menu',
        component: () => import('../views/menu/index.vue')
      },
      {
        path: 'sys-dict',
        name: 'SysDict',
        component: () => import('../views/sys-dict/index.vue')
      },
      {
        path: 'sys-config',
        name: 'SysConfig',
        component: () => import('../views/sys-config/index.vue')
      },
      {
        path: 'log',
        name: 'Log',
        component: () => import('../views/log/index.vue')
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// Navigation Guard
router.beforeEach((to, _from, next) => {
  const token = localStorage.getItem('token')
  if (to.path !== '/login' && !token) {
    next('/login')
  } else if (to.path === '/login' && token) {
    next('/dashboard')
  } else {
    next()
  }
})

export default router
