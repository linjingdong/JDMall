package com.lin.gulimall.order.Listener;

import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.lin.gulimall.order.config.AlipayTemplate;
import com.lin.gulimall.order.service.OmsOrderService;
import com.lin.gulimall.order.vo.PayAsyncVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@Slf4j
@RestController
public class PayNotifyListener {
    @Autowired
    OmsOrderService orderService;
    @Autowired
    AlipayTemplate alipayTemplate;

    /**
     * 处理支付宝支付回调
     *
     * @param vo 回调参数
     * @return success：表示回调成功
     */
    @PostMapping("/payed/notify")
    public String handlePayNotify(PayAsyncVo vo, HttpServletRequest request) throws UnsupportedEncodingException, AlipayApiException {
        // 验签
        Map<String, String> params = new HashMap<String, String>();
        Map<String, String[]> requestParams = request.getParameterMap();
        for (String name : requestParams.keySet()) {
            String[] values = (String[]) requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i]
                        : valueStr + values[i] + ",";
            }
            //乱码解决，这段代码在出现乱码时使用
//            valueStr = new String(valueStr.getBytes("ISO-8859-1"), "utf-8");
            params.put(name, valueStr);
        }

        boolean signVerified = AlipaySignature.rsaCheckV1(params, alipayTemplate.getAlipay_public_key(), alipayTemplate.getCharset(), alipayTemplate.getSign_type());

        if (signVerified) {
            // 验签成功
            log.info("验签成功...");
            return orderService.handlePayResult(vo);
        } else {
            log.info("验签失败...");
            return "error";
        }
    }
}
