package com.lin.gulimall.product.web;

import com.lin.gulimall.product.entity.CategoryEntity;
import com.lin.gulimall.product.service.CategoryService;
import com.lin.gulimall.product.vo.Catelog2Vo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.thymeleaf.util.ObjectUtils;

import java.util.List;
import java.util.Map;

/**
 * @Description TODO
 * @Date 2024/6/17 13:07
 * @Author Lin
 * @Version 1.0
 */
@Controller
public class IndexController {
    @Autowired
    private CategoryService categoryService;

    @GetMapping({"/", "/index.html"})
    public String indexPage(Model model) {
        // TODO：1、查出所有的以及分类
        List<CategoryEntity> categoryEntityList = categoryService.getLevel1Categorys();

        model.addAttribute("categorys", categoryEntityList);
        // 视图解析器进行拼串，有默认的前缀和后缀
        return "index";
    }

    @ResponseBody
    @GetMapping("/index/catalog.json")
    public Map<String, List<Catelog2Vo>> getCatelogJson() {
        return categoryService.getCatalogJson();
    }
}
