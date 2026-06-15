import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'
import { useUserStore } from '@/stores/user'
import 'vant/lib/index.css'
import './styles/global.scss'

const app = createApp(App)
app.use(createPinia())
app.use(router)

// 恢复登录态（token 还在 localStorage 中但 store 为空）
const userStore = useUserStore()
userStore.fetchUser()

app.mount('#app')
