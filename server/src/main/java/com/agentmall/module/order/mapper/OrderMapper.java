package com.agentmall.module.order.mapper;

import com.agentmall.module.order.entity.Orders;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 订单 Mapper
 */
@Mapper
public interface OrderMapper extends BaseMapper<Orders> {

    /** 今日概览 */
    @Select("SELECT COUNT(*) as count, COALESCE(SUM(pay_amount), 0) as amount " +
            "FROM orders WHERE merchant_id = #{merchantId} AND DATE(created_at) = CURDATE()")
    Map<String, Object> todayOverview(@Param("merchantId") Long merchantId);

    /** 本周概览 */
    @Select("SELECT COUNT(*) as count, COALESCE(SUM(pay_amount), 0) as amount " +
            "FROM orders WHERE merchant_id = #{merchantId} AND YEARWEEK(created_at) = YEARWEEK(CURDATE())")
    Map<String, Object> weekOverview(@Param("merchantId") Long merchantId);

    /** 本月概览 */
    @Select("SELECT COUNT(*) as count, COALESCE(SUM(pay_amount), 0) as amount " +
            "FROM orders WHERE merchant_id = #{merchantId} " +
            "AND YEAR(created_at) = YEAR(CURDATE()) AND MONTH(created_at) = MONTH(CURDATE())")
    Map<String, Object> monthOverview(@Param("merchantId") Long merchantId);

    /** 待处理订单数 */
    @Select("SELECT COUNT(*) FROM orders WHERE merchant_id = #{merchantId} AND status = 'PENDING'")
    int pendingCount(@Param("merchantId") Long merchantId);

    /** 近N天每日趋势 */
    @Select("SELECT DATE(created_at) as date, COUNT(*) as count, " +
            "COALESCE(SUM(pay_amount), 0) as amount " +
            "FROM orders WHERE merchant_id = #{merchantId} " +
            "AND created_at >= DATE_SUB(CURDATE(), INTERVAL #{days} DAY) " +
            "GROUP BY DATE(created_at) ORDER BY date")
    List<Map<String, Object>> dailyTrend(@Param("merchantId") Long merchantId, @Param("days") int days);
}
