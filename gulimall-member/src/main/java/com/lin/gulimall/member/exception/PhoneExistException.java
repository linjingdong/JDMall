package com.lin.gulimall.member.exception;

/**
 * @Description TODO
 * @Date 2024/6/29 19:30
 * @Author Lin
 * @Version 1.0
 */
public class PhoneExistException extends RuntimeException{
    public PhoneExistException() {
        super("手机号存在");
    }
}
