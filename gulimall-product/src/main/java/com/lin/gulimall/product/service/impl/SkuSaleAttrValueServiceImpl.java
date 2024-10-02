package com.lin.gulimall.product.service.impl;

import com.lin.gulimall.product.vo.SkuItemSaleAttrsVo;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lin.common.utils.PageUtils;
import com.lin.common.utils.Query;

import com.lin.gulimall.product.dao.SkuSaleAttrValueDao;
import com.lin.gulimall.product.entity.SkuSaleAttrValueEntity;
import com.lin.gulimall.product.service.SkuSaleAttrValueService;


@Service("skuSaleAttrValueService")
public class SkuSaleAttrValueServiceImpl extends ServiceImpl<SkuSaleAttrValueDao, SkuSaleAttrValueEntity> implements SkuSaleAttrValueService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuSaleAttrValueEntity> page = this.page(
                new Query<SkuSaleAttrValueEntity>().getPage(params),
                new QueryWrapper<SkuSaleAttrValueEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<SkuItemSaleAttrsVo> getSaleAttrsBySpuId(Long spuId) {
        SkuSaleAttrValueDao skuSaleAttrValueDao = this.baseMapper;

        return skuSaleAttrValueDao.getSaleAttrsBySpuId(spuId);
    }

    @Override
    public List<String> getSkuSaleAttrListToString(Long skuId) {

        return this.baseMapper.getSkuSaleAttrList(skuId);
    }

}