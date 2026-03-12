package com.concert.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.concert.entity.ConcertSession;
import com.concert.entity.User;
import com.concert.service.ConcertSessionService;
import com.concert.service.UserService;
import com.concert.vo.ConcertSessionVO;
import com.concert.utils.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/session")
@RequiredArgsConstructor
public class ConcertSessionController {
    private final ConcertSessionService sessionService;
    private final UserService userService;

    private User getAdminUser(HttpSession session, Long paramUserId) {
        User user = (User) session.getAttribute("loginUser");
        if (user != null && user.getType() == 1) return user;
        if (paramUserId != null && paramUserId > 0) {
            User dbUser = userService.getById(paramUserId);
            if (dbUser != null && dbUser.getType() == 1) return dbUser;
        }
        return null;
    }

    @PostMapping("/add")
    public Result<?> addSession(@Validated @RequestBody ConcertSessionVO vo) {
        return sessionService.addSession(vo);
    }

    @PostMapping("/preload/{sessionId}")
    public Result<?> preloadStock(@PathVariable Long sessionId) {
        return sessionService.preloadStock(sessionId);
    }

    @PostMapping("/open/{sessionId}")
    public Result<?> openSell(@PathVariable Long sessionId) {
        ConcertSession session = sessionService.getById(sessionId);
        if (session == null) return Result.error("场次不存在");
        session.setStatus(1);
        sessionService.updateById(session);
        return Result.success("已开启售票");
    }

    @GetMapping("/list/{concertId}")
    public Result<List<ConcertSession>> listByConcertId(@PathVariable Long concertId) {
        try {
            LambdaQueryWrapper<ConcertSession> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ConcertSession::getConcertId, concertId)
                    .eq(ConcertSession::getStatus, 1)
                    .orderByAsc(ConcertSession::getShowTime);
            return Result.success(sessionService.list(wrapper));
        } catch (Exception e) {
            return Result.error("查询场次失败：" + e.getMessage());
        }
    }

    @GetMapping("/all")
    public Result<List<ConcertSession>> listAll() {
        return Result.success(sessionService.list());
    }

    @GetMapping("/info/{sessionId}")
    public Result<ConcertSession> getSessionInfo(@PathVariable Long sessionId) {
        ConcertSession session = sessionService.getById(sessionId);
        if (session == null) return Result.error("场次不存在");
        return Result.success(session);
    }

    @GetMapping("/stock/{sessionId}")
    public Result<Integer> getSessionStock(@PathVariable Long sessionId) {
        ConcertSession session = sessionService.getById(sessionId);
        if (session == null) return Result.success(0);
        return Result.success(session.getSurplusStock());
    }

    @GetMapping("/minPrice/{concertId}")
    public Result<BigDecimal> getMinPrice(@PathVariable Long concertId) {
        try {
            LambdaQueryWrapper<ConcertSession> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ConcertSession::getConcertId, concertId)
                    .eq(ConcertSession::getStatus, 1);
            List<ConcertSession> list = sessionService.list(wrapper);
            if (list.isEmpty()) return Result.success(BigDecimal.ZERO);
            BigDecimal minPrice = list.stream()
                    .filter(item -> item.getPrice() != null)
                    .map(ConcertSession::getPrice)
                    .min(BigDecimal::compareTo)
                    .orElse(BigDecimal.ZERO);
            return Result.success(minPrice);
        } catch (Exception e) {
            return Result.error("查询最低票价失败：" + e.getMessage());
        }
    }

    @PutMapping("/update")
    public Result<?> update(@RequestBody ConcertSession concertSession,
                            @RequestParam(required = false) Long userId,
                            HttpSession httpSession) {
        User admin = getAdminUser(httpSession, userId);
        if (admin == null) return Result.build(403, "无权限操作");
        if (concertSession.getId() == null) return Result.paramError("场次ID不能为空");
        sessionService.updateById(concertSession);
        return Result.success("修改成功");
    }

    @DeleteMapping("/delete/{id}")
    public Result<?> delete(@PathVariable Long id,
                            @RequestParam(required = false) Long userId,
                            HttpSession httpSession) {
        User admin = getAdminUser(httpSession, userId);
        if (admin == null) return Result.build(403, "无权限操作");
        sessionService.removeById(id);
        return Result.success("删除成功");
    }
}
