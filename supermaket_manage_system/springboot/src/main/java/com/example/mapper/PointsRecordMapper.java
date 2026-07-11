package com.example.mapper;

import com.example.entity.PointsRecord;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface PointsRecordMapper {

    // 插入
    int insert(PointsRecord record);

    // 根据ID删除
    int deleteById(Integer id);

    // 根据ID更新
    int updateById(PointsRecord record);

    // 根据ID查询
    PointsRecord selectById(Integer id);

    // 根据用户ID查询
    List<PointsRecord> selectByUserId(@Param("userId") Integer userId);

    // 根据订单ID查询
    List<PointsRecord> selectByOrderId(@Param("orderId") Integer orderId);

    // 分页查询
    List<PointsRecord> selectPage(@Param("userId") Integer userId,
                                  @Param("offset") int offset,
                                  @Param("pageSize") int pageSize);

    // 查询总数
    int selectTotal(@Param("userId") Integer userId);

    // 统计用户积分总和
    Integer sumPointsByUserId(@Param("userId") Integer userId,
                              @Param("type") String type);

    // 根据条件查询
    List<PointsRecord> selectByCondition(@Param("userId") Integer userId,
                                         @Param("orderId") Integer orderId,
                                         @Param("type") String type);
}