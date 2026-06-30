# 快速部署指南

## 前置准备

1. GitHub/GitLab 账号
2. Zeabur 账号 (https://zeabur.com)
3. 项目 SQL 数据库脚本

## 快速部署步骤

### 1. 准备代码仓库

```bash
# 进入项目目录
cd c:\maven\travel_system\travel\travel

# 初始化 Git（如果还没有）
git init
git add .
git commit -m "Initial commit"

# 推送到 GitHub/GitLab
git remote add origin <your-repository-url>
git push -u origin main
```

### 2. 在 Zeabur 创建项目

1. 访问 https://zeabur.com
2. 登录后点击 "Create Project"
3. 选择 "Deploy from Git"
4. 授权并选择你的仓库

### 3. 部署 MySQL 数据库

1. 在 Zeabur 项目中点击 "Add Service"
2. 搜索 "MySQL" 并选择
3. 服务名称设置为 `travel-mysql`
4. 等待启动后记录连接信息

### 4. 部署 Redis

1. 点击 "Add Service"
2. 搜索 "Redis" 并选择
3. 服务名称设置为 `travel-redis`

### 5. 初始化数据库

1. 在 Zeabur 控制台进入 MySQL 服务
2. 使用提供的连接信息连接
3. 导入你的 SQL 脚本创建 `travel` 数据库

### 6. 部署后端

1. 点击 "Add Service" → "Git"
2. 选择仓库，Root Directory 填 `travel-server`
3. Build Command: `mvn clean package -DskipTests`
4. Start Command: `java -jar target/travel-0.0.1-SNAPSHOT.jar`
5. 添加环境变量（参考 .env.example）
6. 等待部署完成

### 7. 部署前端

**用户端：**
1. Add Service → Git
2. Root Directory: `travel-front`
3. Build Command: `npm install && npm run build`
4. Output Directory: `dist`
5. Framework: Static
6. 添加环境变量 `VUE_APP_API_BASE_URL` 为你的后端地址

**管理端：**
1. Add Service → Git
2. Root Directory: `travel-front-manage`
3. 其他配置同用户端

### 8. 访问应用

部署完成后，Zeabur 会提供访问地址，你也可以配置自定义域名。

## 需要修改的地方

- 修改前端 `request.js` 中的 `baseURL` 为你的实际后端地址
- 在 Zeabur 中配置所有必要的环境变量
- 确保数据库表结构已正确导入

## 常见问题

**Q: 后端启动失败？**
A: 检查 MySQL 和 Redis 连接信息是否正确，数据库是否已初始化。

**Q: 前端无法访问后端？**
A: 确认后端已成功启动，检查前端 API 地址配置。

**Q: 构建失败？**
A: 查看 Zeabur 构建日志，确认依赖是否完整。
