package com.lin.gulimall.product.feign;

import com.lin.common.to.es.SkuEsModel;
import com.lin.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @Description TODO
 * @Date 2024/6/15 23:49
 * @Author Lin
 * @Version 1.0
 */
@FeignClient("gulimall-search")
public interface SearchFeignService {
    @PostMapping("/search/save/product")
    R productStatusUp(@RequestBody List<SkuEsModel> skuEsModels);
}
