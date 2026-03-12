package com.concert.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单实体类
 */
@Data
@TableName("concert_order")
public class Order implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 订单ID（主键）
     * 改为自增策略，和数据库初始化SQL保持一致
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 订单编号（唯一，雪花算法）
     */
    private String orderNo;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 演唱会ID
     */
    private Long concertId;

    /**
     * 场次ID
     */
    private Long sessionId;

    /**
     * 座位区域ID
     */
    private Long seatAreaId;

    /**
     * 座位区域名称
     */
    private String seatAreaName;

    /**
     * 购票数量
     */
    private Integer ticketNum;

    /**
     * 订单金额（核心字段，已正确定义）
     */
    private BigDecimal totalAmount;

    /**
     * 订单状态：0-待支付 1-已支付 2-已取消 3-已完成 4-已退款
     */
    private Integer status;

    /**
     * 支付时间
     */
    private LocalDateTime payTime;

    /**
     * 核销码
     */
    private String verifyCode;

    /**
     * 核销时间
     */
    private LocalDateTime verifyTime;

    /**
     * 创建时间（自动填充）
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间（自动填充）
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
