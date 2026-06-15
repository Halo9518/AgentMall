import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      name: 'home',
      component: () => import('@/views/home/index.vue'),
      meta: { title: '首页' }
    },
    {
      path: '/merchant/:id',
      name: 'merchant-detail',
      component: () => import('@/views/merchant/detail.vue'),
      meta: { title: '商家详情' }
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/login/index.vue'),
      meta: { title: '登录' }
    },
    {
      path: '/register',
      name: 'register',
      component: () => import('@/views/register/index.vue'),
      meta: { title: '注册' }
    },
    {
      path: '/cart',
      name: 'cart',
      component: () => import('@/views/cart/index.vue'),
      meta: { title: '购物车', requireAuth: true }
    },
    {
      path: '/checkout',
      name: 'checkout',
      component: () => import('@/views/checkout/index.vue'),
      meta: { title: '结算下单', requireAuth: true }
    },
    {
      path: '/orders',
      name: 'orders',
      component: () => import('@/views/orders/list.vue'),
      meta: { title: '我的订单', requireAuth: true }
    },
    {
      path: '/orders/:id',
      name: 'order-detail',
      component: () => import('@/views/orders/detail.vue'),
      meta: { title: '订单详情', requireAuth: true }
    },
    {
      path: '/addresses',
      name: 'addresses',
      component: () => import('@/views/addresses/index.vue'),
      meta: { title: '地址管理', requireAuth: true }
    },
    {
      path: '/profile',
      name: 'profile',
      component: () => import('@/views/profile/index.vue'),
      meta: { title: '个人中心', requireAuth: true }
    }
  ]
})

export default router
