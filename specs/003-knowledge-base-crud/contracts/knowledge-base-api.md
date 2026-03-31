# Knowledge Base API Contracts

本文件定义“知识库增删改查接口”的 REST 契约。响应统一使用现有 `ApiResponse` 结构：

```json
{
  "code": 0,
  "message": "success",
  "data": {}
}
```

错误时：

```json
{
  "code": 400,
  "message": "请求参数错误",
  "data": null
}
```

## 1. 创建知识库

- **Method**: `POST`
- **Path**: `/knowledge-bases`
- **Auth**: 必须登录

### Request Body

```json
{
  "name": "团队知识库",
  "description": "用于沉淀团队文档",
  "category": "技术",
  "tags": ["Java", "后端"]
}
```

### Success Response

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": 101,
    "ownerUserId": 2001,
    "name": "团队知识库",
    "description": "用于沉淀团队文档",
    "category": "技术",
    "tags": ["Java", "后端"],
    "status": 1,
    "createdAt": "2026-03-30T16:00:00",
    "updatedAt": "2026-03-30T16:00:00"
  }
}
```

## 2. 查询知识库详情

- **Method**: `GET`
- **Path**: `/knowledge-bases/{id}`
- **Auth**: 必须登录且资源归属当前用户

### Success Response

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": 101,
    "ownerUserId": 2001,
    "name": "团队知识库",
    "description": "用于沉淀团队文档",
    "category": "技术",
    "tags": ["Java", "后端"],
    "status": 1,
    "createdAt": "2026-03-30T16:00:00",
    "updatedAt": "2026-03-30T16:10:00"
  }
}
```

## 3. 分页查询知识库列表

- **Method**: `GET`
- **Path**: `/knowledge-bases`
- **Auth**: 必须登录

### Query Parameters

| 参数 | 必填 | 默认值 | 说明 |
|---|---|---|---|
| keyword | 否 | 空 | 名称/描述模糊匹配 |
| category | 否 | 空 | 分类过滤 |
| tag | 否 | 空 | 标签过滤 |
| page | 否 | 1 | 页码，最小 1 |
| size | 否 | 10 | 每页条数，最大 100 |

### Success Response

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "list": [
      {
        "id": 101,
        "ownerUserId": 2001,
        "name": "团队知识库",
        "description": "用于沉淀团队文档",
        "category": "技术",
        "tags": ["Java", "后端"],
        "status": 1,
        "createdAt": "2026-03-30T16:00:00",
        "updatedAt": "2026-03-30T16:10:00"
      }
    ],
    "total": 1,
    "page": 1,
    "size": 10
  }
}
```

## 4. 更新知识库

- **Method**: `PUT`
- **Path**: `/knowledge-bases/{id}`
- **Auth**: 必须登录且资源归属当前用户

### Request Body

```json
{
  "name": "团队知识库-升级版",
  "description": "补充架构与规范文档",
  "category": "技术",
  "tags": ["Java", "架构"],
  "lastKnownUpdatedAt": "2026-03-30T16:10:00"
}
```

### Concurrency Rule

- `lastKnownUpdatedAt` 必填。
- `lastKnownUpdatedAt` 使用 `yyyy-MM-ddTHH:mm:ss` 格式（示例：`2026-03-30T16:10:00`）。
- 若与服务端当前 `updatedAt` 不一致，返回冲突错误（`code=409`，`message=RESOURCE_CONFLICT`）。

### Success Response

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": 101,
    "ownerUserId": 2001,
    "name": "团队知识库-升级版",
    "description": "补充架构与规范文档",
    "category": "技术",
    "tags": ["Java", "架构"],
    "status": 1,
    "createdAt": "2026-03-30T16:00:00",
    "updatedAt": "2026-03-30T16:20:00"
  }
}
```

## 5. 删除知识库

- **Method**: `DELETE`
- **Path**: `/knowledge-bases/{id}`
- **Auth**: 必须登录且资源归属当前用户

### Success Response

```json
{
  "code": 0,
  "message": "success",
  "data": null
}
```

### Delete Behavior

- 删除采用软删除语义。
- 默认列表和详情查询不可见已删除资源。
- 重复删除请求按幂等处理，不应引发系统错误。

## 6. 错误码约定

| code | message | 场景 |
|---|---|---|
| 0 | success | 成功 |
| 400 | 请求参数错误 | 参数校验失败 |
| 401 | UNAUTHORIZED | 未登录或登录态失效 |
| 403 | 无权限访问该知识库 | 越权访问 |
| 404 | 知识库不存在 | 资源不存在或已删除不可见 |
| 409 | RESOURCE_CONFLICT | 并发更新冲突 |
| 500 | 服务器内部错误 | 未预期异常 |
