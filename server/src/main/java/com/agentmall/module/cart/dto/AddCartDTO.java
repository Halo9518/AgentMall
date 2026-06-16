package com.agentmall.module.cart.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * 添加购物车请求
 */
@Schema(description = "添加购物车")
public class AddCartDTO {

    @Schema(description = "菜品ID")
    @NotNull(message = "菜品ID不能为空")
    private Long dishId;

    @Schema(description = "数量")
    @Min(value = 1, message = "数量至少为1")
    private Integer quantity = 1;

    public Long getDishId() { return dishId; }
    public void setDishId(Long dishId) { this.dishId = dishId; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
}
