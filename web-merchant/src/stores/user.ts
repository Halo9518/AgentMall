import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getUserInfo, type UserVO } from '@/api/auth'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('merchant_token') || '')
  const user = ref<UserVO | null>(null)

  /** 是否已登录 */
  const isLoggedIn = () => !!token.value

  /** 设置 token */
  function setToken(newToken: string) {
    token.value = newToken
    localStorage.setItem('merchant_token', newToken)
  }

  /** 设置用户信息 */
  function setUser(newUser: UserVO) {
    user.value = newUser
  }

  /** 登录后保存 */
  function loginSuccess(loginData: { token: string; user: UserVO }) {
    setToken(loginData.token)
    setUser(loginData.user)
  }

  /** 获取用户信息（用于刷新页面后恢复） */
  async function fetchUser() {
    if (!token.value) return
    try {
      const u = await getUserInfo()
      user.value = u
    } catch {
      logout()
    }
  }

  /** 退出登录 */
  function logout() {
    token.value = ''
    user.value = null
    localStorage.removeItem('merchant_token')
  }

  return { token, user, isLoggedIn, setToken, setUser, loginSuccess, fetchUser, logout }
})
