package com.ysx.agent.controller;

import com.ysx.agent.domain.RagRetrievalLog;
import com.ysx.agent.rag.config.HybridSearchConfig;
import com.ysx.agent.rag.search.PresetMode;
import com.ysx.agent.service.HybridSearchService;
import com.ysx.agent.service.RagRetrievalLogService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * RAG检索控制器
 * 提供检索配置管理和检索日志查看接口
 */
@RestController
@RequestMapping("/rag/retrieval")
@Validated
public class RagRetrievalController {

    private final HybridSearchService hybridSearchService;
    private final RagRetrievalLogService retrievalLogService;

    public RagRetrievalController(HybridSearchService hybridSearchService,
                                  RagRetrievalLogService retrievalLogService) {
        this.hybridSearchService = hybridSearchService;
        this.retrievalLogService = retrievalLogService;
    }

    /**
     * 获取当前检索配置
     */
    @GetMapping("/config")
    public ResponseEntity<HybridSearchConfig> getConfig(
            @RequestParam(required = false) PresetMode preset) {
        HybridSearchConfig config;
        if (preset != null) {
            config = HybridSearchConfig.fromPreset(preset);
        } else {
            config = HybridSearchConfig.balanced();
        }
        return ResponseEntity.ok(config);
    }

    /**
     * 获取预设配置
     */
    @GetMapping("/config/presets")
    public ResponseEntity<Map<String, HybridSearchConfig>> getPresets() {
        return ResponseEntity.ok(Map.of(
                "balanced", HybridSearchConfig.balanced(),
                "semanticFirst", HybridSearchConfig.semanticFirst(),
                "keywordFirst", HybridSearchConfig.keywordFirst()
        ));
    }

    /**
     * 验证检索配置参数
     */
    @PostMapping("/config/validate")
    public ResponseEntity<Map<String, Object>> validateConfig(
            @RequestBody @Validated HybridSearchConfig config) {
        try {
            config.validateWeights();
            return ResponseEntity.ok(Map.of(
                    "valid", true,
                    "message", "配置验证通过"
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "valid", false,
                    "message", e.getMessage()
            ));
        }
    }

    /**
     * 获取最近的检索日志
     */
    @GetMapping("/logs")
    public ResponseEntity<List<RagRetrievalLog>> getRecentLogs(
            @RequestParam(defaultValue = "100") @Min(1) @Max(1000) int limit) {
        return ResponseEntity.ok(retrievalLogService.getRecentLogs(limit));
    }

    /**
     * 根据知识库ID获取检索日志
     */
    @GetMapping("/logs/kb/{kbId}")
    public ResponseEntity<List<RagRetrievalLog>> getLogsByKbId(
            @PathVariable Long kbId,
            @RequestParam(defaultValue = "100") @Min(1) @Max(1000) int limit) {
        return ResponseEntity.ok(retrievalLogService.getLogsByKbId(kbId, limit));
    }

    /**
     * 健康检查接口
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "HybridSearchService"
        ));
    }

    /**
     * 全局异常处理 - 处理验证异常
     */
    @ExceptionHandler(jakarta.validation.ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(
            jakarta.validation.ConstraintViolationException ex) {
        return ResponseEntity.badRequest().body(Map.of(
                "valid", false,
                "message", "参数验证失败: " + ex.getMessage()
        ));
    }

    /**
     * 全局异常处理 - 处理非法参数异常
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(
            IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(Map.of(
                "valid", false,
                "message", ex.getMessage()
        ));
    }
}
