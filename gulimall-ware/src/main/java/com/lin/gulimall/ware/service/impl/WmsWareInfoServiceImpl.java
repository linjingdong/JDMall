package com.lin.gulimall.ware.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.lin.common.utils.R;
import com.lin.gulimall.ware.feign.MemberFeignService;
import com.lin.gulimall.ware.vo.FareVo;
import com.lin.gulimall.ware.vo.MemberAddressVo;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
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

    @Autowired
    private MemberFeignService memberFeignService;

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

    /**
     * 获取运费等详细信息
     *
     * @param addrId 用户收货地址Id
     * @return 运费等详细信息
     */
    @Override
    public FareVo getFare(Long addrId) {
        FareVo fareVo = new FareVo();

        R r = memberFeignService.addrInfo(addrId);
        MemberAddressVo addrInfo = r.getDataByKey("umsMemberReceiveAddress", new TypeReference<MemberAddressVo>() {
        });

        if (null != addrInfo) {
            // 假设截取电话号码的最后一位作为运费
            String phone = addrInfo.getPhone();
            String fare = phone.substring(phone.length() - 1);
            fareVo.setAddressVo(addrInfo);
            fareVo.setFare(new BigDecimal(fare));
            return fareVo;
        }
        return null;
    }

}