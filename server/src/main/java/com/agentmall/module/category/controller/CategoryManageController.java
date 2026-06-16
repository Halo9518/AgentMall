package com.agentmall.module.category.controller;

import com.agentmall.common.Result;
import com.agentmall.common.exception.BusinessException;
import com.agentmall.module.category.entity.Category;
import com.agentmall.module.category.service.CategoryService;
import com.agentmall.module.user.entity.User;
import com.agentmall.security.CurrentUser;
import com.agentmall.security.MerchantContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 商家端 — 分类管理控制器
 */
@Tag(name = "商家端-分类管理", description = "分类增删改查")
@RestController
@RequestMapping("/api/merchant/categories")
public class CategoryManageController {

    private final CategoryService categoryService;
    private final MerchantContext merchantContext;

    public CategoryManageController(CategoryService categoryService, MerchantContext merchantContext) {
        this.categoryService = categoryService;
        this.merchantContext = merchantContext;
    }

    @Operation(summary = "分类列表")
    @GetMapping
    public Result<List<Category>> list(@CurrentUser User user) {
        Long merchantId = merchantContext.getCurrentMerchantId(user);
        return Result.success(categoryService.listByMerchantId(merchantId));
    }

    @Operation(summary = "添加分类")
    @PostMapping
    public Result<Category> create(@CurrentUser User user, @RequestBody Category category) {
        Long merchantId = merchantContext.getCurrentMerchantId(user);
        category.setMerchantId(merchantId);
        categoryService.save(category);
        return Result.success(category);
    }

    @Operation(summary = "修改分类")
    @PutMapping("/{id}")
    public Result<Void> update(@CurrentUser User user, @PathVariable Long id, @RequestBody Category category) {
        Long merchantId = merchantContext.getCurrentMerchantId(user);
        Category exist = categoryService.getById(id);
        if (exist == null || !exist.getMerchantId().equals(merchantId)) {
            throw new BusinessException("分类不存在");
        }
        category.setId(id);
        category.setMerchantId(merchantId);
        categoryService.updateById(category);
        return Result.success();
    }

    @Operation(summary = "删除分类")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@CurrentUser User user, @PathVariable Long id) {
        Long merchantId = merchantContext.getCurrentMerchantId(user);
        Category exist = categoryService.getById(id);
        if (exist == null || !exist.getMerchantId().equals(merchantId)) {
            throw new BusinessException("分类不存在");
        }
        categoryService.removeById(id);
        return Result.success();
    }
}
