<template>
  <div class="profile-page">
    <van-nav-bar title="个人中心" />

    <!-- 用户信息 -->
    <div class="user-card" v-if="userStore.isLoggedIn() && userStore.user">
      <div class="user-avatar">
        <van-image
          v-if="userStore.user.avatar"
          :src="userStore.user.avatar"
          width="60"
          height="60"
          fit="cover"
          round
        />
        <div v-else class="avatar-placeholder">
          {{ (userStore.user.nickname || userStore.user.phone).charAt(0) }}
        </div>
      </div>
      <div class="user-info">
        <h3>{{ userStore.user.nickname || '用户' }}</h3>
        <p>{{ userStore.user.phone }}</p>
      </div>
    </div>

    <div v-else class="user-card user-card-login" @click="$router.push('/login')">
      <van-icon name="contact-o" size="48" color="#c8c9cc" />
      <p class="login-hint">点击登录</p>
    </div>

    <!-- 功能入口 -->
    <van-cell-group inset class="menu-group">
      <van-cell title="我的订单" icon="records-o" is-link to="/orders" />
      <van-cell title="收货地址" icon="location-o" is-link to="/addresses" />
      <van-cell title="购物车" icon="cart-o" is-link to="/cart" />
    </van-cell-group>

    <!-- 退出登录 -->
    <div class="logout-wrap" v-if="userStore.isLoggedIn()">
      <van-button block round type="danger" plain @click="onLogout">退出登录</van-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showConfirmDialog } from 'vant'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const onLogout = async () => {
  try {
    await showConfirmDialog({ title: '提示', message: '确定退出登录？' })
    userStore.logout()
    router.replace('/')
  } catch {
    // cancelled
  }
}

onMounted(() => {
  if (userStore.isLoggedIn() && !userStore.user) {
    userStore.fetchUser()
  }
})
</script>

<style scoped lang="scss">
.profile-page {
  min-height: 100vh;
  background: #f7f8fa;
}

.user-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 24px 20px;
  background: #fff;
  margin-bottom: 12px;
}

.user-card-login {
  justify-content: center;
  flex-direction: column;
  cursor: pointer;
}

.login-hint {
  margin-top: 8px;
  font-size: 14px;
  color: #969799;
}

.user-avatar {
  flex-shrink: 0;
}

.avatar-placeholder {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background: linear-gradient(135deg, #ff6b35, #f7c948);
  color: #fff;
  font-size: 26px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
}

.user-info {
  h3 {
    font-size: 18px;
    font-weight: 600;
    color: #323233;
    margin-bottom: 4px;
  }

  p {
    font-size: 13px;
    color: #969799;
  }
}

.menu-group {
  margin-bottom: 20px;
}

.logout-wrap {
  padding: 0 20px;
}
</style>
