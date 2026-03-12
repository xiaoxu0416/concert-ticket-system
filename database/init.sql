-- ============================================================
-- Concert Ticket System - Database Init Script
-- Database: concert_ticket
-- ============================================================

CREATE DATABASE IF NOT EXISTS `concert_ticket` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

USE `concert_ticket`;

-- -----------------------------------------------------------
-- Table: sys_user
-- -----------------------------------------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'User ID',
  `username` varchar(50) NOT NULL COMMENT 'Username',
  `password` varchar(100) NOT NULL COMMENT 'Password',
  `nickname` varchar(50) DEFAULT NULL COMMENT 'Nickname',
  `phone` varchar(20) DEFAULT NULL COMMENT 'Phone',
  `type` int NOT NULL DEFAULT 0 COMMENT 'User type: 0-normal 1-admin',
  `avatar` varchar(500) DEFAULT NULL COMMENT 'Avatar URL',
  `status` int NOT NULL DEFAULT 0 COMMENT 'Status: 0-enabled 1-disabled',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='User table';

-- -----------------------------------------------------------
-- Table: concert_info
-- -----------------------------------------------------------
DROP TABLE IF EXISTS `concert_info`;
CREATE TABLE `concert_info` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'Concert ID',
  `name` varchar(200) NOT NULL COMMENT 'Concert name',
  `singer` varchar(100) NOT NULL COMMENT 'Singer/Band name',
  `city` varchar(50) DEFAULT NULL COMMENT 'City',
  `venue` varchar(200) DEFAULT NULL COMMENT 'Venue',
  `poster` varchar(500) DEFAULT NULL COMMENT 'Poster URL',
  `status` int NOT NULL DEFAULT 0 COMMENT 'Status: 0-unlisted 1-listed',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Concert table';

-- -----------------------------------------------------------
-- Table: concert_session
-- -----------------------------------------------------------
DROP TABLE IF EXISTS `concert_session`;
CREATE TABLE `concert_session` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'Session ID',
  `concert_id` bigint NOT NULL COMMENT 'Concert ID (FK)',
  `show_time` datetime NOT NULL COMMENT 'Show time',
  `price` decimal(10,2) NOT NULL COMMENT 'Ticket price',
  `total_stock` int NOT NULL DEFAULT 0 COMMENT 'Total stock',
  `surplus_stock` int NOT NULL DEFAULT 0 COMMENT 'Remaining stock',
  `open_time` datetime DEFAULT NULL COMMENT 'Ticket sale open time',
  `status` int NOT NULL DEFAULT 0 COMMENT 'Status: 0-not open 1-selling 2-sold out 3-finished',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
  PRIMARY KEY (`id`),
  KEY `idx_concert_id` (`concert_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Concert session table';

-- -----------------------------------------------------------
-- Table: concert_order
-- -----------------------------------------------------------
DROP TABLE IF EXISTS `concert_order`;
CREATE TABLE `concert_order` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'Order ID',
  `order_no` varchar(50) DEFAULT NULL COMMENT 'Order number (unique)',
  `user_id` bigint NOT NULL COMMENT 'User ID',
  `concert_id` bigint DEFAULT NULL COMMENT 'Concert ID',
  `session_id` bigint NOT NULL COMMENT 'Session ID',
  `seat_area_id` bigint DEFAULT NULL COMMENT 'Seat area ID',
  `seat_area_name` varchar(100) DEFAULT NULL COMMENT 'Seat area name',
  `ticket_num` int NOT NULL DEFAULT 1 COMMENT 'Ticket quantity',
  `total_amount` decimal(10,2) NOT NULL DEFAULT 0.00 COMMENT 'Total amount',
  `status` int NOT NULL DEFAULT 0 COMMENT 'Status: 0-unpaid 1-paid 2-cancelled 3-creating 4-refunded 5-refund_reviewing 6-verified',
  `pay_time` datetime DEFAULT NULL COMMENT 'Pay time',
  `verify_code` varchar(20) DEFAULT NULL COMMENT 'Ticket verify code',
  `verify_time` datetime DEFAULT NULL COMMENT 'Ticket verify time',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_session_id` (`session_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Order table';

-- -----------------------------------------------------------
-- Table: concert_stock_log
-- -----------------------------------------------------------
DROP TABLE IF EXISTS `concert_stock_log`;
CREATE TABLE `concert_stock_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'Log ID',
  `session_id` bigint NOT NULL COMMENT 'Session ID',
  `order_no` varchar(50) DEFAULT NULL COMMENT 'Related order number',
  `operate_type` int NOT NULL COMMENT 'Operate type: 0-preheat 1-deduct 2-rollback',
  `before_stock` int DEFAULT NULL COMMENT 'Stock before operation',
  `operate_num` int DEFAULT NULL COMMENT 'Operate quantity',
  `after_stock` int DEFAULT NULL COMMENT 'Stock after operation',
  `operator` varchar(50) DEFAULT NULL COMMENT 'Operator',
  `remark` varchar(200) DEFAULT NULL COMMENT 'Remark',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
  PRIMARY KEY (`id`),
  KEY `idx_session_id` (`session_id`),
  KEY `idx_order_no` (`order_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Stock operation log table';

-- -----------------------------------------------------------
-- Table: seat_area
-- -----------------------------------------------------------
DROP TABLE IF EXISTS `seat_area`;
CREATE TABLE `seat_area` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'Seat Area ID',
  `session_id` bigint NOT NULL COMMENT 'Session ID (FK)',
  `area_name` varchar(100) NOT NULL COMMENT 'Area name',
  `price` decimal(10,2) NOT NULL COMMENT 'Area ticket price',
  `total_stock` int NOT NULL DEFAULT 0 COMMENT 'Total seats',
  `surplus_stock` int NOT NULL DEFAULT 0 COMMENT 'Remaining seats',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
  PRIMARY KEY (`id`),
  KEY `idx_session_id` (`session_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Seat area table';

-- -----------------------------------------------------------
-- Table: announcement
-- -----------------------------------------------------------
DROP TABLE IF EXISTS `announcement`;
CREATE TABLE `announcement` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'Announcement ID',
  `title` varchar(200) NOT NULL COMMENT 'Title',
  `content` text COMMENT 'Content',
  `image` varchar(500) DEFAULT NULL COMMENT 'Image URL',
  `type` int NOT NULL DEFAULT 0 COMMENT 'Type: 0-notice 1-banner',
  `status` int NOT NULL DEFAULT 1 COMMENT 'Status: 0-hidden 1-visible',
  `sort_order` int NOT NULL DEFAULT 0 COMMENT 'Sort order',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Announcement table';

-- -----------------------------------------------------------
-- Table: favorite
-- -----------------------------------------------------------
DROP TABLE IF EXISTS `favorite`;
CREATE TABLE `favorite` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'Favorite ID',
  `user_id` bigint NOT NULL COMMENT 'User ID',
  `concert_id` bigint NOT NULL COMMENT 'Concert ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_concert` (`user_id`, `concert_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Favorite table';

-- -----------------------------------------------------------
-- Table: review
-- -----------------------------------------------------------
DROP TABLE IF EXISTS `review`;
CREATE TABLE `review` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'Review ID',
  `user_id` bigint NOT NULL COMMENT 'User ID',
  `concert_id` bigint NOT NULL COMMENT 'Concert ID',
  `rating` int NOT NULL DEFAULT 5 COMMENT 'Rating 1-5',
  `content` varchar(500) DEFAULT NULL COMMENT 'Review content',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
  PRIMARY KEY (`id`),
  KEY `idx_concert_id` (`concert_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Review table';

-- ============================================================
-- Init Data
-- ============================================================

-- Admin account: admin / admin123
INSERT INTO `sys_user` (`username`, `password`, `nickname`, `phone`, `type`, `status`) VALUES
('admin', 'admin123', '系统管理员', '13800000000', 1, 0);

-- Test user accounts
INSERT INTO `sys_user` (`username`, `password`, `nickname`, `phone`, `type`, `status`) VALUES
('zhangsan', '123456', '张三', '13912345678', 0, 0),
('lisi',     '123456', '李四', '13856781234', 0, 0),
('wangwu',   '123456', '王五', '13767890123', 0, 0);

-- Concert data (Unsplash images, realistic Chinese names)
INSERT INTO `concert_info` (`name`, `singer`, `city`, `venue`, `poster`, `status`) VALUES
('周杰伦「嘉年华」世界巡回演唱会 北京站', '周杰伦', '北京', '国家体育场（鸟巢）',
 'https://images.unsplash.com/photo-1493225457124-a3eb161ffa5f?w=600&h=800&fit=crop', 1),
('五月天「人生无限公司」巡回演唱会 上海站', '五月天', '上海', '上海体育场',
 'https://images.unsplash.com/photo-1459749411175-04bf5292ceea?w=600&h=800&fit=crop', 1),
('陈奕迅「Fear and Dreams」巡回演唱会 广州站', '陈奕迅', '广州', '天河体育中心',
 'https://images.unsplash.com/photo-1501386761578-0a55c2ca0e8a?w=600&h=800&fit=crop', 1),
('林俊杰「圣所3.0」世界巡回演唱会 深圳站', '林俊杰', '深圳', '深圳湾体育中心',
 'https://images.unsplash.com/photo-1470229722913-7c0e2dbbafd3?w=600&h=800&fit=crop', 1),
('薛之谦「天外来物」巡回演唱会 成都站', '薛之谦', '成都', '凤凰山体育公园',
 'https://images.unsplash.com/photo-1524368535928-5b5e00ddc76b?w=600&h=800&fit=crop', 1),
('Taylor Swift The Eras Tour 东京站', 'Taylor Swift', '东京', '东京巨蛋',
 'https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=600&h=800&fit=crop', 1);

-- Session data (dynamic time: relative to NOW())
-- Concert 1: 周杰伦 - 2 sessions
INSERT INTO `concert_session` (`concert_id`, `show_time`, `price`, `total_stock`, `surplus_stock`, `open_time`, `status`) VALUES
(1, DATE_ADD(NOW(), INTERVAL 30 DAY) + INTERVAL '19:30' HOUR_MINUTE,  1280.00, 5000, 4200, DATE_ADD(NOW(), INTERVAL -5 DAY), 1),
(1, DATE_ADD(NOW(), INTERVAL 31 DAY) + INTERVAL '19:30' HOUR_MINUTE,  1280.00, 5000, 5000, DATE_ADD(NOW(), INTERVAL -5 DAY), 1);

-- Concert 2: 五月天 - 3 sessions
INSERT INTO `concert_session` (`concert_id`, `show_time`, `price`, `total_stock`, `surplus_stock`, `open_time`, `status`) VALUES
(2, DATE_ADD(NOW(), INTERVAL 45 DAY) + INTERVAL '19:30' HOUR_MINUTE,  980.00, 3000, 2500, DATE_ADD(NOW(), INTERVAL -3 DAY), 1),
(2, DATE_ADD(NOW(), INTERVAL 46 DAY) + INTERVAL '19:30' HOUR_MINUTE,  980.00, 3000, 3000, DATE_ADD(NOW(), INTERVAL -3 DAY), 1),
(2, DATE_ADD(NOW(), INTERVAL 47 DAY) + INTERVAL '19:30' HOUR_MINUTE,  980.00, 3000, 3000, DATE_ADD(NOW(), INTERVAL -3 DAY), 1);

-- Concert 3: 陈奕迅 - 2 sessions
INSERT INTO `concert_session` (`concert_id`, `show_time`, `price`, `total_stock`, `surplus_stock`, `open_time`, `status`) VALUES
(3, DATE_ADD(NOW(), INTERVAL 60 DAY) + INTERVAL '20:00' HOUR_MINUTE,  880.00, 2000, 1800, DATE_ADD(NOW(), INTERVAL -1 DAY), 1),
(3, DATE_ADD(NOW(), INTERVAL 61 DAY) + INTERVAL '20:00' HOUR_MINUTE,  880.00, 2000, 2000, DATE_ADD(NOW(), INTERVAL -1 DAY), 1);

-- Concert 4: 林俊杰 - 2 sessions
INSERT INTO `concert_session` (`concert_id`, `show_time`, `price`, `total_stock`, `surplus_stock`, `open_time`, `status`) VALUES
(4, DATE_ADD(NOW(), INTERVAL 75 DAY) + INTERVAL '19:30' HOUR_MINUTE,  1080.00, 1500, 1500, DATE_ADD(NOW(), INTERVAL 10 DAY), 1),
(4, DATE_ADD(NOW(), INTERVAL 76 DAY) + INTERVAL '19:30' HOUR_MINUTE,  1080.00, 1500, 1500, DATE_ADD(NOW(), INTERVAL 10 DAY), 1);

-- Concert 5: 薛之谦 - 2 sessions
INSERT INTO `concert_session` (`concert_id`, `show_time`, `price`, `total_stock`, `surplus_stock`, `open_time`, `status`) VALUES
(5, DATE_ADD(NOW(), INTERVAL 90 DAY) + INTERVAL '19:30' HOUR_MINUTE,  680.00, 2500, 2500, DATE_ADD(NOW(), INTERVAL 15 DAY), 1),
(5, DATE_ADD(NOW(), INTERVAL 91 DAY) + INTERVAL '19:30' HOUR_MINUTE,  680.00, 2500, 2500, DATE_ADD(NOW(), INTERVAL 15 DAY), 1);

-- Concert 6: Taylor Swift - 3 sessions
INSERT INTO `concert_session` (`concert_id`, `show_time`, `price`, `total_stock`, `surplus_stock`, `open_time`, `status`) VALUES
(6, DATE_ADD(NOW(), INTERVAL 120 DAY) + INTERVAL '18:00' HOUR_MINUTE, 1580.00, 4000, 3500, DATE_ADD(NOW(), INTERVAL 30 DAY), 1),
(6, DATE_ADD(NOW(), INTERVAL 121 DAY) + INTERVAL '18:00' HOUR_MINUTE, 1580.00, 4000, 4000, DATE_ADD(NOW(), INTERVAL 30 DAY), 1),
(6, DATE_ADD(NOW(), INTERVAL 122 DAY) + INTERVAL '18:00' HOUR_MINUTE, 1580.00, 4000, 4000, DATE_ADD(NOW(), INTERVAL 30 DAY), 1);

-- Seat area data (for each session)
-- Session 1 (周杰伦 Day1)
INSERT INTO `seat_area` (`session_id`, `area_name`, `price`, `total_stock`, `surplus_stock`) VALUES
(1, 'VIP内场', 2580.00, 500, 350),
(1, 'A区看台', 1680.00, 1500, 1300),
(1, 'B区看台', 1280.00, 1500, 1250),
(1, 'C区看台', 880.00,  1500, 1300);

-- Session 2 (周杰伦 Day2)
INSERT INTO `seat_area` (`session_id`, `area_name`, `price`, `total_stock`, `surplus_stock`) VALUES
(2, 'VIP内场', 2580.00, 500, 500),
(2, 'A区看台', 1680.00, 1500, 1500),
(2, 'B区看台', 1280.00, 1500, 1500),
(2, 'C区看台', 880.00,  1500, 1500);

-- Session 3 (五月天 Day1)
INSERT INTO `seat_area` (`session_id`, `area_name`, `price`, `total_stock`, `surplus_stock`) VALUES
(3, 'VIP内场', 1880.00, 300, 200),
(3, 'A区看台', 1280.00, 900, 780),
(3, 'B区看台', 980.00,  900, 800),
(3, 'C区看台', 580.00,  900, 720);

-- Session 4 (五月天 Day2)
INSERT INTO `seat_area` (`session_id`, `area_name`, `price`, `total_stock`, `surplus_stock`) VALUES
(4, 'VIP内场', 1880.00, 300, 300),
(4, 'A区看台', 1280.00, 900, 900),
(4, 'B区看台', 980.00,  900, 900),
(4, 'C区看台', 580.00,  900, 900);

-- Session 5 (五月天 Day3)
INSERT INTO `seat_area` (`session_id`, `area_name`, `price`, `total_stock`, `surplus_stock`) VALUES
(5, 'VIP内场', 1880.00, 300, 300),
(5, 'A区看台', 1280.00, 900, 900),
(5, 'B区看台', 980.00,  900, 900),
(5, 'C区看台', 580.00,  900, 900);

-- Session 6 (陈奕迅 Day1)
INSERT INTO `seat_area` (`session_id`, `area_name`, `price`, `total_stock`, `surplus_stock`) VALUES
(6, 'VIP前排', 1680.00, 200, 150),
(6, '内场站区', 1280.00, 600, 550),
(6, '看台区',   880.00, 1200, 1100);

-- Session 7 (陈奕迅 Day2)
INSERT INTO `seat_area` (`session_id`, `area_name`, `price`, `total_stock`, `surplus_stock`) VALUES
(7, 'VIP前排', 1680.00, 200, 200),
(7, '内场站区', 1280.00, 600, 600),
(7, '看台区',   880.00, 1200, 1200);

-- Session 8 (林俊杰 Day1)
INSERT INTO `seat_area` (`session_id`, `area_name`, `price`, `total_stock`, `surplus_stock`) VALUES
(8, 'VIP内场', 1980.00, 200, 200),
(8, 'A区',     1380.00, 400, 400),
(8, 'B区',     1080.00, 450, 450),
(8, 'C区',      780.00, 450, 450);

-- Session 9 (林俊杰 Day2)
INSERT INTO `seat_area` (`session_id`, `area_name`, `price`, `total_stock`, `surplus_stock`) VALUES
(9, 'VIP内场', 1980.00, 200, 200),
(9, 'A区',     1380.00, 400, 400),
(9, 'B区',     1080.00, 450, 450),
(9, 'C区',      780.00, 450, 450);

-- Session 10 (薛之谦 Day1)
INSERT INTO `seat_area` (`session_id`, `area_name`, `price`, `total_stock`, `surplus_stock`) VALUES
(10, 'VIP区',   1280.00, 300, 300),
(10, '内场区',   880.00, 700, 700),
(10, '看台A区',  680.00, 750, 750),
(10, '看台B区',  480.00, 750, 750);

-- Session 11 (薛之谦 Day2)
INSERT INTO `seat_area` (`session_id`, `area_name`, `price`, `total_stock`, `surplus_stock`) VALUES
(11, 'VIP区',   1280.00, 300, 300),
(11, '内场区',   880.00, 700, 700),
(11, '看台A区',  680.00, 750, 750),
(11, '看台B区',  480.00, 750, 750);

-- Session 12-14 (Taylor Swift)
INSERT INTO `seat_area` (`session_id`, `area_name`, `price`, `total_stock`, `surplus_stock`) VALUES
(12, 'VIP Floor',  3280.00, 400, 300),
(12, 'Lower Bowl', 2180.00, 1200, 1100),
(12, 'Upper Bowl', 1580.00, 1200, 1050),
(12, 'Nosebleed',  980.00,  1200, 1050);

INSERT INTO `seat_area` (`session_id`, `area_name`, `price`, `total_stock`, `surplus_stock`) VALUES
(13, 'VIP Floor',  3280.00, 400, 400),
(13, 'Lower Bowl', 2180.00, 1200, 1200),
(13, 'Upper Bowl', 1580.00, 1200, 1200),
(13, 'Nosebleed',  980.00,  1200, 1200);

INSERT INTO `seat_area` (`session_id`, `area_name`, `price`, `total_stock`, `surplus_stock`) VALUES
(14, 'VIP Floor',  3280.00, 400, 400),
(14, 'Lower Bowl', 2180.00, 1200, 1200),
(14, 'Upper Bowl', 1580.00, 1200, 1200),
(14, 'Nosebleed',  980.00,  1200, 1200);

-- Announcement data
INSERT INTO `announcement` (`title`, `content`, `image`, `type`, `status`, `sort_order`) VALUES
('周杰伦演唱会即将开售', '周杰伦「嘉年华」世界巡回演唱会北京站门票即将开售，敬请期待！',
 'https://images.unsplash.com/photo-1540039155733-5bb30b53aa14?w=1200&h=400&fit=crop', 1, 1, 1),
('五月天回归！三场连唱', '五月天「人生无限公司」上海站连开三场，粉丝们准备好了吗？',
 'https://images.unsplash.com/photo-1429962714451-bb934ecdc4ec?w=1200&h=400&fit=crop', 1, 1, 2),
('Taylor Swift 亚洲巡演确认', 'The Eras Tour 东京站正式确认，全球最火巡演来了！',
 'https://images.unsplash.com/photo-1516450360452-9312f5e86fc7?w=1200&h=400&fit=crop', 1, 1, 3),
('购票须知：实名制入场', '自即日起所有演唱会实行实名制购票，请使用真实信息购票。', NULL, 0, 1, 1),
('退票政策更新', '演出前48小时内不支持退票，请合理安排时间。', NULL, 0, 1, 2);

-- Review data
INSERT INTO `review` (`user_id`, `concert_id`, `rating`, `content`, `create_time`) VALUES
(2, 1, 5, '周杰伦永远是YYDS，期待这次演唱会！', DATE_SUB(NOW(), INTERVAL 3 DAY)),
(3, 1, 5, '已经抢到票了，太开心了！鸟巢见！', DATE_SUB(NOW(), INTERVAL 2 DAY)),
(4, 2, 4, '五月天的现场真的太燃了，每次都不会失望', DATE_SUB(NOW(), INTERVAL 5 DAY)),
(2, 2, 5, '人生无限公司！五月天最好的巡演之一', DATE_SUB(NOW(), INTERVAL 4 DAY)),
(3, 3, 5, 'Eason的live实力不用说，声音太好听了', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(4, 4, 4, '林俊杰的圣所系列一直很棒，必须支持', DATE_SUB(NOW(), INTERVAL 6 DAY)),
(2, 5, 5, '薛之谦的演唱会很有趣，互动超多', DATE_SUB(NOW(), INTERVAL 2 DAY)),
(3, 6, 5, 'Taylor终于来亚洲了！必须去！', DATE_SUB(NOW(), INTERVAL 1 DAY));

-- ============================================================
-- Order data (dynamic time, various statuses)
-- Status: 0-待支付 1-已支付 2-已取消 3-创建中 4-已退款 5-退款审核中 6-已核销
-- ============================================================

-- 张三(userId=2) 的订单：6条
INSERT INTO `concert_order` (`order_no`, `user_id`, `concert_id`, `session_id`, `seat_area_id`, `seat_area_name`, `ticket_num`, `total_amount`, `status`, `pay_time`, `create_time`, `verify_code`) VALUES
-- 已支付：周杰伦VIP内场 2张
(CONCAT('ORD', DATE_FORMAT(NOW(),'%Y%m%d'), '100001'), 2, 1, 1, 1, 'VIP内场', 2, 5160.00, 1,
 DATE_SUB(NOW(), INTERVAL 7 DAY) + INTERVAL '10:23' HOUR_MINUTE,
 DATE_SUB(NOW(), INTERVAL 7 DAY) + INTERVAL '10:20' HOUR_MINUTE, 'VK3M7N2P'),
-- 已支付：五月天A区 3张
(CONCAT('ORD', DATE_FORMAT(NOW(),'%Y%m%d'), '100002'), 2, 2, 3, 6, 'A区看台', 3, 3840.00, 1,
 DATE_SUB(NOW(), INTERVAL 5 DAY) + INTERVAL '14:05' HOUR_MINUTE,
 DATE_SUB(NOW(), INTERVAL 5 DAY) + INTERVAL '14:02' HOUR_MINUTE, 'AB4H9T6W'),
-- 待支付：陈奕迅VIP 1张
(CONCAT('ORD', DATE_FORMAT(NOW(),'%Y%m%d'), '100003'), 2, 3, 6, 10, 'VIP前排', 1, 1680.00, 0,
 NULL,
 DATE_SUB(NOW(), INTERVAL 1 DAY) + INTERVAL '09:15' HOUR_MINUTE, NULL),
-- 已取消：薛之谦内场 2张
(CONCAT('ORD', DATE_FORMAT(NOW(),'%Y%m%d'), '100004'), 2, 5, 10, 22, '内场区', 2, 1760.00, 2,
 NULL,
 DATE_SUB(NOW(), INTERVAL 10 DAY) + INTERVAL '20:30' HOUR_MINUTE, NULL),
-- 已退款：周杰伦B区 1张
(CONCAT('ORD', DATE_FORMAT(NOW(),'%Y%m%d'), '100005'), 2, 1, 1, 3, 'B区看台', 1, 1280.00, 4,
 DATE_SUB(NOW(), INTERVAL 6 DAY) + INTERVAL '11:00' HOUR_MINUTE,
 DATE_SUB(NOW(), INTERVAL 6 DAY) + INTERVAL '10:55' HOUR_MINUTE, NULL),
-- 退款审核中：Taylor Swift Lower Bowl 2张
(CONCAT('ORD', DATE_FORMAT(NOW(),'%Y%m%d'), '100006'), 2, 6, 12, 38, 'Lower Bowl', 2, 4360.00, 5,
 DATE_SUB(NOW(), INTERVAL 3 DAY) + INTERVAL '16:40' HOUR_MINUTE,
 DATE_SUB(NOW(), INTERVAL 3 DAY) + INTERVAL '16:35' HOUR_MINUTE, NULL);

-- 李四(userId=3) 的订单：5条
INSERT INTO `concert_order` (`order_no`, `user_id`, `concert_id`, `session_id`, `seat_area_id`, `seat_area_name`, `ticket_num`, `total_amount`, `status`, `pay_time`, `create_time`, `verify_code`) VALUES
-- 已支付：周杰伦A区 2张
(CONCAT('ORD', DATE_FORMAT(NOW(),'%Y%m%d'), '200001'), 3, 1, 1, 2, 'A区看台', 2, 3360.00, 1,
 DATE_SUB(NOW(), INTERVAL 6 DAY) + INTERVAL '09:32' HOUR_MINUTE,
 DATE_SUB(NOW(), INTERVAL 6 DAY) + INTERVAL '09:30' HOUR_MINUTE, 'CD5R8K3J'),
-- 已支付：陈奕迅内场 2张
(CONCAT('ORD', DATE_FORMAT(NOW(),'%Y%m%d'), '200002'), 3, 3, 6, 11, '内场站区', 2, 2560.00, 1,
 DATE_SUB(NOW(), INTERVAL 4 DAY) + INTERVAL '15:18' HOUR_MINUTE,
 DATE_SUB(NOW(), INTERVAL 4 DAY) + INTERVAL '15:15' HOUR_MINUTE, 'EF6N2Q8M'),
-- 已支付：Taylor Swift VIP 1张
(CONCAT('ORD', DATE_FORMAT(NOW(),'%Y%m%d'), '200003'), 3, 6, 12, 37, 'VIP Floor', 1, 3280.00, 1,
 DATE_SUB(NOW(), INTERVAL 2 DAY) + INTERVAL '12:05' HOUR_MINUTE,
 DATE_SUB(NOW(), INTERVAL 2 DAY) + INTERVAL '12:00' HOUR_MINUTE, 'GH7W4L9X'),
-- 待支付：五月天VIP 1张
(CONCAT('ORD', DATE_FORMAT(NOW(),'%Y%m%d'), '200004'), 3, 2, 4, 8, 'VIP内场', 1, 1880.00, 0,
 NULL,
 DATE_SUB(NOW(), INTERVAL 1 HOUR), NULL),
-- 已取消：林俊杰A区 2张
(CONCAT('ORD', DATE_FORMAT(NOW(),'%Y%m%d'), '200005'), 3, 4, 8, 26, 'A区', 2, 2760.00, 2,
 NULL,
 DATE_SUB(NOW(), INTERVAL 8 DAY) + INTERVAL '18:45' HOUR_MINUTE, NULL);

-- 王五(userId=4) 的订单：5条
INSERT INTO `concert_order` (`order_no`, `user_id`, `concert_id`, `session_id`, `seat_area_id`, `seat_area_name`, `ticket_num`, `total_amount`, `status`, `pay_time`, `create_time`, `verify_code`) VALUES
-- 已支付：五月天B区 4张
(CONCAT('ORD', DATE_FORMAT(NOW(),'%Y%m%d'), '300001'), 4, 2, 3, 7, 'B区看台', 4, 3920.00, 1,
 DATE_SUB(NOW(), INTERVAL 4 DAY) + INTERVAL '11:22' HOUR_MINUTE,
 DATE_SUB(NOW(), INTERVAL 4 DAY) + INTERVAL '11:20' HOUR_MINUTE, 'JK8P3V5Y'),
-- 已支付：林俊杰VIP 2张
(CONCAT('ORD', DATE_FORMAT(NOW(),'%Y%m%d'), '300002'), 4, 4, 8, 25, 'VIP内场', 2, 3960.00, 1,
 DATE_SUB(NOW(), INTERVAL 3 DAY) + INTERVAL '19:10' HOUR_MINUTE,
 DATE_SUB(NOW(), INTERVAL 3 DAY) + INTERVAL '19:05' HOUR_MINUTE, 'LM9S4U7Z'),
-- 已支付：薛之谦VIP 1张
(CONCAT('ORD', DATE_FORMAT(NOW(),'%Y%m%d'), '300003'), 4, 5, 10, 21, 'VIP区', 1, 1280.00, 1,
 DATE_SUB(NOW(), INTERVAL 2 DAY) + INTERVAL '08:50' HOUR_MINUTE,
 DATE_SUB(NOW(), INTERVAL 2 DAY) + INTERVAL '08:45' HOUR_MINUTE, 'NQ2D6F8R'),
-- 待支付：周杰伦C区 3张
(CONCAT('ORD', DATE_FORMAT(NOW(),'%Y%m%d'), '300004'), 4, 1, 2, 8, 'C区看台', 3, 2640.00, 0,
 NULL,
 DATE_SUB(NOW(), INTERVAL 2 HOUR), NULL),
-- 退款审核中：陈奕迅看台 2张
(CONCAT('ORD', DATE_FORMAT(NOW(),'%Y%m%d'), '300005'), 4, 3, 6, 12, '看台区', 2, 1760.00, 5,
 DATE_SUB(NOW(), INTERVAL 1 DAY) + INTERVAL '14:30' HOUR_MINUTE,
 DATE_SUB(NOW(), INTERVAL 1 DAY) + INTERVAL '14:25' HOUR_MINUTE, NULL);
