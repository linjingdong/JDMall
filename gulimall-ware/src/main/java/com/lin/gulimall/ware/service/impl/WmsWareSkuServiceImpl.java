package com.lin.gulimall.ware.service.impl;

import com.lin.common.exception.NoStockException;
import com.lin.common.utils.R;
import com.lin.gulimall.ware.feign.ProductFeignService;
import com.lin.gulimall.ware.vo.SkuHasStockVo;
import com.lin.gulimall.ware.vo.WareSkuLockVo;
import lombok.Data;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.client.actuator.HasFeatures;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lin.common.utils.PageUtils;
import com.lin.common.utils.Query;

import com.lin.gulimall.ware.dao.WmsWareSkuDao;
import com.lin.gulimall.ware.entity.WmsWareSkuEntity;
import com.lin.gulimall.ware.service.WmsWareSkuService;
import org.springframework.transaction.annotation.Transactional;


@Service("wmsWareSkuService")
public class WmsWareSkuServiceImpl extends ServiceImpl<WmsWareSkuDao, WmsWareSkuEntity> implements WmsWareSkuService {
    @Autowired
    private WmsWareSkuDao wareSkuDao;
    @Autowired
    private ProductFeignService productFeignService;
    @Qualifier("feignFeature")
    @Autowired
    private HasFeatures feignFeature;

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
        if (entities == null || entities.isEmpty()) {
            WmsWareSkuEntity wmsWareSkuEntity = new WmsWareSkuEntity();
            wmsWareSkuEntity.setSkuId(skuId);
            wmsWareSkuEntity.setWareId(wareId);
            wmsWareSkuEntity.setStock(skuNum);
            wmsWareSkuEntity.setStockLocked(0);
            // 远程查询sku的名字，如果失败，整个事务不回滚
            try {
                R info = productFeignService.info(skuId);
                Map<String, Object> data = (Map<String, Object>) info.get("skuInfo");

                if (info.getCode() == 0) {
                    wmsWareSkuEntity.setSkuName((String) data.get("skuName"));
                }
            } catch (Exception e) {

            }
            wmsWareSkuEntity.setSkuName("");
            wareSkuDao.insert(wmsWareSkuEntity);
        } else {
            wareSkuDao.addStock(skuId, wareId, skuNum);
        }
    }

    @Override
    public List<SkuHasStockVo> getSkusHasStock(List<Long> skuIds) {
        return skuIds.stream().map(skuId -> {
            SkuHasStockVo skuHasStockVo = new SkuHasStockVo();
            // 查询当前sku的总库存量
            Long count = baseMapper.getSkuStock(skuId);
            skuHasStockVo.setSkuId(skuId);
            skuHasStockVo.setHasStock(count == null ? false : count > 0);
            return skuHasStockVo;
        }).collect(Collectors.toList());
    }

    /**
     * 存商品在哪个仓库有库存的映射
     */
    @Data
    static class SkuWareHasStock {
        private Long skuId;
        private Integer num;
        private List<Long> wareIds;
    }

    /**
     * 为订单的每一件商品锁定库存，事务会自动抛出运行时异常
     *
     * @return 是否锁定成功
     */
    @Transactional(rollbackFor = NoStockException.class)
    @Override
    public Boolean orderLockStock(WareSkuLockVo vo) {
        // 1、找到每个商品在哪个仓库有库存
        List<SkuWareHasStock> stocks = vo.getLocks().stream().map(item -> {
            SkuWareHasStock stock = new SkuWareHasStock();
            Long skuId = item.getSkuId();
            // 查询商品在哪个仓库有库存
            List<Long> wareIds = wareSkuDao.listWareIdHasSkuStock(skuId);
            stock.setSkuId(skuId);
            stock.setNum(item.getCount());
            stock.setWareIds(wareIds);
            return stock;
        }).collect(Collectors.toList());

        // 2、锁定库存
        for (SkuWareHasStock stock : stocks) {
            boolean stockFlag = false;
            List<Long> wareIds = stock.getWareIds();
            Long skuId = stock.getSkuId();
            if (null == wareIds || wareIds.isEmpty()) {
                throw new NoStockException(skuId);
            }

            for (Long wareId : wareIds) {
                Long count = wareSkuDao.lockSkuStock(skuId, wareId, stock.getNum());
                if (count == 1) {
                    stockFlag = true;
                    break;
                } else {
                    // 当前仓库锁定失败，尝试下一个仓库
                }
            }
            if (!stockFlag) {
                throw new NoStockException(skuId);
            }
        }

        // 3、肯定全部都是锁定成功的
        return true;
    }

}