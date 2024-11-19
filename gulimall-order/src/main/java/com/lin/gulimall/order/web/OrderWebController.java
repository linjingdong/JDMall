package com.lin.gulimall.order.web;

import com.lin.gulimall.order.service.OmsOrderService;
import com.lin.gulimall.order.vo.OrderConfirmVo;
import com.lin.gulimall.order.vo.OrderItemVo;
import com.lin.gulimall.order.vo.OrderSubmitVo;
import com.lin.gulimall.order.vo.SubmitOrderResponseVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.concurrent.ExecutionException;

/**
 * @Description 订单控制层
 * @Date 2024/7/11 15:10
 * @Author Lin
 * @Version 1.0
 */
@Slf4j
@Controller
public class OrderWebController {
    @Autowired
    private OmsOrderService orderService;

    @GetMapping("/toTrade")
    public String toTrade(Model model) throws ExecutionException, InterruptedException {
        OrderConfirmVo item = orderService.confirmOrder();

        model.addAttribute("orderConfirmData", item);
        return "confirm";
    }

    /**
     * 下单功能
     *
     * @param vo 提交订单的数据
     * @return 下单成功返回到支付页面
     */
    @PostMapping("/submitOrder")
    public String submitOrder(OrderSubmitVo vo, Model model, RedirectAttributes redirectAttributes) {
        // 下单：创建订单、验令牌、验价、锁库存...
        log.info("订单提交的数据：{}", vo);
        SubmitOrderResponseVo responseVo = orderService.submitOrder(vo);

        if (0 == responseVo.getCode()) {
            model.addAttribute("submitOrderResp", responseVo);
            // 下单成功返回到支付页面
            return "pay";
        } else {
            String msg = "下单失败";
            switch (responseVo.getCode()) {
                case 1:
                    msg = "令牌校验失败，请重新提交!";
                case 2:
                    msg = "验价失败，请重新提交！";
                case 3:
                    msg = "库存锁定失败";
            }
            redirectAttributes.addFlashAttribute("msg", msg);
            // 下单失败回到订单确认页重新确认订单信息
            return "redirect:http://order.gulimall.com:8081/toTrade";
        }
    }
}
