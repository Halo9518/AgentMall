package com.agentmall.module.category.entity;

import com.agentmall.common.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 分类实体
 */
@Schema(description = "菜品分类")
@TableName("category")
public class Category extends BaseEntity {

    @Schema(description = "商家ID")
    private Long merchantId;

    @Schema(description = "分类名称")
    private String name;

    @Schema(description = "排序")
    private Integer sortOrder;

    public Long getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(Long merchantId) {
        this.merchantId = merchantId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }
}
