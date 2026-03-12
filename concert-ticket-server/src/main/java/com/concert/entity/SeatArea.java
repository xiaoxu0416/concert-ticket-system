package com.concert.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("seat_area")
public class SeatArea implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long sessionId;
    private String areaName;
    private BigDecimal price;
    private Integer totalStock;
    private Integer surplusStock;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
