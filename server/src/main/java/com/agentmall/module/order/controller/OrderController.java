package com.agentmall.module.order.controller;

import com.agentmall.common.PageResult;
import com.agentmall.common.Result;
import com.agentmall.module.order.dto.CreateOrderDTO;
import com.agentmall.module.order.service.OrderService;
import com.agentmall.module.order.vo.OrderVO;
import com.agentmall.module.user.entity.User;
import com.agentmall.security.CurrentUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * C端 — 订单控制器
 */
@Tag(name = "C端-订单", description = "C端下单、订单查询、取消")
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @Operation(summary = "下单")
    @PostMapping
    public Result<OrderVO> create(@CurrentUser User user, @Valid @RequestBody CreateOrderDTO dto) {
        return Result.success(orderService.createOrder(user.getId(), dto));
    }

    @Operation(summary = "我的订单列表")
    @GetMapping
    public Result<PageResult<OrderVO>> list(
            @CurrentUser User user,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(orderService.listUserOrders(user.getId(), page, pageSize));
    }

    @Operation(summary = "订单详情")
    @GetMapping("/{id}")
    public Result<OrderVO> detail(@CurrentUser User user, @PathVariable Long id) {
        return Result.success(orderService.getOrderDetail(user.getId(), id));
    }

    @Operation(summary = "取消订单（仅PENDING状态）")
    @PutMapping("/{id}/cancel")
    public Result<Void> cancel(@CurrentUser User user, @PathVariable Long id) {
        orderService.cancelOrder(user.getId(), id);
        return Result.success();
    }
}
