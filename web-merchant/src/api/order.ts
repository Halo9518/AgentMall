import request from './request'

export interface OrderItemVO {
  id: number
  dishId: number
  dishName: string
  dishImage: string
  price: number
  quantity: number
  amount: number
}

export interface OrderVO {
  id: number
  orderNo: string
  userId: number
  merchantId: number
  merchantName: string
  addressSnap: string
  totalAmount: number
  deliveryFee: number
  payAmount: number
  status: string
  remark: string
  payTime: string | null
  createdAt: string
  items: OrderItemVO[]
}

export interface PageResult<T> {
  records: T[]
  page: number
  pageSize: number
  total: number
  pages: number
}

/** 订单列表 */
export function getMerchantOrders(
  params: { page?: number; pageSize?: number; status?: string }
): Promise<PageResult<OrderVO>> {
  return request.get('/merchant/orders', { params }).then((res) => res.data)
}

/** 订单详情 */
export function getMerchantOrderDetail(id: number): Promise<OrderVO> {
  return request.get(`/merchant/orders/${id}`).then((res) => res.data)
}

/** 更新订单状态 */
export function updateOrderStatus(id: number, status: string): Promise<void> {
  return request.put(`/merchant/orders/${id}/status`, null, { params: { status } })
}
