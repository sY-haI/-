package com.example.mapper;

import com.example.entity.PurchaseRecord;
import org.apache.ibatis.annotations.Select;
import java.util.List;

public interface PurchaseRecordMapper {
    int insert(PurchaseRecord record);
    int deleteById(Integer id);
    int updateById(PurchaseRecord record);
    PurchaseRecord selectById(Integer id);
    List<PurchaseRecord> selectAll(PurchaseRecord record);

    /** 查询某商品所有未处理且有过期日期的记录 */
    @Select("select * from purchase_record where goods_id = #{goodsId} and handled = 0 and expiration_date is not null")
    List<PurchaseRecord> selectByGoodsIdAndUnhandled(Integer goodsId);
}