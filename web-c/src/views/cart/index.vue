<template>
  <div class="cart-page">
    <van-nav-bar title="购物车" left-arrow @click-left="$router.back()" />

    <!-- 空状态 -->
    <van-empty v-if="!cartStore.loading && cartStore.isEmpty" description="购物车是空的">
      <van-button round type="primary" to="/">去逛逛</van-button>
    </van-empty>

    <!-- 购物车内容 -->
    <template v-else>
      <!-- 商家信息 -->
      <div class="merchant-bar" v-if="cartStore.cart.merchantName">
        <span class="merchant-name">{{ cartStore.cart.merchantName }}</span>
      </div>

      <!-- 商品列表 -->
      <div class="cart-items">
        <div
          v-for="item in cartStore.cart.items"
          :key="item.dishId"
          class="cart-item"
        >
          <div class="item-info">
            <h4 class="item-name">{{ item.dishName }}</h4>
            <span class="item-price">¥{{ item.price }}</span>
          </div>
          <div class="item-actions">
            <van-stepper
              v-model="item.quantity"
              :min="1"
              :max="99"
              integer
              @change="onQuantityChange(item, $event)"
            />
            <span class="item-subtotal">¥{{ (item.price * item.quantity).toFixed(2) }}</span>
          </div>
          <van-button
            class="item-remove"
            icon="delete-o"
            size="small"
            type="danger"
            plain
            @click="onRemove(item)"
          />
        </div>
      </div>

      <!-- 底部结算栏 -->
      <div class="cart-footer" v-if="!cartStore.isEmpty">
        <div class="footer-left">
          <van-button size="small" plain type="danger" @click="onClear">清空</van-button>
        </div>
        <div class="footer-right">
          <span class="footer-total">合计 ¥{{ cartStore.cart.totalAmount.toFixed(2) }}</span>
          <van-button type="primary" round size="small" @click="onCheckout">
            去结算({{ cartStore.itemCount }})
          </van-button>
        </div>
      </div>
    </template>

    <van-loading v-if="cartStore.loading" class="loading-center" type="spinner" />
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showNotify, showConfirmDialog } from 'vant'
import { useCartStore } from '@/stores/cart'
import type { CartItem } from '@/api/cart'

const router = useRouter()
const cartStore = useCartStore()

const onQuantityChange = async (item: CartItem, qty: number | string) => {
  const q = typeof qty === 'string' ? parseInt(qty) : qty
  if (isNaN(q) || q < 1) return
  try {
    await cartStore.update(item.dishId, q)
  } catch {
    // interceptor handles
  }
}

const onRemove = async (item: CartItem) => {
  try {
    await showConfirmDialog({ title: '提示', message: '确定移除该商品？' })
    await cartStore.remove(item.dishId)
    showNotify({ type: 'success', message: '已移除' })
  } catch {
    // cancelled or error
  }
}

const onClear = async () => {
  try {
    await showConfirmDialog({ title: '提示', message: '确定清空购物车？' })
    await cartStore.clear()
    showNotify({ type: 'success', message: '已清空' })
  } catch {
    // cancelled or error
  }
}

const onCheckout = () => {
  router.push('/checkout')
}

onMounted(() => {
  cartStore.fetch()
})
</script>

<style scoped lang="scss">
.cart-page {
  min-height: 100vh;
  background: #f7f8fa;
  padding-bottom: 70px;
}

.merchant-bar {
  padding: 12px 16px;
  background: #fff;
  border-bottom: 1px solid #ebedf0;
  display: flex;
  align-items: center;

  .merchant-name {
    font-size: 15px;
    font-weight: 600;
    color: #323233;
  }
}

.cart-items {
  padding: 0 16px;
}

.cart-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 14px 0;
  background: #fff;
  border-bottom: 1px solid #f5f5f5;
}

.item-info {
  flex: 1;

  .item-name {
    font-size: 15px;
    font-weight: 500;
    color: #323233;
    margin-bottom: 4px;
  }

  .item-price {
    font-size: 13px;
    color: #ff6b35;
  }
}

.item-actions {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 6px;
}

.item-subtotal {
  font-size: 14px;
  font-weight: 600;
  color: #ff6b35;
}

.item-remove {
  margin-left: 4px;
}

.cart-footer {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: #fff;
  padding: 10px 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 -2px 8px rgba(0, 0, 0, 0.06);
  z-index: 10;
}

.footer-left {
  flex-shrink: 0;
}

.footer-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.footer-total {
  font-size: 16px;
  font-weight: 700;
  color: #323233;
}

.loading-center {
  display: flex;
  justify-content: center;
  padding-top: 100px;
}
</style>
