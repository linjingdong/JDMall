package com.lin.gulimall.order.dao;

import com.lin.gulimall.order.entity.OmsOrderEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 订单
 * 
 * @author lin
 * @email lin@gmail.com
 * @date 2024-05-22 13:09:20
 */
@Mapper
public interface OmsOrderDao extends BaseMapper<OmsOrderEntity> {

    void updateOrderStatus(@Param("orderSn") String orderSn,@Param("status") Integer code);
}
