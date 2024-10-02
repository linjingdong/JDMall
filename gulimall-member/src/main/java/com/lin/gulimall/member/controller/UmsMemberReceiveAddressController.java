package com.lin.gulimall.member.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.lin.gulimall.member.entity.UmsMemberReceiveAddressEntity;
import com.lin.gulimall.member.service.UmsMemberReceiveAddressService;
import com.lin.common.utils.PageUtils;
import com.lin.common.utils.R;


/**
 * 会员收货地址
 *
 * @author lin
 * @email lin@gmail.com
 * @date 2024-05-22 12:47:30
 */
@RestController
@RequestMapping("member/memberreceiveaddress")
public class UmsMemberReceiveAddressController {
    @Autowired
    private UmsMemberReceiveAddressService umsMemberReceiveAddressService;

    @GetMapping("/{memberId}/address")
    public List<UmsMemberReceiveAddressEntity> getAddress(@PathVariable("memberId") String memberId) {
        return umsMemberReceiveAddressService.getAddress(memberId);
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("member:umsmemberreceiveaddress:list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = umsMemberReceiveAddressService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("member:umsmemberreceiveaddress:info")
    public R info(@PathVariable("id") Long id) {
        UmsMemberReceiveAddressEntity umsMemberReceiveAddress = umsMemberReceiveAddressService.getById(id);

        return R.ok().put("umsMemberReceiveAddress", umsMemberReceiveAddress);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("member:umsmemberreceiveaddress:save")
    public R save(@RequestBody UmsMemberReceiveAddressEntity umsMemberReceiveAddress) {
        umsMemberReceiveAddressService.save(umsMemberReceiveAddress);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("member:umsmemberreceiveaddress:update")
    public R update(@RequestBody UmsMemberReceiveAddressEntity umsMemberReceiveAddress) {
        umsMemberReceiveAddressService.updateById(umsMemberReceiveAddress);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("member:umsmemberreceiveaddress:delete")
    public R delete(@RequestBody Long[] ids) {
        umsMemberReceiveAddressService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
