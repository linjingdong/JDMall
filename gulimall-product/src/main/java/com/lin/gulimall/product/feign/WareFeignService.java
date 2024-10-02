package com.lin.gulimall.product.feign;

import com.lin.common.to.SkuHasStockVo;
import com.lin.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @Description TODO
 * @Date 2024/6/14 23:12
 * @Author Lin
 * @Version 1.0
 */
@FeignClient("gulimall-ware")
public interface WareFeignService {
    @PostMapping("/ware/waresku/hasstock")
    R getSkusHasStock(@RequestBody List<Long> skuIds);
}
