package com.lin.common.to.mq;

import lombok.Data;

import java.util.List;

/**
 * @Description TODO
 * @Date 2025/1/6 18:16
 * @Author Lin
 * @Version 1.0
 */

@Data
public class StockLockedTo {
    private Long id; // 库存工作单id
    private StockDetailsTo stockDetailsTo; // 详情
}
