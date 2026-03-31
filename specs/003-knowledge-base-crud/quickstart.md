# Quickstart: 知识库 CRUD 接口

本指南用于快速验证“知识库增删改查接口”是否可用。

## 1. 前置条件

- 服务已启动（示例：`http://localhost:8080`）。
- 已完成登录并拿到 token。
- 后续请求通过 Header 携带认证信息（以实际拦截器配置为准）。

示例 Header：

```http
Authorization: Bearer <token>
```

## 2. 创建知识库

```bash
curl -X POST 'http://localhost:8080/api/knowledge-bases' \
  -H 'Content-Type: application/json' \
  -H 'Authorization: Bearer <token>' \
  -d '{
    "name": "团队知识库",
    "description": "用于沉淀团队文档",
    "category": "技术",
    "tags": ["Java", "后端"]
  }'
```

期望：返回 `code=0`，并包含新建 `id`。

## 3. 查询详情

```bash
curl -X GET 'http://localhost:8080/api/knowledge-bases/101' \
  -H 'Authorization: Bearer <token>'
```

期望：返回 `code=0`，字段包含 `name/description/category/tags/updatedAt`。

## 4. 分页列表查询

```bash
curl -X GET 'http://localhost:8080/api/knowledge-bases?keyword=团队&page=1&size=10' \
  -H 'Authorization: Bearer <token>'
```

期望：返回 `list/total/page/size`。

## 5. 更新知识库（含并发控制）

先从详情响应中取 `updatedAt`，再带入更新请求：

```bash
curl -X PUT 'http://localhost:8080/api/knowledge-bases/101' \
  -H 'Content-Type: application/json' \
  -H 'Authorization: Bearer <token>' \
  -d '{
    "name": "团队知识库-升级版",
    "description": "补充架构与规范文档",
    "category": "技术",
    "tags": ["Java", "架构"],
    "lastKnownUpdatedAt": "2026-03-30T16:10:00"
  }'
```

期望：
- 时间戳一致时更新成功（`code=0`）。
- 时间戳不一致时返回冲突（`code=409`，`message=RESOURCE_CONFLICT`）。

## 6. 删除知识库

```bash
curl -X DELETE 'http://localhost:8080/api/knowledge-bases/101' \
  -H 'Authorization: Bearer <token>'
```

期望：返回 `code=0`。

再次查询同一 ID：
- 默认应返回“知识库不存在”（`404`）。

## 7. 最小验收清单

- 创建、详情、列表、更新、删除链路可完整跑通。
- 未登录请求返回 `401`。
- 越权访问他人知识库返回 `403`。
- 并发冲突时返回 `409`。
- 删除后默认不可见，满足幂等删除语义。
