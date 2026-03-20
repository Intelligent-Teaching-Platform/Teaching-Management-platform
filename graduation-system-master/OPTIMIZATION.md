# 项目优化说明

## 一、前端（Vue）

### 1. 接口与配置统一
- **接口基地址**：所有请求通过 `@/utils/request` 发送，`baseURL` 来自 `public/config.json` 的 `serverUrl`，部署时只需改该文件。
- **上传地址**：头像、文件上传统一使用 `getUploadUrl()`（即 `serverUrl + '/files/upload'`），不再使用 `VITE_BASE_URL`。
- **头像展示**：使用 `resolveAvatarUrl(avatar)` 将相对路径转为完整 URL，避免头像不显示；列表头像增加加载失败占位。

### 2. 开发代理（Vite）
- `vite.config.js` 中配置了 `server.proxy`，将 `/material`、`/files`、`/admin` 等路径代理到 `http://localhost:9090`。
- **使用方式**：开发时可将 `public/config.json` 中 `serverUrl` 设为 `""`，则请求走当前域名并由 Vite 转发到后端，减少跨域与配置问题。

### 3. 构建
- 已配置 `manualChunks`（vue-vendor、element-plus）和带 hash 的产出文件名，利于缓存与按需加载。

---

## 二、后端（Spring Boot）

### 1. 依赖
- 已去除 `pom.xml` 中重复依赖（如重复的 web、mybatis、mysql、lombok）。

### 2. 配置
- **开发环境**：可增加 `application-dev.yml`，通过 `spring.profiles.active=dev` 启用，例如关闭 MyBatis 控制台 SQL 日志（`NoLoggingImpl`）。
- **敏感信息**：数据库密码、JWT 密钥、`fileBaseUrl` 建议使用环境变量或配置中心，勿提交到仓库。

---

## 三、建议的后续优化

1. **安全**：登录态使用 HttpOnly Cookie 或短期 Token + 刷新 Token；接口按角色做权限校验。
2. **性能**：列表接口统一分页与索引；大表考虑读写分离或缓存。
3. **代码**：删除未使用的副本页面（如 `material副本.vue`、`Course副本.vue`）；将重复表格/表单抽成公共组件。
4. **部署**：生产环境用 Nginx 反向代理前端与后端，统一域名与 HTTPS。
