package com.example.mapper;

import com.example.entity.MemberLevel;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MemberLevelMapper {

    // 根据积分查询对应的会员等级
    MemberLevel selectByPoints(@Param("points") Integer points);

    // 查询所有等级
    List<MemberLevel> selectAll();

    // 根据ID查询
    MemberLevel selectById(Integer id);
}
