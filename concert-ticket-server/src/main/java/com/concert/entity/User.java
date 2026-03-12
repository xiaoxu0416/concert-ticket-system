package com.concert.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户实体类（毕设：抢票系统用户模块）
 * 优化版：适配自增ID、自动填充时间、补充状态字段、统一常量
 */
@Data
@TableName("sys_user")
public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    // ========== 常量定义（毕设规范：避免硬编码） ==========
    // 用户类型常量
    public static final Integer USER_TYPE_NORMAL = 0;   // 普通用户
    public static final Integer USER_TYPE_ADMIN = 1;    // 管理员
    // 用户状态常量（毕设扩展：增加账号管控能力）
    public static final Integer USER_STATUS_ENABLE = 0; // 启用（默认）
    public static final Integer USER_STATUS_DISABLE = 1;// 禁用

    /**
     * 用户ID（主键）
     * 毕设适配：改为自增策略，和数据库初始化SQL（id=1/2）保持一致
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户名（登录账号，唯一）
     */
    private String username;

    /**
     * 密码（毕设简化：未加密，生产环境需用BCrypt加密）
     */
    private String password;

    /**
     * 昵称（毕设扩展：提升用户模块完整性）
     */
    private String nickname;

    /**
     * 手机号（唯一，用于登录/验证）
     */
    private String phone;

    /**
     * 澶村儚URL
     */
    private String avatar;

    /**
     * 用户类型：0-普通用户 1-管理员
     * 参考常量：USER_TYPE_NORMAL/USER_TYPE_ADMIN
     */
    private Integer type;

    /**
     * 用户状态：0-启用 1-禁用（毕设扩展：账号管控）
     * 参考常量：USER_STATUS_ENABLE/USER_STATUS_DISABLE
     */
    private Integer status;

    /**
     * 创建时间（MyBatis-Plus自动填充，无需手动赋值）
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间（MyBatis-Plus自动填充，插入/更新时赋值）
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}