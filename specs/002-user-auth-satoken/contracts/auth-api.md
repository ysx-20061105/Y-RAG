# Auth API Contracts: 用户登录注册与认证

本文件定义与“用户登录注册与认证”功能相关的 HTTP 接口契约，包括 URL、方法、请求/响应结构及错误情况。所有响应均使用统一的 `ApiResponse` 包装：

```json
{
  "success": true,
  "data": { ... },
  "error": ""       // 仅在 success=false 时出现
}
```

具体字段命名与类型以本文件为准，如有差异，以最新版本为权威。

---

## 1. 注册接口：POST /api/v1/auth/register

### 1.1 功能说明

创建新用户账号，支持手机号或邮箱作为登录标识。成功时返回新建用户的基本信息。

### 1.2 请求

- **URL**: `/api/v1/auth/register`
- **方法**: `POST`
- **请求体（JSON）**:

```json
{
  "phone": "13800138000",       // 可选，与 email 至少填一个
  "email": "user@example.com",  // 可选，与 phone 至少填一个
  "password": "P@ssw0rd123"     // 必填，需满足复杂度要求
}
```

### 1.3 校验规则

- `phone` 与 `email` 至少提供一个：
  - 若两者均缺失或为空，返回参数错误。
- `phone`（如提供）：
  - 必须符合预期的手机号格式（例如中国大陆 11 位数字，以 1 开头）。
- `email`（如提供）：
  - 必须为合法邮箱格式。
- `password`：
  - 必须非空；
  - 最小长度（例如 ≥ 8 位）；
  - 应包含至少两类字符（例如字母、数字、特殊符号），具体策略可在实现中细化。

### 1.4 成功响应

```json
{
  "success": true,
  "data": {
    "userId": "1234567890"
  }
}
```

### 1.5 失败响应场景

- 参数校验失败（例如手机号/邮箱格式非法、密码不符合复杂度）：

```json
{
  "success": false,
  "error": "INVALID_ARGUMENT"
}
```

- 手机号已存在：

```json
{
  "success": false,
  "error": "PHONE_ALREADY_EXISTS"
}
```

- 邮箱已存在：

```json
{
  "success": false,
  "error": "EMAIL_ALREADY_EXISTS"
}
```

---

## 2. 登录接口：POST /api/v1/auth/login

### 2.1 功能说明

使用账号（手机号或邮箱）+ 密码登录系统。成功时创建会话并返回 token，用于访问受保护接口。

### 2.2 请求

- **URL**: `/api/v1/auth/login`
- **方法**: `POST`
- **请求体（JSON）**:

```json
{
  "identifierType": "PHONE",       // 必填：PHONE 或 EMAIL
  "identifier": "13800138000",    // 必填：对应的手机号或邮箱
  "password": "P@ssw0rd123"       // 必填：用户密码
}
```

### 2.3 校验规则

- `identifierType`：
  - 必须为 `PHONE` 或 `EMAIL` 两者之一。
- `identifier`：
  - 根据 `identifierType` 使用对应的格式校验（手机号或邮箱）。
- `password`：
  - 必须非空。

### 2.4 成功响应

```json
{
  "success": true,
  "data": {
    "userId": "1234567890",
    "token": "xxxxx.yyyyy.zzzzz",   // Sa-Token 生成的 token 字符串
    "expiresIn": 7200                // 可选，预计有效期（秒）
  }
}
```

### 2.5 失败响应场景

- 参数不合法：

```json
{
  "success": false,
  "error": "INVALID_ARGUMENT"
}
```

- 账号不存在或密码错误（不区分具体原因，避免泄露账号是否存在）：

```json
{
  "success": false,
  "error": "BAD_CREDENTIALS"
}
```

- 账号状态异常（例如被禁用）：

```json
{
  "success": false,
  "error": "ACCOUNT_STATUS_INVALID"
}
```

- 由于短时间内多次失败触发保护机制：

```json
{
  "success": false,
  "error": "TOO_MANY_ATTEMPTS"
}
```

> 实现上可结合失败次数与封禁窗口控制错误返回节奏，避免暴露过多细节。

---

## 3. 退出登录接口：POST /api/v1/auth/logout

### 3.1 功能说明

注销当前登录会话，使当前 token 立即失效。调用方需在请求中携带有效 token。

### 3.2 请求

- **URL**: `/api/v1/auth/logout`
- **方法**: `POST`
- **请求头**（示例）：

```http
Authorization: Bearer xxxxx.yyyyy.zzzzz
```

或使用契约约定的自定义 Header，例如：

```http
X-Auth-Token: xxxxx.yyyyy.zzzzz
```

### 3.3 成功响应

```json
{
  "success": true
}
```

### 3.4 失败响应场景

- 未携带 token 或 token 无效：

```json
{
  "success": false,
  "error": "UNAUTHORIZED"
}
```

（可与统一未登录错误码共用。）

---

## 4. 受保护接口的统一认证行为

### 4.1 说明

除上述 `/api/v1/auth/*` 接口外，其他需要登录态的业务接口（如笔记 CRUD、知识库管理等）应统一要求携带有效 token，并在会话无效时返回一致的错误响应。

### 4.2 统一错误响应

- 未登录或会话无效：

```json
{
  "success": false,
  "error": "UNAUTHORIZED"
}
```

> 包含以下情况：
> - 未携带 token；
> - token 格式错误或伪造；
> - token 对应会话已过期或被注销。

### 4.3 示例：受保护接口（仅示意）

```http
GET /api/v1/notes
Authorization: Bearer xxxxx.yyyyy.zzzzz
```

成功时返回业务数据（如笔记列表），失败时返回统一的未登录错误响应。

---

## 5. 错误码一览

本节汇总上述接口可能出现的主要错误码，供前端和调用方统一处理。

| 错误码                  | 含义                                           |
|-------------------------|------------------------------------------------|
| `INVALID_ARGUMENT`      | 请求参数不合法（缺失必填项、格式错误等）       |
| `PHONE_ALREADY_EXISTS`  | 注册时手机号已被其他账号使用                   |
| `EMAIL_ALREADY_EXISTS`  | 注册时邮箱已被其他账号使用                     |
| `BAD_CREDENTIALS`       | 账号不存在或密码错误                           |
| `ACCOUNT_STATUS_INVALID`| 账号状态异常（如被禁用）                       |
| `TOO_MANY_ATTEMPTS`     | 登录失败次数过多，被临时限制                   |
| `UNAUTHORIZED`          | 未登录或会话无效，无法访问受保护资源           |

后续如需增加更细粒度的错误码，可在保持向后兼容的前提下扩展本表。