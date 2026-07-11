package com.example.controller;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.example.common.Result;
import com.example.entity.*;
import com.example.mapper.OrderDetailMapper;
import com.example.service.*;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;


@RestController
public class WebController {

    @Resource
    private AdminService adminService;
    @Resource
    private UserService userService;
    @Resource
    private OrdersService ordersService;
    @Resource
    private GoodsService goodsService;
    @Resource
    private CategoryService categoryService;
    @Resource
    private OrderDetailMapper orderDetailMapper;



    /**
     * 默认请求接口
     */
    @GetMapping("/")
    public Result hello() {
        return Result.success();
    }

    /**
     * 登录
     */
    @PostMapping("/login")
    public Result login(@RequestBody Account account) {
        Account ac = null;
        if ("管理员".equals(account.getRole())) {
            ac = adminService.login(account);
        }
        if ("普通用户".equals(account.getRole())) {
            ac = userService.login(account);
        }
        if(ac == null){
            return Result.error("登陆失败，用户不存在！");
        }
        return Result.success(ac);
    }

    /**
     * 注册
     */
    @PostMapping("/register")
    public Result register(@RequestBody User user) {
        if(!user.getPassword().equals(user.getNewPassword())){
            return Result.error("两次输入的密码不一致");
        }
        userService.add(user);
        return Result.success();
    }

    /**
     * 修改密码
     */
    @PutMapping("/updatePassword")
    public Result updatePassword(@RequestBody Account account) {
        if ("管理员".equals(account.getRole())) {
            adminService.updatePassword(account);
        }
        if ("普通用户".equals(account.getRole())) {
            userService.updatePassword(account);
        }
        return Result.success();
    }

    //后台数据统计接口（使用实付金额 actual_price）
    @GetMapping("/count")
    public Result count(){
        List<Orders> ordersList = ordersService.selectAll(null).stream()
                .filter(orders -> !orders.getStatus().equals("已取消"))
                .toList();

        // 销售总额：使用实付金额 actual_price
        BigDecimal total = ordersList.stream()
                .map(orders -> orders.getActualPrice() != null ? orders.getActualPrice() : orders.getTotal())
                .reduce(BigDecimal::add)
                .orElse(BigDecimal.ZERO);

        String todayDate = DateUtil.today();
        // 今日销售额：使用实付金额 actual_price
        BigDecimal today = ordersList.stream()
                .filter(orders -> orders.getTime().contains(todayDate))
                .map(orders -> orders.getActualPrice() != null ? orders.getActualPrice() : orders.getTotal())
                .reduce(BigDecimal::add)
                .orElse(BigDecimal.ZERO);

        Integer goods = goodsService.selectAll(null).size();
        Integer user = userService.selectByAll(null).size();
        Map<String, Object> map = new HashMap<>();
        map.put("total", total);
        map.put("today", today);
        map.put("goods", goods);
        map.put("user", user);
        return Result.success(map);
    }

    @GetMapping("/selectLine")
    public Result selectLine(){
        Date date = new Date();
        DateTime start = DateUtil.offsetDay(date, -6);
        List<DateTime> dateTimes = DateUtil.rangeToList(start, date, DateField.DAY_OF_MONTH);
        List<String> dateStrList = dateTimes.stream()
                .map(dateTime -> DateUtil.format(dateTime, "MM-dd"))
                .sorted()
                .toList();

        List<Orders> ordersList = ordersService.selectAll(null).stream()
                .filter(orders -> !orders.getStatus().equals("已取消"))
                .toList();

        int year = DateUtil.year(date);
        ArrayList<BigDecimal> countList = new ArrayList<>();

        for(String day : dateStrList){
            // 使用实付金额 actual_price
            BigDecimal total = ordersList.stream()
                    .filter(o -> o.getTime().contains(String.valueOf(year)) && o.getTime().contains(day))
                    .map(o -> o.getActualPrice() != null ? o.getActualPrice() : o.getTotal())
                    .reduce(BigDecimal::add)
                    .orElse(BigDecimal.ZERO);
            countList.add(total);
        }

        Map<String, Object> map = new HashMap<>();
        map.put("date", dateStrList);
        map.put("count", countList);
        return Result.success(map);
    }

    @GetMapping("/selectPie")
    public Result selectPie(){
        List<Map<String, Object>> list = new ArrayList<>();
        List<Category> categoryList = categoryService.selectAll(null);

        // 获取所有未取消的订单
        List<Orders> ordersList = ordersService.selectAll(null).stream()
                .filter(orders -> !orders.getStatus().equals("已取消"))
                .toList();

        // 创建订单ID到实付金额的映射
        Map<Integer, BigDecimal> orderActualPriceMap = new HashMap<>();
        for (Orders order : ordersList) {
            BigDecimal actualPrice = order.getActualPrice() != null ? order.getActualPrice() : order.getTotal();
            orderActualPriceMap.put(order.getId(), actualPrice);
        }

        for (Category category : categoryList) {
            Map<String, Object> map = new HashMap<>();
            map.put("name", category.getName());
            BigDecimal total = BigDecimal.ZERO;

            List<OrderDetail> orderDetailsList = orderDetailMapper.selectAll(null);
            for (OrderDetail orderDetail : orderDetailsList) {
                Integer orderId = orderDetail.getOrderId();
                // 检查订单是否未取消
                if (orderActualPriceMap.containsKey(orderId)) {
                    Integer goodsId = orderDetail.getGoodsId();
                    Goods goods = goodsService.selectById(goodsId);
                    if (goods != null && goods.getCategoryId() != null && goods.getCategoryId().equals(category.getId())) {
                        // 计算该商品在该订单中的销售额占比
                        BigDecimal orderActualPrice = orderActualPriceMap.get(orderId);
                        BigDecimal orderTotal = ordersService.selectById(orderId).getTotal();
                        // 按商品价格比例分配订单实付金额
                        if (orderTotal != null && orderTotal.compareTo(BigDecimal.ZERO) > 0) {
                            BigDecimal itemPrice = orderDetail.getGoodsPrice().multiply(BigDecimal.valueOf(orderDetail.getNum()));
                            BigDecimal ratio = itemPrice.divide(orderTotal, 4, BigDecimal.ROUND_HALF_UP);
                            BigDecimal categoryContribution = orderActualPrice.multiply(ratio);
                            total = total.add(categoryContribution);
                        }
                    }
                }
            }

            map.put("value", total);
            if(total.compareTo(BigDecimal.ZERO) > 0){
                list.add(map);
            }
        }
        return Result.success(list);
    }
}