package com.lin.gulimall.cart.vo;

import lombok.Data;

/**
 * @Description TODO
 * @Date 2024/7/6 16:51
 * @Author Lin
 * @Version 1.0
 */
@Data
public class UserInfoTo {
    private Long userId;
    private String userKey;
    private boolean tempUser = false; // 如果设置了user-key：true
}
