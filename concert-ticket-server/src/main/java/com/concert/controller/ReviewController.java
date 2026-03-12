package com.concert.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.concert.entity.Review;
import com.concert.entity.User;
import com.concert.mapper.ReviewMapper;
import com.concert.service.ReviewService;
import com.concert.service.UserService;
import com.concert.utils.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/review")
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewService reviewService;
    private final ReviewMapper reviewMapper;
    private final UserService userService;

    private Long getValidUserId(HttpSession session, Long paramUserId) {
        User user = (User) session.getAttribute("loginUser");
        if (user != null) return user.getId();
        if (paramUserId != null && paramUserId > 0) return paramUserId;
        return null;
    }

    @GetMapping("/list/{concertId}")
    public Result<List<Review>> list(@PathVariable Long concertId) {
        return Result.success(reviewMapper.selectReviewsWithUser(concertId));
    }

    @GetMapping("/all")
    public Result<?> listAll(@RequestParam(required = false) Long userId, HttpSession session) {
        Long validUserId = getValidUserId(session, userId);
        if (validUserId == null) return Result.build(401, "请先登录");
        User user = userService.getById(validUserId);
        if (user == null || user.getType() != 1) return Result.build(403, "无权限访问");
        return Result.success(reviewMapper.selectAllReviewsWithDetail());
    }

    @GetMapping("/avg/{concertId}")
    public Result<?> avgRating(@PathVariable Long concertId) {
        LambdaQueryWrapper<Review> w = new LambdaQueryWrapper<>();
        w.eq(Review::getConcertId, concertId);
        List<Review> list = reviewService.list(w);
        Map<String, Object> result = new HashMap<>();
        if (list.isEmpty()) {
            result.put("avg", 0);
            result.put("count", 0);
        } else {
            double avg = list.stream().mapToInt(Review::getRating).average().orElse(0);
            result.put("avg", BigDecimal.valueOf(avg).setScale(1, RoundingMode.HALF_UP));
            result.put("count", list.size());
        }
        return Result.success(result);
    }

    @PostMapping("/add")
    public Result<?> add(@RequestBody Review review, @RequestParam(required = false) Long userId, HttpSession session) {
        Long validUserId = getValidUserId(session, userId);
        if (validUserId == null) return Result.build(401, "请先登录");
        review.setUserId(validUserId);
        if (review.getRating() == null || review.getRating() < 1 || review.getRating() > 5) return Result.paramError("评分需在1-5之间");
        LambdaQueryWrapper<Review> w = new LambdaQueryWrapper<>();
        w.eq(Review::getUserId, validUserId).eq(Review::getConcertId, review.getConcertId());
        if (reviewService.count(w) > 0) return Result.error("您已评价过该演唱会");
        reviewService.save(review);
        return Result.success("评价成功");
    }

    @DeleteMapping("/delete/{id}")
    public Result<?> delete(@PathVariable Long id, @RequestParam(required = false) Long userId, HttpSession session) {
        Long validUserId = getValidUserId(session, userId);
        if (validUserId == null) return Result.build(401, "请先登录");
        Review review = reviewService.getById(id);
        if (review == null) return Result.error("评价不存在");
        if (!review.getUserId().equals(validUserId)) {
            User user = userService.getById(validUserId);
            if (user == null || user.getType() != 1) return Result.build(403, "无权删除");
        }
        reviewService.removeById(id);
        return Result.success("删除成功");
    }
}
