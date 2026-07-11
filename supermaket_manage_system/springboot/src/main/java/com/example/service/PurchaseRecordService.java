package com.example.service;

import com.example.entity.PurchaseRecord;
import com.example.mapper.PurchaseRecordMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class PurchaseRecordService {

    @Resource
    private PurchaseRecordMapper purchaseRecordMapper;

    public void add(PurchaseRecord record) {
        purchaseRecordMapper.insert(record);
    }

    public void deleteById(Integer id) {
        purchaseRecordMapper.deleteById(id);
    }

    public void updateById(PurchaseRecord record) {
        purchaseRecordMapper.updateById(record);
    }

    public PurchaseRecord selectById(Integer id) {
        return purchaseRecordMapper.selectById(id);
    }

    public List<PurchaseRecord> selectAll(PurchaseRecord record) {
        return purchaseRecordMapper.selectAll(record);
    }

    public PageInfo<PurchaseRecord> selectPage(PurchaseRecord record, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<PurchaseRecord> list = purchaseRecordMapper.selectAll(record);
        return PageInfo.of(list);
    }

    /**
     * 将某商品所有未处理且有过期日期的记录标记为已处理
     */
    @Transactional
    public void markHandledByGoodsId(Integer goodsId) {
        List<PurchaseRecord> records = purchaseRecordMapper.selectByGoodsIdAndUnhandled(goodsId);
        for (PurchaseRecord record : records) {
            record.setHandled(1);
            purchaseRecordMapper.updateById(record);
        }
    }
}