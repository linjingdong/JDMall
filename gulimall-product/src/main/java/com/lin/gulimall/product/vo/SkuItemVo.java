package com.lin.gulimall.product.vo;

import com.lin.gulimall.product.entity.SkuImagesEntity;
import com.lin.gulimall.product.entity.SkuInfoEntity;
import com.lin.gulimall.product.entity.SpuInfoDescEntity;
import lombok.Data;

import java.util.List;

/**
 * @Date 2024/6/27 16:24
 * @Author Lin
 * @Version 1.0
 */
@Data
public class SkuItemVo {
    // 1、sku基本信息获取 pms_sku_info
    SkuInfoEntity info;

    // 2、sku的图片信息 pms_sku_images
    List<SkuImagesEntity> images;

    // 3、获取spu的销售属性组合
    List<SkuItemSaleAttrsVo> saleAttrs;

    // 4、获取spu的介绍
    SpuInfoDescEntity desp;

    // 5、获取spu的规格参数信息
    List<SpuItemAttrGroupVo> groupAttrs;

    // 6、是否有货
    boolean hasStock = true;
}
