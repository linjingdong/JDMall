package com.lin.gulimall.thirdparty.controller;

import com.lin.common.utils.R;
import com.lin.gulimall.thirdparty.component.SmsComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Description TODO
 * @Date 2024/6/28 23:31
 * @Author Lin
 * @Version 1.0
 */
@RestController
@RequestMapping("/sms")
public class SmsSendController {

    @Autowired
    SmsComponent smsComponent;

    @GetMapping("/sendCode")
    public R sendCode(@RequestParam("phone") String phone, @RequestParam("code") String code) {
        try {
            smsComponent.sendSms(phone, code);
        } catch (Exception e) {
            System.out.println("验证码发送失败，失败原因{}");
        }
        return R.ok();
    }
}
