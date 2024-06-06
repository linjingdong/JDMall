package com.lin.gulimall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lin.common.utils.PageUtils;
import com.lin.gulimall.ware.entity.WmsPurchaseDetailEntity;

import java.util.List;
import java.util.Map;

/**
 * 
 *
 * @author lin
 * @email lin@gmail.com
 * @date 2024-05-22 13:21:11
 */
public interface WmsPurchaseDetailService extends IService<WmsPurchaseDetailEntity> {

    PageUtils queryPage(Map<String, Object> params);

    List<WmsPurchaseDetailEntity> listDetailByPurchaseId(Long id);
}

