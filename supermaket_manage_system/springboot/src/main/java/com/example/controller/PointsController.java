package com.example.controller;

import com.example.common.Result;
import com.example.entity.User;
import com.example.service.PointsService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/points")
public class PointsController {

    @Resource
    private PointsService pointsService;

    /**
     * 获取用户积分详情
     */
    @GetMapping("/getUserPointsDetail/{userId}")
    public Result getUserPointsDetail(@PathVariable Integer userId) {
        User user = pointsService.getUserPointsDetail(userId);
        return Result.success(user);
    }
}
