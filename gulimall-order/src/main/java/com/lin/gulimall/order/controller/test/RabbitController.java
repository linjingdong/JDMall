package com.lin.gulimall.order.controller.test;

import com.lin.gulimall.order.entity.OmsOrderEntity;
import com.lin.gulimall.order.entity.OmsOrderReturnReasonEntity;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Date;
import java.util.UUID;

/**
 * @Date 2024/7/10 16:55
 * @Author Lin
 * @Version 1.0
 */
@Controller
public class RabbitController {
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @GetMapping("/sendMq")
    public String sendMq(@RequestParam(value = "num", required = false, defaultValue = "10") Integer num) {
        for (int i = 0; i < num; i++) {
            if (i % 2 == 0) {
                OmsOrderReturnReasonEntity reasonEntity = new OmsOrderReturnReasonEntity();
                reasonEntity.setId(1L);
                reasonEntity.setName("小明");
                reasonEntity.setCreateTime(new Date());
                rabbitTemplate.convertAndSend("hello-java-exchange", "hello.java", reasonEntity);
            } else {
                OmsOrderEntity omsOrderEntity = new OmsOrderEntity();
                omsOrderEntity.setOrderSn(UUID.randomUUID().toString());
                rabbitTemplate.convertAndSend("hello-java-exchange", "hello.java", omsOrderEntity);
            }
        }
        return "ok";
    }
}
