package com.agentmall.module.cart.controller;

import com.agentmall.common.Result;
import com.agentmall.common.exception.BusinessException;
import com.agentmall.module.cart.dto.AddCartDTO;
import com.agentmall.module.cart.service.CartService;
import com.agentmall.module.cart.vo.CartItemVO;
import com.agentmall.module.cart.vo.CartVO;
import com.agentmall.module.dish.entity.Dish;
import com.agentmall.module.dish.service.DishService;
import com.agentmall.module.merchant.entity.Merchant;
import com.agentmall.module.merchant.service.MerchantService;
import com.agentmall.module.user.entity.User;
import com.agentmall.security.CurrentUser;
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
 * 购物车控制器
 */
@Tag(name = "购物车", description = "Redis 购物车增删改查")
@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;
    private final DishService dishService;
    private final MerchantService merchantService;

    public CartController(CartService cartService, DishService dishService, MerchantService merchantService) {
        this.cartService = cartService;
        this.dishService = dishService;
        this.merchantService = merchantService;
    }

    @Operation(summary = "获取购物车")
    @GetMapping
    public Result<CartVO> get(@CurrentUser User user) {
        return Result.success(cartService.getCart(user.getId()));
    }

    @Operation(summary = "添加商品")
    @PostMapping
    public Result<Void> add(@CurrentUser User user, @Valid @RequestBody AddCartDTO dto) {
        // 查询菜品和商家信息
        Dish dish = dishService.getById(dto.getDishId());
        if (dish == null || dish.getStatus() == 0) {
            throw new BusinessException("菜品不存在或已下架");
        }
        Merchant merchant = merchantService.getById(dish.getMerchantId());
        if (merchant == null || merchant.getStatus() == 0) {
            throw new BusinessException("商家已歇业");
        }

        CartItemVO item = new CartItemVO(
                dish.getId(), dish.getName(), dish.getImage(),
                dish.getPrice(), dto.getQuantity(),
                merchant.getId(), merchant.getName()
        );
        cartService.addItem(user.getId(), item);
        return Result.success();
    }

    @Operation(summary = "修改数量")
    @PutMapping("/{dishId}")
    public Result<Void> updateQuantity(
            @CurrentUser User user,
            @PathVariable Long dishId,
            @RequestParam int quantity) {
        cartService.updateQuantity(user.getId(), dishId, quantity);
        return Result.success();
    }

    @Operation(summary = "删除单项")
    @DeleteMapping("/{dishId}")
    public Result<Void> remove(@CurrentUser User user, @PathVariable Long dishId) {
        cartService.removeItem(user.getId(), dishId);
        return Result.success();
    }

    @Operation(summary = "清空")
    @DeleteMapping
    public Result<Void> clear(@CurrentUser User user) {
        cartService.clearCart(user.getId());
        return Result.success();
    }
}
