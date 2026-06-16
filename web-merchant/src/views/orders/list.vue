<template>
  <div class="orders-page">
    <el-card>
      <template #header>
        <div class="header-row">
          <span>订单管理</span>
          <el-select v-model="filterStatus" placeholder="全部状态" clearable style="width:140px" @change="fetchOrders">
            <el-option label="待接单" value="PENDING" />
            <el-option label="已接单" value="ACCEPTED" />
            <el-option label="已拒绝" value="REJECTED" />
            <el-option label="配送中" value="DELIVERING" />
            <el-option label="已完成" value="COMPLETED" />
            <el-option label="已取消" value="CANCELLED" />
          </el-select>
        </div>
      </template>

      <el-table :data="orders" v-loading="loading" stripe>
        <el-table-column prop="orderNo" label="订单号" width="180" />
        <el-table-column prop="addressSnap" label="收货信息" min-width="180">
          <template #default="{ row }">
            <template v-if="getAddr(row.addressSnap)">
              <div>{{ getAddr(row.addressSnap).contactName }} {{ getAddr(row.addressSnap).phone }}</div>
              <div class="addr-detail">
                {{ getAddr(row.addressSnap).province }}{{ getAddr(row.addressSnap).city
                }}{{ getAddr(row.addressSnap).district }} {{ getAddr(row.addressSnap).detail }}
              </div>
            </template>
          </template>
        </el-table-column>
        <el-table-column label="商品" min-width="200">
          <template #default="{ row }">
            <span v-if="row.items && row.items.length">
              {{ row.items.map((i: any) => i.dishName + '×' + i.quantity).join('，') }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="payAmount" label="金额" width="100" align="right">
          <template #default="{ row }">¥{{ row.payAmount.toFixed(2) }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 'PENDING'"
              type="success" size="small" @click="handleStatus(row, 'ACCEPTED')"
            >接单</el-button>
            <el-button
              v-if="row.status === 'PENDING'"
              type="danger" size="small" @click="handleStatus(row, 'REJECTED')"
            >拒绝</el-button>
            <el-button
              v-if="row.status === 'ACCEPTED'"
              type="primary" size="small" @click="handleStatus(row, 'DELIVERING')"
            >配送</el-button>
            <el-button
              v-if="row.status === 'DELIVERING'"
              type="success" size="small" @click="handleStatus(row, 'COMPLETED')"
            >完成</el-button>
            <el-button size="small" @click="showDetail(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap" v-if="total > pageSize">
        <el-pagination
          v-model:current-page="page"
          :page-size="pageSize"
          :total="total"
          layout="total, prev, pager, next"
          @current-change="fetchOrders"
        />
      </div>
    </el-card>

    <!-- 订单详情弹窗 -->
    <el-dialog v-model="detailVisible" title="订单详情" width="600px">
      <template v-if="detail">
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="订单号">{{ detail.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="statusType(detail.status)" size="small">{{ statusLabel(detail.status) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="收货人">{{ detailAddr.contactName }}</el-descriptions-item>
          <el-descriptions-item label="电话">{{ detailAddr.phone }}</el-descriptions-item>
          <el-descriptions-item label="地址" :span="2">
            {{ detailAddr.province }}{{ detailAddr.city }}{{ detailAddr.district }} {{ detailAddr.detail }}
          </el-descriptions-item>
          <el-descriptions-item label="商品小计">¥{{ detail.totalAmount.toFixed(2) }}</el-descriptions-item>
          <el-descriptions-item label="配送费">¥{{ detail.deliveryFee.toFixed(2) }}</el-descriptions-item>
          <el-descriptions-item label="实付金额">
            <strong style="color:#f56c6c">¥{{ detail.payAmount.toFixed(2) }}</strong>
          </el-descriptions-item>
          <el-descriptions-item label="备注">{{ detail.remark || '-' }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ detail.createdAt }}</el-descriptions-item>
        </el-descriptions>

        <h4 style="margin:16px 0 8px">商品明细</h4>
        <el-table :data="detail.items" size="small" border>
          <el-table-column prop="dishName" label="菜品" />
          <el-table-column prop="price" label="单价" width="80">
            <template #default="{ row }">¥{{ row.price.toFixed(2) }}</template>
          </el-table-column>
          <el-table-column prop="quantity" label="数量" width="60" />
          <el-table-column prop="amount" label="小计" width="100">
            <template #default="{ row }">¥{{ row.amount.toFixed(2) }}</template>
          </el-table-column>
        </el-table>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getMerchantOrders,
  getMerchantOrderDetail,
  updateOrderStatus,
  type OrderVO
} from '@/api/order'

const loading = ref(false)
const orders = ref<OrderVO[]>([])
const page = ref(1)
const pageSize = 10
const total = ref(0)
const filterStatus = ref('')

const detailVisible = ref(false)
const detail = ref<OrderVO | null>(null)

const statusLabel = (s: string) => {
  const map: Record<string, string> = {
    PENDING: '待接单', ACCEPTED: '已接单', REJECTED: '已拒绝',
    DELIVERING: '配送中', COMPLETED: '已完成', CANCELLED: '已取消'
  }
  return map[s] || s
}

const statusType = (s: string) => {
  const map: Record<string, string> = {
    PENDING: 'warning', ACCEPTED: 'success', REJECTED: 'danger',
    DELIVERING: '', COMPLETED: 'success', CANCELLED: 'info'
  }
  return map[s] || 'info'
}

const parseAddr = (snap: string) => {
  try { return JSON.parse(snap) } catch { return null }
}

const addrCache = new Map<string, any>()
const getAddr = (snap: string) => {
  if (!snap) return null
  if (addrCache.has(snap)) return addrCache.get(snap)
  const parsed = parseAddr(snap)
  addrCache.set(snap, parsed)
  return parsed
}

const detailAddr = computed(() => parseAddr(detail.value?.addressSnap || '{}'))

const fetchOrders = async () => {
  loading.value = true
  try {
    const params: any = { page: page.value, pageSize }
    if (filterStatus.value) params.status = filterStatus.value
    const res = await getMerchantOrders(params)
    orders.value = res.records
    total.value = res.total
  } finally {
    loading.value = false
  }
}

const handleStatus = async (row: OrderVO, status: string) => {
  const label = statusLabel(status)
  try {
    await ElMessageBox.confirm(`确定${label}该订单？`, '提示', { type: 'warning' })
    await updateOrderStatus(row.id, status)
    ElMessage.success(`已${label}`)
    fetchOrders()
  } catch {
    // cancelled or error
  }
}

const showDetail = async (row: OrderVO) => {
  try {
    detail.value = await getMerchantOrderDetail(row.id)
    detailVisible.value = true
  } catch {
    ElMessage.error('获取详情失败')
  }
}

onMounted(fetchOrders)
</script>

<style scoped lang="scss">
.orders-page { padding: 20px; }
.header-row { display: flex; justify-content: space-between; align-items: center; }
.addr-detail { font-size: 12px; color: #999; }
.pagination-wrap { display: flex; justify-content: center; margin-top: 20px; }
</style>
