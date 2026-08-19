# stall_manage

`stall_manage` 是一个面向校园场景的摊位与菜品后台管理系统，采用 Spring Boot + MyBatis XML 实现。

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

## 技术栈

| 分类 | 技术 |
| --- | --- |
| 语言 | Java 17 |
| Web 框架 | Spring Boot 3.5.11、Spring MVC|
| 数据访问 | MyBatis 3.0.5、MyBatis XML、PageHelper 1.4.6 |
| 数据库 | MySQL、H2（测试） |
| 认证与安全 | JWT 、BCrypt |
| 文件存储 | 阿里云 OSS SDK 3.17.4（当前仅用于用户头像） |
| 测试 | JUnit 5、Mockito、MockMvc、Spring Boot Test |
| 构建 | Maven Wrapper|
| 容器化 | Docker、Docker Compose |

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

### 2. 配置应用

仓库提供了无敏感信息的 [配置模板](src/main/resources/application-example.yml)。使用模板时激活 `example` profile，并至少提供数据库密码和 JWT Secret。PowerShell 示例：

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

模板中的 OSS 配置默认为空，因此不使用头像上传时无需提供；调用头像上传接口前必须配置有效 OSS 参数。

### 3. 启动应用

在已经设置上述环境变量的同一个终端(cmd)中运行：

```bash
./mvnw spring-boot:run
```

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

## Docker Compose 启动
本项目的`.env`文件已被`.gitignore`忽略，请在项目根目录下新建`.env`文件，内容如下：
```.env
MYSQL_ROOT_PASSWORD=<mysql-root-password>
DB_USERNAME=stall_app
DB_PASSWORD=<mysql-stall-app-password>
JWT_SECRET=<replace-with-a-long-random-secret>
ALIYUN_OSS_ENDPOINT=<your-aliyun-oss-endpoint>
ALIYUN_OSS_KEY_ID=<your-aliyun-oss-key-id>
ALIYUN_OSS_KEY_SECRET=<your-aliyun-oss-key-secret>
ALIYUN_OSS_BUCKET_NAME=<your-aliyun-oss-bucket-name>
```
其中，如果不使用头像上传功能，则无需配置 `ALIYUN_OSS_*`相关参数。

构建并启动服务：
```bash
docker compose up --build -d
```

停止服务：
```bash
docker compose down
```
  

`MySQL` 数据保存在 `mysql_data` 命名卷中，初始化 `SQL` 仅在数据卷首次创建时执行。执行下面的命令会删除 `MySQL` 数据卷和其中的数据。
```bash
docker compose down -v 
```

## 测试与性能

- 使用 JUnit 5、Mockito、MockMvc 和 H2 进行分层测试，当前 188 项测试全部通过。
- 根据后台查询设计联合索引；在 10 万条菜品合成数据下，列表 SQL 中位耗时由 `255 ms` 降至 `0.737 ms`。