package com.lin.gulimall.order.vo;

import com.lin.gulimall.order.entity.OmsOrderEntity;
import lombok.Data;

/**
 * @Description 提交订单返回的数据
 * @Date 2024/10/2 16:37
 * @Author Lin
 * @Version 1.0
 */
@Data
public class SubmitOrderResponseVo {
    /*
    订单信息
     */
    private OmsOrderEntity order;

    /*
    错误状态码
     */
    private Integer code; // 0 成功；1 令牌验证失败；2 验价失败

    /*
    错误信息
     */
    private String error;

}
