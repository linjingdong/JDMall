package com.lin.gulimall.ware.feign;

import com.lin.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @Description 会员FEIGN
 * @Date 2024/9/21 14:31
 * @Author Lin
 * @Version 1.0
 */

@FeignClient("gulimall-member")
public interface MemberFeignService {
    @RequestMapping("member/memberreceiveaddress/info/{id}")
    R addrInfo(@PathVariable("id") Long id);
}
