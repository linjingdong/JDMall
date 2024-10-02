package com.lin.common.to;

import lombok.Data;

/**
 * @Description TODO
 * @Date 2024/6/14 23:00
 * @Author Lin
 * @Version 1.0
 */
@Data
public class SkuHasStockVo {
    private Long skuId;
    private boolean hasStock;
}
