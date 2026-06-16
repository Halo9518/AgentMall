<template>
  <div class="merchant-page">
    <!-- 商家信息头 -->
    <div class="merchant-header">
      <van-nav-bar
        :title="(merchant && merchant.name) || '商家详情'"
        left-text="返回"
        left-arrow
        @click-left="$router.back()"
      />
      <div class="merchant-banner" v-if="merchant">
        <div class="merchant-logo">
          <van-image
            v-if="merchant.logo"
            :src="merchant.logo"
            width="72"
            height="72"
            fit="cover"
            round
          />
          <div v-else class="merchant-logo-placeholder">
            {{ merchant.name.charAt(0) }}
          </div>
        </div>
        <div class="merchant-summary">
          <h2>{{ merchant.name }}</h2>
          <p class="merchant-desc">{{ merchant.description || '暂无简介' }}</p>
          <div class="merchant-meta-row">
            <span>⏰ {{ merchant.openingHours }}</span>
            <span>📍 {{ merchant.address }}</span>
          </div>
          <div class="merchant-fee-row">
            <span>起送 ¥{{ merchant.minAmount }}</span>
            <span>配送费 ¥{{ merchant.deliveryFee }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 分类 Tab -->
    <van-tabs
      v-model:active="activeCategory"
      sticky
      offset-top="0"
      @change="onCategoryChange"
    >
      <van-tab title="全部" :name="0" />
      <van-tab
        v-for="cat in categories"
        :key="cat.id"
        :title="cat.name"
        :name="cat.id"
      />
    </van-tabs>

    <!-- 菜品列表 -->
    <div class="dish-list" :style="{ paddingBottom: cartItemCount > 0 ? '70px' : '40px' }">
      <div v-for="dish in filteredDishes" :key="dish.id" class="dish-card">
        <div class="dish-image">
          <van-image
            v-if="dish.image"
            :src="dish.image"
            width="88"
            height="88"
            fit="cover"
            radius="8"
          />
          <div v-else class="dish-image-placeholder">🍽️</div>
        </div>
        <div class="dish-info">
          <h4 class="dish-name">{{ dish.name }}</h4>
          <p class="dish-desc" v-if="dish.description">{{ dish.description }}</p>
          <div class="dish-bottom">
            <span class="dish-price">¥{{ dish.price }}</span>
            <span class="dish-sales">已售 {{ dish.salesCount }}</span>
          </div>
        </div>
        <div class="dish-add-btn">
          <van-button
            icon="plus"
            size="small"
            round
            type="primary"
            @click.stop="addToCart(dish)"
          />
        </div>
      </div>

      <van-empty v-if="!loading && filteredDishes.length === 0" description="暂无菜品" />
    </div>

    <van-loading v-if="loading" class="loading-center" type="spinner" />

    <!-- 底部购物车栏 -->
    <div class="cart-bar" v-if="cartItemCount > 0">
      <div class="cart-bar-left" @click="$router.push('/cart')">
        <van-icon name="cart-o" size="20" color="#fff" />
        <span class="cart-badge">{{ cartItemCount }}</span>
      </div>
      <div class="cart-bar-center">
        <span class="cart-total">¥{{ cartTotal.toFixed(2) }}</span>
        <span class="cart-delivery" v-if="merchant">配送费 ¥{{ merchant.deliveryFee }}</span>
      </div>
      <van-button
        type="primary"
        round
        size="small"
        @click="goCheckout"
      >
        去结算
      </van-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showNotify } from 'vant'
import { getMerchantDetail, getCategories, getDishes } from '@/api/merchant'
import type { Merchant, Category, DishVO } from '@/api/merchant'
import { useCartStore } from '@/stores/cart'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const merchantId = Number(route.params.id)
const cartStore = useCartStore()
const userStore = useUserStore()

const merchant = ref<Merchant | null>(null)
const categories = ref<Category[]>([])
const allDishes = ref<DishVO[]>([])
const activeCategory = ref<number | string>(0)
const loading = ref(true)

const filteredDishes = computed(() => {
  const catId = activeCategory.value
  if (catId === 0 || catId === '0') return allDishes.value
  return allDishes.value.filter((d) => d.categoryId === Number(catId))
})

const cartItemCount = computed(() => cartStore.itemCount)
const cartTotal = computed(() => cartStore.cart.totalAmount)

const onCategoryChange = () => {
  // computed reactivity handles filtering
}

const addToCart = async (dish: DishVO) => {
  if (!userStore.isLoggedIn()) {
    router.push({ name: 'login', query: { redirect: route.fullPath } })
    return
  }
  try {
    await cartStore.add(dish.id)
    showNotify({ type: 'success', message: `已加入购物车：${dish.name}` })
  } catch {
    showNotify({ type: 'danger', message: '添加失败' })
  }
}

const goCheckout = () => {
  if (!userStore.isLoggedIn()) {
    router.push({ name: 'login', query: { redirect: '/checkout' } })
    return
  }
  router.push('/checkout')
}

onMounted(async () => {
  try {
    const [m, cats, dishes] = await Promise.all([
      getMerchantDetail(merchantId),
      getCategories(merchantId),
      getDishes(merchantId)
    ])
    merchant.value = m
    categories.value = cats
    allDishes.value = dishes
  } catch {
    // error handled by interceptor
  } finally {
    loading.value = false
  }

  // 获取购物车数据（已登录时）
  if (userStore.isLoggedIn()) {
    cartStore.fetch()
  }
})
</script>

<style scoped lang="scss">
.merchant-page {
  min-height: 100vh;
  background: #f7f8fa;
}

.merchant-banner {
  display: flex;
  gap: 12px;
  padding: 16px;
  background: #fff;
}

.merchant-logo-placeholder {
  width: 72px;
  height: 72px;
  border-radius: 50%;
  background: linear-gradient(135deg, #ff6b35, #f7c948);
  color: #fff;
  font-size: 32px;
  font-weight: bold;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.merchant-summary {
  flex: 1;

  h2 {
    font-size: 18px;
    font-weight: 600;
    margin-bottom: 4px;
    color: #323233;
  }
}

.merchant-desc {
  font-size: 12px;
  color: #969799;
  margin-bottom: 6px;
}

.merchant-meta-row {
  font-size: 12px;
  color: #969799;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 4px;
}

.merchant-fee-row {
  font-size: 12px;
  color: #ff6b35;
  display: flex;
  gap: 12px;
}

.dish-list {
  padding: 12px 16px 40px;
}

.dish-card {
  display: flex;
  gap: 12px;
  padding: 12px;
  margin-bottom: 10px;
  background: #fff;
  border-radius: 10px;
  position: relative;
}

.dish-add-btn {
  position: absolute;
  right: 8px;
  bottom: 8px;
}

.dish-image-placeholder {
  width: 88px;
  height: 88px;
  border-radius: 8px;
  background: #f7f8fa;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 36px;
  flex-shrink: 0;
}

.dish-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.dish-name {
  font-size: 15px;
  font-weight: 500;
  color: #323233;
}

.dish-desc {
  font-size: 12px;
  color: #969799;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.dish-bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.dish-price {
  font-size: 16px;
  font-weight: 600;
  color: #ff6b35;
}

.dish-sales {
  font-size: 12px;
  color: #c8c9cc;
}

.loading-center {
  display: flex;
  justify-content: center;
  padding-top: 60px;
}

.cart-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 16px;
  background: #2c2c2c;
  z-index: 100;
}

.cart-bar-left {
  position: relative;
  display: flex;
  align-items: center;
  cursor: pointer;
}

.cart-badge {
  position: absolute;
  top: -8px;
  right: -10px;
  min-width: 18px;
  height: 18px;
  line-height: 18px;
  text-align: center;
  border-radius: 9px;
  background: #ee0a24;
  color: #fff;
  font-size: 11px;
  font-weight: 600;
  padding: 0 4px;
}

.cart-bar-center {
  flex: 1;
  display: flex;
  flex-direction: column;
  color: #fff;
}

.cart-total {
  font-size: 16px;
  font-weight: 700;
}

.cart-delivery {
  font-size: 11px;
  color: #c8c9cc;
}
</style>
