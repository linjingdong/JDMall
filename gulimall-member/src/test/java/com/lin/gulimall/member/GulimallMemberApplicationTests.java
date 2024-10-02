package com.lin.gulimall.member;


import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Test;

public class GulimallMemberApplicationTests {
    @Test
    public void contextLoads() {

    }

    @Test
    public void MD5Test() {
        String s = DigestUtils.md5Hex("123456");
        System.out.println(s);
    }

}
