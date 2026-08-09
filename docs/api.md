# `stall_manage` REST API

## 1. 项目定位

`stall_manage` 当前定位为校园摊位与菜品后台管理系统，主要覆盖：

- 用户注册、登录和个人资料维护
- JWT 登录校验与角色权限控制
- 摊位分页查询和基础管理
- 菜品分页查询和基础管理
- 用户头像上传到阿里云 OSS

购物车、订单、商家申请审批和统一 `File` 表不属于当前已实现接口，统一放在本文末尾的 Roadmap 中。

## 2. 当前约定

### 2.1 基础路径

当前 Controller 没有统一的 `/api/v1` 前缀，请直接使用本文列出的路径，例如：

```http
POST /auth/login
GET /stalls
POST /files
```

### 2.2 鉴权

只有以下接口允许匿名访问：

- `POST /auth/register`
- `POST /auth/login`

其他接口当前都需要请求头：

```http
Authorization: Bearer <access_token>
```

JWT 中包含 `id`、`username` 和 `role`。当前角色为：

| 角色 | 说明 |
| --- | --- |
| `USER` | 普通用户，可维护自己的资料和头像 |
| `MERCHANT` | 商家，可查看自己的摊位，并维护自己摊位下的菜品 |
| `ADMIN` | 管理员，可维护摊位和菜品 |

### 2.3 统一响应

业务接口统一返回 `Result<T>`：

```json
{
  "code": 1,
  "msg": "success",
  "data": {}
}
```

- `code = 1`：业务成功
- `code = 0`：业务失败
- `data`：响应数据，无数据时为 `null`

当前异常处理的实际行为：

- Controller 和 Service 中的大部分参数、业务异常仍返回 `HTTP 200 + code=0`
- 登录校验失败由拦截器返回 `HTTP 401`，当前没有统一 JSON 响应体
- 角色权限不足由拦截器返回 `HTTP 403`，当前没有统一 JSON 响应体

### 2.4 分页

摊位和菜品列表支持：

| 参数 | 类型 | 必填 | 默认值 | 约束 |
| --- | --- | --- | --- | --- |
| `page` | integer | 否 | `1` | 必须大于 `0` |
| `pageSize` | integer | 否 | `10` | 必须大于 `0`且不超过 `50` |

实际分页响应字段为 `total` 和 `records`：

```json
{
  "code": 1,
  "msg": "success",
  "data": {
    "total": 1,
    "records": []
  }
}
```

## 3. 接口总览

| 模块 | 方法 | 路径 | 权限 | 状态 |
| --- | --- | --- | --- | --- |
| 认证 | `POST` | `/auth/register` | 匿名 | 已实现 |
| 认证 | `POST` | `/auth/login` | 匿名 | 已实现 |
| 用户 | `GET` | `/users/me` | 已登录 | 已实现 |
| 用户 | `PUT` | `/users/me` | 已登录 | 已实现 |
| 用户 | `PUT` | `/users/me/password` | 已登录 | 已实现 |
| 文件 | `POST` | `/files` | 已登录 | 已实现，仅头像 |
| 摊位 | `GET` | `/stalls` | `ADMIN`、`MERCHANT` | 已实现，商家仅返回自己的摊位 |
| 摊位 | `GET` | `/stalls/{id}` | `ADMIN`、`MERCHANT` | 已实现，商家仅可查看自己的摊位 |
| 摊位 | `POST` | `/stalls` | `ADMIN` | 已实现 |
| 摊位 | `PUT` | `/stalls/{id}` | `ADMIN` | 已实现 |
| 摊位 | `DELETE` | `/stalls?ids=1&ids=2` | `ADMIN` | 已实现，批量逻辑删除 |
| 菜品 | `GET` | `/dishes` | `ADMIN`、`MERCHANT` | 已实现，商家仅返回自己的菜品 |
| 菜品 | `GET` | `/dishes/{id}` | `ADMIN`、`MERCHANT` | 已实现，商家仅可查看自己的菜品 |
| 菜品 | `POST` | `/dishes` | `ADMIN`、`MERCHANT` | 已实现 |
| 菜品 | `PUT` | `/dishes/{id}` | `ADMIN`、`MERCHANT` | 已实现 |
| 菜品 | `DELETE` | `/dishes?ids=1&ids=2` | `ADMIN`、`MERCHANT` | 已实现，批量逻辑删除 |

## 4. 认证接口

### 4.1 `POST /auth/register`

注册普通用户。

- 权限：匿名
- Content-Type：`application/json`

请求体：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `username` | string | 是 | 用户名，不能为空，数据库唯一 |
| `password` | string | 是 | 最少 `8` 位，使用 BCrypt 入库 |
| `nickname` | string | 是 | 数据库字段不能为空；当前 DTO 尚未添加 `@NotBlank` |
| `phone` | string | 否 | 数据库唯一；注册 DTO 当前尚未校验手机号格式 |

请求示例：

```json
{
  "username": "alice01",
  "password": "12345678",
  "nickname": "Alice",
  "phone": "13800000000"
}
```

成功响应：

```json
{
  "code": 1,
  "msg": "success",
  "data": {
    "id": 4,
    "username": "alice01",
    "nickname": "Alice",
    "phone": "13800000000",
    "status": "ACTIVE"
  }
}
```

用户名或手机号重复时返回 `code=0`。

### 4.2 `POST /auth/login`

通过用户名和密码登录。

- 权限：匿名
- Content-Type：`application/json`
- 当前只支持用户名登录，不支持手机号登录

请求体：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `username` | string | 是 | 用户名 |
| `password` | string | 是 | 最少 `8` 位 |

请求示例：

```json
{
  "username": "admin_demo",
  "password": "12345678"
}
```

成功响应：

```json
{
  "code": 1,
  "msg": "success",
  "data": {
    "accessToken": "<jwt-token>",
    "tokenType": "Bearer",
    "expiresIn": 360,
    "user": {
      "id": 3,
      "username": "admin_demo",
      "nickname": "管理员演示账号",
      "phone": null,
      "status": "ACTIVE"
    }
  }
}
```

当前限制：用户的 `status` 尚未参与登录禁用判断。

## 5. 用户接口

### 5.1 `GET /users/me`

查询当前登录用户资料。

- 权限：已登录用户

成功响应：

```json
{
  "code": 1,
  "msg": "success",
  "data": {
    "id": 1,
    "username": "user_demo",
    "nickname": "普通用户演示账号",
    "phone": null,
    "avatarFileId": null,
    "avatarUrl": "https://example.com/avatar.png",
    "role": "USER",
    "status": "ACTIVE",
    "createTime": "2026-07-18T10:00:00",
    "updateTime": "2026-07-18T10:20:00"
  }
}
```

用户不存在时，当前返回 `code=1, data=null`。

### 5.2 `PUT /users/me`

局部修改当前用户资料。

- 权限：已登录用户
- Content-Type：`application/json`
- 至少提供一个可更新字段

请求体：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `nickname` | string | 否 | 昵称 |
| `phone` | string | 否 | `1` 开头的 11 位手机号 |
| `avatarFileId` | integer | 否 | 兼容预留字段，当前头像上传流程不依赖它 |

请求示例：

```json
{
  "nickname": "Alice Zhang",
  "phone": "13800000001"
}
```

成功响应：

```json
{
  "code": 1,
  "msg": "success",
  "data": null
}
```

### 5.3 `PUT /users/me/password`

修改当前用户密码。

- 权限：已登录用户
- Content-Type：`application/json`

请求体：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `oldPassword` | string | 是 | 旧密码，最少 `8` 位 |
| `newPassword` | string | 是 | 新密码，最少 `8` 位 |

请求示例：

```json
{
  "oldPassword": "12345678",
  "newPassword": "newPassword123"
}
```

旧密码不匹配时返回：

```json
{
  "code": 0,
  "msg": "密码错误",
  "data": null
}
```

## 6. 文件接口

### 6.1 `POST /files`

上传并绑定当前用户头像。

- 权限：已登录用户
- Content-Type：`multipart/form-data`
- 当前不建立独立 `File` 表，上传成功后直接保存 `User.avatarUrl`

表单字段：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `file` | file | 是 | 文件不能为空，最大 `2 MB`，扩展名仅允许 `.jpg`、`.png` |
| `bizType` | string | 否 | 省略时按 `avatar` 处理；当前只接受 `avatar` |

curl 示例：

```bash
curl -X POST http://localhost:8080/files \
  -H "Authorization: Bearer <access_token>" \
  -F "file=@avatar.png" \
  -F "bizType=avatar"
```

成功响应：

```json
{
  "code": 1,
  "msg": "success",
  "data": {
    "fileId": null,
    "url": "https://example.com/avatar.png",
    "fileName": "avatar.png",
    "contentType": "image/png",
    "size": 20480,
    "uploadedBy": 1,
    "createTime": "2026-07-18T15:00:00"
  }
}
```

当前格式校验基于文件名扩展名，尚未校验真实文件头或 MIME 内容。

## 7. 摊位接口

### 7.1 `GET /stalls`

分页查询未被逻辑删除的摊位。

- 权限：`ADMIN`、`MERCHANT`
- `ADMIN` 查询全部摊位；`MERCHANT` 仅查询自己的摊位，客户端传入的 `ownerUserId` 不会扩大查询范围
- 当前按 `id ASC` 排序

查询参数：

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `page` | integer | 否 | 默认 `1`，必须大于 `0` |
| `pageSize` | integer | 否 | 默认 `10`，必须大于 `0`且不超过 `50` |
| `id` | integer | 否 | 按 ID 精确查询 |
| `name` | string | 否 | 按名称进行包含式模糊查询 |
| `currentStatus` | integer | 否 | 按营业状态精确筛选，当前使用 `0`、`1` |

请求示例：

```http
GET /stalls?page=1&pageSize=10&name=冷面&currentStatus=1
Authorization: Bearer <access_token>
```

成功响应：

```json
{
  "code": 1,
  "msg": "success",
  "data": {
    "total": 1,
    "records": [
      {
        "id": 1,
        "name": "烤冷面",
        "currentStatus": 1,
        "noonLocation": "东区",
        "eveningLocation": "西区",
        "noonStartTime": "11:00:00",
        "noonEndTime": "13:00:00",
        "eveningStartTime": "17:00:00",
        "eveningEndTime": "20:00:00",
        "createTime": "2026-07-18T10:00:00",
        "updateTime": "2026-07-18T10:00:00",
        "isDelete": 0,
        "ownerUserId": 2
      }
    ]
  }
}
```

### 7.2 `GET /stalls/{id}`

按 ID 查询摊位。

- 权限：`ADMIN`、`MERCHANT`
- `MERCHANT` 只能查看 `ownerUserId` 等于当前用户 ID 的摊位
- `id` 必须大于 `0`
- 查不到时当前返回 `code=1, data=null`

### 7.3 `POST /stalls`

新增摊位。

- 权限：`ADMIN`
- Content-Type：`application/json`
- `name` 必填
- `name` 在未删除摊位中全局唯一；逻辑删除后允许重新使用
- `ownerUserId` 必填，且必须对应一个真实的 `MERCHANT` 用户
- `currentStatus` 省略时默认为 `0`

请求示例：

```json
{
  "name": "张记煎饼",
  "currentStatus": 1,
  "noonLocation": "一食堂门口",
  "eveningLocation": "西操场",
  "ownerUserId": 2,
  "noonStartTime": "11:00:00",
  "noonEndTime": "13:30:00",
  "eveningStartTime": "17:00:00",
  "eveningEndTime": "21:00:00"
}
```

### 7.4 `PUT /stalls/{id}`

局部更新摊位。

- 权限：`ADMIN`
- 路径 `id` 必须大于 `0`
- 请求体中的 `id` 可以省略；如果提供，必须与路径一致
- `ownerUserId` 不允许通过普通更新接口修改
- 目标摊位不存在时返回 `摊位不存在`

请求示例：

```json
{
  "name": "张记煎饼二店",
  "currentStatus": 0
}
```

### 7.5 `DELETE /stalls`

批量逻辑删除摊位，并在同一事务内逻辑删除这些摊位下的菜品。

- 权限：`ADMIN`
- 参数使用重复的 `ids`

请求示例：

```http
DELETE /stalls?ids=1&ids=2
Authorization: Bearer <admin-token>
```

## 8. 菜品接口

### 8.1 `GET /dishes`

分页查询未被逻辑删除的菜品。

- 权限：`ADMIN`、`MERCHANT`
- `ADMIN` 查询全部菜品；`MERCHANT` 仅查询自己摊位下的菜品
- 当前按 `id ASC` 排序

查询参数：

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `page` | integer | 否 | 默认 `1`，必须大于 `0` |
| `pageSize` | integer | 否 | 默认 `10`，必须大于 `0`且不超过 `50` |
| `id` | integer | 否 | 按 ID 精确查询 |
| `stallId` | integer | 否 | 按所属摊位查询 |
| `name` | string | 否 | 按名称进行包含式模糊查询 |
| `price` | number | 否 | 按价格精确查询 |
| `isSoldOut` | integer | 否 | 按售罄状态精确筛选，当前使用 `0`、`1` |

查询条件可组合使用。例如：

```http
GET /dishes?page=1&pageSize=10&name=冷面&isSoldOut=0
Authorization: Bearer <access_token>
```

成功响应：

```json
{
  "code": 1,
  "msg": "success",
  "data": {
    "total": 1,
    "records": [
      {
        "id": 1,
        "stallId": 1,
        "name": "招牌烤冷面",
        "price": 12.50,
        "isSoldOut": 0,
        "createTime": "2026-07-18T10:00:00",
        "updateTime": "2026-07-18T10:00:00",
        "isDelete": 0
      }
    ]
  }
}
```

### 8.2 `GET /dishes/{id}`

按 ID 查询菜品。

- 权限：`ADMIN`、`MERCHANT`
- `MERCHANT` 只能查看自己摊位下的菜品
- `id` 必须大于 `0`
- 查不到时当前返回 `code=1, data=null`

### 8.3 `POST /dishes`

新增菜品。

- 权限：`ADMIN`、`MERCHANT`
- Content-Type：`application/json`
- `ADMIN` 可在任意有效商家摊位下新增菜品
- `MERCHANT` 只能在自己的摊位下新增菜品
- `stallId`、`name`、`price` 必填
- 同一摊位内未删除菜品的 `name` 唯一；不同摊位允许使用相同菜品名
- `price` 不能小于 `0`
- `isSoldOut` 省略时默认为 `0`
- 目标摊位不存在时返回 `摊位不存在`

请求示例：

```json
{
  "stallId": 1,
  "name": "豪华烤冷面",
  "price": 15.00,
  "isSoldOut": 0
}
```

### 8.4 `PUT /dishes/{id}`

局部更新菜品。

- 权限：`ADMIN`、`MERCHANT`
- 请求体中的 `id` 可以省略；如果提供，必须与路径一致
- `stallId` 不允许修改，提供时返回 `stallId不可修改`
- `ADMIN` 可更新任意菜品；`MERCHANT` 只能更新自己摊位下的菜品

请求示例：

```json
{
  "name": "双蛋烤冷面",
  "price": 16.00,
  "isSoldOut": 1
}
```

### 8.5 `DELETE /dishes`

批量逻辑删除菜品。

- 权限：`ADMIN`、`MERCHANT`
- `ADMIN` 可删除任意有效菜品；`MERCHANT` 只能删除自己摊位下的菜品
- 批量删除为原子操作：请求中存在无权限或不存在的 ID 时整批拒绝，不进行部分删除

请求示例：

```http
DELETE /dishes?ids=1&ids=2
Authorization: Bearer <access_token>
```

## 9. 常见错误

| 场景 | 当前响应 |
| --- | --- |
| JSON 字段校验失败 | `HTTP 200, code=0`，`msg` 为字段校验信息 |
| 缺少必要查询参数 | `HTTP 200, code=0`，例如 `缺少必要参数: ids` |
| 用户名或手机号重复 | `HTTP 200, code=0` |
| 摊位不存在 | `HTTP 200, code=0, msg=摊位不存在` |
| 菜品不存在 | `HTTP 200, code=0, msg=菜品不存在` |
| JWT 缺失、格式错误或过期 | `HTTP 401`，当前无统一 JSON 响应体 |
| 角色权限不足 | `HTTP 403`，当前无统一 JSON 响应体 |
| 资源不属于当前商家 | `HTTP 200, code=0`，例如 `无权操作其他商家的菜品` |

## 10. 已知限制

- 摊位和菜品查询是后台管理接口，只允许 `ADMIN` 和 `MERCHANT`，尚未开放顾客浏览
- 用户禁用状态尚未参与登录判断
- 文件上传只校验大小和扩展名，没有校验真实文件内容
- 当前业务异常尚未全面映射为标准 HTTP 状态码

## 11. Roadmap

### 11.1 后台管理系统近期计划

1. 接入 OpenAPI/Swagger
2. 统一 401/403 JSON 响应
3. 增加少量真实 HTTP -> Service -> Mapper -> H2 端到端测试

### 11.2 外卖系统远期扩展

- 商家申请与管理员审批
- 游客浏览摊位和菜品
- 购物车
- 订单与订单明细
- 订单状态流转
- 独立 `File` 表和通用文件管理

远期功能不属于当前已实现 API，不应在当前接口总览中作为可用能力展示。
