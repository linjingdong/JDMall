package com.lin.gulimall.order.vo;


import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @Description TODO
 * @Date 2024/7/11 16:29
 * @Author Lin
 * @Version 1.0
 */
@ToString
public class OrderConfirmVo {
    @Getter
    @Setter
    // 收货地址：ums_member_receive_address表
    private List<MemberAddressVo> address;

    @Getter
    @Setter
    // 所有选中的购物项
    private List<OrderItemVo> items;

    // 发票记录...

    @Getter
    @Setter
    // 优惠券信息
    private Integer Integration;

    @Getter
    @Setter
    // 防重令牌
    private String orderToken;

    @Getter
    @Setter
    private Map<Long, Boolean> stocks;

    // 商品总数量
    private Integer count;

    // 商品总金额
    private BigDecimal total;

    // 应付价格
    private BigDecimal payPrice;

    public Integer getCount() {
        Integer i = 0;
        for (OrderItemVo item : items) {
            i += item.getCount();
        }
        return this.count = i;
    }

    public BigDecimal getTotal() {
        BigDecimal sum = new BigDecimal("0");

        if (items != null) {
            for (OrderItemVo item : items) {
                sum = sum.add(item.getPrice()
                        .multiply(
                                new BigDecimal(item.getCount().toString())
                        ));
            }
        }
        return this.total = sum;
    }


    public BigDecimal getPayPrice() {
        return this.payPrice = getTotal();
    }
}
