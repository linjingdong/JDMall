package com.lin.gulimall.member.dao;

import com.lin.gulimall.member.entity.UmsMemberLevelEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会员等级
 * 
 * @author lin
 * @email lin@gmail.com
 * @date 2024-05-22 12:47:30
 */
@Mapper
public interface UmsMemberLevelDao extends BaseMapper<UmsMemberLevelEntity> {

    UmsMemberLevelEntity getDefaultLevel();
}
