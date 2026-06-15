import request from './request'

export interface LoginParams {
  phone: string
  password: string
  role: string
}

export interface UserVO {
  id: number
  phone: string
  nickname: string
  avatar: string
  role: string
}

export interface LoginVO {
  token: string
  user: UserVO
}

/** 登录 */
export function login(params: LoginParams): Promise<LoginVO> {
  return request.post('/auth/login', params).then((res) => res.data)
}

/** 获取当前用户信息 */
export function getUserInfo(): Promise<UserVO> {
  return request.get('/auth/info').then((res) => res.data)
}

/** 修改个人信息 */
export function updateUserInfo(data: { nickname?: string; avatar?: string }): Promise<void> {
  return request.put('/auth/info', data)
}

/** 修改密码 */
export function updatePassword(data: { oldPassword: string; newPassword: string }): Promise<void> {
  return request.put('/auth/password', data)
}
