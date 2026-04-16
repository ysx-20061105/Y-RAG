package com.ysx.agent.config;

import com.ysx.agent.rag.memory.FileBasedChatMemory;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ChatMemory 配置
 * 使用 FileBasedChatMemory 实现对话记忆持久化
 */
@Configuration
public class ChatMemoryConfig {

    @Value("${rag.chat-memory.dir:data/chat-memory}")
    private String chatMemoryDir;

    @Bean
    public ChatMemory chatMemory() {
        return new FileBasedChatMemory(chatMemoryDir);
    }
}
