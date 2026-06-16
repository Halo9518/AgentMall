package com.agentmall.module.address.service;

import com.agentmall.module.address.entity.Address;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 收货地址 Service
 */
public interface AddressService extends IService<Address> {

    /** 用户地址列表 */
    List<Address> listByUserId(Long userId);

    /** 添加地址 */
    Address addAddress(Long userId, Address address);

    /** 修改地址 */
    void updateAddress(Long userId, Long addressId, Address address);

    /** 删除地址 */
    void deleteAddress(Long userId, Long addressId);

    /** 设为默认 */
    void setDefault(Long userId, Long addressId);
}
