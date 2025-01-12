package com.lin.gulimall.ware.feign;

import com.lin.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @Description 订单远程调用接口
 * @Date 2025/1/9 21:45
 * @Author Lin
 * @Version 1.0
 */
@FeignClient("gulimall-order")
public interface OrderFeignService {
    @GetMapping("/order/omsorder/order/{orderSn}")
    R getOrderByOrderSn(@PathVariable("orderSn") String orderSn);
}
