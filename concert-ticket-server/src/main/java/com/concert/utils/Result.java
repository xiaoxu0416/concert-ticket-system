package com.concert.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import java.io.Serializable;

/**
 * 全局统一返回结果
 * 状态码规范：
 * 200-操作成功  500-系统异常
 * 400-参数错误  401-未登录    403-无权限
 * 429-请求限流  404-资源不存在
 */
@Data
// 核心修复：序列化时忽略null字段，避免空data引发Jackson异常
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Result<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    // 状态码常量（保留原有）
    public static final Integer SUCCESS_CODE = 200;       // 成功
    public static final Integer ERROR_CODE = 500;         // 系统异常
    public static final Integer PARAM_ERROR_CODE = 400;   // 参数错误
    public static final Integer NO_LOGIN_CODE = 401;      // 未登录
    public static final Integer NO_PERMISSION_CODE = 403; // 无权限
    public static final Integer LIMIT_CODE = 429;         // 请求限流
    public static final Integer NOT_FOUND_CODE = 404;     // 资源不存在

    private Integer code; // 状态码
    private String msg;   // 提示信息
    private T data;      // 返回数据

    // 核心修复：构造方法不再初始化空Object，让data默认null
    private Result() {}

    // 成功（无数据，默认提示）
    public static <T> Result<T> success() {
        Result<T> result = new Result<>();
        result.setCode(SUCCESS_CODE);
        result.setMsg("操作成功");
        return result;
    }

    // 成功（有数据，默认提示）
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(SUCCESS_CODE);
        result.setMsg("操作成功");
        result.setData(data);
        return result;
    }

    // 成功（自定义提示+数据，毕设常用）
    public static <T> Result<T> success(String msg, T data) {
        Result<T> result = new Result<>();
        result.setCode(SUCCESS_CODE);
        result.setMsg(msg);
        result.setData(data);
        return result;
    }

    // 失败（默认500码，自定义提示）
    public static <T> Result<T> error(String msg) {
        Result<T> result = new Result<>();
        result.setCode(ERROR_CODE);
        result.setMsg(msg);
        return result;
    }

    // 指定状态码的失败方法（比如参数错误400）
    public static <T> Result<T> error(Integer code, String msg) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMsg(msg);
        return result;
    }

    // 自定义状态码和信息（无数据）
    public static <T> Result<T> build(Integer code, String msg) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMsg(msg);
        return result;
    }

    // 自定义全参数（有数据）
    public static <T> Result<T> build(Integer code, String msg, T data) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMsg(msg);
        result.setData(data);
        return result;
    }

    // 常用快捷方法（保留原有）
    /**
     * 参数错误快捷返回
     */
    public static <T> Result<T> paramError(String msg) {
        return error(PARAM_ERROR_CODE, msg);
    }

    /**
     * 请求限流快捷返回
     */
    public static <T> Result<T> limitError(String msg) {
        return error(LIMIT_CODE, msg);
    }

    /**
     * 资源不存在快捷返回
     */
    public static <T> Result<T> notFoundError(String msg) {
        return error(NOT_FOUND_CODE, msg);
    }
}