package com.example.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import com.example.entity.*;
import com.example.exception.CustomException;
import com.example.mapper.*;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

/**
 * 订单业务处理
 **/
@Service
public class OrdersService {

    @Resource
    private OrdersMapper ordersMapper;
    @Resource
    GoodsMapper goodsMapper;
    @Resource
    UserMapper userMapper;
    @Resource
    OrderDetailMapper orderDetailMapper;
    @Resource
    CartMapper cartMapper;
    @Resource
    private PointsService pointsService;  // 添加积分服务

    /**
     * 根据积分获取折扣率
     */
    private BigDecimal getDiscountByPoints(Integer points) {
        if (points == null) {
            return BigDecimal.ONE;
        }
        // 根据积分设置折扣
        if (points >= 10000) {
            return new BigDecimal("0.90");  // 10000积分以上 9折
        } else if (points >= 4000) {
            return new BigDecimal("0.93");  // 4000积分以上 93折
        } else if (points >= 2000) {
            return new BigDecimal("0.95");  // 2000积分以上 95折
        } else if (points >= 1000) {
            return new BigDecimal("0.98");  // 1000积分以上 98折
        } else if (points >= 500) {
            return new BigDecimal("0.99");  // 500积分以上 99折
        }
        return BigDecimal.ONE;  // 无折扣
    }

    /**
     * 获取折扣描述
     */
    private String getDiscountDesc(Integer points) {
        if (points == null) {
            return "";
        }
        if (points >= 10000) {
            return "至尊会员9折";
        } else if (points >= 4000) {
            return "钻石会员93折";
        } else if (points >= 2000) {
            return "铂金会员95折";
        } else if (points >= 1000) {
            return "黄金会员98折";
        } else if (points >= 500) {
            return "白银会员99折";
        }
        return "";
    }

    /**
     * 新增
     */
    @Transactional
    public void add(Orders orders) {
        orders.setStatus("待接单");
        orders.setTime(DateUtil.now());
        //随机订单编号
        String orderNo = DateUtil.format(new Date(), "yyyyMMdd" + System.currentTimeMillis() + RandomUtil.randomNumbers(4));
        orders.setOrderNo(orderNo);
        ordersMapper.insert(orders);
        Integer orderId = orders.getId();
        List<Cart> cartList = orders.getCartList();
        User user = userMapper.selectById(orders.getUserId());

        // 计算原价总价
        BigDecimal originalTotalPrice = BigDecimal.ZERO;
        for(Cart cart : cartList){
            Integer goodsId = cart.getGoodsId();
            Goods goods = goodsMapper.selectById(goodsId);
            if(goods.getStore() < cart.getNum()){
                throw new CustomException(goods.getName() + "商品库存不足");
            }
            goods.setStore(goods.getStore() - cart.getNum());   //减库存
            goods.setSaleCount(goods.getSaleCount() + cart.getNum());   //加销量
            goodsMapper.updateById(goods);
            //新增订单详情
            OrderDetail ordersDetail = new OrderDetail();
            ordersDetail.setGoodsId(goodsId);
            ordersDetail.setNum(cart.getNum());
            ordersDetail.setGoodsImg(goods.getImg());
            ordersDetail.setGoodsName(goods.getName());
            ordersDetail.setGoodsPrice(goods.getPrice());
            ordersDetail.setOrderId(orderId);
            orderDetailMapper.insert(ordersDetail);

            //删除购物车下单商品
            if(cart.getId() != null){
                cartMapper.deleteById(cart.getId());
            }

            originalTotalPrice = originalTotalPrice.add(goods.getPrice().multiply(BigDecimal.valueOf(cart.getNum())));
        }

        // 获取会员折扣
        BigDecimal discount = getDiscountByPoints(user.getPoints());
        BigDecimal actualPrice = originalTotalPrice.multiply(discount).setScale(2, RoundingMode.HALF_UP);

        // 检查余额（使用折扣后的价格）
        if(user.getAccount().compareTo(actualPrice) < 0){
            throw new CustomException("对不起，你的当前余额不足，请充值！");
        }

        // 扣款
        user.setAccount(user.getAccount().subtract(actualPrice));

        // 增加积分（按实付金额计算，每消费1元增加1积分）
        int pointsToAdd = actualPrice.intValue();
        if (pointsToAdd > 0) {
            int newPoints = (user.getPoints() == null ? 0 : user.getPoints()) + pointsToAdd;
            user.setPoints(newPoints);
        }

        userMapper.updateById(user);

        // 保存订单信息
        orders.setTotal(originalTotalPrice);      // 原价
        orders.setActualPrice(actualPrice);       // 实付价
        orders.setDiscountDesc(getDiscountDesc(user.getPoints())); // 折扣描述
        ordersMapper.updateById(orders);

        // 添加积分记录
        if (pointsToAdd > 0) {
            pointsService.addPointsForOrder(orders.getUserId(), orderId, actualPrice);
        }
    }

    /**
     * 删除
     */
    @Transactional
    public void deleteById(Integer id) {
        ordersMapper.deleteById(id);
        orderDetailMapper.deleteByOrderId(id);
    }

    /**
     * 修改
     */
    @Transactional
    public void updateById(Orders orders) {
        if("已取消".equals(orders.getStatus())){
            Integer userId = orders.getUserId();
            User user = userMapper.selectById(userId);
            // 退还实际支付的金额
            BigDecimal actualPrice = orders.getActualPrice() != null ? orders.getActualPrice() : orders.getTotal();
            user.setAccount(user.getAccount().add(actualPrice));

            // 扣除获得的积分
            int pointsToDeduct = actualPrice.intValue();
            if (pointsToDeduct > 0) {
                int newPoints = (user.getPoints() == null ? 0 : user.getPoints()) - pointsToDeduct;
                if (newPoints < 0) newPoints = 0;
                user.setPoints(newPoints);
            }
            userMapper.updateById(user);

            // 商品回退
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setOrderId(orders.getId());
            List<OrderDetail> orderDetailList = orderDetailMapper.selectAll(orderDetail);
            for (OrderDetail detail : orderDetailList) {
                Integer goodsId = detail.getGoodsId();
                Goods goods = goodsMapper.selectById(goodsId);
                if(goods != null){
                    goods.setStore(goods.getStore() + detail.getNum());
                    goods.setSaleCount(goods.getSaleCount() - detail.getNum());
                    goodsMapper.updateById(goods);
                }
            }
            // 取消订单扣除获得的积分
            pointsService.deductPointsForCancelOrder(orders.getId());
        }
        ordersMapper.updateById(orders);
    }

    /**
     * 根据ID查询
     */
    public Orders selectById(Integer id) {
        return ordersMapper.selectById(id);
    }

    /**
     * 查询所有
     */
    public List<Orders> selectAll(Orders orders) {
        return ordersMapper.selectAll(orders);
    }

    /**
     * 分页查询
     */
    public PageInfo<Orders> selectPage(Orders orders, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Orders> list = ordersMapper.selectAll(orders);
        for (Orders o : list){
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setOrderId(o.getId());
            List<OrderDetail> orderDetailList = orderDetailMapper.selectAll(orderDetail);
            o.setOrderDetailList(orderDetailList);
        }
        return PageInfo.of(list);
    }
}