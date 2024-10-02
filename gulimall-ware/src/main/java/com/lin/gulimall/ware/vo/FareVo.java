package com.lin.gulimall.ware.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @Description 返回地址和运费VO
 * @Date 2024/9/21 15:23
 * @Author Lin
 * @Version 1.0
 */

@Data
public class FareVo {
    private MemberAddressVo addressVo;
    private BigDecimal fare;
}
