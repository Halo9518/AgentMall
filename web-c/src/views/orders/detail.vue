<template>
  <div class="order-detail-page">
    <van-nav-bar title="订单详情" left-arrow @click-left="$router.back()" />

    <van-loading v-if="loading" class="loading-center" type="spinner" />

    <template v-else-if="order">
      <!-- 状态 -->
      <div class="status-bar" :class="`bg-${order.status.toLowerCase()}`">
        <span class="status-text">{{ statusLabel(order.status) }}</span>
      </div>

      <!-- 地址 -->
      <div class="section">
        <h3 class="section-title">收货地址</h3>
        <div class="card">
          <p><strong>{{ addr.contactName }}</strong> {{ addr.phone }}</p>
          <p class="addr-detail">{{ addr.province }}{{ addr.city }}{{ addr.district }} {{ addr.detail }}</p>
        </div>
      </div>

      <!-- 商家 & 商品 -->
      <div class="section">
        <h3 class="section-title">{{ order.merchantName }}</h3>
        <div class="card">
          <div v-for="item in order.items" :key="item.id" class="item-row">
            <span class="item-name">{{ item.dishName }}</span>
            <span class="item-qty">×{{ item.quantity }}</span>
            <span class="item-amount">¥{{ item.amount.toFixed(2) }}</span>
          </div>
        </div>
      </div>

      <!-- 金额 -->
      <div class="section">
        <div class="card">
          <div class="amount-row">
            <span>商品小计</span><span>¥{{ order.totalAmount.toFixed(2) }}</span>
          </div>
          <div class="amount-row">
            <span>配送费</span><span>¥{{ order.deliveryFee.toFixed(2) }}</span>
          </div>
          <div class="amount-row total">
            <span>实付金额</span><span class="price">¥{{ order.payAmount.toFixed(2) }}</span>
          </div>
        </div>
      </div>

      <!-- 订单信息 -->
      <div class="section">
        <div class="card">
          <div class="info-row"><span>订单编号</span><span class="mono">{{ order.orderNo }}</span></div>
          <div class="info-row"><span>创建时间</span><span>{{ order.createdAt }}</span></div>
          <div class="info-row" v-if="order.payTime"><span>支付时间</span><span>{{ order.payTime }}</span></div>
          <div class="info-row" v-if="order.remark"><span>备注</span><span>{{ order.remark }}</span></div>
        </div>
      </div>

      <!-- 取消按钮（仅PENDING） -->
      <div class="action-bar" v-if="order.status === 'PENDING'">
        <van-button type="danger" block round :loading="cancelling" @click="onCancel">
          取消订单
        </van-button>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showNotify, showConfirmDialog } from 'vant'
import { getOrderDetail, cancelOrder, type OrderVO } from '@/api/order'

const route = useRoute()
const router = useRouter()

const loading = ref(true)
const cancelling = ref(false)
const order = ref<OrderVO | null>(null)

const addr = computed(() => {
  try {
    return JSON.parse(order.value?.addressSnap || '{}')
  } catch {
    return {}
  }
})

const statusLabel = (s: string) => {
  const map: Record<string, string> = {
    PENDING: '待接单',
    ACCEPTED: '已接单',
    REJECTED: '已拒绝',
    DELIVERING: '配送中',
    COMPLETED: '已完成',
    CANCELLED: '已取消'
  }
  return map[s] || s
}

const onCancel = async () => {
  if (!order.value) return
  try {
    await showConfirmDialog({ title: '确认取消', message: '确定要取消该订单吗？' })
    cancelling.value = true
    await cancelOrder(order.value.id)
    order.value.status = 'CANCELLED'
    showNotify({ type: 'success', message: '订单已取消' })
  } catch {
    // cancelled or error
  } finally {
    cancelling.value = false
  }
}

onMounted(async () => {
  const id = Number(route.params.id)
  if (!id) {
    router.replace('/orders')
    return
  }
  try {
    order.value = await getOrderDetail(id)
  } finally {
    loading.value = false
  }
})
</script>

<style scoped lang="scss">
.order-detail-page {
  min-height: 100vh;
  background: #f7f8fa;
  padding-bottom: 30px;
}

.loading-center {
  display: flex;
  justify-content: center;
  padding-top: 100px;
}

.status-bar {
  padding: 16px;
  text-align: center;

  .status-text { font-size: 18px; font-weight: 700; color: #fff; }

  &.bg-pending { background: linear-gradient(135deg, #ff976a, #ff6b35); }
  &.bg-accepted, &.bg-completed { background: linear-gradient(135deg, #07c160, #06ad56); }
  &.bg-rejected, &.bg-cancelled { background: linear-gradient(135deg, #999, #666); }
  &.bg-delivering { background: linear-gradient(135deg, #1989fa, #1676d3); }
}

.section {
  margin: 12px 16px;
}

.section-title {
  font-size: 14px;
  font-weight: 600;
  color: #646566;
  padding-bottom: 8px;
}

.card {
  background: #fff;
  border-radius: 8px;
  padding: 12px 16px;
}

.addr-detail { color: #969799; font-size: 13px; margin-top: 4px; }

.item-row {
  display: flex;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px solid #f5f5f5;

  &:last-child { border-bottom: none; }

  .item-name { flex: 1; font-size: 14px; }
  .item-qty { color: #999; margin-right: 12px; font-size: 13px; }
  .item-amount { font-weight: 600; font-size: 14px; }
}

.amount-row {
  display: flex;
  justify-content: space-between;
  padding: 6px 0;
  font-size: 14px;

  &.total { font-weight: 700; font-size: 16px; border-top: 1px solid #f5f5f5; padding-top: 10px; margin-top: 4px; }

  .price { color: #ff6b35; }
}

.info-row {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
  font-size: 13px;
  color: #646566;

  .mono { font-family: monospace; font-size: 12px; }
}

.action-bar {
  padding: 20px 16px;
}
</style>
