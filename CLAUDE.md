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
│   │   │       └── example
│   │   │           └── rag
│   │   │               ├── RagApplication.java             // 启动类
│   │   │               │
│   │   │               ├── config                          // [配置层]
│   │   │               │   ├── AiConfig.java               // Spring AI Alibaba 配置 (ChatClient, Embedding)
│   │   │               │   ├── QdrantConfig.java           // Qdrant 向量库连接配置
│   │   │               │   └── CorsConfig.java             // 跨域配置 (供 Vue 访问)
│   │   │               │
│   │   │               ├── controller                      // [接口层] - 处理 HTTP 请求
│   │   │               │   ├── NoteController.java         // 笔记 CRUD 接口
│   │   │               │   ├── KnowledgeBaseController.java// 知识库管理接口
│   │   │               │   └── ChatController.java         // 聊天与 SSE 流式问答接口
│   │   │               │
│   │   │               ├── service                         // [业务层] - 核心逻辑
│   │   │               │   ├── impl          
│   │   │               │   ├── NoteService.java            // 笔记业务逻辑
│   │   │               │   ├── RagService.java             // RAG 核心编排 (索引、检索、Prompt构建)
│   │   │               │   └── ChatService.java            // 对话流管理
│   │   │               │
│   │   │               ├── mapper                      // [数据访问层]
│   │   │               │   ├── NoteMapper.java
│   │   │               │   └── KnowledgeBaseMapper.java
│   │   │               │
│   │   │               ├── domain                          // [领域实体] - 数据库表映射
│   │   │               │   ├── Note.java                   // 笔记实体 (含 LONGTEXT 内容)
│   │   │               │   └── KnowledgeBase.java          // 知识库实体
│   │   │               │
│   │   │               └── dto                             // [数据传输对象]
│   │   │                   ├── ChatRequest.java            // 聊天请求参数
│   │   │                   └── NoteDTO.java                // 笔记传输对象
│   │   │
│   │   └── resources
│   │       ├── application.yml                             // 全局配置 (DashScope Key, Qdrant 地址, MySQL)
│   │       └── application-dev.yml                         // 开发环境配置
│   │
│   └── test                                                // 单元测试
│       └── java
│           └── com
│               └── example
│                   └── rag
│                       └── RagApplicationTests.java
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

## Recent Changes
- 001-note-backend-api: Added Java 21 + Spring Boot 3.4.4, MyBatis-Plus, MySQL 8.x, JWT (sa-token)
