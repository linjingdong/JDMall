package com.lin.gulimall.seach.controller;

import com.lin.gulimall.seach.service.MallSearchService;
import com.lin.gulimall.seach.vo.SearchParam;
import com.lin.gulimall.seach.vo.SearchResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.Objects;

/**
 * @Description TODO
 * @Date 2024/6/19 22:36
 * @Author Lin
 * @Version 1.0
 */
@Controller
public class SearchController {
    @Autowired
    MallSearchService mallSearchService;

    @GetMapping("/list.html")
    public String listPage(SearchParam param, Model model, HttpServletRequest request) {
        param.set_queryString(request.getQueryString());

        SearchResult result = mallSearchService.search(param);
        model.addAttribute("result", result);
        return "list";
    }
}
