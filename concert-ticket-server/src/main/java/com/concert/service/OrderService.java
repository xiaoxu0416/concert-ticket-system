package com.concert.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.concert.entity.Order;
import com.concert.vo.TicketBuyVO;
import com.concert.vo.OrderPayVO;
import com.concert.utils.Result;

/**
 * 订单服务接口
 * 修复版：兼容新旧payOrder接口，消除编译错误
 */
public interface OrderService extends IService<Order> {
    /**
     * 抢票（核心：限流→Redis扣库存→发送MQ）
     * @param vo 购票参数
     * @param userId 用户ID
     * @return 抢票结果
     */
    Result<?> buyTicket(TicketBuyVO vo, Long userId);

    /**
     * 异步创建订单（MQ消费者调用）
     * @param userId 用户ID
     * @param sessionId 场次ID
     * @param ticketNum 购票数量
     * @param orderNo 订单号
     */
    void createOrderAsync(Long userId, Long sessionId, Integer ticketNum, String orderNo);

    /**
     * 预支付（生成二维码+幂等性校验）
     * @param vo 支付参数
     * @param userId 用户ID
     * @return 二维码信息
     */
    Result<?> prePay(OrderPayVO vo, Long userId);

    /**
     * 支付订单（新接口：带userId，防越权+分布式锁）
     * @param vo 支付参数
     * @param userId 用户ID
     * @return 支付结果
     */
    Result<?> payOrder(OrderPayVO vo, Long userId);

    /**
     * 支付订单（旧接口：兼容用，标记为过时）
     * @param vo 支付参数
     * @return 支付结果
     */
    @Deprecated
    Result<?> payOrder(OrderPayVO vo);

    /**
     * 取消订单（库存回滚+分布式锁）
     * @param orderNo 订单号
     * @param userId 用户ID
     * @param reason 取消原因
     * @return 取消结果
     */
    Result<?> cancelOrder(String orderNo, Long userId, String reason);

    /**
     * 获取订单详情（缓存+权限校验）
     * @param orderNo 订单号
     * @param userId 用户ID
     * @return 订单详情
     */
    Result<?> getOrderInfo(String orderNo, Long userId);

    /**
     * 查询用户订单列表
     * @param userId 用户ID
     * @return 订单列表
     */
    Result<?> listUserOrder(Long userId);

    /**
     * 查询所有订单（仅管理员）
     * @return 所有订单
     */
    Result<?> listAllOrder();

    /**
     * 管理员核销门票
     * @param verifyCode 核销码
     * @return 核销结果
     */
    Result<?> verifyTicket(String verifyCode);
}
