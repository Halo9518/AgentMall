import request from './request'

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

export interface PageResult<T> {
  records: T[]
  page: number
  pageSize: number
  total: number
  pages: number
}

export interface DishForm {
  categoryId: number
  name: string
  image?: string
  price: number
  description?: string
}

// ============ 分类管理 ============

/** 分类列表 */
export function getCategories(): Promise<Category[]> {
  return request.get('/merchant/categories').then((res) => res.data)
}

/** 添加分类 */
export function createCategory(data: { name: string; sortOrder?: number }): Promise<Category> {
  return request.post('/merchant/categories', data).then((res) => res.data)
}

/** 修改分类 */
export function updateCategory(id: number, data: { name: string; sortOrder?: number }): Promise<void> {
  return request.put(`/merchant/categories/${id}`, data)
}

/** 删除分类 */
export function deleteCategory(id: number): Promise<void> {
  return request.delete(`/merchant/categories/${id}`)
}

// ============ 菜品管理 ============

/** 菜品列表（分页） */
export function getDishes(params: {
  page?: number
  pageSize?: number
  categoryId?: number
}): Promise<PageResult<DishVO>> {
  return request.get('/merchant/dishes', { params }).then((res) => res.data)
}

/** 添加菜品 */
export function createDish(data: DishForm): Promise<DishVO> {
  return request.post('/merchant/dishes', data).then((res) => res.data)
}

/** 修改菜品 */
export function updateDish(id: number, data: DishForm): Promise<void> {
  return request.put(`/merchant/dishes/${id}`, data)
}

/** 上下架 */
export function toggleDishStatus(id: number): Promise<void> {
  return request.put(`/merchant/dishes/${id}/status`)
}

/** 删除菜品 */
export function deleteDish(id: number): Promise<void> {
  return request.delete(`/merchant/dishes/${id}`)
}
