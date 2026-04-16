package com.ysx.agent.rag.search;

import com.ysx.agent.rag.config.HybridSearchConfig;
import org.springframework.ai.document.Document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 检索结果类
 * 包含检索到的文档列表、评分、耗时等信息
 */
public class RetrievalResult {

    private List<Document> documents;
    private Map<String, Double> scores;
    private Long latencyMs;
    private String searchType;
    private HybridSearchConfig configUsed;

    public RetrievalResult() {
        this.documents = new ArrayList<>();
        this.scores = new HashMap<>();
    }

    public RetrievalResult(List<Document> documents, Map<String, Double> scores,
                          Long latencyMs, String searchType, HybridSearchConfig configUsed) {
        this.documents = documents != null ? documents : new ArrayList<>();
        this.scores = scores != null ? scores : new HashMap<>();
        this.latencyMs = latencyMs;
        this.searchType = searchType;
        this.configUsed = configUsed;
    }

    // Getters
    public List<Document> getDocuments() {
        return documents;
    }

    public Map<String, Double> getScores() {
        return scores;
    }

    public Long getLatencyMs() {
        return latencyMs;
    }

    public String getSearchType() {
        return searchType;
    }

    public HybridSearchConfig getConfigUsed() {
        return configUsed;
    }

    // Setters
    public void setDocuments(List<Document> documents) {
        this.documents = documents;
    }

    public void setScores(Map<String, Double> scores) {
        this.scores = scores;
    }

    public void setLatencyMs(Long latencyMs) {
        this.latencyMs = latencyMs;
    }

    public void setSearchType(String searchType) {
        this.searchType = searchType;
    }

    public void setConfigUsed(HybridSearchConfig configUsed) {
        this.configUsed = configUsed;
    }

    /**
     * 获取返回文档数量
     */
    public int getResultCount() {
        return documents != null ? documents.size() : 0;
    }

    /**
     * 判断是否有结果
     */
    public boolean hasResults() {
        return documents != null && !documents.isEmpty();
    }

    // Builder
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private List<Document> documents = new ArrayList<>();
        private Map<String, Double> scores = new HashMap<>();
        private Long latencyMs;
        private String searchType;
        private HybridSearchConfig configUsed;

        public Builder documents(List<Document> documents) {
            this.documents = documents;
            return this;
        }

        public Builder scores(Map<String, Double> scores) {
            this.scores = scores;
            return this;
        }

        public Builder latencyMs(Long latencyMs) {
            this.latencyMs = latencyMs;
            return this;
        }

        public Builder searchType(String searchType) {
            this.searchType = searchType;
            return this;
        }

        public Builder configUsed(HybridSearchConfig configUsed) {
            this.configUsed = configUsed;
            return this;
        }

        public RetrievalResult build() {
            return new RetrievalResult(documents, scores, latencyMs, searchType, configUsed);
        }
    }
}
