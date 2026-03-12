package com.concert.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 演唱会场次实体类
 * 关联表：concert_session
 */
@Data
@TableName("concert_session")
public class ConcertSession implements Serializable {
    private static final long serialVersionUID = 1L;

    // ========== 状态常量（核心优化：统一管理状态值） ==========
    public static final Integer STATUS_UNOPEN = 0;     // 未开票
    public static final Integer STATUS_SELLING = 1;    // 售票中
    public static final Integer STATUS_SOLD_OUT = 2;   // 已售罄
    public static final Integer STATUS_FINISHED = 3;   // 已结束

    /**
     * 场次ID（主键）
     * 修复：改为自增策略，和数据库初始化SQL保持一致
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 演唱会ID（关联concert_info表的id）
     */
    private Long concertId;

    /**
     * 演出时间（如：2026-06-01 19:30:00）
     */
    private LocalDateTime showTime;

    /**
     * 票档价格（BigDecimal避免浮点精度问题）
     */
    private BigDecimal price;

    /**
     * 总库存（初始库存）
     */
    private Integer totalStock;

    /**
     * 剩余库存（数据库层面，Redis为主库存）
     */
    private Integer surplusStock;

    /**
     * 开票时间（开始售票的时间）
     */
    private LocalDateTime openTime;

    /**
     * 状态：0-未开票 1-售票中 2-已售罄 3-已结束
     * 参考常量：STATUS_UNOPEN/STATUS_SELLING/STATUS_SOLD_OUT/STATUS_FINISHED
     */
    private Integer status;

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