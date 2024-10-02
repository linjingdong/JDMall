package com.lin.gulimall.order.service.impl;

import com.lin.gulimall.order.entity.OmsOrderReturnReasonEntity;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.nio.channels.Channel;
import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lin.common.utils.PageUtils;
import com.lin.common.utils.Query;

import com.lin.gulimall.order.dao.OmsOrderItemDao;
import com.lin.gulimall.order.entity.OmsOrderItemEntity;
import com.lin.gulimall.order.service.OmsOrderItemService;


@Service("omsOrderItemService")
public class OmsOrderItemServiceImpl extends ServiceImpl<OmsOrderItemDao, OmsOrderItemEntity> implements OmsOrderItemService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OmsOrderItemEntity> page = this.page(
                new Query<OmsOrderItemEntity>().getPage(params),
                new QueryWrapper<OmsOrderItemEntity>()
        );

        return new PageUtils(page);
    }

    @RabbitListener(queues = "hello-java-queue")
    public void receiveMessage(Message message,
                               OmsOrderReturnReasonEntity content,
                               Channel channel) {
        byte[] body = message.getBody();
        System.out.println("接收到消息：" + message + "---内容是：" + content);
    }
}