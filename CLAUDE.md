# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 常用命令

```bash
# 启动后端前确保 MySQL（:3306）和 Redis（:6379）本地服务已运行
# MySQL: root / ywy, Redis: password ywy

# 后端（server/）
cd server
mvn spring-boot:run              # 启动后端 :8080

# 重启后端（先杀旧进程再启动，避免端口占用重试）
powershell -Command "Get-Process -Name java -ErrorAction SilentlyContinue | Stop-Process -Force"
cd server && mvn spring-boot:run

# C端前端（web-c/）
cd web-c
npm install                      # 首次安装依赖
npm run dev                      # 启动 :3000，代理 /api → :8080

# 商家端前端（web-merchant/）
cd web-merchant
npm install                      # 首次安装依赖
npm run dev                      # 启动 :3001，代理 /api → :8080
```

API 文档：后端启动后访问 `http://localhost:8080/doc.html`（Knife4j）

## 架构概览

```
web-c (:3000) ──┐                 ┌── MySQL (:3306, 本地服务)
                ├── server (:8080) ──┤
web-merchant (:3001) ─┘           └── Redis (:6379, 本地服务, 购物车)
```

- **MySQL 和 Redis 均为本地直接安装运行**，非 Docker 容器。`docker-compose.yml` 保留备用
- **MySQL 连接**：`root / ywy`，数据库 `agentmall`，JDBC URL 中 `characterEncoding` 必须用 `UTF-8`（非 `utf8mb4`——MySQL Connector/J 8.3 不支持后者作为 Java 字符集名）
- **Redis 连接**：`localhost:6379`，密码 `ywy`
- **后端**：按模块分层 `entity → mapper → service → controller`，模块目录在 `server/src/main/java/com/agentmall/module/`，现已实现 user/merchant/category/dish/order/statistics 6 个模块
- **C端前端**：Vant4 移动端，路由 10 条，需登录页面通过 `meta.requireAuth` 标记，已实现首页商家列表、商家详情（分类Tab+菜品）、登录/注册
- **商家端前端**：Element Plus PC端，hash 路由 6 条，`meta.role: 'MERCHANT'` 控制权限，已实现 Dashboard、分类管理、菜品管理、登录
- **认证**：JWT（24h过期），Spring Security + 自定义 Filter，`@CurrentUser` 注解注入当前用户
- **购物车**：Redis Hash `cart:{userId}`，非数据库表
- **订单**：下单时固化地址快照（JSON列）和菜品快照（order_item 冗余），状态流转 PENDING → ACCEPTED/REJECTED → DELIVERING → COMPLETED

## 认证模块

**后端安全链**：`JwtAuthFilter`（从 Authorization header 提取 Bearer token）→ `JwtTokenProvider`（JJWT 解析 claims，含 userId/phone/role）→ `UserDetailsServiceImpl`（按 userId 查库构建 SecurityContext）→ `@CurrentUser` + `CurrentUserMethodArgumentResolver`（Controller 参数级注入完整 User 实体）

**JWT**：签名算法由密钥长度自动选择，claims 中存 `userId`/`phone`/`role`，`sub` 为 `userId` 字符串。过期时间 24h，配置在 `application.yml` 的 `jwt.expiration`。

**密码**：BCrypt 加密，`SecurityConfig` 暴露 `BCryptPasswordEncoder` Bean 供全局注入。

**权限模型**（`SecurityConfig` 白名单）：
- 公开：`/api/auth/register`、`/api/auth/login`、`/api/merchants/**`、文档路径
- 商家角色：`/api/merchant/**` → `hasRole("MERCHANT")`
- 其余：需认证（任意角色）

**前端认证**：两个前端各自维护独立的 Pinia `userStore`，Token 存储在 localStorage 的不同 key 中——C端用 `token`，商家端用 `merchant_token`，避免同浏览器冲突。Axios 请求拦截器自动携带，响应拦截器处理 401（清除 token）、403（提示无权限）。

**路由守卫**：
- C端：`meta.requireAuth` 标记的页面，未登录跳 `/login`；已登录访问登录/注册页自动跳首页
- 商家端：额外校验 `meta.role: 'MERCHANT'`，已登录访问登录页自动跳 `/dashboard`

## 商家 & 分类 & 菜品模块

### 商家（Merchant）
- **Entity**: `merchant` 表 — userId(关联用户)、name/logo/phone/address/description/openingHours、status（1营业/0歇业）、minAmount(起送价)、deliveryFee(配送费)
- **C端**: `MerchantController` (`/api/merchants`) — 列表（仅 status=1）、详情，公开访问
- **商家端**: 通过 `MerchantService.getByUserId(userId)` 获取当前登录商家的 merchantId，用于分类/菜品/订单的归属校验
- **归属校验模式**：所有商家端写操作校验 `entity.getMerchantId().equals(currentMerchantId)`，防止越权

### 分类（Category）
- **Entity**: `category` 表 — merchantId、name、sortOrder
- **C端**: `CategoryController` (`/api/merchants/{merchantId}/categories`) — 按商家查分类列表，公开访问
- **商家端**: `CategoryManageController` (`/api/merchant/categories`) — 完整 CRUD，需 MERCHANT 角色

### 菜品（Dish）
- **Entity**: `dish` 表 — merchantId、categoryId、name/image/price/description、salesCount、status（1上架/0下架）
- **DTO**: `DishDTO`（categoryId/name/price/image/description），含 `@Valid` 校验
- **VO**: `DishVO.fromEntity()` — 响应对象，隐藏 `createdAt`/`updatedAt`
- **C端**: `DishController` (`/api/merchants/{merchantId}/dishes`) — 按商家+分类筛选上架菜品，公开访问
- **商家端**: `DishManageController` (`/api/merchant/dishes`) — 分页列表+分类筛选、CRUD、上下架切换（`PUT .../{id}/status`），需 MERCHANT 角色

## Dashboard 统计模块

- **3 个 API**（`/api/merchant/statistics`，需 MERCHANT 角色）：
  - `GET /overview` — 今日/本周/本月 订单数+金额、待处理订单数
  - `GET /trend?days=7` — 近 N 天每日趋势
  - `GET /top-dishes?limit=10` — 热销菜品 Top N
- **数据源**: `orders` + `order_item` 表，SQL 通过 MyBatis `@Select` 注解直接写在 Mapper 上
- **前台**: 统计卡片（彩色顶边）+ CSS 柱状图 + 排名列表（前3名金银铜色）

## 订单实体（Orders / OrderItem）

- `Orders` extends `BaseEntity`，字段：orderNo、userId、merchantId、addressSnap(JSON)、totalAmount、deliveryFee、payAmount、status、remark、payTime
- `OrderItem` 不含 `createdAt`/`updatedAt`（仅 id + 业务字段），字段：orderId、dishId、dishName(快照)、dishImage(快照)、price、quantity、amount
- **当前状态**：实体和 Mapper 已创建（用于统计查询），订单业务 Service/Controller 待 Step 6 实现

## 购物车模块（Redis）

- **存储结构**: Redis Hash `cart:{userId}`，field=dishId，value=CartItemVO JSON
- **5 个 API**（`/api/cart`，需登录）：
  - `GET /` — 获取购物车（含商家信息、商品列表、总金额）
  - `POST /` — 添加商品（body: dishId + quantity），自动累加同商品数量
  - `PUT /{dishId}?quantity=N` — 修改数量（≤0 则删除）
  - `DELETE /{dishId}` — 删除单项
  - `DELETE /` — 清空
- **跨商家校验**：添加时若购物车已有其他商家商品，抛出 BusinessException "购物车中已有其他商家的商品，请先清空"
- **菜品/商家状态校验**：添加时验证菜品上架 + 商家营业，否则拒绝
- **VO 设计**：CartItemVO（dishId/dishName/dishImage/price/quantity/merchantId/merchantName/subtotal），CartVO（merchantId/merchantName/items/totalAmount）
- **前端**: Pinia `cartStore`（fetch/add/update/remove/clear），Vant Stepper 改数量，底部结算栏

## 模块目录索引

```
server/src/main/java/com/agentmall/module/
├── user/        # 用户（Step 3）— entity/mapper/service/dto/vo/controller
├── merchant/    # 商家（Step 4）— entity/mapper/service/controller
├── category/    # 分类（Step 4）— entity/mapper/service/controller（C端+商家端双控制器）
├── dish/        # 菜品（Step 4）— entity/mapper/service/dto/vo/controller（C端+商家端双控制器）
├── cart/        # 购物车（Step 5）— Redis Hash，dto/vo/service/controller
├── order/       # 订单（实体+Mappers 已创建，供统计用；业务逻辑待 Step 6）
├── statistics/  # 统计（Dashboard）— service/controller
```

## API 实现进度（28 个中已完成 26 个）

| 分类 | 已完成 | 待实现 |
|------|--------|--------|
| 认证 `/api/auth` | 5/5 ✅ | — |
| 商家 `/api/merchants` | 2/2 ✅ | — |
| 分类 C端 `/api/merchants/{id}/categories` | 1/1 ✅ | — |
| 分类 商家端 `/api/merchant/categories` | 4/4 ✅ | — |
| 菜品 C端 `/api/merchants/{id}/dishes` | 1/1 ✅ | — |
| 菜品 商家端 `/api/merchant/dishes` | 5/5 ✅ | — |
| 统计 `/api/merchant/statistics` | 3/3 ✅ | — |
| 购物车 `/api/cart` | 5/5 ✅ | — |
| 订单 `/api/orders` + `/api/merchant/orders` | 0/6 | Step 6 |
| 地址 `/api/addresses` | 0/5 | Step 7 |
| 菜品 商家端 `/api/merchant/dishes` | 5/5 ✅ | — |
| 统计 `/api/merchant/statistics` | 3/3 ✅ | — |
| 购物车 `/api/cart` | 0/5 | Step 5 |
| 订单 `/api/orders` + `/api/merchant/orders` | 0/6 | Step 6 |
| 地址 `/api/addresses` | 0/5 | Step 7 |

## 实施计划

详细设计文档：`F:\ClaudeCode\plans\claude-toasty-cupcake.md`（8 步实施，28 个 API，8 张表）
- ✅ Step 1: 基础设施
- ✅ Step 2: 公共层
- ✅ Step 3: 认证模块
- ✅ Step 4: 商家 & 分类 & 菜品
- ✅ Step 5: 购物车（Redis）
- ⬜ Step 6: 订单
- ⬜ Step 7: 地址管理
- ✅ Step 8: 数据统计（已提前完成）

---

# 项目技术栈说明（版本兼容版）

> 基于单体架构的餐饮外卖系统，采用前后端分离模式，后端 Spring Boot 3.2.x，前端 Vue 3，数据库 MySQL 8.0。

## 1. 后端核心框架

| 分类 | 技术 | 版本 | 说明与兼容性要求 |
|------|------|------|------------------|
| 运行时环境 | JDK | 17 | Spring Boot 3.x 最低要求 JDK 17 |
| 基础框架 | Spring Boot | 3.2.5 | 稳定版，要求 JDK 17+ |
| Web 层 | Spring MVC | 6.1.x（内嵌） | Spring Boot 3.2.5 自带 |
| 数据访问层 | MyBatis-Plus | 3.5.5 | 适配 Spring Boot 3.x，需要 JDK 17 |
| 数据库连接池 | HikariCP | 5.0.1 | Spring Boot 3.2.5 默认自带 |
| 工具库 | Hutool | 5.8.26 | 无版本冲突，兼容 JDK 17 |
| | Guava | 33.1.0-jre | 适配 JDK 17 |
| JSON 处理 | Jackson | 2.15.3 | Spring Boot 3.2.5 自带 |
| 参数校验 | Jakarta Validation | 3.0.2 | Spring Boot 3.2.5 内置，配合 Hibernate Validator 8.0 |

## 2. 数据库与存储

| 类型 | 技术 | 版本 | 说明与兼容性 |
|------|------|------|--------------|
| 关系数据库 | MySQL | 8.0.33 | 使用 8.0.x 稳定版，驱动兼容 JDBC 8.0 |
| MySQL驱动 | mysql-connector-j | 8.0.33 | 必须与 MySQL 版本匹配，Spring Boot 推荐版本 |
| 缓存数据库 | Redis | 7.2.4 | 稳定版，兼容 Spring Data Redis 3.2.x |
| 对象存储 | MinIO | 8.5.9 | 自建对象存储，客户端兼容 OkHttp 4.x |
| 可选搜索 | Elasticsearch | 8.11.4 | 如需引入，配合 Spring Data Elasticsearch 5.2.x |

## 3. 消息队列与异步处理

| 组件 | 技术 | 版本 | 说明与兼容性 |
|------|------|------|--------------|
| 消息队列 | RabbitMQ | 3.13.x | 稳定版，支持 AMQP 0-9-1 协议 |


## 4. 任务调度

| 组件 | 技术 | 版本 | 说明 |
|------|------|------|------|
| 分布式定时任务 | XXL-JOB | 2.4.1 | 支持 Spring Boot 3.x，执行器需要 JDK 17 |
| 简单定时任务 | Spring Scheduled | 同 Spring Boot | 单机环境下可直接使用 |

## 5. 安全与认证

| 组件 | 技术 | 版本 | 说明 |
|------|------|------|------|
| 安全框架 | Spring Security | 6.2.4 | Spring Boot 3.2.5 内置 |
| JWT | JJWT | 0.12.5 | 兼容 JDK 17，支持 EdDSA 等现代算法 |
| 加密库 | BCrypt（Spring Security自带） | 同 Spring Security | 用于密码加密 |

## 6. API 文档与测试

| 组件 | 技术 | 版本 | 说明 |
|------|------|------|------|
| API 文档生成 | SpringDoc OpenAPI | 2.5.0 | 适配 Spring Boot 3.x（springdoc-openapi-starter-webmvc-ui） |
| 增强界面 | Knife4j | 4.4.0 | 基于 SpringDoc 的增强 UI，版本需对齐 |
| 单元测试 | JUnit | 5.10.2 | Spring Boot 3.2.5 内置 |
| Mock 框架 | Mockito | 5.11.0 | 与 JUnit 5 集成 |
| 集成测试 | TestContainers | 1.19.7 | 支持 MySQL、Redis、RocketMQ 容器化测试 |
| 压力测试 | JMeter | 5.6.3 | 独立工具，无版本冲突 |

## 7. 前端技术栈

| 分类 | 技术 | 版本 | 说明与兼容性 |
|------|------|------|--------------|
| 框架 | Vue | 3.4.27 | 最新稳定版 |
| 构建工具 | Vite | 5.2.11 | 需 Node.js 18+ / 20+ |
| 状态管理 | Pinia | 2.1.7 | 适配 Vue 3 |
| 路由 | Vue Router | 4.3.2 | 适配 Vue 3 |
| HTTP 客户端 | Axios | 1.6.8 | 无框架依赖 |
| UI 组件库（C端） | Vant | 4.9.4 | 移动端组件库，适配 Vue 3 |
| UI 组件库（后台） | Element Plus | 2.7.4 | PC 端组件库，适配 Vue 3 |
| 地图 API | 高德地图 JS API | 2.0 | 浏览器端调用，无版本冲突 |
| 样式预处理器 | SCSS (sass-loader) | 14.1.1 | 配合 Vite，Node.js 版本需匹配 |
| 代码规范 | ESLint | 8.57.0 | 配合 @vue/eslint-config-typescript 14.0.0 |
| | Prettier | 3.2.5 | 与 ESLint 集成 |

## 8. 开发与运维工具

| 分类 | 工具 | 版本 | 说明 |
|------|------|------|------|
| 版本控制 | Git | 2.40+ | 任意高版本即可 |
| 依赖管理（后端） | Maven | 3.9.6 | 需要 JDK 17 |
| 依赖管理（前端） | npm | 10.5.0 | 配合 Node.js 20 LTS |
| 容器化 | Docker | 24.0.9 | 开发/生产环境容器编排 |
| 容器编排 | Docker Compose | 2.24.6 | 本地快速启动中间件 |
| 日志框架 | SLF4J + Logback | 2.0.13 | Spring Boot 3.2.5 自带 |
| 日志收集（可选） | ELK Stack | 8.11.4 | Elasticsearch + Logstash + Kibana 版本统一 |
| CI/CD（可选） | Jenkins | 2.440.3 | 需 JDK 11 或 17 运行 |

## 9. 核心依赖版本对齐说明（确保兼容）

| 依赖组 | 版本组合说明 |
|--------|--------------|
| Spring Boot + MyBatis-Plus | MyBatis-Plus 3.5.5 明确支持 Spring Boot 3.x，使用官方 `mybatis-plus-boot-starter` |
| Spring Boot + MySQL驱动 | Spring Boot 3.2.5 默认推荐 `mysql:mysql-connector-java:8.0.33`，无需手动指定 |
| Spring Boot + Redis | `spring-boot-starter-data-redis` 引入 Lettuce 6.3.1，兼容 Redis 7.x |
| Spring Boot + RocketMQ | 使用 `org.apache.rocketmq:rocketmq-spring-boot-starter:2.2.3`，支持 Spring Boot 3.x |
| Spring Boot + SpringDoc | `springdoc-openapi-starter-webmvc-ui:2.5.0` 内置 Swagger UI 及 OpenAPI 3.0 |
| Spring Boot + JJWT | JJWT 0.12.5 支持 JDK 17，无 `javax.xml.bind` 依赖问题 |
| Vue 3 + Vite | Vite 5.x 需要 Node.js 18+/20+，Vue 3 官方模板默认组合 |
| Element Plus + Vue 3 | Element Plus 2.7.x 依赖 Vue 3.2+，完全兼容 |

---

## 常见问题

### 端口 8080 被占用（重启后端报错）

**现象**：`mvn spring-boot:run` 报端口占用。在 Git Bash 下用 `taskkill /PID` 可能失败（`/PID` 被 Git Bash 路径转换干扰）。

**解决方案**：用 PowerShell 杀掉所有 Java 进程，一句命令即可：

```bash
powershell -Command "Get-Process -Name java -ErrorAction SilentlyContinue | Stop-Process -Force"
```

> 注意：该命令会杀死所有 Java 进程。若需只释放 8080 端口可定位特定 PID，但上面这条最简单可靠。

### MySQL 连接报 "Unsupported character encoding 'utf8mb4'"

**原因**：MySQL Connector/J 8.3.x 不再将 `utf8mb4` 识别为 Java 字符集别名。

**解决方案**：JDBC URL 中 `characterEncoding` 使用 Java 标准名称 `UTF-8`：

```yaml
# application-dev.yml — 正确写法
url: jdbc:mysql://localhost:3306/agentmall?...&characterEncoding=UTF-8&...

# 错误写法（会报 Unsupported encoding）
url: jdbc:mysql://localhost:3306/agentmall?...&characterEncoding=utf8mb4&...
```

### Windows curl 发送中文 JSON 报 UTF-8 编码错误

**现象**：`curl -d` 发送含中文字符的 JSON 时，服务端报 `Invalid UTF-8 start byte 0xb2`。

**原因**：Windows 版 curl 默认使用系统编码（GBK）发送请求体。

**解决方案**：
1. 测试时用纯 ASCII 字符
2. 或用 `--data-raw` 配合文件输入（确保文件是 UTF-8 编码）

---

<!-- superpowers-zh:begin (do not edit between these markers) -->
# Superpowers-ZH 中文增强版

本项目已安装 superpowers-zh 技能框架（20 个 skills）。

## 核心规则

1. **收到任务时，先检查是否有匹配的 skill** — 哪怕只有 1% 的可能性也要检查
2. **设计先于编码** — 收到功能需求时，先用 brainstorming skill 做需求分析
3. **测试先于实现** — 写代码前先写测试（TDD）
4. **验证先于完成** — 声称完成前必须运行验证命令

## 可用 Skills

Skills 位于 `.claude/skills/` 目录，每个 skill 有独立的 `SKILL.md` 文件。

- **brainstorming**: 在任何创造性工作之前必须使用此技能——创建功能、构建组件、添加功能或修改行为。在实现之前先探索用户意图、需求和设计。
- **chinese-code-review**: 中文 review 沟通参考——话术模板、分级标注（必须修复/建议修改/仅供参考）、国内团队常见反模式应对。仅在用户显式 /chinese-code-review 时调用，不要根据上下文自动触发。
- **chinese-commit-conventions**: 中文 commit 与 changelog 配置参考——Conventional Commits 中文适配、commitlint/husky/commitizen 中文模板、conventional-changelog 中文配置。仅在用户显式 /chinese-commit-conventions 时调用，不要根据上下文自动触发。
- **chinese-documentation**: 中文文档排版参考——中英文空格、全半角标点、术语保留、链接格式、中文文案排版指北约定。仅在用户显式 /chinese-documentation 时调用，不要根据上下文自动触发。
- **chinese-git-workflow**: 国内 Git 平台配置参考——Gitee、Coding.net、极狐 GitLab、CNB 的 SSH/HTTPS/凭据/CI 接入差异与镜像同步配置。仅在用户显式 /chinese-git-workflow 时调用，不要根据上下文自动触发。
- **dispatching-parallel-agents**: 当面对 2 个以上可以独立进行、无共享状态或顺序依赖的任务时使用
- **executing-plans**: 当你有一份书面实现计划需要在单独的会话中执行，并设有审查检查点时使用
- **finishing-a-development-branch**: 当实现完成、所有测试通过、需要决定如何集成工作时使用——通过提供合并、PR 或清理等结构化选项来引导开发工作的收尾
- **mcp-builder**: MCP 服务器构建方法论 — 系统化构建生产级 MCP 工具，让 AI 助手连接外部能力
- **receiving-code-review**: 收到代码审查反馈后、实施建议之前使用，尤其当反馈不明确或技术上有疑问时——需要技术严谨性和验证，而非敷衍附和或盲目执行
- **requesting-code-review**: 完成任务、实现重要功能或合并前使用，用于验证工作成果是否符合要求
- **subagent-driven-development**: 当在当前会话中执行包含独立任务的实现计划时使用
- **systematic-debugging**: 遇到任何 bug、测试失败或异常行为时使用，在提出修复方案之前执行
- **test-driven-development**: 在实现任何功能或修复 bug 时使用，在编写实现代码之前
- **using-git-worktrees**: 当需要开始与当前工作区隔离的功能开发，或在执行实现计划之前使用——通过原生工具或 git worktree 回退机制确保隔离工作区存在
- **using-superpowers**: 在开始任何对话时使用——确立如何查找和使用技能，要求在任何响应（包括澄清性问题）之前调用 Skill 工具
- **verification-before-completion**: 在宣称工作完成、已修复或测试通过之前使用，在提交或创建 PR 之前——必须运行验证命令并确认输出后才能声称成功；始终用证据支撑断言
- **workflow-runner**: 在 Claude Code / OpenClaw / Cursor 中直接运行 agency-orchestrator YAML 工作流——无需 API key，使用当前会话的 LLM 作为执行引擎。当用户提供 .yaml 工作流文件或要求多角色协作完成任务时触发。
- **writing-plans**: 当你有规格说明或需求用于多步骤任务时使用，在动手写代码之前
- **writing-skills**: 当创建新技能、编辑现有技能或在部署前验证技能是否有效时使用

## 如何使用

当任务匹配某个 skill 时，使用 `Skill` 工具加载对应 skill 并严格遵循其流程。绝不要用 Read 工具读取 SKILL.md 文件。

如果你认为哪怕只有 1% 的可能性某个 skill 适用于你正在做的事情，你必须调用该 skill 检查。
<!-- superpowers-zh:end -->
