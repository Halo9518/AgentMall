package com.agentmall.module.order.controller;

import com.agentmall.common.PageResult;
import com.agentmall.common.Result;
import com.agentmall.module.order.service.OrderService;
import com.agentmall.module.order.vo.OrderVO;
import com.agentmall.module.user.entity.User;
import com.agentmall.security.CurrentUser;
import com.agentmall.security.MerchantContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 商家端 — 订单管理控制器
 */
@Tag(name = "商家端-订单管理", description = "商家订单查询、接单/拒绝/配送/完成")
@RestController
@RequestMapping("/api/merchant/orders")
public class OrderManageController {

    private final OrderService orderService;
    private final MerchantContext merchantContext;

    public OrderManageController(OrderService orderService, MerchantContext merchantContext) {
        this.orderService = orderService;
        this.merchantContext = merchantContext;
    }

    @Operation(summary = "订单列表")
    @GetMapping
    public Result<PageResult<OrderVO>> list(
            @CurrentUser User user,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String status) {
        Long merchantId = merchantContext.getCurrentMerchantId(user);
        return Result.success(orderService.listMerchantOrders(merchantId, page, pageSize, status));
    }

    @Operation(summary = "订单详情")
    @GetMapping("/{id}")
    public Result<OrderVO> detail(@CurrentUser User user, @PathVariable Long id) {
        Long merchantId = merchantContext.getCurrentMerchantId(user);
        return Result.success(orderService.getMerchantOrderDetail(merchantId, id));
    }

    @Operation(summary = "更新订单状态（ACCEPTED/REJECTED/DELIVERING/COMPLETED）")
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(
            @CurrentUser User user,
            @PathVariable Long id,
            @RequestParam String status) {
        Long merchantId = merchantContext.getCurrentMerchantId(user);
        orderService.updateOrderStatus(merchantId, id, status);
        return Result.success();
    }
}
