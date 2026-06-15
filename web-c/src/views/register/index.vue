<template>
  <div class="register-page">
    <van-nav-bar title="注册" left-text="返回" left-arrow @click-left="$router.back()" />

    <van-form @submit="onSubmit" class="register-form">
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
          v-model="form.nickname"
          name="nickname"
          label="昵称"
          placeholder="请输入昵称（选填）"
          maxlength="16"
        />
        <van-field
          v-model="form.password"
          name="password"
          label="密码"
          placeholder="请输入密码（6-20位）"
          type="password"
          :rules="[{ required: true, message: '请输入密码' }, { min: 6, message: '密码至少6位' }]"
        />
        <van-field
          v-model="form.confirmPassword"
          name="confirmPassword"
          label="确认密码"
          placeholder="请再次输入密码"
          type="password"
          :rules="[
            { required: true, message: '请确认密码' },
            { validator: () => form.password === form.confirmPassword, message: '两次密码不一致' }
          ]"
        />
      </van-cell-group>

      <div class="register-btn-wrapper">
        <van-button round block type="primary" native-type="submit" :loading="loading">
          注册
        </van-button>
      </div>

      <div class="register-footer">
        <router-link to="/login">已有账号？去登录</router-link>
      </div>
    </van-form>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { showNotify } from 'vant'
import { register } from '@/api/auth'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)

const form = reactive({
  phone: '',
  nickname: '',
  password: '',
  confirmPassword: ''
})

const onSubmit = async () => {
  if (form.password !== form.confirmPassword) {
    showNotify({ type: 'danger', message: '两次密码不一致' })
    return
  }

  loading.value = true
  try {
    const data = await register({
      phone: form.phone,
      password: form.password,
      nickname: form.nickname || undefined,
      role: 'CUSTOMER'
    })
    userStore.loginSuccess(data)
    showNotify({ type: 'success', message: '注册成功' })
    router.replace('/')
  } catch {
    // 错误已在拦截器中处理
  } finally {
    loading.value = false
  }
}
</script>

<style scoped lang="scss">
.register-page {
  min-height: 100vh;
  background: #fff;
}

.register-form {
  padding: 16px 0;
}

.register-btn-wrapper {
  padding: 24px 16px 0;
}

.register-footer {
  text-align: center;
  padding: 24px 0;

  a {
    font-size: 14px;
    color: var(--van-primary-color);
  }
}
</style>
