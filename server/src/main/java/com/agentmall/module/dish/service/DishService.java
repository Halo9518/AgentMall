package com.agentmall.module.dish.service;

import com.agentmall.module.dish.entity.Dish;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 菜品 Service
 */
public interface DishService extends IService<Dish> {

    /**
     * 分页查询商家菜品，可按分类筛选
     */
    IPage<Dish> pageByMerchant(Long merchantId, Long categoryId, int page, int pageSize);

    /**
     * 查询商家上架菜品列表（C端用）
     */
    List<Dish> listByMerchant(Long merchantId, Long categoryId);
}
