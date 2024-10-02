package com.lin.gulimall.seach.vo;

import com.lin.common.to.es.SkuEsModel;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @Description TODO
 * @Date 2024/6/20 10:50
 * @Author Lin
 * @Version 1.0
 */
@Data
public class SearchResult {
    // 查询到的所有商品信息
    private List<SkuEsModel> products;

    /*
        以下是分类信息
     */
    private Integer pageNum; // 当前页码
    private Long total; // 总记录数
    private Integer totalPages; // 总页码
    private List<Integer> pageNavs; // 导航页码

    private List<BrandVo> brands; // 当前查询到的结果，所有涉及到的品牌
    private List<CatalogVo> catalogs; // 当前查询到的结果，所有涉及到的分类
    private List<AttrVo> attrs; // 当前查询到的结果，所有涉及到的属性

    // 面包屑导航数据
    private List<NavVo> navs = new ArrayList<>();
    private List<Long> attrIds = new ArrayList<>();

    @Data
    public static class NavVo {
        private String navName;
        private String navValue;
        private String link;
    }

    @Data
    public static class BrandVo {
        private Long brandId;
        private String brandName;
        private String brandImg;
    }

    @Data
    public static class CatalogVo {
        private Long catalogId;
        private String catalogName;
    }

    @Data
    public static class AttrVo {
        private Long attrId;
        private String attrName;
        private List<String> attrValue;
    }
}
