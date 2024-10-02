package com.lin.gulimall.cart.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.fasterxml.jackson.core.JsonParser;
import com.lin.common.utils.R;
import com.lin.gulimall.cart.config.MyThreadConfig;
import com.lin.gulimall.cart.feign.ProductFeignService;
import com.lin.gulimall.cart.interceptor.CartInterceptor;
import com.lin.gulimall.cart.service.CartService;
import com.lin.gulimall.cart.vo.Cart;
import com.lin.gulimall.cart.vo.CartItem;
import com.lin.gulimall.cart.vo.SkuInfoVo;
import com.lin.gulimall.cart.vo.UserInfoTo;
import io.netty.util.internal.StringUtil;
import lombok.Generated;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.thymeleaf.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

/**
 * @Description 购物车业务接口实现类
 * @Date 2024/7/6 16:27
 * @Author Lin
 * @Version 1.0
 */
@Slf4j
@Service
public class CartServiceImpl implements CartService {
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private ProductFeignService productFeignService;
    @Autowired
    private ThreadPoolExecutor executor;

    private final static String CART_PREFIX = "gulimal:cart:";

    @Override
    public CartItem addToCart(Long skuId, Integer num) throws ExecutionException, InterruptedException {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        String cartItemString = (String) cartOps.get(skuId.toString());

        // 给购物车添加新的商品
        if (StringUtils.isEmpty(cartItemString)) {
            CartItem cartItem = new CartItem();
            // 1、远程调用商品服务来查询当前需要添加的信息
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                R r = productFeignService.getSkuInfo(skuId);
                SkuInfoVo skuInfo = r.getDataByKey("skuInfo", new TypeReference<SkuInfoVo>() {
                });

                // 2、商品添加购物车
                cartItem.setCheck(true);
                cartItem.setCount(num);
                cartItem.setImage(skuInfo.getSkuDefaultImg());
                cartItem.setPrice(skuInfo.getPrice());
                cartItem.setSkuId(skuId);
                cartItem.setTitle(skuInfo.getSkuTitle());
            }, executor);

            // 3、远程查询sku的组合信息
            CompletableFuture<Void> future1 = CompletableFuture.runAsync(() -> {
                List<String> skuSaleAttrList = productFeignService.getSkuSaleAttrList(skuId);
                cartItem.setSkuAttr(skuSaleAttrList);
            }, executor);

            CompletableFuture.allOf(future, future1).get();
            cartOps.put(skuId.toString(), JSON.toJSONString(cartItem));

            return cartItem;
        } else { // 购物车当中有对应的商品，无需添加，但是需要修改修改数量
            CartItem cartItem = JSON.parseObject(cartItemString, CartItem.class);
            Objects.requireNonNull(cartItem).setCount(cartItem.getCount() + num);

            cartOps.put(skuId.toString(), JSON.toJSONString(cartItem));
            return cartItem;
        }
    }

    @Override
    public CartItem getCartItem(Long skuId) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        String resp = (String) cartOps.get(skuId.toString());
        return JSON.parseObject(resp, CartItem.class);
    }

    /**
     *
     */
    @Override
    public Cart getCart() throws ExecutionException, InterruptedException {
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();

        Cart cart = new Cart();
        if (userInfoTo.getUserId() != null) {
            // 1、登录状态
            String cartId = CART_PREFIX + userInfoTo.getUserId();

            // 2、如果临时购物车有数据就合并购物车【合并购物车】
            String tempCartId = CART_PREFIX + userInfoTo.getUserKey();
            List<CartItem> tempCartItems = getCartItems(tempCartId);
            if (tempCartItems != null && !tempCartItems.isEmpty()) {
                // 临时购物车有数据，需要合并
                for (CartItem tempCartItem : tempCartItems) {
                    addToCart(tempCartItem.getSkuId(), tempCartItem.getCount());
                }
            }

            // 3、获取登陆后的购物车的数据【包含合并过来的临时购物车的数据，和登录后的购物车的数据】
            List<CartItem> cartItems = getCartItems(cartId);
            cart.setItems(cartItems);

            // 4、清空临时购物车
            clearCart(tempCartId);
        } else {
            // 未登录状态
            String cartKey = CART_PREFIX + userInfoTo.getUserKey();
            // 获取临时购物车的所有购物项
            List<CartItem> cartItems = getCartItems(cartKey);
            cart.setItems(cartItems);
        }
        return cart;
    }

    @Override
    public void clearCart(String userKey) {
        redisTemplate.delete(userKey);
    }

    @Override
    public void checkItem(Long skuId, Integer checked) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        CartItem cartItem = getCartItem(skuId);
        cartItem.setCheck(checked == 1 ? true : false);
        cartOps.put(skuId.toString(), JSON.toJSONString(cartItem));
    }

    @Override
    public void changeItemCount(Long skuId, Integer num) {
        CartItem cartItem = getCartItem(skuId);
        cartItem.setCount(num);
        getCartOps().put(skuId.toString(), JSON.toJSONString(cartItem));
    }

    @Override
    public void deleteItem(Long skuId) {
        getCartOps().delete(skuId.toString());
    }

    @Override
    public List<CartItem> getCurrentUserItems() {
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        if (userInfoTo == null) {
            return null;
        } else {
            String cartKey = CART_PREFIX + userInfoTo.getUserId();
            List<CartItem> cartItems = getCartItems(cartKey);
            List<CartItem> collect = cartItems.stream().filter(CartItem::getCheck).map(item -> {
                // 远程查询最新的价格信息
                BigDecimal price = productFeignService.getPrice(item.getSkuId());
                // TODO 1、更新为最新的价格
                item.setPrice(price);
                return item;
            }).collect(Collectors.toList());

            return collect;
        }
    }

    /**
     * 获取到我们要操作的购物车
     *
     * @return 返回对指定key的操作
     */
    private BoundHashOperations<String, Object, Object> getCartOps() {
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();

        String cartKey = "";

        if (userInfoTo.getUserId() != null) {
            // 用户登录，将信息保存到用户购物车，redis的前缀应该为gulimall:cart:userId
            cartKey = CART_PREFIX + userInfoTo.getUserId();
        } else {
            cartKey = CART_PREFIX + userInfoTo.getUserKey();
        }

        // 往指定的key进行操作
        return redisTemplate.boundHashOps(cartKey);
    }

    /**
     * 获取购物车
     */
    public List<CartItem> getCartItems(String cartKey) {
        List<Object> values = redisTemplate.boundHashOps(cartKey).values();
        if (values != null && !values.isEmpty()) {
            List<CartItem> collect = values.stream().map(value -> {
                String valueString = value.toString();
                return JSON.parseObject(valueString, CartItem.class);
            }).collect(Collectors.toList());
            return collect;
        } else {
            return null;
        }
    }
}
