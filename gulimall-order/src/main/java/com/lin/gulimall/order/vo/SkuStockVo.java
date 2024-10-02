package com.lin.gulimall.order.vo;

import lombok.Data;

/**
 * @Description sku库存的vo
 * @Date 2024/7/15 16:52
 * @Author Lin
 * @Version 1.0
 */

@Data
public class SkuStockVo {
    private Long skuId;
    private boolean hasStock;
}
