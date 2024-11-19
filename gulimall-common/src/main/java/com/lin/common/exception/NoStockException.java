package com.lin.common.exception;

/**
 * @Description 无库存异常
 * @Date 2024/11/19 22:42
 * @Author Lin
 * @Version 1.0
 */
public class NoStockException extends RuntimeException{
    private Long skuId;
    public NoStockException(Long skuId) {
        super("商品号:" + skuId + "无库存，无法锁定！");
    }

    public NoStockException() {
        super("无库存，无法锁定！");
    }

    public Long setSkuId(Long skuId) {
        this.skuId = skuId;
        return skuId;
    }

    public Long getSkuId() {
        return skuId;
    }
}
