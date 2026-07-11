package com.example.controller;

import com.example.common.Result;
import com.example.entity.UserAddress;
import com.example.service.UserAddressService;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/userAddress")
public class UserAddressController {

    @Autowired
    private UserAddressService addressService;

    // 获取用户所有地址（用于下单选择）
    @GetMapping("/list")
    public Result list(@RequestParam Integer userId) {
        List<UserAddress> list = addressService.getAddressByUserId(userId);
        return Result.success(list);
    }

    // 分页查询（用于管理页）
    @GetMapping("/selectPage")
    public Result selectPage(@RequestParam Integer userId,
                             @RequestParam(required = false) String keyword,
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize) {
        PageInfo<UserAddress> page = addressService.selectPage(userId, keyword, pageNum, pageSize);
        return Result.success(page);
    }

    // 新增地址
    @PostMapping("/add")
    public Result add(@RequestBody UserAddress address) {
        if (address.getUserId() == null || address.getReceiverName() == null ||
                address.getReceiverPhone() == null || address.getDetailAddress() == null) {
            return Result.error("参数不完整");
        }
        if (address.getIsDefault() == null) {
            address.setIsDefault(0);
        }
        addressService.add(address);
        return Result.success();
    }

    // 更新地址
    @PutMapping("/update")
    public Result update(@RequestBody UserAddress address) {
        if (address.getId() == null) {
            return Result.error("缺少ID");
        }
        addressService.update(address);
        return Result.success();
    }

    // 删除地址
    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Integer id) {
        addressService.delete(id);
        return Result.success();
    }

    // 设为默认地址
    @PutMapping("/setDefault/{id}")
    public Result setDefault(@PathVariable Integer id, @RequestParam Integer userId) {
        addressService.setDefault(id, userId);
        return Result.success();
    }
}