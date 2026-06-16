import request from './request'

export interface Overview {
  todayCount: number
  todayAmount: number
  weekCount: number
  weekAmount: number
  monthCount: number
  monthAmount: number
  pendingCount: number
}

export interface TrendItem {
  date: string
  count: number
  amount: number
}

export interface TopDish {
  dishId: number
  dishName: string
  totalQty: number
  totalAmount: number
}

/** 概览数据 */
export function getOverview(): Promise<Overview> {
  return request.get('/merchant/statistics/overview').then((res) => res.data)
}

/** 趋势数据 */
export function getTrend(days?: number): Promise<TrendItem[]> {
  return request.get('/merchant/statistics/trend', { params: { days } }).then((res) => res.data)
}

/** 热销 Top */
export function getTopDishes(limit?: number): Promise<TopDish[]> {
  return request.get('/merchant/statistics/top-dishes', { params: { limit } }).then((res) => res.data)
}
