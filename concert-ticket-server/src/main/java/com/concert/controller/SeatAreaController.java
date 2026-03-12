package com.concert.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.concert.entity.SeatArea;
import com.concert.entity.User;
import com.concert.service.SeatAreaService;
import com.concert.service.UserService;
import com.concert.utils.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.List;

@RestController
@RequestMapping("/seatArea")
@RequiredArgsConstructor
public class SeatAreaController {
    private final SeatAreaService seatAreaService;
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

    @GetMapping("/list/{sessionId}")
    public Result<List<SeatArea>> list(@PathVariable Long sessionId) {
        LambdaQueryWrapper<SeatArea> w = new LambdaQueryWrapper<>();
        w.eq(SeatArea::getSessionId, sessionId).orderByAsc(SeatArea::getPrice);
        return Result.success(seatAreaService.list(w));
    }

    @GetMapping("/all")
    public Result<List<SeatArea>> all() {
        return Result.success(seatAreaService.list());
    }

    @PostMapping("/add")
    public Result<?> add(@RequestBody SeatArea area, @RequestParam(required = false) Long userId, HttpSession session) {
        User admin = getAdminUser(session, userId);
        if (admin == null) return Result.build(403, "无权限");
        seatAreaService.save(area);
        return Result.success("添加成功");
    }

    @PutMapping("/update")
    public Result<?> update(@RequestBody SeatArea area, @RequestParam(required = false) Long userId, HttpSession session) {
        User admin = getAdminUser(session, userId);
        if (admin == null) return Result.build(403, "无权限");
        seatAreaService.updateById(area);
        return Result.success("修改成功");
    }

    @DeleteMapping("/delete/{id}")
    public Result<?> delete(@PathVariable Long id, @RequestParam(required = false) Long userId, HttpSession session) {
        User admin = getAdminUser(session, userId);
        if (admin == null) return Result.build(403, "无权限");
        seatAreaService.removeById(id);
        return Result.success("删除成功");
    }
}
