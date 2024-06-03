package com.lin.gulimall.product.dao;

import com.lin.gulimall.product.entity.CategoryEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品三级分类
 * 
 * @author lin
 * @email lin@gmail.com
 * @date 2024-05-21 18:51:39
 */
@Mapper
public interface CategoryDao extends BaseMapper<CategoryEntity> {
	
}
