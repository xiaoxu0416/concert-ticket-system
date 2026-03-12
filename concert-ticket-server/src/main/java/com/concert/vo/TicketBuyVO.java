package com.concert.vo;

import lombok.Data;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.io.Serializable;

/**
 * 抢票请求VO
 */
@Data
public class TicketBuyVO implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotNull(message = "场次ID不能为空")
    private Long sessionId;

    @Positive(message = "购票数量必须大于0")
    private Integer ticketNum = 1; // 默认购票1张

    // 新增：接收前端传入的用户ID（兼容无session登录态）
    private Long userId;
}