package com.concert.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class OrderVO {
    private Long id;
    private String orderNo;
    private Long userId;
    private Long concertId;
    private Long sessionId;
    private Integer ticketNum;
    private BigDecimal totalAmount;
    private Integer status;
    private Date createTime;

    private String verifyCode;

    private String username;
    private String concertName;
    private Date sessionTime;
}
