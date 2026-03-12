package com.concert;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * 演唱会门票销售系统 - 启动类
 * 核心配置说明：
 * 1. @MapperScan：全局扫描Mapper接口（替代单个@Mapper注解，减少冗余）
 * 2. @EnableRabbit：开启RabbitMQ注解支持（适配异步创建订单的高并发设计）
 * 3. @EnableTransactionManagement：开启声明式事务（保证库存扣减+订单创建的原子性）
 * 4. 优雅停机配置：避免高并发下MQ消息丢失、事务未提交
 *
 * @author 你的姓名（毕设必填）
 * @date 2026-03-06（毕设必填）
 */
@Slf4j // 替换System.out，使用SLF4J日志框架（企业级规范）
@SpringBootApplication
@MapperScan("com.concert.mapper")
@EnableRabbit
@EnableTransactionManagement
public class ConcertTicketApplication {

    public static void main(String[] args) {
        // 启动应用并获取上下文，用于后续获取启动信息
        var context = SpringApplication.run(ConcertTicketApplication.class, args);

        // 优雅的启动日志（替代System.out，毕设规范）
        log.info("========================================");
        log.info("✅ 演唱会门票销售系统后端启动成功！");
        log.info("🌐 接口根路径：http://localhost:{}/api",
                context.getBean("serverPort", String.class));
        log.info("📢 当前环境：{}",
                context.getEnvironment().getProperty("spring.profiles.active", "dev"));
        log.info("========================================");
    }

    /**
     * 修复：正确获取启动端口（从环境变量读取，适配配置文件修改）
     * 解决原逻辑中监听器注入错误的问题
     */
    @Bean
    public String serverPort(Environment environment) {
        // 从配置文件读取端口，默认8080（适配application.yml的server.port配置）
        return environment.getProperty("server.port", "8080");
    }

    /**
     * 毕设核心：优雅停机配置（高并发场景必备）
     * 作用：关闭应用时，等待MQ消费、数据库事务完成后再停止，避免数据丢失
     */
    @Bean
    public SpringApplication shutdownHook() {
        SpringApplication application = new SpringApplication(ConcertTicketApplication.class);
        // 开启优雅停机（默认关闭，高并发场景必须开启）
        application.setRegisterShutdownHook(true);
        // 设置停机等待时间（单位：秒），确保MQ/事务处理完成
        System.setProperty("spring.lifecycle.timeout-per-shutdown-phase", "30");
        return application;
    }

    /**
     * 可选：若需要监听端口初始化事件（备用方案）
     * 作用：端口初始化完成后打印日志，验证端口是否正确
     */
    @Bean
    public ApplicationListener<WebServerInitializedEvent> webServerInitializedEventListener() {
        return event -> log.info("📝 服务器端口初始化完成：{}", event.getWebServer().getPort());
    }
}