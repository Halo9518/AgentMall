<template>
  <div class="home-page">
    <div class="home-header">
      <h1>AgentMall</h1>
      <p>发现附近美食</p>
    </div>

    <div class="merchant-list">
      <div
        v-for="m in merchants"
        :key="m.id"
        class="merchant-card"
        @click="$router.push(`/merchant/${m.id}`)"
      >
        <div class="merchant-logo">
          <van-image
            v-if="m.logo"
            :src="m.logo"
            width="64"
            height="64"
            fit="cover"
            round
          />
          <div v-else class="merchant-logo-placeholder">
            {{ m.name.charAt(0) }}
          </div>
        </div>
        <div class="merchant-info">
          <h3 class="merchant-name">{{ m.name }}</h3>
          <p class="merchant-desc">{{ m.description || '暂无简介' }}</p>
          <div class="merchant-meta">
            <span class="merchant-hours">⏰ {{ m.openingHours }}</span>
            <span class="merchant-min">起送 ¥{{ m.minAmount }}</span>
          </div>
        </div>
      </div>

      <van-empty v-if="!loading && merchants.length === 0" description="暂无商家" />
    </div>

    <van-loading v-if="loading" class="loading-center" type="spinner" />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getMerchants } from '@/api/merchant'
import type { Merchant } from '@/api/merchant'

const merchants = ref<Merchant[]>([])
const loading = ref(true)

onMounted(async () => {
  try {
    merchants.value = await getMerchants()
  } catch {
    // error handled by interceptor
  } finally {
    loading.value = false
  }
})
</script>

<style scoped lang="scss">
.home-page {
  min-height: 100vh;
  background: #f7f8fa;
}

.home-header {
  background: linear-gradient(135deg, #ff6b35 0%, #f7c948 100%);
  padding: 40px 20px 30px;
  color: #fff;

  h1 {
    font-size: 28px;
    margin-bottom: 4px;
  }

  p {
    font-size: 14px;
    opacity: 0.9;
  }
}

.merchant-list {
  padding: 12px 16px;
}

.merchant-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  margin-bottom: 12px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.merchant-logo-placeholder {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  background: linear-gradient(135deg, #ff6b35, #f7c948);
  color: #fff;
  font-size: 28px;
  font-weight: bold;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.merchant-info {
  flex: 1;
  overflow: hidden;
}

.merchant-name {
  font-size: 16px;
  font-weight: 600;
  color: #323233;
  margin-bottom: 4px;
}

.merchant-desc {
  font-size: 12px;
  color: #969799;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-bottom: 6px;
}

.merchant-meta {
  display: flex;
  gap: 12px;
  font-size: 12px;
  color: #969799;
}

.merchant-min {
  color: #ff6b35;
}

.loading-center {
  display: flex;
  justify-content: center;
  padding-top: 60px;
}
</style>
