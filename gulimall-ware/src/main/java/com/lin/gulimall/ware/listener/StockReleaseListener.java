package com.lin.gulimall.ware.listener;

import com.lin.common.to.OrderTo;
import com.lin.common.to.mq.StockDetailsTo;
import com.lin.common.to.mq.StockLockedTo;
import com.lin.common.utils.R;
import com.lin.gulimall.ware.service.WmsWareSkuService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * @Description 解锁服务监听器
 * @Date 2025/1/12 15:57
 * @Author Lin
 * @Version 1.0
 */

@Slf4j
@Service
@RabbitListener(queues = "stock.release.stock.queue")
public class StockReleaseListener {
    @Autowired
    private WmsWareSkuService wareSkuService;

    @RabbitHandler
    public void handleStockLockedRelease(StockLockedTo stockLockedTo, Message message, Channel channel) throws IOException {
        log.info("收到解锁库存消息，messageId = {}", message.getMessageProperties().getMessageId());
        try {
            wareSkuService.unLockStock(stockLockedTo);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            channel.basicReject(message.getMessageProperties().getDeliveryTag(), true);
        }
    }

    @RabbitHandler
    public void handleOrderCanceledRelease(OrderTo order, Message message, Channel channel) throws IOException {
        log.info("收到订单关闭消息 message {}，准备解锁库存...", message.getMessageProperties());
        try {
            wareSkuService.unLockStock(order);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            channel.basicReject(message.getMessageProperties().getDeliveryTag(), true);
        }
    }
}
