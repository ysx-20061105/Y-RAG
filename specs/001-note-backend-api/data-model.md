# Data Model: 笔记模块

## Entity: Note

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | Long | PK, AUTO_INCREMENT | 笔记唯一标识 |
| kb_id | Long | NOT NULL, FK | 所属知识库ID |
| title | VARCHAR(255) | NOT NULL | 笔记标题 |
| content | LONGTEXT | NOT NULL | Markdown内容, 最大10MB |
| created_at | DATETIME | NOT NULL | 创建时间 |
| updated_at | DATETIME | NOT NULL | 更新时间 |

## Entity: KnowledgeBase (已有)

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | Long | PK, AUTO_INCREMENT | 知识库唯一标识 |
| name | VARCHAR(255) | NOT NULL | 知识库名称 |
| user_id | Long | NOT NULL | 所属用户ID |
| created_at | DATETIME | NOT NULL | 创建时间 |

## Relationships

```
KnowledgeBase (1) ----< (N) Note
       |                  |
       |                  | kb_id FK
       |                  |
       +----< cascade ----+
```

## DTOs

### CreateNoteRequest
```java
{
  kbId: Long,      // required
  title: String?,  // optional, auto-extract from H1 if null
  content: String  // required, max 10MB
}
```

### UpdateNoteRequest
```java
{
  title: String?,  // optional
  content: String  // optional, max 10MB
}
```

### NoteResponse
```java
{
  id: Long,
  kbId: Long,
  title: String,
  content: String,  // pure Markdown
  createdAt: LocalDateTime,
  updatedAt: LocalDateTime
}
```

### NoteListResponse (分页)
```java
{
  list: List<NoteResponse>,
  total: Long,
  page: Integer,
  size: Integer
}
```

## Validation Rules

| 字段 | 规则 |
|------|------|
| kbId | 必须 > 0，且对应 KnowledgeBase 存在 |
| title | 最大 255 字符（自动提取时截断超长 H1） |
| content | 最大 10MB (10,485,760 字节)，UTF-8 编码 |

## Auto-extraction Logic

创建/更新笔记时，如果 title 为 null 或空：
1. 提取 content 第一行
2. 如果是 H1 格式 (`# 标题文本`)，提取 `标题文本` 作为 title
3. 如果无 H1 或为空，title 设为 "无标题笔记"
