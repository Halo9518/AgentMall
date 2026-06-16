package com.agentmall.security;

import com.agentmall.common.exception.BusinessException;
import com.agentmall.module.merchant.entity.Merchant;
import com.agentmall.module.merchant.service.MerchantService;
import com.agentmall.module.user.entity.User;
import org.springframework.stereotype.Component;

/**
 * 商家上下文工具 — 替代各 Controller 重复的 getCurrentMerchantId()
 */
@Component
public class MerchantContext {

    private final MerchantService merchantService;

    public MerchantContext(MerchantService merchantService) {
        this.merchantService = merchantService;
    }

    public Long getCurrentMerchantId(User user) {
        Merchant merchant = merchantService.getByUserId(user.getId());
        if (merchant == null) {
            throw new BusinessException("商家不存在");
        }
        return merchant.getId();
    }
}
