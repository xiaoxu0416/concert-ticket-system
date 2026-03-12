package com.concert.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.concert.entity.User;
import com.concert.vo.UserLoginVO;
import com.concert.vo.UserRegisterVO;
import com.concert.utils.Result;

/**
 * 用户服务接口
 */
public interface UserService extends IService<User> {
    /**
     * 用户注册
     */
    Result<?> register(UserRegisterVO vo);

    /**
     * 用户登录
     */
    Result<User> login(UserLoginVO vo);

    /**
     * 根据用户名查询用户
     */
    User getByUsername(String username);
}
