package com.concert.config; // 必须和你的config包路径一致

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

/**
 * MyBatis-Plus 字段自动填充处理器
 * 用于自动填充createTime、updateTime等时间字段
 */
@Component // 必须加这个注解，让Spring扫描并管理该类
public class MyMetaObjectHandler implements MetaObjectHandler {

    // 插入数据时自动填充（比如新增订单时）
    @Override
    public void insertFill(MetaObject metaObject) {
        // 自动填充createTime字段为当前时间
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
        // 自动填充updateTime字段为当前时间
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
    }

    // 更新数据时自动填充（比如修改订单状态时）
    @Override
    public void updateFill(MetaObject metaObject) {
        // 自动更新updateTime字段为当前时间
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
    }
}
