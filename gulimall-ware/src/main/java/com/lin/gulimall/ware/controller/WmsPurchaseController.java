package com.lin.gulimall.ware.controller;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.lin.gulimall.ware.vo.MergeVo;
import com.lin.gulimall.ware.vo.PurchaseDoneVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.lin.gulimall.ware.entity.WmsPurchaseEntity;
import com.lin.gulimall.ware.service.WmsPurchaseService;
import com.lin.common.utils.PageUtils;
import com.lin.common.utils.R;


/**
 * 采购信息
 *
 * @author lin
 * @email lin@gmail.com
 * @date 2024-05-22 13:21:11
 */
@RestController
@RequestMapping("ware/purchase")
public class WmsPurchaseController {
    @Autowired
    private WmsPurchaseService wmsPurchaseService;

    /**
     * 完成采购单
     */
    @PostMapping("/done")
    public R done(@RequestBody PurchaseDoneVo purchaseDoneVo) {
        wmsPurchaseService.done(purchaseDoneVo);
        return R.ok();
    }

    /**
     * 领取采购单
     */
    @PostMapping("/received")
    public R received(@RequestBody List<Long> ids) {
        wmsPurchaseService.received(ids);

        return R.ok();
    }

    /**
     * 合并采购单
     */
    @PostMapping("/merge")
    public R merge(@RequestBody MergeVo mergeVo) {


        return wmsPurchaseService.merge(mergeVo);
    }

    /**
     * 查询未领取的采购单
     */
    @RequestMapping("/unreceive/list")
    //@RequiresPermissions("ware:wmspurchase:list")
    public R unReceiveList(@RequestParam Map<String, Object> params) {
        PageUtils page = wmsPurchaseService.queryPageUnReceiveList(params);

        return R.ok().put("page", page);
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("ware:wmspurchase:list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = wmsPurchaseService.queryPage(params);

        return R.ok().put("page", page);
    }

    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("ware:wmspurchase:info")
    public R info(@PathVariable("id") Long id) {
        WmsPurchaseEntity wmsPurchase = wmsPurchaseService.getById(id);

        return R.ok().put("wmsPurchase", wmsPurchase);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("ware:wmspurchase:save")
    public R save(@RequestBody WmsPurchaseEntity wmsPurchase) {
        wmsPurchase.setCreateTime(new Date());
        wmsPurchase.setUpdateTime(new Date());

        wmsPurchaseService.save(wmsPurchase);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("ware:wmspurchase:update")
    public R update(@RequestBody WmsPurchaseEntity wmsPurchase) {
        wmsPurchaseService.updateById(wmsPurchase);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("ware:wmspurchase:delete")
    public R delete(@RequestBody Long[] ids) {
        wmsPurchaseService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
