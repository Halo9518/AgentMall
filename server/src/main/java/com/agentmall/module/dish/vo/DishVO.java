package com.agentmall.module.dish.vo;

import com.agentmall.module.dish.entity.Dish;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

/**
 * 菜品 VO
 */
@Schema(description = "菜品")
public class DishVO {

    @Schema(description = "菜品ID")
    private Long id;

    @Schema(description = "商家ID")
    private Long merchantId;

    @Schema(description = "分类ID")
    private Long categoryId;

    @Schema(description = "菜品名称")
    private String name;

    @Schema(description = "图片URL")
    private String image;

    @Schema(description = "价格")
    private BigDecimal price;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "销量")
    private Integer salesCount;

    @Schema(description = "状态: 1上架 0下架")
    private Integer status;

    public static DishVO fromEntity(Dish dish) {
        DishVO vo = new DishVO();
        vo.setId(dish.getId());
        vo.setMerchantId(dish.getMerchantId());
        vo.setCategoryId(dish.getCategoryId());
        vo.setName(dish.getName());
        vo.setImage(dish.getImage());
        vo.setPrice(dish.getPrice());
        vo.setDescription(dish.getDescription());
        vo.setSalesCount(dish.getSalesCount());
        vo.setStatus(dish.getStatus());
        return vo;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(Long merchantId) {
        this.merchantId = merchantId;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getSalesCount() {
        return salesCount;
    }

    public void setSalesCount(Integer salesCount) {
        this.salesCount = salesCount;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
