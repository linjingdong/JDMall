package com.lin.gulimall.order.web;

import com.alipay.api.AlipayApiException;
import com.lin.gulimall.order.config.AlipayTemplate;
import com.lin.gulimall.order.service.OmsOrderService;
import com.lin.gulimall.order.vo.PayVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Slf4j
@Controller
public class PayWebController {
    @Autowired
    private AlipayTemplate alipayTemplate;
    @Autowired
    private OmsOrderService orderService;

    /**
     * 统一下单
     * @param orderSn 订单号
     * @return 收银台地址
     */
    @ResponseBody
    @GetMapping(value = "/payOrder", produces = "text/html")
    public String payOrder(@RequestParam("orderSn") String orderSn) throws AlipayApiException {
        PayVo payVo = orderService.getOrderPay(orderSn);
        String pay = alipayTemplate.pay(payVo);
        log.info("调用支付后出参为：{}", pay);
        return pay;
    }
}
