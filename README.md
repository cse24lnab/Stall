# stall_manage

`stall_manage` 是一个面向校园场景的摊位与菜品后台管理系统，采用 Spring Boot + MyBatis XML 实现。项目当前聚焦后台管理能力，用于展示认证鉴权、角色与数据权限、逻辑删除、分页筛选、事务、索引设计和分层测试。

> 当前不包含顾客浏览、购物车、订单、支付和配送等外卖功能。这些能力属于后续扩展方向，不是当前可用接口。

## 核心功能

- 用户注册、登录和 BCrypt 密码存储
- JWT 登录校验及当前用户上下文
- `USER`、`MERCHANT`、`ADMIN` 三种角色权限
- 当前用户资料、密码和头像维护
- 摊位分页查询、组合筛选及后台管理
- 菜品分页查询、组合筛选及后台管理
- 商家资源归属校验
- 摊位与菜品批量逻辑删除
- PageHelper 分页与 H2 集成测试
- 逻辑删除下的名称唯一约束
- 基于真实查询设计并验证联合索引

## 权限模型

| 角色 | 摊位权限 | 菜品权限 | 用户资料与头像 |
| --- | --- | --- | --- |
| `ADMIN` | 查看全部；新增、修改、删除 | 查看并管理全部 | 管理自己的资料与头像 |
| `MERCHANT` | 只能查看自己的摊位 | 只能管理自己摊位下的菜品 | 管理自己的资料与头像 |
| `USER` | 无权访问后台接口 | 无权访问后台接口 | 管理自己的资料与头像 |

资源归属规则：

- `stall.owner_user_id` 表示摊位所属商家。
- `dish` 通过 `stall_id` 间接归属于商家。
- Controller 使用 `@RequireRole` 控制角色入口，Service 再校验资源归属，避免只依赖客户端查询参数。
- 菜品批量删除采用严格原子策略：只要请求中混入不存在或越权 ID，整批拒绝，不执行部分删除。

## 技术栈

| 分类 | 技术 |
| --- | --- |
| 语言 | Java 17 |
| Web 框架 | Spring Boot 3.5.11、Spring MVC、Spring Validation |
| 数据访问 | MyBatis 3.0.5、MyBatis XML、PageHelper 1.4.6 |
| 数据库 | MySQL、H2（测试） |
| 认证与安全 | JWT (`jjwt 0.9.1`)、BCrypt |
| 文件存储 | 阿里云 OSS SDK 3.17.4（当前仅用于用户头像） |
| 测试 | JUnit 5、Mockito、MockMvc、Spring Boot Test |
| 构建 | Maven |

## 项目结构

```text
src/main/java/org/lab/stall_manage
├── annotation       角色权限注解
├── config           MVC、JWT、密码和 OSS 配置
├── context          ThreadLocal 当前用户上下文
├── controller       HTTP 协议绑定与基础校验
├── dto              请求 DTO
├── exception        业务异常与全局异常处理
├── interceptor      JWT 与角色拦截器
├── mapper           MyBatis Mapper 接口
├── pojo             实体、枚举和统一响应
├── service          Service 接口
├── service/impl     业务规则、归属校验和事务
├── utils            JWT、OSS 工具
└── vo               响应 VO

src/main/resources/mapper   MyBatis XML
src/test/java               单元测试和集成测试
src/test/resources          H2 配置、DDL 和初始化数据
docs/api.md                 完整接口文档
docs/sql/canteen.sql        MySQL 新建库脚本
docs/sql/migration          已有数据库升级脚本
```

主要调用链：

```text
HTTP Request
  -> LoginInterceptor（JWT、角色）
  -> Controller（参数绑定与校验）
  -> Service（业务规则、资源归属、事务）
  -> Mapper XML（SQL）
  -> MySQL / H2
```

## 数据库设计

当前包含三张业务表：

- `user`：账号、密码哈希、角色、状态和头像信息。
- `stall`：摊位信息、营业状态、商家归属和逻辑删除标记。
- `dish`：菜品、价格、售罄状态、摊位归属和逻辑删除标记。

项目不使用 `dish.stall_id -> stall.id` 数据库外键，关联存在性和商家归属由 Service 校验。`stall_id` 仍保留索引以支持关联查询。

### 逻辑删除与唯一约束

直接对 `name` 建唯一索引会导致逻辑删除后无法重新使用名称。本项目使用生成列：

```sql
active_name VARCHAR(100) GENERATED ALWAYS AS (
    CASE WHEN is_delete = 0 THEN name ELSE NULL END
) STORED
```

有效记录的 `active_name` 等于真实名称，已删除记录的值为 `NULL`。MySQL 唯一索引允许多个 `NULL`，因此可以同时满足：

- 未删除摊位名称全局唯一。
- 同一摊位内未删除菜品名称唯一。
- 不同摊位允许使用相同菜品名。
- 逻辑删除后允许重新创建同名数据，并保留历史记录。

对应索引：

```sql
UNIQUE KEY uk_stall_active_name (active_name);
UNIQUE KEY uk_dish_stall_active_name (stall_id, active_name);
```

### 查询索引与性能验证

根据商家后台列表的真实查询条件建立联合索引：

```sql
KEY idx_stall_owner_delete_id (owner_user_id, is_delete, id);
KEY idx_dish_stall_delete_id (stall_id, is_delete, id);
```

索引字段顺序先按资源归属缩小范围，再过滤逻辑删除，最后配合 `id` 排序。名称筛选使用 `LIKE '%keyword%'`，普通 B-Tree 索引不能有效加速该条件，因此没有为它盲目增加普通索引。

MySQL 8.4.6 独立压测库结果：

| 查询 | 数据规模 | 索引前中位数 | 索引后中位数 | 扫描范围变化 |
| --- | ---: | ---: | ---: | --- |
| 商家摊位列表 | 10,000 个摊位 | `13.85 ms` | `0.09985 ms` | 10,000 行降至 10 行 |
| 摊位分页 `COUNT` | 10,000 个摊位 | `7.02 ms` | `0.05375 ms` | 全表扫描变为覆盖索引查询 |
| 商家菜品列表 | 100,000 个菜品 | `255 ms` | `0.737 ms` | 10 万条扫描、9 万次关联降为目标摊位索引查询 |
| 菜品分页 `COUNT` | 100,000 个菜品 | `199.5 ms` | `0.185 ms` | 两侧均使用覆盖索引查询 |

测试方法为每条 SQL 预热一次，再执行十次 `EXPLAIN ANALYZE` 并取中位数。结果来自合成数据的 SQL 层对比，不代表线上 HTTP 接口响应时间。

## 本地运行

### 环境要求

- JDK 17
- Maven 3.9+
- MySQL 8.x

### 1. 初始化数据库

新建数据库时执行：

```bash
mysql -u root -p < docs/sql/canteen.sql
```

如果数据库来自旧版建表脚本，先选择目标数据库，再执行迁移：

```bash
mysql -u root -p canteen < docs/sql/migration/V001__logical_unique_and_query_indexes.sql
```

迁移脚本按旧版约束和索引名称编写，只应执行一次。生产或重要数据环境执行 DDL 前应先备份并在测试库验证。

### 2. 配置应用

仓库提供了无敏感信息的 [配置模板](src/main/resources/application-example.yml)。本地 `src/main/resources/application.yml` 被 `.gitignore` 忽略，不应提交真实数据库密码、JWT Secret 或 OSS Key。

使用模板时激活 `example` profile，并至少提供数据库密码和 JWT Secret。PowerShell 示例：

```powershell
$env:SPRING_PROFILES_ACTIVE = "example"
$env:DB_PASSWORD = "<your-mysql-password>"
$env:JWT_SECRET = "<replace-with-a-long-random-secret>"
```

也可以通过 `DB_URL`、`DB_USERNAME`、`JWT_EXPIRE_MS` 覆盖模板中的默认连接和过期时间。

基础功能使用的环境变量：

```text
DB_PASSWORD
JWT_SECRET
DB_URL（可选）
DB_USERNAME（可选）
JWT_EXPIRE_MS（可选）
```

头像上传额外使用：

```text
ALIYUN_OSS_ENDPOINT
ALIYUN_OSS_KEY_ID
ALIYUN_OSS_KEY_SECRET
ALIYUN_OSS_BUCKET_NAME
```

模板中的 OSS 配置默认为空，因此不使用头像上传时无需提供；调用头像上传接口前必须配置有效 OSS 参数。JWT Secret 应使用足够长度的随机值。不要把真实凭据写入 README、Git 或公开日志。

### 3. 启动应用

在已经设置上述环境变量的同一个终端中运行：

```bash
mvn spring-boot:run
```

默认访问地址：`http://localhost:8080`。

### 4. 本地演示账号

`canteen.sql` 初始化了以下仅供本地演示的账号，初始密码均为 `12345678`：

| 用户名 | 角色 |
| --- | --- |
| `user_demo` | `USER` |
| `merchant_demo` | `MERCHANT` |
| `admin_demo` | `ADMIN` |

部署到共享或公开环境前应删除演示账号或立即修改密码。

## 接口概览

| 模块 | 方法与路径 | 权限 |
| --- | --- | --- |
| 认证 | `POST /auth/register`、`POST /auth/login` | 匿名 |
| 用户 | `GET /users/me`、`PUT /users/me`、`PUT /users/me/password` | 已登录 |
| 头像 | `POST /files` | 已登录 |
| 摊位 | `GET /stalls`、`GET /stalls/{id}` | `ADMIN`、`MERCHANT` |
| 摊位 | `POST /stalls`、`PUT /stalls/{id}`、`DELETE /stalls` | `ADMIN` |
| 菜品 | `GET /dishes`、`GET /dishes/{id}` | `ADMIN`、`MERCHANT` |
| 菜品 | `POST /dishes`、`PUT /dishes/{id}`、`DELETE /dishes` | `ADMIN`、`MERCHANT`，同时校验资源归属 |

完整参数、响应示例和当前错误行为见 [接口文档](docs/api.md)。

受保护接口使用：

```http
Authorization: Bearer <access_token>
```

当前统一业务响应结构：

```json
{
  "code": 1,
  "msg": "success",
  "data": null
}
```

大部分业务异常当前仍返回 `HTTP 200 + code=0`；JWT 校验失败返回 `401`，角色不足返回 `403`，后两者尚未统一 JSON 响应体。

## 测试

运行全部测试：

```bash
mvn clean test
```

当前干净测试结果：

```text
Tests run: 188
Failures: 0
Errors: 0
Skipped: 0
```

测试分层：

- Controller：MockMvc + Mock Service，验证协议绑定、校验和响应。
- Service：JUnit 5 + Mockito，验证业务规则、权限、归属和事务前置条件。
- Mapper：Spring Boot + H2，验证动态 SQL、字段映射和数据库约束。
- 分页集成测试：PageHelper + H2，验证筛选结果与 `total` 一致。
- Interceptor：Mock request/response，验证 JWT、角色和 ThreadLocal 清理。

逻辑唯一约束测试覆盖有效名称重复、删除后重复重建、菜品跨摊位同名等场景。

## 当前限制与 Roadmap

当前限制：

- 用户禁用状态尚未参与登录判断。
- JWT 中角色变化后需要重新登录才能生效。
- 401/403 尚未使用统一 JSON 响应体。
- 头像仅校验大小和扩展名，尚未校验真实文件内容。
- 头像 OSS 上传与数据库 URL 更新不是一个原子事务。
- 尚未提供顾客浏览、购物车和订单等外卖功能。

近期计划：

1. 接入 OpenAPI/Swagger。
2. 统一 401/403 JSON 响应。
3. 增加少量真实 HTTP -> Service -> Mapper -> H2 端到端测试。

远期方向包括商家申请审批、顾客浏览、购物车、订单与订单状态流转。这些内容不属于当前已实现功能。
