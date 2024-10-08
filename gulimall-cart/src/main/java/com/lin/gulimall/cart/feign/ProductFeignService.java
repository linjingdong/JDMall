package com.lin.gulimall.cart.feign;

import com.lin.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.util.List;

/**
 * @Description TODO
 * @Date 2024/7/7 15:32
 * @Author Lin
 * @Version 1.0
 */
@FeignClient("gulimall-product")
public interface ProductFeignService {
    @RequestMapping("/product/skuinfo/info/{skuId}")
    R getSkuInfo(@PathVariable("skuId") Long skuId);

    @GetMapping("/product/skusaleattrvalue/stringlist/{skuId}")
    List<String> getSkuSaleAttrList(@PathVariable("skuId") Long skuId);

    @GetMapping("product/skuinfo/price/{skuId}")
    BigDecimal getPrice(@PathVariable("skuId") Long skuId);
}
