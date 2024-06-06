package com.lin.gulimall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lin.common.utils.PageUtils;
import com.lin.common.utils.R;
import com.lin.gulimall.ware.entity.WmsPurchaseEntity;
import com.lin.gulimall.ware.vo.MergeVo;
import com.lin.gulimall.ware.vo.PurchaseDoneVo;

import java.util.List;
import java.util.Map;

/**
 * 采购信息
 *
 * @author lin
 * @email lin@gmail.com
 * @date 2024-05-22 13:21:11
 */
public interface WmsPurchaseService extends IService<WmsPurchaseEntity> {

    PageUtils queryPage(Map<String, Object> params);

    PageUtils queryPageUnReceiveList(Map<String, Object> params);

    void mergePurchase(MergeVo mergeVo);

    void received(List<Long> ids);

    R merge(MergeVo mergeVo);

    void done(PurchaseDoneVo purchaseDoneVo);
}

