# Quickstart: 笔记 API 使用指南

## Base URL

```
/api/v1/notes
```

## 认证

所有接口需要在 Header 中携带 JWT Token：
```
Authorization: Bearer <token>
```

---

## 1. 创建笔记

**POST** `/api/v1/notes`

**Request Body**:
```json
{
  "kbId": 1,
  "title": "可选标题",
  "content": "# Hello World\n\n这是笔记内容..."
}
```

**Response** (201 Created):
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": 1,
    "kbId": 1,
    "title": "可选标题",
    "content": "# Hello World\n\n这是笔记内容...",
    "createdAt": "2026-03-30T10:00:00",
    "updatedAt": "2026-03-30T10:00:00"
  }
}
```

**自动标题提取示例**:
```json
// Request
{
  "kbId": 1,
  "content": "# 我的第一篇笔记\n\n正文内容..."
}

// Response (title 自动提取)
{
  "data": {
    "id": 2,
    "kbId": 1,
    "title": "我的第一篇笔记",
    "content": "# 我的第一篇笔记\n\n正文内容...",
    ...
  }
}
```

---

## 2. 获取笔记详情

**GET** `/api/v1/notes/{id}`

**Response** (200 OK):
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": 1,
    "kbId": 1,
    "title": "笔记标题",
    "content": "# Markdown内容...",
    "createdAt": "2026-03-30T10:00:00",
    "updatedAt": "2026-03-30T10:00:00"
  }
}
```

**Error** (404 Not Found):
```json
{
  "code": 404,
  "message": "笔记不存在",
  "timestamp": "2026-03-30T10:00:00"
}
```

---

## 3. 获取笔记列表

**GET** `/api/v1/notes/list?kbId={kbId}&page={page}&size={size}`

**Query Parameters**:
| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| kbId | Long | 是 | - | 知识库ID |
| page | Integer | 否 | 1 | 页码 |
| size | Integer | 否 | 10 | 每页条数(最大100) |

**Response** (200 OK):
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "list": [
      {
        "id": 1,
        "kbId": 1,
        "title": "笔记1",
        "content": "# Markdown...",
        "createdAt": "2026-03-30T10:00:00",
        "updatedAt": "2026-03-30T10:00:00"
      }
    ],
    "total": 50,
    "page": 1,
    "size": 10
  }
}
```

---

## 4. 更新笔记

**PUT** `/api/v1/notes/{id}`

**Request Body**:
```json
{
  "title": "新标题",
  "content": "# 更新后的内容..."
}
```

**Response** (200 OK):
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": 1,
    "kbId": 1,
    "title": "新标题",
    "content": "# 更新后的内容...",
    "createdAt": "2026-03-30T10:00:00",
    "updatedAt": "2026-03-30T11:00:00"
  }
}
```

---

## 5. 删除笔记

**DELETE** `/api/v1/notes/{id}`

**Response** (200 OK):
```json
{
  "code": 0,
  "message": "success",
  "data": null
}
```

---

## 错误码

| code | 说明 |
|------|------|
| 0 | 成功 |
| 400 | 请求参数错误 |
| 401 | 未认证 |
| 403 | 无权限访问该笔记 |
| 404 | 笔记不存在 |
| 413 | 内容超过10MB限制 |
| 500 | 服务器内部错误 |

---

## 容量提示

当 `content` 长度超过 8MB 时，响应中会包含 `remainingCapacity` 字段提示剩余容量：
```json
{
  "code": 0,
  "message": "success",
  "data": {
    ...
  },
  "remainingCapacity": 1572864
}
```
