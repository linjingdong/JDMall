package com.lin.gulimall.ware.service.impl;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lin.common.utils.PageUtils;
import com.lin.common.utils.Query;

import com.lin.gulimall.ware.dao.WmsWareInfoDao;
import com.lin.gulimall.ware.entity.WmsWareInfoEntity;
import com.lin.gulimall.ware.service.WmsWareInfoService;


@Service("wmsWareInfoService")
public class WmsWareInfoServiceImpl extends ServiceImpl<WmsWareInfoDao, WmsWareInfoEntity> implements WmsWareInfoService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<WmsWareInfoEntity> queryWrapper = new QueryWrapper<>();
        String key = (String) params.get("key");
        if (StringUtils.isNotEmpty(key)) {
            queryWrapper.and(wrapper -> {
                queryWrapper.eq("id", key).or()
                        .like("name", key).or()
                        .eq("address", key).or()
                        .eq("areacode", key);
            });
        }

        IPage<WmsWareInfoEntity> page = this.page(
                new Query<WmsWareInfoEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

}