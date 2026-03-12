package com.concert.vo;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 支付请求VO
 */
@Data
public class OrderPayVO implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "订单编号不能为空")
    private String orderNo;

    // 新增：支付方式（1-微信支付 2-支付宝支付）
    @NotNull(message = "支付方式不能为空")
    private String payType;

    // 新增：用户ID（兼容前端传参，解决session丢失问题）
    private Long userId;
}