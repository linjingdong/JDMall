package com.lin.gulimall.product.dao;

import com.lin.gulimall.product.entity.SpuInfoEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * spu信息
 *
 * @author lin
 * @email lin@gmail.com
 * @date 2024-05-21 18:51:39
 */
@Mapper
public interface SpuInfoDao extends BaseMapper<SpuInfoEntity> {

    void updateSpuStatus(@Param("spuId") Integer spuId, @Param("code") int code);
}
