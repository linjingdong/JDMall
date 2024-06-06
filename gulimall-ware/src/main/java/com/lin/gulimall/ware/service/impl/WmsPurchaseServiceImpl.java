package com.lin.gulimall.ware.service.impl;

import com.lin.common.constant.WareConstant;
import com.lin.common.utils.R;
import com.lin.gulimall.ware.entity.WmsPurchaseDetailEntity;
import com.lin.gulimall.ware.service.WmsPurchaseDetailService;
import com.lin.gulimall.ware.vo.MergeVo;
import com.lin.gulimall.ware.vo.PurchaseDoneVo;
import com.lin.gulimall.ware.vo.PurchaseItemsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lin.common.utils.PageUtils;
import com.lin.common.utils.Query;

import com.lin.gulimall.ware.dao.WmsPurchaseDao;
import com.lin.gulimall.ware.entity.WmsPurchaseEntity;
import com.lin.gulimall.ware.service.WmsPurchaseService;
import org.springframework.transaction.annotation.Transactional;


@Service("wmsPurchaseService")
public class WmsPurchaseServiceImpl extends ServiceImpl<WmsPurchaseDao, WmsPurchaseEntity> implements WmsPurchaseService {
    @Autowired
    WmsPurchaseDetailService wmsPurchaseDetailService;
    @Autowired
    WmsWareSkuServiceImpl wareSkuService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<WmsPurchaseEntity> page = this.page(new Query<WmsPurchaseEntity>().getPage(params), new QueryWrapper<WmsPurchaseEntity>());

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPageUnReceiveList(Map<String, Object> params) {


        IPage<WmsPurchaseEntity> page = this.page(new Query<WmsPurchaseEntity>().getPage(params), new QueryWrapper<WmsPurchaseEntity>().eq("status", 0).or().eq("status", 1));

        return new PageUtils(page);
    }

    @Transactional
    @Override
    public R merge(MergeVo mergeVo) {
        int flag = 0;
        List<Long> items = mergeVo.getItems();
        for (Long item : items) {
            WmsPurchaseDetailEntity byId = wmsPurchaseDetailService.getById(item);
            if (byId.getStatus() == WareConstant.PurchaseDetailEnum.CREATED.getCode()
                    || byId.getStatus() == WareConstant.PurchaseDetailEnum.ASSIGNED.getCode()) {
                flag++;
            }
        }

        if (flag == items.size()) {
            mergePurchase(mergeVo);
            return R.ok();
        } else {
            return R.error("该采购已被分配");
        }
    }

    @Transactional
    @Override
    public void done(PurchaseDoneVo purchaseDoneVo) {
        Long id = purchaseDoneVo.getId();

        // 1、改变采购项状态
        Boolean flag = true;
        List<PurchaseItemsVo> items = purchaseDoneVo.getItems();
        List<WmsPurchaseDetailEntity> updates = new ArrayList<>();
        for (PurchaseItemsVo item : items) {
            WmsPurchaseDetailEntity detailEntity = new WmsPurchaseDetailEntity();
            if (item.getStatus() == WareConstant.PurchaseDetailEnum.HASEEROR.getCode()) {
                flag = false;
                detailEntity.setStatus(item.getStatus());
            } else {
                detailEntity.setStatus(WareConstant.PurchaseDetailEnum.FINISH.getCode());
                // 3、将成功采购的进行入库
                WmsPurchaseDetailEntity purchaseDetail = wmsPurchaseDetailService.getById(item.getItemId());
                wareSkuService.addStock(purchaseDetail.getSkuId(), purchaseDetail.getWareId(), purchaseDetail.getSkuNum());
            }

            detailEntity.setId(item.getItemId());

            updates.add(detailEntity);
        }

        wmsPurchaseDetailService.updateBatchById(updates);

        // 2、改变采购单状态
        WmsPurchaseEntity purchaseEntity = new WmsPurchaseEntity();
        purchaseEntity.setId(id);
        purchaseEntity.setStatus(flag ? WareConstant.PurchaseEnum.FINISH.getCode() : WareConstant.PurchaseEnum.HASEEROR.getCode());
        purchaseEntity.setUpdateTime(new Date());

        this.updateById(purchaseEntity);

    }

    @Override
    public void mergePurchase(MergeVo mergeVo) {

        Long purchaseId = mergeVo.getPurchaseId();

        if (purchaseId == null) {
            // 没有订单Id的时候先新建一个
            WmsPurchaseEntity purchaseEntity = new WmsPurchaseEntity();
            purchaseEntity.setStatus(WareConstant.PurchaseEnum.CREATED.getCode());
            purchaseEntity.setCreateTime(new Date());
            purchaseEntity.setUpdateTime(new Date());

            this.save(purchaseEntity);

            purchaseId = purchaseEntity.getId();
        }


        // 确认采购单状态是0或者1
        List<Long> items = mergeVo.getItems();

        Long finalPurchaseId = purchaseId;
        List<WmsPurchaseDetailEntity> collect = items.stream().map(i -> {
            WmsPurchaseDetailEntity purchaseDetailEntity = new WmsPurchaseDetailEntity();
            purchaseDetailEntity.setId(i);
            purchaseDetailEntity.setPurchaseId(finalPurchaseId);
            purchaseDetailEntity.setStatus(WareConstant.PurchaseDetailEnum.ASSIGNED.getCode());
            return purchaseDetailEntity;
        }).collect(Collectors.toList());

        wmsPurchaseDetailService.updateBatchById(collect);


        WmsPurchaseEntity wmsPurchaseEntity = new WmsPurchaseEntity();
        wmsPurchaseEntity.setId(purchaseId);
        wmsPurchaseEntity.setUpdateTime(new Date());

        this.updateById(wmsPurchaseEntity);
    }

    @Override
    public void received(List<Long> ids) {
        // 1、确认当前采购单是新建或已领取的状态
        List<WmsPurchaseEntity> collect = ids.stream().map(this::getById).filter(item -> {
            return item.getStatus() == WareConstant.PurchaseEnum.CREATED.getCode() || item.getStatus() == WareConstant.PurchaseEnum.ASSIGNED.getCode();
        }).map(item -> {
            item.setStatus(WareConstant.PurchaseEnum.RECEIVE.getCode());
            item.setUpdateTime(new Date());
            return item;
        }).collect(Collectors.toList());

        // 2、改变采购单的状态
        this.updateBatchById(collect);

        // 3、改变采购项的状态
        collect.forEach(item -> {
            List<WmsPurchaseDetailEntity> entities = wmsPurchaseDetailService.listDetailByPurchaseId(item.getId());
            List<WmsPurchaseDetailEntity> purchaseDetailCollect = entities.stream().map(entity -> {
                WmsPurchaseDetailEntity purchaseDetailEntity = new WmsPurchaseDetailEntity();
                purchaseDetailEntity.setId(entity.getId());
                purchaseDetailEntity.setStatus(WareConstant.PurchaseDetailEnum.BUYING.getCode());
                return purchaseDetailEntity;
            }).collect(Collectors.toList());
            wmsPurchaseDetailService.updateBatchById(purchaseDetailCollect);
        });
    }

}