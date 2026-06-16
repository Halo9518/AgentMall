package com.agentmall.module.dish.service.impl;

import com.agentmall.module.dish.entity.Dish;
import com.agentmall.module.dish.mapper.DishMapper;
import com.agentmall.module.dish.service.DishService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 菜品 Service 实现
 */
@Service
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {

    @Override
    public IPage<Dish> pageByMerchant(Long merchantId, Long categoryId, int page, int pageSize) {
        LambdaQueryWrapper<Dish> wrapper = new LambdaQueryWrapper<Dish>()
                .eq(Dish::getMerchantId, merchantId);
        if (categoryId != null) {
            wrapper.eq(Dish::getCategoryId, categoryId);
        }
        wrapper.orderByDesc(Dish::getCreatedAt);
        return page(new Page<>(page, pageSize), wrapper);
    }

    @Override
    public List<Dish> listByMerchant(Long merchantId, Long categoryId) {
        LambdaQueryWrapper<Dish> wrapper = new LambdaQueryWrapper<Dish>()
                .eq(Dish::getMerchantId, merchantId)
                .eq(Dish::getStatus, 1);
        if (categoryId != null) {
            wrapper.eq(Dish::getCategoryId, categoryId);
        }
        wrapper.orderByDesc(Dish::getSalesCount);
        return list(wrapper);
    }
}
