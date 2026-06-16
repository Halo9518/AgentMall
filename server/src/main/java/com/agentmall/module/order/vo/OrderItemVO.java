package com.agentmall.module.order.vo;

import com.agentmall.module.order.entity.OrderItem;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

/**
 * 订单明细 VO
 */
@Schema(description = "订单明细")
public class OrderItemVO {

    @Schema(description = "主键")
    private Long id;

    @Schema(description = "菜品ID")
    private Long dishId;

    @Schema(description = "菜品名称")
    private String dishName;

    @Schema(description = "菜品图片")
    private String dishImage;

    @Schema(description = "单价")
    private BigDecimal price;

    @Schema(description = "数量")
    private Integer quantity;

    @Schema(description = "小计")
    private BigDecimal amount;

    public static OrderItemVO fromEntity(OrderItem item) {
        OrderItemVO vo = new OrderItemVO();
        vo.setId(item.getId());
        vo.setDishId(item.getDishId());
        vo.setDishName(item.getDishName());
        vo.setDishImage(item.getDishImage());
        vo.setPrice(item.getPrice());
        vo.setQuantity(item.getQuantity());
        vo.setAmount(item.getAmount());
        return vo;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
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
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
}
