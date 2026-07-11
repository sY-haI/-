package com.example.mapper;

import com.example.entity.UserAddress;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface UserAddressMapper {
    List<UserAddress> selectByUserId(Integer userId);
    List<UserAddress> selectByKeyword(@Param("userId") Integer userId, @Param("keyword") String keyword);
    int insert(UserAddress address);
    int updateById(UserAddress address);
    int deleteById(Integer id);
    int updateDefaultById(@Param("id") Integer id, @Param("isDefault") Integer isDefault);
    void cancelDefaultByUserId(Integer userId);
}