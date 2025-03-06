package com.lin.gulimall.member.feign;

import com.lin.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient("gulimall-order")
public interface OrderFeignService {
    /**
     * 远程分页查询当前用户的订单列表
     */
    @PostMapping("order/omsorder/listWithItem")
    R listWithItem(@RequestBody Map<String, Object> params);
}
