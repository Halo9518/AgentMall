package com.agentmall.module.category.controller;

import com.agentmall.common.Result;
import com.agentmall.module.category.entity.Category;
import com.agentmall.module.category.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * C端 — 分类控制器
 */
@Tag(name = "C端-分类", description = "商家分类查询")
@RestController
@RequestMapping("/api/merchants/{merchantId}/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @Operation(summary = "商家分类列表")
    @GetMapping
    public Result<List<Category>> list(@PathVariable Long merchantId) {
        return Result.success(categoryService.listByMerchantId(merchantId));
    }
}
