package com.lin.gulimall.order.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @Description 封装订单提交数据VO
 * @Date 2024/10/2 15:44
 * @Author Lin
 * @Version 1.0
 */
@Data
public class OrderSubmitVo {
    // 收货地址ID
    private Long addrId;
    // 支付方式
    private Integer payType;
    // 无需提交购买的商品，去购物车再重新获取一遍
    // 优惠
    // 发票
    // 防重令牌
    private String orderToken;
    // 应付价格，目的：验价
    private BigDecimal payPrice;
    // 订单备注
    private String note;
    // 用户相关信息，直接去session当中取出相关的用户

}
