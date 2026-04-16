-- MySQL dump 10.13  Distrib 8.0.39, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: yrag
-- ------------------------------------------------------
-- Server version	8.0.39

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `knowledge_base`
--

DROP TABLE IF EXISTS `knowledge_base`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `knowledge_base` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '知识库ID',
  `user_id` bigint NOT NULL COMMENT '所属用户ID',
  `name` varchar(255) NOT NULL COMMENT '知识库名称',
  `description` varchar(1000) DEFAULT NULL COMMENT '知识库描述',
  `category` varchar(128) DEFAULT NULL COMMENT '知识库分类',
  `tags` varchar(1024) DEFAULT NULL COMMENT '标签，逗号分隔',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '1启用 0禁用',
  `created_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `deleted_at` datetime(3) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_kb_owner_name` (`user_id`,`name`),
  KEY `idx_kb_owner_created` (`user_id`,`created_at` DESC),
  KEY `idx_kb_owner_status_deleted_updated` (`user_id`,`status`,`deleted_at`,`updated_at`,`id`),
  CONSTRAINT `fk_kb_owner_user` FOREIGN KEY (`user_id`) REFERENCES `user_account` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `knowledge_base`
--

LOCK TABLES `knowledge_base` WRITE;
/*!40000 ALTER TABLE `knowledge_base` DISABLE KEYS */;
INSERT INTO `knowledge_base` VALUES (3,2,'java','java','java','java',1,'2026-04-05 22:58:22.004','2026-04-05 22:58:22.004',NULL);
/*!40000 ALTER TABLE `knowledge_base` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `note`
--

DROP TABLE IF EXISTS `note`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `note` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '笔记ID',
  `kb_id` bigint NOT NULL COMMENT '知识库ID',
  `title` varchar(255) NOT NULL COMMENT '标题',
  `content` longtext NOT NULL COMMENT '纯Markdown内容',
  `content_bytes` int NOT NULL COMMENT '内容字节数(UTF-8)',
  `summary` varchar(500) DEFAULT NULL COMMENT '列表摘要，可由后端异步生成',
  `created_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`),
  KEY `idx_note_kb_updated` (`kb_id`,`updated_at` DESC,`id` DESC),
  KEY `idx_note_kb_created` (`kb_id`,`created_at` DESC,`id` DESC),
  FULLTEXT KEY `ft_note_title_content` (`title`,`content`),
  CONSTRAINT `fk_note_kb` FOREIGN KEY (`kb_id`) REFERENCES `knowledge_base` (`id`) ON DELETE CASCADE,
  CONSTRAINT `ck_note_size` CHECK ((`content_bytes` <= 10485760))
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `note`
--

LOCK TABLES `note` WRITE;
/*!40000 ALTER TABLE `note` DISABLE KEYS */;
INSERT INTO `note` VALUES (1,3,'包装类型与基本类型','**基本类型**：int、long、float、double、char、byte、boolean、short\n\n**包装类型**：Integer、Long、Float、Double、Character、Byte、Boolean、Short\n\n**区别**：\n\n1. 性能：\n    1. 基本类型：占用内存小，效率高\n    2. 包装类型：涉及内存分配和垃圾回收，性能相对较低\n2. 比较方式：\n    1. 基本类型：使用==，直接比较数值\n    2. 包装类型：==比较的是对象的内存地址，equals 比较的是值。\n3. 默认值：\n    1. 基本类型：默认值是 0，false 等。\n    2. 包装类型：默认值为 null\n4. 初始化：\n    1. 基本类型：直接赋值\n    2. 包装类型：new\n5. 存储方式：\n    1. 基本类型：局部变量在栈上，成员变量在堆中。\n    2. 包装类型：保存在堆上。\n\n\n\n拆箱与装箱：\n\n装修：基本类型自动转换为包装类型对象\n\n拆箱：包装类型对象自动转换为基本类型的值\n\n\n\n## 自动装箱与自动拆箱\n### 底层实现\n自动装箱和拆箱是通过调用包装类型的 valueOf()和 xxxValue()方法实现的。\n\n+ 自动装箱调用：Integer.valueOf(int i)\n+ 自动拆箱调用：Integer.intValue()\n\n### 注意点\n#### 性能\n自动装箱和拆箱虽然简化了编码，但在频繁使用的场景，可能导致性能开销，尤其是在循环中频繁发生装箱或拆箱时，容易引入不必要的对象创建和垃圾回收。\n\n所以尽量避免在性能敏感的代码中频繁使用自动装箱和拆箱。\n\n#### NullPointerException\n进行拆箱操作时，如果包装类型对象为 null，会抛出 NPE 错误\n',1599,NULL,'2026-04-05 22:59:52.693','2026-04-05 22:59:52.693'),(3,3,'aaaaaaaaaaaaaa','<h1>test</h1><p>111</p><p></p>',30,NULL,'2026-04-06 21:15:21.267','2026-04-06 21:15:21.267');
/*!40000 ALTER TABLE `note` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `note_version`
--

DROP TABLE IF EXISTS `note_version`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `note_version` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `note_id` bigint NOT NULL,
  `version_no` int NOT NULL,
  `title` varchar(255) NOT NULL,
  `content` longtext NOT NULL,
  `content_bytes` int NOT NULL,
  `created_by` bigint NOT NULL COMMENT '操作人用户ID',
  `created_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_note_version` (`note_id`,`version_no`),
  KEY `fk_note_version_user` (`created_by`),
  KEY `idx_note_version_note_created` (`note_id`,`created_at` DESC),
  CONSTRAINT `fk_note_version_note` FOREIGN KEY (`note_id`) REFERENCES `note` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_note_version_user` FOREIGN KEY (`created_by`) REFERENCES `user_account` (`id`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `note_version`
--

LOCK TABLES `note_version` WRITE;
/*!40000 ALTER TABLE `note_version` DISABLE KEYS */;
/*!40000 ALTER TABLE `note_version` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `rag_chunk`
--

DROP TABLE IF EXISTS `rag_chunk`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `rag_chunk` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `rag_document_id` bigint NOT NULL,
  `chunk_no` int NOT NULL,
  `chunk_text` mediumtext NOT NULL,
  `token_count` int NOT NULL,
  `start_offset` int NOT NULL,
  `end_offset` int NOT NULL,
  `metadata_json` json DEFAULT NULL COMMENT '标题层级/代码块语言/来源段落等',
  `created_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_chunk_doc_no` (`rag_document_id`,`chunk_no`),
  KEY `idx_chunk_doc` (`rag_document_id`,`chunk_no`),
  CONSTRAINT `fk_chunk_doc` FOREIGN KEY (`rag_document_id`) REFERENCES `rag_document` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `rag_chunk`
--

LOCK TABLES `rag_chunk` WRITE;
/*!40000 ALTER TABLE `rag_chunk` DISABLE KEYS */;
/*!40000 ALTER TABLE `rag_chunk` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `rag_chunk_vector_ref`
--

DROP TABLE IF EXISTS `rag_chunk_vector_ref`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `rag_chunk_vector_ref` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `chunk_id` bigint NOT NULL,
  `embedding_model` varchar(128) NOT NULL,
  `embedding_dim` int NOT NULL,
  `vector_store` varchar(32) NOT NULL COMMENT 'milvus|pgvector|es',
  `vector_id` varchar(128) NOT NULL COMMENT '向量库主键/外部引用ID',
  `index_status` varchar(32) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING|SUCCESS|FAILED',
  `created_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_vector_chunk_model` (`chunk_id`,`embedding_model`),
  KEY `idx_vector_store_id` (`vector_store`,`vector_id`),
  KEY `idx_vector_status` (`index_status`,`updated_at` DESC),
  CONSTRAINT `fk_vector_chunk` FOREIGN KEY (`chunk_id`) REFERENCES `rag_chunk` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `rag_chunk_vector_ref`
--

LOCK TABLES `rag_chunk_vector_ref` WRITE;
/*!40000 ALTER TABLE `rag_chunk_vector_ref` DISABLE KEYS */;
/*!40000 ALTER TABLE `rag_chunk_vector_ref` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `rag_document`
--

DROP TABLE IF EXISTS `rag_document`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `rag_document` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `kb_id` bigint NOT NULL,
  `note_id` bigint NOT NULL,
  `source_version_id` bigint DEFAULT NULL COMMENT '对应note_version.id',
  `source_type` varchar(32) NOT NULL DEFAULT 'NOTE_MD',
  `parse_status` varchar(32) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING|RUNNING|SUCCESS|FAILED',
  `chunk_count` int NOT NULL DEFAULT '0',
  `token_count` int NOT NULL DEFAULT '0',
  `content_hash` char(64) NOT NULL COMMENT 'SHA-256, 用于幂等去重',
  `created_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_rag_doc_note_hash` (`note_id`,`content_hash`),
  KEY `idx_rag_doc_kb_status` (`kb_id`,`parse_status`,`updated_at` DESC),
  KEY `idx_rag_doc_note` (`note_id`,`created_at` DESC),
  CONSTRAINT `fk_rag_doc_kb` FOREIGN KEY (`kb_id`) REFERENCES `knowledge_base` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_rag_doc_note` FOREIGN KEY (`note_id`) REFERENCES `note` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `rag_document`
--

LOCK TABLES `rag_document` WRITE;
/*!40000 ALTER TABLE `rag_document` DISABLE KEYS */;
/*!40000 ALTER TABLE `rag_document` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `rag_retrieval_log`
--

DROP TABLE IF EXISTS `rag_retrieval_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `rag_retrieval_log` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `kb_id` bigint NOT NULL,
  `actor_user_id` bigint NOT NULL COMMENT '检索发起用户ID',
  `query_text` text NOT NULL,
  `rewritten_query` text,
  `top_k` int NOT NULL,
  `retrieved_chunk_ids` json NOT NULL COMMENT '召回chunk id列表',
  `latency_ms` int NOT NULL,
  `created_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`),
  KEY `idx_retrieval_kb_created` (`kb_id`,`created_at` DESC),
  KEY `idx_retrieval_actor_created` (`actor_user_id`,`created_at` DESC),
  CONSTRAINT `fk_retrieval_kb` FOREIGN KEY (`kb_id`) REFERENCES `knowledge_base` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_retrieval_user` FOREIGN KEY (`actor_user_id`) REFERENCES `user_account` (`id`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `rag_retrieval_log`
--

LOCK TABLES `rag_retrieval_log` WRITE;
/*!40000 ALTER TABLE `rag_retrieval_log` DISABLE KEYS */;
/*!40000 ALTER TABLE `rag_retrieval_log` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_account`
--

DROP TABLE IF EXISTS `user_account`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_account` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(64) NOT NULL COMMENT '登录用户名',
  `email` varchar(255) DEFAULT NULL COMMENT '邮箱，可用于登录/找回',
  `password_hash` varchar(255) NOT NULL COMMENT '密码哈希(BCrypt/Argon2)',
  `nickname` varchar(64) DEFAULT NULL COMMENT '展示昵称',
  `avatar_url` varchar(512) DEFAULT NULL COMMENT '头像URL',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '1正常 0禁用',
  `last_login_at` datetime(3) DEFAULT NULL COMMENT '最后登录时间',
  `created_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `deleted_at` datetime(3) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_username` (`username`),
  UNIQUE KEY `uk_user_email` (`email`),
  KEY `idx_user_status_created` (`status`,`created_at` DESC)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_account`
--

LOCK TABLES `user_account` WRITE;
/*!40000 ALTER TABLE `user_account` DISABLE KEYS */;
INSERT INTO `user_account` VALUES (2,'17303883112',NULL,'$2a$10$QoT/u8YX1yeg3s2P7gcPOOu8vwFNEnmc5rMgJ0D7CQVxpsG9QihO.',NULL,NULL,0,NULL,'2026-04-05 21:52:42.244','2026-04-05 21:52:42.244',NULL);
/*!40000 ALTER TABLE `user_account` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-04-16 21:13:48
