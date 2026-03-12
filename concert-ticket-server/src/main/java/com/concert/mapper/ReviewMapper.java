package com.concert.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.concert.entity.Review;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ReviewMapper extends BaseMapper<Review> {

    @Select("SELECT r.*, u.username, u.nickname FROM review r LEFT JOIN sys_user u ON r.user_id = u.id WHERE r.concert_id = #{concertId} ORDER BY r.create_time DESC")
    List<Review> selectReviewsWithUser(Long concertId);

    @Select("SELECT r.*, u.username, u.nickname, c.name AS concertName FROM review r LEFT JOIN sys_user u ON r.user_id = u.id LEFT JOIN concert_info c ON r.concert_id = c.id ORDER BY r.create_time DESC")
    List<java.util.Map<String, Object>> selectAllReviewsWithDetail();
}
