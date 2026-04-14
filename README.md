# 数智验舱 · 智辅实验管理平台

面向高校实验教学场景的一体化管理平台，提供**管理员 / 教师 / 学生**多角色能力：组织机构与课程管理、在线资源与作业、测验与成绩、数据驾驶舱统计等。前端为 **Vue 3 + Vite + Element Plus**，后端为 **Spring Boot 3 + MyBatis + MySQL**。

---

## 功能概览

| 方向 | 说明 |
|------|------|
| 组织与基础数据 | 学院、专业、班级、教师、学生、管理员维护 |
| 课程与教学 | 课程信息、我的课程 / 我的授课、课程详情（章节、班级、课件、作业、测验、任务等） |
| 资源与文档 | 资源中心、材料与文档、导出等能力 |
| 通知 | 系统公告发布与展示 |
| 统计看板 | 基于 ECharts 的驾驶舱（按角色展示不同维度） |
| 个人中心 | 分角色个人信息、头像、密码与账号相关页面 |

具体菜单与路由以侧边栏配置为准。

---

## 技术栈

**前端（`vue/`）**

- Vue 3、Vue Router 4、Vite 4  
- Element Plus 2、ECharts 6、Axios  
- 文档导出相关：docxtemplater、file-saver、pizzip  

**后端（`springboot/`）**

- Spring Boot 3.3.x、Java 21  
- MyBatis、PageHelper  
- MySQL（Connector/J）  
- Hutool、Apache POI、JWT（配置见 `application.yml`）  

---

## 仓库结构

```
graduation-system-master/
├── vue/                    # 前端工程
│   ├── public/
│   │   └── config.json     # 运行时后端地址（首屏加载）
│   └── src/                # 页面、路由、工具与样式
├── springboot/             # 后端工程
│   └── src/main/
│       ├── java/           # 控制器、服务、实体
│       └── resources/
│           ├── application.yml
│           ├── mapper/     # MyBatis XML
│           └── sql/        # 部分增量 SQL
├── files/                  # 数据库全量/变更脚本等（如 *-tms.sql）
└── README.md
```

---

## 环境要求

建议与团队验证过的环境（可参考仓库内「关键配置」说明）：

- **JDK** 21  
- **Node.js** 18+（推荐 20 LTS）  
- **Maven** 3.8+  
- **MySQL** 5.7 / 8.0（以实际脚本为准）  

---

## 后端运行

```bash
cd springboot
mvn spring-boot:run
```

---

## 前端运行

1. 安装依赖并启动开发服务器：

   ```bash
   cd vue
   npm install
   npm run dev
   ```

开发服务器默认端口见 **`vite.config.js`**（常见为 **4000**）。已对常用 API 路径配置 **Vite 代理**至 `http://localhost:9090`，便于本地联调。


2. 密码 （用户名/密码）
  管理员：admin / admin
  教师： 2020111027 / 123456
  学生：20231101101 / 101101

3. 访问地址：luotongtong.xin  (复制粘贴到浏览器访问)
