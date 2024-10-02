package com.lin.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lin.common.utils.PageUtils;
import com.lin.gulimall.product.entity.AttrEntity;
import com.lin.gulimall.product.vo.AttrRespVo;
import com.lin.gulimall.product.vo.AttrVo;

import java.util.List;
import java.util.Map;

/**
 * 商品属性
 *
 * @author lin
 * @email lin@gmail.com
 * @date 2024-05-21 18:51:39
 */
public interface AttrService extends IService<AttrEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveAttr(AttrVo attr);

    PageUtils querybaseAttrList(Map<String, Object> params, Long catelogId, String type);

    AttrRespVo getAttrInfo(Long attrId);

    void udpateAttr(AttrVo attrVo);

    List<AttrEntity> getRelationAttr(Long attrgroupId);

    PageUtils getNoRelationAttr(Map<String, Object> params, Long attrgroupId);

    /**
     * @param attrIds 被指定的所有属性
     * @return 返回挑选出来的检索属性
     */
    List<Long> selectSearchAttrs(List<Long> attrIds);
}

