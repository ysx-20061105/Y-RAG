package com.ysx.agent.rag.config;

import com.ysx.agent.rag.search.PresetMode;
import com.ysx.agent.rag.search.SearchMode;
import com.ysx.agent.rag.search.TokenizerType;

/**
 * 混合检索配置类
 * 用于控制混合检索的行为参数
 */
public class HybridSearchConfig {

    private double vectorWeight = 0.5;
    private double keywordWeight = 0.5;
    private int topK = 5;
    private double similarityThreshold = 0.5;
    private SearchMode searchMode = SearchMode.HYBRID;
    private TokenizerType tokenizerType = TokenizerType.STANDARD;

    public HybridSearchConfig() {
    }

    private HybridSearchConfig(double vectorWeight, double keywordWeight, int topK,
                               double similarityThreshold, SearchMode searchMode, TokenizerType tokenizerType) {
        this.vectorWeight = vectorWeight;
        this.keywordWeight = keywordWeight;
        this.topK = topK;
        this.similarityThreshold = similarityThreshold;
        this.searchMode = searchMode;
        this.tokenizerType = tokenizerType;
    }

    // Getters
    public double getVectorWeight() {
        return vectorWeight;
    }

    public double getKeywordWeight() {
        return keywordWeight;
    }

    public int getTopK() {
        return topK;
    }

    public double getSimilarityThreshold() {
        return similarityThreshold;
    }

    public SearchMode getSearchMode() {
        return searchMode;
    }

    public TokenizerType getTokenizerType() {
        return tokenizerType;
    }

    // Setters with validation
    public void setVectorWeight(double vectorWeight) {
        if (vectorWeight < 0.0 || vectorWeight > 1.0) {
            throw new IllegalArgumentException("vectorWeight must be between 0.0 and 1.0");
        }
        this.vectorWeight = vectorWeight;
    }

    public void setKeywordWeight(double keywordWeight) {
        if (keywordWeight < 0.0 || keywordWeight > 1.0) {
            throw new IllegalArgumentException("keywordWeight must be between 0.0 and 1.0");
        }
        this.keywordWeight = keywordWeight;
    }

    public void setTopK(int topK) {
        if (topK <= 0 || topK > 100) {
            throw new IllegalArgumentException("topK must be between 1 and 100");
        }
        this.topK = topK;
    }

    public void setSimilarityThreshold(double similarityThreshold) {
        if (similarityThreshold < 0.0 || similarityThreshold > 1.0) {
            throw new IllegalArgumentException("similarityThreshold must be between 0.0 and 1.0");
        }
        this.similarityThreshold = similarityThreshold;
    }

    public void setSearchMode(SearchMode searchMode) {
        this.searchMode = searchMode;
    }

    public void setTokenizerType(TokenizerType tokenizerType) {
        this.tokenizerType = tokenizerType;
    }

    /**
     * 验证权重配置 - 向量权重和关键词权重之和应为1.0
     */
    public void validateWeights() {
        double sum = vectorWeight + keywordWeight;
        if (Math.abs(sum - 1.0) > 0.01) {
            throw new IllegalArgumentException("vectorWeight + keywordWeight must equal 1.0, but got " + sum);
        }
    }

    // Builder
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private double vectorWeight = 0.5;
        private double keywordWeight = 0.5;
        private int topK = 5;
        private double similarityThreshold = 0.5;
        private SearchMode searchMode = SearchMode.HYBRID;
        private TokenizerType tokenizerType = TokenizerType.STANDARD;

        public Builder vectorWeight(double vectorWeight) {
            this.vectorWeight = vectorWeight;
            return this;
        }

        public Builder keywordWeight(double keywordWeight) {
            this.keywordWeight = keywordWeight;
            return this;
        }

        public Builder topK(int topK) {
            this.topK = topK;
            return this;
        }

        public Builder similarityThreshold(double similarityThreshold) {
            this.similarityThreshold = similarityThreshold;
            return this;
        }

        public Builder searchMode(SearchMode searchMode) {
            this.searchMode = searchMode;
            return this;
        }

        public Builder tokenizerType(TokenizerType tokenizerType) {
            this.tokenizerType = tokenizerType;
            return this;
        }

        public HybridSearchConfig build() {
            return new HybridSearchConfig(vectorWeight, keywordWeight, topK,
                    similarityThreshold, searchMode, tokenizerType);
        }
    }

    // Preset configurations
    public static HybridSearchConfig balanced() {
        return builder()
                .vectorWeight(0.5)
                .keywordWeight(0.5)
                .searchMode(SearchMode.HYBRID)
                .build();
    }

    public static HybridSearchConfig semanticFirst() {
        return builder()
                .vectorWeight(0.8)
                .keywordWeight(0.2)
                .searchMode(SearchMode.HYBRID)
                .build();
    }

    public static HybridSearchConfig keywordFirst() {
        return builder()
                .vectorWeight(0.2)
                .keywordWeight(0.8)
                .searchMode(SearchMode.HYBRID)
                .build();
    }

    public static HybridSearchConfig fromPreset(PresetMode preset) {
        return switch (preset) {
            case BALANCED -> balanced();
            case SEMANTIC_FIRST -> semanticFirst();
            case KEYWORD_FIRST -> keywordFirst();
        };
    }
}
