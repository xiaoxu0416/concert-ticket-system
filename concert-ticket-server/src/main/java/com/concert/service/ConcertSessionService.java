package com.concert.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.concert.entity.ConcertSession;
import com.concert.vo.ConcertSessionVO;
import com.concert.utils.Result;

/**
 * 场次服务接口
 */
public interface ConcertSessionService extends IService<ConcertSession> {
    /**
     * 添加场次并初始化库存
     */
    Result<?> addSession(ConcertSessionVO vo);

    /**
     * 库存预热（加载到Redis）
     */
    Result<?> preloadStock(Long sessionId);

    /**
     * 扣减Redis库存（原子操作）
     */
    boolean deductRedisStock(Long sessionId, Integer num);

    /**
     * 回滚Redis库存
     */
    void rollbackRedisStock(Long sessionId, Integer num);

    /**
     * 扣减数据库库存
     */
    boolean deductDbStock(Long sessionId, Integer num);

    /**
     * 回滚数据库库存
     */
    void rollbackDbStock(Long sessionId, Integer num);

    /**
     * 记录库存操作日志
     */
    void recordStockLog(Long sessionId, Integer operateType, Integer operateNum, Integer afterStock);
}