package com.lin.gulimall.order.to;

import com.lin.gulimall.order.entity.OmsOrderEntity;
import com.lin.gulimall.order.entity.OmsOrderItemEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @Description 创建订单TO
 * @Date 2024/10/2 18:28
 * @Author Lin
 * @Version 1.0
 */
@Data
public class OrderCreateTo {
    /*
    订单
     */
    private OmsOrderEntity order;
    /*
    订单项
     */
    private List<OmsOrderItemEntity> orderItems;
    /*
    计算应付的价格
     */
    private BigDecimal payPrice;
    /*
    运费
     */
    private BigDecimal fare;
}
