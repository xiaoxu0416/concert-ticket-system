package com.concert.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.concert.entity.Concert;
import com.concert.entity.User;
import com.concert.service.ConcertService;
import com.concert.service.UserService;
import com.concert.utils.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.List;

@RestController
@RequestMapping("/concert")
@RequiredArgsConstructor
public class ConcertController {
    private final ConcertService concertService;
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

    @GetMapping("/list")
    public Result<IPage<Concert>> list(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size) {
        Page<Concert> page = new Page<>(current, size);
        LambdaQueryWrapper<Concert> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Concert::getStatus, 1).orderByDesc(Concert::getCreateTime);
        return Result.success(concertService.page(page, queryWrapper));
    }

    @GetMapping("/all")
    public Result<List<Concert>> listAll() {
        LambdaQueryWrapper<Concert> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Concert::getCreateTime);
        return Result.success(concertService.list(wrapper));
    }

    @GetMapping("/detail/{id}")
    public Result<Concert> detail(@PathVariable Long id) {
        Concert concert = concertService.getById(id);
        return concert != null ? Result.success(concert) : Result.error("演唱会不存在");
    }

    @PostMapping("/add")
    public Result<?> add(@RequestBody Concert concert,
                         @RequestParam(required = false) Long userId,
                         HttpSession session) {
        User admin = getAdminUser(session, userId);
        if (admin == null) return Result.build(403, "无权限操作");
        if (concert.getStatus() == null) concert.setStatus(0);
        concertService.save(concert);
        return Result.success("添加成功");
    }

    @PutMapping("/update")
    public Result<?> update(@RequestBody Concert concert,
                            @RequestParam(required = false) Long userId,
                            HttpSession session) {
        User admin = getAdminUser(session, userId);
        if (admin == null) return Result.build(403, "无权限操作");
        if (concert.getId() == null) return Result.paramError("演唱会ID不能为空");
        concertService.updateById(concert);
        return Result.success("修改成功");
    }

    @DeleteMapping("/delete/{id}")
    public Result<?> delete(@PathVariable Long id,
                            @RequestParam(required = false) Long userId,
                            HttpSession session) {
        User admin = getAdminUser(session, userId);
        if (admin == null) return Result.build(403, "无权限操作");
        concertService.removeById(id);
        return Result.success("删除成功");
    }
}
