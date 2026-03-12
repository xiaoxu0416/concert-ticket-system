package com.concert.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.concert.entity.Order;
import com.concert.entity.User;
import com.concert.service.OrderService;
import com.concert.service.UserService;
import com.concert.vo.OrderPayVO;
import com.concert.vo.TicketBuyVO;
import com.concert.utils.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/order")
@RequiredArgsConstructor
@Slf4j
public class OrderController {
    private final OrderService orderService;
    private final UserService userService;

    private Long getValidUserId(HttpSession session, Long paramUserId) {
        User user = (User) session.getAttribute("loginUser");
        if (user != null) return user.getId();
        if (paramUserId == null || paramUserId <= 0) throw new IllegalStateException("请先登录");
        return paramUserId;
    }

    private User getAdminUser(HttpSession session, Long paramUserId) {
        User user = (User) session.getAttribute("loginUser");
        if (user != null && user.getType() == 1) return user;
        if (paramUserId != null && paramUserId > 0) {
            User dbUser = userService.getById(paramUserId);
            if (dbUser != null && dbUser.getType() == 1) return dbUser;
        }
        return null;
    }

    @PostMapping("/buy")
    public Result<?> buyTicket(@Validated @RequestBody TicketBuyVO vo, HttpSession session) {
        try {
            Long userId = getValidUserId(session, vo.getUserId());
            return orderService.buyTicket(vo, userId);
        } catch (IllegalStateException e) {
            return Result.build(401, e.getMessage());
        } catch (Exception e) {
            log.error("抢票接口异常", e);
            return Result.error("抢票失败，系统异常，请重试");
        }
    }

    @PostMapping("/pay/pre")
    public Result<?> prePay(@Validated @RequestBody OrderPayVO vo, HttpSession session) {
        try {
            Long userId = getValidUserId(session, vo.getUserId());
            return orderService.prePay(vo, userId);
        } catch (IllegalStateException e) {
            return Result.build(401, e.getMessage());
        } catch (Exception e) {
            log.error("预支付接口异常", e);
            return Result.error("生成支付二维码失败，请重试");
        }
    }

    @PostMapping("/pay")
    public Result<?> payOrder(@Validated @RequestBody OrderPayVO vo, HttpSession session) {
        try {
            Long userId = getValidUserId(session, vo.getUserId());
            return orderService.payOrder(vo, userId);
        } catch (IllegalStateException e) {
            return Result.build(401, e.getMessage());
        } catch (Exception e) {
            log.error("支付接口异常", e);
            return Result.error("支付失败，请重试");
        }
    }

    @PostMapping("/cancel")
    public Result<?> cancelOrder(@RequestBody Map<String, Object> params, HttpSession session) {
        String orderNo = (String) params.get("orderNo");
        String reason = params.get("reason") != null ? (String) params.get("reason") : "用户主动取消";
        Long userId = params.get("userId") != null ? Long.valueOf(params.get("userId").toString()) : null;
        if (orderNo == null || orderNo.isEmpty()) return Result.error("订单编号不能为空");
        try {
            Long validUserId = getValidUserId(session, userId);
            return orderService.cancelOrder(orderNo, validUserId, reason);
        } catch (IllegalStateException e) {
            return Result.build(401, e.getMessage());
        } catch (Exception e) {
            log.error("取消订单接口异常", e);
            return Result.error("取消订单失败，请重试");
        }
    }

    @GetMapping("/info/{orderNo}")
    public Result<?> getOrderInfo(@PathVariable String orderNo,
                                  @RequestParam(required = false) Long userId,
                                  HttpSession session) {
        try {
            Long validUserId = null;
            User user = (User) session.getAttribute("loginUser");
            if (user != null) validUserId = user.getId();
            else if (userId != null) validUserId = userId;
            return orderService.getOrderInfo(orderNo, validUserId);
        } catch (Exception e) {
            log.error("订单详情接口异常", e);
            return Result.error("查询订单详情失败，请重试");
        }
    }

    @GetMapping("/my")
    public Result<?> listMyOrder(@RequestParam(required = false) Long userId, HttpSession session) {
        try {
            Long validUserId = getValidUserId(session, userId);
            return orderService.listUserOrder(validUserId);
        } catch (IllegalStateException e) {
            return Result.build(401, e.getMessage());
        } catch (Exception e) {
            log.error("查询个人订单接口异常", e);
            return Result.error("查询订单失败，请重试");
        }
    }

    @GetMapping("/all")
    public Result<?> listAllOrder(@RequestParam(required = false) Long userId, HttpSession session) {
        try {
            User admin = getAdminUser(session, userId);
            if (admin == null) return Result.build(403, "无权限访问");
            return orderService.listAllOrder();
        } catch (Exception e) {
            log.error("查询所有订单接口异常", e);
            return Result.error("查询订单失败，请重试");
        }
    }

    @DeleteMapping("/delete/{id}")
    public Result<?> deleteOrder(@PathVariable Long id,
                                 @RequestParam(required = false) Long userId,
                                 HttpSession session) {
        try {
            User admin = getAdminUser(session, userId);
            if (admin == null) return Result.build(403, "无权限操作");
            orderService.removeById(id);
            return Result.success("删除成功");
        } catch (Exception e) {
            log.error("删除订单接口异常", e);
            return Result.error("删除订单失败");
        }
    }

    /**
     * 管理员退款（将已支付订单改为已退款状态4）
     */
    @PostMapping("/refund")
    public Result<?> refundOrder(@RequestBody Map<String, Object> params, HttpSession session) {
        try {
            Long adminUserId = params.get("adminUserId") != null ? Long.valueOf(params.get("adminUserId").toString()) : null;
            User admin = getAdminUser(session, adminUserId);
            if (admin == null) return Result.build(403, "无权限操作");

            String orderNo = (String) params.get("orderNo");
            if (orderNo == null || orderNo.isEmpty()) return Result.error("订单编号不能为空");

            LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Order::getOrderNo, orderNo);
            Order order = orderService.getOne(wrapper);
            if (order == null) return Result.error("订单不存在");
            if (order.getStatus() != 5) return Result.error("只有退款审核中的订单才能执行退款");

            order.setStatus(4);
            order.setUpdateTime(LocalDateTime.now());
            orderService.updateById(order);
            log.info("管理员{}审核通过订单{}的退款申请", admin.getId(), orderNo);
            return Result.success("退款审核通过");
        } catch (Exception e) {
            log.error("退款审核接口异常", e);
            return Result.error("退款审核失败");
        }
    }

    /**
     * 管理员拒绝退款（将退款审核中的订单恢复为已支付状态1）
     */
    @PostMapping("/refund/reject")
    public Result<?> rejectRefund(@RequestBody Map<String, Object> params, HttpSession session) {
        try {
            Long adminUserId = params.get("adminUserId") != null ? Long.valueOf(params.get("adminUserId").toString()) : null;
            User admin = getAdminUser(session, adminUserId);
            if (admin == null) return Result.build(403, "无权限操作");

            String orderNo = (String) params.get("orderNo");
            if (orderNo == null || orderNo.isEmpty()) return Result.error("订单编号不能为空");

            LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Order::getOrderNo, orderNo);
            Order order = orderService.getOne(wrapper);
            if (order == null) return Result.error("订单不存在");
            if (order.getStatus() != 5) return Result.error("只有退款审核中的订单才能拒绝");

            order.setStatus(1);
            order.setUpdateTime(LocalDateTime.now());
            orderService.updateById(order);
            log.info("管理员{}拒绝订单{}的退款申请", admin.getId(), orderNo);
            return Result.success("已拒绝退款申请");
        } catch (Exception e) {
            log.error("拒绝退款接口异常", e);
            return Result.error("操作失败");
        }
    }

    /**
     * 用户申请退款（前台用户自己申请，状态改为5-退款审核中）
     */
    @PostMapping("/refund/apply")
    public Result<?> applyRefund(@RequestBody Map<String, Object> params, HttpSession session) {
        try {
            Long userId = params.get("userId") != null ? Long.valueOf(params.get("userId").toString()) : null;
            Long validUserId = getValidUserId(session, userId);
            String orderNo = (String) params.get("orderNo");
            if (orderNo == null || orderNo.isEmpty()) return Result.error("订单编号不能为空");

            LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Order::getOrderNo, orderNo).eq(Order::getUserId, validUserId);
            Order order = orderService.getOne(wrapper);
            if (order == null) return Result.error("订单不存在");
            if (order.getStatus() != 1) return Result.error("只有已支付订单才能申请退款");

            order.setStatus(5);
            order.setUpdateTime(LocalDateTime.now());
            orderService.updateById(order);
            log.info("用户{}对订单{}提交退款申请，等待管理员审核", validUserId, orderNo);
            return Result.success("退款申请已提交，请等待管理员审核");
        } catch (IllegalStateException e) {
            return Result.build(401, e.getMessage());
        } catch (Exception e) {
            log.error("用户退款接口异常", e);
            return Result.error("退款失败");
        }
    }

    /**
     * 用户删除历史订单（仅允许删除已支付/已取消/已退款的订单）
     */
    @DeleteMapping("/my/delete/{id}")
    public Result<?> deleteMyOrder(@PathVariable Long id,
                                   @RequestParam(required = false) Long userId,
                                   HttpSession session) {
        try {
            Long validUserId = getValidUserId(session, userId);
            Order order = orderService.getById(id);
            if (order == null) return Result.error("订单不存在");
            if (!order.getUserId().equals(validUserId)) return Result.build(403, "无权操作他人订单");
            if (order.getStatus() == 0 || order.getStatus() == 3) return Result.error("待支付或创建中的订单不能删除");
            orderService.removeById(id);
            log.info("用户{}删除历史订单，ID：{}", validUserId, id);
            return Result.success("删除成功");
        } catch (IllegalStateException e) {
            return Result.build(401, e.getMessage());
        } catch (Exception e) {
            log.error("删除历史订单异常", e);
            return Result.error("删除订单失败");
        }
    }

    @GetMapping("/ticket/{orderNo}")
    public Result<?> getTicket(@PathVariable String orderNo,
                               @RequestParam(required = false) Long userId,
                               HttpSession session) {
        try {
            Long validUserId = getValidUserId(session, userId);
            LambdaQueryWrapper<Order> w = new LambdaQueryWrapper<>();
            w.eq(Order::getOrderNo, orderNo);
            Order order = orderService.getOne(w);
            if (order == null) return Result.error("订单不存在");
            if (!order.getUserId().equals(validUserId)) return Result.build(403, "无权查看");
            if (order.getStatus() != 1 && order.getStatus() != 6) return Result.error("仅已支付或已核销订单可查看电子票");
            java.util.Map<String, Object> ticket = new java.util.HashMap<>();
            ticket.put("orderNo", order.getOrderNo());
            ticket.put("concertId", order.getConcertId());
            ticket.put("sessionId", order.getSessionId());
            ticket.put("seatAreaName", order.getSeatAreaName());
            ticket.put("ticketNum", order.getTicketNum());
            ticket.put("totalAmount", order.getTotalAmount());
            ticket.put("payTime", order.getPayTime());
            ticket.put("status", order.getStatus());
            ticket.put("verifyCode", order.getVerifyCode());
            ticket.put("qrContent", "TICKET:" + order.getOrderNo() + ":USER:" + validUserId + ":CODE:" + order.getVerifyCode());
            return Result.success(ticket);
        } catch (IllegalStateException e) {
            return Result.build(401, e.getMessage());
        } catch (Exception e) {
            log.error("获取电子票异常", e);
            return Result.error("获取电子票失败");
        }
    }

    /**
     * 管理员核销门票
     */
    @PostMapping("/verify")
    public Result<?> verifyTicket(@RequestBody Map<String, Object> params, HttpSession session) {
        try {
            Long adminUserId = params.get("adminUserId") != null ? Long.valueOf(params.get("adminUserId").toString()) : null;
            User admin = getAdminUser(session, adminUserId);
            if (admin == null) return Result.build(403, "无权限操作");

            String verifyCode = (String) params.get("verifyCode");
            if (verifyCode == null || verifyCode.trim().isEmpty()) return Result.error("核销码不能为空");

            return orderService.verifyTicket(verifyCode);
        } catch (Exception e) {
            log.error("核销门票异常", e);
            return Result.error("核销失败");
        }
    }
}
