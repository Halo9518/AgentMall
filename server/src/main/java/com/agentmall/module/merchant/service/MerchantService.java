package com.agentmall.module.merchant.service;

import com.agentmall.module.merchant.entity.Merchant;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 商家 Service
 */
public interface MerchantService extends IService<Merchant> {

    /**
     * 根据userId查询商家
     */
    Merchant getByUserId(Long userId);
}
