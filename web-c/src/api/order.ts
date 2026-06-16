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
  addressSnap: string  // JSON string
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

export interface CreateOrderDTO {
  addressSnap: string
  remark?: string
}

/** 下单 */
export function createOrder(data: CreateOrderDTO): Promise<OrderVO> {
  return request.post('/orders', data).then((res) => res.data)
}

/** 我的订单列表 */
export function getOrders(page = 1, pageSize = 10): Promise<PageResult<OrderVO>> {
  return request.get('/orders', { params: { page, pageSize } }).then((res) => res.data)
}

/** 订单详情 */
export function getOrderDetail(id: number): Promise<OrderVO> {
  return request.get(`/orders/${id}`).then((res) => res.data)
}

/** 取消订单 */
export function cancelOrder(id: number): Promise<void> {
  return request.put(`/orders/${id}/cancel`)
}
