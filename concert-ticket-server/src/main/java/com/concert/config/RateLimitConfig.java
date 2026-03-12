package com.concert.config;

import com.google.common.util.concurrent.RateLimiter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 令牌桶限流配置（Guava）
 */
@Configuration
public class RateLimitConfig {
    // 从配置文件读取限流速率
    @Value("${concert.limit.rate}")
    private double limitRate;

    /**
     * 创建全局限流器（10个/秒）
     */
    @Bean
    public RateLimiter rateLimiter() {
        // RateLimiter.create(10)：每秒生成10个令牌
        return RateLimiter.create(limitRate);
    }
}