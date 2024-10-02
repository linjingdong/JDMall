package com.lin.gulimall.product.web;

import com.lin.gulimall.product.service.SkuInfoService;
import com.lin.gulimall.product.vo.SkuItemVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.concurrent.ExecutionException;

/**
 * @Description TODO
 * @Date 2024/6/27 15:59
 * @Author Lin
 * @Version 1.0
 */
@Controller
public class ItemController {
    @Autowired
    private SkuInfoService skuInfoService;

    @GetMapping("/{skuId}.html")
    public String item(@PathVariable("skuId") Long skuId, Model model) throws ExecutionException, InterruptedException {
        SkuItemVo item = skuInfoService.item(skuId);
        model.addAttribute("item", item);

        return "item";
    }
}
