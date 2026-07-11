package com.example.controller;

import com.example.common.Result;
import com.example.entity.PurchaseRecord;
import com.example.service.PurchaseRecordService;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/purchaseRecord")
public class PurchaseRecordController {

    @Resource
    private PurchaseRecordService purchaseRecordService;

    @PostMapping("/add")
    public Result add(@RequestBody PurchaseRecord record) {
        purchaseRecordService.add(record);
        return Result.success();
    }

    @DeleteMapping("/delete/{id}")
    public Result deleteById(@PathVariable Integer id) {
        purchaseRecordService.deleteById(id);
        return Result.success();
    }

    @PutMapping("/update")
    public Result updateById(@RequestBody PurchaseRecord record) {
        purchaseRecordService.updateById(record);
        return Result.success();
    }

    @GetMapping("/selectById/{id}")
    public Result selectById(@PathVariable Integer id) {
        return Result.success(purchaseRecordService.selectById(id));
    }

    @GetMapping("/selectAll")
    public Result selectAll(PurchaseRecord record) {
        return Result.success(purchaseRecordService.selectAll(record));
    }

    @GetMapping("/selectPage")
    public Result selectPage(PurchaseRecord record,
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(purchaseRecordService.selectPage(record, pageNum, pageSize));
    }

    /** 单条标记为已处理（用于进货记录页面） */
    @PutMapping("/handle/{id}")
    public Result handleRecord(@PathVariable Integer id) {
        PurchaseRecord record = purchaseRecordService.selectById(id);
        if (record == null) {
            return Result.error("记录不存在");
        }
        record.setHandled(1);
        purchaseRecordService.updateById(record);
        return Result.success();
    }

    /** 批量处理：标记某商品所有未处理记录为已处理（用于预警页面） */
    @PutMapping("/handleByGoods/{goodsId}")
    public Result handleByGoods(@PathVariable Integer goodsId) {
        purchaseRecordService.markHandledByGoodsId(goodsId);
        return Result.success();
    }
}