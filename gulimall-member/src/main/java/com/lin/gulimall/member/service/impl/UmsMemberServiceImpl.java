package com.lin.gulimall.member.service.impl;

import com.lin.common.constant.ProductConstant;
import com.lin.gulimall.member.dao.UmsMemberLevelDao;
import com.lin.gulimall.member.entity.UmsMemberLevelEntity;
import com.lin.gulimall.member.exception.PhoneExistException;
import com.lin.gulimall.member.exception.UserNameExistException;
import com.lin.gulimall.member.vo.MemberLoginVo;
import com.lin.gulimall.member.vo.MemberRegisterVo;
import com.lin.gulimall.member.vo.SocialUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lin.common.utils.PageUtils;
import com.lin.common.utils.Query;

import com.lin.gulimall.member.dao.UmsMemberDao;
import com.lin.gulimall.member.entity.UmsMemberEntity;
import com.lin.gulimall.member.service.UmsMemberService;


@Service("umsMemberService")
public class UmsMemberServiceImpl extends ServiceImpl<UmsMemberDao, UmsMemberEntity> implements UmsMemberService {
    @Autowired
    private UmsMemberLevelDao memberLevelDao;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<UmsMemberEntity> page = this.page(new Query<UmsMemberEntity>().getPage(params), new QueryWrapper<UmsMemberEntity>());

        return new PageUtils(page);
    }

    @Override
    public void register(MemberRegisterVo vo) {
        UmsMemberEntity entity = new UmsMemberEntity();

        UmsMemberLevelEntity levelEntity = memberLevelDao.getDefaultLevel();
        // 1、设置默认等级
        entity.setLevelId(levelEntity.getId());

        // 2、检查用户名和手机号是否唯一，为了让controller感知到异常，使用异常机制，若是不唯一则保存
        checkPhoneUnique(vo.getPhone());
        checkUsernameUnique(vo.getUsername());

        entity.setUsername(vo.getUsername());
        entity.setMobile(vo.getPhone());
        entity.setNickname(vo.getUsername());

        // 3、用密文存储密码
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String encode = encoder.encode(vo.getPassword());
        entity.setPassword(encode);

        // 4、还有其他的默认设置

        // 5、保存用户
        this.baseMapper.insert(entity);
    }

    @Override
    public void checkPhoneUnique(String phone) throws PhoneExistException {
        Integer count = this.baseMapper.selectCount(
                new QueryWrapper<UmsMemberEntity>().eq("mobile", phone)
        );
        if (count > 0) {
            throw new PhoneExistException();
        }
    }

    @Override
    public void checkUsernameUnique(String username) throws UserNameExistException {
        Integer count = this.baseMapper.selectCount(
                new QueryWrapper<UmsMemberEntity>().eq("username", username)
        );
        if (count > 0) {
            throw new UserNameExistException();
        }
    }

    @Override
    public UmsMemberEntity login(MemberLoginVo vo) {
        String loginacct = vo.getLoginacct();
        String password = vo.getPassword();

        UmsMemberEntity memberEntity = this.baseMapper.selectOne(
                new QueryWrapper<UmsMemberEntity>()
                        .eq("username", loginacct)
                        .or().eq("mobile", loginacct)
        );

        if (memberEntity == null) {
            return null; // 登录失败
        } else {
            String encodePassword = memberEntity.getPassword();
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
            boolean matches = encoder.matches(password, encodePassword);
            if (matches) {
                return memberEntity;
            } else {
                return null;
            }
        }
    }

    /**
     * 通过社交账号进行登录，具有登录和注册的合并逻辑
     *
     * @param vo 社交用户的信息
     * @return 本系统中的会员信息
     */
    @Override
    public UmsMemberEntity login(SocialUser vo) {
        String uid = vo.getUid();

        // 1、判断该社交用户是否在本系统当中登录过
        UmsMemberEntity memberEntity = this.baseMapper.selectOne(
                new QueryWrapper<UmsMemberEntity>().eq("social_uid", uid)
        );

        if (memberEntity != null) { // 表示该用户已经被注册过，更新该用户的令牌信息以及过期时间
            UmsMemberEntity updateEntity = new UmsMemberEntity();
            updateEntity.setId(memberEntity.getId());
            updateEntity.setAccessToken(vo.getAccessToken());
            updateEntity.setExpiresIn(vo.getExpiresIn());

            this.baseMapper.updateById(updateEntity); // 更新数据库

            memberEntity.setAccessToken(vo.getAccessToken()); // 更新查出来的数据，返回给客户端
            memberEntity.setExpiresIn(vo.getExpiresIn());

            return memberEntity;
        } else {
            // 2、如果没有查出来意味着没有注册过这个用户，那么我们需要为该社交用户注册一个本地用户
            UmsMemberEntity newMemberEntity = new UmsMemberEntity();
            newMemberEntity.setNickname(vo.getName());
            newMemberEntity.setLevelId(1L);
            newMemberEntity.setSocialUid(vo.getUid());
            newMemberEntity.setAccessToken(vo.getAccessToken());
            newMemberEntity.setExpiresIn(vo.getExpiresIn());

            this.baseMapper.insert(newMemberEntity);

            return newMemberEntity;
        }
    }
}