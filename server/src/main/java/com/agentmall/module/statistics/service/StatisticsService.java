package com.agentmall.module.statistics.service;

import java.util.Map;
import java.util.List;

/**
 * 数据统计 Service
 */
public interface StatisticsService {

    /** 今日/本周/本月概览 */
    Map<String, Object> getOverview(Long merchantId);

    /** 近N天趋势 */
    List<Map<String, Object>> getTrend(Long merchantId, int days);

    /** 热销菜品 Top N */
    List<Map<String, Object>> getTopDishes(Long merchantId, int limit);
}
