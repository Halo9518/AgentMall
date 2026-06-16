package com.agentmall.module.cart.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.List;

/**
 * 购物车响应 VO
 */
@Schema(description = "购物车")
public class CartVO {

    @Schema(description = "商家ID")
    private Long merchantId;

    @Schema(description = "商家名称")
    private String merchantName;

    @Schema(description = "商品列表")
    private List<CartItemVO> items;

    @Schema(description = "总金额")
    private BigDecimal totalAmount;

    @Schema(description = "配送费")
    private BigDecimal deliveryFee;

    public Long getMerchantId() { return merchantId; }
    public void setMerchantId(Long merchantId) { this.merchantId = merchantId; }
    public String getMerchantName() { return merchantName; }
    public void setMerchantName(String merchantName) { this.merchantName = merchantName; }
    public List<CartItemVO> getItems() { return items; }
    public void setItems(List<CartItemVO> items) { this.items = items; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public BigDecimal getDeliveryFee() { return deliveryFee; }
    public void setDeliveryFee(BigDecimal deliveryFee) { this.deliveryFee = deliveryFee; }
}
