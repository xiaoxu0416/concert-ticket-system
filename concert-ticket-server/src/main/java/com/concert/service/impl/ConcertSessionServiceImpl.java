package com.concert.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.concert.entity.ConcertSession;
import com.concert.entity.StockLog;
import com.concert.mapper.ConcertSessionMapper;
import com.concert.mapper.StockLogMapper;
import com.concert.service.ConcertSessionService;
import com.concert.vo.ConcertSessionVO;
import com.concert.utils.RedisKeyUtil;
import com.concert.utils.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * 场次服务实现类（Redis库存核心）
 * 高并发优化版：
 * 1. 完善Redis库存原子操作，防超卖、防空指针
 * 2. 增加分布式锁，避免并发库存操作冲突
 * 3. 完善日志打印，便于高并发问题排查
 * 4. 兼容订单服务的库存扣减/回滚逻辑
 */
@Service
@RequiredArgsConstructor
@Slf4j // 新增日志注解，替换System.out.println
public class ConcertSessionServiceImpl extends ServiceImpl<ConcertSessionMapper, ConcertSession> implements ConcertSessionService {
    private final ConcertSessionMapper sessionMapper;
    private final StockLogMapper stockLogMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    // 库存操作类型：0-预热 1-扣减 2-回滚
    private static final Integer OPERATE_PRELOAD = 0;
    private static final Integer OPERATE_DEDUCT = 1;
    private static final Integer OPERATE_ROLLBACK = 2;

    // 分布式锁超时时间（秒），避免死锁
    private static final long LOCK_TIMEOUT = 10;

    // ========== 新增：项目启动时自动预热所有在售场次库存 ==========
    @PostConstruct
    public void initAllSessionStock() {
        try {
            // 查询所有已开票场次
            LambdaQueryWrapper<ConcertSession> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ConcertSession::getStatus, 1);
            List<ConcertSession> sessions = list(wrapper);

            if (sessions.isEmpty()) {
                log.info("【Redis库存预热】无在售场次，跳过初始化");
                return;
            }

            // 批量预热库存到Redis
            int successCount = 0;
            for (ConcertSession session : sessions) {
                try {
                    String stockKey = RedisKeyUtil.getSessionStockKey(session.getId());
                    // Redis设置库存（永久有效，库存变更时主动更新）
                    redisTemplate.opsForValue().set(stockKey, session.getSurplusStock());
                    // 记录预热日志
                    recordStockLog(session.getId(), OPERATE_PRELOAD, 0, session.getSurplusStock());
                    successCount++;
                } catch (Exception e) {
                    log.error("【Redis库存预热】场次{}预热失败", session.getId(), e);
                }
            }
            log.info("【Redis库存预热】完成，共处理{}个场次，成功{}个", sessions.size(), successCount);
        } catch (Exception e) {
            log.error("【Redis库存预热】整体初始化失败", e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<?> addSession(ConcertSessionVO vo) {
        try {
            // 构建场次对象
            ConcertSession session = new ConcertSession();
            session.setConcertId(vo.getConcertId());
            session.setShowTime(vo.getShowTime());
            session.setPrice(vo.getPrice());
            session.setTotalStock(vo.getTotalStock());
            session.setSurplusStock(vo.getTotalStock()); // 初始剩余库存=总库存
            session.setOpenTime(vo.getOpenTime());
            session.setStatus(0); // 未开票
            session.setCreateTime(LocalDateTime.now());
            session.setUpdateTime(LocalDateTime.now());

            // 保存场次
            boolean save = save(session);
            if (!save) {
                log.error("【添加场次】保存场次失败，参数：{}", vo);
                return Result.error("添加场次失败");
            }

            // 自动库存预热到Redis
            preloadStock(session.getId());
            log.info("【添加场次】场次{}添加成功，已完成库存预热", session.getId());
            return Result.success("添加场次成功，已完成库存预热");
        } catch (Exception e) {
            log.error("【添加场次】失败，参数：{}", vo, e);
            return Result.error("添加场次失败：" + e.getMessage());
        }
    }

    @Override
    public Result<?> preloadStock(Long sessionId) {
        try {
            ConcertSession session = getById(sessionId);
            if (session == null) {
                log.warn("【库存预热】场次{}不存在", sessionId);
                return Result.error("场次不存在");
            }

            // Redis键
            String stockKey = RedisKeyUtil.getSessionStockKey(sessionId);
            // 库存预热（覆盖原有值）
            redisTemplate.opsForValue().set(stockKey, session.getSurplusStock());
            // 记录库存日志
            recordStockLog(sessionId, OPERATE_PRELOAD, 0, session.getSurplusStock());

            log.info("【库存预热】场次{}预热成功，当前库存：{}", sessionId, session.getSurplusStock());
            return Result.success("库存预热成功，当前库存：" + session.getSurplusStock());
        } catch (Exception e) {
            log.error("【库存预热】场次{}失败", sessionId, e);
            return Result.error("库存预热失败：" + e.getMessage());
        }
    }

    @Override
    public boolean deductRedisStock(Long sessionId, Integer num) {
        // 入参校验
        if (num <= 0 || sessionId == null) {
            log.warn("【Redis库存扣减】入参非法，sessionId：{}，扣减数量：{}", sessionId, num);
            return false;
        }

        String stockKey = RedisKeyUtil.getSessionStockKey(sessionId);
        String lockKey = RedisKeyUtil.getStockLockKey(sessionId);

        // 分布式锁：防止并发扣减导致库存异常
        Boolean locked = redisTemplate.opsForValue().setIfAbsent(lockKey, "1", LOCK_TIMEOUT, TimeUnit.SECONDS);
        if (Boolean.FALSE.equals(locked)) {
            log.warn("【Redis库存扣减】场次{}获取分布式锁失败，并发扣减中", sessionId);
            return false;
        }

        try {
            // 1. 检查Redis是否有库存，无则从数据库加载
            Object stockObj = redisTemplate.opsForValue().get(stockKey);
            Integer currentStock = 0;
            if (stockObj == null) {
                ConcertSession session = getById(sessionId);
                if (session == null) {
                    log.error("【Redis库存扣减】场次{}不存在，无法加载库存", sessionId);
                    return false;
                }
                currentStock = session.getSurplusStock();
                redisTemplate.opsForValue().set(stockKey, currentStock);
                recordStockLog(sessionId, OPERATE_PRELOAD, 0, currentStock);
                log.info("【Redis库存扣减】场次{}Redis无库存，从DB加载并预热：{}", sessionId, currentStock);
            } else {
                currentStock = Integer.parseInt(stockObj.toString());
            }

            // 2. 预检查库存是否足够（避免无效扣减）
            if (currentStock < num) {
                log.warn("【Redis库存扣减】场次{}库存不足，当前：{}，扣减：{}", sessionId, currentStock, num);
                return false;
            }

            // 3. Redis原子扣减（核心：decrement是原子操作，防超卖）
            Long afterStock = redisTemplate.opsForValue().decrement(stockKey, num);
            if (afterStock == null || afterStock < 0) {
                log.error("【Redis库存扣减】场次{}扣减异常，扣减后库存：{}", sessionId, afterStock);
                // 回滚扣减操作
                redisTemplate.opsForValue().increment(stockKey, num);
                return false;
            }

            // 4. 记录库存日志
            recordStockLog(sessionId, OPERATE_DEDUCT, num, afterStock.intValue());
            log.debug("【Redis库存扣减】场次{}成功，扣减{}，剩余{}", sessionId, num, afterStock);
            return true;
        } catch (Exception e) {
            log.error("【Redis库存扣减】场次{}失败，扣减数量：{}", sessionId, num, e);
            // 异常时回滚扣减
            redisTemplate.opsForValue().increment(stockKey, num);
            return false;
        } finally {
            // 释放分布式锁
            redisTemplate.delete(lockKey);
        }
    }

    @Override
    public void rollbackRedisStock(Long sessionId, Integer num) {
        // 入参校验
        if (num <= 0 || sessionId == null) {
            log.warn("【Redis库存回滚】入参非法，sessionId：{}，回滚数量：{}", sessionId, num);
            return;
        }

        String stockKey = RedisKeyUtil.getSessionStockKey(sessionId);
        String lockKey = RedisKeyUtil.getStockLockKey(sessionId);

        // 分布式锁：防止并发回滚导致库存异常
        Boolean locked = redisTemplate.opsForValue().setIfAbsent(lockKey, "1", LOCK_TIMEOUT, TimeUnit.SECONDS);
        if (Boolean.FALSE.equals(locked)) {
            log.warn("【Redis库存回滚】场次{}获取分布式锁失败，并发回滚中", sessionId);
            return;
        }

        try {
            // Redis原子回滚（increment是原子操作）
            Long afterStock = redisTemplate.opsForValue().increment(stockKey, num);
            if (afterStock != null) {
                // 记录回滚日志
                recordStockLog(sessionId, OPERATE_ROLLBACK, num, afterStock.intValue());
                log.debug("【Redis库存回滚】场次{}成功，回滚{}，剩余{}", sessionId, num, afterStock);
            } else {
                log.error("【Redis库存回滚】场次{}回滚后库存为null", sessionId);
            }
        } catch (Exception e) {
            log.error("【Redis库存回滚】场次{}失败，回滚数量：{}", sessionId, num, e);
        } finally {
            // 释放分布式锁
            redisTemplate.delete(lockKey);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deductDbStock(Long sessionId, Integer num) {
        if (num <= 0 || sessionId == null) {
            log.warn("【DB库存扣减】入参非法，sessionId：{}，扣减数量：{}", sessionId, num);
            return false;
        }

        try {
            // 乐观锁扣减库存（SQL保证库存>=num才扣减）
            int row = sessionMapper.deductStock(sessionId, num);
            if (row > 0) {
                log.debug("【DB库存扣减】场次{}成功，扣减{}", sessionId, num);
                return true;
            } else {
                log.warn("【DB库存扣减】场次{}失败，库存不足或数据不存在", sessionId);
                return false;
            }
        } catch (Exception e) {
            log.error("【DB库存扣减】场次{}失败，扣减数量：{}", sessionId, num, e);
            return false;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rollbackDbStock(Long sessionId, Integer num) {
        if (num <= 0 || sessionId == null) {
            log.warn("【DB库存回滚】入参非法，sessionId：{}，回滚数量：{}", sessionId, num);
            return;
        }

        try {
            sessionMapper.rollbackStock(sessionId, num);
            log.debug("【DB库存回滚】场次{}成功，回滚{}", sessionId, num);
        } catch (Exception e) {
            log.error("【DB库存回滚】场次{}失败，回滚数量：{}", sessionId, num, e);
            throw new RuntimeException("DB库存回滚失败", e); // 抛出异常触发事务回滚
        }
    }

    @Override
    public void recordStockLog(Long sessionId, Integer operateType, Integer operateNum, Integer afterStock) {
        try {
            StockLog log = new StockLog();
            log.setSessionId(sessionId);
            log.setOperateType(operateType);
            log.setOperateNum(operateNum);
            log.setAfterStock(afterStock);
            log.setOperator("system");
            log.setCreateTime(LocalDateTime.now());
            stockLogMapper.insert(log);
        } catch (Exception e) {
            log.error("【库存日志记录】场次{}失败，操作类型：{}", sessionId, operateType, e);
            // 日志记录失败不影响核心业务，仅打印日志
        }
    }
}