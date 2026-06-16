package com.agentmall.module.dish.controller;

import com.agentmall.common.PageResult;
import com.agentmall.common.Result;
import com.agentmall.common.exception.BusinessException;
import com.agentmall.module.dish.dto.DishDTO;
import com.agentmall.module.dish.entity.Dish;
import com.agentmall.module.dish.service.DishService;
import com.agentmall.module.dish.vo.DishVO;
import com.agentmall.module.merchant.service.MerchantService;
import com.agentmall.module.user.entity.User;
import com.agentmall.security.CurrentUser;
import com.agentmall.security.MerchantContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 商家端 — 菜品管理控制器
 */
@Tag(name = "商家端-菜品管理", description = "菜品增删改查、上下架")
@RestController
@RequestMapping("/api/merchant/dishes")
public class DishManageController {

    private final DishService dishService;
    private final MerchantContext merchantContext;

    public DishManageController(DishService dishService, MerchantContext merchantContext) {
        this.dishService = dishService;
        this.merchantContext = merchantContext;
    }

    @Operation(summary = "菜品列表（分页）")
    @GetMapping
    public Result<PageResult<DishVO>> list(
            @CurrentUser User user,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Long categoryId) {
        Long merchantId = merchantContext.getCurrentMerchantId(user);
        var pageResult = dishService.pageByMerchant(merchantId, categoryId, page, pageSize);
        var records = pageResult.getRecords().stream().map(DishVO::fromEntity).toList();
        return Result.success(new PageResult<>(records, pageResult.getCurrent(), pageResult.getSize(), pageResult.getTotal()));
    }

    @Operation(summary = "添加菜品")
    @PostMapping
    public Result<DishVO> create(@CurrentUser User user, @Valid @RequestBody DishDTO dto) {
        Long merchantId = merchantContext.getCurrentMerchantId(user);
        Dish dish = new Dish();
        dish.setMerchantId(merchantId);
        dish.setCategoryId(dto.getCategoryId());
        dish.setName(dto.getName());
        dish.setImage(dto.getImage());
        dish.setPrice(dto.getPrice());
        dish.setDescription(dto.getDescription());
        dish.setStatus(1);
        dishService.save(dish);
        return Result.success(DishVO.fromEntity(dish));
    }

    @Operation(summary = "修改菜品")
    @PutMapping("/{id}")
    public Result<Void> update(@CurrentUser User user, @PathVariable Long id, @Valid @RequestBody DishDTO dto) {
        Long merchantId = merchantContext.getCurrentMerchantId(user);
        Dish exist = dishService.getById(id);
        if (exist == null || !exist.getMerchantId().equals(merchantId)) {
            throw new BusinessException("菜品不存在");
        }
        exist.setCategoryId(dto.getCategoryId());
        exist.setName(dto.getName());
        exist.setImage(dto.getImage());
        exist.setPrice(dto.getPrice());
        exist.setDescription(dto.getDescription());
        dishService.updateById(exist);
        return Result.success();
    }

    @Operation(summary = "上下架")
    @PutMapping("/{id}/status")
    public Result<Void> toggleStatus(@CurrentUser User user, @PathVariable Long id) {
        Long merchantId = merchantContext.getCurrentMerchantId(user);
        Dish exist = dishService.getById(id);
        if (exist == null || !exist.getMerchantId().equals(merchantId)) {
            throw new BusinessException("菜品不存在");
        }
        exist.setStatus(exist.getStatus() == 1 ? 0 : 1);
        dishService.updateById(exist);
        return Result.success();
    }

    @Operation(summary = "删除菜品")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@CurrentUser User user, @PathVariable Long id) {
        Long merchantId = merchantContext.getCurrentMerchantId(user);
        Dish exist = dishService.getById(id);
        if (exist == null || !exist.getMerchantId().equals(merchantId)) {
            throw new BusinessException("菜品不存在");
        }
        dishService.removeById(id);
        return Result.success();
    }
}
