package com.concert.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 演唱会实体类
 * 关联表：concert_info
 * 毕设优化版：适配自增ID、自动填充时间、统一状态常量
 */
@Data
@TableName("concert_info")
public class Concert implements Serializable {
    private static final long serialVersionUID = 1L;

    // ========== 状态常量（毕设重点：统一管理，避免硬编码） ==========
    public static final Integer STATUS_UNSHELVE = 0;  // 未上架
    public static final Integer STATUS_SHELVE = 1;    // 已上架

    /**
     * 演唱会ID（主键）
     * 毕设适配：改为自增策略，和数据库初始化SQL（id=1/2/3/4）保持一致
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 演唱会名称（如：嘉年华世界巡回演唱会）
     */
    private String name;

    /**
     * 歌手/乐队名称（如：周杰伦、五月天）
     */
    private String singer;

    /**
     * 演出城市（如：北京、上海）
     */
    private String city;

    /**
     * 演出场馆（如：鸟巢国家体育场、上海体育场）
     */
    private String venue;

    /**
     * 演唱会海报（毕设简化方案：存储图片相对路径/URL，如：/images/default-poster.jpg）
     */
    private String poster;

    /**
     * 状态：0-未上架 1-已上架
     * 参考常量：STATUS_UNSHELVE/STATUS_SHELVE
     */
    private Integer status;

    /**
     * 创建时间（MyBatis-Plus自动填充，无需手动赋值）
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间（MyBatis-Plus自动填充，插入/更新时自动赋值）
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}