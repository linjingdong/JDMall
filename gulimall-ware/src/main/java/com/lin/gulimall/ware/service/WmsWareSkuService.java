package com.lin.gulimall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lin.common.to.OrderTo;
import com.lin.common.to.mq.StockLockedTo;
import com.lin.common.utils.PageUtils;
import com.lin.gulimall.ware.entity.WmsWareSkuEntity;
import com.lin.gulimall.ware.vo.LockStockResult;
import com.lin.gulimall.ware.vo.SkuHasStockVo;
import com.lin.gulimall.ware.vo.WareSkuLockVo;

import java.util.List;
import java.util.Map;

/**
 * 商品库存
 *
 * @author lin
 * @email lin@gmail.com
 * @date 2024-05-22 13:21:11
 */
public interface WmsWareSkuService extends IService<WmsWareSkuEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void addStock(Long skuId, Long wareId, Integer skuNum);

    List<SkuHasStockVo> getSkusHasStock(List<Long> skuIds);

    Boolean orderLockStock(WareSkuLockVo vo);

    void unLockStock(StockLockedTo stockLockedTo);

    void unLockStock(OrderTo orderTo);
}

