package com.lin.gulimall.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lin.common.utils.PageUtils;
import com.lin.gulimall.member.entity.UmsMemberEntity;
import com.lin.gulimall.member.exception.PhoneExistException;
import com.lin.gulimall.member.exception.UserNameExistException;
import com.lin.gulimall.member.vo.MemberLoginVo;
import com.lin.gulimall.member.vo.MemberRegisterVo;
import com.lin.gulimall.member.vo.SocialUser;

import java.util.Map;

/**
 * 会员
 *
 * @author lin
 * @email lin@gmail.com
 * @date 2024-05-22 12:47:30
 */
public interface UmsMemberService extends IService<UmsMemberEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void register(MemberRegisterVo vo);

    void checkPhoneUnique(String phone) throws PhoneExistException;

    void checkUsernameUnique(String username) throws UserNameExistException;

    UmsMemberEntity login(MemberLoginVo vo);

    UmsMemberEntity login(SocialUser vo);
}

