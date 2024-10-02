package com.lin.gulimall.cart.service;

import com.lin.gulimall.cart.vo.Cart;
import com.lin.gulimall.cart.vo.CartItem;

import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * @Description 购物车业务接口
 * @Date 2024/7/6 16:27
 * @Author Lin
 * @Version 1.0
 */

public interface CartService {

    CartItem addToCart(Long skuId, Integer num) throws ExecutionException, InterruptedException;

    CartItem getCartItem(Long skuId);

    Cart getCart() throws ExecutionException, InterruptedException;

    void clearCart(String userKey);

    void checkItem(Long skuId, Integer checked);

    /**
     * 修改购物项的商品数量
     */
    void changeItemCount(Long skuId, Integer num);

    /**
     * 删除购物项
     */
    void deleteItem(Long skuId);

    List<CartItem> getCurrentUserItems();
}
