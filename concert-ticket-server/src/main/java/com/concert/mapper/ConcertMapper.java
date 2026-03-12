package com.concert.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.concert.entity.Concert;
import org.apache.ibatis.annotations.Select;
import java.util.List;

/**
 * 演唱会Mapper接口（MyBatis-Plus）
 * 毕设：基于BaseMapper实现单表CRUD，补充常用查询方法
 * 核心功能：演唱会信息的增删改查，支撑抢票系统的演唱会列表展示
 *
 * @author 真实姓名（替换为你的姓名）
 * @date 2026-03-06
 */
// 注：项目启动类已添加@MapperScan("com.concert.mapper")，无需重复添加@Mapper注解
public interface ConcertMapper extends BaseMapper<Concert> {

    /**
     * 自定义查询：查询已上架（status=1）的演唱会列表（按创建时间倒序）
     * 业务说明：status=1 代表“已上架可购票”，status=0 代表“未上架”，status=2 代表“已下架”
     * 性能优化：仅查询核心字段，避免SELECT * 冗余开销
     *
     * @return 已上架演唱会列表（含ID、名称、封面、演出时间、状态等核心字段）
     */
    @Select("SELECT id, concert_name, cover_img, perform_time, venue, status, create_time " +
            "FROM concert_info " +
            "WHERE status = 1 " +
            "ORDER BY create_time DESC")
    List<Concert> selectShelvedConcerts();

    // 【已删除】冗余的selectList重写方法：BaseMapper原生方法已满足需求，无需重写
}