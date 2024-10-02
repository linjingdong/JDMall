package com.lin.gulimall.order.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.lin.common.utils.R;
import com.lin.common.vo.MemberRespVo;
import com.lin.gulimall.order.config.LoginUserInterceptor;
import com.lin.gulimall.order.constant.OrderConstant;
import com.lin.gulimall.order.feign.CartFeignService;
import com.lin.gulimall.order.feign.MemberFeignService;
import com.lin.gulimall.order.feign.WmsFeignService;
import com.lin.gulimall.order.vo.*;
import io.prometheus.client.Collector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lin.common.utils.PageUtils;
import com.lin.common.utils.Query;

import com.lin.gulimall.order.dao.OmsOrderDao;
import com.lin.gulimall.order.entity.OmsOrderEntity;
import com.lin.gulimall.order.service.OmsOrderService;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;


@Service("omsOrderService")
public class OmsOrderServiceImpl extends ServiceImpl<OmsOrderDao, OmsOrderEntity> implements OmsOrderService {
    @Autowired
    private MemberFeignService memberFeignService;
    @Autowired
    private CartFeignService cartFeignService;
    @Autowired
    private WmsFeignService wmsFeignService;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private ThreadPoolExecutor executor;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OmsOrderEntity> page = this.page(
                new Query<OmsOrderEntity>().getPage(params),
                new QueryWrapper<OmsOrderEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public OrderConfirmVo confirmOrder() throws ExecutionException, InterruptedException {
        OrderConfirmVo confirmVo = new OrderConfirmVo();
        MemberRespVo memberRespVo = LoginUserInterceptor.threadLocal.get();
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes(); // 获取之前的请求

        // 1、远程查询所有的收货地址
        CompletableFuture<Void> getAddressFuture = CompletableFuture.runAsync(() -> {
            RequestContextHolder.setRequestAttributes(requestAttributes); // 每一个异步的线程都共享之前的数据
            List<MemberAddressVo> address = memberFeignService.getAddress(memberRespVo.getId().toString());
            confirmVo.setAddress(address);
        }, executor);

        // 2、远程查询所选购物项的信息
        CompletableFuture<Void> getItemsFuture = CompletableFuture.runAsync(() -> {
            RequestContextHolder.setRequestAttributes(requestAttributes);
            List<OrderItemVo> orderItems = cartFeignService.getCurrentUserItems();
            confirmVo.setItems(orderItems);
        }, executor).thenRunAsync(() -> {
            List<OrderItemVo> items = confirmVo.getItems();
            List<Long> skuIds = items.stream().map(OrderItemVo::getSkuId).collect(Collectors.toList());
            // 远程调用仓库服务来查询是否有货无货
            R r = wmsFeignService.getSkusHasStock(skuIds);
            List<SkuStockVo> skuStockVoList = r.getDataByKey("data", new TypeReference<List<SkuStockVo>>() {
            });

            if (skuStockVoList != null) {
                Map<Long, Boolean> collect = skuStockVoList.stream().collect(Collectors.toMap(SkuStockVo::getSkuId, SkuStockVo::isHasStock));
                confirmVo.setStocks(collect);
            }

        }, executor);

        // 3、获取用户积分
        Integer integration = memberRespVo.getIntegration();
        confirmVo.setIntegration(integration);

        // 4、其他数据自动计算

        // TODO 5、防重令牌
        String token = UUID.randomUUID().toString().replace("_", "");
        redisTemplate.opsForValue().set(OrderConstant.USER_ORDER_TOKEN_PERFIX + memberRespVo.getId(), token, 30, TimeUnit.MINUTES);
        confirmVo.setOrderToken(token);

        CompletableFuture.allOf(getAddressFuture, getItemsFuture).get();

        System.out.println(confirmVo.toString());

        return confirmVo;
    }

    @Override
    public SubmitOrderResponseVo submitOrder(OrderSubmitVo vo) {
        MemberRespVo memberRespVo = LoginUserInterceptor.threadLocal.get();
        SubmitOrderResponseVo response = new SubmitOrderResponseVo();

        // 验证令牌【令牌的对比和删除必须保证原子性（脚本）】
        // 原子验证令牌和删除令牌
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        String orderToken = vo.getOrderToken();

        // 原子验证令牌和删除令牌
        Long result = redisTemplate.execute(
                new DefaultRedisScript<>(script, Long.class),
                Collections.singletonList(OrderConstant.USER_ORDER_TOKEN_PERFIX + memberRespVo.getId()),
                orderToken);

        //
        if (result == 0L) {
            // 令牌验证失败
            return response;
        } else {
            // 令牌验证成功

        }
        return null;
    }

}