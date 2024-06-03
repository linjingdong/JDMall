package com.lin.gulimall.product.vo;

import lombok.Data;

/**
 * @Description TODO
 * @Date 2024/5/31 23:36
 * @Author Lin
 * @Version 1.0
 */
@Data
public class AttrRespVo extends AttrVo {
    /**
     * catelogName:所属分类名字
     */
    private String catelogName;

    /**
     * groupName:所属分组名字
     */
    private String groupName;

    /**
     * catelogPath:所属分类的级联路径
     */
    private Long[] catelogPath;
}
