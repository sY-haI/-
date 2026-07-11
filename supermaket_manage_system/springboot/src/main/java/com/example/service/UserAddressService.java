package com.example.service;

import com.example.entity.UserAddress;
import com.example.mapper.UserAddressMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserAddressService {

    @Autowired
    private UserAddressMapper addressMapper;

    public List<UserAddress> getAddressByUserId(Integer userId) {
        return addressMapper.selectByUserId(userId);
    }

    @Transactional
    public void add(UserAddress address) {
        if (address.getIsDefault() != null && address.getIsDefault() == 1) {
            addressMapper.cancelDefaultByUserId(address.getUserId());
        }
        addressMapper.insert(address);
    }

    public PageInfo<UserAddress> selectPage(Integer userId, String keyword, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<UserAddress> list = addressMapper.selectByKeyword(userId, keyword);
        return new PageInfo<>(list);
    }

    @Transactional
    public void update(UserAddress address) {
        if (address.getIsDefault() != null && address.getIsDefault() == 1) {
            addressMapper.cancelDefaultByUserId(address.getUserId());
        }
        addressMapper.updateById(address);
    }

    public void delete(Integer id) {
        addressMapper.deleteById(id);
    }

    @Transactional
    public void setDefault(Integer id, Integer userId) {
        addressMapper.cancelDefaultByUserId(userId);
        addressMapper.updateDefaultById(id, 1);
    }
}