package com.lin.gulimall.seach.vo;

import lombok.Data;

import java.util.List;

/**
 * @Description TODO
 * @Date 2024/6/20 10:29
 * @Author Lin
 * @Version 1.0
 */

/**
 * 封装页面所有可莪能
 */
@Data
public class SearchParam {
    private String keyword; // 页面传递过来的全文匹配关键字
    private Long catalog3Id; // 三级分类ID
    private String sort; // 排序条件
    private Integer hasStock; // 是否有货 0（无库存） 1（有库存）
    private String skuPrice; // 价格拒签查询
    private List<Long> brandId; // 按照品牌进行查询，可以多选
    private List<String> attrs; // 按照属性进行筛选
    private Integer pageNum = 1; // 页码,如果不传，默认是第一页开始

    private String _queryString; // 原生的所有查询条件
}
