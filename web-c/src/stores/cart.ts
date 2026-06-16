import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getCart, addToCart, updateQuantity, removeItem, clearCart, type CartVO, type CartItem } from '@/api/cart'

export const useCartStore = defineStore('cart', () => {
  const cart = ref<CartVO>({ merchantId: null, merchantName: null, items: [], totalAmount: 0, deliveryFee: 0 })
  const loading = ref(false)

  const itemCount = computed(() => cart.value.items.reduce((s, i) => s + i.quantity, 0))
  const isEmpty = computed(() => cart.value.items.length === 0)

  async function fetch() {
    loading.value = true
    try {
      cart.value = await getCart()
    } catch {
      // 未登录或空购物车
      cart.value = { merchantId: null, merchantName: null, items: [], totalAmount: 0 }
    } finally {
      loading.value = false
    }
  }

  async function add(dishId: number, quantity?: number) {
    await addToCart(dishId, quantity)
    await fetch()
  }

  async function update(dishId: number, quantity: number) {
    await updateQuantity(dishId, quantity)
    await fetch()
  }

  async function remove(dishId: number) {
    await removeItem(dishId)
    await fetch()
  }

  async function clear() {
    await clearCart()
    cart.value = { merchantId: null, merchantName: null, items: [], totalAmount: 0 }
  }

  return { cart, loading, itemCount, isEmpty, fetch, add, update, remove, clear }
})
