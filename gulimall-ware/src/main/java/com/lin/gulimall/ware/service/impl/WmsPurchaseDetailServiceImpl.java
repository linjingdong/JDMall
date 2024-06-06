package com.lin.gulimall.ware.service.impl;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lin.common.utils.PageUtils;
import com.lin.common.utils.Query;

import com.lin.gulimall.ware.dao.WmsPurchaseDetailDao;
import com.lin.gulimall.ware.entity.WmsPurchaseDetailEntity;
import com.lin.gulimall.ware.service.WmsPurchaseDetailService;


@Service("wmsPurchaseDetailService")
public class WmsPurchaseDetailServiceImpl extends ServiceImpl<WmsPurchaseDetailDao, WmsPurchaseDetailEntity> implements WmsPurchaseDetailService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<WmsPurchaseDetailEntity> queryWrapper = new QueryWrapper<>();
        String key = (String) params.get("key");
        if (StringUtils.isNotEmpty(key)) {
            queryWrapper.and(wrapper -> {
                wrapper.eq("purchase_id", key).or().eq("sku_id", key);
            });
        }

        String status = (String) params.get("status");
        if (StringUtils.isNotEmpty(status)) {
            queryWrapper.eq("status", status);
        }

        String wareId = (String) params.get("ware_id");
        if (StringUtils.isNotEmpty(wareId)) {
            queryWrapper.eq("ware_id", wareId);
        }

        IPage<WmsPurchaseDetailEntity> page = this.page(
                new Query<WmsPurchaseDetailEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

    @Override
    public List<WmsPurchaseDetailEntity> listDetailByPurchaseId(Long id) {
        List<WmsPurchaseDetailEntity> purchaseDetail = this.list(
                new QueryWrapper<WmsPurchaseDetailEntity>()
                        .eq("purchase_id", id));
        return purchaseDetail;
    }

}