package com.lin.gulimall.ware.vo;

import lombok.Data;

import java.util.List;

/**
 * @Description TODO
 * @Date 2024/6/4 12:43
 * @Author Lin
 * @Version 1.0
 */
@Data
public class MergeVo {
    // 整单Id
    private Long purchaseId;
    // 合并项集合
    private List<Long> items;
}
