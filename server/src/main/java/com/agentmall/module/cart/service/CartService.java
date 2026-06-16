package com.agentmall.module.cart.service;

import com.agentmall.module.cart.vo.CartItemVO;
import com.agentmall.module.cart.vo.CartVO;

import java.util.List;

/**
 * 购物车 Service（Redis Hash）
 */
public interface CartService {

    /** 获取购物车 */
    CartVO getCart(Long userId);

    /** 添加商品 */
    void addItem(Long userId, CartItemVO item);

    /** 更新数量 */
    void updateQuantity(Long userId, Long dishId, int quantity);

    /** 删除单项 */
    void removeItem(Long userId, Long dishId);

    /** 清空 */
    void clearCart(Long userId);
}
