# Research: 知识库增删改查接口

## 决策 1：资源边界与字段映射

**Decision**

- 本期“知识库增删改查”以现有 `knowledge_base` 作为核心资源。
- 业务语义映射如下：
  - 标题 -> `name`
  - 正文描述 -> `description`
  - 可见状态 -> `status`
  - 所属用户 -> `owner_user_id`
- 分类与标签作为业务元信息字段纳入接口契约，首版可按“可选字段”落地，不影响基础 CRUD 主流程。

**Rationale**

- 仓库中已存在 `KnowledgeBase` 实体、Mapper、Service，可直接复用。
- 避免为了 CRUD 首版引入全新资源模型，降低改造范围。

**Alternatives considered**

- 新建“知识条目”实体与独立表：语义更细，但会与现有 `knowledge_base` 结构并行，造成重复建模。

---

## 决策 2：删除语义采用软删除优先

**Decision**

- 删除接口采用软删除语义：标记 `deleted_at`（并可联动 `status=0`）。
- 默认查询仅返回未删除资源。
- 重复删除返回幂等结果（视为成功或返回“已删除”提示，不产生额外副作用）。

**Rationale**

- 规格要求可追溯，软删除更利于审计与问题排查。
- 当前实体已具备 `deleted_at` 字段，落地成本低。

**Alternatives considered**

- 物理删除：实现简单，但不利于审计追踪和误删恢复。

---

## 决策 3：并发更新采用乐观校验

**Decision**

- 更新请求必须携带 `lastKnownUpdatedAt`（调用方最后读取到的更新时间）。
- 服务端写入前比对当前记录 `updated_at`：
  - 一致 -> 允许更新
  - 不一致 -> 拒绝并返回冲突错误（建议 409）

**Rationale**

- 满足“防止静默覆盖”的需求。
- 不强依赖新增数据库版本字段，易于在现有表结构上落地。

**Alternatives considered**

- 新增 `version` 字段并做版本号乐观锁：一致性明确，但需要数据库结构变更。
- 悲观锁：实现复杂度和锁竞争成本更高，不适合当前场景。

---

## 决策 4：权限模型采用 owner_user_id 强隔离

**Decision**

- 所有读写都基于当前登录用户身份，与 `owner_user_id` 做强校验。
- 对不存在或无权限资源统一返回业务可读错误，不泄露他人资源详情。

**Rationale**

- 与现有 Note 模块权限模型一致，降低认知与实现成本。
- 满足多用户隔离与最小暴露原则。

**Alternatives considered**

- 仅做“是否登录”校验，不做 owner 校验：会产生越权风险。

---

## 决策 5：审计采用结构化日志先行

**Decision**

- 对新增/更新/删除输出结构化日志事件，最小字段包括：
  - `operatorUserId`
  - `operationType`
  - `knowledgeBaseId`
  - `operationAt`
  - `result`
- 首版以应用日志满足可追溯，后续可扩展到独立审计表。

**Rationale**

- 无需新增基础设施即可满足追溯要求。
- 与当前 `AuthService` 日志风格一致，便于统一采集。

**Alternatives considered**

- 首版即上审计表：查询便利，但增加建表和持久化复杂度。

---

## 决策 6：查询策略与分页默认值

**Decision**

- 列表查询支持：`keyword`、`category`、`tag`、`page`、`size`。
- 分页默认值：`page=1`、`size=10`；`size` 上限 100。
- 关键词为空时返回用户可见资源的默认分页列表。

**Rationale**

- 与既有 Note 列表行为一致，调用方学习成本低。
- 可覆盖规格中的主要检索场景。

**Alternatives considered**

- 不分页直接全量返回：在数据规模增长时不可控。
