package com.agentmall.module.merchant.entity;

import com.agentmall.common.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

/**
 * 商家实体
 */
@Schema(description = "商家")
@TableName("merchant")
public class Merchant extends BaseEntity {

    @Schema(description = "关联用户ID")
    private Long userId;

    @Schema(description = "店铺名称")
    private String name;

    @Schema(description = "店铺Logo")
    private String logo;

    @Schema(description = "联系电话")
    private String phone;

    @Schema(description = "店铺地址")
    private String address;

    @Schema(description = "店铺简介")
    private String description;

    @Schema(description = "营业时间")
    private String openingHours;

    @Schema(description = "状态: 1营业 0歇业")
    private Integer status;

    @Schema(description = "起送金额")
    private BigDecimal minAmount;

    @Schema(description = "配送费")
    private BigDecimal deliveryFee;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getOpeningHours() {
        return openingHours;
    }

    public void setOpeningHours(String openingHours) {
        this.openingHours = openingHours;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public BigDecimal getMinAmount() {
        return minAmount;
    }

    public void setMinAmount(BigDecimal minAmount) {
        this.minAmount = minAmount;
    }

    public BigDecimal getDeliveryFee() {
        return deliveryFee;
    }

    public void setDeliveryFee(BigDecimal deliveryFee) {
        this.deliveryFee = deliveryFee;
    }
}
