package com.agentmall.module.cart.service.impl;

import cn.hutool.json.JSONUtil;
import com.agentmall.common.exception.BusinessException;
import com.agentmall.module.cart.service.CartService;
import com.agentmall.module.cart.vo.CartItemVO;
import com.agentmall.module.cart.vo.CartVO;
import com.agentmall.module.merchant.entity.Merchant;
import com.agentmall.module.merchant.service.MerchantService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 购物车 Service 实现
 * <p>
 * Redis Hash: cart:{userId}  →  field=dishId, value=CartItemVO JSON
 */
@Service
public class CartServiceImpl implements CartService {

    private static final String CART_KEY_PREFIX = "cart:";

    private final StringRedisTemplate redis;
    private final MerchantService merchantService;

    public CartServiceImpl(StringRedisTemplate redis, MerchantService merchantService) {
        this.redis = redis;
        this.merchantService = merchantService;
    }

    private String cartKey(Long userId) {
        return CART_KEY_PREFIX + userId;
    }

    @Override
    public CartVO getCart(Long userId) {
        String key = cartKey(userId);
        Map<Object, Object> entries = redis.opsForHash().entries(key);

        List<CartItemVO> items = new ArrayList<>();
        Long merchantId = null;
        String merchantName = null;

        for (Object value : entries.values()) {
            CartItemVO item = JSONUtil.toBean(value.toString(), CartItemVO.class);
            items.add(item);
            if (merchantId == null) {
                merchantId = item.getMerchantId();
                merchantName = item.getMerchantName();
            }
        }

        CartVO vo = new CartVO();
        vo.setMerchantId(merchantId);
        vo.setMerchantName(merchantName);
        vo.setItems(items);
        vo.setTotalAmount(items.stream()
                .map(CartItemVO::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        // 填充配送费
        if (merchantId != null) {
            Merchant merchant = merchantService.getById(merchantId);
            vo.setDeliveryFee(merchant != null && merchant.getDeliveryFee() != null
                    ? merchant.getDeliveryFee() : BigDecimal.ZERO);
        }
        return vo;
    }

    @Override
    public void addItem(Long userId, CartItemVO item) {
        String key = cartKey(userId);

        // 跨商家校验：检查现有购物车是否属于同一商家
        Map<Object, Object> entries = redis.opsForHash().entries(key);
        for (Object value : entries.values()) {
            CartItemVO existing = JSONUtil.toBean(value.toString(), CartItemVO.class);
            if (!existing.getMerchantId().equals(item.getMerchantId())) {
                throw new BusinessException("购物车中已有其他商家的商品，请先清空");
            }
        }

        // 已存在则累加数量，否则新增
        String field = item.getDishId().toString();
        Object old = redis.opsForHash().get(key, field);
        if (old != null) {
            CartItemVO oldItem = JSONUtil.toBean(old.toString(), CartItemVO.class);
            oldItem.setQuantity(oldItem.getQuantity() + item.getQuantity());
            redis.opsForHash().put(key, field, JSONUtil.toJsonStr(oldItem));
        } else {
            redis.opsForHash().put(key, field, JSONUtil.toJsonStr(item));
        }
    }

    @Override
    public void updateQuantity(Long userId, Long dishId, int quantity) {
        if (quantity <= 0) {
            removeItem(userId, dishId);
            return;
        }

        String key = cartKey(userId);
        String field = dishId.toString();
        Object old = redis.opsForHash().get(key, field);
        if (old == null) {
            throw new BusinessException("购物车中无此商品");
        }

        CartItemVO item = JSONUtil.toBean(old.toString(), CartItemVO.class);
        item.setQuantity(quantity);
        redis.opsForHash().put(key, field, JSONUtil.toJsonStr(item));
    }

    @Override
    public void removeItem(Long userId, Long dishId) {
        redis.opsForHash().delete(cartKey(userId), dishId.toString());
    }

    @Override
    public void clearCart(Long userId) {
        redis.delete(cartKey(userId));
    }
}
