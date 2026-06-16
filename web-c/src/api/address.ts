import request from './request'

export interface AddressVO {
  id: number
  userId: number
  contactName: string
  phone: string
  province: string
  city: string
  district: string
  detail: string
  isDefault: number
  createdAt: string
}

export interface AddressDTO {
  contactName: string
  phone: string
  province: string
  city: string
  district?: string
  detail: string
}

/** 地址列表 */
export function getAddresses(): Promise<AddressVO[]> {
  return request.get('/addresses').then((res) => res.data)
}

/** 添加地址 */
export function addAddress(data: AddressDTO): Promise<AddressVO> {
  return request.post('/addresses', data).then((res) => res.data)
}

/** 修改地址 */
export function updateAddress(id: number, data: AddressDTO): Promise<void> {
  return request.put(`/addresses/${id}`, data)
}

/** 删除地址 */
export function deleteAddress(id: number): Promise<void> {
  return request.delete(`/addresses/${id}`)
}

/** 设为默认 */
export function setDefaultAddress(id: number): Promise<void> {
  return request.put(`/addresses/${id}/default`)
}
