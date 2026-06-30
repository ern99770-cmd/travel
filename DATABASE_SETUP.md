# 数据库初始化指南

## 概述

本指南将帮助你在 Zeabur 上初始化旅游管理系统的数据库。

## 前置条件

1. Zeabur MySQL 服务已成功启动
2. 你有 MySQL 的连接信息
3. 你有本地的数据库备份或 SQL 脚本

## 连接方式

### 方式一：使用 Zeabur 控制台

1. 进入 Zeabur 项目
2. 点击 MySQL 服务
3. 找到 "Connect" 或 "连接" 选项
4. 使用提供的 Web 终端或数据库管理工具

### 方式二：使用本地 MySQL 客户端

从 Zeabur 获取连接信息：
- 主机：通常是 `travel-mysql.zeabur.internal`（内部网络）
- 外部连接地址：Zeabur 会提供外部访问地址
- 端口：3306
- 用户名：root
- 密码：在服务详情中查看

使用以下命令连接：
```bash
mysql -h <external-host> -P <port> -u root -p
```

## 数据库初始化步骤

### 1. 创建数据库

```sql
CREATE DATABASE IF NOT EXISTS travel DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE travel;
```

### 2. 导入表结构

如果你有本地的 SQL 备份文件，可以直接导入：

```bash
# 从本地文件导入
mysql -h <external-host> -P <port> -u root -p travel < your_backup.sql
```

或者在 Zeabur 的数据库管理界面中执行 SQL 脚本。

### 3. 验证表结构

导入完成后，验证表是否创建成功：

```sql
USE travel;
SHOW TABLES;
```

你应该看到以下表（根据你的实际项目调整）：
- user
- sys_attractions
- sys_hotel
- sys_hotel_order
- sys_attraction_order
- sys_line
- sys_forum
- sys_comments
- sys_favor
- sys_rotations
- sys_member
- sys_coupon
- sys_coupon_user
- sys_travel_share
- sys_travel_share_like
- sys_ai_log
- sys_points_log
- sys_exchange_record
- sys_travel_plan
- 等等...

### 4. 插入初始数据（可选）

根据需要插入初始数据，比如管理员账号等。

## Zeabur 特定配置

### 环境变量

在 Zeabur 后端服务中配置以下环境变量：

```
MYSQL_HOST=travel-mysql.zeabur.internal
MYSQL_PORT=3306
MYSQL_USERNAME=root
MYSQL_PASSWORD=<your-mysql-password>
SPRING_PROFILES_ACTIVE=prod
```

### 注意事项

1. Zeabur MySQL 服务可能需要几分钟才能完全启动
2. 首次连接时，请确保使用正确的密码
3. 建议定期备份数据库
4. 生产环境请更改默认密码

## 常见问题

**Q: 连接被拒绝？**
A: 确认 MySQL 服务已完全启动，检查连接地址和端口是否正确。

**Q: 字符集问题？**
A: 确保数据库使用 utf8mb4 字符集。

**Q: 如何备份数据库？**
A: 使用 mysqldump 命令或 Zeabur 提供的备份功能。
