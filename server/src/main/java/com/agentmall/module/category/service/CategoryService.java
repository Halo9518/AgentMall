package com.agentmall.module.category.service;

import com.agentmall.module.category.entity.Category;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 分类 Service
 */
public interface CategoryService extends IService<Category> {

    /**
     * 查询某商家的分类列表，按排序字段升序
     */
    List<Category> listByMerchantId(Long merchantId);
}
