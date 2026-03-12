\# 基于Spring Boot的演唱会门票销售系统 🎫



!\[Java](https://img.shields.io/badge/Java-11-blue)

!\[Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.7.18-brightgreen)

!\[MySQL](https://img.shields.io/badge/MySQL-8.0-orange)

!\[Redis](https://img.shields.io/badge/Redis-6.2-red)

!\[RabbitMQ](https://img.shields.io/badge/RabbitMQ-3.9-yellow)

!\[Vue](https://img.shields.io/badge/Vue-2.6-success)



\## 📖 项目简介

这是一个面向C端用户的在线票务平台，应对演唱会抢票场景的高并发挑战。系统支持演唱会浏览、选座购票、订单管理、支付模拟、门票核销等完整业务流程。



\## 🛠️ 技术栈

\*\*后端\*\*

\- Spring Boot 2.7.18

\- MyBatis-Plus 3.5.3.1

\- MySQL 8.0

\- Redis 6.2（缓存、分布式锁）

\- RabbitMQ 3.9（消息队列）

\- JWT + Spring Security



\*\*前端\*\*

\- Vue.js 2.6

\- Element UI 2.15

\- Axios



\## ✨ 核心功能

\### 普通用户端

\- 演唱会浏览、搜索

\- 选座购票（高并发库存控制）

\- 在线支付模拟（分布式锁）

\- 订单管理

\- 电子票查看

\- 收藏、评价



\### 管理员端

\- 演唱会管理

\- 场次管理（库存预热、开票）

\- 订单管理（退款审核）

\- 门票核销

\- 用户管理

\- 公告管理



\## 🚀 快速开始



\### 环境要求

\- JDK 11+

\- MySQL 8.0

\- Redis 6.2+

\- RabbitMQ 3.9+

\- Maven 3.6+

\- Node.js 16+



\### 一键启动（推荐）

1\. \*\*初始化数据库\*\*

&#x20;  - 双击 `init-database.bat`

&#x20;  - 输入 `Y` 确认初始化

&#x20;  - 等待提示成功



2\. \*\*启动系统\*\*

&#x20;  - 双击 `start.bat`

&#x20;  - 脚本会自动检查环境、编译后端、安装前端依赖

&#x20;  - 等待5秒后，前后端会自动启动



3\. \*\*访问系统\*\*

&#x20;  - 前台：http://localhost:8081

&#x20;  - 后台：http://localhost:8081/admin



\### 测试账号

| 角色 | 用户名 | 密码 |

|------|--------|------|

| 管理员 | admin | admin123 |

| 普通用户 | zhangsan | 123456 |

| 普通用户 | lisi | 123456 |

| 普通用户 | wangwu | 123456 |



\## 🏗️ 项目结构

concert-ticket-system/

├── concert-ticket-server/ # 后端源码

├── concert-ticket-front/ # 前端源码

├── database/ # SQL脚本

├── init-database.bat # 数据库初始化脚本

├── start.bat # 一键启动脚本

└── README.md # 项目说明





\## 🧪 核心难点解决

| 难点 | 解决方案 |

|------|---------|

| 高并发库存超卖 | Redis原子递减 + RabbitMQ异步处理 |

| 重复支付 | Redis分布式锁 + 幂等性校验 |

| 流量削峰 | 令牌桶限流 + 消息队列 |

| 库存一致性 | 操作日志 + 最终一致性 |



\## 📧 关于作者

\- 作者：何旭旭

\- 学校：郑州师范学院

\- 专业：软件工程 2026届

\- 邮箱：你的邮箱1096052080@qq.com

\- GitHub：https://github.com/xiaoxu0416



\## 📄 许可证

MIT

