package com.lin.common.to;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @Description TODO
 * @Date 2024/6/3 12:43
 * @Author Lin
 * @Version 1.0
 */
@Data
public class SpuBoundTo {
    private Long spuId;
    private BigDecimal buyBounds;
    private BigDecimal growBounds;
}
