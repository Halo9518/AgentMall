package com.agentmall.module.address.service.impl;

import com.agentmall.common.exception.BusinessException;
import com.agentmall.module.address.entity.Address;
import com.agentmall.module.address.mapper.AddressMapper;
import com.agentmall.module.address.service.AddressService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 收货地址 Service 实现
 */
@Service
public class AddressServiceImpl extends ServiceImpl<AddressMapper, Address> implements AddressService {

    @Override
    public List<Address> listByUserId(Long userId) {
        return list(new LambdaQueryWrapper<Address>()
                .eq(Address::getUserId, userId)
                .orderByDesc(Address::getIsDefault)
                .orderByDesc(Address::getCreatedAt));
    }

    @Override
    @Transactional
    public Address addAddress(Long userId, Address address) {
        address.setUserId(userId);

        // 首个地址自动设为默认
        if (baseMapper.countByUserId(userId) == 0) {
            address.setIsDefault(1);
        } else {
            address.setIsDefault(0);
        }

        save(address);
        return address;
    }

    @Override
    public void updateAddress(Long userId, Long addressId, Address address) {
        Address exist = getByUserIdAndId(userId, addressId);

        exist.setContactName(address.getContactName());
        exist.setPhone(address.getPhone());
        exist.setProvince(address.getProvince());
        exist.setCity(address.getCity());
        exist.setDistrict(address.getDistrict());
        exist.setDetail(address.getDetail());

        updateById(exist);
    }

    @Override
    @Transactional
    public void deleteAddress(Long userId, Long addressId) {
        Address exist = getByUserIdAndId(userId, addressId);
        boolean wasDefault = exist.getIsDefault() == 1;

        removeById(addressId);

        // 如果删除的是默认地址，将最近创建的其他地址设为默认
        if (wasDefault) {
            List<Address> remaining = listByUserId(userId);
            if (!remaining.isEmpty()) {
                Address first = remaining.get(0);
                first.setIsDefault(1);
                updateById(first);
            }
        }
    }

    @Override
    @Transactional
    public void setDefault(Long userId, Long addressId) {
        Address exist = getByUserIdAndId(userId, addressId);

        // 清除所有默认
        baseMapper.clearDefault(userId);

        // 设为默认
        exist.setIsDefault(1);
        updateById(exist);
    }

    private Address getByUserIdAndId(Long userId, Long addressId) {
        Address exist = getOne(new LambdaQueryWrapper<Address>()
                .eq(Address::getId, addressId)
                .eq(Address::getUserId, userId));
        if (exist == null) {
            throw new BusinessException("地址不存在");
        }
        return exist;
    }
}
