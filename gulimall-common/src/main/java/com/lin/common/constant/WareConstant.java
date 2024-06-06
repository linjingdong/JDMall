package com.lin.common.constant;

import lombok.Getter;

/**
 * @Description TODO
 * @Date 2024/6/4 12:52
 * @Author Lin
 * @Version 1.0
 */
public class WareConstant {
    @Getter
    public enum PurchaseEnum {
        CREATED(0, "新建"), ASSIGNED(1, "已分配"),
        RECEIVE(2, "已领取"), FINISH(3, "已完成"),
        HASEEROR(4, "有异常");
        private final int code;
        private final String msg;

        PurchaseEnum(int code, String msg) {
            this.code = code;
            this.msg = msg;
        }
    }
    @Getter
    public enum PurchaseDetailEnum {
        CREATED(0, "新建"), ASSIGNED(1, "已分配"),
        BUYING(2, "正在采购"), FINISH(3, "已完成"),
        HASEEROR(4, "采购失败");
        private final int code;
        private final String msg;

        PurchaseDetailEnum(int code, String msg) {
            this.code = code;
            this.msg = msg;
        }
    }
}
