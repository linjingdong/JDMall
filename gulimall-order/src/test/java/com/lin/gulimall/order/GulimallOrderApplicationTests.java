package com.lin.gulimall.order;

import com.lin.gulimall.order.entity.OmsOrderReturnReasonEntity;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class GulimallOrderApplicationTests {
    @Autowired
    private AmqpAdmin amqpAdmin; // 高级消息场景中的管理组件
    @Autowired
    private RabbitTemplate rabbitTemplate; // 用户发送消息

    @Test
    public void sendMessageTest() {
        // 1、发送的消息是个对象，我们会使用序列化机制，将对象写出出（对象必须实现Serializable）
        OmsOrderReturnReasonEntity reasonEntity = new OmsOrderReturnReasonEntity();
        reasonEntity.setId(1L);
        reasonEntity.setName("小明");
        reasonEntity.setCreateTime(new Date());

        // 2、发送对象类型的消息，也可以是一个json数据，通过配置MessageCover来让rabbitTemplate再容器当中找到规则
        String msg = "hello world";
        rabbitTemplate.convertAndSend("hello-java-exchange", "hello.java", reasonEntity);
        log.info("成功发送消息:{}", reasonEntity.toString());
    }

    // 创建交换机
    @Test
    public void createExchangeTest() {
        DirectExchange directExchange = new DirectExchange("hello-java-exchange", true, false);
        amqpAdmin.declareExchange(directExchange);
        log.info("Exchange{}创建成功", "hello-java-exchange");
    }

    @Test
    public void createQueueTest() {
        Queue queue = new Queue("hello-java-queue");
        amqpAdmin.declareQueue(queue);
        log.info("Queue:{}创建成功", "hello-java-queue");
    }

    @Test
    public void createBindingTest() {
        /*
            String destination, DestinationType destinationType, String exchange, String routingKey, Map<String, Object> arguments
         */
        // 将exchange指定的交换机和destination目的地进行绑定，使用routingKey作为指定的路由键
        Binding binding = new Binding(
                "hello-java-queue",
                Binding.DestinationType.QUEUE,
                "hello-java-exchange",
                "hello.java",
                null
        );
        amqpAdmin.declareBinding(binding);
        log.info("Binding：{}创建成功", "hello-java-banding");
    }
}
