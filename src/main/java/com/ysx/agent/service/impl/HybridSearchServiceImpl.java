package com.ysx.agent.service.impl;

import com.ysx.agent.rag.config.HybridSearchConfig;
import com.ysx.agent.rag.search.*;
import com.ysx.agent.service.HybridSearchService;
import com.ysx.agent.service.RagRetrievalLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * 混合检索服务实现类
 * 同时执行向量检索和关键词检索，并融合结果
 */
@Service
public class HybridSearchServiceImpl implements HybridSearchService {

    private static final Logger log = LoggerFactory.getLogger(HybridSearchServiceImpl.class);

    private final VectorRetriever vectorRetriever;
    private final KeywordRetriever keywordRetriever;
    private final HybridDocumentMerger documentMerger;
    private final RagRetrievalLogService retrievalLogService;

    public HybridSearchServiceImpl(VectorRetriever vectorRetriever,
                                  KeywordRetriever keywordRetriever,
                                  HybridDocumentMerger documentMerger,
                                  RagRetrievalLogService retrievalLogService) {
        this.vectorRetriever = vectorRetriever;
        this.keywordRetriever = keywordRetriever;
        this.documentMerger = documentMerger;
        this.retrievalLogService = retrievalLogService;
    }

    @Override
    public RetrievalResult search(String query, String collectionName, HybridSearchConfig config) {
        if (config == null) {
            config = HybridSearchConfig.balanced();
        }

        long startTime = System.currentTimeMillis();
        log.info("Starting hybrid search: query={}, collection={}, config={}", query, collectionName, config);

        try {
            List<Document> vectorResults = Collections.emptyList();
            List<Document> keywordResults = Collections.emptyList();
            String actualSearchType = config.getSearchMode().name();

            // 根据检索模式执行不同的检索
            switch (config.getSearchMode()) {
                case HYBRID:
                    HybridSearchOutput output = executeHybridSearch(query, collectionName, config);
                    vectorResults = output.vectorResults();
                    keywordResults = output.keywordResults();
                    actualSearchType = output.searchType();
                    break;
                case VECTOR_ONLY:
                    vectorResults = executeVectorSearch(query, collectionName, config);
                    break;
                case KEYWORD_ONLY:
                    keywordResults = executeKeywordSearch(query, collectionName, config);
                    break;
            }

            // 融合结果
            List<Document> mergedResults = documentMerger.merge(vectorResults, keywordResults, config);

            long latencyMs = System.currentTimeMillis() - startTime;

            log.info("Hybrid search completed: resultCount={}, latencyMs={}, searchType={}",
                    mergedResults.size(), latencyMs, actualSearchType);

            // 异步记录检索日志
            logRetrieval(collectionName, query, actualSearchType, config, mergedResults.size(), latencyMs);

            return RetrievalResult.builder()
                    .documents(mergedResults)
                    .latencyMs(latencyMs)
                    .searchType(actualSearchType)
                    .configUsed(config)
                    .build();

        } catch (Exception e) {
            log.error("Hybrid search failed: {}", e.getMessage(), e);
            return handleSearchFailure(e, config, startTime);
        }
    }

    private void logRetrieval(String collectionName, String query, String searchType,
                             HybridSearchConfig config, int resultCount, long latencyMs) {
        try {
            // 从collectionName提取kbId（假设collectionName格式为kb_{kbId}_xxx）
            Long kbId = extractKbIdFromCollection(collectionName);
            retrievalLogService.logRetrievalAsync(
                    kbId,
                    null, // userId - 可从上下文获取
                    query,
                    query, // rewrittenQuery - 已在调用前完成重写
                    config,
                    resultCount,
                    latencyMs
            );
        } catch (Exception e) {
            log.warn("Failed to log retrieval: {}", e.getMessage());
        }
    }

    private Long extractKbIdFromCollection(String collectionName) {
        // 简单解析：假设collectionName格式为 "kb_{id}" 或类似格式
        try {
            if (collectionName != null && collectionName.startsWith("kb_")) {
                return Long.parseLong(collectionName.substring(3));
            }
        } catch (NumberFormatException e) {
            log.debug("Cannot parse kbId from collectionName: {}", collectionName);
        }
        return null;
    }

    private HybridSearchOutput executeHybridSearch(String query, String collectionName, HybridSearchConfig config) {
        List<Document> vectorResults = Collections.emptyList();
        List<Document> keywordResults = Collections.emptyList();
        boolean vectorFailed = false;
        boolean keywordFailed = false;
        String actualSearchType = "HYBRID";

        // 执行向量检索
        try {
            vectorResults = executeVectorSearch(query, collectionName, config);
        } catch (Exception e) {
            log.warn("Vector search failed, falling back to keyword only: {}", e.getMessage());
            vectorFailed = true;
        }

        // 执行关键词检索
        try {
            keywordResults = executeKeywordSearch(query, collectionName, config);
        } catch (Exception e) {
            log.warn("Keyword search failed, falling back to vector only: {}", e.getMessage());
            keywordFailed = true;
        }

        // 处理降级情况
        if (vectorFailed && keywordFailed) {
            actualSearchType = "FAILED";
        } else if (vectorFailed) {
            actualSearchType = "KEYWORD_ONLY";
            vectorResults = Collections.emptyList();
        } else if (keywordFailed) {
            actualSearchType = "VECTOR_ONLY";
            keywordResults = Collections.emptyList();
        }

        return new HybridSearchOutput(vectorResults, keywordResults, actualSearchType);
    }

    private List<Document> executeVectorSearch(String query, String collectionName, HybridSearchConfig config) {
        return vectorRetriever.retrieve(
                query,
                collectionName,
                config.getTopK(),
                config.getSimilarityThreshold()
        );
    }

    private List<Document> executeKeywordSearch(String query, String collectionName, HybridSearchConfig config) {
        return keywordRetriever.retrieve(
                query,
                collectionName,
                config.getTopK(),
                config.getTokenizerType()
        );
    }

    private RetrievalResult handleSearchFailure(Exception e, HybridSearchConfig config, long startTime) {
        long latencyMs = System.currentTimeMillis() - startTime;

        return RetrievalResult.builder()
                .documents(Collections.emptyList())
                .latencyMs(latencyMs)
                .searchType("ERROR")
                .configUsed(config)
                .build();
    }

    // 内部记录类型用于封装混合搜索结果
    private record HybridSearchOutput(List<Document> vectorResults,
                                      List<Document> keywordResults,
                                      String searchType) {}
}
