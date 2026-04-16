package com.ysx.agent.controller;

import com.ysx.agent.rag.config.HybridSearchConfig;
import com.ysx.agent.rag.llm.ChatWithRagResult;
import com.ysx.agent.rag.llm.YRAGAgent;
import com.ysx.agent.rag.search.PresetMode;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;

/**
 * RAG 聊天控制器
 * 提供同步和 SSE 流式两种聊天接口
 */
@RestController
@RequestMapping("/chat")
public class ChatController {

    private final YRAGAgent yragAgent;
    private final ChatMemory chatMemory;

    public ChatController(YRAGAgent yragAgent, ChatMemory chatMemory) {
        this.yragAgent = yragAgent;
        this.chatMemory = chatMemory;
    }

    /**
     * RAG 聊天 - 同步接口
     */
    @PostMapping("/rag")
    public ResponseEntity<ChatWithRagResult> chatRag(
            @RequestParam String collectionName,
            @RequestParam String message,
            @RequestParam String chatId) {
        return ResponseEntity.ok(yragAgent.doChatWithRag(collectionName, message, chatId));
    }

    /**
     * RAG 聊天 - 同步接口（支持预设配置）
     */
    @PostMapping("/rag/with-config")
    public ResponseEntity<ChatWithRagResult> chatRagWithConfig(
            @RequestParam String collectionName,
            @RequestParam String message,
            @RequestParam String chatId,
            @RequestParam(required = false) PresetMode preset) {
        HybridSearchConfig config = preset != null
                ? HybridSearchConfig.fromPreset(preset)
                : HybridSearchConfig.balanced();
        return ResponseEntity.ok(yragAgent.doChatWithRag(collectionName, message, chatId, config));
    }

    /**
     * RAG 聊天 - SSE 流式接口
     */
    @GetMapping(value = "/rag/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chatRagStream(
            @RequestParam String collectionName,
            @RequestParam String message,
            @RequestParam String chatId) {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);

        yragAgent.doChatWithRagStream(collectionName, message, chatId)
                .subscribe(
                        content -> {
                            try {
                                emitter.send(SseEmitter.event()
                                        .name("message")
                                        .data(content));
                            } catch (IOException e) {
                                emitter.completeWithError(e);
                            }
                        },
                        emitter::completeWithError,
                        emitter::complete
                );

        return emitter;
    }

    /**
     * RAG 聊天 - SSE 流式接口（支持预设配置）
     */
    @GetMapping(value = "/rag/stream/with-config", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chatRagStreamWithConfig(
            @RequestParam String collectionName,
            @RequestParam String message,
            @RequestParam String chatId,
            @RequestParam(required = false) PresetMode preset) {
        HybridSearchConfig config = preset != null
                ? HybridSearchConfig.fromPreset(preset)
                : HybridSearchConfig.balanced();

        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);

        yragAgent.doChatWithRagStream(collectionName, message, chatId, config)
                .subscribe(
                        content -> {
                            try {
                                emitter.send(SseEmitter.event()
                                        .name("message")
                                        .data(content));
                            } catch (IOException e) {
                                emitter.completeWithError(e);
                            }
                        },
                        emitter::completeWithError,
                        emitter::complete
                );

        return emitter;
    }

    /**
     * 清理会话记忆
     */
    @DeleteMapping("/memory/{chatId}")
    public ResponseEntity<Map<String, Object>> clearMemory(@PathVariable String chatId) {
        chatMemory.clear(chatId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "会话记忆已清理",
                "chatId", chatId
        ));
    }

    /**
     * 健康检查
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "ChatController"
        ));
    }
}
