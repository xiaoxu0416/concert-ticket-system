//package com.concert.utils;
//
//import com.github.yitter.contract.IdGeneratorOptions;
//import com.github.yitter.idgen.YitIdHelper;
//
///**
// * 雪花算法ID生成工具（订单编号）
// */
//public class IdGeneratorUtil {
//    static {
//        // 初始化雪花算法
//        IdGeneratorOptions options = new IdGeneratorOptions();
//        options.WorkerId = 1; // 工作节点ID（单机设1即可）
//        YitIdHelper.setIdGenerator(options);
//    }
//
//    /**
//     * 生成唯一订单编号
//     */
//    public static String generateOrderNo() {
//        return String.valueOf(YitIdHelper.nextId());
//    }
//}