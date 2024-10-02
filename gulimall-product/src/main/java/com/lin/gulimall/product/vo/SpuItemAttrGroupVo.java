package com.lin.gulimall.product.vo;

import lombok.Data;
import lombok.ToString;

import java.util.List;

/**
 * @Description TODO
 * @Date 2024/6/27 17:31
 * @Author Lin
 * @Version 1.0
 */
@ToString
@Data
public class SpuItemAttrGroupVo {
    private String groupName;
    private List<Attr> attrs;
}
