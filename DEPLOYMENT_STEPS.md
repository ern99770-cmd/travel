# Zeabur 详细部署步骤

## 重要提示

⚠️ **必须更新 GitHub 代码！** 你现在 GitHub 上的代码是修改配置前的版本，需要推我们刚才做的所有修改才能正常部署。

---

## 第一步：更新 GitHub 代码

### 1.1 提交本地修改

在项目根目录下执行：

```powershell
cd c:\maven\travel_system\travel\travel

# 查看修改的文件
git status

# 添加所有修改的文件
git add .

# 提交修改
git commit -m "feat: 配置Zeabur部署支持"

# 推送到 GitHub
git push origin main
```

### 1.2 确认推送的文件

确保以下新文件/修改已推送到 GitHub：

**新增文件：**
- `zeabur.json` - Zeabur 配置
- `Dockerfile` - Docker 构建文件
- `application-prod.yml` - 生产配置
- `.gitignore` - Git 忽略文件
- `.env.example` - 环境变量示例
- `ZEABUR_DEPLOYMENT.md` - 部署指南
- `QUICK_START.md` - 快速开始
- `DATABASE_SETUP.md` - 数据库指南
- `DEPLOYMENT_STEPS.md` - 本文档

**修改文件：**
- `application.yml` - 主配置
- `CorsConfig.java` - 跨域配置
- `travel-front/src/utils/request.js` - 用户端 API 配置
- `travel-front-manage/src/utils/request.js` - 管理端 API 配置

---

## 第二步：在 Zeabur 创建项目

### 2.1 注册/登录 Zeabur

1. 访问 https://zeabur.com
2. 使用 GitHub 账号登录（推荐）

### 2.2 创建新项目

1. 点击右上角 "Create Project" 或 "新建项目"
2. 项目名称：`travel-system`（或你喜欢的名称）
3. 选择区域（推荐选择亚洲节点）
4. 点击 "Create"

---

## 第三步：部署 MySQL 数据库

### 3.1 添加 MySQL 服务

1. 在项目页面点击 "Add Service" 或 "添加服务"
2. 搜索 "MySQL"
3. 选择官方 MySQL 模板
4. 配置服务名称：`travel-mysql`（必须用这个名字！）
5. 点击 "Deploy" 或 "部署"

### 3.2 等待 MySQL 启动

- 等待 2-5 分钟，状态变为 "Ready" 或 "就绪"
- 点击进入 MySQL 服务详情页

### 3.3 获取连接信息

在 MySQL 服务详情页找到并记录：
- **Internal Connection**（内部连接地址）：应该是 `travel-mysql.zeabur.internal`
- **Port**（端口）：`3306`
- **Username**（用户名）：`root`
- **Password**（密码）：点击显示/复制按钮

---

## 第四步：部署 Redis

### 4.1 添加 Redis 服务

1. 返回项目主页面
2. 点击 "Add Service"
3. 搜索 "Redis"
4. 选择官方 Redis 模板
5. 配置服务名称：`travel-redis`（必须用这个名字！）
6. 点击 "Deploy"

### 4.2 等待 Redis 启动

- 等待 1-2 分钟
- 记录内部连接地址：`travel-redis.zeabur.internal`
- 端口：`6379`

---

## 第五步：初始化数据库

### 5.1 连接到 MySQL

**方式一：使用 Zeabur 控制台**
1. 在 MySQL 服务详情页找到 "Connect" 或 "连接" 标签
2. 点击 "Open in Web Terminal" 或 "在终端中打开"
3. 输入密码登录

**方式二：使用外部连接**
1. 在 MySQL 服务页找到 "External Connection" 或 "外部连接"
2. 使用你本地的 MySQL 客户端（如 Navicat、DBeaver 或命令行）连接

### 5.2 创建数据库并导入

在 MySQL 终端中执行：

```sql
-- 创建数据库
CREATE DATABASE IF NOT EXISTS travel DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 使用数据库
USE travel;

-- 接下来导入你的 SQL 备份文件
-- 如果有本地 .sql 文件，可以在 Zeabur 控制台的文件管理器中上传并导入
-- 或者直接粘贴执行你的建表 SQL
```

**提示：** 如果你本地有完整的数据库备份，可以：
1. 使用 `mysqldump` 导出本地数据库
2. 在 Zeabur MySQL 中导入

---

## 第六步：部署后端服务

### 6.1 添加后端服务

1. 返回项目主页面
2. 点击 "Add Service" → "Git"
3. 选择你的 GitHub 仓库
4. 配置以下参数：

| 配置项 | 值 |
|--------|-----|
| **Service Name** | `travel-server` |
| **Root Directory** | `travel-server` |
| **Branch** | `main`（或你的主分支） |

### 6.2 配置构建设置

向下滚动到 "Build" 或 "构建" 部分：

| 配置项 | 值 |
|--------|-----|
| **Builder** | 选择 "Dockerfile" 或 "Nixpacks" |
| 如果用 Nixpacks： | |
| **Build Command** | `mvn clean package -DskipTests` |
| **Start Command** | `java -jar target/travel-0.0.1-SNAPSHOT.jar` |

### 6.3 配置环境变量（重要！）

找到 "Environment Variables" 或 "环境变量" 部分，添加以下变量：

```
SPRING_PROFILES_ACTIVE=prod
MYSQL_HOST=travel-mysql.zeabur.internal
MYSQL_PORT=3306
MYSQL_USERNAME=root
MYSQL_PASSWORD=这里填写你MySQL的密码
REDIS_HOST=travel-redis.zeabur.internal
REDIS_PORT=6379
```

**可选的 AI 配置（如果需要）：**
```
OPENAI_API_KEY=你的OpenAI密钥
XUNFEI_APPID=你的讯飞APPID
XUNFEI_API_KEY=你的讯飞API密钥
XUNFEI_API_SECRET=你的讯飞API密钥
```

### 6.4 开始部署

1. 确认所有配置正确
2. 点击 "Deploy" 或 "部署"
3. 等待构建和部署（可能需要 5-10 分钟）

### 6.5 验证后端启动

- 等待状态变为 "Ready"
- 点击服务查看日志，确认没有错误
- Zeabur 会自动分配一个域名，如 `https://travel-server-xxx.zeabur.app`
- 记录下这个后端地址！

---

## 第七步：部署用户端前端

### 7.1 添加用户端服务

1. 返回项目主页面
2. 点击 "Add Service" → "Git"
3. 选择同一个 GitHub 仓库
4. 配置参数：

| 配置项 | 值 |
|--------|-----|
| **Service Name** | `travel-front` |
| **Root Directory** | `travel-front` |

### 7.2 配置构建设置

| 配置项 | 值 |
|--------|-----|
| **Build Command** | `npm install && npm run build` |
| **Output Directory** | `dist` |
| **Framework** | `Static` 或 `Vite`（自动检测） |

### 7.3 配置环境变量

添加：
```
VUE_APP_API_BASE_URL=https://你的后端地址.zeabur.app
```
**注意：** 把 `你的后端地址` 替换为第六步得到的实际后端域名！

### 7.4 部署

1. 点击 "Deploy"
2. 等待构建（约 3-5 分钟）
3. 记录分配的前端地址

---

## 第八步：部署管理端前端

### 8.1 添加管理端服务

1. 返回项目主页面
2. 点击 "Add Service" → "Git"
3. 选择同一个 GitHub 仓库
4. 配置参数：

| 配置项 | 值 |
|--------|-----|
| **Service Name** | `travel-admin` |
| **Root Directory** | `travel-front-manage` |

### 8.2 配置构建设置

| 配置项 | 值 |
|--------|-----|
| **Build Command** | `npm install && npm run build` |
| **Output Directory** | `dist` |
| **Framework** | `Static` |

### 8.3 配置环境变量

添加：
```
VUE_APP_API_BASE_URL=https://你的后端地址.zeabur.app
```

### 8.4 部署

1. 点击 "Deploy"
2. 等待构建完成
3. 记录管理端地址

---

## 第九步：配置域名（可选但推荐）

### 9.1 配置自定义域名

1. 进入每个服务的详情页
2. 找到 "Domain" 或 "域名" 标签
3. 点击 "Add Custom Domain" 或 "添加自定义域名"
4. 输入你想使用的域名
5. 按照提示配置 DNS 解析

### 9.2 或者使用 Zeabur 域名

Zeabur 会自动分配免费域名，你也可以直接使用：
- 后端：`https://travel-server-xxx.zeabur.app`
- 用户端：`https://travel-front-xxx.zeabur.app`
- 管理端：`https://travel-admin-xxx.zeabur.app`

---

## 第十步：测试访问

### 10.1 测试后端

访问后端健康检查（如果有）或 API：
```
https://你的后端地址.zeabur.app/
```

### 10.2 测试用户端

1. 访问用户端地址
2. 尝试注册/登录
3. 测试各项功能

### 10.3 测试管理端

1. 访问管理端地址
2. 使用管理员账号登录
3. 测试后台功能

---

## 常见问题排查

### 问题1：后端构建失败

**检查：**
- 查看 Zeabur 构建日志
- 确认 `pom.xml` 中的 Java 版本（1.8）
- 确认 Maven 依赖能正常下载

**解决：**
- 如果网络问题，尝试配置 Maven 镜像
- 检查 `application-prod.yml` 配置

### 问题2：后端启动失败

**检查：**
- 确认 MySQL 和 Redis 已启动
- 检查环境变量配置
- 查看应用日志

**解决：**
- 验证数据库连接信息
- 确认数据库 `travel` 已创建
- 检查表结构是否完整

### 问题3：前端无法连接后端

**检查：**
- 前端 `VUE_APP_API_BASE_URL` 是否正确
- 后端是否正常启动
- 浏览器控制台的网络请求

**解决：**
- 确认 API 地址配置正确
- 检查后端 CORS 配置
- 确认后端服务已就绪

### 问题4：数据库连接失败

**检查：**
- MySQL 服务是否 "Ready"
- 环境变量中的密码是否正确
- 数据库 `travel` 是否已创建

---

## 下一步

部署成功后，你可以：
1. 配置监控和日志
2. 设置自动备份
3. 优化资源配置
4. 配置 HTTPS（Zeabur 自动处理）
5. 添加自定义域名

祝你部署顺利！🎉
