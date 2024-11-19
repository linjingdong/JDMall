package com.lin.gulimall.order.feign;

import com.lin.common.utils.R;
import com.lin.gulimall.order.vo.WareSkuLockVo;
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
public interface WmsFeignService {
    @PostMapping("ware/waresku/hasstock")
    R getSkusHasStock(@RequestBody List<Long> skuIds);

    @GetMapping("ware/wareinfo/fare")
    R getFare(@RequestParam("addrId") Long addrId);

    @PostMapping("ware/waresku/lock/order")
    R orderLockStock(@RequestBody WareSkuLockVo vo);
}
