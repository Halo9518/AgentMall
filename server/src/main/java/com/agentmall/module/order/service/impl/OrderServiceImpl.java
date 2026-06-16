package com.agentmall.module.order.service.impl;

import cn.hutool.json.JSONUtil;
import com.agentmall.common.PageResult;
import com.agentmall.common.exception.BusinessException;
import com.agentmall.module.cart.vo.CartItemVO;
import com.agentmall.module.cart.vo.CartVO;
import com.agentmall.module.cart.service.CartService;
import com.agentmall.module.merchant.entity.Merchant;
import com.agentmall.module.merchant.service.MerchantService;
import com.agentmall.module.order.dto.CreateOrderDTO;
import com.agentmall.module.order.entity.OrderItem;
import com.agentmall.module.order.entity.Orders;
import com.agentmall.module.order.mapper.OrderItemMapper;
import com.agentmall.module.order.mapper.OrderMapper;
import com.agentmall.module.order.service.OrderService;
import com.agentmall.module.order.vo.OrderItemVO;
import com.agentmall.module.order.vo.OrderVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 订单 Service 实现
 */
@Service
public class OrderServiceImpl implements OrderService {

    private static final DateTimeFormatter ORDER_NO_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final CartService cartService;
    private final MerchantService merchantService;

    public OrderServiceImpl(OrderMapper orderMapper, OrderItemMapper orderItemMapper,
                            CartService cartService, MerchantService merchantService) {
        this.orderMapper = orderMapper;
        this.orderItemMapper = orderItemMapper;
        this.cartService = cartService;
        this.merchantService = merchantService;
    }

    private String generateOrderNo() {
        String timestamp = LocalDateTime.now().format(ORDER_NO_FORMAT);
        String random = String.format("%04d", ThreadLocalRandom.current().nextInt(10000));
        return timestamp + random;
    }

    @Override
    @Transactional
    public OrderVO createOrder(Long userId, CreateOrderDTO dto) {
        // 获取购物车
        CartVO cart = cartService.getCart(userId);
        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new BusinessException("购物车为空，无法下单");
        }

        // 验证商家状态
        Merchant merchant = merchantService.getById(cart.getMerchantId());
        if (merchant == null || merchant.getStatus() == 0) {
            throw new BusinessException("商家已歇业，无法下单");
        }

        // 计算金额
        BigDecimal totalAmount = cart.getTotalAmount();
        BigDecimal deliveryFee = merchant.getDeliveryFee() != null ? merchant.getDeliveryFee() : BigDecimal.ZERO;
        BigDecimal payAmount = totalAmount.add(deliveryFee);

        // 创建订单
        Orders order = new Orders();
        order.setOrderNo(generateOrderNo());
        order.setUserId(userId);
        order.setMerchantId(cart.getMerchantId());
        order.setAddressSnap(dto.getAddressSnap());
        order.setTotalAmount(totalAmount);
        order.setDeliveryFee(deliveryFee);
        order.setPayAmount(payAmount);
        order.setStatus("PENDING");
        order.setRemark(dto.getRemark());
        orderMapper.insert(order);

        // 构建并批量插入订单明细（快照）
        Long orderId = order.getId();
        List<OrderItem> items = new ArrayList<>();
        for (CartItemVO cartItem : cart.getItems()) {
            OrderItem item = new OrderItem();
            item.setOrderId(orderId);
            item.setDishId(cartItem.getDishId());
            item.setDishName(cartItem.getDishName());
            item.setDishImage(cartItem.getDishImage());
            item.setPrice(cartItem.getPrice());
            item.setQuantity(cartItem.getQuantity());
            item.setAmount(cartItem.getSubtotal());
            items.add(item);
        }
        // 批量插入
        items.forEach(orderItemMapper::insert);

        // 清空购物车
        cartService.clearCart(userId);

        return toVO(order, items);
    }

    @Override
    public PageResult<OrderVO> listUserOrders(Long userId, int page, int pageSize) {
        IPage<Orders> p = orderMapper.selectPage(
                new Page<>(page, pageSize),
                new LambdaQueryWrapper<Orders>()
                        .eq(Orders::getUserId, userId)
                        .orderByDesc(Orders::getCreatedAt));
        List<Orders> orderList = p.getRecords();
        // 批量加载商家名称（N+1 → 1）
        Map<Long, String> merchantNames = resolveMerchantNames(orderList);
        List<OrderVO> records = orderList.stream().map(o -> {
            OrderVO vo = OrderVO.fromEntity(o);
            vo.setMerchantName(merchantNames.getOrDefault(o.getMerchantId(), ""));
            return vo;
        }).toList();
        return new PageResult<>(records, p.getCurrent(), p.getSize(), p.getTotal());
    }

    @Override
    public OrderVO getOrderDetail(Long userId, Long orderId) {
        Orders order = orderMapper.selectOne(
                new LambdaQueryWrapper<Orders>()
                        .eq(Orders::getId, orderId)
                        .eq(Orders::getUserId, userId));
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        return toVO(order, getOrderItems(orderId));
    }

    @Override
    @Transactional
    public void cancelOrder(Long userId, Long orderId) {
        Orders order = orderMapper.selectOne(
                new LambdaQueryWrapper<Orders>()
                        .eq(Orders::getId, orderId)
                        .eq(Orders::getUserId, userId));
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (!"PENDING".equals(order.getStatus())) {
            throw new BusinessException("仅待处理状态的订单可取消");
        }
        order.setStatus("CANCELLED");
        orderMapper.updateById(order);
    }

    @Override
    public PageResult<OrderVO> listMerchantOrders(Long merchantId, int page, int pageSize, String status) {
        LambdaQueryWrapper<Orders> qw = new LambdaQueryWrapper<Orders>()
                .eq(Orders::getMerchantId, merchantId)
                .orderByDesc(Orders::getCreatedAt);
        if (status != null && !status.isEmpty()) {
            qw.eq(Orders::getStatus, status);
        }
        IPage<Orders> p = orderMapper.selectPage(new Page<>(page, pageSize), qw);
        String merchantName = getMerchantName(merchantId);
        List<OrderVO> records = p.getRecords().stream().map(o -> {
            OrderVO vo = OrderVO.fromEntity(o);
            vo.setMerchantName(merchantName);
            return vo;
        }).toList();
        return new PageResult<>(records, p.getCurrent(), p.getSize(), p.getTotal());
    }

    @Override
    public OrderVO getMerchantOrderDetail(Long merchantId, Long orderId) {
        Orders order = orderMapper.selectOne(
                new LambdaQueryWrapper<Orders>()
                        .eq(Orders::getId, orderId)
                        .eq(Orders::getMerchantId, merchantId));
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        return toVO(order, getOrderItems(orderId));
    }

    @Override
    @Transactional
    public void updateOrderStatus(Long merchantId, Long orderId, String status) {
        Orders order = orderMapper.selectOne(
                new LambdaQueryWrapper<Orders>()
                        .eq(Orders::getId, orderId)
                        .eq(Orders::getMerchantId, merchantId));
        if (order == null) {
            throw new BusinessException("订单不存在");
        }

        String currentStatus = order.getStatus();
        switch (status) {
            case "ACCEPTED":
                if (!"PENDING".equals(currentStatus)) {
                    throw new BusinessException("仅待处理订单可接单");
                }
                break;
            case "REJECTED":
                if (!"PENDING".equals(currentStatus)) {
                    throw new BusinessException("仅待处理订单可拒绝");
                }
                break;
            case "DELIVERING":
                if (!"ACCEPTED".equals(currentStatus)) {
                    throw new BusinessException("仅已接单订单可配送");
                }
                break;
            case "COMPLETED":
                if (!"DELIVERING".equals(currentStatus)) {
                    throw new BusinessException("仅配送中订单可完成");
                }
                order.setPayTime(LocalDateTime.now());
                break;
            default:
                throw new BusinessException("无效的状态: " + status);
        }
        order.setStatus(status);
        orderMapper.updateById(order);
    }

    // ========== private helpers ==========

    private List<OrderItem> getOrderItems(Long orderId) {
        return orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, orderId));
    }

    private OrderVO toVO(Orders order, List<OrderItem> items) {
        OrderVO vo = OrderVO.fromEntity(order);
        vo.setMerchantName(getMerchantName(order.getMerchantId()));
        vo.setItems(items.stream().map(OrderItemVO::fromEntity).toList());
        return vo;
    }

    /** 单次查询商家名称 */
    private String getMerchantName(Long merchantId) {
        Merchant merchant = merchantService.getById(merchantId);
        return merchant != null ? merchant.getName() : "";
    }

    /** 批量查询商家名称（避免 N+1） */
    private Map<Long, String> resolveMerchantNames(List<Orders> orders) {
        return merchantService.lambdaQuery()
                .in(Merchant::getId,
                    orders.stream().map(Orders::getMerchantId).distinct().collect(Collectors.toList()))
                .list()
                .stream()
                .collect(Collectors.toMap(Merchant::getId, Merchant::getName));
    }
}
