package com.lin.gulimall.auth.vo;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

/**
 * @Description TODO
 * @Date 2024/6/29 11:23
 * @Author Lin
 * @Version 1.0
 */
@Data
public class UserRegisterVo {
    @NotEmpty(message = "用户名必须填写！")
    @Length(min = 6, max = 18, message = "用户名必须是6-18位字符")
    private String username;

    @NotEmpty(message = "密码必须填写！")
    @Length(min = 6, max = 18, message = "密码必须是6-18位字符")
    private String password;

    @NotEmpty(message = "手机号必须填写！")
    @Pattern(regexp = "^[1]([3-9])[0-9]{9}$", message = "手机号格式不正确")
    private String phone;

    @NotEmpty(message = "验证码必须填写！")
    private String code; // 验证码
}
