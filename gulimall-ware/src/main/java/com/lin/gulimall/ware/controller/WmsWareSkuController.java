package com.lin.gulimall.ware.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.lin.gulimall.ware.vo.SkuHasStockVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.lin.gulimall.ware.entity.WmsWareSkuEntity;
import com.lin.gulimall.ware.service.WmsWareSkuService;
import com.lin.common.utils.PageUtils;
import com.lin.common.utils.R;


/**
 * 商品库存
 *
 * @author lin
 * @email lin@gmail.com
 * @date 2024-05-22 13:21:11
 */
@RestController
@RequestMapping("ware/waresku")
public class WmsWareSkuController {
    @Autowired
    private WmsWareSkuService wmsWareSkuService;

    // 查询sku是否有库存
    @PostMapping("/hasstock")
    public R getSkusHasStock(@RequestBody List<Long> skuIds) {

        List<SkuHasStockVo> vos = wmsWareSkuService.getSkusHasStock(skuIds);

        return R.ok().setData(vos);
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("ware:wmswaresku:list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = wmsWareSkuService.queryPage(params);

        return R.ok().put("page", page);
    }

    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("ware:wmswaresku:info")
    public R info(@PathVariable("id") Long id) {
        WmsWareSkuEntity wmsWareSku = wmsWareSkuService.getById(id);

        return R.ok().put("wmsWareSku", wmsWareSku);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("ware:wmswaresku:save")
    public R save(@RequestBody WmsWareSkuEntity wmsWareSku) {
        wmsWareSkuService.save(wmsWareSku);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("ware:wmswaresku:update")
    public R update(@RequestBody WmsWareSkuEntity wmsWareSku) {
        wmsWareSkuService.updateById(wmsWareSku);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("ware:wmswaresku:delete")
    public R delete(@RequestBody Long[] ids) {
        wmsWareSkuService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
