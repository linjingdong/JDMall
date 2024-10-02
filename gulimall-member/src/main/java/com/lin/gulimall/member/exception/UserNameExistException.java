package com.lin.gulimall.member.exception;

/**
 * @Description TODO
 * @Date 2024/6/29 19:31
 * @Author Lin
 * @Version 1.0
 */
public class UserNameExistException extends RuntimeException{
    public UserNameExistException() {
        super("用户名存在");
    }
}
