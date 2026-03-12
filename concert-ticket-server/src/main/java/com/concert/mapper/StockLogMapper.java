package com.concert.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.concert.entity.StockLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 库存日志Mapper
 */
@Mapper
public interface StockLogMapper extends BaseMapper<StockLog> {
}
