package com.lin.gulimall.product.service.impl;

import com.lin.gulimall.product.dao.AttrAttrgroupRelationDao;
import com.lin.gulimall.product.entity.AttrAttrgroupRelationEntity;
import com.lin.gulimall.product.entity.AttrEntity;
import com.lin.gulimall.product.service.AttrService;
import com.lin.gulimall.product.vo.AttrGroupRelationVo;
import com.lin.gulimall.product.vo.AttrGroupWithAttrs;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lin.common.utils.PageUtils;
import com.lin.common.utils.Query;

import com.lin.gulimall.product.dao.AttrGroupDao;
import com.lin.gulimall.product.entity.AttrGroupEntity;
import com.lin.gulimall.product.service.AttrGroupService;
import org.springframework.util.StringUtils;


@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupDao, AttrGroupEntity> implements AttrGroupService {
    @Autowired
    AttrAttrgroupRelationDao relationDao;
    @Autowired
    private AttrService attrService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrGroupEntity> page = this.page(
                new Query<AttrGroupEntity>().getPage(params),
                new QueryWrapper<AttrGroupEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params, Long catelogId) {
        String key = (String) params.get("key");
        // select * from pms_attr_group where catelog = ? and ( attr_group_id = key or attr_group_name like %key$)
        QueryWrapper<AttrGroupEntity> queryWrapper = new QueryWrapper<AttrGroupEntity>();
        if (!StringUtils.isEmpty(key)) {
            queryWrapper.and((obj) -> {
                obj.eq("attr_group_id", key).or().like("attr_group_name", key);
            });
        }
        if (catelogId == 0) {
            IPage<AttrGroupEntity> page = this.page(new Query<AttrGroupEntity>().getPage(params), queryWrapper);
            return new PageUtils(page);
        } else {
            IPage<AttrGroupEntity> page = this.page(new Query<AttrGroupEntity>().getPage(params), queryWrapper.eq("catelog_id", catelogId));
            return new PageUtils(page);
        }
    }

    @Override
    public void deleteRelationList(AttrGroupRelationVo[] relationVos) {
        List<AttrAttrgroupRelationEntity> relationEntities = Arrays.asList(relationVos).stream().map((item) -> {
            AttrAttrgroupRelationEntity relationEntity = new AttrAttrgroupRelationEntity();
            BeanUtils.copyProperties(item, relationEntity);
            return relationEntity;
        }).collect(Collectors.toList());
        relationDao.deleteBatchRelation(relationEntities);
    }

    /**
     * 根据分类id查出所有的分组以及这些分组里面的所有属性
     * @param catelogId 分类ID
     * @return 所有分组以及所有分组下的属性
     */
    @Override
    public List<AttrGroupWithAttrs> getAttrGroupWithAttrByCatelogId(Long catelogId) {
        // 1、查询分组信息
        List<AttrGroupEntity> AttrGroups = this.baseMapper.selectList(
                new QueryWrapper<AttrGroupEntity>().eq("catelog_id", catelogId)
        );

        // 2、查询所有属性
        List<AttrGroupWithAttrs> AttrGroupWithAttrsList = AttrGroups.stream().map(group -> {
            AttrGroupWithAttrs attrGroupWithAttrs = new AttrGroupWithAttrs();
            BeanUtils.copyProperties(group, attrGroupWithAttrs);
            List<AttrEntity> attrList = attrService.getRelationAttr(group.getAttrGroupId());
            attrGroupWithAttrs.setAttrs(attrList);
            return attrGroupWithAttrs;
        }).collect(Collectors.toList());
        return AttrGroupWithAttrsList;
    }

}