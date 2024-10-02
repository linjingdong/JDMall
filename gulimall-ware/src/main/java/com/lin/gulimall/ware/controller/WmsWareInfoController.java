package com.lin.gulimall.ware.controller;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Map;

import com.lin.gulimall.ware.vo.FareVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.lin.gulimall.ware.entity.WmsWareInfoEntity;
import com.lin.gulimall.ware.service.WmsWareInfoService;
import com.lin.common.utils.PageUtils;
import com.lin.common.utils.R;


/**
 * 仓库信息
 *
 * @author lin
 * @email lin@gmail.com
 * @date 2024-05-22 13:21:11
 */
@RestController
@RequestMapping("ware/wareinfo")
public class WmsWareInfoController {
    @Autowired
    private WmsWareInfoService wmsWareInfoService;

    /**
     * 获取运费等详细信息
     *
     * @param addrId 用户地址Id
     * @return 运费等详细信息
     */
    @GetMapping("/fare")
    public R getFare(@RequestParam("addrId") Long addrId) {
        FareVo fareVo = wmsWareInfoService.getFare(addrId);
        return R.ok().setData(fareVo);
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("ware:wmswareinfo:list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = wmsWareInfoService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("ware:wmswareinfo:info")
    public R info(@PathVariable("id") Long id) {
        WmsWareInfoEntity wmsWareInfo = wmsWareInfoService.getById(id);

        return R.ok().put("wmsWareInfo", wmsWareInfo);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("ware:wmswareinfo:save")
    public R save(@RequestBody WmsWareInfoEntity wmsWareInfo) {
        wmsWareInfoService.save(wmsWareInfo);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("ware:wmswareinfo:update")
    public R update(@RequestBody WmsWareInfoEntity wmsWareInfo) {
        wmsWareInfoService.updateById(wmsWareInfo);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("ware:wmswareinfo:delete")
    public R delete(@RequestBody Long[] ids) {
        wmsWareInfoService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
