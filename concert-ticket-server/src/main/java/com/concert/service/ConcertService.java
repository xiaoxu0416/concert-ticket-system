package com.concert.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.concert.entity.Concert;
import com.concert.utils.Result;

/**
 * 演唱会服务接口
 */
public interface ConcertService extends IService<Concert> {
    /**
     * 发布演唱会
     */
    Result<?> publish(Concert concert);

    /**
     * 上下架演唱会
     */
    Result<?> changeStatus(Long id, Integer status);
}