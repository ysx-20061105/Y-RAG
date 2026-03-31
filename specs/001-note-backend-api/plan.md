# Implementation Plan: RAG系统笔记后端接口

**Branch**: `001-note-backend-api` | **Date**: 2026-03-30 | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `/specs/001-note-backend-api/spec.md`

## Summary

实现RAG系统的笔记后端CRUD接口（仅后端）。支持：
- 笔记创建时自动提取H1作为标题
- 纯Markdown格式存储（不含HTML）
- 按知识库ID过滤的笔记列表查询
- 笔记的创建、查询、更新、删除
- 10MB内容大小限制

## Technical Context

**Language/Version**: Java 21
**Primary Dependencies**: Spring Boot 3.4.4, MyBatis-Plus, MySQL 8.x, JWT (sa-token)
**Storage**: MySQL 8.x
**Testing**: JUnit 5, Mockito, MockMvc
**Target Platform**: Linux server
**Project Type**: Web service (REST API)
**Performance Goals**: 3秒创建响应, 500ms列表查询(100条内)
**Constraints**: JWT认证, 严格隔离(用户只能访问所属知识库的笔记)
**Scale/Scope**: 多知识库场景，每个知识库可有多条笔记

## Project Structure

### Documentation (this feature)

```text
specs/001-note-backend-api/
├── plan.md              # This file
├── spec.md              # Feature specification
├── research.md          # (not needed - no unknowns)
├── data-model.md        # Entity & DTO definitions
├── quickstart.md        # API usage examples
└── contracts/           # API contracts (if needed)
```

### Source Code (repository root)

This is a Spring Boot 3.x project following standard layered architecture:

```text
src/main/java/com/example/rag/
├── controller/          # REST API endpoints
│   └── NoteController.java
├── service/              # Business logic
│   ├── NoteService.java
│   └── NoteServiceImpl.java
├── repository/           # Data access (MyBatis-Plus)
│   └── NoteMapper.java
├── domain/               # Entity classes
│   └── Note.java
├── dto/                  # Request/Response DTOs
│   ├── CreateNoteRequest.java
│   ├── UpdateNoteRequest.java
│   ├── NoteResponse.java
│   └── NoteListResponse.java
└── config/               # Configuration (if needed)
    └── WebConfig.java

src/main/resources/
├── mapper/               # MyBatis XML mappers
│   └── NoteMapper.xml
└── application.yml

src/test/java/com/example/rag/
├── controller/
│   └── NoteControllerTest.java
└── service/
    └── NoteServiceTest.java
```

**Structure Decision**: Standard Spring Boot layered architecture (Controller -> Service -> Repository)

## Phase 0: Research

No research needed - all technical choices are defined in project CLAUDE.md:
- Java 21 + Spring Boot 3.4.4
- MyBatis-Plus for data access
- MySQL 8.x for storage
- JWT (sa-token) for authentication

## Phase 1: Design & Contracts

### 1.1 Data Model

**Note Entity**:
| Field | Type | Constraints |
|-------|------|-------------|
| id | Long | Primary key, auto-increment |
| kbId | Long | NOT NULL, foreign key to KnowledgeBase |
| title | String(255) | NOT NULL |
| content | TEXT | NOT NULL, max 10MB |
| createdAt | LocalDateTime | NOT NULL, auto-set |
| updatedAt | LocalDateTime | NOT NULL, auto-update |

**Relationships**:
- Note belongs to KnowledgeBase (Many-to-One)
- KnowledgeBase has many Notes (One-to-Many)
- Cascade delete: when KnowledgeBase is deleted, all its Notes are deleted

### 1.2 API Contracts

**Base Path**: `/api/v1/notes`

| Method | Path | Description |
|--------|------|-------------|
| POST | `/` | 创建笔记 |
| GET | `/{id}` | 获取笔记详情 |
| GET | `/list` | 获取笔记列表(按kbId分页) |
| PUT | `/{id}` | 更新笔记 |
| DELETE | `/{id}` | 删除笔记 |

**Create Note Request**:
```json
{
  "kbId": 123,
  "title": "可选标题",
  "content": "# Markdown内容..."
}
```

**Note Response**:
```json
{
  "id": 1,
  "kbId": 123,
  "title": "笔记标题",
  "content": "# Markdown内容...",
  "createdAt": "2026-03-30T10:00:00",
  "updatedAt": "2026-03-30T10:00:00"
}
```

**Error Response**:
```json
{
  "code": 404,
  "message": "笔记不存在",
  "timestamp": "2026-03-30T10:00:00"
}
```

## Implementation Tasks

### Task Group 1: Entity & Repository
1. 创建 Note 实体类
2. 创建 NoteMapper 接口 (MyBatis-Plus)
3. 创建 NoteMapper.xml
4. 编写 NoteMapper 单元测试

### Task Group 2: DTOs
5. 创建 CreateNoteRequest DTO (含校验注解)
6. 创建 UpdateNoteRequest DTO
7. 创建 NoteResponse DTO
8. 创建 NoteListResponse DTO (分页)
9. 创建统一错误响应类 ApiResponse

### Task Group 3: Service
10. 创建 NoteService 接口
11. 创建 NoteServiceImpl 实现类
12. 实现自动提取H1标题逻辑
13. 实现10MB内容校验
14. 编写 NoteService 单元测试

### Task Group 4: Controller
15. 创建 NoteController
16. 实现 POST /api/v1/notes (创建)
17. 实现 GET /api/v1/notes/{id} (详情)
18. 实现 GET /api/v1/notes/list?kbId=X&page=X&size=X (列表)
19. 实现 PUT /api/v1/notes/{id} (更新)
20. 实现 DELETE /api/v1/notes/{id} (删除)
21. 实现全局异常处理
22. 编写 NoteController 集成测试

## Verification

- [ ] 创建笔记后，数据库中content字段为纯Markdown
- [ ] 未指定标题时，系统自动提取H1作为标题
- [ ] 空笔记允许保存，标题为"无标题笔记"
- [ ] 内容超过10MB时，返回验证错误
- [ ] 删除知识库时，关联笔记一并删除
- [ ] 用户无法访问其他知识库的笔记
- [ ] 笔记列表按kbId过滤正确
- [ ] 响应时间符合性能要求
