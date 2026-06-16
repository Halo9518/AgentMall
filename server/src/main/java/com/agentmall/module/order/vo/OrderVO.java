package com.agentmall.module.order.vo;

import com.agentmall.module.order.entity.Orders;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单 VO
 */
@Schema(description = "订单")
public class OrderVO {

    @Schema(description = "订单ID")
    private Long id;

    @Schema(description = "订单编号")
    private String orderNo;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "商家ID")
    private Long merchantId;

    @Schema(description = "商家名称")
    private String merchantName;

    @Schema(description = "地址快照(JSON)")
    private String addressSnap;

    @Schema(description = "商品总金额")
    private BigDecimal totalAmount;

    @Schema(description = "配送费")
    private BigDecimal deliveryFee;

    @Schema(description = "实付金额")
    private BigDecimal payAmount;

    @Schema(description = "状态")
    private String status;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "支付时间")
    private LocalDateTime payTime;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "订单明细")
    private List<OrderItemVO> items;

    public static OrderVO fromEntity(Orders order) {
        OrderVO vo = new OrderVO();
        vo.setId(order.getId());
        vo.setOrderNo(order.getOrderNo());
        vo.setUserId(order.getUserId());
        vo.setMerchantId(order.getMerchantId());
        vo.setAddressSnap(order.getAddressSnap());
        vo.setTotalAmount(order.getTotalAmount());
        vo.setDeliveryFee(order.getDeliveryFee());
        vo.setPayAmount(order.getPayAmount());
        vo.setStatus(order.getStatus());
        vo.setRemark(order.getRemark());
        vo.setPayTime(order.getPayTime());
        vo.setCreatedAt(order.getCreatedAt());
        return vo;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getMerchantId() { return merchantId; }
    public void setMerchantId(Long merchantId) { this.merchantId = merchantId; }
    public String getMerchantName() { return merchantName; }
    public void setMerchantName(String merchantName) { this.merchantName = merchantName; }
    public String getAddressSnap() { return addressSnap; }
    public void setAddressSnap(String addressSnap) { this.addressSnap = addressSnap; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public BigDecimal getDeliveryFee() { return deliveryFee; }
    public void setDeliveryFee(BigDecimal deliveryFee) { this.deliveryFee = deliveryFee; }
    public BigDecimal getPayAmount() { return payAmount; }
    public void setPayAmount(BigDecimal payAmount) { this.payAmount = payAmount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public LocalDateTime getPayTime() { return payTime; }
    public void setPayTime(LocalDateTime payTime) { this.payTime = payTime; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public List<OrderItemVO> getItems() { return items; }
    public void setItems(List<OrderItemVO> items) { this.items = items; }
}
