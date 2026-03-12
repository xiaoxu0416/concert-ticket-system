package com.concert.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.concert.entity.Favorite;
import com.concert.entity.User;
import com.concert.service.FavoriteService;
import com.concert.utils.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/favorite")
@RequiredArgsConstructor
public class FavoriteController {
    private final FavoriteService favoriteService;

    private Long getValidUserId(HttpSession session, Long paramUserId) {
        User user = (User) session.getAttribute("loginUser");
        if (user != null) return user.getId();
        if (paramUserId != null && paramUserId > 0) return paramUserId;
        return null;
    }

    @PostMapping("/toggle")
    public Result<?> toggle(@RequestBody Favorite fav, @RequestParam(required = false) Long userId, HttpSession session) {
        Long uid = getValidUserId(session, userId);
        if (uid == null) return Result.build(401, "请先登录");
        LambdaQueryWrapper<Favorite> w = new LambdaQueryWrapper<>();
        w.eq(Favorite::getUserId, uid).eq(Favorite::getConcertId, fav.getConcertId());
        Favorite exist = favoriteService.getOne(w);
        if (exist != null) {
            favoriteService.removeById(exist.getId());
            return Result.success("已取消收藏");
        } else {
            Favorite f = new Favorite();
            f.setUserId(uid);
            f.setConcertId(fav.getConcertId());
            favoriteService.save(f);
            return Result.success("已收藏");
        }
    }

    @GetMapping("/check/{concertId}")
    public Result<Boolean> check(@PathVariable Long concertId, @RequestParam(required = false) Long userId, HttpSession session) {
        Long uid = getValidUserId(session, userId);
        if (uid == null) return Result.success(false);
        LambdaQueryWrapper<Favorite> w = new LambdaQueryWrapper<>();
        w.eq(Favorite::getUserId, uid).eq(Favorite::getConcertId, concertId);
        return Result.success(favoriteService.count(w) > 0);
    }

    @GetMapping("/my")
    public Result<List<Long>> myFavorites(@RequestParam(required = false) Long userId, HttpSession session) {
        Long uid = getValidUserId(session, userId);
        if (uid == null) return Result.success(List.of());
        LambdaQueryWrapper<Favorite> w = new LambdaQueryWrapper<>();
        w.eq(Favorite::getUserId, uid);
        List<Long> ids = favoriteService.list(w).stream().map(Favorite::getConcertId).collect(Collectors.toList());
        return Result.success(ids);
    }
}
