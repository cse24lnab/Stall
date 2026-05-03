# `stall_manage` REST 接口文档

## 1. 文档说明

- 文档版本：`v1`
- 接口前缀：`/api/v1`
- 鉴权方式：`JWT + Bearer Token`
- 统一响应：`Result<T>`
- 面向角色：`USER`、`MERCHANT`、`ADMIN`

> 说明 1：当前代码里已落地的接口只有 `GET /stalls`、`POST /stalls`、`GET /dishes`、`POST /dishes`。  
> 说明 2：当前代码中的路径还没有统一加上 `/api/v1` 前缀，本文档按后续规范化方案编写。  
> 说明 3：当前 `User`、`Order` 还是占位实体，本文档中的字段设计用于后续实现基线。  

## 2. 项目目标

本项目目标是实现一个类似外卖软件的网站，支持以下核心能力：

- 普通用户注册、登录、查看个人信息
- 商家维护摊位和菜品
- 用户上传头像、菜品图、摊位图等文件
- 用户浏览摊位和菜品，加入购物车并提交订单
- 用户查看订单列表
- 商家查看并处理与自己摊位相关的订单

## 3. 通用约定

### 3.1 统一响应格式

```json
{
  "code": 1,
  "msg": "success",
  "data": {}
}
```

- `code = 1`：请求成功
- `code = 0`：请求失败
- `msg`：成功或错误说明
- `data`：响应数据；无数据时可为 `null`

失败示例：

```json
{
  "code": 0,
  "msg": "摊位不存在",
  "data": null
}
```

### 3.2 鉴权请求头

除注册、登录和公开查询接口外，其余接口默认要求携带：

```http
Authorization: Bearer <access_token>
```

### 3.3 HTTP 方法约定

- `GET`：查询资源
- `POST`：创建资源
- `PUT`：完整更新资源
- `PATCH`：局部更新资源或更新状态
- `DELETE`：逻辑删除资源

### 3.4 分页约定

推荐所有列表接口统一支持以下分页参数：

| 参数 | 类型 | 默认值 | 说明 |
| --- | --- | --- | --- |
| `page` | integer | `1` | 页码，从 1 开始 |
| `pageSize` | integer | `10` | 每页条数，建议最大 `100` |
| `sortBy` | string | `createTime` | 排序字段 |
| `sortOrder` | string | `desc` | 排序方式，`asc` 或 `desc` |

分页响应建议统一为：

```json
{
  "code": 1,
  "msg": "success",
  "data": {
    "list": [],
    "total": 0,
    "page": 1,
    "pageSize": 10
  }
}
```

> 当前已实现的 `/stalls` 和 `/dishes` 暂时仍返回数组；后续建议统一升级为分页对象。

### 3.5 角色权限约定

| 角色 | 说明 |
| --- | --- |
| `USER` | 普通用户，可浏览、加购物车、下单、查看自己的订单 |
| `MERCHANT` | 商家，可维护自己的摊位和菜品，处理自己摊位相关订单 |
| `ADMIN` | 管理员，可查看或管理全部资源 |

### 3.6 常见错误场景

| HTTP 状态码 | `code` | 场景 | 示例 `msg` |
| --- | --- | --- | --- |
| `400` | `0` | 参数错误、校验失败 | `价格不能为空` |
| `401` | `0` | 未登录或 token 无效 | `未登录或登录已过期` |
| `403` | `0` | 权限不足 | `无权操作该资源` |
| `404` | `0` | 资源不存在 | `摊位不存在` |
| `409` | `0` | 状态冲突或重复数据 | `用户名已存在` |
| `500` | `0` | 服务异常 | `服务器出错，稍后再试` |

> 当前项目现状可能仍以 `200 + code=0` 返回部分业务错误；新接口建议同时对齐标准 HTTP 状态码。

## 4. 数据模型

## 4.1 User

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `id` | integer | 是 | 用户 ID |
| `username` | string | 是 | 登录用户名，唯一 |
| `nickname` | string | 是 | 展示昵称 |
| `phone` | string | 否 | 手机号，建议唯一 |
| `avatarFileId` | integer | 否 | 头像文件 ID |
| `avatarUrl` | string | 否 | 头像访问地址 |
| `role` | string | 是 | `USER` / `MERCHANT` / `ADMIN` |
| `status` | string | 是 | `ACTIVE` / `DISABLED` |
| `createTime` | string | 是 | 创建时间，ISO-8601 |
| `updateTime` | string | 是 | 更新时间，ISO-8601 |

## 4.2 File

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `fileId` | integer | 是 | 文件 ID |
| `url` | string | 是 | 文件访问地址 |
| `fileName` | string | 是 | 原始文件名 |
| `contentType` | string | 是 | MIME 类型 |
| `size` | integer | 是 | 文件大小，单位字节 |
| `uploadedBy` | integer | 是 | 上传人用户 ID |
| `createTime` | string | 是 | 上传时间 |

## 4.3 Stall

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `id` | integer | 是 | 摊位 ID |
| `name` | string | 是 | 摊位名称 |
| `ownerUserId` | integer | 否 | 商家用户 ID，规划字段 |
| `currentStatus` | integer | 是 | `0=休息`，`1=营业` |
| `noonLocation` | string | 否 | 午市位置 |
| `eveningLocation` | string | 否 | 晚市位置 |
| `noonStartTime` | string | 否 | 午市开始时间，`HH:mm:ss` |
| `noonEndTime` | string | 否 | 午市结束时间，`HH:mm:ss` |
| `eveningStartTime` | string | 否 | 晚市开始时间 |
| `eveningEndTime` | string | 否 | 晚市结束时间 |
| `coverFileId` | integer | 否 | 摊位封面文件 ID，规划字段 |
| `coverUrl` | string | 否 | 摊位封面地址，规划字段 |
| `isDelete` | integer | 是 | `0=未删除`，`1=已删除` |
| `createTime` | string | 是 | 创建时间 |
| `updateTime` | string | 是 | 更新时间 |

## 4.4 Dish

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `id` | integer | 是 | 菜品 ID |
| `stallId` | integer | 是 | 所属摊位 ID |
| `name` | string | 是 | 菜品名称 |
| `description` | string | 否 | 菜品描述，规划字段 |
| `price` | number | 是 | 菜品价格 |
| `isSoldOut` | integer | 是 | `0=有货`，`1=售罄` |
| `imageFileId` | integer | 否 | 菜品图片文件 ID，规划字段 |
| `imageUrl` | string | 否 | 菜品图片地址，规划字段 |
| `isDelete` | integer | 是 | `0=未删除`，`1=已删除` |
| `createTime` | string | 是 | 创建时间 |
| `updateTime` | string | 是 | 更新时间 |

## 4.5 CartItem

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `itemId` | integer | 是 | 购物车项 ID |
| `userId` | integer | 是 | 用户 ID |
| `stallId` | integer | 是 | 摊位 ID |
| `stallName` | string | 是 | 摊位名称快照 |
| `dishId` | integer | 是 | 菜品 ID |
| `dishName` | string | 是 | 菜品名称快照 |
| `dishImageUrl` | string | 否 | 菜品图片 |
| `price` | number | 是 | 加入购物车时的价格快照 |
| `quantity` | integer | 是 | 数量，最小为 1 |
| `amount` | number | 是 | 小计金额 |
| `createTime` | string | 是 | 创建时间 |
| `updateTime` | string | 是 | 更新时间 |

## 4.6 Order

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `id` | integer | 是 | 订单 ID |
| `userId` | integer | 是 | 下单用户 ID |
| `stallId` | integer | 是 | 摊位 ID |
| `status` | string | 是 | `PENDING` / `PREPARING` / `READY_FOR_PICKUP` / `COMPLETED` / `CANCELLED` |
| `totalAmount` | number | 是 | 订单总金额 |
| `pickupTime` | string | 是 | 预约取餐时间 |
| `pickupLocation` | string | 是 | 取餐地点快照 |
| `remark` | string | 否 | 订单备注 |
| `items` | array | 是 | 订单明细 |
| `createTime` | string | 是 | 创建时间 |
| `updateTime` | string | 是 | 更新时间 |

## 4.7 OrderItem

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `dishId` | integer | 是 | 菜品 ID |
| `dishNameSnapshot` | string | 是 | 菜品名快照 |
| `priceSnapshot` | number | 是 | 单价快照 |
| `quantity` | integer | 是 | 购买数量 |
| `amount` | number | 是 | 明细金额 |

## 5. 接口总览

| 模块 | 方法 | 路径 | 说明 | 状态 |
| --- | --- | --- | --- | --- |
| 认证 | `POST` | `/auth/register` | 普通用户注册 | 规划接口 |
| 认证 | `POST` | `/auth/login` | 登录并获取 JWT | 规划接口 |
| 认证 | `GET` | `/auth/me` | 获取当前登录用户 | 规划接口 |
| 用户 | `PUT` | `/users/me` | 修改个人资料 | 规划接口 |
| 用户 | `PUT` | `/users/me/password` | 修改密码 | 规划接口 |
| 文件 | `POST` | `/files` | 通用文件上传 | 规划接口 |
| 文件 | `GET` | `/files/{id}` | 文件元数据 | 规划接口 |
| 文件 | `DELETE` | `/files/{id}` | 删除文件 | 规划接口 |
| 摊位 | `GET` | `/stalls` | 摊位列表查询 | 已有基础实现 |
| 摊位 | `GET` | `/stalls/{id}` | 摊位详情 | 规划接口 |
| 摊位 | `POST` | `/stalls` | 创建摊位 | 已有基础实现 |
| 摊位 | `PUT` | `/stalls/{id}` | 更新摊位 | 规划接口 |
| 摊位 | `DELETE` | `/stalls/{id}` | 逻辑删除摊位 | 规划接口 |
| 菜品 | `GET` | `/dishes` | 菜品列表查询 | 已有基础实现 |
| 菜品 | `GET` | `/dishes/{id}` | 菜品详情 | 规划接口 |
| 菜品 | `POST` | `/dishes` | 创建菜品 | 已有基础实现 |
| 菜品 | `PUT` | `/dishes/{id}` | 更新菜品 | 规划接口 |
| 菜品 | `PATCH` | `/dishes/{id}` | 更新菜品状态 | 规划接口 |
| 菜品 | `DELETE` | `/dishes/{id}` | 逻辑删除菜品 | 规划接口 |
| 购物车 | `GET` | `/users/me/cart-items` | 查询购物车 | 规划接口 |
| 购物车 | `POST` | `/users/me/cart-items` | 加入购物车 | 规划接口 |
| 购物车 | `PUT` | `/users/me/cart-items/{itemId}` | 修改数量 | 规划接口 |
| 购物车 | `DELETE` | `/users/me/cart-items/{itemId}` | 删除单项 | 规划接口 |
| 购物车 | `DELETE` | `/users/me/cart-items` | 清空购物车 | 规划接口 |
| 订单 | `POST` | `/orders` | 提交订单 | 规划接口 |
| 订单 | `GET` | `/orders` | 订单列表 | 规划接口 |
| 订单 | `GET` | `/orders/{id}` | 订单详情 | 规划接口 |
| 订单 | `PATCH` | `/orders/{id}` | 更新订单状态 | 规划接口 |

## 6. 认证接口

## 6.1 `POST /auth/register`

- 用途：普通用户注册
- 权限：匿名

请求体：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `username` | string | 是 | 用户名，唯一 |
| `password` | string | 是 | 密码，建议 8 位以上 |
| `nickname` | string | 是 | 昵称 |
| `phone` | string | 否 | 手机号 |

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
    "id": 101,
    "username": "alice01",
    "nickname": "Alice",
    "phone": "13800000000",
    "role": "USER",
    "status": "ACTIVE"
  }
}
```

失败示例：

```json
{
  "code": 0,
  "msg": "用户名已存在",
  "data": null
}
```

## 6.2 `POST /auth/login`

- 用途：登录并获取 JWT
- 权限：匿名

请求体：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `username` | string | 否 | 用户名，与 `phone` 二选一 |
| `phone` | string | 否 | 手机号，与 `username` 二选一 |
| `password` | string | 是 | 登录密码 |

请求示例：

```json
{
  "username": "alice01",
  "password": "12345678"
}
```

成功响应：

```json
{
  "code": 1,
  "msg": "success",
  "data": {
    "accessToken": "jwt-token-value",
    "tokenType": "Bearer",
    "expiresIn": 7200,
    "user": {
      "id": 101,
      "username": "alice01",
      "nickname": "Alice",
      "role": "USER",
      "status": "ACTIVE"
    }
  }
}
```

失败示例：

```json
{
  "code": 0,
  "msg": "用户名或密码错误",
  "data": null
}
```

## 6.3 `GET /auth/me`

- 用途：获取当前登录用户信息
- 权限：已登录用户

成功响应：

```json
{
  "code": 1,
  "msg": "success",
  "data": {
    "id": 101,
    "username": "alice01",
    "nickname": "Alice",
    "phone": "13800000000",
    "avatarFileId": 11,
    "avatarUrl": "https://cdn.example.com/avatar/11.png",
    "role": "USER",
    "status": "ACTIVE",
    "createTime": "2026-05-03T10:00:00",
    "updateTime": "2026-05-03T10:20:00"
  }
}
```

## 7. 用户接口

## 7.1 `PUT /users/me`

- 用途：修改个人资料
- 权限：已登录用户

请求体：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `nickname` | string | 否 | 昵称 |
| `phone` | string | 否 | 手机号 |
| `avatarFileId` | integer | 否 | 头像文件 ID |

请求示例：

```json
{
  "nickname": "Alice Zhang",
  "phone": "13800000001",
  "avatarFileId": 22
}
```

## 7.2 `PUT /users/me/password`

- 用途：修改密码
- 权限：已登录用户

请求体：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `oldPassword` | string | 是 | 旧密码 |
| `newPassword` | string | 是 | 新密码 |

失败示例：

```json
{
  "code": 0,
  "msg": "旧密码错误",
  "data": null
}
```

## 8. 文件接口

## 8.1 `POST /files`

- 用途：通用文件上传
- 权限：已登录用户
- Content-Type：`multipart/form-data`

表单字段：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `file` | file | 是 | 上传文件 |
| `bizType` | string | 否 | 业务类型，建议值：`avatar`、`stall-cover`、`dish-image` |

成功响应：

```json
{
  "code": 1,
  "msg": "success",
  "data": {
    "fileId": 301,
    "url": "https://cdn.example.com/upload/301.png",
    "fileName": "cover.png",
    "contentType": "image/png",
    "size": 20480,
    "uploadedBy": 101,
    "createTime": "2026-05-03T14:20:00"
  }
}
```

## 8.2 `GET /files/{id}`

- 用途：查询文件元数据
- 权限：已登录用户

## 8.3 `DELETE /files/{id}`

- 用途：删除或失效文件
- 权限：文件所有者、管理员

## 9. 摊位接口

## 9.1 `GET /stalls`

- 用途：查询摊位列表
- 权限：公开；若使用 `mine=true`，则要求 `MERCHANT` 或 `ADMIN`
- 状态：已有基础实现，当前代码支持按 `id`、`name` 查询；以下筛选和分页为规范扩展

查询参数：

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `page` | integer | 否 | 页码 |
| `pageSize` | integer | 否 | 每页数量 |
| `name` | string | 否 | 摊位名称模糊搜索 |
| `currentStatus` | integer | 否 | `0` 或 `1` |
| `mine` | boolean | 否 | 是否只看当前商家自己的摊位 |
| `ownerUserId` | integer | 否 | 管理员按商家筛选 |
| `includeDeleted` | boolean | 否 | 管理员是否包含已删除数据 |

成功响应示例：

```json
{
  "code": 1,
  "msg": "success",
  "data": {
    "list": [
      {
        "id": 1,
        "name": "烤冷面",
        "ownerUserId": 2001,
        "currentStatus": 1,
        "noonLocation": "东区",
        "eveningLocation": "西区",
        "coverUrl": "https://cdn.example.com/stalls/1.png"
      }
    ],
    "total": 1,
    "page": 1,
    "pageSize": 10
  }
}
```

## 9.2 `GET /stalls/{id}`

- 用途：查询单个摊位详情
- 权限：公开

## 9.3 `POST /stalls`

- 用途：创建摊位
- 权限：`MERCHANT`、`ADMIN`
- 状态：已有基础实现，当前代码已支持基础创建

请求体：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `name` | string | 是 | 摊位名称 |
| `currentStatus` | integer | 否 | 默认 `0` |
| `noonLocation` | string | 否 | 午市位置 |
| `eveningLocation` | string | 否 | 晚市位置 |
| `noonStartTime` | string | 否 | 午市开始时间 |
| `noonEndTime` | string | 否 | 午市结束时间 |
| `eveningStartTime` | string | 否 | 晚市开始时间 |
| `eveningEndTime` | string | 否 | 晚市结束时间 |
| `coverFileId` | integer | 否 | 摊位封面文件 ID |

请求示例：

```json
{
  "name": "张记煎饼",
  "currentStatus": 1,
  "noonLocation": "一食堂门口",
  "eveningLocation": "西操场",
  "noonStartTime": "11:00:00",
  "noonEndTime": "13:30:00",
  "eveningStartTime": "17:00:00",
  "eveningEndTime": "21:00:00",
  "coverFileId": 301
}
```

## 9.4 `PUT /stalls/{id}`

- 用途：更新摊位信息
- 权限：该摊位所属商家、管理员

## 9.5 `DELETE /stalls/{id}`

- 用途：逻辑删除摊位
- 权限：该摊位所属商家、管理员
- 说明：底层建议把 `isDelete` 设置为 `1`

成功响应：

```json
{
  "code": 1,
  "msg": "success",
  "data": null
}
```

## 10. 菜品接口

## 10.1 `GET /dishes`

- 用途：查询菜品列表
- 权限：公开
- 状态：已有基础实现，当前代码支持按 `stallId`、`name`、`price` 查询；以下分页和售罄筛选为规范扩展

查询参数：

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `page` | integer | 否 | 页码 |
| `pageSize` | integer | 否 | 每页数量 |
| `stallId` | integer | 否 | 所属摊位 ID |
| `name` | string | 否 | 菜品名称 |
| `isSoldOut` | integer | 否 | `0=有货`，`1=售罄` |
| `minPrice` | number | 否 | 最低价格，规划扩展 |
| `maxPrice` | number | 否 | 最高价格，规划扩展 |
| `includeDeleted` | boolean | 否 | 管理员是否包含已删除数据 |

成功响应示例：

```json
{
  "code": 1,
  "msg": "success",
  "data": {
    "list": [
      {
        "id": 10,
        "stallId": 1,
        "name": "招牌烤冷面",
        "description": "双蛋双肠",
        "price": 12.5,
        "isSoldOut": 0,
        "imageUrl": "https://cdn.example.com/dishes/10.png"
      }
    ],
    "total": 1,
    "page": 1,
    "pageSize": 10
  }
}
```

## 10.2 `GET /dishes/{id}`

- 用途：查询菜品详情
- 权限：公开

## 10.3 `POST /dishes`

- 用途：创建菜品
- 权限：该摊位所属商家、管理员
- 状态：已有基础实现，当前代码会校验 `stallId` 是否存在，并对 `isSoldOut` 赋默认值

请求体：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `stallId` | integer | 是 | 所属摊位 ID |
| `name` | string | 是 | 菜品名称 |
| `description` | string | 否 | 菜品描述 |
| `price` | number | 是 | 菜品价格 |
| `isSoldOut` | integer | 否 | 默认 `1` |
| `imageFileId` | integer | 否 | 菜品图片文件 ID |

请求示例：

```json
{
  "stallId": 1,
  "name": "豪华烤冷面",
  "description": "加蛋加肠加芝士",
  "price": 15.0,
  "isSoldOut": 0,
  "imageFileId": 302
}
```

失败示例：

```json
{
  "code": 0,
  "msg": "摊位不存在",
  "data": null
}
```

## 10.4 `PUT /dishes/{id}`

- 用途：完整更新菜品
- 权限：该摊位所属商家、管理员

## 10.5 `PATCH /dishes/{id}`

- 用途：更新菜品局部状态，例如售罄状态
- 权限：该摊位所属商家、管理员

请求体示例：

```json
{
  "isSoldOut": 1
}
```

## 10.6 `DELETE /dishes/{id}`

- 用途：逻辑删除菜品
- 权限：该摊位所属商家、管理员
- 说明：底层建议把 `isDelete` 设置为 `1`

## 11. 购物车接口

## 11.1 `GET /users/me/cart-items`

- 用途：获取当前用户购物车
- 权限：`USER`

成功响应示例：

```json
{
  "code": 1,
  "msg": "success",
  "data": {
    "list": [
      {
        "itemId": 9001,
        "stallId": 1,
        "stallName": "烤冷面",
        "dishId": 10,
        "dishName": "招牌烤冷面",
        "dishImageUrl": "https://cdn.example.com/dishes/10.png",
        "price": 12.5,
        "quantity": 2,
        "amount": 25.0
      }
    ],
    "totalAmount": 25.0
  }
}
```

## 11.2 `POST /users/me/cart-items`

- 用途：加入购物车
- 权限：`USER`

请求体：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `dishId` | integer | 是 | 菜品 ID |
| `quantity` | integer | 是 | 数量，最小为 1 |

请求示例：

```json
{
  "dishId": 10,
  "quantity": 2
}
```

失败示例：

```json
{
  "code": 0,
  "msg": "菜品已售罄",
  "data": null
}
```

## 11.3 `PUT /users/me/cart-items/{itemId}`

- 用途：修改购物车项数量
- 权限：`USER`

请求体示例：

```json
{
  "quantity": 3
}
```

## 11.4 `DELETE /users/me/cart-items/{itemId}`

- 用途：删除购物车单项
- 权限：`USER`

## 11.5 `DELETE /users/me/cart-items`

- 用途：清空购物车
- 权限：`USER`

## 12. 订单接口

## 12.1 `POST /orders`

- 用途：从当前购物车提交订单
- 权限：`USER`
- 说明：建议一次订单只包含同一摊位的菜品，便于商家处理

请求体：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `pickupTime` | string | 是 | 预约取餐时间 |
| `remark` | string | 否 | 订单备注 |

请求示例：

```json
{
  "pickupTime": "2026-05-03T18:20:00",
  "remark": "少辣，不放香菜"
}
```

成功响应示例：

```json
{
  "code": 1,
  "msg": "success",
  "data": {
    "id": 50001,
    "userId": 101,
    "stallId": 1,
    "status": "PENDING",
    "totalAmount": 25.0,
    "pickupTime": "2026-05-03T18:20:00",
    "pickupLocation": "西操场",
    "remark": "少辣，不放香菜",
    "items": [
      {
        "dishId": 10,
        "dishNameSnapshot": "招牌烤冷面",
        "priceSnapshot": 12.5,
        "quantity": 2,
        "amount": 25.0
      }
    ]
  }
}
```

## 12.2 `GET /orders`

- 用途：查询订单列表
- 权限：
  - `USER`：只能看自己的订单
  - `MERCHANT`：看自己摊位的订单
  - `ADMIN`：可看全部订单

查询参数：

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `page` | integer | 否 | 页码 |
| `pageSize` | integer | 否 | 每页数量 |
| `status` | string | 否 | 订单状态 |
| `stallId` | integer | 否 | 商家或管理员按摊位筛选 |
| `userId` | integer | 否 | 管理员按用户筛选 |
| `dateFrom` | string | 否 | 开始时间 |
| `dateTo` | string | 否 | 结束时间 |
| `mine` | boolean | 否 | 是否只看当前登录人相关订单，默认 `true` |

## 12.3 `GET /orders/{id}`

- 用途：查询订单详情
- 权限：订单所属用户、摊位所属商家、管理员

## 12.4 `PATCH /orders/{id}`

- 用途：更新订单状态
- 权限：依赖状态流转规则

请求体：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `status` | string | 是 | 目标状态 |
| `remark` | string | 否 | 状态变更备注 |

请求示例：

```json
{
  "status": "READY_FOR_PICKUP",
  "remark": "已制作完成，请尽快取餐"
}
```

### 12.4.1 订单状态流转规则

| 操作者 | 允许流转 |
| --- | --- |
| `USER` | `PENDING -> CANCELLED` |
| `USER` | `READY_FOR_PICKUP -> COMPLETED` |
| `MERCHANT` | `PENDING -> PREPARING` |
| `MERCHANT` | `PREPARING -> READY_FOR_PICKUP` |
| `MERCHANT` | `PENDING -> CANCELLED` |
| `MERCHANT` | `PREPARING -> CANCELLED` |
| `ADMIN` | 可人工介入调整终态，需记录备注 |

失败示例：

```json
{
  "code": 0,
  "msg": "当前订单状态不允许该操作",
  "data": null
}
```

## 13. 现有代码与文档差异说明

当前项目中已经存在以下实际接口：

- `GET /stalls`
- `POST /stalls`
- `GET /dishes`
- `POST /dishes`

当前代码与本文档的主要差异：

- 当前 Controller 路径尚未统一加 `/api/v1`
- 当前列表接口返回的是数组，不是分页对象
- 当前 `stall` 和 `dish` 只实现了查和增，尚未实现改和删
- 当前没有登录、文件上传、购物车、订单相关代码
- 当前 `User`、`Order` 仍为空实体，需要按本文档补齐

## 14. 端到端业务流程示例

## 14.1 普通用户下单流程

1. 用户注册：`POST /auth/register`
2. 用户登录：`POST /auth/login`
3. 浏览摊位：`GET /stalls`
4. 浏览菜品：`GET /dishes?stallId=1`
5. 加入购物车：`POST /users/me/cart-items`
6. 查看购物车：`GET /users/me/cart-items`
7. 提交订单：`POST /orders`
8. 查看订单列表：`GET /orders`
9. 查看订单详情：`GET /orders/{id}`
10. 商家备餐完成后，用户确认取餐：`PATCH /orders/{id}`

## 14.2 商家管理流程

1. 商家登录：`POST /auth/login`
2. 上传摊位封面：`POST /files`
3. 创建摊位：`POST /stalls`
4. 上传菜品图片：`POST /files`
5. 创建菜品：`POST /dishes`
6. 修改菜品信息：`PUT /dishes/{id}`
7. 菜品售罄时更新状态：`PATCH /dishes/{id}`
8. 查看自己摊位订单：`GET /orders?mine=true`
9. 接单制作：`PATCH /orders/{id}` 设置为 `PREPARING`
10. 可取餐时更新状态：`PATCH /orders/{id}` 设置为 `READY_FOR_PICKUP`

## 15. 建议的后续实现顺序

建议按下面顺序推进代码实现：

1. 登录鉴权与 `User` 实体
2. 文件上传接口
3. `stall` / `dish` 的完整 CRUD
4. 购物车接口
5. 订单提交与订单状态流转

