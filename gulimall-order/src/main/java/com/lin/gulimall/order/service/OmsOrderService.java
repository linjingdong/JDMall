package com.lin.gulimall.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lin.common.utils.PageUtils;
import com.lin.gulimall.order.entity.OmsOrderEntity;
import com.lin.gulimall.order.vo.OrderConfirmVo;
import com.lin.gulimall.order.vo.OrderSubmitVo;
import com.lin.gulimall.order.vo.SubmitOrderResponseVo;

import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * 订单
 *
 * @author lin
 * @email lin@gmail.com
 * @date 2024-05-22 13:09:20
 */
public interface OmsOrderService extends IService<OmsOrderEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * @return 订单确认页返回需要用的数据
     */
    OrderConfirmVo confirmOrder() throws ExecutionException, InterruptedException;

    /**
     * 下单
     *
     * @param vo 下单是提交的数据
     */
    SubmitOrderResponseVo submitOrder(OrderSubmitVo vo);

    OmsOrderEntity getOrderByOrderSn(String orderSn);
}

