package com.example.service;


import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.example.entity.Account;
import com.example.entity.Admin;
import com.example.entity.User;
import com.example.exception.CustomException;
import com.example.mapper.UserMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class UserService {

    @Resource
    private UserMapper userMapper;

    /**
     * 分页查询方法
     * @param pageNum
     * @param pageSize
     * @return
     */
    public PageInfo<User> selectPage(Integer pageNum, Integer pageSize, String name){
        PageHelper.startPage(pageNum, pageSize);
        List<User> list = userMapper.selectAll(name);
        return PageInfo.of(list);
    }

    public void deleteById(Integer id) {
        userMapper.deleteById(id);
    }

    public void add(User user) {
        String username = user.getUsername();
        User dbUser = userMapper.selectByUsername(username);
        if(dbUser != null){
            throw new CustomException("账号已拥有，添加失败！");
        }
        if(StrUtil.isBlank(user.getPassword())){
            //默认密码
            user.setPassword("123");
        }
        if(StrUtil.isBlank(user.getName())){
            //默认姓名
            user.setName(user.getUsername());
        }
        user.setRole("普通用户"); //默认用户类型
        user.setAccount(BigDecimal.ZERO); //默认账户余额
        userMapper.insert(user);
    }

    public void update(User user) {
        userMapper.updateById(user);
    }

    public Account login(Account account) {
        User dbUser = userMapper.selectByUsername(account.getUsername());
        if (ObjectUtil.isNull(dbUser)) {
            throw new CustomException("用户不存在");
        }
        if (!account.getPassword().equals(dbUser.getPassword())) {
            throw new CustomException("账号或密码错误");
        }
        return dbUser;
    }

    public User selectById(Integer id) {
        return userMapper.selectById(id);
    }

    public void updatePassword(Account account) {
        User dbUser = userMapper.selectByUsername(account.getUsername());
        if (ObjectUtil.isNull(dbUser)) {
            throw new CustomException("用户不存在");
        }
        if (!account.getPassword().equals(dbUser.getPassword())) {
            throw new CustomException("原密码错误");
        }
        dbUser.setPassword(account.getNewPassword());
        userMapper.updateById(dbUser);
    }

    public List<User> selectByAll(String name) {
        return userMapper.selectAll(name);
    }
}
