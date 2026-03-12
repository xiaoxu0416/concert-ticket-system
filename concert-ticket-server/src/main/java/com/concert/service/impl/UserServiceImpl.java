package com.concert.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.concert.entity.User;
import com.concert.mapper.UserMapper;
import com.concert.service.UserService;
import com.concert.vo.UserLoginVO;
import com.concert.vo.UserRegisterVO;
import com.concert.utils.Result;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

/**
 * 用户服务实现类
 */
@Service
@RequiredArgsConstructor
@Mapper
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    private final UserMapper userMapper;

    @Override
    public Result<?> register(UserRegisterVO vo) {
        // 校验用户名是否存在
        User existUser = getByUsername(vo.getUsername());
        if (existUser != null) {
            return Result.error("用户名已存在");
        }
        // 构建用户对象
        User user = new User();
        user.setUsername(vo.getUsername());
        user.setPassword(vo.getPassword()); // 毕设简化，未加密
        user.setPhone(vo.getPhone());
        user.setType(0); // 普通用户
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        // 保存用户
        boolean save = save(user);
        return save ? Result.success() : Result.error("注册失败");
    }

    @Override
    public Result<User> login(UserLoginVO vo) {
        // 查询用户
        User user = getByUsername(vo.getUsername());
        if (user == null) {
            return Result.error("用户名或密码错误");
        }
        // 校验密码（毕设简化）
        if (!vo.getPassword().equals(user.getPassword())) {
            return Result.error("用户名或密码错误");
        }
        return Result.success(user);
    }

    @Override
    public User getByUsername(String username) {
        if (!StringUtils.hasText(username)) {
            return null;
        }
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        return userMapper.selectOne(wrapper);
    }
}
