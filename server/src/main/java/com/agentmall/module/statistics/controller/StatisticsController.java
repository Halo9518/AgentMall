package com.agentmall.module.statistics.controller;

import com.agentmall.common.Result;
import com.agentmall.common.exception.BusinessException;
import com.agentmall.module.merchant.entity.Merchant;
import com.agentmall.module.merchant.service.MerchantService;
import com.agentmall.module.statistics.service.StatisticsService;
import com.agentmall.module.user.entity.User;
import com.agentmall.security.CurrentUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 商家端 — 数据统计控制器
 */
@Tag(name = "商家端-数据统计", description = "Dashboard 数据概览、趋势、热销")
@RestController
@RequestMapping("/api/merchant/statistics")
public class StatisticsController {

    private final StatisticsService statisticsService;
    private final MerchantService merchantService;

    public StatisticsController(StatisticsService statisticsService, MerchantService merchantService) {
        this.statisticsService = statisticsService;
        this.merchantService = merchantService;
    }

    private Long getCurrentMerchantId(User user) {
        Merchant merchant = merchantService.getByUserId(user.getId());
        if (merchant == null) {
            throw new BusinessException("商家不存在");
        }
        return merchant.getId();
    }

    @Operation(summary = "今日/周/月概览")
    @GetMapping("/overview")
    public Result<Map<String, Object>> overview(@CurrentUser User user) {
        Long merchantId = getCurrentMerchantId(user);
        return Result.success(statisticsService.getOverview(merchantId));
    }

    @Operation(summary = "近N天趋势")
    @GetMapping("/trend")
    public Result<List<Map<String, Object>>> trend(
            @CurrentUser User user,
            @RequestParam(defaultValue = "7") int days) {
        Long merchantId = getCurrentMerchantId(user);
        return Result.success(statisticsService.getTrend(merchantId, days));
    }

    @Operation(summary = "热销菜品 Top N")
    @GetMapping("/top-dishes")
    public Result<List<Map<String, Object>>> topDishes(
            @CurrentUser User user,
            @RequestParam(defaultValue = "10") int limit) {
        Long merchantId = getCurrentMerchantId(user);
        return Result.success(statisticsService.getTopDishes(merchantId, limit));
    }
}
