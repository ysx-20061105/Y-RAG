# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

# RAG智能知识库系统

RAG知识库系统旨在为用户提供一个零延迟、高隐私、体验流畅的Markdown编辑环境。通过纯前端技术栈，实现用户输入与预览渲染的毫秒级同步，确保“所见即所得”的写作体验。
同时，确保输出的Markdown源数据纯净、结构化，为后续的RAG检索与处理流程提供高质量输入。

## 项目概述

- **项目名称**: Y-RAG智能知识库系统
- **技术栈**: Java 21 + Spring Boot 3.4.4 + MyBatis-Plus + MySQL 8.x + Qdrant
- **认证**: JWT + sa-token

## 文件

- [功能需求](doc/功能需求.md)
- [架构](doc/架构.md)

## 项目架构

```
Y-RAG
├── src
│   ├── main
│   │   ├── java
│   │   │   └── com
│   │   │       └── ysx
│   │   │           └── agent
│   │   │               ├── RagApplication.java             // 启动类
│   │   │               │
│   │   │               ├── config                          // [配置层]
│   │   │               │   ├── AiConfig.java               // Spring AI 配置 (ChatClient, Embedding)
│   │   │               │   ├── QdrantConfig.java           // Qdrant 向量库连接配置
│   │   │               │   ├── CorsConfig.java             // 跨域配置
│   │   │               │   └── ThreadPoolConfig.java       // 异步线程池配置
│   │   │               │
│   │   │               ├── controller                      // [接口层] - 处理 HTTP 请求
│   │   │               │   ├── NoteController.java         // 笔记 CRUD 接口
│   │   │               │   ├── KnowledgeBaseController.java// 知识库管理接口
│   │   │               │   ├── ChatController.java        // 聊天与 SSE 流式问答接口
│   │   │               │   └── RagRetrievalController.java// 检索配置与日志接口
│   │   │               │
│   │   │               ├── service                         // [业务层] - 核心逻辑
│   │   │               │   ├── HybridSearchService.java    // 混合检索服务接口
│   │   │               │   ├── RagRetrievalLogService.java// 检索日志服务接口
│   │   │               │   └── impl/
│   │   │               │       ├── HybridSearchServiceImpl.java  // 混合检索实现
│   │   │               │       └── RagRetrievalLogServiceImpl.java // 检索日志实现
│   │   │               │
│   │   │               ├── rag                            // [RAG模块]
│   │   │               │   ├── search/                    // 检索组件
│   │   │               │   │   ├── VectorRetriever.java     // 向量检索器接口
│   │   │               │   │   ├── VectorRetrieverImpl.java  // 向量检索器实现
│   │   │               │   │   ├── KeywordRetriever.java     // 关键词检索器接口
│   │   │               │   │   ├── KeywordRetrieverImpl.java // 关键词检索器实现
│   │   │               │   │   ├── HybridDocumentMerger.java // 结果融合器接口
│   │   │               │   │   └── HybridDocumentMergerImpl.java // 结果融合实现
│   │   │               │   ├── config/
│   │   │               │   │   └── HybridSearchConfig.java  // 混合检索配置
│   │   │               │   └── llm/
│   │   │               │       └── YRAGAgent.java          // RAG Agent
│   │   │               │
│   │   │               ├── domain                          // [领域实体] - 数据库表映射
│   │   │               │   ├── Note.java                   // 笔记实体
│   │   │               │   ├── KnowledgeBase.java          // 知识库实体
│   │   │               │   └── RagRetrievalLog.java       // 检索日志实体
│   │   │               │
│   │   │               └── mapper                         // [数据访问层]
│   │   │                   ├── NoteMapper.java
│   │   │                   ├── KnowledgeBaseMapper.java
│   │   │                   └── RagRetrievalLogMapper.java
│   │   │
│   │   └── resources
│   │       ├── application.yml                             // 全局配置
│   │       └── application-dev.yml                         // 开发环境配置
│   │
│   └── test                                                // 单元测试
│       └── java/com/ysx/agent/
│           ├── service/impl/
│           │   └── HybridSearchServiceTest.java
│           └── rag/
│               └── search/
│                   └── HybridSearchIntegrationTest.java   // 混合检索集成测试
│
├── pom.xml                                                 // Maven 依赖管理
└── README.md
```

## 禁止事项

- 禁止：Service接口上使用 `@Transactional`
- 禁止：catch块为空
- 禁止：控制器中写原生SQL
- 禁止：类型压制


## Active Technologies
- Java 21 + Spring Boot 3.4.4, MyBatis-Plus, MySQL 8.x, JWT (sa-token) (001-note-backend-api)
- Java 21 + Spring Boot 3.4.4, Spring AI 1.0.0, MyBatis-Plus 3.5.15, Qdrant (向量数据库), MySQL 8.x (004-rag-hybrid-search)
- MySQL (结构化数据+全文索引), Qdrant (向量数据) (004-rag-hybrid-search)

## Recent Changes
- 001-note-backend-api: Added Java 21 + Spring Boot 3.4.4, MyBatis-Plus, MySQL 8.x, JWT (sa-token)
- 004-rag-hybrid-search: Added hybrid search module with vector + keyword retrieval and RRF fusion
