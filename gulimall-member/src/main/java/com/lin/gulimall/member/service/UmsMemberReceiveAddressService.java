package com.lin.gulimall.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lin.common.utils.PageUtils;
import com.lin.gulimall.member.entity.UmsMemberReceiveAddressEntity;

import java.util.List;
import java.util.Map;

/**
 * 会员收货地址
 *
 * @author lin
 * @email lin@gmail.com
 * @date 2024-05-22 12:47:30
 */
public interface UmsMemberReceiveAddressService extends IService<UmsMemberReceiveAddressEntity> {

    PageUtils queryPage(Map<String, Object> params);

    List<UmsMemberReceiveAddressEntity> getAddress(String memberId);
}

