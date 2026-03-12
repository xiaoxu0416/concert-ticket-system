package com.concert.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.concert.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import com.rabbitmq.client.Channel;

import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * RabbitMQ消费者（异步创建订单）
 * 高并发优化点：
 * 1. JSON字符串解析（解决反序列化安全限制）+ 非法字符过滤 + 大数字Long类型解析（核心修复）
 * 2. 消息重试机制（最多3次，避免死循环）
 * 3. 参数严格校验（防止脏数据）
 * 4. 手动ACK确认（避免重复消费）
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RabbitMQConsumerService {
    private final OrderService orderService;

    /**
     * 监听订单队列，处理异步创建订单任务（高并发削峰核心）
     */
    @RabbitListener(queues = "${concert.rabbitmq.queue.order}")
    public void handleOrderMsg(Message message, Channel channel) {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        log.info("\n===== 开始消费MQ订单消息 =====");
        log.info("消息DeliveryTag：{}", deliveryTag);

        try {
            // 1. 解析MQ消息（指定UTF-8编码，避免乱码）
            String msgStr = new String(message.getBody(), StandardCharsets.UTF_8);
            log.info("原始JSON消息内容：{}", msgStr);

            // 空消息校验
            if (msgStr == null || msgStr.isEmpty()) {
                throw new IllegalArgumentException("MQ消息内容为空，拒绝消费");
            }

            // ========== 核心修复1：过滤不可见控制字符 ==========
            // 过滤 \x00-\x1F（控制字符）和 \x7F（删除符），保留正常JSON字符
            String cleanMsgStr = msgStr.replaceAll("[\\x00-\\x1F\\x7F]", "");
            log.info("过滤非法字符后的消息：{}", cleanMsgStr);

            // ========== 核心修复2：使用TypeReference强制解析为Long类型 ==========
            // 解决大数字userId解析溢出问题（fastjson2默认解析为Integer导致溢出）
            Map<String, Object> msgMap = JSON.parseObject(
                    cleanMsgStr,
                    new TypeReference<Map<String, Object>>() {}
            );

            if (msgMap == null || msgMap.isEmpty()) {
                throw new IllegalArgumentException("MQ JSON消息解析后为空，拒绝消费");
            }

            // 3. 严格校验参数（高并发下防止脏数据导致订单创建失败）
            Long userId = getLongParam(msgMap, "userId");
            Long sessionId = getLongParam(msgMap, "sessionId");
            Integer ticketNum = getIntegerParam(msgMap, "ticketNum");
            String orderNo = getStringParam(msgMap, "orderNo");

            log.info("MQ消息解析参数成功：userId={}, sessionId={}, ticketNum={}, orderNo={}",
                    userId, sessionId, ticketNum, orderNo);

            // 4. 调用异步创建订单（高并发核心：MQ削峰后匀速消费）
            orderService.createOrderAsync(userId, sessionId, ticketNum, orderNo);

            // 5. 手动确认消息（ACK）：消费成功，从队列移除（避免重复消费）
            channel.basicAck(deliveryTag, false);
            log.info("===== MQ订单消息消费成功，已确认（DeliveryTag={}）=====", deliveryTag);

        } catch (IllegalArgumentException e) {
            // 参数异常：直接丢弃消息（参数错误重试也会失败，避免无效重试）
            log.error("MQ消息消费失败（参数异常）：{}", e.getMessage());
            try {
                // 拒绝消息并丢弃（basicNack: requeue=false）
                channel.basicNack(deliveryTag, false, false);
                log.warn("参数异常消息已丢弃，DeliveryTag={}", deliveryTag);
            } catch (Exception ex) {
                log.error("参数异常消息Nack失败：", ex);
            }
        } catch (Exception e) {
            // 业务异常：重试机制（最多3次，高并发下避免消息丢失，同时防止死循环）
            log.error("MQ消息消费失败（业务异常）：", e);
            try {
                // 获取重试次数（自定义Header，首次为0）
                Integer retryCount = message.getMessageProperties().getHeader("x-retry-count");
                retryCount = (retryCount == null) ? 0 : retryCount + 1;
                message.getMessageProperties().setHeader("x-retry-count", retryCount);

                if (retryCount <= 3) {
                    // 重试次数≤3：重新入队（requeue=true），高并发下保证消息不丢失
                    channel.basicNack(deliveryTag, false, true);
                    log.warn("MQ消息消费失败，将重新入队重试（第{}次），DeliveryTag={}", retryCount, deliveryTag);
                } else {
                    // 重试次数>3：丢弃消息+记录日志（避免死循环，高并发下保护消费者）
                    channel.basicNack(deliveryTag, false, false);
                    log.error("MQ消息重试{}次仍失败，已丢弃，DeliveryTag={}", retryCount, deliveryTag);
                }
            } catch (Exception ex) {
                log.error("业务异常消息Nack失败：", ex);
            }
        }
    }

    /**
     * 提取Long类型参数（非空+格式校验，兼容Long/Integer/String类型）
     */
    private Long getLongParam(Map<String, Object> msgMap, String key) {
        Object value = msgMap.get(key);
        if (value == null) {
            throw new IllegalArgumentException("参数[" + key + "]不能为空");
        }
        try {
            // 兼容多种类型：Long/Integer/String
            if (value instanceof Long) {
                return (Long) value;
            } else if (value instanceof Integer) {
                return ((Integer) value).longValue();
            } else {
                return Long.valueOf(value.toString().trim());
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("参数[" + key + "]格式错误（需为整数）：" + value);
        }
    }

    /**
     * 提取Integer类型参数（非空+格式校验）
     */
    private Integer getIntegerParam(Map<String, Object> msgMap, String key) {
        Object value = msgMap.get(key);
        if (value == null) {
            throw new IllegalArgumentException("参数[" + key + "]不能为空");
        }
        try {
            return Integer.valueOf(value.toString().trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("参数[" + key + "]格式错误（需为整数）：" + value);
        }
    }

    /**
     * 提取String类型参数（非空校验）
     */
    private String getStringParam(Map<String, Object> msgMap, String key) {
        Object value = msgMap.get(key);
        if (value == null || value.toString().trim().isEmpty()) {
            throw new IllegalArgumentException("参数[" + key + "]不能为空");
        }
        return value.toString().trim();
    }
}