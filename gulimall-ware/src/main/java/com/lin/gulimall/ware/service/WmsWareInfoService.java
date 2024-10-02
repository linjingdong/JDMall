package com.lin.gulimall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lin.common.utils.PageUtils;
import com.lin.gulimall.ware.entity.WmsWareInfoEntity;
import com.lin.gulimall.ware.vo.FareVo;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 仓库信息
 *
 * @author lin
 * @email lin@gmail.com
 * @date 2024-05-22 13:21:11
 */
public interface WmsWareInfoService extends IService<WmsWareInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 运费等详细信息
     *
     * @param addrId 用户收货地址Id
     * @return 运费等详细信息
     */
    FareVo getFare(Long addrId);
}

