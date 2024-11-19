package com.lin.gulimall.ware.vo;

import lombok.Data;

/**
 * @Description 订单库存锁定结果vo
 * @Date 2024/10/19 20:41
 * @Author Lin
 * @Version 1.0
 */

@Data
public class LockStockResult {
    /**
     * 锁定的商品
     */
    private Long skuId;

    /**
     * 锁定的库存数量
     */
    private Integer count;

    /**
     * 锁定的结果
     */
    private Boolean lockedResult;
}
