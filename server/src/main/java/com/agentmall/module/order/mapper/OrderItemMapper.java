package com.agentmall.module.order.mapper;

import com.agentmall.module.order.entity.OrderItem;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 订单明细 Mapper
 */
@Mapper
public interface OrderItemMapper extends BaseMapper<OrderItem> {

    /** 热销菜品 Top N */
    @Select("SELECT oi.dish_id as dishId, oi.dish_name as dishName, " +
            "SUM(oi.quantity) as totalQty, SUM(oi.amount) as totalAmount " +
            "FROM order_item oi JOIN orders o ON oi.order_id = o.id " +
            "WHERE o.merchant_id = #{merchantId} AND o.status != 'REJECTED' " +
            "GROUP BY oi.dish_id, oi.dish_name " +
            "ORDER BY totalQty DESC LIMIT #{limit}")
    List<Map<String, Object>> topDishes(@Param("merchantId") Long merchantId, @Param("limit") int limit);
}
