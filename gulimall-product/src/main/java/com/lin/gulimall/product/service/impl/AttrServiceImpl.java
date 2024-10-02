package com.lin.gulimall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lin.common.constant.ProductConstant;
import com.lin.gulimall.product.dao.AttrAttrgroupRelationDao;
import com.lin.gulimall.product.dao.AttrGroupDao;
import com.lin.gulimall.product.dao.CategoryDao;
import com.lin.gulimall.product.entity.AttrAttrgroupRelationEntity;
import com.lin.gulimall.product.entity.AttrGroupEntity;
import com.lin.gulimall.product.entity.CategoryEntity;
import com.lin.gulimall.product.service.CategoryService;
import com.lin.gulimall.product.vo.AttrGroupWithAttrs;
import com.lin.gulimall.product.vo.AttrRespVo;
import com.lin.gulimall.product.vo.AttrVo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lin.common.utils.PageUtils;
import com.lin.common.utils.Query;

import com.lin.gulimall.product.dao.AttrDao;
import com.lin.gulimall.product.entity.AttrEntity;
import com.lin.gulimall.product.service.AttrService;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Attr;


@Service("attrService")
public class AttrServiceImpl extends ServiceImpl<AttrDao, AttrEntity> implements AttrService {
    @Autowired
    AttrAttrgroupRelationDao attrAttrgroupRelationDao;
    @Autowired
    CategoryDao categoryDao;
    @Autowired
    AttrGroupDao attrGroupDao;
    @Autowired
    CategoryService categoryService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(params),
                new QueryWrapper<AttrEntity>()
        );
        return new PageUtils(page);
    }

    @Transactional
    @Override
    public void saveAttr(AttrVo attr) {
        AttrEntity attrEntity = new AttrEntity();
        BeanUtils.copyProperties(attr, attrEntity);
        // 保存基本数据
        this.save(attrEntity);

        // 保存关联关系
        if (attr.getAttrType() == ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode() && attr.getAttrGroupId() != null) {
            AttrAttrgroupRelationEntity attrAttrgroupRelationEntity = new AttrAttrgroupRelationEntity();
            attrAttrgroupRelationEntity.setAttrGroupId(attr.getAttrGroupId());
            attrAttrgroupRelationEntity.setAttrId(attrEntity.getAttrId());
            attrAttrgroupRelationDao.insert(attrAttrgroupRelationEntity);
        }
    }

    /**
     * 模糊查询分类属性的信息
     *
     * @param params    分页信息
     * @param catelogId 分类Id
     * @param type
     * @return 封装好分页信息以及查询的分类属性的信息
     */
    @Override
    public PageUtils querybaseAttrList(Map<String, Object> params, Long catelogId, String type) {
        QueryWrapper<AttrEntity> queryWrapper = new QueryWrapper<AttrEntity>().eq(
                "attr_type",
                "base".equalsIgnoreCase(type) ? ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode() : ProductConstant.AttrEnum.ATTR_TYPE_SALE.getCode());
        // 判断是否是分类查询
        if (catelogId != 0) {
            queryWrapper.eq("catelog_id", catelogId);
        }

        // 判断是否是模糊查询
        String key = (String) params.get("key");
        if (StringUtils.isNotEmpty(key)) {
            queryWrapper.and((wrapper) -> {
                wrapper.eq("attr_id", key).or().like("attr_name", key);
            });
        }

        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(params),
                queryWrapper
        );

        List<AttrEntity> records = page.getRecords();
        // 用流式编程收集分类和分组的名字
        List<AttrRespVo> respVo = records.stream().map(attrEntity -> {
            AttrRespVo attrRespVo = new AttrRespVo();
            BeanUtils.copyProperties(attrEntity, attrRespVo);
            // 设置分组的名字
            if ("base".equalsIgnoreCase(type)) {
                AttrAttrgroupRelationEntity RelationEntity = attrAttrgroupRelationDao.selectOne(
                        new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attrEntity.getAttrId())
                );
                if (RelationEntity != null && RelationEntity.getAttrGroupId() != null) {
                    AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(RelationEntity.getAttrGroupId());
                    attrRespVo.setGroupName(attrGroupEntity.getAttrGroupName());
                }
            }
            // 设置分类的名字
            CategoryEntity categoryEntity = categoryDao.selectById(attrEntity.getCatelogId());
            if (categoryEntity != null) {
                attrRespVo.setCatelogName(categoryEntity.getName());
            }
            return attrRespVo;
        }).collect(Collectors.toList());
        PageUtils pageUtils = new PageUtils(page);
        pageUtils.setList(respVo);
        return pageUtils;
    }

    @Cacheable(value = "attr", key = "'attrinfo:' + #root.args[0]")
    @Override
    public AttrRespVo getAttrInfo(Long attrId) {
        AttrRespVo respVo = new AttrRespVo();
        AttrEntity attr = this.getById(attrId);
        BeanUtils.copyProperties(attr, respVo);

        // 判断是否是基本属性，如果是才会设置分组信息
        if (attr.getAttrType() == ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode()) {
            // 设置分组信息
            AttrAttrgroupRelationEntity attrAttrgroupRelation = attrAttrgroupRelationDao.selectOne(
                    new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attrId)
            );
            if (attrAttrgroupRelation != null) {
                // 设置分组ID
                respVo.setAttrGroupId(attrAttrgroupRelation.getAttrGroupId());
                AttrGroupEntity attrGroup = attrGroupDao.selectById(attrAttrgroupRelation.getAttrGroupId());
                if (attrGroup != null) {
                    // 设置分组名字
                    respVo.setGroupName(attrGroup.getAttrGroupName());
                }
            }
        }

        // 设置分类信息
        Long catelogId = attr.getCatelogId();
        // 设置分类的父子ID
        Long[] cateLogPath = categoryService.findCateLogPath(catelogId);
        respVo.setCatelogPath(cateLogPath);
        // 设置分类信息的名字
        CategoryEntity category = categoryDao.selectById(catelogId);
        if (category != null) {
            respVo.setCatelogName(category.getName());
        }

        return respVo;
    }

    @Transactional
    @Override
    public void udpateAttr(AttrVo attrVo) {
        AttrEntity attr = new AttrEntity();
        // 修改平台属性的数据
        BeanUtils.copyProperties(attrVo, attr);
        this.updateById(attr);

        // 如果是基本属性（规格参数）才会更新分组信息
        if (attr.getAttrType() == ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode()) {
            // 分组关联操作
            AttrAttrgroupRelationEntity attrAttrgroupRelation = new AttrAttrgroupRelationEntity();
            attrAttrgroupRelation.setAttrId(attrVo.getAttrId());
            attrAttrgroupRelation.setAttrGroupId(attrVo.getAttrGroupId());
            // 判断平台属性关联分组是否为空
            Integer count = attrAttrgroupRelationDao.selectCount(
                    new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attrVo.getAttrId())
            );

            // 判断平台属性是否关联了分组，如果关联了：做修改操作；否则：做新增操作
            if (count > 0) {
                // 修改分组关联
                attrAttrgroupRelationDao.update(
                        attrAttrgroupRelation,
                        new UpdateWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attrVo.getAttrId())
                );
            } else {
                // 新增分组关联
                attrAttrgroupRelationDao.insert(attrAttrgroupRelation);
            }
        }
    }

    /**
     * 根据分组Id查找关联的所有基本属性
     *
     * @param attrgroupId:分组Id
     * @return 关联的所有属性
     */
    @Override
    public List<AttrEntity> getRelationAttr(Long attrgroupId) {
        List<AttrAttrgroupRelationEntity> relationEntities = attrAttrgroupRelationDao.selectList(
                new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_group_id", attrgroupId)
        );

        List<Long> attrIds = relationEntities.stream()
                .map(AttrAttrgroupRelationEntity::getAttrId).collect(Collectors.toList());

        if (attrIds.isEmpty()) {
            return null;
        }
        return (List<AttrEntity>) this.listByIds(attrIds);
    }

    /**
     * 获取属性分组没有关联的其他属性
     *
     * @param params：分页数据
     * @param attrgroupId：当前分组Id
     * @return 当前分组没有关联的其他属性与分页信息的封装
     */
    @Override
    public PageUtils getNoRelationAttr(Map<String, Object> params, Long attrgroupId) {
        // 1、当前分组只能关联自己所属的分类里面的所有属性
        AttrGroupEntity attrGroup = attrGroupDao.selectById(attrgroupId);
        Long catelogId = attrGroup.getCatelogId();

        // 2、当前分组只能关联别的分组没有引用的其他属性
        // 2.1）、当前分类下的其他分组
        List<AttrGroupEntity> attrGroups = attrGroupDao.selectList(
                new QueryWrapper<AttrGroupEntity>()
                        .eq("catelog_id", catelogId)
        );
        List<Long> attrgroupIds = attrGroups.stream()
                .map(AttrGroupEntity::getAttrGroupId)
                .collect(Collectors.toList());

        // 2.2）、这些分组关联的属性
        List<AttrAttrgroupRelationEntity> relationEntities = attrAttrgroupRelationDao.selectList(
                new QueryWrapper<AttrAttrgroupRelationEntity>()
                        .in("attr_group_id", attrgroupIds)
        );

        List<Long> attrIds = relationEntities.stream()
                .map(AttrAttrgroupRelationEntity::getAttrId)
                .collect(Collectors.toList());

        // 2.3）、从当前分类的所有属性中移除这些属性
        QueryWrapper<AttrEntity> queryWrapper = new QueryWrapper<AttrEntity>()
                .eq("catelog_id", catelogId)
                .eq("attr_type", ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode());

        // 2.3.1）、判断空集合
        if (!attrIds.isEmpty()) {
            queryWrapper.notIn("attr_id", attrIds); // 移除其他分组关联的属性
        }

        // 3、判断模糊查询并且封装分页数据
        String key = (String) params.get("key");
        if (StringUtils.isNotEmpty(key)) {
            queryWrapper.and((wrapper) -> {
                wrapper.eq("attr_id", key).or().like("attr_name", key);
            });
        }

        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(params),
                queryWrapper
        );
        return new PageUtils(page);
    }

    @Override
    public List<Long> selectSearchAttrs(List<Long> attrIds) {
        return baseMapper.selectSearchAttrs(attrIds);
    }
}