package com.lin.gulimall.order.Listener;

import com.lin.gulimall.order.entity.OmsOrderEntity;
import com.lin.gulimall.order.service.OmsOrderService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * @Description 订单关单监听器
 * @Date 2025/1/12 16:47
 * @Author Lin
 * @Version 1.0
 */

@Slf4j
@Service
@RabbitListener(queues = "order.release.order.queue")
public class OrderCloseListener {
    @Autowired
    private OmsOrderService orderService;

    @RabbitHandler
    public void listener(OmsOrderEntity order, Message message, Channel channel) throws IOException {
        log.info("收到关闭订单延时消息 message:{}", message.getMessageProperties());
        try {
            orderService.closeOrder(order);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            channel.basicReject(message.getMessageProperties().getDeliveryTag(), true);
        }
    }
}
