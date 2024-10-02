package com.lin.gulimall.product.vo;

import lombok.Data;
import lombok.ToString;

import java.util.List;

/**
 * @Description TODO
 * @Date 2024/6/27 17:30
 * @Author Lin
 * @Version 1.0
 */
@ToString
@Data
public class SkuItemSaleAttrsVo {
    private Long attrId;
    private String attrName;
    private List<AttrValueWithSkuIdVo> attrValues;
}
