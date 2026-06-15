<template>
  <div class="login-page">
    <div class="login-header">
      <h1>AgentMall</h1>
      <p>外卖点餐，美味直达</p>
    </div>

    <van-form @submit="onSubmit" class="login-form">
      <van-cell-group inset>
        <van-field
          v-model="form.phone"
          name="phone"
          label="手机号"
          placeholder="请输入手机号"
          type="tel"
          maxlength="11"
          :rules="[{ required: true, message: '请输入手机号' }, { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确' }]"
        />
        <van-field
          v-model="form.password"
          name="password"
          label="密码"
          placeholder="请输入密码"
          type="password"
          :rules="[{ required: true, message: '请输入密码' }]"
        />
      </van-cell-group>

      <div class="login-btn-wrapper">
        <van-button round block type="primary" native-type="submit" :loading="loading">
          登录
        </van-button>
      </div>

      <div class="login-footer">
        <router-link to="/register">没有账号？去注册</router-link>
      </div>
    </van-form>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { showNotify } from 'vant'
import { login } from '@/api/auth'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const loading = ref(false)

const form = reactive({
  phone: '',
  password: ''
})

const onSubmit = async () => {
  loading.value = true
  try {
    const data = await login({ ...form, role: 'CUSTOMER' })
    userStore.loginSuccess(data)
    showNotify({ type: 'success', message: '登录成功' })

    // 跳转到之前想访问的页面，或首页
    const redirect = (route.query.redirect as string) || '/'
    router.replace(redirect)
  } catch {
    // 错误已在拦截器中处理
  } finally {
    loading.value = false
  }
}
</script>

<style scoped lang="scss">
.login-page {
  min-height: 100vh;
  background: #fff;
}

.login-header {
  padding: 60px 0 40px;
  text-align: center;

  h1 {
    font-size: 32px;
    color: var(--van-primary-color);
    margin-bottom: 8px;
  }

  p {
    font-size: 14px;
    color: #999;
  }
}

.login-form {
  padding: 0 16px;
}

.login-btn-wrapper {
  padding: 24px 16px 0;
}

.login-footer {
  text-align: center;
  padding: 24px 0;

  a {
    font-size: 14px;
    color: var(--van-primary-color);
  }
}
</style>
