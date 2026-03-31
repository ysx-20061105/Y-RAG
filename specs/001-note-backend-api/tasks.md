# Tasks: RAG系统笔记后端接口

**Feature**: 001-note-backend-api
**Branch**: 001-note-backend-api
**Generated**: 2026-03-30
**Spec**: [spec.md](./spec.md)
**Plan**: [plan.md](./plan.md)

## Summary

实现RAG系统的笔记后端CRUD接口（仅后端）。支持笔记创建、查询、更新、删除，H1自动提取标题，10MB内容限制，严格知识库隔离。

## User Story Completion Order

| User Story | Priority | Tasks | Independent Test |
|------------|----------|-------|------------------|
| US1: 笔记创建与保存 | P1 | T003-T006 | 创建笔记 -> 保存 -> 查询验证 |
| US2: 笔记查询与列表 | P1 | T007-T009 | 创建多个 -> 分页查询 -> 详情验证 |
| US3: 笔记删除 | P2 | T010 | 创建 -> 删除 -> 查询404 |
| US4: 知识库关联 | P1 | T005,T008 | 不同KB笔记 -> 过滤验证 |

## Phase 1: Setup

*Project already initialized. No setup tasks required.*

## Phase 2: Foundational (Entity & Repository)

**Goal**: Create Note entity and MyBatis-Plus mapper

**Independent Test**: Can be tested by verifying Note CRUD operations in database

- [x] T001 [P] Create Note entity class in `src/main/java/com/example/rag/domain/Note.java`
  - Fields: id (Long, PK), kbId (Long, FK), title (String), content (String LONGTEXT), contentBytes (Integer), summary (String), createdAt (LocalDateTime), updatedAt (LocalDateTime)
  - Add MyBatis-Plus annotations (@TableName, @TableId)
  - Add Lombok annotations (@Data, @NoArgsConstructor, @AllArgsConstructor)

- [x] T002 [P] Create NoteMapper interface in `src/main/java/com/example/rag/repository/NoteMapper.java`
  - Extend BaseMapper<Note>
  - Add methods: selectByKbId(Long kbId, Page<Note> page), selectByIdAndKbId(Long id, Long kbId)

- [x] T003 [P] Create NoteMapper XML in `src/main/resources/mapper/NoteMapper.xml`
  - ResultMap for Note entity
  - selectByKbId query with pagination
  - selectByIdAndKbId query with ownership check

## Phase 3: DTOs

**Goal**: Create request/response DTOs for API contracts

**Independent Test**: Can be tested by serializing/deserializing DTOs

- [x] T004 [P] Create CreateNoteRequest DTO in `src/main/java/com/example/rag/dto/CreateNoteRequest.java`
  - Fields: kbId (Long, @NotNull), title (String, optional), content (String, @NotNull)
  - Add Jakarta validation annotations (@NotNull, @Size max=10485760 for content in bytes)

- [x] T005 [P] Create UpdateNoteRequest DTO in `src/main/java/com/example/rag/dto/UpdateNoteRequest.java`
  - Fields: title (String, optional), content (String, optional)
  - Add validation annotations

- [x] T006 [P] Create NoteResponse DTO in `src/main/java/com/example/rag/dto/NoteResponse.java`
  - Fields: id, kbId, title, content, summary, createdAt, updatedAt
  - Add static factory method fromEntity(Note note)

- [x] T007 [P] Create NoteListResponse DTO in `src/main/java/com/example/rag/dto/NoteListResponse.java`
  - Fields: list (List<NoteResponse>), total (Long), page (Integer), size (Integer)

- [x] T008 [P] Create ApiResponse wrapper in `src/main/java/com/example/rag/dto/ApiResponse.java`
  - Fields: code (Integer), message (String), data (Object), remainingCapacity (Long, optional)
  - Static success(Object data) and error(Integer code, String message) methods

## Phase 4: US1 - 笔记创建与保存

**Story Goal**: 用户能够创建新笔记，自动提取H1作为标题，保存纯Markdown内容

**Independent Test**: POST /api/v1/notes -> GET /api/v1/notes/{id} -> verify content matches

- [x] T009 Create NoteService interface in `src/main/java/com/example/rag/service/NoteService.java`
  - Method: NoteResponse createNote(CreateNoteRequest request, Long userId)
  - Method: NoteResponse updateNote(Long noteId, UpdateNoteRequest request, Long userId)
  - Method: void deleteNote(Long noteId, Long userId)
  - Method: NoteResponse getNoteById(Long noteId, Long userId)
  - Method: NoteListResponse listNotes(Long kbId, Integer page, Integer size, Long userId)

- [x] T010 Create NoteServiceImpl in `src/main/java/com/example/rag/service/NoteServiceImpl.java`
  - Implement createNote: validate kbId belongs to userId, extract H1 if title null, check 10MB limit, save
  - Implement H1 extraction: extract first line, if starts with "# " take text after, else use "无标题笔记"
  - Implement 10MB validation: check content.getBytes(UTF_8).length <= 10485760
  - Implement updateNote: same H1 extraction and 10MB validation
  - Implement deleteNote: verify ownership then delete
  - Implement getNoteById: verify ownership, return NoteResponse
  - Implement listNotes: verify kbId ownership, return paginated list

- [x] T011 Create NoteController in `src/main/java/com/example/rag/controller/NoteController.java`
  - POST /api/v1/notes - create note
  - GET /api/v1/notes/{id} - get note detail
  - GET /api/v1/notes/list - list notes by kbId with pagination
  - PUT /api/v1/notes/{id} - update note
  - DELETE /api/v1/notes/{id} - delete note
  - Extract userId from JWT token in each request

## Phase 5: Exception Handling

**Goal**: Global exception handling for consistent error responses

- [x] T012 Create GlobalExceptionHandler in `src/main/java/com/example/rag/exception/GlobalExceptionHandler.java`
  - Handle NoteNotFoundException -> 404
  - Handle AccessDeniedException -> 403
  - Handle ValidationException (10MB exceeded) -> 413
  - Handle IllegalArgumentException -> 400
  - Handle all others -> 500
  - Return ApiResponse with error code and message

- [x] T013 Create custom exceptions in `src/main/java/com/example/rag/exception/NoteNotFoundException.java`
  - RuntimeException with noteId field

- [x] T014 Create AccessDeniedException in `src/main/java/com/example/rag/exception/NoteAccessDeniedException.java`
  - RuntimeException for ownership validation failures

## Phase 6: KnowledgeBase Validation

**Goal**: Verify KnowledgeBase exists and belongs to user before note operations

- [x] T015 Create KnowledgeBaseMapper in `src/main/java/com/example/rag/repository/KnowledgeBaseMapper.java`
  - Extend BaseMapper<KnowledgeBase>
  - Method: selectByIdAndOwnerId(Long id, Long ownerId) for ownership check

- [x] T016 Create KnowledgeBase entity in `src/main/java/com/example/rag/domain/KnowledgeBase.java`
  - Fields: id, ownerUserId, name, description, status, createdAt, updatedAt

- [x] T017 Update NoteServiceImpl to validate KnowledgeBase ownership
  - Before create/update: verify kbId exists and kb.ownerUserId == userId

## Phase 7: RemainingCapacity Hint

**Goal**: Return remaining capacity when content approaches 10MB limit

- [x] T018 Update NoteServiceImpl to calculate remainingCapacity
  - When content.length > 8MB, include remainingCapacity in response
  - remainingCapacity = 10485760 - contentBytes

## Dependencies Graph

```
Phase 2 (Entity/Repo)
    ├── T001 Note Entity
    ├── T002 NoteMapper
    └── T003 NoteMapper.xml
         │
         v
Phase 3 (DTOs) - parallel execution possible
    ├── T004 CreateNoteRequest
    ├── T005 UpdateNoteRequest
    ├── T006 NoteResponse
    ├── T007 NoteListResponse
    └── T008 ApiResponse
         │
         v
Phase 4 (US1 - Service + Controller)
    ├── T009 NoteService Interface
    ├── T010 NoteServiceImpl
    └── T011 NoteController
         │
         v
Phase 5 (Exception Handling)
    ├── T012 GlobalExceptionHandler
    ├── T013 NoteNotFoundException
    └── T014 NoteAccessDeniedException
         │
         v
Phase 6 (KB Validation)
    ├── T015 KnowledgeBaseMapper
    ├── T016 KnowledgeBase Entity
    └── T017 Update NoteServiceImpl
         │
         v
Phase 7 (RemainingCapacity)
    └── T018 Update NoteServiceImpl
```

## Parallel Execution Examples

The following tasks can be executed in parallel (different files, no dependencies):

**Parallel Set A** (Phase 2-3):
- T001 (Note entity), T002 (NoteMapper), T003 (NoteMapper.xml)
- T004, T005, T006, T007, T008 (all DTOs)

**Parallel Set B** (Phase 5):
- T012, T013, T014 (all exception classes)

## Implementation Strategy

### MVP Scope (US1: 笔记创建与保存)
Start with Phase 2 (T001-T003), Phase 3 (T004-T008), Phase 4 (T009-T011)

### Incremental Delivery
1. **Increment 1**: Entity + Mapper + DTOs (T001-T008)
2. **Increment 2**: Basic CRUD Service + Controller (T009-T011)
3. **Increment 3**: Exception Handling (T012-T014)
4. **Increment 4**: KnowledgeBase Validation (T015-T017)
5. **Increment 5**: RemainingCapacity Hint (T018)

## Verification Checklist

- [ ] T019 Create SQL migration for note table (per doc/数据库设计.md)
- [x] T020 Verify H1 extraction works for "# Title" format
- [x] T021 Verify H1 extraction uses "无标题笔记" when no H1
- [x] T022 Verify 10MB validation returns 413 error
- [x] T023 Verify empty content is saved with "无标题笔记" title
- [x] T024 Verify knowledge base isolation (user cannot see other user's notes)
- [ ] T025 Verify cascade delete when knowledge base is deleted
- [ ] T026 Integration test: full create -> list -> get -> update -> delete flow
