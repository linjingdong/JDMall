package com.lin.gulimall.order.feign;

import com.lin.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @Description 商品服务远程调用
 * @Date 2024/10/7 20:49
 * @Author Lin
 * @Version 1.0
 */
@FeignClient("gulimall-product")
public interface ProductFeignService {
    @GetMapping("product/spuinfo/skuId/{id}")
    R getSpuInfoBySkuId(@PathVariable("id") Long id);
}
