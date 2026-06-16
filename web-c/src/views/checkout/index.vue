<template>
  <div class="checkout-page">
    <van-nav-bar title="确认下单" left-arrow @click-left="$router.back()" />

    <template v-if="cartStore.isEmpty && !submitting">
      <van-empty description="购物车是空的">
        <van-button round type="primary" to="/cart">返回购物车</van-button>
      </van-empty>
    </template>

    <template v-else>
      <!-- 地址信息 -->
      <div class="section">
        <h3 class="section-title">收货地址</h3>

        <!-- 已保存地址列表 -->
        <div v-if="savedAddresses.length > 0" class="saved-addresses">
          <van-radio-group v-model="selectedAddrId" @change="onAddressSelect">
            <div
              v-for="addr in savedAddresses"
              :key="addr.id"
              class="saved-addr-item"
              :class="{ selected: selectedAddrId === addr.id }"
            >
              <van-radio :name="addr.id" checked-color="#07c160">
                <div class="addr-info">
                  <span class="addr-name">{{ addr.contactName }} {{ addr.phone }}</span>
                  <span class="addr-detail">{{ addr.province }}{{ addr.city }}{{ addr.district }} {{ addr.detail }}</span>
                </div>
              </van-radio>
            </div>
          </van-radio-group>
          <van-divider>或手动填写</van-divider>
        </div>

        <van-cell-group inset>
          <van-field v-model="form.contactName" label="联系人" placeholder="请输入联系人" required />
          <van-field v-model="form.phone" label="手机号" placeholder="请输入手机号" type="tel" required />
          <van-field v-model="form.province" label="省份" placeholder="省份" required />
          <van-field v-model="form.city" label="城市" placeholder="城市" required />
          <van-field v-model="form.district" label="区/县" placeholder="区/县" />
          <van-field v-model="form.detail" label="详细地址" placeholder="街道/门牌号" required />
        </van-cell-group>
      </div>

      <!-- 订单商品 -->
      <div class="section">
        <h3 class="section-title">{{ cartStore.cart.merchantName }}</h3>
        <div class="order-items">
          <div v-for="item in cartStore.cart.items" :key="item.dishId" class="order-item">
            <span class="item-name">{{ item.dishName }}</span>
            <span class="item-qty">×{{ item.quantity }}</span>
            <span class="item-amount">¥{{ (item.price * item.quantity).toFixed(2) }}</span>
          </div>
        </div>
      </div>

      <!-- 备注 -->
      <div class="section">
        <van-field v-model="form.remark" label="备注" placeholder="选填" />
      </div>

      <!-- 金额明细 -->
      <div class="section">
        <div class="amount-row">
          <span>商品小计</span>
          <span>¥{{ cartStore.cart.totalAmount.toFixed(2) }}</span>
        </div>
        <div class="amount-row">
          <span>配送费</span>
          <span>¥{{ deliveryFee.toFixed(2) }}</span>
        </div>
        <div class="amount-row total">
          <span>合计</span>
          <span class="total-price">¥{{ payAmount.toFixed(2) }}</span>
        </div>
      </div>

      <!-- 提交按钮 -->
      <div class="submit-bar">
        <span class="total-label">实付 <strong>¥{{ payAmount.toFixed(2) }}</strong></span>
        <van-button type="primary" round :loading="submitting" @click="onSubmit">
          提交订单
        </van-button>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showNotify, showDialog } from 'vant'
import { useCartStore } from '@/stores/cart'
import { createOrder } from '@/api/order'
import { getAddresses, type AddressVO } from '@/api/address'

const router = useRouter()
const cartStore = useCartStore()

const submitting = ref(false)
const savedAddresses = ref<AddressVO[]>([])
const selectedAddrId = ref<number | null>(null)

// 配送费来自购物车（后端已填充实际值）
const deliveryFee = computed(() => {
  if (cartStore.cart.isEmpty) return 0
  return cartStore.cart.deliveryFee ?? 0
})

const payAmount = computed(() => {
  return cartStore.cart.totalAmount + deliveryFee.value
})

const form = ref({
  contactName: '',
  phone: '',
  province: '',
  city: '',
  district: '',
  detail: '',
  remark: ''
})

const onAddressSelect = (id: number | null) => {
  if (id == null) return
  const addr = savedAddresses.value.find(a => a.id === id)
  if (!addr) return
  selectedAddrId.value = id
  form.value.contactName = addr.contactName
  form.value.phone = addr.phone
  form.value.province = addr.province
  form.value.city = addr.city
  form.value.district = addr.district || ''
  form.value.detail = addr.detail
}
  selectedAddrId.value = addr.id
  form.value.contactName = addr.contactName
  form.value.phone = addr.phone
  form.value.province = addr.province
  form.value.city = addr.city
  form.value.district = addr.district || ''
  form.value.detail = addr.detail
}

const onSubmit = async () => {
  const { contactName, phone, province, city, detail } = form.value
  if (!contactName || !phone || !province || !city || !detail) {
    showNotify({ type: 'danger', message: '请填写完整的地址信息（联系人、手机号、省份、城市、详细地址）' })
    return
  }

  submitting.value = true
  try {
    const addressSnap = JSON.stringify({
      contactName: form.value.contactName,
      phone: form.value.phone,
      province: form.value.province,
      city: form.value.city,
      district: form.value.district,
      detail: form.value.detail
    })

    await createOrder({
      addressSnap,
      remark: form.value.remark
    })

    await showDialog({ title: '下单成功', message: '订单已提交，请等待商家接单' })
    router.replace('/orders')
  } catch {
    // interceptor handles
  } finally {
    submitting.value = false
  }
}

onMounted(async () => {
  cartStore.fetch()
  try {
    savedAddresses.value = await getAddresses()
  } catch {
    // ignore — address fetch is optional
  }
})
</script>

<style scoped lang="scss">
.checkout-page {
  min-height: 100vh;
  background: #f7f8fa;
  padding-bottom: 70px;
}

.section {
  margin: 12px 0;
}

.section-title {
  font-size: 14px;
  font-weight: 600;
  color: #646566;
  padding: 8px 16px;
}

.order-items {
  background: #fff;
  padding: 0 16px;
}

.order-item {
  display: flex;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px solid #f5f5f5;

  &:last-child { border-bottom: none; }

  .item-name { flex: 1; font-size: 14px; }
  .item-qty { color: #999; margin-right: 12px; font-size: 13px; }
  .item-amount { font-weight: 600; font-size: 14px; }
}

.amount-row {
  display: flex;
  justify-content: space-between;
  padding: 8px 16px;
  background: #fff;
  font-size: 14px;

  &.total {
    margin-top: 1px;
    font-weight: 600;
    font-size: 16px;
  }

  .total-price { color: #ff6b35; }
}

.submit-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: #fff;
  padding: 12px 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 -2px 8px rgba(0, 0, 0, 0.06);

  .total-label strong { color: #ff6b35; font-size: 18px; }
}

.saved-addresses {
  padding: 0 16px;
  margin-bottom: 8px;
}

.saved-addr-item {
  background: #fff;
  border-radius: 8px;
  padding: 12px;
  border: 1px solid #ebedf0;
  margin-bottom: 8px;

  &.selected { border-color: #07c160; background: #f0fff0; }
}

.addr-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
  margin-left: 6px;
}

.addr-name { font-size: 14px; font-weight: 500; }
.addr-detail { font-size: 12px; color: #969799; }
</style>
