package com.lin.gulimall.cart.vo;

import java.math.BigDecimal;
import java.util.List;

/**
 * @Description 整个购物车的VO，需要重写get方法，保证每次获取数据的时候都能先进行计算
 * @Date 2024/7/6 16:00
 * @Author Lin
 * @Version 1.0
 */
public class Cart {
    List<CartItem> items;
    private Integer countNum; // 商品数量
    private Integer countType; // 商品类型数量
    private BigDecimal totalAmount; // 商品总价
    private BigDecimal reduce = new BigDecimal("0"); // 减免价格

    public List<CartItem> getItems() {
        return items;
    }

    public void setItems(List<CartItem> items) {
        this.items = items;
    }

    public Integer getCountNum() {
        int count = 0;
        if (items != null && !items.isEmpty()) {
            for (CartItem item : items) {
                count += item.getCount();
            }
        }
        return count;
    }

    public Integer getCountType() {
        int count = 0;
        if (items != null && !items.isEmpty()) {
            for (CartItem item : items) {
                count += 1;
            }
        }
        return count;
    }

    public BigDecimal getTotalAmount() {
        // 1. 先计算总价
        BigDecimal amount = new BigDecimal("0");
        if (items != null && !items.isEmpty()) {
            for (CartItem item : items) {
                if (item.getCheck()) {
                    amount = amount.add(item.getTotalPrice());
                }
            }
        }

        // 2. 减去优惠价格
        return amount.subtract(getReduce());
    }

    public BigDecimal getReduce() {
        return reduce;
    }

    public void setReduce(BigDecimal reduce) {
        this.reduce = reduce;
    }
}
