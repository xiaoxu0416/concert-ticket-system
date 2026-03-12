package com.concert.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.alibaba.fastjson.JSON;
import com.concert.entity.ConcertSession;
import com.concert.entity.Order;
import com.concert.mapper.ConcertSessionMapper;
import com.concert.mapper.OrderMapper;
import com.concert.service.ConcertSessionService;
import com.concert.service.OrderService;
import com.concert.utils.RedisKeyUtil;
import com.concert.vo.TicketBuyVO;
import com.concert.vo.OrderPayVO;
import com.concert.utils.Result;
import com.google.common.util.concurrent.RateLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 订单服务实现类（抢票核心+MQ发送）
 * 最终修复版：
 * 1. 修复订单编号生成逻辑（纯自增ID+日期，杜绝用户ID混入）
 * 2. 强化支付流程校验，必须选择支付方式且订单为待支付状态才能支付
 * 3. 优化异步订单创建逻辑，增加日志和重试提示
 * 4. 修复状态不一致问题，明确各阶段的订单状态提示
 */
@Service
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings("BetaApi") // 抑制Guava RateLimiter的Beta警告
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {
    // 依赖注入
    private final OrderMapper orderMapper;
    private final ConcertSessionMapper sessionMapper;
    private final ConcertSessionService sessionService;
    private final RabbitTemplate rabbitTemplate;
    private final RateLimiter rateLimiter;
    private final RedisTemplate<String, String> redisTemplate;

    // RabbitMQ配置
    @Value("${concert.rabbitmq.exchange.order}")
    private String orderExchange;

    @Value("${concert.rabbitmq.routing-key.order}")
    private String orderRoutingKey;

    // 订单状态常量（增加注释，便于维护）
    private static final Integer ORDER_STATUS_CREATING = 3;    // 创建中（MQ处理中）
    private static final Integer ORDER_STATUS_WAIT_PAY = 0;    // 待支付（MQ处理完成）
    private static final Integer ORDER_STATUS_PAID = 1;        // 已支付
    private static final Integer ORDER_STATUS_CANCELLED = 2;   // 已取消

    // 订单编号格式化器（精确到分钟，进一步区分订单）
    private static final DateTimeFormatter ORDER_NO_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmm");

    /**
     * 抢票核心方法（修复核心：订单编号+状态提示）
     */
    @Override
    public Result<?> buyTicket(TicketBuyVO vo, Long userId) {
        // 0. 入参全量校验
        if (vo == null || userId == null) {
            log.error("抢票失败：请求参数为空，用户ID：{}", userId);
            return Result.error("请求参数不能为空");
        }

        // 令牌桶限流（每秒1次，防刷）
        if (!rateLimiter.tryAcquire()) {
            log.warn("用户{}抢票触发限流：请求过频", userId);
            return Result.build(429, "请求过于频繁，请稍后再试");
        }

        Long sessionId = vo.getSessionId();
        Integer ticketNum = vo.getTicketNum();
        log.info("处理抢票请求 - 用户{}，场次{}，数量{}", userId, sessionId, ticketNum);

        // 1. 参数校验
        if (sessionId == null || ticketNum == null || ticketNum <= 0) {
            log.error("抢票失败：参数非法，场次{}，数量{}", sessionId, ticketNum);
            return Result.error("购票数量必须大于0");
        }

        // 2. 校验场次存在
        ConcertSession session = sessionMapper.selectById(sessionId);
        if (session == null) {
            log.error("抢票失败：场次{}不存在", sessionId);
            return Result.error("场次不存在");
        }

        // 3. 校验场次状态（仅售票中）
        if (session.getStatus() == null || session.getStatus() != 1) {
            log.error("抢票失败：场次{}状态异常，当前状态{}", sessionId, session.getStatus());
            return Result.error("该场次未开票或已售罄");
        }

        // 4. 校验单价合法
        if (session.getPrice() == null || session.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            log.error("抢票失败：场次{}单价异常，单价{}", sessionId, session.getPrice());
            return Result.error("场次单价异常，无法购票");
        }

        // 5. Redis库存扣减（原子操作，防超卖）
        boolean deductSuccess = sessionService.deductRedisStock(sessionId, ticketNum);
        if (!deductSuccess) {
            log.error("抢票失败：场次{}库存不足，扣减数量{}", sessionId, ticketNum);
            return Result.error("抢票失败，库存不足");
        }

        // 6. 计算金额
        BigDecimal totalAmount = session.getPrice().multiply(new BigDecimal(ticketNum));

        // 7. 插入临时订单（仅存基础信息，orderNo后续生成）
        Order tempOrder = new Order();
        tempOrder.setUserId(userId);
        tempOrder.setSessionId(sessionId);
        tempOrder.setConcertId(session.getConcertId());
        tempOrder.setTicketNum(ticketNum);
        tempOrder.setTotalAmount(totalAmount);
        tempOrder.setStatus(ORDER_STATUS_CREATING); // 标记为创建中
        tempOrder.setCreateTime(LocalDateTime.now());
        tempOrder.setUpdateTime(LocalDateTime.now());
        orderMapper.insert(tempOrder);

        // 8. 生成规范订单编号（核心修复：日期+自增ID，无用户ID）
        Long autoIncrementId = tempOrder.getId();
        String orderNo = "ORDER_" + LocalDateTime.now().format(ORDER_NO_FORMATTER) + "_" + String.format("%06d", autoIncrementId);
        log.info("抢票成功：生成规范订单号{}（自增ID：{}）", orderNo, autoIncrementId);

        // 9. 更新订单编号
        tempOrder.setOrderNo(orderNo);
        orderMapper.updateById(tempOrder);

        // 10. 组装MQ消息
        Map<String, Object> msgMap = new HashMap<>();
        msgMap.put("userId", userId);
        msgMap.put("sessionId", sessionId);
        msgMap.put("ticketNum", ticketNum);
        msgMap.put("orderNo", orderNo);
        msgMap.put("totalAmount", totalAmount);

        // 11. 发送MQ（失败降级为同步创建）
        try {
            rabbitTemplate.convertAndSend(orderExchange, orderRoutingKey, msgMap);
            log.info("MQ发送成功：订单{}入队等待处理", orderNo);
        } catch (Exception e) {
            // MQ不可用，降级为同步创建订单
            log.warn("MQ不可用，订单{}降级为同步创建", orderNo, e);
            try {
                createOrderAsync(userId, sessionId, ticketNum, orderNo);
                log.info("同步创建订单成功：{}", orderNo);
            } catch (Exception syncEx) {
                sessionService.rollbackRedisStock(sessionId, ticketNum);
                orderMapper.deleteById(autoIncrementId);
                log.error("同步创建订单也失败：订单{}回滚", orderNo, syncEx);
                return Result.error("抢票失败，系统异常");
            }
        }

        return Result.build(200, "抢票成功！请前往订单列表查看并支付", orderNo);
    }

    /**
     * 异步创建订单（修复核心：强化异常处理+日志）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createOrderAsync(Long userId, Long sessionId, Integer ticketNum, String orderNo) {
        log.info("开始异步创建订单：{}，用户{}，场次{}", orderNo, userId, sessionId);

        try {
            // 1. 严格参数校验
            if (userId == null || sessionId == null || ticketNum == null || ticketNum <= 0 || orderNo == null) {
                log.error("订单{}创建失败：参数非法", orderNo);
                updateOrderStatus(orderNo, ORDER_STATUS_CANCELLED);
                return;
            }

            // 2. 二次校验场次
            ConcertSession session = sessionMapper.selectById(sessionId);
            if (session == null || session.getStatus() != 1) {
                log.error("订单{}创建失败：场次不存在或已停售", orderNo);
                sessionService.rollbackRedisStock(sessionId, ticketNum);
                updateOrderStatus(orderNo, ORDER_STATUS_CANCELLED);
                return;
            }

            // 3. 扣减数据库库存（核心：增加日志）
            boolean dbDeductSuccess = sessionService.deductDbStock(sessionId, ticketNum);
            if (!dbDeductSuccess) {
                log.error("订单{}创建失败：数据库库存不足（Redis预扣成功但DB扣减失败）", orderNo);
                sessionService.rollbackRedisStock(sessionId, ticketNum);
                updateOrderStatus(orderNo, ORDER_STATUS_CANCELLED);
                return;
            }

            // 4. 更新为待支付（核心：仅更新状态，不重复计算金额）
            LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Order::getOrderNo, orderNo)
                    .eq(Order::getStatus, ORDER_STATUS_CREATING); // 仅更新创建中的订单

            Order order = new Order();
            order.setStatus(ORDER_STATUS_WAIT_PAY);
            order.setUpdateTime(LocalDateTime.now());

            int updateResult = orderMapper.update(order, wrapper);
            if (updateResult > 0) {
                log.info("订单{}创建成功：状态更新为待支付", orderNo);
                redisTemplate.delete(RedisKeyUtil.getOrderCacheKey(orderNo));
            } else {
                log.error("订单{}创建失败：未找到创建中的订单记录", orderNo);
                sessionService.rollbackRedisStock(sessionId, ticketNum);
                sessionService.rollbackDbStock(sessionId, ticketNum);
                throw new RuntimeException("订单状态更新失败：未找到创建中的订单");
            }

        } catch (Exception e) {
            log.error("订单{}创建异常，触发回滚", orderNo, e);
            sessionService.rollbackRedisStock(sessionId, ticketNum);
            sessionService.rollbackDbStock(sessionId, ticketNum);
            updateOrderStatus(orderNo, ORDER_STATUS_CANCELLED);
        }
    }

    /**
     * 预支付接口（修复核心：强化支付方式+状态校验）
     */
    @Override
    public Result<?> prePay(OrderPayVO vo, Long userId) {
        // 0. 入参校验
        if (vo == null || userId == null) {
            log.error("预支付失败：请求参数为空，用户ID：{}", userId);
            return Result.paramError("请求参数不能为空");
        }

        String orderNo = vo.getOrderNo();
        String payType = vo.getPayType();

        // 1. 严格参数校验（支付方式必须是微信/支付宝）
        if (orderNo == null || payType == null) {
            log.error("预支付失败：订单号或支付方式为空，订单{}，支付方式{}", orderNo, payType);
            return Result.paramError("订单号和支付方式不能为空");
        }
        if (!"WECHAT".equals(payType) && !"ALIPAY".equals(payType)) {
            log.error("预支付失败：支付方式非法，订单{}，支付方式{}", orderNo, payType);
            return Result.paramError("仅支持微信(WECHAT)和支付宝(ALIPAY)支付");
        }

        // 2. 获取幂等性缓存key
        String repeatKey = RedisKeyUtil.getPayRepeatKey(orderNo, userId);

        // 3. 校验订单（核心：必须是待支付状态）
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getOrderNo, orderNo)
                .eq(Order::getUserId, userId)
                .eq(Order::getStatus, ORDER_STATUS_WAIT_PAY);
        Order order = orderMapper.selectOne(wrapper);
        if (order == null) {
            log.error("预支付失败：订单{}不存在/非待支付/非当前用户所有", orderNo, userId);
            return Result.notFoundError("订单不存在或非待支付状态，请刷新订单列表后重试");
        }

        // 4. 生成支付二维码（包含支付方式）
        String qrContent = String.format("orderNo=%s&amount=%s&payType=%s&userId=%s",
                orderNo, order.getTotalAmount(), payType, userId);
        String qrCodeUrl = "https://api.qrserver.com/v1/create-qr-code/?size=200x200&data=" + qrContent;

        // 5. 设置幂等性缓存（10分钟）
        redisTemplate.opsForValue().set(repeatKey, payType, 600, TimeUnit.SECONDS);

        log.info("预支付成功：用户{}，订单{}，支付方式{}", userId, orderNo, payType);
        return Result.success(Map.of(
                "qrCodeUrl", qrCodeUrl,
                "orderNo", orderNo,
                "totalAmount", order.getTotalAmount(),
                "payType", payType
        ));
    }

    /**
     * 支付订单（修复核心：杜绝无支付方式支付+状态严格校验）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<?> payOrder(OrderPayVO vo, Long userId) {
        // 0. 入参校验
        if (vo == null || userId == null) {
            log.error("支付失败：请求参数为空，用户ID：{}", userId);
            return Result.paramError("请求参数不能为空");
        }

        String orderNo = vo.getOrderNo();
        String payType = vo.getPayType();

        // 1. 核心参数校验（必须传支付方式）
        if (orderNo == null) {
            log.error("支付失败：订单号为空，用户{}", userId);
            return Result.paramError("订单号不能为空");
        }
        if (payType == null || (!"WECHAT".equals(payType) && !"ALIPAY".equals(payType))) {
            log.error("支付失败：支付方式非法，订单{}，支付方式{}", orderNo, payType);
            return Result.paramError("必须选择微信或支付宝支付");
        }

        // 2. 分布式锁（防并发支付）
        String lockKey = RedisKeyUtil.getOrderLockKey(orderNo);
        Boolean locked = redisTemplate.opsForValue().setIfAbsent(lockKey, "1", 30, TimeUnit.SECONDS);
        if (!Boolean.TRUE.equals(locked)) {
            log.warn("用户{}支付请求处理中，订单{}", userId, orderNo);
            return Result.error("支付请求处理中，请稍候");
        }

        try {
            // 3. 校验订单归属
            LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Order::getOrderNo, orderNo)
                    .eq(Order::getUserId, userId);
            Order order = orderMapper.selectOne(wrapper);
            if (order == null) {
                log.error("支付失败：订单{}不存在或不属于用户{}", orderNo, userId);
                return Result.notFoundError("订单不存在或不属于当前用户");
            }

            // 4. 严格状态校验（核心修复）
            if (order.getStatus().equals(ORDER_STATUS_PAID)) {
                log.warn("用户{}重复支付，订单{}已支付", userId, orderNo);
                return Result.error("该订单已支付，无需重复支付");
            }
            if (order.getStatus().equals(ORDER_STATUS_CANCELLED)) {
                log.error("支付失败：订单{}已取消，用户{}", orderNo, userId);
                return Result.error("该订单已取消，无法支付");
            }
            if (order.getStatus().equals(ORDER_STATUS_CREATING)) {
                log.error("支付失败：订单{}仍在创建中，用户{}", orderNo, userId);
                return Result.error("订单正在创建中，请等待10秒后重试");
            }
            if (!order.getStatus().equals(ORDER_STATUS_WAIT_PAY)) {
                log.error("支付失败：订单{}状态异常，当前状态{}", orderNo, order.getStatus());
                return Result.error("该订单非待支付状态，无法支付");
            }

            // 5. 校验预支付记录（必须先发起预支付）
            String repeatKey = RedisKeyUtil.getPayRepeatKey(orderNo, userId);
            String cachePayType = redisTemplate.opsForValue().get(repeatKey);
            if (cachePayType == null || !cachePayType.equals(payType)) {
                log.error("支付失败：未发起预支付或支付方式不一致，订单{}，请求方式{}，缓存方式{}",
                        orderNo, payType, cachePayType);
                return Result.error("请先生成支付二维码后再支付");
            }

            // 6. 更新订单状态为已支付，生成核销码
            order.setStatus(ORDER_STATUS_PAID);
            order.setPayTime(LocalDateTime.now());
            order.setUpdateTime(LocalDateTime.now());
            order.setVerifyCode(generateVerifyCode());
            int row = orderMapper.updateById(order);

            // 7. 清理缓存
            redisTemplate.delete(RedisKeyUtil.getOrderCacheKey(orderNo));
            redisTemplate.delete(repeatKey);

            if (row > 0) {
                log.info("支付成功：订单{}，用户{}，支付方式{}", orderNo, userId, payType);
                return Result.success("支付成功！");
            } else {
                log.error("支付失败：订单{}更新失败", orderNo);
                return Result.error("支付失败，请重试");
            }
        } finally {
            // 释放锁
            redisTemplate.delete(lockKey);
        }
    }

    /**
     * 废弃旧支付接口（杜绝无userId支付）
     */
    @Override
    @Deprecated
    public Result<?> payOrder(OrderPayVO vo) {
        log.error("禁止调用旧支付接口（无userId），订单{}", vo.getOrderNo());
        return Result.error("支付接口已升级，请重新发起支付请求");
    }

    /**
     * 取消订单（保留原有逻辑，增加日志）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<?> cancelOrder(String orderNo, Long userId, String reason) {
        if (orderNo == null || userId == null) {
            log.error("取消订单失败：参数为空，订单{}，用户{}", orderNo, userId);
            return Result.paramError("请求参数不能为空");
        }

        String lockKey = RedisKeyUtil.getOrderLockKey(orderNo);
        Boolean locked = redisTemplate.opsForValue().setIfAbsent(lockKey, "1", 30, TimeUnit.SECONDS);
        if (!Boolean.TRUE.equals(locked)) {
            log.warn("用户{}取消订单请求处理中，订单{}", userId, orderNo);
            return Result.error("取消请求处理中，请稍候");
        }

        try {
            LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Order::getOrderNo, orderNo)
                    .eq(Order::getUserId, userId)
                    .eq(Order::getStatus, ORDER_STATUS_WAIT_PAY);
            Order order = orderMapper.selectOne(wrapper);
            if (order == null) {
                log.error("取消订单失败：订单{}不存在或非待支付状态", orderNo);
                return Result.error("订单不存在或非待支付状态，无法取消");
            }

            // 回滚库存
            Long sessionId = order.getSessionId();
            Integer ticketNum = order.getTicketNum();
            sessionService.rollbackRedisStock(sessionId, ticketNum);
            sessionService.rollbackDbStock(sessionId, ticketNum);

            // 更新状态
            order.setStatus(ORDER_STATUS_CANCELLED);
            order.setUpdateTime(LocalDateTime.now());
            orderMapper.updateById(order);

            // 清理缓存
            redisTemplate.delete(RedisKeyUtil.getOrderCacheKey(orderNo));
            redisTemplate.delete(RedisKeyUtil.getPayRepeatKey(orderNo, userId));

            log.info("取消订单成功：{}，用户{}，原因{}", orderNo, userId, reason);
            return Result.success("取消订单成功，库存已释放");
        } finally {
            redisTemplate.delete(lockKey);
        }
    }

    /**
     * 获取订单详情（修复：返回状态描述）
     */
    @Override
    public Result<?> getOrderInfo(String orderNo, Long userId) {
        if (orderNo == null) {
            log.error("查询订单详情失败：订单号为空");
            return Result.paramError("订单号不能为空");
        }

        // 1. 查缓存
        String cacheKey = RedisKeyUtil.getOrderCacheKey(orderNo);
        String cacheValue = redisTemplate.opsForValue().get(cacheKey);
        if (cacheValue != null) {
            Order order = JSON.parseObject(cacheValue, Order.class);
            if (userId != null && !userId.equals(order.getUserId())) {
                log.warn("用户{}无权限查看订单{}", userId, orderNo);
                return Result.build(403, "无权限查看该订单");
            }
            // 补充状态描述
            Map<String, Object> result = JSON.parseObject(cacheValue, HashMap.class);
            result.put("statusDesc", getStatusDesc(order.getStatus()));
            return Result.success(result);
        }

        // 2. 查数据库
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getOrderNo, orderNo);
        Order order = orderMapper.selectOne(wrapper);
        if (order == null) {
            log.error("查询订单详情失败：订单{}不存在", orderNo);
            return Result.notFoundError("订单不存在");
        }

        // 3. 权限校验
        if (userId != null && !userId.equals(order.getUserId())) {
            log.warn("用户{}无权限查看订单{}", userId, orderNo);
            return Result.build(403, "无权限查看该订单");
        }

        // 4. 补充状态描述并缓存
        Map<String, Object> result = JSON.parseObject(JSON.toJSONString(order), HashMap.class);
        result.put("statusDesc", getStatusDesc(order.getStatus()));
        redisTemplate.opsForValue().set(cacheKey, JSON.toJSONString(result), 300, TimeUnit.SECONDS);

        return Result.success(result);
    }

    /**
     * 查询用户订单列表（修复：使用关联查询获取演唱会名称和场次时间）
     */
    @Override
    public Result<?> listUserOrder(Long userId) {
        if (userId == null) {
            log.error("查询订单失败：用户ID为空");
            return Result.paramError("用户ID不能为空");
        }

        log.info("查询用户{}订单列表", userId);

        // 使用关联查询获取演唱会名称和场次时间
        return Result.success(orderMapper.selectOrderListByUserId(userId));
    }

    /**
     * 查询所有订单（管理员）
     */
    @Override
    public Result<?> listAllOrder() {
        log.info("查询所有订单");
        return Result.success(orderMapper.selectAllOrderList());
    }

    /**
     * 辅助方法：更新订单状态
     */
    private void updateOrderStatus(String orderNo, Integer status) {
        if (orderNo == null || status == null) {
            log.warn("更新订单状态失败：参数为空");
            return;
        }

        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getOrderNo, orderNo);

        Order order = new Order();
        order.setStatus(status);
        order.setUpdateTime(LocalDateTime.now());

        int updateCount = orderMapper.update(order, wrapper);
        if (updateCount > 0) {
            log.info("订单{}状态更新为{}({})", orderNo, status, getStatusDesc(status));
            redisTemplate.delete(RedisKeyUtil.getOrderCacheKey(orderNo));
        } else {
            log.error("订单{}状态更新失败：未找到订单", orderNo);
        }
    }

    /**
     * 辅助方法：获取订单状态描述（兼容 Java 11）
     */
    private String getStatusDesc(Integer status) {
        if (status == null) {
            return "未知状态";
        }
        switch (status) {
            case 0:
                return "待支付";
            case 1:
                return "已支付";
            case 2:
                return "已取消";
            case 3:
                return "创建中";
            case 4:
                return "已退款";
            case 5:
                return "退款审核中";
            case 6:
                return "已核销";
            default:
                return "未知状态";
        }
    }

    /**
     * 生成6位核销码（大写字母+数字）
     */
    private String generateVerifyCode() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        StringBuilder sb = new StringBuilder();
        java.util.Random random = new java.util.Random();
        for (int i = 0; i < 8; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    /**
     * 管理员核销门票
     */
    public Result<?> verifyTicket(String verifyCode) {
        if (verifyCode == null || verifyCode.trim().isEmpty()) {
            return Result.error("核销码不能为空");
        }
        verifyCode = verifyCode.trim().toUpperCase();

        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getVerifyCode, verifyCode);
        Order order = orderMapper.selectOne(wrapper);

        if (order == null) {
            log.warn("核销失败：核销码{}不存在", verifyCode);
            return Result.error("核销码无效，未找到对应订单");
        }

        if (order.getStatus() == 6) {
            log.warn("核销失败：订单{}已核销", order.getOrderNo());
            return Result.error("该门票已核销，请勿重复核销");
        }

        if (order.getStatus() != 1) {
            log.warn("核销失败：订单{}状态异常，当前状态{}", order.getOrderNo(), order.getStatus());
            return Result.error("该订单状态异常（" + getStatusDesc(order.getStatus()) + "），无法核销");
        }

        order.setStatus(6);
        order.setVerifyTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        orderMapper.updateById(order);

        redisTemplate.delete(RedisKeyUtil.getOrderCacheKey(order.getOrderNo()));

        log.info("核销成功：订单{}，核销码{}", order.getOrderNo(), verifyCode);

        Map<String, Object> result = new HashMap<>();
        result.put("orderNo", order.getOrderNo());
        result.put("ticketNum", order.getTicketNum());
        result.put("totalAmount", order.getTotalAmount());
        result.put("verifyCode", order.getVerifyCode());
        result.put("verifyTime", order.getVerifyTime());
        return Result.success(result);
    }
}
