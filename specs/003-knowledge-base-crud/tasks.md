# Tasks: 知识库增删改查接口

**Input**: Design documents from `/specs/003-knowledge-base-crud/`
**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/, quickstart.md

**Tests**: 本特性按项目 TDD 偏好执行，任务中包含先测后实装的测试任务。

**Organization**: 任务按用户故事分组，保证每个故事可独立实现与验证。

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: 先建立知识库模块的接口与 DTO 基础骨架。

- [X] T001 创建创建请求 DTO 在 `src/main/java/com/ysx/agent/dto/CreateKnowledgeBaseRequest.java`
- [X] T002 [P] 创建更新请求 DTO 在 `src/main/java/com/ysx/agent/dto/UpdateKnowledgeBaseRequest.java`
- [X] T003 [P] 创建详情响应 DTO 在 `src/main/java/com/ysx/agent/dto/KnowledgeBaseResponse.java`
- [X] T004 [P] 创建列表响应 DTO 在 `src/main/java/com/ysx/agent/dto/KnowledgeBaseListResponse.java`
- [X] T005 创建知识库控制器骨架在 `src/main/java/com/ysx/agent/controller/KnowledgeBaseController.java`
- [X] T006 扩展知识库服务接口方法声明在 `src/main/java/com/ysx/agent/service/KnowledgeBaseService.java`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: 所有用户故事都依赖的底层能力（数据模型、Mapper、鉴权、异常）。

**⚠️ CRITICAL**: 本阶段未完成前，不开始任何用户故事实现。

- [X] T007 新增知识库字段迁移脚本在 `doc/sql/20260330_alter_knowledge_base_for_crud.sql`
- [X] T008 扩展知识库实体字段与访问器在 `src/main/java/com/ysx/agent/domain/KnowledgeBase.java`
- [X] T009 扩展知识库 Mapper 方法定义在 `src/main/java/com/ysx/agent/mapper/KnowledgeBaseMapper.java`
- [X] T010 扩展知识库 Mapper SQL 在 `src/main/resources/mapper/KnowledgeBaseMapper.xml`
- [X] T011 [P] 新增资源不存在异常在 `src/main/java/com/ysx/agent/exception/KnowledgeBaseNotFoundException.java`
- [X] T012 [P] 新增资源无权限异常在 `src/main/java/com/ysx/agent/exception/KnowledgeBaseAccessDeniedException.java`
- [X] T013 [P] 新增资源并发冲突异常在 `src/main/java/com/ysx/agent/exception/KnowledgeBaseConflictException.java`
- [X] T014 统一知识库异常响应处理在 `src/main/java/com/ysx/agent/exception/GlobalExceptionHandler.java`
- [X] T015 将知识库接口纳入登录拦截在 `src/main/java/com/ysx/agent/config/SaTokenConfig.java`
- [X] T016 在服务实现中建立公共校验与映射骨架在 `src/main/java/com/ysx/agent/service/impl/KnowledgeBaseServiceImpl.java`

**Checkpoint**: 基础层完成后，用户故事可以并行推进。

---

## Phase 3: User Story 1 - 维护知识条目 (Priority: P1) 🎯 MVP

**Goal**: 支持新增、更新、删除知识库条目并完成参数校验。

**Independent Test**: 调用创建接口得到 ID，再更新该条目，最后删除后默认查询不可见。

### Tests for User Story 1

- [X] T017 [P] [US1] 编写创建与更新服务测试在 `src/test/java/com/ysx/agent/service/KnowledgeBaseServiceImplTest.java`
- [X] T018 [P] [US1] 编写删除幂等与参数校验服务测试在 `src/test/java/com/ysx/agent/service/KnowledgeBaseServiceImplTest.java`
- [X] T019 [P] [US1] 编写新增更新删除接口测试在 `src/test/java/com/ysx/agent/controller/KnowledgeBaseControllerTest.java`

### Implementation for User Story 1

- [X] T020 [US1] 实现创建条目业务逻辑在 `src/main/java/com/ysx/agent/service/impl/KnowledgeBaseServiceImpl.java`
- [X] T021 [US1] 实现更新条目业务逻辑在 `src/main/java/com/ysx/agent/service/impl/KnowledgeBaseServiceImpl.java`
- [X] T022 [US1] 实现软删除与幂等删除逻辑在 `src/main/java/com/ysx/agent/service/impl/KnowledgeBaseServiceImpl.java`
- [X] T023 [US1] 实现创建更新删除接口在 `src/main/java/com/ysx/agent/controller/KnowledgeBaseController.java`
- [X] T024 [US1] 补全创建更新请求参数校验注解在 `src/main/java/com/ysx/agent/dto/CreateKnowledgeBaseRequest.java`
- [X] T025 [US1] 补全更新请求参数校验注解在 `src/main/java/com/ysx/agent/dto/UpdateKnowledgeBaseRequest.java`

**Checkpoint**: User Story 1 完成后，可独立完成“新增-更新-删除”闭环。

---

## Phase 4: User Story 2 - 查询与查看知识条目 (Priority: P2)

**Goal**: 支持关键词/分类/标签过滤的分页列表查询和详情查询。

**Independent Test**: 预置多条数据后，能按 keyword/category/tag 分页检索并查看单条详情。

### Tests for User Story 2

- [X] T026 [P] [US2] 编写列表过滤与分页服务测试在 `src/test/java/com/ysx/agent/service/KnowledgeBaseServiceImplTest.java`
- [X] T027 [P] [US2] 编写详情查询与不存在场景服务测试在 `src/test/java/com/ysx/agent/service/KnowledgeBaseServiceImplTest.java`
- [X] T028 [P] [US2] 编写列表和详情接口测试在 `src/test/java/com/ysx/agent/controller/KnowledgeBaseControllerTest.java`

### Implementation for User Story 2

- [X] T029 [US2] 实现列表查询 SQL（keyword/category/tag/page/size）在 `src/main/resources/mapper/KnowledgeBaseMapper.xml`
- [X] T030 [US2] 实现详情查询 SQL（仅返回 owner 可见资源）在 `src/main/resources/mapper/KnowledgeBaseMapper.xml`
- [X] T031 [US2] 实现列表与详情服务逻辑在 `src/main/java/com/ysx/agent/service/impl/KnowledgeBaseServiceImpl.java`
- [X] T032 [US2] 实现列表与详情接口在 `src/main/java/com/ysx/agent/controller/KnowledgeBaseController.java`
- [X] T033 [US2] 实现实体到响应 DTO 的映射在 `src/main/java/com/ysx/agent/dto/KnowledgeBaseResponse.java`
- [X] T034 [US2] 实现分页返回结构在 `src/main/java/com/ysx/agent/dto/KnowledgeBaseListResponse.java`

**Checkpoint**: User Story 2 完成后，可独立验证查询与查看能力。

---

## Phase 5: User Story 3 - 数据一致性与可追溯 (Priority: P3)

**Goal**: 实现并发冲突保护与结构化审计日志，避免静默覆盖。

**Independent Test**: 模拟同一条目并发编辑时后提交请求返回冲突；创建更新删除均有审计日志输出。

### Tests for User Story 3

- [X] T035 [P] [US3] 编写并发冲突服务测试在 `src/test/java/com/ysx/agent/service/KnowledgeBaseServiceImplTest.java`
- [X] T036 [P] [US3] 编写冲突响应接口测试在 `src/test/java/com/ysx/agent/controller/KnowledgeBaseControllerTest.java`
- [X] T037 [P] [US3] 编写审计日志事件测试在 `src/test/java/com/ysx/agent/service/KnowledgeBaseAuditLogTest.java`

### Implementation for User Story 3

- [X] T038 [US3] 实现基于 lastKnownUpdatedAt 的并发校验在 `src/main/java/com/ysx/agent/service/impl/KnowledgeBaseServiceImpl.java`
- [X] T039 [US3] 实现带更新时间条件的更新 SQL 在 `src/main/resources/mapper/KnowledgeBaseMapper.xml`
- [X] T040 [US3] 接入并发冲突异常与 409 响应在 `src/main/java/com/ysx/agent/exception/GlobalExceptionHandler.java`
- [X] T041 [US3] 实现新增更新删除结构化审计日志在 `src/main/java/com/ysx/agent/service/impl/KnowledgeBaseServiceImpl.java`
- [X] T042 [US3] 在更新请求中接入并发字段序列化与解析在 `src/main/java/com/ysx/agent/dto/UpdateKnowledgeBaseRequest.java`

**Checkpoint**: User Story 3 完成后，一致性与可追溯能力可独立验证。

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: 收敛契约、文档和回归验证。

- [X] T043 [P] 对齐接口契约与实际实现在 `specs/003-knowledge-base-crud/contracts/knowledge-base-api.md`
- [X] T044 [P] 对齐快速验证步骤与最终接口在 `specs/003-knowledge-base-crud/quickstart.md`
- [X] T045 执行并修复知识库相关回归测试在 `src/test/java/com/ysx/agent/controller/KnowledgeBaseControllerTest.java`
- [X] T046 [P] 补充服务层边界条件回归测试在 `src/test/java/com/ysx/agent/service/KnowledgeBaseServiceImplTest.java`

---

## Dependencies & Execution Order

### Phase Dependencies

- **Phase 1 (Setup)**: 无依赖，可立即开始。
- **Phase 2 (Foundational)**: 依赖 Phase 1，且阻塞所有用户故事。
- **Phase 3-5 (User Stories)**: 依赖 Phase 2 完成后开始。
- **Phase 6 (Polish)**: 依赖目标用户故事完成后执行。

### User Story Dependencies

- **US1 (P1)**: 仅依赖 Foundational，优先作为 MVP。
- **US2 (P2)**: 依赖 Foundational，可与 US1 并行但通常在 US1 后落地更稳妥。
- **US3 (P3)**: 依赖 US1 的更新能力与 US2 的查询能力。

### Within Each User Story

- 先写测试任务（TDD），并确保失败后再实现。
- 先改服务层与 Mapper，再接控制器。
- 完成故事内回归后再切换下一个故事。

### Parallel Opportunities

- Phase 1: T002/T003/T004 可并行。
- Phase 2: T011/T012/T013 可并行。
- US1: T017/T018/T019 可并行编写。
- US2: T026/T027/T028 可并行编写。
- US3: T035/T036/T037 可并行编写。
- Polish: T043/T044/T046 可并行。

---

## Parallel Example: User Story 1

```bash
Task: "T017 [US1] service create/update tests in src/test/java/com/ysx/agent/service/KnowledgeBaseServiceImplTest.java"
Task: "T018 [US1] service delete/validation tests in src/test/java/com/ysx/agent/service/KnowledgeBaseServiceImplTest.java"
Task: "T019 [US1] controller create/update/delete tests in src/test/java/com/ysx/agent/controller/KnowledgeBaseControllerTest.java"
```

## Parallel Example: User Story 2

```bash
Task: "T026 [US2] service list filter tests in src/test/java/com/ysx/agent/service/KnowledgeBaseServiceImplTest.java"
Task: "T027 [US2] service detail tests in src/test/java/com/ysx/agent/service/KnowledgeBaseServiceImplTest.java"
Task: "T028 [US2] controller list/detail tests in src/test/java/com/ysx/agent/controller/KnowledgeBaseControllerTest.java"
```

## Parallel Example: User Story 3

```bash
Task: "T035 [US3] conflict service tests in src/test/java/com/ysx/agent/service/KnowledgeBaseServiceImplTest.java"
Task: "T036 [US3] conflict controller tests in src/test/java/com/ysx/agent/controller/KnowledgeBaseControllerTest.java"
Task: "T037 [US3] audit log tests in src/test/java/com/ysx/agent/service/KnowledgeBaseAuditLogTest.java"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. 完成 Phase 1 与 Phase 2。
2. 完成 US1（T017-T025）并通过测试。
3. 在本地验证“新增-更新-删除”闭环后先行演示。

### Incremental Delivery

1. 在 MVP 基础上交付 US2（查询与查看）。
2. 再交付 US3（并发冲突 + 审计）。
3. 最后执行 Polish 阶段统一收口。

### Parallel Team Strategy

1. 一人负责 Mapper/Service 主线（T020/T021/T029/T031/T038/T041）。
2. 一人负责 Controller 与异常映射（T023/T032/T040）。
3. 一人负责测试与文档收口（T017-T019/T026-T028/T035-T037/T043-T046）。
