package com.lin.gulimall.ware.service.impl;

import com.lin.common.utils.R;
import com.lin.gulimall.ware.feign.ProductFeignService;
import com.sun.javaws.exceptions.ExitException;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lin.common.utils.PageUtils;
import com.lin.common.utils.Query;

import com.lin.gulimall.ware.dao.WmsWareSkuDao;
import com.lin.gulimall.ware.entity.WmsWareSkuEntity;
import com.lin.gulimall.ware.service.WmsWareSkuService;


@Service("wmsWareSkuService")
public class WmsWareSkuServiceImpl extends ServiceImpl<WmsWareSkuDao, WmsWareSkuEntity> implements WmsWareSkuService {
    @Autowired
    private WmsWareSkuDao wareSkuDao;
    @Autowired
    private ProductFeignService productFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<WmsWareSkuEntity> queryWrapper = new QueryWrapper<>();

        String wareId = (String) params.get("wareId");
        if (StringUtils.isNotEmpty(wareId)) {
            queryWrapper.eq("ware_id", wareId);
        }

        String skuId = (String) params.get("skuId");
        if (StringUtils.isNotEmpty(skuId)) {
            queryWrapper.eq("sku_id", skuId);
        }

        IPage<WmsWareSkuEntity> page = this.page(
                new Query<WmsWareSkuEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

    @Override
    public void addStock(Long skuId, Long wareId, Integer skuNum) {
        List<WmsWareSkuEntity> entities = wareSkuDao.selectList(new QueryWrapper<WmsWareSkuEntity>().eq("sku_id", skuId).eq("ware_id", wareId));
        if(entities == null || entities.isEmpty()){
            WmsWareSkuEntity wmsWareSkuEntity = new WmsWareSkuEntity();
            wmsWareSkuEntity.setSkuId(skuId);
            wmsWareSkuEntity.setWareId(wareId);
            wmsWareSkuEntity.setStock(skuNum);
            wmsWareSkuEntity.setStockLocked(0);
            // 远程查询sku的名字，如果失败，整个事务不回滚
            try {
                R info = productFeignService.info(skuId);
                Map<String, Object> data = (Map<String, Object>) info.get("skuInfo");

                if(info.getCode() == 0) {
                    wmsWareSkuEntity.setSkuName((String) data.get("skuName"));
                }
            } catch (Exception e) {

            }
            wmsWareSkuEntity.setSkuName("");
            wareSkuDao.insert(wmsWareSkuEntity);
        }else {
            wareSkuDao.addStock(skuId, wareId, skuNum);
        }
    }

}