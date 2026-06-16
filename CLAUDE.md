# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 常用命令

```bash
# 启动后端前确保 MySQL（:3306）和 Redis（:6379）本地服务已运行
# MySQL: root / ywy, Redis: password ywy

# 后端（server/）
cd server && mvn spring-boot:run              # 启动后端 :8080

# 重启后端（先杀旧进程再启动，避免端口占用）
powershell "Get-Process -Name java -ErrorAction SilentlyContinue | Stop-Process -Force"
cd server && mvn spring-boot:run

# C端前端（web-c/）：:3000，代理 /api → :8080
cd web-c && npm install && npm run dev

# 商家端前端（web-merchant/）：:3001，代理 /api → :8080
cd web-merchant && npm install && npm run dev
```

API 文档：后端启动后访问 `http://localhost:8080/doc.html`（Knife4j）

---

## 架构概览

```
web-c (:3000) ──┐                 ┌── MySQL (:3306, 本地服务, 数据库 agentmall)
                ├── server (:8080) ──┤
web-merchant (:3001) ─┘           └── Redis (:6379, 本地服务, password ywy, 购物车)
```

**后端**：Spring Boot 3.2.5 / MyBatis-Plus 3.5.5 / MySQL 8.0 / Redis 7.2 / JDK 17

**前端 C端**：Vue 3.4 + Vite 5 + Vant 4.9（移动端）
**前端 商家端**：Vue 3.4 + Vite 5 + Element Plus 2.7（PC 端）

---

## 快速入口

| 内容 | 位置 |
|------|------|
| 后端模块 | `server/src/main/java/com/agentmall/module/` |
| 前端路由 | `web-c/src/router/index.ts` / `web-merchant/src/router/index.ts` |
| 安全配置 | `server/src/main/java/com/agentmall/config/SecurityConfig.java` |
| JWT 配置 | `server/src/main/resources/application.yml`（`jwt.*`） |
| 测试账户 | 见下方**测试数据** |
| DB Schema | 见下方**数据库** |
| API 文档 | `http://localhost:8080/doc.html`（启动后） |

---

## 测试数据

| 角色 | 手机号 | 密码 | 关联商家 |
|------|--------|------|---------|
| CUSTOMER | 13900000001 | 123456 | — |
| MERCHANT | 13900000002 | 123456 | 老王炒饭（userId=2, merchantId=1, deliveryFee=3） |
| MERCHANT | 13900000003 | 123456 | 小李汉堡店（userId=3, merchantId=2） |

---

## 数据库

### 表结构（8 张表）

```
user ──< address
user ──< orders ──< order_item
user(role=MERCHANT) ── merchant ──< category ──< dish
                                     merchant ──< dish
                                     merchant ──< orders
```

| 表 | 说明 | 关键字段 |
|----|------|---------|
| `user` | 用户 | phone(唯一), password(BCrypt), role(CUSTOMER/MERCHANT) |
| `merchant` | 商家 | userId(关联user), name, status(1营业/0歇业), deliveryFee, minAmount |
| `category` | 菜品分类 | merchantId, name, sortOrder |
| `dish` | 菜品 | merchantId, categoryId, name, price, status(1上架/0下架), salesCount |
| `orders` | 订单 | orderNo(唯一), userId, merchantId, addressSnap(JSON), status, payAmount |
| `order_item` | 订单明细 | orderId, dishName(快照), price(快照), quantity, amount |
| `address` | 收货地址 | userId, contactName, phone, province/city/district/detail, isDefault |
| `cart` | 购物车 | **Redis Hash** `cart:{userId}`，非数据库表 |

> `orders` 和 `order_item` 用菜品快照（下单时固化名称/价格），不随菜品表变更。
> `address_snap` 列存 JSON，内容为 `{contactName, phone, province, city, district, detail}`。

详情建表 SQL 见 `F:\ClaudeCode\plans\claude-toasty-cupcake.md`。

---

## 后端模块

### 分层约定

每个模块结构：`entity → mapper → service(impl) → [dto/vo] → controller`

### MerchantContext（商家身份解析）

`MerchantContext.java` 是公用组件，替代各 Controller 重复的 `getCurrentMerchantId(User)`：
```java
@Component
public class MerchantContext {
    public Long getCurrentMerchantId(User user) {
        Merchant merchant = merchantService.getByUserId(user.getId());
        if (merchant == null) throw new BusinessException("商家不存在");
        return merchant.getId();
    }
}
```
所有商家端 Controller 均注入 `MerchantContext`，通过 `merchantContext.getCurrentMerchantId(user)` 获取当前登录商家的 ID。

### 归属校验模式

商家端写操作必须先获取当前商家 ID，再校验操作实体归属：
```java
Long merchantId = merchantContext.getCurrentMerchantId(user);
Entity exist = service.getById(id);
if (exist == null || !exist.getMerchantId().equals(merchantId)) {
    throw new BusinessException("资源不存在");
}
```

### 模块列表

| 模块 | 目录 | 说明 |
|------|------|------|
| **user** | `module/user/` | 用户注册/登录、个人信息修改、密码修改（5 API） |
| **merchant** | `module/merchant/` | 商家列表浏览（C端公开）、商家管理（商家端） |
| **category** | `module/category/` | C端浏览 + 商家端 CRUD（双控制器） |
| **dish** | `module/dish/` | C端浏览 + 商家端 CRUD/上下架（双控制器 + DisDTO/DisVO） |
| **cart** | `module/cart/` | Redis Hash 增删改查（5 API） |
| **order** | `module/order/` | C端下单/查询/取消 + 商家端接单/配送/完成（双控制器） |
| **address** | `module/address/` | 地址 CRUD + 默认地址管理（5 API） |
| **statistics** | `module/statistics/` | Dashboard 概览/趋势/热销 Top（3 API） |

### C端模块 — 公开 API

| 模块 | 路径 | 说明 | 权限 |
|------|------|------|------|
| **商家** | `GET /api/merchants` | 商家列表（仅 status=1） | 公开 |
| | `GET /api/merchants/{id}` | 商家详情 | 公开 |
| **分类** | `GET /api/merchants/{id}/categories` | 某商家分类列表 | 公开 |
| **菜品** | `GET /api/merchants/{id}/dishes?categoryId=` | 上架菜品（按分类筛选） | 公开 |

### C端模块 — 需登录 API

| 模块 | 方法 | 路径 | 说明 |
|------|------|------|------|
| **购物车** | GET | `/api/cart` | 获取购物车（含商家名、商品、配送费、总金额） |
| | POST | `/api/cart` | 添加商品（body: dishId + quantity），跨商家校验 |
| | PUT | `/api/cart/{dishId}?quantity=N` | 修改数量（≤0 删除） |
| | DELETE | `/api/cart/{dishId}` | 删除单项 |
| | DELETE | `/api/cart` | 清空 |
| **订单** | POST | `/api/orders` | 下单（从购物车创建，自动清空购物车） |
| | GET | `/api/orders` | 我的订单列表（分页） |
| | GET | `/api/orders/{id}` | 订单详情（含商品明细） |
| | PUT | `/api/orders/{id}/cancel` | 取消（仅 PENDING 状态） |
| **地址** | GET | `/api/addresses` | 地址列表（默认优先 + 时间倒序） |
| | POST | `/api/addresses` | 添加（首个自动设默认） |
| | PUT | `/api/addresses/{id}` | 修改 |
| | DELETE | `/api/addresses/{id}` | 删除（删默认时自动提升） |
| | PUT | `/api/addresses/{id}/default` | 设为默认 |
| **认证** | GET | `/api/auth/info` | 当前用户信息 |
| | PUT | `/api/auth/info` | 修改个人信息 |
| | PUT | `/api/auth/password` | 修改密码 |

### 商家端 API（需 MERCHANT 角色，`/api/merchant/**`）

| 模块 | 方法 | 路径 | 说明 |
|------|------|------|------|
| **分类** | GET/POST | `/api/merchant/categories` | 列表 / 添加 |
| | PUT/DELETE | `/api/merchant/categories/{id}` | 修改 / 删除 |
| **菜品** | GET | `/api/merchant/dishes?page=&categoryId=` | 分页列表 + 分类筛选 |
| | POST | `/api/merchant/dishes` | 添加 |
| | PUT | `/api/merchant/dishes/{id}` | 修改 |
| | PUT | `/api/merchant/dishes/{id}/status` | 上下架切换 |
| | DELETE | `/api/merchant/dishes/{id}` | 删除 |
| **订单** | GET | `/api/merchant/orders?page=&status=` | 分页 + 状态筛选 |
| | GET | `/api/merchant/orders/{id}` | 详情（归属校验） |
| | PUT | `/api/merchant/orders/{id}/status?status=X` | 更新状态 |
| **统计** | GET | `/api/merchant/statistics/overview` | 今日/周/月概览 |
| | GET | `/api/merchant/statistics/trend?days=7` | 近 N 天趋势 |
| | GET | `/api/merchant/statistics/top-dishes?limit=10` | 热销 Top N |

### 订单状态流转

```
PENDING ──→ ACCEPTED ──→ DELIVERING ──→ COMPLETED
   │            │
   └──→ CANCELLED       └──→ REJECTED
```
- **用户取消**：仅 PENDING
- **商家接单/拒绝**：仅 PENDING
- **配送**：仅 ACCEPTED
- **完成**：仅 DELIVERING（自动写入 payTime）
- 非法转换在 Service 层抛 `BusinessException`

### 购物车（Redis）

- 结构：Redis Hash `cart:{userId}`，field=dishId，value=CartItemVO(JSON)
- 跨商家校验：添加时若购物车已有其他商家商品，抛 BusinessException
- 菜品/商家状态校验：添加时验证菜品上架 + 商家营业
- CartVO 包含：merchantId, merchantName, items[], totalAmount, **deliveryFee**

### 下单流程

1. 读取 Redis 购物车 → 2. 验证商家营业 → 3. 生成订单号（yyyyMMddHHmmss+4位随机数）
4. 插入 Orders → 5. 批量插入 OrderItem（菜品快照） → 6. 清空购物车
→ `@Transactional` 保证原子性

---

## 前端

### 项目结构

```
web-c/src/                    web-merchant/src/
├── api/                       ├── api/
│   ├── request.ts (axios)     │   ├── request.ts (axios)
│   ├── auth.ts                │   ├── auth.ts
│   ├── merchant.ts            │   ├── category.ts
│   ├── cart.ts                │   ├── dish.ts
│   ├── order.ts               │   ├── order.ts
│   └── address.ts             │   ├── statistics.ts
├── stores/                    │   └── merchant.ts
│   ├── user.ts                ├── stores/user.ts
│   └── cart.ts                ├── router/index.ts
├── router/index.ts            └── views/
├── views/                         ├── login/
│   ├── home/                      ├── dashboard/
│   ├── merchant/                  ├── categories/
│   ├── login/                     ├── dishes/
│   ├── register/                  ├── orders/
│   ├── cart/                      └── settings/
│   ├── checkout/
│   ├── orders/
│   ├── addresses/
│   └── profile/
└── styles/
```

### C端路由（10 条，Vue Router history 模式）

| 路径 | 页面 | 需登录 | 备注 |
|------|------|--------|------|
| `/` | 首页（商家列表） | ❌ | |
| `/merchant/:id` | 商家详情 + 菜品 | ❌ | Vant Tabs 按分类展示 |
| `/login` | 登录 | ❌ | 已登录自动跳首页 |
| `/register` | 注册 | ❌ | |
| `/cart` | 购物车 | ✅ | Stepper 改数量 |
| `/checkout` | 结算下单 | ✅ | 地址选择 + 商品清单 + 金额 |
| `/orders` | 我的订单 | ✅ | 状态标签 + 分页 |
| `/orders/:id` | 订单详情 | ✅ | 状态头部 + 取消按钮 |
| `/addresses` | 地址管理 | ✅ | SwipeCell 编辑/删除 |
| `/profile` | 个人中心 | ✅ | |

### 商家端路由（6 条，Vue Router hash 模式）

| 路径 | 页面 | 权限 |
|------|------|------|
| `/login` | 商家登录 | 未登录 |
| `/dashboard` | 数据概览 | MERCHANT |
| `/categories` | 分类管理 | MERCHANT |
| `/dishes` | 菜品管理 | MERCHANT |
| `/orders` | 订单管理 | MERCHANT |
| `/settings` | 店铺设置 | MERCHANT |

### 认证机制

- **Token 存储**：C端 → `localStorage('token')`，商家端 → `localStorage('merchant_token')`
- **拦截器**：Axios request 自动加 `Authorization: Bearer <token>`
- **401 处理**：清除 token，toast 提示
- **路由守卫**：`meta.requireAuth` 检查登录，`meta.role: 'MERCHANT'` 检查角色

---

## 安全配置

```
SecurityConfig 白名单:
  公开:  POST /api/auth/register, POST /api/auth/login, /api/merchants/**, /doc.html, /v3/api-docs/**
  商家:  /api/merchant/** → hasRole("MERCHANT")
  其他:  需认证（任意角色）
```

JWT：claims 含 userId/phone/role，sub=userId，24h 过期，签名算法由密钥长度自动选择。

---

## 常见问题

### 端口 8080 被占用

```bash
powershell "Get-Process -Name java -ErrorAction SilentlyContinue | Stop-Process -Force"
```

### MySQL 连接报 "Unsupported character encoding 'utf8mb4'"

JDBC URL 中 `characterEncoding` 必须用 `UTF-8`（非 `utf8mb4`）：
```yaml
url: jdbc:mysql://localhost:3306/agentmall?...&characterEncoding=UTF-8&...
```

### Windows curl 发送中文 JSON 报错

Windows curl 默认 GBK 编码。测试时用 ASCII 字符，或用 `--data-raw` + UTF-8 文件。

### category 表缺少 updated_at 列

如果 MyBatis-Plus 插入报 "Unknown column 'updated_at'"，是因为 `Category extends BaseEntity` 含 `@TableField(fill = INSERT_UPDATE)`，但表缺少该列：
```sql
ALTER TABLE category ADD COLUMN updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;
```

---

## 开发指南

### 添加新模块步骤

1. 在 `module/` 下建包：`entity` / `mapper` / `service`(impl) / `dto`(可选) / `vo`(可选) / `controller`
2. Entity 继承 `BaseEntity`（含 id/createdAt/updatedAt 自动填充）
3. Mapper 继承 `BaseMapper<T>`
4. Service 实现业务逻辑，Controller 注入 Service
5. 商家端模块注入 `MerchantContext` 获取商家 ID，做归属校验
6. C端分页查询用 MyBatis-Plus `Page` + `PageResult.of()`
7. 前端在对应 `api/` 目录下创建 API 客户端，`views/` 下创建页面

### 风格约定

- 商家端写操作校验 `entity.getMerchantId().equals(currentMerchantId)`
- Controller 参数用 `@CurrentUser User user` 获取当前登录用户
- DTO 用 `@Valid` + `jakarta.validation.constraints` 校验
- VO 用 `static fromEntity()` 工厂方法
- 业务异常抛 `BusinessException`（GlobalExceptionHandler 统一处理）
- 分页响应统一用 `PageResult<T>` 包装

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
- **chinese-code-review**: 中文 review 沟通参考——话术模板、分级标注、国内团队常见反模式应对。仅在用户显式 /chinese-code-review 时调用。
- **chinese-commit-conventions**: 中文 commit 与 changelog 配置参考。仅在用户显式 /chinese-commit-conventions 时调用。
- **chinese-documentation**: 中文文档排版参考。仅在用户显式 /chinese-documentation 时调用。
- **chinese-git-workflow**: 国内 Git 平台配置参考。仅在用户显式 /chinese-git-workflow 时调用。
- **dispatching-parallel-agents**: 面对 2 个以上独立任务时使用。
- **executing-plans**: 有书面计划需在单独会话执行时使用。
- **finishing-a-development-branch**: 实现完成、测试通过后使用。
- **mcp-builder**: MCP 服务器构建方法论。
- **receiving-code-review**: 收到代码审查反馈后使用。
- **requesting-code-review**: 完成任务或合并前使用。
- **subagent-driven-development**: 含独立任务的实现计划执行时使用。
- **systematic-debugging**: 遇到任何 bug 或异常时使用。
- **test-driven-development**: 实现前先写测试。
- **using-git-worktrees**: 需要隔离工作区时使用。
- **using-superpowers**: 对话开始时使用——确立如何查找和使用技能。
- **verification-before-completion**: 完成或提交前必须运行验证命令。
- **workflow-runner**: 运行 agency-orchestrator YAML 工作流。
- **writing-plans**: 有多步骤任务时使用。
- **writing-skills**: 创建或编辑技能时使用。

## 如何使用

当任务匹配某个 skill 时，使用 `Skill` 工具加载对应 skill 并严格遵循其流程。绝不要用 Read 工具读取 SKILL.md 文件。
<!-- superpowers-zh:end -->
