package com.concert.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 库存操作日志（毕设核心：审计库存变化，防止超卖、追溯问题）
 * 优化版：补充审计字段、统一常量、自动填充时间、适配自增ID
 */
@Data
@TableName("concert_stock_log")
public class StockLog implements Serializable {
    private static final long serialVersionUID = 1L;

    // ========== 操作类型常量（毕设规范：避免硬编码） ==========
    public static final Integer OPERATE_TYPE_PREHEAT = 0;    // 库存预热（初始化Redis库存）
    public static final Integer OPERATE_TYPE_DEDUCT = 1;     // 库存扣减（用户抢票）
    public static final Integer OPERATE_TYPE_ROLLBACK = 2;   // 库存回滚（抢票失败/订单取消）

    /**
     * 日志ID（主键）
     * 毕设适配：改为自增策略，和数据库初始化SQL（id=1/2）保持一致
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 场次ID（关联concert_session表）
     */
    private Long sessionId;

    /**
     * 关联订单号（毕设核心审计字段：追溯“哪个订单导致的库存变化”）
     */
    private String orderNo;

    /**
     * 操作类型：0-库存预热 1-库存扣减 2-库存回滚
     * 参考常量：OPERATE_TYPE_PREHEAT/OPERATE_TYPE_DEDUCT/OPERATE_TYPE_ROLLBACK
     */
    private Integer operateType;

    /**
     * 操作前库存（毕设审计：对比操作前后库存，排查超卖问题）
     */
    private Integer beforeStock;

    /**
     * 操作数量（正数=增加，负数=减少）
     */
    private Integer operateNum;

    /**
     * 操作后库存（核心审计字段）
     */
    private Integer afterStock;

    /**
     * 操作人/系统（如：testuser、system、订单号xxx）
     */
    private String operator;

    /**
     * 操作备注（毕设扩展：记录操作原因，如“用户123抢票扣减”“订单456失败回滚”）
     */
    private String remark;

    /**
     * 创建时间（MyBatis-Plus自动填充，无需手动赋值）
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}