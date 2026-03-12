package com.concert.utils;

/**
 * Redis Key 工具类（高并发优化版）
 * 核心设计：
 * 1. 统一Key前缀+命名规范，避免Key冲突
 * 2. 按业务模块分类，便于维护和排查
 * 3. 预留过期时间常量，适配高并发缓存策略
 * 4. 与订单服务、库存服务完全兼容
 */
public class RedisKeyUtil {
    // ===================== 基础配置 =====================
    // 全局Key分隔符（避免Key名称混乱）
    private static final String SEPARATOR = ":";
    // 业务前缀（区分不同项目/模块）
    private static final String BUSINESS_PREFIX = "concert";

    // ===================== 库存相关 Key =====================
    /**
     * 场次库存Key（Redis原子操作扣减，防超卖）
     * 格式：concert:stock:session:{sessionId}
     */
    public static String getSessionStockKey(Long sessionId) {
        return buildKey("stock", "session", sessionId.toString());
    }

    /**
     * 库存扣减分布式锁Key（防并发扣减）
     * 格式：concert:lock:stock:session:{sessionId}
     */
    public static String getStockLockKey(Long sessionId) {
        return buildKey("lock", "stock", "session", sessionId.toString());
    }

    // ===================== 订单相关 Key =====================
    /**
     * 订单分布式锁Key（防重复支付/取消）
     * 格式：concert:lock:order:{orderNo}
     */
    public static String getOrderLockKey(String orderNo) {
        return buildKey("lock", "order", orderNo);
    }

    /**
     * 订单详情缓存Key（减轻数据库压力，5秒过期）
     * 格式：concert:cache:order:{orderNo}
     */
    public static String getOrderCacheKey(String orderNo) {
        return buildKey("cache", "order", orderNo);
    }

    /**
     * 支付幂等性Key（防重复支付，5分钟过期）
     * 格式：concert:repeat:pay:{orderNo}:{userId}
     */
    public static String getPayRepeatKey(String orderNo, Long userId) {
        return buildKey("repeat", "pay", orderNo, userId.toString());
    }

    // ===================== 限流相关 Key =====================
    /**
     * 抢票接口限流Key（令牌桶限流，防刷）
     * 格式：concert:limit:buy_ticket:{userId}
     */
    public static String getBuyTicketLimitKey(Long userId) {
        return buildKey("limit", "buy_ticket", userId.toString());
    }

    // ===================== 过期时间常量（秒） =====================
    // 订单缓存过期时间（5秒，兼顾实时性和性能）
    public static final long ORDER_CACHE_EXPIRE_SECONDS = 5;
    // 支付幂等性过期时间（5分钟，避免重复支付）
    public static final long PAY_REPEAT_EXPIRE_SECONDS = 5 * 60;
    // 分布式锁过期时间（30秒，避免死锁）
    public static final long LOCK_EXPIRE_SECONDS = 30;
    // 库存缓存过期时间（永久，库存变更时主动更新）
    public static final long STOCK_EXPIRE_SECONDS = -1;

    // ===================== 私有工具方法 =====================
    /**
     * 构建Redis Key（统一规范，避免硬编码）
     * @param parts Key的各个部分
     * @return 完整Redis Key
     */
    private static String buildKey(String... parts) {
        StringBuilder sb = new StringBuilder(BUSINESS_PREFIX);
        for (String part : parts) {
            sb.append(SEPARATOR).append(part);
        }
        return sb.toString();
    }

    // 私有化构造方法，禁止实例化
    private RedisKeyUtil() {}
}