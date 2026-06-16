package com.agentmall.module.dish.controller;

import com.agentmall.common.Result;
import com.agentmall.module.dish.entity.Dish;
import com.agentmall.module.dish.service.DishService;
import com.agentmall.module.dish.vo.DishVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * C端 — 菜品控制器
 */
@Tag(name = "C端-菜品", description = "商家菜品查询")
@RestController
@RequestMapping("/api/merchants/{merchantId}/dishes")
public class DishController {

    private final DishService dishService;

    public DishController(DishService dishService) {
        this.dishService = dishService;
    }

    @Operation(summary = "菜品列表")
    @GetMapping
    public Result<List<DishVO>> list(
            @PathVariable Long merchantId,
            @RequestParam(required = false) Long categoryId) {
        List<Dish> dishes = dishService.listByMerchant(merchantId, categoryId);
        List<DishVO> vos = dishes.stream().map(DishVO::fromEntity).toList();
        return Result.success(vos);
    }
}
