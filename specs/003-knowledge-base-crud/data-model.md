# Data Model: 知识库增删改查接口

## 1. Core Entity: KnowledgeBase

知识库实体是本功能的核心资源，表示用户拥有的独立知识库空间。

| 字段 | 含义 | 规则 |
|---|---|---|
| id | 知识库唯一标识 | 系统生成，外部不可修改 |
| ownerUserId | 所属用户 | 必填；用于所有读写权限校验 |
| name | 知识库名称 | 必填；同一用户下唯一；建议 1-255 字符 |
| description | 知识库描述 | 可选；用于详情展示与关键词检索 |
| category | 知识库分类 | 可选；用于列表筛选 |
| tags | 知识库标签集合 | 可选；用于多维筛选 |
| status | 状态 | `1=可用`，`0=禁用/不可见` |
| createdAt | 创建时间 | 创建时写入，后续只读 |
| updatedAt | 更新时间 | 每次更新写入；用于并发控制 |
| deletedAt | 删除时间 | 软删除标识；为空表示未删除 |

## 2. Audit Model: KnowledgeBaseChangeLog

用于追踪新增/更新/删除操作，首版可落在结构化日志中。

| 字段 | 含义 | 规则 |
|---|---|---|
| operationType | 操作类型 | `CREATE` / `UPDATE` / `DELETE` |
| knowledgeBaseId | 目标资源 ID | 必填 |
| operatorUserId | 操作者 ID | 必填 |
| operationAt | 操作时间 | 必填 |
| result | 操作结果 | `SUCCESS` / `FAILED` |
| reason | 失败原因 | 失败时记录，可选 |

## 3. Relationships

- `UserAccount (1) -> (N) KnowledgeBase`
- `KnowledgeBase (1) -> (N) KnowledgeBaseChangeLog`（逻辑关系，日志可落到文件或持久化）

## 4. Validation Rules

| 字段/行为 | 规则 |
|---|---|
| name | 不能为空；去除首尾空格后不能为空 |
| category | 如提供，不能为空字符串 |
| tags | 如提供，单标签不能为空；标签数量设置合理上限（如 20） |
| 查询 page | 小于 1 时按 1 处理 |
| 查询 size | 小于 1 时按 10；大于上限时截断到 100 |
| 删除 | 仅允许 owner 删除；重复删除幂等 |

## 5. State Transitions

| 当前状态 | 触发操作 | 结果状态 |
|---|---|---|
| ACTIVE (deletedAt = null, status=1) | UPDATE | ACTIVE |
| ACTIVE (deletedAt = null, status=1) | DELETE | DELETED (deletedAt != null, status=0) |
| DELETED (deletedAt != null) | LIST/GET 默认查询 | 不可见 |

## 6. Concurrency Rule

更新请求必须携带 `lastKnownUpdatedAt`：
- 当 `lastKnownUpdatedAt == current.updatedAt` 时允许更新。
- 当二者不一致时拒绝更新，返回“资源已变更，请刷新后重试”。

## 7. DTO Sketch

### CreateKnowledgeBaseRequest

```json
{
  "name": "团队知识库",
  "description": "用于沉淀团队技术文档",
  "category": "技术",
  "tags": ["Java", "后端"]
}
```

### UpdateKnowledgeBaseRequest

```json
{
  "name": "团队知识库-新版",
  "description": "补充开发规范",
  "category": "技术",
  "tags": ["Java", "规范"],
  "lastKnownUpdatedAt": "2026-03-30T15:00:00"
}
```

### KnowledgeBaseResponse

```json
{
  "id": 101,
  "ownerUserId": 2001,
  "name": "团队知识库",
  "description": "用于沉淀团队技术文档",
  "category": "技术",
  "tags": ["Java", "后端"],
  "status": 1,
  "createdAt": "2026-03-30T14:20:00",
  "updatedAt": "2026-03-30T15:00:00"
}
```
