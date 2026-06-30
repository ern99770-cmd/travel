# 部署检查清单

## 部署前检查

- [ ] 所有代码修改已提交到 GitHub
- [ ] 确认 `zeabur.json` 文件存在
- [ ] 确认 `Dockerfile` 文件存在
- [ ] 确认 `application-prod.yml` 存在
- [ ] 确认 `.gitignore` 存在
- [ ] 确认前端 API 配置已修改
- [ ] 确认跨域配置已优化
- [ ] 确认健康检查端点已添加
- [ ] 本地数据库备份已准备好

---

## Zeabur 部署步骤

### 1. MySQL 数据库
- [ ] 服务名称：`travel-mysql`
- [ ] 服务已启动，状态为 "Ready"
- [ ] 已记录连接信息（主机、端口、用户名、密码）
- [ ] 已创建 `travel` 数据库
- [ ] 已导入表结构和数据

### 2. Redis 缓存
- [ ] 服务名称：`travel-redis`
- [ ] 服务已启动，状态为 "Ready"
- [ ] 已记录连接信息

### 3. 后端服务
- [ ] 服务名称：`travel-server`
- [ ] Root Directory：`travel-server`
- [ ] 已配置以下环境变量：
  - [ ] `SPRING_PROFILES_ACTIVE=prod`
  - [ ] `MYSQL_HOST=travel-mysql.zeabur.internal`
  - [ ] `MYSQL_PORT=3306`
  - [ ] `MYSQL_USERNAME=root`
  - [ ] `MYSQL_PASSWORD=xxx`
  - [ ] `REDIS_HOST=travel-redis.zeabur.internal`
  - [ ] `REDIS_PORT=6379`
  - [ ] （可选）AI 相关配置
- [ ] 服务已成功部署
- [ ] 访问 `https://你的后端地址/common/health` 返回正常
- [ ] 已记录后端访问地址

### 4. 用户端前端
- [ ] 服务名称：`travel-front`
- [ ] Root Directory：`travel-front`
- [ ] 已配置环境变量：`VUE_APP_API_BASE_URL=后端地址`
- [ ] 服务已成功部署
- [ ] 前端页面可以正常访问
- [ ] 可以正常调用后端 API

### 5. 管理端前端
- [ ] 服务名称：`travel-admin`
- [ ] Root Directory：`travel-front-manage`
- [ ] 已配置环境变量：`VUE_APP_API_BASE_URL=后端地址`
- [ ] 服务已成功部署
- [ ] 管理端页面可以正常访问

---

## 功能测试

- [ ] 用户注册/登录
- [ ] 景点浏览和搜索
- [ ] 酒店浏览和搜索
- [ ] 景点预约
- [ ] 酒店预订
- [ ] 收藏功能
- [ ] 评论功能
- [ ] AI 对话（如果配置）
- [ ] 管理端登录
- [ ] 管理端各项功能

---

## 已创建的配置文件

- [x] `zeabur.json` - Zeabur 服务配置
- [x] `Dockerfile` - Docker 构建文件
- [x] `application-prod.yml` - 生产环境配置
- [x] `.gitignore` - Git 忽略配置
- [x] `.env.example` - 环境变量示例
- [x] `ZEABUR_DEPLOYMENT.md` - 完整部署指南
- [x] `QUICK_START.md` - 快速开始指南
- [x] `DATABASE_SETUP.md` - 数据库设置指南
- [x] `DEPLOYMENT_STEPS.md` - 详细部署步骤
- [x] `DEPLOYMENT_CHECKLIST.md` - 本文档

---

## 已修改的文件

- [x] `application.yml` - 主配置，支持环境变量
- [x] `CorsConfig.java` - 跨域配置，支持环境变量
- [x] `CommonController.java` - 添加健康检查端点
- [x] `travel-front/src/utils/request.js` - 支持环境变量 API 地址
- [x] `travel-front-manage/src/utils/request.js` - 支持环境变量 API 地址

---

## 重要提醒

⚠️ **文件上传注意事项：**
当前上传功能使用本地文件系统，在 Zeabur 容器环境中：
- 上传的文件会在容器重启后丢失
- 建议后续改为云存储（OSS、S3 等）

⚠️ **密钥安全：**
- 不要将包含密码/密钥的文件提交到 Git
- 所有敏感配置都通过 Zeabur 环境变量设置
