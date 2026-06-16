package com.agentmall.module.category.service.impl;

import com.agentmall.module.category.entity.Category;
import com.agentmall.module.category.mapper.CategoryMapper;
import com.agentmall.module.category.service.CategoryService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 分类 Service 实现
 */
@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    @Override
    public List<Category> listByMerchantId(Long merchantId) {
        return list(new LambdaQueryWrapper<Category>()
                .eq(Category::getMerchantId, merchantId)
                .orderByAsc(Category::getSortOrder));
    }
}
