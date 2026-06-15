import { createRouter, createWebHashHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

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

// 路由守卫 — 登录 + 角色校验
router.beforeEach(async (to, _from, next) => {
  const userStore = useUserStore()

  // 需要登录的页面
  if (to.meta.requireAuth) {
    if (!userStore.isLoggedIn()) {
      next({ name: 'login', query: { redirect: to.fullPath } })
      return
    }
    // 角色校验
    if (to.meta.role && userStore.user && userStore.user.role !== to.meta.role) {
      next({ name: 'login' })
      return
    }
  }

  // 已登录时访问登录页，重定向到 dashboard
  if (to.name === 'login' && userStore.isLoggedIn()) {
    next({ name: 'dashboard' })
    return
  }

  next()
})

export default router
