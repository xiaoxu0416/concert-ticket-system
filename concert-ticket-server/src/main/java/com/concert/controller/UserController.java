package com.concert.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.concert.entity.User;
import com.concert.service.UserService;
import com.concert.vo.UserLoginVO;
import com.concert.vo.UserRegisterVO;
import com.concert.utils.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.List;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
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

    @PostMapping("/register")
    public Result<?> register(@Validated @RequestBody UserRegisterVO vo) {
        return userService.register(vo);
    }

    @PostMapping("/login")
    public Result<User> login(@Validated @RequestBody UserLoginVO vo, HttpSession session) {
        Result<User> result = userService.login(vo);
        if (200 == result.getCode()) {
            session.setAttribute("loginUser", result.getData());
        }
        return result;
    }

    @GetMapping("/current")
    public Result<User> getCurrentUser(HttpSession session) {
        User user = (User) session.getAttribute("loginUser");
        return user != null ? Result.success(user) : Result.build(401, "未登录");
    }

    @PostMapping("/logout")
    public Result<?> logout(HttpSession session) {
        session.removeAttribute("loginUser");
        return Result.success("登出成功");
    }

    @GetMapping("/all")
    public Result<List<User>> listAll(@RequestParam(required = false) Long userId, HttpSession session) {
        User admin = getAdminUser(session, userId);
        if (admin == null) return Result.build(403, "无权限访问");
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(User::getId);
        List<User> list = userService.list(wrapper);
        list.forEach(u -> u.setPassword(null));
        return Result.success(list);
    }

    @PutMapping("/update")
    public Result<?> update(@RequestBody User user,
                            @RequestParam(required = false) Long adminUserId,
                            HttpSession session) {
        User admin = getAdminUser(session, adminUserId);
        if (admin == null) return Result.build(403, "无权限操作");
        if (user.getId() == null) return Result.paramError("用户ID不能为空");
        User updateUser = new User();
        updateUser.setId(user.getId());
        updateUser.setNickname(user.getNickname());
        updateUser.setPhone(user.getPhone());
        updateUser.setType(user.getType());
        updateUser.setStatus(user.getStatus());
        userService.updateById(updateUser);
        return Result.success("修改成功");
    }

    @DeleteMapping("/delete/{id}")
    public Result<?> delete(@PathVariable Long id,
                            @RequestParam(required = false) Long userId,
                            HttpSession session) {
        User admin = getAdminUser(session, userId);
        if (admin == null) return Result.build(403, "无权限操作");
        if (id.equals(admin.getId())) return Result.error("不能删除自己");
        userService.removeById(id);
        return Result.success("删除成功");
    }

    @GetMapping("/profile")
    public Result<User> getProfile(@RequestParam(required = false) Long userId, HttpSession session) {
        Long uid = getValidUserId(session, userId);
        if (uid == null) return Result.build(401, "请先登录");
        User user = userService.getById(uid);
        if (user != null) user.setPassword(null);
        return user != null ? Result.success(user) : Result.error("用户不存在");
    }

    @PutMapping("/profile/update")
    public Result<?> updateProfile(@RequestBody User user,
                                   @RequestParam(required = false) Long userId,
                                   HttpSession session) {
        Long uid = getValidUserId(session, userId);
        if (uid == null) return Result.build(401, "请先登录");
        User existing = userService.getById(uid);
        if (existing == null) return Result.error("用户不存在");
        if (user.getNickname() != null) existing.setNickname(user.getNickname());
        if (user.getPhone() != null) existing.setPhone(user.getPhone());
        if (user.getAvatar() != null) existing.setAvatar(user.getAvatar());
        userService.updateById(existing);
        return Result.success("修改成功");
    }

    @PostMapping("/password/change")
    public Result<?> changePassword(@RequestBody java.util.Map<String, String> params,
                                    @RequestParam(required = false) Long userId,
                                    HttpSession session) {
        Long uid = getValidUserId(session, userId);
        if (uid == null) return Result.build(401, "请先登录");
        String oldPwd = params.get("oldPassword");
        String newPwd = params.get("newPassword");
        if (oldPwd == null || newPwd == null) return Result.paramError("密码不能为空");
        if (newPwd.length() < 6) return Result.paramError("新密码至少6位");
        User user = userService.getById(uid);
        if (user == null) return Result.error("用户不存在");
        if (!user.getPassword().equals(oldPwd)) return Result.error("旧密码错误");
        User upd = new User();
        upd.setId(uid);
        upd.setPassword(newPwd);
        userService.updateById(upd);
        return Result.success("密码修改成功");
    }

    private Long getValidUserId(HttpSession session, Long paramUserId) {
        User user = (User) session.getAttribute("loginUser");
        if (user != null) return user.getId();
        if (paramUserId != null && paramUserId > 0) return paramUserId;
        return null;
    }
}
