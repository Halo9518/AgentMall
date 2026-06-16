package com.agentmall.module.statistics.service.impl;

import com.agentmall.module.order.mapper.OrderItemMapper;
import com.agentmall.module.order.mapper.OrderMapper;
import com.agentmall.module.statistics.service.StatisticsService;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 数据统计 Service 实现
 */
@Service
public class StatisticsServiceImpl implements StatisticsService {

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;

    public StatisticsServiceImpl(OrderMapper orderMapper, OrderItemMapper orderItemMapper) {
        this.orderMapper = orderMapper;
        this.orderItemMapper = orderItemMapper;
    }

    @Override
    public Map<String, Object> getOverview(Long merchantId) {
        Map<String, Object> today = orderMapper.todayOverview(merchantId);
        Map<String, Object> week = orderMapper.weekOverview(merchantId);
        Map<String, Object> month = orderMapper.monthOverview(merchantId);
        int pending = orderMapper.pendingCount(merchantId);

        // MyBatis returns BigDecimal for SUM; convert to string for JSON consistency
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("todayCount", toLong(today.get("count")));
        result.put("todayAmount", today.get("amount"));
        result.put("weekCount", toLong(week.get("count")));
        result.put("weekAmount", week.get("amount"));
        result.put("monthCount", toLong(month.get("count")));
        result.put("monthAmount", month.get("amount"));
        result.put("pendingCount", pending);
        return result;
    }

    @Override
    public List<Map<String, Object>> getTrend(Long merchantId, int days) {
        return orderMapper.dailyTrend(merchantId, days);
    }

    @Override
    public List<Map<String, Object>> getTopDishes(Long merchantId, int limit) {
        return orderItemMapper.topDishes(merchantId, limit);
    }

    private long toLong(Object val) {
        if (val == null) return 0L;
        if (val instanceof Number n) return n.longValue();
        return Long.parseLong(val.toString());
    }
}
