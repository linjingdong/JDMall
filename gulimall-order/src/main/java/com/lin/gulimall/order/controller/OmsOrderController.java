package com.lin.gulimall.order.controller;

import java.util.Arrays;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.lin.gulimall.order.entity.OmsOrderEntity;
import com.lin.gulimall.order.service.OmsOrderService;
import com.lin.common.utils.PageUtils;
import com.lin.common.utils.R;


/**
 * 订单
 *
 * @author lin
 * @email lin@gmail.com
 * @date 2024-05-22 13:09:20
 */
@RestController
@RequestMapping("order/omsorder")
public class OmsOrderController {
    @Autowired
    private OmsOrderService omsOrderService;

    // 根据订单号【orderSn】获取订单信息
    @GetMapping("/order/{orderSn}")
    public R getOrderByOrderSn(@PathVariable("orderSn") String orderSn) {
        OmsOrderEntity order = omsOrderService.getOrderByOrderSn(orderSn);
        return R.ok().setData(order);
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("order:omsorder:list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = omsOrderService.queryPage(params);

        return R.ok().put("page", page);
    }

    /**
     * 分页查询当前用户的订单信息
     * @param params 分页参数
     * @return 分页查询的订单数据
     */
    @PostMapping("/listWithItem")
    public R listWithItem(@RequestBody Map<String, Object> params) {
        PageUtils page = omsOrderService.queryOrderWithItem(params);
        return R.ok().put("page", page);
    }

    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("order:omsorder:info")
    public R info(@PathVariable("id") Long id) {
        OmsOrderEntity omsOrder = omsOrderService.getById(id);

        return R.ok().put("omsOrder", omsOrder);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("order:omsorder:save")
    public R save(@RequestBody OmsOrderEntity omsOrder) {
        omsOrderService.save(omsOrder);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("order:omsorder:update")
    public R update(@RequestBody OmsOrderEntity omsOrder) {
        omsOrderService.updateById(omsOrder);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("order:omsorder:delete")
    public R delete(@RequestBody Long[] ids) {
        omsOrderService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
