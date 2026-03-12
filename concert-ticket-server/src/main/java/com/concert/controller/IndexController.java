package com.concert.controller;

import com.concert.utils.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 测试控制器
 */
@RestController
@RequestMapping("/")
public class IndexController {
    @GetMapping
    public Result<?> index() {
        return Result.success("演唱会门票销售系统后端服务启动成功！");
    }
}