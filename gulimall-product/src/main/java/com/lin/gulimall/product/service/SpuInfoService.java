package com.lin.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lin.common.utils.PageUtils;
import com.lin.gulimall.product.entity.SpuInfoEntity;
import com.lin.gulimall.product.vo.SpuSaveVo;

import java.util.Map;

/**
 * spu信息
 *
 * @author lin
 * @email lin@gmail.com
 * @date 2024-05-21 18:51:39
 */
public interface SpuInfoService extends IService<SpuInfoEntity> {
    PageUtils queryPage(Map<String, Object> params);

    void saveSpuInfo(SpuSaveVo spuSaveVo);

    void saveBaseSpuInfo(SpuInfoEntity spuInfoEntity);

    PageUtils queryPageByCondtion(Map<String, Object> params);

    /**
     * 商品上架
     *
     * @param spuId spuId
     */
    void up(Integer spuId);

    /**
     * 根据skuId返回spuInfo
     *
     * @param id
     * @return
     */
    SpuInfoEntity getSpuInfoBySkuId(Long id);
}

