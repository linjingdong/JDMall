package com.lin.gulimall.product.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @Description 二级分类Vo
 * @Date 2024/6/17 14:59
 * @Author Lin
 * @Version 1.0
 */

// 二级分类Vo
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Catelog2Vo {
    private String catalog1Id; // 1级父分类Id
    private List<Catelog3Vo> catalog3List; // 三级分类集合
    private String Id;
    private String name;


    // 三级分类Vo
    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public static class Catelog3Vo {
        private String catalog2Id;
        private String Id;
        private String name;
    }
}
