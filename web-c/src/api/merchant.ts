import request from './request'

export interface Merchant {
  id: number
  userId: number
  name: string
  logo: string
  phone: string
  address: string
  description: string
  openingHours: string
  status: number
  minAmount: number
  deliveryFee: number
}

export interface Category {
  id: number
  merchantId: number
  name: string
  sortOrder: number
}

export interface DishVO {
  id: number
  merchantId: number
  categoryId: number
  name: string
  image: string
  price: number
  description: string
  salesCount: number
  status: number
}

/** 商家列表 */
export function getMerchants(): Promise<Merchant[]> {
  return request.get('/merchants').then((res) => res.data)
}

/** 商家详情 */
export function getMerchantDetail(id: number): Promise<Merchant> {
  return request.get(`/merchants/${id}`).then((res) => res.data)
}

/** 商家分类列表 */
export function getCategories(merchantId: number): Promise<Category[]> {
  return request.get(`/merchants/${merchantId}/categories`).then((res) => res.data)
}

/** 商家菜品列表 */
export function getDishes(merchantId: number, categoryId?: number): Promise<DishVO[]> {
  return request.get(`/merchants/${merchantId}/dishes`, { params: { categoryId } }).then((res) => res.data)
}
