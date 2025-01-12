package com.lin.gulimall.order.config;

import com.lin.common.constant.AuthConstant;
import com.lin.common.vo.MemberRespVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @Date 2024/7/11 15:34
 * @Author Lin
 * @Version 1.0
 */
@Component
public class LoginUserInterceptor implements HandlerInterceptor {
    public static final ThreadLocal<MemberRespVo> threadLocal = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        MemberRespVo attribute = (MemberRespVo) request.getSession().getAttribute(AuthConstant.LOGIN_USER);

        // 匹配路径放行
        String uri = request.getRequestURI();
        boolean match = new AntPathMatcher().match("/order/omsorder/order/**", uri);
        if (match) {
            return true;
        }

        if (attribute != null) {
            threadLocal.set(attribute);
            return true;
        } else {
            request.getSession().setAttribute("msg", "请先进行登录！");
            response.sendRedirect("http://auth.gulimall.com:8081/login.html");
            return false;
        }
    }
}
