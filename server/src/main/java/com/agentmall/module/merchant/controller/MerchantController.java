package com.agentmall.module.merchant.controller;

import com.agentmall.common.Result;
import com.agentmall.module.merchant.entity.Merchant;
import com.agentmall.module.merchant.service.MerchantService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * C端 — 商家控制器
 */
@Tag(name = "C端-商家", description = "商家列表、详情")
@RestController
@RequestMapping("/api/merchants")
public class MerchantController {

    private final MerchantService merchantService;

    public MerchantController(MerchantService merchantService) {
        this.merchantService = merchantService;
    }

    @Operation(summary = "商家列表")
    @GetMapping
    public Result<List<Merchant>> list() {
        List<Merchant> list = merchantService.list(
                new LambdaQueryWrapper<Merchant>()
                        .eq(Merchant::getStatus, 1)
                        .orderByDesc(Merchant::getCreatedAt)
        );
        return Result.success(list);
    }

    @Operation(summary = "商家详情")
    @GetMapping("/{id}")
    public Result<Merchant> detail(@PathVariable Long id) {
        Merchant merchant = merchantService.getById(id);
        return Result.success(merchant);
    }
}
