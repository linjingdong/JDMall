package com.lin.gulimall.order.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.lin.common.exception.NoStockException;
import com.lin.common.utils.R;
import com.lin.common.vo.MemberRespVo;
import com.lin.gulimall.order.config.LoginUserInterceptor;
import com.lin.gulimall.order.constant.OrderConstant;
import com.lin.gulimall.order.entity.OmsOrderItemEntity;
import com.lin.gulimall.order.enume.OrderStatusEnum;
import com.lin.gulimall.order.feign.CartFeignService;
import com.lin.gulimall.order.feign.MemberFeignService;
import com.lin.gulimall.order.feign.ProductFeignService;
import com.lin.gulimall.order.feign.WmsFeignService;
import com.lin.gulimall.order.service.OmsOrderItemService;
import com.lin.gulimall.order.to.OrderCreateTo;
import com.lin.gulimall.order.vo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;


@Service("omsOrderService")
public class OmsOrderServiceImpl extends ServiceImpl<OmsOrderDao, OmsOrderEntity> implements OmsOrderService {
    private static final ThreadLocal<OrderSubmitVo> orderSubmitThreadLocal = new ThreadLocal<>();
    @Autowired
    private OmsOrderItemService orderItemService;
    @Autowired
    private ProductFeignService productFeignService;
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
        IPage<OmsOrderEntity> page = this.page(new Query<OmsOrderEntity>().getPage(params), new QueryWrapper<OmsOrderEntity>());

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

    @Transactional(rollbackFor = NoStockException.class)
    @Override
    public SubmitOrderResponseVo submitOrder(OrderSubmitVo vo) {
        MemberRespVo memberRespVo = LoginUserInterceptor.threadLocal.get();
        orderSubmitThreadLocal.set(vo);
        SubmitOrderResponseVo response = new SubmitOrderResponseVo();
        response.setCode(0);

        // 验证令牌防止幂等性【令牌的对比和删除必须保证原子性（脚本）】
        // 原子验证令牌和删除令牌
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        String orderToken = vo.getOrderToken();

        // 原子验证令牌和删除令牌
        Long result = redisTemplate.execute(new DefaultRedisScript<>(script, Long.class), Collections.singletonList(OrderConstant.USER_ORDER_TOKEN_PERFIX + memberRespVo.getId()), orderToken);

        if (result == 0L) {
            // 令牌验证失败
            response.setCode(1);
            response.setError("令牌验证失败");
            return response;
        } else { // 令牌验证成功
            // 1、创建订单、订单项等信息
            OrderCreateTo orderCreateTo = createOrder();

            // 2、验价
            if (Math.abs(orderCreateTo.getPayPrice().subtract(vo.getPayPrice()).doubleValue()) < 0.1) {
                // 3、保存订单
                saveOrder(orderCreateTo);
                // 4、锁定库存，有异常需要回滚保存订单的操作（订单号，所有订单项：skuId, skuName, num）
                WareSkuLockVo lockVo = new WareSkuLockVo();
                List<OrderItemVo> lockItem = orderCreateTo.getOrderItems().stream().map(item -> {
                    OrderItemVo orderItemVo = new OrderItemVo();
                    orderItemVo.setSkuId(item.getSkuId());
                    orderItemVo.setCount(item.getSkuQuantity());
                    orderItemVo.setTitle(item.getSkuName());
                    return orderItemVo;
                }).collect(Collectors.toList());
                lockVo.setOrderSn(orderCreateTo.getOrder().getOrderSn());
                lockVo.setLocks(lockItem);

                R r = wmsFeignService.orderLockStock(lockVo);// 锁定库存

                if (0 == r.getCode()) {
                    // 锁定成功
                    response.setOrder(orderCreateTo.getOrder());
                    return response;
                } else {
                    // 锁定异常
                    try {
                        response.setCode(3);
                        return response;
                    } catch (NoStockException e ) {
                        throw new NoStockException();
                    }
                }
            } else {
                response.setCode(2);
                response.setError("验价失败");
                return response;
            }
        }
    }

    /**
     * 保存订单数据
     *
     * @param orderCreateTo 订单创建信息
     */
    private void saveOrder(OrderCreateTo orderCreateTo) {
        OmsOrderEntity order = orderCreateTo.getOrder();
        order.setModifyTime(new Date());
        this.save(order);

        List<OmsOrderItemEntity> orderItems = orderCreateTo.getOrderItems();
        orderItemService.saveBatch(orderItems);
    }

    /**
     * 创建订单
     *
     * @return 订单信息
     */
    private OrderCreateTo createOrder() {
        OrderCreateTo createTo = new OrderCreateTo();

        // 1、构建订单
        String orderSn = IdWorker.getTimeId(); // 创建订单号
        OmsOrderEntity order = buildOrder(orderSn);

        // 2、构建所有订单项
        List<OmsOrderItemEntity> orderItemEntities = buildOrderItems(orderSn);

        // 3、计算相关价格、积分等
        computePrice(order, orderItemEntities);


        createTo.setOrder(order);
        createTo.setOrderItems(orderItemEntities);
        createTo.setPayPrice(order.getPayAmount());
        return createTo;
    }

    /**
     * 计算价格相关信息
     *
     * @param order             订单
     * @param orderItemEntities 订单项
     */
    private void computePrice(OmsOrderEntity order, List<OmsOrderItemEntity> orderItemEntities) {
        BigDecimal realAmount = new BigDecimal("0.0"); // 实际总额
        BigDecimal coupon = new BigDecimal("0.0"); // 优惠券优惠价格
        BigDecimal integrationAmount = new BigDecimal("0.0"); // 积分优惠价格
        BigDecimal promotion = new BigDecimal("0.0"); // 促销优惠价格
        BigDecimal integration = new BigDecimal("0.0"); // 积分
        BigDecimal growth = new BigDecimal("0.0"); // 成长值

        for (OmsOrderItemEntity orderItemEntity : orderItemEntities) {
            realAmount = realAmount.add(orderItemEntity.getRealAmount());
            coupon = coupon.add(orderItemEntity.getCouponAmount());
            integrationAmount = integrationAmount.add(orderItemEntity.getIntegrationAmount());
            promotion = promotion.add(orderItemEntity.getPromotionAmount());
            integration = integration.add(new BigDecimal(orderItemEntity.getGiftIntegration().toString()));
            growth = growth.add(new BigDecimal(orderItemEntity.getGiftGrowth().toString()));
        }

        // 设置订单价格相关信息
        order.setTotalAmount(realAmount);
        order.setPayAmount(realAmount.add(order.getFreightAmount()));
        order.setCouponAmount(coupon);
        order.setIntegrationAmount(integrationAmount);
        order.setPromotionAmount(promotion);

        // 设置积分、成长值
        order.setIntegration(integration.intValue()); // 积分
        order.setGrowth(growth.intValue()); // 成长值
    }

    /**
     * 构建订单
     *
     * @param orderSn 订单号
     * @return 订单
     */
    private OmsOrderEntity buildOrder(String orderSn) {
        OmsOrderEntity order = new OmsOrderEntity();
        order.setOrderSn(orderSn);
        OrderSubmitVo orderSubmitVo = orderSubmitThreadLocal.get();

        // 设置会员信息
        MemberRespVo memberRespVo = LoginUserInterceptor.threadLocal.get();
        order.setMemberId(memberRespVo.getId());
        order.setMemberUsername(memberRespVo.getUsername());

        // 设置收货地址信息
        R fareResp = wmsFeignService.getFare(orderSubmitVo.getAddrId());
        FareVo fareData = fareResp.getData(new TypeReference<FareVo>() {
        });
        MemberAddressVo addressVo = fareData.getAddressVo();
        order.setFreightAmount(fareData.getFare()); // 运费信息
        order.setReceiverName(addressVo.getName()); // 收货人信息
        order.setReceiverCity(addressVo.getCity());
        order.setReceiverPhone(addressVo.getPhone());
        order.setReceiverProvince(addressVo.getProvince());
        order.setReceiverPostCode(addressVo.getPostCode());
        order.setReceiverDetailAddress(addressVo.getDetailAddress());

        // 设置订单其他信息
        order.setStatus(OrderStatusEnum.CREATE_NEW.getCode()); // 设置订单状态
        order.setAutoConfirmDay(7); // 设置订单自动确认时间
        order.setDeleteStatus(0); // 订单删除状态【未删除】

        return order;
    }

    /**
     * 构建所有订单项数据
     *
     * @return 订单项
     */
    private List<OmsOrderItemEntity> buildOrderItems(String orderSn) {
        // 最后确定每个购物项的价格
        List<OrderItemVo> orderItemVoList = cartFeignService.getCurrentUserItems();
        if (!orderItemVoList.isEmpty()) {
            return orderItemVoList.stream().map(cartItem -> {
                OmsOrderItemEntity orderItem = new OmsOrderItemEntity();
                // 1、订单信息
                orderItem = buildOrderItem(cartItem);
                orderItem.setOrderSn(orderSn);
                return orderItem;
            }).collect(Collectors.toList());
        }
        return null;
    }

    /**
     * 构建某一个订单项数据
     */
    private OmsOrderItemEntity buildOrderItem(OrderItemVo cartItem) {
        OmsOrderItemEntity orderItem = new OmsOrderItemEntity();
        // 2、订单的spu信息
        R spuInfoBySkuId = productFeignService.getSpuInfoBySkuId(cartItem.getSkuId());
        SpuInfoVo spuInfoVo = spuInfoBySkuId.getData(new TypeReference<SpuInfoVo>() {
        });
        orderItem.setSpuId(spuInfoVo.getId());
        orderItem.setSpuBrand(spuInfoVo.getBrandId().toString());
        orderItem.setSpuName(spuInfoVo.getSpuName());
        orderItem.setCategoryId(spuInfoVo.getCatalogId());

        // 3、订单的sku信息
        orderItem.setSkuId(cartItem.getSkuId());
        orderItem.setSkuName(cartItem.getTitle());
        orderItem.setSkuPic(cartItem.getImage());
        orderItem.setSkuPrice(cartItem.getPrice());
        orderItem.setSkuQuantity(cartItem.getCount());
        String skuAttrs = StringUtils.collectionToDelimitedString(cartItem.getSkuAttr(), ";");
        orderItem.setSkuAttrsVals(skuAttrs);
        // 4、订单的优惠信息【不做】
        // 5、订单的积分信息
        orderItem.setGiftGrowth(cartItem.getPrice().multiply(new BigDecimal(cartItem.getCount().toString())).intValue());
        orderItem.setGiftIntegration(cartItem.getPrice().multiply(new BigDecimal(cartItem.getCount().toString())).intValue());

        // 6、计算订单项价格
        orderItem.setPromotionAmount(new BigDecimal("0"));
        orderItem.setCouponAmount(new BigDecimal("0"));
        orderItem.setIntegrationAmount(new BigDecimal("0"));
        BigDecimal origin = orderItem.getSkuPrice().multiply(new BigDecimal(orderItem.getSkuQuantity()));
        BigDecimal realAmount = origin.subtract(orderItem.getCouponAmount()).
                subtract(orderItem.getPromotionAmount()).
                subtract(orderItem.getIntegrationAmount());
        orderItem.setRealAmount(realAmount); // 当前订单项的实际总金额【总额-优惠】

        return orderItem;
    }
}