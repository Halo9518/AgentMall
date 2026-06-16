package com.agentmall.module.address.mapper;

import com.agentmall.module.address.entity.Address;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 收货地址 Mapper
 */
@Mapper
public interface AddressMapper extends BaseMapper<Address> {

    /** 清除指定用户的所有默认地址 */
    @Update("UPDATE address SET is_default = 0 WHERE user_id = #{userId}")
    int clearDefault(@Param("userId") Long userId);

    /** 统计用户地址数量 */
    @Select("SELECT COUNT(*) FROM address WHERE user_id = #{userId}")
    int countByUserId(@Param("userId") Long userId);
}
