package com.lin.gulimall.auth.feign;

import com.lin.common.utils.R;
import com.lin.gulimall.auth.vo.SocialUser;
import com.lin.gulimall.auth.vo.UserLoginVo;
import com.lin.gulimall.auth.vo.UserRegisterVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @Description TODO
 * @Date 2024/7/2 10:15
 * @Author Lin
 * @Version 1.0
 */
@FeignClient("gulimall-member")
public interface MemberFeignService {
    @PostMapping("/member/member/regist")
    R register(@RequestBody UserRegisterVo vo);

    @PostMapping("/member/member/login")
    R login(@RequestBody UserLoginVo vo);

    @PostMapping("/member/member/oauth2/login")
    R oauthLogin(@RequestBody SocialUser vo);
}
