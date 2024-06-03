package com.lin.gulimall.product;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 1、整合mybatis-plus
 *      1）、导入依赖
 *      <dependency>
 *             <groupId>com.baomidou</groupId>
 *             <artifactId>mybatis-plus-boot-starter</artifactId>
 *             <version>3.2.0</version>
 *         </dependency>
 *      2）、配置
 *          1、配置数据源；
 *              1）、导入数据库的驱动
 *              2）、在application.yml配置数据源相关信息
 *          2、配置mybatis-plus；
 *              1）、使用@MapperScan
 *              2）、告诉Mypatis-plus，sql映射文件位置
 */
@EnableFeignClients("com.lin.gulimall.product.feign")
@MapperScan("com.lin.gulimall.product.dao")
@SpringBootApplication
@EnableDiscoveryClient
public class GulimallProductApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimallProductApplication.class, args);
    }

}
