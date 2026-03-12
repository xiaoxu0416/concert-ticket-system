package com.concert.vo;

import lombok.Data;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.io.Serializable;

/**
 * 场次配置VO
 */
@Data
public class ConcertSessionVO implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotNull(message = "演唱会ID不能为空")
    private Long concertId;

    @NotNull(message = "演出时间不能为空")
    private LocalDateTime showTime;

    @NotNull(message = "票价不能为空")
    private BigDecimal price;

    @NotNull(message = "总库存不能为空")
    private Integer totalStock;

    @NotNull(message = "开票时间不能为空")
    private LocalDateTime openTime;
}
