package com.concert.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.concert.entity.Concert;
import com.concert.mapper.ConcertMapper;
import com.concert.service.ConcertService;
import com.concert.utils.Result;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 演唱会服务实现类
 */
@Service
@RequiredArgsConstructor
@Mapper
public class ConcertServiceImpl extends ServiceImpl<ConcertMapper, Concert> implements ConcertService {
    private final ConcertMapper concertMapper;

    @Override
    public Result<?> publish(Concert concert) {
        concert.setStatus(0); // 默认未上架
        concert.setCreateTime(LocalDateTime.now());
        concert.setUpdateTime(LocalDateTime.now());
        boolean save = save(concert);
        return save ? Result.success() : Result.error("发布失败");
    }

    @Override
    public Result<?> changeStatus(Long id, Integer status) {
        Concert concert = getById(id);
        if (concert == null) {
            return Result.error("演唱会不存在");
        }
        concert.setStatus(status);
        concert.setUpdateTime(LocalDateTime.now());
        boolean update = updateById(concert);
        return update ? Result.success() : Result.error("操作失败");
    }
}