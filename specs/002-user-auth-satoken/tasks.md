# Tasks: 用户登录注册与认证

**Input**: Design documents from `/specs/002-user-auth-satoken/`
**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/, quickstart.md

**Tests**: 本功能为认证与安全相关能力，推荐采用 TDD。以下任务中包含必要的测试任务，均标记在对应 User Story 阶段。

**Organization**: 按用户故事分组，确保每个故事可以独立实现与验证。

## Format: `[ID] [P?] [Story] Description`

- **[P]**: 可并行执行（不同文件且无依赖）
- **[Story]**: 所属用户故事（US1, US2, US3）
- 任务描述中包含精确文件路径

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: 为认证模块准备基础依赖与项目结构（覆盖本功能专属的最小集）。

- [x] T001 检查并在 `pom.xml` 中引入 Sa-Token、Spring Validation、BCrypt 等所需依赖
- [x] T002 在 `src/main/java/com/ysx/agent/config/SaTokenConfig.java` 中新增/完善 Sa-Token 全局配置类（开启登录拦截、统一未登录处理框架）
- [x] T003 在 `src/main/java/com/ysx/agent/config` 下检查/补充全局异常与 ApiResponse 结构的约定（确保认证错误可统一返回）

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: 所有用户故事都依赖的核心基础能力。

**⚠️ CRITICAL**: 完成本阶段前，不应开始 User Story 实现。

- [x] T004 定义用户实体 `src/main/java/com/ysx/agent/domain/UserAccount.java`（字段参照 data-model.md：id, phone/email 标识、passwordHash 对应字段、status, createdAt, updatedAt）
- [x] T005 创建用户表 Mapper 接口 `src/main/java/com/ysx/agent/mapper/UserAccountMapper.java`，并配置 MyBatis-Plus 映射
- [x] T006 在 `src/main/resources/mapper/UserAccountMapper.xml` 或等效方式中补充 User 相关 SQL（如按 email 查询、唯一约束支持）
- [x] T007 设计并新增数据库层迁移脚本（例如在 `doc/` 或迁移目录中）为 user_account 表添加结构定义及 username/email 唯一索引
- [x] T008 在 `src/main/java/com/ysx/agent/dto/RegisterRequest.java` 中定义注册请求 DTO（包含 phone/email/password，带 Bean Validation 注解）
- [x] T009 在 `src/main/java/com/ysx/agent/dto/LoginRequest.java` 中定义登录请求 DTO（包含 identifierType/identifier/password，带 Bean Validation 注解）
- [x] T010 在 `src/main/java/com/ysx/agent/dto/AuthResponse.java` 中定义登录响应 DTO（包含 userId、token、expiresIn 等）
- [x] T011 在 `src/main/java/com/ysx/agent/service/AuthService.java` 中定义认证服务接口（注册、登录、退出登录等方法，仅声明，不加 @Transactional）
- [x] T012 在 `src/main/java/com/ysx/agent/service/impl/AuthServiceImpl.java` 中创建认证服务实现类骨架（注入 UserMapper、密码编码器、Sa-Token 相关组件）

**Checkpoint**: User 与 AuthService 的基础结构准备完成，可开始按故事实现具体行为。

---

## Phase 3: User Story 1 - 用户注册账号 (Priority: P1) 🎯 MVP

**Goal**: 支持用户通过手机号或邮箱 + 密码完成注册，并保证账号唯一性。

**Independent Test**:
- 成功调用注册接口创建用户；
- 数据库中存在唯一 User 记录；
- 使用该账号可在 User Story 2 中登录。

### Tests for User Story 1

- [x] T013 [P] [US1] 在 `src/test/java/com/ysx/agent/service/AuthServiceImplTest.java` 中编写注册成功单元测试（新手机号/邮箱 + 合法密码 → 创建用户，密码以 BCrypt 哈希存储）
- [x] T014 [P] [US1] 在 `src/test/java/com/ysx/agent/service/AuthServiceImplTest.java` 中编写注册失败单元测试（重复 phone/email、参数不合法场景）

### Implementation for User Story 1

- [x] T015 [US1] 在 `AuthServiceImpl` 中实现注册逻辑（校验至少一个标识存在、校验格式、检查 phone/email 唯一、BCrypt 哈希密码并持久化 User）
- [x] T016 [US1] 在 `src/main/java/com/ysx/agent/controller/AuthController.java` 中新增 `POST /api/v1/auth/register` 接口，接收 `RegisterRequest` 并调用 `AuthService` 完成注册
- [x] T017 [US1] 为注册接口对接统一 ApiResponse 结构与全局异常（参数非法、账号已存在等）
- [x] T018 [US1] 按 contracts/auth-api.md 中示例，手动验证注册接口请求/响应是否符合契约（本地调用或简单集成测试占位）

**Checkpoint**: 注册接口可独立工作，具备基本参数校验和唯一性约束。

---

## Phase 4: User Story 2 - 用户登录获取会话 (Priority: P1)

**Goal**: 支持用户使用手机号或邮箱 + 密码登录，基于 Sa-Token 创建会话并返回 token，支持基础防暴力破解。

**Independent Test**:
- 使用已注册账号登录获取 token；
- 携带 token 访问一个受保护接口成功；
- 未携带或携带无效 token 时统一返回未登录错误；
- 连续多次错误密码后触发失败保护。

### Tests for User Story 2

- [x] T019 [P] [US2] 在 `AuthServiceImplTest` 中编写登录成功单元测试（PHONE/EMAIL 两种 identifierType 均覆盖）
- [x] T020 [P] [US2] 在 `AuthServiceImplTest` 中编写登录失败单元测试（BAD_CREDENTIALS、ACCOUNT_STATUS_INVALID）
- [x] T021 [P] [US2] 在 `AuthServiceImplTest` 中编写登录失败计数与 TOO_MANY_ATTEMPTS 防护测试（超过阈值后一定时间内拒绝登录）

### Implementation for User Story 2

- [x] T022 [P] [US2] 在 `AuthServiceImpl` 中实现登录逻辑（根据 identifierType 选择 phone/email 查询 User，使用 BCrypt 校验密码，更新登录失败计数与封禁状态）
- [x] T023 [US2] 在登录成功路径中调用 `StpUtil.login` 建立 Sa-Token 会话，并返回包含 token 与过期信息的 `AuthResponse`
- [x] T024 [US2] 在 `AuthServiceImpl` 中实现登录失败计数与封禁逻辑（使用内存 Map 或合适的缓存组件，遵循 research.md 中阈值与窗口策略）
- [x] T025 [US2] 在 `AuthController` 中新增 `POST /api/v1/auth/login` 接口，接收 `LoginRequest` 并对接 `AuthService`，按 contracts/auth-api.md 返回 ApiResponse
- [x] T026 [US2] 为至少一个现有受保护接口（如 `NoteController` 的某个方法）添加 Sa-Token 登录校验注解或拦截配置，确保未登录时统一返回 UNAUTHORIZED
- [x] T027 [US2] 在登录成功与失败路径中按 research.md 记录基础审计日志（成功登录、失败原因分类）

**Checkpoint**: 登录与基础会话管理完成，受保护接口能正确识别登录态。

---

## Phase 5: User Story 3 - 用户退出登录与会话失效 (Priority: P2)

**Goal**: 支持已登录用户主动退出登录，并在会话过期时统一表现为未登录状态。

**Independent Test**:
- 已登录状态下调用登出接口后，再访问受保护接口被视为未登录；
- 模拟会话空闲超时或最大存活时间到期后，访问受保护接口需要重新登录。

### Tests for User Story 3

- [x] T028 [P] [US3] 在 `AuthServiceImplTest` 中编写退出登录单元测试（调用登出后当前会话失效）
- [x] T029 [P] [US3] 在集成层或模拟层验证：会话过期后访问受保护接口返回 UNAUTHORIZED（可基于 Sa-Token 配置进行时间控制或模拟）

### Implementation for User Story 3

- [x] T030 [US3] 在 `AuthServiceImpl` 中实现退出登录逻辑，使用 `StpUtil.logout` 注销当前会话
- [x] T031 [US3] 在 `AuthController` 中新增 `POST /api/v1/auth/logout` 接口，读取当前会话信息并调用 `AuthService` 完成退出登录
- [x] T032 [US3] 基于 research.md 中的会话配置，在 Sa-Token 配置类中设置空闲超时与最大存活时长，并验证其生效
- [x] T033 [US3] 在退出登录与会话超时场景中记录 LogoutEvent 审计日志（包含 userId、clientIp、triggerType）

**Checkpoint**: 登出与会话过期行为符合规格，安全日志可用于追踪关键登录事件。

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: 针对本功能的清理、文档与安全加强。

- [x] T034 [P] 更新 `doc/` 或相关 README，补充用户登录注册与认证模块的整体说明
- [x] T035 对 `AuthServiceImpl`、`AuthController`、`User` 相关代码进行适度重构与清理（保持类职责单一、方法简洁）
- [x] T036 [P] 检查并补充单元测试覆盖率，确保认证关键路径测试充分（包括异常路径）
- [x] T037 [P] 对登录失败防护与会话配置做一次安全自查，确认无明显绕过路径与信息泄露
- [x] T038 依据 `quickstart.md` 手工执行一次端到端验证流程（注册 → 登录 → 调用受保护接口 → 退出登录 → 会话过期）并记录结果

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: 无依赖，可立即开始
- **Foundational (Phase 2)**: 依赖 Phase 1，阻塞所有用户故事
- **User Stories (Phase 3–5)**: 依赖 Foundational 完成
  - US1/US2 为 P1，可在 Foundational 完成后并行实施
  - US3 为 P2，可在 US2 基础上并行或顺序实施
- **Polish (Phase 6)**: 依赖所有目标用户故事完成

### User Story Dependencies

- **User Story 1 (US1)**: 仅依赖 Foundational
- **User Story 2 (US2)**: 依赖 Foundational，业务上依赖 US1 已有用户数据
- **User Story 3 (US3)**: 依赖 US2 已建立登录/会话能力

### Parallel Opportunities

- Setup 与 Foundational 中标记 [P] 的任务可并行
- 各用户故事内部标记 [P] 的测试/实现可并行（注意同一文件的修改顺序）
- US1 与 US2 可由不同开发者并行推进，在接口契约已固定的前提下协作

---

## Implementation Strategy

### MVP First (User Story 1 + 基础部分 User Story 2)

1. 完成 Phase 1 + Phase 2（准备 User 与 AuthService 基础）。
2. 实现并验证 User Story 1（注册）。
3. 实现 User Story 2 中登录成功基础路径（不含复杂防爆破），完成基本“注册 + 登录 + 访问受保护接口”链路。
4. 根据需要进行小规模交付或内部演示。

### Incremental Delivery

1. 在 MVP 基础上补全 User Story 2 完整行为（失败计数、防暴力破解、审计日志）。
2. 增量实现 User Story 3（退出登录 + 会话过期策略）。
3. 最后执行 Phase 6 的整理、安全校验与文档完善。
