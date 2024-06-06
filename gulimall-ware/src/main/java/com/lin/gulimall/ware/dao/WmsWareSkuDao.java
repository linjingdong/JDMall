package com.lin.gulimall.ware.dao;

import com.lin.gulimall.ware.entity.WmsWareSkuEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 商品库存
 * 
 * @author lin
 * @email lin@gmail.com
 * @date 2024-05-22 13:21:11
 */
@Mapper
public interface WmsWareSkuDao extends BaseMapper<WmsWareSkuEntity> {

    void addStock(@Param("skuId") Long skuId, @Param("wareId") Long wareId, @Param("skuNum") Integer skuNum);
}
