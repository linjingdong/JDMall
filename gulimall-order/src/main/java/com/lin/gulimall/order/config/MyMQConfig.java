package com.lin.gulimall.order.config;


import com.lin.gulimall.order.entity.OmsOrderEntity;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Bean;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @Description 队列配置
 * @Date 2024/12/3 22:46
 * @Author Lin
 * @Version 1.0
 */
public class MyMQConfig {
    /**
     * 模拟监听消息
     */

    @RabbitListener(queues = "order.release.queue")
    public void listener(OmsOrderEntity entity, Channel channel, Message message) throws IOException {
        System.out.println("收到过期的订单信息，准备关闭订单：" + entity.getOrderSn());
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }


    // 订单延迟队列
    @Bean
    public Queue orderDelayQueue() {
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("x-dead-letter-exchange", "order-event-exchange"); // 死信的路由
        arguments.put("x-dead-letter-routing-key", "order.release.order"); // 死信后的路由键
        arguments.put("x-message-ttl", 60000); // 队列延迟时间
        //String name, boolean durable, boolean exclusive, boolean autoDelete, Map<String, Object> arguments
        return new Queue("order.delay.queue", true, false, false, arguments);
    }

    //订单释放队列
    @Bean
    public Queue orderReleaseQueue() {
        return new Queue("order.release.queue", true, false, false);
    }

    // 订单事件交换机
    @Bean
    public Exchange orderEventExchange() {
        return new TopicExchange("order-event-exchange", true, false);
    }

    // 创建订单绑定
    @Bean
    public Binding orderCreateOrderBinding() {
        return new Binding("order.delay.order", Binding.DestinationType.QUEUE, "order-event-exchange", "order.create.order", null);
    }

    // 释放订单绑定
    @Bean
    public Binding orderReleaseOrderBinding() {
        return new Binding("order.release.order", Binding.DestinationType.QUEUE, "order-event-exchange", "order.release.order", null);
    }
}
