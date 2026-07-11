package com.example.controller;

import com.example.common.Result;
import com.example.entity.Supplier;
import com.example.service.SupplierService;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 供货商前端操作接口
 **/
@RestController
@RequestMapping("/supplier")
public class SupplierController {

    @Resource
    private SupplierService supplierService;

    /**
     * 验证手机号格式
     * @param phone 手机号
     * @return true:有效 false:无效
     */
    private boolean isValidPhone(String phone) {
        // 检查是否为空
        if (phone == null || phone.trim().isEmpty()) {
            return false;
        }
        // 去除空格
        phone = phone.trim();
        // 检查长度是否为11位
        if (phone.length() != 11) {
            return false;
        }
        // 检查是否全部为数字
        if (!phone.matches("\\d+")) {
            return false;
        }
        // 检查第一位是否为1
        if (!phone.startsWith("1")) {
            return false;
        }
        // 检查第二位是否在3-9之间
        char secondChar = phone.charAt(1);
        if (secondChar < '3' || secondChar > '9') {
            return false;
        }
        return true;
    }

    /**
     * 新增
     */
    @PostMapping("/add")
    public Result add(@RequestBody Supplier supplier) {
        // 验证手机号
        String contact = supplier.getContact();
        if (contact == null || contact.trim().isEmpty()) {
            return Result.error("联系方式不能为空");
        }
        if (!isValidPhone(contact)) {
            return Result.error("手机号格式不正确，必须是11位数字（1开头，第二位3-9）");
        }

        supplierService.add(supplier);
        return Result.success();
    }

    /**
     * 删除
     */
    @DeleteMapping("/delete/{id}")
    public Result deleteById(@PathVariable Integer id) {
        supplierService.deleteById(id);
        return Result.success();
    }

    /**
     * 修改
     */
    @PutMapping("/update")
    public Result updateById(@RequestBody Supplier supplier) {
        // 验证手机号
        String contact = supplier.getContact();
        if (contact == null || contact.trim().isEmpty()) {
            return Result.error("联系方式不能为空");
        }
        if (!isValidPhone(contact)) {
            return Result.error("手机号格式不正确，必须是11位数字（1开头，第二位3-9）");
        }

        supplierService.updateById(supplier);
        return Result.success();
    }

    /**
     * 进货（只更新进货数量和进货时间）
     */
    @PutMapping("/purchase")
    public Result purchase(@RequestBody Supplier supplier) {
        Integer purchaseQuantity = supplier.getPurchaseQuantity();
        if (purchaseQuantity == null || purchaseQuantity < 1) {
            return Result.error("进货数量必须大于0");
        }
        supplierService.purchase(supplier);
        return Result.success();
    }

    /**
     * 根据ID查询
     */
    @GetMapping("/selectById/{id}")
    public Result selectById(@PathVariable Integer id) {
        Supplier supplier = supplierService.selectById(id);
        return Result.success(supplier);
    }

    /**
     * 查询所有
     */
    @GetMapping("/selectAll")
    public Result selectAll(Supplier supplier) {
        List<Supplier> list = supplierService.selectAll(supplier);
        return Result.success(list);
    }

    /**
     * 分页查询
     */
    @GetMapping("/selectPage")
    public Result selectPage(Supplier supplier,
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize) {
        PageInfo<Supplier> page = supplierService.selectPage(supplier, pageNum, pageSize);
        return Result.success(page);
    }
}