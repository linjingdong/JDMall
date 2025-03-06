package com.lin.gulimall.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lin.common.utils.PageUtils;
import com.lin.gulimall.order.entity.OmsOrderEntity;
import com.lin.gulimall.order.vo.*;

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

    /**
     * 关闭订单操作
     * @param order 需要关闭的订单
     */
    void closeOrder(OmsOrderEntity order);

    /**
     * 获取当前订单的支付信息
     * @param orderSn 订单号
     * @return 订单信息
     */
    PayVo getOrderPay(String orderSn);

    PageUtils queryOrderWithItem(Map<String, Object> params);

    String handlePayResult(PayAsyncVo vo);
}

