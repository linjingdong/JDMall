package com.lin.gulimall.ware.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.lin.common.exception.NoStockException;
import com.lin.common.to.mq.StockDetailsTo;
import com.lin.common.to.mq.StockLockedTo;
import com.lin.common.utils.R;
import com.lin.gulimall.ware.entity.WmsWareOrderTaskDetailEntity;
import com.lin.gulimall.ware.entity.WmsWareOrderTaskEntity;
import com.lin.gulimall.ware.feign.OrderFeignService;
import com.lin.gulimall.ware.feign.ProductFeignService;
import com.lin.gulimall.ware.service.WmsWareOrderTaskDetailService;
import com.lin.gulimall.ware.service.WmsWareOrderTaskService;
import com.lin.gulimall.ware.vo.OrderVo;
import com.lin.gulimall.ware.vo.SkuHasStockVo;
import com.lin.gulimall.ware.vo.WareSkuLockVo;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.client.actuator.HasFeatures;
import org.springframework.stereotype.Service;

import java.io.IOException;
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


@Slf4j
@Service("wmsWareSkuService")
public class WmsWareSkuServiceImpl extends ServiceImpl<WmsWareSkuDao, WmsWareSkuEntity> implements WmsWareSkuService {
    @Autowired
    private WmsWareSkuDao wareSkuDao;
    @Autowired
    private ProductFeignService productFeignService;
    @Qualifier("feignFeature")
    @Autowired
    private HasFeatures feignFeature;
    @Autowired
    private WmsWareOrderTaskService wareOrderTaskService;
    @Autowired
    private WmsWareOrderTaskDetailService wareOrderTaskDetailService;
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private OrderFeignService orderFeignService;

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

        IPage<WmsWareSkuEntity> page = this.page(new Query<WmsWareSkuEntity>().getPage(params), queryWrapper);

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
     * 库存解锁的场景：
     * 1）、下订单成功后，订单过期没有支付被系统自动取消，被用户手动取消，都要解锁库存
     * 2）、下订单成功后，库存锁定成功，接下来的业务调用失败，导致订单回滚，之前被锁定的库存就要自动解锁
     *
     * @return 是否锁定成功
     */
    @Transactional(rollbackFor = NoStockException.class)
    @Override
    public Boolean orderLockStock(WareSkuLockVo vo) {
        /*
          首先先保存库存工作单，便于追溯
         */
        WmsWareOrderTaskEntity wareOrderTaskEntity = new WmsWareOrderTaskEntity();
        wareOrderTaskEntity.setOrderSn(vo.getOrderSn());
        wareOrderTaskService.save(wareOrderTaskEntity);

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
                // 1、如果每个商品都锁定成功了，那么就将锁定的工作单发送到MQ
                // 2、如果锁定失败了，前面保存的工作单回滚，发送消息即使要解锁，因找不到id，无需解锁
                Long count = wareSkuDao.lockSkuStock(skuId, wareId, stock.getNum());
                if (count == 1) {
                    stockFlag = true;
                    WmsWareOrderTaskDetailEntity wareOrderTaskDetailEntity = new WmsWareOrderTaskDetailEntity();
                    wareOrderTaskDetailEntity.setWareId(wareId);
                    wareOrderTaskDetailEntity.setSkuId(skuId);
                    wareOrderTaskDetailEntity.setTaskId(wareOrderTaskEntity.getId());
                    wareOrderTaskDetailEntity.setSkuNum(stock.getNum());
                    wareOrderTaskDetailEntity.setLockStatus(1);

                    wareOrderTaskDetailService.save(wareOrderTaskDetailEntity);

                    StockDetailsTo stockDetailsTo = new StockDetailsTo();
                    BeanUtils.copyProperties(wareOrderTaskDetailEntity, stockDetailsTo);

                    StockLockedTo stockLockedTo = new StockLockedTo();
                    stockLockedTo.setId(wareOrderTaskEntity.getId());
                    stockLockedTo.setStockDetailsTo(stockDetailsTo);
                    rabbitTemplate.convertAndSend("stock_event_exchange", "stock.locked", stockLockedTo);
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


    @Override
    public void unLockStock(StockLockedTo stockLockedTo) {
        StockDetailsTo detail = stockLockedTo.getStockDetailsTo();
        // 解锁
        Long detailId = detail.getId();

        WmsWareOrderTaskDetailEntity wareOrderTaskDetail = wareOrderTaskDetailService.getById(detailId);

        if (null != wareOrderTaskDetail) {
            // 锁定库存成功
            /*
            解锁：需要判断订单状况：
                1、没有这个订单，必须解锁
                2、有这个订单，判断订单状态：已取消-解锁；未取消-不可解锁
             */
            WmsWareOrderTaskEntity wareOrderTask = wareOrderTaskService.getById(wareOrderTaskDetail.getTaskId());
            // 根据订单号查询订单状态
            R orderR = orderFeignService.getOrderByOrderSn(wareOrderTask.getOrderSn());
            if (orderR.getCode() == 0) {
                // 订单信息获取成功
                OrderVo order = orderR.getData(new TypeReference<OrderVo>() {
                });
                if (null == order || order.getStatus() == 4) {
                    if (wareOrderTaskDetail.getLockStatus() == 1) {
                        // 订单不存在 || 订单被取消，解锁库存 || 库存工作单中的状态==锁定
                        unLockStock(detail.getId(), detail.getSkuId(), detail.getWareId(), detail.getSkuNum());
                    }
                }
            } else {
                throw new RuntimeException("远程调用库存服务失败...");
            }
        } else {
            // 锁定库存失败，无需解锁
            log.info("锁定库存失败，无需解锁...");
        }
    }

    private void unLockStock(Long taskDetailId, Long skuId, Long wareId, Integer num) {
        WmsWareOrderTaskDetailEntity wareOrderTaskDetailEntity = new WmsWareOrderTaskDetailEntity();
        wareOrderTaskDetailEntity.setId(taskDetailId);
        wareOrderTaskDetailEntity.setLockStatus(2);
        wareOrderTaskDetailService.updateById(wareOrderTaskDetailEntity); // 更新工作单状态
        wareSkuDao.unLockStock(skuId, wareId, num);
    }
}