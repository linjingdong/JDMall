package com.lin.gulimall.order.feign;

import com.lin.gulimall.order.vo.OrderItemVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

/**
 * @Description TODO
 * @Date 2024/7/11 17:49
 * @Author Lin
 * @Version 1.0
 */
@FeignClient("gulimall-cart")
public interface CartFeignService {
    @GetMapping("/currentUserItems")
    List<OrderItemVo> getCurrentUserItems();
}
