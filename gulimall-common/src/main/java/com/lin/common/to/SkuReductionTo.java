package com.lin.common.to;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @Description TODO
 * @Date 2024/6/3 16:02
 * @Author Lin
 * @Version 1.0
 */
@Data
public class SkuReductionTo {
    private Long SkuId;
    private int fullCount;
    private BigDecimal discount;
    private int countStatus;
    private BigDecimal fullPrice;
    private BigDecimal reducePrice;
    private int priceStatus;

    private List<MemberPrice> memberPrice;

}
