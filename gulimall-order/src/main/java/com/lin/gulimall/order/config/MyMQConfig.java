package com.lin.gulimall.order.config;


import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * @Description 队列配置
 * @Date 2024/12/3 22:46
 * @Author Lin
 * @Version 1.0
 */
@Configuration
public class MyMQConfig {
    // 订单延迟队列
    @Bean
    public Queue orderDelayQueue() {
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("x-dead-letter-exchange", "order-event-exchange"); // 死信的路由
        arguments.put("x-dead-letter-routing-key", "order.release.order"); // 死信后的路由键
        arguments.put("x-message-ttl", 1000 * 60 * 2); // 队列延迟时间
        return new Queue("order.delay.queue", true, false, false, arguments); // String name, boolean durable, boolean exclusive, boolean autoDelete, Map<String, Object> arguments
    }

    //订单释放队列
    @Bean
    public Queue orderReleaseQueue() {
        return new Queue("order.release.order.queue", true, false, false);
    }

    // 订单事件交换机
    @Bean
    public Exchange orderEventExchange() {
        return new TopicExchange("order-event-exchange", true, false);
    }

    // 创建订单绑定
    @Bean
    public Binding orderCreateOrderBinding() {
        return new Binding("order.delay.queue", Binding.DestinationType.QUEUE, "order-event-exchange", "order.create.order", null);
    }

    // 释放订单绑定
    @Bean
    public Binding orderReleaseOrderBinding() {
        return new Binding("order.release.order.queue", Binding.DestinationType.QUEUE, "order-event-exchange", "order.release.order", null);
    }

    // 订单释放直接和库存进行绑定
    @Bean
    public Binding orderReleaseOtherBinding() {
        return new Binding("stock.release.stock.queue", Binding.DestinationType.QUEUE, "order-event-exchange", "order.release.other.#", null);
    }
}
