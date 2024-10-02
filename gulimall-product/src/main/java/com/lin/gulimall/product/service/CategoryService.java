package com.lin.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lin.common.utils.PageUtils;
import com.lin.gulimall.product.entity.CategoryEntity;
import com.lin.gulimall.product.vo.Catelog2Vo;

import java.util.List;
import java.util.Map;

/**
 * 商品三级分类
 *
 * @author lin
 * @email lin@gmail.com
 * @date 2024-05-21 18:51:39
 */
public interface CategoryService extends IService<CategoryEntity> {

    PageUtils queryPage(Map<String, Object> params);

    List<CategoryEntity> listWithTree();

    void removeMenusByIds(List<Long> catIds);

    /**
     * 找到catelogId的完整路径
     * [父/子/孙]
     * @param catelogId 当前三级菜单的id
     * @return 返回完整路径
     */
    Long[] findCateLogPath(Long catelogId);

    void updateDetail(CategoryEntity category);

    List<CategoryEntity> getLevel1Categorys();

    Map<String, List<Catelog2Vo>> getCatalogJson();
}

