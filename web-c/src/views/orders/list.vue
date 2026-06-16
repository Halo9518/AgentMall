<template>
  <div class="orders-page">
    <van-nav-bar title="我的订单" left-arrow @click-left="$router.back()" />

    <van-loading v-if="loading" class="loading-center" type="spinner" />

    <van-empty v-else-if="orders.length === 0" description="暂无订单">
      <van-button round type="primary" to="/">去下单</van-button>
    </van-empty>

    <template v-else>
      <div class="order-list">
        <div
          v-for="order in orders"
          :key="order.id"
          class="order-card"
          @click="$router.push(`/orders/${order.id}`)"
        >
          <div class="order-header">
            <span class="merchant-name">{{ order.merchantName }}</span>
            <span class="order-status" :class="statusClass(order.status)">
              {{ statusLabel(order.status) }}
            </span>
          </div>
          <div class="order-body">
            <div class="order-items-preview">
              {{ itemsPreview(order) }}
            </div>
            <div class="order-footer">
              <span class="order-no">{{ order.orderNo }}</span>
              <span class="order-amount">¥{{ order.payAmount.toFixed(2) }}</span>
            </div>
          </div>
        </div>
      </div>

      <div class="pagination" v-if="pages > 1">
        <van-pagination
          v-model="currentPage"
          :total-items="total"
          :page-size="pageSize"
          mode="simple"
          @change="fetchOrders"
        />
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getOrders, type OrderVO } from '@/api/order'

const loading = ref(true)
const orders = ref<OrderVO[]>([])
const currentPage = ref(1)
const pageSize = 10
const total = ref(0)
const pages = ref(0)

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

const statusClass = (s: string) => `status-${s.toLowerCase()}`

const itemsPreview = (order: OrderVO) => {
  return order.items && order.items.length
    ? order.items.map(i => i.dishName).join('、')
    : '订单详情'
}

const fetchOrders = async () => {
  loading.value = true
  try {
    const res = await getOrders(currentPage.value, pageSize)
    orders.value = res.records
    total.value = res.total
    pages.value = res.pages
  } finally {
    loading.value = false
  }
}

onMounted(fetchOrders)
</script>

<style scoped lang="scss">
.orders-page {
  min-height: 100vh;
  background: #f7f8fa;
  padding-bottom: 20px;
}

.loading-center {
  display: flex;
  justify-content: center;
  padding-top: 100px;
}

.order-list {
  padding: 12px;
}

.order-card {
  background: #fff;
  border-radius: 8px;
  margin-bottom: 12px;
  overflow: hidden;

  .order-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 12px 16px;
    border-bottom: 1px solid #f5f5f5;
  }

  .merchant-name { font-weight: 600; font-size: 15px; }
  .order-status { font-size: 13px; font-weight: 500; }

  .status-pending { color: #ff976a; }
  .status-accepted { color: #07c160; }
  .status-rejected { color: #ee0a24; }
  .status-delivering { color: #1989fa; }
  .status-completed { color: #07c160; }
  .status-cancelled { color: #999; }

  .order-body {
    padding: 10px 16px 12px;
  }

  .order-items-preview {
    font-size: 13px;
    color: #646566;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    margin-bottom: 8px;
  }

  .order-footer {
    display: flex;
    justify-content: space-between;
    font-size: 12px;
    color: #999;

    .order-amount {
      font-size: 16px;
      font-weight: 700;
      color: #ff6b35;
    }
  }
}

.pagination {
  display: flex;
  justify-content: center;
  padding: 16px 0;
}
</style>
