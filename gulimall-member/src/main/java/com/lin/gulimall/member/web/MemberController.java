package com.lin.gulimall.member.web;

import com.alibaba.fastjson.JSONObject;
import com.lin.common.utils.R;
import com.lin.gulimall.member.feign.OrderFeignService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.Map;

@Controller
public class MemberController {
    @Autowired
    private OrderFeignService orderFeignService;

    @GetMapping("/memberOrder.html")
    public String memberOrderPage(@RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum, Model model) {
        Map<String, Object> param = new HashMap<>();
        param.put("page", pageNum.toString());
        R r = orderFeignService.listWithItem(param);
        System.out.println(JSONObject.toJSONString(r));
        model.addAttribute("orders", r);
        return "orderList";
    }
}
