package com.lin.gulimall.cart.interceptor;

import com.lin.common.constant.AuthConstant;
import com.lin.common.constant.CartConstant;
import com.lin.common.vo.MemberRespVo;
import com.lin.gulimall.cart.vo.UserInfoTo;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.thymeleaf.util.StringUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.UUID;

/**
 * @Description 在执行目标任务之前，判断用户的登录状态，并封装传递给controller目标请求
 * @Date 2024/7/6 16:46
 * @Author Lin
 * @Version 1.0
 */
@Component
public class CartInterceptor implements HandlerInterceptor {
    // 通过ThreadLocal共享同一个线程的数据
    public static final ThreadLocal<UserInfoTo> threadLocal = new ThreadLocal<>();

    // 目标方法执行之前
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        UserInfoTo userInfo = new UserInfoTo();
        HttpSession session = request.getSession();
        MemberRespVo member = (MemberRespVo) session.getAttribute(AuthConstant.LOGIN_USER);
        if (member != null) {
            // 用户登录，获取用户的用户Id
            userInfo.setUserId(member.getId());
        }

        Cookie[] cookies = request.getCookies();
        if (cookies != null) { // 获取user-key
            for (Cookie cookie : cookies) {
                String name = cookie.getName();
                if (name.equals(CartConstant.TEMP_USER_COOKIE_NAME)) {
                    userInfo.setUserKey(cookie.getValue());
                    userInfo.setTempUser(true);
                }
            }
        }

        // 给临时用户分配一个user-key
        if (StringUtils.isEmpty(userInfo.getUserKey())) {
            String userKey = UUID.randomUUID().toString();
            userInfo.setUserKey(userKey);
        }

        // 发送到执行目标之前先保存需要共享同一线程的数据
        threadLocal.set(userInfo);
        return true;
    }

    /**
     *  在执行业务之后，需要给临时用户的cookie添加上一个user-key
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        UserInfoTo userInfoTo = threadLocal.get();

        // 给临时用户分配一个user-key
        if(!userInfoTo.isTempUser()) {
            Cookie cookie = new Cookie(CartConstant.TEMP_USER_COOKIE_NAME, userInfoTo.getUserKey());
            cookie.setDomain("gulimall.com");
            cookie.setMaxAge(CartConstant.TEMP_USER_COOKIE_TIMEOUT);
            response.addCookie(cookie);
        }
    }
}
