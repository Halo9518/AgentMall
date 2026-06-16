<template>
  <div class="addresses-page">
    <van-nav-bar title="收货地址" left-arrow @click-left="$router.back()" />

    <van-loading v-if="loading" class="loading-center" type="spinner" />

    <template v-else>
      <!-- 地址列表 -->
      <div class="address-list" v-if="addresses.length > 0">
        <van-swipe-cell v-for="addr in addresses" :key="addr.id">
          <div class="address-card" :class="{ 'is-default': addr.isDefault === 1 }" @click="onSelect(addr)">
            <div class="card-top">
              <span class="contact">{{ addr.contactName }}</span>
              <span class="phone">{{ addr.phone }}</span>
              <van-tag v-if="addr.isDefault === 1" type="primary" size="small">默认</van-tag>
            </div>
            <div class="card-addr">
              {{ addr.province }}{{ addr.city }}{{ addr.district }} {{ addr.detail }}
            </div>
          </div>
          <template #right>
            <div class="swipe-actions">
              <van-button square type="primary" text="编辑" class="swipe-btn" @click="onEdit(addr)" />
              <van-button square type="danger" text="删除" class="swipe-btn" @click="onDelete(addr)" />
            </div>
          </template>
        </van-swipe-cell>
      </div>

      <van-empty v-else description="暂无收货地址" />

      <!-- 底部添加按钮 -->
      <div class="bottom-btn">
        <van-button type="primary" block round @click="showDialog = true">添加新地址</van-button>
      </div>
    </template>

    <!-- 添加/编辑弹窗 -->
    <van-dialog
      v-model:show="showDialog"
      :title="editingIndex >= 0 ? '编辑地址' : '添加地址'"
      show-cancel-button
      @confirm="onSubmit"
      @cancel="resetForm"
    >
      <div class="form-wrap">
        <van-field v-model="form.contactName" label="联系人" placeholder="联系人" required />
        <van-field v-model="form.phone" label="手机号" placeholder="手机号" type="tel" required />
        <van-field v-model="form.province" label="省份" placeholder="省份" required />
        <van-field v-model="form.city" label="城市" placeholder="城市" required />
        <van-field v-model="form.district" label="区/县" placeholder="区/县" />
        <van-field v-model="form.detail" label="详细地址" placeholder="街道/门牌号" required />
      </div>
    </van-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { showNotify, showConfirmDialog } from 'vant'
import {
  getAddresses,
  addAddress,
  updateAddress,
  deleteAddress,
  setDefaultAddress,
  type AddressVO
} from '@/api/address'

const loading = ref(true)
const addresses = ref<AddressVO[]>([])
const showDialog = ref(false)
const editingId = ref<number | null>(null) // null = add, number = edit
const fetchPending = ref(false)

const emptyForm = () => ({
  contactName: '',
  phone: '',
  province: '',
  city: '',
  district: '',
  detail: ''
})

const form = ref(emptyForm())

const fetchAddresses = async () => {
  if (fetchPending.value) return // prevent concurrent calls
  fetchPending.value = true
  try {
    addresses.value = await getAddresses()
  } finally {
    loading.value = false
    fetchPending.value = false
  }
}

const resetForm = () => {
  form.value = emptyForm()
  editingId.value = null
}

const onSelect = async (addr: AddressVO) => {
  if (addr.isDefault === 1) return
  try {
    await setDefaultAddress(addr.id)
    fetchAddresses()
    showNotify({ type: 'success', message: '已设为默认地址' })
  } catch {
    // interceptor handles
  }
}

const onEdit = (addr: AddressVO) => {
  editingId.value = addr.id
  form.value = {
    contactName: addr.contactName,
    phone: addr.phone,
    province: addr.province,
    city: addr.city,
    district: addr.district,
    detail: addr.detail
  }
  showDialog.value = true
}

const onDelete = async (addr: AddressVO) => {
  try {
    await showConfirmDialog({ title: '确认删除', message: `确定删除${addr.contactName}的地址？` })
    await deleteAddress(addr.id)
    fetchAddresses()
    showNotify({ type: 'success', message: '已删除' })
  } catch {
    // cancelled or error
  }
}

const onSubmit = async () => {
  const { contactName, phone, province, city, detail } = form.value
  if (!contactName || !phone || !province || !city || !detail) {
    showNotify({ type: 'danger', message: '请填写完整信息' })
    return
  }

  try {
    if (editingId.value != null) {
      await updateAddress(editingId.value, form.value)
    } else {
      await addAddress(form.value)
    }
    resetForm()
    fetchAddresses()
    showNotify({ type: 'success', message: editingId.value != null ? '已修改' : '已添加' })
  } catch {
    // interceptor handles
  }
}

onMounted(fetchAddresses)
</script>

<style scoped lang="scss">
.addresses-page {
  min-height: 100vh;
  background: #f7f8fa;
  padding-bottom: 80px;
}

.loading-center {
  display: flex;
  justify-content: center;
  padding-top: 100px;
}

.address-list {
  padding: 12px 16px;
}

.address-card {
  background: #fff;
  border-radius: 8px;
  padding: 14px 16px;
  margin-bottom: 12px;

  &.is-default {
    border: 1px solid #1989fa;
  }
}

.card-top {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;

  .contact { font-size: 15px; font-weight: 600; }
  .phone { color: #969799; font-size: 13px; flex: 1; }
}

.card-addr {
  font-size: 13px;
  color: #646566;
}

.swipe-actions {
  display: flex;
  height: 100%;
}

.swipe-btn {
  height: 100%;
  width: 64px;
}

.bottom-btn {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 12px 16px;
  background: #fff;
  box-shadow: 0 -2px 8px rgba(0, 0, 0, 0.06);
}

.form-wrap {
  padding: 8px 0;
}
</style>
