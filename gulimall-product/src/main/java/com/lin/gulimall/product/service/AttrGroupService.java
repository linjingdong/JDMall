package com.lin.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lin.common.utils.PageUtils;
import com.lin.gulimall.product.entity.AttrGroupEntity;
import com.lin.gulimall.product.vo.AttrGroupRelationVo;
import com.lin.gulimall.product.vo.AttrGroupWithAttrs;

import java.util.List;
import java.util.Map;

/**
 * 属性分组
 *
 * @author lin
 * @email lin@gmail.com
 * @date 2024-05-21 18:51:39
 */
public interface AttrGroupService extends IService<AttrGroupEntity> {

    PageUtils queryPage(Map<String, Object> params);

    PageUtils queryPage(Map<String, Object> params, Long catelogId);

    void deleteRelationList(AttrGroupRelationVo[] relationVos);

    List<AttrGroupWithAttrs> getAttrGroupWithAttrByCatelogId(Long catelogId);
}

