package com.lin.gulimall.member.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Objects;

/**
 * @Description TODO
 * @Date 2024/7/12 17:47
 * @Author Lin
 * @Version 1.0
 */
@Configuration
public class GulimallFeignConfig {
    @Bean("requestInterceptor")
    public RequestInterceptor requestInterceptor() {
        return new RequestInterceptor() {
            /**
             * 在发送远程调用之前，限制性当前请求拦截器当中的apply方法
             * @param requestTemplate 新请求
             */
            @Override
            public void apply(RequestTemplate requestTemplate) {
                // 1、从RequestContextHolder中获取从控制层刚进来的请求
                ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

                if(requestAttributes != null) {
                    HttpServletRequest request = Objects.requireNonNull(requestAttributes).getRequest(); // 获取到请求

                    // 2、同步请求头的数据
                    String cookie = request.getHeader("Cookie");

                    // 3、给新请求同步了老请求的cookie
                    requestTemplate.header("Cookie", cookie);
                }
            }
        };
    }
}
