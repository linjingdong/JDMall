package com.lin.gulimall.auth.controller;

import com.alibaba.fastjson.TypeReference;
import com.lin.common.constant.AuthConstant;
import com.lin.common.exception.BizCodeEnum;
import com.lin.common.utils.R;
import com.lin.common.vo.MemberRespVo;
import com.lin.gulimall.auth.feign.MemberFeignService;
import com.lin.gulimall.auth.feign.ThirdPartFeignService;
import com.lin.gulimall.auth.vo.UserLoginVo;
import com.lin.gulimall.auth.vo.UserRegisterVo;
import com.mysql.cj.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.thymeleaf.util.StringUtils;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @Description TODO
 * @Date 2024/6/28 12:08
 * @Author Lin
 * @Version 1.0
 */
@Controller
public class LoginRegController {
    @Autowired
    private ThirdPartFeignService thirdPartFeignService;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private MemberFeignService memberFeignService;

    @ResponseBody
    @GetMapping("sms/sendCode")
    public R sendCode(@RequestParam("phone") String phone) {
        // TODO 1、接口防刷
        // 1.1、验证码存储在redis当中的业务逻辑
        // 1.2、在发送之前先去redis获取有没有该验证码，并且判断是否到了六十秒，若到了六十秒，则不可重新发送
        String RedisCode = redisTemplate.opsForValue().get(AuthConstant.SMS_CODE_CACHE_PREFIX + phone);
        if (!StringUtils.isEmpty(RedisCode)) { // 说明之前没有发过验证码，跳过该步骤
            long saveTime = Long.parseLong(RedisCode.split("_")[1]);
            if (System.currentTimeMillis() - saveTime <= 60000) {
                return R.error(BizCodeEnum.SMS_CODE_EXCEPTION.getCode(), BizCodeEnum.SMS_CODE_EXCEPTION.getMsg());
            }
        }

        String codeSubstring = UUID.randomUUID().toString().substring(0, 5);
        String code = codeSubstring + "_" + System.currentTimeMillis();

        // 2、 验证码的再次校验：redis(key格式:sms:code:phoneNumber)
        redisTemplate.opsForValue().set(AuthConstant.SMS_CODE_CACHE_PREFIX + phone, code, 10, TimeUnit.MINUTES);

        // 远程调用短信服务，给用户发短信
        thirdPartFeignService.sendCode(phone, codeSubstring);
        return R.ok();
    }

    /**
     * TODO 重定向携带数据，利用session原理，将数据放在session中
     * TODO 分布式下的session问题
     *  redirectAttributes：只要跳到下一个页面取到数据之后，session里面的数据就会删掉
     *
     * @param vo
     * @param result
     * @param redirectAttributes
     * @return
     */
    @PostMapping("/regist")
    public String register(@Valid UserRegisterVo vo, BindingResult result, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            Map<String, String> errors = result.getFieldErrors().stream().collect(
                    Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage)
            );
//            model.addAttribute("errors", errors);
            redirectAttributes.addFlashAttribute("errors", errors);
            return "redirect:http://auth.gulimall.com:8081/reg.html"; // 如果校验出现错误，转发到注册页面
        }

        // 1、校验验证码
        String code = vo.getCode();

        String redisCode = redisTemplate.opsForValue().get(AuthConstant.SMS_CODE_CACHE_PREFIX + vo.getPhone());
        if (!StringUtils.isEmpty(redisCode)) {
            if (code.equals(redisCode.split("_")[0])) {
                // 删除验证码
                redisTemplate.delete(AuthConstant.SMS_CODE_CACHE_PREFIX + vo.getPhone());
                // 真正注册，调用远程服务进行注册
                R r = memberFeignService.register(vo);
                if (r.getCode() == 0) {
                    return "redirect:http://auth.gulimall.com:8081/login.html"; // 注册成功返回到首页，回到登陆页面
                } else {
                    Map<String, String> errors = new HashMap<>();
                    errors.put("msg", r.getDataByKey("msg", new TypeReference<String>() {
                    }));
                    redirectAttributes.addFlashAttribute("errors", errors);
                    return "redirect:http://auth.gulimall.com:8081/reg.html";
                }

            } else {
                Map<String, String> errors = new HashMap<>();
                errors.put("code", "验证码错误");
                redirectAttributes.addFlashAttribute("errors", errors);
                return "redirect:http://auth.gulimall.com:8081/reg.html";
            }
        } else {
            Map<String, String> errors = new HashMap<>();
            errors.put("code", "验证码错误");
            redirectAttributes.addFlashAttribute("errors", errors);
            return "redirect:http://auth.gulimall.com:8081/reg.html";
        }
    }

    @GetMapping("/login.html")
    public String loginPage(HttpSession session) {
        if (session.getAttribute(AuthConstant.LOGIN_USER) == null) {
            // 表示还没登录
            return "login";
        } else {
            // 表示登录了
            return "redirect:http://gulimall.com:8081";
        }
    }

    @PostMapping("/login")
    public String login(UserLoginVo vo, RedirectAttributes redirectAttributes, HttpSession session) {
        // 发送远程登录请求
        R r = memberFeignService.login(vo);
        if (r.getCode() == 0) {
            MemberRespVo data = r.getDataByKey("data", new TypeReference<MemberRespVo>() {
            });
            // 登录成功之后放到session中
            session.setAttribute(AuthConstant.LOGIN_USER, data);

            return "redirect:http://gulimall.com:8081";
        } else {
            Map<String, String> errors = new HashMap<>();
            errors.put("msg", r.getDataByKey("msg", new TypeReference<String>() {
            }));
            redirectAttributes.addFlashAttribute("errors", errors);

            return "redirect:http://auth.gulimall.com:8081/login.html";
        }
    }
}
