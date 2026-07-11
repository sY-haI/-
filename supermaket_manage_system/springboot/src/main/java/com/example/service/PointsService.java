package com.example.service;

import com.example.entity.MemberLevel;
import com.example.entity.PointsRecord;
import com.example.entity.User;
import com.example.mapper.MemberLevelMapper;
import com.example.mapper.PointsRecordMapper;
import com.example.mapper.UserMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Service
public class PointsService {

    @Resource
    private PointsRecordMapper pointsRecordMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private MemberLevelMapper memberLevelMapper;

    /**
     * 根据积分获取会员等级和折扣
     */
    public MemberLevel getLevelByPoints(Integer points) {
        return memberLevelMapper.selectByPoints(points);
    }

    /**
     * 计算订单折扣后的金额
     */
    public BigDecimal calculateDiscountPrice(BigDecimal originalPrice, Integer userId) {
        User user = userMapper.selectById(userId);
        if (user == null || user.getPoints() == null) {
            return originalPrice;
        }

        MemberLevel level = getLevelByPoints(user.getPoints());
        if (level != null && level.getDiscount() != null) {
            return originalPrice.multiply(level.getDiscount());
        }
        return originalPrice;
    }

    /**
     * 购买商品增加积分（每消费1元增加1积分）
     */
    @Transactional
    public void addPointsForOrder(Integer userId, Integer orderId, BigDecimal orderAmount) {
        // 计算获得积分（消费金额取整）
        int pointsToAdd = orderAmount.intValue();
        if (pointsToAdd <= 0) {
            return;
        }

        // 更新用户积分
        User user = userMapper.selectById(userId);
        int newPoints = (user.getPoints() == null ? 0 : user.getPoints()) + pointsToAdd;
        user.setPoints(newPoints);

        // 更新会员等级
        MemberLevel newLevel = getLevelByPoints(newPoints);
        if (newLevel != null) {
            user.setLevelId(newLevel.getId());
        }

        userMapper.updateById(user);

        // 添加积分记录
        PointsRecord record = new PointsRecord();
        record.setUserId(userId);
        record.setOrderId(orderId);
        record.setPoints(pointsToAdd);
        record.setType("order_gain");
        record.setDescription("购物获得" + pointsToAdd + "积分");
        record.setCreateTime(new Date());
        pointsRecordMapper.insert(record);
    }

    /**
     * 取消订单扣减积分
     */
    @Transactional
    public void deductPointsForCancelOrder(Integer orderId) {
        // 查找订单相关的积分获得记录
        List<PointsRecord> records = pointsRecordMapper.selectByOrderId(orderId);

        for (PointsRecord pr : records) {
            if ("order_gain".equals(pr.getType())) {
                // 扣减积分
                User user = userMapper.selectById(pr.getUserId());
                if (user != null && user.getPoints() != null) {
                    int newPoints = user.getPoints() - pr.getPoints();
                    if (newPoints < 0) newPoints = 0;
                    user.setPoints(newPoints);

                    // 更新会员等级
                    MemberLevel newLevel = getLevelByPoints(newPoints);
                    if (newLevel != null) {
                        user.setLevelId(newLevel.getId());
                    }
                    userMapper.updateById(user);

                    // 添加扣减记录
                    PointsRecord deductRecord = new PointsRecord();
                    deductRecord.setUserId(pr.getUserId());
                    deductRecord.setOrderId(orderId);
                    deductRecord.setPoints(-pr.getPoints());
                    deductRecord.setType("order_cancel");
                    deductRecord.setDescription("取消订单，扣除" + pr.getPoints() + "积分");
                    deductRecord.setCreateTime(new Date());
                    pointsRecordMapper.insert(deductRecord);
                }
            }
        }
    }

    /**
     * 使用积分抵扣金额
     */
    @Transactional
    public BigDecimal usePointsForDiscount(Integer userId, Integer orderId, Integer pointsToUse) {
        User user = userMapper.selectById(userId);
        if (user == null || user.getPoints() == null || user.getPoints() < pointsToUse) {
            return BigDecimal.ZERO;
        }

        // 计算可抵扣金额（100积分抵扣1元）
        BigDecimal discountAmount = new BigDecimal(pointsToUse).divide(new BigDecimal(100));

        // 扣减积分
        int newPoints = user.getPoints() - pointsToUse;
        user.setPoints(newPoints);

        // 更新会员等级
        MemberLevel newLevel = getLevelByPoints(newPoints);
        if (newLevel != null) {
            user.setLevelId(newLevel.getId());
        }

        userMapper.updateById(user);

        // 添加积分记录
        PointsRecord record = new PointsRecord();
        record.setUserId(userId);
        record.setOrderId(orderId);
        record.setPoints(-pointsToUse);
        record.setType("order_use");
        record.setDescription("使用" + pointsToUse + "积分抵扣" + discountAmount + "元");
        record.setCreateTime(new Date());
        pointsRecordMapper.insert(record);

        return discountAmount;
    }

    /**
     * 获取用户积分详情
     */
    public User getUserPointsDetail(Integer userId) {
        User user = userMapper.selectById(userId);
        if (user != null) {
            MemberLevel level = getLevelByPoints(user.getPoints());
            if (level != null) {
                user.setLevelName(level.getLevelName());
                user.setDiscount(level.getDiscount());
            }
        }
        return user;
    }
}
