package com.concert.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.concert.entity.Announcement;
import com.concert.entity.User;
import com.concert.service.AnnouncementService;
import com.concert.service.UserService;
import com.concert.utils.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.List;

@RestController
@RequestMapping("/announcement")
@RequiredArgsConstructor
public class AnnouncementController {
    private final AnnouncementService announcementService;
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
    public Result<List<Announcement>> list() {
        LambdaQueryWrapper<Announcement> w = new LambdaQueryWrapper<>();
        w.eq(Announcement::getStatus, 1).orderByAsc(Announcement::getSortOrder).orderByDesc(Announcement::getCreateTime);
        return Result.success(announcementService.list(w));
    }

    @GetMapping("/banners")
    public Result<List<Announcement>> banners() {
        LambdaQueryWrapper<Announcement> w = new LambdaQueryWrapper<>();
        w.eq(Announcement::getType, 1).eq(Announcement::getStatus, 1).orderByAsc(Announcement::getSortOrder);
        return Result.success(announcementService.list(w));
    }

    @GetMapping("/all")
    public Result<List<Announcement>> all(@RequestParam(required = false) Long userId, HttpSession session) {
        User admin = getAdminUser(session, userId);
        if (admin == null) return Result.build(403, "无权限");
        return Result.success(announcementService.list(new LambdaQueryWrapper<Announcement>().orderByDesc(Announcement::getCreateTime)));
    }

    @PostMapping("/add")
    public Result<?> add(@RequestBody Announcement a, @RequestParam(required = false) Long userId, HttpSession session) {
        User admin = getAdminUser(session, userId);
        if (admin == null) return Result.build(403, "无权限");
        announcementService.save(a);
        return Result.success("添加成功");
    }

    @PutMapping("/update")
    public Result<?> update(@RequestBody Announcement a, @RequestParam(required = false) Long userId, HttpSession session) {
        User admin = getAdminUser(session, userId);
        if (admin == null) return Result.build(403, "无权限");
        announcementService.updateById(a);
        return Result.success("修改成功");
    }

    @DeleteMapping("/delete/{id}")
    public Result<?> delete(@PathVariable Long id, @RequestParam(required = false) Long userId, HttpSession session) {
        User admin = getAdminUser(session, userId);
        if (admin == null) return Result.build(403, "无权限");
        announcementService.removeById(id);
        return Result.success("删除成功");
    }
}
