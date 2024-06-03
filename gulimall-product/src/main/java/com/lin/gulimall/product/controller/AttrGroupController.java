package com.lin.gulimall.product.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.lin.gulimall.product.entity.AttrEntity;
import com.lin.gulimall.product.service.AttrAttrgroupRelationService;
import com.lin.gulimall.product.service.AttrService;
import com.lin.gulimall.product.service.CategoryService;
import com.lin.gulimall.product.vo.AttrGroupRelationVo;
import com.lin.gulimall.product.vo.AttrGroupWithAttrs;
import com.lin.gulimall.product.vo.AttrRespVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.lin.gulimall.product.entity.AttrGroupEntity;
import com.lin.gulimall.product.service.AttrGroupService;
import com.lin.common.utils.PageUtils;
import com.lin.common.utils.R;


/**
 * 属性分组
 *
 * @author lin
 * @email lin@gmail.com
 * @date 2024-05-21 19:37:44
 */
@RestController
@RequestMapping("product/attrgroup")
public class AttrGroupController {
    @Autowired
    private AttrGroupService attrGroupService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private AttrService attrService;
    @Autowired
    private AttrAttrgroupRelationService attrAttrgroupRelationService;

    @PostMapping("/attr/relation")
    public R addRelation(@RequestBody List<AttrGroupRelationVo> respVos) {
        attrAttrgroupRelationService.saveBatch(respVos);

        return R.ok();
    }

    // {catelogId}/withattr
    @GetMapping("{catelogId}/withattr")
    public R getAttrGroupWithAttr(@PathVariable("catelogId") Long catelogId) {
        // 1、查出当前分类下的所有属性分组
        // 2、查出当前分组下的所有属性
        List<AttrGroupWithAttrs> respVo = attrGroupService.getAttrGroupWithAttrByCatelogId(catelogId);

        return R.ok().put("data", respVo);
    }


    /**
     * 获取属性分组的关联的所有属性
     * /product/attrgroup/{attrgroupId}/attr/relation
     */
    @GetMapping("/{attrgroupId}/attr/relation")
    public R attrRelationList(@PathVariable("attrgroupId") Long attrgroupId) {
        List<AttrEntity> attr = attrService.getRelationAttr(attrgroupId);
        return R.ok().put("data", attr);
    }

    /**
     * 获取属性分组里面还没有关联的本分类里面的其他属性，方便添加新的关联
     */
    @GetMapping("/{attrgroupId}/noattr/relation")
    public R attrNoRelation(@PathVariable("attrgroupId") Long attrgroupId,
                            @RequestParam Map<String, Object> params) {
        PageUtils page = attrService.getNoRelationAttr(params, attrgroupId);
        return R.ok().put("page", page);
    }

    /**
     * 批量删除分组关联的属性
     */
    @PostMapping("/attr/relation/delete")
    public R attrRelationList(@RequestBody AttrGroupRelationVo[] relationVos) {
        attrGroupService.deleteRelationList(relationVos);
        return R.ok();
    }


    /**
     * 列表
     */
    @RequestMapping("/list/{catelogId}")
    //@RequiresPermissions("product:attrgroup:list")
    public R list(@RequestParam Map<String, Object> params, @PathVariable("catelogId") Long catelogId) {
//        PageUtils page = attrGroupService.queryPage(params);
        PageUtils page = attrGroupService.queryPage(params, catelogId);
        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{attrGroupId}")
    //@RequiresPermissions("product:attrgroup:info")
    public R info(@PathVariable("attrGroupId") Long attrGroupId) {
        AttrGroupEntity attrGroup = attrGroupService.getById(attrGroupId);

        Long catelogId = attrGroup.getCatelogId();
        Long[] path = categoryService.findCateLogPath(catelogId);

        attrGroup.setCatelogPath(path);

        return R.ok().put("attrGroup", attrGroup);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("product:attrgroup:save")
    public R save(@RequestBody AttrGroupEntity attrGroup) {
        attrGroupService.save(attrGroup);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("product:attrgroup:update")
    public R update(@RequestBody AttrGroupEntity attrGroup) {
        attrGroupService.updateById(attrGroup);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("product:attrgroup:delete")
    public R delete(@RequestBody Long[] attrGroupIds) {
        attrGroupService.removeByIds(Arrays.asList(attrGroupIds));

        return R.ok();
    }

}
