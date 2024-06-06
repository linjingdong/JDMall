package com.lin.gulimall.ware.vo;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @Description TODO
 * @Date 2024/6/4 22:42
 * @Author Lin
 * @Version 1.0
 */
@Data
public class PurchaseDoneVo {
    @NotNull
    private Long id; // 采购单Id
    private List<PurchaseItemsVo> items;
}
