package com.lin.gulimall.product.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

/**
 * @Date 2024/6/18 22:05
 * @Author Lin
 * @Version 1.0
 */
@Configuration
public class MyRedissonConfig {
    /**
     * 所有对Redisson的使用都是通过RedissonClient这个对象
     * @return redissonClient
     */
    @Bean(destroyMethod = "shutdown")
    public RedissonClient redisson() throws IOException {
        // 创建配置：单个redis配置
        Config config = new Config();
        config.useSingleServer().setAddress("redis://192.168.233.131:6379");
        // 根据config创建redisson实例
        return Redisson.create(config);
    }
}

