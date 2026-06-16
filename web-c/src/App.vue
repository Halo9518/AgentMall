<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()

const MAIN_ROUTES = ['home', 'cart', 'orders', 'addresses', 'profile']

const showTabBar = computed(() => MAIN_ROUTES.includes(route.name as string))

const tabItems = [
  { name: 'home', icon: 'home-o', label: '首页', to: '/' },
  { name: 'cart', icon: 'cart-o', label: '购物车', to: '/cart' },
  { name: 'orders', icon: 'records-o', label: '订单', to: '/orders' },
  { name: 'addresses', icon: 'location-o', label: '地址', to: '/addresses' },
  { name: 'profile', icon: 'contact-o', label: '我的', to: '/profile' }
]

const activeTabName = computed(() => {
  const name = route.name as string
  return MAIN_ROUTES.includes(name) ? name : ''
})
</script>

<template>
  <div id="app-wrapper">
    <router-view />
    <van-tabbar
      v-if="showTabBar"
      v-model="activeTabName"
      route
      active-color="#ff6b35"
      placeholder
    >
      <van-tabbar-item
        v-for="item in tabItems"
        :key="item.name"
        :name="item.name"
        :icon="item.icon"
        :to="item.to"
      >
        {{ item.label }}
      </van-tabbar-item>
    </van-tabbar>
  </div>
</template>

<style>
#app {
  min-height: 100vh;
  background-color: #f7f8fa;
}
</style>
