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
    private Integer code; // 0 成功 ；1 失败

}
