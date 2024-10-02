package com.lin.gulimall.cart.controller;

import com.lin.gulimall.cart.interceptor.CartInterceptor;
import com.lin.gulimall.cart.service.CartService;
import com.lin.gulimall.cart.vo.Cart;
import com.lin.gulimall.cart.vo.CartItem;
import com.lin.gulimall.cart.vo.UserInfoTo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * @Description 购物车控制层
 * @Date 2024/7/6 16:33
 * @Author Lin
 * @Version 1.0
 */
@Controller
public class CartController {
    @Autowired
    private CartService cartService;

    @GetMapping("/currentUserItems")
    @ResponseBody
    public List<CartItem> getCurrentUserItems() {
        return cartService.getCurrentUserItems();
    }

    @GetMapping("/deleteItem")
    public String deleteItem(@RequestParam("skuId") Long skuId) {
        cartService.deleteItem(skuId);
        return "redirect:http://cart.gulimall.com:8081/cart.html";
    }

    @GetMapping("/countItem")
    public String itemCountChange(@RequestParam("skuId") Long skuId,
                                  @RequestParam("num") Integer num) {
        cartService.changeItemCount(skuId, num);
        return "redirect:http://cart.gulimall.com:8081/cart.html";
    }

    @GetMapping("/checkItem")
    public String itemChecked(@RequestParam("skuId") Long skuId,
                              @RequestParam("checked") Integer checked) {
        cartService.checkItem(skuId, checked);
        return "redirect:http://cart.gulimall.com:8081/cart.html";
    }

    /**
     * 获取购物车
     * 1、浏览器有一个名为user-key的cookie：标识用户身份，一个月后过期；
     * 2、如果第一次使用jd的购物车功能，都会给一个临时的用户身份（user-key）；
     * 3、浏览器以后保存，每一次访问都会带上这个cookie；
     */
    @GetMapping("/cart.html")
    public String cartListPage(Model model) throws ExecutionException, InterruptedException {
        Cart cart = cartService.getCart();
        model.addAttribute(cart);

        return "cartList";
    }

    /**
     * 添加购物车
     * RedirectAttributes中的red.addFlashAttribute()将数据放在session里面可以在页面当中取出，但是只能取一次
     * red.addAttribute("skuId", skuId);将数据放在url后面拼接
     */
    @GetMapping("/addToCart")
    public String addToCart(@RequestParam("skuId") Long skuId, @RequestParam("num") Integer num, RedirectAttributes red) throws ExecutionException, InterruptedException {
        cartService.addToCart(skuId, num);
        red.addAttribute("skuId", skuId);
        return "redirect:http://cart.gulimall.com:8081/addToCartSuccess.html";
    }

    // 跳转成功页
    @GetMapping("/addToCartSuccess.html")
    public String addToCartSuccess(@RequestParam("skuId") Long skuId, Model model) {
        // 重定向到成功的页面后，再重新查询一次
        CartItem cartItem = cartService.getCartItem(skuId);

        model.addAttribute("item", cartItem);

        return "success";
    }
}
