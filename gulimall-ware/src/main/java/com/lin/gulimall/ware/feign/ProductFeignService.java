package com.lin.gulimall.ware.feign;

import com.lin.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @Description TODO
 * @Date 2024/6/4 23:31
 * @Author Lin
 * @Version 1.0
 */
@FeignClient("gulimall-product")
public interface ProductFeignService {
    /**
     *
     * @param skuId
     * @return
     */
    @RequestMapping("/product/skuinfo/info/{skuId}")
    public R info(@PathVariable("skuId") Long skuId);
}
