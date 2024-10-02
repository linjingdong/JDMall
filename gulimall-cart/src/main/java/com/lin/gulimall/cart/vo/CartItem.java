package com.lin.gulimall.cart.vo;

import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

/**
 * @Description 购物车上的购物项的详细信息
 * @Date 2024/7/6 16:01
 * @Author Lin
 * @Version 1.0
 */
public class CartItem {
    @Getter
    private Long skuId;
    @Getter
    private String title;
    @Getter
    private Boolean check = true;
    @Getter
    private String image;
    @Getter
    private List<String> skuAttr;
    @Getter
    private BigDecimal price;
    @Getter
    private Integer count;
    private BigDecimal totalPrice;

    public void setSkuId(Long skuId) {
        this.skuId = skuId;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setCheck(Boolean check) {
        this.check = check;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public void setSkuAttr(List<String> skuAttr) {
        this.skuAttr = skuAttr;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    /**
     * @return 用价格来计算当前项总价
     */
    public BigDecimal getTotalPrice() {
        return this.price.multiply(new BigDecimal("" + count));
    }
}
