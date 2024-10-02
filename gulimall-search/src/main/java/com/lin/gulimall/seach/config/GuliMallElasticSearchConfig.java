package com.lin.gulimall.seach.config;

import org.apache.http.HttpHost;
import org.elasticsearch.client.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 1、导入依赖
 * 2、编写配置，给容器当中注入一个RestHighLevelClient
 * 3、参照API文档来使用
 * @Date 2024/6/13 21:08
 * @Author Lin
 * @Version 1.0
 */
@Configuration
public class GuliMallElasticSearchConfig {

    public static final RequestOptions COMMON_OPTIONS;
    static {
        RequestOptions.Builder builder = RequestOptions.DEFAULT.toBuilder();
//        builder.addHeader("Authorization", "Bearer " + TOKEN);
//        builder.setHttpAsyncResponseConsumerFactory(
//                new HttpAsyncResponseConsumerFactory
//                        .HeapBufferedResponseConsumerFactory(30)
//        );
        COMMON_OPTIONS = builder.build();
    }

    @Bean
    public RestHighLevelClient esRestClient() {

        return new RestHighLevelClient(RestClient.builder(
                new HttpHost("192.168.233.131", 9200, "http")
        ));
    }
}
