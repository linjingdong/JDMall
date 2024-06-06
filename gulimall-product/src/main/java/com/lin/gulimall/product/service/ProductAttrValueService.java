package com.lin.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lin.common.utils.PageUtils;
import com.lin.gulimall.product.entity.ProductAttrValueEntity;

import java.util.List;
import java.util.Map;

/**
 * spu属性值
 *
 * @author lin
 * @email lin@gmail.com
 * @date 2024-05-21 18:51:39
 */
public interface ProductAttrValueService extends IService<ProductAttrValueEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveProductAttr(List<ProductAttrValueEntity> productAttrValueList);

    List<ProductAttrValueEntity> baseAttrListForSpu(String spuId);

    void updateSpuAttr(Long spuId, List<ProductAttrValueEntity> entities);
}

