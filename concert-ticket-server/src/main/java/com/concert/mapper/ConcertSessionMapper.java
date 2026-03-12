package com.concert.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.concert.entity.ConcertSession;
import org.apache.ibatis.annotations.Param;

/**
 * 场次Mapper接口（含库存扣减/回滚）
 */
public interface ConcertSessionMapper extends BaseMapper<ConcertSession> {

    /**
     * 乐观锁扣减库存
     * @param sessionId 场次ID
     * @param num 扣减数量
     * @return 影响行数（>0表示扣减成功）
     */
    int deductStock(@Param("sessionId") Long sessionId, @Param("num") Integer num);

    /**
     * 回滚库存
     * @param sessionId 场次ID
     * @param num 回滚数量
     * @return 影响行数
     */
    int rollbackStock(@Param("sessionId") Long sessionId, @Param("num") Integer num);
}