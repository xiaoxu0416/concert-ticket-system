package com.concert.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.concert.entity.Order;
import com.concert.vo.OrderVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface OrderMapper extends BaseMapper<Order> {

    @Select("SELECT " +
            "o.id, " +
            "o.order_no AS orderNo, " +
            "o.user_id AS userId, " +
            "o.concert_id AS concertId, " +
            "o.session_id AS sessionId, " +
            "o.ticket_num AS ticketNum, " +
            "o.total_amount AS totalAmount, " +
            "o.status, " +
            "o.verify_code AS verifyCode, " +
            "o.create_time AS createTime, " +
            "c.name AS concertName, " +
            "cs.show_time AS sessionTime " +
            "FROM concert_order o " +
            "LEFT JOIN concert_session cs ON o.session_id = cs.id " +
            "LEFT JOIN concert_info c ON o.concert_id = c.id " +
            "WHERE o.user_id = #{userId} " +
            "ORDER BY o.create_time DESC")
    List<OrderVO> selectOrderListByUserId(Long userId);

    @Select("SELECT " +
            "o.id, " +
            "o.order_no AS orderNo, " +
            "o.user_id AS userId, " +
            "o.concert_id AS concertId, " +
            "o.session_id AS sessionId, " +
            "o.ticket_num AS ticketNum, " +
            "o.total_amount AS totalAmount, " +
            "o.status, " +
            "o.verify_code AS verifyCode, " +
            "o.create_time AS createTime, " +
            "u.username AS username, " +
            "c.name AS concertName, " +
            "cs.show_time AS sessionTime " +
            "FROM concert_order o " +
            "LEFT JOIN sys_user u ON o.user_id = u.id " +
            "LEFT JOIN concert_session cs ON o.session_id = cs.id " +
            "LEFT JOIN concert_info c ON o.concert_id = c.id " +
            "ORDER BY o.create_time DESC")
    List<OrderVO> selectAllOrderList();
}
