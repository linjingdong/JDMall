package com.lin.gulimall.auth.vo;

import lombok.Data;
import lombok.ToString;

/**
 * @Description TODO
 * @Date 2024/7/3 15:39
 * @Author Lin
 * @Version 1.0
 */
@ToString
@Data
public class SocialUser {
    private String accessToken;
    private String refreshToken;
    private String scope;
    private String createdAt;
    private String tokenType;
    private String expiresIn;
    private String uid;
    private String name;
}
