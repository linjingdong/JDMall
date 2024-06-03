package com.lin.gulimall.product.vo;

import lombok.Data;

/**
 * @Description 分组和分组关联的属性Id的VO表
 * @Date 2024/6/1 19:38
 * @Author Lin
 * @Version 1.0
 */
@Data
public class AttrGroupRelationVo {
    // 关联的属性Id
    private Long attrId;
    // 分组Id
    private Long attrGroupId;
}
