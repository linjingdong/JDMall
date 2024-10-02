package com.lin.gulimall.auth.feign;

import com.lin.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @Description TODO
 * @Date 2024/6/28 23:44
 * @Author Lin
 * @Version 1.0
 */
@FeignClient("gulimall-third-party")
public interface ThirdPartFeignService {
    @GetMapping("/sms/sendCode")
    R sendCode(@RequestParam("phone") String phone, @RequestParam("code") String code);
}
