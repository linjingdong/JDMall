package com.lin.common.to.mq;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

/**
 * @Description 发送锁定库存详情的信息
 * @Date 2025/1/9 20:31
 * @Author Lin
 * @Version 1.0
 */

@Data
public class StockDetailsTo {
    /**
     * id
     */
    private Long id;
    /**
     * sku_id
     */
    private Long skuId;
    /**
     * sku_name
     */
    private String skuName;
    /**
     * 购买个数
     */
    private Integer skuNum;
    /**
     * 工作单id
     */
    private Long taskId;
    /**
     * 仓库id
     */
    private Long wareId;
    /**
     * 仓库锁定状态
     */
    private Integer lockStatus;
}
