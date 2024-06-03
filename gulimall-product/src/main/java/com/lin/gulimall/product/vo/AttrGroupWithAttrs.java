package com.lin.gulimall.product.vo;

import com.baomidou.mybatisplus.annotation.TableId;
import com.lin.gulimall.product.entity.AttrEntity;
import lombok.Data;

import java.util.List;

/**
 * @Description TODO
 * @Date 2024/6/2 15:25
 * @Author Lin
 * @Version 1.0
 */
@Data
public class AttrGroupWithAttrs {
    /**
     * 分组id
     */
    private Long attrGroupId;
    /**
     * 组名
     */
    private String attrGroupName;
    /**
     * 排序
     */
    private Integer sort;
    /**
     * 描述
     */
    private String descript;
    /**
     * 组图标
     */
    private String icon;
    /**
     * 所属分类id
     */
    private Long catelogId;
    // 封装好分组下的属性
    private List<AttrEntity> attrs;
}
