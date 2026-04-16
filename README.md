# Y-RAG 智能知识库系统

Y-RAG 是一个基于混合检索增强生成（RAG）的智能知识库问答系统，支持同步和 SSE 流式输出，为用户提供高效、准确的基于私有知识库的智能问答服务。

## 技术栈

| 分类 | 技术 |
|------|------|
| 核心框架 | Java 21 + Spring Boot 3.4.4 |
| AI 框架 | Spring AI 1.0.0 |
| 向量数据库 | Qdrant |
| 关系数据库 | MySQL 8.x |
| ORM | MyBatis-Plus 3.5.15 |
| 认证 | JWT + Sa-Token |
| API 文档 | Knife4j + SpringDoc OpenAPI |

## 核心特性

### 1. 混合检索
- **向量检索**：基于语义相似度的向量搜索，捕获深层语义关联
- **关键词检索**：BM25 算法，精确匹配关键词和术语
- **RRF 融合**：使用倒数排名融合算法（Reciprocal Rank Fusion）合并多通道结果

### 2. RAG Agent
- 查询重写：优化用户问题以提升检索效果
- 上下文构建：将检索文档格式化为 LLM 可理解的上下文
- 流式输出：支持 SSE 流式响应，实时返回 AI 生成内容

### 3. 多轮对话记忆
- 基于文件的持久化对话记忆（FileBasedChatMemory）
- Kryo 序列化存储，高效且可靠
- 支持会话上下文关联

## 项目架构

```
src/main/java/com/ysx/agent/
├── config/                    # 配置层
│   ├── AiConfig.java         # Spring AI 配置
│   ├── ChatMemoryConfig.java  # 对话记忆配置
│   ├── CorsConfig.java        # 跨域配置
│   └── SaTokenConfig.java     # 认证配置
├── controller/                # 接口层
│   ├── ChatController.java           # RAG 聊天接口
│   ├── NoteController.java           # 笔记 CRUD
│   ├── KnowledgeBaseController.java  # 知识库管理
│   └── RagRetrievalController.java  # 检索配置与日志
├── service/                   # 业务层
│   ├── HybridSearchService.java
│   └── RagRetrievalLogService.java
├── rag/                       # RAG 核心模块
│   ├── search/                # 检索组件
│   │   ├── VectorRetriever          # 向量检索
│   │   ├── KeywordRetriever         # 关键词检索
│   │   └── HybridDocumentMerger     # RRF 结果融合
│   ├── llm/
│   │   └── YRAGAgent.java          # RAG Agent
│   └── memory/
│       └── FileBasedChatMemory.java # 对话记忆
└── domain/                    # 领域实体
```

## API 接口

### RAG 聊天

| 接口 | 方法 | 说明 |
|------|------|------|
| `/chat/rag` | POST | RAG 同步聊天 |
| `/chat/rag/stream` | GET | RAG SSE 流式聊天 |
| `/chat/rag/with-config` | POST | 带配置参数的同步聊天 |
| `/chat/rag/stream/with-config` | GET | 带配置参数的流式聊天 |
| `/chat/memory/{chatId}` | DELETE | 清理会话记忆 |

### 知识库管理

| 接口 | 方法 | 说明 |
|------|------|------|
| `/kb/create` | POST | 创建知识库 |
| `/kb/{id}` | GET | 获取知识库信息 |
| `/kb/{id}` | PUT | 更新知识库 |
| `/kb/{id}` | DELETE | 删除知识库 |

### 检索配置

| 接口 | 方法 | 说明 |
|------|------|------|
| `/retrieval/config` | GET | 获取当前检索配置 |
| `/retrieval/config/presets` | GET | 获取预设配置 |
| `/retrieval/logs` | GET | 获取检索日志 |

## 快速开始

### 1. 环境要求

- JDK 21+
- MySQL 8.x
- Qdrant 向量数据库
- Ollama（可选，用于本地 Embedding 模型）

### 2. 配置文件

#### application.yaml（主配置）

```yaml
server:
  port: 8080
  servlet:
    context-path: /api

spring:
  application:
    name: Y-RAG
  profiles:
    active: local

  # Qdrant 向量数据库配置
  ai:
    vectorstore:
      qdrant:
        host: 192.168.237.134
        port: 6334
        initialize-schema: true
        enabled: true
        collection-name-prefix: kb_
    # Ollama Embedding 配置
    ollama:
      base-url: http://127.0.0.1:11434
      embedding:
        enabled: true
        model: qwen3-embedding:0.6b
        options:
          temperature: 0
          dimension: 768

  # MySQL 数据源配置
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://127.0.0.1:3306/yrag?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true
    username: root
    password: 12345678

# Sa-Token 认证配置
sa-token:
  timeout: 604800        # 7 天最大存活
  active-timeout: 7200   # 2 小时空闲超时
  is-read-header: true
  token-name: yrag-token
  is-log: true
  token-style: uuid

# MyBatis-Plus 配置
mybatis-plus:
  mapper-locations: classpath*:mapper/*.xml
  type-aliases-package: com.ysx.agent.domain
  global-config:
    db-config:
      id-type: auto
      select-strategy: not_empty
  configuration:
    map-underscore-to-camel-case: true
    cache-enabled: false
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl

# RAG 聊天记忆配置
rag:
  chat-memory:
    dir: data/chat-memory

# API 文档配置
springdoc:
  swagger-ui:
    path: /swagger-ui.html
  api-docs:
    path: /v3/api-docs

knife4j:
  enable: true
  setting:
    language: zh_cn
```

#### application-local.yaml（本地 LLM 配置）

```yaml
spring:
  ai:
    openai:
      # MiniMax API 配置
      base-url: https://api.minimaxi.com
      api-key: your_api_key_here
      chat:
        options:
          model: MiniMax-M2.7
```

### 3. 启动服务

```bash
mvn spring-boot:run
```

## 检索配置

系统提供三种预设检索模式：

| 模式 | 向量权重 | 关键词权重 | 适用场景 |
|------|----------|------------|----------|
| balanced | 0.5 | 0.5 | 平衡模式 |
| semanticFirst | 0.8 | 0.2 | 语义优先，概念性问题 |
| keywordFirst | 0.2 | 0.8 | 关键词优先，精确匹配 |

## License

MIT
