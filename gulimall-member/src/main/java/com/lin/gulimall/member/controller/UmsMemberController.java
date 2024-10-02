package com.lin.gulimall.member.controller;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

import com.lin.common.exception.BizCodeEnum;
import com.lin.gulimall.member.exception.PhoneExistException;
import com.lin.gulimall.member.exception.UserNameExistException;
import com.lin.gulimall.member.feign.CouponFeignService;
import com.lin.gulimall.member.vo.MemberLoginVo;
import com.lin.gulimall.member.vo.MemberRegisterVo;
import com.lin.gulimall.member.vo.SocialUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.lin.gulimall.member.entity.UmsMemberEntity;
import com.lin.gulimall.member.service.UmsMemberService;
import com.lin.common.utils.PageUtils;
import com.lin.common.utils.R;


/**
 * 会员
 *
 * @author lin
 * @email lin@gmail.com
 * @date 2024-05-22 12:47:30
 */
@RestController
@RequestMapping("member/member")
public class UmsMemberController {
    @Autowired
    private UmsMemberService umsMemberService;

    @Autowired
    private CouponFeignService couponFeignService;

    @RequestMapping("/coupons")
    public R test() {
        UmsMemberEntity memberEntity = new UmsMemberEntity();
        memberEntity.setNickname("张三");

        R r = couponFeignService.memberCoupons();

        return Objects.requireNonNull(R.ok().put("member", memberEntity)).put("coupons", r.get("coupons"));

    }

    @PostMapping("/oauth2/login")
    public R oauthLogin(@RequestBody SocialUser vo) {
        UmsMemberEntity entity = umsMemberService.login(vo);
        if (entity != null) {
            return R.ok().setData(entity);
        } else {
            return R.error(BizCodeEnum.LOGINACCT_PASSWORD_INVALID_EXCEPTION.getCode(), BizCodeEnum.LOGINACCT_PASSWORD_INVALID_EXCEPTION.getMsg());
        }
    }

    @PostMapping("/login")
    public R login(@RequestBody MemberLoginVo vo) {
        UmsMemberEntity entity = umsMemberService.login(vo);
        if (entity != null) {
            return R.ok().setData(entity);
        } else {
            return R.error(BizCodeEnum.LOGINACCT_PASSWORD_INVALID_EXCEPTION.getCode(), BizCodeEnum.LOGINACCT_PASSWORD_INVALID_EXCEPTION.getMsg());
        }
    }

    @PostMapping("/regist")
    public R register(@RequestBody MemberRegisterVo vo) {
        try {
            umsMemberService.register(vo);
        } catch (PhoneExistException e) {
            R.error(BizCodeEnum.PHONE_EXIST_EXCEPTION.getCode(), BizCodeEnum.PHONE_EXIST_EXCEPTION.getMsg());
        } catch (UserNameExistException e) {
            R.error(BizCodeEnum.USER_EXIST_EXCEPTION.getCode(), BizCodeEnum.USER_EXIST_EXCEPTION.getMsg());
        }
        return R.ok();
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("member:umsmember:list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = umsMemberService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("member:umsmember:info")
    public R info(@PathVariable("id") Long id) {
        UmsMemberEntity umsMember = umsMemberService.getById(id);

        return R.ok().put("umsMember", umsMember);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("member:umsmember:save")
    public R save(@RequestBody UmsMemberEntity umsMember) {
        umsMemberService.save(umsMember);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("member:umsmember:update")
    public R update(@RequestBody UmsMemberEntity umsMember) {
        umsMemberService.updateById(umsMember);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("member:umsmember:delete")
    public R delete(@RequestBody Long[] ids) {
        umsMemberService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
