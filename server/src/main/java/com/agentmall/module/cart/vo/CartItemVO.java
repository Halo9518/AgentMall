package com.agentmall.module.cart.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

/**
 * 购物车单项 VO（存储在 Redis Hash 中）
 */
@Schema(description = "购物车单项")
public class CartItemVO {

    @Schema(description = "菜品ID")
    private Long dishId;

    @Schema(description = "菜品名称")
    private String dishName;

    @Schema(description = "图片URL")
    private String dishImage;

    @Schema(description = "单价")
    private BigDecimal price;

    @Schema(description = "数量")
    private Integer quantity;

    @Schema(description = "商家ID")
    private Long merchantId;

    @Schema(description = "商家名称")
    private String merchantName;

    public CartItemVO() {}

    public CartItemVO(Long dishId, String dishName, String dishImage, BigDecimal price,
                      Integer quantity, Long merchantId, String merchantName) {
        this.dishId = dishId;
        this.dishName = dishName;
        this.dishImage = dishImage;
        this.price = price;
        this.quantity = quantity;
        this.merchantId = merchantId;
        this.merchantName = merchantName;
    }

    public Long getDishId() { return dishId; }
    public void setDishId(Long dishId) { this.dishId = dishId; }
    public String getDishName() { return dishName; }
    public void setDishName(String dishName) { this.dishName = dishName; }
    public String getDishImage() { return dishImage; }
    public void setDishImage(String dishImage) { this.dishImage = dishImage; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public Long getMerchantId() { return merchantId; }
    public void setMerchantId(Long merchantId) { this.merchantId = merchantId; }
    public String getMerchantName() { return merchantName; }
    public void setMerchantName(String merchantName) { this.merchantName = merchantName; }

    /** 小计 */
    public BigDecimal getSubtotal() {
        return price.multiply(BigDecimal.valueOf(quantity));
    }
}
