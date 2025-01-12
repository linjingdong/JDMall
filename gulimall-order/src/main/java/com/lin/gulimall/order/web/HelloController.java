package com.lin.gulimall.order.web;

import com.lin.gulimall.order.entity.OmsOrderEntity;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.UUID;

/**
 * @Description TODO
 * @Date 2024/7/11 11:20
 * @Author Lin
 * @Version 1.0
 */
@Controller
public class HelloController {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @ResponseBody
    @GetMapping("/test/createOrder")
    public String createOrder() {
        OmsOrderEntity omsOrderEntity = new OmsOrderEntity();
        omsOrderEntity.setOrderSn(UUID.randomUUID().toString());
        omsOrderEntity.setModifyTime(new Date());
        rabbitTemplate.convertAndSend("over-event-exchange", "order.create.order", omsOrderEntity);
        return "ok";
    }

    @GetMapping("/{page}.html")
    public String helloPage(@PathVariable("page") String page) {

        return page;
    }
}
