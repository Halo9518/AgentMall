import request from './request'

export interface CartItem {
  dishId: number
  dishName: string
  dishImage: string
  price: number
  quantity: number
  merchantId: number
  merchantName: string
  subtotal: number
}

export interface CartVO {
  merchantId: number | null
  merchantName: string | null
  items: CartItem[]
  totalAmount: number
}

/** 获取购物车 */
export function getCart(): Promise<CartVO> {
  return request.get('/cart').then((res) => res.data)
}

/** 添加到购物车 */
export function addToCart(dishId: number, quantity?: number): Promise<void> {
  return request.post('/cart', { dishId, quantity: quantity || 1 })
}

/** 修改数量 */
export function updateQuantity(dishId: number, quantity: number): Promise<void> {
  return request.put(`/cart/${dishId}`, null, { params: { quantity } })
}

/** 删除单项 */
export function removeItem(dishId: number): Promise<void> {
  return request.delete(`/cart/${dishId}`)
}

/** 清空购物车 */
export function clearCart(): Promise<void> {
  return request.delete('/cart')
}
