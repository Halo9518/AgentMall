package com.agentmall.module.merchant.service.impl;

import com.agentmall.module.merchant.entity.Merchant;
import com.agentmall.module.merchant.mapper.MerchantMapper;
import com.agentmall.module.merchant.service.MerchantService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * 商家 Service 实现
 */
@Service
public class MerchantServiceImpl extends ServiceImpl<MerchantMapper, Merchant> implements MerchantService {

    @Override
    public Merchant getByUserId(Long userId) {
        return getOne(new LambdaQueryWrapper<Merchant>().eq(Merchant::getUserId, userId));
    }
}
