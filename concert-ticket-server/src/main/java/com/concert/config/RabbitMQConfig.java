package com.concert.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ配置（订单队列/交换机/绑定 + JSON消息转换器）
 * 新增：JSON消息转换器，解决反序列化安全限制问题
 */
@Configuration
public class RabbitMQConfig {
    // 从配置文件读取参数
    @Value("${concert.rabbitmq.queue.order}")
    private String orderQueue;

    @Value("${concert.rabbitmq.exchange.order}")
    private String orderExchange;

    @Value("${concert.rabbitmq.routing-key.order}")
    private String orderRoutingKey;

    /**
     * 创建订单队列（持久化）
     */
    @Bean
    public Queue orderQueue() {
        return new Queue(orderQueue, true, false, false);
    }

    /**
     * 创建订单交换机（直连型，持久化）
     */
    @Bean
    public DirectExchange orderExchange() {
        return new DirectExchange(orderExchange, true, false);
    }

    /**
     * 绑定队列和交换机
     */
    @Bean
    public Binding orderBinding() {
        return BindingBuilder.bind(orderQueue()).to(orderExchange()).with(orderRoutingKey);
    }

    /**
     * 核心新增：配置JSON消息转换器（解决反序列化安全限制）
     * 替代默认的SimpleMessageConverter，强制所有MQ消息以JSON格式传输/解析
     */
    @Bean
    public MessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}