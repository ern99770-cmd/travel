# Zeabur 部署指南

## 项目概述

这是一个完整的旅游管理系统，包含：
- **后端**：Spring Boot 2.7.18 + MySQL + Redis
- **前端**：Vue 2.6.14（用户端 + 管理端）

## 部署架构

在 Zeabur 上部署需要以下服务：
1. MySQL 数据库
2. Redis 缓存
3. Spring Boot 后端
4. Vue 用户端前端
5. Vue 管理端前端

## 部署步骤

### 第一步：准备代码仓库

1. 将项目推送到 GitHub/GitLab 仓库
2. 确保以下文件已提交：
   - `zeabur.json` - Zeabur 部署配置
   - `Dockerfile` - 后端构建配置
   - `application-prod.yml` - 生产环境配置
   - 修改后的 `application.yml`

### 第二步：在 Zeabur 创建项目

1. 访问 [Zeabur](https://zeabur.com) 并登录
2. 点击 "Create Project" 创建新项目
3. 连接你的 GitHub/GitLab 仓库

### 第三步：添加 MySQL 服务

1. 在 Zeabur 项目中点击 "Add Service"
2. 搜索并选择 "MySQL"
3. 配置服务名称为 `travel-mysql`
4. 等待服务启动后，记录以下信息：
   - 内部连接地址（类似 `travel-mysql.zeabur.internal`）
   - 端口（通常是 3306）
   - 用户名（root）
   - 密码（自动生成）

### 第四步：添加 Redis 服务

1. 点击 "Add Service"
2. 搜索并选择 "Redis"
3. 配置服务名称为 `travel-redis`
4. 记录以下信息：
   - 内部连接地址（类似 `travel-redis.zeabur.internal`）
   - 端口（通常是 6379）

### 第五步：初始化数据库

1. 在 Zeabur 控制台进入 MySQL 服务
2. 使用提供的连接信息连接到数据库
3. 导入你的 SQL 脚本创建 `travel` 数据库和表结构

### 第六步：部署后端服务

1. 点击 "Add Service" → "Git"
2. 选择你的仓库
3. 配置如下：
   - **Root Directory**: `travel-server`
   - **Build Command**: `mvn clean package -DskipTests`
   - **Start Command**: `java -jar target/travel-0.0.1-SNAPSHOT.jar`
4. 添加环境变量：
   ```
   SPRING_PROFILES_ACTIVE=prod
   MYSQL_HOST=travel-mysql.zeabur.internal
   MYSQL_PORT=3306
   MYSQL_USERNAME=root
   MYSQL_PASSWORD=<你的MySQL密码>
   REDIS_HOST=travel-redis.zeabur.internal
   REDIS_PORT=6379
   OPENAI_API_KEY=<你的API密钥>
   XUNFEI_APPID=<你的讯飞APPID>
   XUNFEI_API_KEY=<你的讯飞API密钥>
   XUNFEI_API_SECRET=<你的讯飞API密钥>
   ```
5. 等待部署完成

### 第七步：部署用户端前端

1. 点击 "Add Service" → "Git"
2. 选择同一仓库
3. 配置如下：
   - **Root Directory**: `travel-front`
   - **Build Command**: `npm install && npm run build`
   - **Output Directory**: `dist`
   - **Framework**: Static
4. 修改前端的 API 地址配置，指向后端服务的 Zeabur 域名

### 第八步：部署管理端前端

1. 点击 "Add Service" → "Git"
2. 选择同一仓库
3. 配置如下：
   - **Root Directory**: `travel-front-manage`
   - **Build Command**: `npm install && npm run build`
   - **Output Directory**: `dist`
   - **Framework**: Static
4. 修改前端的 API 地址配置

### 第九步：配置域名（可选）

1. 在 Zeabur 中为每个服务配置自定义域名
2. 或者使用 Zeabur 提供的免费域名

## 前端 API 配置修改

需要修改前端的 API 基础地址：

**用户端** (`travel-front/src/utils/request.js` 或类似文件):
```javascript
// 修改为你的后端服务地址
const baseURL = 'https://your-backend-service.zeabur.app'
```

**管理端** (`travel-front-manage/src/utils/request.js`):
```javascript
// 修改为你的后端服务地址
const baseURL = 'https://your-backend-service.zeabur.app'
```

## 环境变量说明

| 变量名 | 说明 | 示例值 |
|--------|------|--------|
| SPRING_PROFILES_ACTIVE | Spring 配置文件 | prod |
| MYSQL_HOST | MySQL 主机 | travel-mysql.zeabur.internal |
| MYSQL_PORT | MySQL 端口 | 3306 |
| MYSQL_USERNAME | MySQL 用户名 | root |
| MYSQL_PASSWORD | MySQL 密码 | ******** |
| REDIS_HOST | Redis 主机 | travel-redis.zeabur.internal |
| REDIS_PORT | Redis 端口 | 6379 |
| OPENAI_API_KEY | OpenAI API 密钥 | sk-... |
| XUNFEI_APPID | 讯飞 APPID | 3864d35b |
| XUNFEI_API_KEY | 讯飞 API Key | ... |
| XUNFEI_API_SECRET | 讯飞 API Secret | ... |

## 故障排查

### 后端无法连接数据库
- 检查环境变量配置是否正确
- 确认 MySQL 服务已启动
- 验证数据库名称和表结构已创建

### 前端无法访问后端
- 确认后端服务已部署完成
- 检查前端 API 地址配置
- 验证 CORS 配置

### 构建失败
- 检查 Maven 依赖是否完整
- 确认 Node.js 版本兼容性
- 查看构建日志获取详细错误

## 成本优化建议

1. 使用 Zeabur 的免费额度进行测试
2. 生产环境考虑升级到付费计划
3. 定期清理不需要的日志和临时文件
4. 合理配置资源限制

## 注意事项

1. 不要在代码中提交敏感信息（密码、API 密钥等）
2. 使用环境变量管理敏感配置
3. 定期备份数据库
4. 监控服务状态和资源使用情况
