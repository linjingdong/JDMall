package com.lin.gulimall.auth.controller;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.lin.common.utils.R;
import com.lin.gulimall.auth.feign.MemberFeignService;
import com.lin.common.vo.MemberRespVo;
import com.lin.gulimall.auth.vo.SocialUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.Objects;

/**
 * @Description 专门处理社交登录请求
 * @Date 2024/7/3 14:34
 * @Author Lin
 * @Version 1.0
 */
@Slf4j
@Controller
public class OAuth2Controller {
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private MemberFeignService memberFeignService;

    private final static String getAccessTokenUrl = "https://gitee.com/oauth/token";
    private final static String getSocialUserIdUrl = "https://gitee.com/api/v5/user";


    @GetMapping("/oauth2.0/gitee/success")
    public String gitee(@RequestParam("code") String code, HttpSession session) {
        LinkedMultiValueMap<String, String> request = new LinkedMultiValueMap<>();
        request.set("grant_type", "authorization_code");
        request.set("client_id", "7dbeafc3779ca38d0d987a9eb8386a009eae4cd1a1dbd3210262bfcb48bebf23");
        request.set("client_secret", "7bda1c713dfec35e99d03d3de70b7130ce7d7188e3bb1e5a8f6c7b579162e1f4");
        request.set("redirect_uri", "http://auth.gulimall.com:8081/oauth2.0/gitee/success");
        request.set("code", code);

        // 1、根据code换取accessToken
        String response = restTemplate.postForObject(getAccessTokenUrl, request, String.class);
        if (!StringUtils.isEmpty(response)) {
            // 1.1、获取accessToken
            SocialUser socialUser = JSONObject.parseObject(response, SocialUser.class);

            // 1.2、获取accessToken后，判断该用户是否第一次进本网站，若是：自动注册账号（为当前社交用户生成一个会员信息账号，以后这个社交账号就对应指定的唯一值
            String newGetSocialUserIdUrl = getSocialUserIdUrl + "?access_token=" + socialUser.getAccessToken();
            String resp = restTemplate.getForObject(newGetSocialUserIdUrl, String.class); // 获取社交账号的唯一ID
            Map<String, String> userForId = JSONObject.parseObject(resp, new TypeReference<Map<String, String>>() {
            });
            if (Objects.requireNonNull(userForId).get("id") != null) {
                System.out.println(userForId.get("id"));
                socialUser.setUid(userForId.get("id")); // 给社交用户信息设置一个唯一的标识
                socialUser.setName(userForId.get("name"));

                R r = memberFeignService.oauthLogin(socialUser); // 远程调用会员服务进行登录或者注册的逻辑

                if (r.getCode() == 0) { // 远程调用成功
                    MemberRespVo data = r.getDataByKey("data", new TypeReference<MemberRespVo>() {
                    });

                    // TODO: 1、默认发的令牌：session=daflsjg 作用域是当前域（需要解决子域session共享问题）
                    // TODO：2、使用json的序列化方式来序列化对象到redis中，方便我们查看信息

                    log.info("登陆成功，用户：{}", data);

                    session.setAttribute("loginUser", data);
                    // 2、登录成功后返回首页
                    return "redirect:http://gulimall.com:8081/";
                } else {
                    return "redirect:http://auth.gulimall.com:8081/login.html";
                }
            } else {
                return "redirect:http://auth.gulimall.com:8081/login.html";
            }
        } else {
            return "redirect:http://auth.gulimall.com:8081/login.html";
        }
    }
}
