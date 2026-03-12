package com.concert.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.concert.entity.StockLog;
import com.concert.mapper.StockLogMapper;
import com.concert.service.StockLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 库存日志服务实现类
 */
@Service
@RequiredArgsConstructor
public class StockLogServiceImpl extends ServiceImpl<StockLogMapper, StockLog> implements StockLogService {
}