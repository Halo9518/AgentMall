package com.agentmall.module.order.service;

import com.agentmall.common.PageResult;
import com.agentmall.module.order.dto.CreateOrderDTO;
import com.agentmall.module.order.vo.OrderVO;

/**
 * 订单 Service
 */
public interface OrderService {

    /**
     * 下单 — 从购物车创建订单
     */
    OrderVO createOrder(Long userId, CreateOrderDTO dto);

    /**
     * 用户订单列表（分页）
     */
    PageResult<OrderVO> listUserOrders(Long userId, int page, int pageSize);

    /**
     * 用户订单详情
     */
    OrderVO getOrderDetail(Long userId, Long orderId);

    /**
     * 用户取消订单（仅 PENDING 状态）
     */
    void cancelOrder(Long userId, Long orderId);

    /**
     * 商家订单列表（分页）
     */
    PageResult<OrderVO> listMerchantOrders(Long merchantId, int page, int pageSize, String status);

    /**
     * 商家订单详情
     */
    OrderVO getMerchantOrderDetail(Long merchantId, Long orderId);

    /**
     * 商家更新订单状态
     */
    void updateOrderStatus(Long merchantId, Long orderId, String status);
}
