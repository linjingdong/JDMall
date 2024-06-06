package com.lin.common.constant;

import lombok.Getter;

/**
 * @Description TODO
 * @Date 2024/6/1 17:09
 * @Author Lin
 * @Version 1.0
 */
public class ProductConstant {
    @Getter
    public enum AttrEnum {
        ATTR_TYPE_BASE(1, "基本属性"), ATTR_TYPE_SALE(0, "销售属性");
        private final int code;
        private final String msg;

        AttrEnum(int code, String msg) {
            this.code = code;
            this.msg = msg;
        }
    }
}
