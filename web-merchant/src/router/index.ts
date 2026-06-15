import { createRouter, createWebHashHistory } from 'vue-router'

const router = createRouter({
  history: createWebHashHistory(),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/login/index.vue'),
      meta: { title: '商家登录' }
    },
    {
      path: '/',
      redirect: '/dashboard'
    },
    {
      path: '/dashboard',
      name: 'dashboard',
      component: () => import('@/views/dashboard/index.vue'),
      meta: { title: '数据概览', requireAuth: true, role: 'MERCHANT' }
    },
    {
      path: '/dishes',
      name: 'dishes',
      component: () => import('@/views/dishes/list.vue'),
      meta: { title: '菜品管理', requireAuth: true, role: 'MERCHANT' }
    },
    {
      path: '/categories',
      name: 'categories',
      component: () => import('@/views/categories/index.vue'),
      meta: { title: '分类管理', requireAuth: true, role: 'MERCHANT' }
    },
    {
      path: '/orders',
      name: 'orders',
      component: () => import('@/views/orders/list.vue'),
      meta: { title: '订单管理', requireAuth: true, role: 'MERCHANT' }
    },
    {
      path: '/settings',
      name: 'settings',
      component: () => import('@/views/settings/index.vue'),
      meta: { title: '店铺设置', requireAuth: true, role: 'MERCHANT' }
    }
  ]
})

export default router
