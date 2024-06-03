package com.lin.gulimall.product.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.lin.gulimall.product.entity.CategoryEntity;
import com.lin.gulimall.product.service.CategoryService;
import com.lin.common.utils.PageUtils;
import com.lin.common.utils.R;



/**
 * 商品三级分类
 *
 * @author lin
 * @email lin@gmail.com
 * @date 2024-05-21 19:37:44
 */
@RestController
@RequestMapping("product/category")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    /**
     * 查出所有分类以及子分类，并将他们封装成树结构
     */
    @RequestMapping("/list/tree")
    public R list(){
        List<CategoryEntity> entities = categoryService.listWithTree();

        return R.ok().put("data", entities);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{catId}")
    //@RequiresPermissions("product:category:info")
    public R info(@PathVariable("catId") Long catId){
		CategoryEntity category = categoryService.getById(catId);

        return R.ok().put("data", category);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("product:category:save")
    public R save(@RequestBody CategoryEntity category){
		categoryService.save(category);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("product:category:update")
    public R update(@RequestBody CategoryEntity category){
		categoryService.updateDetail(category);

        return R.ok();
    }

    /**
     * 修改： 批量修改客户端传来的集合的排序以及层级
     */
    @RequestMapping("/update/sort")
    //@RequiresPermissions("product:category:update")
    public R updateSort(@RequestBody List<CategoryEntity> categoryList){
        categoryService.updateBatchById(categoryList);
        return R.ok();
    }


    /**
     * 删除
     * @RequestBody：获取请求体内的数据，必须发送的是POST请求才能有请求体
     * springmvc自动将请求体的（JSON）数据转化封装为对应的对象数据
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("product:category:delete")
    public R delete(@RequestBody Long[] catIds){
		// categoryService.removeByIds(Arrays.asList(catIds));

        categoryService.removeMenusByIds(Arrays.asList(catIds));
        return R.ok();
    }

}
