package com.lin.gulimall.order.feign;

import com.lin.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @Description TODO
 * @Date 2024/7/15 16:44
 * @Author Lin
 * @Version 1.0
 */
@FeignClient("gulimall-ware")
@RequestMapping("/ware/wareinfo")
public interface WmsFeignService {
    @PostMapping("/hasstock")
    R getSkusHasStock(@RequestBody List<Long> skuIds);

    @GetMapping("/fare")
    R getFare(@RequestParam("addrId") Long addrId);
}
