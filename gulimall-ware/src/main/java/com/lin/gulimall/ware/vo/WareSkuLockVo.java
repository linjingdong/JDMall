package com.lin.gulimall.ware.vo;

import lombok.Data;

import java.util.List;

/**
 * @Description 库存锁定的VO
 * @Date 2024/10/19 20:34
 * @Author Lin
 * @Version 1.0
 */
@Data
public class WareSkuLockVo {
    /**
     * 需要锁定的订单号
     */
    private String orderSn;

    /**
     *  需要锁定的库存信息
     */
    private List<OrderItemVo> locks;
}
