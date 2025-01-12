package com.lin.gulimall.ware.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * @Description rabbitMq配置
 * @Date 2025/1/6 16:55
 * @Author Lin
 * @Version 1.0
 */

@Configuration
public class MyRabbitConfig {
    /**
     * 使用JSON序列化机制，进行消息转换
     */
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    // 库存交换机
    @Bean
    public Exchange stockEventExchange() {
        return new TopicExchange("stock_event_exchange", true, false);
    }

    // 库存释放队列
    @Bean
    public Queue stockReleaseStockQueue() {
        return new Queue("stock.release.stock.queue", true, false, false);
    }

    // 库存延时队列
    @Bean
    public Queue stockDelayQueue() {
        // 配置死信队列参数：x-dead-letter-exchange、x-dead-letter-routing-key、x-message-ttl
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", "stock_event_exchange");
        args.put("x-dead-letter-routing-key", "stock.release.stock");
        args.put("x-message-ttl", 2*60*1000);

        return new Queue("stock.delay.queue", true, false, false, args);
    }

    @Bean
    public Binding stockReleaseBinding() {
        return new Binding(
                "stock.release.stock.queue",
                Binding.DestinationType.QUEUE,
                "stock_event_exchange",
                "stock.release.#",
                null);
    }

    @Bean
    public Binding stockLockedBinding() {
        return new Binding(
                "stock.delay.queue",
                Binding.DestinationType.QUEUE,
                "stock_event_exchange",
                "stock.locked",
                null);
    }
}

